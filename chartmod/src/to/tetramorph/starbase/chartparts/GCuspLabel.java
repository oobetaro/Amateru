/*
 * GCuspLabel.java
 *
 * Created on 2007/10/07, 12:42
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.AstroFont;
/**
 * カスプ位置をグラフィックで表示するラベル。
 * @author 大澤義鷹
 */
public class GCuspLabel extends GComponent {
  private Font font = new Font("Monospaced",Font.PLAIN,10);
  private Font dialogFont = new Font("Dialog",Font.PLAIN,10);
  private Font timesFont = new Font("Times",Font.PLAIN,10);
  private static final int LARGE_FONT = 0; //主に数字の整数部に使用
  private static final int SMALL_FONT = 1; //数字の小数部に使用
  private static final int COMMA_FONT = 2; //数字のコンマに使用
  private static final int BODY_FONT = 3;  //占星術シンボル用
  private static final int HOUSE_FONT = 4; //ハウス番号のローマ数字に使用
  Font [] fonts = new Font[5]; //large,small,comma,body各フォントを格納
  private static final int [] fontnums = { 
    HOUSE_FONT,BODY_FONT,LARGE_FONT,COMMA_FONT,SMALL_FONT };
  private static final String [] sampleStrings = new String [] {
    "VIII", " " + ZODIAC_CHARS[LIB], "29", ".", "01" };
//  private static final String [] houseNumbers ={ 
//    "I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII" };
  double fontSizePer = 1d / 60d;
  private Rectangle2D.Float sizeRect; //天体度数1行分のサイズ
  private Rectangle2D.Float fullRect = new Rectangle2D.Float(); //インセットこみのサイズ
  private Rectangle2D boundsRect;
  private float numMaxWidth;
  private Body body; //カスプ度数

  Color signColor = Color.BLUE;
  Color textColor = Color.BLACK;
  Color houseColor = Color.BLACK;

  /** サインシンボル色をセットする。 */
  public void setSignColor(Color signColor) {
    this.signColor = signColor;
  }
  /** 度数の色をセットする。*/
  public void setTextColor(Color textColor) {
    this.textColor = textColor;
  }
  /** ハウス番号色をセットする。*/
  public void setHouseColor(Color houseColor) {
    this.houseColor = houseColor;
  }
  /** サインシンボル色を返す。 */  
  public Color getSignColor() {
    return signColor;
  }
  /** 度数の色を返す。*/
  public Color getTextColor() {
    return textColor;
  }
  /** ハウス番号色を返す。*/
  public Color getHouseColor() {
    return houseColor;
  }
  /**  GCuspLabel オブジェクトを作成する */
  public GCuspLabel() {
  }
  /** フォントスケールを指定して空のオブジェクトを作成する。 */
  public GCuspLabel( double fontScale ) {
    setFontScale(fontScale);
  }
  public GCuspLabel( int align,double fontScale) {
    this(fontScale);
    setAlign(align);
  }
  /**
   * ハウスカスプをセットする。
   * @param body ハウス番号(1-12)
   */
  public void setBody(Body body) {
    this.body = body;
  }
  /**
   * ハウスカスプを返す。
   */
  public Body getBody() {
    return body;
  }
  protected void draw(double x,double y) {
    if(body == null) return;
    float xofs = 0;
    String [] str = new String[5];
    Color [] colors = { houseColor,signColor,textColor,textColor,textColor };
    FontRenderContext frc = g.getFontRenderContext();
    int hnum = body.id - CUSP1;
    str[0] = HOUSE_NUMBERS[ hnum ];
    str[1] = " " + ZODIAC_CHARS[body.getSign()];
//    String [] v =  AngleConverter
//      .getSignAngleConstantLength(body.getSignAngle(),2).split("\\.");
    String [] v = AngleConverter.getSignAngle(body.getSignAngle()).split("\\.");
    str[2] = v[0];
    str[3] = ".";
    str[4] = v[1];
    AffineTransform at = new AffineTransform();
    at.translate(x,y);
    Shape boundsShape = at.createTransformedShape(getSize());
    boundsRect = boundsShape.getBounds2D();
    if(bgColor != null) {
      g.setPaint(bgColor);
      g.fill(boundsShape);
    }
    float h = getSize().height;
    for(int i=0; i<str.length; i++) {
      int fn = fontnums[i];
      TextLayout tl = new TextLayout( str[i], fonts[ fn ], frc );
      at = new AffineTransform();
      if(i==0) { //ハウス番号は右寄せする。
        at.translate( x + (numMaxWidth - tl.getAdvance()), y + h);
        xofs += numMaxWidth;
      } else { //ハウス番号以外は文字幅を求めてxofsに加算(つまり左寄せ)
        at.translate( x + xofs, y + h );
        xofs += tl.getAdvance();
      }
      Shape outline = tl.getOutline(at);
      g.setPaint(colors[i]);
      g.fill(outline);
    }
  }
  /**
   * フォントサイズをbaaseWidthを1000とみなし、それに対する比率で指定する。
   */
  public void setFontScale(double fontSizePer) {
    this.fontSizePer = fontSizePer / 1000;
  }
  /**
   * 描画に必要なフォントを作成する。
   * 1行分の矩形エリア、インセット込みの矩形エリアのサイズを求める。
   */
  public void setup() {
    FontRenderContext frc = g.getFontRenderContext();

    float sz = (float)(baseWidth * fontSizePer);
    fonts[BODY_FONT] = AstroFont.getFont(sz);
    fonts[LARGE_FONT] = font.deriveFont(sz);
    fonts[SMALL_FONT] = font.deriveFont(sz * .6f);
    fonts[COMMA_FONT] = dialogFont.deriveFont(sz);
    fonts[HOUSE_FONT] = timesFont.deriveFont(sz);

    sizeRect = new Rectangle2D.Float();
    for(int i=0; i<sampleStrings.length; i++) {
      TextLayout tl = new TextLayout( sampleStrings[i], fonts[ fontnums[i] ], frc );
      //文字の高さは2文字目(つまりサインシンボル)のみで決定
      if( i == 1 ) sizeRect.height = tl.getAscent();
      sizeRect.width += tl.getAdvance();
    }
    float w = sizeRect.width;
    float h = sizeRect.height;
    float b = baseWidth;
    fullRect.width = w + insets[ LEFT ] * b + insets[ RIGHT ] * b;
    fullRect.height = h + insets[ TOP ] * b + insets[ BOTTOM ] * b;     
    //ハウス番号で一番長い幅をもつもののサイズを返す。右寄せ描画に必要なパラメタ。
    numMaxWidth = new TextLayout("VIII",fonts[HOUSE_FONT],frc).getAdvance();
  }
  
  public Rectangle2D.Float getSize() {
    return sizeRect;
  }
  public Rectangle2D.Float getFullSize() {
    return fullRect;
  }

  public GComponent contains(int x, int y) {
    if(getGComponentListener() == null) return null;
    if(boundsRect == null) return null;
    if(boundsRect.contains(x,y)) return this;
    return null;
  }

}
