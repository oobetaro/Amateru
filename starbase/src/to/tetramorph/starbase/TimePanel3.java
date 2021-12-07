/*
 * TimePanel3.java
 *
 * Created on 2007/11/13, 6:25
 */

package to.tetramorph.starbase;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import to.tetramorph.starbase.formatter.LimitedDocument;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * abstractクラスのTimePanelを継承し実装したクラス。
 * 重要なメソッド setModule
 * 2011-08-08 トランジットパネルの名前とメモの入力欄に文字数制限がかかっていなか
 * ったのを修正。名前欄は40文字、メモ欄は60文字の制限をかけた。
 * @author  大澤義孝
 */
public class TimePanel3 extends TimePanel {
    public static final int NATAL_BUTTON = 0;
    public static final int TRANSIT_BUTTON = 1;

    TimeManeuverPanel maneuverPanel;
    ChartModulePanel module; //set()が参照
    ChartPane iframe;
    Transit transit = new Transit(); //トランジットタブの値
    /**
     * 通常このコンストラクタは使用しない。
     */
    public TimePanel3() {
        initComponents();
        maneuverPanel = new TimeManeuverPanel(maneuverToggleButton);
        maneuverPanel.setTimeManeuverListener(new TimeManeuverHandler());
        basePanel.add(maneuverPanel,"HANDLE");
        //CardLayout clay = (CardLayout)dataPanel.getLayout();
        nameTextField.setDocument(new LimitedDocument(28));
        memoTextField.setDocument(new LimitedDocument(28));
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JToggleButton button = (JToggleButton)evt.getSource();
                if ( button == tabToggleButton1 ) {
                    if ( button.isSelected() ) {
                        setSelectedButton( NATAL_BUTTON );
                    }
                } else {
                    if ( button.isSelected() ) {
                        setSelectedButton( TRANSIT_BUTTON );
                    }
                }
            }
        };
        tabToggleButton1.addActionListener(al);
        tabToggleButton2.addActionListener(al);
        maneuverToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JToggleButton button = (JToggleButton)evt.getSource();
                setSelectedManeuver( button.isSelected() );
                if ( ! button.isSelected() ) {
                    tabToggleButton1.setEnabled( true );
                    if ( module == null ) {
                        tabToggleButton1.setEnabled( true );
                    } else {
                        tabToggleButton2.setEnabled( module.isNeedTransit() );
                    }
                }
            }
        });
        setSelectedButton( TRANSIT_BUTTON );
        channelData = new ChannelData( this );
        transitTimePlacePanel.setDefault();
        transitTimePlacePanel.getTimePlace(transit); //参照でtransitに書き込み
        channelData.setTransit(transit);
    }

    /**
     * オブジェクトを作成。ChartPaneから呼び出される。
     * @param iframe 呼出もとのチャートフレームの参照アドレス
     */
    public TimePanel3(ChartPane iframe) {
        this();
        this.iframe = iframe;
    }

    /**
     * ネイタルボタンか、トランジットボタンのどちからを選択状態にする。
     * ただしトランジットボタンがdisenabledのときTRANIST_BUTTONが指定されたら、
     * 要求は無視する。
     * @param index NATAL_BUTTON,TRANSIT_BUTTONのどちらか。
     */
    @Override
    public final void setSelectedButton( int index ) {
        setSelectedManeuver( false );
        tabToggleButton1.setEnabled( true );
        boolean transitButtonEnabled = module == null ? true : module.isNeedTransit();
        tabToggleButton2.setEnabled( transitButtonEnabled );
        CardLayout lay = (CardLayout)dataPanel.getLayout();
        if ( NATAL_BUTTON == index ) {
            lay.show( dataPanel, "NATAL" );
            tabToggleButton1.setSelected( true );
        } else if ( TRANSIT_BUTTON == index ) {
            if ( ! transitButtonEnabled ) return;
            lay.show( dataPanel,"TRANSIT" );
            tabToggleButton2.setSelected( true );
        } else {
            throw new java.lang.IllegalArgumentException("indexが範囲外");
        }
    }
    /**
     * ネイタルボタンか、トランジットボタンのどちらが選択されているかを返す。
     */
    public int getSelectedButtonIndex() {
        return tabToggleButton1.isSelected() ? NATAL_BUTTON : TRANSIT_BUTTON;
    }
    /**
     * ハンドルボタンの選択状態をセットする。
     * @param b trueならON,falseならOFF。
     */
    @Override
    public void setSelectedManeuver( boolean b ) {
        CardLayout lay = (CardLayout)basePanel.getLayout();
        if(b) {
            lay.show(basePanel,"HANDLE");
            maneuverToggleButton.setSelected(true);
            tabToggleButton1.setEnabled( false );
            tabToggleButton2.setEnabled( false );
        } else {
            lay.show(basePanel,"DATA");
            maneuverToggleButton.setSelected(false);
//            tabToggleButton1.setEnabled( true );
//            tabToggleButton2.setEnabled( true );
        }
    }
    /**
     * ハンドルボタンの選択状態を返す。
     */
    @Override
    public boolean isSelectedManeuverButton() {
        return maneuverToggleButton.isSelected();
    }


    /**
     * トグルボタンのEnabled状態をセットする。
     * ハンドルボタンが押されているときはなにもしない。
     * @param index NATAL_BUTTON,TRANSIT_BUTTONのどちらか。
     * @param b trueならenabled,falseならdisenabled
     */
    void setTabEnabled(int index,boolean b) {
        if( maneuverToggleButton.isSelected()) return;
        if ( index == NATAL_BUTTON )
            tabToggleButton1.setEnabled(b);
        else if ( index == TRANSIT_BUTTON )
            tabToggleButton2.setEnabled(b);
        else
            throw new java.lang.IllegalArgumentException("indexが範囲外");
    }

    @Override
    public boolean isComprise(int id) {
        for(int i=0; i<channelData.size(); i++) {
            //ChartDataPanelのisCompriseを呼び出している。
            if(channelData.get(i).isComprise(id)) return true;
        }
        return false;
    }
    /**
     * ChartModulePanelをセットする。モジュールのチャンネル数に応じて必要な
     * チャンネルタブが用意される。
     */
    @Override
    public void setModule( ChartModulePanel module ) {
        入力準備();
        this.module = module;
        maneuverPanel.setAnimationActivated( module.isAnimationActivated() );
        tabbedPane.removeAll();
        //トランジット不要のモジュールはトランジットボタンをdisenabledにする
        //setTabEnabled( TRANSIT_BUTTON, module.isNeedTransit() );
        if ( module.isNeedTransit() ) {
            setSelectedButton( TRANSIT_BUTTON );
        } else {
            setSelectedButton( NATAL_BUTTON );
        }
        while ( module.getChannelSize() > channelData.size() ) {
            ChartDataPanel cdp = new ChartDataPanel( this, iframe );
            cdp.setEnabled( false );
            channelData.add( cdp );
        }
        for ( int i=0; i < module.getChannelSize(); i++ ) {
            tabbedPane.add( (ChartDataPanel)channelData.get(i) );
            tabbedPane.setTitleAt( i, module.getChannelNames()[i] );
        }
        revalidate();
        repaint();
    }
    /**
     * オートインクリメント(アニメーション)を停止する。
     */
    @Override
    public void stopTimer() {
        maneuverPanel.stopTimer();
    }

    @Override
    public void stop() {
        maneuverPanel.stop();
    }

    private void 入力準備() {
        stopTimer();
        maneuverPanel.reset();
        setSelectedManeuver(false);
    }

    /**
     * ネイタルデータのリストをセットする。
     * このパネルがオートインクリメント中の場合解除される。
     */
    @Override
    public void setNatal(List<Natal> list) {
        入力準備();
        ChartDataPanel cdp = ((ChartDataPanel)tabbedPane.getSelectedComponent());
        cdp.setEnabled(true);
        cdp.setNatal(list);
    }
    /**
     * ネイタルデータのリストを追加する。このパネルがオートインクリメント中の場合、
     * それは解除される。
     */
    @Override
    public void addNatal(List<Natal> list) {
        入力準備();
        ChartDataPanel cdp = ((ChartDataPanel)tabbedPane.getSelectedComponent());
        cdp.setEnabled(true);
        cdp.addNatal(list);
    }

    @Override
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
     * フィールドに入力されているトランジットを返す。
     */
    @Override
    public Transit getTransit() {
        transitTimePlacePanel.getTimePlace(transit);
        transit.setName(nameTextField.getText().trim());
        transit.setMemo(memoTextField.getText().trim());
        return transit;
    }

    @Override
    public ChartData getSelectedChartData() {
        return (ChartData)tabbedPane.getSelectedComponent();
    }

    @Override
    public void dataCopy(TimePanel tp) {
        ChannelData srcChannel = tp.channelData;
        //ﾄﾗﾝｼｯﾄを複製
        transit = new Transit(tp.getTransit());
        nameTextField.setText(transit.getName());
        memoTextField.setText(transit.getMemo());
        transitTimePlacePanel.setTimePlace(transit);
        if ( transit.getTime() == null ) {
            String dt = Config.getDefaultTime();
            transit.setTime(Time.valueOf(dt));
        }
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

    @Override
    public void set() {
        assert SwingUtilities.isEventDispatchThread();
        assert module != null : "モジュールがnull";
        module.setData( channelData );
    }

    @Override
    public ChartModulePanel getModule() {
        return module;
    }
    /**
     * トランジットをフィールドにセットする。
     */
    @Override
    public void setTransit(Transit t) {
        this.transit = new Transit( t ); //複製する
        setSelectedButton( TRANSIT_BUTTON );
        nameTextField.setText( transit.getName() );
        memoTextField.setText( transit.getMemo() );
        transitTimePlacePanel.setTimePlace( transit );
        if ( transit.getTime() == null ) {
            String dt = Config.getDefaultTime();
            transitTimePlacePanel.setTime( Time.valueOf(dt) );
        }
        transitSetButtonActionPerformed( null );
    }

    /**
     * トランジットの日時をチャートモジュール側から更新するときに使用する。
     */
    @Override
    public void updateTransit( Transit transit ) {
        this.transit = transit;
        setSelectedButton( TRANSIT_BUTTON );
        nameTextField.setText( transit.getName() );
        memoTextField.setText( transit.getMemo() );
        transitTimePlacePanel.setTimePlace( transit );
    }

//内部クラスにするのは、これらのメソッドをJavaDocに出したくないから
    class TimeManeuverHandler implements TimeManeuverListener {
        //カレンダーに時間を足す
        @Override
        public void increment() {
            addCalendar(1);
        }
        //カレンダーから時間を引く
        @Override
        public void decrement() {
            addCalendar(-1);
        }

        @Override
        public void store() {
            GregorianCalendar cal = maneuverPanel.getCalendar();
            if(getSelectedButtonIndex() == NATAL_BUTTON) { //ネイタルタブが選択
                ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
                //cdp.getSelectedData().getTimePlace().setCalendar(cal,TimePlace.DATE_AND_TIME);
                cdp.setCalendar(cal);
                set();
            } else {
                transitTimePlacePanel.setCalendar(cal);
                transitSetButtonActionPerformed(null);
            }
        }
    }
    // NumberFieldの各桁の値をカレンダーに足し、日付と時間のﾌｨｰﾙﾄﾞに値をｾｯﾄする
    private void addCalendar(int sign) {
        int second = maneuverPanel.getSecond() * sign;
        int minute = maneuverPanel.getMinute() * sign;
        int hour = maneuverPanel.getHour() * sign;
        int day = maneuverPanel.getDay() * sign;
        GregorianCalendar cal;
        if(getSelectedButtonIndex() == NATAL_BUTTON) { //ネイタルタブが選択
            ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
            cal = cdp.addCalendar(day,hour,minute,second);
            //set()はcdp側でキックされる
        } else {
            cal = transitTimePlacePanel.addCalendar(day,hour,minute,second);
            transitSetButtonActionPerformed(null);
        }
        maneuverPanel.setCalendar(cal);
    }

    private void setupManeuver() {
        GregorianCalendar cal = null;
        String title = null;
        if(getSelectedButtonIndex() == NATAL_BUTTON) { //ネイタルタブが選択
            ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
            Data data = cdp.getSelectedData();
            cal = data.getTimePlace().getCalendar();
            title = data.getNatal().getName();
        } else {
            cal = transitTimePlacePanel.getCalendar();
            title = nameTextField.getText();
            if(title.length() == 0) title = "トランジット";
        }
        maneuverPanel.setCalendar(cal);
        maneuverPanel.setTitle( title );
    }

    static void createAndShowGUI() {
//        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
                 UIManager.setLookAndFeel(
                        "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch ( Exception e ) {
            Logger.getLogger(TimePanel3.class.getName()).log(Level.SEVERE,null,e);
        }
//        FontUIResource font = new FontUIResource(new Font("Dialog",Font.PLAIN,10));
//        UIManager.put("Button.font",font);
//        UIManager.put("ToggleButton.font",font);
//        UIManager.put("Label.font",font);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout());
        frame.getContentPane().add(new TimePanel3());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        basePanel = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        javax.swing.JPanel transitPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        memoTextField = new javax.swing.JTextField();
        transitTimePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        transitSetButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel togglePane = new javax.swing.JPanel();
        tabToggleButton1 = new javax.swing.JToggleButton();
        tabToggleButton2 = new javax.swing.JToggleButton();
        javax.swing.JPanel handlePane = new javax.swing.JPanel();
        maneuverToggleButton = new javax.swing.JToggleButton();

        buttonGroup.add(tabToggleButton1);
        buttonGroup.add(tabToggleButton2);

        setLayout(new java.awt.GridBagLayout());

        basePanel.setLayout(new java.awt.CardLayout());

        dataPanel.setLayout(new java.awt.CardLayout());
        dataPanel.add(tabbedPane, "NATAL");

        transitPanel.setFocusable(false);
        transitPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("名前");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 4);
        jPanel2.add(jLabel1, gridBagConstraints);

        nameTextField.setColumns(10);
        nameTextField.setPreferredSize(new java.awt.Dimension(97, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel2.add(nameTextField, gridBagConstraints);

        jLabel2.setText("メモ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(jLabel2, gridBagConstraints);

        memoTextField.setPreferredSize(new java.awt.Dimension(97, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(memoTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        transitPanel.add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        transitPanel.add(transitTimePlacePanel, gridBagConstraints);

        jPanel4.setFocusable(false);

        transitSetButton.setMnemonic('S');
        transitSetButton.setText("Set");
        transitSetButton.setToolTipText("フィールドの値をチャートに送る");
        transitSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transitSetButtonActionPerformed(evt);
            }
        });
        jPanel4.add(transitSetButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        transitPanel.add(jPanel4, gridBagConstraints);

        dataPanel.add(transitPanel, "TRANSIT");

        basePanel.add(dataPanel, "DATA");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(basePanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.BorderLayout());

        togglePane.setLayout(new java.awt.GridLayout(1, 2));

        tabToggleButton1.setFont(new java.awt.Font("MS UI Gothic", 0, 10));
        tabToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tume1.gif"))); // NOI18N
        tabToggleButton1.setText("ネイタル");
        tabToggleButton1.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tume2.gif"))); // NOI18N
        tabToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        togglePane.add(tabToggleButton1);

        tabToggleButton2.setFont(new java.awt.Font("MS UI Gothic", 0, 10));
        tabToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tume1.gif"))); // NOI18N
        tabToggleButton2.setText("トランジット");
        tabToggleButton2.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/tume2.gif"))); // NOI18N
        tabToggleButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tabToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabToggleButton2ActionPerformed(evt);
            }
        });
        togglePane.add(tabToggleButton2);

        jPanel1.add(togglePane, java.awt.BorderLayout.CENTER);

        handlePane.setLayout(new java.awt.GridLayout(1, 0));

        maneuverToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/handle_off.png"))); // NOI18N
        maneuverToggleButton.setFocusPainted(false);
        maneuverToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        maneuverToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/handle_on.png"))); // NOI18N
        maneuverToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maneuverToggleButtonActionPerformed(evt);
            }
        });
        handlePane.add(maneuverToggleButton);

        jPanel1.add(handlePane, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void maneuverToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maneuverToggleButtonActionPerformed
      AbstractButton button = (AbstractButton)evt.getSource();
      if(button.isSelected()) setupManeuver();
  }//GEN-LAST:event_maneuverToggleButtonActionPerformed

  private void transitSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitSetButtonActionPerformed
      transitTimePlacePanel.getTimePlace(transit);
      transit.setName(nameTextField.getText().trim());
      transit.setMemo(memoTextField.getText().trim());
      channelData.setTransit(transit);
      set();
  }//GEN-LAST:event_transitSetButtonActionPerformed

  private void tabToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabToggleButton2ActionPerformed
      // TODO add your handling code here:
  }//GEN-LAST:event_tabToggleButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel basePanel;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JToggleButton maneuverToggleButton;
    private javax.swing.JTextField memoTextField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JToggleButton tabToggleButton1;
    private javax.swing.JToggleButton tabToggleButton2;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JButton transitSetButton;
    private to.tetramorph.starbase.TimePlacePanel transitTimePlacePanel;
    // End of variables declaration//GEN-END:variables

}
