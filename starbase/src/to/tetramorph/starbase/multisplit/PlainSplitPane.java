/*
 * PlainSplitPane.java
 * Created on 2007/10/26, 0:23
 */

package to.tetramorph.starbase.multisplit;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * ボーダー線を消去し、なんのそっけもないものにした専用のSplitPane。
 * JSplitPaneを多重に入れ子にして使おうとしたとき、デバイダのデザインやボーダー
 * の設定が邪魔になるため用意した。
 * @author 大澤義鷹
 */
public class PlainSplitPane extends JSplitPane {
  static final Border emptyBorder = new EmptyBorder(0,0,0,0);
  /**
   * このSplitPaneのデバイダのサイズで値は4(pixcel)。これ以上小さいと、マウス操作
   * がしずらくなる。
   */
  public static final int DIVIDER_SIZE = 4;
  /**
   *  
   * PlainSplitPane オブジェクトを作成する
   */
  public PlainSplitPane() {
    super();
    setDividerSize( DIVIDER_SIZE );
    setResizeWeight(1.0);
    setBorder(emptyBorder);
    setContinuousLayout( false );
    setUI( new MyUI() );
  }
  /**
   *  
   * PlainSplitPane オブジェクトを作成する
   */
  public PlainSplitPane(int orientation) {
    super(orientation);
    setDividerSize( DIVIDER_SIZE );
    setResizeWeight(1.0);
    setBorder(emptyBorder);
    setContinuousLayout( false );
    setUI( new MyUI() );
  }
  
  /**
   * PlainSplitPane オブジェクトを作成する。
   */
  public PlainSplitPane( int orientation, Component left, Component right) {
    super(orientation,left,right);
    setDividerSize( DIVIDER_SIZE );
    setResizeWeight(1.0);
    setContinuousLayout( false );
    setBorder(emptyBorder);
    setUI( new MyUI() );
  }
  /**
   * UIを更新
   */
  public void updateUI() {
//    setDividerSize( DIVIDER_SIZE );
//    setBorder(emptyBorder);
    setUI( new MyUI() );
    revalidate();
  }
  
  class MyUI extends BasicSplitPaneUI {
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new Divider(this);
    }
  }
  // 親クラスではボーダー線を描画する処理をしているのだが、
  // paint()をオーバーライドしてなにもしない。
  
  class Divider extends BasicSplitPaneDivider {
    
    Divider(BasicSplitPaneUI ui) {
      super(ui);
    }
    @Override
    public void paint(Graphics g) {
    }
  }
}
