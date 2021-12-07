/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import static java.lang.Math.*;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * ホロスコープに数値(たとえば天体のそばでその度数やハウス番号)を
 * 表示する際に使用する。
 * @author 大澤義鷹
 */
public class NumberNeedle extends ChartParts {
  float symbolSize;
  Font font = new Font("Monospaced",Font.PLAIN,10);
  float fontSize;
  double [] angles;
  String [] angleStrings;
  int align;
  double voffset;
  double direction;
  public static final int INNER = 0;
  public static final int CENTER = 1;
  public static final int OUTER = 2;
  double ringWidth;
  Color color = Color.BLACK;
  /**
   * 文字針オブジェクトを作成する。
   */
  public NumberNeedle() {
  }
  /**
   * 文字針オブジェクトを作成する。
   * @param bp BasePositionオブジェクト
   */
  public NumberNeedle(BasePosition bp) {
    super(bp);
  }
  /**
   * 描画する位置(角度)と、表示する値をセットする。
   * @param angles 表示する角度
   * @param angleStrings 表示する文字列
   */
  public void setAngleWithValues(double [] angles, String [] angleStrings ) {
    this.angles = angles;
    this.angleStrings = angleStrings;
  }
  /**
   * カスプ表示はsetAngleWithValues()の変わりにこっちを使うと簡単。
   */
  public void setCusps(double [] cusps) {
    this.angles = cusps;
    angleStrings = new String[cusps.length];
    for(int i=0; i<cusps.length; i++) {
      int a = (int)(cusps[i] % 30);
      angleStrings[i] = a < 10 ? " " + a : "" + a;
    }
  }
//  /**
//   * 文字のサイズを指定する。
//   * @param fontSize 0〜1の値。
//   */
//  public void setSymbolSize(double fontSize) {
//    float sz = (float)(fontSize * bp.w);
//    if(font == null || sz != this.fontSize) {
//      this.fontSize = sz;
//      font = AstroFont.getFont(this.fontSize);
//    }
//  }
  public void setSymbolSize(float symbolSize) {
    this.symbolSize = symbolSize;
  }
  /**
   * このオブジェクトにセットされているパラメターにしたがって数字針を描画する。
   */
  public void draw() {
    float sz = (float)(symbolSize * bp.w);
    if(sz != fontSize) {
      fontSize = sz;
      font = font.deriveFont(fontSize);
    }
    FontRenderContext render = bp.g.getFontRenderContext();
    double radius = diameter * bp.w / 2d;

    AffineTransform roat = new AffineTransform(); //回転用
    AffineTransform mvat = new AffineTransform(); //平行移動用

    for(int i=0; i<angles.length; i++) {
      double angle = angles[i];
      double a = -(angle - (roll + ascendant));
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double bx = cv * radius + bp.x;
      double by = sv * radius + bp.y;
      TextLayout textlayout =
        new TextLayout(angleStrings[i],font,render);
      float txh = textlayout.getAscent();
      float txw = textlayout.getAdvance();
      Shape strShape = null;
      //文字列の中心を基準に回転
      roat.setToRotation( direction * PI/180f );
      strShape = textlayout.getOutline(roat);
      mvat.setToTranslation(txw/2f, -txh/2f);
      strShape = roat.createTransformedShape(strShape);

      //文字列を移動
      double hoffset = 0;
      switch( align ) {
        case INNER  : hoffset = radius;          break;
        case CENTER : hoffset = (radius+txw/2f); break;
        case OUTER  : hoffset = (radius+txw);    break;
      }
      mvat.setToTranslation(-hoffset,(float)(voffset * bp.w)); //左横に移動
      strShape = mvat.createTransformedShape(strShape);

      //文字列をホロスコープの中心を基準に回転
      roat.setToRotation(a * PI/180f,0d,0d);
      strShape = roat.createTransformedShape(strShape);

      //文字列をキャンバスの中心に移動
      mvat.setToTranslation(bp.x,bp.y);
      strShape = mvat.createTransformedShape(strShape);

      bp.g.setPaint( color );
      bp.g.fill(strShape);
    }
  }
  /**
   * 配置位置をセットする。このクラスのフィールド定数INNER,OUTER,CENTERのいずれか。
   */
  public void setAlign(int align) {
    this.align = align;
  }
  /**
   * 文字列の垂直方向への移動オフセット(±0..1)
   */
  public void setVOffset(double voffset) {
    this.voffset = voffset;
  }
  /**
   * 文字の向きをセットする。
   */
  public void setDirection(double angle) {
    this.direction = angle;
  }
  public void setColor( Color color ) {
    this.color = color;
  }
}
