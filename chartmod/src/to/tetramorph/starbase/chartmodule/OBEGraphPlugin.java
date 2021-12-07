/*
 * OBEGraphPlugin.java
 * 2008/03/28 09:46
 */
package to.tetramorph.starbase.chartmodule;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import to.tetramorph.starbase.util.NatalChart;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * 幽体離脱予報グラフプラグイン。
 * DBから送られてきた複数のデータを天体別に集計して、星座ごとにグラフ化する。
 * @author 大澤義鷹
 */
public class OBEGraphPlugin extends ChartModulePanel {
    ChartConfig cc = new ChartConfig();
    NatalChart chart = new NatalChart(cc,0); // グループコードはゼロ
    ChannelData channelData;
    ChartData chartData;
    OBEGraphPanel graphPanel;
    OBECalendar obeCalendar;
    Transit transit;
    int DAYS = 3;
    JCheckBox [] checkBoxs;
    /**
     * チャートモジュールを作成する。
     */
    public OBEGraphPlugin() {
        super();
        initComponents();
        checkBoxs = new JCheckBox [] {
            visibleCheckBox1,
            visibleCheckBox2,
            visibleCheckBox3,
            visibleCheckBox4,
        };
    }
    /**
     * 初期化
     */
    @Override
    public void init() {
        graphPanel = new OBEGraphPanel();
        Border border = BorderFactory.createBevelBorder( BevelBorder.LOWERED );
        graphPanel.setBorder( border );
        //予報計算をするクラスを、計算結果を通達するリスナを指定して作成
        //OBECalendarの計算結果はOBEGraphPaneに通達される。
        obeCalendar = new OBECalendar( graphPanel );
        add( graphPanel, BorderLayout.CENTER );
        //グラフバーの表示選択チェックボックスのリスナを作成登録
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                // OBEGraphPanelにスイッチの情報を与え、再描画
                graphPanel.setBarVisible( checkBoxs );
                graphPanel.repaint();
            }
        };
        for ( int i=0; i < checkBoxs.length; i++ ) {
            checkBoxs[i].addActionListener( al );
        }
        //表示日数選択コンボボックスのリスナを登録
        daysComboBox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                Object o = ((JComboBox)evt.getSource()).getSelectedItem();
                //コンボボックスから日数を取得し、DAYSに設定
                DAYS = Integer.parseInt((String)o);
                calc(); //再計算メソッドを呼び出し
            }
        });
    }
    
    /**
     * Natal(複数)を受け取る。
     */
    @Override
    public void setData( ChannelData data ) {
        
        //OBEGraphPanelがOBECalendarからまだ計算結果を受け取っていないときは待つ
        //マルチスレッドで計算されるため、計算中にこのメソッドが割り込むとまずい。
        //なぜマルチスレッドで計算するかというと時間がかかるので、シングルだと
        //Swingがフリーズするから。
        
        while ( graphPanel.isBusy() ) {
            try {
                Thread.sleep( 5 );
            } catch ( InterruptedException e ) { }
        } 
        this.channelData = data;
        chartData = channelData.get(0);
        chartData.setTabIcon();
        transit = data.getTransit();
        if ( chartData.getSelectedIndex() < 0 ) return;
        calc();
    }
    
    /**
     * 予報計算処理
     */
    private void calc() {
        graphPanel.setTransit( transit );
        Natal natal = chartData.getSelectedData().getNatal();
        graphPanel.setName( natal.getName() );
        // 計算開始前にビジーフラグを立てる
        graphPanel.setBusy( true );
        TimePlace natalTimePlace = chartData.getSelectedData().getTimePlace();
        // 計算開始。計算終了のときビジーフラグはfalseになる。
        // 計算結果はOBEGraphPanel#calcuratedに通達される。
        obeCalendar.setTimePlace( (TimePlace)transit, 
                                  natalTimePlace , 
                                  DAYS * 24 );         // 日数×24時間
    }
    
    //    obeCalendarには、graphPaneがリスナとして登録されている。
    //    setTimePlace呼びだしによって、別スレッドで天体計算が行われ、
    //    計算結果はリスナのOBEGraphPanel#calcurateに引き渡される。
    
    /**
     * チャートの画像を返す。
     */
    @Override
    public BufferedImage getBufferedImage( Dimension size ) {
        return graphPanel.getBufferedImage( size );
    }
    
    /**
     * このプラグインは画像を返す機能を実装していて、このメソッドはtrueを返す。
     * 実装していない場合はfalseを返す。
     */
    @Override
    public boolean isImageServiceActivated() {
        return true;
    }

    @Override
    public String toString() {
        return "幽体離脱予報グラフ";
    }
    
    @Override
    public int getChannelSize() {
        return 1;
    }
    
    static final String [] channelNames = { "ネイタル" };
    
    @Override
    public String [] getChannelNames() {
        return channelNames;
    }
    
    @Override
    public boolean isNeedTransit() {
        return true;
    }
    
    @Override
    public Printable getPainter() { return graphPanel; }
    
    @Override
    public boolean isPrintable() { return true; }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JLabel jLabel1;
        javax.swing.JPanel jPanel1;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        visibleCheckBox1 = new javax.swing.JCheckBox();
        visibleCheckBox2 = new javax.swing.JCheckBox();
        visibleCheckBox3 = new javax.swing.JCheckBox();
        visibleCheckBox4 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        daysComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("\u30b0\u30e9\u30d5\u8868\u793a\u9078\u629e");
        jPanel1.add(jLabel1);

        visibleCheckBox1.setSelected(true);
        visibleCheckBox1.setText("\u592a\u967d\u306e\u30ea\u30ba\u30e0");
        visibleCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        visibleCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(visibleCheckBox1);

        visibleCheckBox2.setSelected(true);
        visibleCheckBox2.setText("\u706b\u661f\u306e\u30ea\u30ba\u30e0");
        visibleCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        visibleCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(visibleCheckBox2);

        visibleCheckBox3.setSelected(true);
        visibleCheckBox3.setText("\u6708\u306e\u30ea\u30ba\u30e0");
        visibleCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        visibleCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(visibleCheckBox3);

        visibleCheckBox4.setSelected(true);
        visibleCheckBox4.setText("\u500b\u4eba\u306e\u30ea\u30ba\u30e0");
        visibleCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        visibleCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(visibleCheckBox4);

        jLabel2.setText("\uff0f\u8868\u793a\u65e5\u6570");
        jPanel1.add(jLabel2);

        daysComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3", "5", "7", "15", "30" }));
        jPanel1.add(daysComboBox);

        jLabel3.setText("\u65e5");
        jPanel1.add(jLabel3);

        add(jPanel1, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox daysComboBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox visibleCheckBox1;
    private javax.swing.JCheckBox visibleCheckBox2;
    private javax.swing.JCheckBox visibleCheckBox3;
    private javax.swing.JCheckBox visibleCheckBox4;
    // End of variables declaration//GEN-END:variables
    
}
