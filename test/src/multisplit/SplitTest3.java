/*
 * SplitTest.java
 *
 * Created on 2007/10/26, 0:27
 */

package multisplit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * NetBeansのような多重分割パネルを考える。
 * このプログラムでは、マウスカーソルの位置に、どのコンポーネントがあるかを識別
 * して、そのコンポーネントを周囲に赤い線を表示することを目指す。
 * @author  大澤義鷹
 */
public class SplitTest3 extends javax.swing.JFrame {
  int count;
  SplitNode rootNode = new SplitNode();
  JButton [] buttons;
  GlassPane gpan = new GlassPane(this);
  MouseHandler mh = new MouseHandler();
  /** Creates new form SplitTest */
  
  public SplitTest3() {
    initComponents();
    add( rootNode, BorderLayout.CENTER ); // SplitPaneをレイアウトにセット
    setGlassPane(gpan);
    pack();
    this.setLocationRelativeTo(null);
   
    buttons = new JButton[] { topButton,leftButton,bottomButton,rightButton };
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for(int i=0; i<buttons.length; i++) {
          if(buttons[i] == evt.getSource()) {
            Component tree = createTabbedPane();
            if(count == 1) {
              comboBox.removeAllItems();
              rootNode.addComponent(tree,i,null);
              System.out.println(tree + "を" + evt.getActionCommand() + " 挿入");
              comboBox.setEnabled(true);
              removeButton.setEnabled(true);
            } else {
              InnerTabbedPane target = (InnerTabbedPane)comboBox.getSelectedItem();
              SplitNode n = rootNode.findNode( target );
              if(n == null)
                JOptionPane.showMessageDialog(
                  SplitTest3.this,  target.toString() + "が見つからない" );
              else {
                System.out.println( target + "に" + tree + "を" +
                  evt.getActionCommand() + "挿入" );
                n.addComponent(tree,i,target);
              }
            }
            comboBox.addItem(tree);
            validate();
            break;
          }
        }
      }
    };
    for(JButton b: buttons) b.addActionListener( al );
    reset();
  }
  
  class GlassPane extends JComponent {
    Frame frame;
    GlassPane(Frame frame) {
      this.frame = frame;
    }
    Rectangle bounds;
    @Override
    protected void paintComponent(Graphics g) {
      Dimension size = frame.getSize();
      if(bounds != null) {
        g.setColor(Color.RED);
        Rectangle r = bounds;
        g.drawRect( r.x, r.y, r.width-2, r.height-2 );
        g.drawRect( r.x+1, r.y+1, r.width-4, r.height-4 );
      }
    }
    void drawBounds(Rectangle bounds) {
      this.bounds = bounds;
      repaint();
    }
    void eraseBounds() {
      bounds = null;
      repaint();
    }
  }
  
  class MouseHandler extends MouseAdapter implements MouseMotionListener {

    public void mouseDragged(MouseEvent evt) {
      System.out.println("Dragged = " + evt.getPoint());
      updateBounds();
      gpan.setVisible(true);
      if ( ! comboBox.isEnabled() ) return;
      MouseEvent ev = SwingUtilities.convertMouseEvent((Component)evt.getSource(),evt, gpan); 
      Point p = ev.getPoint();
      for(int i=0; i<comboBox.getItemCount(); i++) {
        InnerTabbedPane pan = (InnerTabbedPane)comboBox.getItemAt(i);
        if ( pan.glassBounds.contains(p.getX(),p.getY()) ) {
            gpan.drawBounds(pan.glassBounds);
            break;
        } else {
            gpan.eraseBounds();
        }
      }
      
    }
    public void mouseReleased( MouseEvent evt) {
      gpan.eraseBounds();
      gpan.setVisible(false);
    }
      
    public void mouseMoved(MouseEvent evt) {
    }
  }

  private void reset() {
    comboBox.removeAllItems();
    comboBox.addItem("選択");
    rootNode.removeAll();
    count = 0;
    comboBox.setEnabled(false);
    removeButton.setEnabled(false);
    System.out.println("リセット");
    updateBounds();
  }
  
  private void remove() {
    InnerTabbedPane p = (InnerTabbedPane)comboBox.getSelectedItem();
    System.out.println(p + "を削除");
    rootNode.removeComponent(p);
    comboBox.removeItem(p);
    if ( comboBox.getItemCount() == 0 ) reset();
    updateBounds();
  }

  //生命の木パネルに自身がFrame上でどこにいるか座標を書きこむ
  private void updateBounds() {
    if(! comboBox.isEnabled()) return;
    for(int i=0; i<comboBox.getItemCount(); i++) {
      InnerTabbedPane p = (InnerTabbedPane)comboBox.getItemAt(i);
      Point z = SwingUtilities.convertPoint( p, 0, 0, gpan );
      p.glassBounds.setLocation( z );
      p.glassBounds.setSize( p.getSize() );
      System.out.println(p + "  " + z + " " + p.getSize()  );
    }
  }
  
  //　テスト用のタベッドペインを作成
  private Component createTabbedPane() {
    InnerTabbedPane tabpan = new InnerTabbedPane();
    tabpan.addMouseListener(mh);
    tabpan.addMouseMotionListener(mh);
    count++;
    tabpan.setName("No." + count);
    TreeOfLifePanel [] trees = new TreeOfLifePanel [3];
    for(int i=0; i<trees.length; i++) trees[i] = new TreeOfLifePanel();
    
    Icon maleIcon = new ImageIcon(
      SplitTest3.class.getResource("/resources/List.male.png"));
    Icon femaleIcon = new ImageIcon(
      SplitTest3.class.getResource("/resources/List.female.png"));

    for(int i=0; i<trees.length; i++) {
      trees[i].setBorder(null);
      trees[i].setNumber(i+1);
      trees[i].setBackground(Color.LIGHT_GRAY);
      tabpan.addTab("Tree " + (i+1), femaleIcon, trees[i]);
    }
    return tabpan;
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    panel = new javax.swing.JPanel();
    topButton = new javax.swing.JButton();
    leftButton = new javax.swing.JButton();
    bottomButton = new javax.swing.JButton();
    rightButton = new javax.swing.JButton();
    comboBox = new javax.swing.JComboBox();
    removeButton = new javax.swing.JButton();
    resetButton = new javax.swing.JButton();
    pane2l = new javax.swing.JPanel();
    testButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("\u5404SplitPane\u306e\u8f2a\u90ed\u3092\u8a8d\u8b58\u3059\u308b\u5b9f\u9a13");
    setMinimumSize(new java.awt.Dimension(600, 600));
    panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    topButton.setText("Top");
    panel.add(topButton);

    leftButton.setText("Left");
    panel.add(leftButton);

    bottomButton.setText("Buttom");
    panel.add(bottomButton);

    rightButton.setText("Right");
    panel.add(rightButton);

    comboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    panel.add(comboBox);

    removeButton.setText("Remove");
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });

    panel.add(removeButton);

    resetButton.setText("Reset");
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    panel.add(resetButton);

    getContentPane().add(panel, java.awt.BorderLayout.NORTH);

    pane2l.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    testButton.setText("TEST");
    testButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        testButtonActionPerformed(evt);
      }
    });

    pane2l.add(testButton);

    getContentPane().add(pane2l, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
    gpan.setVisible(true); //可視化されてから実行しないと効かない
    if ( ! comboBox.isEnabled()) return;
    for(int i=0; i<comboBox.getItemCount(); i++) {
      TreeOfLifePanel p = (TreeOfLifePanel)comboBox.getItemAt(i);
      System.out.println(p + "  " + p.glassBounds  );
    }
  }//GEN-LAST:event_testButtonActionPerformed
  
  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    remove();
  }//GEN-LAST:event_removeButtonActionPerformed
  
  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    reset();
  }//GEN-LAST:event_resetButtonActionPerformed
  
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new SplitTest3().setVisible(true);
      }
    });
  }
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton bottomButton;
  private javax.swing.JComboBox comboBox;
  private javax.swing.JButton leftButton;
  private javax.swing.JPanel pane2l;
  private javax.swing.JPanel panel;
  private javax.swing.JButton removeButton;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton rightButton;
  private javax.swing.JButton testButton;
  private javax.swing.JButton topButton;
  // End of variables declaration//GEN-END:variables
  
}
