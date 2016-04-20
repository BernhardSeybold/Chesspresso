/*
 * ChAbstractPositionDataGame.java
 *
 * Created on 9. August 2001, 21:03
 */

package chesspresso.position.map;

import chesspresso.*;
import chesspresso.game.*;
import chesspresso.move.*;
import chesspresso.pgn.*;
import java.io.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public abstract class AbstractPositionDataGame extends ChAbstractPositionData
{
    
    protected long m_hashCode;

    /*================================================================================*/
    
    AbstractPositionDataGame(long hashCode)
    {
        m_hashCode = hashCode;
    }

    /*================================================================================*/
    
    public abstract PositionReadMap getMap();
    public abstract int getGameIndex();
    public abstract GameModel getGameModel();
    protected abstract short getNextMove();
    
    /*================================================================================*/
    
    public long getHashCode() {return m_hashCode;}
    protected GameHeaderModel getHeaderModel() {return getGameModel().getHeaderModel();}
    
    public int getNumOfGames() {return getGameModel() == null ? 0 : 1;}
    public int getWhiteWins()  {return getGameModel() != null && getHeaderModel().getResult() == Chess.RES_WHITE_WINS ? 1 : 0;}
    public int getDraws()      {return getGameModel() != null && getHeaderModel().getResult() == Chess.RES_DRAW       ? 1 : 0;}
    public int getBlackWins()  {return getGameModel() != null && getHeaderModel().getResult() == Chess.RES_BLACK_WINS ? 1 : 0;}

    public int getFirstOccurrence()
    {
        try {
            int year = PGN.getYearOfPGNDate(getHeaderModel().getDate());
            return (year == 1792 ? 9999 : year);  // error in ChessBase?? TODO move to ChPGN
        } catch (Exception ex) {  // NullPointer and IllegalArgument
            return 9999;
        }
    }
    
    public long getWhiteElos()
    {
        int elo = 0;
        if (getGameModel() != null) {
            elo = getHeaderModel().getWhiteElo();
            if (elo < 1500) elo = 0;
        } 
        return elo;
    }
        
    public long getBlackElos()
    {
        int elo = 0;
        if (getGameModel() != null) {
            elo = getHeaderModel().getBlackElo();
            if (elo < 1500) elo = 0;
        } 
        return elo;
    }
        
//    public long getBlackElos() {return getGameModel() == null ? 0 : getHeaderModel().getBlackElo();} 
      
    public int getWhiteEloGames() {return getWhiteElos() > 0 ? 1 : 0;}
    public int getBlackEloGames() {return getBlackElos() > 0 ? 1 : 0;}

    public boolean wasMovePlayed(short move) {return getNextMove() == move;}
    public short[] getPlayedMoves()
    {
        if (getNextMove() == Move.ILLEGAL_MOVE) return new short[0];
        short[] m = new short[1]; m[0] = getNextMove(); return m;
    }
    
    /*================================================================================*/
    
    public PositionData add(PositionData data)
    {
        if (data == null) {
            return this;
        } else {
//            return new ChPositionData(this, data);
            return new ChPositionData(this, data);
        }
//        return ((data == null) ? this : new ChPositionData(this, data));
    }
    
    public PositionData add(Game game, short nextMove)
    {
        if (getGameModel() == null) {
            new Exception().printStackTrace();
            return null;
//            m_gameModel = game.getModel();
//            m_nextMove = m_nextMove;
//            return this;
        } else {
            return add(new DefaultPositionDataGame(m_hashCode, game.getModel(), nextMove));
        }
    }
    
    /*================================================================================*/
    
    static final int getSize(int numOfGames) {return 8 + 2 + BitArray.getBytesForNumber(numOfGames);}
    
    public void write(DataOutput out, int numOfGames) throws IOException
    {
        out.writeLong(m_hashCode);
        out.writeShort(getNextMove());
        BitArray.writeInt(out, getGameIndex() + getMap().getGameOffset(), BitArray.getBytesForNumber(numOfGames));
    }
}