/*
 * SearchModulePanel.java
 *
 * Created on 2006/07/21, 20:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.module;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.util.Preference;

/**
 * SearchDialogにはめ込んで使う様々な検索モジュールパネルの親クラス。
 * @author 大澤義鷹
 */
public abstract class SearchModulePanel extends JPanel {
    
    /**
     * Creates a new instance of SearchModulePanel
     */
    public SearchModulePanel() {
        super();
        //左右に8pxのスペースをあける。
        Border border = BorderFactory.createEmptyBorder( 0, 8, 0, 8 );
        setBorder(border);
    }
    /**
     * このモジュールに初期化パラメターを渡す。モジュール読みこみ時に一度だけ
     * 呼び出される。
     */
    public abstract void init( Preference configData );
    /**
     * 検索前に呼び出され、入力パラメターに誤りがないか検査しOKならtrueを返す。
     * たとえばアスペクト検索で二つとも太陽を選んだらダイアログでそのことを伝え
     * るのはこのメソッド内ですればよい。
     * 検索ボタンが押されるとまずこのメソッドが呼び出されtrueなら次にsearchが呼び出される。
     */
    public abstract boolean begin();
    /**
     *条件に一致するデータを検索する。
     */
    public abstract SearchResult search( SearchOption searchOption );
    /**
     * "テキスト検索"等、このクラスの機能を表す文字列を返す。
     * この文字列はSearchFrameのコンボボックスに入る。
     */
    @Override
    public abstract String toString();
}
