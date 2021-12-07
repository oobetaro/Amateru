/*
 * ChartPane.java
 *
 * Created on 2006/09/11, 4:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.io.File;
import java.io.FilePermission;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.module.ChartTab;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.util.IconLoader;
import static java.lang.System.getProperty;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.module.ChartModuleMode;
import to.tetramorph.starbase.module.DictionaryActionFile;
import to.tetramorph.util.ImageSelection;
import to.tetramorph.util.Preference;

/**
 * ホロスコープをはじめとして、さまざまな占星術上のグラフを描くパネルで、
 * MultiTabbedPaneのタブの中にはめ込まれる。
 * メインメニューバーのチャート(C)、時間(T)にセットする二つのメニューを提供する。
 * チャート(C)は、どのようなグラフを描くかモジュールを選択する。<br>
 * 時間(T)は、ヒストリーをロードしたりできる。<br>
 *
 * 各ChartPanelごとにすべてのモジュールのインスタンスをまとめて用意していたが、
 * ChartPanelを増やすたびに、すべてのプラグインのインスタンスが作成されるのは、
 * あまりにもリソースを消費するのでやめた。
 * その分、切替のレスポンスが遅くなるのは止むを得ない。
 *
 * 入力メソッド
 * setNatal()とaddNatalがNatalデータの入口。
 */
final class ChartPane extends  JPanel implements ChartTab {
    private static Icon [] icons;
    private static final String [] options = {
                                      "はい(Y)", "いいえ(N)", "すべていいえ" };
    static {
        icons = new Icon[5];
        icons[0] = IconLoader.getImageIcon("/resources/images/List.male.png");
        icons[1] = IconLoader.getImageIcon("/resources/images/List.female.png");
        icons[2] = IconLoader.getImageIcon("/resources/images/List.none.png");
        icons[3] = IconLoader.getImageIcon("/resources/images/List.clock.png");
        icons[4] = IconLoader.getImageIcon("/resources/images/List.composit.png");
    }

    private ChartModulePanel module = null;
    //private JRadioButtonMenuItem [] moduleRadioButtonMenuItems;
    private ButtonGroup moduleButtonGroup;
    private TimePanel timePanel;
    private JMenuItem addHistoryMenuItem =
        new JMenuItem("ヒストリーに追加");
    private JMenuItem editNatalMenuItem =
        new JMenuItem("現在のデータを編集");
    private JMenuItem saveCompositDataMenuItem =
        new JMenuItem("コンポジットとして保存");
    private JMenuItem stopTimerMenuItem =
        new JMenuItem("アニメーション停止");
    private JMenuItem copyTransitMenuItem =
        new JMenuItem("トランジットをコピー");
    private JMenuItem peastTransitMenuItem =
        new JMenuItem("トランジットをペースト");
    private JMenuItem clipboardMenuItem = new JMenuItem("クリップボードにコピー");
    private JMenuItem saveImageMenuItem = new JMenuItem("画像をファイルに保存");
    private JMenuItem myChartMenuItem = new JMenuItem("図をマイチャートに登録");
    private JMenuItem nowMenuItem = new JMenuItem("表示条件を今ボタンに登録");
    private DataExplorer explorer;
    private JMenu editMenu;
    private Frame owner;
    private int moduleNum;
    private ModuleCustomizer colorSpecificCustomizer;
    private ModuleCustomizer calcSpecificCustomizer;
    private SabianDialogHandler sabianHandler;
    private String title = null;
    private ChartPanelListener cpl;
    private static ImageSaveFileChooser imageSaver = new ImageSaveFileChooser();
    private static ImageSizeMenu imageSizeMenu = new ImageSizeMenu();
    private Dictionary dictionary;
    /**
     * 複製用コンストラクタで引数であたえられたchartPaneと同じ内容のオブジェクト
     * を複製する。完全に同じというわけではないが、時間パネルにセットされている
     * ネイタルデータやトランジットデータがデープコピーされ複製される。
     * フレームのサイズや位置、選択されているモジュールなども再現される。
     */
    public ChartPane( ChartPane chartPane ) {
        super(new BorderLayout(0,0));
        this.explorer = chartPane.explorer;
        this.editMenu = chartPane.editMenu;
        this.owner = chartPane.owner;
        this.sabianHandler = chartPane.sabianHandler;
        this.dictionary = chartPane.dictionary;
        timePanel = new TimePanel3(this);
        timePanel.dataCopy(chartPane.timePanel);
        moduleSetup();
        init();
        String colorConf = chartPane.colorSpecificCustomizer.getSelectedName();
        String calcConf = chartPane.calcSpecificCustomizer.getSelectedName();
        selectModule( chartPane.getSelectedModuleNumber(),colorConf,calcConf );
        setChartPaneListener(chartPane.cpl);
        this.setModuleMode( chartPane.getSelectedModuleMode() );
    }
    /**
     * オブジェクトを作成する。
     * @param explorer      システムのデータベース
     * @param editMenu      メニューバーにある時間メニューのインスタンス
     * @param sabianHandler システム全体で共有しているサビアン辞書
     * @param owner         親フレームで、ダイアログを開くときに必要になる。
     */
    public ChartPane( DataExplorer explorer,
                        JMenu editMenu,
                        SabianDialogHandler sabianHandler,
                        Dictionary dictionary,
                        Frame owner) {
        super(new BorderLayout(0,0));
        this.explorer = explorer;
        this.editMenu = editMenu;
        this.owner = owner;
        this.sabianHandler = sabianHandler;
        this.dictionary = dictionary;
        timePanel = new TimePanel3(this);
        moduleSetup();
        init();
    }
    //
    private static Permission [] getPermissions() {
        String sp = File.separator;
        String tempPath = getProperty("app.home") + sp + "temp" + sp + "-";
        String swePath  = getProperty("swe.path") + sp + "-";

        Permission [] perms = new Permission [] {
            new FilePermission( tempPath, "read,write,delete" ),
            new FilePermission( swePath,  "read" ),
            new PropertyPermission( "swe.path", "read"),
            new PropertyPermission( "DefaultTime", "read"),
            new PropertyPermission( "app.topounit", "read"),
            new PropertyPermission( "app.angleunit", "read")
        };
        return perms;
    }
    // モジュールのクラスファイルを保管する。
    private static List<Class> classList;       //モジュールクラス
    private static List<String> classNameList;  //モジュールのフルクラス名
    private static List<String> moduleNameList; //三重円とか具体名のリスト
    private static List< ChartModuleMode [] > moduleModeList;
    /**
     * モジュールのクラスファイルを読込、このオブジェクト内部のstaticの
     * classListにクラス(Classであり、インスタンス化されていない)を、プラグイン
     * の名前をclassNameListに格納する。classListとclassNameListは対になっている。
     * このメソッドは、アプリ起動時にStarbase.exec()内で呼び出される。
     * ロードには数秒の時間がかかる。
     * 読みこまれたデータはインスタンス化されるまえのクラスの設計図とみなせ、
     * チャートが作成されるときは、その設計図にnewInstance()を実行して、
     * インスタンスを作成する。
     */
    public static void loadModules() {
        String moddir = System.getProperty("app.cmod",""); //Config.usr.getProperty("ChartModule.dir","");
        if ( moddir.isEmpty() ) throw
            new IllegalStateException("'ChartModule.dir' property not found.");
        classList = new ArrayList<Class>();      //クラス保管用
        classNameList = new ArrayList<String>(); //クラス名保管用
        moduleNameList = new ArrayList<String>(); //モジュール名保管用
        moduleModeList = new ArrayList< ChartModuleMode [] >();
//        String path = System.getProperty("app.home") + File.separator + "temp"
//            + File.separator + "-";
        try {
            File classFile = new File( moddir );
            String[] files = classFile.list();
            for ( int i = 0; i < files.length; i++ ) {
                if (files[i].endsWith(".jar")) {
                    File file = new File(classFile,files[i]);
                    JarFile jarFile = new JarFile(file);
                    Manifest mf = jarFile.getManifest();
                    Attributes att = mf.getMainAttributes();
                    URL url = file.getCanonicalFile().toURI().toURL();
                    //Jawa Web Startがjarを読みこむのに使ったクラスローダーを
                    //使ってロードしなければならないので、それを親として与え
                    //モジュール用のクラスローダーを作成する。
                    ModuleClassLoader loader =
                        new ModuleClassLoader( new URL[] { url });
                    for ( int j=1; ;j++ ) {
                        String className = att.getValue("ChartModule-Class" + j);
                        if ( className == null ) break;
                        System.out.println("Load ChartModule: " + className);
                        Class plugin =
                            loader.loadModule( className, getPermissions() );
                        classList.add( plugin );
                        classNameList.add( className );
                        ChartModulePanel p =
                            (ChartModulePanel)plugin.newInstance();
                        moduleNameList.add( p.toString() );
                        moduleModeList.add( p.getModuleModes() );
                        SplashWindow.getInstance().addValue(3);
                    }
                }
            }
        } catch ( ClassNotFoundException e ) {
            System.out.println("ChartModule Not Found : " + e);
        } catch ( Exception ex ) {
            Logger.getLogger(ChartPane.class.getName())
                    .log(Level.SEVERE,null,ex);
        }
    }
    /**
     * プラグインの名前一覧を返す。読みこみ順でならんでいる。
     */
    public List<String> getModuleNameList() {
        return moduleNameList;
    }
    /**
     * 選択可能なチャートモジュールの数を返す。
     * EDTに関係なく呼び出せる。
     */
    public int getModuleCount() {
        return moduleNameList.size();
    }

    /**
     * チャート描画用のモジュール群を準備する。
     * チャート(C)のメニューリストを作成し、モジュール呼びだしのイベント処理を
     * 登録する。
     */
//    private void moduleSetup() {
//        int moduleQuant = moduleNameList.size();
//        moduleRadioButtonMenuItems =
//                             new JRadioButtonMenuItem[ moduleQuant ];
//        moduleButtonGroup = new ButtonGroup();
//        for ( int i=0; i < moduleQuant; i++ ) {
//            // プラグイン名を取得してメニューにセット
//            moduleRadioButtonMenuItems[i] =
//                new JRadioButtonMenuItem( moduleNameList.get(i) );
//            moduleRadioButtonMenuItems[i].setActionCommand( ""+i );
//            moduleButtonGroup.add( moduleRadioButtonMenuItems[i] );
//            moduleRadioButtonMenuItems[i]
//                .addActionListener( new ActionListener() {
//                public void actionPerformed( ActionEvent evt ) {
//                    String value = moduleButtonGroup.getSelection().
//                                                            getActionCommand();
//                    int i= Integer.parseInt( value );//選択メニュー番号を得て
//                    selectModule(i);  //モジュールを選択
//                    timePanel.set();
//                }
//            });
//        }
//        moduleRadioButtonMenuItems[0].setSelected( true );
//    }

    List<JMenuItem> moduleMenuList;
    /**
     * メニューバーの「チャート（Ｃ）」に出現する、モジュール選択メニューを作る
     * ための下準備を行い、各配列に必要な情報を格納しておく。
     * JRadioButtonMenuItem(複数)は、ButtonGroupを用意してユニーク選択にしたり。
     */
    private void moduleSetup() {
        int moduleQuant = moduleNameList.size();
        moduleMenuList = new ArrayList<JMenuItem>();
        moduleButtonGroup = new ButtonGroup();
        for ( int i=0; i < moduleQuant; i++ ) {
            ChartModuleMode [] modes = moduleModeList.get(i);
            String moduleName = moduleNameList.get(i);
            JRadioButtonMenuItem item = null;
            if ( modes == null ) {
                item = new JRadioButtonMenuItem( moduleName );
                item.setActionCommand( ""+i );
                moduleButtonGroup.add( item );
                item.addActionListener( new ActionListener() {
                    @Override
                    public void actionPerformed( ActionEvent evt ) {
                        String value = moduleButtonGroup.getSelection().
                                                        getActionCommand();
                        int i= Integer.parseInt( value );//選択メニュー番号を得て
                        selectModule(i);  //モジュールを選択
                        timePanel.set();
                    }
                });
                moduleMenuList.add( item );
            } else {
                JMenu jMenu = new JMenu( moduleName );
                for ( int j = 0; j < modes.length; j++ ) {
                    item = new JRadioButtonMenuItem( modes[j].getTitle() );
                    item.setActionCommand( i + "," + j );
                    moduleButtonGroup.add( item );
                    item.addActionListener( new ActionListener() {
                        @Override
                        public void actionPerformed( ActionEvent evt ) {
                            String value = moduleButtonGroup
                                .getSelection().getActionCommand();
                            String [] v = value.split(",");
                            //選択されたモジュール番号を取得
                            int moduleNum = Integer.parseInt( v[0] );
                            int modeNum = Integer.parseInt( v[1] );
                            selectModule( moduleNum );  //モジュールを選択
                            ChartModuleMode [] modes =
                                moduleModeList.get( moduleNum );
                            setModuleMode( modes[ modeNum ] );
                            timePanel.set();
                        }
                    });
                    jMenu.add( item );
                }
                moduleMenuList.add( jMenu );
            }
        }
    }
    /**
     * モジュール選択用のメニューアイテムをこのオブジェクトがmenuにセットして返す。
     * menuには参照で書き込まれる。
     */
    public JMenu getChartMenu( JMenu menu ) {
        menu.removeAll();
        for ( int i=0; i<moduleMenuList.size(); i++ ) {
            JMenuItem item = moduleMenuList.get( i );
            menu.add(item);
        }
        //現在選択中のモジュールメニューのラジオボタンを選択状態にする。
        //ちょっとめんどくさい。
        JMenuItem item = moduleMenuList.get( moduleNum );
        if ( item instanceof JRadioButtonMenuItem ) {
            //モードを持たないモジュールの場合
            ((JRadioButtonMenuItem)item).setSelected( true );
        } else if ( item instanceof JMenu ) {
            //モード付きモジュールはサブメニューにモードメニューが入ってる
            ChartModuleMode [] modes = getSelectedModuleModes();
            ChartModuleMode cm = module.getModuleMode();
            JMenu modeMenu = (JMenu)item;
            for ( int i = 0; i < modeMenu.getItemCount(); i++ ) {
                if ( modes[i] == cm ) {
                    JMenuItem it = modeMenu.getItem( i );
                    ((JRadioButtonMenuItem)it).setSelected( true );
                }
            }
        }
        //moduleRadioButtonMenuItems[moduleNum].setSelected(true);
        return menu;
    }

//    public JMenu getChartMenu( JMenu menu ) {
//        menu.removeAll();
//        for ( int i=0; i < moduleRadioButtonMenuItems.length; i++ )
//            menu.add( moduleRadioButtonMenuItems[i] );
//        moduleRadioButtonMenuItems[moduleNum].setSelected(true);
//        return menu;
//    }

    /**
     * 指定番号のプラグインのインスタンスを作成して返す。
     */
    private ChartModulePanel createChartModulePanel( int num ) {
        Class plugin = classList.get( num );
        String className = classNameList.get( num );
        ChartModulePanel p = null;

        //Preference conf_data = new Preference();
        //PrefUtils.copy(Conf.data, conf_data);

        Preference usr_pref = Preference.getNewPreference( Config.usr );
        try {
            p = (ChartModulePanel)plugin.newInstance();
            p.setConstructArgs( this, sabianHandler, dictionary,
                                usr_pref, className, owner );
        } catch ( Exception e ) {
            Logger.getLogger(ChartPane.class.getName()).log( Level.SEVERE,
                    "モジュールのインスタンスが作成できません :" + className , e );
        }
        return p;
    }
    /**
     * 指定番号のモジュールを選択。このオブジェクトを作成したあと、このメソッドで
     * どれかのモジュールを選択しないと表示されない。EDTから呼び出すこと。
     */
    public void selectModule( int num ) {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        //if ( module == moduleList.get(num) ) return;
        if ( module != null ) {
            //すでにモジュールがセットされてるならレイアウトマネージャから削除
            getLayout().removeLayoutComponent( module );
            remove( module ); //Containarからも削除
        }
        //this.module = moduleList.get( num );
        this.module = createChartModulePanel( num );
        this.moduleNum = num;
        add( module, BorderLayout.CENTER );
        timePanel.setModule( module );
        imageSizeMenu.update( 0.707f, module.getClassName() );
        getEditMenu( editMenu );
        colorSpecificCustomizer.load( module );
        calcSpecificCustomizer.load( module );
        //モジュール側からも設定パネルが開けるように、カスタマイザを登録してやる
        module.setCustomizeListeners( calcSpecificCustomizer,
                                      colorSpecificCustomizer );
        revalidate();
        repaint();
        if ( cpl != null ) cpl.selectedChartModule( this );
    }
    /**
     * ChartPaneの複製用で、複製元の設定と同じ設定を複製先に適用するためにこの
     * メソッドがある。selectModule(int)とほとんど同じコードで最適化の余地は
     * 多いにあるが、エンバグが怖いので分けてある。
     * @param colorConfName スキンの設定名
     * @param specificConfName 計算条件の設定名
     */
    public void selectModule( int num,
                                 String colorConfName,
                                 String specificConfName )       {
        //if ( module == moduleList.get(num) ) return;
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        if ( this.module != null ) {
            //すでにモジュールがセットされてるならレイアウトマネージャから削除
            getLayout().removeLayoutComponent(this.module);
            remove(module); //Containarからも削除
        }
        //this.module = moduleList.get( num );
        this.module = createChartModulePanel( num );
        this.moduleNum = num;
        add( module, BorderLayout.CENTER );
        timePanel.setModule( module );
        imageSizeMenu.update( 0.707f, module.getClassName() );
        getEditMenu( editMenu );
        // ここがsetModule(int)と異なる
        colorSpecificCustomizer.load( module, colorConfName  );
        calcSpecificCustomizer.load( module, specificConfName);
        //モジュール側からも設定パネルが開けるように、カスタマイザを登録してやる
        module.setCustomizeListeners( calcSpecificCustomizer,
                                     colorSpecificCustomizer );
        revalidate();
        repaint();
        if ( cpl != null ) cpl.selectedChartModule(this);
    }

    public void selectModule( String moduleName,
                                ChartModuleMode cmm,
                                String skinName,
                                String calcName ) {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        for ( int i = 0; i < classNameList.size(); i++ ) {
            if ( classNameList.get(i).equals( moduleName ) ) {
                selectModule( i, skinName, calcName );
                this.setModuleMode( cmm );
            }
        }
    }

    /**
     * 選択されているモジュール番号を返す。
     */
    public int getSelectedModuleNumber() {
        return moduleNum;
    }

    /**
     * 選択されているモジュールのクラス名を返す。
     */
    public String getSelectedModuleClassName() {
        return module.getClassName();
    }

    /**
     * 現在選択中のモジュールに対して、モードを設定する。
     * ただしモードを持たないモジュールの場合は無視される。
     */
    public void setModuleMode( ChartModuleMode mode ) {
        this.module.setModuleMode( mode );
    }

    /**
     * 現在選択中のモジュールのモード一覧リストを返す。
     * モードを持たないモジュールの場合はnullを返す。
     */
    public ChartModuleMode [] getSelectedModuleModes() {
        return this.module.getModuleModes();
    }
    /**
     * 現在選択中のモジュールのモード情報を返す。
     * モードを持たないモジュールの場合はnullを返す。
     */
    public ChartModuleMode getSelectedModuleMode() {
        return this.module.getModuleMode();
    }
    /**
     * 現在選択中のモジュールの判読用の名前を返す。EDTに関係なく呼び出せる。
     */
    public String getSelectedModuleName() {
        return module.toString();
    }
    /**
     * ChartPaneを複製したあと、そのオブジェクトをMultiTabbedPaneにinsertし、
     * このメソッドを呼び出す事で、チャートタブに名前とアイコンがセットされる。
     * 名前とアイコンをタブにセットするのは、チャートモジュール側の仕事で、
     * チャートモジュールがそれを行うのは、普通はsetNatalされたときなのだが、
     * 複製では内部的にNatalデータの複製を行うが、複製したNatalリストをモジュール
     * に通達はしない。このメソッドはその通達作業を行う。
     * 通達を行うと、Main.ChartPaneHandlerクラスが、少々複雑なプロセスを経て
     * 呼び出され、Main.multiPaneにアイコンと名前を登録する。
     */
    public void validateNatal() {
        timePanel.set();
    }

    //二つのコンストラクタから呼び出される共通の初期化ルーチン
    private void init() {
        //タイムコントローラの閉じるボタンのアクション処理。パネルを閉じる。
        //timePanel.getCloseButton().addActionListener(new CloseButtonHandler());
        //トランジットをヒストリーに追加のメニューイベント
        addHistoryMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addHistoryActionPerformed(evt);
            }
        });
        //現在のデータを編集(コンポジットなら全データ、その他なら選択データ)
        editNatalMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                editDataActionPerformed();
            }
        });
        //コンポジットデータとして保存
        saveCompositDataMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveCompositActionPerformed();
            }
        });
        //アニメーション停止
        stopTimerMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //stopTimer();
                timePanel.stopTimer();
                requestFocus();
            }
        });
        //トランジットをコピー
        copyTransitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                TransitClip.transit = timePanel.getTransit();
            }
        });
        //トランジットをペースト
        peastTransitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( TransitClip.transit != null ) {
                    //アニメーション中のこともあるから、それを停止させ、
                    //トランジットパネルに切り替える
                    timePanel.stopTimer();
                    if ( timePanel.isSelectedManeuverButton() ) {
                        timePanel.setSelectedManeuver(false);
                        timePanel.setSelectedButton(TimePanel3.TRANSIT_BUTTON);
                    }
                    timePanel.setTransit( TransitClip.transit );
                }
            }
        });
        //クリップボードに画像をコピー
        clipboardMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                BufferedImage img = module.getBufferedImage(
                    imageSizeMenu.getSelectedDimension() );
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                ImageSelection imgselection = new ImageSelection(img);
                clipboard.setContents( imgselection, null );
            }
        });
        //ファイルに画像を保存
        saveImageMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                BufferedImage img = module.getBufferedImage(
                    imageSizeMenu.getSelectedDimension() );
                imageSaver.showSaveDialog( owner, null, getTitle(),img );
            }
        });
        // この図をマイチャートに登録
        myChartMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                System.out.println("selectedModuleName = " + getSelectedModuleClassName());
                System.out.println("getSelectedModuleMode = " +getSelectedModuleMode());
                System.out.println("Calc selectedName = " + calcSpecificCustomizer.getSelectedName() );
                System.out.println("Skin selectedName = " + colorSpecificCustomizer.getSelectedName() );
                int chart_id = timePanel.getSelectedChartData().getSelectedData().getNatal().getId();
                Config.usr.setProperty("MyData.id", "" + chart_id );
                Config.usr.setProperty("MyData.moduleName", getSelectedModuleClassName());
                ChartModuleMode cmm = getSelectedModuleMode();
                String cmmTitle = (cmm == null) ? "" : cmm.getTitle();
                String cmmCommand = (cmm == null) ? "" : cmm.getCommand();
                Config.usr.setProperty("MyData.moduleModeTitle", cmmTitle );
                Config.usr.setProperty("MyData.moduleModeCommand", cmmCommand );
                Config.usr.setProperty("MyData.calcName",calcSpecificCustomizer.getSelectedName() );
                Config.usr.setProperty("MyData.skinName", colorSpecificCustomizer.getSelectedName() );
                Config.save();
            }
        });
        //この図のスキンと条件を今ボタンに登録
        nowMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                Config.usr.setProperty( "NowButton.calcName", calcSpecificCustomizer.getSelectedName() );
                Config.usr.setProperty( "NowButton.skinName", colorSpecificCustomizer.getSelectedName() );
                Config.save();
            }
        });

        //Ctrl-Lを割り当て
        addHistoryMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        //Ctrl-E
        editNatalMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
//        saveCompositDataMenuItem.setAccelerator(
//            KeyStroke.getKeyStroke(KeyEvent.VK_C,
//                InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
        //Ctrl-C でアニメ停止
        stopTimerMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        setFocusable(true);
        colorSpecificCustomizer = new SkinCustomizer( owner );
        calcSpecificCustomizer = new CalcCustomizer( owner );
    }


    // 時間(T)のメニューから「トランジットをヒストリーに追加」
    private void addHistoryActionPerformed(ActionEvent evt) {
        Transit transit = new Transit(timePanel.getTransit());
        ChartData chartData = timePanel.getSelectedChartData();
        Natal natal = null;
        if(chartData.isComposit())
            natal = chartData.getComposit();
        else
            natal = chartData.getSelectedData().getNatal();
        boolean registed = EventInputDialog.showDialog(
            this,transit, natal.getName() + "にヒストリーを登録");
        if(! registed) return;
        List<Transit> historyList = natal.getHistory();
        if(historyList == null) {
            historyList = new ArrayList<Transit>();
            natal.setHistory(historyList);
        }
        historyList.add(transit); //occの中にあるlistは参照で書き換わる
        //ヒストリーメニュー作り直し
        ((ChartDataPanel)chartData).createHistoryMenu();
        //DBに登録済みのデータならDBのほうも更新
        if(natal.getId() > Natal.UNREGISTERED)
            DBFactory.getInstance().updateHistory( natal.getId(), historyList);
        //時間メニューを作り直し
        getEditMenu(editMenu);
    }

    // 時間(T)のメニューから「現在のデータを編集」
    private void editDataActionPerformed() {
        ChartData chartData = timePanel.getSelectedChartData();
        if ( chartData.isComposit() ) { //コンポジットのとき
            Natal compositNatal = chartData.getComposit();
            List<Transit> eventList = new ArrayList<Transit>();
            // コンポジットのリストは作り直す
            for ( Data d : chartData.getDataList() )
                eventList.add((Transit)d.getNatal());
            compositNatal.setComposit( eventList );
            Natal result = DataInputDialog.showEditDialog( this, compositNatal, null );
            if ( result != null ) {
                explorer.registNatal( compositNatal );
                List<Natal> compositList = new ArrayList<Natal>();
                compositList.add( compositNatal );
                chartData.setNatal( compositList );
            }
        } else  //非コンポジットのとき
            saveNatal( chartData );
        timePanel.set();
    }

    // 引数で指定されたDataを編集しDBに保存したり上書きしたりする。
    //終了時に保存完了してないデータがあるときもよばれる。
    private void saveNatal(ChartData chartData) {
        //occを壊したくないので、IDを使って新しいインスタンスを得る
        Natal natal = chartData.getSelectedData().getNatal();
        Natal tempNatal = new Natal(natal); //DB内のNatalの複製品ができる。
        //現在のネイタルの時と場所を書き写す
        TimePlace tp = chartData.getSelectedData().getTimePlace();
        tempNatal.setTimePlace(chartData.getSelectedData().getTimePlace());
        //編集ダイアログを開く
        Natal updateNatal = DataInputDialog.showEditDialog(this,tempNatal,null);
        if ( updateNatal != null ) {
            if ( explorer.getNatal( updateNatal.getId() ) != null ) {
                //指定IDがすでに登録ずみなら
                String msg = "<html>データベース上の" + natal.getName()+
                    "に、<br>編集したデータを上書きしてもよろしいですか？</html>";
                int result = JOptionPane.showConfirmDialog(
                    this,msg,"ネイタルデータの上書き",
                    JOptionPane.YES_NO_OPTION);
                if ( result == JOptionPane.YES_OPTION ) {
                    if ( SwingUtilities.isEventDispatchThread() ) {
                        System.out.println("★EDTである");
                    }
                    explorer.registNatal(updateNatal);
                    chartData.replaceNatal( updateNatal );
                    System.out.println( updateNatal );
                    System.out.println("上書きしました");
                }
            } else {
                //DBにまだ登録されてないデータか
                //コンポジットの一部のデータの一部の場合
                explorer.registNatal( updateNatal ); //保存先選択ダイアログが開く
                chartData.replaceNatal( updateNatal );

            }
        }
    }

    // 時間制御パネルの複数のデータをコンポジットデータとして保存
    private void saveCompositActionPerformed() {
        ChartData chartData = timePanel.getSelectedChartData();
        Natal selectedNatal = chartData.isComposit() ?
            chartData.getComposit() : chartData.getSelectedData().getNatal();
        Natal occ = new Natal(selectedNatal);
        occ.setChartType(Natal.COMPOSIT);
        List<Transit> compositList = new ArrayList<Transit>();
        for ( Data d : chartData.getDataList() ) {
            Transit transit = new Transit();
            transit.setTimePlace( d.getTimePlace() );
            transit.setName( d.getNatal().getName() );
            transit.setMemo( d.getNatal().getMemo() );
            compositList.add(transit);
        }
        occ.setComposit( compositList );
        occ.setId(-1); // IDを消去し、DB上にない新規データとして扱う
        Natal result = DataInputDialog.showEditDialog( this, occ, null );
        if ( result != null )
            explorer.registNatal( occ );
    }
    /**
     * Natalのリストをセットする。この内部フレームへのデータの入口。
     * たとえばネイタルデータを選んで新規チャートとして表示したときはここが呼ばれる。
     * アニメーション中の場合、アニメーションは停止する。
     * ネイタルにトランジットの観測地が登録されているときは、それを採用し、TimePanel
     * にセットする。
     */
    public void setNatal( List<Natal> list ) {
        Natal n = list.get(0);
//        System.out.println( "Natal = " + n.toString() );
//        System.out.println("デフォルト観測地" + n.getTransitPlace());
        timePanel.setNatal(list);
        if ( list.get(0).getTransitPlace() != null ) {
            Transit t = timePanel.getTransit();
            t.setPlace(list.get(0).getTransitPlace());
            timePanel.setTransit(t);
//            System.out.println("をセットしたぞ" + t);
        }
        getEditMenu(editMenu);
    }

    /**
     * Natalのリストをセットする。この内部フレームへのデータの入口。
     * アニメーション中の場合、アニメーションは停止する。
     */
    public void addNatal( List<Natal> list ) {
        timePanel.addNatal(list);
        getEditMenu(editMenu);
    }

    /**
     * リスト先頭のNatalをTransitとしてセットする。
     */
    public void setTransit( List<Natal> list ) {
        System.out.println("setTransitが呼ばれた " + list.get(0).toString());
        timePanel.stopTimer();
        timePanel.setTransit( (Transit) list.get(0) );
        getEditMenu( editMenu );
    }

    /**
     * ChartPaneがとじたら、必ずこのメソッドを呼び出し、
     * 未保存のデータ(登録せずにチャートを作成した場合、未保存状態が生じる)を
     * DB保存したり、タイマーを停止させる。
     * このメソッドで終了処理を行わないと、タイマーが動きっぱなしになるので
     * 非常にまずい。
     */
    public void close() {
        //closing = true;
        timePanel.stop(); // スレッド停止
        ChartData chartData = timePanel.getSelectedChartData();
        List<Data> list = chartData.getDataList();
        for ( int i=0; i < list.size(); i++ ) {
            Natal natal = list.get(i).getNatal();
            if ( natal.getId() == Natal.NEED_REGIST ) {
//                String title = "未保存データをデータベースへ登録";
                String mes = natal.getName() + "をデータベースに保存しますか？";
                int result = JOptionPane.showOptionDialog(
                        owner,
                        mes,
                        "未保存データをデータベースへ登録",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,options, options[0] );
                if ( result == JOptionPane.YES_OPTION ) { //データ保存
                    natal.setTimePlace(list.get(i).getTimePlace());
                    Natal newNatal =
                        DataInputDialog.showEditDialog(owner,natal,null);
                    if ( newNatal != null )
                        explorer.registNatal(newNatal);
                } else if ( result == JOptionPane.NO_OPTION ) {
                    continue;
                } else {
                    break;
                }
            }
        }
        editMenu.removeAll();
    }

    /**
     * タイマーを停止させる。close()と異なるのは、未登録のデータをDBに保存する
     * 処理を行わないこと。
     * このオブジェクトのインスタンスからモジュールとその設定情報を取り出し、
     * LaunchChartSelectorPanelにセットしたあと、インスタンスを破棄する場合に
     * 使う。
     */
    protected void close2() {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        timePanel.stop();
        editMenu.removeAll();
    }

    /**
     * メインメニューバーにある「編集(E)」メニューに、メニュー内容を書き込む。
     * このメニューの特に「ヒストリー」はTimeControlPanelで選択するデータによって
     * 動的に変化する。
     * @param menu このオブジェクトがもっているメニューがmenuに書き込まれる。
     * setされるのはmenuであって、このオブジェクトではない。
     */
    public void getEditMenu( JMenu menu ) {
        menu.removeAll();
        menu.add( timePanel.getHistoryMenu() );
        menu.add( addHistoryMenuItem );
        menu.addSeparator();
        menu.add( copyTransitMenuItem );
        menu.add( peastTransitMenuItem );
        menu.addSeparator();
        menu.add( editNatalMenuItem );
        menu.add( MenuManager.getMenuItem("EditMenu.duplicate") );
        menu.add( saveCompositDataMenuItem );
        menu.addSeparator();
        saveImageMenuItem.setEnabled( module.isImageServiceActivated() );
        clipboardMenuItem.setEnabled( module.isImageServiceActivated() );
        if ( imageSizeMenu.getHeightPer() != module.getHeightPer() ) {
            imageSizeMenu.update( module.getHeightPer(), module.getClassName() );
        }
        imageSizeMenu.setEnabled(     module.isImageServiceActivated() &&
                                   (! module.isFixedImageSize()) );
        menu.add( clipboardMenuItem );
        menu.add( saveImageMenuItem );
        menu.add( imageSizeMenu );
        menu.addSeparator();
        menu.add( myChartMenuItem );
        menu.add( nowMenuItem );
        menu.addSeparator();
        menu.add( stopTimerMenuItem );
    }
    /**
     * フレームにアイコンをセットする。マルチパネル化によってこのメソッドは修正
     * が必要。ChartDataPanelから呼ばれているだけ。
     */
    @Override
    public void setIcon(Natal occ) {
        int i = -1;
        if ( occ.getChartType().equals("NATAL") ) {
            if ( occ.getGender() == Natal.MALE )        i=0;
            else if( occ.getGender() == Natal.FEMALE ) i=1;
            else                                       i=2;
        } else if ( occ.getChartType().equals( Natal.EVENT ) )  i=3;
        else if ( occ.getChartType().equals( Natal.COMPOSIT ) ) i=4;
        if ( cpl != null ) {
            cpl.setIcon( icons[i], this );
        }
    }

    @Override
    public void setTitle( String title ) {
        this.title = title;
        if ( cpl != null ) {
            cpl.setTitle( title, this );
        }
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void contentSelected() {
        if ( cpl != null ) {
            cpl.contentSelected( this );
        }
    }

    public void setChartPaneListener( ChartPanelListener cpl ) {
        this.cpl = cpl;
//        colorSpecificCustomizer.setChartPaneLisetner( cpl );
//        calcSpecificCustomizer.setChartPaneLisetner( cpl );
    }

    /**
     * 引数で与えられたChartPaneが保持している、NatalやTransitのデータ
     * をこのチャートフレームにディープコピーする。チャート複製用。
     */
    public void dataCopy( ChartPane chartPane ) {
        timePanel.dataCopy( chartPane.timePanel );
    }

    /**
     * このチャートフレームに指定されたIDのNatalデータがセットされている場合は
     * trueを返す。チャート表示中データの識別用で、表示中のデータに対して、
     * 削除や移動は禁止する仕様。編集は別ルートで許可。
     */
    public boolean isComprise( int id ) {
        return timePanel.isComprise( id );
    }

    /**
     * 時間パネル(タブ式で日付入力を受け付ける部分)を返す。
     */
    public TimePanel getTimePanel() {
        return timePanel;
    }

    /**
     * 色設定機能のメニューを返す。
     * @param menu メニューを書きこむオブジェクト。引数で与えたメニューに、この
     * メソッドはメニューアイテムをaddする。
     */
    public void getColorSelectionMenu( JMenu menu ) {
        colorSpecificCustomizer.getMenu( menu );
    }

    /**
     * 計算設定機能のメニューを返す。
     * @param menu メニューを書きこむオブジェクト。引数で与えたメニューに、この
     * メソッドはメニューアイテムをaddする。
     */
    public void getSpecificSelectionMenu( JMenu menu ) {
        calcSpecificCustomizer.getMenu( menu );
    }

    /**
     * 色設定の切替メニューを作り直す
     */
    public void updateColorSelectionMenu() {
        //colorSpecificCustomizer.createMenu();
        colorSpecificCustomizer.reload();

    }

    /**
     * 計算仕様の切替メニューを作り直す
     */
    public void updateSpecificSelectionMenu() {
        //calcSpecificCustomizer.createMenu();
        calcSpecificCustomizer.reload();
    }

    /**
     * 現在選択されているモジュールの表示メニュー用アイテムリストを返す。
     */
    public List<Component> getViewMenuList() {
        return module.getViewMenuList();
    }

    /**
     * 現在選択されているモジュールの設定メニュー用アイテムリストを返す。
     */
    public List<Component> getSpecificMenuList() {
        return module.getSpecificMenuList();
    }

    /**
     * 現在選択中のチャートモジュールにコマンドを送る。
     */
    public void setCommand( String [] args ) {
        module.setCommand( args );
    }
    /**
     * このチャートの印刷用オブジェクトを返す。
     */
    public Printable getPainter() {
        return module.getPainter();
    }
    /**
     * このチャートが印刷可能ならtrueを返す。
     */
    public boolean isPrintable() {
        return module.isPrintable();
    }

    /**
     * 印刷の際の用紙の向き、印刷部数などを定義したハッシュセットを返す。
     * isPrintable()がfalseのときはnullを返す。
     */
    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        return module.getPrintRequestAttributeSet();
    }

    /**
     * 選択されているモジュール用の辞書アクションファイルのURLを返す。
     * EDTに関係なく呼び出せる。
     */
    public DictionaryActionFile getDictionaryAction() {
        return module.getDictionaryAction();
    }
}
