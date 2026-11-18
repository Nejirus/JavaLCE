// PS4 biome-layer port; see docs/LEGACY-WORLDGEN.md for source methods and verification.
package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

final class GenLayerAddIsland extends GenLayer
{
    public GenLayerAddIsland(long p_i2119_1_, GenLayer p_i2119_3_)
    {
        super(p_i2119_1_);
        this.parent = p_i2119_3_;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight)
    {
        int i = areaX - 1;
        int j = areaY - 1;
        int k = areaWidth + 2;
        int l = areaHeight + 2;
        int[] aint = this.parent.getInts(i, j, k, l);
        int[] aint1 = new int[areaWidth * areaHeight];

        for (int i1 = 0; i1 < areaHeight; ++i1)
        {
            for (int j1 = 0; j1 < areaWidth; ++j1)
            {
                int k1 = aint[j1 + 0 + (i1 + 0) * k];
                int l1 = aint[j1 + 2 + (i1 + 0) * k];
                int i2 = aint[j1 + 0 + (i1 + 2) * k];
                int j2 = aint[j1 + 2 + (i1 + 2) * k];
                int k2 = aint[j1 + 1 + (i1 + 1) * k];
                this.initChunkSeed((long)(j1 + areaX), (long)(i1 + areaY));

                if (!Ps4BiomeData.isShallowOcean(k2) || Ps4BiomeData.isShallowOcean(k1) && Ps4BiomeData.isShallowOcean(l1) && Ps4BiomeData.isShallowOcean(i2) && Ps4BiomeData.isShallowOcean(j2))
                {
                    if (!Ps4BiomeData.isShallowOcean(k2) && (Ps4BiomeData.isShallowOcean(k1) || Ps4BiomeData.isShallowOcean(l1) || Ps4BiomeData.isShallowOcean(i2) || Ps4BiomeData.isShallowOcean(j2)))
                    {
                        if (this.nextInt(5) == 0)
                        {
                            if (k2 == 4)
                            {
                                aint1[j1 + i1 * areaWidth] = 4;
                            }
                            else
                            {
                                aint1[j1 + i1 * areaWidth] = Ps4BiomeData.isShallowOcean(k1)?k1:Ps4BiomeData.isShallowOcean(l1)?l1:Ps4BiomeData.isShallowOcean(i2)?i2:Ps4BiomeData.isShallowOcean(j2)?j2:0;
                            }
                        }
                        else
                        {
                            aint1[j1 + i1 * areaWidth] = k2;
                        }
                    }
                    else
                    {
                        aint1[j1 + i1 * areaWidth] = k2;
                    }
                }
                else
                {
                    int l2 = 1;
                    int i3 = 1;

                    if (!Ps4BiomeData.isShallowOcean(k1) && this.nextInt(l2++) == 0)
                    {
                        i3 = k1;
                    }

                    if (!Ps4BiomeData.isShallowOcean(l1) && this.nextInt(l2++) == 0)
                    {
                        i3 = l1;
                    }

                    if (!Ps4BiomeData.isShallowOcean(i2) && this.nextInt(l2++) == 0)
                    {
                        i3 = i2;
                    }

                    if (!Ps4BiomeData.isShallowOcean(j2) && this.nextInt(l2++) == 0)
                    {
                        i3 = j2;
                    }

                    if (this.nextInt(3) == 0)
                    {
                        aint1[j1 + i1 * areaWidth] = i3;
                    }
                    else if (i3 == 4)
                    {
                        aint1[j1 + i1 * areaWidth] = 4;
                    }
                    else
                    {
                        aint1[j1 + i1 * areaWidth] = 0;
                    }
                }
            }
        }

        return aint1;
    }
}
