package dev.legacyconsole.worldgen.ps4;

import java.util.Arrays;
import java.util.Random;

/** Console octave ordering and coordinate wrapping, ported from PerlinNoise.cpp. */
public final class Ps4PerlinNoise {
  private final Ps4ImprovedNoise[] octaves;

  public Ps4PerlinNoise(Random random, int levels) {
    octaves = new Ps4ImprovedNoise[levels];
    for (int i = 0; i < levels; i++) octaves[i] = new Ps4ImprovedNoise(random);
  }

  public double value(double x, double y) {
    double value = 0, amplitude = 1;
    for (Ps4ImprovedNoise octave : octaves) {
      value += octave.value(x * amplitude, y * amplitude, 0) / amplitude;
      amplitude /= 2;
    }
    return value;
  }

  public double[] region(int x, int y, int z, int nx, int ny, int nz,
      double sx, double sy, double sz) {
    return region(null, x, y, z, nx, ny, nz, sx, sy, sz);
  }

  public double[] region(double[] buffer, int x, int y, int z, int nx, int ny, int nz,
      double sx, double sy, double sz) {
    int length = Math.multiplyExact(Math.multiplyExact(nx, ny), nz);
    if (buffer == null || buffer.length != length) buffer = new double[length];
    else Arrays.fill(buffer, 0);
    double amplitude = 1;
    for (Ps4ImprovedNoise octave : octaves) {
      double xx = x * amplitude * sx, yy = y * amplitude * sy, zz = z * amplitude * sz;
      long xb = (long)Math.floor(xx), zb = (long)Math.floor(zz);
      xx -= xb; zz -= zb;
      xx += xb % 16777216; zz += zb % 16777216;
      octave.add(buffer, xx, yy, zz, nx, ny, nz, sx * amplitude, sy * amplitude, sz * amplitude, amplitude);
      amplitude /= 2;
    }
    return buffer;
  }
}
