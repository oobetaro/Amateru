/*
 * SplitTest.java
 *
 * Created on 2007/10/26, 0:27
 */

package multisplit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import to.tetramorph.util.IconLoader;

/**
 * NetBeansのような多重分割パネルを考える。
 * このプログラムでは、マウスカーソルの位置に、どのコンポーネントがあるかを識別
 * して、そのコンポーネントを周囲に赤い線を表示することを目指す。
 * @author  大澤義鷹
 */
public class MultiTabbedTest extends javax.swing.JFrame {
  int count;
  MultiTabbedPane multiPane = new MultiTabbedPane(this);
  Icon maleIcon = IconLoader.getImageIcon("/resources/List.male.png");
  Icon femaleIcon = IconLoader.getImageIcon("/resources/List.female.png");

  /** Creates new form SplitTest */
  
  public MultiTabbedTest() {
    initComponents();
    add(multiPane,BorderLayout.CENTER);
    pack();
    this.setLocationRelativeTo(null);
   
  }
  
  
  
  private void add() {
    count++;
    multiPane.insert(maleIcon, "No." + count, createPane());
  }
  //　テスト用のタベッドペインを作成
  private Component createPane() {
    TreeOfLifePanel tree = new TreeOfLifePanel();
    tree.setNumber(count);
    tree.setBackground(Color.LIGHT_GRAY);
    return tree;
  }
  private void reset() {
    count = 0;
    multiPane.removeAll();
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    panel = new javax.swing.JPanel();
    pane2l = new javax.swing.JPanel();
    testButton = new javax.swing.JButton();
    resetButton = new javax.swing.JButton();
    jButton1 = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("\u30c9\u30e9\u30c3\u30b0\u64cd\u4f5c\u3067\u30c0\u30d6\u30da\u30a4\u30f3\u3092\u64cd\u4f5c");
    setMinimumSize(new java.awt.Dimension(600, 600));
    panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    getContentPane().add(panel, java.awt.BorderLayout.NORTH);

    pane2l.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    testButton.setText("TabAdd");
    testButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        testButtonActionPerformed(evt);
      }
    });

    pane2l.add(testButton);

    resetButton.setText("Reset");
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    pane2l.add(resetButton);

    jButton1.setText("test");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    pane2l.add(jButton1);

    getContentPane().add(pane2l, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    List<InnerTabbedPane> list = multiPane.tabpanList;
    for(int i=0; i<list.size(); i++) {
      System.out.println(list.get(i));
    }
  }//GEN-LAST:event_jButton1ActionPerformed

  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    reset();
  }//GEN-LAST:event_resetButtonActionPerformed

  private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
    add();
  }//GEN-LAST:event_testButtonActionPerformed
      
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new MultiTabbedTest().setVisible(true);
      }
    });
  }
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JPanel pane2l;
  private javax.swing.JPanel panel;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton testButton;
  // End of variables declaration//GEN-END:variables
  
}
