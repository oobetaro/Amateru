/*
 * CustomizePanel.java
 *
 * Created on 2007/02/21, 13:41
 *
 */

package to.tetramorph.starbase.widget;

import javax.swing.JPanel;
import to.tetramorph.starbase.lib.SpecificDialogOperations;
import to.tetramorph.util.Preference;

/**
 * カラー設定や使用天体や使用アスペクトを設定するためのパネルで、
 * ColorDialog等にはめ込んで使う色設定パネルはこのクラスを継承して作成する。
 */
public abstract class CustomizePanel extends JPanel {
    private SpecificDialogOperations op;
    
    /**
     * 設定パネルの設定状態をprefに書きこむ。
     * @param pref Preferenceオブジェクト
     * @return prefを返す。
     */
    public abstract Preference getPreference( Preference pref );
    
    /**
     * 設定パネルにPreferenceの情報を反映させる。
     * @param pref しかるべき値が登録されているPreferenceオブジェクト
     */
    public abstract void setPreference( Preference pref );
    
    /**
     * 設定パネル内の全ての入力パラメターに異常がない場合はtrueを、異常な値が入力
     * されていたり、必要な設定がなされていない場合はfalseを返す。
     * またfalseを返す場合、引数で与えられた文字配列[0]にエラーメッセージを書きこむ。
     * getPreference()呼びだし前に呼び出される。
     */
    public abstract boolean isCorrect( String [] errmsg );
    
    /**
     * SpecificDialogOperationsオブジェクトをセットする。
     * SpecificDialogOperationsインターフェイスはSpecificDialogが実装しており、
     * SpecificDialogはCustomizePanelに対して、自身の参照をこのメソッドで引き渡す。
     * 現在、それ以外の用途には使用されていない。
     */
    public void setSpecificDialogOperations( SpecificDialogOperations op ) {
        this.op = op;
    }
    
    /**
     * SpecificDialogの「保存せず適用」ボタンを押したのと同等の処理を行う。
     * ボタンが押されると、ChartModulePanelのsetSpecificConfig()が呼び出される。
     */
    public void doClickUseButton() {
        op.doClickUseButton();
    }
}
