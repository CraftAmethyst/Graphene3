package org.craftamethyst.tritium.mixin.packs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

        }
        TRITIUM$ZIP_FILE_GETTER = getter;
    }

    @Final
    @Shadow private String prefix;
    @Unique
    private volatile Map<PackType, Set<String>> tritium$namespaceCache;
    @Unique
    private volatile Map<String, List<ZipEntry>> tritium$entryCache;
    @Unique
    private volatile boolean tritium$cacheInitialized = false;
    @Unique
    private volatile ZipFile tritium$cachedZipFile;

    @Unique
    private static String tritium$extractNamespace(String directory, String name) {
        if (!name.startsWith(directory)) return "";
        int start = directory.length();
        int end = name.indexOf('/', start);
        return end == -1 ? name.substring(start) : name.substring(start, end);
    }

    @Unique
    private static Map<String, List<ZipEntry>> tritium$scanAllEntries(ZipFile zipFile) {
        if (zipFile == null || zipFile.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, List<ZipEntry>> map = new ConcurrentHashMap<>();
        zipFile.stream().forEach(entry -> {
            String name = entry.getName();
            int slash = name.lastIndexOf('/');
            if (slash > 0) {
                String dir = name.substring(0, slash + 1);
                map.computeIfAbsent(dir, k -> new ArrayList<>()).add(entry);
            }
        });
        return map;
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
                                this.tritium$entryCache = tritium$scanAllEntries(tritium$cachedZipFile);
                            } else {
                                this.tritium$entryCache = Collections.emptyMap();
                            }
                        } else {
                            this.tritium$entryCache = Collections.emptyMap();
                        }
                        tritium$namespaceCache = new ConcurrentHashMap<>();
                        tritium$cacheInitialized = true;
                    } catch (Throwable e) {
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

        if (tritium$entryCache.isEmpty()) {
            return;
        }

        Set<String> cached = tritium$namespaceCache.get(type);
        if (cached != null) {
            cir.setReturnValue(cached);
            return;
        }

        String dir = tritium$addPrefix(type.getDirectory() + "/");
        Set<String> set = ConcurrentHashMap.newKeySet();

        List<ZipEntry> entries = tritium$entryCache.get(dir);
        if (entries != null && !entries.isEmpty()) {
            entries.stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.startsWith(dir))
                    .map(name -> tritium$extractNamespace(dir, name))
                    .filter(ns -> !ns.isEmpty() && ResourceLocation.isValidNamespace(ns))
                    .forEach(set::add);
        }

        tritium$namespaceCache.put(type, set);
        cir.setReturnValue(set);
    }

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
    private void fastListResources(PackType packType, String namespace, String path,
                                   PackResources.ResourceOutput output, CallbackInfo ci) {
        tritium$ensureCacheInitialized();

        if (tritium$entryCache.isEmpty() || tritium$cachedZipFile == null) {
            return;
        }

        ci.cancel();
        String dir = tritium$addPrefix(packType.getDirectory() + "/" + namespace + "/");
        String prefixPath = dir + path + "/";

        List<ZipEntry> entries = tritium$entryCache.get(dir);
        if (entries != null) {
            entries.stream()
                    .filter(e -> !e.isDirectory())
                    .filter(e -> e.getName().startsWith(prefixPath))
                    .forEach(entry -> {
                        String full = entry.getName();
                        String rel = full.substring(dir.length());
                        ResourceLocation loc = ResourceLocation.tryBuild(namespace, rel);
                        if (loc != null) {
                            output.accept(loc, IoSupplier.create(tritium$cachedZipFile, entry));
                        }
                    });
        }
    }

    @Unique
    private String tritium$addPrefix(String path) {
        return prefix.isEmpty() ? path : prefix + "/" + path;
    }
}