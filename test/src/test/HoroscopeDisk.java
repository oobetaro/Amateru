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
 * @author 大澤義鷹
 */
public interface HoroscopeDisk {
  public void draw();
  public void setPos(double cx,double cy,double dia);
  public void setGraphics2D(Graphics2D g);
  /**
   * リングの外径とその幅を0～1までのパーセンテージで指定する。
   */
  public void setSize(double wi,double wo);
}
