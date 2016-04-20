/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id: PGNReader.java,v 1.2 2003/01/04 16:13:22 BerniMan Exp $
 */

package chesspresso.position;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import ch.seybold.util.PerformanceTest;
import chesspresso.game.Game;
import chesspresso.game.GameModel;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;


/**
 * Move performance.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public class MovePerformance extends PerformanceTest
{
   
    private int search(Position pos, int depth) throws Exception
    {
        if (depth < 0) return 1;
        
        short[] moves = pos.getAllMoves();
        int sum = 0;
        for (int i=0; i<moves.length; i++) {
            pos.doMove(moves[i]);
            sum += search(pos, depth - 1);
            pos.undoMove();
        }
        return sum;
    }
    
    @Test
    public void testSearch() throws Exception
    {
        resetTimer(new String[] {"positions"});
        startTimer();
        stopTimer(search(Position.createInitialPosition(), 4));
        printReport();
    }
    
    //======================================================================
    
    @Test
    public void testDosAndUndos() throws Exception
    {
        final int DO_MUL = 100, GEN_MUL = 10;
        
        resetTimer(new String[] {
            "doMove", "undoMove", "redoMove",
            "getAllMoves", "getAllCapturingMoves", "getAllNonCapturingMoves", "getAllReCapturingMoves"
        });
        
        String pgnFilename = "chesspresso/pgn/fidech99.pgn.gz";
        InputStream is = new GZIPInputStream(ClassLoader.getSystemResourceAsStream(pgnFilename));        
        PGNReader pgnReader = new PGNReader(is, pgnFilename);
        
        for (;;) {
            GameModel gameModel = pgnReader.parseGame();
            if (gameModel == null) break;
            Game game = new Game(gameModel);
            Move[] fullMoves = game.getMainLine();
            short[] moves = new short[fullMoves.length];
            for (int i=0; i<moves.length; i++) moves[i] = fullMoves[i].getShortMoveDesc();
            
            game.gotoStart();
            for (int i=0; i<DO_MUL; i++) {
                Position pos = new Position(game.getPosition());
                startTimer(0);
                for (int index=0; index<moves.length; index++) pos.doMove(moves[index]);
                stopTimer(moves.length);
                startTimer(1);
                for (int index=0; index<moves.length; index++) pos.undoMove();
                stopTimer(moves.length);                    
                startTimer(2);
                for (int index=0; index<moves.length; index++) pos.redoMove();
                stopTimer(moves.length);                    
            }
            
            Position pos = new Position(game.getPosition());
            for (int index=0; index<moves.length; index++) {
                startTimer(3);
                for (int i=0; i<GEN_MUL; i++) {
                    pos.getAllMoves();
                }
                stopTimer(GEN_MUL);
                startTimer(4);
                for (int i=0; i<GEN_MUL; i++) {
                    pos.getAllCapturingMoves();
                }
                stopTimer(GEN_MUL);
                startTimer(5);
                for (int i=0; i<GEN_MUL; i++) {
                    pos.getAllNonCapturingMoves();
                }
                stopTimer(GEN_MUL);
                if (index > 0) {
                    startTimer(6);
                    for (int i=0; i<GEN_MUL; i++) {
                        pos.getAllReCapturingMoves(moves[index-1]);
                    }
                    stopTimer(GEN_MUL);
                }
                pos.doMove(moves[index]);
            }
        }
        
        printReport();
    }
    
    //======================================================================
    
    @Test
    public void testPositionCreation() throws Exception
    {
        int MUL = 10000;
        
        resetTimer(new String[] {"createInitPos"});
        startTimer();
        for (int i=0; i<MUL; i++) {
            Position.createInitialPosition();
        }
        stopTimer(MUL);
        
        printReport();
    }
    
}