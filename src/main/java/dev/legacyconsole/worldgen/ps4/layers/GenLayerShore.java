// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerShore extends GenLayer
{
    public GenLayerShore(long p_i2130_1_, GenLayer p_i2130_3_)
    {
        super(p_i2130_1_);
        this.parent = p_i2130_3_;
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
                Ps4BiomeData biome = Ps4BiomeData.getBiome(k);

                if (k == Ps4BiomeData.getIdForBiome(Ps4BiomeData.MUSHROOM_ISLAND))
                {
                    int j2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    int i3 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    int l3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    int k4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                    if (!Ps4BiomeData.isShallowOcean(j2) && !Ps4BiomeData.isShallowOcean(i3) && !Ps4BiomeData.isShallowOcean(l3) && !Ps4BiomeData.isShallowOcean(k4))
                    {
                        aint1[j + i * areaWidth] = k;
                    }
                    else
                    {
                        aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.MUSHROOM_ISLAND_SHORE);
                    }
                }
                else if (biome != null && biome.family == 6)
                {
                    int i2 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                    int l2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                    int k3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                    int j4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                    if (this.isJungleCompatible(i2) && this.isJungleCompatible(l2) && this.isJungleCompatible(k3) && this.isJungleCompatible(j4))
                    {
                        if (!isBiomeOceanic(i2) && !isBiomeOceanic(l2) && !isBiomeOceanic(k3) && !isBiomeOceanic(j4))
                        {
                            aint1[j + i * areaWidth] = k;
                        }
                        else
                        {
                            aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.BEACH);
                        }
                    }
                    else
                    {
                        aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE_EDGE);
                    }
                }
                else if (k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS) && k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS_WITH_TREES) && k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.EXTREME_HILLS_EDGE))
                {
                    if (biome != null && biome.isSnowyBiome())
                    {
                        this.replaceIfNeighborOcean(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.COLD_BEACH));
                    }
                    else if (k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA) && k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.MESA_ROCK))
                    {
                        if (!Ps4BiomeData.isOcean(k) && k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.RIVER) && k != Ps4BiomeData.getIdForBiome(Ps4BiomeData.SWAMPLAND))
                        {
                            int l1 = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                            int k2 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                            int j3 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                            int i4 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                            if (!isBiomeOceanic(l1) && !isBiomeOceanic(k2) && !isBiomeOceanic(j3) && !isBiomeOceanic(i4))
                            {
                                aint1[j + i * areaWidth] = k;
                            }
                            else
                            {
                                aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.BEACH);
                            }
                        }
                        else
                        {
                            aint1[j + i * areaWidth] = k;
                        }
                    }
                    else
                    {
                        int l = aint[j + 1 + (i + 1 - 1) * (areaWidth + 2)];
                        int i1 = aint[j + 1 + 1 + (i + 1) * (areaWidth + 2)];
                        int j1 = aint[j + 1 - 1 + (i + 1) * (areaWidth + 2)];
                        int k1 = aint[j + 1 + (i + 1 + 1) * (areaWidth + 2)];

                        if (!isBiomeOceanic(l) && !isBiomeOceanic(i1) && !isBiomeOceanic(j1) && !isBiomeOceanic(k1))
                        {
                            if (this.isMesa(l) && this.isMesa(i1) && this.isMesa(j1) && this.isMesa(k1))
                            {
                                aint1[j + i * areaWidth] = k;
                            }
                            else
                            {
                                aint1[j + i * areaWidth] = Ps4BiomeData.getIdForBiome(Ps4BiomeData.DESERT);
                            }
                        }
                        else
                        {
                            aint1[j + i * areaWidth] = k;
                        }
                    }
                }
                else
                {
                    this.replaceIfNeighborOcean(aint, aint1, j, i, areaWidth, k, Ps4BiomeData.getIdForBiome(Ps4BiomeData.STONE_BEACH));
                }
            }
        }

        return aint1;
    }

    private void replaceIfNeighborOcean(int[] p_151632_1_, int[] p_151632_2_, int p_151632_3_, int p_151632_4_, int p_151632_5_, int p_151632_6_, int p_151632_7_)
    {
        if (isBiomeOceanic(p_151632_6_))
        {
            p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
        }
        else
        {
            int i = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 - 1) * (p_151632_5_ + 2)];
            int j = p_151632_1_[p_151632_3_ + 1 + 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
            int k = p_151632_1_[p_151632_3_ + 1 - 1 + (p_151632_4_ + 1) * (p_151632_5_ + 2)];
            int l = p_151632_1_[p_151632_3_ + 1 + (p_151632_4_ + 1 + 1) * (p_151632_5_ + 2)];

            if (!isBiomeOceanic(i) && !isBiomeOceanic(j) && !isBiomeOceanic(k) && !isBiomeOceanic(l))
            {
                p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_6_;
            }
            else
            {
                p_151632_2_[p_151632_3_ + p_151632_4_ * p_151632_5_] = p_151632_7_;
            }
        }
    }

    private boolean isJungleCompatible(int p_151631_1_)
    {
        return Ps4BiomeData.getBiome(p_151631_1_) != null && Ps4BiomeData.getBiome(p_151631_1_).family == 6 ? true : p_151631_1_ == Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE_EDGE) || p_151631_1_ == Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE) || p_151631_1_ == Ps4BiomeData.getIdForBiome(Ps4BiomeData.JUNGLE_HILLS) || p_151631_1_ == Ps4BiomeData.getIdForBiome(Ps4BiomeData.FOREST) || p_151631_1_ == Ps4BiomeData.getIdForBiome(Ps4BiomeData.TAIGA) || isBiomeOceanic(p_151631_1_);
    }

    private boolean isMesa(int p_151633_1_)
    {
        return Ps4BiomeData.getBiome(p_151633_1_) != null && Ps4BiomeData.getBiome(p_151633_1_).family == 7;
    }
}
