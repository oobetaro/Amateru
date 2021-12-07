/*
 * ChartData.java
 *
 * Created on 2006/11/28, 4:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.util.List;

/**
 * ChartDataPanelがimplementsしている。
 * @author 大澤義鷹
 */
public interface ChartData {
    /**
     * コンボボックスに登録されているDataのリストを返す。
     */
    
    public List<Data> getDataList();
    
    /**
     * コンボボックスのデータの何番目が選択されているか返す。
     * 選択されていない場合は負数を返す。(たぶん-1)
     */
    public int getSelectedIndex();
    
    /**
     * 直接入力フォームの日時場所のデータを返す。
     */
    public Transit getTransit();
    
    /**
     * このChartDataにふさわしいアイコン(男女の顔や時計やコンポジットのシンボル)を
     * タブパネル内のタブにセットする。
     */
    
    public void setTabIcon();
    /**
     * 現在選択中のNatalデータをDataオブジェクトで返す。
     * 直入力タブにフォーカスがあったり、データが未設定ならnullを返す。
     */
    public Data getSelectedData();
    public boolean isComposit();
    public Natal getComposit();
    public boolean isComprise(int id);
    public void replaceNatal(Natal natal);
    public void setNatal(List<Natal> natalList);
    public void addNatal(List<Natal> natalList);
    public void updateData(Data data);
    public void setTransit(Transit transit);
}
