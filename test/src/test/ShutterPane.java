/*
 * ShutterPane.java
 *
 * Created on 2007/11/03, 9:37
 */

package to.tetramorph.starbase.multisplit;

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
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import to.tetramorph.starbase.widget.PanelCloseButton;
import to.tetramorph.starbase.widget.PanelComebackButton;
import to.tetramorph.starbase.widget.PanelMinimizeButton;

/**
 *
 * @author  大澤義鷹
 */
public class ShutterPane extends javax.swing.JPanel {
  ShutterLayout layout;
  JPanel subPanel;
  Component mainPanel;
  Component subComp;
  ShutterBar bar;
  public static final int CLOSE_BUTTON = 0;
  public static final int MINIMIZE_BUTTON = 1;
  public static final int COMEBACK_BUTTON = 2;
  
  
  /** Creates new form ShutterPane */

  public ShutterPane() {
    initComponents();
    layout = new ShutterLayout(this);
    setLayout(layout);
    bar = new ShutterBar();
    MouseHandler mh = new MouseHandler(bar);
    bar.addMouseListener(mh);
    bar.addMouseMotionListener(mh);
    bar.setMinimumSize(new Dimension(0,20));
    bar.setPreferredSize(new Dimension(0,20));
    subPanel = new JPanel();
    subPanel.setLayout(new BorderLayout(0,0));
    subPanel.add(bar,BorderLayout.NORTH);
  }
  /**
   * シャッターバーに出すボタンを指定する。指定した順番でボタンは表示される。
   * <pre>
   * setButton(ShutterPane.MINIMIZE_BUTTON, ShutterPane.CLOSE_BUTTON);
   * とすれば、バーの右端に[最小化ボタン][閉じるボタン]が二つならんで表示される。
   * 可変長引数を受け取るので、列挙するボタンの数に応じて表示されるボタンも変化
   * する。初期値はすべてのボタンが表示されない。すべてのボタンを表示したくない
   * 場合はsetButtons()とすればよい。
   * @param buttonCodes CLOSE_BUTTON,MIMIZE_BUTTON,COMBACK_BUTTONの3種類を指定
   * できる。同じボタンを重複指定しても一つしか表示されない。
   */
  public void setButtons(int ... buttonCodes) {
    bar.setButtons( buttonCodes );
  }
  /**
   * シャッターボタンオブジェクトを返す。ボタンオブジェクトにリスナを登録するとき
   * はこのメソッドでボタンのインスタンスを取得し、それに対して登録する。
   * @param buttonCode CLOSE_BUTTON,MIMIZE_BUTTON,COMBACK_BUTTONのどれか。
   */
  public AbstractButton getButtons(int buttonCode) {
    return bar.getButton(buttonCode);
  }
  /**
   * 背景側にコンポーネントをセットする。 コンポーネントの削除はnullをセットする。
   */
  public void setBackgroundComponent(Component c) {
    if ( c == null ) {
      remove(mainPanel);
    } else {
      mainPanel = c;
      layout.addLayoutComponent("main",c);
    }
    revalidate();
    repaint();
  }
  /**
   * 前景側にコンポーネントをセットする。これでセットしたコンポーネントは、
   * マウス操作でシャッターバーを上下させ、表示位置をユーザーが変更できる。
   * コンポーネントの削除はnullをセットする。そのときシャッターバーも画面から消える。
   */
  public void setForegroundComponent(Component c) {
    if ( c == null ) {
      remove(subPanel);
      subPanel.remove(subComp);
    } else {
      this.subComp = c;
      subPanel.add(c,BorderLayout.CENTER);
      layout.addLayoutComponent("sub",subPanel);
    }
    revalidate();
    repaint();
  }
  //シャッターバーの上下移動を行うためのマウスリスナ
  private class MouseHandler implements MouseMotionListener,MouseListener {
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
      //overlayPanel.revalidate();
      revalidate();
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

    public void mouseReleased(MouseEvent e) {
      parent.setCursor(Cursor.getDefaultCursor());
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

  }

  //重ね合わせ描画を行う専用のレイアウトマネージャ
  private class ShutterLayout implements LayoutManager {
    Component main;
    Component sub;
    Container owner;
    int ypos = -1;
    Rectangle subBounds = new Rectangle();
    
    ShutterLayout(Container owner) {
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
            System.out.println("main z-order = " + parent.getComponentZOrder(main));
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
    
    //バーのY座標を返す
    int getYpos() { return ypos; };

    //バーY座標をセットする
    void setYpos(int y) { 
      int h = owner.getBounds().height;
      if ( y <= 0 ) ypos = 1;
      else if ( y >= h ) ypos = h - 1;
      else ypos = y;
    }
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setLayout(null);

  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
  
}
