// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerBiomeEdge extends GenLayer
{
    public GenLayerBiomeEdge(long p_i45475_1_, GenLayer p_i45475_3_)
    {
        super(p_i45475_1_);
        this.parent = p_i45475_3_;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int[] aint = this.parent.getInts(areaX - 1, areaY - 1, areaWidth + 2, areaHeight + 2);
        int[] aint1 = new int[areaWidth * areaHeight];

        for (int i = 0; i < areaHeight; ++i)
        {
            for (int j = 0; j < areaWidth; ++j)
            {
                this.initChunkSeed((long)(j + areaX), (long)(i + areaY));
                int k = aint[j + 1 + (i + 1) * (areaWidth + 2)];

                if (!this.replaceBiomeEdgeIfNecessary(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS), Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS_EDGE)) && !this.replaceBiomeEdge(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA_ROCK), Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA)) && !this.replaceBiomeEdge(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA_CLEAR_ROCK), Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA)) && !this.replaceBiomeEdge(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.REDWOOD_TAIGA), Ps4BiomeData.getIdForBiome(Ps4BiomeData.TAIGA)))
                {
                    if (k == Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT))
                    {
                        int l1 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        int i2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        int j2 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        int k2 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                        if (l1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && i2 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && j2 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && k2 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS))
                        {
                            aint1[j + i * areaWidth] = k;
                        }
                        else
                        {
                            aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS_WITH_TREES);
                        }
                    }
                    else if (k == Ps4BiomeData.getIdForBiome(Ps4BiomeData.SWAMPLAND))
                    {
                        int l = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        int i1 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        int j1 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        int k1 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                        if (l != Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT) && i1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT) && j1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT) && k1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT) && l != Ps4BiomeData.getIdForBiome(Ps4BiomeData.COLD_TAIGA) && i1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.COLD_TAIGA) && j1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.COLD_TAIGA) && k1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.COLD_TAIGA) && l != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && i1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && j1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS) && k1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.ICE_PLAINS))
                        {
                            if (l != Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE) && k1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE) && i1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE) && j1 != Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE))
                            {
                                aint1[j + i * areaWidth] = k;
                            }
                            else
                            {
                                aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE_EDGE);
                            }
                        }
                        else
                        {
                            aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.PLAINS);
                        }
                    }
                    else
                    {
                        aint1[j + i * areaWidth] = k;
                    }
                }
            }
        }

        return aint1;
    }

    private boolean replaceBiomeEdgeIfNecessary(int[] p_151636_1_, int[] p_151636_2_, int p_151636_3_, int p_151636_4_, int p_151636_5_, int p_151636_6_, int p_151636_7_, int p_151636_8_)
    {
        if (!biomesEqualOrMesaPlateau(p_151636_6_, p_151636_7_))
        {
            return false;
        }
        else
        {
            int i = p_151636_1_[p_151636_3_ + 1 + (p_151636_4_ + 1 - 1) * (p_151636_5_ + 2)];
            int j = p_151636_1_[p_151636_3_ + 1 + 1 + (p_151636_4_ + 1) * (p_151636_5_ + 2)];
            int k = p_151636_1_[p_151636_3_ + 1 - 1 + (p_151636_4_ + 1) * (p_151636_5_ + 2)];
            int l = p_151636_1_[p_151636_3_ + 1 + (p_151636_4_ + 1 + 1) * (p_151636_5_ + 2)];

            if (this.canBiomesBeNeighbors(i, p_151636_7_) && this.canBiomesBeNeighbors(j, p_151636_7_) && this.canBiomesBeNeighbors(k, p_151636_7_) && this.canBiomesBeNeighbors(l, p_151636_7_))
            {
                p_151636_2_[p_151636_3_ + p_151636_4_ * p_151636_5_] = p_151636_6_;
            }
            else
            {
                p_151636_2_[p_151636_3_ + p_151636_4_ * p_151636_5_] = p_151636_8_;
            }

            return true;
        }
    }

    private boolean replaceBiomeEdge(int[] p_151635_1_, int[] p_151635_2_, int p_151635_3_, int p_151635_4_, int p_151635_5_, int p_151635_6_, int p_151635_7_, int p_151635_8_)
    {
        if (p_151635_6_ != p_151635_7_)
        {
            return false;
        }
        else
        {
            int i = p_151635_1_[p_151635_3_ + 1 + (p_151635_4_ + 1 - 1) * (p_151635_5_ + 2)];
            int j = p_151635_1_[p_151635_3_ + 1 + 1 + (p_151635_4_ + 1) * (p_151635_5_ + 2)];
            int k = p_151635_1_[p_151635_3_ + 1 - 1 + (p_151635_4_ + 1) * (p_151635_5_ + 2)];
            int l = p_151635_1_[p_151635_3_ + 1 + (p_151635_4_ + 1 + 1) * (p_151635_5_ + 2)];

            if (biomesEqualOrMesaPlateau(i, p_151635_7_) && biomesEqualOrMesaPlateau(j, p_151635_7_) && biomesEqualOrMesaPlateau(k, p_151635_7_) && biomesEqualOrMesaPlateau(l, p_151635_7_))
            {
                p_151635_2_[p_151635_3_ + p_151635_4_ * p_151635_5_] = p_151635_6_;
            }
            else
            {
                p_151635_2_[p_151635_3_ + p_151635_4_ * p_151635_5_] = p_151635_8_;
            }

            return true;
        }
    }

    private boolean canBiomesBeNeighbors(int p_151634_1_, int p_151634_2_)
    {
        if (biomesEqualOrMesaPlateau(p_151634_1_, p_151634_2_))
        {
            return true;
        }
        else
        {
            Ps4BiomeData biome = Ps4BiomeData.getBiome(p_151634_1_);
            Ps4BiomeData biome1 = Ps4BiomeData.getBiome(p_151634_2_);

            if (biome != null && biome1 != null)
            {
                Ps4BiomeData.TempCategory biome$tempcategory = biome.getTempCategory();
                Ps4BiomeData.TempCategory biome$tempcategory1 = biome1.getTempCategory();
                return biome$tempcategory == biome$tempcategory1 || biome$tempcategory == Ps4BiomeData.TempCategory.MEDIUM || biome$tempcategory1 == Ps4BiomeData.TempCategory.MEDIUM;
            }
            else
            {
                return false;
            }
        }
    }
}
