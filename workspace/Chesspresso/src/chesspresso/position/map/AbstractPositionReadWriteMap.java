/*
 * ChAbstractPositionReadWriteMap.java
 *
 * Created on 3. Juli 2001, 13:30
 */

package chesspresso.position.map;

import chesspresso.Chess;
import chesspresso.position.*;
import chesspresso.game.*;
import chesspresso.pgn.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  BerniMan
 * @version 
 */
public abstract class AbstractPositionReadWriteMap extends ChAbstractPositionReadMap implements PositionWriteMap
{
    
    private GameScorer m_gameScorer;
    
    /*================================================================================*/
    
    protected AbstractPositionReadWriteMap()
    {
        this(new DefaultGameScorer());
    }
    
    protected AbstractPositionReadWriteMap(GameScorer gameScorer)
    {
        m_gameScorer = gameScorer;
        resetChanged();
    }
    
    /*================================================================================*/
    
    protected abstract void addGameModel(GameModel gameModel);
    
    protected GameScorer getGameScorer() {return m_gameScorer;}
    
    public void putData(GameModel gameModel, int untilPlyNumber)
    {
        if (containsGameModel(gameModel)) {
            GameModel existingGameModel = getGameModel(gameModel);
            if (existingGameModel.getHeaderModel().isSimilar(gameModel.getHeaderModel())) {
                int score1 = getGameScorer().getScore(existingGameModel);
                int score2 = getGameScorer().getScore(gameModel);
//                Logger.log("Double found : " + gameModel + " vs. " + existingGameModel, this);
//                Logger.log("  score: " + score1 + " " + score2, this);
                if (score1 < score2) {
                    replaceGameModel(existingGameModel, gameModel);
                }
                return;  // =====>
            } else {
//                Logger.log("Same moves, but not a double : " + gameModel + " vs. " + existingGameModel, this);
            }
        }
        
        /*---------- insert the game now ----------*/
        addGameModel(gameModel); // needs to go first to allow PositionDataGame to access it via the index
		Game game = new Game(gameModel);
        game.gotoStart();
        for (int plyNumber=0; plyNumber < untilPlyNumber; plyNumber++) {
            Position pos = game.getPosition();
            putData(pos, game, game.getNextShortMove());
            if (!game.hasNextMove()) break;
            game.goForward();
        }
        // TODO fireGameModelChanged
    }
    
    public int putData(PGNReader reader, int untilPlyNumber) throws Exception
    {
        int numOfGames = 0, numOfErrors = 0;
//        if (listener != null) listener.notifyTask(1, 1, "Parse Games");
//        long progressTotal = reader.getTotalSize();
//        long progressStep = progressTotal / 200;  // do not call more than 200 times
//        long progressLast = -progressStep;
        for (;;) {
            try {
                GameModel gameModel = reader.parseGame();
                if (gameModel == null) break;
                int res = gameModel.getHeaderModel().getResult();
                if (res == Chess.RES_WHITE_WINS || res == Chess.RES_DRAW || res == Chess.RES_BLACK_WINS) {
                    putData(gameModel, untilPlyNumber);
                    numOfGames++;
                }
                // TODO use Runtime.getRuntime().freeMemory()
//                if ((numOfGames % 1000) == 0) System.out.println(numOfGames + " " + Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().totalMemory());
//                if (listener != null) {
//                    long cur = reader.getCurrentPosition();
//                    if (cur - progressLast > progressStep) {
//                        listener.notifyProgress(cur, progressTotal, numOfGames + " games parsed");
//                        progressLast = cur;
//                    }
//                }
            } catch (PGNSyntaxError ex) {
                System.out.println(ex.getMessage());
                numOfErrors++;
            } catch (IOException ex) {
                break;
            }
        }
        return numOfGames;
    }
    
    public int putData(PositionReadMap map, int untilPlyNumber) throws Exception
    {
//        if (listener != null) listener.startActivity("Load Position Map");
//        if (listener != null) listener.notifyTask(1, 1, "Load Games");
        int totalGames = map.getNumOfGames();
        int cur = 0;
        for (Iterator it = map.getGameModelIterator(); it.hasNext(); ) {
            GameModel gameModel = (GameModel)it.next();
            putData(gameModel, untilPlyNumber);
//            if (listener != null) listener.notifyProgress(++cur, totalGames, cur + " games loaded");
        }
//        if (listener != null) listener.stopActivity();
        return totalGames;
    }
    
}