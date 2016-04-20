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
 * $Id: MoveablePositionTests.java,v 1.2 2003/04/11 17:47:28 BerniMan Exp $
 */

package chesspresso.position;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.junit.Test;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;

/**
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class MoveablePositionTests extends MutablePositionTests
{
    
    protected abstract MoveablePosition createMoveablePosition();
    
    //======================================================================
    
    private String getAllMoves(MoveablePosition position)
    {
        short[] moves = position.getAllMoves();
        Move.normalizeOrder(moves);
        return position.getMovesAsString(moves, true);
    }
    
    private String readLine(LineNumberReader in) throws IOException
    {
        for (;;) {
            String line = in.readLine();
            if (line == null) return null;
            line = line.trim();
            if (line.length() > 0 && !line.startsWith(";")) {
                return line;
            }
        }
    }
    
    //======================================================================
    
    @Test
    public void testMove() throws IllegalMoveException
    {
        MoveablePosition position = createMoveablePosition();
        
        position.setStart();
        position.doMove(Move.getPawnMove(Chess.E2, Chess.E4, false, Chess.NO_PIECE));
    }
    
    @Test
    public void testGenerateMoves_basic()
    {
        MoveablePosition position = createMoveablePosition();
        
        position.setStart();
        assertEquals("Moves in startpos",
                     "{Na3,a3,b3,Nc3,c3,d3,e3,Nf3,f3,g3,Nh3,h3,a4,b4,c4,d4,e4,f4,g4,h4}",
                     getAllMoves(position));
    }
    
    @Test
    public void testGenerateMoves_extended() throws IOException
    {        
        LineNumberReader in = new LineNumberReader(
            new InputStreamReader(
                ClassLoader.getSystemResourceAsStream("chesspresso/position/testGenerateMoves.txt")));
        
        for (;;) {
            String fen = readLine(in);
            if (fen == null) break; // =====>
            String fileMoves = readLine(in);
            MoveablePosition position = createMoveablePosition();
            FEN.initFromFEN(position, fen, true);
            String moves = getAllMoves(position);
            assertEquals("Moves wrong in position \"" + fen + "\"", fileMoves, moves);
        }
    }
    
}