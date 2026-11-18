package dev.legacyconsole.worldgen.ps4;

import java.io.*;
import java.nio.*;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MathOracleTest {
 @Test public void originalPs4DirectTrigonometryMatchesFloatBits()throws Exception{
  byte[] raw;
  try(InputStream source=getClass().getResourceAsStream("/worldgen/ps4-direct-math.f32.gz");GZIPInputStream gzip=new GZIPInputStream(source)){raw=gzip.readAllBytes();}
  ByteBuffer data=ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);assertEquals(100124*12,data.remaining());
  while(data.hasRemaining()){
   int bits=data.getInt(),sin=data.getInt(),cos=data.getInt();float angle=Float.intBitsToFloat(bits);
   assertEquals("PS4 sinf input="+Integer.toHexString(bits),sin,Float.floatToRawIntBits(Ps4Math.sinDirect(angle)));
   assertEquals("PS4 cosf input="+Integer.toHexString(bits),cos,Float.floatToRawIntBits(Ps4Math.cosDirect(angle)));
  }
 }
 @Test public void originalPs4MthTableIsPreserved()throws Exception{
  byte[] raw;
  try(InputStream source=getClass().getResourceAsStream("/assets/legacyconsole/worldgen/ps4-sine.f32.gz");GZIPInputStream gzip=new GZIPInputStream(source)){raw=gzip.readAllBytes();}
  assertEquals(65536*4,raw.length);StringBuilder hex=new StringBuilder();
  for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hex.append(String.format("%02x",b&255));
  assertEquals("a994dcf0a00f927939600f8f242e634741a8e6e544745851a263bb4ee7206051",hex.toString());
 }
 @Test public void originalPs4DoubleCosMatchesBits()throws Exception{
  byte[] raw;
  try(InputStream source=getClass().getResourceAsStream("/worldgen/ps4-double-cos.f64.gz");GZIPInputStream gzip=new GZIPInputStream(source)){raw=gzip.readAllBytes();}
  ByteBuffer data=ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);assertEquals(101085*16,data.remaining());
  while(data.hasRemaining()){
   long bits=data.getLong(),expected=data.getLong();
   assertEquals("PS4 cos input="+Long.toHexString(bits),expected,Double.doubleToRawLongBits(Ps4Math.cosDouble(Double.longBitsToDouble(bits))));
  }
 }
 @Test public void originalPs4DoubleSineMatchesBits()throws Exception{
  byte[] raw;
  try(InputStream source=getClass().getResourceAsStream("/worldgen/ps4-double-sine.f64.gz");GZIPInputStream gzip=new GZIPInputStream(source)){raw=gzip.readAllBytes();}
  ByteBuffer data=ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);assertEquals(100146*16,data.remaining());
  while(data.hasRemaining()){
   long bits=data.getLong(),expected=data.getLong();
   assertEquals("PS4 sine input="+Long.toHexString(bits),expected,Double.doubleToRawLongBits(Ps4Math.sinDouble(Double.longBitsToDouble(bits))));
  }
 }
}
