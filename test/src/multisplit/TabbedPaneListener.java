/*
 * TabbedPaneListener.java
 *
 * Created on 2007/10/30, 16:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package multisplit;

import java.awt.Component;

/**
 * InnerTabbedPaneにセットするリスナ
 * @author 大澤義鷹
 */
public interface TabbedPaneListener {
  /**
   * タブのクローズボタンでタブが閉じられた。
   * @param tabpan タブ閉じが発生したInnerTabbedPane
   * @param c 閉じられたタブに入っていたコンポーネント
   */
  void closedTab( InnerTabbedPane tabpan, Component c );
  /**
   * タブがダブルクリックされた。
   * @param tabpan ダブルクリックが発生したInnerTabbedPane
   * @param c ダブクリされたタブに入っていたコンポーネント
   */
  void doubleClicked( InnerTabbedPane tabpan, Component c);
}
