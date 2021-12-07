/*
 * PlanetActionListener.java
 *
 * Created on 2007/05/05, 4:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import to.tetramorph.starbase.lib.Body;

/**
 *
 * @author 大澤義鷹
 */
public interface PlanetActionListener {
  /**
   * PlanetNeedleの天体がクリックされたことを知らせる。
   */
  public void planetClicked(Body b);
  /**
   * PlanetNeedleの天体がダブルクリックされたことを知らせる。
   */
  public void planetDoubleClicked(Body b);
  /**
   * PlanetNeedleの天体がドラッグで移動されたことを知らせる。
   */
  //public void planetDragged(Body b);
}
