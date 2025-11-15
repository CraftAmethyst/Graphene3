package org.craftamethyst.tritium.mixin.exper.jig;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplatePool.class)
public class StructureTemplatePoolMixin {

    @Shadow @Final
    private List<Pair<StructurePoolElement, Integer>> rawTemplates;

    @Inject(
            method = "getShuffledTemplates",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tritium$optimizeShuffledTemplates(RandomSource random,
                                                   CallbackInfoReturnable<List<StructurePoolElement>> cir) {


        List<StructurePoolElement> out = new ArrayList<>();
        for (Pair<StructurePoolElement, Integer> pair : this.rawTemplates) {
            StructurePoolElement element = pair.getFirst();
            int weight = pair.getSecond();
            if (weight <= 0 || out.contains(element)) continue;
            for (int i = 0; i < weight; i++) {
                out.add(element);
            }
        }

        ObjectArrayList<StructurePoolElement> oal = new ObjectArrayList<>(out);
        cir.setReturnValue(Util.shuffledCopy(oal, random));
    }
}