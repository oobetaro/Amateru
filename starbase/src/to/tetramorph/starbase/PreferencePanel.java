/*
 * PreferencePanel.java
 *
 * Created on 2006/09/15, 6:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import javax.swing.JPanel;
import to.tetramorph.util.Preference;

/**
 * 色々な設定を行うパネルを作成するときに、このクラスを継承して作る。
 * 各種設定パネルはMainConfigPanel内でJTabbedPaneに入れて使用するため、
 * interfaceではなくabstractクラスを使用する。
 * @author 大澤義鷹
 */
abstract class PreferencePanel extends JPanel {
  /**
   * サブクラスから渡されたプレファランス
   */
  //protected Preference pref;
//  /**
//   * プレファランスパネルのオブジェクトを作成する。
//   * @param pref 各種設定情報が書かれているプロパティ
//   */
//  protected PreferencePanel() {
//    //this.pref = pref;
//  }
  /**
   * このパネルの部品を読み取り、設定情報をSysProp等に設定する。
   * 「登録」ボタンが押されたときに呼ばれる。
   */
  public abstract void regist();

}
