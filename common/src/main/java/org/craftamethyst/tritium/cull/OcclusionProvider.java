package org.craftamethyst.tritium.cull;

import com.logisticscraft.occlusionculling.DataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class OcclusionProvider implements DataProvider {

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        // 1.20: No-op; real implementation can prefetch chunk data if needed.
        return true;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        return level.getBlockState(new BlockPos(x, y, z)).isSolidRender(level, new BlockPos(x, y, z));
    }
}
