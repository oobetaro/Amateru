/*
 * OverlayFrame.java
 *
 * Created on 2007/11/02, 8:46
 */

package multisplit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * オーバーラップパネルを作るためのテスト
 * @author  大澤義鷹
 */
public class OverlayFrame extends javax.swing.JFrame {
  MyOverlayLayout layout;
  JPanel subPanel;
  JPanel overlayPanel;
  /**
   * Creates new form OverlayFrame
   */
  public OverlayFrame() {
    initComponents();
    this.setPreferredSize(new Dimension(400,400));
    overlayPanel = new JPanel();
    layout = new MyOverlayLayout(overlayPanel);
    overlayPanel.setLayout(layout);
    add(overlayPanel,BorderLayout.CENTER);
    
    TreeOfLifePanel tree = new TreeOfLifePanel();
    tree.setNumber(1);
    TreeOfLifePanel tree2 = new TreeOfLifePanel();
    tree2.setNumber(2);
    
    ShutterBar bar = new ShutterBar();
    MouseHandler mh = new MouseHandler(bar);
    bar.addMouseListener(mh);
    bar.addMouseMotionListener(mh);
    bar.setMinimumSize(new Dimension(0,20));
    bar.setPreferredSize(new Dimension(0,20));
    
    subPanel = new JPanel();
    subPanel.setLayout(new BorderLayout(0,0));
    subPanel.add(bar,BorderLayout.NORTH);
    subPanel.add(tree2,BorderLayout.CENTER);
    subPanel.setPreferredSize(new Dimension(350,150));

    layout.addLayoutComponent("sub",subPanel);
    layout.addLayoutComponent("main",tree);
    
    this.setLocationRelativeTo(null);
    pack();
  }

  class MouseHandler implements MouseMotionListener,MouseListener {
    Component parent;
    Rectangle bounds = new Rectangle();
    Dimension startDim;
    int oldHeight;
    
    MouseHandler(Component parent) {
      this.parent = parent;
    }
    
    public void mouseDragged(MouseEvent e) {
//      System.out.println("y = " + e.getY());
      int h = layout.getYpos() - e.getY();
      layout.setYpos(h);
      overlayPanel.revalidate();
    }

    public void mouseMoved(MouseEvent e) {
      if(bounds.contains(e.getPoint())) {
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
      } else {
        parent.setCursor(Cursor.getDefaultCursor());
      }
    }

    public void mousePressed(MouseEvent e) {
      startDim = subPanel.getPreferredSize();
    }


    public void mouseEntered(MouseEvent e) {
      parent.getBounds(bounds);
      bounds.height = 5;
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
      parent.setCursor(Cursor.getDefaultCursor());
    }

  }
  
  class MyOverlayLayout implements LayoutManager {
    Component main;
    Component sub;
    Container owner;
    int ypos = -1;
    Rectangle subBounds = new Rectangle();
    MyOverlayLayout(Container owner) {
      this.owner = owner;
    }
    public void addLayoutComponent(String name, Component comp) {
      if(name.equalsIgnoreCase("main")) {
        this.main = comp;
        owner.add(comp);
      } else if(name.equalsIgnoreCase("sub")) {
        this.sub = comp;
        owner.add(comp);
      }
      System.out.println("Component Count = " + owner.getComponentCount());
      if(owner.getComponentCount() == 1) {
        owner.setComponentZOrder(comp,0);
      } else if( main != null && sub != null) {
        owner.setComponentZOrder(main,1);
        owner.setComponentZOrder(sub,0);        
      }
    }

    public void removeLayoutComponent(Component comp) {
      if(comp == main) main = null;
      else if( comp == sub ) sub = null;
      ypos = -1;
    }

    public Dimension preferredLayoutSize(Container parent) {
      synchronized( parent.getTreeLock() ) {
        return parent.getSize();
      }
   }

    public Dimension minimumLayoutSize(Container parent) {
      synchronized( parent.getTreeLock() ) {
        return parent.getMinimumSize();
      }
    }

    public void layoutContainer(Container parent) {
      synchronized( parent.getTreeLock() ) {
        int size = parent.getComponentCount();
        Rectangle r = parent.getBounds();
        
        for(int i=0; i<size; i++) {
          Component c = parent.getComponent(i);
          if(! c.isVisible()) continue;
          if( c == main ) {
            c.setBounds(parent.getBounds());
          } else if( c == sub ) {
            Rectangle sr = sub.getBounds();
            if( ypos < 0 ) {
              ypos = sr.height;
              if(ypos == 0 || ypos >= r.height ) ypos = r.height * 22 / 100;
            }
            int ry = r.y + r.height - ypos;
            subBounds.setBounds(r.x, ry, r.width, r.height - ry);
            c.setBounds(subBounds);
          }
        }
      }
    }
    
    int getYpos() { return ypos; };

    void setYpos(int y) { 
      int h = owner.getBounds().height;
      if(y <= 0) ypos = 1;
      else if(y >= h) ypos = h - 1;
      else ypos = y;
      System.out.println("ypos = " + ypos);
    }
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    jPanel1 = new javax.swing.JPanel();
    jCheckBox1 = new javax.swing.JCheckBox();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    jCheckBox1.setSelected(true);
    jCheckBox1.setText("\u8868\u793a");
    jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
    jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jCheckBox1ActionPerformed(evt);
      }
    });

    jPanel1.add(jCheckBox1);

    getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
    JCheckBox cb = (JCheckBox)evt.getSource();
    if(cb.isSelected()) {
      layout.addLayoutComponent("sub",subPanel);
    } else {
      overlayPanel.remove(subPanel);
    }
    validate();
    repaint();
  }//GEN-LAST:event_jCheckBox1ActionPerformed
  
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new OverlayFrame().setVisible(true);
      }
    });
  }
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox jCheckBox1;
  private javax.swing.JPanel jPanel1;
  // End of variables declaration//GEN-END:variables
  
}
