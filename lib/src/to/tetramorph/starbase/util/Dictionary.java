/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.util;

/**
 * 辞書ダイアログのインスタンスにはこのインターフェイスでアクセスする。
 * @author 大澤義鷹
 */
public interface Dictionary {
    /**
     * 辞書に検索コマンドを与える。
     */
    void search( DictionaryRequest req );
    /**
     * 辞書ダイアログが可視化されているときはtrueを返す。
     */
    boolean isVisible();
    /**
     * 辞書ダイアログの可視／非可視状態を設定する。
     */
    void setVisible(boolean b);
}
