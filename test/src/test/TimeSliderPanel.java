/*
 * TimeSliderPanel.java
 *
 * Created on 2006/10/11, 19:08
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * ChartInternalFrameの中のTimeControllerPanelの中の時間制御パネル。
 * 主な機能は二つ。<br>
 * 1.スライダやスピナーで設定された時間オフセットを読み取るメソッド。<br>
 * 2.ステップ(+/-)ボタンが押されるたびに登録されているリスナのincrement/decrement
 * メソッドを呼び出す。<br>
 * オート(+/-)ボタンがONになったときは、インターバルタイマーにより、一定周期で
 * increment/decrementメソッドを呼び出す。<br>
 * リスナ側では、呼び出されたときこのパネルで設定されている時間オフセットを
 * getHour/getDay等で読み取り、カレンダーへの加算または減算を行うといった処理をする。<br>
 * 関連 : TimeSliderListener
 */
class TimeSliderPanel extends javax.swing.JPanel implements TimeSliderListener {
  JSlider [] timeSliders = new JSlider[4];
  JSpinner [] timeSpinners = new JSpinner[4];
  Map<String,Integer []> dateMap = new HashMap<String,Integer []>();
  TimeSliderListener listener = this;
  Timer timer;
  int timerCount = 0;
  /** 
   * オブジェクトを作成する。 
   */
  public TimeSliderPanel() {
    initComponents();
    timeComboBox.addActionListener(new TimeComboBoxHandler());
    //Clearボタンのイベント
    offsetResetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for(int i=0; i<timeSliders.length; i++) {
          timeSliders[i].setValue(0);
          timeSpinners[i].setValue(0);
        }
        timeComboBox.setSelectedIndex(0);
        incToggleButton.setSelected(false);
        decToggleButton.setSelected(false);
        intervalSlider.setValue(1);
        setButtonEnabled(true);
      }
    });
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
    //スライダーを配列化して要素番号で参照できるようにする
    timeSliders[0] = secondSlider;
    timeSliders[1] = minuteSlider;
    timeSliders[2] = hourSlider;
    timeSliders[3] = daySlider;
    //スピナーの数値範囲を設定
    secondSpinner.setModel(new SpinnerNumberModel(0,0,59,1));
    minuteSpinner.setModel(new SpinnerNumberModel(0,0,59,1));
    hourSpinner.setModel(new SpinnerNumberModel(0,0,23,1));
    daySpinner.setModel(new SpinnerNumberModel(0,0,365,1));
    //スピナーを配列化して要素番号で参照できるようにする
    timeSpinners[0] = secondSpinner;
    timeSpinners[1] = minuteSpinner;
    timeSpinners[2] = hourSpinner;
    timeSpinners[3] = daySpinner;
    //ｽﾗｲﾀﾞとｽﾋﾟﾅｰにﾘｽﾅを登録
    for(int i=0; i<timeSliders.length; i++) {
      SliderMouseWheelHandler handle = new SliderMouseWheelHandler(i);
      timeSliders[i].addMouseWheelListener(handle);
      timeSliders[i].addChangeListener(handle);
      timeSpinners[i].addChangeListener(new TimeSpinnerHandler(i));
    }
    //ｵｰﾄｲﾝｸﾘﾒﾝﾄのためのｲﾝﾀｰﾊﾞﾙﾀｲﾏｰを設定しｽﾀｰﾄさせる。stop()が呼ばれるまで停止しない。
    timer = new Timer(4,new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        int max = intervalSlider.getMaximum();
        int value = max - intervalSlider.getValue();
        if(timerCount >= value) {
            if(incToggleButton.isSelected()) listener.increment();
            else if(decToggleButton.isSelected()) listener.decrement();
            timerCount = 0;
        }
        timerCount++;
      }
    });
    timer.start();
    incButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        listener.increment();
      }
    });
    decButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        listener.decrement();
      }
    });
    incToggleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(decToggleButton.isSelected()) decToggleButton.setSelected(false);
        setButtonEnabled(!(incToggleButton.isSelected() || decToggleButton.isSelected())); 
      }
    });
    decToggleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(incToggleButton.isSelected()) incToggleButton.setSelected(false);
        setButtonEnabled(!(incToggleButton.isSelected() || decToggleButton.isSelected()));        
      }
    });
    //速度ｽﾗｲﾀﾞへﾎｲｰﾙﾘｽﾅを登録
    intervalSlider.addMouseWheelListener(new IntervalMouseWheelHandler());
    //ｽﾃｯﾌﾟﾎﾞﾀﾝにﾎｲｰﾙﾘｽﾅを登録。これでﾎｲｰﾙで時間を加減できる。
    ButtonMouseWheelHandler bh = new ButtonMouseWheelHandler();
    incButton.addMouseWheelListener(bh);
    decButton.addMouseWheelListener(bh);
  }
//  public void fire() {
//    int max = intervalSlider.getMaximum();
//    int value = max - intervalSlider.getValue();
//    if(timerCount >= value) {
//      if(incToggleButton.isSelected()) listener.increment();
//      else if(decToggleButton.isSelected()) listener.decrement();
//      timerCount = 0;
//    }
//    timerCount++;
//  }

  //時間調整のインクリ/デクリのボタンのEnable状態をまとめてセットする
  private void setButtonEnabled(boolean flag) {
    incButton.setEnabled(flag);
    decButton.setEnabled(flag);
  }  
  /**
   * オートボタンを非選択状態にして自動インクリメントを止める。内部で動いている
   * タイマーは停止しない。ステップボタンがディスイネーブルならイネーブルにする。
   */
  protected void stopTimer() {
    incToggleButton.setSelected(false);
    decToggleButton.setSelected(false);
    setButtonEnabled(true);;
  }
  /**
   * setTimeSliderListenerをセットする。
   */
  protected void setTimeSliderListener(TimeSliderListener l) {
    this.listener = l;
  }
  /**
   * setTimeSliderListenerがセットされるまでこのメソッドが使用される。中身は空。
   */
  public void increment() { 
    System.out.println("(+) : TimeSliderListenerをセットしてください");
  }
  /**
   * setTimeSliderListenerがセットされるまでこのメソッドが使用される。中身は空。
   */
  public void decrement() { 
    System.out.println("(-) : TimeSliderListenerをセットしてください");
  }
  /** このパネルのスピナーの「秒」を返す。 */
  protected int getSecond() {
    return ((Integer)secondSpinner.getValue()).intValue();
  }
  /** このパネルのスピナーの「分」を返す。 */
  protected int getMinute() {
    return ((Integer)minuteSpinner.getValue()).intValue();
  }
  /** このパネルのスピナーの「時」を返す。 */
  protected int getHour() {
    return ((Integer)hourSpinner.getValue()).intValue();
  }
  /** このパネルのスピナーの「日」を返す。 */
  protected int getDay() {
    return ((Integer)daySpinner.getValue()).intValue();
  }
  /** タイマーを停止する。*/
  protected void stop() {
    timer.stop();
  }
  //ﾏｳｽﾎｲｰﾙの回転でｽﾗｲﾀﾞを上下させ、ｽﾋﾟﾅｰにも値をｾｯﾄする。
  private class SliderMouseWheelHandler implements MouseWheelListener,ChangeListener {
    int serial = 0;
    SliderMouseWheelHandler(int serial) {
      this.serial = serial;
    }
    public void mouseWheelMoved(MouseWheelEvent e) {
      JSlider s = (JSlider)e.getSource();
      int max = s.getMaximum();
      int min = s.getMinimum();
      if(e.getWheelRotation() < 0) {
        int tmp = s.getValue() + Math.abs(e.getWheelRotation());        
        tmp =  tmp > max ? max : tmp;
        s.setValue(tmp);
        timeSpinners[serial].setValue(tmp);
      } else if(e.getWheelRotation() > 0) {
        int tmp = s.getValue() - Math.abs(e.getWheelRotation());
        tmp = tmp < min ? min : tmp;
        s.setValue(tmp);
        timeSpinners[serial].setValue(tmp);
      }
    }
    public void stateChanged(ChangeEvent e) {
      JSlider s = (JSlider)e.getSource();
      timeSpinners[serial].setValue( s.getValue() );
    }    
  }
  //ｽﾋﾟﾅｰが変更されたらｽﾗｲﾀﾞｰにも値をｾｯﾄする。
  private class TimeSpinnerHandler implements ChangeListener {
    int serial = 0;
    TimeSpinnerHandler(int serial) {
      this.serial = serial;
    }
    public void stateChanged(ChangeEvent e) {
      JSpinner s = (JSpinner)e.getSource();
      int value = ((Integer)s.getValue()).intValue();
      int max = timeSliders[serial].getMaximum();
      int min = timeSliders[serial].getMinimum();
      if(value <= max && value >= min)
        timeSliders[serial].setValue(value);
    }        
  }
  //ｺﾝﾎﾞﾎﾞｯｸｽで時間を選択されたらｽﾗｲﾀﾞｰとｽﾋﾟﾅｰにそれをｾｯﾄする。
  private class TimeComboBoxHandler implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      JComboBox combo = (JComboBox)evt.getSource();
      String time = (String)combo.getSelectedItem();
      Integer [] figure = dateMap.get(time);
      if(figure == null) return;
      for(int i=0; i<timeSliders.length;i++) {
        timeSliders[i].setValue(figure[i]);
        timeSpinners[i].setValue(figure[i]);
      }
    }
  }
  //ｲﾝﾀｰﾊﾞﾙﾀｲﾏｰのｽﾗｲﾀﾞｰをﾎｲｰﾙで上下させるハンドラ
  private class IntervalMouseWheelHandler  implements MouseWheelListener {
    public void mouseWheelMoved(MouseWheelEvent e) {
      JSlider s = (JSlider)e.getSource();
      int max = s.getMaximum();
      int min = s.getMinimum();
      if(e.getWheelRotation() < 0) {
        int tmp = s.getValue() + Math.abs(e.getWheelRotation());        
        tmp =  tmp > max ? max : tmp;
        s.setValue(tmp);
      } else if(e.getWheelRotation() > 0) {
        int tmp = s.getValue() - Math.abs(e.getWheelRotation());
        tmp = tmp < min ? min : tmp;
        s.setValue(tmp);
      }
    }    
  }
  //ｽﾃｯﾌﾟﾎﾞﾀﾝの上でﾏｳｽﾎｲｰﾙを回して、時間を進めたり戻したりする
  private class ButtonMouseWheelHandler implements MouseWheelListener {
    public void mouseWheelMoved(MouseWheelEvent e) {
      if(incButton.isEnabled() && decButton.isEnabled()) {
        if(e.getWheelRotation() < 0) 
          listener.increment();
        else if(e.getWheelRotation() > 0)
          listener.decrement();
      }
    }
  }
  /**
   * このパネルのスライダーやスピナーなど部品すべてにリスナを登録する。
   */
  public void addKeyListener(KeyListener l) {
    super.addKeyListener(l);
    decButton.addKeyListener(l);
    decToggleButton.addKeyListener(l);
    incButton.addKeyListener(l);
    incToggleButton.addKeyListener(l);
    intervalSlider.addKeyListener(l);
    offsetResetButton.addKeyListener(l);
    secondSpinner.addKeyListener(l);
    minuteSpinner.addKeyListener(l);
    hourSpinner.addKeyListener(l);
    daySpinner.addKeyListener(l);
    secondSlider.addKeyListener(l);
    minuteSlider.addKeyListener(l);
    hourSlider.addKeyListener(l);
    daySlider.addKeyListener(l);
    timeComboBox.addKeyListener(l);
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel buttonPanel;
    javax.swing.JPanel degitalPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel10;
    javax.swing.JLabel jLabel11;
    javax.swing.JLabel jLabel12;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel7;
    javax.swing.JLabel jLabel8;
    javax.swing.JLabel jLabel9;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel jPanel5;
    javax.swing.JPanel jPanel6;
    javax.swing.JPanel jPanel7;
    javax.swing.JPanel jPanel8;
    javax.swing.JPanel sliderPanel;

    jPanel8 = new javax.swing.JPanel();
    jPanel6 = new javax.swing.JPanel();
    buttonPanel = new javax.swing.JPanel();
    jPanel7 = new javax.swing.JPanel();
    incButton = new javax.swing.JButton();
    decButton = new javax.swing.JButton();
    jPanel5 = new javax.swing.JPanel();
    incToggleButton = new javax.swing.JToggleButton();
    decToggleButton = new javax.swing.JToggleButton();
    jPanel3 = new javax.swing.JPanel();
    jPanel4 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    intervalSlider = new javax.swing.JSlider();
    jPanel2 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    degitalPanel = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jLabel10 = new javax.swing.JLabel();
    jLabel11 = new javax.swing.JLabel();
    jLabel12 = new javax.swing.JLabel();
    daySpinner = new javax.swing.JSpinner();
    hourSpinner = new javax.swing.JSpinner();
    minuteSpinner = new javax.swing.JSpinner();
    secondSpinner = new javax.swing.JSpinner();
    sliderPanel = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    jLabel9 = new javax.swing.JLabel();
    daySlider = new javax.swing.JSlider();
    hourSlider = new javax.swing.JSlider();
    minuteSlider = new javax.swing.JSlider();
    secondSlider = new javax.swing.JSlider();
    timeComboBox = new javax.swing.JComboBox();
    offsetResetButton = new javax.swing.JButton();

    setLayout(new java.awt.GridLayout(1, 0));

    jPanel8.setLayout(new java.awt.GridBagLayout());

    jPanel6.setLayout(new java.awt.GridLayout(1, 0));

    jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    buttonPanel.setLayout(new java.awt.GridBagLayout());

    jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 1));

    jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "\u30b9\u30c6\u30c3\u30d7", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
    incButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/plus.png")));
    incButton.setMnemonic('J');
    incButton.setToolTipText("<html>\n\u6642\u9593\u3092\u30a4\u30f3\u30af\u30ea\u30e1\u30f3\u30c8 <font size=2>Alt-J</font><br>\n\u30de\u30a6\u30b9\u30db\u30a4\u30fc\u30eb\u3082\u4f7f\u7528\u53ef\n</htm>");
    incButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
    jPanel7.add(incButton);

    decButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/minus.png")));
    decButton.setMnemonic('K');
    decButton.setToolTipText("<html>\n\u6642\u9593\u3092\u30c7\u30af\u30ea\u30e1\u30f3\u30c8 <font size=2>Alt-K</font><br>\n\u30de\u30a6\u30b9\u30db\u30a4\u30fc\u30eb\u3082\u4f7f\u7528\u53ef\n</htm>");
    decButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
    jPanel7.add(decButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    buttonPanel.add(jPanel7, gridBagConstraints);

    jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 1));

    jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "\u30aa\u30fc\u30c8", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
    incToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/plus.png")));
    incToggleButton.setMnemonic('U');
    incToggleButton.setToolTipText("<html>\n\u30aa\u30fc\u30c8\u30a4\u30f3\u30af\u30ea\u30e1\u30f3\u30c8 <font size=2>Alt-U</font><br>\n\u518d\u5ea6\u62bc\u305b\u3070\u505c\u6b62\n</html>");
    incToggleButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
    incToggleButton.setMinimumSize(new java.awt.Dimension(37, 26));
    incToggleButton.setPreferredSize(new java.awt.Dimension(40, 23));
    jPanel5.add(incToggleButton);

    decToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/minus.png")));
    decToggleButton.setMnemonic('I');
    decToggleButton.setToolTipText("<html>\n\u30aa\u30fc\u30c8\u30c7\u30af\u30ea\u30e1\u30f3\u30c8 <font size=2>Alt-I</font><br>\n\u518d\u5ea6\u62bc\u305b\u3070\u505c\u6b62\n</html>");
    decToggleButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
    decToggleButton.setMinimumSize(new java.awt.Dimension(37, 26));
    decToggleButton.setPreferredSize(new java.awt.Dimension(40, 23));
    jPanel5.add(decToggleButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    buttonPanel.add(jPanel5, gridBagConstraints);

    jPanel6.add(buttonPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
    jPanel8.add(jPanel6, gridBagConstraints);

    jPanel3.setLayout(new java.awt.GridLayout(1, 0));

    jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jPanel4.setLayout(new java.awt.GridBagLayout());

    jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    jLabel1.setText("\u901f\u5ea6");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    jPanel4.add(jLabel1, gridBagConstraints);

    intervalSlider.setMinorTickSpacing(10);
    intervalSlider.setOrientation(javax.swing.JSlider.VERTICAL);
    intervalSlider.setToolTipText("\u30a2\u30cb\u30e1\u30fc\u30b7\u30e7\u30f3\u306e\u901f\u5ea6\u3092\u53ef\u5909");
    intervalSlider.setPreferredSize(new java.awt.Dimension(20, 100));
    jPanel4.add(intervalSlider, new java.awt.GridBagConstraints());

    jPanel3.add(jPanel4);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    jPanel8.add(jPanel3, gridBagConstraints);

    jPanel2.setLayout(new java.awt.GridLayout(1, 0));

    jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 4, 8, 2));
    degitalPanel.setLayout(new java.awt.GridBagLayout());

    jLabel3.setText("\u65e5");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    degitalPanel.add(jLabel3, gridBagConstraints);

    jLabel10.setText("\u6642");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    degitalPanel.add(jLabel10, gridBagConstraints);

    jLabel11.setText("\u5206");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    degitalPanel.add(jLabel11, gridBagConstraints);

    jLabel12.setText("\u79d2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    degitalPanel.add(jLabel12, gridBagConstraints);

    daySpinner.setPreferredSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    degitalPanel.add(daySpinner, gridBagConstraints);

    hourSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    degitalPanel.add(hourSpinner, gridBagConstraints);

    minuteSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    degitalPanel.add(minuteSpinner, gridBagConstraints);

    secondSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    degitalPanel.add(secondSpinner, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
    jPanel1.add(degitalPanel, gridBagConstraints);

    sliderPanel.setLayout(new java.awt.GridBagLayout());

    jLabel2.setText("\u65e5");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    sliderPanel.add(jLabel2, gridBagConstraints);

    jLabel7.setText("\u6642");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    sliderPanel.add(jLabel7, gridBagConstraints);

    jLabel8.setText("\u5206");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    sliderPanel.add(jLabel8, gridBagConstraints);

    jLabel9.setText("\u79d2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    sliderPanel.add(jLabel9, gridBagConstraints);

    daySlider.setMaximum(365);
    daySlider.setOrientation(javax.swing.JSlider.VERTICAL);
    daySlider.setPaintLabels(true);
    daySlider.setValue(0);
    daySlider.setPreferredSize(new java.awt.Dimension(22, 70));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    sliderPanel.add(daySlider, gridBagConstraints);

    hourSlider.setMaximum(24);
    hourSlider.setOrientation(javax.swing.JSlider.VERTICAL);
    hourSlider.setValue(0);
    hourSlider.setPreferredSize(new java.awt.Dimension(22, 70));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    sliderPanel.add(hourSlider, gridBagConstraints);

    minuteSlider.setMaximum(59);
    minuteSlider.setOrientation(javax.swing.JSlider.VERTICAL);
    minuteSlider.setValue(0);
    minuteSlider.setPreferredSize(new java.awt.Dimension(22, 70));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    sliderPanel.add(minuteSlider, gridBagConstraints);

    secondSlider.setMaximum(59);
    secondSlider.setOrientation(javax.swing.JSlider.VERTICAL);
    secondSlider.setValue(0);
    secondSlider.setPreferredSize(new java.awt.Dimension(22, 70));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    sliderPanel.add(secondSlider, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    jPanel1.add(sliderPanel, gridBagConstraints);

    timeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\u6642\u9593\u9078\u629e", "1\u5206", "4\u5206", "10\u5206", "1\u6642\u9593", "1\u65e5", "10\u65e5", "30\u65e5", "365\u65e5", "\u6052\u661f\u65e5", "\u6052\u661f\u6708", "\u592a\u967d\u5e74" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    jPanel1.add(timeComboBox, gridBagConstraints);

    offsetResetButton.setMnemonic('C');
    offsetResetButton.setText("Clear");
    offsetResetButton.setToolTipText("\u30aa\u30d5\u30bb\u30c3\u30c8\u30bf\u30a4\u30e0\u3092\u30ea\u30bb\u30c3\u30c8");
    offsetResetButton.setMargin(new java.awt.Insets(2, 3, 3, 2));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
    jPanel1.add(offsetResetButton, gridBagConstraints);

    jPanel2.add(jPanel1);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
    jPanel8.add(jPanel2, gridBagConstraints);

    add(jPanel8);

  }// </editor-fold>//GEN-END:initComponents

    public void store() {
    }
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JSlider daySlider;
  private javax.swing.JSpinner daySpinner;
  private javax.swing.JButton decButton;
  private javax.swing.JToggleButton decToggleButton;
  private javax.swing.JSlider hourSlider;
  private javax.swing.JSpinner hourSpinner;
  private javax.swing.JButton incButton;
  private javax.swing.JToggleButton incToggleButton;
  private javax.swing.JSlider intervalSlider;
  private javax.swing.JSlider minuteSlider;
  private javax.swing.JSpinner minuteSpinner;
  private javax.swing.JButton offsetResetButton;
  private javax.swing.JSlider secondSlider;
  private javax.swing.JSpinner secondSpinner;
  private javax.swing.JComboBox timeComboBox;
  // End of variables declaration//GEN-END:variables
  
}
