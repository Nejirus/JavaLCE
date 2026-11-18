// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerHills extends GenLayer
{
    private final GenLayer riverLayer;

    public GenLayerHills(long p_i45479_1_, GenLayer p_i45479_3_, GenLayer p_i45479_4_)
    {
        super(p_i45479_1_);
        this.parent = p_i45479_3_;
        this.riverLayer = p_i45479_4_;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] aint = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] aint1 = this.riverLayer.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] aint2 = new int[areaWidth * areaHeight];

        for (int i = 0; i < areaHeight; ++i)
        {
            for (int j = 0; j < areaWidth; ++j)
            {
                this.initChunkSeed((long)(j + areaX), (long)(i + areaY));
                int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];
                int l = aint1[j + 1 + (i + 1) * (areaWidth + 2)];
                boolean flag = (l - 2) % 29 == 0;


                Ps4BiomeData biome = Ps4BiomeData.getBiomeForId(k);
                boolean flag1 = biome != null && biome.isMutation();

                if (!Ps4BiomeData.isShallowOcean(k) && l >= 2 && (l - 2) % 29 == 1 && !flag1)
                {
                    Ps4BiomeData biome3 = Ps4BiomeData.getMutationForBiome(biome);
                    aint2[j + i * areaWidth] = biome3 == null ? k : Ps4BiomeData.getIdForBiome(biome3);
                }
                else if (this.nextInt(3) != 0 && !flag)
                {
                    aint2[j + i * areaWidth] = k;
                }
                else
                {
                    Ps4BiomeData biome1 = biome;

                    if (biome == Ps4BiomeData.DESERT)
                    {
                        biome1 = Ps4BiomeData.DESERT_HILLS;
                    }
                    else if (biome == Ps4BiomeData.FOREST)
                    {
                        biome1 = Ps4BiomeData.FOREST_HILLS;
                    }
                    else if (biome == Ps4BiomeData.BIRCH_FOREST)
                    {
                        biome1 = Ps4BiomeData.BIRCH_FOREST_HILLS;
                    }
                    else if (biome == Ps4BiomeData.ROOFED_FOREST)
                    {
                        biome1 = Ps4BiomeData.PLAINS;
                    }
                    else if (biome == Ps4BiomeData.TAIGA)
                    {
                        biome1 = Ps4BiomeData.TAIGA_HILLS;
                    }
                    else if (biome == Ps4BiomeData.REDWOOD_TAIGA)
                    {
                        biome1 = Ps4BiomeData.REDWOOD_TAIGA_HILLS;
                    }
                    else if (biome == Ps4BiomeData.COLD_TAIGA)
                    {
                        biome1 = Ps4BiomeData.COLD_TAIGA_HILLS;
                    }
                    else if (biome == Ps4BiomeData.PLAINS)
                    {
                        if (this.nextInt(3) == 0)
                        {
                            biome1 = Ps4BiomeData.FOREST_HILLS;
                        }
                        else
                        {
                            biome1 = Ps4BiomeData.FOREST;
                        }
                    }
                    else if (biome == Ps4BiomeData.ICE_PLAINS)
                    {
                        biome1 = Ps4BiomeData.ICE_MOUNTAINS;
                    }
                    else if (biome == Ps4BiomeData.BAMBOO_JUNGLE)
                    {
                        biome1 = Ps4BiomeData.BAMBOO_JUNGLE_HILLS;
                    }
                    else if (biome == Ps4BiomeData.JUNGLE)
                    {
                        biome1 = Ps4BiomeData.JUNGLE_HILLS;
                    }
                    else if (Ps4BiomeData.isShallowOcean(k))
                    {
                        biome1 = Ps4BiomeData.getBiome(k==0?24:k+1);
                    }
                    else if (biome == Ps4BiomeData.EXTREME_HILLS)
                    {
                        biome1 = Ps4BiomeData.EXTREME_HILLS_WITH_TREES;
                    }
                    else if (biome == Ps4BiomeData.SAVANNA)
                    {
                        biome1 = Ps4BiomeData.SAVANNA_PLATEAU;
                    }
                    else if (biomesEqualOrMesaPlateau(k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA_ROCK)))
                    {
                        biome1 = Ps4BiomeData.MESA;
                    }
                    else if (Ps4BiomeData.isOcean(k) && !Ps4BiomeData.isShallowOcean(k) && this.nextInt(3) == 0)
                    {
                        int i1 = this.nextInt(2);

                        if (i1 == 0)
                        {
                            biome1 = Ps4BiomeData.PLAINS;
                        }
                        else
                        {
                            biome1 = Ps4BiomeData.FOREST;
                        }
                    }

                    int j2 = Ps4BiomeData.getIdForBiome(biome1);

                    if (flag && j2 != k)
                    {
                        Ps4BiomeData biome2 = Ps4BiomeData.getMutationForBiome(biome1);
                        j2 = biome2 == null ? k : Ps4BiomeData.getIdForBiome(biome2);
                    }

                    if (j2 == k)
                    {
                        aint2[j + i * areaWidth] = k;
                    }
                    else
                    {
                        int k2 = aint[j + 1 + (i + 0) * (areaWidth + 2)];
                        int j1 = aint[j + 2 + (i + 1) * (areaWidth + 2)];
                        int k1 = aint[j + 0 + (i + 1) * (areaWidth + 2)];
                        int l1 = aint[j + 1 + (i + 2) * (areaWidth + 2)];
                        int i2 = 0;

                        if (biomesEqualOrMesaPlateau(k2, k))
                        {
                            ++i2;
                        }

                        if (biomesEqualOrMesaPlateau(j1, k))
                        {
                            ++i2;
                        }

                        if (biomesEqualOrMesaPlateau(k1, k))
                        {
                            ++i2;
                        }

                        if (biomesEqualOrMesaPlateau(l1, k))
                        {
                            ++i2;
                        }

                        if (i2 >= 3)
                        {
                            aint2[j + i * areaWidth] = j2;
                        }
                        else
                        {
                            aint2[j + i * areaWidth] = k;
                        }
                    }
                }
            }
        }

        return aint2;
    }
}
