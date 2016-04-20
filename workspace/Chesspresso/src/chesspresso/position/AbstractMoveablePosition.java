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
 * $Id: AbstractMoveablePosition.java,v 1.2 2003/04/09 18:06:52 BerniMan Exp $
 */

package chesspresso.position;


import chesspresso.*;
import chesspresso.move.Move;
import chesspresso.move.IllegalMoveException;


/**
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class AbstractMoveablePosition extends AbstractMutablePosition
    implements MoveablePosition
{
    
    public void doMove(Move move) throws IllegalMoveException
    {
        doMove(move.getShortMoveDesc());
    }

    public short getMove(int from, int to, int promoPiece)
    {
        if (getColor(from) != getToPlay()) return Move.ILLEGAL_MOVE;  // =====>
        int piece = getPiece(from);
        if (piece == Chess.PAWN) {
            return Move.getPawnMove(from, to, Chess.sqiToCol(from) != Chess.sqiToCol(to), promoPiece);
        } else if (piece == Chess.KING && (to - from) ==  2) {
            return Move.getShortCastle(getToPlay());
        } else if (piece == Chess.KING && (to - from) == -2) {
            return Move.getLongCastle(getToPlay());
        } else {
            return Move.getRegularMove(from, to, !isSquareEmpty(to));
        }
    }

}