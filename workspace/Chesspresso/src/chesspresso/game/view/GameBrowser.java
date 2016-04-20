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
 * $Id: GameBrowser.java,v 1.4 2003/01/04 16:24:04 BerniMan Exp $
 */

package chesspresso.game.view;


import chesspresso.Chess;
import chesspresso.game.*;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.*;
import chesspresso.position.view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Game browser.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.4 $
 */
public class GameBrowser extends JPanel implements PositionMotionListener
{

    private Game m_game;
    private PositionView m_positionView;
    private boolean m_editable;
        
    //======================================================================
    
    /**
     * Create a new game browser.
     *
     *@param gameModel the game model to be displayed
     */
    public GameBrowser(GameModel gameModel)
    {
        this(new Game(gameModel), Chess.WHITE, false);
    }
    
    /**
     * Create a new game browser.
     *
     *@param game the game to be displayed
     */
    public GameBrowser(Game game)
    {
        this(game, Chess.WHITE, false);
    }
    
    /**
     * Create a new game browser.
     *
     *@param game the game to be displayed
     *@param bottomPlayer the player on the lower edge
     */
    public GameBrowser(Game game, int bottomPlayer)
    {
        this(game, bottomPlayer, false);
    }
    
    /**
     * Create a new game browser.
     *
     *@param game the game to be displayed
     *@param bottomPlayer the player on the lower edge
     *@param editable whether the game can be edited by the view
     */
    public GameBrowser(Game game, int bottomPlayer, boolean editable)
    {
        super();
        
        m_game = game;
        m_game.gotoStart();
        initComponents();
        
        m_positionView = new PositionView(m_game.getPosition(), bottomPlayer);
        m_positionView.setEnPassantColor(null);
        m_positionView.setPositionMotionListener(this);
//        m_positionView.setFocusable(false);
        m_positionFrame.add(m_positionView, BorderLayout.CENTER);
        
        GameTextViewer textViewer = new GameTextViewer(m_game);
        m_textFrame.add(new JScrollPane(textViewer), BorderLayout.CENTER);
        
        m_lbHeader0.setText(m_game.getHeaderString(0));
        m_lbHeader1.setText(m_game.getHeaderString(1));
        m_lbHeader2.setText(m_game.getHeaderString(2));
        
        m_editable = editable;
        if (!m_editable) {
            m_buttNAG.setVisible(false);
            m_buttNAG2.setVisible(false);
            m_buttComment.setVisible(false);
            m_buttDelete.setVisible(false);
        }
    }

    //======================================================================
    
    public void setProperties(PositionViewProperties props)
    {
        m_positionView.setProperties(props);
        invalidate();
    }
    
    //======================================================================
    // Methods to implement PositionMotionListener
    
    public boolean allowDrag(ImmutablePosition position, int from)
    {
        // allow dragging only if editable and there is a stone on the square
        return m_editable && m_game.getPosition().getStone(from) != Chess.NO_STONE;
    }
    
    public int getPartnerSqi(ImmutablePosition position, int from)
    {
        return Chess.NO_SQUARE;  // =====>
    }
    
    public void dragged(ImmutablePosition position, int from, int to, MouseEvent e)
    {
        try {
            m_game.getPosition().doMove(m_game.getPosition().getMove(from, to, Chess.NO_PIECE));
        } catch (IllegalMoveException ex) {
            ex.printStackTrace();
            // games should only contain legal moves, so there must
            // be a bug somewhere
        }
    }
    
    public void squareClicked(ImmutablePosition position, int sqi, MouseEvent e)
    {
        // nothing
    }
    
    //======================================================================
    
    public int getBottomPlayer() {return m_positionView.getBottomPlayer();}
    public void setBottomPlayer(int player) {m_positionView.setBottomPlayer(player);}
    
    public boolean getEditable() {return m_editable;}
    public void setEditable(boolean editable) {m_editable = editable;}
    
    //=======================================================================
    
    private void doShowNAGMenu(String[] nags, Component invoker)
    {
        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (m_game.getCurNode() > 0) {
                    short nag = NAG.ofString(event.getActionCommand());
                    if (m_game.hasNag(nag)) {
                        m_game.removeNag(nag);
                    } else {
                        m_game.addNag(nag);
                    }
                }
            }
        };
        
        for (int i=0; i<nags.length; i++) {
            boolean isChecked = m_game.hasNag(NAG.ofString(nags[i]));
            JMenuItem item = new JCheckBoxMenuItem(nags[i], isChecked);
            popup.add(item);
            item.addActionListener(actionListener);
            item.setActionCommand(nags[i]);
        }

        popup.show(invoker, 0, 0);
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        m_lbHeader0 = new javax.swing.JLabel();
        m_lbHeader1 = new javax.swing.JLabel();
        m_lbHeader2 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        m_positionFrame = new javax.swing.JPanel();
        m_textFrame = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        m_buttFlip = new javax.swing.JButton();
        m_buttStart = new javax.swing.JButton();
        m_buttBackToMainLine = new javax.swing.JButton();
        m_buttBackToLineBegin = new javax.swing.JButton();
        m_buttBackward = new javax.swing.JButton();
        m_buttForward = new javax.swing.JButton();
        m_buttForwardMainLine = new javax.swing.JButton();
        m_buttEnd = new javax.swing.JButton();
        m_buttEndOfLine = new javax.swing.JButton();
        m_buttNAG = new javax.swing.JButton();
        m_buttNAG2 = new javax.swing.JButton();
        m_buttComment = new javax.swing.JButton();
        m_buttDelete = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        m_lbHeader0.setText("0");
        jPanel1.add(m_lbHeader0);

        m_lbHeader1.setText("1");
        jPanel1.add(m_lbHeader1);

        m_lbHeader2.setText("2");
        jPanel1.add(m_lbHeader2);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        m_positionFrame.setLayout(new javax.swing.BoxLayout(m_positionFrame, javax.swing.BoxLayout.X_AXIS));

        jSplitPane1.setLeftComponent(m_positionFrame);

        m_textFrame.setLayout(new java.awt.BorderLayout());

        m_textFrame.setMinimumSize(new java.awt.Dimension(256, 128));
        m_textFrame.setPreferredSize(new java.awt.Dimension(256, 256));
        jSplitPane1.setRightComponent(m_textFrame);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        m_buttFlip.setText("^");
        m_buttFlip.setToolTipText("Flip");
        m_buttFlip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttFlipActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttFlip);

        m_buttStart.setText("<<");
        m_buttStart.setToolTipText("Start");
        m_buttStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttStartActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttStart);

        m_buttBackToMainLine.setText("<|");
        m_buttBackToMainLine.setToolTipText("Main Line");
        m_buttBackToMainLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttBackToMainLineActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttBackToMainLine);

        m_buttBackToLineBegin.setText("|<");
        m_buttBackToLineBegin.setToolTipText("line Begin");
        m_buttBackToLineBegin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttBackToLineBeginActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttBackToLineBegin);

        m_buttBackward.setText("<");
        m_buttBackward.setToolTipText("Backward");
        m_buttBackward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttBackwardActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttBackward);

        m_buttForward.setText(">");
        m_buttForward.setToolTipText("Foward");
        m_buttForward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttForwardActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttForward);

        m_buttForwardMainLine.setText(">!");
        m_buttForwardMainLine.setToolTipText("Forward in main line");
        m_buttForwardMainLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttForwardMainLineActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttForwardMainLine);

        m_buttEnd.setText(">|");
        m_buttEnd.setToolTipText("End");
        m_buttEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttEndActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttEnd);

        m_buttEndOfLine.setText(">>");
        m_buttEndOfLine.setToolTipText("end of line");
        m_buttEndOfLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttEndOfLineActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttEndOfLine);

        m_buttNAG.setText("NAG");
        m_buttNAG.setToolTipText("NAG");
        m_buttNAG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttNAGActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttNAG);

        m_buttNAG2.setText("NAG2");
        m_buttNAG2.setToolTipText("Additional NAGs");
        m_buttNAG2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttNAG2ActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttNAG2);

        m_buttComment.setText("comment");
        m_buttComment.setToolTipText("Comment");
        m_buttComment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttCommentActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttComment);

        m_buttDelete.setText("del");
        m_buttDelete.setToolTipText("Delete line");
        m_buttDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttDeleteActionPerformed(evt);
            }
        });

        jToolBar1.add(m_buttDelete);

        add(jToolBar1, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void m_buttNAG2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttNAG2ActionPerformed
        doShowNAGMenu(NAG.getUnDefinedShortNags(), m_buttNAG2);
    }//GEN-LAST:event_m_buttNAG2ActionPerformed

    private void m_buttEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttEndActionPerformed
        m_game.gotoEndOfLine();
    }//GEN-LAST:event_m_buttEndActionPerformed

    private void m_buttDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttDeleteActionPerformed
        m_game.deleteCurrentLine();
    }//GEN-LAST:event_m_buttDeleteActionPerformed

    private void m_buttCommentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttCommentActionPerformed
        Object comment = JOptionPane.showInputDialog(this, "Comment", "Add comment", JOptionPane.OK_CANCEL_OPTION, null, null, m_game.getComment());
        if (comment != null) {
            m_game.setComment(comment.toString());
        }
    }//GEN-LAST:event_m_buttCommentActionPerformed

    private void m_buttNAGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttNAGActionPerformed
        doShowNAGMenu(NAG.getDefinedShortNags(), m_buttNAG);
    }//GEN-LAST:event_m_buttNAGActionPerformed

    private void m_buttEndOfLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttEndOfLineActionPerformed
        m_game.gotoEndOfLine();
    }//GEN-LAST:event_m_buttEndOfLineActionPerformed

    private void m_buttForwardMainLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttForwardMainLineActionPerformed
        m_game.goForward(0);
    }//GEN-LAST:event_m_buttForwardMainLineActionPerformed

    private void m_buttForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttForwardActionPerformed
        m_game.goForward();
    }//GEN-LAST:event_m_buttForwardActionPerformed

    private void m_buttBackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttBackwardActionPerformed
        m_game.goBack();
    }//GEN-LAST:event_m_buttBackwardActionPerformed

    private void m_buttBackToLineBeginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttBackToLineBeginActionPerformed
        m_game.goBackToLineBegin();
    }//GEN-LAST:event_m_buttBackToLineBeginActionPerformed

    private void m_buttBackToMainLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttBackToMainLineActionPerformed
        m_game.goBackToMainLine();
    }//GEN-LAST:event_m_buttBackToMainLineActionPerformed

    private void m_buttStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttStartActionPerformed
        m_game.gotoStart();
    }//GEN-LAST:event_m_buttStartActionPerformed

    private void m_buttFlipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttFlipActionPerformed
        m_positionView.flip();
    }//GEN-LAST:event_m_buttFlipActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton m_buttBackToLineBegin;
    private javax.swing.JButton m_buttEndOfLine;
    private javax.swing.JButton m_buttBackward;
    private javax.swing.JPanel m_textFrame;
    private javax.swing.JButton m_buttFlip;
    private javax.swing.JButton m_buttStart;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel m_lbHeader2;
    private javax.swing.JButton m_buttEnd;
    private javax.swing.JLabel m_lbHeader1;
    private javax.swing.JButton m_buttBackToMainLine;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel m_positionFrame;
    private javax.swing.JButton m_buttNAG;
    private javax.swing.JLabel m_lbHeader0;
    private javax.swing.JButton m_buttForwardMainLine;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton m_buttDelete;
    private javax.swing.JButton m_buttNAG2;
    private javax.swing.JButton m_buttComment;
    private javax.swing.JButton m_buttForward;
    // End of variables declaration//GEN-END:variables
    
}
