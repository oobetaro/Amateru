/*
 * ChartPanelListener.java
 *
 * Created on 2007/11/09, 11:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.Component;
import javax.swing.Icon;

/**
 * ChartPaneのリスナ。ChartPane内でおきるイベントはこのリスナで通達される。
 * Mainの内部クラスChartPanelHandlerにimplementされている。
 * @author 大澤義鷹
 */
interface ChartPanelListener {
    public void setTitle( String title, Component c );
    public void setIcon( Icon icon, Component c );
    public void contentSelected( Component c );
    /**
     * チャートモジュールが切り替えられた時に呼び出される。
     * @param chartPane 切替が発生したChartPane
     */
    public void selectedChartModule(ChartPane chartPane);
    /**
     * 色の仕様設定メニューに変更があった場合に呼び出される。
     * 仕様カスタマイズ(仕様名の変更・削除・登録)が行われとき。
     */
    public void updateColorPreferenceMenu();
    /**
     * 計算の仕様設定メニューに変更があった場合に呼び出される。
     * 仕様カスタマイズ(仕様名の変更・削除・登録)が行われとき。
     */
    public void updateSpecificPreferenceMenu();
    
}
