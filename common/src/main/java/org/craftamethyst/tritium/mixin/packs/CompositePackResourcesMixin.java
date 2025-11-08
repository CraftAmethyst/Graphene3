package org.craftamethyst.tritium.mixin.packs;

import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CompositePackResources.class)
class CompositePackResourcesMixin {

    @Unique
    private PackResources tritium$primary;

    @Unique
    private List<PackResources> tritium$overlays;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tritium$init(PackResources primary, List<PackResources> overlays, CallbackInfo ci) {
        this.tritium$primary = primary;
        this.tritium$overlays = overlays;
    }

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
    private void tritium$listResources(PackType type, String ns, String path,
                                       PackResources.ResourceOutput output, CallbackInfo ci) {
        // keep order: primary first, then overlays sequentially to preserve overlay semantics
        ci.cancel();
        if (tritium$primary != null) {
            tritium$primary.listResources(type, ns, path, output);
        }
        if (tritium$overlays != null && !tritium$overlays.isEmpty()) {
            for (PackResources p : tritium$overlays) {
                p.listResources(type, ns, path, output);
            }
        }
    }
}