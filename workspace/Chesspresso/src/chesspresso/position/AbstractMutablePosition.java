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
 * $Id: AbstractMutablePosition.java,v 1.2 2003/04/09 18:07:21 BerniMan Exp $
 */

package chesspresso.position;


import chesspresso.*;


/**
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public abstract class AbstractMutablePosition extends AbstractPosition implements MutablePosition
{
    protected PositionListener[] m_listeners;     // protected to allow fast read access
    protected PositionChangeListener[] m_changeListeners;
    protected boolean m_notifyListeners;      // ... to check whether or not to fire
    protected boolean m_notifyPositionChanged;
    
    /*================================================================================*/
    
    protected AbstractMutablePosition()
    {
        m_listeners = null;
        m_changeListeners = null;
        m_notifyListeners = true;
        m_notifyPositionChanged = true;
    }
    
    /*================================================================================*/
    
    public int getPiece(int sqi) {return Chess.stoneToPiece(getStone(sqi));}
    public int getColor(int sqi) {return Chess.stoneToColor(getStone(sqi));}
    public boolean isSquareEmpty(int sqi) {return getStone(sqi) == Chess.NO_STONE;}
    
    /*================================================================================*/
    
    public void toggleToPlay() {setToPlay(Chess.otherPlayer(getToPlay()));}
    
    /*================================================================================*/
    
    public void clear()
    {
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        for (int sqi=0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            setStone(sqi, Chess.NO_STONE);
        }
        setSqiEP(Chess.NO_SQUARE);
        setCastles(NO_CASTLES);
        setToPlay(Chess.WHITE);
        setPlyNumber(0);
        setHalfMoveClock(0);
        
        m_notifyPositionChanged = notify;
        firePositionChanged();
    }
    
    public void setStart()
    {
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        FEN.initFromFEN(this, FEN.START_POSITION, true);
        
        m_notifyPositionChanged = notify;
        firePositionChanged();
    }
    
    public void set(ImmutablePosition position)
    {
        boolean notify = m_notifyPositionChanged;
        m_notifyPositionChanged = false;
        
        for (int sqi=0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            setStone(sqi, position.getStone(sqi));
        }
        setCastles(position.getCastles());
        setSqiEP(position.getSqiEP());
        setToPlay(position.getToPlay());
        setPlyNumber(position.getPlyNumber());
        setHalfMoveClock(position.getHalfMoveClock());
        
        m_notifyPositionChanged = notify;
        firePositionChanged();
    }
    
    /*================================================================================*/
    // inverse
    
    public final void inverse()
    {
        /*---------- inverse stones ----------*/
        // avoid to have two same kings on the board at the same time
        int[] stones = new int[Chess.NUM_OF_SQUARES];
        for (int sqi = 0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            stones[sqi] = getStone(sqi);
            setStone(sqi, Chess.NO_STONE);
        }
        for (int sqi = 0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            int partnerSqi = Chess.coorToSqi(Chess.sqiToCol(sqi), Chess.NUM_OF_ROWS - Chess.sqiToRow(sqi) - 1);
            setStone(sqi, Chess.getOpponentStone(stones[partnerSqi]));
        }
        
        /*---------- inverse en passant square ----------*/
        int sqiEP = getSqiEP();
        if (sqiEP != Chess.NO_SQUARE) {
            setSqiEP(Chess.coorToSqi(Chess.sqiToCol(sqiEP), Chess.NUM_OF_ROWS - Chess.sqiToRow(sqiEP) - 1));
        }
        
        /*---------- inverse castles ----------*/
        int castles = getCastles();
        setCastles(NO_CASTLES);
        if ((castles & WHITE_SHORT_CASTLE) != 0) includeCastles(BLACK_SHORT_CASTLE);
        if ((castles & WHITE_LONG_CASTLE)  != 0) includeCastles(BLACK_LONG_CASTLE);
        if ((castles & BLACK_SHORT_CASTLE) != 0) includeCastles(WHITE_SHORT_CASTLE);
        if ((castles & BLACK_LONG_CASTLE)  != 0) includeCastles(WHITE_LONG_CASTLE);
        
        /*---------- inverse to play ----------*/
        toggleToPlay();
    }
    
    /*================================================================================*/
    // convenience methods

    public final void includeCastles(int whichCastles)
    {
        setCastles(getCastles() | whichCastles);
    }
    
    public final void excludeCastles(int whichCastles)
    {
        setCastles(getCastles() & (~whichCastles));
    }
    
    public final void resetHalfMoveClock()
    {
        setHalfMoveClock(0);
    }
    
    public final void incHalfMoveClock()
    {
        setHalfMoveClock(getHalfMoveClock() + 1);
    }
    
    /*================================================================================*/
    // trigger listeners
    
    protected void fireSquareChanged(int sqi)
    {
        if (m_notifyListeners && m_listeners != null) {
            int stone = getStone(sqi);
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].squareChanged(sqi, stone);
            }
            firePositionChanged();
        }
    }
    
    protected void fireToPlayChanged()
    {
        if (m_notifyListeners && m_listeners != null) {
            int toPlay = getToPlay();
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].toPlayChanged(toPlay);
            }
            firePositionChanged();
        }
    }
    
    protected void fireSqiEPChanged()
    {
        if (m_notifyListeners && m_listeners != null) {
            int sqiEP = getSqiEP();
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].sqiEPChanged(sqiEP);
            }
            firePositionChanged();
        }
    }
    
    protected void fireCastlesChanged()
    {
        if (m_notifyListeners && m_listeners != null) {
            int castles = getCastles();
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].castlesChanged(castles);
            }
            firePositionChanged();
        }
    }
    
    protected void firePlyNumberChanged()
    {
        if (m_notifyListeners && m_listeners != null) {
            int plyNumber = getPlyNumber();
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].plyNumberChanged(plyNumber);
            }
            firePositionChanged();
        }
    }
    
    protected void fireHalfMoveClockChanged()
    {
        if (m_notifyListeners && m_listeners != null) {
            int halfMoveClock = getHalfMoveClock();
            for (int i=0; i<m_listeners.length; i++) {
                m_listeners[i].halfMoveClockChanged(halfMoveClock);
            }
            firePositionChanged();
        }
    }
    
    protected void fireMoveDone(short move)
    {
        if (m_notifyListeners && m_changeListeners != null) {
            for (int i=0; i<m_changeListeners.length; i++) {
                m_changeListeners[i].notifyMoveDone(this, move);
            }
        }
    }
    
    protected void fireMoveUndone()
    {
        if (m_notifyListeners && m_changeListeners != null) {
            for (int i=0; i<m_changeListeners.length; i++) {
                m_changeListeners[i].notifyMoveUndone(this);
            }
        }
    }
    
//    private void firePositionChanged()
    public void firePositionChanged()
    {
        if (m_notifyPositionChanged && m_changeListeners != null) {
            for (int i=0; i<m_changeListeners.length; i++) {
                m_changeListeners[i].notifyPositionChanged(this);
            }
        }
    }
    
    /*================================================================================*/
    // IChPositionListener
    
    public final void addPositionListener(PositionListener listener)
    {
        if (m_listeners == null) {
            m_listeners = new PositionListener[1];
            m_listeners[0] = listener;
        } else {
            PositionListener[] listeners = m_listeners;
            m_listeners = new PositionListener[listeners.length + 1];
            System.arraycopy(listeners, 0, m_listeners, 0, listeners.length);
            m_listeners[m_listeners.length - 1] = listener;
        }
        
        for (int sqi = 0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
            listener.squareChanged(sqi, getStone(sqi));
        }
        listener.toPlayChanged(getToPlay());
        listener.castlesChanged(getCastles());
        listener.sqiEPChanged(getSqiEP());
    }
    
    public final void removePositionListener(PositionListener listener)
    {
        for (int i=0; i<m_listeners.length; i++) {
            if (m_listeners[i] == listener) {
                if (m_listeners.length == 1) {
                    m_listeners = null;
                } else {
                    PositionListener[] listeners = m_listeners;
                    m_listeners = new PositionListener[listeners.length - 1];
                    System.arraycopy(listeners, 0, listeners, 0, i);
                    System.arraycopy(listeners, i + 1, listeners, i, m_listeners.length - i - 1);
                }
                return;  // =====>
            }
        }
    }
    
    public final synchronized void setNotifyListeners(boolean notify)
    {
        m_notifyListeners = notify;
    }
    
    /*================================================================================*/
    // IChPositionChangeListener
    
    public final void addPositionChangeListener(PositionChangeListener listener)
    {
//        System.out.println("addPositionChangeListener " + listener);
        if (m_changeListeners == null) {
            m_changeListeners = new PositionChangeListener[1];
            m_changeListeners[0] = listener;
        } else {
            PositionChangeListener[] oldListeners = m_changeListeners;
            m_changeListeners = new PositionChangeListener[oldListeners.length + 1];
            System.arraycopy(oldListeners, 0, m_changeListeners, 0, oldListeners.length);
            m_changeListeners[m_changeListeners.length-1] = listener;
        }
        
        listener.notifyPositionChanged(this);  // for initialization
//        for (int i=0; i<m_changeListeners.length; i++) {
//            System.out.println(m_changeListeners[i]);
//        }
    }
    
    public final void removePositionChangeListener(PositionChangeListener listener)
    {
//        System.out.println("removePositionChangeListener " + listener);
        for (int i=0; i<m_changeListeners.length; i++) {
            if (m_changeListeners[i] == listener) {
                if (m_changeListeners.length == 1) {
                    m_changeListeners = null;
                } else {
                    PositionChangeListener[] oldListeners = m_changeListeners;
                    m_changeListeners = new PositionChangeListener[oldListeners.length - 1];
                    System.arraycopy(oldListeners, 0, m_changeListeners, 0, i);
                    System.arraycopy(oldListeners, i + 1, m_changeListeners, i, m_changeListeners.length - i - 1);
                }
                break;  // =====>
            }
        }
//        for (int i=0; i<m_changeListeners.length; i++) {
//            System.out.println(m_changeListeners[i]);
//        }
    }
    
}