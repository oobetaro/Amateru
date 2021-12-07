/*
 * AstroCalendar.java
 *
 * Created on 2006/05/22, 6:58
 */

package to.tetramorph.michiteru;


import java.util.logging.Level;
import to.tetramorph.almanac.Almanac;
import to.tetramorph.almanac.MoonFace;
import to.tetramorph.almanac.PlanetEvent;
import to.tetramorph.almanac.VoidTime;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import to.tetramorph.kyuureki.KyuurekiAlmanac;
import to.tetramorph.kyuureki.KyuurekiDate;
import to.tetramorph.tzdialog.TimeZoneDialog;
import static to.tetramorph.michiteru.Const.*;
import static java.util.Calendar.*;
import java.util.Enumeration;
import java.util.logging.Logger;
import to.tetramorph.kyuureki.Kansi;
import to.tetramorph.kyuureki.KansiKyuuseiAlmanac;
import to.tetramorph.kyuureki.Main;
import to.tetramorph.kyuureki.SetuiriAlmanac;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.IconLoader;
import to.tetramorph.widget.LanguageDialog;
import static java.lang.System.setProperty;
import static java.lang.System.getProperty;

/**
 * ボイドタイムや天体運行を表示する天象カレンダー(ミチテル)。<br>
 * スタンドアローンのアプリケーションとしても、アマテルからメソッドの呼びだしに
 * よっても起動できる。<br>
 * 独自に排他制御サーバーをもっていて、多重起動できない仕組みになっている。<br>
 * スタンドアローンでミチテルが実行中のとき、アマテルがミチテルを起動しようと
 * すると、すでに起動されているミチテルが画面最前面に出る。<br>
 * 逆にアマテルからミチテルが起動されている場合、スタンドアローンで(Java WebStart
 * やコマンドラインから)起動しようとすると、アマテルが使っているミチテルが画面
 * 最前面に出る。<br>
 * 祝日データはネットからＤＬするのはやめて、リソースから取得するようにした。
 * @author 大澤義孝
 */
public final class AstroCalendar extends javax.swing.JFrame
                                                     implements MutexListener {
    private Font astrofont;
    private Font smallAstroFont;
    private Font monthFont;
    private Font smallFont = new Font("SansSerif",Font.PLAIN,10);
    private static final String [] STATE = { "","","D","R" };
    private int width;
    private int height;
    //
    private Calendar cal;
    private Calendar todayCal;
    //
    private CalendarPanel calPanel;
    //
    private int headderHeight = 25;
    private boolean outOfRange = false;
    private int dayOfMonth = 0;
    private int dayOfWeek = 0;
    private List<VoidTime> voidList = null;
    private List<Integer>dayList = null;
    private int zodiacBarHeight = 18;
    private Image testImage;

    private Properties masterProp; //システム情報用
    private List<JRadioButtonMenuItem> themeList =
                                        new ArrayList<JRadioButtonMenuItem>();
    private ButtonGroup themeButtonGroup = new ButtonGroup();

    private int [] moonCourseBitmap = null;
    private Map<Integer,MoonFace> moonFaceMap;
    private Map<Integer,java.util.List<PlanetEvent>> planetEventMap;
    private Map<String,String> voidStampMap = new HashMap<String,String>();
    private javax.swing.Timer clockTimer;
    private KyuurekiAlmanac kyuureki = new KyuurekiAlmanac();

    private AstroCalendar() {
        this(true);
    }

    /**
     * 天体カレンダーオブジェクトを作る
     */
    private AstroCalendar(boolean singleExec) {
        masterPropFile = new File( System.getProperty("app.properties"),
                                     "Michiteru.properties");

        System.out.println("プロパティファイル = " + masterPropFile);
        setIconImage( IconLoader.getImage(
                                        "/resources/michiteru.png"));
        // 占星術フォントの読込
        astrofont = AstroFont.getFont(14f);
        masterProp = new Properties();
        if(! FileTools.loadProperties(masterProp,masterPropFile)) {
            masterProp.setProperty("ColorConfigNumber","3");
        }
        //シャットダウンフックを登録
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Rectangle rect = getBounds(null);
                masterProp.setProperty("cell.width",""+width);
                masterProp.setProperty("cell.height",""+height);
                clockTimer.stop();
                FileTools.saveProperties(masterProp,masterPropFile,
                    "AstroCalendar master configuration");
            }
        });
        setNow();
        setVoidMap();
        smallAstroFont = AstroFont.getFont(14f);
        astrofont = AstroFont.getFont(14f);
        monthFont = new Font("SansSerif",Font.PLAIN,30);

        initComponents();
        if(! singleExec )
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        calPanel = new CalendarPanel(
            Integer.parseInt(masterProp.getProperty("cell.width","80")),
            Integer.parseInt(masterProp.getProperty("cell.height","80")));
        mainPanel.add(calPanel,BorderLayout.CENTER);
        WindowHandler winhand = new WindowHandler(this,masterProp);
        addComponentListener(winhand);
        pack();
        winhand.setBounds();

        DateChangeHandler dateChanger = new DateChangeHandler();
        yearIncButton.addActionListener(dateChanger);
        yearDecButton.addActionListener(dateChanger);
        monthIncButton.addActionListener(dateChanger);
        monthDecButton.addActionListener(dateChanger);
        nowButton.addActionListener(dateChanger);

        for(int i=0; i<10; i++) {
            if ( Design.loadConfig( "" + i ) ) {
                String title = Design.getProperty("title");
                //if ( title == null ) break;
                setBackground( Design.bgColor );
                MonthName.update(voidList,outOfRange,masterProp,voidStampMap,zoneLabel);
                themeList.add(new JRadioButtonMenuItem(title));
                themeMenu.add(themeList.get(i));
                themeButtonGroup.add(themeList.get(i));
            }
        }

        int themeNum = Integer.parseInt(masterProp.getProperty("ColorConfigNumber"));
        themeButtonGroup.setSelected(themeList.get(themeNum).getModel(),true);
        for(int i=0; i<themeList.size(); i++) {
            themeList.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    for(int j=0; j<themeList.size(); j++) {
                        if(event.getSource() == themeList.get(j)) {
                            Design.loadConfig(""+j);
                            setBackground(Design.bgColor);
                            MonthName.update(voidList,outOfRange,masterProp,voidStampMap,zoneLabel);
                            masterProp.setProperty("ColorConfigNumber",""+j);
                            repaint();
                        }
                    }
                }
            });
        }
        Design.loadConfig(masterProp.getProperty("ColorConfigNumber"));
        setBackground(Design.bgColor);
        MonthName.update(voidList,outOfRange,masterProp,voidStampMap,zoneLabel);
        Border zoneBorder = BorderFactory.createEmptyBorder(0,10,0,0);
        zoneLabel.setBorder(zoneBorder);
        clockLabel.setBorder(zoneBorder);
        //時計
        clockTimer = new javax.swing.Timer(1000,new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                TimeZone zone = TimeZone.getTimeZone(
                    masterProp.getProperty("TimeZoneID",
                                           TimeZone.getDefault().getID()));
                Calendar cal = Calendar.getInstance(zone);
                zoneLabel.setForeground( Design.monthNumberColor );
                clockLabel.setForeground( Design.monthNumberColor );
                clockLabel.setText( String.format(
                    MonthName.getLocaleProperty( masterProp ),
                                                 "%tB %td %ta, %tp %tl:%tM:%tS",
                                                 cal,cal,cal,cal,cal,cal,cal));
                clockLabel.revalidate();
            }
        });
        clockTimer.start(); //stop()はシャットダウンフックの中で行う。
    }

//月や年や設定ボタンのイベントリスナ
    class DateChangeHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Object button = event.getSource();
            if(button == yearIncButton)
                cal.add(YEAR,1);
            else if(button == yearDecButton)
                cal.add(YEAR,-1);
            else if(button == monthIncButton)
                cal.add(MONTH,1);
            else if(button == monthDecButton)
                cal.add(MONTH,-1);
            else if(button == nowButton) {
                setNow();
            }
            setVoidMap();
            MonthName.update(voidList,outOfRange,masterProp,voidStampMap,zoneLabel);
            calPanel.repaint();
        }
    }
//カレントCalendarを今月の1日にセット現在Calendarを現在時刻にセット
    private void setNow() {
        TimeZone zone = TimeZone.getTimeZone(
            masterProp.getProperty("TimeZoneID",TimeZone.getDefault().getID()));
        cal = Calendar.getInstance(zone);
        todayCal = (Calendar)cal.clone();
        cal.set(DAY_OF_MONTH,1);
    }
//ボイド情報を格納したリストやマップを作成。グローバル変数のcalを参照している。
    private void setVoidMap() {
        voidList = Almanac.getVoidOfCourseMoonList(cal);
        moonFaceMap = Almanac.getMoonFaceMap(cal);
        planetEventMap = Almanac.getPlanetEventMap(cal);
        if ( voidList == null||moonFaceMap == null||planetEventMap == null) {
            System.out.println("voidList = " + voidList );
            System.out.println("moonFaceMap = " + moonFaceMap );
            System.out.println("planetEventMap = " + planetEventMap );
            outOfRange = true;
        } else {
            outOfRange = false;
        }
        dayOfMonth = cal.getActualMaximum(DAY_OF_MONTH);
        dayOfWeek = cal.get(DAY_OF_WEEK);
        dayList = new ArrayList<Integer>();
        for(int i=1; i<= dayOfMonth; i++) dayList.add(i);
        for(int i=1; i<  dayOfWeek; i++) dayList.add(0,null);
    }


    /**
     * カレンダーグラフィックを作成するパネル
     */
    class CalendarPanel extends JPanel {
        Image moonCourseImage = null;
        /**
         *
         */
        CalendarPanel(int cellWidth,int cellHeight) {
            setPreferredSize(new Dimension(cellWidth*7,cellHeight*6));
            setOpaque(false);
            MouseHandler mh = new MouseHandler();
            this.addMouseListener(mh);

        }
        /**
         * まず最初に幅の長〜い、月の日数分すべがはいる一直線のイメージ領域を用
         * 意して、そこに獣帯やボイドバーなどをグラフィックで描画、それができあ
         * がったら、一日分ごとに切り出して、七×四〜五段のマトリクス上にコピー
         * することで描画を行う。
         * 長いソースだが三分の二以上は月の獣帯とカレンダーのヘッダーを描く処理で、
         * 残りの部分でワンセルづつ描いていく。
         */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D)graphics;
            //アンチエイリアシングでスムースな線を描きたいときにsetする
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            //端数があれば無条件に切り上げ
            int rowSize = (((float)dayList.size() % 7) == 0 ) ?
                    dayList.size() / 7    :     dayList.size() / 7 + 1;
            //可視領域の幅を7で割り1セルの幅を決める。割る前に-1しているのは、
            //外枠が可視領域外にはみだして描画されない事があるため
            width = ( getVisibleRect().width - 1 ) / 7;
            height = ( getVisibleRect().height
                       - headderHeight - 40 - 1 ) / rowSize;
            //可視領域の左上隅を原点とする
            //g.translate( getVisibleRect().x, getVisibleRect().y );
            //System.out.println( " x = " + getVisibleRect().x + ", y = " + getVisibleRect().y );
            int zodiacBarWidth = width * dayOfMonth;
            moonCourseBitmap = new int[ zodiacBarWidth ];
            moonCourseImage = createImage( zodiacBarWidth, zodiacBarHeight );
            Graphics2D mg = (Graphics2D)moonCourseImage.getGraphics();
            int sign = -1;
            if ( ! outOfRange ) {
                //月の初めの深夜0時を原点とする
                Calendar zeroCal = (Calendar)cal.clone();
                zeroCal.set( DAY_OF_MONTH, 1 );
                zeroCal.set( HOUR_OF_DAY, 0);
                zeroCal.set( MINUTE, 0 );
                zeroCal.set( SECOND, 0 );
                long zero = zeroCal.getTimeInMillis();

                java.util.List<Calendar> signList = new ArrayList<Calendar>();
                signList.add( zeroCal );
                for ( int i=0; i<voidList.size(); i++ )
                    signList.add(voidList.get(i).end);
                //月の終わりは来月の初めとする
                Calendar botomCal = (Calendar)zeroCal.clone();
                botomCal.add( MONTH, 1 );
                signList.add( botomCal );
                sign = voidList.get(0).getPrevSign();
                //獣帯バーを描く
                for(int i=0; i<signList.size()-1; i++) {
                    long bt = (signList.get(i).getTimeInMillis() - zero) / 1000L;
                    long et = (signList.get(i+1).getTimeInMillis() - zero) / 1000L;
                    int s = Math.round(bt/3600f/24f * width);
                    int w = Math.abs(
                        Math.round(et / 3600f / 24f * width -
                                   bt / 3600f / 24f * width ) );
                    mg.setColor( Design.testcolor[sign] );
                    mg.fillRect( s, 0, w, zodiacBarHeight );
                    //浮き彫り風にバーの上下に明るい線と位線を描く
                    mg.setColor( Color.BLACK );
                    mg.drawLine( s, 0, s + w, 0 );
                    mg.setColor( Design.testcolor[sign].brighter() );
                    mg.drawLine( s, 1, s + w, 1 );
                    mg.setColor( Design.testcolor[sign].darker() );
                    mg.drawLine(     s, zodiacBarHeight - 1,
                                 s + w, zodiacBarHeight - 1 );
                    for ( int j=0; j<w; j++ ) {
                        if ( (j + s) < moonCourseBitmap.length)
                            moonCourseBitmap[j+s] = sign;
                    }
                    sign++;
                    if ( sign >= 12 ) sign = 0;
                }
                // ボイドバーを描く
                for ( int i=0; i < voidList.size(); i++ ) {
                    VoidTime vt = voidList.get(i);
                    //ボイド開始・終了時刻から原点の時刻を減じ、
                    //24時間を1.0とした値に変換。セルの幅をかける。
                    long bt = ( vt.begin.getTimeInMillis() - zero ) / 1000L;
                    long et = ( vt.end.getTimeInMillis()   - zero ) / 1000L;
                    int p1 = Math.round( bt / 3600f / 24f * width );
                    int p2 = Math.abs(
                        Math.round( et / 3600f / 24f * width -
                                    bt / 3600f / 24f * width) );
                    mg.setColor(
                        Design.addColor( Design.voidBarColor,
                                         Design.testcolor[vt.getPrevSign()]) );
                    mg.fillRect(p1,2,p2,zodiacBarHeight/2-2);
                }
            }
            Kansi kansi = KansiKyuuseiAlmanac.getLenientValue(cal);
            //年月を描く
            FontRenderContext render = g.getFontRenderContext();
            String monthName = MonthName.monthNames[ cal.get(MONTH) ];
            String 月干支九星 = kansi.get干支( Kansi.MONTH )
                              + kansi.get九星略称( Kansi.MONTH );
            if ( kyuurekiCheckBoxMenuItem.isSelected() ) {
                monthName += " " + 月干支九星;
            }
            TextLayout tl =
                new TextLayout( monthName , monthFont, render );
            g.setFont( monthFont );
            g.setColor( Design.monthNumberColor );
            //月の名前(May,June等)は左寄せ
            g.drawString( monthName, 0, 30 );
//            //月の数字は中央
//            g.drawString( ""+(cal.get(MONTH)+1),
//                          ( getVisibleRect().width - tl.getAdvance() ) / 2, 30 );
            //年号は右寄せ
            String yearName = ""+(cal.get(YEAR));
            String 年干支九星 = kansi.get干支(Kansi.YEAR)
                              + kansi.get九星略称(Kansi.YEAR);
            if ( kyuurekiCheckBoxMenuItem.isSelected() ) {
                yearName += " " + 年干支九星;
            }
            tl = new TextLayout( yearName, monthFont, render );
            g.setColor( Design.yearNumberColor );
            tl.draw(g,getVisibleRect().width - tl.getAdvance(), 30);
//            g.drawString( "" + cal.get(YEAR),
//                          getVisibleRect().width - tl.getAdvance(), 30 );
            // 曜日ヘッダを描く
            g.translate( getVisibleRect().x, getVisibleRect().y + 40 );
//            StringBuffer sb = new StringBuffer();
            float w = 0f;
            boolean cellPaint =
                Design.getProperty("weekCellPaint","yes").equals("yes");
            for ( int col = 0; col < 7 && cellPaint; col++ ) {
                g.setColor( Design.weekCellColor );
                g.fillRect( col * width, 0, width, headderHeight );
                g.setColor( Design.lineColor );
                g.drawRect( col * width, 0, width, headderHeight );
                g.setColor( Design.weekCellColor );
                g.draw3DRect( col * width + 1, 1, width - 2, headderHeight - 2,
                                                                        true );
            }
            g.setFont( Design.weekHeadderFont );
            for ( int col = 0; col < 7; col++ ) {
                g.setColor( Design.weekHeadderColors[col] );
                TextLayout textLayout = new TextLayout(
                    MonthName.weekNames[col+1],
                    Design.weekHeadderFont, render );
                float h = ( textLayout.getAscent()
                         + textLayout.getDescent() ) / 2f;
                float ad =  textLayout.getAdvance();
                if ( ad > w ) w = ad;
                int tx = Math.round( col * width + width / 2 - ad / 2 );
                int ty = Math.round( headderHeight / 2 + h / 2 + 1 );
                g.setColor( Color.DARK_GRAY );
                g.drawString( MonthName.weekNames[ col + 1 ], tx + 1,ty + 1 );
                g.setColor( Design.weekHeadderColors[ col ] );
                g.drawString( MonthName.weekNames[ col + 1 ], tx, ty );
            }
            //カレンダー領域を塗りつぶし、外枠線を描く
            g.setColor( Design.nullCellColor );
            g.fillRect( 0, headderHeight, width * 7, rowSize * height );
            g.setColor( Design.lineColor );
            g.drawRect( 0, headderHeight, width * 7, rowSize*height );
            //1セルずつカレンダーを描画
            dateRectList.clear();
            for ( int row = 0; row < rowSize; row++ ) {
                for ( int col = 0; col < 7; col++ ) {
                    paintCell( col, row, dayList, g );
                }
            }
//            g.setColor(Color.RED);
//            for ( DateRectangle r : dateRectList ) {
//                g.draw(r);
//            }
        }

        /**
         * colとrowで位置を指定したら、その位置にその日の数字をdayListから取得
         * して画面に描く。その他のパラメターもセルに描く。
         * つまり一日分のワンセルだけ指定された場所に描くメソッド。
         * @param col 列
         * @param row 行
         * @param dayList 日のリスト
         */
        void paintCell(int col,int row,java.util.List<Integer> dayList,Graphics2D g) {
            int x = col * width;
            int y = row * height + headderHeight;
            int n = row * 7 + col;
            Calendar kyuurekiCal = (Calendar)cal.clone();
            int year = cal.get(YEAR);
            int month = cal.get(MONTH) + 1;
            if ( n >= dayList.size() ) return;
            if ( dayList.get(n) == null ) return;
            int day = dayList.get(n);
            int x2 = ( day - 1 ) * width;
            kyuurekiCal.set( Calendar.DAY_OF_MONTH, day );
            kyuurekiCal.set( Calendar.HOUR_OF_DAY, 12);

            //1〜31の日数を表示(今の日付ならセル色を変化させる)
            if ( todayCal.get(DAY_OF_MONTH) == day &&
                 todayCal.get(MONTH)        == cal.get(MONTH) &&
                 todayCal.get(YEAR)         == cal.get(YEAR) ) {
                g.setColor(Design.todayCellColor);
            }
            else g.setColor( Design.cellColor );
            g.fillRect( x, y, width, height - 16 );
            dateRectList.add(
                new DateRectangle( x, y + 40, width, height, kyuurekiCal) );

            g.setFont( Design.dayNumberFont );
            g.setColor( new Color(0xc0c0c0) );
            g.drawString(""+day,x+4+1,y+15+1); // 日付の数字の影
            //土日祝日の数字の色をセット
            String holiday = HolidayAlmanac.get( year, month, day );
            if ( holiday == null ) g.setColor( Design.weekColors[col] );
            else g.setColor( Design.weekColors[0] );
            g.drawString(""+day,x+4,y+15); //日付の数字
            //祝日名の表示
            if ( holiday != null && height > 72 ) {
                g.setFont( smallFont );
                g.drawString( holiday, x + 5, y + 26 );
            }
            //月齢表示
            if ( !outOfRange ) {
                MoonFace moonface = moonFaceMap.get(day);
                if(moonface != null) {
                    g.setColor(Design.moonFaceColor);
                    g.setFont(astrofont);
                    g.drawString( "" + MOON_CHARS[ moonface.face ],
                                  x + width - 15,    y + 16 );
                    String time = moonface.getTime();
                    int offset = (time.length() == 4) ? 38:44;
                    g.setFont(smallFont);
                    g.drawString( moonface.getTime(),x + width - offset, y + 14 );
                }
                //獣帯の帯を描く(先に用意した帯画像からコピーしてくる)
                g.drawImage(moonCourseImage, x, y + height-zodiacBarHeight,
                    x + width, y + height,
                    x2,0,x2+width,zodiacBarHeight,this);
                //星座シンボルを描く
                int center = (day-1)*width + width/2;
                int sign = moonCourseBitmap[center];
                //中央から左右10pxの位置を調べて星座番号が異なれば、ちょうど星座切替点がそこにあり、
                //そこに星座記号を書くと境目に描いてしまうことを意味しているので。境目には描かない。
                if( moonCourseBitmap[center-10] == moonCourseBitmap[center+10] ) {
                    FontRenderContext render = g.getFontRenderContext();
                    TextLayout textLayout =
                        new TextLayout(""+ZODIAC_CHARS[sign],astrofont,render);
                    TextLayout textLayoutShadow =
                        new TextLayout(""+ZODIAC_CHARS[sign],astrofont,render);
                    float h = textLayout.getAscent() + textLayout.getDescent();
                    float w = width / 2f - textLayout.getAdvance() / 2f;

                    AffineTransform at = new AffineTransform();
                    at.translate(
                        (float)x + w + 1,
                        y + height - zodiacBarHeight / 2 + h / 2 + 1 - 1 ); //これ+1-1は不要かと
                    g.setColor(Color.DARK_GRAY);
                    g.fill(textLayoutShadow.getOutline(at));

                    at = new AffineTransform();
                    at.translate((float)x+w, y+height-zodiacBarHeight/2+h/2-1);
                    g.setColor(Design.signSymColors[sign]);
                    g.fill(textLayout.getOutline(at));
                }
            }
            //今日の日付のセルなら時刻針を描く
            if ( todayCal.get(DAY_OF_MONTH) == day            &&
                 todayCal.get(MONTH)        == cal.get(MONTH) &&
                 todayCal.get(YEAR)         == cal.get(YEAR))       {

                int w = Math.round((todayCal.get(HOUR_OF_DAY)/24f
                    + todayCal.get(MINUTE)/24f/60f) * width);
                g.setColor(Color.RED);
                g.drawLine(x+w,y + height-zodiacBarHeight+2,x+w,y + height-2);
            }
            //ボイド開始〜終了時間を描く
            if ( !outOfRange ) {
                String time = voidStampMap.get( "" + cal.get(MONTH) + "." + day );
                if (time != null ) {
                    g.setFont(smallFont);
                    g.setColor(Design.voidTimeStringColor);
                    FontRenderContext render = g.getFontRenderContext();
                    String vstr = time.replaceAll("#","〜");
                    TextLayout textLayout = new TextLayout(vstr,smallFont,render);
                    int w = (int)((width - textLayout.getAdvance())/2);
                    g.drawString(vstr,x+w,y+height-zodiacBarHeight-1);
                }
            }
            //天体イベント(イングレス/逆行/順行)を描く
            if ( !outOfRange && ! kyuurekiCheckBoxMenuItem.isSelected()) {
                java.util.List<PlanetEvent> evList = planetEventMap.get(day);
                if ( evList != null ) {
                    g.setColor(new Color(0x990040));
                    int c = (width - 30)/2+5;
                    int lh = 14;  //line height
                    int yofs = (height-lh)/2;
                    if ( moonFaceMap.get(day)== null && evList.size() >1 )
                        yofs -= 10;
                    else yofs += 10;
                    for ( int i=0; i < evList.size(); i++ ) {
                        PlanetEvent ev = evList.get(i);
                        if ( ev.state < 2 ) {
                            g.setFont(smallAstroFont);
                            g.setColor(Color.BLACK);
                            String s = "" + BODY_CHARS[ ev.planet ]
                                          + ZODIAC_CHARS[ ev.sign ];
                            g.drawString( s, c + x + 5, y + yofs + ( i * lh ) );
                            g.setFont(smallFont);
                        } else {
                            g.setFont(smallAstroFont);
                            String s = "" + ZODIAC_CHARS[ ev.sign ]
                                          + BODY_CHARS[ ev.planet ];
                            g.drawString( s, c + x + 5, y + yofs + ( i * lh ) );
                            g.setFont( smallFont );
                            g.drawString( STATE[ ev.state ],
                                        c + x + 30, y + yofs + ( i * lh ) - 1 );
                        }
                    }
                }
            }
            if ( kyuurekiCheckBoxMenuItem.isSelected() ) {
                //旧暦の日付を書く
                KyuurekiDate kdate = kyuureki.getKyuurekiDate( kyuurekiCal );
                g.setFont(smallFont);
                g.setColor(Color.BLACK);
                g.drawString(getKyuurekiDayString( kdate ), x + 5, y + 38 );
                //日の干支と九星を求める
                Kansi kansi = KansiKyuuseiAlmanac.getValue( kyuurekiCal );
                String 日干 = kansi.get干支( Kansi.DAY );
                String 日九星 = kansi.get九星略称( Kansi.DAY );
                g.drawString( 日干 + " " + 日九星, x + 5, y + 51);
                Calendar setuiriCal =
                    SetuiriAlmanac.getInstance().getSetuiriTime(kyuurekiCal);
                if ( setuiriCal.get(Calendar.DAY_OF_MONTH) == day ) {
                    if ( setuiriCal.get(Calendar.SECOND) >= 30 ) {
                        setuiriCal.add(Calendar.MINUTE,1);
                    }
                    String time = String.format(
                        "節入 %tI:%tM %Tp", setuiriCal,setuiriCal,setuiriCal);
                    g.drawString( time, x + 5, y + 64);
                }
            }
            //セルの枠線を描く
            g.setColor(Design.lineColor);
            g.drawRect(x,y,width,height);
        }
    }

    List<DateRectangle> dateRectList = new ArrayList<DateRectangle>();

    String getKyuurekiDayString(KyuurekiDate date) {
        String ls = ( date.leap ) ? "閏" : "";
        return String.format("旧 %s%02d/%02d", ls, date.month, date.day);

    }
    class MouseHandler extends MouseAdapter {
        DateRectangle contains(MouseEvent e) {
            for ( DateRectangle rect : dateRectList ) {
                if ( rect.contains(e.getPoint() ) ) {
                    return rect;
                }
            }
            return null;
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            DateRectangle rect = contains(e);
            System.out.println(e.getPoint());
            if ( rect != null ) {
                System.out.println("      " + rect);
            }
        }

//        public void mousePressed(MouseEvent e) {}
//        public void mouseReleased(MouseEvent e) {}
//        public void mouseEntered(MouseEvent e) {}
//        public void mouseExited(MouseEvent e) {}
//        public void mouseDragged(MouseEvent e) {}
//        public void mouseMoved(MouseEvent e) {}

    }
/*****************************************************************************/
    private static File masterPropFile;
    static final int MUTEX_PORT = 12398;

    /**
     * Java Web Startを使用しない実行時において、二重起動が検出されたときこの
     * メソッドが呼び出される。このオブジェクトは、アイコン化されているフレーム
     * を可視化する処理をする。
     */
    @Override
    public void mutexPerformed() {
        setExtendedState(Frame.NORMAL);
        toFront();
        setVisible(true);
        System.out.println("Mutexアクション実行");
    }

    private static void setupUI() {
        if ( UIManager.getLookAndFeel().getName().equals("Metal") ) {
             UIManager.put("swing.boldMetal", Boolean.FALSE);
             Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
    }
    static AstroCalendar ac;

    /**
     * 天体カレンダーをスタンドアローンで起動する。micheteru.jarのあるフォルダには
     * AMATERU.propertiesファイルがあり、"app.properties"で、設定ファイルの
     * 置き場所のフォルダが宣言されている必要がある。
     * JVMのオプションで"-Di=".."で、AMATERU.propertiesの置き場所を明示的に
     * 指定することができる。これは開発用のオプションで、通常は使用しない。
     * 2011-07-28 Java WebStartから起動することも想定していたがそれをやめた。
     * JWSは最悪だ。
     */
    public static void main(String args[]) throws Exception {
        if ( MutexServer.isRunning( MUTEX_PORT ) ) {
            System.out.println("すでに動作中です。");
            System.exit(0);
        }
        File am = null;
        if ( getProperty("i") != null ) {
            // iオプションはAMATERU.prpertiesの場所を明示的に指定するもの
            am = new File( getProperty("i"));
        } else {
            File prog_dir = new File( getProperty("java.class.path") ).getParentFile();
            am = new File(prog_dir,"AMATERU.properties");
        }
        if ( ! am.exists() )
            throw new java.lang.IllegalStateException( am.getAbsolutePath() + "が見つからない");

        Properties p = new Properties();
        FileTools.loadProperties(p, am);
        Enumeration enu = p.propertyNames();
        while( enu.hasMoreElements() ) {
            String key = (String)enu.nextElement();
            String val = (String)p.getProperty(key);
            setProperty( key, val );
            if ( key.matches("nodb|app\\.database|support_url"))
                continue;
            File f = new File(val);
            if ( ! f.exists() ) throw new IllegalArgumentException(
                    "フォルダが存在しない : key = " + key + ", val = "
                    + val + ", " + f.getAbsolutePath());
        }

        File apphome = new File( getProperty("user.home"), ".AMATERU2.0");
        if ( getProperty("app.home") == null ) {
            for(;;) {
                Properties user_prop = new Properties();
                File user_prop_file = new File(apphome,"AMATERU_USER.properties");
                if ( user_prop_file.exists() ) {
                    System.out.println("AMATERU_USER.propertiesを検出");
                    user_prop.clear();
                    FileTools.loadProperties( user_prop, user_prop_file );
                    if ( user_prop.getProperty("app.home") != null ) {
                        //次に参照するapphomeを書き換え
                        apphome = new File( user_prop.getProperty("app.home") );
                        System.out.println( "app.homeを"
                                             + apphome.getAbsolutePath()
                                             + "にリダイレクト");
                        continue;
                    }
                }
                Enumeration amu_enu = user_prop.propertyNames();
                while( amu_enu.hasMoreElements() ) {
                    String key = (String)amu_enu.nextElement();
                    String value = user_prop.getProperty(key);
                    File uf = new File( value );
                    if ( ! ( uf.exists() && uf.isDirectory()) ) {
                        throw new IllegalStateException
                                ("フォルダではない: " + uf.getAbsolutePath());
                    } else {
                        setProperty( key, value );
                    }
                }
                break;
            }

            // app.homeをセット
            setProperty("app.home",apphome.getAbsolutePath());
        } else {
            apphome = new File( getProperty("app.home") );
        }
        if ( apphome.isFile() ) throw new IllegalStateException
                ("アプリケーションのホームフォルダがファイルになっている");
        /* app.home下のフォルダは毎回作成しシステムプロパティにも登録 */
        apphome.mkdir();
        String [] subdir = { "properties","userdict","temp","database" };
        for ( String dir : subdir ) {
            File subfol = new File(apphome, dir );
            subfol.mkdir();
            setProperty("app." + dir, subfol.getAbsolutePath() );
        }

//        String app_prop = p.getProperty("app.properties","");
//        if ( app_prop.isEmpty() )
//            throw new IllegalStateException( "app.properteisが見つからない");
//
//        System.setProperty( "app.properties", app_prop );

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if ( System.getProperty("os.name").indexOf("Windows") >= 0) {
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf"
                                + ".windows.WindowsLookAndFeel");
                    } catch ( Exception e ) {
                        Logger.getLogger( Main.class.getName())
                                .log( Level.WARNING, null, e);
                    }
                }
                ac = new AstroCalendar();
                // 排他制御サーバをスタート
                if ( ! MutexServer.exec( MUTEX_PORT, ac ) ) {
                    throw new IllegalStateException(
                        "MutexServerを起動できません");
                }
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        MutexServer.abort(MUTEX_PORT);
                    }
                });
                ac.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                ac.setVisible( true );
            }
        });
    }

    /**
     * アマテルからカレンダーを起動する場合のエントリー。<br>
     * このメソッドは必ずEDTから呼び出さなければならない。<br>
     * スタンドアローンでの起動でも、アマテルからの起動でも、すでにカレンダーが
     * 動作中なら、なにもせずにリターンするが、MutexServerによりこのメソッドが
     * 呼ばれた事が実行中のミチテルに通達される。通達を受け取った実行中のミチテル
     * はアイコン化されていようが、他のフレームの背後にまわっていようが、
     * 自らのフレームを可視化し、画面の最前面に自らの姿を現す。
     */
    public static void showCalendar() {
        if ( MutexServer.isRunning( MUTEX_PORT ) ) {
            return;
        }
        if ( ac == null ) {
            ac = new AstroCalendar();
            // 排他制御サーバをスタート
            if ( ! MutexServer.exec( MUTEX_PORT, ac ) ) {
                throw new IllegalStateException(
                    "MutexServerを起動できません");
            }
            Runtime.getRuntime().addShutdownHook( new Thread() {
                @Override
                public void run() {
                    MutexServer.abort( MUTEX_PORT );
                }
            });
            ac.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        }
        if ( ac.getExtendedState() == Frame.ICONIFIED ) {
            ac.setExtendedState( Frame.NORMAL );
        }
        ac.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainPanel = new JPanel() {
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D)graphics;
                int h = getSize().height;
                int w = getSize().width;
                g.setColor(Design.bgColor);
                g.fillRect(0,0,w,h);
            }
        };
        javax.swing.JPanel controlPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        yearDecButton = new javax.swing.JButton();
        yearIncButton = new javax.swing.JButton();
        nowButton = new javax.swing.JButton();
        monthDecButton = new javax.swing.JButton();
        monthIncButton = new javax.swing.JButton();
        confPanel = new javax.swing.JPanel();
        clockLabel = new javax.swing.JLabel();
        zoneLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        kyuurekiCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        themeMenu = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JSeparator();
        timeZoneMenuItem = new javax.swing.JMenuItem();
        langMenuItem = new javax.swing.JMenuItem();

        setTitle("MICHITERU");
        setBackground(new java.awt.Color(255, 0, 102));
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 10));
        mainPanel.setPreferredSize(new java.awt.Dimension(600, 570));
        mainPanel.setLayout(new java.awt.BorderLayout());

        controlPanel1.setOpaque(false);
        controlPanel1.setLayout(new java.awt.BorderLayout());

        buttonPanel.setOpaque(false);

        yearDecButton.setText("前年");
        yearDecButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        buttonPanel.add(yearDecButton);

        yearIncButton.setText("次年");
        yearIncButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        buttonPanel.add(yearIncButton);

        nowButton.setText("現在");
        nowButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        buttonPanel.add(nowButton);

        monthDecButton.setText("前月");
        monthDecButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        buttonPanel.add(monthDecButton);

        monthIncButton.setText("来月");
        monthIncButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        buttonPanel.add(monthIncButton);

        controlPanel1.add(buttonPanel, java.awt.BorderLayout.EAST);

        confPanel.setOpaque(false);
        confPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        clockLabel.setText("datetime");
        confPanel.add(clockLabel);

        zoneLabel.setText("timeZone");
        confPanel.add(zoneLabel);

        controlPanel1.add(confPanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(controlPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        jMenu1.setText("設定");

        kyuurekiCheckBoxMenuItem.setText("旧暦モード");
        kyuurekiCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kyuurekiCheckBoxMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(kyuurekiCheckBoxMenuItem);

        themeMenu.setText("カラーテーマ");
        jMenu1.add(themeMenu);
        jMenu1.add(jSeparator1);

        timeZoneMenuItem.setText("タイムゾーンの変更");
        timeZoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeZoneMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(timeZoneMenuItem);

        langMenuItem.setText("言語の選択...");
        langMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                langMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(langMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void langMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_langMenuItemActionPerformed
        String dl = Locale.getDefault().getLanguage();
        Locale defloc = new Locale( masterProp.getProperty( "language", dl ) );
        Locale loc = LanguageDialog.showDialog( this,defloc );
        if ( loc == null ) return;
        masterProp.setProperty( "language", loc.getLanguage() );
        MonthName.update( voidList,outOfRange,
                          masterProp,
                          voidStampMap,
                          zoneLabel );
        repaint();
    }//GEN-LAST:event_langMenuItemActionPerformed

    private void kyuurekiCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kyuurekiCheckBoxMenuItemActionPerformed
        repaint();
    }//GEN-LAST:event_kyuurekiCheckBoxMenuItemActionPerformed

    private void timeZoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeZoneMenuItemActionPerformed
        TimeZone zone = TimeZoneDialog.showDialog(AstroCalendar.this);
        if(zone != null) {
            masterProp.setProperty("TimeZoneID",zone.getID());
            cal.setTimeZone(zone);
            todayCal.setTimeZone(zone);
            setNow();
            setVoidMap(); //時差を変更すればボイド情報は作り直し
            MonthName.update(voidList,outOfRange,masterProp,voidStampMap,zoneLabel);
            repaint();
        }
    }//GEN-LAST:event_timeZoneMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clockLabel;
    private javax.swing.JPanel confPanel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBoxMenuItem kyuurekiCheckBoxMenuItem;
    private javax.swing.JMenuItem langMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton monthDecButton;
    private javax.swing.JButton monthIncButton;
    private javax.swing.JButton nowButton;
    private javax.swing.JMenu themeMenu;
    private javax.swing.JMenuItem timeZoneMenuItem;
    private javax.swing.JButton yearDecButton;
    private javax.swing.JButton yearIncButton;
    private javax.swing.JLabel zoneLabel;
    // End of variables declaration//GEN-END:variables

}
