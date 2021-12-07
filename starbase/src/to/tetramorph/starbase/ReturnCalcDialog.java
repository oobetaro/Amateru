/*
 * ReturnCalcDialog.java
 *
 * Created on 2008/09/03, 17:20
 */

package to.tetramorph.starbase;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TCPlanet;
import swisseph.TransitCalculator;
import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.GregorianDateFormatter;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.lib.SearchResultReceiver;
import to.tetramorph.starbase.util.NatalChart;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.time.JDay;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.ParentWindow;
/**
 * 回帰検索ダイアログ。
 * @author  大澤義鷹
 */
public class ReturnCalcDialog extends javax.swing.JDialog {
    SearchResultReceiver resultReceiver;
    Natal natal;
    Place place;

    /**
     *
     */
    public ReturnCalcDialog( java.awt.Frame parent,
                              SearchResultReceiver resultReceiver ) {
        super( parent, true );
        initComponents();
        this.resultReceiver = resultReceiver;
        dateFTextField1.setFormatterFactory(
            new FormatterFactory( new GregorianDateFormatter() ) );
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 99, 1);
        countSpinner.setModel( model );

        Integer [] planets = new Integer [] {
            SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN };
        planetComboBox.setItems( planets );
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        if(System.getProperty("java.version").startsWith("1.6")) {
//            Image img = IconLoader.getImage("/resources/niwatori2.png");
            setIconImage(AppIcon.TITLE_BAR_ICON);
        }
        getRootPane().setDefaultButton(searchButton);
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("クローズボタン押された");
            }
        });
//        WindowMoveHandler winmove =
//            new WindowMoveHandler("ReturnCalcDialog.BOUNDS",this);
//        addComponentListener( winmove );
//        winmove.setBounds();
        setLocationRelativeTo( parent );
        planetComboBox.addActionListener( new PlanetSelectionListener() );
    }

    /**
     * 回帰計算ダイアログを可視化する。
     * @param natal 回帰計算を行う出生データ
     */
    public void showDialog( Natal natal ) {
        dateFTextField1.setValue( new GregorianCalendar() );
        setNatal( natal );
        // DefaultButtonの設定はdisposeされたときクリアされてしまうので、
        // 再セットが必要。
        getRootPane().setDefaultButton( searchButton );
        pack();
        setVisible(true);
        toFront();
    }

    // 天体ごとに検索する期間は異なる。おおよそ天体が一周する日数。
    private static final int [] termDays = {
        367,    // sun
        32,     // mon
        90,     // mer
        230,    // ven
        700,    // mar
        4382,   // jup
        10800   // sat
    };

    /**
     *
     * 回帰計算する出生データをセットする。
     */
    public void setNatal( Natal natal ) {
        this.natal = natal;
        nameLabel.setText( natal.getName() );
        dateLabel.setText( natal.getStringDate() );
        timeLabel.setText( natal.getStringTime() );
        lastDate();
        Place pl = natal.getTransitPlace();
        if ( pl == null ) pl = natal.getPlace();
        if ( pl != null ) placePanel.setPlace( pl );
    }

    private void lastDate() {
        int body_id = planetComboBox.getSelectedBody();
        GregorianCalendar gcal = (GregorianCalendar)dateFTextField1.getValue();
        if ( gcal == null ) {
            gcal = new GregorianCalendar();
            dateFTextField1.setValue( gcal );
        }
        int i = ( body_id <= 1 ) ? 1 : 3;
        countSpinner.setValue( i );
    }

    class PlanetSelectionListener implements ActionListener {
        public void actionPerformed( ActionEvent evt ) {
            lastDate();
        }
    }

    // 検索期間の指定が不完全なときは、エラーダイアログを出す
    private boolean isTermError() {
        Object s = dateFTextField1.getValue();
        if ( s == null ) {
            JOptionPane.showMessageDialog(
                this,
                "検索期間を指定して下さい。",
                "回帰日時の検索のエラー",
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        Place place = placePanel.getPlace();
        if ( ! place.isCompletePlace() ) {
            JOptionPane.showMessageDialog(
                this,
                "観測地を指定して下さい。",
                "回帰日時の検索のエラー",
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        return false;
    }
    /**
     * @param begin_gcal 検索開始日時
     * @param hit_count 検出回数
     * @param body_id 検索天体
     * @param lon 検索天体が黄経何度になる時を検索するかを指定。黄道座標で。
     * @param backward 過去に向かって検索したいときはtrueを指定。
     * @param resultList 結果の日時のリストを返す引数。
     */
    private void search( GregorianCalendar begin_gcal,
                           int hit_count,
                           int body_id,
                           double lon,
                           boolean backward,
                           List<GregorianCalendar> resultList ) {
        SwissEph eph = Ephemeris.getInstance().getSwissEph();
        TimeZone timeZone = begin_gcal.getTimeZone();
        double jday = JDay.get( begin_gcal );
        System.out.println("start jday = " + jday );
        System.out.println("target lon = " + lon );
        System.out.println("body   id  = " + body_id );
        while ( hit_count > 0 ) {
            double deltaT = SweDate.getDeltaT( jday );
            double ET = jday + deltaT;
            int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
            TransitCalculator tcal = new TCPlanet( eph,
                                                    body_id,
                                                    flags,
                                                    lon );
            double transitTime = eph.getTransitET( tcal, ET, backward ) - deltaT;
            GregorianCalendar gcal2 = JDay.getCalendar( transitTime, timeZone );
            resultList.add( gcal2 );
            jday = transitTime;
            hit_count--;
        }
    }

    private void createNatals( List<GregorianCalendar> list,
                                 int body_id,
                                 Place place ) {
        List<Natal> natalList = new ArrayList<Natal>();
        String bodyName = Const.PLANET_NAMES[ body_id ];
        String name = natal.getName().split(" |　")[0];
        String tabName = String.format( "%s %s回帰", name, bodyName );
        String title = String.format( "%s %s回帰", natal.getName(), bodyName );
        for ( int i = 0; i < list.size(); i++ ) {
            Natal n = new Natal();
            //String bodyName = Const.PLANET_NAMES[ body_id ];
            //String title = natal.getName() + " " + bodyName + "回帰";
            n.setName( title );
            n.setChartType( Natal.NATAL );
            n.setId( Natal.NEED_REGIST );
            n.setGender( natal.getGender() );
            n.setCalendar( list.get(i), Natal.DATE_AND_TIME );
            n.setPlace( place );
            natalList.add( n );
        }
        resultReceiver.write( new SearchResult( natalList ,
                                                tabName,
                                                title ,
                                                "(未登録)",
                                                toString()));
    }

    public String toString() {
        return "回帰検索";
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel datePanel;
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel2;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel7;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;
        javax.swing.JPanel jPanel4;

        jPanel1 = new javax.swing.JPanel();
        datePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dateFTextField1 = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        countSpinner = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();
        jLabel3 = new javax.swing.JLabel();
        placePanel = new to.tetramorph.starbase.PlacePanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        backwardCheckBox = new javax.swing.JCheckBox();
        searchButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u56de\u5e30\u691c\u7d22");
        setResizable(false);
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 1, 16));
        datePanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("\u691c\u7d22\u958b\u59cb\u65e5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
        datePanel.add(jLabel1, gridBagConstraints);

        dateFTextField1.setColumns(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        datePanel.add(dateFTextField1, gridBagConstraints);

        jLabel2.setText("\u691c\u51fa\u56de\u6570");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 5);
        datePanel.add(jLabel2, gridBagConstraints);

        countSpinner.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        datePanel.add(countSpinner, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanel1.add(datePanel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel2.add(planetComboBox, gridBagConstraints);

        jLabel3.setText("\u56de\u5e30\u691c\u7d22\u5929\u4f53");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        jPanel1.add(placePanel, gridBagConstraints);

        jLabel4.setText("\u56de\u5e30\u89b3\u6e2c\u5730");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("\u540d\u524d\uff1a");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        jPanel4.add(jLabel5, gridBagConstraints);

        nameLabel.setText("                        ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 2, 0);
        jPanel4.add(nameLabel, gridBagConstraints);

        jLabel6.setText("\u6642\u523b\uff1a");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 2);
        jPanel4.add(jLabel6, gridBagConstraints);

        dateLabel.setText("                        ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        jPanel4.add(dateLabel, gridBagConstraints);

        jLabel7.setText("\u65e5\u4ed8\uff1a");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        jPanel4.add(jLabel7, gridBagConstraints);

        timeLabel.setText("                        ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanel4.add(timeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(jPanel4, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        backwardCheckBox.setText("\u904e\u53bb\u65b9\u5411\u306b");
        backwardCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        backwardCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel3.add(backwardCheckBox);

        searchButton.setMnemonic('Y');
        searchButton.setText("\u691c\u7d22(Y)");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        jPanel3.add(searchButton);

        abortButton.setMnemonic('N');
        abortButton.setText("\u4e2d\u6b62(N)");
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });

        jPanel3.add(abortButton);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
        dispose();
    }//GEN-LAST:event_abortButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        if ( isTermError() ) return;
        GregorianCalendar gcal1 = (GregorianCalendar)dateFTextField1.getValue();
        int body_id = planetComboBox.getSelectedBody();
        NatalChart chart = new NatalChart();
        int [] bodys = new int [] { body_id };
        chart.setTimePlace( natal, bodys );
        Body body = chart.getBody( body_id );
        System.out.println( body.toString() );
        List<GregorianCalendar> list = new ArrayList<GregorianCalendar>();
        int count = ((Integer)countSpinner.getValue()).intValue();
        boolean bkw = backwardCheckBox.isSelected();
        search( gcal1,count, body_id, body.lon, bkw, list );
        System.out.println("検出した日時の数 = " + list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            GregorianCalendar cal = list.get(i);
            System.out.printf( "%tF %tT\n", cal, cal );
        }
        createNatals( list, body_id, placePanel.getPlace() );
        dispose();
    }//GEN-LAST:event_searchButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReturnCalcDialog(new javax.swing.JFrame(),null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JCheckBox backwardCheckBox;
    private javax.swing.JSpinner countSpinner;
    private javax.swing.JFormattedTextField dateFTextField1;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel nameLabel;
    private to.tetramorph.starbase.PlacePanel placePanel;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables

}
