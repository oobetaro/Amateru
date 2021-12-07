/*
 * TimeManeuverPanel.java
 *
 * Created on 2006/10/11, 19:08
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.Timer;
import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.GregorianDateFormatter;
import to.tetramorph.starbase.formatter.LimitedDocument;
import to.tetramorph.starbase.formatter.TimeFormatter;
import to.tetramorph.starbase.widget.NumberField;
import static java.util.GregorianCalendar.*;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import to.tetramorph.starbase.lib.TimePlace;

/**
 * ChartInternalFrameの中のTimeControllerPanelの中の時間制御パネル。
 * 主な機能は二つ。<br>
 * 1.設定された時間オフセットを読み取るメソッド。<br>
 * 2.ステップ(+/-)ボタンが押されるたびに登録されているリスナのincrement/decrement
 * メソッドを呼び出す。<br>
 * オート(+/-)ボタンがONになったときは、インターバルタイマーにより、一定周期で
 * increment/decrementメソッドを呼び出す。<br>
 * リスナ側では、呼び出されたときこのパネルで設定されている時間オフセットを
 * getHour/getDay等で読み取り、カレンダーへの加算または減算を行うといった処理をする。<br>
 * 関連 : TimeManeuverListener
 */
class TimeManeuverPanel extends javax.swing.JPanel {

    NumberField [] numberFields;
    Map<String,Integer []> dateMap = new HashMap<String,Integer []>();
    TimeManeuverListener listener = null;
    Timer timer;
    int timerCount = 0;
    GregorianDateFormatter dateFormatter;
    TimeZone timeZone;
    AbstractButton maneuverButton;
    boolean animationActivated = true;
    public TimeManeuverPanel() {
        this(null);
    }
    /**
     * オブジェクトを作成する。
     */
    public TimeManeuverPanel(AbstractButton maneuverButton) {
        this.maneuverButton = maneuverButton;
        initComponents();
        timeFTextField.setFormatterFactory(
            new FormatterFactory( new TimeFormatter() ) );
        timeFTextField.setDocument( new LimitedDocument(12) );
        dateFormatter = new GregorianDateFormatter( timeFTextField );
        dateFTextField.setFormatterFactory(
            new FormatterFactory( dateFormatter ) );
        dateFTextField.setDocument( new LimitedDocument( 13 + 13 ) );

        timeComboBox.addActionListener( new TimeComboBoxHandler() );

        numberFields = new NumberField[] {
            secondNumberField,minuteNumberField,hourNumberField,dayNumberField
        };
        secondNumberField.setMaximum(59);
        minuteNumberField.setMaximum(59);
        hourNumberField.setMaximum(23);
        dayNumberField.setMaximum(365);

        //コンボボックスの文字列を時間オフセットに変換するハッシュ
        dateMap.put("1分",new Integer [] { 0,1,0,0 } );
        dateMap.put("4分",new Integer [] { 0,4,0,0 } );
        dateMap.put("10分",new Integer [] { 0,10,0,0 } );
        dateMap.put("1時間",new Integer [] { 0,0,1,0 } );
        dateMap.put("1日",new Integer [] { 0,0,0,1 } );
        dateMap.put("10日",new Integer [] { 0,0,0,10 } );
        dateMap.put("30日",new Integer [] { 0,0,0,30 } );
        dateMap.put("365日",new Integer [] { 0,0,0,365 } );
        dateMap.put("恒星日",new Integer [] { 4,56,23,0 } );
        dateMap.put("恒星月",new Integer [] { 5,43,7,27 } );
        dateMap.put("太陽年",new Integer [] { 5,48,5,365 } );

        // ｵｰﾄｲﾝｸﾘﾒﾝﾄのためのｲﾝﾀｰﾊﾞﾙﾀｲﾏｰを設定しｽﾀｰﾄさせる。
        // stop()が呼ばれるまで停止しない。

        timer = new Timer( 4, new ActionListener() {
            int sec = 0;
            @Override
            public void actionPerformed( ActionEvent evt ) {
                if ( clockModeToggleButton.isSelected() ) {
                    //時計モードのとき。現在時刻を10秒ごとにリスナにストアする。
                    GregorianCalendar gcal = new GregorianCalendar( timeZone );
                    gcal.setTimeInMillis( System.currentTimeMillis() );
                    if ( gcal.get( Calendar.SECOND ) != sec ) {
                        sec = gcal.get( Calendar.SECOND );
                        setCalendar( gcal );
                        listener.store();
                    }
                } else {
                    //自動インクリメントモードのとき。
                    int max = intervalSlider.getMaximum();
                    int value = max - intervalSlider.getValue();
                    if ( timerCount >= value && listener != null ) {
                        if ( incToggleButton.isSelected() )
                            listener.increment();
                        else if ( decToggleButton.isSelected() )
                            listener.decrement();
                        timerCount = 0;
                    }
                }
                timerCount++;
            }
        });
        timer.start();
        incButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( listener != null ) listener.increment();
            }
        });
        decButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( listener != null ) listener.decrement();
            }
        });
        incToggleButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( decToggleButton.isSelected() )
                    decToggleButton.setSelected(false);
                setButtonEnabled( ! ( incToggleButton.isSelected() ||
                                      decToggleButton.isSelected() ) );
            }
        });
        decToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( incToggleButton.isSelected() )
                    incToggleButton.setSelected( false );
                setButtonEnabled( ! ( incToggleButton.isSelected() ||
                                      decToggleButton.isSelected() ) );
            }
        });
        //速度ｽﾗｲﾀﾞへﾎｲｰﾙﾘｽﾅを登録
        intervalSlider.addMouseWheelListener(new IntervalMouseWheelHandler());
        //ｽﾃｯﾌﾟﾎﾞﾀﾝにﾎｲｰﾙﾘｽﾅを登録。これでﾎｲｰﾙで時間を加減できる。
        ButtonMouseWheelHandler bh = new ButtonMouseWheelHandler();
        incButton.addMouseWheelListener(bh);
        decButton.addMouseWheelListener(bh);
    }

    //時間調整のインクリ/デクリのボタンのEnable状態をまとめてセットする
    private void setButtonEnabled( boolean flag ) {
        incButton.setEnabled( flag );
        decButton.setEnabled( flag );
        dateFTextField.setEditable( flag );
        timeFTextField.setEditable( flag );
        nowButton.setEnabled( flag );
        setButton.setEnabled( flag );
        resetButton.setEnabled( flag );
        timeComboBox.setEnabled( flag );
        if ( maneuverButton != null ) maneuverButton.setEnabled( flag );
    }

    /**
     * アニメーションの禁止/許可を設定する。デフォルトは許可(true)
     * 禁止(false)にセットするとオートインクリメントのボタンと、スライダーと、
     * 時計モードのボタンがDisenabledになり、アニメーション操作はできなくなる。
     * しかしこのメソッドは、初期の段階で呼ばれる事しか考慮していなくて、
     * アニメーションをやったあとで、途中から禁止というような事は考慮していない。
     */
    protected void setAnimationActivated( boolean b ) {
        animationActivated = b;
        incToggleButton.setEnabled( b );
        decToggleButton.setEnabled( b );
        clockModeToggleButton.setEnabled( b );
        intervalSlider.setEnabled( b );
    }

    /**
     * オートボタンを非選択状態にして自動インクリメントを止める。内部で動いている
     * タイマーは停止しない。ステップボタンがディスイネーブルならイネーブルにする。
     */
    protected void stopTimer() {
        incToggleButton.setSelected( false );
        decToggleButton.setSelected( false );
        setButtonEnabled( true );
        if ( clockModeToggleButton.isSelected() ) {
            clockModeToggleButton.doClick();
        }
    }
    /**
     * setTimeSliderListenerをセットする。
     */
    protected void setTimeManeuverListener(TimeManeuverListener l) {
        this.listener = l;
    }

    /** このパネルのスピナーの「秒」を返す。 */
    protected int getSecond() {
        return secondNumberField.getValue();
    }
    /** このパネルのスピナーの「分」を返す。 */
    protected int getMinute() {
        return minuteNumberField.getValue();
    }
    /** このパネルのスピナーの「時」を返す。 */
    protected int getHour() {
        return hourNumberField.getValue();
    }
    /** このパネルのスピナーの「日」を返す。 */
    protected int getDay() {
        return dayNumberField.getValue();
    }
    /** タイマーを停止する。*/
    protected void stop() {
        timer.stop();
    }

    //ｺﾝﾎﾞﾎﾞｯｸｽで時間を選択されたらNumberFieldにそれをｾｯﾄする。
    private class TimeComboBoxHandler implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            JComboBox combo = (JComboBox)evt.getSource();
            String time = (String)combo.getSelectedItem();
            Integer [] figure = dateMap.get(time);
            if ( figure == null ) return;
            for ( int i = 0; i < numberFields.length; i++ ) {
                numberFields[i].setValue( figure[i] );
            }
        }
    }
    //ｲﾝﾀｰﾊﾞﾙﾀｲﾏｰのｽﾗｲﾀﾞｰをﾎｲｰﾙで上下させるハンドラ
    private class IntervalMouseWheelHandler  implements MouseWheelListener {
        @Override
        public void mouseWheelMoved( MouseWheelEvent e ) {
            JSlider s = (JSlider)e.getSource();
            int max = s.getMaximum();
            int min = s.getMinimum();
            if ( e.getWheelRotation() < 0 ) {
                int tmp = s.getValue() + Math.abs( e.getWheelRotation() );
                tmp =  tmp > max ? max : tmp;
                s.setValue( tmp );
            } else if( e.getWheelRotation() > 0 ) {
                int tmp = s.getValue() - Math.abs( e.getWheelRotation() );
                tmp = tmp < min ? min : tmp;
                s.setValue( tmp );
            }
        }
    }

    //ｽﾃｯﾌﾟﾎﾞﾀﾝの上でﾏｳｽﾎｲｰﾙを回して、時間を進めたり戻したりする
    private class ButtonMouseWheelHandler implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if ( incButton.isEnabled() && decButton.isEnabled() ) {
                if ( listener != null ) {
                    if ( e.getWheelRotation() < 0 )
                        listener.increment();
                    else if ( e.getWheelRotation() > 0 )
                        listener.decrement();
                }
            }
        }
    }

    /**
     * パネル内の日付と日時とタイムゾーンからをGregorianCalendarを作成して返す。
     * 日付フィールドが空の場合はnullを返す。
     * 時刻フィールドがnullの場合はDefaultTimeプロパティから返す。
     * DefaultTimeが未設定の場合はIllegalStateExcepitonを出す。
     */
    public GregorianCalendar getCalendar() {
        GregorianCalendar dateCal = (GregorianCalendar)dateFTextField.getValue();
        if ( dateCal == null ) return null;
        GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.set( MILLISECOND, 0 );
        cal.set( ERA, dateCal.get( ERA ) );
        cal.set( YEAR, dateCal.get( YEAR ) );
        cal.set( MONTH, dateCal.get( MONTH ) );
        cal.set( DAY_OF_MONTH, dateCal.get( DAY_OF_MONTH ) );
        GregorianCalendar timeCal = (GregorianCalendar)timeFTextField.getValue();
        if ( timeCal == null ) {
            String timeString = Config.getDefaultTime();
            int [] t = TimePlace.getDateParams( timeString );
            cal.set( HOUR_OF_DAY, t[0] );
            cal.set( MINUTE, t[1] );
            cal.set( SECOND, t[2] );
        } else {
            cal.set( HOUR_OF_DAY, timeCal.get( HOUR_OF_DAY ) );
            cal.set( MINUTE, timeCal.get( MINUTE ) );
            cal.set( SECOND, timeCal.get( SECOND ) );
        }
        return cal;
    }

    /**
     * カレンダーオブジェクトの値をセットする。
     * 日付フィールド、時刻フィールド、タイムゾーンフィールドを一括でセット。
     * @param cal カレンダーオブジェクトによる日付
     */
    public void setCalendar( GregorianCalendar cal ) {
        dateFTextField.setValue( cal );
        timeFTextField.setValue( cal );
        timeZone = cal.getTimeZone();
    }

    /**
     * タイトル(名前欄)をセットする。
     */
    public void setTitle( String name ) {
        nameLabel.setText( name );
    }

    /**
     * 入力された時間オフセットをリセットする。
     */
    public void reset() {
        for ( int i = 0; i < numberFields.length; i++ ) {
            numberFields[i].setValue( 0 );
        }
        timeComboBox.setSelectedIndex( 0 );
        incToggleButton.setSelected( false );
        decToggleButton.setSelected( false );
        intervalSlider.setValue( 1 );
        setButtonEnabled( true );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel jPanel8 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel6 = new javax.swing.JPanel();
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel7 = new javax.swing.JPanel();
        incButton = new javax.swing.JButton();
        decButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        incToggleButton = new javax.swing.JToggleButton();
        decToggleButton = new javax.swing.JToggleButton();
        jLabel5 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        intervalSlider = new javax.swing.JSlider();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        clockModeToggleButton = new javax.swing.JToggleButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel9 = new javax.swing.JPanel();
        dayNumberField = new to.tetramorph.starbase.widget.NumberField();
        hourNumberField = new to.tetramorph.starbase.widget.NumberField();
        minuteNumberField = new to.tetramorph.starbase.widget.NumberField();
        secondNumberField = new to.tetramorph.starbase.widget.NumberField();
        javax.swing.JLabel timeLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel timeLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel timeLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel timeLabel4 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        timeComboBox = new javax.swing.JComboBox();
        resetButton = new javax.swing.JButton();
        javax.swing.JPanel datePanel = new javax.swing.JPanel();
        javax.swing.JLabel dateLabel = new javax.swing.JLabel();
        javax.swing.JLabel timeLabel = new javax.swing.JLabel();
        dateFTextField = new javax.swing.JFormattedTextField();
        timeFTextField = new javax.swing.JFormattedTextField();
        nowButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        setButton = new javax.swing.JButton();

        setLayout(new java.awt.GridLayout(1, 0));

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel6.setLayout(new java.awt.GridLayout(1, 0));

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        jPanel7.setLayout(new java.awt.GridBagLayout());

        incButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/plus.png"))); // NOI18N
        incButton.setMnemonic('J');
        incButton.setToolTipText("<html>\n時間を加算 <font size=2>Alt-J</font><br>\nマウスホイールも使用可\n</htm>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel7.add(incButton, gridBagConstraints);

        decButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/minus.png"))); // NOI18N
        decButton.setMnemonic('K');
        decButton.setToolTipText("<html>\n時間を減算<font size=2>Alt-K</font><br>\nマウスホイールも使用可\n</htm>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel7.add(decButton, gridBagConstraints);

        jLabel3.setText("ステップ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel7.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        buttonPanel.add(jPanel7, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        incToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/plus.png"))); // NOI18N
        incToggleButton.setMnemonic('U');
        incToggleButton.setToolTipText("<html>\n自動加算 <font size=2>Alt-U</font><br>\n再度押せば停止\n</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel5.add(incToggleButton, gridBagConstraints);

        decToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/minus.png"))); // NOI18N
        decToggleButton.setMnemonic('I');
        decToggleButton.setToolTipText("<html>\n自動減算<font size=2>Alt-I</font><br>\n再度押せば停止\n</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel5.add(decToggleButton, gridBagConstraints);

        jLabel5.setText("オート");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        jPanel5.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        buttonPanel.add(jPanel5, gridBagConstraints);

        jPanel6.add(buttonPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jPanel6, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("遅");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel4.add(jLabel1, gridBagConstraints);

        intervalSlider.setMinorTickSpacing(10);
        intervalSlider.setPaintTicks(true);
        intervalSlider.setToolTipText("アニメーションの速度を可変");
        intervalSlider.setPreferredSize(new java.awt.Dimension(100, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel4.add(intervalSlider, gridBagConstraints);

        jLabel4.setText("速");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jLabel4, gridBagConstraints);

        jLabel6.setText("加算スピード");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel4.add(jLabel6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel3.add(jPanel4, gridBagConstraints);

        clockModeToggleButton.setText("時計モード");
        clockModeToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clockModeToggleButtonActionPerformed(evt);
            }
        });
        jPanel10.add(clockModeToggleButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jPanel10, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel9.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel9.add(dayNumberField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel9.add(hourNumberField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel9.add(minuteNumberField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel9.add(secondNumberField, gridBagConstraints);

        timeLabel1.setText("日");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel9.add(timeLabel1, gridBagConstraints);

        timeLabel2.setText("時");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel9.add(timeLabel2, gridBagConstraints);

        timeLabel3.setText("分");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel9.add(timeLabel3, gridBagConstraints);

        timeLabel4.setText("秒");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel9.add(timeLabel4, gridBagConstraints);

        jPanel2.add(jPanel9, new java.awt.GridBagConstraints());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 4, 2, 2));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        timeComboBox.setMaximumRowCount(12);
        timeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "時間選択", "1分", "4分", "10分", "1時間", "1日", "10日", "30日", "365日", "恒星日", "恒星月", "太陽年" }));
        timeComboBox.setPreferredSize(new java.awt.Dimension(82, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(timeComboBox, gridBagConstraints);

        resetButton.setMnemonic('C');
        resetButton.setText("C");
        resetButton.setToolTipText("オフセットタイムをリセット");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel1.add(resetButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jPanel2, gridBagConstraints);

        datePanel.setLayout(new java.awt.GridBagLayout());

        dateLabel.setText("日付");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        datePanel.add(dateLabel, gridBagConstraints);

        timeLabel.setText("時刻");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 3);
        datePanel.add(timeLabel, gridBagConstraints);

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
        gridBagConstraints.gridy = 1;
        datePanel.add(dateFTextField, gridBagConstraints);

        timeFTextField.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        datePanel.add(timeFTextField, gridBagConstraints);

        nowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowNormal.png"))); // NOI18N
        nowButton.setMnemonic('T');
        nowButton.setToolTipText("現在の日時をセット");
        nowButton.setBorderPainted(false);
        nowButton.setContentAreaFilled(false);
        nowButton.setFocusPainted(false);
        nowButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nowButton.setMaximumSize(new java.awt.Dimension(21, 21));
        nowButton.setMinimumSize(new java.awt.Dimension(21, 21));
        nowButton.setPreferredSize(new java.awt.Dimension(21, 21));
        nowButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowPressed.png"))); // NOI18N
        nowButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/nowRollover.png"))); // NOI18N
        nowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nowButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 0);
        datePanel.add(nowButton, gridBagConstraints);

        jLabel2.setText("名前");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 3);
        datePanel.add(jLabel2, gridBagConstraints);

        nameLabel.setText("name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        datePanel.add(nameLabel, gridBagConstraints);

        setButton.setText("SET");
        setButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        datePanel.add(setButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel8.add(datePanel, gridBagConstraints);

        add(jPanel8);
    }// </editor-fold>//GEN-END:initComponents

    private void clockModeToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clockModeToggleButtonActionPerformed
        if (((JToggleButton)evt.getSource()).isSelected() ) {
            //stopTimer()を呼べば良さそうにみえるが、stopTimerは時計モード停止の
            //役割も持つためここでは呼ばずにコピーしている。
            incToggleButton.setSelected( false );
            decToggleButton.setSelected( false );
            //setButtonEnabled( true );
            setButtonEnabled( false );
            incToggleButton.setEnabled( false );
            decToggleButton.setEnabled( false );
            intervalSlider.setEnabled( false );
        } else {
            setButtonEnabled( true );
            incToggleButton.setEnabled( true );
            decToggleButton.setEnabled( true );
            intervalSlider.setEnabled( true );
        }
    }//GEN-LAST:event_clockModeToggleButtonActionPerformed

  private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
      if ( listener != null ) listener.store();
  }//GEN-LAST:event_setButtonActionPerformed

  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
      reset();
  }//GEN-LAST:event_resetButtonActionPerformed

  private void nowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nowButtonActionPerformed
      GregorianCalendar gc = (GregorianCalendar)dateFTextField.getValue();
      GregorianCalendar cal = new GregorianCalendar(timeZone);
      dateFTextField.setValue(cal);
      timeFTextField.setValue(cal);
  }//GEN-LAST:event_nowButtonActionPerformed

  private void dateFTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dateFTextFieldKeyTyped
//未使用
  }//GEN-LAST:event_dateFTextFieldKeyTyped

  private void dateFTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dateFTextFieldFocusLost
//ただShift-TABがおされたら時計ﾎﾞﾀﾝにﾌｫｰｶｽは移ってしまう。つまりまだ不完全。
      timeFTextField.requestFocus();
  }//GEN-LAST:event_dateFTextFieldFocusLost

  private void dateFTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateFTextFieldActionPerformed
//    System.out.println("ｱｸｼｮﾝｲﾍﾞﾝﾄ発生！");
//    this.setDaylightDisplay();
  }//GEN-LAST:event_dateFTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton clockModeToggleButton;
    private javax.swing.JFormattedTextField dateFTextField;
    private to.tetramorph.starbase.widget.NumberField dayNumberField;
    private javax.swing.JButton decButton;
    private javax.swing.JToggleButton decToggleButton;
    private to.tetramorph.starbase.widget.NumberField hourNumberField;
    private javax.swing.JButton incButton;
    private javax.swing.JToggleButton incToggleButton;
    private javax.swing.JSlider intervalSlider;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel10;
    private to.tetramorph.starbase.widget.NumberField minuteNumberField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton nowButton;
    private javax.swing.JButton resetButton;
    private to.tetramorph.starbase.widget.NumberField secondNumberField;
    private javax.swing.JButton setButton;
    private javax.swing.JComboBox timeComboBox;
    private javax.swing.JFormattedTextField timeFTextField;
    // End of variables declaration//GEN-END:variables

}
