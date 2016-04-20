/*
 * IChPositionWriteMap.java
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
public interface PositionWriteMap
{
    public boolean isChanged();
    public void resetChanged();
    public void putData(ImmutablePosition pos, Game game, short nextMove);
    public void replaceGameModel(GameModel oldGameModel, GameModel newGameModel);
}