/*
 * TimeManeuverListener.java
 *
 * Created on 2006/10/11, 19:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

/**
 * TimeManeuverPanelからの時間変更イベントを受け取るリスナ。
 * @author 大澤義鷹
 */
interface TimeManeuverListener {
  /**
   * 日時をインクリメントする。
   */
  public void increment();
  /**
   * 日時をデクリメントする。
   */
  public void decrement();
  /**
   * 値をセット
   */
  public void store();
}
