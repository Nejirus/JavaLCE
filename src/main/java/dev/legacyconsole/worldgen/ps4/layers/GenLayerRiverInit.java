// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerRiverInit extends GenLayer
{
    public GenLayerRiverInit(long p_i2127_1_, GenLayer p_i2127_3_)
    {
        super(p_i2127_1_);
        this.parent = p_i2127_3_;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] aint = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = new int[areaWidth * areaHeight];

        for (int i = 0; i < areaHeight; ++i)
        {
            for (int j = 0; j < areaWidth; ++j)
            {
                this.initChunkSeed((long)(j + areaX), (long)(i + areaY));
                aint1[j + i * areaWidth] = aint[j + i * areaWidth] > 0 ? this.nextInt(299999) + 2 : 0;
            }
        }

        return aint1;
    }
}
