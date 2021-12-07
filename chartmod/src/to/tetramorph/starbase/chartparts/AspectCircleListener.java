/*
 * AspectCircleListener.java
 *
 * Created on 2007/08/23, 10:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

/**
 * AspectCircleのイベントリスナインターフェイス。
 * @author 大澤義鷹
 */
public interface AspectCircleListener {
  /**
   * アスペクト円内のアスペクト線がクリックされた時に呼び出される。
   */
  public void aspectClicked(AspectCircleEvent evt);
  
  /**
   * アスペクト円内のアスペクト線がダブルクリックされた時に呼び出される。
   */
  public void aspectDoubleClicked(AspectCircleEvent evt);

  /**
   * アスペクト円内で、アスペクト線以外が左クリックされた時に呼び出される。
   */
  public void aspectCircleClicked(AspectCircleEvent evt);
  
  /**
   * アスペクト円内のアスペクト線にマウスカーソルが接触したときに呼び出される。
   */
  public void aspectOnCursor(AspectCircleEvent evt);
  
  /**
   * アスペクト円内のアスペクト線からマウスカーソルが離れたときに呼び出される。
   */
  public void aspectOutCursor(AspectCircleEvent evt);
}
