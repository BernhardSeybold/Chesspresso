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
 * $Id: PositionView.java,v 1.2 2003/01/04 16:25:44 BerniMan Exp $
 */

package chesspresso.position.view;

import chesspresso.*;
import chesspresso.position.*;

import java.awt.*;
import java.awt.event.*;


/**
 * Position view.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public class PositionView extends java.awt.Component
    implements PositionListener, MouseListener, MouseMotionListener
{
    private int m_bottom;
    private AbstractMutablePosition m_position;
    private Color m_enPassantColor;
    private Color m_whiteSquareColor;
    private Color m_blackSquareColor;
    private Color m_whiteColor;
    private Color m_blackColor;
    private String m_pieceChars;
    
    private int m_draggedFrom;
    private int m_draggedStone;
    private int m_draggedX, m_draggedY;
    private int m_draggedPartnerSqi;
    private PositionMotionListener m_positionMotionListener;
    
    //======================================================================
    
    /**
     * Create a new position view.
     *
     *@param position the position to display
     */
    public PositionView(AbstractMutablePosition position)
    {
        this(position, Chess.WHITE);
    }
    
    /**
     * Create a new position view.
     *
     *@param position the position to display
     *@param bottomPlayer the player at the lower edge
     */
    public PositionView(AbstractMutablePosition position, int bottomPlayer)
    {
        m_position = position;
        m_bottom = bottomPlayer;
        m_enPassantColor = null;
        m_whiteSquareColor = new Color(128, 128, 192);
        m_blackSquareColor = new Color(0, 0, 192);
        m_whiteColor = new Color(255, 255, 255);  // jdk1.4: Color.WHITE, jdk1.3: Color.white
        m_blackColor = new Color(0, 0, 0); // jdk1.4: Color.BLACK, jdk1.3: Color.black
        setFont(new Font("Chess Cases", Font.PLAIN, 32));
        setPieceChars("kpqrbn mvtwol");
        
        m_draggedStone = Chess.NO_STONE;
        m_draggedFrom = Chess.NO_SQUARE;
        m_draggedPartnerSqi = Chess.NO_SQUARE;
        m_positionMotionListener = null;
        m_position.addPositionListener(this);  // TODO: when do we remove it?
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    //======================================================================
    
    public void setPositionMotionListener(PositionMotionListener listener)
    {
        m_positionMotionListener = listener;
    }
    
    public int getBottomPlayer() {return m_bottom;}
    
    public void setBottomPlayer(int player)
    {
        if (player != m_bottom) {
            m_bottom = player;
            repaint();
        }
    }
    
    public Color getWhiteSquareColor() {return m_whiteSquareColor;}
    public Color getBlackSquareColor() {return m_blackSquareColor;}
    public Color getWhiteColor() {return m_whiteColor;}
    public Color getBlackColor() {return m_blackColor;}
    public String getPieceChars() {return m_pieceChars;}
    
    public void setWhiteSquareColor(Color color)
    {
        if (m_whiteSquareColor == color) return;
        m_whiteSquareColor = color;
        repaint();
    }
    
    public void setBlackSquareColor(Color color)
    {
        if (m_blackSquareColor == color) return;
        m_blackSquareColor = color;
        repaint();
    }
    
    public void setWhiteColor(Color color)
    {
        if (m_whiteColor == color) return;
        m_whiteColor = color;
        repaint();
    }
    
    public void setBlackColor(Color color)
    {
        if (m_blackColor == color) return;
        m_blackColor = color;
        repaint();
    }
    
    private static Color stringToColor(String s, Color def)
    {
        try {
            return new Color(Integer.parseInt(s));
        } catch (NumberFormatException ex) {
            return def;
        }
    }
    
    private static String colorToString(Color color)
    {
        return "" + color.getRGB();
    }
    
    public void setProperties(PositionViewProperties props)
    {
        PositionView view = props.getPositionView();
        setWhiteColor(view.getWhiteColor());
        setBlackColor(view.getBlackColor());
        setWhiteSquareColor(view.getWhiteSquareColor());
        setBlackSquareColor(view.getBlackSquareColor());
        setFont(view.getFont());
    }
    
    public void setProperties(java.util.Properties properties)
    {
        setWhiteColor(stringToColor(properties.getProperty("PV_whiteColor"), getWhiteColor()));
        setBlackColor(stringToColor(properties.getProperty("PV_blackColor"), getBlackColor()));
        setWhiteSquareColor(stringToColor(properties.getProperty("PV_whiteSquareColor"), getWhiteSquareColor()));
        setBlackSquareColor(stringToColor(properties.getProperty("PV_blackSquareColor"), getBlackSquareColor()));
        setFont(Font.decode(properties.getProperty("PV_font")));
    }
    
    public void getProperties(java.util.Properties properties)
    {
        properties.setProperty("PV_whiteColor", colorToString(getWhiteColor()));
        properties.setProperty("PV_blackColor", colorToString(getBlackColor()));
        properties.setProperty("PV_whiteSquareColor", colorToString(getWhiteSquareColor()));
        properties.setProperty("PV_blackSquareColor", colorToString(getBlackSquareColor()));
        properties.setProperty("PV_font", getFont().getFamily() + "-plain-" + getFont().getSize());
    }
    
    public void setFont(Font font)
    {
        super.setFont(font); repaint();
    }
    
    public void setPieceChars(String pieceChars)
    {
        m_pieceChars = pieceChars + "             ".substring(0, 13);
    }
        
    /**
     * Flip the sides.
     */
    public void flip()
    {
        setBottomPlayer(Chess.otherPlayer(m_bottom));
    }
    
    /**
     * Sets the color to mark the en passant square (if exists). Set to null to
     * not mark.
     *
     *@param color the color to mark the en passant square
     */
    public void setEnPassantColor(Color color)
    {
        m_enPassantColor = color;
        sqiEPChanged(m_position.getSqiEP());
    }
    
    public AbstractMutablePosition getPosition() {return m_position;}
    
    public Dimension getPreferredSize() {return new Dimension(8*getFont().getSize(), 8*getFont().getSize());}
    public Dimension getMinimumSize() {return new Dimension(8*getFont().getSize(), 8*getFont().getSize());}
    public Dimension getMaximumSize() {return new Dimension(8*getFont().getSize(), 8*getFont().getSize());}

    //======================================================================
    // interface PositionListener
    
    public void squareChanged(int sqi, int stone)
    {
        repaint();
    }
    
    public void toPlayChanged(int toPlay) {} // TODO white / black border?
    public void castlesChanged(int castles) {}
    
    public void sqiEPChanged(int sqiEP)
    {
    }
    
    public void plyNumberChanged(int plyNumber) {}
    public void halfMoveClockChanged(int halfMoveClock) {}
    
    //======================================================================
    // MouseListener and MouseMotionListener methods
    
    private int getSquareForEvent(MouseEvent evt)
    {
        int size = getFont().getSize();
        return (m_bottom == Chess.WHITE
              ? Chess.coorToSqi(evt.getX() / size, Chess.NUM_OF_ROWS - evt.getY() / size - 1)
              : Chess.coorToSqi(Chess.NUM_OF_COLS - evt.getX() / size, evt.getY() / size - 1));
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e)
    {
        if (m_positionMotionListener == null) return;
        m_draggedFrom = getSquareForEvent(e);
        if (m_positionMotionListener.allowDrag(m_position, m_draggedFrom)) {
            m_draggedStone = m_position.getStone(m_draggedFrom);
            m_draggedX = e.getX();
            m_draggedY = e.getY();
            m_draggedPartnerSqi = m_positionMotionListener.getPartnerSqi(m_position, m_draggedFrom);
            //TODO mark m_draggedPartnerSqi
            repaint();
        } else {
            m_positionMotionListener.squareClicked(m_position, m_draggedFrom, e);
            m_draggedFrom = Chess.NO_SQUARE;
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if (m_positionMotionListener == null) return;
        if (m_draggedFrom != Chess.NO_SQUARE) {
            int draggedTo = getSquareForEvent(e);
            if (draggedTo != Chess.NO_SQUARE) {
                if (m_draggedFrom == draggedTo) {
                    if (m_draggedPartnerSqi != Chess.NO_SQUARE) {
                        m_positionMotionListener.dragged(m_position, m_draggedFrom, m_draggedPartnerSqi, e);
                    } else {
                        m_positionMotionListener.squareClicked(m_position, m_draggedFrom, e);
                    }
                } else {
                    m_positionMotionListener.dragged(m_position, m_draggedFrom, draggedTo, e);
                }
            }
            m_draggedFrom = Chess.NO_SQUARE;
            m_draggedStone = Chess.NO_STONE;
            // TODO unmark m_draggedPartnerSqi
            repaint();
        }
    }
    
    public void mouseDragged(MouseEvent e)
    {
        if (m_draggedFrom != Chess.NO_SQUARE) {
            m_draggedX = e.getX();
            m_draggedY = e.getY();
            repaint();
        }
    }
    
    public void mouseMoved(MouseEvent e) {}
    
    
    //======================================================================
    
    private String getStringForStone(int stone)
    {
        return m_pieceChars.substring(stone - Chess.MIN_STONE, stone - Chess.MIN_STONE + 1);
    }
    
    public void paint(Graphics graphics)
    {
        super.paint(graphics);
        int size = getFont().getSize();
        for (int y = 0; y < Chess.NUM_OF_ROWS; y++) {
            for (int x = 0; x < Chess.NUM_OF_COLS; x++) {
//                System.out.println(y + " " + x);
                int sqi = (m_bottom == Chess.WHITE
                         ? Chess.coorToSqi(x, Chess.NUM_OF_ROWS - y - 1)
                         : Chess.coorToSqi(Chess.NUM_OF_COLS - x - 1, y));
                if (Chess.isWhiteSquare(sqi)) {
                    graphics.setColor(m_whiteSquareColor);
                    graphics.fillRect(x * size, y * size, size, size);
                } else {
                    graphics.setColor(m_blackSquareColor);
                    graphics.fillRect(x * size, y * size, size, size);
                }
                if (m_enPassantColor != null && sqi == m_position.getSqiEP()) {
                    graphics.setColor(m_enPassantColor);
                    graphics.drawRect(x * size, y * size, size-1, size-1);
                }
                int stone = (sqi == m_draggedFrom ? Chess.NO_STONE : m_position.getStone(sqi));
                graphics.setColor(Chess.stoneToColor(stone) == Chess.WHITE ? m_whiteColor : m_blackColor);
                graphics.drawString(getStringForStone(stone), x * size, (y + 1) * size);
            }
        }
        
        if (m_draggedStone != Chess.NO_STONE) {
            graphics.setColor(Chess.stoneToColor(m_draggedStone) == Chess.WHITE ? m_whiteColor : m_blackColor);
            graphics.drawString(getStringForStone(m_draggedStone), m_draggedX - size/2, m_draggedY + size/2);
        }
    }
    
}