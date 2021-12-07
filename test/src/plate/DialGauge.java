/*
 * DialGauge.java
 *
 * Created on 2006/10/29, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;
import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import to.tetramorph.starbase.*;

/**
 * �z���X�R�[�v�̕��i�ŁA�ڐ���̃����O��`�悷��B
 * �ŏI�I�ɂ͕`��F��l�X�ȏ������v���p�e�B�Ŏ󂯎��
 * @author ���V�`��
 */
public class DialGauge {
  /**
   * �e�T�C����0�x�̖ڐ����`�悵�Ȃ��BsetOption���\�b�h�Ŏw�肷������B
   */
  public static final int NOT_DRAW_ZERO_DEGREES = 1;
  int option = 0;
  double radius;
  double w;
  double asc;
  double x,y;
  Graphics2D g;

  /**
   * Creates a new instance of DialGauge
   */
  public DialGauge() {
  }
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,double radius) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.radius = radius;
    this.g = g;
  }
  /**
   *
   */
  public void draw() {
    GeneralPath path = new GeneralPath();
    double r0 = (radius * w) / 2;         //���~ �������a
    double r1 = ((radius + 0.008) * w) / 2; //
    double r2 = ((radius + 0.02) * w) / 2;
    double r3 = ((radius + 0.012) * w) / 2;
    for(int a=0; a<360; a++) {
      double a0 = -(a + 180d - asc);
      double cv = cos(a0 * PI / 180d);
      double sv = sin(a0 * PI / 180d);
      float x0 = (float)(cv * r0 + x);
      float y0 = (float)(sv * r0 + y);
      double r = 0;
      if(a % 10 == 0) r = r2;
      else if( a % 5 == 0) r = r3;
      else r = r1;
      float x1 = (float)(cv * r + x);
      float y1 = (float)(sv * r + y);
      if(a % 30 == 0 && ((option & NOT_DRAW_ZERO_DEGREES) != 0)) continue;
      if(w < 350 && r == r1) continue;
      path.moveTo(x0,y0);
      path.lineTo(x1,y1);  
    }
    path.closePath();
    g.setPaint(Color.BLACK);
    g.draw(path);
  }
  /**
   * �`��I�v�V�����̐ݒ�BNOT_DRAW_ZERO_DEGREES���̃t�B�[���h�萔���w��B
   */
  public void setOption(int option) {
    this.option = option;
  }

}
