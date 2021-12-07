/*
 * HoroscopeCase.java
 *
 * Created on 2007/05/07, 6:57
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.chartplate.AspectCircle;

/**
 * 1�d�~���瑽�d�~��\�����邽�߂̕��i�BHouseDisk,ZodiacDisc�𕡐��ǉ����邱�Ƃ�
 * ���d�~�̕`�悪�ł���B
 * @author ���V�`��
 */
public class HoroscopeCase {
  AspectCircle aspectCircle;
  List<HoroscopeDisk> horoList = new ArrayList<HoroscopeDisk>();
  double cx,cy,maxdia;
  double dia;
  Graphics2D g;
  // �e�~�̕�(�����O�̏��Ő錾)
  double [][] diskSizes  = { 
    { 0.85,0.75 }, //�~����� 
    { 0.85,0.75,0.57 }, //�~����� �l�C�^��1�d�~�͂�������
    { 0.85,0.75,0.6,0.45 },
    { 0.85,0.75,0.61,0.47,0.33 }
  };

  /**  HoroscopeCase �I�u�W�F�N�g���쐬���� */
  public HoroscopeCase() {
  }
  /**
   * �`��ʒu�ƍő咼�a���w�肷��B
   */
  public void setPos(double cx,double cy,double maxdia) {
    this.cx = cx;
    this.cy = cy;
    this.maxdia = maxdia; //�t���[���ɖ��ڂ���~�̒��a
  }
  /** 
   * �`�悷��O���t�B�b�N�X�I�u�W�F�N�g���Z�b�g���� 
   */
  public void setGraphiecs2D(Graphics2D g) {
    this.g = g;
  }
  /**
   * �z���X�R�[�v�̈�ԊO���̉~(���������͉��������O)�̒��a���Z�b�g����B
   * setPos()��maxdia��1�Ƃ��Ă��̔䗦�Ŏw�肷��B���Ȃ炸1�ȉ��ł��邱�ƁB
   */
  public void setDiameter(double dia) {
    this.dia = dia;
  }
  /**
   * 1�d�~,2�d�~,3�d�~,,,�Ɖ~�̐��ɉ����ẮA���ꂼ��̑傫�����w�肷��B
   * setDiameter�̒l��1�Ƃ��āA���̔䗦�ŕ������肵�A���̕��̑��a�̎c�肪�A
   * �A�X�y�N�g�~�̃T�C�Y�ƂȂ�B
   */
  public void setDiskSizes(double [][] diskSizes) {
    this.diskSizes = diskSizes;
  }
  /**
   * ���̃P�[�X�Ƀz���X�R�[�v�f�B�X�N��ǉ�����B
   */
  public void addHoroscopeDisk(HoroscopeDisk disk) {
    horoList.add(disk);
  }
  /**
   * ���̃P�[�X����z���X�R�[�v�f�B�X�N���폜����
   */
  public boolean remveHoroscopeDisk(HoroscopeDisk disk) {
    return horoList.remove(disk);
  }
  /**
   * �w�肳�ꂽ�z���X�R�[�v�̈ʒu����������B
   */
  public void swapHoroscopeDisk(HoroscopeDisk disk1,HoroscopeDisk disk2) {
    
  }
  /**
   * �A�X�y�N�g�~�����̃P�[�X�ɃZ�b�g����
   */
  public void setAspectCircle(AspectCircle aspectCircle) {
    this.aspectCircle = aspectCircle;
  }
  /**
   * ���̃P�[�X���̃A�X�y�N�g�~���폜����
   */
  public void removeAspectCircle() {
    this.aspectCircle = null;
  }
  /**
   * �z���X�R�[�v��`�悷��B
   */
  public void draw() {
    double [] ws = diskSizes[ horoList.size() - 1];
    for(int i=0; i<horoList.size(); i++) {
      HoroscopeDisk disk = horoList.get(i);
      disk.setGraphics2D(g);
      disk.setPos(cx,cy,maxdia);
      disk.setSize(ws[i+1],ws[i]);
      disk.draw();
    }
//    if(aspectCircle != null) {
//      aspectCircle.setGraphics2D(g);
//      aspectCircle.setPos(cx,cy,dia);
//      aspectCircle.setSize(ws[0]);
//      aspectCircle.draw();
//    }
  }
}
