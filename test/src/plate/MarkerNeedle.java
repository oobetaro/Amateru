/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import java.awt.BasicStroke;
import java.awt.Color;
import static java.lang.Math.*;
import to.tetramorph.starbase.*;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.List;
import to.tetramorph.starbase.lib.Body;

/**
 * ホロスコープの感受点の位置を明確に指し示すマーカーを描画する。
 * スケールの目盛りを指ししめすバー( | )。
 * @author 大澤義鷹
 */
public class MarkerNeedle {
  public static final int INNER = 0;
  public static final int CENTER = 1;
  public static final int OUTER = 2;
  double x,y;
  double w;
  double asc;
  double d;
  double size = 0.008;
  double radius;
  Graphics2D g;
  List<Body> bodys;
  int type;
  float sz;
  int direction;
  double extensionDiameter;
  double extensionRadius;
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public MarkerNeedle() {
  }
  /**
   * @param x 軌道の中心のx座標
   * @param y 軌道の中心のy座標
   * @param w 基準となる幅(pixcel)
   * @param asc アセンダントの角度
   * @param d 軌道の直径 0..1
   *
   * @param bodys 天体
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,double d,List<Body> bodys) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.bodys = bodys; this.g = g;
    radius = d * w / 2d;
    sz = (float)(size * w);
  }
  public void draw() {
    // 天体シンボルを描く
    extensionRadius = extensionDiameter * w / 2d;

    for(Body sp: bodys) {
      double a = -(sp.lon + 180 - asc);
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double sx,sy,ex,ey;
      if(direction == INNER) {
        sx = cv * radius;
        sy = sv * radius;
        ex = cv * (radius - sz);
        ey = sv * (radius - sz);
      } else if(direction == OUTER) {
        sx = cv * radius;
        sy = sv * radius;
        ex = cv * (radius + sz);
        ey = sv * (radius + sz);        
      } else {
        double l = sz / 2d;
        sx = cv * (radius - l);
        sy = sv * (radius - l);
        ex = cv * (radius + l);
        ey = sv * (radius + l);
      }
      sx += x; sy += y; ex += x; ey += y;
      g.setPaint(Color.BLACK);
      g.setStroke(new BasicStroke(1f)); 
      Line2D line = new Line2D.Double(sx,sy,ex,ey);
      g.draw(line);
      if(extensionDiameter > 0) {
        //if(angleAbs(sp.lon,sp.plot) > 4d) { //拡張線はlonとplotが4度以上離れているときのみ引く
        double a2 = -(sp.plot + 180 - asc);
        sx = cos( a2 * PI / 180d) * extensionRadius + x;
        sy = sin( a2 * PI / 180d) * extensionRadius + y;
        Line2D line2 = new Line2D.Double(ex,ey,sx,sy);
        g.draw(line2);
        //}
      }
    }
  }
  // 二つの角度の
  private double angleAbs(double a1,double a2) {
    double angle = abs(a1-a2);
    if(angle >= 180d) angle = 360d - angle;
    return angle;
  }
  public static final int LINE = 0;
  public static final int CIRCLE = 1;
  
  public void setType(int type) {
    this.type = type;
  }
  public void setSize(double size) {
    this.size = size;
  }
  /**
   * マーカー線を指定直径から内に伸ばすか外側にのばすか。
   * INNERを指定すると内、OUTERを指定すると外、CENTERを指定すると内と外に伸ばす。
   */
  public void setDirection(int direction) {
    this.direction = direction;
  }
  /**
   * マーカーと天体を結ぶ線も追加する
   * @param diameter trueなら拡張線を引く。falseなら引かない。デフォルトはfalse。
   */
  public void setExtension(double diameter) {
    this.extensionDiameter = diameter;
  }
}
