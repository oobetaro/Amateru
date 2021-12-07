/*
 * HouseNumberDial.java
 *
 * Created on 2006/11/03, 2:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import static java.lang.Math.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.util.AstroFont;
/**
 * �n�E�X�ԍ���\������B
 * �J�X�v�ƃJ�X�v�̒��Ԃ̈ʒu(�p�x)�ɁA�n�E�X�ԍ���`���B
 * @author ���V�`��
 */
public class HouseNumberDial {

  double [] cusps;
  double [] houses;
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
  String [] values;
//  public static final int INNER = 0;
//  public static final int CENTER = 1;
//  public static final int OUTER = 2;
  public Color numberColor = Color.BLACK;
  /**
   * Creates a new instance of HouseNumberDial
   */
  public HouseNumberDial() {
  }

  /**
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �O���̒��a 0..1
   * @param fontSize �����t�H���g�̃T�C�Y(0..1)
   * @param cusps �n�E�X�J�X�v
   *
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double d,double fontSize,double [] cusps) {
    this.cusps = cusps;
    this.g = g;
    render = g.getFontRenderContext();
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    radius = d * w / 2d;
    float sz = (float)(fontSize * w);
    if(font == null || sz != this.fontSize) {
      this.fontSize = sz;
      font = AstroFont.getFont(this.fontSize);          
    }
    houses = new double[cusps.length];
    for(int i=0; i<cusps.length; i++) {
      double nextCusp = (i == (cusps.length - 1)) ? cusps[0] : cusps[i+1];
      double arcAngle = (nextCusp > cusps[i]) ? 
        abs(nextCusp - cusps[i]) : abs(360 + nextCusp - cusps[i]);
      houses[i] = cusps[i] + arcAngle/2d;
    }
  }
  public void draw() {
    for(int i=0; i<houses.length; i++) {
      double angle = houses[i];
      //NumberNeedle�N���X�ł�+180���Ă��Ȃ����A���̃N���X�ł͂��Ă���B
      //�Ȃ�ł��Ƃ�����sin,cos�ŉ�]�����邩�Arotate���\�b�h�ŉ�]�����邩�̈Ⴂ�ŁA
      //+180����̂�sin,cos�̌v�Z�̓s����̂��ƁB
      double a = -(angle + 180 - asc);
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double bx = cv * radius + x;
      double by = sv * radius + y;
      TextLayout textlayout = 
        new TextLayout( Const.HOUSE_NUMBERS[i], font, render);
      float txh = textlayout.getAscent();
      float txw = textlayout.getAdvance();
      Shape strShape = null;
      //������̒��S����ɉ�]
      AffineTransform at = new AffineTransform();
      strShape = textlayout.getOutline(at);
      at.rotate((a+90) * PI/180f,txw/2f,-txh/2f);
      strShape = at.createTransformedShape(strShape);
      AffineTransform at2 = new AffineTransform();
      at2.translate(bx-txw/2f,by+txh/2f);
      strShape = at2.createTransformedShape(strShape);
      g.setPaint(numberColor);
      g.fill(strShape);
    }
  }
  
}
