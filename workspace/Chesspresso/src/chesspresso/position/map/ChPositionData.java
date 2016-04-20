/*
 * ChPositionData.java
 *
 * Created on 27. Juni 2001, 16:14
 */

package chesspresso.position.map;

import chesspresso.game.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class ChPositionData extends ChAbstractPositionData
{
    
    protected long m_hashCode;
    protected byte[] m_data;
//    protected int m_whiteWins;
//    protected int m_draws;
//    protected int m_blackWins;
//    protected long m_whiteElos;
//    protected int m_whiteEloGames;
//    protected long m_blackElos;
//    protected int m_blackEloGames;
//    protected int m_firstOccurrence;

    protected short[] m_movesPlayed;
    
    /*================================================================================*/
    
    public long getHashCode()        {return m_hashCode;}
    public int  getWhiteWins()       {return BitArray.getInt(m_data,   0, 24);}
    public int  getDraws()           {return BitArray.getInt(m_data,  24, 24);}
    public int  getBlackWins()       {return BitArray.getInt(m_data,  48, 24);}
    public long getWhiteElos()       {return BitArray.getInt(m_data,  72, 48);}
    public int  getWhiteEloGames()   {return BitArray.getInt(m_data, 120, 24);}
    public long getBlackElos()       {return BitArray.getInt(m_data, 144, 48);}
    public int  getBlackEloGames()   {return BitArray.getInt(m_data, 192, 24);}
    public int  getFirstOccurrence() {return BitArray.getInt(m_data, 216, 16);}

    public short[] getPlayedMoves() {return m_movesPlayed;}
    
    public void setHashCode(long val)       {m_hashCode = val;}
    public void setWhiteWins(int val)       {BitArray.setInt( m_data, val,   0, 24);}
    public void setDraws(int val)           {BitArray.setInt( m_data, val,  24, 24);}
    public void setBlackWins(int val)       {BitArray.setInt( m_data, val,  48, 24);}
    public void setWhiteElos(long val)      {BitArray.setLong(m_data, val,  72, 48);}
    public void setWhiteEloGames(int val)   {BitArray.setInt( m_data, val, 120, 24);}
    public void setBlackElos(long val)      {BitArray.setLong(m_data, val, 144, 48);}
    public void setBlackEloGames(int val)   {BitArray.setInt( m_data, val, 192, 24);}
    public void setFirstOccurrence(int val) {BitArray.setInt( m_data, val, 216, 16);}
    
    public void setPlayedMoves(short[] val) {m_movesPlayed = val;}
    
    /*================================================================================*/
    
    protected ChPositionData(long hashCode)
    {
        m_hashCode        = hashCode;
        m_data = new byte[(228 + 7) / 8];
        setFirstOccurrence(9999);
        m_movesPlayed = null;
    }

    ChPositionData(ChFilePositionMap map) throws IOException
    {
        m_movesPlayed = null;
        read(map, true);
    }
    
    ChPositionData(long hashCode, ChFilePositionMap map) throws IOException
    {
        m_hashCode = hashCode;
        m_movesPlayed = null;
        read(map, false);
    }
    
    ChPositionData(DataInput in, ChFilePositionMap map) throws IOException
    {
        m_movesPlayed = null;
        read(in, map, true);
    }
    
    ChPositionData(long hashCode, DataInput in, ChFilePositionMap map) throws IOException
    {
        m_hashCode = hashCode;
        m_movesPlayed = null;
        read(in, map, false);
    }
    
    public ChPositionData(PositionData data1, PositionData data2)
    {
        setHashCode(data1.getHashCode());
        m_data = new byte[(228 + 7) / 8];
        
        setWhiteWins(    data1.getWhiteWins()     + data2.getWhiteWins());
        setDraws(        data1.getDraws()         + data2.getDraws());
        setBlackWins(    data1.getBlackWins()     + data2.getBlackWins());
        setWhiteElos(    data1.getWhiteElos()     + data2.getWhiteElos());
        setWhiteEloGames(data1.getWhiteEloGames() + data2.getWhiteEloGames());
        setBlackElos(    data1.getBlackElos()     + data2.getBlackElos());
        setBlackEloGames(data1.getBlackEloGames() + data2.getBlackEloGames());
        
        setFirstOccurrence(Math.min(data1.getFirstOccurrence(), data2.getFirstOccurrence()));
        setPlayedMoves(combineMovesPlayed(data1.getPlayedMoves(), data2.getPlayedMoves()));
    }
    
//    public IChPositionData createCopy()
//    {
//        ChPositionData data = new ChPositionData(getHashCode());
//        data.setWhiteWins(getWhiteWins());
//        data.setDraws(getDraws());
//        data.setBlackWins(getBlackWins());
//        data.setWhiteElos(getWhiteElos());
//        data.setWhiteEloGames(getWhiteEloGames());
//        data.setBlackElos(getBlackElos());
//        data.setBlackEloGames(getBlackEloGames());
//        data.setFirstOccurrence(getFirstOccurrence());
//        data.m_movesPlayed = new short[m_movesPlayed.length];
//        System.arraycopy(m_movesPlayed, 0, data.m_movesPlayed, 0, m_movesPlayed.length);
//        
//        return data;
//    }
    
    /*================================================================================*/
    
    private short[] combineMovesPlayed(short[] moves1, short[] moves2)
    {
        if (moves1 == null) {short[] m = new short[moves2.length]; System.arraycopy(moves2, 0, m, 0, moves2.length); return m;};
        if (moves2 == null) {short[] m = new short[moves1.length]; System.arraycopy(moves1, 0, m, 0, moves1.length); return m;};
        
        short[] res = new short[moves1.length + moves2.length];
        int i=0, j=0, k=0;
        while (i < moves1.length || j < moves2.length) {
            if      (i >= moves1.length)    res[k++] = moves2[j++];
            else if (j >= moves2.length)    res[k++] = moves1[i++];
            else if (moves1[i] < moves2[j]) res[k++] = moves1[i++];
            else if (moves1[i] > moves2[j]) res[k++] = moves2[j++];
            else                           {res[k++] = moves1[i++]; j++;}
        }
        if (k < res.length) {
            short[] newRes = new short[k];
            System.arraycopy(res, 0, newRes, 0, k);
            res = newRes;
        }
        return res;
    }
    
    /*================================================================================*/

//    public final long getHashCode() {return m_hashCode;}
//    
//    public final int getWhiteWins() {return m_whiteWins;}
//    public final int getDraws() {return m_draws;}
//    public final int getBlackWins() {return m_blackWins;}
//    
//    public final int getFirstOccurrence() {return m_firstOccurrence;}
//    
//    public final long getWhiteElos() {return m_whiteElos;} 
//    public final long getBlackElos() {return m_blackElos;} 
//      
//    public final int getWhiteEloGames() {return m_whiteEloGames;}
//    public final int getBlackEloGames() {return m_blackEloGames;}

    public final boolean wasMovePlayed(short move)
    {
        return Arrays.binarySearch(m_movesPlayed, move) >= 0;
    }
    
    public final GameModel getGameModel() {return null;}
    
    /*================================================================================*/
    
    public PositionData add(PositionData data)
    {
        if (data == null) return this;  // =====>
        
        setWhiteWins(    getWhiteWins()     + data.getWhiteWins());
        setDraws(        getDraws()         + data.getDraws());
        setBlackWins(    getBlackWins()     + data.getBlackWins());
        setWhiteElos(    getWhiteElos()     + data.getWhiteElos());
        setWhiteEloGames(getWhiteEloGames() + data.getWhiteEloGames());
        setBlackElos(    getBlackElos()     + data.getBlackElos());
        setBlackEloGames(getBlackEloGames() + data.getBlackEloGames());
        
        if (data.getFirstOccurrence() < getFirstOccurrence())
            setFirstOccurrence(data.getFirstOccurrence());
        m_movesPlayed = combineMovesPlayed(m_movesPlayed, data.getPlayedMoves()); 
        
        return this;
    }
    
    public PositionData add(Game game, short nextMove)
    {
        return add(new DefaultPositionDataGame(getHashCode(), game.getModel(), nextMove));
    }
   
    /*================================================================================*/
    
    static final int bitsForHashCode    = 64; 
    static final int bitsForElo         = 12;
    static final int bitsForYear        = 12;
    static final int bitsForMovesFlag   =  1;
    static final int bitsForMoves       = 64;
        
    public static int getSize(int largestGameCounter)
    {
        int bitsForGameCounter = BitArray.getBitsForNumber(largestGameCounter);
        int bits = bitsForHashCode + 5 * bitsForGameCounter + 2 * bitsForElo + bitsForYear + bitsForMovesFlag + bitsForMoves;
        return (bits + 7) / 8;
    }
    
    private void read(ChFilePositionMap map, boolean readHashCode) throws IOException
    {
        read(map.getFile(), map, readHashCode);
    }
    
    private void read(DataInput in, ChFilePositionMap map, boolean readHashCode) throws IOException
    {
//        Logger.log(new Exception("ChPositionData.read deprecated"), this);
        
//        int largestGameCounter = map.getLargestGameCounter();
//        
//        if (readHashCode)
//            m_hashCode = in.readLong();
//        
//        int bitsForGameCounter = ChBitArray.getBitsForNumber(largestGameCounter);
//        byte[] data = new byte[getSize(largestGameCounter) - 8];
//        in.readFully(data);        
////        System.out.println("read: largestGameCounter=" + largestGameCounter + " size=" + getSize(largestGameCounter)); 
//        
//        int index = 0;
//        m_whiteWins       = (int) ChBitArray.get(data, index, bitsForGameCounter);           index += bitsForGameCounter;
//        m_draws           = (int) ChBitArray.get(data, index, bitsForGameCounter);           index += bitsForGameCounter;
//        m_blackWins       = (int) ChBitArray.get(data, index, bitsForGameCounter);           index += bitsForGameCounter;
//        m_whiteEloGames   = (int) ChBitArray.get(data, index, bitsForGameCounter);           index += bitsForGameCounter;
//        m_whiteElos       =       ChBitArray.get(data, index, bitsForElo) * m_whiteEloGames; index += bitsForElo;
//        m_blackEloGames   = (int) ChBitArray.get(data, index, bitsForGameCounter);           index += bitsForGameCounter;
//        m_blackElos       =       ChBitArray.get(data, index, bitsForElo) * m_blackEloGames; index += bitsForElo;
//        m_firstOccurrence = (int) ChBitArray.get(data, index, bitsForYear);                  index += bitsForYear;
//        boolean movesFlag =       ChBitArray.getBoolean(data, index);                        index += 1;
//        long movesPtr     =       ChBitArray.get(data, index, bitsForMoves);                 index += bitsForMoves;
//   
//        if (movesFlag) {
//            // encode moves in pointer
//           short numOfMovesPlayed = 0;
//           if      ((movesPtr & 0xFFFF000000000000L) != 0) numOfMovesPlayed = 4;
//           else if ((movesPtr & 0x0000FFFF00000000L) != 0) numOfMovesPlayed = 3;
//           else if ((movesPtr & 0x00000000FFFF0000L) != 0) numOfMovesPlayed = 2;
//           else if ((movesPtr & 0x000000000000FFFFL) != 0) numOfMovesPlayed = 1;
//           m_movesPlayed = new short[numOfMovesPlayed];
//           for (int i=0; i<numOfMovesPlayed; i++) {m_movesPlayed[i] = (short)movesPtr; movesPtr >>>= 16;}
//        } else {
//            synchronized(map) {
//                // pointer, put moves into addons
//                map.getFile().seek(map.getStartOfAddons() + movesPtr);
//                short numOfMovesPlayed = map.getFile().readShort();
////                System.out.println(numOfMovesPlayed);
//                m_movesPlayed = new short[numOfMovesPlayed];
//                for (int i=0; i<m_movesPlayed.length; i++) m_movesPlayed[i] = map.getFile().readShort();
//            }
//        }
    }
    
    public void write(DataOutput out, int largestGameCounter, int largestCounterDifference, DataOutputStream movesOut) throws IOException
    {
//        Logger.log(new Exception("ChPositionData.write deprecated"), this);
        
//        out.writeLong(m_hashCode);
//        
//        int bitsForGameCounter = ChBitArray.getBitsForNumber(largestGameCounter);
//        byte[] data = new byte[getSize(largestGameCounter) - 8];
//       
//        int index = 0;
//        index = ChBitArray.put(data, m_whiteWins          , index, bitsForGameCounter);
//        index = ChBitArray.put(data, m_draws              , index, bitsForGameCounter);
//        index = ChBitArray.put(data, m_blackWins          , index, bitsForGameCounter);
//        index = ChBitArray.put(data, m_whiteEloGames      , index, bitsForGameCounter);
//        index = ChBitArray.put(data, getWhiteEloAverage() , index, bitsForElo);
//        index = ChBitArray.put(data, m_blackEloGames      , index, bitsForGameCounter);
//        index = ChBitArray.put(data, getBlackEloAverage() , index, bitsForElo);
//        index = ChBitArray.put(data, m_firstOccurrence    , index, bitsForYear);
//        
//        if (m_movesPlayed.length <= 4) {
////            System.out.print(".");
//            // encode moves in pointer
//            long moves = 0L;
//            for (int i=m_movesPlayed.length - 1; i >= 0; i--) {moves = (moves << 16) + ((long)m_movesPlayed[i] & 0xFFFFL);}
//            index = ChBitArray.putBoolean(data, true          , index);
//            index = ChBitArray.put(data, moves                , index, bitsForMoves);        
//        } else {
////            System.out.print("x");
//            // pointer, put moves into addons
//            index = ChBitArray.putBoolean(data, false         , index);
//            index = ChBitArray.put(data, (long)movesOut.size(), index, bitsForMoves);
//            movesOut.writeShort(m_movesPlayed.length);
//            for (int i=0; i<m_movesPlayed.length; i++) movesOut.writeShort(m_movesPlayed[i]);
//        }
//        out.write(data);
    }
        
    /*================================================================================*/
    
    public String toString() {return getHashCode() + ": " + getNumOfGames() + " ( +" + getWhiteWins() + " =" + getDraws() + " -" + getBlackWins() + " )"; }
    
}