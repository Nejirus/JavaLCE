// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerIsland extends GenLayer
{
    public GenLayerIsland(long p_i2124_1_)
    {
        super(p_i2124_1_);
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] aint = new int[areaWidth * areaHeight];

        for (int i = 0; i < areaHeight; ++i)
        {
            for (int j = 0; j < areaWidth; ++j)
            {
                this.initChunkSeed((long)(areaX + j), (long)(areaY + i));
                aint[j + i * areaWidth] = this.nextInt(10) == 0 ? 1 : 0;
            }
        }

        if (areaX > -areaWidth && areaX <= 0 && areaY > -areaHeight && areaY <= 0)
        {
            aint[-areaX + -areaY * areaWidth] = 1;
        }

        return aint;
    }
}
