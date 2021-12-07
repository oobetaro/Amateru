/*
 * SearchOptionPanel.java
 *
 * Created on 2006/07/23, 6:55
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.GregorianDateFormatter;
import java.sql.Date;
import java.util.GregorianCalendar;

/**
 * 性別や日付範囲など検索でのオプションを指定するためのパネル。
 * SearchFrameから直接各フォームにアクセスされる。
 */
class SearchOptionPanel extends javax.swing.JPanel {
  
  /** Creates new form SearchOptionPanel */
  public SearchOptionPanel() {
    initComponents();
    dateFTextField1.setFormatterFactory(new FormatterFactory(new GregorianDateFormatter()));
    dateFTextField2.setFormatterFactory(new FormatterFactory(new GregorianDateFormatter()));
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JPanel jPanel1;

    buttonGroup1 = new javax.swing.ButtonGroup();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    maleCheckBox = new javax.swing.JCheckBox();
    femaleCheckBox = new javax.swing.JCheckBox();
    noneCheckBox = new javax.swing.JCheckBox();
    natalCheckBox = new javax.swing.JCheckBox();
    orgCheckBox = new javax.swing.JCheckBox();
    eventCheckBox = new javax.swing.JCheckBox();
    compositCheckBox = new javax.swing.JCheckBox();
    jLabel3 = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    dateFTextField1 = new javax.swing.JFormattedTextField();
    jLabel4 = new javax.swing.JLabel();
    dateFTextField2 = new javax.swing.JFormattedTextField();
    jLabel5 = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 8, 8));
    jLabel1.setText("\u6027\u5225");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 12);
    add(jLabel1, gridBagConstraints);

    jLabel2.setText("\u30c7\u30fc\u30bf\u30bf\u30a4\u30d7");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 12);
    add(jLabel2, gridBagConstraints);

    maleCheckBox.setSelected(true);
    maleCheckBox.setText("\u7537");
    maleCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    maleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(maleCheckBox, gridBagConstraints);

    femaleCheckBox.setSelected(true);
    femaleCheckBox.setText("\u5973");
    femaleCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    femaleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(femaleCheckBox, gridBagConstraints);

    noneCheckBox.setSelected(true);
    noneCheckBox.setText("\u7121");
    noneCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    noneCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(noneCheckBox, gridBagConstraints);

    natalCheckBox.setSelected(true);
    natalCheckBox.setText("\u4eba\u7269");
    natalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    natalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(natalCheckBox, gridBagConstraints);

    orgCheckBox.setSelected(true);
    orgCheckBox.setText("\u7d44\u7e54");
    orgCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    orgCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(orgCheckBox, gridBagConstraints);

    eventCheckBox.setSelected(true);
    eventCheckBox.setText("\u305d\u306e\u6642");
    eventCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    eventCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(eventCheckBox, gridBagConstraints);

    compositCheckBox.setSelected(true);
    compositCheckBox.setText("\u30b3\u30f3\u30dd\u30b8\u30c3\u30c8");
    compositCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    compositCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(compositCheckBox, gridBagConstraints);

    jLabel3.setText("\u65e5\u4ed8\u7bc4\u56f2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 12);
    add(jLabel3, gridBagConstraints);

    jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));

    dateFTextField1.setColumns(8);
    jPanel1.add(dateFTextField1);

    jLabel4.setText("\u4ee5\u964d\u304b\u3089");
    jPanel1.add(jLabel4);

    dateFTextField2.setColumns(8);
    jPanel1.add(dateFTextField2);

    jLabel5.setText("\u307e\u3067");
    jPanel1.add(jLabel5);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(jPanel1, gridBagConstraints);

  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup1;
  protected javax.swing.JCheckBox compositCheckBox;
  protected javax.swing.JFormattedTextField dateFTextField1;
  protected javax.swing.JFormattedTextField dateFTextField2;
  protected javax.swing.JCheckBox eventCheckBox;
  protected javax.swing.JCheckBox femaleCheckBox;
  protected javax.swing.JCheckBox maleCheckBox;
  protected javax.swing.JCheckBox natalCheckBox;
  protected javax.swing.JCheckBox noneCheckBox;
  protected javax.swing.JCheckBox orgCheckBox;
  // End of variables declaration//GEN-END:variables
  
}
