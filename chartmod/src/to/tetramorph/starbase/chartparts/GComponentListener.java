/*
 * GComponentListener.java
 *
 * Created on 2007/10/05, 9:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

/**
 * GComponentのイベントリスナインターフェィス。
 * @author 大澤義鷹
 */
public interface GComponentListener {
  /**
   * GComponentがクリックされた時に呼び出される。
   */
  public void componentClicked(GComponentEvent evt);
  
  /**
   * GComponentがダブルクリックされた時に呼び出される。
   */
  public void componentDoubleClicked(GComponentEvent evt);
  
  /**
   * GComponentにマウスカーソルが接触したときに呼び出される。
   */
  public void componentOnCursor(GComponentEvent evt);
  
  /**
   * GComponentからマウスカーソルが離れたときに呼び出される。
   */
  public void componentOutCursor(GComponentEvent evt);  
}
