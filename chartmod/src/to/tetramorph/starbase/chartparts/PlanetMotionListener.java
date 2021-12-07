/*
 * PlanetMotionListener.java
 *
 * Created on 2007/07/18, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

/**
 * PlanetRing内の天体がドラッグ操作で移動された際のリスナインターフェイス。
 * @author 大澤義鷹
 */
public interface PlanetMotionListener {
  /**
   * PlanetRing内の天体がドラッグで移動されたことを知らせる。
   */
  public void bodyDragged(PlanetEvent evt);
}
