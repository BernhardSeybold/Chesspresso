/*
 * ChFilePositionMap.java
 *
 * Created on 28. Juni 2001, 10:44
 */

package chesspresso.position.map;

import chesspresso.game.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class ChFilePositionMap extends ChAbstractPositionReadMap implements PositionReadMap
{
    private static final boolean EXTRA_CHECKS = true;
    
    // A position map file consists of six parts:
    //
    // 1) The header
    //     - numOfEntries                   int   4
    //     - numOfPositions                 int   4
    //     - numOfGames                     int   4
    //     - startOfSinglePositionMaps (3)  long  8
    //     - startOfGameIndices (4)         long  8
    //     - startOfGames (5)               long  8
    //     - startOfAddons (6)              long  8
    //     - largestGameCounter             int   4
    //                                         = 48
    //     
    // 2) The position data with more than one game
    //     - records are read and written by ChPositionData
    //     - each record has the same length determined by ChPositionData.getSize
    //     - each record starts with the 8 byte (long) hashCode
    //
    // 3) The position data with one game
    //     - records are read and written by ChPositionDataGame
    //     - each record has the same length determined by ChPositionDataGame.getSize
    //     - each record starts with the 8 byte (long) hashCode
    //     - the record contains an offset into the games section to find the corresponding game
    //
    // 4) The gameIndices
    //     - list of all gamePointers
    //     - positionDataGame index into this list
    //
    // 5) The games
    //     - records are read and written by ChGameHeaderModel and ChGameMoveModel, called
    //       by ChPositionDataGame
    //     - records have different length
    //
    // 6) The addons
    //     - additional data for ChPositionData (eg movesPlayed)
    //     - record may have different length
    
    /*================================================================================*/
    
    private class DataIterator implements PositionDataIterator
    {
        private int m_multiIndex;
        private int m_singleIndex;
        
        private DataIterator()
        {
            m_multiIndex = 0;
            m_singleIndex = 0;
        }
        
        public PositionData getNext()
        {
            try {
                if (m_multiIndex >= m_header.getNumOfMultiPos()) {
                    if (m_singleIndex >= m_header.getNumOfSinglePos()) {
                        return null;
                    } else {
                        PositionData data = readSinglePosData(m_singleIndex);
                        m_singleIndex++;
                        return data;
                    }
                } else if (m_singleIndex >= m_header.getNumOfSinglePos()) {
                    PositionData data = readMultiPosData(m_multiIndex);
                    m_multiIndex++;
                    return data;
                } else {
                    PositionData multiData = readMultiPosData(m_multiIndex);
                    PositionData singleData = readSinglePosData(m_singleIndex);
                    if (multiData.getHashCode() < singleData.getHashCode()) {
                        m_multiIndex++;
                        return multiData;
                    } else {
                        m_singleIndex++;
                        return singleData;
                    }
                }
            } catch (IOException ex) {
//                Logger.log(ex, this);
                return null;
            }
        }
    }
    
    /*================================================================================*/
    
    // TODO: private class for PositionData, read lazily (especially addons for moves)
    
    class PositionDataGame extends AbstractPositionDataGame
    {
        private int m_gameIndex;
        private short m_nextMove;
        
        /*--------------------------------------------------------------------------------*/
        
        private PositionDataGame(long hashCode, short nextMove, int gameIndex)
        {
            super(hashCode);
            m_gameIndex = gameIndex;
            m_nextMove = nextMove;
        }

        private PositionDataGame(DataInput in, int numOfHashCodeBits, long hashPrefix) throws IOException
        {
            super(-1);
            read(in, numOfHashCodeBits);
            m_hashCode += hashPrefix;
        }

//        public IChPositionData createCopy()
//        {
//            return new PositionDataGame(m_hashCode, m_nextMove, m_gameIndex);
//        }
//
        /*--------------------------------------------------------------------------------*/
        
        public PositionReadMap getMap() {return ChFilePositionMap.this;}        
        public int getGameIndex() {return m_gameIndex;}
        protected short getNextMove() {return m_nextMove;}
        
        public GameModel getGameModel()
        {
            return getMap().getGameModel(m_gameIndex);
        }
        
        /*--------------------------------------------------------------------------------*/

        private void read(DataInput in, int numOfHashCodeBits) throws IOException
        {
            if (numOfHashCodeBits > 0)
                m_hashCode = BitArray.readLong(in, (numOfHashCodeBits + 7) / 8);
//            if (m_hashCode <= 0) Logger.log("Illegal hashcode " + m_hashCode, this);
            m_nextMove = (short)BitArray.readInt(in, 2);
            m_gameIndex = BitArray.readInt(in, BitArray.getBytesForNumber(getMap().getNumOfGames()));
        }
        
    }
    
    private static int getSizeOfSinglePosData(Header header)
    {
        int bitsForHashCode = 63 - header.getSinglePosIndexBits();
        return (bitsForHashCode + 7) / 8 + 2 + BitArray.getBytesForNumber(header.getNumOfGames());
    }

    private static void writeSinglePosData(AbstractPositionDataGame data, DataOutput out, Header header) throws IOException
    {
        BitArray.writeLong(out, data.getHashCode(), (63 - header.getSinglePosIndexBits() + 7) / 8);
        BitArray.writeInt(out, data.getNextMove(), 2);
        BitArray.writeInt(out, data.getGameIndex() + data.getMap().getGameOffset(), BitArray.getBytesForNumber(header.getNumOfGames()));
    }
    
    /*================================================================================*/
    
    class MyPositionData extends ChAbstractPositionData
    {
        private long m_hashPrefix;
        private byte[] m_data;
        private short[] m_movesPlayed;
        
        private MyPositionData(long hashPrefix) throws IOException
        {
            super();
            m_hashPrefix = hashPrefix;
            m_movesPlayed = null;
            read();
//            System.out.println("new PositionData " + m_data.length + " bytes " + this);
        }

        private void read() throws IOException
        {
            DataInput in = m_dataFile;
            m_data = new byte[getSizeOfMultiPosData(m_header)];
            in.readFully(m_data);
//            for (int i=0; i<m_data.length; i++) {System.out.print(m_data[i] + " ");} System.out.println();
        }
        
        public long getHashCode()        {return BitArray.getLong(m_data, HC_S,  bitsForHashCode) + m_hashPrefix;}
        public int  getWhiteWins()       {return BitArray.getInt (m_data, WW_S,  bitsForGameCounter);}
        public int  getDraws()           {return BitArray.getInt (m_data, D_S,   bitsForGameCounter);}
        public int  getBlackWins()       {return BitArray.getInt (m_data, BW_S,  bitsForGameCounter);}
        public int  getWhiteEloAverage() {return BitArray.getInt (m_data, WE_S,  bitsForElo);}
        public int  getWhiteEloGames()   {return BitArray.getInt (m_data, WEG_S, bitsForGameCounter);}
        public int  getBlackEloAverage() {return BitArray.getInt (m_data, BE_S,  bitsForElo);}
        public int  getBlackEloGames()   {return BitArray.getInt (m_data, BEG_S, bitsForGameCounter);}
        public int  getFirstOccurrence() {return BitArray.getInt (m_data, FO_S,  bitsForYear);}
        
        public short[] getPlayedMoves() 
        {
            if (m_movesPlayed == null) {
                boolean movesFlag = BitArray.getBoolean(m_data, MOF_S);
                long movesPtr     = BitArray.getLong(m_data, MO_S, bitsForMoves);

                if (movesFlag) {
                    // encode moves in pointer
                   short numOfMovesPlayed = 0;
                   if      ((movesPtr & 0xFFFF000000000000L) != 0) numOfMovesPlayed = 4;
                   else if ((movesPtr & 0x0000FFFF00000000L) != 0) numOfMovesPlayed = 3;
                   else if ((movesPtr & 0x00000000FFFF0000L) != 0) numOfMovesPlayed = 2;
                   else if ((movesPtr & 0x000000000000FFFFL) != 0) numOfMovesPlayed = 1;
                   m_movesPlayed = new short[numOfMovesPlayed];
                   for (int i=0; i<numOfMovesPlayed; i++) {m_movesPlayed[i] = (short)movesPtr; movesPtr >>>= 16;}
                } else {
                    try {
                        synchronized(ChFilePositionMap.this) {
                            // pointer, put moves into addons
                            m_dataFile.seek(m_header.getStartOfMultiPosAddons() + movesPtr);
                            short numOfMovesPlayed = m_dataFile.readShort();
            //                System.out.println(numOfMovesPlayed);
                            m_movesPlayed = new short[numOfMovesPlayed];
                            for (int i=0; i<m_movesPlayed.length; i++) m_movesPlayed[i] = m_dataFile.readShort();
                        }
                    } catch(IOException ex) {
//                        Logger.log(ex, this);
                    }
                }
            }
            
            return m_movesPlayed;
        }

        public PositionData add(PositionData data)
        {
            return new ChPositionData(this, data);
        }
        
        public PositionData add(Game game, short nextMove)
        {
            return new ChPositionData(this, new DefaultPositionDataGame(getHashCode(), game.getModel(), nextMove));
        }
        
//        public IChPositionData createCopy() {
//            throw new UnsupportedOperationException("createCopy");
//        }
        
        
        
        
        
//        private PositionData(long hashPrefix) throws IOException
//        {
//            super(0L);
//            m_movesPlayed = null;
//            read();
//            m_hashCode += hashPrefix;
//        }
//    
//        private void read() throws IOException
//        {
////            Logger.log("Read temporarily unavailable", this);
//            
//    int bitsForElo         = 12;
//    int bitsForYear        = 12;
//    int bitsForMovesFlag   =  1;
//    int bitsForMoves       = 64;
//    
////            int largestGameCounter = map.getLargestGameCounter();
//            DataInput in = m_dataFile;
//            int numOfHashCodeBits = 63 - m_header.getMultiPosIndexBits();
//
//            int bitsForGameCounter = BitArray.getBitsForNumber(m_header.getLargestGameCounter());
//            byte[] data = new byte[getSizeOfMultiPosData(m_header)];
//            in.readFully(data);
//    //        System.out.println("read: largestGameCounter=" + largestGameCounter + " size=" + getSize(largestGameCounter)); 
//
//            int index = 0;
//            setHashCode(        BitArray.getLong(data, index, numOfHashCodeBits));               index += numOfHashCodeBits;
//            setWhiteWins(       BitArray.getInt( data, index, bitsForGameCounter));              index += bitsForGameCounter;
//            setDraws(           BitArray.getInt( data, index, bitsForGameCounter));              index += bitsForGameCounter;
//            setBlackWins(       BitArray.getInt( data, index, bitsForGameCounter));              index += bitsForGameCounter;
//            setWhiteEloGames(   BitArray.getInt( data, index, bitsForGameCounter));              index += bitsForGameCounter;
//            setWhiteElos(       BitArray.getLong(data, index, bitsForElo) * getWhiteEloGames()); index += bitsForElo;
//            setBlackEloGames(   BitArray.getInt( data, index, bitsForGameCounter));              index += bitsForGameCounter;
//            setBlackElos(       BitArray.getLong(data, index, bitsForElo) * getBlackEloGames()); index += bitsForElo;
//            setFirstOccurrence( BitArray.getInt( data, index, bitsForYear));                     index += bitsForYear;
//            boolean movesFlag = BitArray.getBoolean(data, index);                                index += 1;
//            long movesPtr     = BitArray.getLong(data, index, bitsForMoves);                     index += bitsForMoves;
//
//            if (movesFlag) {
//                // encode moves in pointer
//               short numOfMovesPlayed = 0;
//               if      ((movesPtr & 0xFFFF000000000000L) != 0) numOfMovesPlayed = 4;
//               else if ((movesPtr & 0x0000FFFF00000000L) != 0) numOfMovesPlayed = 3;
//               else if ((movesPtr & 0x00000000FFFF0000L) != 0) numOfMovesPlayed = 2;
//               else if ((movesPtr & 0x000000000000FFFFL) != 0) numOfMovesPlayed = 1;
//               m_movesPlayed = new short[numOfMovesPlayed];
//               for (int i=0; i<numOfMovesPlayed; i++) {m_movesPlayed[i] = (short)movesPtr; movesPtr >>>= 16;}
//            } else {
//                synchronized(ChFilePositionMap.this) {
//                    // pointer, put moves into addons
//                    m_dataFile.seek(m_header.getStartOfMultiPosAddons() + movesPtr);
//                    short numOfMovesPlayed = m_dataFile.readShort();
//    //                System.out.println(numOfMovesPlayed);
//                    m_movesPlayed = new short[numOfMovesPlayed];
//                    for (int i=0; i<m_movesPlayed.length; i++) m_movesPlayed[i] = m_dataFile.readShort();
//                }
//            }
//        }

    }
        
    
    public static int getSizeOfMultiPosData(Header header)
    {
int bitsForElo         = 12;
int bitsForYear        = 12;
int bitsForMovesFlag   =  1;
int bitsForMoves       = 64;

        int bitsForGameCounter = BitArray.getBitsForNumber(header.getLargestGameCounter());
        int bitsForHashCode = 63 - header.getMultiPosIndexBits();
        int bits = bitsForHashCode + 5 * bitsForGameCounter + 2 * bitsForElo + bitsForYear + bitsForMovesFlag + bitsForMoves;
        return (bits + 7) / 8;
    }
    
        
    private static void writeMultiPosData(PositionData data, DataOutput out, DataOutputStream outAddons, Header header) throws IOException
    {
    int bitsForElo         = 12;
    int bitsForYear        = 12;
    int bitsForMovesFlag   =  1;
    int bitsForMoves       = 64;
    
        int bitsForGameCounter = BitArray.getBitsForNumber(header.getLargestGameCounter());
        
        byte[] bytes = new byte[header.getSizeOfMultiPosData()];       
        int index = 0;
        index = BitArray.setLong(bytes, data.getHashCode()       , index, 63 - header.getMultiPosIndexBits());
        index = BitArray.setInt( bytes, data.getWhiteWins()      , index, bitsForGameCounter);
        index = BitArray.setInt( bytes, data.getDraws()          , index, bitsForGameCounter);
        index = BitArray.setInt( bytes, data.getBlackWins()      , index, bitsForGameCounter);
        index = BitArray.setInt( bytes, data.getWhiteEloAverage(), index, bitsForElo);
        index = BitArray.setInt( bytes, data.getWhiteEloGames()  , index, bitsForGameCounter);
        index = BitArray.setInt( bytes, data.getBlackEloAverage(), index, bitsForElo);
        index = BitArray.setInt( bytes, data.getBlackEloGames()  , index, bitsForGameCounter);
        index = BitArray.setInt( bytes, data.getFirstOccurrence(), index, bitsForYear);
//        BitArray.print(bytes);
        
        short[] movesPlayed = data.getPlayedMoves();
        if (movesPlayed.length <= 4) {
//            System.out.print(".");
            // encode moves in pointer
            long moves = 0L;
            for (int i=movesPlayed.length - 1; i >= 0; i--) {moves = (moves << 16) + ((long)movesPlayed[i] & 0xFFFFL);}
            index = BitArray.setBoolean(bytes, true           , index);
            index = BitArray.setLong(bytes, moves                 , index, bitsForMoves);        
        } else {
//            System.out.print("x");
            // pointer, put moves into addons
            index = BitArray.setBoolean(bytes, false          , index);
            index = BitArray.setLong(bytes, (long)outAddons.size(), index, bitsForMoves);
            outAddons.writeShort(movesPlayed.length);
            for (int i=0; i<movesPlayed.length; i++) outAddons.writeShort(movesPlayed[i]);
        }
        out.write(bytes);
    }
    /*================================================================================*/
    
    private class MyGameModelIterator implements GameModelIterator
    {
        private int m_index;

		MyGameModelIterator() {m_index = 0;}
        
        public boolean hasNext() {return m_index < m_header.getNumOfGames();}
        public Object next() {return getGameModel(m_index++);}
        public GameModel nextGameModel() {return getGameModel(m_index++);}
        public void remove() {throw new UnsupportedOperationException("Remove not supported in GameModelIterator");}
    }
    
    /*================================================================================*/
    
    public static javax.swing.filechooser.FileFilter getFileFilter()
    {
        return new javax.swing.filechooser.FileFilter() {
            public boolean accept(File file) {return file.isDirectory() || isPositionFile(file.getName());}
            public String getDescription() {return "Position map files (*.pm)";}
        };
    }
    
    public static boolean isPositionFile(String name) {return name != null && name.endsWith(".pm");}
        
    /*================================================================================*/
    // Header
    
    private static class Header
    {
        static final int SIZE = 128;
        
        // data                                        size start total
        int m_numOfEntries = 0;                       // 4    0     4
        int m_numOfMultiPos = 0;                      // 4    4     8
        int m_numOfSinglePos = 0;                     // 4    8    12
        int m_numOfGames = 0;                         // 4   12    16
        long m_startOfMultiPos = 0;                   // 8   16    24
        long m_startOfSinglePos = 0;                  // 8   24    32
        long m_startOfGames = 0;                      // 8   32    40
        int m_largestGameCounter = 0;                 // 4   40    44
        byte m_multiPosIndexBits = 0;                 // 1   44    45
        byte m_multiPosIndexPointerBits = 0;          // 1   45    46
        byte m_singlePosIndexBits = 0;                // 1   46    47
        byte m_singlePosIndexPointerBits = 0;         // 1   47    48
        byte m_variationDepth = 0;                    // 1   49    49
        
        public Header() {}
        
        public int  getNumOfEntries()              {return m_numOfEntries;}
        public int  getNumOfMultiPos()             {return m_numOfMultiPos;}
        public int  getNumOfSinglePos()            {return m_numOfSinglePos;}
        public long getStartOfMultiPos()           {return m_startOfMultiPos;}
        public long getStartOfSinglePos()          {return m_startOfSinglePos;}
        public int  getLargestGameCounter()        {return m_largestGameCounter;}
        public byte getMultiPosIndexBits()         {return m_multiPosIndexBits;}
        public byte getMultiPosIndexPointerBits()  {return m_multiPosIndexPointerBits;}
        public byte getSinglePosIndexBits()        {return m_singlePosIndexBits;}
        public byte getSinglePosIndexPointerBits() {return m_singlePosIndexPointerBits;}
        public byte getVariationDepth()            {return m_variationDepth;}
        
        public long getEndOfMultiPosData()          {return getStartOfMultiPosAddons();}
        public long getEndOfSinglePosData()         {return getStartOfGames();}
//        public long getEndOfGames()                 {return m_dataFile.length();}
        
        public int getSizeOfMultiPosData()  {return ChFilePositionMap.getSizeOfMultiPosData(this);}
        public int getSizeOfSinglePosData() {return ChFilePositionMap.getSizeOfSinglePosData(this);}
        
        public long getStartOfMultiPosData()
        {
            return m_startOfMultiPos + (((1 << m_multiPosIndexBits) - 1) * m_multiPosIndexPointerBits + 7) / 8;
        }
        
        public long getStartOfMultiPosAddons()
        {
            return getStartOfMultiPosData() + getSizeOfMultiPosData() * m_numOfMultiPos;
        }
        
        public long getStartOfSinglePosData()
        {
            return m_startOfSinglePos + (((1 << m_singlePosIndexBits) - 1) * m_singlePosIndexPointerBits + 7) / 8;
        }
        
        public int  getNumOfGames()         {return m_numOfGames;}
        public long getStartOfGames()       {return m_startOfGames;}
        public long getStartOfGameIndices() {return m_startOfGames;}
        public long getStartOfGameData()    {return m_startOfGames + m_numOfGames * 4;}
//        public long getEndOfGameData()      {return m_startOfGameDatatry{return m_dataFile.length();} catch (Exception ex) {return -1;}}
        
        public void setNumOfEntries(int val)               {m_numOfEntries = val;}
        public void setNumOfMultiPos(int val)              {m_numOfMultiPos = val;}
        public void setNumOfSinglePos(int val)             {m_numOfSinglePos = val;}
        public void setNumOfGames(int val)                 {m_numOfGames = val;}
        public void setStartOfMultiPos(long val)           {m_startOfMultiPos = val;}
        public void setStartOfSinglePos(long val)          {m_startOfSinglePos = val;}
        public void setStartOfGames(long val)              {m_startOfGames = val;}
        public void setLargestGameCounter(int val)         {m_largestGameCounter = val;}
        public void setMultiPosIndexBits(byte val)         {m_multiPosIndexBits = val;}
        public void setMultiPosIndexPointerBits(byte val)  {m_multiPosIndexPointerBits = val;}
        public void setSinglePosIndexBits(byte val)        {m_singlePosIndexBits = val;}
        public void setSinglePosIndexPointerBits(byte val) {m_singlePosIndexPointerBits = val;}
        public void setVariationDepth(byte val)            {m_variationDepth = val;}
        
        public void read(DataInput in) throws IOException
        {
            byte[] data = new byte[SIZE];
            in.readFully(data);
            DataInput dataIn = new DataInputStream(new ByteArrayInputStream(data));
            
            m_numOfEntries              = dataIn.readInt();
            m_numOfMultiPos             = dataIn.readInt();
            m_numOfSinglePos            = dataIn.readInt();
            m_numOfGames                = dataIn.readInt();
            m_startOfMultiPos           = dataIn.readLong();
            m_startOfSinglePos          = dataIn.readLong();
            m_startOfGames              = dataIn.readLong();
            m_largestGameCounter        = dataIn.readInt();
            m_multiPosIndexBits         = dataIn.readByte();
            m_multiPosIndexPointerBits  = dataIn.readByte();
            m_singlePosIndexBits        = dataIn.readByte();
            m_singlePosIndexPointerBits = dataIn.readByte();
            m_variationDepth            = dataIn.readByte();
        }
        
        public void write(DataOutput out) throws IOException
        {
            ByteArrayOutputStream data = new ByteArrayOutputStream(SIZE);
            DataOutput dataOut = new DataOutputStream(data);
            
            dataOut.writeInt(m_numOfEntries);
            dataOut.writeInt(m_numOfMultiPos);
            dataOut.writeInt(m_numOfSinglePos);
            dataOut.writeInt(m_numOfGames);
            dataOut.writeLong(m_startOfMultiPos);
            dataOut.writeLong(m_startOfSinglePos);
            dataOut.writeLong(m_startOfGames);
            dataOut.writeInt(m_largestGameCounter);
            dataOut.writeByte(m_multiPosIndexBits);
            dataOut.writeByte(m_multiPosIndexPointerBits);
            dataOut.writeByte(m_singlePosIndexBits);
            dataOut.writeByte(m_singlePosIndexPointerBits);
            dataOut.writeByte(m_variationDepth);
            
            out.write(data.toByteArray());
        }
        
        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("numOfEntries              ").append(m_numOfEntries).append('\n');
            sb.append("numOfMultiPos             ").append(m_numOfMultiPos).append('\n');
            sb.append("  sizeOfMultiPosData      ").append(getSizeOfMultiPosData()).append('\n');
            sb.append("numOfSinglePos            ").append(m_numOfSinglePos).append('\n');
            sb.append("  sizeOfSinglePosData     ").append(getSizeOfSinglePosData()).append('\n');
            sb.append("numOfGames                ").append(m_numOfGames).append('\n');
            sb.append("startOfMultiPos           ").append(m_startOfMultiPos).append('\n');
            sb.append("  startOfMultiPosData     ").append(getStartOfMultiPosData()).append('\n');
            sb.append("  endOfMultiPosData       ").append(getEndOfMultiPosData()).append('\n');
            sb.append("  startOfMultiPosAddons   ").append(getStartOfMultiPosAddons()).append('\n');
            sb.append("startOfSinglePos          ").append(m_startOfSinglePos).append('\n');
            sb.append("  startOfSinglePosData    ").append(getStartOfSinglePosData()).append('\n');
            sb.append("  endOfSinglePosData      ").append(getEndOfSinglePosData()).append('\n');
            sb.append("startOfGames              ").append(m_startOfGames).append('\n');
            sb.append("largestGameCounter        ").append(m_largestGameCounter).append('\n');
            sb.append("multiPosIndexBits         ").append(m_multiPosIndexBits).append('\n');
            sb.append("multiPosIndexPointerBits  ").append(m_multiPosIndexPointerBits).append('\n');
            sb.append("singlePosIndexBits        ").append(m_singlePosIndexBits).append('\n');
            sb.append("singlePosIndexPointerBits ").append(m_singlePosIndexPointerBits).append('\n');
            sb.append("variationDepth            ").append(m_variationDepth);
            return sb.toString();
        }
    }
    
    /*================================================================================*/
    
    private RandomAccessFile m_dataFile;
    private String m_name;
    
    private Header m_header;
    private int[] m_multiPosDataIndex;    
    private int[] m_singlePosDataIndex;
    
    private long[] m_absentPositionCache;
    private PositionData[] m_positionDataCache;
    
    /*================================================================================*/
    
    public ChFilePositionMap(File file) throws IOException
    {
//        Logger.log("Create new FilePositionMap", this);
        m_name = file.getName();
        m_dataFile = new RandomAccessFile(file, "r");
        
        readHeader();
//        if (Debug.debug()) System.out.println(m_header);
        initPosDataIndices();
        
        m_absentPositionCache = new long[65336];
        m_positionDataCache = new PositionData[65536];
        
        initPositionData();
        
//        printStatistics();
//        print(true);
    }
    
    public void close()
    {
        try {m_dataFile.close();} catch (Exception ex) {}
    }
        
    /*================================================================================*/
    
    private int bitsForHashCode;
    private int bitsForGameCounter;
    private int bitsForElo;
    private int bitsForYear;
    private int bitsForMovesFlag;
    private int bitsForMoves;

    private int HC_S, WW_S, D_S, BW_S, WE_S, WEG_S, BE_S, BEG_S, FO_S, MOF_S, MO_S;

    private void initPositionData()
    {
        bitsForHashCode    = 63 - m_header.getMultiPosIndexBits();
        bitsForGameCounter = BitArray.getBitsForNumber(m_header.getLargestGameCounter());
        bitsForElo         = 12;
        bitsForYear        = 12;
        bitsForMovesFlag   =  1;
        bitsForMoves       = 64;

        HC_S  = 0;
        WW_S  = HC_S  + bitsForHashCode;
        D_S   = WW_S  + bitsForGameCounter;
        BW_S  = D_S   + bitsForGameCounter;
        WE_S  = BW_S  + bitsForGameCounter;
        WEG_S = WE_S  + bitsForElo;
        BE_S  = WEG_S + bitsForGameCounter;
        BEG_S = BE_S  + bitsForElo;
        FO_S  = BEG_S + bitsForGameCounter;
        MOF_S = FO_S  + bitsForYear;
        MO_S  = MOF_S + bitsForMovesFlag;
    }
        
    /*================================================================================*/
    
    private void readHeader() throws IOException
    {
        m_dataFile.seek(0L);
        m_header = new Header();
        m_header.read(m_dataFile);
    }
    
    private void initPosDataIndices() throws IOException
    {
        //---------- multi pos ----------
        m_dataFile.seek(m_header.getStartOfMultiPos());
        int numOfIndices = 1 << m_header.getMultiPosIndexBits();
        m_multiPosDataIndex = new int[numOfIndices + 1];
        
        m_multiPosDataIndex[0] = 0;
        m_multiPosDataIndex[numOfIndices] = m_header.getNumOfMultiPos();
        for (int i = 1; i < numOfIndices; i++) {
            int delta = BitArray.readInt(m_dataFile, m_header.getMultiPosIndexPointerBits() / 8);
            m_multiPosDataIndex[i] = m_multiPosDataIndex[i - 1] + delta;
//            System.out.println(delta + " " + m_multiPosDataIndex[i]);
        }
        
        //---------- single pos ----------
        m_dataFile.seek(m_header.getStartOfSinglePos());
        numOfIndices = 1 << m_header.getSinglePosIndexBits();
        m_singlePosDataIndex = new int[numOfIndices + 1];
        
        m_singlePosDataIndex[0] = 0;
        m_singlePosDataIndex[numOfIndices] = m_header.getNumOfSinglePos();
        for (int i = 1; i < numOfIndices; i++) {
            int delta = BitArray.readInt(m_dataFile, m_header.getSinglePosIndexPointerBits() / 8);
            m_singlePosDataIndex[i] = m_singlePosDataIndex[i - 1] + delta;
//            System.out.println(delta + " " + m_singlePosDataIndex[i]);
        }
    }
    
    /*================================================================================*/

    RandomAccessFile getFile()               {return m_dataFile;}
    public int getNumOfData()                {return m_header.getNumOfSinglePos() + m_header.getNumOfMultiPos();}
    public int getNumOfPositions()           {return m_header.getNumOfEntries();}
    public int getNumOfGames()               {return m_header.getNumOfGames();}
    long getStartOfGames()                   {return m_header.getStartOfGames();}
//    long getStartOfAddons()                  {return m_header.getm_startOfAddons;}
    public int getLargestGameCounter()       {return m_header.getLargestGameCounter();}
    public int getLargestCounterDifference() {return -1;} // TODO
 
    /*================================================================================*/
    // DataIterator
    
    public PositionDataIterator getDataIterator()
    {
         return new DataIterator();
    }

    /*================================================================================*/
    
    private synchronized long getMultiPosHashPrefix(int index)
    {
        int hashIndex = Arrays.binarySearch(m_multiPosDataIndex, index);
        if (hashIndex < 0) {
            hashIndex = -(hashIndex + 1) - 1;
        } else {
            while (m_multiPosDataIndex[hashIndex + 1] == index) hashIndex++;
        }
        return (long)hashIndex << (63 - m_header.getMultiPosIndexBits());
    }
    
    private synchronized long getSinglePosHashPrefix(int index)
    {
        int hashIndex = Arrays.binarySearch(m_singlePosDataIndex, index);
        if (hashIndex < 0) {
            hashIndex = -(hashIndex + 1) - 1;
        } else {
            while (m_singlePosDataIndex[hashIndex + 1] == index) hashIndex++;
        }
        return (long)hashIndex << (63 - m_header.getSinglePosIndexBits());
    }
    
    private synchronized PositionData readMultiPosData(int index) throws IOException
    {
        m_dataFile.seek(m_header.getStartOfMultiPosData() + index * m_header.getSizeOfMultiPosData());
        return new ChPositionData(getMultiPosHashPrefix(index));
    }
    
    private synchronized PositionData readSinglePosData(int index) throws IOException
    {
        m_dataFile.seek(m_header.getStartOfSinglePosData() + index * m_header.getSizeOfSinglePosData());
        return new PositionDataGame(m_dataFile, 63 - m_header.getSinglePosIndexBits(), getSinglePosHashPrefix(index));
    }
    
    public synchronized PositionData getData(long hashCode)
    {
//        if (Debug.debug()) System.out.println("getData(" + hashCode + ")");
        s_getData++;
        
        if (m_absentPositionCache[(int)(hashCode % m_absentPositionCache.length)] == hashCode) {
            s_absentCacheHits++;
            s_getDataMiss++;
            return null;  // =====>
        }
        
        int pdcIndex = (int)(hashCode % m_positionDataCache.length);
        if (m_positionDataCache[pdcIndex] != null && m_positionDataCache[pdcIndex].getHashCode() == hashCode) {
            s_cacheHits++;
            return m_positionDataCache[pdcIndex];  // =====>
        }
        
        try {
            //---------- multi data ----------
            int hashIndex = (int)(hashCode >>> (63 - m_header.getMultiPosIndexBits()));
            for (int index = m_multiPosDataIndex[hashIndex]; index < m_multiPosDataIndex[hashIndex + 1]; index++) {
                PositionData data = readMultiPosData(index);
//                System.out.println(data);
                if (data.getHashCode() == hashCode) {
                    m_positionDataCache[pdcIndex] = data;
                    s_regularFinds++;
                    return data;  // =====>
                }
            }

            //---------- single data ----------
            hashIndex = (int)(hashCode >>> (63 - m_header.getSinglePosIndexBits()));
            for (int index = m_singlePosDataIndex[hashIndex]; index < m_singlePosDataIndex[hashIndex + 1]; index++) {
                PositionData data = readSinglePosData(index);
                if (data.getHashCode() == hashCode) {
                    m_positionDataCache[pdcIndex] = data;
                    s_singleFinds++;
                    return data;  // =====>
                }
            }
        } catch (IOException ex) {
//            Logger.log(ex, this);
        }
        
        m_absentPositionCache[(int)(hashCode % m_absentPositionCache.length)] = hashCode;
        s_getDataMiss++;
        return null;  // =====>
    }
    
    /*================================================================================*/
    
    private static long s_getData = 0, s_absentCacheHits = 0, s_cacheHits = 0, s_regularFinds = 0, s_singleFinds = 0, s_getDataMiss = 0;
    private static long s_finds = 0, s_succ = 0, s_fail = 0, s_succTries = 0, s_failTries = 0;
    private static long s_gameCacheHit = 0, s_gameCacheFail = 0, s_gameCacheReplace = 0;
    private static long s_time = 0;
    
    private static DecimalFormat df = new DecimalFormat("#");
    private static DecimalFormat s_dfperc = new DecimalFormat("#0.000%");
    
    private static String format(long num)
    {
        String res = "               " + df.format(num);
        return res.substring(res.length() - 12);
    }
    
    public void printPerformanceStatistics()
    {
        System.out.println("==============================================================================");
        System.out.println("FilePositionMap " + m_name);
        System.out.println();
        
        System.out.println("#getData          = " + format(s_getData));
        System.out.println("#no data          = " + format(s_getDataMiss) + " " + s_dfperc.format((double)s_getDataMiss/s_getData));
        System.out.println("  #absentCacheHit = " + format(s_absentCacheHits) + "   " + s_dfperc.format((double)s_absentCacheHits/s_getDataMiss)); 
        System.out.println("#regularFinds     = " + format(s_regularFinds)); 
        System.out.println("#singleFinds      = " + format(s_singleFinds));
        System.out.println("  #cacheHit       = " + format(s_cacheHits)); 
        System.out.println("#data existed     = " + format(s_getData - s_getDataMiss) + " " + s_dfperc.format((double)(s_getData-s_getDataMiss)/s_getData));
//        System.out.println();
//        
//        System.out.println("#find        = " + format(s_finds)); 
//        System.out.println("#succ        = " + format(s_succ)); 
//        System.out.println("#fail        = " + format(s_fail)); 
//        System.out.println("#tries       = " + format(s_succTries + s_failTries) + " " + ((double)(s_succTries + s_failTries) / (s_succ + s_fail)));
//        System.out.println("#succTries   = " + format(s_succTries) + " " + ((double)(s_succTries) / s_succ));
//        System.out.println("#failTries   = " + format(s_failTries) + " " + ((double)(s_failTries) / s_fail));
//        System.out.println();
        
//        int num = 0; for (int i=0; i<HASH_SIZE; i++) {if (m_positionDataCache[i] != 0) num++;}
//        System.out.println("# in positionsInCache          " + format(num) + " of " + format(HASH_SIZE));
//        num = 0; for (int i=0; i<POSITION_HASH_SIZE; i++) {if (m_positionDataByPointer[i] != 0) num++;}
//        System.out.println("# in positionsByPointerInCache " + format(num) + " of " + format(OSITION_HASH_SIZE));
//        num = 0; for (int i=0; i<ABSENT_HASH_SIZE; i++) {if (m_absentHashCodeCache[i] != 0) num++;}
//        System.out.println("# in positionsInCache " + format(num) + " of " + format(ABSENT_HASH_SIZE));
//        num = 0; for (int i=0; i<GAME_CACHE_SIZE; i++) {if (m_gameModelCache[i] != 0) num++;}
//        System.out.println("# in positionsInCache " + format(num) + " of " + format(GAME_CACHE_SIZE));
//        System.out.println();
        
        System.out.println();
        System.out.println("#gameCacheHit      = " + format(s_gameCacheHit));
        System.out.println("#gameCacheFail     = " + format(s_gameCacheFail));
        System.out.println("#gameCacheReplace  = " + format(s_gameCacheReplace));
        System.out.println();
        
        System.out.println("time            = " + format(s_time) + "ms");
        System.out.println();
        
//        int num = 0; for (int i = 0; i < m_positionDataCache.length; i++) if (m_positionDataCache[i] != null) num++;
//        System.out.println("positionDataCache:        " + format(num) + " / " + format(m_positionDataCache.length));
//        num = 0; for (int i = 0; i < m_positionDataByPointer.length; i++) if (m_positionDataByPointer[i] != null) num++;
//        System.out.println("m_positionDataByPointer:  " + format(num) + " / " + format(m_positionDataByPointer.length));
//        num = 0; for (int i = 0; i < m_absentHashCodeCache.length; i++) if (m_absentHashCodeCache[i] != 0) num++;
//        System.out.println("absentHashCodeCache:      " + format(num) + " / " + format(m_absentHashCodeCache.length));
//        num = 0; for (int i = 0; i < m_gameModelCache.length; i++) if (m_gameModelCache[i] != null) num++;
//        System.out.println("gameModelCache:           " + format(num) + " / " + format(m_gameModelCache.length));
        
        System.out.println("==============================================================================");
    }
    
    /*================================================================================*/
    
    public synchronized GameModel getGameModel(int index)
    {
        if (index < 0 || index >= m_header.getNumOfGames()) {
            new Exception("Illegal index " + index).printStackTrace();
            return null;
        }
        
        try {
            m_dataFile.seek(m_header.getStartOfGameIndices() + 4 * index);
            int gamePointer = m_dataFile.readInt();
            m_dataFile.seek(m_header.getStartOfGameData() + gamePointer);
            return new GameModel(m_dataFile, GameHeaderModel.MODE_STANDARD_TAGS, GameMoveModel.MODE_EVERYTHING);
        } catch (IOException ex) {
//            Logger.log(ex, this);
            return null;
        }
    }
    
    public int getGameModelIndex(GameModel gameModel)
    {
        new Exception().printStackTrace();
        return -1;
    }
    
    public GameModelIterator getGameModelIterator()
    {
        return new MyGameModelIterator();
    }
    
    public boolean containsGameModel(GameModel gameModel)
    {
        // TODO
        return false;
    }
    
    public GameModel getGameModel(GameModel gameModel)
    {
        // TODO
        return null;
    }
    
    /*================================================================================*/
    
    private void printStatistics()
    {
        System.out.println(m_name);
        System.out.println("regular data ");
//        System.out.println("  total size    " + (m_header.getEndOfMultiPosData() - m_header.getStartOfMultiPos());
//        System.out.println("  num of data   " + (m_startOfSinglePositionMaps - HEADER_SIZE) / ChPositionData.getSize(getLargestGameCounter()));
//        System.out.println("  size per data " + ChPositionData.getSize(getLargestGameCounter()));
//        System.out.println("single data ");
//        System.out.println("  total size    " + (m_startOfGameIndices - m_startOfSinglePositionMaps));
//        System.out.println("  num of data   " + (m_startOfGameIndices - m_startOfSinglePositionMaps) / ChPositionDataGame.getSize(getNumOfGames()));
//        System.out.println("  size per data " + ChPositionDataGame.getSize(getNumOfGames()));
    }
    
    private synchronized void testPerformance()
    {
//        Random rand = new Random();
//        final int LOOP = 1000;
//        
//        try {
//            long time = System.currentTimeMillis();
//            for (int i=0; i<LOOP; i++) {
//                m_dataFile.seek(Math.abs(rand.nextLong() % m_startOfGameIndices));
//            }
//            System.out.println("seek " + (System.currentTimeMillis() - time) + "ms");
//
//            time = System.currentTimeMillis();
//            for (int i=0; i<LOOP; i++) {
//                m_dataFile.seek(Math.abs(rand.nextLong() % m_startOfGameIndices));
//                m_dataFile.readLong();
//            }
//            System.out.println("seek + readLong " + (System.currentTimeMillis() - time) + "ms");
//
//            time = System.currentTimeMillis();
//            for (int i=0; i<LOOP; i++) {
//                m_dataFile.seek(Math.abs(rand.nextLong() % m_startOfGameIndices));
//                readHashCode();
//            }
//            System.out.println("seek + readHashCode " + (System.currentTimeMillis() - time) + "ms");
//
//            time = System.currentTimeMillis();
//            byte[] buf = new byte[8];
//            for (int i=0; i<LOOP; i++) {
//                m_dataFile.seek(Math.abs(rand.nextLong() % m_startOfGameIndices));
//                m_dataFile.readFully(buf);
//            }
//            System.out.println("seek + readByte[8] " + (System.currentTimeMillis() - time) + "ms");
//            
//            time = System.currentTimeMillis();
//            buf = new byte[4096];
//            for (int i=0; i<LOOP; i++) {
//                m_dataFile.seek(Math.abs(rand.nextLong() % m_startOfGameIndices));
//                m_dataFile.readFully(buf);
//            }
//            System.out.println("seek + readByte[4096] " + (System.currentTimeMillis() - time) + "ms");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }
    
    public synchronized void readGamesSequentially(boolean verbose)
    {
        int num = 0;
        try {
            m_dataFile.seek(m_header.getStartOfGameData());
            for(;;) {
                GameModel gameModel = new GameModel(m_dataFile, GameHeaderModel.MODE_STANDARD_TAGS, GameMoveModel.MODE_EVERYTHING);
                if (verbose) System.out.println(gameModel);
                num++;
            }
        } catch (EOFException ex) {
            // finished
        } catch (IOException ex) {
//            Logger.log(ex, this);
        }
        System.out.println("Games found " + df.format(num));
    }
    
    public synchronized void print(boolean verbose)
    {
//        testPerformance();
//        Logger.log("print", this);
        System.out.println("Header");
        System.out.println("============================================================");
        System.out.println(m_header);
        System.out.println();
        
        System.out.println("Games");
        System.out.println("============================================================");
        readGamesSequentially(verbose);
        System.out.println();
        
        System.out.println("MultiPosData");
        System.out.println("============================================================");
        try {
            for (int i=0; i<m_header.getNumOfMultiPos(); i++) {
                m_dataFile.seek(m_header.getStartOfMultiPosData() + i * m_header.getSizeOfMultiPosData());
                System.out.println(new ChPositionData(getMultiPosHashPrefix(i)));
            }
        } catch (IOException ex) {
//            Logger.log(ex, this);
        }
        System.out.println();
        
        System.out.println("SinglePosData");
        System.out.println("============================================================");
        try {
            for (int i=0; i<m_header.getNumOfSinglePos(); i++) {
                m_dataFile.seek(m_header.getStartOfSinglePosData() + i * m_header.getSizeOfSinglePosData());
                System.out.println(new PositionDataGame(m_dataFile, 63 - m_header.getSinglePosIndexBits(), getSinglePosHashPrefix(i)));
            }
        } catch (IOException ex) {
//            Logger.log(ex, this);
        }
        
//        long lastHash = -1;
//        int entries = 0, positions = 0;
//        int[] num = new int[32];
//        IChIterator it = getDataIterator();
//        for (IChPositionData data = it.getNext(); data != null; data = it.getNext()) {
//            if (verbose) System.out.println(data);
//            if (data.getHashCode() <= lastHash) {
//                System.out.println("Hash not increasing: " + data);
//            }
//            if (data.getNumOfGames() <= 0) {
//                System.out.println("Data with less than one game: " + data);
//            }
//            lastHash = data.getHashCode();
//            entries++; positions += data.getNumOfGames();
//            num[ChBitArray.getBitsForNumber(data.getLargestGameCounter())]++;
//        }
//        
//        int index = 31;
//        while (index > 0 && num[index] == 0) index--;
//        while (index > 0) {System.out.println(df.format(num[index]) + " " + index); index--;}
//
//        if (entries != m_numOfEntries) System.out.println("numOfEntries wrong, should be " + m_numOfEntries + " but was " + entries);
//        if (positions != m_numOfPositions) System.out.println("numOfPositions wrong, should be " + m_numOfPositions + " but was " + positions);
//        
//        int games = 0;
//        for (Iterator git = getGameModelIterator(); git.hasNext(); ) {
//            ChGameModel gameModel = (ChGameModel)git.next();
//            if (verbose) System.out.println(gameModel);
//            games++;
//        }
//        if (games != m_numOfGames) System.out.println("numOfGames wrong, should be " + m_numOfGames + " but was " + games);
//
    }

    /*================================================================================*/
    
    private static void append(File inFile, DataOutputStream out) throws IOException
    {
        try {
            FileInputStream in = new FileInputStream(inFile);
            int cur = 0;
            long length = inFile.length();
            byte[] buf = new byte[8192];
            for(;;) {
                int num = in.read(buf);
                if (num == -1) break;
                out.write(buf, 0, num);
                cur += num;
//                if (listener != null) listener.notifyProgress(cur, length, "");
            }
        } catch (EOFException ex) {}
    }
    
    public static void writeToDataFile(ChAbstractPositionReadMap map, File file) throws IOException
    {        
//        Logger.log("FilePositionMap.writeToDataFile", ChFilePositionMap.class);
        map.initForWriting();
        
        /*---------- create files ----------*/
        File multiPosIndexFile  = File.createTempFile("chmpi", ".tmp");
        File multiPosDataFile   = File.createTempFile("chmpd", ".tmp");
        File multiPosAddonFile  = File.createTempFile("chmpa", ".tmp");
        File singlePosIndexFile = File.createTempFile("chspi", ".tmp");
        File singlePosDataFile  = File.createTempFile("chspd", ".tmp");
        File gameIndexFile      = File.createTempFile("chgi",  ".tmp");
        File gameDataFile       = File.createTempFile("chgd",  ".tmp");
        
        DataOutputStream outMultiPosIndex  = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(multiPosIndexFile)));
        DataOutputStream outMultiPosData   = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(multiPosDataFile)));
        DataOutputStream outMultiPosAddons = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(multiPosAddonFile)));
        DataOutputStream outSinglePosIndex = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(singlePosIndexFile)));
        DataOutputStream outSinglePosData  = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(singlePosDataFile)));
        DataOutputStream outGameIndices    = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(gameIndexFile)));
//        DataOutputStream outGameData       = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(gameDataFile)));

        /*---------- header ----------*/
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));        
        Header header = new Header();
        header.write(out);

        header.setNumOfGames(map.getNumOfGames());
        header.setLargestGameCounter(map.getLargestGameCounter());
//        header.setLargestCounterDifference(map.getLargestCounterDifference());
        header.setMultiPosIndexBits((byte)7);
        header.setSinglePosIndexBits((byte)15);

        /*---------- write regular position data ----------*/
//        if (Debug.debug()) System.out.println("PositionData");

//        if (listener != null) listener.notifyTask(1, 3, "Write position data");
        int progressTotal = map.getNumOfPositions();
        int progressStep = map.getNumOfPositions() / 200;
        int progressLast = -progressStep;

        int[] singlePosIndexCount = new int[1 << header.getSinglePosIndexBits()];
        int[] multiPosIndexCount  = new int[1 << header.getMultiPosIndexBits()];
        
        int multiPosCount = 0;
        int singlePosCount = 0;
        int numOfEntries = 0;
        
        PositionDataIterator it = map.getDataIterator();
        for (PositionData data = it.getNext(); data != null; data = it.getNext()) {
//            System.out.println(data);
            if (data.getNumOfGames() == 1) {
                singlePosIndexCount[(int)(data.getHashCode() >>> (63 - header.getSinglePosIndexBits()))]++;
                singlePosCount++;
                writeSinglePosData((AbstractPositionDataGame)data, outSinglePosData, header);
            } else {
                multiPosIndexCount[(int)(data.getHashCode() >>> (63 - header.getMultiPosIndexBits()))]++;
                multiPosCount++;
                writeMultiPosData(data, outMultiPosData, outMultiPosAddons, header);
            }
            
            numOfEntries += data.getNumOfGames();
//            if (listener != null) {
//                if (singlePosCount + multiPosCount - progressLast > progressStep) {
//                    listener.notifyProgress(singlePosCount + multiPosCount, progressTotal, numOfEntries + " entries saved");
//                    progressLast = singlePosCount + multiPosCount;
//                }
//            }
        }
        
        int maxDelta = 0;
        for (int i = 0; i < singlePosIndexCount.length - 1; i++) {
            if (singlePosIndexCount[i] > maxDelta) maxDelta = singlePosIndexCount[i];
        }
        header.setSinglePosIndexPointerBits((byte)(8 * BitArray.getBytesForNumber(maxDelta)));
        for (int i = 0; i < singlePosIndexCount.length - 1; i++) {
            BitArray.writeInt(outSinglePosIndex, singlePosIndexCount[i], header.getSinglePosIndexPointerBits() / 8);
//            if (Debug.debug()) System.out.println("Single " + i + " " + singlePosIndexCount[i]);
        }
        
        maxDelta = 0;
        for (int i = 0; i < multiPosIndexCount.length - 1; i++) {
            if (multiPosIndexCount[i] > maxDelta) maxDelta = multiPosIndexCount[i];
        }
        header.setMultiPosIndexPointerBits((byte)(8 * BitArray.getBytesForNumber(maxDelta)));
        for (int i = 0; i < multiPosIndexCount.length - 1; i++) {
            BitArray.writeInt(outMultiPosIndex, multiPosIndexCount[i], header.getMultiPosIndexPointerBits() / 8);
//            if (Debug.debug()) System.out.println("Multi " + i + " " + multiPosIndexCount[i]);
        }
        
        header.setNumOfEntries(numOfEntries);
        header.setNumOfMultiPos(multiPosCount);
        header.setNumOfSinglePos(singlePosCount);
        
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        header.setStartOfMultiPos(out.size());
        outMultiPosIndex.close();
        append(multiPosIndexFile, out);
        multiPosIndexFile.delete();
        
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        outMultiPosData.close();
        append(multiPosDataFile, out);
        multiPosDataFile.delete();
        
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        outMultiPosAddons.close();
        append(multiPosAddonFile, out);
        multiPosAddonFile.delete();
        
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        header.setStartOfSinglePos(out.size());
        outSinglePosIndex.close();
        append(singlePosIndexFile, out);
        singlePosIndexFile.delete();
        
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        outSinglePosData.close();
        append(singlePosDataFile, out);
        singlePosDataFile.delete();
//        if (Debug.debug()) System.out.println("out.size=" + out.size());
        
        /*---------- game indices and games ----------*/
//        if (Debug.debug()) System.out.println("Write Games");        
        map.writeGames(outGameIndices, gameDataFile, GameHeaderModel.MODE_STANDARD_TAGS, GameMoveModel.MODE_EVERYTHING);
        outGameIndices.close();

        map = null;   // allow gc to allocate map in low memory situation
        
        header.setStartOfGames(out.size());
        append(gameIndexFile, out);
        gameIndexFile.delete();
        
        append(gameDataFile, out);        
        gameDataFile.delete();
        
        out.close();
        
        /*---------- complete header ----------*/
        RandomAccessFile randomOut = new RandomAccessFile(file, "rw");
        randomOut.seek(0L);
        header.write(randomOut);
        randomOut.close();
        
//        if (Debug.debug()) System.out.println("Done");
    }

    /*================================================================================*/
    
    public String toString() {return m_name;}
    
    /*================================================================================*/
    
//    public static void create(DataOutput gameIndices, File gamesFile, int headerMode, int movesMode) throws IOException;
    
    /*================================================================================*/
    
    public static void main(String[] args)
    {
        boolean verbose = false;
        boolean iterator = false;
        
        int index = 0;
        while (index < args.length && args[index].startsWith("-")) {
            if (args[index].equals("-verbose")) {
                verbose = true; index++;
            } else if (args[index].equals("-iterator")) {
                iterator = true; index++;
            } else {
                System.out.println("Unknown option " + args[index]);
                System.exit(0);
            }
        }
        
        if (index < args.length) {
            try {
                if (iterator) {
                    ChFilePositionMap map = new ChFilePositionMap(new File(args[index]));
                    for (PositionDataIterator it = map.getDataIterator(); ; ) {
                        PositionData data = it.getNext();
                        if (data == null) break;
                        System.out.println(data);
                    }
                } else {
                    new ChFilePositionMap(new File(args[index])).print(verbose);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Must indicate a filename");
        }
    }
    
}