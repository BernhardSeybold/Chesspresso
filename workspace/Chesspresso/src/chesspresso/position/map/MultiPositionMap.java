/*
 * ChMultiPositionMap.java
 *
 * Created on 30. Juni 2001, 18:01
 */

package chesspresso.position.map;

import chesspresso.position.*;
import chesspresso.game.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class MultiPositionMap extends AbstractPositionReadWriteMap
{
    
    private class DataIterator implements PositionDataIterator
    {
        private PositionDataIterator[] m_iterators;
        private PositionData[] m_data;

        public DataIterator()
        {
            m_iterators = new PositionDataIterator[m_maps.size()];
            m_data = new PositionData[m_maps.size()];
            int i = 0;
            for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
                PositionReadMap map = (PositionReadMap)it.next();
                m_iterators[i] = map.getDataIterator();
                if (m_iterators[i] != null) m_data[i] = m_iterators[i].getNext();
                i++;
            }
        }

        public PositionData getNext()
        {
            long minHashCode = Long.MAX_VALUE;
            for (int i=0; i<m_data.length; i++) {
                if (m_data[i] != null && m_data[i].getHashCode() < minHashCode) {
                    minHashCode = m_data[i].getHashCode();
                }
            }
            PositionData data = null;
            boolean first = true;
            for (int i=0; i<m_data.length; i++) {
                if (m_data[i] != null && m_data[i].getHashCode() == minHashCode) {
                    if (data == null) {
                        data = m_data[i]; first = true;
                    } else if (first) {
                        data = new ChPositionData(data, m_data[i]); first = false;
                    } else {
                        data = data.add(m_data[i]);
                    }
                    m_data[i] = m_iterators[i].getNext();
                }
            }
            return data;
        }
    }
    
    /*================================================================================*/
    
    private class MyGameModelIterator implements GameModelIterator
    {
        private Iterator m_mapIterator;
        private GameModelIterator m_gameModelIterator;
        
        MyGameModelIterator()
        {
            m_mapIterator = m_maps.iterator();
            if (m_mapIterator.hasNext()) {
                m_gameModelIterator = ((PositionReadMap)m_mapIterator.next()).getGameModelIterator();
            }
        }
        
        private void goForward()
        {
            for (;;) {
                if (m_gameModelIterator == null || m_gameModelIterator.hasNext()) return;
                if (m_mapIterator.hasNext()) {
                    m_gameModelIterator = ((PositionReadMap)m_mapIterator.next()).getGameModelIterator();
                } else {
                    m_gameModelIterator = null;
                }
            }
        }
        
        public boolean hasNext()
        {
            goForward();
            return m_gameModelIterator != null;
        }
        public GameModel nextGameModel()
        {
            goForward();
            return m_gameModelIterator.nextGameModel();
        }
        public Object next() {return nextGameModel();}
        public void remove() {throw new UnsupportedOperationException("Remove not supported in GameModelIterator");}
    }
    
    /*================================================================================*/
    
    private List m_maps;
    private PositionMap m_writeMap;
    
    /*================================================================================*/
    
    public MultiPositionMap()
    {
        this(new DefaultGameScorer());
    }

    public MultiPositionMap(GameScorer gameScorer)
    {
        super(gameScorer);
        m_maps = new ArrayList();
        m_writeMap = null;
    }

    public void close()
    {
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            map.close();
        }
    }
        
    /*================================================================================*/
    
    public boolean isChanged() {return m_writeMap != null && m_writeMap.isChanged();}
    public void resetChanged() {if (m_writeMap != null) m_writeMap.resetChanged();}
    
    /*================================================================================*/
    
    public int getNumOfMaps()
    {
        return m_maps.size(); // dont count the write map
    }
    
    public synchronized void addMap(PositionReadMap map)
    {
        m_maps.add(map);
    }
    
    public synchronized void removeMap(PositionReadMap map)
    {
        m_maps.remove(map);
    }
    
    private synchronized void createWriteMap()
    {
        m_writeMap = new PositionMap(getGameScorer());
        m_maps.add(m_writeMap);
    }
    
    private synchronized AbstractPositionReadWriteMap removeWriteMap()
    {
        PositionMap map = m_writeMap;
        m_maps.remove(map);
        m_writeMap = null;
        return map;
    }
    
    /*================================================================================*/
        
    public void initForWriting()
    {
        int offset = getGameOffset();
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            map.setGameOffset(offset);
            map.initForWriting();
            offset += map.getNumOfGames();
        }
    }
    
    /*================================================================================*/
    
    public GameModel getGameModel(int index)
    {
        int num = 0;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if (index < map.getNumOfGames()) {
                return map.getGameModel(index);
            }
            index -= map.getNumOfGames();
        }
        return null;
    }
    
    public int getGameModelIndex(GameModel gameModel)
    {
        new Exception().printStackTrace();
        
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            int index = map.getGameModelIndex(gameModel);
            if (index >= 0) return index;
        }
        return -1;
    }
    
    public GameModelIterator getGameModelIterator()
    {
        return new MyGameModelIterator();
    }
    
    public boolean containsGameModel(GameModel gameModel)
    {
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if (map.containsGameModel(gameModel)) return true;  // =====>
        }
        return false;
    }
    
    public GameModel getGameModel(GameModel gameModel)
    {
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            GameModel model = map.getGameModel(gameModel);
            if (model != null) return model;  // =====>
        }
        return null;
    }
    
    public void replaceGameModel(GameModel oldGameModel, GameModel newGameModel)
    {
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if ((map instanceof PositionWriteMap) && map.containsGameModel(oldGameModel)) {
                ((PositionWriteMap)map).replaceGameModel(oldGameModel, newGameModel);
                return;  // =====>
            }
        }
    }
    
    /*================================================================================*/
    
    public PositionData getData(long hashCode)
    {
        PositionData data = null;
        boolean first = true;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            PositionData mapData = map.getData(hashCode);
            if (mapData != null) {
                if (data == null) {
                    data = mapData; first = true;
                } else if (first) {
                    data = new ChPositionData(data, mapData); first = false;
                } else {
                    data = data.add(mapData);
                }
            }
        }
        return data;
    }
    
    public int getNumOfData()
    {
        // TODO include writeMap into count
        if (getNumOfMaps() == 0) {
            return 0;
        } else if (getNumOfMaps() == 1) {
            return ((PositionReadMap)m_maps.get(0)).getNumOfData();
        } else {
            int num = 0;
            PositionDataIterator it = getDataIterator();
            while (it.getNext() != null) num++;
            return num;
        }
    }
    
    public int getNumOfDataLazy()
    {
        // TODO include writeMap into count
        if (getNumOfMaps() == 0) {
            return 0;
        } else if (getNumOfMaps() == 1) {
            return ((PositionReadMap)m_maps.get(0)).getNumOfData();
        } else {
            return -1;
        }
    }
    
    public int getNumOfPositions()
    {
        int num = 0;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            num += map.getNumOfPositions();
        }
        return num;
    }
    
    public int getNumOfGames()
    {
        int num = 0;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            num += map.getNumOfGames();
        }
        return num;
    }
    
    public int getLargestGameCounter()
    {
        // TODO does not work for more than one map!!!!!
        int max = 0;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if (map.getLargestGameCounter() > max) max = map.getLargestGameCounter();
        }
        return max;
        
    }
    
    public int getLargestCounterDifference()
    {
        int max = 0;
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if (map.getLargestCounterDifference() > max) max = map.getLargestCounterDifference();
        }
        return max;
    }
    
    /*================================================================================*/
    
    protected void addGameModel(GameModel gameModel)
    {
        if (m_writeMap == null) createWriteMap();        
        m_writeMap.addGameModel(gameModel);
    }
    
    public void putData(GameModel gameModel, int untilPlyNumber)
    {
        long free = Runtime.getRuntime().freeMemory(); 
        if (free < Runtime.getRuntime().totalMemory() / 10) {
            System.out.println("Low memory " + free + " " + Runtime.getRuntime().totalMemory());
            System.gc();
            System.out.println("  after gc " + Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().totalMemory());
            if (Runtime.getRuntime().freeMemory() - free < Runtime.getRuntime().totalMemory() / 100) {
                if (m_writeMap != null && m_writeMap.getNumOfData() > 1000) {
                    try {
                        System.out.println("Externalizing position map");
                        File file = File.createTempFile("chpm", ".tmp");
                        // get rid of all pointers to write map except the argument
                        // such that writeToDataFile can decide when to free the map
                        ChFilePositionMap.writeToDataFile(removeWriteMap(), file);
                        System.out.println("  after save " + Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().totalMemory());
                        System.gc();
                        System.out.println("  after gc " + Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().totalMemory());
                        addMap(new ChFilePositionMap(file));
                        createWriteMap();
                        file.deleteOnExit();   // remove at shutdown
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        super.putData(gameModel, untilPlyNumber);
    }
    
    public void putData(ImmutablePosition pos, Game game, short nextMove)
    {
        if (m_writeMap == null) createWriteMap();        
        m_writeMap.putData(pos, game, nextMove);
    }
    
    /*================================================================================*/
    
    public PositionDataIterator getDataIterator()
    {
        return new DataIterator();
    }
    
    public String toString()
    {
        boolean first = true;
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            if (!first) sb.append(", ");
            sb.append(map);
            first = false;
        }
        return sb.append("]").toString();
    }
    
    /*================================================================================*/
    
    public void printPerformanceStatistics()
    {
        for (Iterator it = m_maps.iterator(); it.hasNext(); ) {
            PositionReadMap map = (PositionReadMap)it.next();
            map.printPerformanceStatistics();
        }
    }
    
    
}