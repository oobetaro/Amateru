/*
 *
 */
package to.tetramorph.util;
import static java.awt.GridBagConstraints.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import to.tetramorph.util.*;
/**
 * GridBagLayoutを簡単に使うためのツール。
 * <pre>
 * 簡単な分、GridBagLayoutをそのまま使うより小回りがきかないところもあるが、
 * かなり楽に使えるようになる。非常に多くのボタンやパネルを規則正しくならべたい
 * 場合、NetBeans5.5のGUIエディタでは配列にして扱う事ができないため、作業的に
 * 少々苦しい場合がある。そんなときはGUIエディタに頼らず、手書きで配列を使って
 * 書いたほうが簡単な場合もある。そういうときこのユーティリティは役にたつかもし
 * れない。<p>
 *
 * 使い方は各部品の位置やサイズを書いた配列int design[]を用意する。<p>
 *
 * <p>配列の最初の1個目はコマンド、あとにオペランドがつづく。
 * どのようなオペランドがつづくかはコマンドによって違う。
 * またGridBagConstraintsもstatic importしておくとなお使いやすい。
 * 定数名がバッティングしたときはご愁傷様。</p>
 *
 * SETコマンド(部品の配置位置とanchor,fillの指定)
 * SET,x,y,w,h,anchor,fill
 * fill,anchor共にGridBagConstraintsの定数を使用
 * anchor 余白があったときの部品の表示位置(alignと同じ)
 * anchor=CENTER,NORTH,NORTHEAST,EAST,SOUTHEAST,SOUTH,SOUTHWEST,WEST,NORTHWEST
 * fill 部品サイズの水平・垂直への引き延ばし指定
 * fill = NONE        (ひきのばし無し)
 * HORIZONTAL　(水平への引きのばし指定)
 * VERTICAL    (垂直への引きのばし指定)
 * BOTH        (水平・垂直ともに引きのばす)
 *
 * INSETコマンド　(部品の外側の余白。単位ピクセル)
 * INSETS,top,left,bottom,right
 * IPADコマンド   (部品の内側の余白。単位はピクセル)
 * IPAD,x,y
 * WEIGHTコマンド (部品の外側の余白。デフォルトは0。単位は不明。)
 * WEIGHT,x,y
 *
 * 例
 * import static java.awt.GridBagConstraints.*;
 * //import staticしたいところだが、現在GridBagUtilityにはパッケージ名が無く
 * //それがないとstaticインポートできない。
 * static final int SET = GridBagUtility.SET;
 * static final int INSETS = GridBagUtility.INSETS;
 * static final int IPAD = GridBagUtility.IPAD;
 * static final int WEIGHT = GridBagUtility.WEIGHT;
 *
 * static int [] design = {
 * SET,0,0,2,1,WEST,NONE,
 * SET,0,1,2,1,WEST,HORIZONTAL,
 * INSETS,top,left,bottom,right,
 * IPAD,x,y
 * SET,0,2,1,1,WEST,
 * WEIGHT,x,y
 * }
 * class .... extends JPanel {
 * .....
 * GridBagUtility gb = new GridBagUtility(this,design);
 * JLabel label1 = new JLable("HogeHoge");
 * gb.add(label1);
 * JTextField textfield = new JTextField();
 * gb.add(textfield);
 * ....
 *
 * つまり、位置は配列で指定しておくが、各部品は後からメソッドでsetしていく。
 * GridBagUtilityはdesign[]に書かれている条件にしたがって、addされた部品を順番にコンポーネントに配置していく。
 * </pre>
 */
public class GridBagUtility {
  public static final int SET = 100;
  public static final int INSETS = 200;
  public static final int IPAD = 300;
  public static final int WEIGHT = 400;
  
  GridBagConstraints conf = null;
  GridBagLayout layout = null;
  int [] design;
  int p=0;
  JComponent target;
  /**
   * targetはGridBagLayoutを適用して部品を配置するコンポーネント
   * design[]は部品配置を記述した配列。
   */
  public GridBagUtility(JComponent target,int [] design) {
    layout = new GridBagLayout();
    this.target = target;
    target.setLayout(layout);
    conf = new GridBagConstraints();
    this.design = design;
  }
  /**
   * コンポーネントを削除する。
   */
  public void remove(JComponent component) {
    //ﾚｲｱｳﾄﾏﾈｰｼﾞｬとｺﾝﾎﾟｰﾈﾝﾄ双方から削除してはじめて消える
    layout.removeLayoutComponent(component);
    target.remove(component);
  }
  /**
   * コンポーネントを配置する。
   */
  public void add(JComponent component) {
    while( p < design.length ) {
      if(design[p] == SET) {
        conf.gridx = design[p+1];
        conf.gridy = design[p+2];
        conf.gridwidth = design[p+3];
        conf.gridheight = design[p+4];
        conf.anchor = design[p+5];
        conf.fill   = design[p+6];
        p += 7;
        layout.setConstraints(component,conf);
        target.add(component);
        return;
      } else if(design[p] == INSETS) {
        conf.insets.top = design[p+1];
        conf.insets.left = design[p+2];
        conf.insets.bottom = design[p+3];
        conf.insets.right = design[p+4];
        p += 5;
      } else if(design[p] == IPAD) {
        conf.ipadx = design[p+1];
        conf.ipady = design[p+2];
        p += 3;
      } else if(design[p] == WEIGHT) {
        conf.weightx = design[p+1];
        conf.weighty = design[p+2];
        p += 3;
      } else
        throw new IllegalArgumentException
          ("GridBagUtility : 未定義コマンド検出");
    }
  }
  /**
   * デザインをセットする。
   */
  public void setDesign(int [] design) {
    this.design = design;
    p = 0;
  }
}

