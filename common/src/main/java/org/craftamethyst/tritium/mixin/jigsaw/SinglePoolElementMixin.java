package org.craftamethyst.tritium.mixin.jigsaw;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.craftamethyst.tritium.octree.BoxOctree;
import org.craftamethyst.tritium.util.OctreeHolder;
import org.craftamethyst.tritium.util.RotationFailMask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementMixin {

    @Shadow
    @Final
    protected Either<ResourceLocation, StructureTemplate> template;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true, require = 1)
    private void tritium$skipIfGlobalRotationsFailed(
            StructureTemplateManager manager,
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            BlockPos offset,
            BlockPos pos,
            Rotation rotation,
            BoundingBox box,
            RandomSource random,
            LiquidSettings liquidSettings,
            boolean keepJigsaws,
            CallbackInfoReturnable<Boolean> cir) {

        ResourceLocation id = this.template.left().orElse(null);
        if (id == null) return;
        int templateId = id.hashCode();
        int rotIdx = rotation.ordinal();

        if (RotationFailMask.isFullyFailed(templateId, pos.getX(), pos.getY(), pos.getZ())) {
            cir.setReturnValue(false);
            return;
        }


        BoxOctree octree = OctreeHolder.get();
        if (octree == null) return;

        StructureTemplate tpl = manager.getOrCreate(id);
        BoundingBox bb = tpl.getBoundingBox(new StructurePlaceSettings().setRotation(rotation), pos);
        AABB aabb = new AABB(bb.minX(), bb.minY(), bb.minZ(), bb.maxX(), bb.maxY(), bb.maxZ());

        if (octree.intersects(aabb)) {
            if (RotationFailMask.markFailed(templateId, pos.getX(), pos.getY(), pos.getZ(), rotIdx)) {

            }
            cir.setReturnValue(false);
            return;
        }

        VoxelShape shape = Shapes.create(aabb);
        octree.addShape(shape, this);
    }
}