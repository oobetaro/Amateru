/*
 * HouseRing.java
 *
 * Created on 2006/10/28, 18:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import java.awt.Color;
import static java.lang.Math.*;
import java.awt.Graphics2D;
import to.tetramorph.starbase.*;

/**
 * �z���X�R�[�v�̗ւ�\������N���X�B�n�E�X��T�C���̐�`�őg�܂ꂽ�ԗւ�`���B
 * @author ���V�`��
 */
public class HouseRing {
  public static final int OUTER_ARC = 1;
  public static final int INNER_ARC = 2;
  public static final int SEPARATOR = 4;
  public static final int FILL = 8;
  protected int condition = OUTER_ARC | INNER_ARC | SEPARATOR;
  public static final double [] ZODIAC_ANGLES =
    { 0d,30d,60d,90d,120d,150d,180d,210d,240d,270d,300d,330d };
  double x,y;
  double w;
  double ri;
  double ro;
  double [] cusps;
  double asc;
  public Sector [] sectors;
  Graphics2D g;
  public Color [] bgColors;
  public Color innerLineColor = Color.BLACK;
  public Color outerLineColor = Color.BLACK;;
  public Color separatorLineColor = Color.BLACK;;
  public boolean isNoBorder = false;
  public boolean isNoBackground = false;
  /** 
   * �����O�����ݒ�̋�̃I�u�W�F�N�g���쐬����B
   */
  public HouseRing() {
  }

  /**
   * �z���X�R�[�v�p�̕������ꂽ�����O��p�ӂ���B
   * �����O�̓L���b�V������āA�O��Ɠ����p�����^�[�Ȃ�L���b�V������Ă�����̂��g���B
   * @param x �����O�̒��S�ɂȂ�x���W
   * @param y �����O�̒��S�ɂȂ�y���W
   * @param w �����O�̍ő啝(pixcel)
   * @param ri �����O�̓��~(w�ɑ΂���p�[�Z���e�[�W�Ŏw�肵0�`1�̒l�����)
   * @param ro �����O�̊O�~(w�ɑ΂���p�[�Z���e�[�W�Ŏw�肵0�`1�̒l�����)
   * @param cusps �J�X�v�̃��X�g
   * @param asc �����O�̉�]�p�B�b�тȂ�ʏ�̓A�Z���_���g�̒l���Z�b�g����B
   */
  public Sector [] getRing(double x,double y,double w,double ri,double ro,
    double [] cusps,double asc) {
    boolean changed = false;
    if(sectors != null) {
      if(this.x == x && this.y == y && this.w == w && this.ri == ri 
         && this.ro == ro && this.asc == asc) {
        for(int i=0; i<cusps.length; i++) {
          if(this.cusps[i] != cusps[i]) { changed = true; break; } 
        }
      } else changed = true;
    } changed = true;
    if(changed) {
      this.x = x; this.y = y; this.w = w; this.ri = ri; this.ro = ro;
      this.cusps = cusps; this.asc = asc;
      sectors = Sector.getRing(x,y,w,ri,ro,cusps,asc);
    }
    return sectors;
  }
  /**
   * �z���X�R�[�v�p�̕������ꂽ�����O�̃p�����^�[���Z�b�g����
   * @param x �����O�̒��S�ɂȂ�x���W
   * @param y �����O�̒��S�ɂȂ�y���W
   * @param w �����O�̍ő啝(pixcel)
   * @param ri �����O�̓��~(w�ɑ΂���p�[�Z���e�[�W�Ŏw�肵0�`1�̒l�����)
   * @param ro �����O�̊O�~(w�ɑ΂���p�[�Z���e�[�W�Ŏw�肵0�`1�̒l�����)
   * @param cusps �J�X�v�̃��X�g�Bnull���w�肷���0,30,60..�Ƃ����悤��12�����̓x�����X�g���w�肵�����ƂɂȂ�B
   * @param asc �����O�̉�]�p�B�b�тȂ�ʏ�̓A�Z���_���g�̒l���Z�b�g����B
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double ri,double ro,double [] cusps) {
    this.g = g;
    this.x = x; this.y = y; 
    this.w = w; this.asc = asc; this.ri = ri;
    this.ro = ro;
    this.cusps = cusps == null ? ZODIAC_ANGLES : cusps;
  }
  public void draw() {
    Sector [] array = Sector.getRing(x,y,w,ri,ro,cusps,asc);
    if((condition & 8) != 0 && (! isNoBackground )) {
      for(int i=0; i<array.length; i++) {
        if(bgColors != null) {
          int c = i % bgColors.length;
          g.setPaint(bgColors[c]);
          g.fill( array[i].sector );
        }
      }
    }
    if(isNoBorder) return;
    g.setPaint(Color.BLACK);
    for(int i=0; i<array.length; i++) {
      if((condition & 1) != 0) {
        g.setPaint(outerLineColor);
        g.draw(array[i].outer);
      }
      if((condition & 2) != 0) {
        g.setPaint(innerLineColor);
        g.draw( array[i].inner );
      }
      if((condition & 4) != 0) {
        g.setPaint(separatorLineColor);
        g.draw( array[i].line1 );
      }
    }
  }
  /**
   * �h��Ԃ��̗L���A�O�~�ʂ̕`��̗L���A���~�ʂ̕`��̗L���A�������̗L��
   */
  public void setPaintCondition(int condition) {
    this.condition = condition;
  }
}
