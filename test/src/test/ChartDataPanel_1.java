/*
 * ChartDataPanel.java
 *
 * Created on 2006/11/28, 1:03
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;

/**
 * �����͋@�\���폜����܂��̃o�[�W�����B�g�p���Ă��Ȃ��B
 * �f�[�^�x�[�X�ɂ�Natal�̃f�[�^�������o�^����Ă��āA���̃f�[�^����܂��͕�����
 * ���X�g�ɂ��ăR���{�{�b�N�X�ɓ���āA�C�ӂ�Natal�f�[�^��I��������ҏW������ł�
 * ��悤�ɂ���p�l���B
 * �f�[�^�x�[�X����Natal�I�u�W�F�N�g�̃f�[�^��setNatal()/addNatal()�ŁA���̃p�l����
 * �������BaddNatal�͂��̃p�l�����̃��X�g�ɒǉ������B
 * 
 * Natal�̃f�[�^�͂��̂܂܃��X�g�������̂ł͂Ȃ��AData�I�u�W�F�N�g�Ƀ��b�v�����
 * ���X�g�ɂ��ĊǗ������BData�I�u�W�F�N�g��Natal�f�[�^�ƁA���̃f�[�^�Ɠ����l������
 * TimePlace�f�[�^�̃C���X�^���X���쐬���āA��d�ɓ����Əꏊ��ێ����A���[�U�[��
 * ������ꏊ��ύX����Ƃ��͕�������TimePlace��ύX����d�g�݂ɂȂ��Ă���B��������
 * ���ƂŁA�ȒP�Ɍ��̓����ɖ߂����Ƃ��ł���@�\��񋟂���B
 * 
 * Natal�f�[�^�ɂ́A�l�̃f�[�^�ƁA�����l�̃f�[�^�W������Ȃ�R���|�W�b�g�f�[�^��
 * �����鎖���ł��邪�AChartDataPanel��setNatal()/addNatal()�����Ƃ��A�R���|�W�b�g
 * �f�[�^�̒��ɓo�^����Ă��镡���̌l�f�[�^(Transit�I�u�W�F�N�g)�́A���ꂼ���l
 * �Âɕ�������āAData�I�u�W�F�N�g�Ń��b�v�����B
 * �܂�Data�I�u�W�F�N�g�́ATransit��Natal���ǂ���̃I�u�W�F�N�g�����b�v�\�B
 * 
 * �R���|�W�b�g�f�[�^���o�^����Ă���Ƃ��́AisComposit()�ł���𔻒�ł���B
 * ChartDataPanel�̓R���|�W�b�g�f�[�^�ƌl�f�[�^�����݂��邱�Ƃ͔F�߂Ȃ��B
 * ����͍ŏ���setNatal�����Ƃ��̃f�[�^���l�f�[�^���������R���|�W�b�g����������
 * ���܂�B
 * �R���|�W�b�g�p�ƂȂ���ChartDataPanel�ɁA�l�f�[�^��addNatal()����ƁA�����
 * �R���|�W�b�g�̃��X�g�ɐV���Ɉ�l���������ƂɂȂ�B
 * �l�f�[�^�p�ƂȂ���ChartDataPanel�ɁA�l�f�[�^��addNatal()����ƁA�l�f�[�^
 * ����l���������ɂȂ�B�ꌩ���Ȃ������f�[�^��ۑ�����ꍇ�ɕς���Ă���B
 * @author ���V�`��
 */
class ChartDataPanel_1 extends javax.swing.JPanel 
  implements ChartData,java.io.Serializable {
  Natal compositNatal = null;
  boolean eventStop = false;
  List<Data>dataList; //�o���f�[�^�̕ۊǗp
  Transit transit = new Transit(); // �����̓f�[�^�̕ۊǗp
  ChartInternalFrame iframe;
  TimeController tcont; // set()���Ăяo���f�[�^��ChartModulePanel�ɑ���B
  JMenu historyMenu = new JMenu("�q�X�g���[(H)");
  List<Transit> historyList = null;
  TimePlacePanel transitTimePlacePanel;
  /** 
   * �I�u�W�F�N�g���쐬����BGUI�G�f�B�^�p�ɗp�ӂ���Ă邾���ł��̃R���X�g���N�^
   * �͎g�p���Ȃ����ƁB
   */
  public ChartDataPanel_1() {
    initComponents();
    init();
  }
  private void init() {
    dataList = new ArrayList<Data>();
    timePlacePanel2.setDefault();
    timePlacePanel2.getTimePlace(transit);
    tabbedPane.setEnabledAt(0,false); //���������̓f�[�^�^�u�͎g�p�s�B
    tabbedPane.setSelectedIndex(1); //�����̓^�u�̂ݎg�p�ł���B
  }  
  /**
   * �I�u�W�F�N�g���쐬����B������List<Data>���������A�����̓^�u�ɂ͌��ݎ���
   * �ƃf�t�H���g�̊ϑ��n���Z�b�g�������I����ԂɁB�f�[�^�^�u��Disenabled�B
   * setNatal�Ȃǂ̃��\�b�h�Ńf�[�^���Z�b�g�����ƃf�[�^�^�u��Enabled�ɂȂ�B
   * @param tcont TimeController�����������I�u�W�F�N�g
   * @param iframe ChartInternalFrame�I�u�W�F�N�g
   */
  public ChartDataPanel_1(TimeController tcont,ChartInternalFrame iframe) {
    initComponents();
    init();
    this.iframe = iframe;
    this.tcont = tcont;
  }
  /**
   * Natal�̃��X�g���Z�b�g����B
   */
  public void setNatal(List<Natal> list) {
    if(list.size() > 0) {
      tabbedPane.setSelectedIndex(0);
      tabbedPane.setEnabledAt(0,true);
    }
    eventStop = true;
    // ���߼ޯĂ��^����ꂽ���͕ۊǂ��Ă���
    compositNatal = list.get(0).getChartType().equals(Natal.COMPOSIT) ? list.get(0) : null;
    nameComboBox.removeAllItems();
    dataList.clear();
    for(int i=0; i < list.size(); i++) {
      Data data = null;
      if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
        //�R���|�W�b�g�͂ق�����List<Data>�Ƀ��b�v�B
        List<Transit> compositList = list.get(i).getComposit();
        for(int j=0; j<compositList.size(); j++) {
          data = new Data(compositList.get(j));
          nameComboBox.addItem(data.toString());
          dataList.add(data);
        }
      } else { //���ʂ̃f�[�^��List<Data>�Ƀ��b�v����B
        data = new Data(list.get(i));
        nameComboBox.addItem(data.toString());
        dataList.add(data);
      }
    }
    nameComboBox.setSelectedIndex(0);
    timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
    eventStop = false;
    nameComboBoxActionPerformed(null);
  }
  /**
   * Natal�̃��X�g��ǉ�����B
   */
  public void addNatal(List<Natal> list) {
    if(list.size() > 0) {
      tabbedPane.setSelectedIndex(0);
      tabbedPane.setEnabledAt(0,true);
    }
    eventStop = true;
    SKIP:
    for(int i=0; i < list.size(); i++) {
      if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
        //�R���|�W�b�g�Ȃ疳�����ɒǉ��B�d�����Ēǉ����Ă��C�ɂ��Ȃ��B
        List<Transit> compList = list.get(i).getComposit();
        for(Transit evt : compList) {
          Data data  = new Data(evt);
          nameComboBox.addItem(data.toString());
          dataList.add(data);
        }
      } else {
        int id = list.get(i).getId();
        //ID�Ō�������dataList�ɏd������f�[�^������ꍇ�͒ǉ����Ȃ��B������NEED_REGIST�f�[�^�͏���
        if(id != Natal.NEED_REGIST) {
          for(int j=0; j < dataList.size(); j++) {
            if(dataList.get(j).getNatal().getId() == id) continue SKIP;
          }
        }
        Data data = new Data(list.get(i));
        nameComboBox.addItem(data.toString());
        dataList.add(data);
      }
    }
    //�����Ă����Ō�̂��̂��Z���N�g
    nameComboBox.setSelectedIndex(nameComboBox.getItemCount()-1);
    //timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
    eventStop = false;
    nameComboBoxActionPerformed(null);    
  }
  /**
   * ���݃t�H�[�J�X������^�u�̓����t�H�[���Ɏ��Ԃ����Z����B
   * �l�ɕ������w�肷��ƌ��Z���s����B
   */
  public void addCalendar(int day,int hour,int minute,int second) {
    if(tabbedPane.getSelectedIndex() == 0) { //�f�[�^�p
      timePlacePanel.addCalendar(day,hour,minute,second);
      setButtonActionPerformed(null);
    } else {
      timePlacePanel2.addCalendar(day,hour,minute,second);
      setButton2ActionPerformed(null);
    }
  }
  /**
   * �R���{�{�b�N�X�ɓo�^����Ă���Data�̃��X�g��Ԃ��B
   */
  public List<Data> getDataList() {
    return dataList;
  }
  /**
   * �����Ŏw�肳�ꂽData�̃��X�g�����̃p�l���ɃZ�b�g����B�R���{�{�b�N�X��
   * ���̓t�H�[���ɂ��l������B
   */
  public void setDataList(List<Data> dataList) {
    this.dataList = dataList;
    tabbedPane.setSelectedIndex(0);
    tabbedPane.setEnabledAt(0,true);
    eventStop = true;  
    nameComboBox.removeAllItems();
    for(Data d : dataList)
      nameComboBox.addItem(d.toString());
    nameComboBox.setSelectedIndex(0);
    eventStop = false;
    nameComboBoxActionPerformed(null);
    //timePlacePanel.setTimePlace(dataList.get(0).getTimePlace());
  }
  /** 
   * �`���[�g�t���[���̕����̍ۂ�Data�̃��X�g�����̃p�l���ɃZ�b�g����B 
   * setDataList()�Ƃقړ�������TimePanel.set()���Ăяo���Ȃ��B
   * ChartInternalFrame�������A�܂��e�`���[�g���W���[���̃Z�b�g�A�b�v���ςޑO��
   * setDataList()���g���ƁAset()���Ăяo����NullPorinterException���o�Ă��܂��B
   * �����������邽�߂ɂ��̃��\�b�h������B�Ď�
   */
  public void initDataList(List<Data> dataList) {
    this.dataList = dataList;
    tabbedPane.setSelectedIndex(0);
    tabbedPane.setEnabledAt(0,true);
    eventStop = true;  
    nameComboBox.removeAllItems();
    for(Data d : dataList)
      nameComboBox.addItem(d.toString());
    nameComboBox.setSelectedIndex(0);
    Data data = dataList.get(0);
    timePlacePanel.setTimePlace(data.getTimePlace());
    if(data.getNatal().getTime() == null)
      timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    createHistoryMenu();
    eventStop = false;
  }
  /**
   * �R���{�{�b�N�X�̃f�[�^�̉��Ԗڂ��I������Ă��邩�Ԃ��B
   * �I������Ă��Ȃ��ꍇ�͕�����Ԃ��B(���Ԃ�-1)
   */
  public int getSelectedIndex() {
    if(tabbedPane.getSelectedIndex() == 0)
      return nameComboBox.getSelectedIndex();
    else
      return -1;
  }
  /**
   * ���ݑI�𒆂�Natal�f�[�^��Data�I�u�W�F�N�g�ŕԂ��B
   * �����̓^�u�Ƀt�H�[�J�X����������A�f�[�^�����ݒ�Ȃ�null��Ԃ��B
   */
  public Data getSelectedData() {
    if(getSelectedIndex() >= 0) return dataList.get(getSelectedIndex());
    return null;
  }
  /**
   * ���ړ��̓t�H�[���̓����ꏊ�̃f�[�^��Ԃ��B
   */
  public Transit getTransit() {
    return transit;
  }
  /**
   * ���ړ��̓t�H�[���ɓ����ꏊ���Z�b�g
   */
  public void setTransit(Transit transit) {
    this.transit = transit;
    timePlacePanel2.setTimePlace(transit);
  }
  /**
   * ���̃`���[�g�f�[�^�p�l���ɓo�^����Ă���f�[�^���A�R���|�W�b�g�f�[�^�Ȃ�
   * true��Ԃ��B
   */
  public boolean isComposit() {
    return compositNatal != null;
  }
  /**
   * ���̃p�l���ɃR���|�W�b�g�f�[�^���Z�b�g����Ă���ꍇ�͂��̃f�[�^��Ԃ��B
   * �Z�b�g���ꂽ���Ƃ��Ȃ��Ȃ�null��Ԃ��B
   */
  public Natal getComposit() {
    return compositNatal;
  }
  /**
   * �w�肳�ꂽID������Natal�f�[�^�����̃p�l���ɓ����Ă���Ƃ���true��Ԃ��B
   */
  public boolean isComprise(int id) {
    if(isComposit()) return compositNatal.getId() == id;
    for(int i=0; i<dataList.size(); i++) {
      if(dataList.get(i).getNatal().getId() == id) return true;
    }
    return false;
  }
  /**
   * ����ChartData�ɂӂ��킵���A�C�R��(�j���̊�⎞�v��R���|�W�b�g�̃V���{��)��
   * �����t���[���ɃZ�b�g����B
   */
  public void setFrameIcon() {
    if(! isComposit()) {
      Data data = getSelectedData();
      if(data != null) {
        iframe.setFrameIcon(data.getNatal());
        iframe.setTitle(data.getNatal().getName());
      }
    } else {
      iframe.setFrameIcon(getComposit());
      iframe.setTitle(getComposit().getName());
      System.out.println("�R���|�W�b�g�A�C�R���Z�b�g " + getComposit().getName());
    }
  }
  /**
   * �u���ԃ��j���[���q�X�g���[�v�ŕ\�������q�X�g���[�ꗗ�̃��j���[���쐬����B
   * ���̃p�l���̃R���{�{�b�N�X�Ńf�[�^�̑I�������ꂽ�Ƃ��ƁA
   * �u���ԃ��j���[���g�����V�b�g���q�X�g���[�ɒǉ��v����Ăяo�����B�����
   * ���j���[�쐬�̃g���K�ɂ����Ȃ��B�쐬�������j���[�̎擾��getHistoryMenu()�B
   */
  public void createHistoryMenu() {
    // selected()����Ăяo�����B�q�X�g���[���ǉ����ꂽ�Ƃ�ChartInternalFrame������B
    historyList = new ArrayList<Transit>();
    historyMenu.removeAll(); //�Q�ƃA�h���X��ύX�������Ȃ��̂�remove�őΏ��B
    List<Transit> list = null;
    if(isComposit())
      list = compositNatal.getHistory();
    else {
      int num = nameComboBox.getSelectedIndex();
      list = dataList.get(num).getNatal().getHistory();
    }
    if(list == null) historyMenu.setEnabled(false);
    else {
      for(int i=0,j=0; i<list.size(); i++) {
        Transit evt = list.get(i);
        if(! evt.getName().equals("TRANSIT_PLACE")) { //�o�ߒn���͏��O
          historyList.add(evt);
          String title = String.format("%s    %s",evt.getDate().toString(),evt.getName());
          JMenuItem item = new JMenuItem(title);
          item.setActionCommand(""+j); //�A�C�e���ɃV���A���ԍ���^����
          j++;
          historyMenu.add(item);
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              setHistoryActionPerformed(evt);
            }
          });
        }
      }
      historyMenu.setMnemonic(KeyEvent.VK_H);
    }
    if(historyMenu.getItemCount()==0) historyMenu.setEnabled(false);
  }
  /**
   * ����ChartDataPanel�ɓo�^����Ă��āA���ݑI������Ă���f�[�^(�R���|�W�b�g
   * �f�[�^�̏ꍇ�͑I���͊֌W�Ȃ�)�̃q�X�g���[�̃��j���[��Ԃ��B
   */
  public JMenu getHistoryMenu() {
    return historyMenu;
  }
  /**
   * ���ݑI������Ă���Data�ɁA�����Ŏw�肳�ꂽnatal�f�[�^���Z�b�g���t�H�[��
   * �ɂ����̓��e�𔽉f������B
   * ���ԃ��j���[�́u���݂̃f�[�^��ҏW�v����Ăяo�����B
   */
  public void replaceNatal(Natal natal) {
    int num = nameComboBox.getSelectedIndex();
    Data data = getSelectedData();
    data.setNatal(natal);
    dataList.set(num,data);
    nameComboBoxActionPerformed(null);
  }
//  //���j���[�őI�����ꂽ�q�X�g���[���g�����V�b�g�ɃZ�b�g����
  private void setHistoryActionPerformed(ActionEvent evt) {
    JMenuItem item = (JMenuItem)evt.getSource();
    int n = Integer.parseInt(item.getActionCommand());
    Transit event = historyList.get(n);
    tcont.setTransit(event);
  }
  /**
   * �`���[�g���W���[�����œ����̍X�V�����������Ƃ��A���̃��\�b�h���ĂԎ��ŁA
   * ChartDataPanel(�������̓t�H�[��)�ɁA�X�V���ꂽ�����𔽉f������B
   * @param data TimePlace���X�V����Date�I�u�W�F�N�g
   * @exception IllegalArgumentException �f�[�^���X�g�ɑ��݂��Ȃ�Data�I�u�W�F�N�g
   * ���w�肳�ꂽ�Ƃ��B
   */
  public void updateData(Data data) {
    if( dataList.indexOf(data) < 0 ) throw 
      new IllegalArgumentException("�f�[�^���X�g�ɑ��݂��Ȃ�Data�I�u�W�F�N�g�ł�");
    TimePlace tp = data.getTimePlace();
    timePlacePanel.setTimePlace(tp);
    //if(tp.getTime() == null)
    //  timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
 }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel dataSelectPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel transitPanel;

    tabbedPane = new javax.swing.JTabbedPane();
    dataSelectPanel = new javax.swing.JPanel();
    timePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
    jPanel2 = new javax.swing.JPanel();
    resetButton = new javax.swing.JButton();
    setButton = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    nameComboBox = new javax.swing.JComboBox();
    delButton = new javax.swing.JButton();
    transitPanel = new javax.swing.JPanel();
    timePlacePanel2 = new to.tetramorph.starbase.TimePlacePanel();
    jPanel4 = new javax.swing.JPanel();
    setButton2 = new javax.swing.JButton();

    setLayout(new java.awt.GridLayout(1, 0));

    dataSelectPanel.setLayout(new java.awt.GridBagLayout());

    dataSelectPanel.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    dataSelectPanel.add(timePlacePanel, gridBagConstraints);

    jPanel2.setFocusable(false);
    resetButton.setMnemonic('R');
    resetButton.setText("Reset");
    resetButton.setToolTipText("\u6700\u521d\u306e\u30c7\u30fc\u30bf\u306b\u623b\u3059");
    resetButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });

    jPanel2.add(resetButton);

    setButton.setMnemonic('S');
    setButton.setText("Set");
    setButton.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    setButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    setButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setButtonActionPerformed(evt);
      }
    });

    jPanel2.add(setButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    dataSelectPanel.add(jPanel2, gridBagConstraints);

    jPanel3.setLayout(new java.awt.GridBagLayout());

    jPanel3.setFocusable(false);
    jLabel2.setText("\u5bfe\u8c61");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 4);
    jPanel3.add(jLabel2, gridBagConstraints);

    nameComboBox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        nameComboBoxItemStateChanged(evt);
      }
    });
    nameComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nameComboBoxActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    jPanel3.add(nameComboBox, gridBagConstraints);

    delButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/delete.png")));
    delButton.setToolTipText("\u9078\u629e\u4e2d\u306e\u30c7\u30fc\u30bf\u3092\u9664\u5916");
    delButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    delButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        delButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 3, 0);
    jPanel3.add(delButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    dataSelectPanel.add(jPanel3, gridBagConstraints);

    tabbedPane.addTab("\u30c7\u30fc\u30bf", dataSelectPanel);

    transitPanel.setLayout(new java.awt.GridBagLayout());

    transitPanel.setFocusable(false);
    transitPanel.add(timePlacePanel2, new java.awt.GridBagConstraints());

    jPanel4.setFocusable(false);
    setButton2.setMnemonic('S');
    setButton2.setText("Set");
    setButton2.setToolTipText("\u30d5\u30a3\u30fc\u30eb\u30c9\u306e\u5024\u3092\u30c1\u30e3\u30fc\u30c8\u306b\u9001\u308b");
    setButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        setButton2ActionPerformed(evt);
      }
    });

    jPanel4.add(setButton2);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    transitPanel.add(jPanel4, gridBagConstraints);

    tabbedPane.addTab("\u76f4\u5165\u529b", transitPanel);

    add(tabbedPane);

  }// </editor-fold>//GEN-END:initComponents
  // �����͂̃Z�b�g�{�^��
  private void setButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButton2ActionPerformed
    timePlacePanel2.getTimePlace(transit);
    System.out.println("written = " + transit.toString());
    int num = nameComboBox.getSelectedIndex();
    if(num >= 0) {
      TimePlace tap = dataList.get(num).getTimePlace();
      timePlacePanel.getTimePlace(tap);
    }
    tcont.set();
  }//GEN-LAST:event_setButton2ActionPerformed
  //�R���{�{�b�N�X���̃f�[�^�폜
  private void delButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delButtonActionPerformed
    if(compositNatal == null) {
      if(dataList.size() <= 1) {
        JOptionPane.showInternalMessageDialog(iframe,"�Ō�̃f�[�^�͍폜�ł��܂���",
          "�f�[�^���X�g�̍폜",JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {
      if(dataList.size() <= 2) {
        JOptionPane.showInternalMessageDialog(iframe,"�R���|�W�b�g�f�[�^��2���ȉ��ɂ͂ł��܂���",
          "�f�[�^���X�g�̍폜",JOptionPane.ERROR_MESSAGE);
        return;        
      }
    }
    int num = nameComboBox.getSelectedIndex();
    if( JOptionPane.showInternalConfirmDialog(iframe,
          "\""+dataList.get(num).toString()+"\"���폜���܂����H",
          "�f�[�^���X�g����̍폜",JOptionPane.YES_NO_OPTION) 
      == JOptionPane.YES_OPTION ) {
      dataList.remove(num);
      eventStop = true; //�Z���N�g�C�x���g�֎~
      nameComboBox.removeItemAt(num);  //�폜
      eventStop = false; //�֎~����
      nameComboBox.setSelectedIndex(0); //0�Ԗڂ�I���Bselected()���Ă΂��B
    }
  }//GEN-LAST:event_delButtonActionPerformed
  //�R���{�{�b�N�X�őI���C�x���g���N�����Ƃ�
  private void nameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameComboBoxActionPerformed
    if(eventStop) return;
    int num = nameComboBox.getSelectedIndex();
    if(num < 0) return;
    Data data = dataList.get(num);
    timePlacePanel.setTimePlace(data.getTimePlace());
    if(data.getNatal().getTime() == null)
      timePlacePanel.setTime(Time.valueOf(Config.data.getProperty("DefaultTime")));
    createHistoryMenu();
    tcont.set();
  }//GEN-LAST:event_nameComboBoxActionPerformed

  private void nameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nameComboBoxItemStateChanged

  }//GEN-LAST:event_nameComboBoxItemStateChanged
  //�l�C�^���t�H�[���̃Z�b�g�{�^��
  private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButtonActionPerformed
    timePlacePanel2.getTimePlace(transit);
    int num = nameComboBox.getSelectedIndex();
    TimePlace tap = dataList.get(num).getTimePlace();
    timePlacePanel.getTimePlace(tap);
    tcont.set();
  }//GEN-LAST:event_setButtonActionPerformed
  //�l�C�^���t�H�[���̃��Z�b�g�{�^��
  private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
    int num = nameComboBox.getSelectedIndex();
    dataList.get(num).resetTimePlace();
    timePlacePanel.setTimePlace(dataList.get(num).getTimePlace());
    tcont.set();
  }//GEN-LAST:event_resetButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton delButton;
  private javax.swing.JComboBox nameComboBox;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton setButton;
  private javax.swing.JButton setButton2;
  private javax.swing.JTabbedPane tabbedPane;
  private to.tetramorph.starbase.TimePlacePanel timePlacePanel;
  private to.tetramorph.starbase.TimePlacePanel timePlacePanel2;
  // End of variables declaration//GEN-END:variables
  
}
