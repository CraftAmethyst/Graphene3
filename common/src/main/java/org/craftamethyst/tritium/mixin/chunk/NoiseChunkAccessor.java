package org.craftamethyst.tritium.mixin.chunk;

import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseChunk.class)
public interface NoiseChunkAccessor {
    @Accessor("cellWidth")
    int tritium_getCellWidth();

    @Accessor("cellHeight")
    int tritium_getCellHeight();
}