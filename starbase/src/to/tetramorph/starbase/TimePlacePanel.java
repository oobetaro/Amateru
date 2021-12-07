/*
 * TimePlacePanel.java
 *
 * Created on 2006/07/04, 18:28
 */

package to.tetramorph.starbase;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.LimitedDocument;
import to.tetramorph.starbase.formatter.TopoFormatter;
import to.tetramorph.starbase.formatter.GregorianDateFormatter;
import to.tetramorph.starbase.formatter.TimeFormatter;
import to.tetramorph.starbase.formatter.TimeZoneFormatter;
import java.sql.Time;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import static java.util.GregorianCalendar.*;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.tzdialog.TimeZoneDialog;

/**
 * 時と場所を入力するためのパネル。
 * このクラスはpublicである必要はないのだが、publicにしておかないとGUIエディタで
 * 編集することができない。このクラス自身は問題ないのだが、このクラスを使用してい
 * るTimePanel3でエラーが発生する。
 * @author  大澤義鷹
 */
public class TimePlacePanel extends javax.swing.JPanel {
    //GregorianCalendar cal = new GregorianCalendar();
    TimeZone timeZone;
    GregorianDateFormatter dateFormatter;
    /** Creates new form TimePlacePanel */
    public TimePlacePanel() {
        initComponents();
        init();
    }
    private void init() {
        timeZone = TimeZone.getDefault();
        placenameTextField.setDocument(new LimitedDocument(28));

        timeFTextField.setFormatterFactory(new FormatterFactory(new TimeFormatter()));
        timeFTextField.setDocument(new LimitedDocument(12));
        dateFormatter = new GregorianDateFormatter(timeFTextField);
        dateFTextField.setFormatterFactory(new FormatterFactory(dateFormatter));
        dateFTextField.setDocument(new LimitedDocument(13+13));

        lonFTextField.setFormatterFactory(new FormatterFactory(new TopoFormatter(TopoFormatter.LONGITUDE)));
        lonFTextField.setDocument(new LimitedDocument(12));
        latFTextField.setFormatterFactory(new FormatterFactory(new TopoFormatter(TopoFormatter.LATITUDE,lonFTextField)));
        latFTextField.setDocument(new LimitedDocument(12+13));
        timezoneFTextField.setFormatterFactory(new FormatterFactory(new TimeZoneFormatter()));
        timezoneFTextField.setValue(timeZone);
        dateFormatter.setTimeZone(timeZone);
        setFocusListener();
        setDaylightDisplay();
    }
    /**
     * カレンダーオブジェクトの値をセットする。
     * 日付フィールド、時刻フィールド、タイムゾーンフィールドを一括でセット。
     * @param cal カレンダーオブジェクトによる日付
     */
    public void setCalendar(GregorianCalendar cal) {
        dateFTextField.setValue(cal);
        timeFTextField.setValue(cal);
        timezoneFTextField.setValue(cal.getTimeZone());
    }
    /**
     * パネル内の日付と日時とタイムゾーンからをGregorianCalendarを作成して返す。
     * 日付フィールドが空の場合はnullを返す。
     * 時刻フィールドがnullの場合はDefaultTimeプロパティから返す。
     * DefaultTimeが未設定の場合はIllegalStateExcepitonを出す。
     */
    public GregorianCalendar getCalendar() {
        GregorianCalendar dateCal = (GregorianCalendar)dateFTextField.getValue();
        if(dateCal == null) return null;
        GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.set(MILLISECOND,0);
        cal.set(ERA,dateCal.get(ERA));
        cal.set(YEAR,dateCal.get(YEAR));
        cal.set(MONTH,dateCal.get(MONTH));
        cal.set(DAY_OF_MONTH,dateCal.get(DAY_OF_MONTH));
        GregorianCalendar timeCal = (GregorianCalendar)timeFTextField.getValue();
        if(timeCal == null) {
            String timeString = Config.getDefaultTime();
            int [] t = TimePlace.getDateParams(timeString);
            cal.set(HOUR_OF_DAY,t[0]);
            cal.set(MINUTE,t[1]);
            cal.set(SECOND,t[2]);
        } else {
            cal.set(HOUR_OF_DAY,timeCal.get(HOUR_OF_DAY));
            cal.set(MINUTE,timeCal.get(MINUTE));
            cal.set(SECOND,timeCal.get(SECOND));
        }
        return cal;
    }
    /**
     * 日付(ERA・年・月・日)をセットする。
     * @param gcal カレンダーオブジェクト
     */
    public void setDate(GregorianCalendar gcal) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.MILLISECOND,0);
        cal.set(Calendar.YEAR, gcal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, gcal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, gcal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.ERA, gcal.get(Calendar.ERA));
        dateFTextField.setValue(cal);
        setDaylightDisplay();
    }
    /**
     * フォームの日付情報を取得する。
     */
    public Date getDate() {
        GregorianCalendar cal =  (GregorianCalendar)dateFTextField.getValue();
        if(cal == null) return null;
        String v = String.format("%tY-%tm-%td", cal, cal, cal);
        return Date.valueOf(v);
        //return new Date(cal.getTimeInMillis());
    }
    /**
     * 日付が紀元前か紀元後かを返す。
     * @return 紀元前なら"BC"、紀元後なら"AD"
     */
    public String getERA() {
        GregorianCalendar cal =  (GregorianCalendar)dateFTextField.getValue();
        if(cal == null) return null;
        return
            ( cal.get(GregorianCalendar.ERA) == GregorianCalendar.BC) ? "BC" : "";
    }
    /**
     * 時刻フィールドに時刻をセット
     * @param time 時刻
     */
    public void setTime(Time time) {
        if(time == null) {
            timeFTextField.setValue(null);
            return;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.MILLISECOND,0);
        //String [] t = time.toString().split(":");
        int [] t = TimePlace.getDateParams(time.toString());
        cal.set(Calendar.HOUR_OF_DAY,t[0]);
        cal.set(Calendar.MINUTE,t[1]);
        cal.set(Calendar.SECOND,t[2]);
        timeFTextField.setValue(cal);
    }
    /**
     * カレンダーオブジェクトで時間フィールドをセットする。
     */
    public void setTime(GregorianCalendar gcal) {
        if(gcal == null) {
            timeFTextField.setValue(null);
            return;
        }
//    cal.set(Calendar.HOUR_OF_DAY,gcal.get(Calendar.HOUR_OF_DAY));
//    cal.set(Calendar.MINUTE, gcal.get(Calendar.MINUTE));
//    cal.set(Calendar.SECOND, gcal.get(Calendar.SECOND));
//    timeFTextField.setValue(cal);
        timeFTextField.setValue(gcal);
    }
    /**
     * 時刻フィールドの値を返す。
     */
    public Time getTime() {
        GregorianCalendar cal = (GregorianCalendar)timeFTextField.getValue();
        if(cal == null) return null;
        return Time.valueOf(String.format("%tT",cal));
    }
    /**
     * 経度フィールドの値を返す。
     */
    public Double getLongitude() {
        return (Double)lonFTextField.getValue();
    }
    /**
     * 経度フィールドに値をセットする。
     */
    public void setLongitude(Double value) {
        lonFTextField.setValue(value);
    }
    /**
     * 緯度フィールドの値を返す。
     */
    public Double getLatitude() {
        return (Double)latFTextField.getValue();
    }
    /**
     * 緯度フィールドに値をセットする
     */
    public void setLatitude(Double value) {
        latFTextField.setValue(value);
    }
    /**
     * タイムゾーンフィールドの値を返す。
     */
    public TimeZone getTimeZone() {
        //return cal.getTimeZone();
        return timeZone;
    }
    /**
     * タイムゾーンフィールドに値をセットする。
     */
    public void setTimeZone(TimeZone tz) {
        this.timeZone = tz;
        //cal.setTimeZone(timezone);
        //timezoneFTextField.setValue(cal.getTimeZone());
        timezoneFTextField.setValue(timeZone);
        setDaylightDisplay();
    }
    /**
     * 地名フィールドの値を返す。
     */
    public String getPlaceName() {
        return placenameTextField.getText();
    }
    /**
     * 地名フィールドに値をセットする。
     */
    public void setPlaceName(String placeName) {
        placenameTextField.setText(placeName);
    }
    /**
     * 場所をセットする
     */
    public void setPlace(Place p) {
        setTimeZone( p.getTimeZone() );
        setPlaceName( p.getPlaceName() );
        setLatitude( p.getLatitude() );
        setLongitude( p.getLongitude() );
    }
    /**
     * occに含まれる日付、時間、地名、緯度、経度、タイムゾーンを各フィールドにセットする。
     */
    public void setTimePlace(TimePlace tp) {
        //最初にタイムゾーンを設定しないと、最後に設定するとすでにセットされた日付が変わってしまう。
        //同じＴＺなら問題ないのだがちがうものに変更するとアウト
        setTimeZone( tp.getTimeZone() );
        //System.out.println("TimeZone ID = " + tp.getTimeZone().getID());
        setDate(tp.getCalendar());
        setTime( tp.getTime() );
        setPlaceName( tp.getPlaceName() );
        setLatitude( tp.getLatitude() );
        setLongitude( tp.getLongitude() );
    }
    /**
     * 入力フィールドの値を引数tpに書き込み、tpの参照を返す。
     * tpがnullのときは内部で新しくインスタンスが作成されそれに書き込まれその参照を返す。
     */
    public TimePlace getTimePlace(TimePlace tp) {
        if(tp == null) tp = new TimePlace();
        tp.setTimeZone(getTimeZone());
        tp.setDate(getDate(),getERA());
        tp.setTime(getTime());
        tp.setPlaceName(getPlaceName());
        tp.setLatitude(getLatitude());
        tp.setLongitude(getLongitude());
        return tp;
    }
    // 夏時間を考慮したタイムゾーン名の表示
    void setDaylightDisplay() {
        timeOffsetLabel.setText(getUTCOffset(timeZone.getRawOffset()));
        if(getDate() == null) {
            zoneNameTextField.setText(timeZone.getDisplayName(false,TimeZone.LONG));
            return;
        }
        GregorianCalendar gcal = new GregorianCalendar(timeZone);
        gcal.set(MILLISECOND,0);
        GregorianCalendar cal =  (GregorianCalendar)dateFTextField.getValue();
        gcal.set(ERA,cal.get(ERA));
        gcal.set(YEAR,cal.get(YEAR));
        gcal.set(MONTH,cal.get(MONTH));
        gcal.set(DAY_OF_MONTH,cal.get(DAY_OF_MONTH));
        gcal.set(HOUR_OF_DAY,cal.get(HOUR_OF_DAY));
        gcal.set(MINUTE,cal.get(MINUTE));
        gcal.set(SECOND,cal.get(SECOND));

        boolean isDaylightTime =
            timeZone.useDaylightTime() && timeZone.inDaylightTime(gcal.getTime());
        String tzName = timeZone.getDisplayName(isDaylightTime, TimeZone.LONG);
        zoneNameTextField.setText(tzName);
        if(isDaylightTime)
            timeOffsetLabel.setText(getUTCOffset(timeZone.getRawOffset() +
                timeZone.getDSTSavings()));
    }
    String getUTCOffset(long t) {
        String sign = ( t < 0 ) ? "-":"+";
        t = Math.abs(t);
        int sec = (int)(t / 1000L);
        int hour   = sec / 3600;
        int minute = (sec % 3600) / 60;
        return String.format("UTC%s%02d:%02d",sign,hour,minute);
    }
    /**
     * このパネル内のボタンとフィールドすべてにキーリスナを登録する。
     * ESCキーなどでダイアログを閉じたり、その他のキーイベントハンドラを
     * すべてのフォーカスを受け取る部品にセットする。
     */
    public void addKeyListener(KeyListener l) {
        nowButton.addKeyListener(l);
        placeButton.addKeyListener(l);
        zoneButton.addKeyListener(l);
        //
        dateFTextField.addKeyListener(l);
        latFTextField.addKeyListener(l);
        lonFTextField.addKeyListener(l);
        placenameTextField.addKeyListener(l);
        timeFTextField.addKeyListener(l);
    }
    //日付入力が行われたとき、夏時間制を導入している地域の場合、ゾーン名が変化する。
    //日付入力完了を検出する確実な方法がないので、このパネルの入力フォームがﾌｫｰｶｽ
    //を取得したりﾛｽﾄしたﾀｲﾐﾝｸﾞでｿﾞｰﾝ名の表示を更新するようにしている。
    //ただし完全とはいえない。
    void setFocusListener() {
        FocusListener l = new FocusListener() {
            public void focusGained(FocusEvent evt) { setDaylightDisplay(); }
            public void focusLost(FocusEvent evt) { setDaylightDisplay(); }
        };
        nowButton.addFocusListener(l);
        placeButton.addFocusListener(l);
        zoneButton.addFocusListener(l);
        dateFTextField.addFocusListener(l);
        latFTextField.addFocusListener(l);
        lonFTextField.addFocusListener(l);
        placenameTextField.addFocusListener(l);
        timeFTextField.addFocusListener(l);
    }
    /**
     * デフォルトの観測地、タイムゾーンと、現在の日時でフィールドを満たす。
     * デフォルトの観測地が設定されていない場合はConst.DEFAULT_PLACEの値が採用
     * される。
     * @exception IllegalStateException デフォルト観測地プロパティ(DefaultTransitPlace)が未定義の場合。
     */
    public void setDefault() {
        //トランジットに現時刻とデフォルトの場所情報をセット
        Place p = Config.usr.getPlace( "DefaultTransitPlace" );
        //Place p = PrefUtils.getPlace(Conf.data, "DefaultTransitPlace");
        setPlace(p);
        GregorianCalendar cal = new GregorianCalendar();
        setDate(cal);
        setTime(new Time(cal.getTimeInMillis()));
    }
    /**
     * 時間を加算する。
     * 各引数の値に負数を指定すると減算も行われる。
     * @return 加算後のカレンダーオブジェクト
     */
    public GregorianCalendar addCalendar(int day,int hour,int minute,int second) {
        GregorianCalendar cal = getCalendar();
        if(second != 0) cal.add( Calendar.SECOND, second );
        if(minute != 0) cal.add( Calendar.MINUTE, minute );
        if(hour != 0) cal.add( Calendar.HOUR_OF_DAY, hour );
        if(day != 0) cal.add( Calendar.DAY_OF_MONTH, day );
        setCalendar(cal);
        return cal;
    }
    /**
     * このパネルのEnabled状態をセットする。
     * @param b falseなら入力フィールドやボタンがすべてDisenabledになり入力不可
     * となる。trueならすべてenabledになり入力可能になる。
     */
    public void setEnabled( boolean b ) {
        nowButton.setEnabled( b );
        placeButton.setEnabled( b );
        zoneButton.setEnabled( b );
        dateFTextField.setEnabled( b );
        timeFTextField.setEnabled( b );
        placenameTextField.setEnabled( b );
        latFTextField.setEnabled( b );
        lonFTextField.setEnabled( b );
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dateFTextField = new javax.swing.JFormattedTextField();
        timeFTextField = new javax.swing.JFormattedTextField();
        placenameTextField = new javax.swing.JFormattedTextField();
        latFTextField = new javax.swing.JFormattedTextField();
        lonFTextField = new javax.swing.JFormattedTextField();
        timezoneFTextField = new javax.swing.JFormattedTextField();
        zoneButton = new javax.swing.JButton();
        placeButton = new javax.swing.JButton();
        javax.swing.JLabel dateLabel = new javax.swing.JLabel();
        javax.swing.JLabel timeLabel = new javax.swing.JLabel();
        javax.swing.JLabel placeLabel = new javax.swing.JLabel();
        javax.swing.JLabel latLabel = new javax.swing.JLabel();
        javax.swing.JLabel lonLabel = new javax.swing.JLabel();
        javax.swing.JLabel zoneLabel = new javax.swing.JLabel();
        nowButton = new javax.swing.JButton();
        timeOffsetLabel = new javax.swing.JLabel();
        zoneNameTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        setLayout(new java.awt.GridBagLayout());

        dateFTextField.setColumns(10);
        dateFTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateFTextFieldActionPerformed(evt);
            }
        });
        dateFTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dateFTextFieldFocusLost(evt);
            }
        });
        dateFTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                dateFTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(dateFTextField, gridBagConstraints);

        timeFTextField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(timeFTextField, gridBagConstraints);

        placenameTextField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(placenameTextField, gridBagConstraints);

        latFTextField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(latFTextField, gridBagConstraints);

        lonFTextField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(lonFTextField, gridBagConstraints);

        timezoneFTextField.setColumns(10);
        timezoneFTextField.setEditable(false);
        timezoneFTextField.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        add(timezoneFTextField, gridBagConstraints);

        zoneButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/jisaNormal.png"))); // NOI18N
        zoneButton.setMnemonic('Z');
        zoneButton.setToolTipText("世界のタイムゾーンを選択");
        zoneButton.setBorder(null);
        zoneButton.setBorderPainted(false);
        zoneButton.setContentAreaFilled(false);
        zoneButton.setFocusPainted(false);
        zoneButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        zoneButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/jisaPressed.png"))); // NOI18N
        zoneButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/jisaRollover.png"))); // NOI18N
        zoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoneButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 0);
        add(zoneButton, gridBagConstraints);

        placeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/earthNormal.png"))); // NOI18N
        placeButton.setMnemonic('G');
        placeButton.setToolTipText("観測地データベースから選択");
        placeButton.setBorder(null);
        placeButton.setBorderPainted(false);
        placeButton.setContentAreaFilled(false);
        placeButton.setFocusPainted(false);
        placeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        placeButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/earthPressed.png"))); // NOI18N
        placeButton.setRolloverEnabled(true);
        placeButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/earthRollover.png"))); // NOI18N
        placeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                placeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 0);
        add(placeButton, gridBagConstraints);

        dateLabel.setText("日付");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(dateLabel, gridBagConstraints);

        timeLabel.setText("時刻");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(timeLabel, gridBagConstraints);

        placeLabel.setText("地名");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(placeLabel, gridBagConstraints);

        latLabel.setText("緯度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(latLabel, gridBagConstraints);

        lonLabel.setText("経度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(lonLabel, gridBagConstraints);

        zoneLabel.setText("時差");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        add(zoneLabel, gridBagConstraints);

        nowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowNormal.png"))); // NOI18N
        nowButton.setMnemonic('T');
        nowButton.setToolTipText("現在の日時をセット");
        nowButton.setBorderPainted(false);
        nowButton.setContentAreaFilled(false);
        nowButton.setFocusPainted(false);
        nowButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nowButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowPressed.png"))); // NOI18N
        nowButton.setRolloverEnabled(true);
        nowButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowRollover.png"))); // NOI18N
        nowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nowButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 0);
        add(nowButton, gridBagConstraints);

        timeOffsetLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(timeOffsetLabel, gridBagConstraints);

        zoneNameTextField.setColumns(10);
        zoneNameTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(zoneNameTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void dateFTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateFTextFieldActionPerformed
//    System.out.println("ｱｸｼｮﾝｲﾍﾞﾝﾄ発生！");
//    this.setDaylightDisplay();
  }//GEN-LAST:event_dateFTextFieldActionPerformed

  private void dateFTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dateFTextFieldKeyTyped
      //未使用
  }//GEN-LAST:event_dateFTextFieldKeyTyped
  //日付入力がされてTABが押されると時計ﾎﾞﾀﾝにﾌｫｰｶｽが行くがそれをやめて時間入力にﾌｫｰｶｽが移るようにしている
  private void dateFTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dateFTextFieldFocusLost
      //ただShift-TABがおされたら時計ﾎﾞﾀﾝにﾌｫｰｶｽは移ってしまう。つまりまだ不完全。
      timeFTextField.requestFocus();
  }//GEN-LAST:event_dateFTextFieldFocusLost
  // 今ボタンが押されたら現在時刻を入力
  private void nowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nowButtonActionPerformed
      //TimeZone tz = cal.getTimeZone(); //タイムゾーンは退避
      //cal = new GregorianCalendar(tz);
      GregorianCalendar cal = new GregorianCalendar(timeZone);
      dateFTextField.setValue(cal);
      timeFTextField.setValue(cal);
  }//GEN-LAST:event_nowButtonActionPerformed
  //場所ダイアログを開き、場所を入力
  private void placeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_placeButtonActionPerformed
      Place place = PlaceChooser.showDialog(this);
      if(place == null) return; //地名選択中止
      placenameTextField.setText(place.getPlaceName());
      latFTextField.setValue(place.getLatitude());
      lonFTextField.setValue(place.getLongitude());
      //cal.setTimeZone(place.getTimeZone());
      timeZone = place.getTimeZone();
      timezoneFTextField.setValue(timeZone);
  }//GEN-LAST:event_placeButtonActionPerformed
  //タイムゾーンダイアログでタイムゾーンを入力
  private void zoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoneButtonActionPerformed
      TimeZone tz = TimeZoneDialog.showDialog(this);
      if(tz == null) return;
      dateFormatter.setTimeZone(tz); //JD入力を可能にするために引き渡しているだけ
      setTimeZone(tz);
  }//GEN-LAST:event_zoneButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField dateFTextField;
    private javax.swing.JFormattedTextField latFTextField;
    private javax.swing.JFormattedTextField lonFTextField;
    private javax.swing.JButton nowButton;
    private javax.swing.JButton placeButton;
    private javax.swing.JFormattedTextField placenameTextField;
    private javax.swing.JFormattedTextField timeFTextField;
    private javax.swing.JLabel timeOffsetLabel;
    private javax.swing.JFormattedTextField timezoneFTextField;
    private javax.swing.JButton zoneButton;
    private javax.swing.JTextField zoneNameTextField;
    // End of variables declaration//GEN-END:variables

}
