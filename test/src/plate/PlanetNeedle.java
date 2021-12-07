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
import java.awt.Component;
import java.awt.event.MouseEvent;
import static java.lang.Math.*;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.Timer;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;

/**
 * �z���X�R�[�v�̘f���⊴��_��`�悷��B���v�ł����Θf���j�B
 * @author ���V�`��
 */
public class PlanetNeedle implements MouseListener,MouseMotionListener {
  double x,y;
  double w;
  double asc;
  double d;
  double bodySize;
  double radius;
  Graphics2D g;
  Font bodyFont;
  FontRenderContext render;
  List<Body> bodys;
  float bodyFontSize;
  Shape [] shapeBuf;
  int onCursor = -1; //�I���J�[�\�������Ƃ��̃I�t�Z�b�g�ԍ���ۊ�
  Body selectedPoint;
  Aspect selectedAspect;
  Component c;
  PlanetActionListener l;
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public PlanetNeedle() {
  }
  public PlanetNeedle(Component c,PlanetActionListener l) {
    this.c = c;
    this.l = l;
    c.addMouseMotionListener(this);
    c.addMouseListener(this);
  }
  /**
   * �f�����د����ꂽ�Ƃ��̃��X�i��o�^����B���X�i�̍폜��null��set����B
   */
  public void setPlanetActionListener(PlanetActionListener l) {
    this.l = l;
  }
  /**
   * @param x �O���̒��S��x���W
   * @param y �O���̒��S��y���W
   * @param w ��ƂȂ镝(pixcel)
   * @param asc �A�Z���_���g�̊p�x
   * @param d �O���̒��a 0..1
   * @param bodySize �V�̂̕`��T�C�Y 0..1
   * @param bodys �V��
   * @param selectedAspect selected aspects
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double d,double bodySize,List<Body> bodys,Aspect selectedAspect) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.bodys = bodys; this.bodySize = bodySize;
    this.g = g;
    this.selectedAspect = selectedAspect;
    render = g.getFontRenderContext();
    radius = d * w / 2d;
    float sz = (float)(bodySize * w);
    if(bodyFont == null || sz != bodyFontSize) {
      bodyFontSize = sz;
      bodyFont = AstroFont.getFont(bodyFontSize);          
    }
  }

  /**
   * setFactor()�ŗ^����ꂽ�p�����^�[�ɏ]���ĕ`�悷��B
   */
  public void draw() {
    // �V�̃V���{����`��
    //for(Body sp: bodys) {
    shapeBuf = new Shape[bodys.size()];
    for(int i=0; i < bodys.size(); i++) {
      Body sp = bodys.get(i);
      double a = 0;
      if(dragging && sp.id == draggBodyID) {
        a = -(draggAngle + 180 - asc);
      } else
        a = -(sp.plot + 180 - asc);
      
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double bx = cv * radius + x;
      double by = sv * radius + y;
      if(dragging && sp.id == draggBodyID) {
        g.setColor(Color.RED);
        g.draw(new Line2D.Double(x,y,bx,by));
      }
      //�V�̂̃O���t�B�b�N�\���𓾂�
      TextLayout textlayout =
        new TextLayout(""+BODY_CHARS[sp.id],bodyFont,render);
      //���̃O���t�B�b�N������(����/2)��(��/2)�𓾂�
      float h = textlayout.getAscent()/2f;
      float w = textlayout.getAdvance()/2f;
      //�V���{���̒��S�����_�ɗ���悤�Ɉړ�������
      AffineTransform at = new AffineTransform();
      at.translate(-w,h); //�ړ��O�����̌��_�͍����ɂ��邩��A���ɔ����A��ɔ������������΂悢
      Shape signSymbol = textlayout.getOutline(at);
      //�V���{������]
      AffineTransform at3 = new AffineTransform();
      at3.rotate((a+90) * PI/180f);
      signSymbol = at3.createTransformedShape(signSymbol);
      //�V���{�����b�щ~�̏���̏ꏊ�Ɉړ�
      AffineTransform at2 = new AffineTransform();
      at2.translate(bx,by);
      signSymbol = at2.createTransformedShape(signSymbol);
      boolean b = false;
      if(selectedAspect != null) b = selectedAspect.contains(sp);
      if(onCursor == i || b) g.setPaint(Color.RED);      
      else g.setPaint(Color.BLACK);
      //�p���ۂ߂鏈�����w�肵�Ȃ��ƃg�Q�g�Q����яo���Ĕ������Ȃ�
      g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND));
      g.draw(signSymbol); //�������ŃT�C���̗֊s��`���A���ɁE�E�E
      g.setStroke(new BasicStroke(1f)); 
      g.setPaint(Color.WHITE);
      g.fill(signSymbol); //�ׂ����ŃT�C����h��Ԃ��B�������肵���T�C�����`����B
      shapeBuf[i] = signSymbol.getBounds2D(); //�����蔻��p�̃{�b�N�X���쐬
      //textlayout.draw(g,x-w,y+h);
    }
  }
  /**
   * �w�肳�ꂽ���W�ɓV�̂����݂���ꍇ��true��Ԃ��B�������O��R�[�����ꂽ�Ƃ�
   * �w�肵�����W�ɓV�̂����݂��A���񑶍݂��Ȃ��ꍇ��true��Ԃ��B�O��������
   * �w����W�ɓV�̂����݂��Ȃ��Ƃ���false��Ԃ��B�A�E�g�J�[�\�������Ƃ��A�Ăт���
   * ���̃R���|�[�l���g�ł̓��y�C���g�������āA�ԓ_�����������������̂ł��̂悤��
   * �d�g�݂ɂȂ��Ă���Bx,y�ɂ̓}�E�X���[�u�C�x���g��x,y���W���^�����鎖��
   * �O��ɂ��Ă���B
   */
  public boolean contains(int x,int y) {
    if(shapeBuf != null) {
      for(int i=0; i<shapeBuf.length; i++) {
        if(shapeBuf[i].contains((double)x,(double)y)) {
          onCursor = i;
          selectedPoint = bodys.get(i);
          return true;
        }
      }
    }
    if(onCursor >= 0) { //�I���J�[�\�����Ă��̂����ꂽ�u�Ԃ�true�Ƃ݂Ȃ��B
      onCursor = -1;
      return true;
    }
    selectedPoint = null;
    return false;
  }
  /**
   * contains()���\�b�h��true��Ԃ��ꍇ�A���̃��\�b�h�őI�𒆂̓V�̂��擾�ł���B
   * false���Ԃ����ꍇ�́A���̃��\�b�h��null��Ԃ��B
   */
  public Body getSelectedBody() {
    return selectedPoint;
  }
  int clickCount = 0;
  public void mouseClicked(MouseEvent e) {
  }
  Timer clickTimer = null;
  boolean clickCheckTime = false;
  //�}�E�X�{�^���������ꂽ
  public void mousePressed(final MouseEvent e) {
    clickCount++;
    if(contains(e.getX(),e.getY())) {
      draggBodyID = selectedPoint.id;
    }
  }
  //�}�E�X�{�^�������ꂽ
  public void mouseReleased(final MouseEvent e) {
    if(clickCount == 1) {
      clickTimer = new Timer(100,new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if(l != null) {
            if(clickCount>=2)
              l.planetDoubleClicked(selectedPoint);
            else
              l.planetClicked(selectedPoint);
          }
          clickCount = 0;
          clickTimer.stop();
        }
      });
      clickTimer.start();
    }
    dragging = false;
    c.repaint();
  }
  //�}�E�X���h���b�O���ꂽ
  public void mouseDragged(MouseEvent e) {
      dragging = true;
      double zx = e.getX() - this.x;
      double zy = -(e.getY() - this.y);
      draggAngle = trigon(zx,zy);
      c.repaint();
  }
  //�}�E�X���ړ����ꂽ
  public void mouseMoved(MouseEvent e) {
  }
  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }
  public static double trigon(double x,double y) {
    double a = Math.atan(x/y) * 180d / Math.PI;
    if( y<0 ) a -= 180.0;
    if( a<0 ) a += 360.0;
    a = 180 - a -90;
    if(  a<0 ) a += 360;
    return a;
  }
  
  /**
   * zx,zy�����_�Ƃ��Ax,y�_�Ƃ̑��Ίp�x�����߂�B
   * @param zx ���_x
   * @param zy ���_y
   * @param x x���W
   * @param y y���W
   * @return ���[��0�x�Ƃ��Ď��v�܂���360�x�܂ł̊p�x��Ԃ��B
   */
  static double trigon(double zx,double zy,double x,double y) {
    x -= zx; y -= zy;
    double a = Math.atan(x/y) * 180d / Math.PI;
    if( y < 0 ) a -= 180.0;
    if( a < 0 ) a += 360.0;
    a = 180 - a - 90;
    if(  a < 0 ) a += 360;
    return a;
  } 
  int draggBodyID = -1;
  boolean dragging = false;
  double draggAngle = 0;
  
}
