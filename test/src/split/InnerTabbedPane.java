/*
 * InnerTabbedPane.java
 *
 * Created on 2007/10/30, 1:53
 *
 */

package split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 *
 * @author 大澤義鷹
 */
public class InnerTabbedPane extends JTabbedPane {
  
  /**  InnerTabbedPane オブジェクトを作成する */
  public InnerTabbedPane() {
    super();
    setUI( new MyUI() );
    setTabLayoutPolicy( SCROLL_TAB_LAYOUT );
    setBorder( new MyBorder());
  }
  
  class MyUI extends BasicTabbedPaneUI {
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) { 
    }

    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) { 
    }

    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
    }
  }  

  //へこんだベベルボーダー(親は彫り込みが2pixcelだが1pixcelにしたもの。)
  class MyBorder extends BevelBorder {
    MyBorder() {
      super( LOWERED );
    }
    protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
                                        int width, int height)  {
        Color oldColor = g.getColor();
        int h = height;
        int w = width;

        g.translate(x, y);

        g.setColor(getShadowInnerColor(c));
        g.drawLine(0, 0, 0, h-1); //左
        g.drawLine(1, 0, w-1, 0); //上

        g.setColor(getHighlightOuterColor(c));
        g.drawLine(1, h-1, w-1, h-1); //下
        g.drawLine(w-1, 1, w-1, h-2); //右

        g.translate(-x, -y);
        g.setColor(oldColor);

    }
  }
  
  public void updateUI() {
    setUI( new MyUI() );
    revalidate();
  }

  public void addTab( String title, Component c) {
    super.addTab(null, c);
    int tc = getTabCount()-1;
    setTabComponentAt( tc, createTabComponent(title) );
  }
  
  private JComponent createTabComponent(String title) {
    JComponent comp = new JComponent() {};
    comp.setLayout(new BorderLayout(5, 5));
    
    JLabel label = new JLabel(title, JLabel.LEFT);
    comp.add(label, BorderLayout.CENTER);
    TabCloseButton button = new TabCloseButton();
    comp.add(button, BorderLayout.EAST);
    
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Component tabComp = ((Component)event.getSource()).getParent();
        remove(tabComp);
        System.out.println("クローズボタンが押された");
      }
    });
    
    return comp;
  }
  private static void createAndShowGUI() {
    JFrame frame = new JFrame("カスタムTabbedPane");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    InnerTabbedPane tabpan = new InnerTabbedPane();
    TreeOfLifePanel [] trees = new TreeOfLifePanel [] { 
      new TreeOfLifePanel(),new TreeOfLifePanel(),new TreeOfLifePanel()
    };
    for(int i=0; i<trees.length; i++) {
      trees[i].setBorder(null);
      trees[i].setNumber(i+1);
      trees[i].setBackground(Color.LIGHT_GRAY);
      tabpan.addTab("生命の木" + (i+1),trees[i]);
    }
    frame.getContentPane().add(tabpan);
    frame.pack();
    frame.setVisible(true);
  }
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
  
}
