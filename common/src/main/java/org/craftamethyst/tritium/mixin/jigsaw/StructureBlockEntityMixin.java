package org.craftamethyst.tritium.mixin.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureBlockEntityMixin {
    @Inject(
            method = "processBlockInfos",
            at = @At("HEAD")
    )
    private static void tritium$filterBlocksOutsideBox(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureBlockInfo> blockInfos,
            CallbackInfoReturnable<List<StructureBlockInfo>> cir) {
        if (!TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations ||
                !TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering) {
            return;
        }

        BoundingBox box = settings.getBoundingBox();
        if (box == null) {
            return;
        }

        blockInfos.removeIf(info -> {
            BlockPos target = StructureTemplate.calculateRelativePosition(settings, info.pos()).offset(offset);
            return !box.isInside(target);
        });
    }

    @Inject(
            method = "processBlockInfos",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void tritium$filterAgain(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureBlockInfo> original,
            CallbackInfoReturnable<List<StructureBlockInfo>> cir) {
        if (!TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations ||
                !TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableStructureBlockFiltering) {
            return;
        }

        List<StructureBlockInfo> firstCut = cir.getReturnValue();
        if (firstCut == null) {
            return;
        }

        BoundingBox box = settings.getBoundingBox();
        if (box == null) {
            return;
        }

        List<StructureBlockInfo> secondCut = new ArrayList<>(firstCut.size());
        for (StructureBlockInfo info : firstCut) {
            if (box.isInside(info.pos())) {
                secondCut.add(info);
            }
        }
        cir.setReturnValue(secondCut);
    }
}
