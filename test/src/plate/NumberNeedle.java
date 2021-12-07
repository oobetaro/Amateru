/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import java.awt.Color;
import static java.lang.Math.*;
import to.tetramorph.starbase.*;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープに数値を描画する。
 * @author 大澤義鷹
 */
public class NumberNeedle {
  double x,y;
  double w;
  double asc;
  double d;
  double radius;
  Graphics2D g;
  Font font;
  FontRenderContext render;
  float fontSize;
  double [] angles;
  String [] angleStrings;
  int align;
  double voffset;
  double direction;
  //List<Angle> angleList;
  public static final int INNER = 0;
  public static final int CENTER = 1;
  public static final int OUTER = 2;
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public NumberNeedle() {
  }
//  public void setGraphics(Graphics2D g) {
//    this.g = g;
//    render = g.getFontRenderContext();
//  }
  /**
   * @param x 軌道の中心のx座標
   * @param y 軌道の中心のy座標
   * @param w 基準となる幅(pixcel)
   * @param asc アセンダントの角度
   * @param d 軌道の直径 0..1
   * @param fontSize 文字フォントのサイズ(0..1)
   * @param angles 表示する角度
   * @param values 表示する文字列
   */
//  public void setFactor(double x,double y,double w,double asc,double d,
//    double fontSize,double [] angles,String [] values) {
//    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
//    this.angles = angles; this.values = values;
//    radius = d * w / 2d;
//    float sz = (float)(fontSize * w);
//    if(font == null || sz != this.fontSize) {
//      this.fontSize = sz;
//      font = AstroFont.getFont(this.fontSize);          
//    }
//  }
  /**
   * @param x 軌道の中心のx座標
   * @param y 軌道の中心のy座標
   * @param w 基準となる幅(pixcel)
   * @param asc アセンダントの角度
   * @param d 軌道の直径 0..1
   * @param fontSize 文字フォントのサイズ(0..1)
   * @param angles 表示する角度
   * @param angleStrings 表示する文字列
   */

  public void setFactor(Graphics2D g,double x,double y,double w,double asc,double d,
    double fontSize,double [] angles,String [] angleStrings ) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.angles = angles; this.angleStrings = angleStrings;
    this.g = g;
    render = g.getFontRenderContext();
    radius = d * w / 2d;
    float sz = (float)(fontSize * w);
    if(font == null || sz != this.fontSize) {
      this.fontSize = sz;
      font = AstroFont.getFont(this.fontSize);          
    }
  }
  
  public void draw() {
    for(int i=0; i<angles.length; i++) {
    //for(int i=0; i<1; i++) {
      double angle = angles[i];
      double a = -(angle - asc);
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double bx = cv * radius + x;
      double by = sv * radius + y;
      TextLayout textlayout =
        new TextLayout(angleStrings[i],font,render);
      float txh = textlayout.getAscent();
      float txw = textlayout.getAdvance();
      Shape strShape = null;
      //文字列の中心を基準に回転
      AffineTransform at = new AffineTransform();
      strShape = textlayout.getOutline(at);
      at.rotate(direction * PI/180f,txw/2f,-txh/2f);
      strShape = at.createTransformedShape(strShape);
      Rectangle2D rect = strShape.getBounds2D();
      //txh = (float)rect.getHeight();
      //txw = (float)rect.getWidth();
      //System.out.println("h = " + txh + ", w = " + txw);
      //文字列を移動
      double hoffset = 0;
      switch( align ) {
        case INNER  : hoffset = radius;          break;
        case CENTER : hoffset = (radius+txw/2f); break;
        case OUTER  : hoffset = (radius+txw);    break;
      }
      AffineTransform at1 = new AffineTransform();
      at1.translate(-hoffset,(float)(voffset * w)); //左横に移動
      //at1.translate(-hoffset,0);
      strShape = at1.createTransformedShape(strShape);
      //文字列をホロスコープの中心を基準に回転
      AffineTransform at3 = new AffineTransform();
      at3.rotate(a * PI/180f,0d,0d);
      strShape = at3.createTransformedShape(strShape);
      //文字列をキャンバスの中心に移動
      AffineTransform at2 = new AffineTransform();
      at2.translate(x,y);
      strShape = at2.createTransformedShape(strShape);
      g.setPaint(Color.BLACK);
      g.fill(strShape);
    }
  }
  public void setAlign(int align) {
    this.align = align;
  }
  /**
   * 文字列の垂直方向への移動オフセット(±0..1)
   */
  public void setVOffset(double voffset) {
    this.voffset = voffset;
  }
  public void setDirection(double angle) {
    this.direction = angle;
  }
}
