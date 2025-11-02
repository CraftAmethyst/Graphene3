package org.craftamethyst.tritium.mixin.client.renderer.culling;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.engine.cull.CullCache;
import org.craftamethyst.tritium.helper.EntityTickHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void graphene$earlyCullingCheck(
            E entity, Frustum frustum, double camX, double camY, double camZ,
            CallbackInfoReturnable<Boolean> cir) {

        if (!TritiumConfig.get().entities.ite) return;

        TritiumClient client = TritiumClient.instance;
        if (client == null) return;
        if (client.getCullCache() != null) {
            CullCache.CullResult cached = client.getCullCache().checkEntity(entity);
            if (cached.isCached() && cached.isCulled()) {
                cir.setReturnValue(false);
                return;
            }
        }
        if (EntityTickHelper.shouldSkipTick(entity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "shouldRender",
            at = @At("TAIL"),
            cancellable = true
    )
    private <E extends Entity> void graphene$skipCulledOrTickSkippedEntity(
            E entity, Frustum frustum, double camX, double camY, double camZ,
            CallbackInfoReturnable<Boolean> cir) {
        if (!TritiumConfig.get().entities.ite) return;
        if (!cir.getReturnValue()) return;

        if (EntityTickHelper.shouldSkipTick(entity) ||
                (TritiumClient.instance != null && TritiumClient.instance.shouldSkipEntity(entity))) {
            cir.setReturnValue(false);
        }
    }
}