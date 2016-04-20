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
 * $Id: ImmutablePosition.java,v 1.1 2002/12/08 13:27:35 BerniMan Exp $
 */

package chesspresso.position;

/**
 *
 * @author Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public interface ImmutablePosition
{
    // TODO have method to get initial ply number?
    
    //======================================================================
    // constants for castle mask
    
    public final int
        NO_CASTLES         = 0,
        WHITE_LONG_CASTLE  = 1,
        WHITE_SHORT_CASTLE = 2,
        BLACK_LONG_CASTLE  = 4,
        BLACK_SHORT_CASTLE = 8,
        WHITE_CASTLE       = WHITE_LONG_CASTLE + WHITE_SHORT_CASTLE,
        BLACK_CASTLE       = BLACK_LONG_CASTLE + BLACK_SHORT_CASTLE,
        ALL_CASTLES        = WHITE_CASTLE + BLACK_CASTLE;
    
    //======================================================================
    // read access
    
    /**
     * Return the stone currently on the given square.
     *
     *@param sqi the square
     *@return the stone of the given square
     */
    public int getStone(int sqi);
    
    /**
     * Return the current en passant square.
     *
     *@return the current en passant square, NO_SQUARE if none
     */
    public int getSqiEP();
    
    /**
     * Return the still allowed castles as mask.
     *
     *@return the still allowed castles as mask.
     */
    public int getCastles();
    
    /**
     * Return the player whose turn it is.
     *
     *@return the player whose turn it is
     */
    public int getToPlay();
    
    /**
     * Return the current ply number.
     *
     *@return the current ply number, starting at play no. 0
     */
    public int getPlyNumber();
    
    /**
     * Return the number of moves since the last capture and the last pawn move.
     * This number is used for the 50-move rule.
     *
     *@return the number of moves since the last capture and the last pawn move
     */
    public int getHalfMoveClock();
    
    /**
     * Return whether the current position is legal.
     *
     *@return whether the current position is legal
     */
    public boolean isLegal();
    
    //======================================================================
    // FEN
    
    /**
     * Return the FEN representation of the current position
     *{@link FEN}
     *
     *@return the FEN representation of the current position
     */
    public String getFEN();
    
    //======================================================================
    
    /**
     * Returns whether the represented position is the startposition
     *
     @return whether the represented position is the startposition
     */
    public boolean isStartPosition();
    
    //======================================================================
    // hash codes
    
    /**
     * Returns a 64bit hash code of the current position.
     * 64bit should be enough to disnstinguish positions with almost no collisions.
     * TODO: add reference to paper
     *
     *@return a 64bit hash code
     */
    public long getHashCode();
    
    /**
     * Returns a 32bit hash code of the current position.
     * 32 bit is not enough to distinguish positions reliably, use only if
     * collisions are handled.
     *
     *@return a 32bit hash code
     */
    public int hashCode();
    
    //======================================================================

    /**
     * Validates the internal state. Used for debugging and testing.
     *
     *@throws IllegalPositionException if the internal state is illegal
     */
    void validate() throws IllegalPositionException;
    
}