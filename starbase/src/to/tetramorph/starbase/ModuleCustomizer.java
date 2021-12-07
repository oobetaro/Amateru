/*
 * SpecificCustomizer.java
 *
 * Created on 2007/03/31, 8:21
 *
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.module.ChartCustomizeListener;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;
import to.tetramorph.util.XMLFileFilter;

/**
 * モジュールごとのスキンや計算条件設定を、管理する親クラス。
 * このクラスから派生して、スキン管理、計算条件管理の二つのサブクラスを作る。
 * どちらも同じ方法で設定を管理していて、ただしメニューの名前とか、設定データを
 * 保管しておくDBのテーブル名などが違う。その違いの部分を、サブクラスで帳尻を
 * あわせて実装する。
 * @author 大澤義鷹
 */
abstract class ModuleCustomizer implements ChartCustomizeListener,ChangeSpecificListener {

    /**
     * 設定メニュー中の「標準」を表す文字列
     */
    protected static final String DEFAULT = "標準";

    protected Frame ownerFrame;


    protected DB db = DBFactory.getInstance();

    // 選択されている設定情報。load()の際に更新される。
    protected Preference selectedPref = new Preference();

    // 選択されている設定名。設定名リストが選択されたとき更新される。
    // ""ならデフォルト設定を意味する。
    protected String selectedConfName = "";

    protected JMenuItem selectionMenuItem;        //「選択」
    protected JMenuItem arrangmentMenuItem;      //「整理」
    protected JMenuItem customizeMenuItem;       //「ｶｽﾀﾏｲｽﾞ」

    protected SpecificEditDialog specificEditDialog;
    protected CustomizePanel customizePanel;

    protected ChartModulePanel module;
    protected String moduleClassName;

    protected Properties prop = new Properties();
    /**
     * 「選択、整理、カスタマイズ」の三つのメニューアイテムの状態を設定する。
     */
    protected void setEnabled( boolean b ) {
        selectionMenuItem.setEnabled( b );
        arrangmentMenuItem.setEnabled( b );
        customizeMenuItem.setEnabled( b );
    }
    /**
     * オブジェクトを作成する。
     * @param ownerFrame 親となるフレームで通常MainFrameを指定する。
     */
    public ModuleCustomizer( Frame ownerFrame ) {
        this.ownerFrame = ownerFrame;
        initFileChooser();
    }

    /**
     * 現在選択されている設定名を返す。
     */
    public String getSelectedName() {
        return selectedConfName;
    }

    /**
     * 現在どの設定を使うか、その設定名を返す。これはサブクラスで実装されるが、
     * その際、不揮発性のプロパティから値(名前)を読み出す。そのときのキーは、
     * module.getClassName() + ".DefaultColorConfName"で求まる文字列を使う。
     * つまりモジュールごとの、デフォルト値を保管し管理している必要がある。
     * なお、インストール直後などまだキーと値が空の状態のとき、このメソッドは
     * ""を返す。nullが戻ることはない。
     */
    public abstract String getDefaultConfName();

    /**
     * 現在どの設定を使うか、その設定名を不揮発性プロパティに保管する。
     * これもgetDefaultConfName()同様、サブクラスで実装される。
     */
    public abstract void setDefaultConfName( String confName );

    /**
     * サブクラスのload()から呼び出され、SpecificDialogのインスタンスを作る。
     */
    protected void createSpecificDialog() {
        specificEditDialog = new SpecificEditDialog(
                                              ownerFrame,
                                              selectedPref,
                                              selectedConfName,
                                              getCustomizePanel(),
                                              module.getClassName(),
                                              this,
                                              ModuleCustomizer.this );
    }

    /**
     * チャートモジュールに指定された名前の設定をロードする。
     * チャートの複製の際に使用している。
     * ChartPane内から呼び出される。このメソッドはこのクラスの代表的な入口。
     * @param module チャートモジュール
     * @param confName 設定名 nullを指定するデフォルトに設定されているものを使う
     */
    public void load( ChartModulePanel module, String confName ) {
        this.module = module;
        this.moduleClassName = module.getClassName();
        selectedConfName = ( confName == null ) ?
            getDefaultConfName() : confName;
        if ( selectedConfName.equals( DEFAULT ) ) selectedConfName = "";
        load( selectedConfName );
    }


    /**
     * デフォルトとして登録されている設定をChartModulePanelにロードする。
     * ChartPaneから最初にコールされ、他からは呼び出されない。
     * ChartPane内から呼び出される。このメソッドはこのクラスの代表的な入口。
     */
    public void load( ChartModulePanel module ) {
        load( module, null );
    }

    /**
     * 現在セットされているモジュールに、指定された名前の設定をロードする。
     * このメソッドの中身はサブクラスで実装される。
     * このクラスに実装されている二つのload()、reload()なども、最後はこのメソッド
     * を呼び出して処理の完了とする。サブクラスではDBに登録されている設定情報を
     * 読み出し、このクラスのselectedPrefに読みこみ、またその内容を、モジュール
     * にセットして、その内容を反映させる。また読みこんだ設定を編集するための、
     * SpecificDialoのインスタンスを作り直す(createSpecificDialogを呼ぶ)。
     */
    public abstract void load( String confName );

    /**
     * 現在選択されているモジュールに
     */
    public abstract CustomizePanel getCustomizePanel();

    /**
     * モジュールに設定変更が起きたことを通達する。(サブクラスで実装。)
     */
    protected abstract void updateModuleSetting();

     /**
      * ○○設定の選択、○○設定の整理、二つのメニューから、SpecificChangerDialog
      * を表示するハンドラ。このダイアログは、選択と整理両方の機能をもっていて、
      * 「選択」のときは目障りな整理に必要なボタンを隠しておくことができる。
      * 選択用として開くか、整理用として開くかは、showDialog()の引数で指定するが、
      * このハンドラのコンストラクタでそれを渡し、オブジェクトを作り、メニュー
      * アイテムにaddActionListenerする。
      */
    class SelectionMenuHandler implements ActionListener {
        boolean isEditMode;
        SelectionMenuHandler( boolean isEditMode ) {
            this.isEditMode = isEditMode;
        }
        @Override
        public void actionPerformed( ActionEvent evt ) {
            Preference pref = new Preference();
            pref.copy( selectedPref );
            String result = SpecificChangerDialog.showDialog(
                                                ownerFrame,
                                                moduleClassName,
                                                selectedConfName,
                                                ModuleCustomizer.this,
                                                isEditMode );
            if ( result != null ) {
                selectedConfName = result;
                load( selectedConfName );
                setDefaultConfName( selectedConfName );
            } else {
                selectedPref.copy( pref );
                getCustomizePanel().setPreference( selectedPref );
                updateModuleSetting();
            }
        }
    }

    /**
     * ○○設定の、選択、編集、整理の各ダイアログを呼び出すメニューを作成する。
     * 内部クラスSelectionMenuHandlerを、そのメニューにリスナとして登録する。
     * 作成されたメニューはgetMenu()で取得できる。
     * このクラスは、サブクラスのコンストラクタ内で、親クラス(つまりこのクラス)
     * のコンストラクタが呼ばれたあと、このメソッドを呼び出してメニューインスタンス
     * を用意する。メニューインスタンスはフィールド変数に保管され、サブクラスから
     * そのメニューにショートカットキーを割り与えることもできる。
     */
    protected void createMenu() {
        customizeMenuItem = new JMenuItem( prop.getProperty("customizeMenuName" ));
        customizeMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                showCustomizeDialog();
            }
        });

        arrangmentMenuItem = new JMenuItem( prop.getProperty("arrangmentMenuName" ));
        arrangmentMenuItem.addActionListener( new SelectionMenuHandler( true ));

        selectionMenuItem = new JMenuItem( prop.getProperty("selectionMenuName"));
        selectionMenuItem.addActionListener( new SelectionMenuHandler( false ));
    }

    /**
     * カスタマイズダイアログを現在の設定で可視化する。これはChartModulePanel
     * のサブクラス、つまりモジュール側から、設定編集ダイアログ(SpecificEditDialog)
     * を表示させたい場合に使用される。
     */
    @Override
    public void showCustomizeDialog() {
        specificEditDialog.setVisible( true );
    }

    /**
     * 設定をモジュールにリロードする。
     */
    public void reload() {
        load( selectedConfName );
    }

    /**
     * サブクラスのコンストラクタ内で登録されたプロパティを読み出す。
     * SpecificChangerDialog,SpecificEditDialog,SpecificSaveDialog,各MenuItemは、
     * このプロパティの値に従って、表題を決定している。またアクセスする、DBの
     * テーブル名なども格納される。
     * サブクラスのコンストラクタでは、メニューやダイアログの名前を、このクラスの
     * Propertiesオブジェクトに格納する。このクラスとサブクラスのメソッドは、
     * その登録情報に従って、メニューのテキストやダイアログのタイトルをセットする。
     */
    public String getProperty( String key ) {
        return prop.getProperty( key );
    }

    /**
     * 表示メニューを返す。
     * <pre>
     * |設定名リスト・・・｜標準｜現在の選択をデフォルト|カスタマイズ|設定の整理|
     * </pre>
     * このメニューはMainFrameのメニューバーで使用される。
     * ChartModulePanelのgetDefaultColor()またはgetDefaultSpecific()が
     *  nullを返す場合はDisenabledになったJMenuを返す。
     * @param menu 表示メニューを書きこむメニューオブジェクト。引数で与えた
     * メニューオブジェクトに、表示メニューがaddされる。
     */
    public void getMenu( JMenu menu ) {
        menu.add( selectionMenuItem );
        menu.add( customizeMenuItem );
        menu.add( arrangmentMenuItem );
    }

    private static JFileChooser fileChooser;
    private static XMLFileFilter fileFilter;

    private void initFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter( new XMLFileFilter() );
        fileChooser.setMultiSelectionEnabled( false );
    }

    /**
     * モジュールの設定をインポートする。
     */
    public void importConfigure() {
        Object [] selValues = { "はい","いいえ","中止" };
        fileChooser.setDialogTitle( "インポートするファイルを指定してください〔複数選択も可能〕" );
        fileChooser.setMultiSelectionEnabled( true );
        fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
        String path = Config.usr.getProperty( "SpecificImportDirectory","" );
        if ( ! path.isEmpty() ) {
            File dir = new File( path );
            if ( dir.isDirectory() ) {
                fileChooser.setCurrentDirectory( dir );
            }
        }
        int result = fileChooser.showOpenDialog( ownerFrame );
        if ( result != JFileChooser.APPROVE_OPTION ) return;
        File [] files = fileChooser.getSelectedFiles();
        if ( files == null || files.length == 0 ) return;
        Config.usr.setProperty( "SpecificImportDirectory",
                  fileChooser.getCurrentDirectory().getAbsolutePath() );
        StringBuilder sb = new StringBuilder();
        Loop:
        for ( File sel_file : files ) {
            //入力された名前が".xml"の拡張子を持たないときは修正する
//            if ( ! sel_file.getName().toLowerCase().matches(".*\\.xml$") ) {
//                sel_file = new File( sel_file.getAbsolutePath().concat(".xml"));
//            }
            if ( ! sel_file.exists() ) {
                sb.append( String.format("\"%s\"は存在しません。\n",
                           sel_file.getName() ) );
                continue;
            }
            Preference pref = new Preference();
            loadProperties( pref, sel_file );
            String tableName = prop.getProperty("tableName");
            String prefTableName = pref.getProperty( "tableName" );
            String prefSpecificName = pref.getProperty( "specificName" );
            String prefClassName = pref.getProperty( "moduleClassName" );
            if ( prefClassName     == null ||
                 prefTableName     == null ||
                 prefSpecificName  == null     ) {
                sb.append( String.format(
                    "「%s」はアマテルのモジュール設定ファイルではありません。\n",
                    sel_file.getName() ) );
                continue;
            }
            // ロードした設定プロパティの、moduleClassNameの値が異なる場合は、
            // 別のモジュールの設定ファイルなので、受け入れを拒否する
            if ( ! ( prefClassName.equals( moduleClassName ) &&
                     prefTableName.equals( tableName ) ) ) {
                String fname = sel_file.getName(); //pref.getProperty("moduleTitle","");
                String mes = String.format(
                    "「%s」は別チャートのものか、または" +
                    "設定の種類（スキン｜計算条件）が\n" +
                    "異なるためインポートできません。\n",
                    fname );
                sb.append( mes );
                continue;
            }
            Vector<String> list = new Vector<String>();
            db.getConfigNames( moduleClassName, list, tableName );
            boolean ok = false;

            while ( ! ok ) {
                ok = true;
                for ( int i=0; i<list.size(); i++ ) {
                    if ( list.get(i).equals( prefSpecificName )) {
                        String res = JOptionPane.showInputDialog(
                                           ownerFrame,
                                           String.format(
                                               "%sという名前はすでに登録" +
                                               "されています。上書きしますか？",
                                           prefSpecificName ),
                                           prefSpecificName );
                        if ( res == null ) continue Loop; //「いいえ」
                        if ( res.equals( prefSpecificName ) ) { //「はい」(保存)
                            ok = true;
                            break;
                        } else {
                            prefSpecificName = res;
                            ok = false; //名前を変更した場合はもう一度検査する
                            break;
                        }
                    }
                }
            }
            //改名される場合もあるので、最終的に決まった設定名を再登録する。
            pref.setProperty( "specificName", prefSpecificName );
            db.setConfigProperties( prefSpecificName, moduleClassName,
                                                pref,       tableName  );
            sb.append( prefSpecificName ).append( "をインポート\n" );
        }
        messageDialog( "インポート処理完了\n" + sb.toString() );
    }

    /**
     * モジュールのスキンや計算条件の設定情報をエクスポートする。
     * @param confNameList 設定名が入ったリスト。
     */
    public void exportConfigure( List<String> confNameList ) {
        // confNameListの名前で、DBから設定情報を取り出し、設定名ごとにPreference
        // に入れて、prefListを作る。名前のリストが一つなら、一つのPreferenceだが
        // 複数の名前が入ってきたときは、複数のPreferenceが作成される。
        List<Preference> prefList = new ArrayList<Preference>();
        for ( String confName : confNameList ) {
            Preference pref = new Preference();
            boolean found = db.getConfigProperties( confName,
                                                     moduleClassName,
                                                     pref,
                                                     getProperty("tableName") );
            if ( ! found ) {
                errorDialog( String.format(
                         "「%s」という設定は登録されていません。", confName ) );
                return;
            }
            pref.setProperty( "tableName", prop.getProperty("tableName") );
            pref.setProperty( "moduleClassName", moduleClassName );
            pref.setProperty( "specificName",confName );
            pref.setProperty( "moduleTitle", module.toString());
            prefList.add( pref );
        }
        //ファイルチューザーを開き保存先を指定する。
        ExportConfirmDialog dialog = new ExportConfirmDialog(
                specificEditDialog,"設定のエクスポート",moduleClassName,prefList);
	assert SwingUtilities.isEventDispatchThread();
        dialog.setVisible(true);
    }

    private static void showDialog( String message ) {
        Object [] selValues = { "はい","いいえ","中止" };
        JOptionPane.showConfirmDialog( JOptionPane.getRootFrame(),
            message,
            "title",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
            );
    }
    private static void showDialog2( String message ) {
        Object [] selValues = { "Yes","No","Cancel" };
        JOptionPane.showInputDialog( JOptionPane.getRootFrame(),
            message,
            "title",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            selValues[0]
            );
    }
    public static void main( String args[] ) {
        showDialog("ファイルが重複しています。上書きしますか？");
    }
    /**
     * 指定された設定をXML形式でファイルにエクスポートする。
     * ファイルチューザーを開いて、出力ファイルをユーザに問い合わせる。
     * ファイルチューザーのカレントディレクトリはレジューム機能つき。
     * @param confName 設定名
     */
    public void exportConfigure( String confName ) {
        Preference pref = new Preference();
        boolean found = db.getConfigProperties( confName,
                                                 moduleClassName,
                                                 pref,
                                                 getProperty("tableName") );
        if ( ! found ) {
            errorDialog( String.format(
                         "「%s」という設定は登録されていません。", confName ) );
            return;
        }
        fileChooser.setMultiSelectionEnabled( false );
        fileChooser.setDialogTitle( "設定のエキスポート" );
        File file = new File( confName + ".xml" );
        String path = pref.getProperty( "SpecificExportDirectory" );
        if ( ! path.isEmpty() ) {
            File dir = new File( path );
            if ( dir.isDirectory() ) {
                fileChooser.setCurrentDirectory( dir );
            }
        }
        fileChooser.setSelectedFile( file );
        int result = fileChooser.showSaveDialog( ownerFrame );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            //ファイルチューザーの「保存」ボタンが押された場合
            File sel_file = fileChooser.getSelectedFile();
            File dir = fileChooser.getCurrentDirectory();
            pref.put( "SpecificExportDirectory", dir.getAbsolutePath() );
            // ファイル名の拡張子が.xmlかを検査。ちがうときは修正。
            String name = sel_file.getName();
            if ( ! name.toLowerCase().matches(".*\\.xml$") ) {
                sel_file = new File( sel_file.getAbsolutePath().concat(".xml"));
            }
            // 保存のまえに既存ファイルに上書きになるかどうかの検査
            if ( sel_file.exists() ) {
                int res = JOptionPane.showConfirmDialog( ownerFrame,
                          String.format(
                              "「%s」はすでに存在します。上書きしますか？",
                              sel_file.getName() ),
                          "保存の確認",
                          JOptionPane.YES_NO_OPTION );
                if ( res == JOptionPane.NO_OPTION ) return;
            }
            pref.setProperty( "tableName", prop.getProperty("tableName") );
            pref.setProperty( "moduleClassName", moduleClassName );
            pref.setProperty( "specificName",confName );
            pref.setProperty( "moduleTitle", module.toString());
            saveProperties( pref, sel_file );
            messageDialog( String.format(
                           "「%s」を保存しました。", sel_file.getName())   );
        } else if ( result == JFileChooser.CANCEL_OPTION ) {

        }
    }

//    public static void main( String args[] ) {
//        String name = "hoge.XMLU";
//        if ( name.toLowerCase().matches(".*\\.xml$") ) {
//            System.out.println("xmlで終わった");
//        }
//    }

    private void errorDialog( String message ) {
        JOptionPane.showMessageDialog( ownerFrame,
            message,
            "エラー",
            JOptionPane.ERROR_MESSAGE );
    }

    private void messageDialog( String message ) {
        JOptionPane.showMessageDialog( ownerFrame,message,"レポート",JOptionPane.INFORMATION_MESSAGE);
    }
    private static boolean saveProperties( Properties prop, File file ) {
        FileOutputStream fis = null;
        boolean ok = true;
        try {
            fis = new FileOutputStream( file );
            prop.storeToXML( fis,"SpecificData" );
        } catch ( IOException e ) {
            Logger.getLogger( ModuleCustomizer.class.getName())
                    .log(Level.SEVERE,null,e);
            ok = false;
        } finally {
            try { fis.close(); } catch ( Exception e ) { }
        }
        return ok;
    }

    private static boolean loadProperties( Properties prop, File file ) {
        FileInputStream fis = null;
        boolean ok = true;
        try {
            fis = new FileInputStream( file );
            prop.loadFromXML( fis );
        } catch ( IOException e ) {
            Logger.getLogger( ModuleCustomizer.class.getName())
                    .log(Level.SEVERE,null,e);
            ok = false;
        } finally {
            try { fis.close(); } catch ( Exception e ) { }
        }
        return ok;
    }
    private static final char [] ERROR_CHARS = {
        '\"','%',',','.','/',':',';',
        '<','=','>','?','\'','\\','　'
    };

    private static boolean isErrorCode(char c) {
        for ( char out : ERROR_CHARS ) {
            if ( c == out ) return true;
        }
        return false;
    }

    /**
     * 異常な設定名かどうかを検査して、異常なものであれぱtrueを返す。
     */
    protected static boolean isIllegalName(String name) {
        if( name == null ) return true;
        name = name.trim();
        if ( name.equals("") )     return true;
        if ( name.equals( DEFAULT ) ) return true;
        if ( name.length() >= 16 ) return true;
        char [] buf = name.toCharArray();
        for ( char c : buf ) {
            if ( isErrorCode( c ) ) return true;
        }
        return false;
    }
}
