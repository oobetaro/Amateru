/*
 * InnerTabbedPane.java
 *
 * Created on 2007/10/30, 1:53
 *
 */

package multisplit;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import to.tetramorph.starbase.widget.TabCloseButton;

/**
 *
 * @author 大澤義鷹
 */
public class InnerTabbedPane extends JTabbedPane {
  //static int count = 0;

  /**  InnerTabbedPane オブジェクトを作成する */
  public InnerTabbedPane() {
    super();
    setUI( new MyUI() );
    setTabLayoutPolicy( SCROLL_TAB_LAYOUT ); //これを入れないとタブの高さが変化
    setBorder( new MyBorder());
    //別のTabbedPaneをクリックしたとき一度目ではフォーカスが来ないが、このリスナ
    //でフォーカスを要求する。
    addMouseListener(new MouseAdapter() {
      @Override
      //タブがクリックされたらフォーカスを呼ぶ
      public void mouseReleased(MouseEvent evt) {
        boolean b = requestFocusInWindow();
        //System.out.println("フォーカスを要求したら = " + b);
        //実際しばしばfalseが返るが、動作に問題は見受けられない
      }
      public void mouseClicked(MouseEvent evt) {
        if(evt.getClickCount() == 2) {
          int i = indexAtLocation(evt.getX(), evt.getY());
          if( i >= 0) {
            l.doubleClicked(THIS,getSelectedComponent());
          }
        }
      }
    });
  }

  public void updateUI() {
    setUI( new MyUI() );
    revalidate();
  }

  public void addTab( String title, Component c) {
    super.addTab(null, c);
    int tc = getTabCount()-1;
    setTabComponentAt( tc, new TabComponent(title,null) );
  }
  
  
  public void addTab( String title,Icon icon, Component c) {
    super.addTab( title, icon, c);
    int tc = getTabCount()-1;
    setTabComponentAt( tc, new TabComponent(title,icon) );
  }

  public void insertTab( String title, Icon icon, Component c, int index ) {
    super.insertTab(title,icon,c,null,index);
    setTabComponentAt( index, new TabComponent(title,icon) );
  }

  public void setIconAt(int index, Icon icon) {
    TabComponent tabComp = (TabComponent)getTabComponentAt(index);
    tabComp.setIcon(icon);
    super.setIconAt(index, icon);
  }
  
  public void setTitleAt(int index, String title) {
    TabComponent tabComp = (TabComponent)getTabComponentAt(index);
    tabComp.setTitle(title);
    super.setTitleAt(index,title);
  }
  
  public void setTabComponentAt(int index, Component c) {
    if ( c instanceof TabComponent) {
      TabComponent tabc = (TabComponent)c;
      super.setIconAt(index,tabc.getIcon());
      super.setTitleAt(index,tabc.getTitle());
    }
    super.setTabComponentAt(index,c);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(50);
    sb.append("InnerTabbedPane (");
    for(int i=0; i<getTabCount(); i++) {
      sb.append(getTitleAt(i));
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() -1);
    sb.append(")");
    return sb.toString();
  }
  
  protected TabbedPaneListener l;
  protected boolean hide = false;
  public void setTabbedPaneListener(TabbedPaneListener l) {
    this.l = l;
  }

  private InnerTabbedPane THIS = this;  
  
//  private JComponent createTabComponent(String title,Icon icon) {
//    JComponent comp = new JComponent() {}; //abstractでもこうすればインスタンスを作れる
//    comp.setLayout(new BorderLayout(5, 0));
//    comp.setBorder(null);
//    JLabel label = new JLabel(title, JLabel.LEFT);
//    if ( icon != null ) label.setIcon(icon);
//    comp.add(label, BorderLayout.CENTER);
//    TabCloseButton button = new TabCloseButton();
//    comp.add(button, BorderLayout.EAST);
//    
//    button.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
//        Component tabComp = ((Component)event.getSource()).getParent();
//        remove( indexOfTabComponent(tabComp) );
//        if ( l != null ) {
//          l.closedTab(THIS,tabComp);
//          if( getTabCount() == 0 )   l.emptyTabs(THIS);
//        }
//      }
//    });
//    return comp;
//  }
  class TabComponent extends JComponent {
    JLabel label;
    TabCloseButton button = new TabCloseButton();
    TabComponent( String title, Icon icon ) {
      setLayout(new BorderLayout(5,0));
      setBorder(null);
      label = new JLabel(title,JLabel.LEFT);
      if(icon != null) label.setIcon(icon);
      add(label, BorderLayout.CENTER);
      TabCloseButton button = new TabCloseButton();
      add(button, BorderLayout.EAST);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          Component tabComp = ((Component)event.getSource()).getParent();
          System.out.println("indexOfTabComponent = " +  indexOfTabComponent(tabComp));
          InnerTabbedPane.this.remove( indexOfTabComponent(tabComp) );
          if ( l != null ) {
            l.closedTab(THIS,tabComp);
            //if( getTabCount() == 0 )   l.emptyTabs(THIS);
          }
        }
      });     
    }
    void setIcon(Icon icon) {
      label.setIcon(icon);
    }
    void setTitle(String title) {
      label.setText(title);
    }
    Icon getIcon() {
      return label.getIcon();
    }
    String getTitle() {
      return label.getText();
    }
  }
  protected Rectangle glassBounds = new Rectangle();
  protected boolean onCursor = false;

  //主にタブ区画のカスタマイズ
  class MyUI extends BasicTabbedPaneUI {
    Color focusColor = new Color(72,143,251);
    MyUI() {
      System.out.println("tabInsets = " + tabInsets);
    }
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) { 
    }
    //コンテンツ区画の下のボーダー表示をやめる
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) { 
    }
    //コンテンツ区画の右のボーダー表示をやめる
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                                               int selectedIndex,
                                               int x, int y, int w, int h) {
    }
    protected void paintTabBackground(Graphics g, int tabPlacement,
                                        int tabIndex,
                                        int x, int y, int w, int h,
                                        boolean isSelected ) {
      Color selcol = UIManager.getColor("TabbedPane.selected");
      Color bgcol = Color.LIGHT_GRAY; //isSelected? selcol : tabPane.getBackgroundAt(tabIndex);
      Graphics2D g2 = (Graphics2D)g;
      int x2,y2;
      if(tabPane.hasFocus() && isSelected) {
      //if(isSelected) {
        x2 = x + w; //(int)(w * 0.8);
        y2 = y;
        GradientPaint gp = new GradientPaint(x,y,focusColor,x2,y2,selcol,false );
        g2.setPaint(gp);
        g2.fill(new Rectangle(x+1,y+1,w-3,h-1));
      } else if( isSelected ) {        
        g2.setPaint(selcol);
        g2.fill(new Rectangle(x+1,y+1,w-3,h-1));
      } else {
        g2.setPaint(bgcol);
        g2.fill(new Rectangle(x+1,y+1,w-3,h-1));        
      }
      
    }
    //タブ選択時に点線の矩形でタイトルが囲まれるのをやめる
    protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                       Rectangle[] rects, int tabIndex, 
                                       Rectangle iconRect, Rectangle textRect,
                                       boolean isSelected) {      
    }
    //タブ選択されたときタイトルが1pixcel上に上がるのをやめる
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        Rectangle tabRect = rects[tabIndex];
        int nudge = 0;
        switch(tabPlacement) {
           case BOTTOM:
              nudge = isSelected? 1 : -1;
              break;
          case LEFT:
          case RIGHT:
              nudge = tabRect.height % 2;
              break;
          case TOP:
          default:
              nudge = 1;
              //nudge = isSelected? -1  : 1;;
        }
        return nudge;
    }
    //×ボタンの右横を少し詰める
    protected void installDefaults() {
      UIManager.put("TabbedPane.tabInsets",new Insets(0,5,1,2));
      super.installDefaults();
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

  //テスト
  private static void createAndShowGUI() {
    
    UIManager.put("swing.boldMetal", Boolean.FALSE);    
    JFrame frame = new JFrame("カスタムTabbedPane");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    InnerTabbedPane tabpan = new InnerTabbedPane();
    TreeOfLifePanel [] trees = new TreeOfLifePanel [10];
    for(int i=0; i<trees.length; i++) trees[i] = new TreeOfLifePanel();
    
    Icon maleIcon = new ImageIcon(
      InnerTabbedPane.class.getResource("/resources/List.male.png"));
    Icon femaleIcon = new ImageIcon(
      InnerTabbedPane.class.getResource("/resources/List.female.png"));

    for(int i=0; i<trees.length; i++) {
      trees[i].setBorder(null);
      trees[i].setNumber(i+1);
      trees[i].setBackground(Color.LIGHT_GRAY);
      tabpan.addTab("Tree " + (i+1), femaleIcon, trees[i]);
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
