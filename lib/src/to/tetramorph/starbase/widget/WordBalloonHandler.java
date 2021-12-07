/*
 * WordBalloonHandler.java
 *
 * Created on 2006/11/14, 10:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.widget;

/**
 * ワードバルーンを表示するときは、オンカーソル中であることが条件になるが、
 * それを検査するメソッドを定義しているインターフェイス。
 *
 * ホロスコープ上の天体にオンカーソルしてコンマ数秒おいてからワードバルーンが出て
 * キャプションを表示させたい(ToolTipsと同じ動作)。
 * オンカーソルしたときに、WordBalloonのshowメソッドを呼び出すが、その後時間を
 * おかずにアウトカーソルしてしまった場合、ワードバルーンを出さないようにしたい。
 * このインターフェイスのgetSelectedObject()は、オンカーソルしたオブジェクトを
 * 返す。アウトカーソルしてしまったときはnullを返す。
 * showメソッド呼びだしのとき、showメソッド内では呼び出された時点でのオンカーソル
 * オブジェクトの参照アドレスをキープする。タイマーが起動され一定時間後、
 * もう一度WrodBalloonHandler#getSelectedObject()が呼び出され、showメソッド呼びだし
 * のときと同じオブジェクトであれば、一定時間アウトカーソルもしなかったし、一定
 * 時間の間に隣の別のオブジェクトに選択が映ったわけでもないとみなし、ワードバルーン
 * を出すという処理をする。
 * もちろん一定時間内に別の天体にカーソルが行って一定時間内に戻ってきた場合は、
 * アウトカーソルしなかったとみなされる。
 */
public interface WordBalloonHandler {
  /**
   * 現在選択中のオブジェクトを返す。
   */
  public Object getSelectedObject();
  /**
   * 選択中のオブジェクトをセットする。WordBalloonはワードバルーン消去後、
   * このメソッドをつかって選択中オブジェクトにnullをセットする。
   */
  public void setSelectedObject(Object o);
}
