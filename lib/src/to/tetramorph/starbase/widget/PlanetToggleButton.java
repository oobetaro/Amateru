/*
 *
 */
package to.tetramorph.starbase.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.util.IconLoader;
//import static java.lang.System.*;
/**
 * 使用する感受点を選択する時のトグルボタン。天体のシンボル画像のボタンで、
 * 選択すると緑系の色で点灯したたように表示。非選択にするとグレーで消灯。
 * 画像は/resources/symbols/の中のGIF画像を使用している。
 * それらは黒色でシンボルが描かれた透過GIFで、それをロードしたあと再加工して点灯と
 * 消灯の二つの画像を生成し、それをアイコンにしてトグルボタンにセットしている。<p>
 * トグルボタンの状態によって背景色を変化させることができれば、一枚の透過GIFがあれ
 * ば事たりるわけだが、JToggleButtonの仕様上それは難しい。一番簡単な方法は、選択・
 * 非選択の二種類のアイコンを用意してボタンにセットすることなのだが、それだと新たに
 * 画像ファイルをシンボルの数だけ(つまり大量に)用意しなければならなくなる。
 * だからすでに使用しているシンボル画像ファイルのビットマップを内部で加工して、
 * 自動生成する方式を採った。
 * 
 * 惑星シンボルの画像ファイルはクラスファイルパスの下の
 * "/resources/symbols/"内にあり、Const.SYMBOL_NAMES[id]+".gif"という名前。
 * (具体的にはsun.gif,mon.gif,mer.gif等)。
 * 点灯時の背景は同内"brightBG.gif"。消灯時は"darkBG.gif"。
 * 背景ビットマップに惑星シンボルのビットマップを合成して、ボタン用のシンボル
 * 画像を生成している。
 * 
 * 画像ファイルの作成には注意が必要で、GIFのパレットのうち、0番目が文字色、1番目は
 * 無視され、2番目が背景色となっていて、背景色を指定して透過GIFでなければならない。
 * このようにパレットを狙ったGIFはPhotoshopではうまく作成できなかった(私が正しく
 * 条件を設定できていないのかもしれないが)。EDGEというフリーソフトを使って作成した。
 */
public class PlanetToggleButton extends JToggleButton {
  private int id;
  private static ImageIcon[] brightImageIcons;
  private static ImageIcon[] darkImageIcons;
//  private static ImageIcon[] brightImageIcons2;
//  private static ImageIcon[] darkImageIcons2;
  private static final int size = 21;
  /**
   * トグルボタンを作成する。初期値は太陽のシンボルで作成。
   * 後からsetBody()で変更可能。
   */
  public PlanetToggleButton() {
    super();
    init();
    setBodyID(SUN);
    setPreferredSize(new Dimension(size,size)); //19 x 19
    setMinimumSize(new Dimension(size,size));
    setMargin(new Insets(0,0,0,0));
    setSelected(true);
    setRolloverEnabled(false);
    setBorder( BorderFactory.createLineBorder(Color.darkGray) );
  }
  /**
   * idで指定された天体番号でトグルボタンを作成。初期値は選択状態。
   * @param id Constで宣言されている惑星定数(SUN,MOON等)
   */
  public PlanetToggleButton(int id) {
    super();
    init();
    setBodyID(id);
    setPreferredSize(new Dimension(size,size));
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
   * このボタンの天体IDを返す。
   */
  public int getBodyID() {
    return id;
  }
  /**
   * このボタンに天体IDを指定する。指定することでボタンに天体記号が表示される。
   */
  public void setBodyID(int id) {
    this.id = id;
    setIcon( brightImageIcons[id] );
    setSelectedIcon( darkImageIcons[id] );
    setToolTipText(PLANET_NAMES[id]);
//    setRolloverIcon( darkImageIcons2[id] );
//    setRolloverSelectedIcon( brightImageIcons2[id]);
  }

  private void init() {
    //一度だけ全シンボルのﾋﾞｯﾄﾏｯﾌﾟｲﾒｰｼﾞを作成しstaticのﾘｽﾄに保管する。
    if(brightImageIcons != null) return;
    brightImageIcons = new ImageIcon[ANTI_VERTEX+1];
    darkImageIcons = new ImageIcon[ANTI_VERTEX+1];
//    brightImageIcons2 = new ImageIcon[ANTI_VERTEX+1];
//    darkImageIcons2 = new ImageIcon[ANTI_VERTEX+1];
    //Image [] img = new Image[3];
    Image brightImage = IconLoader.getImage("/resources/symbols/brightBG.gif");
    Image darkImage = IconLoader.getImage("/resources/symbols/darkBG.gif");
//    Image brightImage2 = IconLoader.getImage("/resources/symbols/brightBG2.gif");
//    Image darkImage2 = IconLoader.getImage("/resources/symbols/darkBG2.gif");
    for ( int id = SUN; id <= ANTI_VERTEX; id++ ) {
      if ( Const.SYMBOL_NAMES[id] == null ) continue;
      Image iconImage = IconLoader
        .getImage("/resources/symbols/" + SYMBOL_NAMES[id] + ".gif");
      int size = 21;
      int [] src = new int[15 * 15];
      int [] brightBG = new int[21 * 21];
      int [] darkBG = new int[21*21];
//      int [] brightBG2 = new int[21*21];
//      int [] darkBG2 = new int[21*21];
      String errmsg = "PlatetToggleButton : Grabber Error!";
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

//        pg = new PixelGrabber(brightImage2,0,0,21,21,brightBG2,0,21);
//        pg.grabPixels();
//        if( (pg.status() & ImageObserver.ABORT) != 0 )
//          System.out.println(errmsg);
//        pg = new PixelGrabber(darkImage2,0,0,21,21,darkBG2,0,21);
//        pg.grabPixels();

        if( (pg.status() & ImageObserver.ABORT) != 0 )
          System.out.println(errmsg);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
      createBitmap(src,brightBG);
      createBitmap(src,darkBG);
//      createBitmap(src,brightBG2);
//      createBitmap(src,darkBG2);
      brightImageIcons[id] = new ImageIcon(
        createImage(new MemoryImageSource(size,size,brightBG,0,size)));
      darkImageIcons[id] = new ImageIcon(
        createImage(new MemoryImageSource(size,size,darkBG,0,size)));
//      brightImageIcons2[id] = new ImageIcon(
//        createImage(new MemoryImageSource(size,size,brightBG2,0,size)));
//      darkImageIcons2[id] = new ImageIcon(
//        createImage(new MemoryImageSource(size,size,darkBG2,0,size)));
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
//  // テストメソッド-----------------------------------------------------------
//  static void createAndShowGUI() {
//    UIManager.put("swing.boldMetal", Boolean.FALSE);
//        try {
//        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }    final JFrame frame = new JFrame();
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setLayout(new FlowLayout());
//    final PlanetToggleButton planetButton = new PlanetToggleButton( Const.SUN );
//    frame.getContentPane().add(planetButton);
//    frame.pack();
//    frame.setLocationRelativeTo(null);
//    frame.setVisible(true);
//  }
//
//  public static void main(String[] args) {
//    javax.swing.SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//        createAndShowGUI();
//      }
//    });
//  }

}