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
 * $Id: PGNReaderTest.java,v 1.3 2003/04/11 17:50:46 BerniMan Exp $
 */

package chesspresso.pgn;

import junit.framework.*;
import ch.seybold.util.FootprintTestCase;
import chesspresso.game.*;
import chesspresso.move.*;
import chesspresso.position.*;

import java.io.*;
import java.util.zip.*;


/**
 * Tests for the PGNReader.
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public class PGNReaderTest extends FootprintTestCase
{
    
    public static Test suite()
    {
        return new TestSuite(PGNReaderTest.class);
    }
    
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    //======================================================================
    
    public void testPGNReadWrite() throws Exception
    {
        doParseTest("PGNTest", true);
    }
    
    public void testPGNSuite() throws Exception
    {
        doParseTest("PGNTestSuite", false);
    }
    
    public void testFidech99() throws Exception
    {
        doExtendedTest("fidech99", true);
    }
    
    public void testChusa99() throws Exception
    {
        doExtendedTest("chusa99", true);
    }
    
    public void doParseTest(String name, boolean zipped) throws Exception
    {
        String pgnFilename;
        InputStream is;
        String footprintName;
        if (zipped) {
            pgnFilename = "chesspresso/pgn/" + name + ".pgn.gz";
            is = new GZIPInputStream(ClassLoader.getSystemResourceAsStream(pgnFilename));
            startFootprint(name + ".pgn.footprint.gz", true);
        } else {
            pgnFilename = "chesspresso/pgn/" + name + ".pgn";
            is = ClassLoader.getSystemResourceAsStream(pgnFilename);
            startFootprint(name + ".pgn.footprint", false);
        }
        
        PGNReader pgnReader = new PGNReader(is, pgnFilename);
        pgnReader.setErrorHandler(new PGNErrorHandler() {
            public void handleError(PGNSyntaxError error) {writeln(PGN.TOK_PGN_ESCAPE + error.toString());}
            public void handleWarning(PGNSyntaxError warning)  {writeln(PGN.TOK_PGN_ESCAPE + warning.toString());}
        });
        
        PGNWriter pgnWriter = new PGNWriter(getFootprint());
        
        for (;;) {
            try {
                GameModel gameModel = pgnReader.parseGame();
                if (gameModel == null) break;
                pgnWriter.write(gameModel);
                writeln();
            } catch (Exception ex) {
                writeln(ex.getMessage());
            }
        }

        stopFootprint();
    }
    
    public void doExtendedTest(String name, boolean zipped) throws Exception
    {
        String pgnFilename;
        InputStream is;
        if (zipped) {
            pgnFilename = "chesspresso/pgn/" + name + ".pgn.gz";
            is = new GZIPInputStream(ClassLoader.getSystemResourceAsStream(pgnFilename));
        } else {
            pgnFilename = "chesspresso/pgn/" + name + ".pgn";
            is = ClassLoader.getSystemResourceAsStream(pgnFilename);
        }
        
        
        PGNReader pgnReader = new PGNReader(is, pgnFilename);
        pgnReader.setErrorHandler(new PGNErrorHandler() {
            public void handleError(PGNSyntaxError error) {writeln(error.toString());}
            public void handleWarning(PGNSyntaxError warning)  {writeln(warning.toString());}
        });
        
        startFootprint(name + ".footprint.gz", true);
        
        boolean first = true;
        for (;;) {
            if (!first) writeln("");
            first = false;
            GameModel gameModel = pgnReader.parseGame();
            if (gameModel == null) break;
            Game game = new Game(gameModel);
            
            writeln(game.getInfoString());
            game.gotoStart();
            
            /*---------- check start position ----------*/
            String fen = game.getTag(PGN.TAG_FEN);
            if (fen != null) {
                Position fenPos = new Position(fen, false);
                assertEquals("FEN present, but start position is wrong, fen = " + fen, fenPos.getHashCode(), game.getPosition().getHashCode());
            } else {
                assertTrue("initial position is not start position", game.getPosition().isStartPosition());
            }           
            
            int numOfPlies = game.getNumOfPlies();
            long[] hashCodes = new long[numOfPlies + 1];
            for (int plyIndex=0; plyIndex <= numOfPlies; plyIndex++) {
                Position pos = new Position(game.getPosition());
                
                writeln(pos.toString());
                
                /*---------- validate position ----------*/
                pos.validate();
                
                /*---------- store hash code ----------*/
                hashCodes[plyIndex] = pos.getHashCode();
                
                /*---------- test getAllMoves ----------*/
                short allMoves[] = pos.getAllMoves();
                writeln(pos.getMovesAsString(allMoves, true));
                assertEquals("error in canMove", allMoves.length != 0, pos.canMove());

                /*---------- test capturing moves ----------*/
                short[] capturingMoves = pos.getAllCapturingMoves();                
                for (int i = 0; i < capturingMoves.length; i++) {
                    short moveDesc = capturingMoves[i];
                    assertTrue("Move produced by 'getAllCapturingMoves' is non-capturing: " + Integer.toBinaryString(moveDesc) + " " + Move.getString(moveDesc), Move.isCapturing(moveDesc));
                    pos.doMove(moveDesc);
                    Move m = pos.getLastMove();
                    assertTrue("Move.isCapturing is wrong for capturing move: " + m, m.isCapturing());
                    assertEquals("Move.isCheck does not correspond to pos.isCheck" + m, m.isCheck(), pos.isCheck());
                    assertEquals("Move.isCheck does not correspond to pos.isCheck (2nd check)" + m, m.isCheck(), pos.isCheck());
                    assertEquals("Move.isMate does not correspond to pos.isMate" + m, m.isMate(), pos.isMate());
                    assertEquals("Move.isMate does not correspond to pos.isMate (2nd check)" + m, m.isMate(), pos.isMate());
                    pos.undoMove();
                }
                
                /*---------- test non-capturing moves ----------*/
                short[] nonCapturingMoves = pos.getAllNonCapturingMoves();
                for (int i = 0; i < nonCapturingMoves.length; i++) {
                    short moveDesc = nonCapturingMoves[i];
                    assertTrue("Move produced by 'getAllNonCapturingMoves' is capturing: " + Move.getString(moveDesc), !Move.isCapturing(moveDesc));
                    pos.doMove(moveDesc);
                    Move m = pos.getLastMove();
                    assertTrue("Move.isCapturing is wrong for non-capturing move: " + m, !m.isCapturing());
                    assertEquals("Move.isCheck does not correspond to pos.isCheck" + m, m.isCheck(), pos.isCheck());
                    assertEquals("Move.isCheck does not correspond to pos.isCheck (2nd check)" + m, m.isCheck(), pos.isCheck());
                    assertEquals("Move.isMate does not correspond to pos.isMate" + m, m.isMate(), pos.isMate());
                    assertEquals("Move.isMate does not correspond to pos.isMate (2nd check)" + m, m.isMate(), pos.isMate());
                    pos.undoMove();
                }
                
                /*---------- test sum ----------*/
                assertEquals("capturing / non-capturing moves don't add up", allMoves.length, capturingMoves.length + nonCapturingMoves.length);
                
                /*---------- move forward ----------*/
                if (plyIndex < numOfPlies) {
                    assertTrue("Unexpected end of game at ply " + plyIndex + " of " + numOfPlies, game.hasNextMove());
                    game.goForward();
                }
            }
            for (int plyIndex = numOfPlies - 1; plyIndex >=0; plyIndex--) {
                assertTrue("cannot undo move", game.getPosition().canUndoMove());
                game.getPosition().undoMove();
                assertEquals("hash code changed while undoing moves", hashCodes[plyIndex], game.getPosition().getHashCode());
            }
            for (int plyIndex = 0; plyIndex < numOfPlies; plyIndex++) {
                assertTrue("cannot redo move", game.getPosition().canRedoMove());
                assertEquals("hash code changed while redoing moves", hashCodes[plyIndex], game.getPosition().getHashCode());
                game.getPosition().redoMove();
            }

        }

        stopFootprint();
    }
}
