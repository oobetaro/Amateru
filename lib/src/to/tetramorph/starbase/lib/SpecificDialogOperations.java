/*
 * SpecificDialogOperations.java
 *
 * Created on 2007/09/23, 3:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

/**
 * チャートモジュールの設定パネル側から、SpecificDialogのボタンを押したことにする
 * ために用意されたインターフェイス。
 * SpecificDialogはto.tetramorph.starbaseのパッケージに属しており、これを直接
 * チャートモジュール(プラグイン)側からは操禁止にしてある。しかしボタンを押した
 * ことにする必要性がある。
 * 具体的には、NPTCalcConfPanel.aspectDisplaySettingPanel.getRingMenu()で、
 * これはプラグインのツールバーで使用するJMenuだが、設定パネルと連動する。
 * 設定パネルを開いてNPTリングの表示状態を選択することもできるし、ツールバーの
 * メニューから選択することもあり、メニューから選択したとき、設定パネルの
 * 「保存せず適用」ボタンを押したことにしたい。このボタンが押されると、設定変更の
 * イベントが発生し、プラグインのsetSpecificConfig()が呼び出される。プラグインは
 * このメソッドの呼び出して、設定パネルの情報をホロスコープ描画に反映させる。
 * この処理は、メニューが選択されたタイミングで、SpecificDialogのボタンを押すのと
 * 同等の処理によって行われる。
 * SpecificDialogはSpecificDialogPperationsをimplementsして、ボタンが押されたのと
 * 同等の処理をするメソッドを実装し、設定パネルにはこのインターフェイスを型として
 * オブジェクトを渡すことで、設定パネル側からSpecificDialogのアクセスされては困る
 * メソッドを使う事を禁止している。
 * @author 大澤義鷹
 */
public interface SpecificDialogOperations {
      /**
       * SpecificDialogの「保存せず適用ボタン」を押したことにする。
       */
      public void doClickUseButton();
}
