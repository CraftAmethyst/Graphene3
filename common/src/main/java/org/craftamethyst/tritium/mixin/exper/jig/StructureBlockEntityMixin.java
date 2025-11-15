package org.craftamethyst.tritium.mixin.exper.jig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
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
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tritium$filterBlocksOutsideBox(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureBlockInfo> blockInfos,
            CallbackInfoReturnable<List<StructureBlockInfo>> cir) {

        BoundingBox box = settings.getBoundingBox();
        if (box == null) {
            return;
        }

        List<StructureBlockInfo> filtered = new ArrayList<>(blockInfos.size());
        for (StructureBlockInfo info : blockInfos) {
            BlockPos target = StructureTemplate.calculateRelativePosition(settings, info.pos()).offset(offset);
            if (box.isInside(target)) {
                filtered.add(info);
            }
        }

        cir.setReturnValue(filtered);
    }
}