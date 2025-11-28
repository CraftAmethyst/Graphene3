package org.craftamethyst.tritium.cull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class OcclusionProvider implements DataProvider {

    private final Minecraft mc = Minecraft.getInstance();
    private ClientLevel level;

    @Override
    public boolean prepareChunk(int chunkX, int chunkZ) {
        level = mc.level;
        return level != null;
    }

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        ClientLevel level = mc.level;
        if (level == null) return false;

        BlockPos pos = new BlockPos(x, y, z);
        return level.getBlockState(pos).isSolidRender(level, pos);
    }

    @Override
    public void cleanup() {
        level = null;
    }
}