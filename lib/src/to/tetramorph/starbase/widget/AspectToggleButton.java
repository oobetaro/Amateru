/*
 *
 */
package to.tetramorph.starbase.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.util.IconLoader;

/**
 * 使用するアスペクトを選択する時のトグルボタン。アスペクトのシンボル画像のボタンで、
 * 選択すると緑系の色で点灯したたように表示。非選択にするとグレーで消灯。
 * 画像は/resources/symbols/の中のGIF画像を使用している。
 * それらは黒色でシンボルが描かれた透過GIFで、それをロードしたあと再加工して点灯と
 * 消灯の二つの画像を生成し、それをアイコンにしてトグルボタンにセットしている。<p>
 * トグルボタンの状態によって背景色を変化させることができれば、一枚の透過GIFがあれ
 * ば事たりるわけだが、JToggleButtonの仕様上それは難しい。一番簡単な方法は、選択・
 * 非選択の二種類のアイコンを用意してボタンにセットすることなのだが、それだと新たに
 * 二種類の画像ファイルをシンボルの数だけ(つまり大量に)用意しなければならなくなる。
 * だからすでに使用しているシンボル画像ファイルのビットマップを内部で加工して、
 * 自動生成する方式を採った。
 * 
 * アスペクトシンボルの画像ファイルはクラスファイルパスの下の
 * "/resources/symbols/"内にあり、Const.ASPECT_SYMBOL_NAMES[id]+".gif"という名前。
 * (具体的にはa0.gif,a60.gif,a90.gif等)。
 * 点灯時の背景は同内"brightBG.gif"。消灯時は"darkBG.gif"。
 * 背景ビットマップにアスペクトシンボルのビットマップを合成して、ボタン用のシンボル
 * 画像を生成している。
 * 
 * 画像ファイルの作成には注意が必要で、GIFのパレットのうち、0番目が文字色、1番目は
 * 無視され、2番目が背景色となっていて、背景色を透過指定した透過GIFでなければならない。
 * このようにパレットを狙ったGIFはPhotoshopではうまく作成できなかった(私が正しく
 * 条件を設定できていないのかもしれないが)。EDGEというフリーソフトを使って作成した。
 */
public class AspectToggleButton extends JToggleButton {
  private int id;
  private static ImageIcon[] brightImageIcons;
  private static ImageIcon[] darkImageIcons;
  private static final int size = 21;
  /**
   * トグルボタンを作成する。初期値はコンジャクションのシンボルで作成。
   * 後からsetBody()で変更可能。
   */
  public AspectToggleButton() {
    super();
    init();
    setAspectID(CONJUNCTION);
    setPreferredSize(new Dimension(size,size)); //19 x 19
    setMinimumSize(new Dimension(size,size));
    setMargin(new Insets(0,0,0,0));
    setSelected(true);
    setRolloverEnabled(false);
    setBorder( BorderFactory.createLineBorder(Color.darkGray) );
  }
  /**
   * idで指定されたアスペクト番号でトグルボタンを作成。初期値は選択状態。
   * @param id Constで宣言されているアスペクト定数(CONJUNCTION,SEXTILE等)
   */
  public AspectToggleButton(int id) {
    super();
    init();
    setAspectID(id);
    setPreferredSize(new Dimension(size,size)); //19 x 19
    setMinimumSize(new Dimension(size,size));
    setMargin(new Insets(0,0,0,0));
    setSelected(true);
    setRolloverEnabled(false);
    setBorder( BorderFactory.createLineBorder(Color.darkGray) );
  }
  /**
   * スイッチの選択状態を設定する。trueなら点灯、falseなら消灯。
   */
  public void setSelected(boolean b) {
    super.setSelected(! b);
  }
  /**
   * スイッチの選択状態を返す。点灯しているならtrue、消灯ならfalse。
   */
  public boolean isSelected() {
    return ! super.isSelected();
  }
  /**
   * このボタンのアスペクト番号を返す。
   */
  public int getAspectID() {
    return id;
  }
  /**
   * このボタンにアスペクト番号を指定する。
   * 指定することでボタンに天体記号が表示される。
   */
  public void setAspectID(int id) {
    this.id = id;
    setIcon( brightImageIcons[id] );
    setSelectedIcon( darkImageIcons[id] );
    // ToolTip文を作成。"ｱｽﾍﾟｸﾄ名(離角)"という書式に変換。
    // 離角の小数点以下の桁が0の場合はそれを取り去る。
    double a = ASPECT_ANGLES[id];
    double ia = (double)((int)a);
    String v = ((a - ia) > 0d) ? "" + a : "" + (int)ia;
    setToolTipText( String.format("%s(%s)",ASPECT_NAMES[id],v) );
  }
  //ｱｽﾍﾟｸﾄｼﾝﾎﾞﾙ画像のﾛｰﾄﾞと生成
  private void init() {
    //初期化時に一度だけ全シンボルのﾋﾞｯﾄﾏｯﾌﾟｲﾒｰｼﾞを作成しstaticのﾘｽﾄに保管する。
    if(brightImageIcons != null) return;
    brightImageIcons = new ImageIcon[ ASPECT_SYMBOL_NAMES.length ];
    darkImageIcons = new ImageIcon[ ASPECT_SYMBOL_NAMES.length ];
    Image brightImage = IconLoader.getImage("/resources/symbols/brightBG.gif");
    Image darkImage = IconLoader.getImage("/resources/symbols/darkBG.gif");
    for(int id = CONJUNCTION; id <= PARALLEL; id++) {
      Image iconImage = IconLoader
        .getImage("/resources/symbols/" + ASPECT_SYMBOL_NAMES[id] + ".gif");
      int size = 21;
      int [] src = new int[15 * 15];
      int [] brightBG = new int[21 * 21];
      int [] darkBG = new int[21 * 21];
      String errmsg = "AspectToggleButton : Grabber Error!";
      try {
        PixelGrabber pg = new PixelGrabber(iconImage,0,0,15,15,src,0,15);
        pg.grabPixels();
        if( (pg.status() & ImageObserver.ABORT) != 0 )
          System.out.println(errmsg);
        pg = new PixelGrabber(brightImage,0,0,21,21,brightBG,0,21);
        pg.grabPixels();
        if( (pg.status() & ImageObserver.ABORT) != 0 )
          System.out.println(errmsg);
        pg = new PixelGrabber(darkImage,0,0,21,21,darkBG,0,21);
        pg.grabPixels();
        if( (pg.status() & ImageObserver.ABORT) != 0 )
          System.out.println(errmsg);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
      createBitmap(src,brightBG);
      createBitmap(src,darkBG);
      brightImageIcons[id] = new ImageIcon(
        createImage(new MemoryImageSource(size,size,brightBG,0,size)));
      darkImageIcons[id] = new ImageIcon(
        createImage(new MemoryImageSource(size,size,darkBG,0,size)));
    }
  }
  // src[]は15*15pixcel
  // buf[]は21*21pixcelを想定
  private static void createBitmap(int [] source,int [] bg) {
    int dist_w = 21, dist_h=21;
    int src_w = 15,src_h = 15;
    int y_offset = ( dist_h - src_h ) / 2 * dist_w;
    int x_offset = ( dist_w - src_w ) / 2;
    for(int y=0; y<src_h; y++) {
      for(int x=0; x<src_w; x++) {
        int d = (y * dist_w + y_offset) + (x + x_offset);
        int p = source[y * src_w + x];
        if(p != 0) bg[d] = 0xFF404040; //灰色の1ドット
      }
    }
  }  
}