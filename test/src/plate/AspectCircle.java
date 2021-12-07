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
import java.awt.Font;
import static java.lang.Math.*;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import to.tetramorph.starbase.*;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectStylist;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;

/**
 * �z���X�R�[�v�̃A�X�y�N�g�~��`���̂Ɏg���B
 */
public class AspectCircle {
  /** mode[SHOW]��true�Ȃ�A�X�y�N�g��\���Bfalse�Ȃ��\���B*/
  public static final int SHOW = 0;
  /** mode[TIGHT]��true�Ȃ�^�C�g�A�X�y�N�g��\���Bfalse�Ȃ�^�C�g�A�X�y�N�g��\���B*/
  public static final int TIGHT = 1;
  /** mode[LOOSE]��true�Ȃ烋�[�Y�A�X�y�N�g��\���Bfalse�Ȃ烋�[�Y�A�X�y�N�g��\���B*/
  public static final int LOOSE = 2;
  /** 
   * mode[CATEGORY1]��true�Ȃ��1��A�X�y�N�g�\���Bfalse�Ȃ��\���B
   * 1��Ƃ�CONJUNCTION,SEXTILE,SQUARE,TRINE,OPPOSITION�B
   */
  public static final int CATEGORY1 = 3;
  /** 
   * mode[CATEGORY2]��true�Ȃ��2��A�X�y�N�g�\���Bfalse�Ȃ��\���B
   * 2��Ƃ�SEMI_SEXTILE,SEMI_SQUARE,SESQUIQUADRATE(135),ENCONJUNCT(150)
   */
  public static final int CATEGORY2 = 4;
  /** 
   * mode[CATEGORY3]��true�Ȃ��3��A�X�y�N�g�\���Bfalse�Ȃ��\���B
   * QUINTILE(72),SEMI_QUINTILE(36),BI_QUINTILE(144)
   */
  public static final int CATEGORY3 = 5;
  /** 
   * mode[CATEGORY4]��true�Ȃ����A�X�y�N�g�\���Bfalse�Ȃ��\���B
   * PARALLEL�₻�̑��̃A�X�y�N�g
   */
  public static final int CATEGORY4 = 6;
  double x,y;
  double w;
  double asc;
  double d;
  double radius;
  Graphics2D g;
  List<Aspect> aspects;   //�\������A�X�y�N�g�̃��X�g
  boolean [] mode = new boolean [] { true,true,true,true,true,true,true };       //�`�惂�[�h�ŁA�^�C�g�A���[�Y�A1��A2��Ȃǂ̕\���I���X�C�b�`
  AspectStylist aspectStylist = new AspectStylist(); //���̃X�^�C����J���[�����Ǘ�����
  Body selectedPoint;   //�I�����ꂽ�V��
  double spaceWidth;     //�����̒����ɊJ����u�����N�̒���
  float symbolFontSize;  //�A�X�y�N�g�L���̃t�H���g�T�C�Y
  Font symbolFont;        //�萯�p�t�H���g
  FontRenderContext render; //�t�H���g�`��ɕK�v
  //
  boolean [] visibles;   //�A�X�y�N�g���̕\��/��\����ۊǂ���
  Line2D.Double [] lines; //�A�X�y�N�g���̍��W��ۊǂ���
  int onCursor = -1;     //�I���J�[�\�������A�X�y�N�g���̃I�t�Z�b�g��ۊǂ���
  Shape aspectCircle;    //�A�X�y�N�g�����\�������T�[�N���~��Shape
  double conjRadius;    //�R���W���N�V�����L�����o���ʒu
  Aspect selectedAspect; //�I�����ꂽ�A�X�y�N�g�I�u�W�F�N�g
  Ellipse2D.Double selectedCircle;  //�A�X�y�N�g�����I�����ꂽ�Ƃ��ɶ��وʒu�ɏo������~
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public AspectCircle() {
  }
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }
  double cx,cy,W,dia;
  /** 
   * �`�悷���Ƃ�����W���Z�b�g����B
   * @param cx ���S���Wx
   * @param cy ���S���Wy
   * @param W ��ƂȂ�`��G���A�̕�(�˂ɐ����`�Ƃ݂Ȃ�)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }
  public void setSize(double dia) {
    this.dia = dia;
  }
  public void setFactor(double asc,List<Aspect> aspects,boolean [] mode,Body selectedPoint,double symbolSize) {
    setFactor(g,cx,cy,dia,asc,dia,aspects,mode,selectedPoint,symbolSize);
  }
  /**
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �A�X�y�N�g�~�̒��a 0..1
   * @param aspects �A�X�y�N�g�̃��X�g
   * @param mode �v�f��7��boolean�z��B
   * @param selectedPoint �I�𒆂̊���_�B���ꂪ��null�Ȃ炱�̓V�̂Ƒ��̓V�̂Ƃ̃A�X�y�N�g�݂̂�\���B
   * @param symbolSize ���߸ċL���̻���(0..1)
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double d,List<Aspect> aspects,boolean [] mode,Body selectedPoint,double symbolSize) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.aspects = aspects; this.g = g;
    this.mode = mode;
    this.selectedPoint = selectedPoint;
    radius = d * w / 2d;
    conjRadius = (d - 0.06) * w / 2d;
    spaceWidth = w * 0.04;
    float sz = (float)(symbolSize * w);
    if(symbolFont == null || sz != symbolFontSize) {
      symbolFontSize = sz;
      symbolFont = AstroFont.getFont(symbolFontSize);          
    }    
    render = g.getFontRenderContext();
    visibles = new boolean[aspects.size()];
    for(int i=0; i<visibles.length; i++) visibles[i] = false;
    lines = new Line2D.Double[ aspects.size() ];
    aspectCircle = new Ellipse2D.Double(x-radius,y-radius,d * w,d * w);
  }
  /**
   * �A�X�y�N�g����`�悷��B
   */
  public void draw() {
    if(! mode[SHOW]) return;
    if(onCursor >= 0) {
      g.setPaint(Color.LIGHT_GRAY);
      g.fill(selectedCircle);
    }
    //�\�����Ă���A�X�y�N�g�����ʂ��邽�߂�boolean�z���������(���ׂĔ�\����)
    for(int i=0; i<visibles.length; i++) visibles[i] = false;
    for(int i=0; i<aspects.size(); i++) {
      Aspect aspect = aspects.get(i);
      //�ȉ��ɂÂ��������ŁA�`�悳��Ȃ��A�X�y�N�g�͍��������
      if(aspect.isNoAspect()) continue;
      if(aspect.tight && ! mode[TIGHT]) continue;
      if(! aspect.tight && ! mode[LOOSE]) continue;
      if( ! mode[CATEGORY3]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY3)) continue;
      }
      if( ! mode[CATEGORY2]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY2)) continue;
      }
      if( ! mode[CATEGORY1]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY1)) continue;
      }
      if(selectedPoint != null) {
        if(! aspect.contains(selectedPoint)) continue;
      }
      visibles[i] = true; //�����܂ŗ����Ă����A�X�y�N�g�͕`�悳���
      double c = PI/ 180d;
      double a1 = -(aspect.p1.lon + 180d - asc);
      double cv1 = cos( a1 * c);
      double sv1 = sin( a1 * c);
      double a2 = -(aspect.p2.lon + 180d - asc);
      double cv2 = cos( a2 * c);
      double sv2 = sin( a2 * c);
      double sx,sy,ex,ey,mx,my;
      sx = cv1 * radius + x;
      sy = sv1 * radius + y;
      ex = cv2 * radius + x;
      ey = sv2 * radius + y;
      if(onCursor == i) g.setPaint( Color.RED );
      else g.setPaint( aspectStylist.getColor(aspect) );
      Stroke stroke = (onCursor == i) ? 
        new BasicStroke(2f) : aspectStylist.getStroke(aspect);
      g.setStroke( stroke);
      lines[i] = new Line2D.Double(sx,sy,ex,ey);
      if(selectedPoint != null) {
        // �I������_������Ƃ��̕`��
        double [] p = spacer(sx,sy,ex,ey,spaceWidth);
        //�A�X�y�N�g�L���̃O���t�B�b�N�\���𓾂�
        TextLayout textlayout =
          new TextLayout(""+ASPECT_CHARS[aspect.aid],symbolFont,render);
        //���̃O���t�B�b�N������(����/2)��(��/2)�𓾂�
        float h = textlayout.getAscent()/2f;
        float w = textlayout.getAdvance()/2f;
        AffineTransform at = new AffineTransform();
        if(p != null) {
          Line2D line1 = new Line2D.Double(p[0],p[1],p[2],p[3]);
          Line2D line2 = new Line2D.Double(p[4],p[5],p[6],p[7]);
          g.draw(line1);
          g.draw(line2);
          g.setStroke(stroke);
          //�V���{���̒��S�����_�ɗ���悤�Ɉړ�������
          at.translate(-w+p[8],h+p[9]); //�ړ��O�����̌��_�͍����ɂ��邩��A���ɔ����A��ɔ������������΂悢
          g.fill(textlayout.getOutline(at));
        } else { //���̏ꍇ�ݼެ����
          g.draw(lines[i]);
          Body sp = aspect.getOther(selectedPoint);
          if(sp != null) {
            g.setStroke(stroke);
            double a = -(sp.plot + 180 - asc) * PI / 180d;
            double bx = cos(a) * conjRadius + x;
            double by = sin(a) * conjRadius + y;
            at.translate(-w+bx,h+by); //�ړ��O�����̌��_�͍����ɂ��邩��A���ɔ����A��ɔ������������΂悢
            //Paint paint = g.getPaint();
            //g.setPaint(Color.LIGHT_GRAY);
            //g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            //g.draw(textlayout.getOutline(at));
            g.setStroke(stroke);
            //g.setPaint(paint);
            g.fill(textlayout.getOutline(at));
          }
        }
      } else {
        // �I������_�������Ƃ��̕`��
        //Line2D line = new Line2D.Double(sx,sy,ex,ey);
        g.draw(lines[i]);
      }
    }
  } 
  // apects�̔z�񒆂�aid�����݂����true��Ԃ��B
  private boolean contains(int aid,int [] aspects) {
    for(int i=0; i<aspects.length; i++) {
      if(aspects[i] == aid) return true;
    }
    return false;
  }
  /**
   * ���͂��ꂽ���W�ƁA�`�悳��Ă���A�X�y�N�g�����q�b�g����ꍇ��true��Ԃ��B
   */
  public boolean contains(int x,int y) {
    if(onCursor >= 0) {
      if(selectedCircle.contains(x,y)) return true;
    }
    if(aspects == null) return false;
    for(int i=0; i<aspects.size(); i++) {
      if(visibles[i]) {
        Line2D.Double line = lines[i];
        if(contains(line.x1,line.y1,line.x2,line.y2,x,y)) {
          onCursor = i;
          selectedAspect = aspects.get(onCursor);
          selectedCircle = new Ellipse2D.Double(x-7,y-7,15,15);
          return true;
        }
      }
    }
    if(onCursor >= 0) { //�I���J�[�\�����Ă��̂����ꂽ�u�Ԃ�true�Ƃ݂Ȃ��B
      onCursor = -1;
      return true;
    }
    selectedAspect = null;
    return false;
  }
  /**
   * �_(x,y)�����̃A�X�y�N�g�T�[�N���̒��ɑ��݂���Ȃ�true��Ԃ��B
   */
  public boolean isContainCircle(int x,int y) {
    return aspectCircle.contains(x,y);
  }
  /**
   * �I������Ă���A�X�y�N�g��Ԃ��B�ǂ̃A�X�y�N�g���I������Ă��Ȃ��Ƃ���null
   * ��Ԃ��B
   */
  public Aspect getSelectedAspect() {
    return selectedAspect;
    //if(onCursor < 0) return null;
    //return aspects.get(onCursor);    
  }
  /**
   * �^�񒆂ɃX�y�[�T���J�������̍��W��Ԃ��B
   * ����x1,y1,x2,y2�͐��̍��W�Bsize�͒��S�ɊJ����󔒂̒���(�u�����N���C��)�B
   * �߂�l��10�̍��W�l
   * [0] x1
   * [1] y1
   * [2] x1����ŏ��̃u�����N�n�_x
   * [3] y1����ŏ��̃u�����N�n�_y
   * [4] �u�����N�n�_�̏I�_x
   * [5] �u�����N�n�_�̏I�_y
   * [6] x2
   * [7] y2
   * [8] x1,y1,x2,y2�̒��S�_��x
   * [9] x1,y1,x2,y2�̒��S�_��y
   * �^�񒆂����ݸ�������邱�Ƃ��ł��Ȃ��قǒZ�����̏ꍇ(���̒��� < size)�̂Ƃ�
   * ��null��Ԃ��B
   */
  static double [] spacer(double x1,double y1,double x2,double y2,double size) {
    double [] res = new double[10];
    res[0] = x1; res[1] = y1;
    res[6] = x2; res[7] = y2;
    double dx = Math.abs(x1-x2); //��x
    double dy = Math.abs(y1-y2); //��y
    double len = Math.sqrt(dx * dx + dy * dy); //���̒���
    if(len < size) return null;
    double a = trigon(x1,y1,x2,y2) * Math.PI / 180d; //x1,y1�𒆐S�_�Ƃ������̊p�x
    double cv = Math.cos(a);
    double sv = Math.sin(a);
    double r = (len - size) / 2d; //x1,y1���猩���u�����N���C���̎n�_�܂ł̒���
    res[2] = cv * r + x1;
    res[3] = sv * r + y1;
    r += size; //x1,y1����݂��u�����N���C���I�_�܂ł̒���
    res[4] = cv * r + x1;
    res[5] = sv * r + y1;
    res[8] = min(x1,x2) + dx / 2d;
    res[9] = min(y1,y2) + dy / 2d;
    return res;
  }
  /**
   *���_����w��_�����x�̊p�x�ɂ��邩��Ԃ��B
   *�E���O�x�Ƃ��āA�����v����360�x�܂ł̒l��Ԃ��B
   *���w�A�O�p�֐��ł悭�g������W�n�B
   */
  static double trigon(double x,double y) {
    double a = Math.atan(x/y)*180d/Math.PI;
    if( y<0 ) a -= 180.0;
    if( a<0 ) a += 360.0;
    a = 180 - a -90;
    if(  a<0 ) a += 360;
    return a;
  }
  /**
   *zx,zy�����_�Ƃ��Ax,y�_�Ƃ̑��Ίp�x�����߂�B
   */
  static double trigon(double zx,double zy,double x,double y) {
    x -= zx; y -= zy;
    return trigon(x,y);
  }
  /**
   * ���Ɠ_�̏Փ˔���B
   * �����̒��� L1 = sqrt( (x1-x0)^2 + (y1-y0)^2 )
   * �����̎n�_����_�܂ł̒��� L2 = sqrt( (x2-x0)^2 + (y2-y0)^2 )
   * (x1-x0)*(x2-x0) + (y1-y0)*(y2-y0) �� L1*L2 �ɓ������A����L1��L2�̎��Փ˂��Ă���
   * @param x0,y0,x1,y1 ���̍��W
   * @param x2,y2 �_�̍��W�B
   */
  static boolean contains(double x0,double y0,double x1,double y1,double x2,double y2) {
    double dx1 = x1-x0;
    double dy1 = y1-y0;
    double dx2 = x2 - x0;
    double dy2 = y2 - y0;
    double l1 = Math.sqrt(dx1*dx1 + dy1*dy1 );
    double l2 = Math.sqrt(dx2*dx2 + dy2*dy2 );
    return (Math.abs((dx1*dx2 + dy1*dy2) - l1*l2) <= 2 && l1 >= l2);
  }
}
