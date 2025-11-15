package org.craftamethyst.tritium.mixin.exper.jig;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
    @WrapOperation(
            method = "placeInWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;" +
                            "processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;" +
                            "Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;" +
                            "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;" +
                            "Ljava/util/List;)Ljava/util/List;"
            )
    )
    private List<StructureBlockInfo> tritium$filterAgain(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureBlockInfo> original,
            Operation<List<StructureBlockInfo>> originalCall) {

        List<StructureBlockInfo> firstCut = originalCall.call(level, offset, pos, settings, original);
        BoundingBox box = settings.getBoundingBox();
        if (box == null) {
            return firstCut;
        }

        List<StructureBlockInfo> secondCut = new ArrayList<>(firstCut.size());
        for (StructureBlockInfo info : firstCut) {
            if (box.isInside(info.pos())) {
                secondCut.add(info);
            }
        }
        return secondCut;
    }
}
