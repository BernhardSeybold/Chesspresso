/*
 * ChPositionMap.java
 *
 * Created on 27. Juni 2001, 17:17
 */

package chesspresso.position.map;

import chesspresso.position.*;
import chesspresso.game.*;
import java.util.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class PositionMap extends AbstractPositionReadWriteMap
{
    
    private class MyDataIterator implements PositionDataIterator
    {
        private long m_lastHashCode;
        
        private MyDataIterator()
        {
            m_lastHashCode = 0;
        }
        
        public PositionData getNext()
        {
            if (m_lastHashCode < 0) return null;  // =====>
            
            synchronized(PositionMap.this) {
                int index = (int)(m_lastHashCode >> (63 - m_indexBits));

                short[] data = m_map[index];
                if (data != null) {
                    int index2 = 0;
                    while (index2 < data.length) {
                        long hc = getHashCodeAt(index, index2);
                        if (hc > m_lastHashCode) {
                            m_lastHashCode = hc;
                            return getPositionDataAt(hc, index, index2); // =====>
                        }
                        index2 += (data[index2] & 0xFF);
                    }
                }
                
                do {index++;} while (index < m_map.length && m_map[index] == null);
                if (index >= m_map.length) {
                    m_lastHashCode = -1;
                    return null;  // =====>
                }
                
                m_lastHashCode = getHashCodeAt(index, 0);
                return getPositionDataAt(m_lastHashCode, index, 0);
            }
        }
        
    }
    
    /*================================================================================*/
    
    private class MyGameModelIterator implements GameModelIterator
    {
        private Iterator m_iterator;

        MyGameModelIterator(Iterator iterator) {m_iterator = iterator;}
        
        public boolean hasNext() {return m_iterator.hasNext();}
        public Object next() {return m_iterator.next();}
        public GameModel nextGameModel() {return (GameModel)m_iterator.next();}
        public void remove() {m_iterator.remove();}
    }
    
    
    //================================================================================
    // BasePositionData
    //================================================================================

    static abstract class BasePositionData extends ChAbstractPositionData
    {
        private long m_hashCode;
        protected short[] m_movesPlayed;
        
        //================================================================================
        
        private BasePositionData(long hashCode)
        {
            m_hashCode = hashCode;
            m_movesPlayed = null;
        }
        
        //================================================================================
        
        public long getHashCode() {return m_hashCode;}
        
        public short[] getPlayedMoves() {return m_movesPlayed;}
        
        //================================================================================
        
        public PositionData add(Game game, short nextMove)
        {
            return add(new PositionDataGame(getHashCode(), game.getModel(), nextMove));
        }
        
        protected short[] combineMovesPlayed(short[] moves1, short[] moves2)
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

    }
    

    //================================================================================
    // PositionDataGameShort
    //================================================================================

    public static class PositionDataGameShort extends AbstractPositionDataGame
    {
        protected PositionReadMap m_map;
        protected short m_gameIndex;
        protected short m_nextMove;

        /*================================================================================*/

        protected PositionDataGameShort(long hashCode, PositionReadMap map, short gameIndex, short nextMove)
        {
            super(hashCode);
            m_map = map;
            m_gameIndex = gameIndex;
            m_nextMove = nextMove;
        }

        protected PositionDataGameShort(long hashCode, PositionReadMap map, short[] arr)
        {
            this(hashCode, map, arr[0], arr[1]);
        }

        public Object getBytes()
        {
            return new short[] {m_gameIndex, m_nextMove};
        }
        
        /*================================================================================*/
        
        public PositionData add(PositionData data)
        {
            return new PositionDataLong(this).add(data);
        }
        
        /*================================================================================*/

        public PositionReadMap getMap() {return m_map;}
        public int getGameIndex() {return m_gameIndex;}
        public GameModel getGameModel() {return m_map.getGameModel(m_gameIndex);}
        protected short getNextMove() {return m_nextMove;}
    }
    
    
    //================================================================================
    // PositionDataGameInt
    //================================================================================

    public static class PositionDataGameInt extends AbstractPositionDataGame
    {
        protected PositionReadMap m_map;
        protected int m_gameIndex;
        protected short m_nextMove;

        /*================================================================================*/

        protected PositionDataGameInt(long hashCode, PositionReadMap map, int gameIndex, short nextMove)
        {
            super(hashCode);
            m_map = map;
            m_gameIndex = gameIndex;
            m_nextMove = nextMove;
        }

        public Object getBytes()
        {
            return null;
        }
        
        /*================================================================================*/
        
        public PositionData add(PositionData data)
        {
            return new PositionDataLong(this).add(data);
        }
        
        /*================================================================================*/

        public PositionReadMap getMap() {return m_map;}
        public int getGameIndex() {return m_gameIndex;}
        public GameModel getGameModel() {return m_map.getGameModel(m_gameIndex);}
        protected short getNextMove() {return m_nextMove;}
    }
    
    
    //================================================================================
    // PositionDataGame
    //================================================================================

    public static class PositionDataGame extends AbstractPositionDataGame
    {
        protected GameModel m_gameModel;
        protected short m_nextMove;

        /*================================================================================*/

        protected PositionDataGame(long hashCode, GameModel gameModel, short nextMove)
        {
            super(hashCode);
            m_gameModel = gameModel;
            m_nextMove = nextMove;
        }

        public Object getBytes()
        {
            throw new UnsupportedOperationException("");
        }
        
        /*================================================================================*/
        
        public PositionData add(PositionData data)
        {
//            return new PositionDataLong(this).add(data);
            return new ChPositionData(this, data);
        }
        
        /*================================================================================*/

        public PositionReadMap getMap() {return null;}
        public int getGameIndex() {return -1;}
        public GameModel getGameModel() {return m_gameModel;}
        protected short getNextMove() {return m_nextMove;}        
    }
    
    
    //================================================================================
    // PositionDataLong
    //================================================================================

    private static int[]  BITS  = new int[] {6, 7, 6, 11, 7, 11, 7, 9};
    private static long[] MASK;  // long to allow shifting of masks without casting to long
    private static int[]  SHIFT;
    
    private static int MIN_YEAR = 1600;
    private static int MIN_ELO  = 1500;
    
    static {
        MASK  = new long[BITS.length];
        SHIFT = new int [BITS.length];
        int shift = 0;
        for (int i=0; i<BITS.length; i++) {
            MASK[i] = (1 << BITS[i]) - 1;
            SHIFT[i] = shift;
            shift += BITS[i];
        }
//        System.out.println(shift + " bits used for PositionDataLong");
        if (shift > 64) throw new RuntimeException();
    }
    

    public static class PositionDataLong extends BasePositionData
    {
        private long m_data;
        
        //================================================================================
        
        public PositionDataLong(long hashCode)
        {
            super(hashCode);
            m_data = MASK[7] << SHIFT[7];   // set max year
        }
        
        public PositionDataLong(PositionData data)
        {
            super(data.getHashCode());
            m_data = MASK[7] << SHIFT[7];   // set max year
            add(data);
        }
        
        public PositionDataLong(PositionData data1, PositionData data2)
        {
            super(data1.getHashCode());
            m_data = MASK[7] << SHIFT[7];   // set max year
            add(data1);
            add(data2);
        }
        
        private PositionDataLong(long hashCode, long data, short[] movesPlayed)
        {
            super(hashCode);
            m_data = data;
            m_movesPlayed = movesPlayed;
        }
        
        public Object getBytes()
        {
            return null;
//            short[] res = new short[4 + m_movesPlayed.length];
//            res[0] = (short)((m_data >> 48) & 0xFFFF);
//            res[1] = (short)((m_data >> 32) & 0xFFFF);
//            res[2] = (short)((m_data >> 16) & 0xFFFF);
//            res[3] = (short)(m_data         & 0xFFFF);
//            System.arraycopy(m_movesPlayed, 0, res, 4, m_movesPlayed.length);
//            return res;
        }
        
        //================================================================================

        public int getWhiteWins()       {return (int)((m_data >> SHIFT[0]) & MASK[0]);}
        public int getDraws()           {return (int)((m_data >> SHIFT[1]) & MASK[1]);}
        public int getBlackWins()       {return (int)((m_data >> SHIFT[2]) & MASK[2]);}
        
        public int getWhiteEloAverage() {return (getWhiteEloGames() == 0 ? 0 : (int)((m_data >> SHIFT[3]) & MASK[3])) + MIN_ELO;}
        public int getWhiteEloGames()   {return (int)((m_data >> SHIFT[4]) & MASK[4]);}
        public long getWhiteElos()      {return getWhiteEloGames() * getWhiteEloAverage();}
        public int getBlackEloAverage() {return (getBlackEloGames() == 0 ? 0 : (int)((m_data >> SHIFT[5]) & MASK[5])) + MIN_ELO;}
        public int getBlackEloGames()   {return (int)((m_data >> SHIFT[6]) & MASK[6]);}
        public long getBlackElos()      {return getBlackEloGames() * getBlackEloAverage();}
        
        public int getFirstOccurrence() {return (int)((m_data >> SHIFT[7]) & MASK[7]) + MIN_YEAR;}
        
        //================================================================================
        
        private void incWhiteWins(long val)      {m_data += (val << SHIFT[0]);}
        private void incDraws(long val)          {m_data += (val << SHIFT[1]);}
        private void incBlackWins(long val)      {m_data += (val << SHIFT[2]);}
        
        private void setWhiteElos(long elos, long games)
        {
            if (games == 0) return;  // =====>
            
//            System.out.print(getWhiteEloAverage() + " " + getWhiteEloGames() + "  + " + elos + " " + games + " = ");
            long avg = (getWhiteElos() + elos) / (getWhiteEloGames() + games);
            m_data &= ~(MASK[3] << SHIFT[3]);
            m_data += ((avg - MIN_ELO) << SHIFT[3]);
            
            m_data += (games << SHIFT[4]);
//            System.out.println(getWhiteEloAverage() + " " + getWhiteEloGames());
        }
        
        private void setBlackElos(long elos, long games)
        {
            if (games == 0) return;  // =====>
            
            long avg = (getBlackElos() + elos) / (getBlackEloGames() + games);
            m_data &= ~(MASK[5] << SHIFT[5]);
            m_data += ((avg - MIN_ELO) << SHIFT[5]);
            
            m_data += (games << SHIFT[6]);
        }
        
        private void setFirstOccurrence(long val)
        {
            if (val < MIN_YEAR) val = MIN_YEAR;
            m_data &= ~(MASK[7] << SHIFT[7]);
            m_data += ((val - MIN_YEAR) << SHIFT[7]);
        }
        
        //================================================================================
        
        private boolean canHold(PositionData data)
        {
            return (((getWhiteWins()     + data.getWhiteWins())     <= MASK[0]) &&
                    ((getDraws()         + data.getDraws())         <= MASK[1]) &&
                    ((getBlackWins()     + data.getBlackWins())     <= MASK[2]) &&
                    ((getWhiteEloGames() + data.getWhiteEloGames()) <= MASK[4]) &&
                    ((getBlackEloGames() + data.getBlackEloGames()) <= MASK[6]));
        }
        
        public PositionData add(PositionData data)
        {
            if (!canHold(data)) {
                return new ChPositionData(this, data);
            }
            
            incWhiteWins(data.getWhiteWins());
            incDraws(data.getDraws());
            incBlackWins(data.getBlackWins());
            setWhiteElos(data.getWhiteElos(), data.getWhiteEloGames());
            setBlackElos(data.getBlackElos(), data.getBlackEloGames());
            
            if (data.getFirstOccurrence() < getFirstOccurrence()) {
                setFirstOccurrence(data.getFirstOccurrence());
            }
            
            m_movesPlayed = combineMovesPlayed(m_movesPlayed, data.getPlayedMoves());
            
            return this;
        }
        
    }
    
    /*================================================================================*/
    
    private int m_indexBits;
    
    private short[][] m_map;

    private java.util.List m_games;
    private java.util.Map m_gamesMap;

    private GameScorer m_gameScorer;
    
    private int m_numOfData;
    private int m_numOfPositions;
    
    
//    private Object[] m_posTable;
//    private int m_numOfData;
//    private int m_numOfPositions;
//    private java.util.List m_games;
//    private java.util.Map m_gamesMap;
//    private int m_largestGameCounter;
//    private int m_largestCounterDifference;
    
    /*================================================================================*/
    
    public PositionMap()
    {
        this(new DefaultGameScorer(), 15);
    }
    
    public PositionMap(GameScorer gameScorer)
    {
        this(gameScorer, 15);
    }
    
    public PositionMap(GameScorer gameScorer, int indexBits)
    {
        super(gameScorer);
        if (indexBits < 1 || indexBits > 31) throw new RuntimeException("Wrong number of index bits " + indexBits);
        m_indexBits = indexBits;
        
        m_map = new short[1 << m_indexBits][];
        
        m_games = new java.util.ArrayList();
        m_gamesMap = new java.util.HashMap();
        m_gameScorer = new DefaultGameScorer();
        
        m_numOfData = 0;
        m_numOfPositions = 0;

        
//        if (indexBits < 1 || indexBits > 31) throw new RuntimeException("Wrong number of index bits " + indexBits);
//        m_indexBits = indexBits;
//        m_posTable = new Object[1 << m_indexBits];
//        m_numOfData = 0;
//        m_numOfPositions = 0;
//        m_games = new java.util.ArrayList();
//        m_gamesMap = new java.util.HashMap();
//        m_largestGameCounter = 1;
//        m_largestCounterDifference = 1;
//        m_gameScorer = new ChDefaultGameScorer();
    }
    
    public void close()
    {
    }
    
    /*================================================================================*/
    
    // TODO timestamp
    
    private boolean m_changed;
    
    public boolean isChanged() {return m_changed;}
    public void resetChanged() {m_changed = false;}
    protected void changed()   {m_changed = true;}
    
    /*================================================================================*/
    
    protected GameScorer getGameScorer() {return m_gameScorer;}
    
    public GameModel getGameModel(int index)
    {
        return (GameModel)m_games.get(index);
    }
    
    protected void addGameModel(GameModel gameModel)
    {
//        System.out.println(gameModel);
        m_games.add(gameModel);
        m_gamesMap.put(gameModel, gameModel);
    }
    
    public int getGameModelIndex(GameModel gameModel)
    {
        return m_games.indexOf(gameModel);
    }
    
    public GameModelIterator getGameModelIterator()
    {
        return new MyGameModelIterator(m_games.iterator());
    }
    
    public boolean containsGameModel(GameModel gameModel)
    {
        return m_gamesMap.containsKey(gameModel);
    }
    
    public GameModel getGameModel(GameModel gameModel)
    {
        return (GameModel)m_gamesMap.get(gameModel);
    }
    
    public void replaceGameModel(GameModel oldGameModel, GameModel newGameModel)
    {
        m_games.set(m_games.indexOf(oldGameModel), newGameModel);
        m_gamesMap.remove(oldGameModel);
        m_gamesMap.put(newGameModel, newGameModel);
    }
    
    /*================================================================================*/
    
    private short[] getShortArray(int length)
    {
        return new short[length];
    }
    
    private void releaseShortArray(short[] arr)
    {
    }
    
    /*================================================================================*/
    
    private final short[] makeSpace(int index1, int index2, int delta)
    {
        short[] oldData = m_map[index1];
        
        if (delta == 0) return oldData;
        if (delta < 0) throw new RuntimeException("delta=" + delta);
        
        if (oldData == null) {
            m_map[index1] = getShortArray(delta);
            return m_map[index1];        
        } else {
            short[] newData = getShortArray(oldData.length + delta);
            System.arraycopy(oldData, 0,      newData, 0,              index2);
            System.arraycopy(oldData, index2, newData, index2 + delta, oldData.length - index2);
            releaseShortArray(oldData);
            m_map[index1] = newData;
            return newData;
        }
    }
    
    private final void storePositionDataAt(PositionData posData, int index1, int index2)
    {
        short[] arr = m_map[index1];
        
        int oldLen = 0;
        if (arr != null && index2 < arr.length && getHashCodeAt(index1, index2) == posData.getHashCode()) {
            oldLen = (arr[index2] & 0xFF);
        }
        
        int len = -1;
        int type = -1;
        
        if (posData instanceof PositionDataGameShort) {
            type = 0; len = 6;
            arr = makeSpace(index1, index2, len - oldLen);
            PositionDataGameShort pdgs = (PositionDataGameShort)posData;
            arr[index2 + 4] = pdgs.m_gameIndex;
            arr[index2 + 5] = pdgs.m_nextMove;
        } else if (posData instanceof PositionDataGameInt) {
            type = 1; len = 7;
            arr = makeSpace(index1, index2, len - oldLen);
            PositionDataGameInt pdgi = (PositionDataGameInt)posData;
            BitArray.setInt(arr, pdgi.m_gameIndex, 16 * (index2 + 4), 32);
            arr[index2 + 6] = pdgi.m_nextMove;
        } else if (posData instanceof PositionDataLong) {
            PositionDataLong pdl = (PositionDataLong)posData;
            short[] movesPlayed = pdl.getPlayedMoves();
            type = 2; len = 8 + movesPlayed.length;
            arr = makeSpace(index1, index2, len - oldLen);
            BitArray.setLong(arr, pdl.m_data, 16 * (index2 + 4), 64);
            for (int i=0; i<movesPlayed.length; i++) arr[index2 + 8 + i] = movesPlayed[i];
        } else {
            short[] movesPlayed = posData.getPlayedMoves();
            type = 3; len = 24 + movesPlayed.length;
            arr = makeSpace(index1, index2, len - oldLen);
            BitArray.setInt (arr, posData.getWhiteWins(),       16 * (index2 +  4), 32);
            BitArray.setInt (arr, posData.getDraws(),           16 * (index2 +  6), 32);
            BitArray.setInt (arr, posData.getBlackWins(),       16 * (index2 +  8), 32);
            BitArray.setLong(arr, posData.getWhiteElos(),       16 * (index2 + 10), 64);
            BitArray.setInt (arr, posData.getWhiteEloGames(),   16 * (index2 + 14), 32);
            BitArray.setLong(arr, posData.getBlackElos(),       16 * (index2 + 16), 64);
            BitArray.setInt (arr, posData.getBlackEloGames(),   16 * (index2 + 20), 32);
            BitArray.setInt (arr, posData.getFirstOccurrence(), 16 * (index2 + 22), 32);
            for (int i=0; i<movesPlayed.length; i++) arr[index2 + 24 + i] = movesPlayed[i];
        }
        // commons
        if (len <= 0) throw new RuntimeException();
        arr[index2] = (short)((type << 8) + len);
        BitArray.setLong(arr, posData.getHashCode(), 16 * (index2 + 1), 48);
    }
    
    private PositionData getPositionDataAt(long hashCode, int index1, int index2)
    {
        short[] arr = m_map[index1];
        if (arr == null || index2 >= arr.length) return null;
        
        int type = arr[index2] >> 8;
        int len  = arr[index2] & 0xFF;
        if (type == 0) {
            short gameIndex = arr[index2 + 4];
            short nextMove = arr[index2 + 5];
            return new PositionDataGameShort(hashCode, this, gameIndex, nextMove);
        } else if (type == 1) {
            int gameIndex = BitArray.getInt(arr, 16 * (index2 + 4), 32);
            short nextMove = arr[index2 + 6];
            return new PositionDataGameInt(hashCode, this, gameIndex, nextMove);
        } else if (type == 2) {
            long data = BitArray.getLong(arr, 16 * (index2 + 4), 64);
            short[] movesPlayed = new short[len - 8];
            for (int i=0; i<movesPlayed.length; i++) {
                movesPlayed[i] = arr[index2 + 8 + i];
            }
            return new PositionDataLong(hashCode, data, movesPlayed);
        } else if (type == 3) {
            ChPositionData posData = new ChPositionData(hashCode);
            posData.setWhiteWins      (BitArray.getInt(arr, 16 * (index2 +  4), 32));
            posData.setDraws          (BitArray.getInt(arr, 16 * (index2 +  6), 32));
            posData.setBlackWins      (BitArray.getInt(arr, 16 * (index2 +  8), 32));
            posData.setWhiteElos      (BitArray.getLong(arr, 16 * (index2 + 10), 64));
            posData.setWhiteEloGames  (BitArray.getInt(arr, 16 * (index2 + 14), 32));
            posData.setBlackElos      (BitArray.getLong(arr, 16 * (index2 + 16), 64));
            posData.setBlackEloGames  (BitArray.getInt(arr, 16 * (index2 + 20), 32));
            posData.setFirstOccurrence(BitArray.getInt(arr, 16 * (index2 + 22), 32));
            short[] movesPlayed = new short[len - 24];
            for (int i=0; i<movesPlayed.length; i++) {movesPlayed[i] = arr[index2 + 24 + i];}
            posData.setPlayedMoves(movesPlayed);
            return posData;
        } else {
            throw new IllegalArgumentException("type wrong " + arr[index2 + 2]);
        }
    }
    
    private long getHashCodeAt(int index1, int index2)
    {
        return BitArray.getLong(m_map[index1], 16 * (index2 + 1), 48) | ((long)index1 << (63 - m_indexBits));
    }
    
//    private boolean isHashCodeAt(long hashCode, short[] data, int index)
//    {
//        return BitArray.getLong(data, 16 * (index + 1), 48) == (hashCode & ((1L << 48) - 1));
//    }
//    
//    private boolean isHashCodeLargerAt(long hashCode, short[] data, int index)
//    {
//        return BitArray.getLong(data, 16 * (index + 1), 48) > (hashCode & ((1L << 48) - 1));
//    }
//    
    /*================================================================================*/
    
    public int getLargestGameCounter() {return 1 << 20;}   // HACK
    public int getLargestCounterDifference() {return -1;}

    public synchronized PositionData getData(long hashCode)
    {
        int index = (int)(hashCode >> (63 - m_indexBits));
        
        short[] data = m_map[index];
        if (data != null) {
            int index2 = 0;
            while (index2 < data.length) {
                long hc = getHashCodeAt(index, index2);
                if (hc == hashCode) {
                    return getPositionDataAt(hashCode, index, index2);
                } else if (hc > hashCode) {
                    break;
                }
                index2 += (data[index2] & 0xFF);
            }
        }
        return null;  // =====>
    }

    private void enlarge(int index, int delta)
    {
        short[] newData = getShortArray(m_map[index].length + delta);
        System.arraycopy(m_map[index], 0, newData, 0, m_map[index].length);
        releaseShortArray(m_map[index]);
        m_map[index] = newData;
    }
    
    private PositionData create(long hashCode, int gameIndex, short nextMove)
    {
        if (gameIndex < 0x8000) {
            return new PositionDataGameShort(hashCode, this, (short)gameIndex, nextMove);
        } else {
            return new PositionDataGameInt(hashCode, this, gameIndex, nextMove);
        }
    }
    
    public synchronized void putData(ImmutablePosition pos, Game game, short nextMove)
    {        
        // assume: game is last in m_games!!!
        long hashCode = pos.getHashCode();
        m_numOfPositions++;
        
        int index  = (int)(hashCode >> (63 - m_indexBits));
        
        short[] data = m_map[index];
        if (data == null) {
            storePositionDataAt(create(hashCode, m_games.size()-1, nextMove), index, 0);
            m_numOfData++;
        } else {
            int index2 = 0;
            while (index2 < data.length) {
                long hc = getHashCodeAt(index, index2);
                if (hc == hashCode) {
                    PositionData posData = getPositionDataAt(hashCode, index, index2);
//                    System.out.println("pos Data " + posData);
                    posData = posData.add(game, nextMove);
                    storePositionDataAt(posData, index, index2);
                    changed();
                    return;  // =====>
                } else if (hc > hashCode) {
                    storePositionDataAt(create(hashCode, m_games.size()-1, nextMove), index, index2);
                    m_numOfData++;
                    changed();
                    return;  // =====>
                }
                index2 += (data[index2] & 0xFF);
            }
            storePositionDataAt(create(hashCode, m_games.size()-1, nextMove), index, data.length);
            m_numOfData++;
        }
        changed();
    }
        
        
        
//        int index = (int)(hashCode >> (63 - m_indexBits));
//        Object entry = m_posTable[index];
//        if (entry instanceof IChPositionData) {
//            return ((IChPositionData)entry).getHashCode() == hashCode ? (IChPositionData)entry : null;
//        } else {
//            IChPositionData[] data = (IChPositionData[])entry;
//            if (data != null) {
//                for (int i=0; i < data.length && data[i] != null; i++) {
//                    if (data[i].getHashCode() == hashCode) return data[i];
//                }
//            }
//            return null;
//        }
//    }
    
//    private final void updateLargestCounters(IChPositionData data)
//    {
//        int gameCounter = data.getLargestGameCounter();
//        if (m_largestGameCounter < gameCounter) m_largestGameCounter = gameCounter;
//        
//        int diff = Math.abs(data.getDraws() - data.getWhiteWins());
//        if (diff > m_largestCounterDifference) m_largestCounterDifference = diff;
//        diff = Math.abs(data.getWhiteWins() - data.getBlackWins());
//        if (diff > m_largestCounterDifference) m_largestCounterDifference = diff;
//    }
//    
//    public synchronized void putData(IChPosition pos, ChGame game, short nextMove)
//    {
//        // assume: game is last in m_games!!!
//        m_numOfPositions++;
//        long hashCode = pos.getHashCode();
//        int index = (int)(hashCode >> (63 - m_indexBits));
//        Object entry = m_posTable[index];
//        if (entry == null) {
////            m_posTable[index] = ChAbstractPositionData.create(hashCode, game, nextMove);
////            m_posTable[index] = new ChPositionDataGame(hashCode, nextMove);
//            m_posTable[index] = PositionDataFactory.create(hashCode, this, m_games.size() - 1, nextMove);
//            m_numOfData++;
//        } else if (entry instanceof IChPositionData) {
//            IChPositionData data = (IChPositionData)entry;
//            if (data.getHashCode() == hashCode) {
//                m_posTable[index] = data.add(game, nextMove);
//                updateLargestCounters(data);
//            } else {
//                IChPositionData[] newData = new IChPositionData[2];
//                newData[0] = data;
////                newData[1] = new ChPositionDataGame(hashCode, nextMove);
//                newData[1] = PositionDataFactory.create(hashCode, this, m_games.size() - 1, nextMove);
//                m_posTable[index] = newData;
//                m_numOfData++;
//            }                
//        } else {
//            IChPositionData[] data = (IChPositionData[])entry;
//            int i = 0;
//            while (i < data.length && data[i] != null) {
//                if (data[i].getHashCode() == hashCode) {
//                    data[i] = data[i].add(game, nextMove);
//                    updateLargestCounters(data[i]);
//                    changed();
//                    return;  // =====>
//                }
//                i++;
//            }
//            if (i >= data.length) {
//                data = new IChPositionData[i * 2];
//                System.arraycopy(m_posTable[index], 0, data, 0, i);
//                m_posTable[index] = data;
//            }
////            data[i] = new ChPositionDataGame(hashCode, nextMove);
//            data[i] = PositionDataFactory.create(hashCode, this, m_games.size() - 1, nextMove);
//            m_numOfData++;
//        }
//        changed();
//    }
    
    /*================================================================================*/

    public int getNumOfData() {return m_numOfData;}
    public int getNumOfPositions() {return m_numOfPositions;}
    public int getNumOfGames() {return m_games.size();}
    
    /*================================================================================*/
    // iterator
    // store last served hash code in iterator
    
    public PositionDataIterator getDataIterator()
    {
        return new MyDataIterator();
    }
    
    /*================================================================================*/
    
    public void printPerformanceStatistics()
    {
        System.out.println("Statistics for " + this);
        
        int[] num  = new int[65536];
        int[] size = new int[65536];
        
        for (int i=0; i<m_map.length; i++) {
            if (m_map[i] != null) {
                size[m_map[i].length]++;
                int nu = 0;
                for (int j=0; j<m_map[i].length; j += (m_map[i][j] & 0xFF)) {
                    nu++;
                }
                num[nu]++;
            } else {
                size[0]++;
                num[0]++;
            }
        }
        
        System.out.println("Sizes --------------------");
        int max = size.length-1;
        while (max >= 0 && size[max] == 0) max--;
        for (int i=0; i <= max; i++) {
            System.out.println(i + "    " + size[i]);
        }            
        
        System.out.println("posData -------------------");
        max = num.length-1;
        while (max >= 0 && num[max] == 0) max--;
        for (int i=0; i <= max; i++) {
            System.out.println(i + "    " + num[i]);
        }            
    }
    
    /*================================================================================*/
    
    public String toString() {return "PositionMap@" + hashCode();}
}