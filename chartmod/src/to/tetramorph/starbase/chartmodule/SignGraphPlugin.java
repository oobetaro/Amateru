/*
 * SignGraphPlugin.java
 * Created on 2008/03/11, 15:45
 */
package to.tetramorph.starbase.chartmodule;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JRadioButton;
import to.tetramorph.starbase.util.NatalChart;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * 星座分布グラフプラグイン。
 * DBから送られてきた複数のデータを天体別に集計して、星座ごとにグラフ化する。
 * @author 大澤義鷹
 */
public class SignGraphPlugin extends ChartModulePanel {
    ChartConfig cc = new ChartConfig();
    NatalChart chart = new NatalChart(cc,0); // グループコードはゼロ
    ChannelData channelData;
    ChartData chartData;
    SignGraphPanel signGraphPanel;

    /**
     * チャートモジュールを作成する。
     */
    public SignGraphPlugin() {
        super();
        initComponents();
    }
    
    public void init() {
        signGraphPanel = new SignGraphPanel();
        add( signGraphPanel, BorderLayout.CENTER );
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                String cmd = ((JRadioButton)evt.getSource()).getActionCommand();
                signGraphPanel.setGraphMode( Integer.parseInt( cmd ));
                signGraphPanel.repaint();
            }
        };
        genderRadioButton1.addActionListener( al );
        genderRadioButton2.addActionListener( al );
        planetComboBox.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                calc();
            }
        });
    }
    
    /**
     * Natal(複数)を受け取る。
     */
    public void setData( ChannelData data ) {
        this.channelData = data;
        chartData = channelData.get(0);
        chartData.setTabIcon();
        if ( chartData.getSelectedIndex() < 0 ) return;
        calc();
    }
    
    private void calc() {
        List<Data> dataList = chartData.getDataList();
        List<Body> list = new ArrayList<Body>();
        for ( int i=0; i < dataList.size(); i++ ) {
            Data data = dataList.get(i);
            TimePlace tp = data.getTimePlace();
            chart.setTimePlace( tp );
            int id = planetComboBox.getSelectedBody();
            Body body = chart.getBody( id );
            body.group = data.getNatal().getGender();
            list.add( body );
        }
        signGraphPanel.setBodyList( list );
        String cmd = genderButtonGroup.getSelection().getActionCommand();
        signGraphPanel.setGraphMode( Integer.parseInt(cmd));
        signGraphPanel.repaint();
    }
    /**
     * チャートの画像を返す。
     */
    @Override
    public BufferedImage getBufferedImage( Dimension size ) {
        return signGraphPanel.getBufferedImage( size );
    }
    /**
     * このプラグインは画像を返す機能を実装していて、このメソッドはtrueを返す。
     * 実装していない場合はfalseを返す。
     */
    @Override
    public boolean isImageServiceActivated() {
        return true;
    }

    public String toString() {
        return "星座分布グラフ";
    }
    
    public int getChannelSize() {
        return 1;
    }
    
    static final String [] channelNames = { "ネイタル" };
    
    public String [] getChannelNames() {
        return channelNames;
    }
    
    public boolean isNeedTransit() {
        return false;
    }
    
    @Override
    public Printable getPainter() { return signGraphPanel; }
    
    @Override
    public boolean isPrintable() { return true; }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel jPanel1;

        genderButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();
        genderRadioButton1 = new javax.swing.JRadioButton();
        genderRadioButton2 = new javax.swing.JRadioButton();

        genderButtonGroup.add( genderRadioButton1 );
        genderButtonGroup.add( genderRadioButton2 );

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel1.add(planetComboBox);

        genderRadioButton1.setSelected(true);
        genderRadioButton1.setText("\u6027\u5225\u7121\u8996");
        genderRadioButton1.setActionCommand("0");
        genderRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        genderRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(genderRadioButton1);

        genderRadioButton2.setText("\u7537\u5973\u5225");
        genderRadioButton2.setActionCommand("1");
        genderRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        genderRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(genderRadioButton2);

        add(jPanel1, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup genderButtonGroup;
    private javax.swing.JRadioButton genderRadioButton1;
    private javax.swing.JRadioButton genderRadioButton2;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
    // End of variables declaration//GEN-END:variables
    
}
