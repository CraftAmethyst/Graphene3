package org.craftamethyst.tritium;

public interface DataProvider {

    boolean prepareChunk(int chunkX, int chunkZ);

    boolean isOpaqueFullCube(int x, int y, int z);

    void cleanup();
}
