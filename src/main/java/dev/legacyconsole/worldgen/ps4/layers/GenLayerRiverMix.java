// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerRiverMix extends GenLayer
{
    private final GenLayer biomePatternGeneratorChain;
    private final GenLayer riverPatternGeneratorChain;

    public GenLayerRiverMix(long p_i2129_1_, GenLayer p_i2129_3_, GenLayer p_i2129_4_)
    {
        super(p_i2129_1_);
        this.biomePatternGeneratorChain = p_i2129_3_;
        this.riverPatternGeneratorChain = p_i2129_4_;
    }

    public void initWorldGenSeed(long seed)
    {
        this.biomePatternGeneratorChain.initWorldGenSeed(seed);
        this.riverPatternGeneratorChain.initWorldGenSeed(seed);
        super.initWorldGenSeed(seed);
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] aint = this.biomePatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = this.riverPatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint2 = new int[areaWidth * areaHeight];

        for (int i = 0; i < areaWidth * areaHeight; ++i)
        {
            if (!Ps4BiomeData.isOcean(aint[i]))
            {
                if (aint1[i] == Ps4BiomeData.getIdForBiome(Ps4BiomeData.RIVER))
                {
                    if (aint[i] == Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS))
                    {
                        aint2[i] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.FROZEN_RIVER);
                    }
                    else if (aint[i] != Ps4BiomeData.getIdForBiome(Ps4BiomeData.MUSHROOM_ISLAND) && aint[i] != Ps4BiomeData.getIdForBiome(Ps4BiomeData.MUSHROOM_ISLAND_SHORE))
                    {
                        aint2[i] = aint1[i] & 255;
                    }
                    else
                    {
                        aint2[i] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.MUSHROOM_ISLAND);
                    }
                }
                else
                {
                    aint2[i] = aint[i];
                }
            }
            else
            {
                aint2[i] = aint[i];
            }
        }

        return aint2;
    }
}
