/*
 * SignDial.java
 *
 * Created on 2006/10/30, 2:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import static java.lang.Math.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import static java.awt.Color.*;
import to.tetramorph.starbase.*;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープの部品で星座シンボルを描くクラス。
 * @author 大澤義鷹
 */
public class SignDial {
//  double li;
//  double lo;
  double w;
  double asc;
  double x,y;
  double radius;
  double d;
  Graphics2D g;
  Font signFont;
  FontRenderContext render;
  float signFontSize;
  public Color [] symbolColors = { WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,
    WHITE,WHITE,WHITE,WHITE };
  public Color [] borderColors = { BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,
    BLACK,BLACK,BLACK,BLACK };
  public boolean isNoSymbolBorders = false;
  /** Creates a new instance of SignDial */
  public SignDial() {
  }
//  public void setGraphics(Graphics2D g) {
//    this.g = g;
//    render = g.getFontRenderContext();
//  }
//  public void setFactor(double x,double y,double w,double asc,double li,double lo) {
//    this.x = x; this.y = y; this.w = w; this.asc = asc; this.li = li; this.lo = lo;
//    float zw = (float)((lo - li) * w / 2d * 0.7);
//    signFont = AstroFont.getFont(zw);
//    radius = ((lo - li)/2d + li) * w / 2d;
//  }
  /**
   * 描画条件をセットする
   * @param x 中心座標x
   * @param y 中心座標y
   * @param w 基準幅(pixcel)
   * @param asc アセンダントの位置
   * @param d 直径(0..1)
   * @param sd サインのシンボルの直径(0..1)
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,double d,float sd) {
    this.x = x; this.y = y; this.w = w; this.asc = asc;
    this.radius = d * w / 2d;
    this.d = d;
    this.g = g;
    render = g.getFontRenderContext();

    float sz = (float)(sd * w);
    if(signFont == null || sz != signFontSize) {
      signFontSize = sz;
      signFont = AstroFont.getFont(signFontSize);
    }
  }
  public void draw() {
    // 星座シンボルを描く
    for(int i=0; i<12; i++) {
      double a = -(i * 30 + 15 + 180 - asc);
      double cv = cos(a * PI/180d);
      double sv = sin(a * PI/180d);
      float sx = (float)(cv * radius + x);
      float sy = (float)(sv * radius + y);
      //サイン文字のグラフィック表現を得る
      TextLayout textlayout =
        new TextLayout(""+ZODIAC_CHARS[i],signFont,render);
      //そのグラフィック文字の(高さ/2)と(幅/2)を得る
      float h = textlayout.getAscent()/2f;
      float w = textlayout.getAdvance()/2f;
      //シンボルの中心が原点に来るように移動させる
      AffineTransform at = new AffineTransform();
      at.translate(-w,h); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
      Shape signSymbol = textlayout.getOutline(at);
      //シンボルを回転
      AffineTransform at3 = new AffineTransform();
      at3.rotate((a+90) * PI/180f);
      signSymbol = at3.createTransformedShape(signSymbol);
      //シンボルを獣帯円の所定の場所に移動
      AffineTransform at2 = new AffineTransform();
      at2.translate(sx,sy);
      signSymbol = at2.createTransformedShape(signSymbol);
      if(! isNoSymbolBorders) {
        g.setPaint(borderColors[i]);
        //角を丸める処理を指定しないとトゲトゲが飛び出して美しくない
        g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_ROUND));
        g.draw(signSymbol); //太い線でサインの輪郭を描き、次に・・・
      }
      g.setStroke(new BasicStroke(1f));
      g.setPaint(symbolColors[i]);
      g.fill(signSymbol); //細い線でサインを塗りつぶす。くっきりしたサインが描ける。
    }    
  }
}
