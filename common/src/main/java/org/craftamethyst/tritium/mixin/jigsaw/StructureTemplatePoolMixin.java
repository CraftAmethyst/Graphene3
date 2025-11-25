package org.craftamethyst.tritium.mixin.jigsaw;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public class StructureTemplatePoolMixin {

    @Shadow @Final private List<Pair<StructurePoolElement, Integer>> rawTemplates;

    @Unique private final Object tritium$lock = new Object();
    @Unique private volatile boolean tritium$initialized = false;
    @Unique private StructurePoolElement[] tritium$elements;
    @Unique private int[] tritium$prefix;
    @Unique private int tritium$total;

    @Inject(method = "getShuffledTemplates",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void tritium$fastWeightedSample(RandomSource random,
                                            CallbackInfoReturnable<List<StructurePoolElement>> cir) {
        try {
            if (!tritium$initialized) {
                synchronized (tritium$lock) {
                    if (!tritium$initialized) {
                        tritium$init();
                        tritium$initialized = true;
                    }
                }
            }

            if (tritium$elements == null || tritium$elements.length == 0 || tritium$total <= 0) {
                return;
            }

            int point = random.nextInt(tritium$total);
            int idx = IntArrays.binarySearch(tritium$prefix, point);
            if (idx < 0) idx = -idx - 1;

            if (idx < tritium$elements.length && tritium$elements[idx] != null) {
                cir.setReturnValue(java.util.Collections.singletonList(tritium$elements[idx]));
            }
        } catch (Exception e) {
            System.err.println("Tritium StructureTemplatePool failed, falling back to vanilla: " + e.getMessage());
        }
    }

    @Unique
    private void tritium$init() {
        try {
            int n = rawTemplates.size();
            tritium$elements = new StructurePoolElement[n];
            tritium$prefix = new int[n];
            int cursor = 0;
            int i = 0;

            for (Pair<StructurePoolElement, Integer> pair : rawTemplates) {
                StructurePoolElement ele = pair.getFirst();
                int w = pair.getSecond();
                if (w <= 0 || ele == null) continue;
                tritium$elements[i] = ele;
                cursor += w;
                tritium$prefix[i] = cursor;
                i++;
            }
            if (i == 0) {
                tritium$elements = null;
                tritium$prefix = null;
                tritium$total = 0;
                return;
            }

            if (i < n) {
                tritium$elements = java.util.Arrays.copyOf(tritium$elements, i);
                tritium$prefix = java.util.Arrays.copyOf(tritium$prefix, i);
            }
            tritium$total = cursor;
        } catch (Exception e) {
            tritium$elements = null;
            tritium$prefix = null;
            tritium$total = 0;
            System.err.println("Tritium StructureTemplatePoolMixin init failed: " + e.getMessage());
        }
    }
}