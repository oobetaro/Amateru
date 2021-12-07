/*
 * MultiTabbedListener.java
 *
 * Created on 2007/11/09, 19:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.multisplit;

import java.awt.Component;

/**
 * MultiTabbedPaneのイベントを検出するためのリスナインターフェイス。
 * @author 大澤義鷹
 */
public interface MultiTabbedListener {
  /**
   * タブが選択されたとき、コンポーネントがインサートされ(
   * そのタブが選択され)たとき呼び出される。
   * @param c 選択されたコンポーネント
   */
  public void tabSelected( Component c );
  /**
   * タブがクローズされる直前に呼び出される。
   * @param c 削除されるコンポーネント
   */
  public void tabClosed( Component c );
  
}
