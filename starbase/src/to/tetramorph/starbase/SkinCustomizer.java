/*
 * SkinCustomizer.java
 *
 * Created on 2008/10/02, 18:40
 *
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import javax.swing.event.ChangeEvent;
import to.tetramorph.starbase.widget.CustomizePanel;

/**
 * スキンの設定を管理するクラス。管理に必要なメニューと、そのメニューの機能を
 * である、設定の選択、編集、整理の機能を有する。
 * @author 大澤義鷹
 */
class SkinCustomizer extends ModuleCustomizer {


    /**  SkinCustomizer オブジェクトを作成する */
    public SkinCustomizer( Frame ownerFrame ) {
        super( ownerFrame );
        prop.setProperty( "selectionMenuName",  "チャートのスキンを選択" );
        prop.setProperty( "arrangmentMenuName",  "スキンを整理" );
        prop.setProperty( "customizeMenuName",   "チャートのスキンを編集" );
        prop.setProperty( "tableName",           "COLOR_PROPERTIES" );
        prop.setProperty( "changerDialogTitle",  "スキンの選択と整理" );
        prop.setProperty( "customizeDialogTitle","スキンの編集" );
        prop.setProperty( "saveDialogTitle",     "スキンの保存" );
        prop.setProperty( "exportSaveDialogTitle","スキンのエキスポート" );
        createMenu();

        customizeMenuItem.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S,
                java.awt.event.InputEvent.SHIFT_MASK |
                java.awt.event.InputEvent.CTRL_MASK ) );

    }

    public String getDefaultConfName() {
//        return Config.data.getProperty(
//            module.getClassName() + ".DefaultColorConfName", "" );
        String className = module.getClassName();
        return Config.usr.getProperty( "DefaultColorConfName:" + className, "" );
    }

    public void setDefaultConfName( String confName ) {
        if ( confName == null ) confName = "";
        String className = module.getClassName();
        Config.usr.setProperty( "DefaultColorConfName:" + className, confName);
        Config.save();
//        Config.data.setProperty( module.getClassName()
//                                 + ".DefaultColorConfName", confName );
    }

    public CustomizePanel getCustomizePanel() {
        return module.getColorCustomizePanel();
    }

    public void load( String confName ) {
        assert module != null;
        if ( module.getDefaultColor() == null ) {
            //モジュールが計算条件設定機能をもたないときは、メニューを不活性化
            setEnabled( false );
            return;
        } else setEnabled( true );
        selectedPref.clear();
        selectedPref.copy( module.getDefaultColor() );
        boolean found = db.getConfigProperties( confName,
                                                 moduleClassName,
                                                 selectedPref,
                                                 getProperty("tableName") );
        module.getColorCustomizePanel().setPreference( selectedPref );
        module.updateColorSetting();
        createSpecificDialog();
    }

    public void updateModuleSetting() {
        module.updateColorSetting();
    }

    public void stateChanged( ChangeEvent evt ) {
         assert module != null;
         int state = specificEditDialog.getState();
         module.updateColorSetting();
         if ( state == SpecificEditDialog.SAVE ) {
             selectedConfName = specificEditDialog.getConfName();
             setDefaultConfName( selectedConfName );
         }
//         if ( state == SpecificEditDialog.PREVIEW  ||
//              state == SpecificEditDialog.USE      ||
//              state == SpecificEditDialog.CANCELED    ) {
//             module.updateColorSetting();
//         } else if ( state == SpecificEditDialog.SAVE ) {
//             //この時点でSave処理はSpesificDialogで処理済みになっている
//             selectedConfName = specificEditDialog.getConfName();
//             setDefaultConfName( selectedConfName );
//             module.updateColorSetting();
//         } else
//             throw new UnsupportedOperationException(
//                 "SkinCustomizerはこのstate=" + state + "をサポートしません");
     }
}
