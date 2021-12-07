/*
 * MainFrame2.java
 *
 * Created on 2007/11/05, 9:56
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.util.WindowMoveHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import to.tetramorph.starbase.multisplit.InnerPanel;
import to.tetramorph.starbase.multisplit.MultiTabbedPane;
import to.tetramorph.starbase.multisplit.PlainSplitPane;
import to.tetramorph.starbase.multisplit.PanelBar;
import to.tetramorph.starbase.multisplit.ShallowBevelBorder;
import to.tetramorph.starbase.multisplit.ShutterPane;
import to.tetramorph.starbase.widget.SingleSideBorder;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.MaximumWindowBounds;

/**
 * アプリケーションのメインのGUIコンポーネント。
 * @author  大澤義鷹
 */
class MainFrame2 extends javax.swing.JFrame {
    ShutterPane shutterPane;
    PlainSplitPane splitV;
    MultiTabbedPane multiPane;
    private static final Insets buttonInsets = new Insets(0,0,0,0);

    Icon maleIcon = IconLoader.getImageIcon("/resources/List.male.png");
    Icon femaleIcon = IconLoader.getImageIcon("/resources/List.female.png");

    Border border = BorderFactory.createLineBorder(Color.BLACK);
    boolean dbShutterMode = true;

    DBSplitPane dbSplit = new DBSplitPane();
    InnerPanel dbPane = new InnerPanel();
    ChartPane chartPane = new ChartPane();

    //検索結果出力パネル
    Component resultComponent;
    InnerPanel datePane; //日付入力パネルの入れ物

    /**
     * オブジェクトを作成する。
     */
    protected MainFrame2() {
        initComponents();
        setPreferredSize(new Dimension(1024,800));
        registMap();
        create();
        createToolBar();
        //ウィンドウ最大化の時、タスクバーにかぶらないようにする。
        addWindowFocusListener(new MaximumWindowBounds(this));
//        //ウィンドウのデスクトップ上での位置とサイズを記憶するようにする。
//        WindowMoveHandler winmove =
//            new WindowMoveHandler("MainFrame.BOUNDS", this);
//        addComponentListener(winmove);
//        winmove.setBounds();
        if ( ! Config.usr.getBoolean("ToolBarVisible",true) ) {
            getContentPane().remove( toolBar );
            toolbarCheckBoxMenuItem.setSelected( false );
        }
    }
    private void registMap() {
        MenuManager.put("Window.db",dbMenuItem);
        MenuManager.put("Window.search",searchMenuItem);
        MenuManager.put("Window.sabian",sabianMenuItem);
        MenuManager.put("Window.calendar",calendarMenuItem);
        MenuManager.put("Window.enneagram",enneagramMenuItem);
        MenuManager.put("Window.julian",julianMenuItem);
        MenuManager.put("Window.dict",dictMenuItem);
        MenuManager.put("ConfigMenu.baseConfig",configMenuItem);
        MenuManager.put("ModuleMenu",moduleMenu);
        MenuManager.put("ConfigMenu",configMenu);
        MenuManager.put("ViewMenu",viewMenu);
        MenuManager.put("WindowMenu",windowMenu);
        MenuManager.put("EditMenu",editMenu);
        MenuManager.put("FileMenu",fileMenu);
        MenuManager.put("FileMenu.nowChart",nowMenuItem );
        MenuManager.put("FileMenu.myChart",myChartMenuItem );
        MenuManager.put("FileMenu.regNatal",regNatalMenuItem);
        MenuManager.put("FileMenu.regEvent",regEventMenuItem);
        MenuManager.put("FileMenu.natalChart",natalChartMenuItem);
        MenuManager.put("FileMenu.eventChart",eventChartMenuItem);
        MenuManager.put("FileMenu.addNatal",addNatalMenuItem);
        MenuManager.put("FileMenu.addEvent",addEventMenuItem);
        MenuManager.put("FileMenu.exportAll",exportAllMenuItem);
        MenuManager.put("FileMenu.exportSelected",exportSelectedMenuItem);
        MenuManager.put("FileMenu.importAll",importAllMenuItem);
        MenuManager.put("FileMenu.importSelected",importSelectedMenuItem);
        MenuManager.put("FileMenu.exit", exitMenuItem );
        MenuManager.put("FileMenu.print", printMenuItem );
        MenuManager.put("EditMenu.duplicate",duplicateMenuItem);
        MenuManager.put("HelpMenu.version", versionMenuItem );
        MenuManager.put("HelpMenu.manual", manualMenuItem );
        MenuManager.put("HelpMenu.homepage", homeMenuItem );
        MenuManager.put("Button.maneuver",maneuverButton);
        //MenuManager.put("ChartsComboBox", chartsComboBox );
        MenuManager.put("LaunchChartSelectorPanel",launchChartSelectorPanel );
    }
    /**
     * データベース(ツリーと一覧テーブル)のパネルをセットする。
     */
    protected void setDBComponent(Component c) {
        dbSplit.setDBComponent( c );
    }
    /**
     * 検索結果出力パネルをセットする。
     */
    protected void setResultComponent(Component c) {
        resultComponent = c;
    }
    private void setToolTips() {
        PanelBar bar = dbPane.bar;
        bar.getButton(PanelBar.VISIBLE_BUTTON).setToolTipText("検索結果窓");
        bar.getButton(PanelBar.MINIMIZE_BUTTON).setToolTipText("最小化");
        shutterPane.getButton( PanelBar.COMEBACK_BUTTON ).
            setToolTipText("はめ込み");
        shutterPane.getButton( PanelBar.VISIBLE_BUTTON ).
            setToolTipText("検索結果窓");
        shutterPane.getButton( PanelBar.MINIMIZE_BUTTON ).
            setToolTipText("最小化");
    }
    private void create() {
        shutterPane = new ShutterPane();
        splitV = new PlainSplitPane( JSplitPane.VERTICAL_SPLIT );
        splitV.setTopComponent( chartPane );
        splitV.setDividerSize(1);
        dbPane.setComponent(dbSplit);
        dbPane.bar.setButtons( PanelBar.VISIBLE_BUTTON, PanelBar.MINIMIZE_BUTTON );
        dbPane.bar.getButton( PanelBar.MINIMIZE_BUTTON ).
            addActionListener(new ActionListener() {
            // はめ込みモードでのDB窓のバーの最小化ボタン
            // (→シャッターモードへ移行するボタン)
            @Override
            public void actionPerformed(ActionEvent evt) {
                splitV.setBottomComponent( null );
                splitV.setDividerSize(1);
                splitV.revalidate();
                shutterPane.setForegroundComponent(null);
                dbButton.setSelected(false);
                dbButtonVisible(true);
                dbShutterMode = true;
            }
        });

        multiPane = new MultiTabbedPane(this);
        chartPane.setCenter( multiPane );
        shutterPane.setBackgroundComponent(splitV);
        shutterPane.setButtons(  PanelBar.VISIBLE_BUTTON, PanelBar.COMEBACK_BUTTON,
            PanelBar.MINIMIZE_BUTTON);
        shutterPane.getButton(PanelBar.COMEBACK_BUTTON).
            addActionListener(new ActionListener() {
            //シャッターパネルのカムバックボタン(はめ込みモードにする)
            @Override
            public void actionPerformed(ActionEvent evt) {
                System.out.println("ボタンが押された2");
                Component c = shutterPane.getForegroundComponent();
                shutterPane.setForegroundComponent(null);
                dbPane.setScrollableComponent(dbSplit);
                splitV.setBottomComponent( dbPane );

                //splitV.setBottomComponent(dbSplit); //TEST
                splitV.setDividerSize(PlainSplitPane.DIVIDER_SIZE);
                splitV.setDividerLocation(0.7);
                dbButtonVisible(false);
                dbShutterMode = false;
            }
        });
        shutterPane.getButton( PanelBar.MINIMIZE_BUTTON).
            addActionListener(new ActionListener() {
            //シャッターパネルの最小化ボタン
            @Override
            public void actionPerformed(ActionEvent evt) {
                shutterPane.setForegroundComponent(null);
                splitV.setDividerSize(1);
                dbButton.setSelected(false);
            }
        });

        //検索結果窓を表示/非表示ボタン
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                AbstractButton btn = (AbstractButton)evt.getSource();
                setResultVisible(btn.isSelected());
            }
        };
        shutterPane.getButton( PanelBar.VISIBLE_BUTTON ).
            addActionListener(al);
        dbPane.bar.getButton( PanelBar.VISIBLE_BUTTON ).
            addActionListener(al);

        //setDatePanelVisible(false);
        datePane = new InnerPanel();
        datePane.setBorder(new ShallowBevelBorder());
        datePane.bar.setTitle("タイムマニューバ");
        datePane.bar.setButtons(PanelBar.MINIMIZE_BUTTON);
        datePane.bar.getButton(PanelBar.MINIMIZE_BUTTON).addActionListener(
            new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setDatePanelVisible(false);
            }
        });
        //datePane.setComponent(datePanel);
        chartPane.setDateComponent(datePane);
        chartPane.setDateVisible(false);
        statusPanel.setBorder(new SingleSideBorder(SingleSideBorder.TOP));
        statusPanel2.setBorder(new SingleSideBorder(SingleSideBorder.LEFT));
        add(shutterPane,BorderLayout.CENTER);
        setToolTips();
        pack();
    }

    /**
     * 検索結果のパネルを可視化/非可視化する。
     */
    public void setResultVisible(boolean b) {
        setShutterVisible(true);
        if ( b ) {
            dbSplit.setResultComponent(resultComponent);
        } else {
            dbSplit.setResultComponent(null);
        }
        shutterPane.getButton(PanelBar.VISIBLE_BUTTON).setSelected(b);
        dbPane.bar.getButton( PanelBar.VISIBLE_BUTTON ).setSelected(b);
    }

    //DBと検索結果を入れるパネル
    private class DBSplitPane extends PlainSplitPane {

        JPanel pane1 = new JPanel(new GridLayout(0,1));
        JPanel pane2 = new JPanel(new GridLayout(0,1));

        DBSplitPane() {
            super(PlainSplitPane.HORIZONTAL_SPLIT);
            pane1.setBorder(new ShallowBevelBorder());
            pane2.setBorder(new ShallowBevelBorder());
        }

        void setDBComponent(Component c) {
            pane1.removeAll();
            if(c != null) pane1.add(c);
            Component comp = c == null ? null : pane1;
            super.setLeftComponent( comp );
            setDivi();
        }

        void setResultComponent(Component c) {
            pane2.removeAll();
            if(c != null) pane2.add(c);
            Component comp = c == null ? null : pane2;
            super.setRightComponent( comp );
            setDivi();
        }

        void setDivi() {
            if(getLeftComponent() != null && getRightComponent() != null) {
                setDividerSize(PlainSplitPane.DIVIDER_SIZE);
                setDividerLocation(0.5);
            } else {
                setDividerSize(1);
            }
        }
        boolean isEmpty() {
            return getLeftComponent() == null && getRightComponent() == null;
        }

    }

    //マルチパネルと日付パネルを入れるパネル
    private class ChartPane extends JPanel {
        //InnerPanel datePane = new InnerPanel();
        boolean left = false;
        Component comp;
        boolean _visible = true;
        ChartPane() {
            super(new BorderLayout(4,0));
        }

        void setCenter(Component c) {
            add(c,BorderLayout.CENTER);
        }

        void setDateComponent(Component c) {
            if( c == null ) {
                if(comp != null) remove(comp);
                return;
            }
            if( comp != null && comp != c) remove(comp);
            String dir = left? BorderLayout.WEST : BorderLayout.EAST;
            add( c, dir );
            this.comp = c;
            revalidate();
        }

        void setDateVisible(boolean visible) {
            if(comp == null) return; //コンポーネントが無いなら無効
            if( visible ) {
                if(! _visible) setDateComponent(comp); //現在非表示なら再挿入
                //表示されてるならなにもしない
                _visible = true;
            } else {
                remove(comp);
                revalidate();
                _visible = false;
            }
        }
    }

    //「データベース」ボタンの可視/不可視をセットする。
    public void dbButtonVisible(boolean visible) {
        if(visible) {
            dbButtonPanel.add(dbButton);
        } else {
            dbButtonPanel.remove(dbButton);
        }
        dbButtonPanel.revalidate();
        trayVisible();
    }
    /**
     * 「日時」パネルの可視/不可視をセットする。
     */
    public void setDatePanelVisible(boolean visible) {
        if(visible) {
            dateButtonPanel.remove(maneuverButton);
            chartPane.setDateVisible(true);
        } else {
            dateButtonPanel.add(maneuverButton);
            chartPane.setDateVisible(false);
        }
        dateButtonPanel.revalidate();
        trayVisible();
    }
    // DBボタンと日時ボタンの両方が画面から無くなったときは、そのボタンが入っている
    // トレイのようなパネルも画面から消去。もし一つでもボタンがあり、トレイが非表示
    // のときは、トレイを表示する。トレイに表示されているときはなにもしない。
    private void trayVisible() {
        int db = dbButtonPanel.getComponentCount();
        int dt = dateButtonPanel.getComponentCount();
        if(db == 0 && dt == 0) {
            statusPanel.remove(trayButtonPanel);
        } else {
            for(Component c : statusPanel.getComponents()) {
                if(c == trayButtonPanel) return;
            }
            statusPanel.add(trayButtonPanel,BorderLayout.WEST);
        }
        validate();
        repaint();
    }
    /**
     * 日付入力パネルをセットする。
     */
    public void setDatePanel(Component c) {
        datePane.setComponent(c);
        datePane.revalidate();
        datePane.repaint();
    }
    /**
     * シャッターが有効のとき、シャッターの表示/非表示をセットする。
     * シャッター無効のときはなにもしない。
     */
    public void setShutterVisible(boolean visible) {
        if( dbShutterMode) {
            if(visible) {
                shutterPane.setTitle("データベース");
                shutterPane.setForegroundComponent(null);
                shutterPane.setForegroundComponent(dbSplit);
            } else {
                shutterPane.setForegroundComponent(null);
            }
            dbButton.setSelected(visible);
        }
    }



    public MultiTabbedPane getMultiTabbedPane() {
        return multiPane;
    }
//  /**
//   * ウィンドウ(W)メニューを返す。
//   */
//  public JMenu getWindowMenu() {
//    return windowMenu;
//  }
    /**
     * ツールバーに配置する画像ボタンを作成してかえす。
     */
    JButton createToolButton(String iconFileName) {
        JButton button = new JButton();
        button.setMargin(buttonInsets);
        //button.setContentAreaFilled( false );
        String iconName = "/resources/" + iconFileName;
        Icon normal = IconLoader.getImageIcon(iconName.concat("Normal.png"));
        button.setIcon(normal);
        return button;
    }

    /**
     * ツールバーにボタンを配置し、MenuManagerにボタンを登録する。
     */
    final void createToolBar() {
        toolBar.removeAll();
        toolBar.setFloatable(true);
        String [] iconNames = { "dbin","search","|","now","mychart","|",
        "dict","sabian","voidcal","enncal" };

        String [] tips = { "データベース登録","検索","","今のチャート","マイチャート","",
        "辞書","サビアン辞書","ボイドカレンダー","エニアグラムカレンダー" };
        for(int i=0; i<iconNames.length; i++) {
            String name = iconNames[i];
            if(name.equals("|")) {
                toolBar.addSeparator(new Dimension(8,32));
            } else {
                JButton button = createToolButton(name);
                button.setToolTipText(tips[i]);
                MenuManager.put("ToolButton."+name,button);
                toolBar.add(button);
            }
        }
    }
//  //以下はテスト用メソッド
//
//  int count = 0; //タブのシリアル番号
//
//  private void add() {
//    count++;
//    multiPane.insert(maleIcon, "No." + count, createTab());
//    //multiPane.revalidate();
//  }
//  //　テスト用のタベッドペインを作成
//  private Component createTab() {
//    TreeOfLifePanel tree = new TreeOfLifePanel();
//    tree.setNumber(count);
//    tree.setBackground(Color.LIGHT_GRAY);
//    return tree;
//  }
//  private void reset() {
//    count = 0;
//    multiPane.removeAll();
//  }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configMenuItem = new javax.swing.JMenuItem();
        duplicateMenuItem = new javax.swing.JMenuItem();
        toolBar = new javax.swing.JToolBar();
        nowButton = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        trayButtonPanel = new javax.swing.JPanel();
        dbButtonPanel = new javax.swing.JPanel();
        dbButton = new javax.swing.JToggleButton();
        dateButtonPanel = new javax.swing.JPanel();
        maneuverButton = new javax.swing.JButton();
        statusPanel2 = new javax.swing.JPanel();
        launchChartSelectorPanel = new to.tetramorph.starbase.LaunchChartSelectorPanel();
        statusLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        nowMenuItem = new javax.swing.JMenuItem();
        myChartMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        regNatalMenuItem = new javax.swing.JMenuItem();
        regEventMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        natalChartMenuItem = new javax.swing.JMenuItem();
        eventChartMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        addNatalMenuItem = new javax.swing.JMenuItem();
        addEventMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        exportMenu = new javax.swing.JMenu();
        exportSelectedMenuItem = new javax.swing.JMenuItem();
        exportAllMenuItem = new javax.swing.JMenuItem();
        importMenu = new javax.swing.JMenu();
        importSelectedMenuItem = new javax.swing.JMenuItem();
        importAllMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        moduleMenu = new javax.swing.JMenu();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        dbMenuItem = new javax.swing.JMenuItem();
        searchMenuItem = new javax.swing.JMenuItem();
        dictMenuItem = new javax.swing.JMenuItem();
        sabianMenuItem = new javax.swing.JMenuItem();
        calendarMenuItem = new javax.swing.JMenuItem();
        enneagramMenuItem = new javax.swing.JMenuItem();
        julianMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        toolbarCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        configMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        versionMenuItem = new javax.swing.JMenuItem();
        manualMenuItem = new javax.swing.JMenuItem();
        homeMenuItem = new javax.swing.JMenuItem();

        configMenuItem.setText("環境設定");
        configMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configMenuItemActionPerformed(evt);
            }
        });

        duplicateMenuItem.setText("チャートを複製");
        duplicateMenuItem.setEnabled(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AMATERU");

        toolBar.setFloatable(false);

        nowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/nowNormal.png"))); // NOI18N
        nowButton.setBorderPainted(false);
        nowButton.setContentAreaFilled(false);
        nowButton.setFocusPainted(false);
        toolBar.add(nowButton);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        statusPanel.setLayout(new java.awt.BorderLayout(16, 0));

        trayButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 2));

        dbButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        dbButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/db2.png"))); // NOI18N
        dbButton.setText("データベース");
        dbButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        dbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbButtonActionPerformed(evt);
            }
        });
        dbButtonPanel.add(dbButton);

        trayButtonPanel.add(dbButtonPanel);

        dateButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        maneuverButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/maneuver2.png"))); // NOI18N
        maneuverButton.setText("マニューバ");
        maneuverButton.setToolTipText("ネイタルやトランジットの時間を加減操作");
        maneuverButton.setEnabled(false);
        maneuverButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        maneuverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maneuverButtonActionPerformed(evt);
            }
        });
        dateButtonPanel.add(maneuverButton);

        trayButtonPanel.add(dateButtonPanel);

        statusPanel.add(trayButtonPanel, java.awt.BorderLayout.WEST);

        statusPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        statusPanel2.add(launchChartSelectorPanel);

        statusLabel.setText(" ");
        statusPanel2.add(statusLabel);

        statusPanel.add(statusPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setMnemonic('F');
        fileMenu.setText("ファイル(F)");

        nowMenuItem.setText("今のチャート");
        fileMenu.add(nowMenuItem);

        myChartMenuItem.setText("マイチャート");
        fileMenu.add(myChartMenuItem);
        fileMenu.add(jSeparator8);

        regNatalMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        regNatalMenuItem.setText("ネイタルデータの登録");
        fileMenu.add(regNatalMenuItem);

        regEventMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        regEventMenuItem.setText("イベントデータの登録");
        fileMenu.add(regEventMenuItem);
        fileMenu.add(jSeparator2);

        natalChartMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        natalChartMenuItem.setText("登録せずネイタルチャートを表示");
        fileMenu.add(natalChartMenuItem);

        eventChartMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        eventChartMenuItem.setText("登録せずイベントチャートを表示");
        fileMenu.add(eventChartMenuItem);
        fileMenu.add(jSeparator4);

        addNatalMenuItem.setText("ネイタルデータをチャートに追加");
        fileMenu.add(addNatalMenuItem);

        addEventMenuItem.setText("イベントデータをチャートに追加");
        fileMenu.add(addEventMenuItem);
        fileMenu.add(jSeparator6);

        printMenuItem.setText("印刷");
        fileMenu.add(printMenuItem);
        fileMenu.add(jSeparator5);

        exportMenu.setText("エクスポート");

        exportSelectedMenuItem.setText("部分エクスポート");
        exportMenu.add(exportSelectedMenuItem);

        exportAllMenuItem.setText("全データをエクスポート");
        exportMenu.add(exportAllMenuItem);

        fileMenu.add(exportMenu);

        importMenu.setText("インポート");

        importSelectedMenuItem.setText("部分インポート");
        importMenu.add(importSelectedMenuItem);

        importAllMenuItem.setText("全データをインポート");
        importMenu.add(importAllMenuItem);

        fileMenu.add(importMenu);
        fileMenu.add(jSeparator3);

        exitMenuItem.setText("終了");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("編集(E)");
        jMenuBar1.add(editMenu);

        moduleMenu.setMnemonic('C');
        moduleMenu.setText("チャート(C)");
        jMenuBar1.add(moduleMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("表示(V)");
        jMenuBar1.add(viewMenu);

        windowMenu.setMnemonic('W');
        windowMenu.setText("ウィンドウ(W)");

        dbMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        dbMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/db2.png"))); // NOI18N
        dbMenuItem.setText("データベース");
        windowMenu.add(dbMenuItem);

        searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        searchMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search_icon.gif"))); // NOI18N
        searchMenuItem.setText("検索");
        windowMenu.add(searchMenuItem);

        dictMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/smallDictIcon.png"))); // NOI18N
        dictMenuItem.setText("辞書");
        windowMenu.add(dictMenuItem);

        sabianMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sabianIcon.png"))); // NOI18N
        sabianMenuItem.setText("サビアン辞書");
        windowMenu.add(sabianMenuItem);

        calendarMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/voidcalIcon.png"))); // NOI18N
        calendarMenuItem.setText("天象カレンダーミチテル");
        windowMenu.add(calendarMenuItem);

        enneagramMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/enneagram.gif"))); // NOI18N
        enneagramMenuItem.setText("エニアグラムカレンダー");
        windowMenu.add(enneagramMenuItem);

        julianMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Julius.png"))); // NOI18N
        julianMenuItem.setText("ユリウス計算機");
        windowMenu.add(julianMenuItem);
        windowMenu.add(jSeparator7);

        toolbarCheckBoxMenuItem.setSelected(true);
        toolbarCheckBoxMenuItem.setText("ツールバー表示");
        toolbarCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarCheckBoxMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(toolbarCheckBoxMenuItem);

        jMenuBar1.add(windowMenu);

        configMenu.setMnemonic('P');
        configMenu.setText("設定(P)");
        jMenuBar1.add(configMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("ヘルプ(H)");

        versionMenuItem.setText("AMATERUについて");
        helpMenu.add(versionMenuItem);

        manualMenuItem.setText("マニュアル");
        helpMenu.add(manualMenuItem);

        homeMenuItem.setText("アマテルのホームページ");
        helpMenu.add(homeMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    Object constraints;
    private void toolbarCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarCheckBoxMenuItemActionPerformed
        boolean seld = ((JMenuItem)evt.getSource()).isSelected();
        Config.usr.setBoolean( "ToolBarVisible", seld );
        if ( ! seld ) {
            constraints = ((BorderLayout)getContentPane().getLayout()).getConstraints( toolBar );
            getContentPane().remove( toolBar );
            validate();
        } else {
            if ( constraints == null ) constraints = BorderLayout.NORTH;
            getContentPane().add( toolBar, constraints );
            validate();
        }
    }//GEN-LAST:event_toolbarCheckBoxMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void maneuverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maneuverButtonActionPerformed
        setDatePanelVisible(true);
    }//GEN-LAST:event_maneuverButtonActionPerformed

  private void configMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configMenuItemActionPerformed

  }//GEN-LAST:event_configMenuItemActionPerformed
  // 「日付」ボタン
  private void dbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbButtonActionPerformed
      setShutterVisible(dbButton.isSelected());
  }//GEN-LAST:event_dbButtonActionPerformed

  private static void createAndShowGUI() {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      new MainFrame2().setVisible(true);
  }
  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
      java.awt.EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
              createAndShowGUI();
          }
      });
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addEventMenuItem;
    private javax.swing.JMenuItem addNatalMenuItem;
    private javax.swing.JMenuItem calendarMenuItem;
    protected javax.swing.JMenu configMenu;
    protected javax.swing.JMenuItem configMenuItem;
    private javax.swing.JPanel dateButtonPanel;
    private javax.swing.JToggleButton dbButton;
    private javax.swing.JPanel dbButtonPanel;
    private javax.swing.JMenuItem dbMenuItem;
    private javax.swing.JMenuItem dictMenuItem;
    private javax.swing.JMenuItem duplicateMenuItem;
    protected javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem enneagramMenuItem;
    private javax.swing.JMenuItem eventChartMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportAllMenuItem;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JMenuItem exportSelectedMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem homeMenuItem;
    private javax.swing.JMenuItem importAllMenuItem;
    private javax.swing.JMenu importMenu;
    private javax.swing.JMenuItem importSelectedMenuItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenuItem julianMenuItem;
    private to.tetramorph.starbase.LaunchChartSelectorPanel launchChartSelectorPanel;
    private javax.swing.JButton maneuverButton;
    private javax.swing.JMenuItem manualMenuItem;
    private javax.swing.JMenu moduleMenu;
    private javax.swing.JMenuItem myChartMenuItem;
    private javax.swing.JMenuItem natalChartMenuItem;
    private javax.swing.JButton nowButton;
    private javax.swing.JMenuItem nowMenuItem;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem regEventMenuItem;
    private javax.swing.JMenuItem regNatalMenuItem;
    private javax.swing.JMenuItem sabianMenuItem;
    private javax.swing.JMenuItem searchMenuItem;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel statusPanel2;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JCheckBoxMenuItem toolbarCheckBoxMenuItem;
    private javax.swing.JPanel trayButtonPanel;
    private javax.swing.JMenuItem versionMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables

}
