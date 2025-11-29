package org.craftamethyst.tritium.mixin.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.octree.BoxOctree;
import org.craftamethyst.tritium.util.OctreeHolder;
import org.craftamethyst.tritium.util.RotationFailMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {

    @Inject(method = "addPieces", at = @At("HEAD"), require = 1)
    private static void onAddPiecesStart(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            PoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {

        if (TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations &&
                TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection) {
            if (OctreeHolder.get() == null) {
                OctreeHolder.set(new BoxOctree(new AABB(-300, -64, -300, 300, 256, 300)));
            }
        }
    }

    @Inject(method = "addPieces", at = @At("RETURN"), require = 1)
    private static void onAddPiecesEnd(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            PoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {

        if (TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableJigsawOptimizations &&
                TritiumConfigBase.ServerPerformance.JigsawOptimizations.enableOctreeCollisionDetection) {
            OctreeHolder.clear();
            RotationFailMask.clear();
        }
    }
}