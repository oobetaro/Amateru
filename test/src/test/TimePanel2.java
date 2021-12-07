/*
 * TimePanel2.java
 *
 * Created on 2006/11/28, 0:45
 */

package to.tetramorph.starbase;

import java.awt.GridLayout;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.lib.TransitTabReceiver;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * TimePanelはデータベースとチャートフレームを中継する。
 * DBやこのパネルの入力フォームから入力された日時場所データを、チャートモジュール
 * に引き渡す。与えられたデータに対して、時間を変更したり自動インクリメント/
 * デクリメントができる。また選択中のデータのヒストリー情報をJMenuで提供する。
 * チャートモジュールへのデータの引き渡しはChannelDataクラスで行われる。
 * <pre>
 * 選択データのヒストリーの選択メニューを取得
 * 　　JMenu getHistoryMenu() 
 * チャートモジュールのセット
 * 　　ChartModulePanel getModule() 
 * 　　void setModule(ChartModulePanel module) 
 * 出生・経過データの登録・追加・取得
 * 　　void setNatal(List<Natal> list) 
 * 　　void addNatal(List<Natal> list) 
 * 　　void setTransit(Transit transit) 
 * 　　Transit getTransit() 
 * 　　ChartData getSelectedChartData() 
 * 　　void set() 
 * タイマー制御
 * 　　void stopTimer() 
 * 　　void stop() 
 * 複製用
 * 　　void dataCopy(TimePanel2 tp) 
 * </pre>
 */
class TimePanel2 extends TimePanel {
//  implements TimeController,TransitTabReceiver,java.io.Serializable  {

  ChartInternalFrame iframe;
  ChartModulePanel module; //set()が参照
  ChannelData channelData = new ChannelData(this); //ChartModuleに引き渡すﾁｬﾝﾈﾙﾃﾞｰﾀ
  JMenu historyMenu = null;
  Transit transit = new Transit(); //トランジットタブの値
  //JComponent toolComponent;
  TimeSliderPanel timeSliderPanel;
  /** 
   * GUIエディタのためにあるコンストラクタでこれは使用しないこと。
   */
  public TimePanel2() {
    initComponents();
    timeSliderPanel = new TimeSliderPanel();
    timeSliderPanel.setTimeSliderListener(new TimeSliderHandler());
    transitTimePlacePanel.setDefault();
    transitTimePlacePanel.getTimePlace(transit); //参照でtransitに書き込み
    channelData.setTransit(transit);
    //toolBar.setLayout(new GridLayout(0,1));
    //toolComponent = voidLabel;
  }
  /**
   * オブジェクトを作成。ChartInternalFrameから呼び出される。
   * @param iframe 呼出もとのチャートフレームの参照アドレス
   */
  public TimePanel2(ChartInternalFrame iframe) {
    this();
    this.iframe = iframe;
  }
  /** タイムスライダーパネルを返す */
  public TimeSliderPanel getTimeSliderPanel() {
    return timeSliderPanel;
  }
  /**
   * 指定されたIDのNatalデータがこのパネルにセットされている場合はtrueを返す。
   */
  public boolean isComprise(int id) {
    for(int i=0; i<channelData.size(); i++) {
      //ChartDataPanelのisCompriseを呼び出している。
      if(channelData.get(i).isComprise(id)) return true;
    }
    return false;
  }
  /**
   * このTimePanelにセットされているチャートモジュールを返す。
   */
  public ChartModulePanel getModule() {
    return module;
  }
  /**
   * チャートモジュールをセットする。TimePanelへのデータの入口はsetNatal,addNatal
   * だが、ここから入ってきたデータはタブの選択されているフォーム(ChartDataPanelや
   * トランシットのフォームに振り分けてセットする。そしてそれをChannelDataオブジ
   * ェクトとして、チャートモジュールに送る。
   */
  public void setModule(ChartModulePanel module) {
    this.module = module;
    tabbedPane.removeAll();
    while( module.getChannelSize() > channelData.size())
      channelData.add(new ChartDataPanel(this,iframe));
    for(int i=0; i<module.getChannelSize(); i++) {
      tabbedPane.add((ChartDataPanel)channelData.get(i));
      tabbedPane.setTitleAt(i,module.getChannelNames()[i]);
    }
    mainTabbedPane.setEnabledAt(1,module.isNeedTransit());
    revalidate();
    repaint();
  }

  // Spinnerの各桁の値をカレンダーに足し、日付と時間のﾌｨｰﾙﾄﾞに値をｾｯﾄする
  private void addCalendar(int sign) {
    int second = timeSliderPanel.getSecond() * sign;
    int minute = timeSliderPanel.getMinute() * sign;
    int hour = timeSliderPanel.getHour() * sign;
    int day = timeSliderPanel.getDay() * sign;    
    if(mainTabbedPane.getSelectedIndex() == 0) { //ネイタルタブが選択
      ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
      cdp.addCalendar(day,hour,minute,second);
      //set()はcdp側でキックされる
    } else {
      transitTimePlacePanel.addCalendar(day,hour,minute,second);
      transitSetButtonActionPerformed(null);
    }
  }
  
//内部クラスにするのは、これらのメソッドをJavaDocに出したくないから
  class TimeSliderHandler implements TimeSliderListener {
     //カレンダーに時間を足す
    public void increment() {
      addCalendar(1);
    }
    //カレンダーから時間を引く
    public void decrement() {
      addCalendar(-1);
    }

        public void store() {
        }
  }
  /**
   * 現在選択されているチャートモジュールのsetData(channelData)を、
   * イベントキューを使って呼び出す。
   */
  public void set() {
    assert SwingUtilities.isEventDispatchThread();
    assert module != null : "モジュールがnull";
    module.setData(channelData);
  }

  /**
   * これはｵｰﾄｲﾝｸﾘﾒﾝﾄを停止させるときのストップメソッド。内部タイマーは動作している。
   * 名前が悪いので変更したほうが良い。
   */  
  public void stopTimer() {
    timeSliderPanel.stopTimer();
  }
  
  /**
   * 内部フレームをクローズする際このメソッドを呼び出し、
   * このオブジェクトの中で動いているアニメーション用タイマーを停止させること。
   * これも名前がわるいので変更したほうがよい。dispose()が正しい。
   */  
  public void stop() {
    timeSliderPanel.stop();
  }
  
  /**
   * このパネルにNatalのリストを追加する。このメソッドはこのオブジェクトの
   * 第一の入口。IDで検査してdataListに重複するデータがある場合は追加しない。
   * listでうけとった日付情報はこのオブジェクト内で加工されmoduleに与えられる。
   */
  public void setNatal(List<Natal> list) {
    if(mainTabbedPane.getSelectedIndex() == 1) {
      setTransit(list.get(0));
    } else 
      ((ChartDataPanel)tabbedPane.getSelectedComponent()).setNatal(list);
  }
  
  /**
   * このパネルにNatalのリストを追加する。このメソッドはこのオブジェクトの
   * 第二の入口。IDで検査してdataListに重複するデータがある場合は追加しない。
   * listでうけとった日付情報はこのオブジェクト内で加工されmoduleに与えられる。
   */    
  public void addNatal(List<Natal> list) {
    if(mainTabbedPane.getSelectedIndex() == 1) {
      setTransit(list.get(0));
    } else
      ((ChartDataPanel)tabbedPane.getSelectedComponent()).addNatal(list);
  }
  
  /**
   * ヒストリーメニューを返す。トランシットが無効なモジュールの場合はDisenable
   * にして返す。
   */
  public JMenu getHistoryMenu() {
    ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
    JMenu menu = cdp.getHistoryMenu();
    //ﾄﾗﾝｼｯﾄ不要のﾓｼﾞｭｰﾙはDisenabledにする。必要でﾒﾆｭｰがあるときはenabledに。
    if(module.isNeedTransit() && menu.getMenuComponentCount() > 0)
      menu.setEnabled(true);
    else 
      menu.setEnabled(false);
    return menu;
  }
  /**
   * このパネルのトランシットタブの日時を返す。
   */
  public Transit getTransit() {
    transitTimePlacePanel.getTimePlace(transit);
    transit.setName(nameTextField.getText().trim());
    transit.setMemo(memoTextField.getText().trim());
    return transit;
  }
  /**
   * トランシットタブに値をセットする。
   */
  public void setTransit(Transit transit) {
    this.transit = transit;
    mainTabbedPane.setSelectedIndex(1);
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
    if(transit.getTime() == null)
      transitTimePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    transitSetButtonActionPerformed(null);
  }
  /**
   * トランシットタブの値をセットする。これはChannelDataオブジェクトにこの
   * オブジェクト(TimePanel2)を引き渡す際、TransitTabReceiverとして渡すためにある。
   * ChannelData#updateTransit()によって、このパネルにチャートモジュール側で
   * 変更されたTransitオブジェクトを受け取る。
   * このクラスのsetTransit()と違うところは、時刻未設定の場合のデフォルトタイム
   * を入れる処理をしていないのと、チャートモジュールに対してのイベントを発生
   * させないこと。
   */
  public void updateTransit(Transit transit) {
    this.transit = transit;
    mainTabbedPane.setSelectedIndex(1);
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
  }
  /**
   * ネイタルタブの中にあるチャンネルタブの中で選択されているチャートデータを返す。
   * 未選択状態ならnullが返る。
   */
  public ChartData getSelectedChartData() {
    return (ChartData)tabbedPane.getSelectedComponent();
  }
  /**
   * 引数で与えられたTimePanelに格納されているトランシットやネイタルのデータを
   * デープコピーで複製してこのパネルにセットする。これはチャートフレームの複製の
   * 際に使用される。
   */
  public void dataCopy(TimePanel tp) {
    ChannelData srcChannel = tp.channelData;
    //ﾄﾗﾝｼｯﾄを複製
    transit = new Transit(tp.getTransit());
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
    if(transit.getTime() == null)
      transit.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    transitTimePlacePanel.setTimePlace(transit);
    channelData.setTransit(transit);
    //ﾁｬﾝﾈﾙ数分、ChartDataPanelを複製
    for(int i=0; i<srcChannel.size(); i++) {
      ChartDataPanel chartData = new ChartDataPanel(this,iframe);
      //chartData.setTransit(new Transit(srcChannel.get(i).getTransit()));
      List<Data> temp = srcChannel.get(i).getDataList();
      List<Data> dataList = new ArrayList<Data>();
      for(Data d : temp) dataList.add(new Data(d));
      chartData.initDataList(dataList);
      Natal composit = srcChannel.get(i).getComposit();
      if(composit != null) chartData.compositNatal = new Natal(composit);
      channelData.add(chartData);
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel transitPanel;

    mainPanel = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    mainTabbedPane = new javax.swing.JTabbedPane();
    tabbedPane = new javax.swing.JTabbedPane();
    transitPanel = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    nameTextField = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    memoTextField = new javax.swing.JTextField();
    transitTimePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
    jPanel4 = new javax.swing.JPanel();
    transitSetButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    mainPanel.setLayout(new java.awt.BorderLayout());

    jPanel1.setLayout(new java.awt.GridBagLayout());

    mainTabbedPane.addTab("\u30cd\u30fc\u30bf\u30eb", tabbedPane);

    transitPanel.setLayout(new java.awt.BorderLayout());

    transitPanel.setFocusable(false);
    jPanel2.setLayout(new java.awt.GridBagLayout());

    jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 18));
    jLabel1.setText("\u540d\u524d");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
    jPanel2.add(jLabel1, gridBagConstraints);

    nameTextField.setPreferredSize(new java.awt.Dimension(116, 19));
    jPanel2.add(nameTextField, new java.awt.GridBagConstraints());

    jLabel2.setText("\u30e1\u30e2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
    jPanel2.add(jLabel2, gridBagConstraints);

    memoTextField.setPreferredSize(new java.awt.Dimension(116, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(memoTextField, gridBagConstraints);

    transitPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

    transitPanel.add(transitTimePlacePanel, java.awt.BorderLayout.CENTER);

    jPanel4.setFocusable(false);
    transitSetButton.setMnemonic('S');
    transitSetButton.setText("Set");
    transitSetButton.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    transitSetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        transitSetButtonActionPerformed(evt);
      }
    });

    jPanel4.add(transitSetButton);

    transitPanel.add(jPanel4, java.awt.BorderLayout.SOUTH);

    mainTabbedPane.addTab("\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8", transitPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanel1.add(mainTabbedPane, gridBagConstraints);

    mainPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

    add(mainPanel, java.awt.BorderLayout.NORTH);

  }// </editor-fold>//GEN-END:initComponents

  private void transitSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitSetButtonActionPerformed
    transitTimePlacePanel.getTimePlace(transit);
    transit.setName(nameTextField.getText().trim());
    transit.setMemo(memoTextField.getText().trim());
    channelData.setTransit(transit);
    set();
  }//GEN-LAST:event_transitSetButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mainPanel;
  private javax.swing.JTabbedPane mainTabbedPane;
  private javax.swing.JTextField memoTextField;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JButton transitSetButton;
  private to.tetramorph.starbase.TimePlacePanel transitTimePlacePanel;
  // End of variables declaration//GEN-END:variables
  
}
