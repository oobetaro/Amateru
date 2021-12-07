/*
 * MainFrame.java
 *
 * Created on 2006/09/08, 19:02
 */

package to.tetramorph.starbase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreePath;
import to.tetramorph.astrocalendar.AstroCalendar;
import to.tetramorph.astrocalendar.EnneagramCalendar2;
import to.tetramorph.starbase.lib.Home;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.MaximumWindowBounds;

/**
 * この占星術ソフトのメインフレーム。
 * 複数のチャートフレームを内部フレームで格納したり、様々な機能を呼び出すメニュー
 * をもっている。内部フレームを管理する。
 * System.getProperty("SeparateMode").equals("true")ならフレーム分離モードとなり、
 * setVisible,toFrontなどの操作を行う。
 * <pre>
 * System.setProperty()されるもの。
 * DefaultTime = "12:00:00"
 * SeparaterMode = "true"
 * <pre>
 *
 * プロパティの管理について。
 * 起動したらすぐにSystem.setProperties()して、アプリ専用のプロパティオブジェクト
 * にすげかえ、さらにそこにDBから設定値をロードする。
 * Systemプロパティはどのクラスからもstatic参照できるので、グローバル変数とみなし
 * て活用する。アプリ終了のとき、System.getProperties()したものをHSQLDBに書き出し
 * て一括保存する。つまりアプリ起動中にSystem.setProperty()した値はつねにDBに保管
 * され、不揮発性変数として使うことができるようになっている。
 * System.setProperties()するオブジェクトはPropertiesオブジェクトを継承した
 * Preferenceオブジェクト。Propertiesの機能で不十分なときは、Preferenceにキャスト
 * して使う。またSystem.setProperties()するPreferenceオブジェクトは、
 * System.getProperties()を継承したものなので、システム情報も同じプロパティから
 * 取得することができる。
 */
final class MainFrame extends JFrame implements ChartDesktop {
  //データベースフレーム
  DatabaseFrame editor = new DatabaseFrame(this);
  //ESCキーで内部フレームを閉じるようにするためのキーハンドラ
  MainKeyHandler mainKeyHandler = new MainKeyHandler();
  //選択されている内部フレームが所有している時間制御パネル
  TimePanel selectedTimeControllerPanel = null;
  //内部フレームスプレッド機能で使用
  int count = 0; //スプレッドボタンのトグル回数
  JInternalFrame frame0 = null; //スプレッド機能で使用。選択中の内部フレーム
  SplitPane splitPane = new SplitPane(); //オリジナルのスプリッタ
  int devider = 0; // データーベース区画のJSplitPaneのスライダーの位置(pixel)
  SabianDialog sabianDialog; //サビアンダイアログ
  JDayDialog jdayDialog; //ユリウス日計算機
  TimeSliderDialog timeSliderDialog; //時間制御ダイアログ
  FocusHandler iframeFocusHandler = new FocusHandler();
  ChartFrameCloseHandler iframeCloseHandler = new ChartFrameCloseHandler();

  /**
   * mainメソッドが呼び出すコンストラクタ。直接インスタンスは作成できない。
   */  
  private MainFrame() {
    initComponents();
    splitPane.setMasterComponent(desktopPane);
    splitPane.setMinimumSize(new Dimension(0,0));
    windowMenu.add(splitPane.getVisibleCheckBoxMenuItem());
    windowMenu.add(splitPane.getSwapCheckBoxMenuItem());
    if(System.getProperty("SeparateMode","false").equals("true")) {
      add( splitPane,BorderLayout.CENTER );
      editor.setVisible(true);
    } else {
      add(bottomSplitPane,BorderLayout.CENTER);
      bottomSplitPane.setTopComponent(splitPane);
      bottomSplitPane.setDividerLocation(500);
      dbSplitPane.setLeftComponent(editor.getContentPane());
      dbSplitPane.setRightComponent(editor.getSearchResultContentPane());
    }
    createMenu();
    addWindowFocusListener(new MaximumWindowBounds(this));
    //フレームが最大化されたとき検索やデータベースのフレームを前面に移動
    addWindowStateListener(new WindowAdapter() {
      //フレーム最大化を検出
      public void windowStateChanged(WindowEvent evt) {
        if(evt.getNewState() == Frame.MAXIMIZED_BOTH) {
          if(editor.isShowing() ) editor.toFront();
          if(editor.isSearchFrameShowing()) editor.showSearchFrame();
        }
      }
    });
    WindowMoveHandler winmove =
      new WindowMoveHandler("MainFrame.BOUNDS", this);
    addComponentListener(winmove);
    winmove.setBounds();
    desktopPane.addContainerListener(new DesktopPaneHandler());
    setIconImage(IconLoader.getImage("/resources/images/starbase_icon.png"));
    setKeyListener(mainKeyHandler);    
    TreePath current = editor.foundTreePath( Config.system.getProperty("CurrentTreePath") );
    editor.selectFolder(current);
    sabianDialog = new SabianDialog(this); //Systemプロパティがロードされてから作る
    timeSliderDialog = new TimeSliderDialog(this);
    jdayDialog = new JDayDialog(this);
    pack();
  }
  /** サビアンダイアログを返す。*/
  protected SabianDialog getSabianDialog() {
    return sabianDialog;
  }
  //キーボードショートカットのハンドラ
  private class MainKeyHandler extends KeyAdapter {
    public void keyTyped(KeyEvent e) {
      int code =(int)e.getKeyChar();
      char c = e.getKeyChar();
      //System.out.println("MainFrame: KEYCODE = " + code +", KEYCHAR = " + e.getKeyChar()+ ", SHIFT_DOWN = " + e.isShiftDown());
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame == null) {
        //System.out.println("セレクトされてるチャートフレームはない");
        return;
      }
      if(code == KeyEvent.VK_ESCAPE) { //ESCキーでウィンドウ閉じる
        chartFrame.doDefaultCloseAction();
      }
    }
  }
  //DesktopPaneで内部フレームが追加・削除された際のイベント処理
  private class DesktopPaneHandler implements ContainerListener {
    public void componentAdded(ContainerEvent e) {
      count = 0;
      chartAddMenu.setEnabled(true);
      splitPane.getVisibleCheckBoxMenuItem().setEnabled(true);
      newChartFrameMenuItem.setEnabled(true);
    }
    //内部ﾌﾚｰﾑが削除されたとき
    public void componentRemoved(ContainerEvent e) {
       //内部ﾌﾚｰﾑが0枚になったら時間制御ﾊﾟﾈﾙとｽﾌﾟﾘｯﾀを消去
      ChartInternalFrame [] frame = getChartFrames();
//      System.out.println("内部フレーム削除 残りフレーム数 = " + frame.length);
      if(focusFrame != null && frame.length > 0 ) {
        focusFrame.restoreSubcomponentFocus();
//        System.out.println("フレーム名 " + focusFrame.getTitle() + 
//          ", フォーカスの状態 = " + focusFrame.isRequestFocusEnabled() + 
//          " フォーカス要求の成否 = " + focusFrame.requestFocusInWindow());
      }
      count = 0;
    }
  }
  private class ChartFrameCloseHandler extends InternalFrameAdapter {
    public void internalFrameClosed(InternalFrameEvent e) {
      //System.out.println("内部フレームClosed 残りフレーム数 = " + getChartFrames().length);
      if(e.getInternalFrame() == focusFrame) focusFrame = null;
      if(isEmptyChartFrame()) {
        timeSliderDialog.setVisible(false);
        chartAddMenu.setEnabled(false);
        setTimeController(null);
        splitPane.getVisibleCheckBoxMenuItem().setEnabled(false);
        newChartFrameMenuItem.setEnabled(false);
      }      
    }
  }
  ChartInternalFrame focusFrame;
  //チャートフレームのフォーカスイベントの処理を行う
  private class FocusHandler implements FocusListener {
    public void focusGained(FocusEvent e) {
      //JDesktopPaneにはJOptionPane.showInternalMessageDialog等が挿入されることが
      //ありうる。このハンドラはChartInternalFrameのフォーカス処理以外は無視する。
      //System.out.println("フォーカスが来た ");
      if( ! (e.getComponent() instanceof ChartInternalFrame) ) {
        //System.out.println("チャートフレームじゃないので無視します");
        return;
      }
      ChartInternalFrame frame = (ChartInternalFrame)e.getComponent();
      if(frame.isClosed()) {
        //System.out.println("クローズされてるフレームなので無視します");
        return;
      }
      focusFrame = frame;
      System.out.println(frame.getTitle());
      //ﾌｫｰｶｽを持つ最後のｻﾌﾞｺﾝﾎﾟｰﾈﾝﾄにﾌｫｰｶｽを復元するよう、内部ﾌﾚｰﾑに要求
      //しないと複数ﾌﾚｰﾑを連続でESCｷｰでｸﾛｰｽﾞできなくなる。
      frame.restoreSubcomponentFocus();
      frame.getTimeMenu(timeMenu);
      TimePanel tp = frame.getTimePanel();
      setTimeController(tp);
      timeSliderDialog.setTimeSliderPanel(tp.getTimeSliderPanel());
      setPreferenceMenu();
    }
    public void focusLost(FocusEvent e) {
      System.out.println("フォーカスが去った ");
      setPreferenceMenu();
    }
  }
  //InternalFrameにフォーカスが来たﾀｲﾐﾝｸﾞで、計算設定ﾒﾆｭｰを動的に設定する。
  //各ﾌﾟﾗｸﾞｲﾝの設定ﾊﾟﾈﾙ呼びだしに関係。ｱｸｾﾗﾚｰﾀｰｷｰで呼び出すためには、
  //preferenceMenuのｲﾍﾞﾝﾄのﾀｲﾐﾝｸﾞで行うわけにはいかない。07/9/16変更
  private void setPreferenceMenu() {
    prefMenu.removeAll();
    prefMenu.add(configMenuItem);
    ChartInternalFrame f = getSelectedFrame();
    if(f == null) return;
    prefMenu.add(f.getColorSelectionMenu());
    prefMenu.add(f.getSpecificSelectionMenu());
  }
  
  //チャートフレームの複製
  void duplicateChartFrame() {
//    ChartInternalFrame nowFrame = //現在選択中の内部フレーム
//      (ChartInternalFrame)desktopPane.getSelectedFrame();
    ChartInternalFrame nowFrame = getSelectedFrame();
    ChartInternalFrame newFrame = new ChartInternalFrame(nowFrame); //複製
    newFrame.addInternalFrameListener( iframeCloseHandler );
    newFrame.addKeyListener(mainKeyHandler);
    newFrame.addFocusListener(iframeFocusHandler);
    timeSliderDialog.setTimeSliderPanel(newFrame.getTimeSliderPanel());
    newFrame.setVisible(true);
    desktopPane.add(newFrame);
    try {
      newFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
  }
  /**
   *
   */
  public Frame getFrame() {
    return this;
  }
  /**
   * 新しくチャートフレームを開いて、Natalのリストをセットする。
   */
  public void openChartFrame(List<Natal> list) {
    assert SwingUtilities.isEventDispatchThread();
    if(!(list.size() == 1 && list.get(0).getChartType().equals(Natal.COMPOSIT))) {
      for(int i=0; i<list.size(); i++) {
        if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
          StringBuffer sb = new StringBuffer();
          sb.append("<html>");
          sb.append("複数のデータを一度に入力する場合、コンポジットデータと<br>");
          sb.append("非コンポジットデータを混在させて指定することはできません。<br>");
          sb.append("また複数のコンポジットデータを一度に入力することもできません。<br>");
          sb.append("ただし後から追加することは可能です。<br>");
          sb.append("</html>");
          JOptionPane.showMessageDialog(MainFrame.this,sb.toString(),
            "新規チャートのエラー",JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    ChartInternalFrame frame = new ChartInternalFrame(editor,timeMenu,sabianDialog,this);
    frame.selectModule(0);
    frame.addInternalFrameListener( iframeCloseHandler );
    frame.addKeyListener( mainKeyHandler );
    frame.addFocusListener( iframeFocusHandler );
    frame.setVisible(true);
    timeSliderDialog.setTimeSliderPanel(frame.getTimeSliderPanel());
    desktopPane.add(frame);
    try {
      frame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
    frame.setNatal(list);
  }

  /**
   * すべてのチャートフレームのリストを返す。
   * 一枚も無いときは長さ0の配列が返る。
   */
  public ChartInternalFrame [] getChartFrames() {
    JInternalFrame [] frames = desktopPane.getAllFrames();
    List<ChartInternalFrame> list = new ArrayList<ChartInternalFrame>();
    for(JInternalFrame f:frames) {
      if(f instanceof ChartInternalFrame)
        list.add((ChartInternalFrame)f);
    }
    ChartInternalFrame [] results = new ChartInternalFrame[ list.size() ];
    for(int i=0; i<list.size(); i++) results[i] = list.get(i);
    return results;
  }
  /**
   * 指定されたIDのデータがチャートフレーム(複数)に登録されている場合はtrueを
   * 返す。つまりチャートに表示されているNatalデータならtrueとなる。
   * その際、データベース上からそのNatalデータを削除と移動はしてはならない。
   * 表示中のデータは「現在のデータを編集」機能で編集しなければならない。
   */
  public boolean isDataBusy(int id) {
    ChartInternalFrame [] frames = getChartFrames();
    for(int i=0; i<frames.length; i++) {
      if(frames[i].isComprise(id)) return true;
    }
    return false;
  }
  /**
   * チャートフレームが一枚も無いときはtrueを返す。
   */
  public boolean isEmptyChartFrame() {
    return getChartFrames().length == 0;
  }  
  /**
   * ChartInnerFrameにDatabaseFrameやSerachResultTable上で選択されたNatal
   * (複数)をセット。ChartInternalFrameにある既存のデータは消え新しいものに置き換わる。
   * @param list Natalのリスト
   * @param targetFrame nullなら現在選択されているChartInternalFrameにlistをセット。
   * null以外なら指定されたフレームにlistをセット。
   */
  public void setNatal(final List<Natal> list,final ChartInternalFrame targetFrame) {
    assert SwingUtilities.isEventDispatchThread();
    if(targetFrame == null) {
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame != null) {
        chartFrame.setNatal(list);
      }
    } else {
      targetFrame.setNatal(list);
    }
  }
  /**
   * ChartInnerFrameにDB上で選択されたNatal(複数)を追加で渡す。
   * openNewChartと同様にイベントキューをつかって実行される。
   * @param list Natalのリスト
   * @param targetFrame nullなら現在選択されているChartInternalFrameにlistを追加。
   * null以外なら指定されたフレームにlistを追加。
   */
  public void addNatal(final List<Natal> list,final ChartInternalFrame targetFrame) {
    assert SwingUtilities.isEventDispatchThread();
    if(targetFrame == null) {
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame != null) {
        chartFrame.addNatal(list);
      }
    } else {
      targetFrame.addNatal(list);
    }
  }
  //メニューバーのメニュー(一部)を作成
  private void createMenu() {
    //モジュールメニュー
    moduleMenu.add(new JMenuItem("test"));
    moduleMenu.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent e) {
        ChartInternalFrame frame = getSelectedFrame();
          //(ChartInternalFrame)desktopPane.getSelectedFrame();
        if(frame != null) frame.getChartMenu(moduleMenu);
      }
      public void menuCanceled(MenuEvent e) { }
      public void menuDeselected(MenuEvent e) { }
    });
  }
  /**
   * 時間制御パネルの可視/非可視を選択する。
   * @param visible trueなら可視化、falseなら非可視化。
   */
  void showTimeController(boolean visible) {
    splitPane.setVisible(visible);
  }
  /**
   * 時間パネルをMainFrameの右端にセットし可視化する。すでに登録されている
   * パネルは削除される。nullをセットすると時間パネルは消去される。
   */
  private void setTimeController(TimePanel tcp) {
    System.out.println("MainFrame#setTimeController() : タイムパネルセット");
    splitPane.setSlaveComponent(tcp);
  }
  // すでに開いているチャートフレームに、ダイアログでNatalを入力し追加する。
  private void addNatalToChart(String occasionType) {
    Natal occ = null;
    if(occasionType.equals(Natal.NATAL)) {
      occ = DataInputDialog.showNatalDialog(this,null,null);
    } else {
      occ = DataInputDialog.showEventDialog(this,null,null);
    }
    if(occ == null) return;
    occ.setId(Natal.NEED_REGIST); //後でDBへの保存が必要ですよという印をつける
    List<Natal> list = new ArrayList<Natal>();
    list.add(occ);
    ChartInternalFrame chartFrame =
      (ChartInternalFrame)desktopPane.getSelectedFrame();
    if(chartFrame != null) {
      chartFrame.addNatal(list);
    }
  }
  //登録せずチャート作成
  private void createChart(String occasionType) {
    Natal occ = null;
    if(occasionType.equals(Natal.NATAL)) {
      occ = DataInputDialog.showNatalDialog(this,null,null);
    } else {
      occ = DataInputDialog.showEventDialog(this,null,null);
    }
    if(occ == null) return;
    occ.setId(Natal.NEED_REGIST); //後でDBへの保存が必要ですよという印をつける
    List<Natal> list = new ArrayList<Natal>();
    list.add(occ);
    this.openChartFrame(list);
  }
  //MainFrame内のフォーカスを受け取る部品すべてにキーリスナを登録
  void setKeyListener(KeyListener l) {
    addKeyListener(l);
    closeFrameButton.addKeyListener(l);
    dbButton.addKeyListener(l);
    desktopPane.addKeyListener(l);
    expansionFrameButton.addKeyListener(l);
    fileMenu.addKeyListener(l);
    iconizeFrameButton.addKeyListener(l);
    menuBar.addKeyListener(l);
    moduleMenu.addKeyListener(l);
    prefMenu.addKeyListener(l);
    searchButton.addKeyListener(l);
    spreadButton.addKeyListener(l);
    spreadButton2.addKeyListener(l);
    timeMenu.addKeyListener(l);
    toolBar.addKeyListener(l);
    windowMenu.addKeyListener(l);
  }
  /** 時間制御ダイアログを返す (使用されていない) */
  protected TimeSliderDialog getTimeSliderDialog() {
    return timeSliderDialog;
  }
  /** 
   * 選択中のチャートフレームを返す。(このクラス内でのみ使用)
   * 選択中のチャートフレームが存在しない場合はnullを返す。
   */
  protected ChartInternalFrame getSelectedFrame() {
    if(desktopPane.getSelectedFrame() instanceof ChartInternalFrame)
      return (ChartInternalFrame)desktopPane.getSelectedFrame();
    return null;
  }
  //メタルのLookAndFeelを設定
  private static void createAndShowGUI() {
    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame.setDefaultLookAndFeelDecorated(true);
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      UIManager.put("AuditoryCues.playList",
        UIManager.get("AuditoryCues.allAuditoryCues"));
      //updateLookAndFeel();
    }
    MainFrame frame = new MainFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
  /**
   * Systemプロパティに"TestMode=false"をセットすると、本番モードになり、
   * プログラムの実行と共にDBの起動、終了と共にDBのSHUTDOWNが行われる。<BR>
   *
   * "TestMode=true"にしておくとテスト用モードとなり、DBは事前に起動しておかなけ
   * ればならない。DBの起動は時間がかかるため、テストランのたびに起動・停止をする
   * のはかったるいのでDBは常駐させておくやり方をする。
   *
   * アプリの二重起動はトラブルの元なので抑止してあるが、テストモードでは二重起動
   * が起きうる。
   */
  public static void main(String args[]) throws MalformedURLException {
    //Preference pref = new Preference(System.getProperties());
    //System.setProperties(pref);
    //本番モードはfalseをセット。DBの起動が行われるようになる。
    System.setProperty("TestMode","true");
    if(System.getProperty("swe.path") == null) throw
      new IllegalStateException("System property 'swe.path' not found.");
    //DBや検索結果の窓を別フレームにするときはtrueをセット。一体型はfalse。
    System.setProperty("SeparateMode","false");
    System.setProperty("DefaultTime",Config.data.getProperty("DefaultTime"));
    System.out.println("System : DefaultTime = " + System.getProperty("DefaultTime"));
    //JDBC をロードする
    try { Class.forName("org.hsqldb.jdbcDriver");
    } catch ( ClassNotFoundException e ) { e.printStackTrace(); }
    if(System.getProperty("TestMode").equals("false")) {
      //本番モード
      //DBに接続してみるがこの段階でDBが起動してれば二重起動なので終わる。
      //つまりここでは接続できないのが正しい
      boolean doubleBoot = false;
      try {
        String driverURL = "jdbc:hsqldb:hsql://localhost";
        Connection con = DriverManager.getConnection(driverURL,"sa",
          Config.system.getProperty("db.admin.pw"));
        doubleBoot = true;
      } catch( SQLException e ) { 
        //e.printStackTrace();
      }
      System.out.println("JDBCロード完了");
      if(doubleBoot) {
        JOptionPane.showMessageDialog(null,"二重起動はできません。","StarBase",
          JOptionPane.ERROR_MESSAGE);
        System.out.println("二重起動はできません。");
        System.exit(0);
      }
      //標準出力をすげ替え、エラー情報等をlog.txtに出力するようにする。
      try {
        PrintStream ps = new PrintStream(new File(Home.dir,"log.txt"),"sjis");
        System.setOut(ps);
        System.setErr(ps);
      } catch(IOException e) {
        e.printStackTrace();
      }
      //HSQLDBを起動
      String dbfile = null;
      try {
        //dbfile = Home.database.toURI().toURL().toString();
        dbfile = Home.database.toURL().toString();
      } catch (MalformedURLException e) { }
      // 行末の""はデータベースに別名を与える際に使用するが与えないので空。
      org.hsqldb.Server
        .main(new String [] {"-database.0",dbfile,"-dbname.0",""});
    }
    //ﾃﾞｰﾀﾍﾞｰｽにｱｶｳﾝﾄが用意されているか検査して
    //正しくDBのｲﾝｽﾀﾝｽが取得できればｽﾀｰﾄ。だめならExceptionが出てｽﾄｯﾌﾟ
    Database.getInstance();
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    configMenuItem = new javax.swing.JMenuItem();
    desktopPane = new javax.swing.JDesktopPane();
    bottomSplitPane = new javax.swing.JSplitPane();
    dbSplitPane = new javax.swing.JSplitPane();
    toolBar = new javax.swing.JToolBar();
    dbButton = new javax.swing.JButton();
    searchButton = new javax.swing.JButton();
    spreadButton = new javax.swing.JButton();
    spreadButton2 = new javax.swing.JButton();
    iconizeFrameButton = new javax.swing.JButton();
    expansionFrameButton = new javax.swing.JButton();
    closeFrameButton = new javax.swing.JButton();
    nowChartButton = new javax.swing.JButton();
    myChartButton = new javax.swing.JButton();
    menuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    registMenu = new javax.swing.JMenu();
    regNatalMenuItem = new javax.swing.JMenuItem();
    regEventMenuItem = new javax.swing.JMenuItem();
    chartMenu = new javax.swing.JMenu();
    natalChartMenuItem = new javax.swing.JMenuItem();
    eventMenuItem = new javax.swing.JMenuItem();
    chartAddMenu = new javax.swing.JMenu();
    addNatalMenuItem = new javax.swing.JMenuItem();
    addEventMenuItem = new javax.swing.JMenuItem();
    backupMenu = new javax.swing.JMenu();
    exportAllMenuItem = new javax.swing.JMenuItem();
    exportMenuItem = new javax.swing.JMenuItem();
    windowMenu = new javax.swing.JMenu();
    dbMenuItem = new javax.swing.JMenuItem();
    searchMenuItem = new javax.swing.JMenuItem();
    sabianDictMenuItem = new javax.swing.JMenuItem();
    calendarMenu = new javax.swing.JMenu();
    calendarMenuItem = new javax.swing.JMenuItem();
    enneagramMenuItem = new javax.swing.JMenuItem();
    newChartFrameMenuItem = new javax.swing.JMenuItem();
    jdayMenuItem = new javax.swing.JMenuItem();
    timeSliderMenuItem = new javax.swing.JMenuItem();
    moduleMenu = new javax.swing.JMenu();
    timeMenu = new javax.swing.JMenu();
    prefMenu = new javax.swing.JMenu();

    configMenuItem.setText("\u57fa\u672c\u8a2d\u5b9a");
    configMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configMenuItemActionPerformed(evt);
      }
    });

    desktopPane.setBackground(new java.awt.Color(102, 102, 102));
    bottomSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    bottomSplitPane.setOneTouchExpandable(true);
    dbSplitPane.setOneTouchExpandable(true);
    bottomSplitPane.setBottomComponent(dbSplitPane);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    dbButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/database_icon.png")));
    dbButton.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9");
    dbButton.setToolTipText("\u30c7\u30fc\u30bf\u9078\u629e\u3068\u7ba1\u7406 Ctrl-D");
    dbButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dbButtonActionPerformed(evt);
      }
    });

    toolBar.add(dbButton);

    searchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search_icon.gif")));
    searchButton.setText("\u691c\u7d22");
    searchButton.setToolTipText("\u30c7\u30fc\u30bf\u691c\u7d22 Ctrl-F");
    searchButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchButtonActionPerformed(evt);
      }
    });

    toolBar.add(searchButton);

    toolBar.addSeparator();
    spreadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/spread2_icon.gif")));
    spreadButton.setToolTipText("\u5de6\u53f3\u306b\u6574\u5217");
    spreadButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        spreadButtonActionPerformed(evt);
      }
    });

    toolBar.add(spreadButton);

    spreadButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/spread3_icon.gif")));
    spreadButton2.setToolTipText("\u4e09\u9762\u3067\u6574\u5217");
    spreadButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        spreadButton2ActionPerformed(evt);
      }
    });

    toolBar.add(spreadButton2);

    iconizeFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/iconize_icon.gif")));
    iconizeFrameButton.setToolTipText("\u3059\u3079\u3066\u30a2\u30a4\u30b3\u30f3\u5316");
    iconizeFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        iconizeFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(iconizeFrameButton);

    expansionFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/expantion_icon.gif")));
    expansionFrameButton.setToolTipText("\u3059\u3079\u3066\u3092\u5c55\u958b");
    expansionFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        expansionFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(expansionFrameButton);

    closeFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/close_icon.gif")));
    closeFrameButton.setToolTipText("\u3059\u3079\u3066\u30af\u30ed\u30fc\u30ba");
    closeFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(closeFrameButton);

    toolBar.addSeparator();
    nowChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/now_transit.gif")));
    nowChartButton.setToolTipText("\u4eca\u306e\u304a\u7a7a\u306e\u304a\u661f\u69d8");
    nowChartButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nowChartButtonActionPerformed(evt);
      }
    });

    toolBar.add(nowChartButton);

    myChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/natal.gif")));
    myChartButton.setToolTipText("\u30de\u30a4\u30c1\u30e3\u30fc\u30c8");
    myChartButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        myChartButtonActionPerformed(evt);
      }
    });

    toolBar.add(myChartButton);

    getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

    fileMenu.setMnemonic('F');
    fileMenu.setText("\u30d5\u30a1\u30a4\u30eb(F)");
    registMenu.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9\u306b\u767b\u9332");
    regNatalMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    regNatalMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c7\u30fc\u30bf\u306e\u767b\u9332");
    regNatalMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        regNatalMenuItemActionPerformed(evt);
      }
    });

    registMenu.add(regNatalMenuItem);

    regEventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c7\u30fc\u30bf\u306e\u767b\u9332");
    regEventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        regEventMenuItemActionPerformed(evt);
      }
    });

    registMenu.add(regEventMenuItem);

    fileMenu.add(registMenu);

    chartMenu.setText("\u767b\u9332\u305b\u305a\u306b\u30c1\u30e3\u30fc\u30c8\u4f5c\u6210");
    natalChartMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c1\u30e3\u30fc\u30c8");
    natalChartMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        natalChartMenuItemActionPerformed(evt);
      }
    });

    chartMenu.add(natalChartMenuItem);

    eventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c1\u30e3\u30fc\u30c8");
    eventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eventMenuItemActionPerformed(evt);
      }
    });

    chartMenu.add(eventMenuItem);

    fileMenu.add(chartMenu);

    chartAddMenu.setText("\u30c1\u30e3\u30fc\u30c8\u306b\u30c7\u30fc\u30bf\u8ffd\u52a0\u5165\u529b");
    chartAddMenu.setEnabled(false);
    addNatalMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c7\u30fc\u30bf\u8ffd\u52a0");
    addNatalMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addNatalMenuItemActionPerformed(evt);
      }
    });

    chartAddMenu.add(addNatalMenuItem);

    addEventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c7\u30fc\u30bf\u8ffd\u52a0");
    addEventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addEventMenuItemActionPerformed(evt);
      }
    });

    chartAddMenu.add(addEventMenuItem);

    fileMenu.add(chartAddMenu);

    backupMenu.setText("\u30d0\u30c3\u30af\u30a2\u30c3\u30d7");
    exportAllMenuItem.setText("\u5168\u30d0\u30fc\u30b9\u30c7\u30fc\u30bf\u3092\u30a8\u30af\u30b9\u30dd\u30fc\u30c8");
    exportAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportAllMenuItemActionPerformed(evt);
      }
    });

    backupMenu.add(exportAllMenuItem);

    exportMenuItem.setText("\u9078\u629e\u3057\u305f\u30d5\u30a9\u30eb\u30c0\u3092\u30a8\u30af\u30b9\u30dd\u30fc\u30c8");
    exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportMenuItemActionPerformed(evt);
      }
    });

    backupMenu.add(exportMenuItem);

    fileMenu.add(backupMenu);

    menuBar.add(fileMenu);

    windowMenu.setMnemonic('W');
    windowMenu.setText("\u30a6\u30a3\u30f3\u30c9\u30a6(W)");
    windowMenu.addMenuListener(new javax.swing.event.MenuListener() {
      public void menuCanceled(javax.swing.event.MenuEvent evt) {
      }
      public void menuDeselected(javax.swing.event.MenuEvent evt) {
      }
      public void menuSelected(javax.swing.event.MenuEvent evt) {
        windowMenuMenuSelected(evt);
      }
    });

    dbMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
    dbMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/database_icon.png")));
    dbMenuItem.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9");
    dbMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dbMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(dbMenuItem);

    searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
    searchMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search_icon.gif")));
    searchMenuItem.setText("\u691c\u7d22");
    searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(searchMenuItem);

    sabianDictMenuItem.setText("\u30b5\u30d3\u30a2\u30f3\u8f9e\u66f8");
    sabianDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sabianDictMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(sabianDictMenuItem);

    calendarMenu.setText("\u30ab\u30ec\u30f3\u30c0\u30fc");
    calendarMenuItem.setText("\u5929\u4f53\u30ab\u30ec\u30f3\u30c0\u30fc");
    calendarMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        calendarMenuItemActionPerformed(evt);
      }
    });

    calendarMenu.add(calendarMenuItem);

    enneagramMenuItem.setText("\u30a8\u30cb\u30a2\u30b0\u30e9\u30e0\u30ab\u30ec\u30f3\u30c0\u30fc");
    enneagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        enneagramMenuItemActionPerformed(evt);
      }
    });

    calendarMenu.add(enneagramMenuItem);

    windowMenu.add(calendarMenu);

    newChartFrameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    newChartFrameMenuItem.setText("\u30c1\u30e3\u30fc\u30c8\u30d5\u30ec\u30fc\u30e0\u3092\u8907\u88fd");
    newChartFrameMenuItem.setEnabled(false);
    newChartFrameMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newChartFrameMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(newChartFrameMenuItem);

    jdayMenuItem.setText("\u30e6\u30ea\u30a6\u30b9\u8a08\u7b97\u6a5f");
    jdayMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jdayMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(jdayMenuItem);

    timeSliderMenuItem.setText("\u6642\u9593\u5236\u5fa1");
    timeSliderMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        timeSliderMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(timeSliderMenuItem);

    menuBar.add(windowMenu);

    moduleMenu.setMnemonic('C');
    moduleMenu.setText("\u30c1\u30e3\u30fc\u30c8(C)");
    menuBar.add(moduleMenu);

    timeMenu.setMnemonic('T');
    timeMenu.setText("\u6642\u9593(T)");
    menuBar.add(timeMenu);

    prefMenu.setMnemonic('P');
    prefMenu.setText("\u8a2d\u5b9a(P)");
    menuBar.add(prefMenu);

    setJMenuBar(menuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void configMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configMenuItemActionPerformed
    String defTime = Config.data.getProperty("DefaultTime");
    if(MainConfigDialog.showDialog(MainFrame.this,Config.data)) {
      // ﾃﾞﾌｫﾙﾄﾀｲﾑが変更されたときはDBの天体位置の再計算を行う
      if(! defTime.equals(Config.data.getProperty("DefaultTime"))) {
        RecalculationDialog.showDialog(MainFrame.this);
      }
    }
  }//GEN-LAST:event_configMenuItemActionPerformed

  private void timeSliderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeSliderMenuItemActionPerformed
    if(isEmptyChartFrame()) return;
    timeSliderDialog.pack();
    timeSliderDialog.setVisible(true);
  }//GEN-LAST:event_timeSliderMenuItemActionPerformed

  private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
    TreePath path = editor.
      showFolderSelectDialog("エクスポートするフォルダを選択してください。");
    if(path != null) DataExporterDialog.export(this,path);
  }//GEN-LAST:event_exportMenuItemActionPerformed
//全バースデータをエクスポート
  private void exportAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAllMenuItemActionPerformed
    DataExporterDialog.exportAll(this);
  }//GEN-LAST:event_exportAllMenuItemActionPerformed

  private void jdayMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdayMenuItemActionPerformed
    jdayDialog.setVisible(true);
  }//GEN-LAST:event_jdayMenuItemActionPerformed

  private void sabianDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sabianDictMenuItemActionPerformed
    sabianDialog.setVisible(true);
  }//GEN-LAST:event_sabianDictMenuItemActionPerformed
// メインフレームのクローズボタンが押されたとき
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    editor.dispose();
    Database.getInstance().setProperties("Default",System.getProperties());
    Ephemeris.getSwissEph().swe_close();
  }//GEN-LAST:event_formWindowClosing
//自分のﾁｬｰﾄをすばやく表示   MyDataﾌﾟﾛﾊﾟﾃｨのIDをもつﾈｰﾀﾙﾁｬｰﾄを表示
  private void myChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myChartButtonActionPerformed
    //Properties prop = Database.getInstance().getProperties("Default");
    //String value = prop.getProperty("MyData");
    String value = Config.data.getProperty("MyData");
    if(value == null) return;
    int id = Integer.parseInt(value);
    List<Natal> list = new ArrayList<Natal>();
    Natal natal = Database.getInstance().getNatal(id);
    if(natal == null) return;
    list.add(natal);
    openChartFrame( list );
  }//GEN-LAST:event_myChartButtonActionPerformed
//現在のチャートを出す
  private void nowChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nowChartButtonActionPerformed
// TODO add your handling code here:
    //Natal natal = Natal.getDefault();
    Natal natal = new Natal();
    Place place = Config.data.getPlace("DefaultTransitPlace");
    natal.setPlace(place);
    natal.setCalendar(new GregorianCalendar(),TimePlace.DATE_AND_TIME);

    natal.setChartType(Natal.EVENT);
    natal.setName("現在");
    List<Natal> list = new ArrayList<Natal>();
    list.add(natal);
    openChartFrame( list );
  }//GEN-LAST:event_nowChartButtonActionPerformed
  
  private void enneagramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enneagramMenuItemActionPerformed
    EnneagramCalendar2.exec(false);
  }//GEN-LAST:event_enneagramMenuItemActionPerformed
  
  private void calendarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarMenuItemActionPerformed
    AstroCalendar.exec(false);
  }//GEN-LAST:event_calendarMenuItemActionPerformed
  //ウィンドゥ(W)が選択されたときのイベント処理
  private void windowMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_windowMenuMenuSelected
    //newChartFrameMenuItem.setEnabled(desktopPane.getSelectedFrame() != null);
  }//GEN-LAST:event_windowMenuMenuSelected
  //ウィンドウ(W) 時間制御パネル  //ウィンドウ(W) チャートフレームを複製
  private void newChartFrameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChartFrameMenuItemActionPerformed
    duplicateChartFrame();
    //java.awt.EventQueue.invokeLater(new ChartFrameDuplicator());
    //newChartFrameActionPerformed();
  }//GEN-LAST:event_newChartFrameMenuItemActionPerformed
  //ウィンドウ(W) 検索
  private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
    searchButton.doClick();
  }//GEN-LAST:event_searchMenuItemActionPerformed
  //ウィンドウ(W) データベース
  private void dbMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbMenuItemActionPerformed
    dbButton.doClick();
  }//GEN-LAST:event_dbMenuItemActionPerformed
  //ファイル(F)→チャートにデータ追加入力→イベントデータ追加
  private void addEventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventMenuItemActionPerformed
    addNatalToChart(Natal.EVENT);
  }//GEN-LAST:event_addEventMenuItemActionPerformed
  //ファイル(F) 登録せずチャート作成→イベントチャート
  private void eventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventMenuItemActionPerformed
    createChart(Natal.EVENT);
  }//GEN-LAST:event_eventMenuItemActionPerformed
  //ファイル(F)→チャートにデータ追加入力→ネイタルデータ追加
  private void addNatalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNatalMenuItemActionPerformed
    addNatalToChart(Natal.NATAL);
  }//GEN-LAST:event_addNatalMenuItemActionPerformed
  //ファイル(F) 登録せずチャート作成→ネイタルチャート
  private void natalChartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_natalChartMenuItemActionPerformed
    createChart(Natal.NATAL);
  }//GEN-LAST:event_natalChartMenuItemActionPerformed
  //ファイル(F)「イベントデータの登録」
  private void regEventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regEventMenuItemActionPerformed
    editor.registEvent();
  }//GEN-LAST:event_regEventMenuItemActionPerformed
  //ファイル(F)「ネイタルデータの登録」
  private void regNatalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regNatalMenuItemActionPerformed
    editor.registNatal();
  }//GEN-LAST:event_regNatalMenuItemActionPerformed
  //３面で整列
  private void spreadButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spreadButton2ActionPerformed
    frame0 = desktopPane.getSelectedFrame();
    if(frame0 == null) return; // フレームが非選択状態なら終わり
    Rectangle rect = desktopPane.getBounds();
    int w = (int)((float)rect.width * 0.618f);
    int w2 = rect.width - w;
    int h = rect.height;
    int h2 = rect.height / 2;
    List<ChartInternalFrame> frameList = new ArrayList<ChartInternalFrame>();
    //アイコン化されているフレーム以外のフレームをリストアップ
    //List<ChartInternalFrame> cfList = getChartInternalFrames();
    for(ChartInternalFrame cif : getChartFrames()) {
      if(! cif.isIcon()) frameList.add(cif);
    }
    if(count >= frameList.size()) count = 0;
    //初回のときは選択されているフレームをメイン(大きなサイズ)の位置にもってくる。
    if(frameList.size() >= 2) {
      for(int i=0; i<count; i++) {
        ChartInternalFrame fr = frameList.get(0);
        frameList.remove(0);
        frameList.add(fr);
      }
    }
    frameList.get(0).reshape(w2,0,w,h); //一枚目をスプレッド(左半分)
    frameList.get(0).toFront();
    if(frameList.size() == 1) return;   //二枚目が無いなら終わる
    frameList.get(1).reshape(0,0,w2,h2); //二枚目をスプレッド(左上)
    frameList.get(1).toFront();
    if(frameList.size() == 2) { count++; return; } //三枚目が無いなら終わる
    frameList.get(2).reshape(0,h2,w2,h2); //三枚目をスプレッド(左下)
    frameList.get(2).toFront();
    count++;
  }//GEN-LAST:event_spreadButton2ActionPerformed
  //内部フレームをすべてクローズ
  private void closeFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeFrameButtonActionPerformed
//    this.showTimeController(false);
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
//      ((ChartInternalFrame)frames[i]).doDefaultCloseAction();
      try {
        ((ChartInternalFrame)frames[i]).setClosed(true);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_closeFrameButtonActionPerformed
  //内部フレームをすべて非アイコン化
  private void expansionFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expansionFrameButtonActionPerformed
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
      try {
        frames[i].setIcon(false);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_expansionFrameButtonActionPerformed
  //内部フレームをすべてアイコン化
  private void iconizeFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconizeFrameButtonActionPerformed
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
      try {
        frames[i].setIcon(true);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_iconizeFrameButtonActionPerformed
  //２面で整列
  private void spreadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spreadButtonActionPerformed
    //アイコン化されているものをみつけたら解除する
//    for(JInternalFrame f : iFrames) {
//      if(f.isIcon()) {
//        JInternalFrame frame = desktopPane.getSelectedFrame();
//        expansionFrameButtonActionPerformed(null);
//        try {
//          if(frame != null) frame.setSelected(true);
//        } catch (PropertyVetoException ex) {
//          ex.printStackTrace();
//        }
//        System.out.println("アイコン処理した");
//        break;
//      }
//    }
    frame0 = getSelectedFrame(); //desktopPane.getSelectedFrame();
    if(frame0 == null) return; // フレームが非選択状態なら終わり
    Rectangle rect = desktopPane.getBounds();
    int w = rect.width / 2;
    int h = rect.height;
    frame0.reshape(0,0,w,h);
    frame0.toFront(); //一枚目をスプレッド
    ChartInternalFrame [] cFrames = getChartFrames();
    if(cFrames.length == 1) return;//一枚しかないなら終わる
    List<ChartInternalFrame> frameList = new ArrayList<ChartInternalFrame>();
    // 一枚目のフレームとアイコン化されているフレーム以外のフレームをリストアップ
    for(ChartInternalFrame cif: cFrames) {
      if(cif != frame0 && (! cif.isIcon()))
        frameList.add(cif);
    }
    if(count >= frameList.size()) count = 0;
    ChartInternalFrame frame1 = frameList.get(count);
    frame1.reshape(w,0,w,h);
    frame1.toFront();
    count++;
  }//GEN-LAST:event_spreadButtonActionPerformed
  //ツールバーの「検索」ボタンの処理
  private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    if(System.getProperty("SeparateMode","false").equals("true")) {
      editor.setExtendedState(Frame.NORMAL);
      editor.setVisible(true);
    } else {
      bottomSplitPane.setDividerLocation(0.7);
    }
    editor.showSearchFrame();
  }//GEN-LAST:event_searchButtonActionPerformed
  //ツールバーの「データベース」ボタンの処理
  private void dbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbButtonActionPerformed
    if(System.getProperty("SeparateMode","false").equals("true")) {
      editor.setExtendedState(Frame.NORMAL);
      editor.setVisible(true);
      searchButton.setEnabled(true);
    } else {
      if(devider == 0) {
        bottomSplitPane.setDividerLocation(0.7);
        devider = bottomSplitPane.getDividerLocation();
      }
      if(devider == bottomSplitPane.getDividerLocation())
        bottomSplitPane.setDividerLocation(1.0);
      else
        bottomSplitPane.setDividerLocation(0.7);
    }
  }//GEN-LAST:event_dbButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem addEventMenuItem;
  private javax.swing.JMenuItem addNatalMenuItem;
  private javax.swing.JMenu backupMenu;
  private javax.swing.JSplitPane bottomSplitPane;
  private javax.swing.JMenu calendarMenu;
  private javax.swing.JMenuItem calendarMenuItem;
  private javax.swing.JMenu chartAddMenu;
  private javax.swing.JMenu chartMenu;
  private javax.swing.JButton closeFrameButton;
  private javax.swing.JMenuItem configMenuItem;
  private javax.swing.JButton dbButton;
  private javax.swing.JMenuItem dbMenuItem;
  private javax.swing.JSplitPane dbSplitPane;
  private javax.swing.JDesktopPane desktopPane;
  private javax.swing.JMenuItem enneagramMenuItem;
  private javax.swing.JMenuItem eventMenuItem;
  private javax.swing.JButton expansionFrameButton;
  private javax.swing.JMenuItem exportAllMenuItem;
  private javax.swing.JMenuItem exportMenuItem;
  private javax.swing.JMenu fileMenu;
  private javax.swing.JButton iconizeFrameButton;
  private javax.swing.JMenuItem jdayMenuItem;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenu moduleMenu;
  private javax.swing.JButton myChartButton;
  private javax.swing.JMenuItem natalChartMenuItem;
  private javax.swing.JMenuItem newChartFrameMenuItem;
  private javax.swing.JButton nowChartButton;
  private javax.swing.JMenu prefMenu;
  private javax.swing.JMenuItem regEventMenuItem;
  private javax.swing.JMenuItem regNatalMenuItem;
  private javax.swing.JMenu registMenu;
  private javax.swing.JMenuItem sabianDictMenuItem;
  private javax.swing.JButton searchButton;
  private javax.swing.JMenuItem searchMenuItem;
  private javax.swing.JButton spreadButton;
  private javax.swing.JButton spreadButton2;
  private javax.swing.JMenu timeMenu;
  private javax.swing.JMenuItem timeSliderMenuItem;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JMenu windowMenu;
  // End of variables declaration//GEN-END:variables
  
}
