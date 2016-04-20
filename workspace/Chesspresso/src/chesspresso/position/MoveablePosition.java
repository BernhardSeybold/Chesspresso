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
 * $Id: MoveablePosition.java,v 1.1 2002/12/08 13:27:36 BerniMan Exp $
 */

package chesspresso.position;

import chesspresso.move.*;

/**
 *
 * @author $Author: BerniMan $
 * @version $Revision: 1.1 $
 */
public interface MoveablePosition extends MutablePosition
{
    public void doMove(short move) throws IllegalMoveException;
    public void doMove(Move move) throws IllegalMoveException;
    
    public short getLastShortMove() throws IllegalMoveException;
    public Move getLastMove() throws IllegalMoveException;
    
    public boolean canUndoMove();
    public boolean undoMove();
    
    public boolean canRedoMove();
    public boolean redoMove();
    
    public short getMove(int from, int to, int promoPiece);
    
    public short[] getAllMoves();    
    public String getMovesAsString(short[] moves, boolean validateEachMove);
     
}