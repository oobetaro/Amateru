/*
 * RingActionListener.java
 *
 * Created on 2007/07/07, 5:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

/**
 * 惑星や獣帯リングのリスナ。PlanetActionListenerは天体がセンスされたときのもの
 * だが、これはリングそのものがセンスされたときのもの。
 * @author 大澤義鷹
 */
public interface RingActionListener {
  /**
   * PlanetRingの天体以外の場所がダブルクリックされたことを知らせる。
   * PlanetEventのbodyの値はnull。
   */
  public void ringDoubleClicked(RingEvent evt);
}
