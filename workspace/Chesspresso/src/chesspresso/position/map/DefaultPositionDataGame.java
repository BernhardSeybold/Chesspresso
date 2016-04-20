/*
 * DefaultPositionDataGame.java
 *
 * Created on 21. Juli 2001, 13:53
 */

package chesspresso.position.map;

import chesspresso.game.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public class DefaultPositionDataGame extends AbstractPositionDataGame
{
    
    protected GameModel m_gameModel;
    protected short m_nextMove;

    /*================================================================================*/
    
    protected DefaultPositionDataGame(long hashCode, GameModel gameModel, short nextMove)
    {
        super(hashCode);
        m_gameModel = gameModel;
        m_nextMove = nextMove;
    }
    
    /*================================================================================*/
    
    public PositionReadMap getMap() {return null;}
    public int getGameIndex() {return -1;}    
    public GameModel getGameModel() {return m_gameModel;}
    protected short getNextMove() {return m_nextMove;}
    
}