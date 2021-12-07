/*
 * SpecificCustomizer.java
 *
 * Created on 2007/03/31, 8:21
 *
 */

package chartmodule;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 * 色の設定や、計算設定を管理するためのクラスで、設定に必要なJMenuやJMenuItem
 * を提供し、そのメニューが選択された際に、SpecificSaveDialo,SpecificRemoveDialog,
 * SpecificDialogなどを呼びだし、保存、整理(削除、順番交換等)、設定、設定ロード
 * などの処理を行う。
 * このクラスは、設定管理を総合的に行うクラスで、前述のダイアログを使役するまとめ役。
 * 設定は色設定と他の使用天体や計算仕様の二種類があり、このクラスを作成する際に
 * どちらの設定を行うかそのモードを指定する。
 * モードの違いに応じてテーブル名を変更するのと、ChartModulePanel内の呼びだし
 * メソッドを切り替えているだけ。
 * @author 大澤義鷹
 */
class SpecificCustomizer {

  private ChartModulePanel module;
  private MainFrame mainFrame;
  private boolean update = false;
  /**
   * 色設定編集モードを表す定数で、コンストラクタの引数に使用する。
   */
  public static final int COLOR_EDIT_MODE = 0;
  /**
   * 仕様編集モードを表す定数で、コンストラクタの引数に使用する。
   */
  public static final int SPECIFIC_EDIT_MODE = 1;
  /**
   * [0]は色設定モードのときに使用されるDB上のテーブル名。
   * [1]は使用設定モードのときに使用されるDB上のテーブル名。
   */
  protected static final String [] tableNames = 
    { "COLOR_PROPERTIES","SPECIFIC_PROPERTIES" };
  private int mode;
  private Database db = Database.getInstance();
  // 選択されている設定情報。load()の際に更新される。
  private Preference selectedPref = new Preference();
  // 選択されている設定名。設定名リストが選択されたとき更新される。""ならデフォルト設定を意味する。
  private String selectedConfName = "";
  private JMenu selectionMenu;               // 選択メニュー
  private JRadioButtonMenuItem defaultRadioButtonMenuItem; //「標準」ﾒﾆｭｰｱｲﾃﾑ
  private JMenuItem customizeMenuItem;       //「ｶｽﾀﾏｲｽﾞ」ﾒﾆｭｰｱｲﾃﾑ
  private JMenuItem arrangmentMenuItem;      //「整理」ﾒﾆｭｰｱｲﾃﾑ
  private JMenuItem makeDefaultMenuItem; //「既定値にする」ﾒﾆｭｰｱｲﾃﾑ

  /**
   * オブジェクトを作成する。
   * @param mainFrame 親となるフレームで通常MainFrameを指定する。
   * @param mode フィールド定数のCOLOR_EDIT_MODEまたはSPECIFIC_EDIT_MODEのどち
   * らかを指定する。
   */
  public SpecificCustomizer(MainFrame mainFrame,int mode) {
    this.mainFrame = mainFrame;
    this.mode = mode;
  }
  
  /**
   * モードに応じてDB上のテーブル名を返す。
   */
  public String getTableName() {
    return tableNames[mode];
  }
  /**
   * このオブジェクトがCOLOR_EDIT_MODEがSPECIFIC_EDIT_MODEかを返す。
   * @return COLOR_EDIT_MODEまたはSPECIFIC_EDIT_MODEかのどちらか。
   */
  public int getMode() {
    return mode;
  }
  /**
   * 初期値として登録されている設定名から設定をChartModulePanelにロードする。
   * ChartInternalFrameから最初にコールされ、他からは呼び出されない。
   */
  public void loadDefault(ChartModulePanel module) {
    this.module = module;
    String confName = (getMode() == COLOR_EDIT_MODE) ?
      ".DefaultColorConfName" : ".DefaultSpecificConfName";
    selectedConfName =
      Config.data.getProperty(module.getClassName() + confName,"");
    load(selectedConfName);
    update = true;
  }
  //メニューからいつくかの設定の中から選択したときの処理。
  //設定(Propertiesになったもの)をDBからﾛｰﾄﾞしてﾓｼﾞｭｰﾙに反映させる
  //ﾓｼﾞｭｰﾙがもっているｶｽﾀﾏｲｽﾞﾊﾟﾈﾙにもﾛｰﾄﾞ内容を反映させる。
  private void load(String confName) {
    assert module != null;
    this.module = module;
    String className = module.getClassName();
    //色設定ﾓｰﾄﾞのとき
    if(getMode() == COLOR_EDIT_MODE) {
      if(module.getDefaultColor() == null) return; //ﾃﾞﾌｫﾙﾄ設定が無いと無効
      //selectedPref = new Preference(module.getDefaultColor());
      selectedPref.clear();
      selectedPref.copy( module.getDefaultColor() );
      boolean found = db.getConfigProperties(
        confName, className, selectedPref, getTableName());
      module.getColorCustomizePanel().setPreference(selectedPref);
      module.setColorConfig(selectedPref);
    }
    //仕様設定ﾓｰﾄﾞのとき
    else {
      if(module.getDefaultSpecific() == null) return; //ﾃﾞﾌｫﾙﾄ設定が無いと無効
      //selectedPref = new Preference(module.getDefaultSpecific());
      selectedPref.clear();
      selectedPref.copy( module.getDefaultSpecific() );
      boolean found = db.getConfigProperties(
        confName, className, selectedPref, getTableName()); 
      //if( ! found ) selectedPref.copy( module.getDefaultSpecific() ); 最初に読みこんでるから不要
      module.getSpecificCustomizePanel().setPreference(selectedPref);
      module.setSpecificConfig(selectedPref); //引数は実のとこ不要
    }
  }
  // ﾒﾆｭｰの名前。ﾓｰﾄﾞによって名前は切り替わる。
  private static final String [] selectionMenuNames = { 
    "色設定の切替","計算仕様の切替" };
  private static final String [] customizeMenuNames = { 
    "配色のカスタマイズ","計算仕様のカスタマイズ" };
  private static final String [] arrangmentMenuNames = { 
    "色設定の整理","計算仕様の整理" };
  /**
   * 機能メニューを作成して返す。このメニューはMainFrameのメニューバーに組み込まれ
   * GUIとして使用される。ChartModulePanelのgetDefaultColor()または
   * getDefaultSpecific()がnullを返す場合はDisenabledになったJMenuを返す。
   */
  public JMenu getMenu() {
    assert module != null;
    this.module = module;
    // moduleが変更されたときのみメニューを作り直す。moduleが変更されるのは、
    // loadDefault()が外部から呼ばれたときのみ。
    if(! update ) return selectionMenu;
    update = false;
    selectionMenu = new JMenu(selectionMenuNames[getMode()]);
    if(getMode() == COLOR_EDIT_MODE) {
      if(module.getDefaultColor() == null) {
        selectionMenu.setEnabled(false);
        return selectionMenu;
      }
    } else {
      if(module.getDefaultSpecific() == null) {
        selectionMenu.setEnabled(false);
        return selectionMenu;
      }      
    }
    //「選択を既定値に」メニューを作成
    makeDefaultMenuItem = new JMenuItem("選択を既定値に");
    makeDefaultMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String confName = (getMode() == COLOR_EDIT_MODE) ? 
          ".DefaultColorConfName" : ".DefaultSpecificConfName";
        Config.data
          .setProperty(module.getClassName() + confName, selectedConfName);
        update = true;
      }
    });
    // 「標準」メニューを作成
    defaultRadioButtonMenuItem = new JRadioButtonMenuItem("標準");
    defaultRadioButtonMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectedConfName = "";
        load("");
        update = true;
      }
    });
    // 「設定の整理」メニューを作成
    arrangmentMenuItem = new JMenuItem( arrangmentMenuNames[ mode ]);
    arrangmentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectedConfName = SpecificRemoveDialog.showDialog(
          mainFrame,
          module.getClassName(),
          selectedConfName,
          SpecificCustomizer.this);
        load(selectedConfName);
        update = true;
      }
    });

    // 「色のカスタマイズ」メニューを作成
    customizeMenuItem = new JMenuItem( customizeMenuNames[ mode ]);
    customizeMenuItem.addActionListener(new SpecificDialogHandler());
    
    //アクセラレーターキーを設定
    if(mode == 0) //色設定ダイアログはCtrl+Shift+Sで呼びだし
      customizeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | 
        java.awt.event.InputEvent.CTRL_MASK));
    else //計算設定ダイアログはCtrl+Sで呼びだし
      customizeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));

    Vector<String> vector = new Vector<String>();
    String className = module.getClassName();
    db.getConfigNames(className,vector,getTableName());
    ButtonGroup bGroup = new ButtonGroup();
    //設定名の一覧をﾒﾆｭｰｱｲﾃﾑにしてﾒﾆｭｰに登録
    for(String name:vector) {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
      if(name.equals(selectedConfName)) item.setSelected(true);
      bGroup.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          JMenuItem item = (JMenuItem)evt.getSource();
          selectedConfName = item.getText();
          load( selectedConfName );
        }
      });
      selectionMenu.add(item);
    }
    if(selectedConfName.equals("")) 
      defaultRadioButtonMenuItem.setSelected(true);
    bGroup.add(defaultRadioButtonMenuItem);
    selectionMenu.addSeparator();                  // |---------------------
    selectionMenu.add(defaultRadioButtonMenuItem); // | 標準
    selectionMenu.addSeparator();                  // |---------------------
    selectionMenu.add(makeDefaultMenuItem);        // | 選択を既定値に
    selectionMenu.add(customizeMenuItem);          // | カスタマイズ
    selectionMenu.add(arrangmentMenuItem);         // | 設定の整理
    selectionMenu.setEnabled(true);
    return selectionMenu;
  }
  //メニューがキックされたら設定パネルを表示する
  //設定パネルを開くたびにインスタンスを作り直していたのをやめにするため別クラスを用意した。
  
  class SpecificDialogHandler implements ActionListener {
    SpecificDialog dialog;
    
    SpecificDialogHandler() {
      CustomizePanel customPanel = (getMode() == COLOR_EDIT_MODE) ?
        module.getColorCustomizePanel() : module.getSpecificCustomizePanel();
      dialog = new SpecificDialog(  mainFrame,
                                    selectedPref,
                                    selectedConfName,
                                    customPanel,
                                    module.getClassName(),
                                    new ColorChangeHandler(),
                                    SpecificCustomizer.this);
    }
    
    public void actionPerformed(ActionEvent evt) {
      dialog.setConfName( selectedConfName );
      dialog.setVisible(true);
      update = true;
    }
  }
  // 設定変更ハンドラ
  //ボタンが押されたときに、サブクラスで表示されているチャートの設定
  //メソッドを呼び出す。それによりチャートの表示色や計算仕様変わる。

  class ColorChangeHandler implements ChangeListener {
    public void stateChanged(ChangeEvent evt) {
      assert module != null;
      SpecificDialog dialog = (SpecificDialog)evt.getSource();
      //プレビューの場合、SpecificDialogからプレビュー用のPreference得てセット
      int state = dialog.getState();
      if(state == SpecificDialog.PREVIEW) {
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(dialog.getPreviewPreference());
        } else {
          module.setSpecificConfig(dialog.getPreviewPreference());
        }
      }
      else if(state == SpecificDialog.CANCELED) {
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(selectedPref);
        } else {
          module.setSpecificConfig(selectedPref);
        }
      }
      else if(state == SpecificDialog.SAVE || state == SpecificDialog.USE ) {
        //この時点でSave処理はSpesificDialogで処理済みになっている
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(selectedPref);
        } else {
          module.setSpecificConfig(selectedPref);
        }
      } else 
        throw new UnsupportedOperationException(
          "SpecificDialogはこのstate=" + state + "をサポートしません");
    }
  }
}
