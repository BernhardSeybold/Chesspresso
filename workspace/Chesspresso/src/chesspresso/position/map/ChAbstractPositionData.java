/*
 * IChPositionData.java
 *
 * Created on 21. Juli 2001, 13:43
 */

package chesspresso.position.map;

import chesspresso.*;
import chesspresso.position.*;
import chesspresso.game.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public abstract class ChAbstractPositionData implements PositionData
{
        
    public int getToPlay() {return AbstractPosition.isWhiteToPlay(getHashCode()) ? Chess.WHITE : Chess.BLACK;}
        
    public int getNumOfGames() {return getWhiteWins() + getDraws() + getBlackWins();}
    
    //---------- elos ----------
    // override eather getXXXEloGames or getXXXEloAverage
    public long getWhiteElos() {return (long)getWhiteEloGames() * getWhiteEloAverage();} 
    public long getBlackElos() {return (long)getBlackEloGames() * getBlackEloAverage();}
    
    public int getWhiteEloAverage() {return getWhiteEloGames() == 0 ? -1 : (int)(getWhiteElos() / getWhiteEloGames());} 
    public int getBlackEloAverage() {return getBlackEloGames() == 0 ? -1 : (int)(getBlackElos() / getBlackEloGames());}
    
    //---------- result, performance ----------
    public double getWhiteResult() {return (getWhiteWins() + 0.5 * getDraws()) / getNumOfGames();}
    public double getBlackResult() {return (getBlackWins() + 0.5 * getDraws()) / getNumOfGames();}
    
    public double getWhiteExpectation()
    {
        double diff = getWhiteEloAverage() - getBlackEloAverage();
        return 1 / (1 + Math.pow(10, -diff / 400));
    }
    public double getBlackExpectation() {return 1 - getWhiteExpectation();}

    public int getWhitePerformance()
    {
        double diff = getWhiteEloAverage() - getBlackEloAverage();
        double res = getWhiteResult();
        return (int)(getWhiteEloAverage() + 400 * Math.log(res / (1 - res)) / Math.log(10));
    }
    
    public int getBlackPerformance()
    {
        double diff = getBlackEloAverage() - getWhiteEloAverage();
        double res = getBlackResult();
        return (int)(getBlackEloAverage() + 400 * Math.log(res / (1 - res)) / Math.log(10));
    }
    
    //---------- convenience methods with a player argument ----------
    public int    getWins       (int player) {return player == Chess.WHITE ? getWhiteWins()        : getBlackWins();}
    public double getResult     (int player) {return player == Chess.WHITE ? getWhiteResult()      : getBlackResult();}
    public long   getElos       (int player) {return player == Chess.WHITE ? getWhiteElos()        : getBlackElos();}
    public int    getEloAverage (int player) {return player == Chess.WHITE ? getWhiteEloAverage()  : getBlackEloAverage();}
    public int    getEloGames   (int player) {return player == Chess.WHITE ? getWhiteEloGames()    : getBlackEloGames();}
    public double getExpectation(int player) {return player == Chess.WHITE ? getWhiteExpectation() : getBlackExpectation();}
    public int    getPerformance(int player) {return player == Chess.WHITE ? getWhitePerformance() : getBlackPerformance();}


    public int getLargestGameCounter()
    {
        int counter = getWhiteWins();
        if (getDraws() > counter) counter = getDraws();
        if (getBlackWins() > counter) counter = getBlackWins();
        return counter;
    }
    
    /*================================================================================*/

    public GameModel getGameModel()
    {
        return null;
    }

    public boolean wasMovePlayed(short move)
    {
        short[] allMoves = getPlayedMoves();
        for (int i=0; i<allMoves.length; i++) {
            if (allMoves[i] == move) return true;  // =====>
        }
        return false;  // =====>
    }
    
    /*================================================================================*/
    
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PositionData)) return false;
        PositionData data = (PositionData)obj;
        if (getHashCode()            != data.getHashCode())            return false;
        if (getToPlay()              != data.getToPlay())              return false;
        if (getNumOfGames()          != data.getNumOfGames())          return false;
        if (getWhiteWins()           != data.getWhiteWins())           return false;
        if (getDraws()               != data.getDraws())               return false;
        if (getBlackWins()           != data.getBlackWins())           return false;
        if (getWhiteEloAverage()     != data.getWhiteEloAverage())     return false;
        if (getBlackEloAverage()     != data.getBlackEloAverage())     return false;
        if (getFirstOccurrence()     != data.getFirstOccurrence())     return false;
        if (getLargestGameCounter()  != data.getLargestGameCounter())  return false;
        if (!java.util.Arrays.equals(getPlayedMoves(), data.getPlayedMoves())) return false;
        return true;
    }
    
    public String toString()
    {
        return            getHashCode()
               + ": "   + getNumOfGames()
               + " ( +" + getWhiteWins()
               + " ="   + getDraws()
               + " -"   + getBlackWins()
               + " )";
    }
 
    public Object getBytes() {return this;}
    
 }