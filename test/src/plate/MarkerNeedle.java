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
 * �z���X�R�[�v�̊���_�̈ʒu�𖾊m�Ɏw�������}�[�J�[��`�悷��B
 * �X�P�[���̖ڐ�����w�����߂��o�[( | )�B
 * @author ���V�`��
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
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �O���̒��a 0..1
   *
   * @param bodys �V��
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,double d,List<Body> bodys) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.bodys = bodys; this.g = g;
    radius = d * w / 2d;
    sz = (float)(size * w);
  }
  public void draw() {
    // �V�̃V���{����`��
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
        //if(angleAbs(sp.lon,sp.plot) > 4d) { //�g������lon��plot��4�x�ȏ㗣��Ă���Ƃ��݈̂���
        double a2 = -(sp.plot + 180 - asc);
        sx = cos( a2 * PI / 180d) * extensionRadius + x;
        sy = sin( a2 * PI / 180d) * extensionRadius + y;
        Line2D line2 = new Line2D.Double(ex,ey,sx,sy);
        g.draw(line2);
        //}
      }
    }
  }
  // ��̊p�x��
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
   * �}�[�J�[�����w�蒼�a������ɐL�΂����O���ɂ̂΂����B
   * INNER���w�肷��Ɠ��AOUTER���w�肷��ƊO�ACENTER���w�肷��Ɠ��ƊO�ɐL�΂��B
   */
  public void setDirection(int direction) {
    this.direction = direction;
  }
  /**
   * �}�[�J�[�ƓV�̂����Ԑ����ǉ�����
   * @param diameter true�Ȃ�g�����������Bfalse�Ȃ�����Ȃ��B�f�t�H���g��false�B
   */
  public void setExtension(double diameter) {
    this.extensionDiameter = diameter;
  }
}
