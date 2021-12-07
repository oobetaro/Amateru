/*
 * EnneagramCalendar2.java
 *
 * Created on 2007/01/14, 15:22
 */

package to.tetramorph.astrocalendar;

import to.tetramorph.almanac.Almanac;
import to.tetramorph.almanac.MoonFace;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import static java.lang.Math.*;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JColorChooser;
import javax.swing.Timer;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.tzdialog.TimeZoneDialog;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.MaximumWindowBounds;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * エニアグラムカレンダーバージョン1.2.0
 * 全面的に作り直して、いろいろな描画設定ができるようにした。
 * 2011-07-28 設定ファイルのフォルダ参照法を修正。オーバーライドアノテーションを追加。
 */
public final class EnneagramCalendar2 extends javax.swing.JFrame {
    //一番細い線
    static final Stroke pleanStroke = new BasicStroke(1f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND);
    //太線
    static final Stroke boldStroke = new BasicStroke(4f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND);
    //細実線
    static final Stroke solidStroke = new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND);
    //破線
    static final Stroke brokenStroke = new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_BEVEL,3f,new float [] {8f,10f,8f,10f},5f);
    static final String [] NUMBERS = {
        "０","１","２","３","４","５","６","７","８","９" };
    static final String [] YOUBI = {
        "無効","日","月","火","水","木","金","土" };
    static final String [] DOREMI = {
        "ド","レ","ミ"," ","ファ","ソ"," ","ラ","シ","ド" };
    // 7天体の文字ｺｰﾄﾞ
    static final String [] PLANETS = {
        " ",
        "" + BODY_CHARS[MOON],"" + BODY_CHARS[MARS],
        " ",
        "" + BODY_CHARS[MERCURY],"" + BODY_CHARS[JUPITER],
        " ",
        "" + BODY_CHARS[VENUS],"" + BODY_CHARS[SATURN],"" + BODY_CHARS[SUN]
    };
    // 10天体の文字ｺｰﾄﾞ
    static final String [] PLANETS2 = {
        " ",
        "" + BODY_CHARS[MERCURY],"" + BODY_CHARS[VENUS],"" + BODY_CHARS[SUN],
        "" + BODY_CHARS[MARS],"" + BODY_CHARS[JUPITER],"" + BODY_CHARS[SATURN],
        "" + BODY_CHARS[URANUS],"" + BODY_CHARS[NEPTUNE],
        "" + BODY_CHARS[PLUTO] + BODY_CHARS[MOON]
    };
    static final String [] MOONFACE = { "新月","上弦","満月","下弦" };
    static final Font astroFont = AstroFont.getFont(15f);        // 占星術ﾌｫﾝﾄ
    TimeZone zone;                                    // 使用しているタイムゾーン
    Calendar cal;        //現在時刻を表すカレンダー(タイムゾーン保存用というべきか)
    List<MoonFace> faceList;                                     // 天文暦データ
    Properties prop;                            // ファイルに保存されるプロパティ
    //プロパティ保存用ファイル
    File masterPropFile = new File( System.getProperty("app.properties"),
                                      "EnneagramCalendar.properties");
    // draw()の引数で日付文字列格納用 [0]=エニ9,[1]=エニ1,
    String [] dateStrings = { " "," "," "," "," "," "," "," "," " };
    int pointer = 0;                                //天文暦を指し示すポインター
    int hed = 0;             //一番未来の日付が入っている円上のオフセット値(0-8)
    Calendar cal2;                          //曜日のエニアグラム表示用カレンダー
    MainPanel mainPanel = new MainPanel();              //ｴﾆｱｸﾞﾗﾑを描画するﾊﾟﾈﾙ
    // 日づけを進めたり戻したり現在に合わせたりする機能がひとまとめにされている
    // ｸﾗｽで、youbiCalendarHandlerやmoonCalendarHandlerが代入される変数。
    DateHandler dateHandler;
    // DateHandlerを実装したもので曜日ｶﾚﾝﾀﾞｰを前後させるのに使用
    YoubiCalendarHandler youbiCalendarHandler = new YoubiCalendarHandler();
    // DateHandlerを実装したものでﾑｰﾝﾌｪｲｽｶﾚﾝﾀﾞｰを前後させるのに使用
    MoonCalendarHandler moonCalendarHandler = new MoonCalendarHandler();

    Calendar nowcal;
    Timer clockTimer;

    public EnneagramCalendar2() {
        this(true);
    }
    /**
     * コンストラクタ
     */
    public EnneagramCalendar2(boolean singleExec) {
        initComponents();
        if(! singleExec)
            setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

        add(mainPanel,BorderLayout.CENTER);
        pack();
        setIconImage(IconLoader.getImage("/resources/images/enneagram.gif"));
        //プロパティをロードして環境を復元。
        prop = new Properties();
        if ( masterPropFile.exists() )
            FileTools.loadProperties(prop,masterPropFile);
        Rectangle r = this.getBounds();
        setBounds(
            Integer.parseInt(prop.getProperty("frame.x","0")),
            Integer.parseInt(prop.getProperty("frame.y","0")),
            Integer.parseInt(prop.getProperty("frame.width","400")),
            Integer.parseInt(prop.getProperty("frame.height","400"))
            );
        this.setLocationRelativeTo(null);

        //プロパティからタイムゾーンを取得し、カレンダーとzoneLabelにセット
        zone = TimeZone.getTimeZone(
            prop.getProperty("TimeZoneID",TimeZone.getDefault().getID()));
        boolean daylight = zone.useDaylightTime();
        zoneLabel.setText(zone.getDisplayName( daylight, TimeZone.SHORT,
                                                         Locale.getDefault()));
        //天文暦を読込
        faceList = Almanac.getEnneaAlmanac(zone);
        cal = Calendar.getInstance(zone);
        cal2 = (Calendar)cal.clone();
        //
        init();
        if(youbiRadioButton.isSelected())
            dateHandler = youbiCalendarHandler;
        else
            dateHandler = moonCalendarHandler;

        this.addMouseWheelListener(new MouseWheelHandler());
        this.addWindowFocusListener(new MaximumWindowBounds(this));
        dateHandler.now();
        startClock();
    }
    // 文字列の数字をintに変換しそこからColorｵﾌﾞｼﾞｪｸﾄを作成する
    private Color getColor(String value) {
        return new Color(Integer.parseInt(value));
    }
    // ﾌﾟﾛﾊﾟﾃｨの読込やﾗｼﾞｵﾎﾞﾀﾝやﾁｪｯｸﾎﾞｯｸｽの値を再現する
    void init() {
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                repaint();
            }
        };
        //ﾊﾟﾗﾀﾒｰを増やしたときは、ﾘｽﾅの登録、ﾗｼﾞｵﾎﾞﾀﾝならButtonGropuへの追加、
        //ﾌﾟﾛﾊﾟﾃｨの読込とセーブ(saveProperties()で)を忘れずに。けっこう多い。
        circleCheckBox.addActionListener(l);
        line1CheckBox.addActionListener(l);
        line2CheckBox.addActionListener(l);
        trainCheckBox.addActionListener(l);
        numberRadioButton.addActionListener(l);
        doremiRadioButton.addActionListener(l);
        planetRadioButton1.addActionListener(l);
        planetRadioButton2.addActionListener(l);

        noneRadioButton.addActionListener(l);
        configButtonGroup.add(moonfaceRadioButton);
        configButtonGroup.add(youbiRadioButton);
        configButtonGroup.add(numberRadioButton);
        configButtonGroup.add(doremiRadioButton);
        configButtonGroup.add(noneRadioButton);
        configButtonGroup.add(planetRadioButton1);
        configButtonGroup.add(planetRadioButton2);
        circleCheckBox.setSelected(prop.getProperty("circleCheckBox","true").equals("true"));
        line1CheckBox.setSelected(prop.getProperty("line1CheckBox","true").equals("true"));
        line2CheckBox.setSelected(prop.getProperty("line2CheckBox","true").equals("true"));
        trainCheckBox.setSelected(prop.getProperty("trainCheckBox","true").equals("true"));
        displayTimeCheckBox.setSelected(prop.getProperty("displayTimeCheckBox","false").equals("true"));
        moonfaceRadioButton.setSelected(prop.getProperty("moonfaceRadioButton","true").equals("true"));
        youbiRadioButton.setSelected(prop.getProperty("youbiRadioButton","false").equals("true"));
        numberRadioButton.setSelected(prop.getProperty("numberRadioButton","false").equals("true"));
        doremiRadioButton.setSelected(prop.getProperty("doremiRadioButton","false").equals("true"));
        planetRadioButton1.setSelected(prop.getProperty("planetRadioButton1","false").equals("true"));
        planetRadioButton2.setSelected(prop.getProperty("planetRadioButton2","false").equals("true"));

        noneRadioButton.setSelected(prop.getProperty("noneRadioButton","false").equals("true"));
        mainPanel.setBackground(getColor(prop.getProperty("bgColor","" + Color.WHITE.getRGB())));
        mainPanel.setForeground(getColor(prop.getProperty("fgColor","" + Color.BLACK.getRGB())));
    }
    // ﾌｧｲﾙにﾌﾟﾛﾊﾟﾃｨを保存する。ﾌﾚｰﾑをｸﾛｰｽﾞしたときに呼び出される。
    void saveProperties() {
        Rectangle rect = getBounds(null);
        prop.setProperty("frame.x",""+rect.x);
        prop.setProperty("frame.y",""+rect.y);
        prop.setProperty("frame.width",""+rect.width);
        prop.setProperty("frame.height",""+rect.height);
        prop.setProperty("circleCheckBox","" + circleCheckBox.isSelected());
        prop.setProperty("line1CheckBox","" + line1CheckBox.isSelected());
        prop.setProperty("line2CheckBox","" + line2CheckBox.isSelected());
        prop.setProperty("trainCheckBox","" + trainCheckBox.isSelected());
        prop.setProperty("displayTimeCheckBox","" + displayTimeCheckBox.isSelected());
        prop.setProperty("moonfaceRadioButton","" + moonfaceRadioButton.isSelected());
        prop.setProperty("youbiRadioButton","" + youbiRadioButton.isSelected());
        prop.setProperty("numberRadioButton","" + numberRadioButton.isSelected());
        prop.setProperty("doremiRadioButton",""+doremiRadioButton.isSelected());
        prop.setProperty("planetRadioButton1","" + planetRadioButton1.isSelected());
        prop.setProperty("planetRadioButton2","" + planetRadioButton1.isSelected());
        prop.setProperty("noneRadioButton","" + noneRadioButton.isSelected());
        prop.setProperty("bgColor",""+mainPanel.getBackground().getRGB());
        prop.setProperty("fgColor",""+mainPanel.getForeground().getRGB());
        FileTools.saveProperties(prop,masterPropFile,"EnneagramCalendar Configuration");
    }

    void startClock() {
        //時計
        clockTimer = new javax.swing.Timer(1000,new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Calendar cal = Calendar.getInstance(zone);
                clockLabel.setText( String.format(
                    Locale.getDefault(),"%tB %td %ta, %tp %tl:%tM:%tS",
                                         cal,cal,cal,cal,cal,cal,cal));
                clockLabel.revalidate();
            }
        });
        clockTimer.start(); //stop()はシャットダウンフックの中で行う。
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clockTimer.stop();
            }
        });
    }
    // ｴﾆｱｸﾞﾗﾑを描画するﾊﾟﾈﾙだが、
    // していることはJava2Dの準備をしたあとdraw()を呼び出すだけ
    class MainPanel extends JPanel implements Printable {
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D)graphics;
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rect = getBounds();
            draw(g,rect.width,rect.height);
        }
        @Override
        public int print( Graphics g1, PageFormat pf, int pageIndex ) {
            Graphics2D g = (Graphics2D)g1;
            if ( pageIndex == 0 ) {
                draw( g, pf.getImageableWidth(), pf.getImageableHeight() );
                g.dispose();
                return Printable.PAGE_EXISTS;
            } else return Printable.NO_SUCH_PAGE;
        }
    }

    // エニアグラムを描画
    //  ﾌｨｰﾙﾄﾞ変数dateStrings[]を引数として参照している。
    void draw( Graphics2D g, double width, double height ) {
        Point2D.Double [] point = new Point2D.Double[10];
        Point2D.Double [] datePoint = new Point2D.Double[10];
        Point2D.Double [] facePoints = new Point2D.Double[4];
        double centerX = width/2;
        double centerY = height * 0.5;
        double radius = (width > height ? height : width) * 0.78d;
        double dateRadius = radius * 1.1f;
        int ofx = 0,ofy = 0;
        //九つの点座標と九つの日付用の点座標を算出
        int n=1;
        for(double a=40; a<=360d; a += 40d,n++) {
            point[n] = new Point2D.Double( //図形用
                round(cos(PI/180.0 * (a-90) ) * radius/2d + centerX),
                round(sin(PI/180.0 * (a-90) ) * radius/2d + centerY));
            datePoint[n] = new Point2D.Double( //日付用
                round(cos(PI/180.0 * (a-90) ) * dateRadius/2d + centerX + ofx),
                round(sin(PI/180.0 * (a-90) ) * dateRadius/2d + centerY + ofy));
        }
        //新月、上弦、満月、下弦の表示座標
        n=0;
        double faceRadius = radius * 1.15;
        for(double a=0; a<=270; a+= 90,n++) {
            facePoints[n] = new Point2D.Double(
                round(cos(PI/180.0 * (a-90) ) * faceRadius/2d + centerX),
                round(sin(PI/180.0 * (a-90) ) * faceRadius/2d + centerY));
        }
        //円の表示
        if(circleCheckBox.isSelected()) {
            Shape circle = new Ellipse2D.Double(centerX-radius/2d,centerY-radius/2d,radius,radius);
            //g.translate(zx,zy);
            g.setStroke(boldStroke);
            g.draw(circle);
        }
        //円の中の実線ライン1
        if(line1CheckBox.isSelected()) {
            g.setStroke(solidStroke);
            g.draw(new Line2D.Double(point[1],point[4]));
            g.draw(new Line2D.Double(point[1],point[7]));
            g.draw(new Line2D.Double(point[7],point[5]));
        }
        //円の中の実線ライン2
        if(line2CheckBox.isSelected()) {
            g.setStroke(solidStroke);
            g.draw(new Line2D.Double(point[2],point[8]));
            g.draw(new Line2D.Double(point[2],point[4]));
            g.draw(new Line2D.Double(point[5],point[8]));
        }
        //円の中の点線の三角ライン
        if(trainCheckBox.isSelected()) {
            g.setStroke(brokenStroke);
            g.draw(new Line2D.Double(point[9],point[3]));
            g.draw(new Line2D.Double(point[3],point[6]));
            g.draw(new Line2D.Double(point[6],point[9]));
        }
        if(moonfaceRadioButton.isSelected() || youbiRadioButton.isSelected()) {
            FontRenderContext render = g.getFontRenderContext();
            //それぞれの日付文字列の横幅をadv[]に求める
            double []adv = new double[dateStrings.length];
            TextLayout tl = new TextLayout(dateStrings[0],getFont(),render);
            double asc = tl.getAscent()/2d; //高さは共通で良し
            for(int i=0; i<dateStrings.length; i++) {
                tl = new TextLayout(dateStrings[i],getFont(),render);
                adv[i] = tl.getVisibleAdvance();
            }
            //エニアグラムのまわりに日付文字列を描画
            g.drawString(dateStrings[0],round(datePoint[9].getX()-adv[0]/2d),round(datePoint[9].getY()+asc));
            g.drawString(dateStrings[1],round(datePoint[1].getX()),round(datePoint[1].getY()+asc));
            g.drawString(dateStrings[2],round(datePoint[2].getX()),round(datePoint[2].getY()+asc));
            g.drawString(dateStrings[3],round(datePoint[3].getX()),round(datePoint[3].getY()+asc));
            g.drawString(dateStrings[4],round(datePoint[4].getX()),round(datePoint[4].getY()+asc));
            g.drawString(dateStrings[5],round(datePoint[5].getX()-adv[5]),round(datePoint[5].getY()+asc));
            g.drawString(dateStrings[6],round(datePoint[6].getX()-adv[6]),round(datePoint[6].getY()+asc));
            g.drawString(dateStrings[7],round(datePoint[7].getX()-adv[7]),round(datePoint[7].getY()+asc));
            g.drawString(dateStrings[8],round(datePoint[8].getX()-adv[8]),round(datePoint[8].getY()+asc));
            //図の名前を表示
            String title = moonfaceRadioButton.isSelected() ? "月相エニアグラム" : "曜日のエニアグラム";
            tl = new TextLayout(title,getFont(),render);
            g.drawString(title,(int)(width-tl.getAdvance()-10),(int)(height - 20));
        }
        if(moonfaceRadioButton.isSelected()) {
            for(int i=0; i<facePoints.length; i++) {
                FontRenderContext render = g.getFontRenderContext();
                TextLayout tl = new TextLayout(MOONFACE[i],getFont(),render);
                float w = tl.getAdvance() / 2f;
                float h = tl.getAscent() / 2f;
                g.drawString(MOONFACE[i],round(facePoints[i].getX()-w),round(facePoints[i].getY()));
            }
        }

        if(numberRadioButton.isSelected()) {
            FontRenderContext render = g.getFontRenderContext();
            TextLayout tl = new TextLayout("１",getFont(),render);
            double asc = tl.getAscent()/2d; //高さは共通で良し
            for(int i=1; i<=9; i++) {
                tl = new TextLayout(NUMBERS[i],getFont(),render);
                double adv = tl.getVisibleAdvance() / 2;
                g.drawString(NUMBERS[i],round(datePoint[i].getX()-adv),round(datePoint[i].getY()+asc));
            }
        }
        if(doremiRadioButton.isSelected()) {
            FontRenderContext render = g.getFontRenderContext();
            TextLayout tl = new TextLayout("ド",getFont(),render);
            double asc = tl.getAscent()/2d; //高さは共通で良し
            for(int i=1; i<=9; i++) {
                tl = new TextLayout(DOREMI[i],getFont(),render);
                double adv = tl.getVisibleAdvance() / 2;
                g.drawString(DOREMI[i],round(datePoint[i].getX()-adv),round(datePoint[i].getY()+asc));
            }
        }
        if(planetRadioButton1.isSelected()|| planetRadioButton2.isSelected()) {
            String [] symbols = planetRadioButton1.isSelected() ? PLANETS : PLANETS2;
            FontRenderContext render = g.getFontRenderContext();
            for(int i=1; i<=9; i++) {
                TextLayout tl = new TextLayout(symbols[i],astroFont,render);
                float h = tl.getAscent()/2f;
                float w = tl.getAdvance()/2f;
                AffineTransform at = new AffineTransform();
                at.translate(-w,h); //移動前文字の原点は左下にあるから、左に半分、上に半分
                Shape planetShape = tl.getOutline(at);
                AffineTransform at2 = new AffineTransform();
                at2.translate(datePoint[i].getX(),datePoint[i].getY());
                planetShape = at2.createTransformedShape(planetShape);
                g.setStroke(pleanStroke);
                g.fill(planetShape); //細い線でサインを塗りつぶす。くっきりしたサインが描け
            }
        }
    }
    // ﾏｳｽﾎｲｰﾙで日付を前後させるﾊﾝﾄﾞﾗ
    class MouseWheelHandler implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(nowButton.isEnabled()) {
                if(e.getWheelRotation() < 0)
                    dateHandler.next();
                else
                    dateHandler.back();
            }
        }
    }

    /**
     * エニアグラムカレンダーを単体で実行。exec(true)を実行するだけ。
     */
    public static void main(String args[]) {
        exec(true);
    }

    static EnneagramCalendar2 instance;
    /**
     * singleExecがtrueなら、フレームのクローズボタンでSystemExit。
     * falseならDISPOSEのみ。
     * このカレンダーを単体で動かすときはtrue、アプリから呼ぶときはfalseを与える。
     */
    public static void exec(final boolean singleExce) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(UIManager.getLookAndFeel().getName().equals("Metal")) {
                    UIManager.put("swing.boldMetal", Boolean.FALSE);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    Toolkit.getDefaultToolkit().setDynamicLayout(true);
                }
                EnneagramCalendar2 ec = new EnneagramCalendar2();
                ec.setVisible(true);
            }
        });
    }

    /**
     * メニュー「印刷」のアクションリスナから呼び出され印刷を行う。
     */
    private void doPrint() {
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add( OrientationRequested.LANDSCAPE );
        aset.add( new Copies(2) );
        aset.add( new JobName("Enneagram Calendar", null) );

        /* Create a print job */
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(mainPanel);
        /* locate a print service that can handle the request */
        PrintService[] services =
            PrinterJob.lookupPrintServices();
        if ( services.length > 0 ) {
            System.out.println( "selected printer " + services[0].getName() );
            try {
                pj.setPrintService( services[0] );
                PageFormat pf = pj.pageDialog( aset );
                if ( pf != null && pj.printDialog( aset ) ) {
                    pj.print( aset );
                }
            } catch ( PrinterException pe ) {
                System.err.println(pe);
            }
        }
    }

    //月相ｴﾆｱｸﾞﾗﾑ用のﾊﾝﾄﾞﾗ
    class MoonCalendarHandler implements DateHandler {
        @Override
        public void now() {
            Calendar c = Calendar.getInstance(zone);
            for ( int i=0; i < faceList.size()-1; i++ ) {
                if ( c.before( faceList.get(i).date ) ) {
                    pointer = i;
                    set(); //2020年12月6日 アマテルが起動しなくなったための応急処置
                    break;
                }
            }
        }
        @Override
        public void back() {
            pointer--;
            set();
        }
        @Override
        public void next() {
            pointer++;
            set();
        }
        @Override
        public void set() {
            String format1 = displayTimeCheckBox.isSelected() ? "%tb-%te %Tk:%TM" : "%tb-%te";         //年無し
            String format2 = displayTimeCheckBox.isSelected() ? "%tY-%tb-%te %Tk:%TM" : "%tY-%tb-%te"; //年つき
            MoonFace [] buf = new MoonFace[dateStrings.length];
            //アマテル起動時に歴範囲外エラーになると起動できなくなる。
            //起動中はオプションパンのエラー表示もでない。
            //だから歴範囲外を検出したらなにもせず戻るだけに修整。
            //歴範囲を超えると無反応になるが、起動しなくなるよりずっとよかろう。
            //2020-12-08
            if ( faceList.size() <= (pointer+dateStrings.length ))
                return;
            //try {
            for ( int i=0; i < dateStrings.length; i++ ) {
                buf[i] = faceList.get( pointer + i );
            }
            //} catch ( ArrayIndexOutOfBoundsException e ) {
            //    JOptionPane.showMessageDialog( EnneagramCalendar2.this, "天文暦の範囲を超えました" );
            //    return;
            //}
            //配列を月相の数だけローテイトして[0]を新月にする。
            int face = faceList.get(pointer).face;
            hed = (face + 8) % 9;
            while ( face > 0 ) {
                MoonFace temp = buf[8];
                for ( int i = buf.length -1; i >= 1; i-- ) buf[i] = buf[i-1];
                buf[0] = temp;
                face--;
            }
            for ( int i=0; i < buf.length; i++ ) {
                Calendar c = buf[i].date;
                if ( i == hed )
                    dateStrings[i] = "☆" + String.format(format2,c,c,c,c,c);
                else
                    dateStrings[i] = String.format(format1,c,c,c,c);
            }
            repaint();
        }
    }
    //曜日ｴﾆｱｸﾞﾗﾑ用のﾊﾝﾄﾞﾗ
    class YoubiCalendarHandler implements DateHandler {
        @Override
        public void now() {
            cal2 = Calendar.getInstance(zone);
            //cal2.setTimeInMillis(System.currentTimeMillis());
            set();
        }
        @Override
        public void back() {
            cal2.add(Calendar.DAY_OF_MONTH,-1);
            set();
        }
        @Override
        public void next() {
            cal2.add(Calendar.DAY_OF_MONTH,1);
            set();
        }
        //曜日エニアグラム
        @Override
        public void set() {
            String format1 = "%tb-%te(%s)";     // 年無し
            String format2 = "%tY-%tb-%te(%s)"; // 年つき
            int youbi = cal2.get(Calendar.DAY_OF_WEEK);
            Calendar c = (Calendar)cal2.clone();
            String [] buf = new String[7];
            for(int i=0; i<7; i++) {
                int w = c.get(Calendar.DAY_OF_WEEK);
                if(i==0)
                    buf[i] = "☆" + String.format(format2,c,c,c,YOUBI[w],c,c);
                else
                    buf[i] = String.format(format1,c,c,YOUBI[w],c,c);
                c.add(Calendar.DAY_OF_MONTH,1);
            }
            while(youbi-- > 1) {
                String temp = buf[6];
                for(int i=buf.length -1; i>=1; i--) buf[i] = buf[i-1];
                buf[0] = temp;
            }
            for(int i=0,j=0; i<9; i++) {
                if(i==3 || i==6) {
                    dateStrings[i] = " ";
                    continue;
                }
                dateStrings[i] = buf[j++];
            }
            repaint();
        }
    }
    //次、前、今のボタンのEnable状態を一括セット
    private void setButtonEnabled(boolean b) {
        backButton.setEnabled(b);
        nextButton.setEnabled(b);
        nowButton.setEnabled(b);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel buttonPanel;
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel jPanel1;

        configPanel = new javax.swing.JPanel();
        configPanel2 = new javax.swing.JPanel();
        circleCheckBox = new javax.swing.JCheckBox();
        trainCheckBox = new javax.swing.JCheckBox();
        line1CheckBox = new javax.swing.JCheckBox();
        line2CheckBox = new javax.swing.JCheckBox();
        confCloseButton = new javax.swing.JButton();
        moonfaceRadioButton = new javax.swing.JRadioButton();
        numberRadioButton = new javax.swing.JRadioButton();
        noneRadioButton = new javax.swing.JRadioButton();
        bgColorButton = new javax.swing.JButton();
        fgColorButton = new javax.swing.JButton();
        displayTimeCheckBox = new javax.swing.JCheckBox();
        youbiRadioButton = new javax.swing.JRadioButton();
        doremiRadioButton = new javax.swing.JRadioButton();
        planetRadioButton1 = new javax.swing.JRadioButton();
        planetRadioButton2 = new javax.swing.JRadioButton();
        configButtonGroup = new javax.swing.ButtonGroup();
        buttonPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        clockLabel = new javax.swing.JLabel();
        zoneLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        nowButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        printMenuItem = new javax.swing.JMenuItem();
        confMenuItem = new javax.swing.JMenuItem();
        timezoneMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();

        configPanel.setLayout(new java.awt.BorderLayout());

        configPanel2.setLayout(new java.awt.GridBagLayout());

        configPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("\u8a2d\u5b9a"));
        circleCheckBox.setSelected(true);
        circleCheckBox.setText("\u5186");
        circleCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        circleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(circleCheckBox, gridBagConstraints);

        trainCheckBox.setSelected(true);
        trainCheckBox.setText("\u7834\u7dda");
        trainCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        trainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(trainCheckBox, gridBagConstraints);

        line1CheckBox.setSelected(true);
        line1CheckBox.setText("\u5b9f\u7dda1");
        line1CheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        line1CheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(line1CheckBox, gridBagConstraints);

        line2CheckBox.setSelected(true);
        line2CheckBox.setText("\u5b9f\u7dda2");
        line2CheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        line2CheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(line2CheckBox, gridBagConstraints);

        confCloseButton.setText("\u9589\u3058\u308b");
        confCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confCloseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(confCloseButton, gridBagConstraints);

        moonfaceRadioButton.setSelected(true);
        moonfaceRadioButton.setText("\u6708\u76f8");
        moonfaceRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moonfaceRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        moonfaceRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                moonfaceRadioButtonItemStateChanged(evt);
            }
        });
        moonfaceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moonfaceRadioButtonActionPerformed(evt);
            }
        });
        moonfaceRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                moonfaceRadioButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(moonfaceRadioButton, gridBagConstraints);

        numberRadioButton.setText("\u6570\u5b57");
        numberRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        numberRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(numberRadioButton, gridBagConstraints);

        noneRadioButton.setText("\u7121\u3057");
        noneRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noneRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(noneRadioButton, gridBagConstraints);

        bgColorButton.setText("\u80cc\u666f\u8272");
        bgColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgColorButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        configPanel2.add(bgColorButton, gridBagConstraints);

        fgColorButton.setText("\u7dda\u8272");
        fgColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgColorButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        configPanel2.add(fgColorButton, gridBagConstraints);

        displayTimeCheckBox.setText("\u6642\u9593");
        displayTimeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        displayTimeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        displayTimeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayTimeCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(displayTimeCheckBox, gridBagConstraints);

        youbiRadioButton.setText("\u4e03\u66dc\u65e5");
        youbiRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        youbiRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        youbiRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                youbiRadioButtonItemStateChanged(evt);
            }
        });
        youbiRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                youbiRadioButtonActionPerformed(evt);
            }
        });
        youbiRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                youbiRadioButtonStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(youbiRadioButton, gridBagConstraints);

        doremiRadioButton.setText("\u97f3\u968e");
        doremiRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doremiRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(doremiRadioButton, gridBagConstraints);

        planetRadioButton1.setText("\u4e03\u60d1\u661f");
        planetRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        planetRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(planetRadioButton1, gridBagConstraints);

        planetRadioButton2.setText("\u5341\u60d1\u661f");
        planetRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        planetRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        configPanel2.add(planetRadioButton2, gridBagConstraints);

        configPanel.add(configPanel2, java.awt.BorderLayout.NORTH);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("\u30a8\u30cb\u30a2\u30b0\u30e9\u30e0\u30ab\u30ec\u30f3\u30c0\u30fc");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        clockLabel.setText("datetime");
        jPanel2.add(clockLabel);

        zoneLabel.setText("timezone");
        jPanel2.add(zoneLabel);

        buttonPanel.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        nowButton.setText("\u4eca");
        nowButton.setMargin(new java.awt.Insets(1, 14, 1, 14));
        nowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nowButtonActionPerformed(evt);
            }
        });

        jPanel1.add(nowButton);

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/minus.png")));
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        jPanel1.add(backButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/plus.png")));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        jPanel1.add(nextButton);

        buttonPanel.add(jPanel1, java.awt.BorderLayout.EAST);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("\u30e1\u30cb\u30e5\u30fc");
        printMenuItem.setText("\u5370\u5237");
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(printMenuItem);

        confMenuItem.setText("\u8a2d\u5b9a");
        confMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(confMenuItem);

        timezoneMenuItem.setText("\u6642\u5dee\u8a2d\u5b9a");
        timezoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timezoneMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(timezoneMenuItem);

        jMenu1.add(jSeparator1);

        exitMenuItem.setText("\u7d42\u4e86");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
  // 設定の曜日ﾗｲｼﾞｵﾎﾞﾀﾝの状態が変化したとき呼び出され、「今次前」ﾎﾞﾀﾝのEnabled状態
  // を切り替える。
  private void youbiRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_youbiRadioButtonItemStateChanged
      setButtonEnabled(youbiRadioButton.isSelected());
  }//GEN-LAST:event_youbiRadioButtonItemStateChanged
  // 設定の月相ﾗｼﾞｵﾎﾞﾀﾝの状態が変化したとき呼び出され、今次前ﾎﾞﾀﾝのEnabled状態を
  // 切り替える
  private void moonfaceRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_moonfaceRadioButtonItemStateChanged
      displayTimeCheckBox.setEnabled(moonfaceRadioButton.isSelected());
      setButtonEnabled(moonfaceRadioButton.isSelected());
  }//GEN-LAST:event_moonfaceRadioButtonItemStateChanged
  // ﾒﾆｭｰからﾀｲﾑｿﾞｰﾝの変更
  private void timezoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timezoneMenuItemActionPerformed
      TimeZone zn = TimeZoneDialog.showDialog(EnneagramCalendar2.this);
      if(zn != null) {
          prop.setProperty("TimeZoneID",zn.getID());
          this.zone = zn;
          zoneLabel.setText(zn.getDisplayName(
              zn.useDaylightTime(),TimeZone.SHORT));
          faceList = Almanac.getEnneaAlmanac(zn);
          dateHandler.now();
      }
  }//GEN-LAST:event_timezoneMenuItemActionPerformed
  // 未使用
  private void youbiRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_youbiRadioButtonStateChanged
        }//GEN-LAST:event_youbiRadioButtonStateChanged
  // 未使用
  private void moonfaceRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_moonfaceRadioButtonStateChanged
        }//GEN-LAST:event_moonfaceRadioButtonStateChanged
  //メニューから終了
  private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
      saveProperties();
      dispose();
  }//GEN-LAST:event_exitMenuItemActionPerformed
  //設定から七曜日が選択された
  private void youbiRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_youbiRadioButtonActionPerformed
      dateHandler = youbiCalendarHandler;
      dateHandler.now();
  }//GEN-LAST:event_youbiRadioButtonActionPerformed
  //設定から月相が選択された
  private void moonfaceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moonfaceRadioButtonActionPerformed
      dateHandler = moonCalendarHandler;
      dateHandler.now();
  }//GEN-LAST:event_moonfaceRadioButtonActionPerformed
  //設定から時間ﾁｪｯｸﾎﾞｯｸｽが選択された
  private void displayTimeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayTimeCheckBoxActionPerformed
      dateHandler.set();
  }//GEN-LAST:event_displayTimeCheckBoxActionPerformed
  // ﾒﾆｭｰから印刷が呼び出された
  private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMenuItemActionPerformed
      doPrint();
  }//GEN-LAST:event_printMenuItemActionPerformed
  //設定から線色の指定
  private void fgColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorButtonActionPerformed
      Color col = JColorChooser.showDialog(this,"title",mainPanel.getForeground());
      if(col == null) return;
      mainPanel.setForeground(col);
  }//GEN-LAST:event_fgColorButtonActionPerformed
  //設定から背景色の指定
  private void bgColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorButtonActionPerformed
      Color bg = JColorChooser.showDialog(this,"title",mainPanel.getBackground());
      if(bg == null) return;
      mainPanel.setBackground(bg);
  }//GEN-LAST:event_bgColorButtonActionPerformed
  //設定パネル隠す
  private void confCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confCloseButtonActionPerformed
      getLayout().removeLayoutComponent(configPanel);
      remove(configPanel);
      confMenuItem.setEnabled(true);
      validate();
      repaint();
  }//GEN-LAST:event_confCloseButtonActionPerformed
  //設定パネル見せる
  private void confMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confMenuItemActionPerformed
      confMenuItem.setEnabled(false);
      add(configPanel,BorderLayout.EAST);
      validate();
      repaint();
  }//GEN-LAST:event_confMenuItemActionPerformed
  // 今ﾎﾞﾀﾝが押された
  private void nowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nowButtonActionPerformed
      dateHandler.now();

  }//GEN-LAST:event_nowButtonActionPerformed
  // 前ﾎﾞﾀﾝが押された
  private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
      dateHandler.back();
  }//GEN-LAST:event_backButtonActionPerformed
  // 次ﾎﾞﾀﾝが押された
  private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
      dateHandler.next();
  }//GEN-LAST:event_nextButtonActionPerformed
  // ﾌﾚｰﾑの×ﾎﾞﾀﾝが押された
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      saveProperties();
  }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton bgColorButton;
    private javax.swing.JCheckBox circleCheckBox;
    private javax.swing.JLabel clockLabel;
    private javax.swing.JButton confCloseButton;
    private javax.swing.JMenuItem confMenuItem;
    private javax.swing.ButtonGroup configButtonGroup;
    private javax.swing.JPanel configPanel;
    private javax.swing.JPanel configPanel2;
    private javax.swing.JCheckBox displayTimeCheckBox;
    private javax.swing.JRadioButton doremiRadioButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton fgColorButton;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox line1CheckBox;
    private javax.swing.JCheckBox line2CheckBox;
    private javax.swing.JRadioButton moonfaceRadioButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JRadioButton noneRadioButton;
    private javax.swing.JButton nowButton;
    private javax.swing.JRadioButton numberRadioButton;
    private javax.swing.JRadioButton planetRadioButton1;
    private javax.swing.JRadioButton planetRadioButton2;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem timezoneMenuItem;
    private javax.swing.JCheckBox trainCheckBox;
    private javax.swing.JRadioButton youbiRadioButton;
    private javax.swing.JLabel zoneLabel;
    // End of variables declaration//GEN-END:variables

}
