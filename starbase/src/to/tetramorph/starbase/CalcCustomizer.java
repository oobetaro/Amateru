/*
 * CalcCustomizer.java
 *
 * Created on 2008/10/02, 20:26
 *
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import javax.swing.event.ChangeEvent;
import to.tetramorph.starbase.widget.CustomizePanel;

/**
 * 計算条件の設定を管理するクラス。管理に必要なメニューと、そのメニューの機能を
 * である、設定の選択、編集、整理の機能を有する。
 * @author 大澤義鷹
 */
class CalcCustomizer extends ModuleCustomizer {

    /**  CalcCustomizer オブジェクトを作成する */
    public CalcCustomizer( Frame ownerFrame ) {
        super( ownerFrame );
        prop.setProperty( "selectionMenuName",    "チャート計算条件を選択" );
        prop.setProperty( "arrangmentMenuName",   "計算条件の整理" );
        prop.setProperty( "customizeMenuName",    "チャート計算条件を設定" );
        prop.setProperty( "tableName",            "SPECIFIC_PROPERTIES" );
        prop.setProperty( "changerDialogTitle",   "計算条件の選択と整理" );
        prop.setProperty( "customizeDialogTitle", "計算条件の設定" );
        prop.setProperty( "saveDialogTitle",      "計算条件の保存" );
        prop.setProperty( "exportSaveDialogTitle","計算条件のエキスポート" );
        createMenu();
        customizeMenuItem.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S,
                java.awt.event.InputEvent.CTRL_MASK ) );
    }

    @Override
    public String getDefaultConfName() {
        String className = module.getClassName();
        return Config.usr.getProperty("DefaultSpecificConfName:" + className,"");
    }
    @Override
    public void setDefaultConfName( String confName ) {
        if ( confName == null ) confName = "";
        String className = module.getClassName();
        Config.usr.setProperty("DefaultSpecificConfName:" + className, confName);
        Config.save();
    }

    @Override
    public CustomizePanel getCustomizePanel() {
        return module.getSpecificCustomizePanel();
    }

    @Override
    public void load( String confName ) {
        assert module != null;
        if ( module.getDefaultSpecific() == null ) {
            //モジュールが計算条件設定機能をもたないときは、メニューを不活性化
            super.setEnabled( false );
            return;
        } else setEnabled( true );
        selectedPref.clear();
        selectedPref.copy( module.getDefaultSpecific() );
        boolean found = db.getConfigProperties( confName,
                                                 moduleClassName,
                                                 selectedPref,
                                                 getProperty( "tableName" ) );
        module.getSpecificCustomizePanel().setPreference( selectedPref );
        module.updateSpecificSetting();
        createSpecificDialog();
    }

    @Override
    protected void updateModuleSetting() {
        module.updateSpecificSetting();
    }

    @Override
    public void stateChanged( ChangeEvent evt ) {
        assert module != null;
        //SpecificEditDialog dialog = (SpecificEditDialog)evt.getSource();
        //プレビューの場合、SpecificDialogからプレビュー用のPreference得てセット
        int state = specificEditDialog.getState();
        module.updateSpecificSetting();
        if (state == SpecificEditDialog.SAVE) {
            selectedConfName = specificEditDialog.getConfName();
            setDefaultConfName(selectedConfName);
        }
    }
}
