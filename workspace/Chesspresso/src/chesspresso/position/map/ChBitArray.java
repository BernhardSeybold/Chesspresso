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
public final class ChBitArray
{
    
    static int getBytesForNumber(long num)
    {
        int bytes = 0;
        while (num != 0) {num >>>= 8; bytes++;}
        return bytes;
    }
    
    static int getBitsForNumber(long num)
    {
        int bits = 0;
        while (num != 0) {num >>>= 1; bits++;} 
        return bits;
    }
    
    /*================================================================================*/
    
    static int readInt(DataInput in, int numOfBytes) throws IOException
    {
        int res = 0;
        for (; numOfBytes > 0; numOfBytes--) {
            res = (res << 8) | (((int)in.readByte()) & 0xFF);
        }
	return res;
    }
    
    static long readLong(DataInput in, int numOfBytes) throws IOException
    {
        long res = 0;
        for (; numOfBytes > 0; numOfBytes--) {
            res = (res << 8) | (((long)in.readByte()) & 0xFF);
        }
	return res;
    }
    
    static void writeInt(DataOutput out, int value, int numOfBytes) throws IOException
    {
        for (; numOfBytes > 0; numOfBytes--) {
            out.write((int)(value >>> (8 * numOfBytes - 8) & 0xFF));
        }
    }
    
    static void writeLong(DataOutput out, long value, int numOfBytes) throws IOException
    {
        for (; numOfBytes > 0; numOfBytes--) {
            out.write((int)(value >>> (8 * numOfBytes - 8) & 0xFF));
        }
    }
    
    /*================================================================================*/
    
    static long get2(byte[] data, int startBit, int numOfBits)
    {
        long res = 0L;
        
        for (int i = startBit + numOfBits - 1; i >= startBit; i--) {
            res <<= 1;
            if ((data[i / 8] & (1 << (i % 8))) != 0) res++;
        }
        return res;
    }
    
    static long get(byte[] data, int startBit, int numOfBits)
    {
        long res = 0L;
        
        int startByte = startBit / 8;
        int lastByte = (startBit + numOfBits - 1) / 8;
        
        for (int i = lastByte; i >= startByte; i--) {
            res <<= 8;
            int d = data[i];
            if (d<0) d += 256;
            res += d;
        }
        
        res >>>= (startBit & 7);
        return res & ((1L << numOfBits) - 1);
    }
    
    static boolean getBoolean(byte[] data, int startBit)
    {
        return (data[startBit / 8] & (1L << (startBit % 8))) != 0L;
    }
    
    static int put(byte[] data, long value, int startBit, int numOfBits)
    {
        for (int i = startBit; i < startBit + numOfBits; i++) {
            if ((value & 0x1) != 0) {
                data[i / 8] |=   1 << (i % 8);
            } else {
                data[i / 8] &= ~(1 << (i % 8));
            }
            value >>>= 1;
        }
        return startBit + numOfBits;
    }
    
    static int putBoolean(byte[] data, boolean value, int startBit)
    {
        if (value) {
            data[startBit / 8] |=   1 << (startBit % 8);
        } else {
            data[startBit / 8] &= ~(1 << (startBit % 8));
        }
        return startBit + 1;
    }
    
    /*================================================================================*/    

    public static void main(String[] args)
    {
        byte b = 0;
        
        b |= 27; System.out.println(b + " " + (int)b);
        b |= 81; System.out.println(b + " " + (int)b);
        b |= 240; System.out.println(b + " " + (int)b);
        
        byte[] data = new byte[5];
        
        int val = 98765;
        put(data, val, 5, 17);
        if (get(data, 5, 17) != val) {
            System.out.println("Not ok. i=" + val + " data=" + get(data, 5, 17) + " diff=" + (val - get(data, 5, 17)));
            for (int i=0; i<data.length; i++) {
                System.out.println(Integer.toBinaryString(data[i]));
            }
        } else {
            System.out.println("Ok.");
        }
    }
}