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
 * $Id: PositionViewProperties.java,v 1.1 2003/01/04 16:26:05 BerniMan Exp $
 */

package chesspresso.position.view;

import chesspresso.position.*;
import javax.swing.*;
import java.awt.*;


/**
 *
 * @author  BerniMan
 */
public class PositionViewProperties extends javax.swing.JDialog
{
    
    private static class FontListRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (value instanceof Font) {
                return super.getListCellRendererComponent(list, ((Font)value).getName(), index, isSelected, cellHasFocus);
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }
    
    //======================================================================
    
    private PositionView m_positionView;
    
    public PositionViewProperties(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        
        m_positionView = new PositionView(Position.createInitialPosition());
        m_positionFrame.add(m_positionView, BorderLayout.CENTER);
        
        Font font = m_positionView.getFont();
        teFontSize.setText(Integer.toString(font.getSize()));
        tePieces.setText(m_positionView.getPieceChars());
        
        Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (int i=0; i<allFonts.length; i++) {
            cbFonts.addItem(allFonts[i]);
            cbFonts.setRenderer(new FontListRenderer());
            if (allFonts[i].getName().equals(font.getName())) {
                cbFonts.setSelectedIndex(i);
            }
        }
        
        pack();
    }
    
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    //======================================================================
    
    private void setFont()
    {
        try {
            Font font = (Font)cbFonts.getSelectedItem();
            if (font != null) {
                int fontSize = Integer.parseInt(teFontSize.getText());
                font = font.deriveFont(Font.PLAIN, fontSize);
                m_positionView.setFont(font);
                tePieces.setFont(font);
            }
            m_positionView.setPieceChars(tePieces.getText());
        } catch (NumberFormatException ex) {
            // nothing
        }
    }
    
    public PositionView getPositionView() {return m_positionView;}
    
    //======================================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        butWhiteSquare = new javax.swing.JButton();
        butBlackSquare = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        butWhite = new javax.swing.JButton();
        butBlack = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        cbFonts = new javax.swing.JComboBox();
        teFontSize = new javax.swing.JTextField();
        tePieces = new javax.swing.JTextField();
        m_positionFrame = new javax.swing.JPanel();

        setTitle("Position View Properties");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.X_AXIS));

        jPanel3.setBorder(new javax.swing.border.TitledBorder("Square Color"));
        butWhiteSquare.setText("white");
        butWhiteSquare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butWhiteSquareActionPerformed(evt);
            }
        });

        jPanel3.add(butWhiteSquare);

        butBlackSquare.setText("black");
        butBlackSquare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butBlackSquareActionPerformed(evt);
            }
        });

        jPanel3.add(butBlackSquare);

        jPanel6.add(jPanel3);

        jPanel2.setBorder(new javax.swing.border.TitledBorder("Piece Color"));
        butWhite.setText("white");
        butWhite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butWhiteActionPerformed(evt);
            }
        });

        jPanel2.add(butWhite);

        butBlack.setText("black");
        butBlack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butBlackActionPerformed(evt);
            }
        });

        jPanel2.add(butBlack);

        jPanel6.add(jPanel2);

        jPanel1.add(jPanel6);

        jPanel4.setBorder(new javax.swing.border.TitledBorder("Font"));
        cbFonts.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbFontsItemStateChanged(evt);
            }
        });

        jPanel4.add(cbFonts);

        teFontSize.setText("12");
        teFontSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                teFontSizeKeyTyped(evt);
            }
        });

        jPanel4.add(teFontSize);

        tePieces.setColumns(13);
        tePieces.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tePiecesKeyTyped(evt);
            }
        });

        jPanel4.add(tePieces);

        jPanel1.add(jPanel4);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        m_positionFrame.setLayout(new java.awt.BorderLayout());

        getContentPane().add(m_positionFrame, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void tePiecesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tePiecesKeyTyped
        setFont();
    }//GEN-LAST:event_tePiecesKeyTyped

    private void teFontSizeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_teFontSizeKeyTyped
        setFont();
    }//GEN-LAST:event_teFontSizeKeyTyped

    private void cbFontsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbFontsItemStateChanged
        setFont();
    }//GEN-LAST:event_cbFontsItemStateChanged

    private void butBlackSquareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butBlackSquareActionPerformed
        JColorChooser colorChooser = new JColorChooser(m_positionView.getBlackSquareColor());
        JDialog dialog = JColorChooser.createDialog(this, "Black Square Color", true, colorChooser, null, null);
        dialog.setVisible(true);
        m_positionView.setBlackSquareColor(colorChooser.getColor());
    }//GEN-LAST:event_butBlackSquareActionPerformed

    private void butWhiteSquareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butWhiteSquareActionPerformed
        JColorChooser colorChooser = new JColorChooser(m_positionView.getWhiteSquareColor());
        JDialog dialog = JColorChooser.createDialog(this, "White Square Color", true, colorChooser, null, null);
        dialog.setVisible(true);
        m_positionView.setWhiteSquareColor(colorChooser.getColor());
    }//GEN-LAST:event_butWhiteSquareActionPerformed

    private void butBlackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butBlackActionPerformed
        JColorChooser colorChooser = new JColorChooser(m_positionView.getBlackColor());
        JDialog dialog = JColorChooser.createDialog(this, "Black Color", true, colorChooser, null, null);
        dialog.setVisible(true);
        m_positionView.setBlackColor(colorChooser.getColor());
    }//GEN-LAST:event_butBlackActionPerformed

    private void butWhiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butWhiteActionPerformed
        JColorChooser colorChooser = new JColorChooser(m_positionView.getWhiteColor());
        JDialog dialog = JColorChooser.createDialog(this, "White Color", true, colorChooser, null, null);
        dialog.setVisible(true);
        m_positionView.setWhiteColor(colorChooser.getColor());
    }//GEN-LAST:event_butWhiteActionPerformed
    
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        close();
    }//GEN-LAST:event_closeDialog
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField tePieces;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton butBlackSquare;
    private javax.swing.JButton butBlack;
    private javax.swing.JButton butWhite;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton butWhiteSquare;
    private javax.swing.JPanel m_positionFrame;
    private javax.swing.JComboBox cbFonts;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField teFontSize;
    // End of variables declaration//GEN-END:variables
    
}
