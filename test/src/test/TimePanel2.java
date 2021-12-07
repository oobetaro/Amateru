/*
 * TimePanel2.java
 *
 * Created on 2006/11/28, 0:45
 */

package to.tetramorph.starbase;

import java.awt.GridLayout;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.lib.TransitTabReceiver;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * TimePanel�̓f�[�^�x�[�X�ƃ`���[�g�t���[���𒆌p����B
 * DB�₱�̃p�l���̓��̓t�H�[��������͂��ꂽ�����ꏊ�f�[�^���A�`���[�g���W���[��
 * �Ɉ����n���B�^����ꂽ�f�[�^�ɑ΂��āA���Ԃ�ύX�����莩���C���N�������g/
 * �f�N�������g���ł���B�܂��I�𒆂̃f�[�^�̃q�X�g���[����JMenu�Œ񋟂���B
 * �`���[�g���W���[���ւ̃f�[�^�̈����n����ChannelData�N���X�ōs����B
 * <pre>
 * �I���f�[�^�̃q�X�g���[�̑I�����j���[���擾
 * �@�@JMenu getHistoryMenu() 
 * �`���[�g���W���[���̃Z�b�g
 * �@�@ChartModulePanel getModule() 
 * �@�@void setModule(ChartModulePanel module) 
 * �o���E�o�߃f�[�^�̓o�^�E�ǉ��E�擾
 * �@�@void setNatal(List<Natal> list) 
 * �@�@void addNatal(List<Natal> list) 
 * �@�@void setTransit(Transit transit) 
 * �@�@Transit getTransit() 
 * �@�@ChartData getSelectedChartData() 
 * �@�@void set() 
 * �^�C�}�[����
 * �@�@void stopTimer() 
 * �@�@void stop() 
 * �����p
 * �@�@void dataCopy(TimePanel2 tp) 
 * </pre>
 */
class TimePanel2 extends TimePanel {
//  implements TimeController,TransitTabReceiver,java.io.Serializable  {

  ChartInternalFrame iframe;
  ChartModulePanel module; //set()���Q��
  ChannelData channelData = new ChannelData(this); //ChartModule�Ɉ����n��������ް�
  JMenu historyMenu = null;
  Transit transit = new Transit(); //�g�����W�b�g�^�u�̒l
  //JComponent toolComponent;
  TimeSliderPanel timeSliderPanel;
  /** 
   * GUI�G�f�B�^�̂��߂ɂ���R���X�g���N�^�ł���͎g�p���Ȃ����ƁB
   */
  public TimePanel2() {
    initComponents();
    timeSliderPanel = new TimeSliderPanel();
    timeSliderPanel.setTimeSliderListener(new TimeSliderHandler());
    transitTimePlacePanel.setDefault();
    transitTimePlacePanel.getTimePlace(transit); //�Q�Ƃ�transit�ɏ�������
    channelData.setTransit(transit);
    //toolBar.setLayout(new GridLayout(0,1));
    //toolComponent = voidLabel;
  }
  /**
   * �I�u�W�F�N�g���쐬�BChartInternalFrame����Ăяo�����B
   * @param iframe �ďo���Ƃ̃`���[�g�t���[���̎Q�ƃA�h���X
   */
  public TimePanel2(ChartInternalFrame iframe) {
    this();
    this.iframe = iframe;
  }
  /** �^�C���X���C�_�[�p�l����Ԃ� */
  public TimeSliderPanel getTimeSliderPanel() {
    return timeSliderPanel;
  }
  /**
   * �w�肳�ꂽID��Natal�f�[�^�����̃p�l���ɃZ�b�g����Ă���ꍇ��true��Ԃ��B
   */
  public boolean isComprise(int id) {
    for(int i=0; i<channelData.size(); i++) {
      //ChartDataPanel��isComprise���Ăяo���Ă���B
      if(channelData.get(i).isComprise(id)) return true;
    }
    return false;
  }
  /**
   * ����TimePanel�ɃZ�b�g����Ă���`���[�g���W���[����Ԃ��B
   */
  public ChartModulePanel getModule() {
    return module;
  }
  /**
   * �`���[�g���W���[�����Z�b�g����BTimePanel�ւ̃f�[�^�̓�����setNatal,addNatal
   * �����A������������Ă����f�[�^�̓^�u�̑I������Ă���t�H�[��(ChartDataPanel��
   * �g�����V�b�g�̃t�H�[���ɐU�蕪���ăZ�b�g����B�����Ă����ChannelData�I�u�W
   * �F�N�g�Ƃ��āA�`���[�g���W���[���ɑ���B
   */
  public void setModule(ChartModulePanel module) {
    this.module = module;
    tabbedPane.removeAll();
    while( module.getChannelSize() > channelData.size())
      channelData.add(new ChartDataPanel(this,iframe));
    for(int i=0; i<module.getChannelSize(); i++) {
      tabbedPane.add((ChartDataPanel)channelData.get(i));
      tabbedPane.setTitleAt(i,module.getChannelNames()[i]);
    }
    mainTabbedPane.setEnabledAt(1,module.isNeedTransit());
    revalidate();
    repaint();
  }

  // Spinner�̊e���̒l���J�����_�[�ɑ����A���t�Ǝ��Ԃ�̨���ނɒl��Ă���
  private void addCalendar(int sign) {
    int second = timeSliderPanel.getSecond() * sign;
    int minute = timeSliderPanel.getMinute() * sign;
    int hour = timeSliderPanel.getHour() * sign;
    int day = timeSliderPanel.getDay() * sign;    
    if(mainTabbedPane.getSelectedIndex() == 0) { //�l�C�^���^�u���I��
      ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
      cdp.addCalendar(day,hour,minute,second);
      //set()��cdp���ŃL�b�N�����
    } else {
      transitTimePlacePanel.addCalendar(day,hour,minute,second);
      transitSetButtonActionPerformed(null);
    }
  }
  
//�����N���X�ɂ���̂́A�����̃��\�b�h��JavaDoc�ɏo�������Ȃ�����
  class TimeSliderHandler implements TimeSliderListener {
     //�J�����_�[�Ɏ��Ԃ𑫂�
    public void increment() {
      addCalendar(1);
    }
    //�J�����_�[���玞�Ԃ�����
    public void decrement() {
      addCalendar(-1);
    }

        public void store() {
        }
  }
  /**
   * ���ݑI������Ă���`���[�g���W���[����setData(channelData)���A
   * �C�x���g�L���[���g���ČĂяo���B
   */
  public void set() {
    assert SwingUtilities.isEventDispatchThread();
    assert module != null : "���W���[����null";
    module.setData(channelData);
  }

  /**
   * ����͵�Ĳݸ���Ă��~������Ƃ��̃X�g�b�v���\�b�h�B�����^�C�}�[�͓��삵�Ă���B
   * ���O�������̂ŕύX�����ق����ǂ��B
   */  
  public void stopTimer() {
    timeSliderPanel.stopTimer();
  }
  
  /**
   * �����t���[�����N���[�Y����ۂ��̃��\�b�h���Ăяo���A
   * ���̃I�u�W�F�N�g�̒��œ����Ă���A�j���[�V�����p�^�C�}�[���~�����邱�ƁB
   * ��������O����邢�̂ŕύX�����ق����悢�Bdispose()���������B
   */  
  public void stop() {
    timeSliderPanel.stop();
  }
  
  /**
   * ���̃p�l����Natal�̃��X�g��ǉ�����B���̃��\�b�h�͂��̃I�u�W�F�N�g��
   * ���̓����BID�Ō�������dataList�ɏd������f�[�^������ꍇ�͒ǉ����Ȃ��B
   * list�ł����Ƃ������t���͂��̃I�u�W�F�N�g���ŉ��H����module�ɗ^������B
   */
  public void setNatal(List<Natal> list) {
    if(mainTabbedPane.getSelectedIndex() == 1) {
      setTransit(list.get(0));
    } else 
      ((ChartDataPanel)tabbedPane.getSelectedComponent()).setNatal(list);
  }
  
  /**
   * ���̃p�l����Natal�̃��X�g��ǉ�����B���̃��\�b�h�͂��̃I�u�W�F�N�g��
   * ���̓����BID�Ō�������dataList�ɏd������f�[�^������ꍇ�͒ǉ����Ȃ��B
   * list�ł����Ƃ������t���͂��̃I�u�W�F�N�g���ŉ��H����module�ɗ^������B
   */    
  public void addNatal(List<Natal> list) {
    if(mainTabbedPane.getSelectedIndex() == 1) {
      setTransit(list.get(0));
    } else
      ((ChartDataPanel)tabbedPane.getSelectedComponent()).addNatal(list);
  }
  
  /**
   * �q�X�g���[���j���[��Ԃ��B�g�����V�b�g�������ȃ��W���[���̏ꍇ��Disenable
   * �ɂ��ĕԂ��B
   */
  public JMenu getHistoryMenu() {
    ChartDataPanel cdp = (ChartDataPanel)tabbedPane.getSelectedComponent();
    JMenu menu = cdp.getHistoryMenu();
    //��ݼ�ĕs�v��Ӽޭ�ق�Disenabled�ɂ���B�K�v���ƭ�������Ƃ���enabled�ɁB
    if(module.isNeedTransit() && menu.getMenuComponentCount() > 0)
      menu.setEnabled(true);
    else 
      menu.setEnabled(false);
    return menu;
  }
  /**
   * ���̃p�l���̃g�����V�b�g�^�u�̓�����Ԃ��B
   */
  public Transit getTransit() {
    transitTimePlacePanel.getTimePlace(transit);
    transit.setName(nameTextField.getText().trim());
    transit.setMemo(memoTextField.getText().trim());
    return transit;
  }
  /**
   * �g�����V�b�g�^�u�ɒl���Z�b�g����B
   */
  public void setTransit(Transit transit) {
    this.transit = transit;
    mainTabbedPane.setSelectedIndex(1);
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
    if(transit.getTime() == null)
      transitTimePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    transitSetButtonActionPerformed(null);
  }
  /**
   * �g�����V�b�g�^�u�̒l���Z�b�g����B�����ChannelData�I�u�W�F�N�g�ɂ���
   * �I�u�W�F�N�g(TimePanel2)�������n���ہATransitTabReceiver�Ƃ��ēn�����߂ɂ���B
   * ChannelData#updateTransit()�ɂ���āA���̃p�l���Ƀ`���[�g���W���[������
   * �ύX���ꂽTransit�I�u�W�F�N�g���󂯎��B
   * ���̃N���X��setTransit()�ƈႤ�Ƃ���́A�������ݒ�̏ꍇ�̃f�t�H���g�^�C��
   * �����鏈�������Ă��Ȃ��̂ƁA�`���[�g���W���[���ɑ΂��ẴC�x���g�𔭐�
   * �����Ȃ����ƁB
   */
  public void updateTransit(Transit transit) {
    this.transit = transit;
    mainTabbedPane.setSelectedIndex(1);
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
  }
  /**
   * �l�C�^���^�u�̒��ɂ���`�����l���^�u�̒��őI������Ă���`���[�g�f�[�^��Ԃ��B
   * ���I����ԂȂ�null���Ԃ�B
   */
  public ChartData getSelectedChartData() {
    return (ChartData)tabbedPane.getSelectedComponent();
  }
  /**
   * �����ŗ^����ꂽTimePanel�Ɋi�[����Ă���g�����V�b�g��l�C�^���̃f�[�^��
   * �f�[�v�R�s�[�ŕ������Ă��̃p�l���ɃZ�b�g����B����̓`���[�g�t���[���̕�����
   * �ۂɎg�p�����B
   */
  public void dataCopy(TimePanel tp) {
    ChannelData srcChannel = tp.channelData;
    //��ݼ�Ă𕡐�
    transit = new Transit(tp.getTransit());
    nameTextField.setText(transit.getName());
    memoTextField.setText(transit.getMemo());
    transitTimePlacePanel.setTimePlace(transit);
    if(transit.getTime() == null)
      transit.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    transitTimePlacePanel.setTimePlace(transit);
    channelData.setTransit(transit);
    //����ِ����AChartDataPanel�𕡐�
    for(int i=0; i<srcChannel.size(); i++) {
      ChartDataPanel chartData = new ChartDataPanel(this,iframe);
      //chartData.setTransit(new Transit(srcChannel.get(i).getTransit()));
      List<Data> temp = srcChannel.get(i).getDataList();
      List<Data> dataList = new ArrayList<Data>();
      for(Data d : temp) dataList.add(new Data(d));
      chartData.initDataList(dataList);
      Natal composit = srcChannel.get(i).getComposit();
      if(composit != null) chartData.compositNatal = new Natal(composit);
      channelData.add(chartData);
    }
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
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel transitPanel;

    mainPanel = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    mainTabbedPane = new javax.swing.JTabbedPane();
    tabbedPane = new javax.swing.JTabbedPane();
    transitPanel = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    nameTextField = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    memoTextField = new javax.swing.JTextField();
    transitTimePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
    jPanel4 = new javax.swing.JPanel();
    transitSetButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    mainPanel.setLayout(new java.awt.BorderLayout());

    jPanel1.setLayout(new java.awt.GridBagLayout());

    mainTabbedPane.addTab("\u30cd\u30fc\u30bf\u30eb", tabbedPane);

    transitPanel.setLayout(new java.awt.BorderLayout());

    transitPanel.setFocusable(false);
    jPanel2.setLayout(new java.awt.GridBagLayout());

    jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 18));
    jLabel1.setText("\u540d\u524d");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
    jPanel2.add(jLabel1, gridBagConstraints);

    nameTextField.setPreferredSize(new java.awt.Dimension(116, 19));
    jPanel2.add(nameTextField, new java.awt.GridBagConstraints());

    jLabel2.setText("\u30e1\u30e2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
    jPanel2.add(jLabel2, gridBagConstraints);

    memoTextField.setPreferredSize(new java.awt.Dimension(116, 19));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(memoTextField, gridBagConstraints);

    transitPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

    transitPanel.add(transitTimePlacePanel, java.awt.BorderLayout.CENTER);

    jPanel4.setFocusable(false);
    transitSetButton.setMnemonic('S');
    transitSetButton.setText("Set");
    transitSetButton.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    transitSetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        transitSetButtonActionPerformed(evt);
      }
    });

    jPanel4.add(transitSetButton);

    transitPanel.add(jPanel4, java.awt.BorderLayout.SOUTH);

    mainTabbedPane.addTab("\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8", transitPanel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanel1.add(mainTabbedPane, gridBagConstraints);

    mainPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

    add(mainPanel, java.awt.BorderLayout.NORTH);

  }// </editor-fold>//GEN-END:initComponents

  private void transitSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitSetButtonActionPerformed
    transitTimePlacePanel.getTimePlace(transit);
    transit.setName(nameTextField.getText().trim());
    transit.setMemo(memoTextField.getText().trim());
    channelData.setTransit(transit);
    set();
  }//GEN-LAST:event_transitSetButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mainPanel;
  private javax.swing.JTabbedPane mainTabbedPane;
  private javax.swing.JTextField memoTextField;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JButton transitSetButton;
  private to.tetramorph.starbase.TimePlacePanel transitTimePlacePanel;
  // End of variables declaration//GEN-END:variables
  
}
