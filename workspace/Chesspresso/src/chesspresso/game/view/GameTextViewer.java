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
 * $Id: GameTextViewer.java,v 1.2 2003/01/04 16:15:50 BerniMan Exp $
 */

package chesspresso.game.view;

import chesspresso.game.*;
import chesspresso.move.*;
import chesspresso.position.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Textual representation of a game on a panel.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.2 $
 */
public class GameTextViewer extends JEditorPane
    implements GameListener, PositionChangeListener, GameModelChangeListener
{
    
    // attributes for main line
    private static SimpleAttributeSet MAIN = new SimpleAttributeSet();
    
//    // attributes for NAGSs
//    private static SimpleAttributeSet NAG_SET = new SimpleAttributeSet();
    
    // attributes for comments
    private static SimpleAttributeSet COMMENT = new SimpleAttributeSet();
    
    // attributes for lines
    private static SimpleAttributeSet LINE = new SimpleAttributeSet();

    static {
        // TODO take some parameters from actual editor pane instance
        // (e.g. font, font size)
        
        StyleConstants.setForeground(MAIN, Color.black);
        StyleConstants.setBold(MAIN, true);
        StyleConstants.setFontFamily(MAIN, "Serif");
        StyleConstants.setFontSize(MAIN, 12);

//        StyleConstants.setForeground(NAG_SET, Color.black);
//        StyleConstants.setFontFamily(NAG_SET, "Serif");
//        StyleConstants.setFontSize(NAG_SET, 12);

        StyleConstants.setForeground(COMMENT, Color.gray);
        StyleConstants.setFontFamily(COMMENT, "Serif");
        StyleConstants.setFontSize(COMMENT, 12);    
        StyleConstants.setItalic(COMMENT, true);
        
        StyleConstants.setForeground(LINE, Color.black);
        StyleConstants.setFontFamily(LINE, "Serif");
        StyleConstants.setFontSize(LINE, 12);
    }

    //======================================================================
    
    private Game m_game;
    private int[] m_moveBegin, m_moveEnd;
    private int[] m_moveNode;
    private String m_nagChars;
    
    //======================================================================
    
    /**
     * Create a text viewer for the given game
     *
     *@param game the game to represent
     */
    public GameTextViewer(Game game)
    {
        EditorKit editorKit = new StyledEditorKit();
        setEditorKit(editorKit);
        setEditable(false);
        
        setSelectionColor(Color.darkGray);
        setSelectedTextColor(Color.white);
        
        m_game = game;
        createText();
        setCaretPosition(getDocument().getStartPosition().getOffset());
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {getCaret().setMagicCaretPosition(e.getPoint()); gotoPlyForCaret();}
        });
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) return; // =====>
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) return; // =====>
                if (e.getKeyCode() == KeyEvent.VK_ALT) return; // =====>
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if ((e.getModifiers() & KeyEvent.CTRL_MASK)  != 0)
                        goBackToLineBegin();
                    else
                        goBackward();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if      ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)  gotoEndOfLine();
                    else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) goForwardMainLine();
                    else                                                    goForward();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) gotoPlyForCaret();
                else if (e.getKeyCode() == KeyEvent.VK_UP) gotoPlyForCaret();
                else if (e.getKeyCode() == KeyEvent.VK_HOME) goStart();
                else if (e.getKeyCode() == KeyEvent.VK_END) goEnd();
                else gotoPlyForCaret();
                if (getSelectionStart() == getSelectionEnd()) // assure that we always have a selection
                    showCurrentGameNode();
                //				System.out.println("HashCode: " + m_game.getCurPos().getHashCode());
            }
        });
        
        m_game.getPosition().addPositionChangeListener(this);
        m_game.addChangeListener(this);
        
        requestFocus();
    }
    
    //======================================================================
    
    public void setNagChars(String chars)
    {
        m_nagChars = chars;
    }
    
    private String getNagChar(int nag)
    {
        if (m_nagChars == null || nag > m_nagChars.length()) return null;
        return m_nagChars.substring(nag, nag + 1);
    }
    
    //======================================================================
    // Methods to implement GameModelChangeListener
    
    public void headerModelChanged(Game game)
    {
        // ignore since no header information is displayed
    }
    
    public void moveModelChanged(Game game)
    {
        setDocument(new DefaultStyledDocument());
        createText();
    }
    
    //======================================================================
    
    private void showCurrentGameNode()
    {
        int node = m_game.getCurNode();
        if (node <= 0) {
            setCaretPosition(getDocument().getStartPosition().getOffset());
        } else {
            for (int i=0; i < m_moveNode.length; i++) {
                if (m_moveNode[i] >= node) {
                    setCaretPosition(m_moveBegin[i]);
                    moveCaretPosition(m_moveEnd[i]);
                    break;
                }
            }
        }
    }
    
    // Methods to implement PositionChangeListener 
    
    public void notifyPositionChanged(ImmutablePosition position)
    {
        // not allowed
    }
    
    public void notifyMoveDone(ImmutablePosition position, short move)
    {
        requestFocus();
        showCurrentGameNode();
    }
    
    public void notifyMoveUndone(ImmutablePosition position)
    {
        requestFocus();
        showCurrentGameNode();
    }
    
    //======================================================================
    
    /**
     * Append the text to the document with given attributes.
     *
     *@param text the text to append
     *@param set the text attributes
     */
    private void appendText(String text, AttributeSet set)
    {
        try {
            getDocument().insertString(getDocument().getLength(), text, set);
        } catch (Exception e) {System.out.println(e.getMessage());}
    }
    
//    /**
//     * Append the NAGs to the document.
//     *
//     *@param nags the nags to append
//     */
//    private void appendNags(short[] nags)
//    {
//        for (int i=0; i < nags.length; i++) {
//            String nagChar = getNagChar(nags[i]);
//            if (nagChar == null) {
//                appendText(NAG.getShortString((short)nags[i]) + " ", NAG_SET);
//            } else {
//                appendText(nagChar, NAG_SET);
//            }
//        }
//    }
    
    //======================================================================
    // Methods to implement GameListener
    
    // state to indicate whether a line number will be needed for the next move
    private boolean m_needsLineNumber;

    // current move index
    private int m_notifyIndex;
    
    public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level)
    {
        AttributeSet attrs = (level == 0 ? MAIN : LINE);
        
        /*---------- move number ----------*/
        if (m_needsLineNumber) {
            if (move.isWhiteMove()) {
                appendText(((plyNumber + 2) / 2) + ". ", attrs);
            } else {
                appendText(((plyNumber + 2) / 2) + "... ", attrs);
            }
        }        
        
        /*---------- move text ----------*/
        m_moveNode[m_notifyIndex] = m_game.getCurNode();
        m_moveBegin[m_notifyIndex] = getDocument().getEndPosition().getOffset() - 1;
        appendText(move.toString(), attrs);        
        
        /*---------- nags ----------*/
        if (nags != null) {
            for (int i=0; i < nags.length; i++) {
                String nagChar = getNagChar(nags[i]);
                if (nagChar == null) {
                    appendText(" " + NAG.getShortString((short)nags[i], false), attrs);
                } else {
                    appendText(nagChar, attrs);
                }
            }
        }
        appendText(" ", attrs);
        m_moveEnd[m_notifyIndex] = getDocument().getEndPosition().getOffset() - 2;
        
        /*---------- comment ----------*/
        if (comment != null) appendText(comment + " ", COMMENT);
        
        m_notifyIndex++;
        
        m_needsLineNumber = !move.isWhiteMove() || (comment != null);
    }
    
    public void notifyLineStart(int level)
    {
        appendText(" (", LINE);
        m_needsLineNumber = true;
    }
    
    public void notifyLineEnd(int level)
    {
        appendText(") ", LINE);
        m_needsLineNumber = true;
    }
    
    /**
     * (Re)Create the game text based on the current game.
     */
    private synchronized void createText()
    {
        int totalPlies = m_game.getTotalNumOfPlies();
        m_moveBegin = new int[totalPlies];
        m_moveEnd = new int[totalPlies];
        m_moveNode = new int[totalPlies];
        m_notifyIndex = 0;
        
        m_needsLineNumber = true;
        
        // leading comments
        String comment = m_game.getComment();
        if (comment != null) appendText(comment + " ", COMMENT);
        
        m_game.traverse(this, true);
        appendText(m_game.getResultStr(), MAIN);
    }
    
    //======================================================================
    // Methods to walk through the game
    
    private void goBackward()
    {
        m_game.goBack();
    }
    
    private void goBackToLineBegin()
    {
        m_game.goBackToLineBegin();
    }
    
    private void gotoEndOfLine()
    {
        m_game.gotoEndOfLine();
    }
    
    private void goForwardMainLine()
    {
        m_game.goForward(0);
    }
    
    private void goForward()
    {
        int num = m_game.getNumOfNextMoves();
        if (num > 1) {
            // display popup if more than one move possible
            JPopupMenu popup = new JPopupMenu();
            final Move[] moves = m_game.getNextMoves();
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = Integer.parseInt(event.getActionCommand());
                    m_game.goForward(index);
                }
            };
            
            for (int i = 0; i < moves.length; i++) {
                JMenuItem item = popup.add(moves[i].toString());
                item.addActionListener(actionListener);
                item.setActionCommand(Integer.toString(i));
            }
            Point caretLocation = getCaret().getMagicCaretPosition();
            popup.show(this, (int)caretLocation.getX(), (int)caretLocation.getY());
            popup.requestFocus();  // to prevent the keyboards events from being sent
                                   // to the keylistener of the textviewer
            // TODO enable standard keyboard actions
        } else if (num == 1) {
            m_game.goForward();
        }
            
    }
    
    private void goStart()
    {
        m_game.gotoStart();
    }
    
    private void goEnd()
    {
        m_game.gotoEndOfLine();
    }
    
    private int getNodeForCaret()
    {
        int caret = getCaretPosition();
        if (caret == 0) return m_game.getRootNode();
        for (int i = 0; i < m_moveNode.length - 1; i++) {  // TODO: bin search?
            if (m_moveBegin[i + 1] > caret) return m_moveNode[i];
        }
        return m_moveNode[m_moveNode.length - 1];
    }
    
    private void gotoPlyForCaret()
    {
        m_game.gotoNode(getNodeForCaret());
    }
    
}