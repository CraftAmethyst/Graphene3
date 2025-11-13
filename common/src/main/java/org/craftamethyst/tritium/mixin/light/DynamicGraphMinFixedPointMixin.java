package org.craftamethyst.tritium.mixin.light;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.LongPredicate;

@Mixin(DynamicGraphMinFixedPoint.class)
public abstract class DynamicGraphMinFixedPointMixin {

    @Mutable
    @Shadow @Final
    private it.unimi.dsi.fastutil.longs.Long2ByteMap computedLevels;

    @Shadow protected abstract void removeFromQueue(long pos);


    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(int levelCount, int queueSize, int mapCapacity, CallbackInfo ci) {
        int capacity = 1 << (32 - Integer.numberOfLeadingZeros(mapCapacity - 1));
        Long2ByteOpenHashMap map = new Long2ByteOpenHashMap(capacity, 0.75f);
        map.defaultReturnValue((byte) 255);
        this.computedLevels = map;
    }

    @Inject(method = "removeIf", at = @At("HEAD"), cancellable = true)
    private void removeIf(LongPredicate predicate, CallbackInfo ci) {
        ci.cancel();
        LongIterator it = computedLevels.keySet().iterator();
        while (it.hasNext()) {
            long pos = it.nextLong();
            if (predicate.test(pos)) {
                removeFromQueue(pos);
            }
        }
    }
}