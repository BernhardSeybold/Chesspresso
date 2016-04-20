/*
 * IChPositionReadMap.java
 *
 * Created on 28. Juni 2001, 10:47
 */

package chesspresso.position.map;

import chesspresso.position.*;
import chesspresso.game.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public interface PositionReadMap
{
    public void close();

    public int getNumOfData();
    public int getNumOfPositions();
    public PositionData getData(ImmutablePosition pos);
    public PositionData getData(long hashCode);
    
    public int getNumOfGames();
    public GameModelIterator getGameModelIterator();
    public GameModel getGameModel(int index);
    public int getGameModelIndex(GameModel gameModel);
    public javax.swing.ListModel getGameModelListModel();
    public boolean containsGameModel(GameModel gameModel);
    public GameModel getGameModel(GameModel gameModel);
    
    public PositionDataIterator getDataIterator();    
    
    public int getLargestGameCounter();  // TODO remove from interface, only in ChAbstract...
    public int getLargestCounterDifference();  // TODO remove from interface, only in ChAbstract...
    
    void setGameOffset(int offset);
    int getGameOffset();        
    void initForWriting();
    
    public void printPerformanceStatistics();
}