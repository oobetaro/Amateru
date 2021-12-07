/*
 * ZodiacDisk.java
 *
 * Created on 2007/05/05, 3:29
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;
import to.tetramorph.starbase.chartplate.DialGauge;
import to.tetramorph.starbase.chartplate.HouseNumberDial;
import to.tetramorph.starbase.chartplate.HouseRing;
import to.tetramorph.starbase.chartplate.NumberNeedle;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
import to.tetramorph.starbase.chartplate.SignDial;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChartFactor;

/**
 * �b�у����O�Ƃ���ɂ��Ă���Q�[�W��`��
 * �܂��b�я�ɂ��Ԃ�J�X�v����b�у����O�̊O�ɔz�u�����AC,MC,DC,IC�̊���_�\���@�\������B
 * @author ���V�`��
 */
public class ZodiacDisk implements HoroscopeDisk {
  double cx,cy,W;
  Graphics2D g;
  double asc;
  
  public HouseRing zodiacRing = new HouseRing();
  public DialGauge gauge = new DialGauge(); //�b�у����O�̃Q�[�W
  public SignDial signDial = new SignDial(); //�b�у����O
  public HouseRing houseRing2 = new HouseRing(); //�n�E�X�����O���J�X�v���Ƃ��Ďg��
  public NumberNeedle cuspAngleNumberNeedle = new NumberNeedle(); //�J�X�v�ԍ��j
  public HouseNumberDial houseNumberDial = new HouseNumberDial(); //�n�E�X�ԍ��_�C�A��
  public PlanetNeedle specialPlanetNeedle = new PlanetNeedle(); //AC,MC,DC,IC�p�̐j
  Component component;
  double [] cusps;     //�J�X�v�̉��o���W
  String [] cuspAngles; //�J�X�v�x���̕�����\��
  List<Body> acmcList;  //AC,MC,IC,DC��ۊǗp
  Aspect selectedAspect;
    
  /**  ZodiacDisk �I�u�W�F�N�g���쐬���� */
  public ZodiacDisk(Component c) {
    this.component = c;
  }
  /** �`�悷��O���t�B�b�N�X�I�u�W�F�N�g���Z�b�g���� */
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }  
  /** 
   * �`�悷���Ƃ�����W���Z�b�g����B
   * @param cx ���S���Wx
   * @param cy ���S���Wy
   * @param W ��ƂȂ�`��G���A�̕�(�˂ɐ����`�Ƃ݂Ȃ�)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }  
  /**
   * �A�Z���_���g�_��ݒ肷��B
   */
  public void setAC(double ac) {
    this.asc = ac;
  }
  /**
   * �w�肳�ꂽ�A�X�y�N�g�����V�̂�ԓ_������B
   */
  public void setSelectedAspect(Aspect aspect) {
    this.selectedAspect = aspect;
  }
  /**
   * AC,MC,DC,IC�̊���_�̃��X�g���Z�b�g����B
   */
  public void setACList(List<Body> acmcList) {
    this.acmcList = acmcList;
  }  
  /**
   * �n�E�X�J�X�v�̉������W��z��Ŏw�肷��B[0�`11]�܂ŁB
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps;
    cuspAngles = ChartFactor.formatSignAngles(cusps,0); //�\���p�J�X�v�x���̃��X�g
  }
  double wi,wo;
  public void setSize(double wi,double wo) {
    this.wi = wi;
    this.wo = wo;
  }
  /**
   * �`�悷��
   */
  public void draw() {
    // �L�����o�X�̕� W=1�ɑ΂��āE�E�E
    double ww = wo - wi;
    zodiacRing.setFactor(g,cx,cy,W,asc,wi,wo,null);
    signDial.setFactor(g,cx,cy,W,asc,wo-ww/2d,0.03f);
    gauge.setFactor(g,cx,cy,W,asc,wi);
    houseRing2.setPaintCondition(HouseRing.SEPARATOR);
    houseRing2.setFactor(g,cx,cy,W,asc,wi,wo+0.05,cusps);
    cuspAngleNumberNeedle.setFactor(g,cx,cy,W,asc,wo+0.01,0.015,cusps,cuspAngles);
    specialPlanetNeedle.setFactor(g,cx,cy,W,asc,wo+0.07,0.04,acmcList,selectedAspect);
    cuspAngleNumberNeedle.setVOffset(0.018);
    cuspAngleNumberNeedle.setAlign(NumberNeedle.OUTER);
    houseNumberDial.setFactor(g,cx,cy,W,asc,wo+0.03,0.02,cusps);
    //�b�у����O�Ƃ��̖ڐ���
    zodiacRing.draw();
    gauge.draw();
    signDial.draw();  
    //�J�X�v��AC,MC���̓���I�u�W�F�N�g�̕\��
    houseRing2.draw();
    cuspAngleNumberNeedle.draw();
    houseNumberDial.draw();
    specialPlanetNeedle.draw();
  }

}
