/*
 * SearchResult.java
 *
 * Created on 2008/09/20, 2:21
 *
 */

package to.tetramorph.starbase.lib;

import java.util.List;

/**
 *
 * @author 大澤義孝
 */
public class SearchResult {
    protected List<Natal> list;
    protected String tabName;
    protected String title;
    protected String pathName;
    protected String searchType;
    protected int count = -1;
    /**  
     * SearchResult オブジェクトを作成する。
     * @param list 検索結果のリスト
     * @param tabName 検索結果窓のタブに表示される名前。
     * @param title 検索条件を表す文字列。「太陽 蟹13度」等。
     * @param searchType 検索方式 「テキスト検索」「アスペクト検索」等。
     */
    public SearchResult( List<Natal> list,
                          String tabName,
                          String title,
                          String pathName,
                          String searchType ) {
        this.list = list;
        this.tabName = tabName;
        this.title = title;
        this.pathName = pathName;
        this.searchType = searchType;
    }
    public List<Natal> getNatalList() {
        return list;
    }
    public String getTabName() {
        return tabName;
    }
    public String getTitle() {
        return title;
    }
    public String getPathName() {
        return pathName;
    }
    public String getSearchType() {
        return searchType;
    }
    /**
     * 検索対象となったデータの総数をセットする。初期値は-1でこれは未登録状態
     * を表している。
     * これはSearchDialogの中から呼び出され、SearchModuleの中から操作しては
     * ならない。
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return count;
    }
}
