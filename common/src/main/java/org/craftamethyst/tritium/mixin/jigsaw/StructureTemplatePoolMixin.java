package org.craftamethyst.tritium.mixin.jigsaw;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

    @Unique private final ObjectArrayList<StructurePoolElement> tritium$flatDistinct = new ObjectArrayList<>();
    @Shadow @Final private List<Pair<StructurePoolElement, Integer>> rawTemplates;

    @Inject(method = "getShuffledTemplates", at = @At("HEAD"), cancellable = true)
    private void tritium$weightRoulette(RandomSource random,
                                        CallbackInfoReturnable<List<StructurePoolElement>> cir) {
        if (tritium$flatDistinct.isEmpty()) {
            synchronized (tritium$flatDistinct) {
                if (tritium$flatDistinct.isEmpty()) {
                    for (Pair<StructurePoolElement, Integer> pair : rawTemplates) {
                        StructurePoolElement ele = pair.getFirst();
                        int w = pair.getSecond();
                        if (w <= 0 || tritium$flatDistinct.contains(ele)) continue;
                        tritium$flatDistinct.add(ele);
                    }
                }
            }
        }
        if (!tritium$flatDistinct.isEmpty()) {
            StructurePoolElement picked = tritium$flatDistinct.get(random.nextInt(tritium$flatDistinct.size()));
            ObjectArrayList<StructurePoolElement> out = new ObjectArrayList<>();
            out.add(picked);
            cir.setReturnValue(out);
        }
    }
}