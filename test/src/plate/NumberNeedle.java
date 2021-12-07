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
 * �z���X�R�[�v�ɐ��l��`�悷��B
 * @author ���V�`��
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
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �O���̒��a 0..1
   * @param fontSize �����t�H���g�̃T�C�Y(0..1)
   * @param angles �\������p�x
   * @param values �\�����镶����
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
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �O���̒��a 0..1
   * @param fontSize �����t�H���g�̃T�C�Y(0..1)
   * @param angles �\������p�x
   * @param angleStrings �\�����镶����
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
      //������̒��S����ɉ�]
      AffineTransform at = new AffineTransform();
      strShape = textlayout.getOutline(at);
      at.rotate(direction * PI/180f,txw/2f,-txh/2f);
      strShape = at.createTransformedShape(strShape);
      Rectangle2D rect = strShape.getBounds2D();
      //txh = (float)rect.getHeight();
      //txw = (float)rect.getWidth();
      //System.out.println("h = " + txh + ", w = " + txw);
      //��������ړ�
      double hoffset = 0;
      switch( align ) {
        case INNER  : hoffset = radius;          break;
        case CENTER : hoffset = (radius+txw/2f); break;
        case OUTER  : hoffset = (radius+txw);    break;
      }
      AffineTransform at1 = new AffineTransform();
      at1.translate(-hoffset,(float)(voffset * w)); //�����Ɉړ�
      //at1.translate(-hoffset,0);
      strShape = at1.createTransformedShape(strShape);
      //��������z���X�R�[�v�̒��S����ɉ�]
      AffineTransform at3 = new AffineTransform();
      at3.rotate(a * PI/180f,0d,0d);
      strShape = at3.createTransformedShape(strShape);
      //��������L�����o�X�̒��S�Ɉړ�
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
   * ������̐��������ւ̈ړ��I�t�Z�b�g(�}0..1)
   */
  public void setVOffset(double voffset) {
    this.voffset = voffset;
  }
  public void setDirection(double angle) {
    this.direction = angle;
  }
}
