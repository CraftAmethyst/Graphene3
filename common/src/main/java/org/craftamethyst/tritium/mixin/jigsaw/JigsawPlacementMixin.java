package org.craftamethyst.tritium.mixin.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.AABB;
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

    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;I)Ljava/util/Optional;", at = @At("HEAD"), require = 1)
    private static void onAddPiecesStart(Structure.GenerationContext context, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {

        if (OctreeHolder.get() == null) {
            OctreeHolder.set(new BoxOctree(new AABB(-300, -64, -300, 300, 256, 300)));
        }

        OctreeHolder.clear();
        RotationFailMask.clear();
    }

    /*@Inject(method = "addPieces", at = @At("RETURN"), require = 1)
    private static void onAddPiecesEnd(
            Structure.GenerationContext context,
            net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool> startPool,
            Optional<net.minecraft.resources.ResourceLocation> startJigsawName,
            int maxDepth,
            net.minecraft.core.BlockPos pos,
            boolean useExpansionHack,
            Optional<net.minecraft.world.level.levelgen.Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup aliasLookup,
            net.minecraft.world.level.levelgen.structure.pools.DimensionPadding dimensionPadding,
            net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings liquidSettings,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {


    }*/
}