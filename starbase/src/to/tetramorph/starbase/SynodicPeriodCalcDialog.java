/*
 * ReturnCalcDialog.java
 *
 * Created on 2008/09/03, 17:20
 */

package to.tetramorph.starbase;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TCPlanetPlanet;
import swisseph.TransitCalculator;
import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.GregorianDateFormatter;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.lib.SearchResultReceiver;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.time.JDay;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.ParentWindow;
/**
 * トランジットの会合周期を計算し、検索結果として出力するダイアログ。
 * @author  大澤義鷹
 */
public class SynodicPeriodCalcDialog extends javax.swing.JDialog {
    SearchResultReceiver resultReceiver;
    //
    private static final Integer [] transitIDs = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
        URANUS,NEPTUNE,PLUTO,NODE,APOGEE,
        CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,NODE
    };

    /**
     *
     */
    public SynodicPeriodCalcDialog( java.awt.Frame parent,
                              SearchResultReceiver resultReceiver ) {
        super( parent, true );
        initComponents();
        this.resultReceiver = resultReceiver;
        //Place place = PrefUtils.getPlace(Conf.data, "DefaultTransitPlace");
        Place place = Config.usr.getPlace( "DefaultTransitPlace" );
        placePanel.setPlace( place );
        dateFTextField1.setFormatterFactory(
            new FormatterFactory( new GregorianDateFormatter() ) );

        SpinnerNumberModel model = new SpinnerNumberModel(4, 1, 999, 1);
        countSpinner.setModel( model );
        divSpinner.setModel( new SpinnerNumberModel( 1, 1, 72, 1 ) );
        // N,Tの天体コンボボックスにシンボルを登録
        planetComboBox1.setItems( transitIDs );
        planetComboBox2.setItems( transitIDs );
        //
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        if ( System.getProperty("java.version").startsWith("1.6") ) {
//            Image img = IconLoader.getImage("/resources/niwatori2.png");
            setIconImage(AppIcon.TITLE_BAR_ICON);
        }
        getRootPane().setDefaultButton(searchButton);
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("クローズボタン押された");
            }
        });
        setLocationRelativeTo( parent );
        // 月以外の惑星会合計算機能は今のところ使用不可にしておく
        tabbedPane.setEnabledAt( 1, false );
    }


    /**
     * 回帰計算ダイアログを可視化する。
     *
     *
     */
    public void showDialog() {
        dateFTextField1.setValue( new GregorianCalendar() );
//        setNatal( natal );
        // DefaultButtonの設定はdisposeされたときクリアされてしまうので、
        // 再セットが必要。
        getRootPane().setDefaultButton( searchButton );
        pack();
        setVisible(true);
        toFront();
    }

    // 検索期間の指定が不完全なときは、エラーダイアログを出す
    private boolean isTermError() {
        String errmsg = "会合時期検索のエラー";
        Object s = dateFTextField1.getValue();
        if ( s == null ) {
            JOptionPane.showMessageDialog(
                this,
                "検索開始日を指定して下さい。",
                errmsg,
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        Place place = placePanel.getPlace();
        if ( ! place.isCompletePlace() ) {
            JOptionPane.showMessageDialog(
                this,
                "観測地を指定して下さい。",
                errmsg,
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        if ( tabbedPane.getSelectedIndex() == 0 ) {
            boolean [] ck = getFaceCheckArray();
            if ( ! ( ck[0] | ck[1] | ck[2] | ck[3] ) ) {
                JOptionPane.showMessageDialog(
                    this,
                    "検出する月相を最低一つは選択してください。",
                    errmsg,
                    JOptionPane.ERROR_MESSAGE );
                return true;
            }
        }
        if ( tabbedPane.getSelectedIndex() == 1 ) {
            int p1 = planetComboBox1.getSelectedBody();
            int p2 = planetComboBox2.getSelectedBody();
            if ( p1 == p2 ) {
                JOptionPane.showMessageDialog(
                    this,
                    "二つとも同じ天体は指定できません。",
                    errmsg,
                    JOptionPane.ERROR_MESSAGE );
                return true;
            }
        }
        return false;
    }
    /**
     * ４つの月相の時期を検索開始日時より調べてresultListにセットする。
     * @param start_cal 検索開始日時
     * @param place 観測地
     * @param quant 検出回数
     * @param checked 4要素の配列で、新月、上弦、満月、下弦の順で、各月相を
     * resultListに加えるかどうかを指定する。checked[]がt,f,f,fなら、新月の時だけ
     * がリストに追加され。t,f,t,fなら、新月と満月のときだけ。
     * @param backward 過去に向かって検索したいときはtrueを指定。
     * @param resultList 結果の日時のリストを返す引数。
     */
    private void search( GregorianCalendar start_cal,
                           Place place,
                           int quant,
                           boolean [] checked,
                           boolean backward,
                           List<Natal> resultList ) {
        SwissEph eph = Ephemeris.getInstance().getSwissEph();
        TimeZone timeZone = start_cal.getTimeZone();
        double jday = JDay.get( start_cal );
//        System.out.println("calendar = " + String.format("%tF %tT",start_cal,start_cal));
//        System.out.println("start jday = " + jday );
//        System.out.println("backward = " + backward );
        //計算起点とする新月時刻を求める
        int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
        TransitCalculator tcal = new TCPlanetPlanet( eph,
                                                     SweConst.SE_MOON,
                                                     SweConst.SE_SUN,
                                                     flags, 0. );
        double deltaT = SweDate.getDeltaT( jday );
        double ET = jday + deltaT;
        double transitTime = eph.getTransitET( tcal, ET, ! backward ) - deltaT;
        double [] angles = { 0, 90, 180, 270 };
        String [] faceNames = { "新月","上弦","満月","下弦" };
        int i = 0;
        do {
            GregorianCalendar cal = JDay.getCalendar( transitTime, timeZone );
            if (  ( cal.after( start_cal ) && ! backward ) ||
                   ( cal.before( start_cal ) && backward  )    ){
                Natal n = new Natal();
                n.setName( faceNames[i] );
                n.setChartType( Natal.EVENT );
                n.setId( Natal.NEED_REGIST );
                n.setCalendar( cal, Natal.DATE_AND_TIME );
                n.setPlace( place );
                if ( checked[ i ] ) {
                    resultList.add( n );
                    quant--;
                }
            }
            jday = transitTime;
            int sign = backward ? -1 : 1;
            i = ( i + sign ) & 3;
            tcal.setOffset( angles[i] );
            deltaT = SweDate.getDeltaT( jday );
            ET = jday + deltaT;
            transitTime = eph.getTransitET( tcal, ET, backward ) - deltaT;
        } while ( quant > 0 );
    }
    /**
     * ４つの月相の時期を検索開始日時より調べてresultListにセットする。
     * @param start_cal 検索開始日時
     * @param place 観測地
     * @param quant 検出回数
     * @param backward 過去に向かって検索したいときはtrueを指定。
     * @param resultList 結果の日時のリストを返す引数。
     */
    private void search2( GregorianCalendar start_cal,
                            Place place,
                            int quant,
                            int body_id1,
                            int body_id2,
                            List<Double> angles,
                            boolean backward,
                            List<Natal> resultList ) {
        SwissEph eph = Ephemeris.getInstance().getSwissEph();
        TimeZone timeZone = start_cal.getTimeZone();
        double jday = JDay.get( start_cal );
        //計算起点とする合の時刻を求める
        int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
        TransitCalculator tcal = new TCPlanetPlanet( eph,
                                                     body_id1,
                                                     body_id2,
                                                     flags, 0. );
        double deltaT = SweDate.getDeltaT( jday );
//        double ET = jday + deltaT;
        double transitTime = eph.getTransitUT( tcal, jday, ! backward );
        System.out.println("deltaT = " + deltaT );
        System.out.println( "First transitTime = " + transitTime );
        int i = 0;
        do {
            GregorianCalendar cal = JDay.getCalendar(
                transitTime - SweDate.getDeltaT( transitTime ), timeZone );
            if (  ( cal.after( start_cal ) && ! backward ) ||
                   ( cal.before( start_cal ) && backward  )    ){
                Natal n = new Natal();
                String title = String.format( "%s−%s・離角%4.1f",
                                              PLANET_NAMES[ body_id1],
                                              PLANET_NAMES[ body_id2],
                                              angles.get(i));
                n.setName( title );
                n.setChartType( Natal.EVENT );
                n.setId( Natal.NEED_REGIST );
                n.setCalendar( cal, Natal.DATE_AND_TIME );
                n.setPlace( place );
                resultList.add( n );
                quant--;
            }
            int sign = backward ? -1 : 1;
            i += sign;
            if ( i < 0 ) i = angles.size() - 1;
            else if ( i >= angles.size() ) i = 0;
            tcal.setOffset( angles.get(i) );
            double temp = 0;
            for(;;) {
                temp = eph.getTransitUT( tcal, transitTime, backward );
                System.out.println( "transitTime = " + transitTime );
                if ( temp == transitTime ) {
                    transitTime += 1.0;
                } else {
                    transitTime = temp;
                    break;
                }
            }
//                double offset = tcal.getOffset();
//                double skipAngle = (360. / angles.size() / 2.) * sign;
//                System.out.println("skipAngle = " + skipAngle );
//                tcal.setOffset( offset + skipAngle );
//                transitTime = eph.getTransitUT( tcal, transitTime, backward );
//                tcal.setOffset( offset );
//                transitTime = eph.getTransitUT( tcal, transitTime, backward );
//            } else {
//                transitTime = temp;
//            }
        } while ( quant > 0 );
        dispose();
    }

    private boolean [] getFaceCheckArray() {
        boolean [] checked = { moonfaceCheckBox1.isSelected(),
                                moonfaceCheckBox2.isSelected(),
                                moonfaceCheckBox3.isSelected(),
                                moonfaceCheckBox4.isSelected() };
        return checked;
    }

    public String toString() {
        return "会合時期検索";
    }

    private GregorianCalendar getStartDate() {
        GregorianCalendar gcal = (GregorianCalendar)dateFTextField1.getValue();
        TimeZone tz = placePanel.getTimeZone();
        gcal.setTimeZone( tz );
        gcal.set( Calendar.HOUR_OF_DAY, 12 );
        gcal.set( Calendar.MINUTE, 0 );
        gcal.set( Calendar.SECOND, 0 );
        gcal.set( Calendar.MILLISECOND, 0 );
        return gcal;
    }

    // 月相タブが選択されているときの検索
    private void searchMoonFace() {
        if ( isTermError() ) return;
        GregorianCalendar gcal = getStartDate();
        List<Natal> list = new ArrayList<Natal>();
        int count = ((Integer)countSpinner.getValue()).intValue();
        boolean bkw = backwardCheckBox.isSelected();
        Place place = placePanel.getPlace();
        boolean [] checked = getFaceCheckArray();
        search( gcal, place, count, checked, bkw, list );
        String title = "月相";
        String tabName = "月相";
        resultReceiver.write( new SearchResult( list ,
                                                tabName,
                                                title ,
                                                "(未登録)",
                                                toString()) );
    }
    /**
     * スピナーの分割数に応じて、角度を列挙した配列を作成して返す。
     */
    private List<Double> getDivAngles() {
        List<Double> list = new ArrayList<Double>();
        int div = ((Integer)divSpinner.getValue()).intValue();
        double angle = 360.0 / div;
        double temp = 0;
        while ( div > 0 ) {
            list.add( temp );
            temp += angle;
            div--;
        }
        for ( int i = 0; i < list.size(); i++ ) {
            System.out.print( list.get(i) + "," );
        }
        System.out.println();
        return list;
    }
    // 会合周期タブが選択されているときの検索
    private void searchSynodicPeriod() {
        if ( isTermError() ) return;
        GregorianCalendar gcal = getStartDate();
        List<Natal> list = new ArrayList<Natal>();
        int count = ((Integer)countSpinner.getValue()).intValue();
        boolean bkw = backwardCheckBox.isSelected();
        Place place = placePanel.getPlace();
        boolean [] checked = getFaceCheckArray();
        int p1 = planetComboBox1.getSelectedBody();
        int p2 = planetComboBox2.getSelectedBody();
        List<Double> angles = getDivAngles();
        search2( gcal, place, count, p1, p2, angles, bkw, list );
        String title = String.format( "%s−%sの離角",
                                      PLANET_NAMES[ p1],
                                      PLANET_NAMES[ p2] );
        String tabName = "会合";
        resultReceiver.write( new SearchResult( list ,
                                                tabName,
                                                title ,
                                                "(未登録)",
                                                toString()) );
        dispose();
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
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel8;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        facePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        moonfaceCheckBox1 = new javax.swing.JCheckBox();
        moonfaceCheckBox2 = new javax.swing.JCheckBox();
        moonfaceCheckBox3 = new javax.swing.JCheckBox();
        moonfaceCheckBox4 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        planetComboBox1 = new to.tetramorph.starbase.widget.PlanetComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        planetComboBox2 = new to.tetramorph.starbase.widget.PlanetComboBox();
        angleLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        divSpinner = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        placePanel = new to.tetramorph.starbase.PlacePanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        countSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        dateFTextField1 = new javax.swing.JFormattedTextField();
        jPanel3 = new javax.swing.JPanel();
        backwardCheckBox = new javax.swing.JCheckBox();
        searchButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u4f1a\u5408\u6642\u671f\u691c\u7d22");
        setResizable(false);
        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 16, 1, 16));
        facePanel.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("\u691c\u51fa\u5bfe\u8c61");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        facePanel.add(jLabel5, gridBagConstraints);

        moonfaceCheckBox1.setSelected(true);
        moonfaceCheckBox1.setText("\u65b0\u6708");
        moonfaceCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moonfaceCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        facePanel.add(moonfaceCheckBox1, gridBagConstraints);

        moonfaceCheckBox2.setSelected(true);
        moonfaceCheckBox2.setText("\u4e0a\u5f26");
        moonfaceCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moonfaceCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        facePanel.add(moonfaceCheckBox2, gridBagConstraints);

        moonfaceCheckBox3.setSelected(true);
        moonfaceCheckBox3.setText("\u6e80\u6708");
        moonfaceCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moonfaceCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        facePanel.add(moonfaceCheckBox3, gridBagConstraints);

        moonfaceCheckBox4.setSelected(true);
        moonfaceCheckBox4.setText("\u4e0b\u5f26");
        moonfaceCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moonfaceCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        facePanel.add(moonfaceCheckBox4, gridBagConstraints);

        tabbedPane.addTab("\u6708\u76f8\u691c\u7d22", facePanel);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(planetComboBox1, gridBagConstraints);

        jLabel3.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c81");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel8.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c82");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(planetComboBox2, gridBagConstraints);

        angleLabel.setText("      ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(angleLabel, gridBagConstraints);

        jLabel6.setText("\u5186\u5206\u5272\u6570");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel6, gridBagConstraints);

        divSpinner.setPreferredSize(new java.awt.Dimension(40, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        jPanel2.add(divSpinner, gridBagConstraints);

        tabbedPane.addTab("\u4f1a\u5408\u691c\u7d22", jPanel2);

        jPanel1.add(tabbedPane);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(placePanel, gridBagConstraints);

        jLabel4.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8\u89b3\u6e2c\u5730");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 4, 0);
        jPanel4.add(jLabel4, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("\u691c\u51fa\u56de\u6570");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel6.add(jLabel2, gridBagConstraints);

        countSpinner.setPreferredSize(new java.awt.Dimension(40, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        jPanel6.add(countSpinner, gridBagConstraints);

        jLabel1.setText("\u691c\u7d22\u958b\u59cb\u65e5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel6.add(jLabel1, gridBagConstraints);

        dateFTextField1.setColumns(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel6.add(dateFTextField1, gridBagConstraints);

        jPanel4.add(jPanel6, new java.awt.GridBagConstraints());

        jPanel1.add(jPanel4);

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
        if ( tabbedPane.getSelectedIndex() == 0 )
            searchMoonFace();
        else
            searchSynodicPeriod();
    }//GEN-LAST:event_searchButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AspectCalcDialog acd = new AspectCalcDialog(new javax.swing.JFrame(),null);
                acd.pack();
                acd.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JLabel angleLabel;
    private javax.swing.JCheckBox backwardCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSpinner countSpinner;
    private javax.swing.JFormattedTextField dateFTextField1;
    private javax.swing.JSpinner divSpinner;
    private javax.swing.JPanel facePanel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JCheckBox moonfaceCheckBox1;
    private javax.swing.JCheckBox moonfaceCheckBox2;
    private javax.swing.JCheckBox moonfaceCheckBox3;
    private javax.swing.JCheckBox moonfaceCheckBox4;
    private to.tetramorph.starbase.PlacePanel placePanel;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox1;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox2;
    private javax.swing.JButton searchButton;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

}
