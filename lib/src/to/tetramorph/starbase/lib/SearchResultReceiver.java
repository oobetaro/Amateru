/*
 * SearchResultReceiver.java
 *
 * Created on 2006/07/27, 0:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

/**
 * 検索結果を受け取るレシーバー
 */
public interface SearchResultReceiver {
//    /**
//     * 検索結果を受け取るレシーバーにNatalデータを書き込む
//     * @param list Natalのリスト
//     * @param tabName 検索窓の各タブに表示される名前。nullを指定したときは、
//     * 自動的に"No.xx"というように番号表示になる。
//     * @param message タブ内のメッセージラベルに表示するメッセージ。
//     * たとえば、「太陽と火星が１８０度(の検索結果)」などと、なにを検索したのか
//     * をユーザに伝える情報を提供する。nullを指定した場合は""と解釈される。
//     */
//    public void write( List<Natal> list, String tabName, String message , java.lang.String pathName);
    /**
     * 検索結果を受け取るレシーバーにNatalデータを書き込む
     */
    public void write( SearchResult result );
}
