/*
 * Main.java
 *
 * Created on 2007/11/08, 18:37
 *
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreePath;
import to.tetramorph.astrocalendar.EnneagramCalendar2;
import to.tetramorph.michiteru.AstroCalendar;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.dict.DictTable;
import to.tetramorph.starbase.dict.DictionaryDialog;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.module.ChartModuleMode;
import to.tetramorph.starbase.module.DictionaryActionFile;
import to.tetramorph.starbase.multisplit.MultiTabbedListener;
import to.tetramorph.starbase.multisplit.MultiTabbedPane;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.starbase.util.WindowMoveHandler;
import to.tetramorph.util.Sleep;
import to.tetramorph.util.StopWatch;
/**
 * このアプリケーションのメインクラスで全体の統括を行う。
 * @author 大澤義鷹
 */
class Main implements MutexListener {
    //データベースフレーム
    MainFrame2 mf = new MainFrame2();
    JDayDialog         jdayDialog = new JDayDialog( mf );
    DesktopHandler     desktop = new DesktopHandler();
    DataExplorer       explorer = new DataExplorerPanel(desktop);
    EnneagramCalendar2 ennCal = new EnneagramCalendar2(false);
    SabianDialog       sabianDialog;
    MultiTabbedPane    multiPane;
    ChartPanelHandler  cph;
    MultiTabbedHandler tabbedHandler = new MultiTabbedHandler();
    Dictionary dictionary;
    /**
     * Main オブジェクトを作成する。EDTから呼び出すこと。
     */
    protected Main() {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        // MainFrameを初期化してからDesktopHandlerをnewしないと、getFrame()で
        // nullが返ることになる。
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AstroFont.registEnvironment();
        this.multiPane = mf.getMultiTabbedPane();
        cph = new ChartPanelHandler(multiPane);
        sabianDialog = new SabianDialog( mf ); // Frameを渡す

        mf.setDBComponent( (Component)explorer );
        mf.setResultComponent( explorer.getSearchResultPanel());
//        mf.setIconImage( IconLoader.getImage(
//                                        "/resources/niwatori2.png"));
        mf.setIconImages(AppIcon.DESKTOP_ICONS);
        TreePath current = explorer.foundTreePath(
                    Config.usr.getProperty( "CurrentTreePath", "MyChart" ) );
        explorer.selectFolder(current);
        setupFileMenu();
        setupWindowMenu();
        setupToolButton();
        setupPreferenceMenu();
        setupChartMenu();
        setupEditMenu();
        setupViewMenu();
        setPreferenceMenu();
        multiPane.setMultiTabbedListener( tabbedHandler );
        //ウィンドウのデスクトップ上での位置とサイズを記憶するようにする。
        WindowMoveHandler winmove =
            new WindowMoveHandler("MainFrame.BOUNDS", mf);
        mf.addComponentListener(winmove);

        mf.pack();
        winmove.setBounds();
        //mf.setLocationRelativeTo(null);
        JMenu editMenu =  MenuManager.getMenu( "EditMenu" );
        // 辞書はまだ作成されていないのでnullを渡しておく。
        dummyChartPane = new ChartPane(
                explorer, editMenu, sabianDialog, null, mf );
        LaunchChartSelectorPanel panel =
        ( LaunchChartSelectorPanel )MenuManager.get("LaunchChartSelectorPanel");
        panel.setChartPane( dummyChartPane );
        mf.setVisible(true);
    }
    ChartPane dummyChartPane;
    int dummyCount;
    /**
     * すべてのプラグインで使用可能な仕様設定のリストを取得する。
     * スプラッシュウィンドウのプログレスバーに経過を出力する。
     * このメソッドはnew Main()の後にかならず呼び出すこと。
     * また非EDTで呼び出すこと。そうしないとプログレスバーが更新されない。
     */
    void setup() {
        //二つのマップを作り辞書ダイアログを初期化
        DictTable table = new DictTable();
        try {
            for ( dummyCount=0; dummyCount < dummyChartPane.getModuleCount(); dummyCount++ ) {

                java.awt.EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        dummyChartPane.selectModule(dummyCount);
                    }
                });

                DictionaryActionFile act = dummyChartPane.getDictionaryAction();
                if ( act == null ) continue;
                String name = dummyChartPane.getSelectedModuleName();
                table.add( name, act.getName(), act.getUrl() );
                SplashWindow.getInstance().addValue(10);
                Sleep.exec();
            }
        } catch ( Exception e ) {
            Logger.getLogger( Main.class.getName() )
                    .log( Level.SEVERE, null, e );
        }
        dictionary = DictionaryDialog.createInstance(mf, table);
        SplashWindow.getInstance().setValue(100);

        //トランジットチャートを出す
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                dummyChartPane.close2();
                MenuManager.getButton("ToolButton.now").getActionListeners()[0].actionPerformed(null);
            }
        });


        StopWatch.show("起動完了までの時間");
        //バージョンが上がった(変わった)ときはお知らせを表示
        String version = Config.usr.getProperty( "Version", "" );
        if ( ! version.equals( VersionDialog.getVersion() ) ) {
            VersionDialog.showAnnounce(mf);
            Config.usr.setProperty( "Version", VersionDialog.getVersion() );
        }
        //アマテルが起動した時刻をセット。アマテルの稼働時間計算に使用。
        //System.setProperty("StartTime", "" + System.currentTimeMillis() );
        StopWatch.set("稼働時間");
    }

    /**
     * Java Web Startを使用しない実行時において、二重起動が検出されたときこの
     * メソッドが呼び出される。このオブジェクトは、アイコン化されているフレーム
     * を可視化する処理をする。
     * テスト時はJava Web Startを使うと検査が煩わしいため、独自の排他制御を用意
     * している。
     */
    @Override
    public void mutexPerformed() {
        mf.setExtendedState(Frame.NORMAL);
        mf.toFront();
        System.out.println("Mutexアクション実行");
    }
//    /**
//     * Java Web Startでの実行時に、二重起動が検出されたときこのメソッドが呼び
//     * 出される。このオブジェクトは、アイコン化されているフレームを可視化する
//     * 処理をする。
//     */
//    @Override
//    public void  newActivation(String[] string) {
//        mf.setExtendedState(Frame.NORMAL);
//        mf.toFront();
//        System.out.println("SingleInstanceListener実行");
//    }

    //ChartPaneにタイトルやアイコンがセットされたタイミングで、
    //MultiTabbedPane側にそれを通達し、タブにそれを反映させる。
    class ChartPanelHandler implements ChartPanelListener {

        MultiTabbedPane multiPane;

        ChartPanelHandler(MultiTabbedPane multiPane) {
            this.multiPane = multiPane;
        }
        @Override
        public void setTitle(String title, Component c) {
            multiPane.setTitle(title,c);
        }

        @Override
        public void setIcon(Icon icon, Component c) {
            multiPane.setIcon(icon,c);
        }

        @Override
        public void contentSelected(Component c) {
            multiPane.contentSelected(c);
        }

        @Override
        public void selectedChartModule( ChartPane chartPane ) {
            System.out.println("チャートモジュール選択");
            setChartItemsToFrame(chartPane);
        }
        //メニューでなくダイアログで切り替えるようにしたのでこのメソッドは不要
        //になるかもしれない。
        @Override
        public void updateColorPreferenceMenu() {
            System.out.println("すべてのChartPaneの色仕様の切替メニューを更新");
            List<Component> list = multiPane.getAllComponents();
            for(Component c : list) {
                if( c instanceof ChartPane) {
                    ((ChartPane)c).updateColorSelectionMenu();
                }
            }
            setPreferenceMenu();
        }

        @Override
        public void updateSpecificPreferenceMenu() {
            System.out.println("すべてのChartPaneの計算仕様切替メニューを更新");
            List<Component> list = multiPane.getAllComponents();
            for(Component c : list) {
                if( c instanceof ChartPane) {
                    ((ChartPane)c).updateSpecificSelectionMenu();
                }
            }
            setPreferenceMenu();
        }
    }

    //DatabaseExplorerが必要とするメソッドをまとめたクラス

    class DesktopHandler implements ChartDesktop {
        String MESSAGE = "<html>"
        + "複数のデータを一度に入力する場合、コンポジットデータと<br>"
        + "非コンポジットデータを混在させて指定することはできません。<br>"
        + "また複数のコンポジットデータを一度に入力することもできません。<br>"
        + "ただし後から追加することは可能です。<br></html>";
        /**
         * チャートフレームが一枚も無いときはtrueを返す。
         */
        @Override
        public boolean isEmptyChartPane() {
            return getChartPanes().length == 0;
        }

//        /**
//         * すべてのプラグインで使用可能な仕様設定のリストを取得する。
//         */
//        void setup() {
//            ChartPane cp = new ChartPane( explorer,
//                                           MenuManager.getMenu( "EditMenu" ),
//                                           sabianDialog, mf );
//            LaunchChartSelectorPanel
//                panel = ( LaunchChartSelectorPanel )
//                                    MenuManager.get("LaunchChartSelectorPanel");
//            panel.setChartPane( cp );
//            //二つのマップを作る
//            Map<String,URL> urlMap = new HashMap<String,URL>();
//            Map<URL,Map<String,DictAction>>
//                    actionMap = new HashMap<URL,Map<String,DictAction>>();
//            int size = cp.getModuleNameList().size();
//            try {
//                for ( int i=0; i<size; i++) {
//                    cp.selectModule(i);
//                    URL url = cp.getDictionaryAction();
//                    if ( url == null ) continue;
//                    Map<String,DictAction> map = FileUtils.getDictActionMap(url.openStream());
//                    String className = cp.getSelectedModuleClassName();
//                    urlMap.put(className, url);
//                    actionMap.put(url, map);
//                }
//            } catch ( Exception e ) {
//                e.printStackTrace();
//            }
//
//            cp.close2();
//        }
        @Override
        public ChartPane openChartPane( List<Natal> list ) {
            return openChartPane( list, -1, null );
        }
        /**
         * 新しくチャートフレームを開いて、Natalのリストをセットする。
         * @param moduleNum チャートモジュール番号。負数を指定するとデフォルトを使用。
         */
        public ChartPane openChartPane( List<Natal> list,
                                         int moduleNum,
                                         String specName ) {
            assert SwingUtilities.isEventDispatchThread();
            if ( ! ( list.size() == 1 &&
                      list.get(0).getChartType().equals( Natal.COMPOSIT ) ) ) {
                for ( int i = 0; i < list.size(); i++ ) {
                    if ( list.get(i).getChartType().equals( Natal.COMPOSIT ) ) {
                        JOptionPane.showMessageDialog( getFrame(), MESSAGE,
                            "新規チャートのエラー", JOptionPane.ERROR_MESSAGE );
                        return null;
                    }
                }
            }
            ChartPane cp = new ChartPane( explorer,
                                           MenuManager.getMenu( "EditMenu" ),
                                           sabianDialog,
                                           dictionary,
                                           mf );
            cp.setChartPaneListener( cph );
            LaunchChartSelectorPanel
                panel = ( LaunchChartSelectorPanel )
                                    MenuManager.get("LaunchChartSelectorPanel");
            int n = ( moduleNum < 0 ) ?
                panel.getSelectedChartModuleIndex() : moduleNum;
            ChartModuleMode mode = panel.getSelectedModuleMode();
            cp.selectModule( n );
            cp.setModuleMode( mode );
            multiPane.insert( null, null, cp );
            setNatal( list, cp );
            MenuManager.getMenuItem( "EditMenu.duplicate" ).setEnabled( true );
            MenuManager.getButton( "Button.maneuver" ).setEnabled( true );
            return cp;
        }

        /**
         * 新しくチャートフレームを開いて、Natalのリストをセットする。
         * @param moduleName チャートモジュール名。
         */
        public ChartPane openChartPane( List<Natal> list,
                                         String moduleName,
                                         ChartModuleMode cmm,
                                         String skinName,
                                         String calcName ) {
            assert SwingUtilities.isEventDispatchThread();
            if ( ! ( list.size() == 1 &&
                      list.get(0).getChartType().equals( Natal.COMPOSIT ) ) ) {
                for ( int i = 0; i < list.size(); i++ ) {
                    if ( list.get(i).getChartType().equals( Natal.COMPOSIT ) ) {
                        JOptionPane.showMessageDialog( getFrame(), MESSAGE,
                            "新規チャートのエラー", JOptionPane.ERROR_MESSAGE );
                        return null;
                    }
                }
            }
            ChartPane cp = new ChartPane( explorer,
                                           MenuManager.getMenu( "EditMenu" ),
                                           sabianDialog,
                                           dictionary,
                                           mf );
            cp.setChartPaneListener( cph );
            LaunchChartSelectorPanel
                panel = ( LaunchChartSelectorPanel )
                                    MenuManager.get("LaunchChartSelectorPanel");
            cp.selectModule( moduleName, cmm, skinName, calcName );
//            int n = ( moduleNum < 0 ) ?
//                panel.getSelectedChartModuleIndex() : moduleNum;
//            ChartModuleMode mode = panel.getSelectedModuleMode();
//            cp.selectModule( n );
//            cp.setModuleMode( mode );
            multiPane.insert( null, null, cp );
            setNatal( list, cp );
            MenuManager.getMenuItem( "EditMenu.duplicate" ).setEnabled( true );
            MenuManager.getButton( "Button.maneuver" ).setEnabled( true );
            return cp;
        }

        // 一見、ChartItemToFrameの中でcontentSelectedを呼べば良いように思えるが
        // それをすると、リスナが呼びだしあって無限ループに落ちるためこのメソッド
        // がある。
        void setChartItemWithSelect( ChartPane chartPanel ) {
            multiPane.contentSelected( chartPanel );
            setChartItemsToFrame( chartPanel );
        }

        /**
         * ChartPaneにDatabaseExplorerやSerachResultTable上で選択されたNatal
         * (複数)をセット。ChartPaneにある既存のデータは消え新しいもの
         * に置き換わる。
         * @param list Natalのリスト
         * @param targetChartPane リストをセットするChartPane
         */
        @Override
        public void setNatal( List<Natal> list,
                                ChartPane targetChartPane ) {
            assert SwingUtilities.isEventDispatchThread();
            if ( targetChartPane == null ) {
                ChartPane chartPane =
                    (ChartPane)multiPane.getSelectedComponent();
                if ( chartPane != null ) {
                    chartPane.setNatal( list );
                    setChartItemWithSelect( chartPane );
                }
            } else {
                targetChartPane.setNatal( list );
                setChartItemWithSelect( targetChartPane );
            }
        }

        /**
         * ChartPaneのトランジットチャンネルにネイタルデータをセットする。
         */
        @Override
        public void setTransit( List<Natal> list,
                                 ChartPane targetChartPane ) {
            if ( targetChartPane == null ) {
                ChartPane chartPane =
                    (ChartPane)multiPane.getSelectedComponent();
                if ( chartPane != null ) {
                    chartPane.setTransit( list );
                    setChartItemWithSelect( chartPane );
                }
            } else {
                targetChartPane.setTransit( list );
                setChartItemWithSelect( targetChartPane );
            }

        }
        /**
         * すべてのチャートフレームのリストを返す。
         * 一枚も無いときは長さ0の配列が返る。
         */
        @Override
        public ChartPane[] getChartPanes() {
            List<Component> list = multiPane.getAllComponents();
            List<ChartPane> charts = new ArrayList<ChartPane>();
            for(Component c : list) {
                if(c instanceof ChartPane) {
                    charts.add((ChartPane)c);
                }
            }
            ChartPane [] results = new ChartPane[ charts.size() ];
            for(int i=0; i<charts.size(); i++) results[i] = charts.get(i);
            return results;
        }
        /**
         * ChartInnerFrameにDB上で選択されたNatal(複数)を追加で渡す。
         * openNewChartと同様にイベントキューをつかって実行される。
         * @param list Natalのリスト
         * @param targetFrame nullなら現在選択されているChartInternalFrameにlistを追加。
         * null以外なら指定されたフレームにlistを追加。
         */
        @Override
        public void addNatal(List<Natal> list, ChartPane targetFrame) {
            assert SwingUtilities.isEventDispatchThread();
            if(targetFrame == null) {
                ChartPane chartFrame =
                    (ChartPane)multiPane.getSelectedComponent();
                if(chartFrame != null) {
                    chartFrame.addNatal(list);
                    setChartItemWithSelect(chartFrame);
                }
            } else {
                targetFrame.addNatal(list);
                setChartItemWithSelect(targetFrame);
            }
        }
        /**
         * 指定されたIDのデータがチャートフレーム(複数)に登録されている場合はtrueを
         * 返す。つまりチャートに表示されているNatalデータならtrueとなる。
         * その際、データベース上からそのNatalデータを削除と移動はしてはならない。
         * 表示中のデータは「現在のデータを編集」機能で編集しなければならない。
         */
        @Override
        public boolean isDataBusy(int id) {
            ChartPane [] frames = getChartPanes();
            for(int i=0; i<frames.length; i++) {
                if(frames[i].isComprise(id)) return true;
            }
            return false;
        }

        @Override
        public Frame getFrame() {
            return mf;
        }

        @Override
        public void setResultVisible( boolean b ) {
            mf.setResultVisible(b);
        }
    }

    /**
     * チャートパネルは３のアイテムをもっている。
     * 1.タイムマヌーバのパネル(日付変更を行うパネル)
     * 2.時間メニュー(ヒストリーを登録等)
     * 3.色と計算設定のメニュー
     * これらは各チャートパネル固有のもので、チャートタブが選択されるごとに、
     * フレーム側のメニューにセットする必要がある。
     * このメソッドはそれを行う。
     * DesktopHandler,MultiTabbedHandler内から呼び出されている。
     */
    void setChartItemsToFrame( ChartPane chartPane ) {
        //chartPane.getEditMenu( MenuManager.getMenu("EditMenu") );
        TimePanel tp = chartPane.getTimePanel();
        mf.setDatePanel(tp);
        setPreferenceMenu();
    }

    /**
     * 設定メニューをChartPaneから取得して、メニューバーの表示と設定メニューに
     * セットする。
     */
    private void setPreferenceMenu() {
        JMenu prefMenu = MenuManager.getMenu("ConfigMenu");
        JMenu viewMenu = MenuManager.getMenu("ViewMenu");
        viewMenu.removeAll();
        prefMenu.removeAll();
        // 設定メニューを作る
        ChartPane cp = getSelectedChartPane();
        if ( cp == null ) { //基本設定メニューはかならず入れる
            prefMenu.add( MenuManager.getMenuItem("ConfigMenu.baseConfig"));
            return;
        }
        List<Component> specificMenuList = cp.getSpecificMenuList();
        if ( specificMenuList != null ) {
            for ( Component c : specificMenuList ) prefMenu.add( c );
            prefMenu.add( new JSeparator() );
        }
        prefMenu.add( MenuManager.getMenuItem("ConfigMenu.baseConfig"));
        // 表示メニューを作る
        List<Component> viewMenuList = cp.getViewMenuList();
        if ( viewMenuList != null ) {
            for ( Component c : viewMenuList ) viewMenu.add(c);
            viewMenu.add(new JSeparator());
        }
        cp.getColorSelectionMenu( viewMenu );
        viewMenu.add( new JSeparator() );
        cp.getSpecificSelectionMenu( viewMenu );
//        viewMenu.add( cp.getColorSelectionMenu() );
//        viewMenu.add( cp.getSpecificSelectionMenu() );
    }

    /**
     * 選択中のチャートフレームを返す。(このクラス内でのみ使用)
     * 選択中のチャートフレームが存在しない場合はnullを返す。
     */
    protected ChartPane getSelectedChartPane() {
        Component c = multiPane.getSelectedComponent();
        if( c instanceof ChartPane)
            return (ChartPane)c;
        return null;
    }
    //ChartPaneの複製
    void duplicateChartPane() {
        ChartPane nowPane = getSelectedChartPane();
        ChartPane newPane = new ChartPane(nowPane); //複製
        multiPane.insert( null, null, newPane);
        multiPane.contentSelected( newPane );
        newPane.validateNatal();
    }
    /**
     * タブのセレクトやクローズのときの処理
     */
    class MultiTabbedHandler implements MultiTabbedListener {
        //タブが選択された
        @Override
        public void tabSelected(Component c) {
            //System.out.println("タブが選択された");
            if(c instanceof ChartPane ) {
                ChartPane chart = (ChartPane)c;
                setChartItemsToFrame(chart);
            }
            mf.setShutterVisible(false);
        }

        @Override
        public void tabClosed(Component c) {
            System.out.println("タブがクローズされた");
            if( c instanceof ChartPane) {
                System.out.println("ChartPaneのクローズ");
                ((ChartPane)c).close();
            }
            mf.setDatePanel(null);
            if ( desktop.isEmptyChartPane() ) {
                MenuManager.getMenuItem("EditMenu.duplicate").setEnabled(false);
                MenuManager.getButton("Button.maneuver").setEnabled(false);
            }
        }

    }

    private void setupPreferenceMenu() {
        /*
         * メニューバーの「設定(P)」の「基本設定」のリスナ登録
         */
        MenuManager.getMenuItem("ConfigMenu.baseConfig").addActionListener(
            new BaseConfigAction());
        //lafMenu = createLookAndFeelMenu();
    }

    //「チャート(C)」メニューの設定
    private void setupChartMenu() {
        /*
         * メニューバーの「チャート(C)」のメニューにリスナを登録。
         * これはモジュールを選択するメニューだが、このメニュー自身がセレクトされ
         * たタイミングで、メニューの中身をChartPaneから受け取る。
         */
        MenuManager.getMenu("ModuleMenu").addMenuListener( new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                ChartPane cp = getSelectedChartPane();
                JMenu menu = (JMenu)e.getSource();
                if ( cp != null )
                    cp.getChartMenu( menu );
                else
                    menu.removeAll();
            }
            @Override
            public void menuCanceled(MenuEvent e) { }
            @Override
            public void menuDeselected(MenuEvent e) { }
        });
    }
    // 編集(E)メニューが選択されたときのイベントリスナを定義
    private void setupEditMenu() {
        MenuManager.getMenu("EditMenu").addMenuListener( new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                    System.out.println("編集メニューが選択。メニュー再取得");
                ChartPane cp = getSelectedChartPane();
                JMenu menu = (JMenu)e.getSource();
                if ( cp != null ) {
                    //cp.getChartMenu( menu );
                    cp.getEditMenu( menu );
                }
                else
                    menu.removeAll();
            }
            @Override
            public void menuCanceled(MenuEvent e) { }
            @Override
            public void menuDeselected(MenuEvent e) { }
        });

    }
    private void setupViewMenu() {
        JMenu viewMenu = MenuManager.getMenu("ViewMenu");
        viewMenu.addMenuListener( new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                ChartPane cp = getSelectedChartPane();
                JMenu menu = (JMenu)e.getSource();
                if ( cp == null )
                    menu.removeAll();
            }
            @Override
            public void menuCanceled(MenuEvent e) { }
            @Override
            public void menuDeselected(MenuEvent e) { }
        });
    }
    // 「ウィンドウ(W)」メニューのセットアップ
    private void setupWindowMenu() {
        MenuManager.getMenuItem("Window.search")
        .addActionListener(new SearchAction());

        MenuManager.getMenuItem("Window.dict")
        .addActionListener(new DictionaryAction());

        MenuManager.getMenuItem("Window.sabian")
        .addActionListener(new SabianAction());

        MenuManager.getMenuItem("Window.calendar")
        .addActionListener(new CalendarAction());

        MenuManager.getMenuItem("Window.enneagram")
        .addActionListener(new EnneagramAction());

        MenuManager.getMenuItem("Window.julian")
        .addActionListener(new JulianAction());
        MenuManager.getMenuItem("Window.db")
        .addActionListener(new DBOpenAction());

    }
    /**
     * ツールボタンのセットアップ
     */
    private void setupToolButton() {
        MenuManager.getButton("ToolButton.search")
            .addActionListener(new SearchAction());
        MenuManager.getButton("ToolButton.voidcal")
          .addActionListener(new CalendarAction());
        MenuManager.getButton("ToolButton.enncal")
            .addActionListener(new EnneagramAction());
        MenuManager.getButton("ToolButton.now")
            .addActionListener(new NowChartAction());
        MenuManager.getButton("ToolButton.mychart")
            .addActionListener(new MyChartAction());
        MenuManager.getButton("ToolButton.dict")
            .addActionListener(new DictionaryAction());
        MenuManager.getButton("ToolButton.sabian")
            .addActionListener(new SabianAction());
        MenuManager.getButton("ToolButton.dbin")
            .addActionListener(new RegNatalAction());
    }
    /**
     * ファイルメニューのセットアップ
     */
    private void setupFileMenu() {
        MenuManager.getMenuItem("FileMenu.myChart")
            .addActionListener( new MyChartAction() );
        MenuManager.getMenuItem("FileMenu.nowChart")
            .addActionListener( new NowChartAction() );
        MenuManager.getMenuItem("FileMenu.regNatal")
            .addActionListener( new RegNatalAction());
        MenuManager.getMenuItem("FileMenu.regEvent")
            .addActionListener( new RegEventAction());
        MenuManager.getMenuItem("FileMenu.natalChart")
            .addActionListener( new NatalChartAction());
        MenuManager.getMenuItem("FileMenu.eventChart")
            .addActionListener( new EventChartAction());
        MenuManager.getMenuItem("FileMenu.addNatal")
            .addActionListener( new AddNatalAction());
        MenuManager.getMenuItem("FileMenu.addEvent")
            .addActionListener( new AddEventAction());
        MenuManager.getMenuItem("FileMenu.exportAll")
            .addActionListener( new ExportAllAction());
        MenuManager.getMenuItem("FileMenu.exportSelected")
            .addActionListener( new ExportSelectedAction());
        MenuManager.getMenuItem("FileMenu.importAll")
            .addActionListener( new ImportAllAction());
        MenuManager.getMenuItem("FileMenu.importSelected")
            .addActionListener( new ImportSelectedAction());
        MenuManager.getMenuItem("EditMenu.duplicate")
            .addActionListener( new DuplicateAction());
        final ExitAction exitAction = new ExitAction();
        MenuManager.getMenuItem("FileMenu.exit")
            .addActionListener( exitAction );
        mf.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent evt ) {
                exitAction.actionPerformed( null );
            }
        });
        MenuManager.getMenuItem("FileMenu.print")
            .addActionListener(new PrintAction());

        MenuManager.getMenu("FileMenu").addMenuListener( new FileMenuAction() );
        //プリンタが接続されていない場合のフラグを立てる
        PrintService ps = PrinterJob.getPrinterJob().getPrintService();
        System.setProperty( "PrinterEnabled",
                            ( ps != null ) ? "true" : "false" );

        MenuManager.getMenuItem("HelpMenu.version")
            .addActionListener(new VersionAction());
        MenuManager.getMenuItem("HelpMenu.manual")
            .addActionListener( new ManualAction(mf) );
        MenuManager.getMenuItem("HelpMenu.homepage")
            .addActionListener( new AmateruSiteAction(mf));
    }

    //ファイルメニューが選択されたとき、印刷メニューのEnable/Disenableをセットする
    private class FileMenuAction implements MenuListener {
        @Override
        public void menuSelected( MenuEvent evt ) {
            System.out.println("メニューが選択");
            ChartPane cp = getSelectedChartPane();
            boolean state = true;
            if ( ( cp == null )        ||
                 ( ! cp.isPrintable() ) ||
                 System.getProperty("PrinterEnabled","false").equals("false") ) {
                state = false;
            }
//            if ( cp == null ) state = false;
//            else if( ! cp.isPrintable() ) state = false;
//            if ( System.getProperty("PrinterEnabled","false").equals("false") )
//                state= false;
            MenuManager.getMenuItem("FileMenu.print").setEnabled( state );
        }
        @Override
        public void menuCanceled( MenuEvent evt ) {}
        @Override
        public void menuDeselected( MenuEvent evt) {}
    }

    private class SearchAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            explorer.showSearchFrame();
        }
    }
    private class DictionaryAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            dictionary.setVisible(true);
        }
    }

    private class SabianAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            sabianDialog.setVisible(true);
        }
    }
    private class CalendarAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            AstroCalendar.showCalendar();
        }
    }
    private class EnneagramAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            ennCal.setVisible(true);
            if ( ennCal.getExtendedState() == Frame.ICONIFIED ) {
                ennCal.setExtendedState(Frame.NORMAL);
            }
        }
    }
    private class JulianAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            jdayDialog.setVisible(true);
        }
    }
    /**
     * 今のトランジットチャートを出す
     */
    private class NowChartAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            Natal natal = new Natal();
//            Place place = PrefUtils.getPlace(Conf.data,"DefaultTransitPlace");
            Place place = Config.usr.getPlace( "DefaultTransitPlace" );
            natal.setPlace( place );
            natal.setCalendar( new GregorianCalendar(),
                               TimePlace.DATE_AND_TIME );
            natal.setChartType( Natal.EVENT );
            natal.setId( Natal.NEED_REGIST );
            natal.setName( "現在" );
            List<Natal> list = new ArrayList<Natal>();
            list.add( natal );
            LaunchChartSelectorPanel panel = ( LaunchChartSelectorPanel )
                                    MenuManager.get("LaunchChartSelectorPanel");
            String skin = Config.usr.getProperty("NowButton.skinName","");
            String calc = Config.usr.getProperty("NowButton.calcName","");
            ChartModuleMode cmm = new ChartModuleMode( "トランジット円", "3,T" );
            desktop.openChartPane( list,
                "to.tetramorph.starbase.chartmodule.NPTChartPlugin",
                cmm, skin, calc );
        }
    }

    private class MyChartAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            int id = Config.usr.getInteger("MyData.id",-1);
            if ( id < 0 ) {
                JOptionPane.showMessageDialog(mf,
                    "<html>自分のチャートをすばやく表示するためのボタンです。<br>" +
                    "チャート表示中に編集(E)から「この図をマイチャートボタンに登録」を選択。<br>" +
                    "以後、このボタンを押せば登録したチャートが表示されます。</html>");
                return;
            }
            List<Natal> list = new ArrayList<Natal>();
            Natal natal = DBFactory.getInstance().getNatal( id );
            if ( natal == null ) return; //削除されて存在しないとエラーを告げるべき
            list.add(natal);

            String skin = Config.usr.getProperty("MyData.skinName","");
            String calc = Config.usr.getProperty("MyData.calcName","");
            String moduName = Config.usr.getProperty("MyData.moduleName","");
            String moduCommand = Config.usr.getProperty("MyData.moduleModeCommand","");
            String moduTitle = Config.usr.getProperty("MyData.moduleModeTitle","");
            ChartModuleMode cmm = new ChartModuleMode( moduTitle, moduCommand );
            desktop.openChartPane( list, moduName, cmm, skin, calc );
        }
    }
    //「設定(P)」の「基本設定」
    private class BaseConfigAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String defTime = Config.getDefaultTime();
            if ( MainConfigDialog.showDialog( mf ) ) {
                // ﾃﾞﾌｫﾙﾄﾀｲﾑが変更されたときはDBの天体位置の再計算を行う
                if ( ! defTime.equals( Config.getDefaultTime() ) ) {
                    RecalculationDialog.showDialog(mf);
                }
            }
        }
    }
    //登録せずチャート作成
    private void createChart(String occasionType) {
        Natal natal = null;
        if ( occasionType.equals( Natal.NATAL ) ) {
            natal = DataInputDialog.showNatalDialog( mf,null,null );
        } else {
            natal = DataInputDialog.showEventDialog( mf,null,null );
        }
        if ( natal == null ) return;
        natal.setId( Natal.NEED_REGIST ); //後でDBへの保存が必要ですよという印をつける
        List<Natal> list = new ArrayList<Natal>();
        list.add( natal );
        desktop.openChartPane( list );
    }
    /**
     * チャートパネルにDB上で選択されたNatal(複数)を追加で渡す。
     * openNewChartと同様にイベントキューをつかって実行される。
     * @param list Natalのリスト
     * @param targetFrame nullなら現在選択されているChartInternalFrameにlistを追加。
     * null以外なら指定されたフレームにlistを追加。
     */
    public void addNatal(final List<Natal> list,final ChartPane targetFrame) {
        assert SwingUtilities.isEventDispatchThread();
        if(targetFrame == null) {
            ChartPane chartFrame = getSelectedChartPane();
            if(chartFrame != null) {
                chartFrame.addNatal(list);
            }
        } else {
            targetFrame.addNatal(list);
        }
    }
    // すでに開いているチャートフレームに、ダイアログでNatalを入力し追加する。
    private void addNatalToChart(String occasionType) {
        Natal natal = null;
        if ( occasionType.equals(Natal.NATAL) ) {
            natal = DataInputDialog.showNatalDialog(mf,null,null);
        } else {
            natal = DataInputDialog.showEventDialog(mf,null,null);
        }
        if ( natal == null ) return;
        natal.setId(Natal.NEED_REGIST); //後でDBへの保存が必要ですよという印をつける
        List<Natal> list = new ArrayList<Natal>();
        list.add( natal );
        ChartPane chartFrame = getSelectedChartPane();
        if ( chartFrame != null ) {
            chartFrame.addNatal(list);
        }
    }

   //ファイル(F) → チャートにデータ追加入力 → イベントデータ追加
    private class AddEventAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            addNatalToChart(Natal.EVENT);
        }
    }
    //ファイル(F)→チャートにデータ追加入力 → ネイタルデータ追加
    private class AddNatalAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            addNatalToChart(Natal.NATAL);
        }
    }
    //ファイル(F) 登録せずチャート作成 → イベントチャート
    private class EventChartAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            createChart(Natal.EVENT);
        }
    }
    //ファイル(F) 登録せずチャート作成 → ネイタルチャート
    private class NatalChartAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            createChart(Natal.NATAL);
        }
    }
    //ファイル(F) → データベースに登録 → イベントデータの登録
    private class RegEventAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            explorer.registEvent();
        }
    }
    //ファイル(F) → データベースに登録 → ネイタルデータの登録
    private class RegNatalAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            mf.setShutterVisible(true);
            explorer.registNatal();
        }
    }
    //ファイル(F) → バックアップ → 全データをエクスポート
    private class ExportAllAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            DataExporterDialog.exportAll(mf);
        }
    }
    //ファイル(F) → バックアップ → 選択したフォルダをエクスポート
    private class ExportSelectedAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            TreePath path = explorer.showFolderSelectDialog(
                "エクスポートするフォルダを選択してください。");
            if ( path == null ) return;
            String p = DBFactory.getPathString(path);
            if(p.length() == 0 || p.equals("ごみ箱")) {
                errorMessage( "ルートフォルダやごみ箱は指定できません。",
                              "エクスポートのエラー");
            } else {
                DataExporterDialog.export(mf,path);
            }
        }
    }
    //ファイル(F) → バックアップ → 全データをインポート
    private class ImportAllAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            String msg = "<html>全データのインポートを実行すると、" +
                "すでに登録されている出生データはすべて抹消されます。<br>" +
                "よろしいですか？<br><br>" +
                "( 実行する前に全データのエクスポートで" +
                "バックアップを取っておく事をお勧めします。)";
            if ( confirmMessage(msg,"全データをエクスポート") ) {
               DataImporterDialog.importAll(mf,explorer);
            }
        }
    }
    //ファイル(F) → バックアップ → 選択したフォルダにインポート
    private class ImportSelectedAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            TreePath path = explorer.showFolderSelectDialog(
                "インポートするフォルダを選択してください。");
            if ( path == null ) return;
            String p = DBFactory.getPathString(path);
            if ( p.length() == 0 || p.equals("ごみ箱") ) {
                errorMessage( "ルートフォルダやごみ箱は指定できません。",
                              "インポートのエラー" );
            } else {
                DataImporterDialog.load(mf,path,explorer);
            }
       }
    }
    //編集(E) → チャートの複製
    private class DuplicateAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
            duplicateChartPane();
        }
    }
    /**
     * HTMLの印刷実験用。普通に印刷すると大きすぎるので、マトリックスの係数を
     * いじって0.7掛け、つまり30%縮小するように指定してみた。
     * するとたしかに縮小されたが、印刷範囲が広くはならず、はみだした画像が、
     * そのまま小さく印刷された。失敗だが、しかしPageFormatをいじれば、
     * 縮小や拡大印刷が可能なことを表している。
     */
    class TestPageFormat extends PageFormat {
        double [] matrix = { 0.7, 0.0, 0.0, 0.7, 0.0, 0.0 };
        TestPageFormat() {
            super();
        }
        @Override
        public double [] getMatrix() {
            return matrix;
        }
    }
    //ファイル(F) → 印刷
    private class PrintAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            ChartPane chart = getSelectedChartPane();
            Printable printable = chart.getPainter();
            if ( printable == null ) return;

            PrintRequestAttributeSet aset = chart.getPrintRequestAttributeSet();
            if ( aset == null ) {
                System.out.println("印刷をサポートしてないモジュール");
                return;
            }
            /* Create a print job */
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setPrintable( printable );
            // プリンタ一覧を取得
            PrintService[] services =
                PrinterJob.lookupPrintServices();
            if ( services.length > 0 ) {
                //プロパティに保存してある名前と同名のサービスを取得
                String name = Config.usr.getProperty("PrinterName","");
                PrintService selectedService = services[0];
                for ( int i=0; i < services.length; i++ ) {
                    if ( services[i].getName().equals( name ) ) {
                        selectedService = services[i];
                        break;
                    }
                }
                try {
                    pj.setPrintService( selectedService );
                    //用紙選択のダイアログが開く
                    PageFormat pf = pj.pageDialog( aset );
                    if ( pf != null && pj.printDialog( aset ) ) {
                        //プリンタ選択ダイアログが開く
                        Config.usr.setProperty( "PrinterName",
                                         pj.getPrintService().getName() );
                        Config.save();
                        pj.print( aset );
                    }
                } catch ( PrinterException pe ) {
                    System.err.println(pe);
                }
            }

        }
    }
    //ファイル(F) → 終了
    private class ExitAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt) {
            //終了時に、アマテルが動いていた時間を、アマテルの年齢に加算保存
            long pt = StopWatch.getTimeBySec("稼働時間");
            long age = Config.usr.getLong( "AgeOfAmateru", 0L );
            Config.usr.setLong( "AgeOfAmateru", age + pt );
            Config.save();
            mf.dispose();
            System.exit(0);
        }
    }
    //ヘルプ(H) → AMATERUについて
    private class VersionAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            VersionDialog.showDialog(mf);
        }
    }

    private class DBOpenAction extends AbstractAction {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            mf.setShutterVisible(true);
        }
    }
    // エラーメッセージをダイアログで表示
    private void errorMessage(String msg,String title) {
        JOptionPane.showMessageDialog(
            mf, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    // 質問メッセージをダイアログで表示し、YESが選択されたときはtrueを返す。
    private boolean confirmMessage(String msg,String title) {
        int result = JOptionPane.showConfirmDialog( mf, msg, title,
             JOptionPane.YES_NO_OPTION,
             JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}
