/*
 * GBodyLabel.java
 *
 * Created on 2007/10/03, 17:31
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
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.util.AngleConverter;
/**
 * 天体位置をグラフィックで表示するラベル。
 * @author 大澤義鷹
 */
public class GBodyLabel extends GComponent {
  private Font font = new Font("Monospaced",Font.PLAIN,10);
  private Font dialogFont = new Font("Dialog",Font.PLAIN,10);
  //Font timesFont = new Font("Times",Font.PLAIN,10);
  private static final int LARGE_FONT = 0; //主に数字の整数部に使用
  private static final int SMALL_FONT = 1; //数字の小数部に使用
  private static final int COMMA_FONT = 2; //数字のコンマに使用
  private static final int BODY_FONT = 3;  //占星術シンボル用
  Font [] fonts = new Font[4]; //large,small,comma,body各フォントを格納
  private static final int [] fontnums = 
    { BODY_FONT,BODY_FONT,LARGE_FONT,COMMA_FONT,SMALL_FONT,BODY_FONT };
  private static final String [] str = new String [] { 
    BODY_CHARS[SUN] + " ", "" + ZODIAC_CHARS[LIB], "29", ".", "01", "\u00CF" };
  
  double fontSizePer = 1d / 60d;
  private Rectangle2D.Float sizeRect; //天体度数1行分のサイズ
  private Rectangle2D.Float fullRect = new Rectangle2D.Float(); //インセットこみのサイズ
  private Rectangle2D boundsRect;
  private Body body;


  Color signColor = Color.BLUE;
  Color bodyColor = Color.RED;
  Color textColor = Color.BLACK;
  Color revColor = Color.RED;
  /**
   *  GBodyLabel オブジェクトを作成する
   */
  public GBodyLabel() {
  }
  public GBodyLabel(Body body) {
    setBody(body);
  }

  /** サインシンボル色をセットする。 */
  public void setSignColor(Color signColor) {
    this.signColor = signColor;
  }
  /** 天体シンボル色をセットする。*/
  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }
  /** 数字の色をセットする。*/
  public void setTextColor(Color textColor) {
    this.textColor = textColor;
  }
  /** 逆行シンボル色をセットする。*/
  public void setRevColor(Color revColor) {
    this.revColor = revColor;
  }
  

  /** サインシンボル色をセットする。 */
  public Color getSignColor() {
    return signColor;
  }
  /** 天体シンボル色をセットする。*/
  public Color getBodyColor() {
    return bodyColor;
  }
  /** 数字の色をセットする。*/
  public Color getTextColor() {
    return textColor;
  }
  /** 逆行シンボル色をセットする。*/
  public Color getRevColor() {
    return revColor;
  }
  
  /**
   * 天体オブジェクトをセットする。
   */
  public void setBody(Body body) {
    this.body = body;
  }
  /**
   * 天体オブジェクトを返す。
   */
  public Body getBody() {
    return body;
  }
  protected void draw(double x,double y) {
    if(body == null) return;
    float xofs = 0;
    String [] str = new String[6];
    Color [] colors = { bodyColor,signColor,textColor,textColor,textColor,revColor };
    FontRenderContext frc = g.getFontRenderContext();

    str[0] = BODY_CHARS[body.id] + " ";
    str[1] = "" + ZODIAC_CHARS[body.getSign()];
//    String [] v =  AngleConverter
//      .getSignAngleConstantLength(body.getSignAngle(),2).split("\\.");
    String [] v = AngleConverter.getSignAngle(body.getSignAngle()).split("\\.");
    str[2] = v[0];
    str[3] = ".";
    str[4] = v[1];
    str[5] = (body.lonSpeed < 0) ? "\u00CF" : null;;
    AffineTransform at = new AffineTransform();
    at.translate(x,y);
    Shape boundsShape = at.createTransformedShape(getSize());
    boundsRect = boundsShape.getBounds2D();
    if(bgColor != null) {
      g.setPaint(bgColor);
      g.fill(boundsShape);
    }
    float h = 0;
    for(int i=0; i<str.length; i++) {
      if(str[i] == null) break;
      int fn = fontnums[i];
      TextLayout tl = new TextLayout( str[i], fonts[ fn ], frc );
      if(i==0) h = tl.getAscent();
      at = new AffineTransform();
      at.translate( x + xofs, y + h );
      Shape outline = tl.getOutline(at);
      g.setPaint(colors[i]);
      g.fill(outline);
      xofs += tl.getAdvance();
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

    sizeRect = new Rectangle2D.Float();
    for(int i=0; i<str.length; i++) {
      TextLayout tl = new TextLayout( str[i], fonts[ fontnums[i] ], frc );
      if( i == 0 ) sizeRect.height = tl.getAscent(); //文字の高さは最初の1文字のみで決定
      sizeRect.width += tl.getAdvance();
    }
    float w = sizeRect.width;
    float h = sizeRect.height;
    float b = baseWidth;
    fullRect.width = w + insets[ LEFT ] * b + insets[ RIGHT ] * b;
    fullRect.height = h + insets[ TOP ] * b + insets[ BOTTOM ] * b;     
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
