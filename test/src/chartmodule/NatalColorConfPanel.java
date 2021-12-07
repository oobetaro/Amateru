/*
 * NatalColorConfPanel.java
 *
 * Created on 2007/02/21, 13:20
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import javax.swing.JColorChooser;
import to.tetramorph.starbase.*;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 *
 * @author  大澤義鷹
 */
public class NatalColorConfPanel extends CustomizePanel {
  Color bgColor = Color.WHITE;
  /** Creates new form NatalColorConfPanel */
  public NatalColorConfPanel() {
    initComponents();
  }
  //パネル内の設定情報を引数のprefに書きこんで返す。
  public Preference getPreference(Preference pref) {
    signColorPanel2.getPreference(pref);
    pref.setColor("background",bgColor);
    return pref;
  }
  //prefの値をパネルに反映させる。
  public void setPreference(Preference pref) {
    signColorPanel2.setPreference(pref);
    bgColor = pref.getColor("background",Color.WHITE);
  }

  public boolean isCorrect(String[] errmsg) {
    return true;
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    tabbedPane = new javax.swing.JTabbedPane();
    signColorPanel2 = new to.tetramorph.starbase.widget.SignColorPanel2();
    planetsColorPanel1 = new to.tetramorph.starbase.widget.PlanetsColorPanel();
    houseColorPanel1 = new to.tetramorph.starbase.widget.HouseColorPanel();
    aspectsPanel1 = new to.tetramorph.starbase.widget.AspectsPanel();
    otherPanel = new javax.swing.JPanel();
    jButton1 = new javax.swing.JButton();
    backgroundLabel = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();

    setLayout(new java.awt.GridLayout());

    tabbedPane.addTab("\u9ec4\u9053\u5341\u4e8c\u5bae", signColorPanel2);

    tabbedPane.addTab("\u5929\u4f53", planetsColorPanel1);

    tabbedPane.addTab("\u30cf\u30a6\u30b9", houseColorPanel1);

    tabbedPane.addTab("\u30a2\u30b9\u30da\u30af\u30c8", aspectsPanel1);

    otherPanel.setLayout(new java.awt.GridBagLayout());

    jButton1.setText("\u80cc\u666f\u8272");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    otherPanel.add(jButton1, new java.awt.GridBagConstraints());

    backgroundLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    backgroundLabel.setPreferredSize(new java.awt.Dimension(60, 20));
    otherPanel.add(backgroundLabel, new java.awt.GridBagConstraints());

    jLabel2.setText("\u540d\u524d\u3084\u5730\u540d\u306e\u6587\u5b57\u8272\u306a\u3069");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    otherPanel.add(jLabel2, gridBagConstraints);

    tabbedPane.addTab("\u305d\u306e\u4ed6", otherPanel);

    add(tabbedPane);

  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    bgColor = JColorChooser.showDialog(this,"背景色の選択",Color.WHITE);
    if(bgColor == null) bgColor = Color.WHITE;
    planetsColorPanel1.setBG(bgColor);
    aspectsPanel1.setBGColor(bgColor);
    houseColorPanel1.setBG(bgColor);
    signColorPanel2.setBG(bgColor);
  }//GEN-LAST:event_jButton1ActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private to.tetramorph.starbase.widget.AspectsPanel aspectsPanel1;
  private javax.swing.JLabel backgroundLabel;
  private to.tetramorph.starbase.widget.HouseColorPanel houseColorPanel1;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel otherPanel;
  private to.tetramorph.starbase.widget.PlanetsColorPanel planetsColorPanel1;
  private to.tetramorph.starbase.widget.SignColorPanel2 signColorPanel2;
  private javax.swing.JTabbedPane tabbedPane;
  // End of variables declaration//GEN-END:variables
  
}
