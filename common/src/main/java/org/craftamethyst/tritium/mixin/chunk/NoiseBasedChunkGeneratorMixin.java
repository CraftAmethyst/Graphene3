package org.craftamethyst.tritium.mixin.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin extends ChunkGenerator {

    @Unique
    private static final double[] INV_CELL_SIZE = new double[17];

    static {
        for (int i = 1; i <= 16; i++) {
            INV_CELL_SIZE[i] = 1.0 / i;
        }
    }

    @Unique
    private int tritium_cellWidth;
    @Unique
    private int tritium_cellHeight;

    protected NoiseBasedChunkGeneratorMixin(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Inject(method = "doFill", at = @At("HEAD"))
    private void tritium_onDoFillHead(Blender pBlender, StructureManager pStructureManager,
                                      RandomState pRandom, ChunkAccess pChunk,
                                      int pMinCellY, int pCellCountY, CallbackInfoReturnable<ChunkAccess> cir) {
    }

    @Inject(method = "doFill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseChunk;initializeForFirstCellX()V"))
    private void tritium_captureCellSizes(Blender pBlender, StructureManager pStructureManager, RandomState pRandom,
                                          ChunkAccess pChunk, int pMinCellY, int pCellCountY,
                                          CallbackInfoReturnable<ChunkAccess> cir,
                                          @Local NoiseChunk noisechunk) {
        NoiseChunkAccessor accessor = (NoiseChunkAccessor) noisechunk;
        this.tritium_cellWidth = accessor.tritium_getCellWidth();
        this.tritium_cellHeight = accessor.tritium_getCellHeight();
    }

    @ModifyVariable(method = "doFill", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private double tritium_optimizeD0(double original) {
        return ((int)(original * this.tritium_cellHeight)) * INV_CELL_SIZE[this.tritium_cellHeight];
    }

    @ModifyVariable(method = "doFill", at = @At(value = "STORE", ordinal = 1), ordinal = 1)
    private double tritium_optimizeD1(double original) {
        return ((int)(original * this.tritium_cellWidth)) * INV_CELL_SIZE[this.tritium_cellWidth];
    }
}