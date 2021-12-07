/*
 * CombinationSelectorPanel.java
 *
 * Created on 2008/02/26, 16:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import javax.swing.JPanel;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 * 仕様設定パネルにアスペクトやチャート円の組合せ選択を行うクラスをセットするとき、
 * 選択を行うGUIパネルはこのクラスを継承して作成する。そのパネルはJPanelでなけれ
 * ばならず、この抽象クラスがもつ抽象メソッドを実装していなければならない、という
 * のが条件。
 * @author 大澤義鷹
 */
public abstract class CombinationSelectorPanel extends JPanel {
    public abstract Preference getPreference( Preference pref );
    public abstract void setPreference( Preference pref );
    public abstract void setCustomizePanel( CustomizePanel cp );
}
