/*
 * HouseDisk.java
 *
 * Created on 2007/05/02, 20:13
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;
import to.tetramorph.starbase.chartplate.DialGauge;
import to.tetramorph.starbase.chartplate.HouseNumberDial;
import to.tetramorph.starbase.chartplate.HouseRing;
import to.tetramorph.starbase.chartplate.MarkerNeedle;
import to.tetramorph.starbase.chartplate.NumberNeedle;
import to.tetramorph.starbase.chartplate.PlanetActionListener;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChartFactor;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * �A�X�y�N�g�ȊO�̃z���X�R�[�v�̕��i���W�񂵂āA��̕��i�Ƃ���N���X�B
 * �n�E�X�Ƃ��̒��̓V�́A�Q�[�W�A�x���\���Ȃǂ���\�������}�N���I�ȕ��i�B
 * @author ���V�`��
 */
public class HouseDisk implements HoroscopeDisk {
  // �z���X�R�[�v���\������T�C����V�̂�J�X�v���`��I�u�W�F�N�g
  public DialGauge gauge2 = new DialGauge();
  public PlanetNeedle planetNeedle;
  public HouseRing houseRing = new HouseRing();
  public HouseRing houseRing2 = new HouseRing();
  public MarkerNeedle markerNeedle = new MarkerNeedle();
  public MarkerNeedle markerNeedle2 = new MarkerNeedle();
  public NumberNeedle cuspAngleNumberNeedle = new NumberNeedle();
  public NumberNeedle planetAngleNumberNeedle = new NumberNeedle();
  public HouseNumberDial houseNumberDial = new HouseNumberDial();
  Component component;
  // �O������񋟂����ϐ�
  Graphics2D g;
    double cx,cy,W;
  double asc;
  double [] cusps;
  Aspect selectedAspect;
  //  �����Ő�������f�[�^
  String [] cuspsString;
  List<Body> planetList; //AC,MC,IC,DC�ȊO�̕ۊǗp
  double [] planetsAngle;
  String [] planetsAngleString;

  /**
   *  
   * HouseDisk �I�u�W�F�N�g���쐬����
   */
  public HouseDisk(Component c,PlanetActionListener l) {
    this.component = c;
    planetNeedle = new PlanetNeedle(c,l);
  }
  /** �`�悷��O���t�B�b�N�X�I�u�W�F�N�g���Z�b�g���� */
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }
  /** 
   * �`�悷���Ƃ�����W���Z�b�g����B
   * @param cx ���S���Wx
   * @param cy ���S���Wy
   * @param W ��ƂȂ�`��G���A�̕�(�P��pixel �˂ɐ����`�Ƃ݂Ȃ�)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }
  /**
   * �n�E�X�J�X�v�̉������W��z��Ŏw�肷��B[0�`11]�܂ŁB
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps;
    cuspsString = ChartFactor.formatSignAngles(cusps,0); //�\���p�J�X�v�x���̃��X�g
  }
  /**
   * �w�肳�ꂽ�A�X�y�N�g�����V�̂�ԓ_������B
   */
  public void setSelectedAspect(Aspect aspect) {
    this.selectedAspect = aspect;
  }
  public void setAC(double ac) {
    this.asc = ac;
  }
  /**
   * �\������V�̂̃��X�g���Z�b�g����B
   */
  public void setPlanetList(List<Body> planetList) {
    this.planetList = planetList;
    planetsAngle = ChartFactor.getPlanetsPlotAngle(planetList);
    planetsAngleString = ChartFactor.formatSignAngles(
      ChartFactor.getPlanetsAngle(planetList),0); //�V�̂̕\���p�x�����X�g    
  }
  double wi,wo;
  public void setSize(double wi,double wo) {
    this.wi = wi;
    this.wo = wo;
  }
  /**
   * ���̃I�u�W�F�N�g�ɓo�^����Ă�������ŕ`������s����B
   */
  public void draw() {
    // �L�����o�X�̕� W=1�ɑ΂��āE�E�E
    double ww = wo - wi;
    double d2 = wi + ww / 2.0;
    houseRing.setPaintCondition(HouseRing.SEPARATOR|HouseRing.INNER_ARC);
    houseRing.setFactor(g,cx,cy,W,asc,wi,wo,cusps);
    markerNeedle2.setExtension(d2 - 0.05);

    gauge2.setFactor(g,cx,cy,W,asc,wi);
    planetNeedle.setFactor(g,cx,cy,W,asc,d2,0.03,planetList,selectedAspect);
    markerNeedle.setFactor(g,cx,cy,W,asc,wo,planetList); //�O�̃Q�[�W
    markerNeedle2.setFactor(g,cx,cy,W,asc,wi,planetList);//���̃Q�[�W
    planetAngleNumberNeedle.setFactor(g,cx,cy,W,asc,d2 + 0.07,0.015,planetsAngle,planetsAngleString);
    planetAngleNumberNeedle.setVOffset(0.0085);
    houseRing.draw();
    markerNeedle2.draw();
    planetNeedle.draw();
    markerNeedle.draw();
    planetAngleNumberNeedle.draw();
    gauge2.draw();
  }
}
