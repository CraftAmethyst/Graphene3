package org.craftamethyst.tritium.mixin.packs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import org.craftamethyst.tritium.TritiumCommon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin {

    @Unique
    private static final MethodHandle TRITIUM$ZIP_FILE_GETTER;

    static {
        MethodHandle getter = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            for (java.lang.reflect.Field field : FilePackResources.class.getDeclaredFields()) {
                if (field.getType().getSimpleName().contains("ZipFileAccess")) {
                    field.setAccessible(true);
                    Class<?> zipFileAccessClass = field.getType();
                    try {
                        java.lang.reflect.Method method = zipFileAccessClass.getMethod("getOrCreateZipFile");
                        method.setAccessible(true);
                        MethodHandle fieldHandle = lookup.unreflectGetter(field);
                        MethodHandle methodHandle = lookup.unreflect(method);
                        getter = MethodHandles.filterReturnValue(fieldHandle, methodHandle);
                        break;
                    } catch (Exception e) {
                        TritiumCommon.LOG.debug("Failed to access getOrCreateZipFile via reflection", e);
                    }
                }
            }

            if (getter == null) {
                for (java.lang.reflect.Field field : FilePackResources.class.getDeclaredFields()) {
                    if (field.getType() == ZipFile.class) {
                        field.setAccessible(true);
                        getter = lookup.unreflectGetter(field);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.debug("Failed to resolve ZipFile getter for FilePackResources; optimization disabled", e);
        }
        TRITIUM$ZIP_FILE_GETTER = getter;
    }

    @Unique
    private volatile Map<PackType, Set<String>> tritium$namespaceCache;
    @Unique
    private volatile Map<String, List<ZipEntry>> tritium$entryCache;
    @Unique
    private volatile boolean tritium$cacheInitialized = false;
    @Unique
    private volatile ZipFile tritium$cachedZipFile;

    @Unique
    private static boolean tritium$isLikelyValidNamespace(String ns) {
        if (ns == null || ns.isEmpty()) return false;
        for (int i = 0; i < ns.length(); i++) {
            char c = ns.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
            if (!ok) return false;
        }
        return true;
    }

    @Unique
    private void tritium$buildCaches(ZipFile zipFile) {
        Map<String, List<ZipEntry>> nsMap = new ConcurrentHashMap<>();
        Map<PackType, Set<String>> nsSets = new ConcurrentHashMap<>();
        nsSets.put(PackType.CLIENT_RESOURCES, ConcurrentHashMap.newKeySet());
        nsSets.put(PackType.SERVER_DATA, ConcurrentHashMap.newKeySet());

        if (zipFile != null && zipFile.size() > 0) {
            String assetsBase = PackType.CLIENT_RESOURCES.getDirectory() + "/";
            String dataBase = PackType.SERVER_DATA.getDirectory() + "/";

            zipFile.stream().forEach(entry -> {
                String name = entry.getName();
                PackType pt = null;
                String base = null;
                if (name.startsWith(assetsBase)) {
                    pt = PackType.CLIENT_RESOURCES;
                    base = assetsBase;
                } else if (name.startsWith(dataBase)) {
                    pt = PackType.SERVER_DATA;
                    base = dataBase;
                } else {
                    return;
                }
                int nsStart = base.length();
                int nsEnd = name.indexOf('/', nsStart);
                if (nsEnd <= nsStart) return;
                String ns = name.substring(nsStart, nsEnd);
                if (!tritium$isLikelyValidNamespace(ns)) return;

                nsSets.get(pt).add(ns);
                String nsDir = base + ns + "/";
                nsMap.computeIfAbsent(nsDir, k -> new ArrayList<>()).add(entry);
            });
        }

        this.tritium$entryCache = nsMap;
        this.tritium$namespaceCache = nsSets;
    }

    @Unique
    private void tritium$ensureCacheInitialized() {
        if (!tritium$cacheInitialized) {
            synchronized (this) {
                if (!tritium$cacheInitialized) {
                    try {
                        if (TRITIUM$ZIP_FILE_GETTER != null) {
                            tritium$cachedZipFile = (ZipFile) TRITIUM$ZIP_FILE_GETTER.invoke(this);
                            if (tritium$cachedZipFile != null && tritium$cachedZipFile.size() > 100) {
                                tritium$buildCaches(tritium$cachedZipFile);
                            } else {
                                this.tritium$entryCache = Collections.emptyMap();
                                this.tritium$namespaceCache = new ConcurrentHashMap<>();
                            }
                        } else {
                            this.tritium$entryCache = Collections.emptyMap();
                            this.tritium$namespaceCache = new ConcurrentHashMap<>();
                        }
                        tritium$cacheInitialized = true;
                    } catch (Throwable e) {
                        TritiumCommon.LOG.warn("Failed to initialize zip cache in FilePackResourcesMixin", e);
                        tritium$entryCache = Collections.emptyMap();
                        tritium$namespaceCache = new ConcurrentHashMap<>();
                        tritium$cacheInitialized = true;
                    }
                }
            }
        }
    }

    @Inject(method = "getNamespaces", at = @At("HEAD"), cancellable = true)
    private void fastGetNamespaces(PackType type, CallbackInfoReturnable<Set<String>> cir) {
        tritium$ensureCacheInitialized();

        if (tritium$entryCache == null || tritium$entryCache.isEmpty()) {
            return;
        }

        Set<String> cached = tritium$namespaceCache.get(type);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getResources", at = @At("HEAD"), cancellable = true)
    private void fastGetResources(PackType packType, String namespace, String path,
                                  Predicate<ResourceLocation> filter, CallbackInfo ci) {
        tritium$ensureCacheInitialized();

        if (tritium$entryCache == null || tritium$entryCache.isEmpty() || tritium$cachedZipFile == null) {
            return;
        }

        String nsDir = packType.getDirectory() + "/" + namespace + "/";
        List<ZipEntry> entries = tritium$entryCache.get(nsDir);
        if (entries == null || entries.isEmpty()) {
            return;
        }

        ci.cancel();
        String prefixPath = path.isEmpty() ? nsDir : (nsDir + path + "/");

        for (ZipEntry entry : entries) {
            if (entry.isDirectory()) continue;
            String name = entry.getName();
            if (!name.startsWith(prefixPath)) continue;
            String rel = name.substring(nsDir.length());
            ResourceLocation loc = ResourceLocation.tryParse(namespace + ":" + rel);
            if (loc != null && filter.test(loc)) {
                try {
                    InputStream stream = tritium$cachedZipFile.getInputStream(entry);
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    TritiumCommon.LOG.debug("Failed to get resource stream for: {}", loc, e);
                }
            }
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void tritium$onClose(CallbackInfo ci) {
        tritium$entryCache = null;
        tritium$namespaceCache = null;
        tritium$cachedZipFile = null;
        tritium$cacheInitialized = false;
    }
}
