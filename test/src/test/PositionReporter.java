/*
 * PositionReporter.java
 *
 * Created on 2007/06/15, 23:18
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Unit;
/**
 * �V�̈ʒu�̓x�����X�g���O���t�B�b�N�ŕ\������B
 * @author ���V�`��
 */
public class PositionReporter {
  List<Body> bodyList = null;
  List<Body> cuspList = null;
  float bodyFontSize = 0;
  Font bodyFont;
  Font font = new Font("Monospaced",Font.PLAIN,10);
  Font dialogFont = new Font("Dialog",Font.PLAIN,10);
  Font timesFont = new Font("Times",Font.PLAIN,10);
  //�\�����ɂȂ��ł���V��ID
  static final int [] bodys = {
    SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO,AC,MC,DC,IC,
    NODE,TRUE_NODE,APOGEE,OSCU_APOGEE,EARTH,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,
    SOUTH_NODE,ANTI_APOGEE,ANTI_OSCU_APOGEE,VERTEX,ANTI_VERTEX
  };
  
  /**
   * PositionReporter �I�u�W�F�N�g���쐬����
   */
  public PositionReporter() {
  }
  /**
   * �V�̃��X�g���Z�b�g����B
   */
  public void setBodyList(List<Body> bodyList) {
    this.bodyList = bodyList;
  }
  /**
   * �n�E�X�J�X�v�̃��X�g���Z�b�g����B
   */
  public void setCuspList(List<Body> cuspList) {
    this.cuspList = cuspList;
  }
  /**
   * �w�肳�ꂽ�V�̂�bodyList����T���ĕԂ��B
   */
  private Body getBody(int id) {
    for(Body b : bodyList)
      if(b.id == id) return b;
    return null;
  }
  
  void drawCusps( Graphics2D g,double x,double y,double width,double height ) {
    if(cuspList == null) return;
    float sz = (float)(width * 0.08);
    bodyFont = AstroFont.getFont(sz);
    Font largeFont = font.deriveFont(sz);
    Font smallFont = font.deriveFont(sz * 0.6f);
    Font commaFont = dialogFont.deriveFont(sz);
    Font houseFont = timesFont.deriveFont(sz);
    TextLayout tl = new TextLayout("XIII",houseFont,g.getFontRenderContext());
    double ypos = 0;
    float [] size = new float[2];
    for(int i=0, hn = 0; i<cuspList.size(); i++) {
      double cuspAngle = cuspList.get(i).getSignAngle();
      int cuspSign = cuspList.get(i).getSign();
      List<TextNode> list = new ArrayList<TextNode>();
      //�J�X�v�ԍ�
      TextNode hnum = new TextNode( HOUSE_NUMBERS[hn] );
      hnum.font = houseFont;
      hnum.advance = tl.getAdvance();
      hnum.align = RIGHT;
      list.add(hnum);
      //�J�X�v�T�C��
      TextNode sym = new TextNode( " " + ZODIAC_CHARS[cuspSign]);
      sym.font = bodyFont;
      list.add(sym);
      //�x��(�������Ə������ɂ킩��Ă���
      String value = format(cuspAngle,2);
      String [] v = value.split("\\.");
      TextNode angle = new TextNode(v[0]);
      angle.font = largeFont;
      list.add(angle);
      TextNode comma = new TextNode(".");
      comma.font = commaFont;
      list.add(comma);
      TextNode angle2 = new TextNode(v[1]);
      angle2.font = smallFont;
      list.add(angle2);
      drawText(x,y+ypos,list,g,size);
      ypos += size[1]; //ascent;
      hn++;
    }
  }
  
  public void draw( Graphics2D g,double x,double y,double width,double height ) {
    if(bodyList == null || bodyList.size() == 0) return;
    float sz = (float)(width * 0.08);
    bodyFont = AstroFont.getFont(sz);
    Font largeFont = font.deriveFont(sz);
    Font smallFont = font.deriveFont(sz * 0.6f);
    Font commaFont = dialogFont.deriveFont(sz);
    
    double ypos = 0;
    float [] size = new float[2];
    for(int i=0; i < bodys.length; i++) {
      Body body = getBody(bodys[i]);
      if(body == null) continue;
      List<TextNode> list = new ArrayList<TextNode>();
      //�f���ƃT�C��
      TextNode sym = new TextNode( BODY_CHARS[body.id] + " " + ZODIAC_CHARS[body.getSign()]);
      sym.font = bodyFont;
      list.add(sym);
      //�x��(�������Ə������ɂ킩��Ă���
      String value = format(body.getSignAngle(),2);
      String [] v = value.split("\\.");
      TextNode angle = new TextNode(v[0]);
      angle.font = largeFont;
      list.add(angle);
      TextNode comma = new TextNode(".");
      comma.font = commaFont;
      list.add(comma);
      TextNode angle2 = new TextNode(v[1]);
      angle2.font = smallFont;
      list.add(angle2);
      //�t�s
      if(body.lonSpeed < 0) {
        TextNode rev = new TextNode("\u00CF");
        rev.font = bodyFont;
        list.add(rev);
      }
      drawText(x,y+ypos,list,g,size);
      ypos += size[1];
    }
    drawCusps( g,x + size[0] * 1.2,y,width,height);
  }
  /**
   * �w����W��list�̓��e��`�悷��B�`���Aascent�ɂ͕`�悳�ꂽ�����̍����A
   * advance�ɂ͕����̕����Z�b�g�����Bsize�ɂ͕`���̍ő�T�C�Y(��,��)���߂�B
   */
  void drawText(double x,double y,List<TextNode> list,Graphics2D g,float [] size) {
    float xo = 0;
    for(int i=0; i<list.size(); i++) {
      TextNode tn = list.get(i);
      TextLayout tl = new TextLayout(tn.text,tn.font,g.getFontRenderContext());
      float h = tl.getAscent();
      float w = tl.getAdvance();
      if( size[1] < h ) size[1] = h;
      if(tn.align == LEFT) {
        AffineTransform at = new AffineTransform();
        at.translate( x + xo, y );
        g.setPaint(tn.color);
        g.fill(tl.getOutline(at));
        xo += tl.getAdvance();
      } else if(tn.align == RIGHT) {
        AffineTransform at = new AffineTransform();
        at.translate( x + xo, y );
        Shape s = tl.getOutline(at);
        double adv = tn.advance - s.getBounds2D().getWidth();
        AffineTransform at2 = new AffineTransform();
        at2.translate(adv,0);
        g.setPaint(tn.color);
        g.fill(at2.createTransformedShape(s));
        xo += tn.advance; //tl.getAdvance();
      }
    }
    if(size[0]<xo) size[0] = xo;
  }
  
  
  /**
   * �����������w�茅���Ő؂�̂āA�E�l�߂ɐ��`���ĕԂ��B
   * @param value ��������
   * @param precision ���x�B2���w�肷��ƃR���}�񌅂܂ŁB
   */
  static String format(double value,int precision) {
    double v = Unit.truncate(value,precision);
    if(v<10) return " " + v;
    return "" + v;
  }
  static final int RIGHT = 1;
  static final int LEFT = 0;
  //�e�L�X�g���t�H���g�A�J���[�̑��������ă��b�v����N���X
  class TextNode {
    Font font;
    Color color = Color.BLACK;
    String text;
    float advance = 0; //�ŏ������w��ł���B
    int align = 0;
    TextNode(String text) {
      this.text = text;
    }
  }
}
