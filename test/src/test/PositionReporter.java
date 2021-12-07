/*
 * PositionReporter.java
 *
 * Created on 2007/06/15, 23:18
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Unit;
/**
 * 天体位置の度数リストをグラフィックで表示する。
 * @author 大澤義鷹
 */
public class PositionReporter {
  List<Body> bodyList = null;
  List<Body> cuspList = null;
  float bodyFontSize = 0;
  Font bodyFont;
  Font font = new Font("Monospaced",Font.PLAIN,10);
  Font dialogFont = new Font("Dialog",Font.PLAIN,10);
  Font timesFont = new Font("Times",Font.PLAIN,10);
  //表示順にならんでいる天体ID
  static final int [] bodys = {
    SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO,AC,MC,DC,IC,
    NODE,TRUE_NODE,APOGEE,OSCU_APOGEE,EARTH,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,
    SOUTH_NODE,ANTI_APOGEE,ANTI_OSCU_APOGEE,VERTEX,ANTI_VERTEX
  };
  
  /**
   * PositionReporter オブジェクトを作成する
   */
  public PositionReporter() {
  }
  /**
   * 天体リストをセットする。
   */
  public void setBodyList(List<Body> bodyList) {
    this.bodyList = bodyList;
  }
  /**
   * ハウスカスプのリストをセットする。
   */
  public void setCuspList(List<Body> cuspList) {
    this.cuspList = cuspList;
  }
  /**
   * 指定された天体をbodyListから探して返す。
   */
  private Body getBody(int id) {
    for(Body b : bodyList)
      if(b.id == id) return b;
    return null;
  }
  
  void drawCusps( Graphics2D g,double x,double y,double width,double height ) {
    if(cuspList == null) return;
    float sz = (float)(width * 0.08);
    bodyFont = AstroFont.getFont(sz);
    Font largeFont = font.deriveFont(sz);
    Font smallFont = font.deriveFont(sz * 0.6f);
    Font commaFont = dialogFont.deriveFont(sz);
    Font houseFont = timesFont.deriveFont(sz);
    TextLayout tl = new TextLayout("XIII",houseFont,g.getFontRenderContext());
    double ypos = 0;
    float [] size = new float[2];
    for(int i=0, hn = 0; i<cuspList.size(); i++) {
      double cuspAngle = cuspList.get(i).getSignAngle();
      int cuspSign = cuspList.get(i).getSign();
      List<TextNode> list = new ArrayList<TextNode>();
      //カスプ番号
      TextNode hnum = new TextNode( HOUSE_NUMBERS[hn] );
      hnum.font = houseFont;
      hnum.advance = tl.getAdvance();
      hnum.align = RIGHT;
      list.add(hnum);
      //カスプサイン
      TextNode sym = new TextNode( " " + ZODIAC_CHARS[cuspSign]);
      sym.font = bodyFont;
      list.add(sym);
      //度数(整数部と小数部にわかれている
      String value = format(cuspAngle,2);
      String [] v = value.split("\\.");
      TextNode angle = new TextNode(v[0]);
      angle.font = largeFont;
      list.add(angle);
      TextNode comma = new TextNode(".");
      comma.font = commaFont;
      list.add(comma);
      TextNode angle2 = new TextNode(v[1]);
      angle2.font = smallFont;
      list.add(angle2);
      drawText(x,y+ypos,list,g,size);
      ypos += size[1]; //ascent;
      hn++;
    }
  }
  
  public void draw( Graphics2D g,double x,double y,double width,double height ) {
    if(bodyList == null || bodyList.size() == 0) return;
    float sz = (float)(width * 0.08);
    bodyFont = AstroFont.getFont(sz);
    Font largeFont = font.deriveFont(sz);
    Font smallFont = font.deriveFont(sz * 0.6f);
    Font commaFont = dialogFont.deriveFont(sz);
    
    double ypos = 0;
    float [] size = new float[2];
    for(int i=0; i < bodys.length; i++) {
      Body body = getBody(bodys[i]);
      if(body == null) continue;
      List<TextNode> list = new ArrayList<TextNode>();
      //惑星とサイン
      TextNode sym = new TextNode( BODY_CHARS[body.id] + " " + ZODIAC_CHARS[body.getSign()]);
      sym.font = bodyFont;
      list.add(sym);
      //度数(整数部と小数部にわかれている
      String value = format(body.getSignAngle(),2);
      String [] v = value.split("\\.");
      TextNode angle = new TextNode(v[0]);
      angle.font = largeFont;
      list.add(angle);
      TextNode comma = new TextNode(".");
      comma.font = commaFont;
      list.add(comma);
      TextNode angle2 = new TextNode(v[1]);
      angle2.font = smallFont;
      list.add(angle2);
      //逆行
      if(body.lonSpeed < 0) {
        TextNode rev = new TextNode("\u00CF");
        rev.font = bodyFont;
        list.add(rev);
      }
      drawText(x,y+ypos,list,g,size);
      ypos += size[1];
    }
    drawCusps( g,x + size[0] * 1.2,y,width,height);
  }
  /**
   * 指定座標にlistの内容を描画する。描画後、ascentには描画された文字の高さ、
   * advanceには文字の幅がセットされる。sizeには描画後の最大サイズ(幅,高)が戻る。
   */
  void drawText(double x,double y,List<TextNode> list,Graphics2D g,float [] size) {
    float xo = 0;
    for(int i=0; i<list.size(); i++) {
      TextNode tn = list.get(i);
      TextLayout tl = new TextLayout(tn.text,tn.font,g.getFontRenderContext());
      float h = tl.getAscent();
      float w = tl.getAdvance();
      if( size[1] < h ) size[1] = h;
      if(tn.align == LEFT) {
        AffineTransform at = new AffineTransform();
        at.translate( x + xo, y );
        g.setPaint(tn.color);
        g.fill(tl.getOutline(at));
        xo += tl.getAdvance();
      } else if(tn.align == RIGHT) {
        AffineTransform at = new AffineTransform();
        at.translate( x + xo, y );
        Shape s = tl.getOutline(at);
        double adv = tn.advance - s.getBounds2D().getWidth();
        AffineTransform at2 = new AffineTransform();
        at2.translate(adv,0);
        g.setPaint(tn.color);
        g.fill(at2.createTransformedShape(s));
        xo += tn.advance; //tl.getAdvance();
      }
    }
    if(size[0]<xo) size[0] = xo;
  }
  
  
  /**
   * 浮動小数を指定桁数で切り捨て、右詰めに整形して返す。
   * @param value 浮動小数
   * @param precision 精度。2を指定するとコンマ二桁まで。
   */
  static String format(double value,int precision) {
    double v = Unit.truncate(value,precision);
    if(v<10) return " " + v;
    return "" + v;
  }
  static final int RIGHT = 1;
  static final int LEFT = 0;
  //テキストをフォント、カラーの属性をつけてラップするクラス
  class TextNode {
    Font font;
    Color color = Color.BLACK;
    String text;
    float advance = 0; //最小幅を指定できる。
    int align = 0;
    TextNode(String text) {
      this.text = text;
    }
  }
}
