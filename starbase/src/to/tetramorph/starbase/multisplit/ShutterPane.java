/*
 * ShutterPane.java
 *
 * Created on 2007/11/05, 14:4
 *
 */

package to.tetramorph.starbase.multisplit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * 背景コンポーネントに重ねて前景コンポーネントを配置するためのパネル。
 * 前景コンポーネントにはシャッターバーがつき、それをマウスで上下させることがで
 * きる。
 * @author 大澤義鷹
 */
public class ShutterPane extends JLayeredPane {

  //前景コンポーネントはバーのついた前景用パネルにはめ込まれて、
  //それがレイヤーペインにセットされる。
  //背景パネルはそのまま仲介なしにレイヤーペインにセットされる。

  JPanel subPanel;     //前景用パネル
  Component mainPanel; //背景コンポーネント
  Component subComp;   //前景コンポーネント
  PanelBar bar;      //ボタンつきのスライダーバー

  /** 「閉じる」ボタン */
  public static final int CLOSE_BUTTON = 0;
  /** 「最小化」ボタン */
  public static final int MINIMIZE_BUTTON = 1;
  /** 「復帰」ボタン */
  public static final int COMEBACK_BUTTON = 2;

  //スライダーバーの位置 (単位 : pixcel)
  int ypos = -1;

  /**
   * オブジェクトを作成する。
   */
  public ShutterPane() {
    super();
    addComponentListener(new ComponentHandler());
    bar = new PanelBar();
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
  public AbstractButton getButton(int buttonCode) {
    return bar.getButton(buttonCode);
  }
  /**
   * 背景側にコンポーネントをセットする。 コンポーネントの削除はnullをセットする。
   */
  public void setBackgroundComponent(Component c) {
    if( c == null) {
      if( mainPanel == null) return;
      remove(getIndexOf( mainPanel ));
      mainPanel = null;
    } else {
      if( mainPanel != null) remove(getIndexOf( mainPanel ));
      add( c, JLayeredPane.DEFAULT_LAYER);
      mainPanel = c;
    }
    resize();
  }
  /**
   * 前景側にコンポーネントをセットする。これでセットしたコンポーネントは、
   * マウス操作でシャッターバーを上下させ、表示位置をユーザーが変更できる。
   * コンポーネントの削除はnullをセットする。そのときシャッターバーも画面から消える。
   */
  public void setForegroundComponent(Component c) {
    if ( c == null ) {
      if( subComp == null) return;
      remove(getIndexOf( subPanel ));
      subPanel.remove(subComp);
      subComp = null;
    } else {
      if( subComp != null) subPanel.remove(subComp);
      subPanel.add(c,BorderLayout.CENTER);
      add( subPanel, JLayeredPane.PALETTE_LAYER);
      this.subComp = c;
    }
    resize();
  }
  public Component getForegroundComponent() {
    return subComp;
  }
  public void setTitle(String title) {
    bar.setTitle(title);
  }

  /**
   * 現在のレイヤーペインのサイズに応じて、背景用レイヤー(のコンポーネント)と
   * 前景用コンポーネントに、適切な座標とサイズをセットする。
   * componentResized(),setForegroundComponent(),setBackgroundComponent()から
   * 呼び出される。
   */
  void resize() {
    Dimension size = getSize();
    if(mainPanel != null) {
      mainPanel.setLocation(0,0);
      mainPanel.setSize(size);
    }
    if(subPanel != null) {
      ypos = (int)(size.height * yper);
      subPanel.setLocation(0,ypos);
      subPanel.setSize(size.width, size.height - ypos);
    }
    revalidate();
    repaint();
  }

  // レイヤーペイン高さに対するバーのY座標の比率。比率は画面(フレーム等)がリサイズ
  // されたときに、画面サイズに応じて比率からバーの位置を決定するため。
  // 画面の高さ半分の位置なら0.5で、画面がどんなサイズにリサイズされても、バーが
  // 画面の高さの中間に来るように設定できる。

  double yper = 78d/100d;

  // レイヤーペインは中のコンポーネントのサイズがセットされていなければ表示でき
  // ない。しかしオブジェクト作成の時点では、レイヤーペインのサイズも未決定で、
  // getBounds()などもゼロ値を返してくる。背景レイヤはこのペインと同サイズの値
  // を設定すればいいわけだが、この時点ではペインのサイズが取得できない。
  // そこで、このリスナのメソッドが呼ばれるとき、ペインの画面サイズが決定してい
  // るので、このタイミングで、中のコンポーネントにサイズを設定する。

  class ComponentHandler extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      resize();
    }
  }

  //シャッターバーの上下移動を行うためのマウスリスナ
  private class MouseHandler implements MouseMotionListener,MouseListener {
    Component parent;                    //バーへの参照
    Rectangle bounds = new Rectangle(); //当たり判定用
    Rectangle subBounds;                //サブパネルの座標・サイズ保管用

    //バーのコンポーネントを指定してオブジェクトを作る
    MouseHandler(Component parent) {
      this.parent = parent;
    }

    //マウスドラッグでサブパネルの位置とサイズを変化させる
    public void mouseDragged(MouseEvent e) {
      Dimension size = getSize();       //レイヤペインのサイズを取得
      ypos = subBounds.y + e.getY();    //開始時のY座標にドラッグ移動距離を足す
      if(ypos < 0) ypos = 0;
      else if( ypos > (size.height -4)) ypos = size.height - 4;
      //Y座標のパーセント値を更新。この変数はresize()で必要
      yper = (double)ypos  / (double)size.height;

      subBounds.y = ypos;                           //サブパネルの位置を移動し、
      subBounds.height = size.height - ypos;        //サブパネルの高さを更新
      subPanel.setBounds(subBounds);
      revalidate();
    }
    //シャッターバーの上にマウスが来たらカーソルを変更
    public void mouseMoved(MouseEvent e) {
      if(bounds.contains(e.getPoint())) {
        parent.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
      } else {
        parent.setCursor(Cursor.getDefaultCursor());
      }
    }

    //ドラッグ開始時点のサブパネルの位置を保管
    public void mousePressed(MouseEvent e) {
      subBounds = subPanel.getBounds();
    }

    //マウスのバーへの当たり判定に使う矩形を作成(バーの高さより小さく設定)
    public void mouseEntered(MouseEvent e) {
      parent.getBounds(bounds);
      bounds.height = 5;
    }

    //マウスボタンが押し離されたタイミングでカーソルをデフォルトに戻す
    public void mouseReleased(MouseEvent e) {
      parent.setCursor(Cursor.getDefaultCursor());
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

  }
  /* これはJLayeredPaneを使っているが、最初は独自のレイアウトマネージャを用意して
   * 同じようなことをしていた。しかし背景コンポーネントにJSplitPaneの派生クラス
   * を入れたところ、そのスライダーバーを移動させると、前景コンポーネントを塗り
   * つぶして消してしまうという問題が発生した。なにかやり方がまずかったのかもし
   * れないが、コンポーネントをオーバーラップさせるのは、込み入った事情があるよう
   * で、それを解析して独自に実装することも考えたが、JLayeredPaneを使ったほうが
   * 異なるプラットホームでの動作も安心できると考え、今のように作り直した。
   */
}
