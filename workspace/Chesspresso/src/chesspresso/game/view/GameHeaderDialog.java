/*
 * GameHeaderDialog.java
 *
 * Created on 23. Februar 2003, 11:02
 */

package chesspresso.game.view;

import chesspresso.game.*;
import chesspresso.pgn.PGN;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author  BerniMan
 */
public class GameHeaderDialog extends JDialog
{
    
    private class TagRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            String tagName = (String)value;
            String text = tagName + " = \"" + m_headerModel.getTag(tagName) + "\"";
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
 
    }
    
    //======================================================================
    
    private GameHeaderModel m_headerModelBackup;
    private GameHeaderModel m_headerModel;
    private int m_lastSelectedIndex = -1;

    //======================================================================
    
    public GameHeaderDialog(GameHeaderModel headerModel, java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        m_headerModel = headerModel;
        m_headerModelBackup = new GameHeaderModel(headerModel);
        
        initComponents();        
        m_tagList.setCellRenderer(new TagRenderer());
        initValues();
        pack();
    }
    
    //======================================================================
    
    public GameHeaderModel getGameHeaderModel() {return m_headerModel;}
    
    //======================================================================
    
    private void initValues()
    {
        DefaultListModel model = new DefaultListModel();
        
        // seven tag roaster
        for (int i=0; i<PGN.SEVEN_TAG_ROASTER.length; i++) {
            model.addElement(PGN.SEVEN_TAG_ROASTER[i]);
        }
        
        String[] tags = m_headerModel.getTags();
        for (int i=0; i<tags.length; i++) {
            if (!PGN.isInSevenTagRoaster(tags[i])) {
                model.addElement(tags[i]);
            }
        }
        m_tagList.setModel(model);
    }
    
    private void setButtons()
    {
        String tagName = m_teTagName.getText();
        String tagValue = m_headerModel.getTag(tagName);
        
        m_buttAdd.setMnemonic(KeyEvent.VK_ENTER);
        
        if (tagValue != null) {
            m_buttAdd.setText("Change");
            m_buttAdd.setEnabled(m_teTagValue.getText().equals(tagValue));
            m_buttDelete.setEnabled(true);
        } else {
            m_buttAdd.setText("Add");
            m_buttAdd.setEnabled(tagName.length() > 0);
            m_buttDelete.setEnabled(false);
        }
    }
    
    private void close()
    {
        setVisible(false);
        dispose();
    }
    
    private void doChangeValue()
    {
        String tagName = m_teTagName.getText();
        String tagValue = m_teTagValue.getText();
        m_headerModel.setTag(tagName, tagValue);        
        initValues();

        // preselect "next" entry
        for (int i=0; i<m_tagList.getModel().getSize(); i++) {
            if (tagName.equals(m_tagList.getModel().getElementAt(i))) {
                if (i < m_tagList.getModel().getSize() - 1) {
                    m_tagList.setSelectedIndex(i + 1);
                } else {
                    m_tagList.setSelectedIndex(0);
                }
                doSelectTag();
                break;
            }
        }
    }
    
    private void doSelectTag()
    {
        Object selection = m_tagList.getSelectedValue();
        if (selection != null) {
            String tagName = (String)selection;
            m_teTagName.setText(tagName);
            m_teTagValue.setText(m_headerModel.getTag(tagName));
            m_teTagValue.requestFocus();
            setButtons();
        }
    }
    
    //======================================================================
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel2 = new javax.swing.JPanel();
        buttOk = new javax.swing.JButton();
        buttCancel = new javax.swing.JButton();
        buttOldValues = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_teTagName = new javax.swing.JTextField();
        m_teTagValue = new javax.swing.JTextField();
        m_buttAdd = new javax.swing.JButton();
        m_buttDelete = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_tagList = new javax.swing.JList();

        setTitle("Game Header Dialog");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttOk.setText("Ok");
        buttOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttOkActionPerformed(evt);
            }
        });

        jPanel2.add(buttOk);

        buttCancel.setText("Cancel");
        buttCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttCancelActionPerformed(evt);
            }
        });

        jPanel2.add(buttCancel);

        buttOldValues.setText("Old Values");
        jPanel2.add(buttOldValues);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));

        jPanel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 4, 4, 4)));
        m_teTagName.setColumns(10);
        m_teTagName.setToolTipText("Tag name");
        m_teTagName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                m_teTagNameKeyReleased(evt);
            }
        });

        jPanel3.add(m_teTagName);

        m_teTagValue.setColumns(20);
        m_teTagValue.setToolTipText("Tag value");
        jPanel3.add(m_teTagValue);

        m_buttAdd.setText("Add");
        m_buttAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttAddActionPerformed(evt);
            }
        });

        jPanel3.add(m_buttAdd);

        m_buttDelete.setText("Delete");
        m_buttDelete.setToolTipText("Change");
        m_buttDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_buttDeleteActionPerformed(evt);
            }
        });

        jPanel3.add(m_buttDelete);

        jPanel4.add(jPanel3, java.awt.BorderLayout.SOUTH);

        m_tagList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        m_tagList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                m_tagListMousePressed(evt);
            }
        });

        jScrollPane1.setViewportView(m_tagList);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void buttOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttOkActionPerformed
        close();
    }//GEN-LAST:event_buttOkActionPerformed

    private void m_teTagNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_teTagNameKeyReleased
        setButtons();
    }//GEN-LAST:event_m_teTagNameKeyReleased

    private void m_buttDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttDeleteActionPerformed
        String tagName = m_teTagName.getText();
        m_headerModel.removeTag(tagName);
        initValues();
    }//GEN-LAST:event_m_buttDeleteActionPerformed

    private void m_buttAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_buttAddActionPerformed
        doChangeValue();
    }//GEN-LAST:event_m_buttAddActionPerformed

    private void m_tagListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_tagListMousePressed
        doSelectTag();
    }//GEN-LAST:event_m_tagListMousePressed

    private void buttCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttCancelActionPerformed
        close();
    }//GEN-LAST:event_buttCancelActionPerformed
    
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton m_buttAdd;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton buttOldValues;
    private javax.swing.JButton buttOk;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton buttCancel;
    private javax.swing.JTextField m_teTagValue;
    private javax.swing.JList m_tagList;
    private javax.swing.JButton m_buttDelete;
    private javax.swing.JTextField m_teTagName;
    // End of variables declaration//GEN-END:variables
    
}
