package dev.legacyconsole.worldgen.ps4;

import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;

/** mcps4 LargeCaveFeature/CanyonFeature, checked against the original routines.
 * Original feature odds, packed-buffer Y bounds, biome gates, RNG and math are retained. */
public final class Ps4Carvers {
 private Ps4Carvers(){}
 private static final float PI=(float)Math.PI;
 private static float sin(float a){return Ps4Math.sin(a);}
 private static float cos(float a){return Ps4Math.cos(a);}
 private static int floor(double a){return (int)Math.floor(a);}
 public static void carve(long seed,int cx,int cz,byte[] blocks,IntBinaryOperator biomes){
  carve(seed,cx,cz,blocks,biomes,(x,z)->Ps4BiomeSurfaces.defaultTopTile(biomes.applyAsInt(x,z)));
 }
 public static void carve(long seed,int cx,int cz,byte[] blocks,IntBinaryOperator biomes,IntBinaryOperator topTiles){
  carve(seed,cx,cz,blocks,biomes,topTiles,index->{});
 }
 /** Preserve metadata on untouched voxels, and clear it on every original
  * default-state write, including writes whose tile ID stays unchanged. */
 public static void carve(long seed,int cx,int cz,int[] states,IntBinaryOperator biomes,IntBinaryOperator topTiles){
  if(states.length!=32768)throw new IllegalArgumentException("PS4 carver requires a 16x128x16 chunk");
  byte[] blocks=new byte[states.length];for(int i=0;i<states.length;i++)blocks[i]=(byte)states[i];
  carve(seed,cx,cz,blocks,biomes,topTiles,index->states[index]=blocks[index]&255);
 }
 private static void carve(long seed,int cx,int cz,byte[] blocks,IntBinaryOperator biomes,IntBinaryOperator topTiles,IntConsumer writes){
  if(blocks.length!=32768)throw new IllegalArgumentException("PS4 carver requires a 16x128x16 chunk");
  // getChunk (0x508360) runs both underwater features before the air features.
  Ps4UnderwaterCarvers.carve(seed,cx,cz,blocks,biomes,writes);
  new Caves(biomes,topTiles,writes).apply(seed,cx,cz,blocks);new Canyon(biomes,topTiles,writes).apply(seed,cx,cz,blocks);
 }
 static void write(byte[] blocks,int index,int id,IntConsumer writes){blocks[index]=(byte)id;writes.accept(index);}
 private static boolean canReplace(int id,int above){
  return id==1||id==2||id==3||id==24||id==78||id==110||id==159||id==172||id==179
   ||((id==12||id==13)&&above!=8&&above!=9);
 }
 private abstract static class Large {
  protected final int radius=8;
  protected final Random random=new Random();
  protected final IntBinaryOperator biomes,top;
  protected final IntConsumer writes;
  Large(IntBinaryOperator biomes,IntBinaryOperator topTiles,IntConsumer writes){this.biomes=biomes;this.top=topTiles;this.writes=writes;}
  void write(byte[] blocks,int index,int id){Ps4Carvers.write(blocks,index,id,writes);}
  boolean ocean(int x,int z){return Ps4BiomeData.isOceanCategory(biomes.applyAsInt(x,z));}
  // 0x85a50 reads base depth. 0x85300 accepts the snow flag or the
  // frozen-ocean accumulation setting, both represented by isSnowyBiome.
  boolean allowWetCarving(int x,int z){var data=Ps4BiomeData.getBiome(biomes.applyAsInt(x,z));return data.depth<.1f&&data.isSnowyBiome();}
  void apply(long seed,int cx,int cz,byte[] blocks){
   random.setSeed(seed);long a=random.nextLong(),b=random.nextLong();
   for(int x=cx-radius;x<=cx+radius;x++)for(int z=cz-radius;z<=cz+radius;z++){
    random.setSeed((long)x*a^(long)z*b^seed);addFeature(x,z,cx,cz,blocks);
   }
  }
  abstract void addFeature(int x,int z,int xOffs,int zOffs,byte[] blocks);
 }
 private static final class Caves extends Large {
 Caves(IntBinaryOperator biomes,IntBinaryOperator topTiles,IntConsumer writes){super(biomes,topTiles,writes);}
void addRoom(long seed, int xOffs, int zOffs, byte[] blocks, double xRoom, double yRoom, double zRoom)
{
	addTunnel(seed, xOffs, zOffs, blocks, xRoom, yRoom, zRoom, 1 + random.nextFloat() * 6, 0, 0, -1, -1, 0.5);
}

void addTunnel(long seed, int xOffs, int zOffs, byte[] blocks, double xCave, double yCave, double zCave, float thickness, float yRot, float xRot, int step, int dist, double yScale)
{
    int startX=floor(xCave),startZ=floor(zCave);
    double xMid = xOffs * 16 + 8;
    double zMid = zOffs * 16 + 8;

    float yRota = 0;
    float xRota = 0;
    Random random = new Random(seed);

    if (dist <= 0)
	{
        int max = radius * 16 - 16;
        dist = max - random.nextInt(max / 4);
    }
    boolean singleStep = false;

    if (step == -1)
	{
        step = dist / 2;
        singleStep = true;
    }

    int splitPoint = random.nextInt(dist / 2) + dist / 4;
    boolean steep = random.nextInt(6) == 0;

    for (; step < dist; step++)
	{
        double rad = 1.5 + (sin(step * PI / dist) * thickness) * 1;
        double yRad = rad * yScale;

        float xc = cos(xRot);
        float xs = sin(xRot);
        xCave += cos(yRot) * xc;
        yCave += xs;
        zCave += sin(yRot) * xc;

        if (steep)
		{
            xRot *= 0.92f;
        }
		else
		{
            xRot *= 0.7f;
        }
        xRot += xRota * 0.1f;
        yRot += yRota * 0.1f;

        xRota *= 0.90f;
        yRota *= 0.75f;
        xRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2;
        yRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4;

        if (!singleStep && step == splitPoint && thickness > 1 && dist > 0)
		{
            addTunnel(random.nextLong(), xOffs, zOffs, blocks, xCave, yCave, zCave, random.nextFloat() * 0.5f + 0.5f, yRot - PI / 2, xRot / 3, step, dist, 1.0);
            addTunnel(random.nextLong(), xOffs, zOffs, blocks, xCave, yCave, zCave, random.nextFloat() * 0.5f + 0.5f, yRot + PI / 2, xRot / 3, step, dist, 1.0);
            return;
        }
        if (!singleStep && random.nextInt(4) == 0) continue;
        {
            double xd = xCave - xMid;
            double zd = zCave - zMid;
            double remaining = dist - step;
            double rr = (thickness + 2) + 16;
            if (xd * xd + zd * zd - (remaining * remaining) > rr * rr)
			{
                return;
            }
        }

        if (xCave < xMid - 16 - rad * 2 || zCave < zMid - 16 - rad * 2 || xCave > xMid + 16 + rad * 2 || zCave > zMid + 16 + rad * 2) continue;

        int x0 = floor(xCave - rad) - xOffs * 16 - 1;
        int x1 = floor(xCave + rad) - xOffs * 16 + 1;

        int y0 = floor(yCave - yRad) - 1;
        int y1 = floor(yCave + yRad) + 1;

        int z0 = floor(zCave - rad) - zOffs * 16 - 1;
        int z1 = floor(zCave + rad) - zOffs * 16 + 1;

        if (x0 < 0) x0 = 0;
        if (x1 > 16) x1 = 16;

        if (y0 < 1) y0 = 1;
        if (y1 > 128 - 8) y1 = 128 - 8;

        if (z0 < 0) z0 = 0;
        if (z1 > 16) z1 = 16;

        boolean detectedWater = false;
        for (int xx = x0; !detectedWater && xx < x1; xx++)
		{
            for (int zz = z0; !detectedWater && zz < z1; zz++)
			{
                for (int yy = y1 + 1; !detectedWater && yy >= y0 - 1; yy--)
				{
                    int p = (xx * 16 + zz) * 128 + yy;
                    if (yy < 0 || yy >= 128) continue;
                    if (blocks[p] == 8 || blocks[p] == 9)
					{
                        detectedWater = true;
                    }
                    if (yy != y0 - 1 && xx != x0 && xx != x1 - 1 && zz != z0 && zz != z1 - 1)
					{
                        yy = y0;
                    }
                }
            }
        }
        if (detectedWater&&!allowWetCarving((int)xMid,(int)zMid)) continue;
        if (ocean(startX,startZ)) break;

        carveSphere(blocks,xOffs,zOffs,xCave,yCave,zCave,rad,yRad,x0,x1,y0,y1,z0,z1,detectedWater);
        if (singleStep) break;
    }

}

/** Original block-removal helper 0x3deb80; its unusual index/sample Y relation is intentional. */
void carveSphere(byte[] blocks,int xOffs,int zOffs,double xCave,double yCave,double zCave,double rad,double yRad,int x0,int x1,int y0,int y1,int z0,int z1,boolean detectedWater){
        for (int xx = x0; xx < x1; xx++) 
		{
            double xd = ((xx + xOffs * 16 + 0.5) - xCave) / rad;
            for (int zz = z0; zz < z1; zz++)
			{
                boolean cold=allowWetCarving(xx+xOffs*16,zz+zOffs*16);
                if(detectedWater&&!cold)return;
                double zd = ((zz + zOffs * 16 + 0.5) - zCave) / rad;
                int p = (xx * 16 + zz) * 128 + y1 - 1;
                boolean hasGrass = false;
                if (xd * xd + zd * zd < 1)
				{
                    for (int yy = y1 - 2; yy >= y0 - 1; yy--)
					{
                        double yd = (yy + 0.5 - yCave) / yRad;
                        if (yd > -0.7 && xd * xd + yd * yd + zd * zd < 1)
						{
                            int block = blocks[p]&255;
                            if (block == 2 || block == 110) hasGrass = true;
                            if ((!detectedWater||block==174)&&canReplace(block,blocks[p+1]&255))
							{
                                if(ocean(floor(xCave),floor(zCave)))return;
                                if (yy < 10) 
								{
                                    write(blocks,p,11);
                                }
								else
								{
                                    write(blocks,p,cold&&block==174&&yy+2<=63?9:0);
                                    if (hasGrass && blocks[p - 1] == 3) write(blocks,p-1,top.applyAsInt(xx+xOffs*16,zz+zOffs*16));
                                }
                            }
                        }
                        p--;
                    }
                }
            }
        }
}

void addFeature(int x, int z, int xOffs, int zOffs, byte[] blocks)
{
	int caves = random.nextInt(random.nextInt(random.nextInt(40) + 1) + 1);
    if (random.nextInt(15) != 0) caves = 0;

    for (int cave = 0; cave < caves; cave++)
	{
        double xCave = x * 16 + random.nextInt(16);
        double yCave = random.nextInt(random.nextInt(128 - 8) + 8);
        double zCave = z * 16 + random.nextInt(16);

        int tunnels = 1;
        if (random.nextInt(4) == 0)
		{
            addRoom(random.nextLong(), xOffs, zOffs, blocks, xCave, yCave, zCave);
            tunnels += random.nextInt(4);
        }

        for (int i = 0; i < tunnels; i++)
		{

            float yRot = random.nextFloat() * PI * 2;
            float xRot = ((random.nextFloat() - 0.5f) * 2) / 8;
            float thickness = random.nextFloat() * 2 + random.nextFloat();
			if (random.nextInt(10) == 0) thickness *= random.nextFloat() * random.nextFloat() * 3 + 1;

            addTunnel(random.nextLong(), xOffs, zOffs, blocks, xCave, yCave, zCave, thickness, yRot, xRot, 0, 0, 1.0);
        }
    }

}

 }
 private static final class Canyon extends Large {
 Canyon(IntBinaryOperator biomes,IntBinaryOperator topTiles,IntConsumer writes){super(biomes,topTiles,writes);}
private final float[] rs=new float[128];
void addTunnel(long seed, int xOffs, int zOffs, byte[] blocks, double xCave, double yCave, double zCave, float thickness, float yRot, float xRot, int step, int dist, double yScale)
{
	
	Random random = new Random(seed);
	
    int startX=floor(xCave),startZ=floor(zCave);
    double xMid = xOffs * 16 + 8;
    double zMid = zOffs * 16 + 8;

    float yRota = 0;
    float xRota = 0;

    if (dist <= 0)
	{
        int max = radius * 16 - 16;
        dist = max - random.nextInt(max / 4);
    }
    boolean singleStep = false;

    if (step == -1)
	{
        step = dist / 2;
        singleStep = true;
    }

	float f = 1;
	for (int i = 0; i < 128; i++)
	{
		if (i == 0 || random.nextInt(3) == 0)
		{
			f = 1 + (random.nextFloat() * random.nextFloat()) * 1.0f;
		}
		rs[i] = f * f;
	}

    for (; step < dist; step++)
	{
        double rad = 1.5 + (sin(step * PI / dist) * thickness) * 1;
        double yRad = rad * yScale;

		rad *= (random.nextFloat() * 0.25 + 0.75);
		yRad *= (random.nextFloat() * 0.25 + 0.75);

        float xc = cos(xRot);
        float xs = sin(xRot);
        xCave += cos(yRot) * xc;
        yCave += xs;
        zCave += sin(yRot) * xc;

		xRot *= 0.7f;

        xRot += xRota * 0.05f;
        yRot += yRota * 0.05f;

        xRota *= 0.80f;
        yRota *= 0.50f;
        xRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2;
        yRota += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4;

        if (!singleStep && random.nextInt(4) == 0) continue;

        {
            double xd = xCave - xMid;
            double zd = zCave - zMid;
            double remaining = dist - step;
            double rr = (thickness + 2) + 16;
            if (xd * xd + zd * zd - (remaining * remaining) > rr * rr)
			{
				
                return;
            }
        }

        if (xCave < xMid - 16 - rad * 2 || zCave < zMid - 16 - rad * 2 || xCave > xMid + 16 + rad * 2 || zCave > zMid + 16 + rad * 2) continue;

        int x0 = floor(xCave - rad) - xOffs * 16 - 1;
        int x1 = floor(xCave + rad) - xOffs * 16 + 1;

        int y0 = floor(yCave - yRad) - 1;
        int y1 = floor(yCave + yRad) + 1;

        int z0 = floor(zCave - rad) - zOffs * 16 - 1;
        int z1 = floor(zCave + rad) - zOffs * 16 + 1;

        if (x0 < 0) x0 = 0;
        if (x1 > 16) x1 = 16;

        if (y0 < 1) y0 = 1;
        if (y1 > 128 - 8) y1 = 128 - 8;

        if (z0 < 0) z0 = 0;
        if (z1 > 16) z1 = 16;

        boolean detectedWater = false;
        for (int xx = x0; !detectedWater && xx < x1; xx++)
		{
            for (int zz = z0; !detectedWater && zz < z1; zz++)
			{
                for (int yy = y1 + 1; !detectedWater && yy >= y0 - 1; yy--)
				{
                    int p = (xx * 16 + zz) * 128 + yy;
                    if (yy < 0 || yy >= 128) continue;
                    if (blocks[p] == 8 || blocks[p] == 9)
					{
                        detectedWater = true;
                    }
                    if (yy != y0 - 1 && xx != x0 && xx != x1 - 1 && zz != z0 && zz != z1 - 1)
					{
                        yy = y0;
                    }
                }
            }
        }
        if (detectedWater) continue;

        for (int xx = x0; xx < x1; xx++)
		{
            double xd = ((xx + xOffs * 16 + 0.5) - xCave) / rad;
            for (int zz = z0; zz < z1; zz++)
			{
                double zd = ((zz + zOffs * 16 + 0.5) - zCave) / rad;
                int p = (xx * 16 + zz) * 128 + y1 - 1;
                boolean hasGrass = false;
				if (xd * xd + zd * zd < 1)
				{
					for (int yy = y1 - 1; yy >= y0; yy--)
					{
						double yd = (yy + 0.5 - yCave) / yRad;
						if ((xd * xd + zd * zd) * rs[yy] + (yd * yd / 6) < 1)
						{
							int block = blocks[p]&255;
							if (block == 2) hasGrass = true;
							if (canReplace(block,blocks[p+1]&255))
							{
								if(ocean(startX,startZ))return;
								if (yy <= 10)
								{
									write(blocks,p,10);
								}
								else
								{
									write(blocks,p,0);
									if (hasGrass && blocks[p - 1] == 3) write(blocks,p-1,top.applyAsInt(xx+xOffs*16,zz+zOffs*16));
								}
							}
						}
						p--;
					}
				}
            }
        }
        if (singleStep) break;
    }
	
}

void addFeature(int x, int z, int xOffs, int zOffs, byte[] blocks)
{
    if (random.nextInt(50) != 0) return;

    double xCave = x * 16 + random.nextInt(16);
    double yCave = random.nextInt(random.nextInt(40) + 8) + 20;
    double zCave = z * 16 + random.nextInt(16);

	int tunnels = 1;

	for (int i = 0; i < tunnels; i++)
	{
		float yRot = random.nextFloat() * PI * 2;
		float xRot = ((random.nextFloat() - 0.5f) * 2) / 8;
		float thickness = (random.nextFloat() * 2 + random.nextFloat()) * 2;

		addTunnel(random.nextLong(), xOffs, zOffs, blocks, xCave, yCave, zCave, thickness, yRot, xRot, 0, 0, 3.0);

	}

}
 }
}
