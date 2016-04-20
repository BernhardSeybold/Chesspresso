/*
 * PositionData.java
 *
 * Created on 21. Juli 2001, 15:57
 */

package chesspresso.position.map;

import chesspresso.game.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public interface PositionData
{
    public long getHashCode();
    
    public int getToPlay();
    
    public int getNumOfGames();
    public int getWhiteWins();
    public int getDraws();
    public int getBlackWins();
    public int getWins(int player);
    
    public long getWhiteElos(); 
    public long getBlackElos(); 
    public long getElos(int player); 
     
    public int getWhiteEloGames();
    public int getBlackEloGames();
    public int getEloGames(int player);
    
    public int getWhiteEloAverage();
    public int getBlackEloAverage();
    public int getEloAverage(int player);

    public double getWhiteResult();
    public double getBlackResult();
    public double getResult(int player);

    public int getWhitePerformance();
    public int getBlackPerformance();
    public int getPerformance(int player);
    
    public double getWhiteExpectation();
    public double getBlackExpectation();
    public double getExpectation(int player);
    
    public boolean wasMovePlayed(short move);
    public short[] getPlayedMoves();
    
    public int getFirstOccurrence();
    public GameModel getGameModel();
    
    public PositionData add(PositionData data);
    public PositionData add(Game game, short nextMove);  // TODO gameModel?
    
    public int getLargestGameCounter();  // TODO: delete?
    
    
    public Object getBytes();
    
}