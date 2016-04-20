/*
 * ChBitArray.java
 *
 * Created on 3. Juli 2001, 10:42
 */

package chesspresso.position.map;

import java.io.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public final class BitArray
{
    
    public static int getBytesForNumber(long num)
    {
        int bytes = 0;
        while (num != 0) {num >>>= 8; bytes++;}
        return bytes;
    }
    
    public static int getBitsForNumber(long num)
    {
        int bits = 0;
        while (num != 0) {num >>>= 1; bits++;} 
        return bits;
    }
    
    /*================================================================================*/
    
    public static int readInt(DataInput in, int numOfBytes) throws IOException
    {
        int res = 0;
        for (; numOfBytes > 0; numOfBytes--) {
            res = (res << 8) | (((int)in.readByte()) & 0xFF);
        }
	return res;
    }
    
    public static long readLong(DataInput in, int numOfBytes) throws IOException
    {
        long res = 0;
        for (; numOfBytes > 0; numOfBytes--) {
            res = (res << 8) | (((long)in.readByte()) & 0xFF);
        }
	return res;
    }
    
    public static void writeInt(DataOutput out, int value, int numOfBytes) throws IOException
    {
        for (; numOfBytes > 0; numOfBytes--) {
            out.write((int)(value >>> (8 * numOfBytes - 8) & 0xFF));
        }
    }
    
    public static void writeLong(DataOutput out, long value, int numOfBytes) throws IOException
    {
        for (; numOfBytes > 0; numOfBytes--) {
            out.write((int)(value >>> (8 * numOfBytes - 8) & 0xFF));
        }
    }
    
    /*================================================================================*/
    
    public static void clear(byte[] data)
    {
        for (int i=0; i<data.length; i++) {
            data[i] = 0;
        }
    }    
    
    public static int getInt(byte[] data, int startBit, int numOfBits)
    {
        long res = 0;
        
        int startByte = startBit / 8;
        int lastByte = (startBit + numOfBits - 1) / 8;
        
        for (int i = lastByte; i >= startByte; i--) {
            res <<= 8;
            res |= ((int)data[i]) & 255;
        }
        
        res >>>= (startBit & 7);
        return (int)(res & ((1L << numOfBits) - 1));
    }

    public static long getLong(byte[] data, int startBit, int numOfBits)
    {
        if (numOfBits > 32) {
            return (((long)getInt(data, startBit + 32, numOfBits - 32)) << 32)
                        | (getInt(data, startBit, 32) & ((1L << 32) - 1));
        } else {
            return ((long)getInt(data, startBit, numOfBits)) & ((1L << 32) - 1);
        }
    }

    public static boolean getBoolean(byte[] data, int startBit)
    {
        return (data[startBit / 8] & (1L << (startBit % 8))) != 0L;
    }
    
    public static int setInt(byte[] data, int value, int startBit, int numOfBits)
    {
        long val = value;
        
        int lastBit = startBit + numOfBits - 1;
        int startByte = startBit / 8;
        int lastByte = lastBit / 8;
        
        if ((startBit & 7) != 0) {
            val <<= (startBit & 7);
            val |= ((int)data[startByte]) & ((1 << (startBit & 7)) - 1);
            numOfBits += (startBit & 7);
        }
        
        if ((numOfBits & 7) != 0) {
            long d = ((long)data[lastByte]) & 255;
            d >>>= (numOfBits & 7);
            val |= (d << numOfBits);
        }
        
        for (int i=startByte; i<=lastByte; i++) {
            data[i] = (byte)(val & 255);
            val >>= 8;
        }
        
        return lastBit + 1;
    }
    
    public static int setLong(byte[] data, long value, int startBit, int numOfBits)
    {
        if (numOfBits > 32) {
            setInt(data, (int)value, startBit, 32);
            return setInt(data, (int)(value >> 32), startBit + 32, numOfBits - 32);
        } else {
            return setInt(data, (int)value, startBit, numOfBits);
        }
    }
    
    public static int setBoolean(byte[] data, boolean value, int startBit)
    {
        if (value) {
            data[startBit / 8] |=   1 << (startBit % 8);
        } else {
            data[startBit / 8] &= ~(1 << (startBit % 8));
        }
        return startBit + 1;
    }
    
    public static void print(byte[] data)
    {
        for (int i=0; i<data.length; i++) {
            for (int j=0; j<8; j++) {
                System.out.print((data[i] & (1 << j)) != 0 ? "1" : "0");
            }
        }
        System.out.println();
    }
    
    public static void print(byte[] data, int startBit, int numOfBits)
    {
        print(data);
        for (int i=0; i<startBit; i++) System.out.print(" ");
        for (int i=0; i<numOfBits; i++) System.out.print("-");
    }
    
    /*================================================================================*/
    
    public static void clear(short[] data)
    {
        for (int i=0; i<data.length; i++) {
            data[i] = 0;
        }
    }    
    
    public static int getInt(short[] data, int startBit, int numOfBits)
    {
        long res = 0;
        
        int startByte = startBit / 16;
        int lastByte = (startBit + numOfBits - 1) / 16;
        
        for (int i = lastByte; i >= startByte; i--) {
            res <<= 16;
            res |= ((int)data[i]) & 0xFFFF;
        }
        
        res >>>= (startBit & 15);
        return (int)(res & ((1L << numOfBits) - 1));
    }

    public static long getLong(short[] data, int startBit, int numOfBits)
    {
        if (numOfBits > 32) {
            return (((long)getInt(data, startBit + 32, numOfBits - 32)) << 32)
                        | (getInt(data, startBit, 32) & ((1L << 32) - 1));
        } else {
            return ((long)getInt(data, startBit, numOfBits)) & ((1L << 32) - 1);
        }
    }

    public static boolean getBoolean(short[] data, int startBit)
    {
        return (data[startBit / 16] & (1L << (startBit % 16))) != 0L;
    }
    
    public static int setInt(short[] data, int value, int startBit, int numOfBits)
    {
        long val = value;
        
        int lastBit = startBit + numOfBits - 1;
        int startByte = startBit / 16;
        int lastByte = lastBit / 16;
        
        if ((startBit & 15) != 0) {
            val <<= (startBit & 15);
            val |= ((int)data[startByte]) & ((1 << (startBit & 15)) - 1);
            numOfBits += (startBit & 15);
        }
        
        if ((numOfBits & 15) != 0) {
            long d = ((long)data[lastByte]) & 0xFFFF;
            d >>>= (numOfBits & 15);
            val |= (d << numOfBits);
        }
        
        for (int i=startByte; i<=lastByte; i++) {
            data[i] = (short)(val & 0xFFFF);
            val >>= 16;
        }
        
        return lastBit + 1;
    }
    
    public static int setLong(short[] data, long value, int startBit, int numOfBits)
    {
        if (numOfBits > 32) {
            setInt(data, (int)value, startBit, 32);
            return setInt(data, (int)(value >> 32), startBit + 32, numOfBits - 32);
        } else {
            return setInt(data, (int)value, startBit, numOfBits);
        }
    }
    
    static int setBoolean(short[] data, boolean value, int startBit)
    {
        if (value) {
            data[startBit / 16] |=   1 << (startBit % 16);
        } else {
            data[startBit / 16] &= ~(1 << (startBit % 16));
        }
        return startBit + 1;
    }
    
    public static void print(short[] data)
    {
        for (int i=0; i<data.length; i++) {
            for (int j=0; j<16; j++) {
                System.out.print((data[i] & (1 << j)) != 0 ? "1" : "0");
            }
        }
        System.out.println();
    }
    
    public static void print(short[] data, int startBit, int numOfBits)
    {
        print(data);
        for (int i=0; i<startBit; i++) System.out.print(" ");
        for (int i=0; i<numOfBits; i++) System.out.print("-");
    }
    
    /*================================================================================*/    

    private static void test()
    {
        int LEN = 16;
        int ITER = 1000;
        java.util.Random rand = new java.util.Random(13);
        
        byte[] data = new byte[LEN];
        
        System.out.println("Testing byte[]");
        
        System.out.println("Testing Int");       
        clear(data);
        long time = System.currentTimeMillis(); long numOfOps = 0;
        for (int numOfBits = 1; numOfBits <= 32; numOfBits++) {
            for (int startBit = 0; startBit <= 8 * LEN - numOfBits; startBit++) {
                for (int i=0; i<ITER; i++) {
                    int val = rand.nextInt() & ((1 << numOfBits) - 1);
                    setInt(data, val, startBit, numOfBits);
                    int res = getInt(data, startBit, numOfBits);
                    if (res != val) {
                        System.out.println(val + " " + startBit + " " + numOfBits);
                        System.out.println(res);
                        System.out.println(val - res);
                        print(data, startBit, numOfBits);
                        System.exit(0);
                    }
                    numOfOps++;
                }
            }
        }
        System.out.println(numOfOps + " ops in " + (System.currentTimeMillis() - time) + "ms");
        
        System.out.println("Testing Long");
        clear(data);
        time = System.currentTimeMillis(); numOfOps = 0;
        for (int numOfBits = 1; numOfBits <= 64; numOfBits++) {
            for (int startBit = 0; startBit <= 8 * LEN - numOfBits; startBit++) {
                for (int i=0; i<ITER; i++) {
                    long val = rand.nextLong(); if (numOfBits < 64) val &= (1L << numOfBits) -1;
                    setLong(data, val, startBit, numOfBits);
                    long res = getLong(data, startBit, numOfBits);
                    if (res != val) {
                        System.out.println(val + " " + startBit + " " + numOfBits);
                        System.out.println(res);
                        System.out.println(val - res);
                        print(data, startBit, numOfBits);
                        System.exit(0);
                    }
                    numOfOps++;
                }
            }
        }
        System.out.println(numOfOps + " ops in " + (System.currentTimeMillis() - time) + "ms");
        
        System.out.println("ok");
        System.out.println();
    }
    
    private static void testShort()
    {
        int LEN = 8;
        int ITER = 1000;
        java.util.Random rand = new java.util.Random(13);
        
        short[] data = new short[LEN];
        
        System.out.println("Testing short[]");
        
        System.out.println("Testing Int");
        clear(data);
        long time = System.currentTimeMillis(); long numOfOps = 0;
        for (int numOfBits = 1; numOfBits <= 32; numOfBits++) {
            for (int startBit = 0; startBit <= 8 * LEN - numOfBits; startBit++) {
                for (int i=0; i<ITER; i++) {
                    int val = rand.nextInt() & ((1 << numOfBits) - 1);
                    setInt(data, val, startBit, numOfBits);
                    int res = getInt(data, startBit, numOfBits);
                    if (res != val) {
                        System.out.println(val + " " + startBit + " " + numOfBits);
                        System.out.println(res);
                        System.out.println(val - res);
                        print(data, startBit, numOfBits);
                        System.exit(0);
                    }
                    numOfOps++;
                }
            }
        }
        System.out.println(numOfOps + " ops in " + (System.currentTimeMillis() - time) + "ms");
        
        System.out.println("Testing Long");
        clear(data);
        time = System.currentTimeMillis(); numOfOps = 0;
        for (int numOfBits = 1; numOfBits <= 64; numOfBits++) {
            for (int startBit = 0; startBit <= 8 * LEN - numOfBits; startBit++) {
                for (int i=0; i<ITER; i++) {
                    long val = rand.nextLong(); if (numOfBits < 64) val &= (1L << numOfBits) -1;
                    setLong(data, val, startBit, numOfBits);
                    long res = getLong(data, startBit, numOfBits);
                    if (res != val) {
                        System.out.println(val + " " + startBit + " " + numOfBits);
                        System.out.println(res);
                        System.out.println(val - res);
                        print(data, startBit, numOfBits);
                        System.exit(0);
                    }
                    numOfOps++;
                }
            }
        }
        System.out.println(numOfOps + " ops in " + (System.currentTimeMillis() - time) + "ms");
        
        System.out.println("ok");
        System.out.println();
    }
    
    public static void main(String[] args)
    {
        test();
        testShort();
    }
}