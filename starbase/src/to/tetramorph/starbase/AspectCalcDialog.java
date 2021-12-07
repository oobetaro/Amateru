/*
 * ReturnCalcDialog.java
 *
 * Created on 2008/09/03, 17:20
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.util.WindowMoveHandler;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
 * ネイタルへとトランジットのアスペクト検索ダイアログ。
 * @author  大澤義鷹
 */
public class AspectCalcDialog extends javax.swing.JDialog {
    SearchResultReceiver resultReceiver;
    Natal natal;
    Place place;
    WindowMoveHandler winmove;

    //ネイタルコンボにセットする天体シンボル
    private static final Integer [] natalIDs = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
        URANUS,NEPTUNE,PLUTO,NODE,APOGEE,
        CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,NODE,
        AC,DC,MC,IC,SOUTH_NODE,VERTEX,ANTI_VERTEX,ANTI_APOGEE
    };
    //
    private static final Integer [] transitIDs = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
        URANUS,NEPTUNE,PLUTO,NODE,APOGEE,
        CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,NODE,
        VERTEX
    };
    /**
     *
     */
    public AspectCalcDialog( java.awt.Frame parent,
                              SearchResultReceiver resultReceiver ) {
        super( parent, true );
        initComponents();
        this.resultReceiver = resultReceiver;
        buttonGroup1.add( radioButton1 );
        buttonGroup1.add( radioButton2 );
        dateFTextField1.setFormatterFactory(
            new FormatterFactory( new GregorianDateFormatter() ) );
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 99, 1);
        countSpinner.setModel( model );
        // N,Tの天体コンボボックスにシンボルを登録
        planetComboBox.setItems( natalIDs );
        planetComboBox2.setItems( transitIDs );
        // アスペクトの指定方式(シンボル/数値指定)をラジオボタンで切り替える
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                boolean b = evt.getSource() == radioButton1;
                angleTextField.setEnabled( ! b );
                aspectComboBox1.setEnabled( b );
                setDoubleSign();
            }
        };
        radioButton1.addActionListener( al );
        radioButton2.addActionListener( al );
        //
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        // 座相で合とオポ以外なら検出回数×２の表示を出す。ちがうときは消す。
        aspectComboBox1.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                setDoubleSign();
            }
        });
        angleTextField.addFocusListener( new FocusListener() {
           public void focusGained( FocusEvent e ) { }
           public void focusLost( FocusEvent e) {
               setDoubleSign();
           }
        });
        if ( System.getProperty("java.version").startsWith("1.6") ) {
            //Image img = IconLoader.getImage("/resources/niwatori2.png");
            setIconImage(AppIcon.TITLE_BAR_ICON);
        }
        getRootPane().setDefaultButton(searchButton);
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("クローズボタン押された");
            }
        });
        //winmove = new WindowMoveHandler( "AspectCalcDialog.BOUNDS", this );
        //addComponentListener( winmove );
        //winmove.setBounds();
        setLocationRelativeTo( parent );
        planetComboBox.addActionListener( new PlanetSelectionListener() );
    }
    // ×2の表示
    private void setDoubleSign() {
        double a = getSelectedAspect();
        if ( a == 0 || a == 180 ) multiLabel.setText("");
        else multiLabel.setText("×2");
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

    /**
     *
     * 回帰計算する出生データをセットする。
     */
    public void setNatal( Natal natal ) {
        this.natal = natal;
        nameLabel.setText( natal.getName() );
        //lastDate();
        Place place = natal.getTransitPlace();
        if ( place == null ) place = natal.getPlace();
        if ( place != null ) placePanel.setPlace( place );
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
                "アスペクト時期の検索のエラー",
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        Place place = placePanel.getPlace();
        if ( ! place.isCompletePlace() ) {
            JOptionPane.showMessageDialog(
                this,
                "観測地を指定して下さい。",
                "アスペクト時期の検索のエラー",
                JOptionPane.ERROR_MESSAGE );
            return true;
        }
        if ( getSelectedAspect() < 0 ) {
            JOptionPane.showMessageDialog(
                this,
                "離角(0〜180度まで)を正しく入力してください。",
                "アスペクト時期の検索のエラー",
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
        System.out.println("calendar = " + String.format("%tF %tT",begin_gcal,begin_gcal));
        System.out.println("start jday = " + jday );
        System.out.println("target lon = " + lon );
        System.out.println("body   id  = " + body_id );
        System.out.println("backward = " + backward );
        int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
        TransitCalculator tcal = new TCPlanet( eph, body_id, flags, lon );
        while ( hit_count > 0 ) {
            double deltaT = SweDate.getDeltaT( jday );
            double ET = jday + deltaT;
            double transitTime = eph.getTransitET( tcal, ET, backward ) - deltaT;
            GregorianCalendar gcal2 = JDay.getCalendar( transitTime, timeZone );
            resultList.add( gcal2 );
            jday = transitTime;
            hit_count--;
        }
    }
    /**
     * angleが端数のときは"xx.x"という形式で、端数がないときは"xx"という形式で
     * 値を数字にして返す。
     */
    private String getAspectAngle( double angle ) {
        int i = (int)angle;
        if ( (angle - i) > 0.0 ) {
            return String.format("%3.1f", angle );
        }
        return String.format("%d", i);
    }

    private void createNatals( List<GregorianCalendar> list,
                                 int natal_body_id,
                                 int transit_body_id,
                                 double angle,
                                 Place place ) {
        List<Natal> natalList = new ArrayList<Natal>();
        String title = String.format( "%s Ｎ%sとＴ%sの%s度",
                                      natal.getName(),
                                      Const.PLANET_NAMES[ natal_body_id ],
                                      Const.PLANET_NAMES[ transit_body_id ],
                                      getAspectAngle( angle ) );
        String name = natal.getName().split(" |　")[0];
        String tabName = String.format( "%s N%s-T%s",
                                        name,
                                        Const.PLANET_NAMES[ natal_body_id ],
                                        Const.PLANET_NAMES[ transit_body_id ] );
        for ( int i = 0; i < list.size(); i++ ) {
            Natal n = new Natal();
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
                                                toString()) );
    }
    // 選択または入力されたアスペクトの離角を返す。
    private double getSelectedAspect() {
        double angle = 0.0;
        if ( radioButton1.isSelected() ) {
            int i = (Integer)aspectComboBox1.getSelectedItem();
            angle = Const.ASPECT_ANGLES[i];
        } else {
            try {
                angle = Double.parseDouble( angleTextField.getText() );
            } catch ( NumberFormatException e ) {
                angle = -1.0;
            }
        }
        if ( angle > 180 ) angle = -1.0;
        return angle;
    }

    public String toString() {
        return "アスペクト時期検索";
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
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel8;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();
        jLabel2 = new javax.swing.JLabel();
        dateFTextField1 = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        planetComboBox2 = new to.tetramorph.starbase.widget.PlanetComboBox();
        aspectComboBox1 = new to.tetramorph.starbase.widget.AspectComboBox();
        radioButton1 = new javax.swing.JRadioButton();
        radioButton2 = new javax.swing.JRadioButton();
        angleTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        countSpinner = new javax.swing.JSpinner();
        multiLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        placePanel = new to.tetramorph.starbase.PlacePanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        backwardCheckBox = new javax.swing.JCheckBox();
        searchButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u30a2\u30b9\u30da\u30af\u30c8\u6642\u671f\u691c\u7d22");
        setResizable(false);
        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 16, 1, 16));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(planetComboBox, gridBagConstraints);

        jLabel2.setText("\u691c\u51fa\u56de\u6570");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel2, gridBagConstraints);

        dateFTextField1.setColumns(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(dateFTextField1, gridBagConstraints);

        jLabel1.setText("\u691c\u7d22\u958b\u59cb\u65e5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel3.setText("\u30cd\u30a4\u30bf\u30eb");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel8.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(planetComboBox2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(aspectComboBox1, gridBagConstraints);

        radioButton1.setSelected(true);
        radioButton1.setText("\u30a2\u30b9\u30da\u30af\u30c8");
        radioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(radioButton1, gridBagConstraints);

        radioButton2.setText("\u96e2\u89d2(\u5ea6)");
        radioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(radioButton2, gridBagConstraints);

        angleTextField.setColumns(4);
        angleTextField.setText("0");
        angleTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(angleTextField, gridBagConstraints);

        jLabel5.setText("\u540d\u524d\uff1a");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jLabel5, gridBagConstraints);

        nameLabel.setText("name                ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(nameLabel, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        countSpinner.setPreferredSize(new java.awt.Dimension(40, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel5.add(countSpinner, gridBagConstraints);

        multiLabel.setText("      ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel5.add(multiLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(jPanel5, gridBagConstraints);

        jPanel1.add(jPanel2);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel4.add(placePanel, gridBagConstraints);

        jLabel4.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8\u89b3\u6e2c\u5730");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel4.add(jLabel4, gridBagConstraints);

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
        if ( isTermError() ) return;
        GregorianCalendar gcal = (GregorianCalendar)dateFTextField1.getValue();
        gcal.set( Calendar.HOUR_OF_DAY, 12 );
        gcal.set( Calendar.MINUTE, 0 );
        gcal.set( Calendar.SECOND, 0 );
        gcal.set( Calendar.MILLISECOND, 0 );
        int body_id = planetComboBox.getSelectedBody();
        NatalChart chart = new NatalChart();
        int [] bodys = new int [] { body_id };
        //natal = TestConst.getMyNatal();       //てすとでおます
        chart.setTimePlace( natal, bodys );
        Body body = chart.getBody( body_id ); //ネイタル求まる
        System.out.println( body.toString() );
        List<GregorianCalendar> list = new ArrayList<GregorianCalendar>();
        int count = ((Integer)countSpinner.getValue()).intValue();
        boolean bkw = backwardCheckBox.isSelected();
        int body_id2 = planetComboBox2.getSelectedBody();
        double aa = getSelectedAspect();
        //System.out.println( "Aspect Angle = " + getSelectedAspect() );
        //0度,180度が発生するのは円周上に1点しか存在しないが、他の座相は
        //2点発生する。そのため処理を分岐させている。
        System.out.println("aa = " + aa);
        if ( aa == 0 || aa == 180 ) {
            double lon = ( body.lon + aa ) % 360.0;
            search( gcal, count, body_id2, lon, bkw, list );
        } else {
            double lon = ( body.lon - aa ) % 360.0;
            search( gcal, count, body_id2, lon, bkw, list );
            lon = ( body.lon + aa ) % 360.0;
            search( gcal, count, body_id2, lon, bkw, list );
        }
        createNatals( list, body_id, body_id2, aa, placePanel.getPlace() );
        dispose();
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
    private javax.swing.JTextField angleTextField;
    private to.tetramorph.starbase.widget.AspectComboBox aspectComboBox1;
    private javax.swing.JCheckBox backwardCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSpinner countSpinner;
    private javax.swing.JFormattedTextField dateFTextField1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel multiLabel;
    private javax.swing.JLabel nameLabel;
    private to.tetramorph.starbase.PlacePanel placePanel;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox2;
    private javax.swing.JRadioButton radioButton1;
    private javax.swing.JRadioButton radioButton2;
    private javax.swing.JButton searchButton;
    // End of variables declaration//GEN-END:variables

}
