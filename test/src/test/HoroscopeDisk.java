/*
 * HoroscopeDisk.java
 *
 * Created on 2007/05/07, 6:28
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Graphics2D;

/**
 *
 * @author ���V�`��
 */
public interface HoroscopeDisk {
  public void draw();
  public void setPos(double cx,double cy,double dia);
  public void setGraphics2D(Graphics2D g);
  /**
   * �����O�̊O�a�Ƃ��̕���0�`1�܂ł̃p�[�Z���e�[�W�Ŏw�肷��B
   */
  public void setSize(double wi,double wo);
}
