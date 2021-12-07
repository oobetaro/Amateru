/*
 * ChartDataPanel.java
 *
 * Created on 2006/11/28, 1:03
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;

/**
 * 直入力機能を削除するまえのバージョン。使用していない。
 * データベースにはNatalのデータが多数登録されていて、そのデータを一つまたは複数で
 * リストにしてコンボボックスに入れて、任意のNatalデータを選択したり編集したりでき
 * るようにするパネル。
 * データベースからNatalオブジェクトのデータがsetNatal()/addNatal()で、このパネルに
 * 入れられる。addNatalはこのパネル内のリストに追加される。
 * 
 * Natalのデータはそのままリスト化されるのではなく、Dataオブジェクトにラップされて
 * リストにして管理される。DataオブジェクトはNatalデータと、そのデータと同じ値をもつ
 * TimePlaceデータのインスタンスを作成して、二重に日時と場所を保持し、ユーザーが
 * 日時や場所を変更するときは複製したTimePlaceを変更する仕組みになっている。こうする
 * ことで、簡単に元の日時に戻すことができる機能を提供する。
 * 
 * Natalデータには、個人のデータと、複数人のデータ集合からなるコンポジットデータに
 * 分ける事ができるが、ChartDataPanelにsetNatal()/addNatal()されるとき、コンポジット
 * データの中に登録されている複数の個人データ(Transitオブジェクト)は、それぞれ一人
 * づつに分解されて、Dataオブジェクトでラップされる。
 * つまりDataオブジェクトは、TransitもNatalもどちらのオブジェクトもラップ可能。
 * 
 * コンポジットデータが登録されているときは、isComposit()でそれを判定できる。
 * ChartDataPanelはコンポジットデータと個人データが混在することは認めない。
 * それは最初にsetNatalしたときのデータが個人データだったかコンポジットだったかで
 * 決まる。
 * コンポジット用となったChartDataPanelに、個人データをaddNatal()すると、それは
 * コンポジットのリストに新たに一人加えたことになる。
 * 個人データ用となったChartDataPanelに、個人データをaddNatal()すると、個人データ
 * が一人増えた事になる。一見おなじだがデータを保存する場合に変わってくる。
 * @author 大澤義鷹
 */
class ChartDataPanel_1 extends javax.swing.JPanel 
  implements ChartData,java.io.Serializable {
  Natal compositNatal = null;
  boolean eventStop = false;
  List<Data>dataList; //出生データの保管用
  Transit transit = new Transit(); // 直入力データの保管用
  ChartInternalFrame iframe;
  TimeController tcont; // set()を呼び出しデータをChartModulePanelに送る。
  JMenu historyMenu = new JMenu("ヒストリー(H)");
  List<Transit> historyList = null;
  TimePlacePanel transitTimePlacePanel;
  /** 
   * オブジェクトを作成する。GUIエディタ用に用意されてるだけでこのコンストラクタ
   * は使用しないこと。
   */
  public ChartDataPanel_1() {
    initComponents();
    init();
  }
  private void init() {
    dataList = new ArrayList<Data>();
    timePlacePanel2.setDefault();
    timePlacePanel2.getTimePlace(transit);
    tabbedPane.setEnabledAt(0,false); //初期化時はデータタブは使用不可。
    tabbedPane.setSelectedIndex(1); //直入力タブのみ使用できる。
  }  
  /**
   * オブジェクトを作成する。内部のList<Data>を初期化、直入力タブには現在時刻
   * とデフォルトの観測地をセットしこれを選択状態に。データタブはDisenabled。
   * setNatalなどのメソッドでデータがセットされるとデータタブはEnabledになる。
   * @param tcont TimeControllerを実装したオブジェクト
   * @param iframe ChartInternalFrameオブジェクト
   */
  public ChartDataPanel_1(TimeController tcont,ChartInternalFrame iframe) {
    initComponents();
    init();
    this.iframe = iframe;
    this.tcont = tcont;
  }
  /**
   * Natalのリストをセットする。
   */
  public void setNatal(List<Natal> list) {
    if(list.size() > 0) {
      tabbedPane.setSelectedIndex(0);
      tabbedPane.setEnabledAt(0,true);
    }
    eventStop = true;
    // ｺﾝﾎﾟｼﾞｯﾄが与えられた時は保管しておく
    compositNatal = list.get(0).getChartType().equals(Natal.COMPOSIT) ? list.get(0) : null;
    nameComboBox.removeAllItems();
    dataList.clear();
    for(int i=0; i < list.size(); i++) {
      Data data = null;
      if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
        //コンポジットはほぐしてList<Data>にラップ。
        List<Transit> compositList = list.get(i).getComposit();
        for(int j=0; j<compositList.size(); j++) {
          data = new Data(compositList.get(j));
          nameComboBox.addItem(data.toString());
          dataList.add(data);
        }
      } else { //普通のデータはList<Data>にラップする。
        data = new Data(list.get(i));
        nameComboBox.addItem(data.toString());
        dataList.add(data);
      }
    }
    nameComboBox.setSelectedIndex(0);
    timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
    eventStop = false;
    nameComboBoxActionPerformed(null);
  }
  /**
   * Natalのリストを追加する。
   */
  public void addNatal(List<Natal> list) {
    if(list.size() > 0) {
      tabbedPane.setSelectedIndex(0);
      tabbedPane.setEnabledAt(0,true);
    }
    eventStop = true;
    SKIP:
    for(int i=0; i < list.size(); i++) {
      if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
        //コンポジットなら無条件に追加。重複して追加しても気にしない。
        List<Transit> compList = list.get(i).getComposit();
        for(Transit evt : compList) {
          Data data  = new Data(evt);
          nameComboBox.addItem(data.toString());
          dataList.add(data);
        }
      } else {
        int id = list.get(i).getId();
        //IDで検査してdataListに重複するデータがある場合は追加しない。ただしNEED_REGISTデータは除く
        if(id != Natal.NEED_REGIST) {
          for(int j=0; j < dataList.size(); j++) {
            if(dataList.get(j).getNatal().getId() == id) continue SKIP;
          }
        }
        Data data = new Data(list.get(i));
        nameComboBox.addItem(data.toString());
        dataList.add(data);
      }
    }
    //送られてきた最後のものをセレクト
    nameComboBox.setSelectedIndex(nameComboBox.getItemCount()-1);
    //timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
    eventStop = false;
    nameComboBoxActionPerformed(null);    
  }
  /**
   * 現在フォーカスがあるタブの日時フォームに時間を加算する。
   * 値に負数を指定すると減算も行われる。
   */
  public void addCalendar(int day,int hour,int minute,int second) {
    if(tabbedPane.getSelectedIndex() == 0) { //データ用
      timePlacePanel.addCalendar(day,hour,minute,second);
      setButtonActionPerformed(null);
    } else {
      timePlacePanel2.addCalendar(day,hour,minute,second);
      setButton2ActionPerformed(null);
    }
  }
  /**
   * コンボボックスに登録されているDataのリストを返す。
   */
  public List<Data> getDataList() {
    return dataList;
  }
  /**
   * 引数で指定されたDataのリストをこのパネルにセットする。コンボボックスや
   * 入力フォームにも値が入る。
   */
  public void setDataList(List<Data> dataList) {
    this.dataList = dataList;
    tabbedPane.setSelectedIndex(0);
    tabbedPane.setEnabledAt(0,true);
    eventStop = true;  
    nameComboBox.removeAllItems();
    for(Data d : dataList)
      nameComboBox.addItem(d.toString());
    nameComboBox.setSelectedIndex(0);
    eventStop = false;
    nameComboBoxActionPerformed(null);
    //timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
  }
  /** 
   * チャートフレームの複製の際にDataのリストをこのパネルにセットする。 
   * setDataList()とほぼ同じだがTimePanel.set()を呼び出さない。
   * ChartInternalFrame複製中、まだ各チャートモジュールのセットアップが済む前に
   * setDataList()を使うと、set()が呼び出されNullPorinterExceptionが出てしまう。
   * それを回避するためにこのメソッドがある。監視
   */
  public void initDataList(List<Data> dataList) {
    this.dataList = dataList;
    tabbedPane.setSelectedIndex(0);
    tabbedPane.setEnabledAt(0,true);
    eventStop = true;  
    nameComboBox.removeAllItems();
    for(Data d : dataList)
      nameComboBox.addItem(d.toString());
    nameComboBox.setSelectedIndex(0);
    Data data = dataList.get(0);
    timePlacePanel.setTimePlace(data.getTimePlace());
    if(data.getNatal().getTime() == null)
      timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    createHistoryMenu();
    eventStop = false;
  }
  /**
   * コンボボックスのデータの何番目が選択されているか返す。
   * 選択されていない場合は負数を返す。(たぶん-1)
   */
  public int getSelectedIndex() {
    if(tabbedPane.getSelectedIndex() == 0)
      return nameComboBox.getSelectedIndex();
    else
      return -1;
  }
  /**
   * 現在選択中のNatalデータをDataオブジェクトで返す。
   * 直入力タブにフォーカスがあったり、データが未設定ならnullを返す。
   */
  public Data getSelectedData() {
    if(getSelectedIndex() >= 0) return dataList.get(getSelectedIndex());
    return null;
  }
  /**
   * 直接入力フォームの日時場所のデータを返す。
   */
  public Transit getTransit() {
    return transit;
  }
  /**
   * 直接入力フォームに日時場所をセット
   */
  public void setTransit(Transit transit) {
    this.transit = transit;
    timePlacePanel2.setTimePlace(transit);
  }
  /**
   * このチャートデータパネルに登録されているデータが、コンポジットデータなら
   * trueを返す。
   */
  public boolean isComposit() {
    return compositNatal != null;
  }
  /**
   * このパネルにコンポジットデータがセットされている場合はそのデータを返す。
   * セットされたことがないならnullを返す。
   */
  public Natal getComposit() {
    return compositNatal;
  }
  /**
   * 指定されたIDを持つNatalデータがこのパネルに入っているときはtrueを返す。
   */
  public boolean isComprise(int id) {
    if(isComposit()) return compositNatal.getId() == id;
    for(int i=0; i<dataList.size(); i++) {
      if(dataList.get(i).getNatal().getId() == id) return true;
    }
    return false;
  }
  /**
   * このChartDataにふさわしいアイコン(男女の顔や時計やコンポジットのシンボル)を
   * 内部フレームにセットする。
   */
  public void setFrameIcon() {
    if(! isComposit()) {
      Data data = getSelectedData();
      if(data != null) {
        iframe.setFrameIcon(data.getNatal());
        iframe.setTitle(data.getNatal().getName());
      }
    } else {
      iframe.setFrameIcon(getComposit());
      iframe.setTitle(getComposit().getName());
      System.out.println("コンポジットアイコンセット " + getComposit().getName());
    }
  }
  /**
   * 「時間メニュー→ヒストリー」で表示されるヒストリー一覧のメニューを作成する。
   * このパネルのコンボボックスでデータの選択がされたときと、
   * 「時間メニュー→トランシットをヒストリーに追加」から呼び出される。これは
   * メニュー作成のトリガにすぎない。作成したメニューの取得はgetHistoryMenu()。
   */
  public void createHistoryMenu() {
    // selected()から呼び出される。ヒストリーが追加されたときChartInternalFrameからも。
    historyList = new ArrayList<Transit>();
    historyMenu.removeAll(); //参照アドレスを変更したくないのでremoveで対処。
    List<Transit> list = null;
    if(isComposit())
      list = compositNatal.getHistory();
    else {
      int num = nameComboBox.getSelectedIndex();
      list = dataList.get(num).getNatal().getHistory();
    }
    if(list == null) historyMenu.setEnabled(false);
    else {
      for(int i=0,j=0; i<list.size(); i++) {
        Transit evt = list.get(i);
        if(! evt.getName().equals("TRANSIT_PLACE")) { //経過地情報は除外
          historyList.add(evt);
          String title = String.format("%s    %s",evt.getDate().toString(),evt.getName());
          JMenuItem item = new JMenuItem(title);
          item.setActionCommand(""+j); //アイテムにシリアル番号を与える
          j++;
          historyMenu.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              setHistoryActionPerformed(evt);
            }
          });
        }
      }
      historyMenu.setMnemonic(KeyEvent.VK_H);
    }
    if(historyMenu.getItemCount()==0) historyMenu.setEnabled(false);
  }
  /**
   * このChartDataPanelに登録されていて、現在選択されているデータ(コンポジット
   * データの場合は選択は関係なし)のヒストリーのメニューを返す。
   */
  public JMenu getHistoryMenu() {
    return historyMenu;
  }
  /**
   * 現在選択されているDataに、引数で指定されたnatalデータをセットしフォーム
   * にもその内容を反映させる。
   * 時間メニューの「現在のデータを編集」から呼び出される。
   */
  public void replaceNatal(Natal natal) {
    int num = nameComboBox.getSelectedIndex();
    Data data = getSelectedData();
    data.setNatal(natal);
    dataList.set(num,data);
    nameComboBoxActionPerformed(null);
  }
//  //メニューで選択されたヒストリーをトランシットにセットする
  private void setHistoryActionPerformed(ActionEvent evt) {
    JMenuItem item = (JMenuItem)evt.getSource();
    int n = Integer.parseInt(item.getActionCommand());
    Transit event = historyList.get(n);
    tcont.setTransit(event);
  }
  /**
   * チャートモジュール側で日時の更新が発生したとき、このメソッドを呼ぶ事で、
   * ChartDataPanel(日時入力フォーム)に、更新された日時を反映させる。
   * @param data TimePlaceを更新したDateオブジェクト
   * @exception IllegalArgumentException データリストに存在しないDataオブジェクト
   * が指定されたとき。
   */
  public void updateData(Data data) {
    if( dataList.indexOf(data) < 0 ) throw 
      new IllegalArgumentException("データリストに存在しないDataオブジェクトです");
    TimePlace tp = data.getTimePlace();
    timePlacePanel.setTimePlace(tp);
    //if(tp.getTime() == null)
    //  timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
 }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel dataSelectPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel transitPanel;

    tabbedPane = new javax.swing.JTabbedPane();
    dataSelectPanel = new javax.swing.JPanel();
    timePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
    jPanel2 = new javax.swing.JPanel();
    resetButton = new javax.swing.JButton();
    setButton = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    nameComboBox = new javax.swing.JComboBox();
    delButton = new javax.swing.JButton();
    transitPanel = new javax.swing.JPanel();
    timePlacePanel2 = new to.tetramorph.starbase.TimePlacePanel();
    jPanel4 = new javax.swing.JPanel();
    setButton2 = new javax.swing.JButton();

    setLayout(new java.awt.GridLayout(1, 0));

    dataSelectPanel.setLayout(new java.awt.GridBagLayout());

    dataSelectPanel.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    dataSelectPanel.add(timePlacePanel, gridBagConstraints);

    jPanel2.setFocusable(false);
    resetButton.setMnemonic('R');
    resetButton.setText("Reset");
    resetButton.setToolTipText("\u6700\u521d\u306e\u30c7\u30fc\u30bf\u306b\u623b\u3059");
    resetButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    jPanel2.add(resetButton);

    setButton.setMnemonic('S');
    setButton.setText("Set");
    setButton.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    setButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    setButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setButtonActionPerformed(evt);
      }
    });

    jPanel2.add(setButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    dataSelectPanel.add(jPanel2, gridBagConstraints);

    jPanel3.setLayout(new java.awt.GridBagLayout());

    jPanel3.setFocusable(false);
    jLabel2.setText("\u5bfe\u8c61");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 4);
    jPanel3.add(jLabel2, gridBagConstraints);

    nameComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        nameComboBoxItemStateChanged(evt);
      }
    });
    nameComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nameComboBoxActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    jPanel3.add(nameComboBox, gridBagConstraints);

    delButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/delete.png")));
    delButton.setToolTipText("\u9078\u629e\u4e2d\u306e\u30c7\u30fc\u30bf\u3092\u9664\u5916");
    delButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    delButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        delButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 3, 0);
    jPanel3.add(delButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    dataSelectPanel.add(jPanel3, gridBagConstraints);

    tabbedPane.addTab("\u30c7\u30fc\u30bf", dataSelectPanel);

    transitPanel.setLayout(new java.awt.GridBagLayout());

    transitPanel.setFocusable(false);
    transitPanel.add(timePlacePanel2, new java.awt.GridBagConstraints());

    jPanel4.setFocusable(false);
    setButton2.setMnemonic('S');
    setButton2.setText("Set");
    setButton2.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    setButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setButton2ActionPerformed(evt);
      }
    });

    jPanel4.add(setButton2);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    transitPanel.add(jPanel4, gridBagConstraints);

    tabbedPane.addTab("\u76f4\u5165\u529b", transitPanel);

    add(tabbedPane);

  }// </editor-fold>//GEN-END:initComponents
  // 直入力のセットボタン
  private void setButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButton2ActionPerformed
    timePlacePanel2.getTimePlace(transit);
    System.out.println("written = " + transit.toString());
    int num = nameComboBox.getSelectedIndex();
    if(num >= 0) {
      TimePlace tap = dataList.get(num).getTimePlace();
      timePlacePanel.getTimePlace(tap);
    }
    tcont.set();
  }//GEN-LAST:event_setButton2ActionPerformed
  //コンボボックス内のデータ削除
  private void delButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delButtonActionPerformed
    if(compositNatal == null) {
      if(dataList.size() <= 1) {
        JOptionPane.showInternalMessageDialog(iframe,"最後のデータは削除できません",
          "データリストの削除",JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {
      if(dataList.size() <= 2) {
        JOptionPane.showInternalMessageDialog(iframe,"コンポジットデータは2件以下にはできません",
          "データリストの削除",JOptionPane.ERROR_MESSAGE);
        return;        
      }
    }
    int num = nameComboBox.getSelectedIndex();
    if( JOptionPane.showInternalConfirmDialog(iframe,
          "\""+dataList.get(num).toString()+"\"を削除しますか？",
          "データリストからの削除",JOptionPane.YES_NO_OPTION) 
      == JOptionPane.YES_OPTION ) {
      dataList.remove(num);
      eventStop = true; //セレクトイベント禁止
      nameComboBox.removeItemAt(num);  //削除
      eventStop = false; //禁止解除
      nameComboBox.setSelectedIndex(0); //0番目を選択。selected()が呼ばれる。
    }
  }//GEN-LAST:event_delButtonActionPerformed
  //コンボボックスで選択イベントが起きたとき
  private void nameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameComboBoxActionPerformed
    if(eventStop) return;
    int num = nameComboBox.getSelectedIndex();
    if(num < 0) return;
    Data data = dataList.get(num);
    timePlacePanel.setTimePlace(data.getTimePlace());
    if(data.getNatal().getTime() == null)
      timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    createHistoryMenu();
    tcont.set();
  }//GEN-LAST:event_nameComboBoxActionPerformed

  private void nameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nameComboBoxItemStateChanged

  }//GEN-LAST:event_nameComboBoxItemStateChanged
  //ネイタルフォームのセットボタン
  private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
    timePlacePanel2.getTimePlace(transit);
    int num = nameComboBox.getSelectedIndex();
    TimePlace tap = dataList.get(num).getTimePlace();
    timePlacePanel.getTimePlace(tap);
    tcont.set();
  }//GEN-LAST:event_setButtonActionPerformed
  //ネイタルフォームのリセットボタン
  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    int num = nameComboBox.getSelectedIndex();
    dataList.get(num).resetTimePlace();
    timePlacePanel.setTimePlace(dataList.get(num).getTimePlace());
    tcont.set();
  }//GEN-LAST:event_resetButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton delButton;
  private javax.swing.JComboBox nameComboBox;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton setButton;
  private javax.swing.JButton setButton2;
  private javax.swing.JTabbedPane tabbedPane;
  private to.tetramorph.starbase.TimePlacePanel timePlacePanel;
  private to.tetramorph.starbase.TimePlacePanel timePlacePanel2;
  // End of variables declaration//GEN-END:variables
  
}
