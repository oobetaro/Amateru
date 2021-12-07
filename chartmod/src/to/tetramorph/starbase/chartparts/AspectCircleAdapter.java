/*
 * AspectCircleAdapter.java
 *
 * Created on 2007/10/08, 15:01
 *
 */

package to.tetramorph.starbase.chartparts;

/**
 * AspectCircleListenerのアダプタークラス。
 * @author 大澤義鷹
 */
public abstract class AspectCircleAdapter implements AspectCircleListener {
  /**
   * アスペクト円内のアスペクト線がクリックされた時に呼び出される。
   */
  public void aspectClicked(AspectCircleEvent evt) {
  }
  /**
   * アスペクト円内のアスペクト線がダブルクリックされた時に呼び出される。
   */
  public void aspectDoubleClicked(AspectCircleEvent evt) {
  }
  /**
   * アスペクト円内で、アスペクト線以外が左クリックされた時に呼び出される。
   */
  public void aspectCircleClicked(AspectCircleEvent evt) {
  }
  /**
   * アスペクト円内のアスペクト線にマウスカーソルが接触したときに呼び出される。
   */
  public void aspectOnCursor(AspectCircleEvent evt) {
  }
  
  /**
   * アスペクト円内のアスペクト線からマウスカーソルが離れたときに呼び出される。
   */
  public void aspectOutCursor(AspectCircleEvent evt) {
  }  
}
