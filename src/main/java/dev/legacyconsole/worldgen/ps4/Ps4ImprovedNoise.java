package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** LCE ImprovedNoise, including its original one-row path and vertical gradient cache. */
public final class Ps4ImprovedNoise {
  private final int[] p = new int[512];
  public final double xo, yo, zo;

  public Ps4ImprovedNoise(Random random) {
    xo = random.nextDouble() * 256;
    yo = random.nextDouble() * 256;
    zo = random.nextDouble() * 256;
    for (int i = 0; i < 256; i++) p[i] = i;
    for (int i = 0; i < 256; i++) {
      int j = random.nextInt(256 - i) + i;
      int value = p[i]; p[i] = p[j]; p[j] = value;
      p[i + 256] = p[i];
    }
  }

  public double value(double x, double y, double z) {
    x += xo; y += yo; z += zo;
    int xf = floor(x), yf = floor(y), zf = floor(z);
    int X = xf & 255, Y = yf & 255, Z = zf & 255;
    x -= xf; y -= yf; z -= zf;
    double u = fade(x), v = fade(y), w = fade(z);
    int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
    int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;
    return lerp(w, lerp(v,
        lerp(u, grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z)),
        lerp(u, grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z))),
        lerp(v,
        lerp(u, grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1)),
        lerp(u, grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1))));
  }

  public void add(double[] buffer, double ox, double oy, double oz, int nx, int ny, int nz,
      double sx, double sy, double sz, double amplitude) {
    int index = 0, previousY = -1;
    double inverse = 1 / amplitude;
    double v0 = 0, v1 = 0, v2 = 0, v3 = 0;
    double absoluteX = ox + xo;
    for (int xx = 0; xx < nx; xx++) {
      double x = absoluteX; absoluteX += sx;
      int xf = floor(x), X = xf & 255; x -= xf;
      double u = fade(x);
      double absoluteZ = oz + zo;
      for (int zz = 0; zz < nz; zz++) {
        double z = absoluteZ; absoluteZ += sz;
        int zf = floor(z), Z = zf & 255; z -= zf;
        double w = fade(z);
        if (ny == 1) {
          int AA = p[p[X]] + Z, BA = p[p[X + 1]] + Z;
          double a = lerp(u, grad2(p[AA], x, z), grad(p[BA], x - 1, 0, z));
          double b = lerp(u, grad(p[AA + 1], x, 0, z - 1), grad(p[BA + 1], x - 1, 0, z - 1));
          buffer[index++] += lerp(w, a, b) * inverse;
          continue;
        }
        double absoluteY = oy + yo;
        for (int yy = 0; yy < ny; yy++) {
          double y = absoluteY; absoluteY += sy;
          int yf = floor(y), Y = yf & 255; y -= yf;
          double v = fade(y);
          if (yy == 0 || Y != previousY) {
            previousY = Y;
            int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
            int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;
            v0 = lerp(u, grad(p[AA], x, y, z), grad(p[BA], x - 1, y, z));
            v1 = lerp(u, grad(p[AB], x, y - 1, z), grad(p[BB], x - 1, y - 1, z));
            v2 = lerp(u, grad(p[AA + 1], x, y, z - 1), grad(p[BA + 1], x - 1, y, z - 1));
            v3 = lerp(u, grad(p[AB + 1], x, y - 1, z - 1), grad(p[BB + 1], x - 1, y - 1, z - 1));
          }
          buffer[index++] += lerp(w, lerp(v, v0, v1), lerp(v, v2, v3)) * inverse;
        }
      }
    }
  }

  private static int floor(double x) { int i = (int)x; return x < i ? i - 1 : i; }
  private static double fade(double x) { return x * x * x * (x * (x * 6 - 15) + 10); }
  private static double lerp(double t, double a, double b) { return a + t * (b - a); }
  private static double grad2(int hash, double x, double z) {
    int h = hash & 15;
    double u = (1 - ((h & 8) >> 3)) * x, v = h < 4 ? 0 : h == 12 || h == 14 ? x : z;
    return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
  }
  private static double grad(int hash, double x, double y, double z) {
    int h = hash & 15;
    double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
    return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
  }
}
