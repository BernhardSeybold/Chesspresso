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
 * $Id: LightWeightPosition.java,v 1.1 2002/12/08 13:27:36 BerniMan Exp $
 */

package chesspresso.position;

import chesspresso.*;
import chesspresso.move.*;

/**
 * A light-weight implementation of the position interface.
 *
 * This class is optimized for simplicity of the underlying data structure, not
 * for speed of accessors nor for memory footprint. Use this class if you do not
 * care about performance.
 *
 * @author  BerniMan
 * @version $Revision: 1.1 $
 */
public class LightWeightPosition extends AbstractMutablePosition
{
    private int[] m_stone;
    private int m_sqiEP;
    private int m_castles;
    private int m_toPlay;
    private int m_plyNumber;
    private int m_halfMoveClock;
    
    /*================================================================================*/
    
    public LightWeightPosition()
    {
        m_stone = new int[Chess.NUM_OF_SQUARES];
        clear();
    }

    public LightWeightPosition(ImmutablePosition position)
    {
        this();
        set(position);
    }
    
    /*================================================================================*/
    
    public int getStone(int sqi)  {return m_stone[sqi];}    
    public int getToPlay()        {return m_toPlay;}
    public int getSqiEP()         {return m_sqiEP;}
    public int getCastles()       {return m_castles;}    
    public int getPlyNumber()     {return m_plyNumber;}    
    public int getHalfMoveClock() {return m_halfMoveClock;}

    /*================================================================================*/
    
    public void setStone(int sqi, int stone)
    {
        if (m_stone[sqi] != stone) {
            m_stone[sqi] = stone;
            fireSquareChanged(sqi);
        }
    }

    public void setCastles(int castles)
    {
        if (m_castles != castles) {
            m_castles = castles;
            fireCastlesChanged();
        }
    }
    
    public void setSqiEP(int sqiEP)
    {
        if (m_sqiEP != sqiEP) {
            m_sqiEP = sqiEP;
            fireSqiEPChanged();
        }
    }
    
    public void setToPlay(int toPlay)
    {
        if (m_toPlay != toPlay) {
            m_toPlay = toPlay;
            fireToPlayChanged();
        }
    }
    
    public void setPlyNumber(int plyNumber)
    {
        if (m_plyNumber != plyNumber) {
            m_plyNumber = plyNumber;
            firePlyNumberChanged();
        }
    }
    
    public void setHalfMoveClock(int halfMoveClock)
    {
        if (m_halfMoveClock != halfMoveClock) {
            m_halfMoveClock = halfMoveClock;
            fireHalfMoveClockChanged();
        }
    }
    
    /*================================================================================*/
    
    public void doMove(short move) throws IllegalMoveException
    {
        throw new IllegalMoveException ("Moves not supported");
    }
    
    public boolean canUndoMove() {return false;}
    
    public boolean undoMove() {return false;}
    
    public short getLastShortMove() throws IllegalMoveException
    {
        throw new IllegalMoveException ("Moves not supported");
    }
    
    public Move getLastMove() throws IllegalMoveException
    {
        throw new IllegalMoveException ("Moves not supported");
    }
    
    public boolean canRedoMove() {return false;}
    public boolean redoMove() {return false;}
    
}