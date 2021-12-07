/*
 * PlanetActionListener.java
 *
 * Created on 2007/05/05, 4:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import to.tetramorph.starbase.lib.Body;

/**
 * ホロスコープ上の天体のイベントリスナインターフェイス。
 * @author 大澤義鷹
 */
public interface PlanetActionListener {
  /**
   * PlanetRing内の天体がクリックされたことを知らせる。
   */
  public void bodyClicked(PlanetEvent evt);
  /**
   * PlanetRing内の天体がダブルクリックされたことを知らせる。
   */
  public void bodyDoubleClicked(PlanetEvent evt);
  /**
   * PlanetRing内の天体にオンカーソルしたことを知らせる。
   */
  public void bodyOnCursor(PlanetEvent evt);
  /**
   * PlanetRing内の天体からアウトカーソルしたことを知らせる。
   */
  public void bodyOutCursor(PlanetEvent evt);
  
//  /**
//   * PlanetRing内の天体がドラッグで移動されたことを知らせる。
//   */
//  public void bodyDragged(PlanetEvent evt);

}
