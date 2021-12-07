/*
 * ChartTab.java
 *
 * Created on 2007/11/09, 12:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.module;

import to.tetramorph.starbase.lib.Natal;

/**
 * ChartModulePanelにChartPaneのインスタンスをそのまま渡すのは、セキュリティの
 * 問題があるのでこのインターフェイスで渡す。
 * ChartPaneはこのインターフェイスを実装する。
 * @author 大澤義鷹
 */
public interface ChartTab {
  /**
   * このタブにタイトルをセットする。
   */
  public void setTitle(String title);
  /**
   * このタブにアイコンをセットする。
   */
  public void setIcon(Natal occ);
  /**
   * このタブのコンテンツエリアが選択されたことをアマテルに通達する。
   * アマテルは、DBのシャッターパネルが可視化されていた場合、それを非可視化する。
   */
  public void contentSelected();
}
