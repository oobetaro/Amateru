/*
 * MainFrame.java
 *
 * Created on 2006/09/08, 19:02
 */

package to.tetramorph.starbase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.TreePath;
import to.tetramorph.astrocalendar.AstroCalendar;
import to.tetramorph.astrocalendar.EnneagramCalendar2;
import to.tetramorph.starbase.lib.Home;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.MaximumWindowBounds;

/**
 * ���̐萯�p�\�t�g�̃��C���t���[���B
 * �����̃`���[�g�t���[��������t���[���Ŋi�[������A�l�X�ȋ@�\���Ăяo�����j���[
 * �������Ă���B�����t���[�����Ǘ�����B
 * System.getProperty("SeparateMode").equals("true")�Ȃ�t���[���������[�h�ƂȂ�A
 * setVisible,toFront�Ȃǂ̑�����s���B
 * <pre>
 * System.setProperty()�������́B
 * DefaultTime = "12:00:00"
 * SeparaterMode = "true"
 * <pre>
 *
 * �v���p�e�B�̊Ǘ��ɂ��āB
 * �N�������炷����System.setProperties()���āA�A�v����p�̃v���p�e�B�I�u�W�F�N�g
 * �ɂ��������A����ɂ�����DB����ݒ�l�����[�h����B
 * System�v���p�e�B�͂ǂ̃N���X�����static�Q�Ƃł���̂ŁA�O���[�o���ϐ��Ƃ݂Ȃ�
 * �Ċ��p����B�A�v���I���̂Ƃ��ASystem.getProperties()�������̂�HSQLDB�ɏ����o��
 * �Ĉꊇ�ۑ�����B�܂�A�v���N������System.setProperty()�����l�͂˂�DB�ɕۊ�
 * ����A�s�������ϐ��Ƃ��Ďg�����Ƃ��ł���悤�ɂȂ��Ă���B
 * System.setProperties()����I�u�W�F�N�g��Properties�I�u�W�F�N�g���p������
 * Preference�I�u�W�F�N�g�BProperties�̋@�\�ŕs�\���ȂƂ��́APreference�ɃL���X�g
 * ���Ďg���B�܂�System.setProperties()����Preference�I�u�W�F�N�g�́A
 * System.getProperties()���p���������̂Ȃ̂ŁA�V�X�e�����������v���p�e�B����
 * �擾���邱�Ƃ��ł���B
 */
final class MainFrame extends JFrame implements ChartDesktop {
  //�f�[�^�x�[�X�t���[��
  DatabaseFrame editor = new DatabaseFrame(this);
  //ESC�L�[�œ����t���[�������悤�ɂ��邽�߂̃L�[�n���h��
  MainKeyHandler mainKeyHandler = new MainKeyHandler();
  //�I������Ă�������t���[�������L���Ă��鎞�Ԑ���p�l��
  TimePanel selectedTimeControllerPanel = null;
  //�����t���[���X�v���b�h�@�\�Ŏg�p
  int count = 0; //�X�v���b�h�{�^���̃g�O����
  JInternalFrame frame0 = null; //�X�v���b�h�@�\�Ŏg�p�B�I�𒆂̓����t���[��
  SplitPane splitPane = new SplitPane(); //�I���W�i���̃X�v���b�^
  int devider = 0; // �f�[�^�[�x�[�X����JSplitPane�̃X���C�_�[�̈ʒu(pixel)
  SabianDialog sabianDialog; //�T�r�A���_�C�A���O
  JDayDialog jdayDialog; //�����E�X���v�Z�@
  TimeSliderDialog timeSliderDialog; //���Ԑ���_�C�A���O
  FocusHandler iframeFocusHandler = new FocusHandler();
  ChartFrameCloseHandler iframeCloseHandler = new ChartFrameCloseHandler();

  /**
   * main���\�b�h���Ăяo���R���X�g���N�^�B���ڃC���X�^���X�͍쐬�ł��Ȃ��B
   */  
  private MainFrame() {
    initComponents();
    splitPane.setMasterComponent(desktopPane);
    splitPane.setMinimumSize(new Dimension(0,0));
    windowMenu.add(splitPane.getVisibleCheckBoxMenuItem());
    windowMenu.add(splitPane.getSwapCheckBoxMenuItem());
    if(System.getProperty("SeparateMode","false").equals("true")) {
      add( splitPane,BorderLayout.CENTER );
      editor.setVisible(true);
    } else {
      add(bottomSplitPane,BorderLayout.CENTER);
      bottomSplitPane.setTopComponent(splitPane);
      bottomSplitPane.setDividerLocation(500);
      dbSplitPane.setLeftComponent(editor.getContentPane());
      dbSplitPane.setRightComponent(editor.getSearchResultContentPane());
    }
    createMenu();
    addWindowFocusListener(new MaximumWindowBounds(this));
    //�t���[�����ő剻���ꂽ�Ƃ�������f�[�^�x�[�X�̃t���[����O�ʂɈړ�
    addWindowStateListener(new WindowAdapter() {
      //�t���[���ő剻�����o
      public void windowStateChanged(WindowEvent evt) {
        if(evt.getNewState() == Frame.MAXIMIZED_BOTH) {
          if(editor.isShowing() ) editor.toFront();
          if(editor.isSearchFrameShowing()) editor.showSearchFrame();
        }
      }
    });
    WindowMoveHandler winmove =
      new WindowMoveHandler("MainFrame.BOUNDS", this);
    addComponentListener(winmove);
    winmove.setBounds();
    desktopPane.addContainerListener(new DesktopPaneHandler());
    setIconImage(IconLoader.getImage("/resources/images/starbase_icon.png"));
    setKeyListener(mainKeyHandler);    
    TreePath current = editor.foundTreePath( Config.system.getProperty("CurrentTreePath") );
    editor.selectFolder(current);
    sabianDialog = new SabianDialog(this); //System�v���p�e�B�����[�h����Ă�����
    timeSliderDialog = new TimeSliderDialog(this);
    jdayDialog = new JDayDialog(this);
    pack();
  }
  /** �T�r�A���_�C�A���O��Ԃ��B*/
  protected SabianDialog getSabianDialog() {
    return sabianDialog;
  }
  //�L�[�{�[�h�V���[�g�J�b�g�̃n���h��
  private class MainKeyHandler extends KeyAdapter {
    public void keyTyped(KeyEvent e) {
      int code =(int)e.getKeyChar();
      char c = e.getKeyChar();
      //System.out.println("MainFrame: KEYCODE = " + code +", KEYCHAR = " + e.getKeyChar()+ ", SHIFT_DOWN = " + e.isShiftDown());
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame == null) {
        //System.out.println("�Z���N�g����Ă�`���[�g�t���[���͂Ȃ�");
        return;
      }
      if(code == KeyEvent.VK_ESCAPE) { //ESC�L�[�ŃE�B���h�E����
        chartFrame.doDefaultCloseAction();
      }
    }
  }
  //DesktopPane�œ����t���[�����ǉ��E�폜���ꂽ�ۂ̃C�x���g����
  private class DesktopPaneHandler implements ContainerListener {
    public void componentAdded(ContainerEvent e) {
      count = 0;
      chartAddMenu.setEnabled(true);
      splitPane.getVisibleCheckBoxMenuItem().setEnabled(true);
      newChartFrameMenuItem.setEnabled(true);
    }
    //�����ڰт��폜���ꂽ�Ƃ�
    public void componentRemoved(ContainerEvent e) {
       //�����ڰт�0���ɂȂ����玞�Ԑ������قƽ��د�������
      ChartInternalFrame [] frame = getChartFrames();
//      System.out.println("�����t���[���폜 �c��t���[���� = " + frame.length);
      if(focusFrame != null && frame.length > 0 ) {
        focusFrame.restoreSubcomponentFocus();
//        System.out.println("�t���[���� " + focusFrame.getTitle() + 
//          ", �t�H�[�J�X�̏�� = " + focusFrame.isRequestFocusEnabled() + 
//          " �t�H�[�J�X�v���̐��� = " + focusFrame.requestFocusInWindow());
      }
      count = 0;
    }
  }
  private class ChartFrameCloseHandler extends InternalFrameAdapter {
    public void internalFrameClosed(InternalFrameEvent e) {
      //System.out.println("�����t���[��Closed �c��t���[���� = " + getChartFrames().length);
      if(e.getInternalFrame() == focusFrame) focusFrame = null;
      if(isEmptyChartFrame()) {
        timeSliderDialog.setVisible(false);
        chartAddMenu.setEnabled(false);
        setTimeController(null);
        splitPane.getVisibleCheckBoxMenuItem().setEnabled(false);
        newChartFrameMenuItem.setEnabled(false);
      }      
    }
  }
  ChartInternalFrame focusFrame;
  //�`���[�g�t���[���̃t�H�[�J�X�C�x���g�̏������s��
  private class FocusHandler implements FocusListener {
    public void focusGained(FocusEvent e) {
      //JDesktopPane�ɂ�JOptionPane.showInternalMessageDialog�����}������邱�Ƃ�
      //���肤��B���̃n���h����ChartInternalFrame�̃t�H�[�J�X�����ȊO�͖�������B
      //System.out.println("�t�H�[�J�X������ ");
      if( ! (e.getComponent() instanceof ChartInternalFrame) ) {
        //System.out.println("�`���[�g�t���[������Ȃ��̂Ŗ������܂�");
        return;
      }
      ChartInternalFrame frame = (ChartInternalFrame)e.getComponent();
      if(frame.isClosed()) {
        //System.out.println("�N���[�Y����Ă�t���[���Ȃ̂Ŗ������܂�");
        return;
      }
      focusFrame = frame;
      System.out.println(frame.getTitle());
      //̫��������Ō�̻�޺��߰��Ă�̫����𕜌�����悤�A�����ڰтɗv��
      //���Ȃ��ƕ����ڰт�A����ESC���Ÿ۰�ނł��Ȃ��Ȃ�B
      frame.restoreSubcomponentFocus();
      frame.getTimeMenu(timeMenu);
      TimePanel tp = frame.getTimePanel();
      setTimeController(tp);
      timeSliderDialog.setTimeSliderPanel(tp.getTimeSliderPanel());
      setPreferenceMenu();
    }
    public void focusLost(FocusEvent e) {
      System.out.println("�t�H�[�J�X�������� ");
      setPreferenceMenu();
    }
  }
  //InternalFrame�Ƀt�H�[�J�X���������ݸނŁA�v�Z�ݒ��ƭ��𓮓I�ɐݒ肷��B
  //�e��׸޲݂̐ݒ����ٌĂт����Ɋ֌W�B����ڰ�����ŌĂяo�����߂ɂ́A
  //preferenceMenu�̲���Ă����ݸނōs���킯�ɂ͂����Ȃ��B07/9/16�ύX
  private void setPreferenceMenu() {
    prefMenu.removeAll();
    prefMenu.add(configMenuItem);
    ChartInternalFrame f = getSelectedFrame();
    if(f == null) return;
    prefMenu.add(f.getColorSelectionMenu());
    prefMenu.add(f.getSpecificSelectionMenu());
  }
  
  //�`���[�g�t���[���̕���
  void duplicateChartFrame() {
//    ChartInternalFrame nowFrame = //���ݑI�𒆂̓����t���[��
//      (ChartInternalFrame)desktopPane.getSelectedFrame();
    ChartInternalFrame nowFrame = getSelectedFrame();
    ChartInternalFrame newFrame = new ChartInternalFrame(nowFrame); //����
    newFrame.addInternalFrameListener( iframeCloseHandler );
    newFrame.addKeyListener(mainKeyHandler);
    newFrame.addFocusListener(iframeFocusHandler);
    timeSliderDialog.setTimeSliderPanel(newFrame.getTimeSliderPanel());
    newFrame.setVisible(true);
    desktopPane.add(newFrame);
    try {
      newFrame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
  }
  /**
   *
   */
  public Frame getFrame() {
    return this;
  }
  /**
   * �V�����`���[�g�t���[�����J���āANatal�̃��X�g���Z�b�g����B
   */
  public void openChartFrame(List<Natal> list) {
    assert SwingUtilities.isEventDispatchThread();
    if(!(list.size() == 1 && list.get(0).getChartType().equals(Natal.COMPOSIT))) {
      for(int i=0; i<list.size(); i++) {
        if(list.get(i).getChartType().equals(Natal.COMPOSIT)) {
          StringBuffer sb = new StringBuffer();
          sb.append("<html>");
          sb.append("�����̃f�[�^����x�ɓ��͂���ꍇ�A�R���|�W�b�g�f�[�^��<br>");
          sb.append("��R���|�W�b�g�f�[�^�����݂����Ďw�肷�邱�Ƃ͂ł��܂���B<br>");
          sb.append("�܂������̃R���|�W�b�g�f�[�^����x�ɓ��͂��邱�Ƃ��ł��܂���B<br>");
          sb.append("�������ォ��ǉ����邱�Ƃ͉\�ł��B<br>");
          sb.append("</html>");
          JOptionPane.showMessageDialog(MainFrame.this,sb.toString(),
            "�V�K�`���[�g�̃G���[",JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    ChartInternalFrame frame = new ChartInternalFrame(editor,timeMenu,sabianDialog,this);
    frame.selectModule(0);
    frame.addInternalFrameListener( iframeCloseHandler );
    frame.addKeyListener( mainKeyHandler );
    frame.addFocusListener( iframeFocusHandler );
    frame.setVisible(true);
    timeSliderDialog.setTimeSliderPanel(frame.getTimeSliderPanel());
    desktopPane.add(frame);
    try {
      frame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
    frame.setNatal(list);
  }

  /**
   * ���ׂẴ`���[�g�t���[���̃��X�g��Ԃ��B
   * �ꖇ�������Ƃ��͒���0�̔z�񂪕Ԃ�B
   */
  public ChartInternalFrame [] getChartFrames() {
    JInternalFrame [] frames = desktopPane.getAllFrames();
    List<ChartInternalFrame> list = new ArrayList<ChartInternalFrame>();
    for(JInternalFrame f:frames) {
      if(f instanceof ChartInternalFrame)
        list.add((ChartInternalFrame)f);
    }
    ChartInternalFrame [] results = new ChartInternalFrame[ list.size() ];
    for(int i=0; i<list.size(); i++) results[i] = list.get(i);
    return results;
  }
  /**
   * �w�肳�ꂽID�̃f�[�^���`���[�g�t���[��(����)�ɓo�^����Ă���ꍇ��true��
   * �Ԃ��B�܂�`���[�g�ɕ\������Ă���Natal�f�[�^�Ȃ�true�ƂȂ�B
   * ���̍ہA�f�[�^�x�[�X�ォ�炻��Natal�f�[�^���폜�ƈړ��͂��Ă͂Ȃ�Ȃ��B
   * �\�����̃f�[�^�́u���݂̃f�[�^��ҏW�v�@�\�ŕҏW���Ȃ���΂Ȃ�Ȃ��B
   */
  public boolean isDataBusy(int id) {
    ChartInternalFrame [] frames = getChartFrames();
    for(int i=0; i<frames.length; i++) {
      if(frames[i].isComprise(id)) return true;
    }
    return false;
  }
  /**
   * �`���[�g�t���[�����ꖇ�������Ƃ���true��Ԃ��B
   */
  public boolean isEmptyChartFrame() {
    return getChartFrames().length == 0;
  }  
  /**
   * ChartInnerFrame��DatabaseFrame��SerachResultTable��őI�����ꂽNatal
   * (����)���Z�b�g�BChartInternalFrame�ɂ�������̃f�[�^�͏����V�������̂ɒu�������B
   * @param list Natal�̃��X�g
   * @param targetFrame null�Ȃ猻�ݑI������Ă���ChartInternalFrame��list���Z�b�g�B
   * null�ȊO�Ȃ�w�肳�ꂽ�t���[����list���Z�b�g�B
   */
  public void setNatal(final List<Natal> list,final ChartInternalFrame targetFrame) {
    assert SwingUtilities.isEventDispatchThread();
    if(targetFrame == null) {
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame != null) {
        chartFrame.setNatal(list);
      }
    } else {
      targetFrame.setNatal(list);
    }
  }
  /**
   * ChartInnerFrame��DB��őI�����ꂽNatal(����)��ǉ��œn���B
   * openNewChart�Ɠ��l�ɃC�x���g�L���[�������Ď��s�����B
   * @param list Natal�̃��X�g
   * @param targetFrame null�Ȃ猻�ݑI������Ă���ChartInternalFrame��list��ǉ��B
   * null�ȊO�Ȃ�w�肳�ꂽ�t���[����list��ǉ��B
   */
  public void addNatal(final List<Natal> list,final ChartInternalFrame targetFrame) {
    assert SwingUtilities.isEventDispatchThread();
    if(targetFrame == null) {
      ChartInternalFrame chartFrame =
        (ChartInternalFrame)desktopPane.getSelectedFrame();
      if(chartFrame != null) {
        chartFrame.addNatal(list);
      }
    } else {
      targetFrame.addNatal(list);
    }
  }
  //���j���[�o�[�̃��j���[(�ꕔ)���쐬
  private void createMenu() {
    //���W���[�����j���[
    moduleMenu.add(new JMenuItem("test"));
    moduleMenu.addMenuListener(new MenuListener() {
      public void menuSelected(MenuEvent e) {
        ChartInternalFrame frame = getSelectedFrame();
          //(ChartInternalFrame)desktopPane.getSelectedFrame();
        if(frame != null) frame.getChartMenu(moduleMenu);
      }
      public void menuCanceled(MenuEvent e) { }
      public void menuDeselected(MenuEvent e) { }
    });
  }
  /**
   * ���Ԑ���p�l���̉�/�����I������B
   * @param visible true�Ȃ�����Afalse�Ȃ������B
   */
  void showTimeController(boolean visible) {
    splitPane.setVisible(visible);
  }
  /**
   * ���ԃp�l����MainFrame�̉E�[�ɃZ�b�g����������B���łɓo�^����Ă���
   * �p�l���͍폜�����Bnull���Z�b�g����Ǝ��ԃp�l���͏��������B
   */
  private void setTimeController(TimePanel tcp) {
    System.out.println("MainFrame#setTimeController() : �^�C���p�l���Z�b�g");
    splitPane.setSlaveComponent(tcp);
  }
  // ���łɊJ���Ă���`���[�g�t���[���ɁA�_�C�A���O��Natal����͂��ǉ�����B
  private void addNatalToChart(String occasionType) {
    Natal occ = null;
    if(occasionType.equals(Natal.NATAL)) {
      occ = DataInputDialog.showNatalDialog(this,null,null);
    } else {
      occ = DataInputDialog.showEventDialog(this,null,null);
    }
    if(occ == null) return;
    occ.setId(Natal.NEED_REGIST); //���DB�ւ̕ۑ����K�v�ł���Ƃ����������
    List<Natal> list = new ArrayList<Natal>();
    list.add(occ);
    ChartInternalFrame chartFrame =
      (ChartInternalFrame)desktopPane.getSelectedFrame();
    if(chartFrame != null) {
      chartFrame.addNatal(list);
    }
  }
  //�o�^�����`���[�g�쐬
  private void createChart(String occasionType) {
    Natal occ = null;
    if(occasionType.equals(Natal.NATAL)) {
      occ = DataInputDialog.showNatalDialog(this,null,null);
    } else {
      occ = DataInputDialog.showEventDialog(this,null,null);
    }
    if(occ == null) return;
    occ.setId(Natal.NEED_REGIST); //���DB�ւ̕ۑ����K�v�ł���Ƃ����������
    List<Natal> list = new ArrayList<Natal>();
    list.add(occ);
    this.openChartFrame(list);
  }
  //MainFrame���̃t�H�[�J�X���󂯎�镔�i���ׂĂɃL�[���X�i��o�^
  void setKeyListener(KeyListener l) {
    addKeyListener(l);
    closeFrameButton.addKeyListener(l);
    dbButton.addKeyListener(l);
    desktopPane.addKeyListener(l);
    expansionFrameButton.addKeyListener(l);
    fileMenu.addKeyListener(l);
    iconizeFrameButton.addKeyListener(l);
    menuBar.addKeyListener(l);
    moduleMenu.addKeyListener(l);
    prefMenu.addKeyListener(l);
    searchButton.addKeyListener(l);
    spreadButton.addKeyListener(l);
    spreadButton2.addKeyListener(l);
    timeMenu.addKeyListener(l);
    toolBar.addKeyListener(l);
    windowMenu.addKeyListener(l);
  }
  /** ���Ԑ���_�C�A���O��Ԃ� (�g�p����Ă��Ȃ�) */
  protected TimeSliderDialog getTimeSliderDialog() {
    return timeSliderDialog;
  }
  /** 
   * �I�𒆂̃`���[�g�t���[����Ԃ��B(���̃N���X���ł̂ݎg�p)
   * �I�𒆂̃`���[�g�t���[�������݂��Ȃ��ꍇ��null��Ԃ��B
   */
  protected ChartInternalFrame getSelectedFrame() {
    if(desktopPane.getSelectedFrame() instanceof ChartInternalFrame)
      return (ChartInternalFrame)desktopPane.getSelectedFrame();
    return null;
  }
  //���^����LookAndFeel��ݒ�
  private static void createAndShowGUI() {
    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame.setDefaultLookAndFeelDecorated(true);
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      UIManager.put("AuditoryCues.playList",
        UIManager.get("AuditoryCues.allAuditoryCues"));
      //updateLookAndFeel();
    }
    MainFrame frame = new MainFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
  /**
   * System�v���p�e�B��"TestMode=false"���Z�b�g����ƁA�{�ԃ��[�h�ɂȂ�A
   * �v���O�����̎��s�Ƌ���DB�̋N���A�I���Ƌ���DB��SHUTDOWN���s����B<BR>
   *
   * "TestMode=true"�ɂ��Ă����ƃe�X�g�p���[�h�ƂȂ�ADB�͎��O�ɋN�����Ă����Ȃ�
   * ��΂Ȃ�Ȃ��BDB�̋N���͎��Ԃ������邽�߁A�e�X�g�����̂��тɋN���E��~������
   * �̂͂������邢�̂�DB�͏풓�����Ă�������������B
   *
   * �A�v���̓�d�N���̓g���u���̌��Ȃ̂ŗ}�~���Ă��邪�A�e�X�g���[�h�ł͓�d�N��
   * ���N������B
   */
  public static void main(String args[]) throws MalformedURLException {
    //Preference pref = new Preference(System.getProperties());
    //System.setProperties(pref);
    //�{�ԃ��[�h��false���Z�b�g�BDB�̋N�����s����悤�ɂȂ�B
    System.setProperty("TestMode","true");
    if(System.getProperty("swe.path") == null) throw
      new IllegalStateException("System property 'swe.path' not found.");
    //DB�⌟�����ʂ̑���ʃt���[���ɂ���Ƃ���true���Z�b�g�B��̌^��false�B
    System.setProperty("SeparateMode","false");
    System.setProperty("DefaultTime",Config.data.getProperty("DefaultTime"));
    System.out.println("System : DefaultTime = " + System.getProperty("DefaultTime"));
    //JDBC �����[�h����
    try { Class.forName("org.hsqldb.jdbcDriver");
    } catch ( ClassNotFoundException e ) { e.printStackTrace(); }
    if(System.getProperty("TestMode").equals("false")) {
      //�{�ԃ��[�h
      //DB�ɐڑ����Ă݂邪���̒i�K��DB���N�����Ă�Γ�d�N���Ȃ̂ŏI���B
      //�܂肱���ł͐ڑ��ł��Ȃ��̂�������
      boolean doubleBoot = false;
      try {
        String driverURL = "jdbc:hsqldb:hsql://localhost";
        Connection con = DriverManager.getConnection(driverURL,"sa",
          Config.system.getProperty("db.admin.pw"));
        doubleBoot = true;
      } catch( SQLException e ) { 
        //e.printStackTrace();
      }
      System.out.println("JDBC���[�h����");
      if(doubleBoot) {
        JOptionPane.showMessageDialog(null,"��d�N���͂ł��܂���B","StarBase",
          JOptionPane.ERROR_MESSAGE);
        System.out.println("��d�N���͂ł��܂���B");
        System.exit(0);
      }
      //�W���o�͂������ւ��A�G���[��񓙂�log.txt�ɏo�͂���悤�ɂ���B
      try {
        PrintStream ps = new PrintStream(new File(Home.dir,"log.txt"),"sjis");
        System.setOut(ps);
        System.setErr(ps);
      } catch(IOException e) {
        e.printStackTrace();
      }
      //HSQLDB���N��
      String dbfile = null;
      try {
        //dbfile = Home.database.toURI().toURL().toString();
        dbfile = Home.database.toURL().toString();
      } catch (MalformedURLException e) { }
      // �s����""�̓f�[�^�x�[�X�ɕʖ���^����ۂɎg�p���邪�^���Ȃ��̂ŋ�B
      org.hsqldb.Server
        .main(new String [] {"-database.0",dbfile,"-dbname.0",""});
    }
    //�ް��ް��ɱ���Ă��p�ӂ���Ă��邩��������
    //������DB�̲ݽ�ݽ���擾�ł���ν��āB���߂Ȃ�Exception���o�Ľį��
    Database.getInstance();
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    configMenuItem = new javax.swing.JMenuItem();
    desktopPane = new javax.swing.JDesktopPane();
    bottomSplitPane = new javax.swing.JSplitPane();
    dbSplitPane = new javax.swing.JSplitPane();
    toolBar = new javax.swing.JToolBar();
    dbButton = new javax.swing.JButton();
    searchButton = new javax.swing.JButton();
    spreadButton = new javax.swing.JButton();
    spreadButton2 = new javax.swing.JButton();
    iconizeFrameButton = new javax.swing.JButton();
    expansionFrameButton = new javax.swing.JButton();
    closeFrameButton = new javax.swing.JButton();
    nowChartButton = new javax.swing.JButton();
    myChartButton = new javax.swing.JButton();
    menuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    registMenu = new javax.swing.JMenu();
    regNatalMenuItem = new javax.swing.JMenuItem();
    regEventMenuItem = new javax.swing.JMenuItem();
    chartMenu = new javax.swing.JMenu();
    natalChartMenuItem = new javax.swing.JMenuItem();
    eventMenuItem = new javax.swing.JMenuItem();
    chartAddMenu = new javax.swing.JMenu();
    addNatalMenuItem = new javax.swing.JMenuItem();
    addEventMenuItem = new javax.swing.JMenuItem();
    backupMenu = new javax.swing.JMenu();
    exportAllMenuItem = new javax.swing.JMenuItem();
    exportMenuItem = new javax.swing.JMenuItem();
    windowMenu = new javax.swing.JMenu();
    dbMenuItem = new javax.swing.JMenuItem();
    searchMenuItem = new javax.swing.JMenuItem();
    sabianDictMenuItem = new javax.swing.JMenuItem();
    calendarMenu = new javax.swing.JMenu();
    calendarMenuItem = new javax.swing.JMenuItem();
    enneagramMenuItem = new javax.swing.JMenuItem();
    newChartFrameMenuItem = new javax.swing.JMenuItem();
    jdayMenuItem = new javax.swing.JMenuItem();
    timeSliderMenuItem = new javax.swing.JMenuItem();
    moduleMenu = new javax.swing.JMenu();
    timeMenu = new javax.swing.JMenu();
    prefMenu = new javax.swing.JMenu();

    configMenuItem.setText("\u57fa\u672c\u8a2d\u5b9a");
    configMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        configMenuItemActionPerformed(evt);
      }
    });

    desktopPane.setBackground(new java.awt.Color(102, 102, 102));
    bottomSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    bottomSplitPane.setOneTouchExpandable(true);
    dbSplitPane.setOneTouchExpandable(true);
    bottomSplitPane.setBottomComponent(dbSplitPane);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    dbButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/database_icon.png")));
    dbButton.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9");
    dbButton.setToolTipText("\u30c7\u30fc\u30bf\u9078\u629e\u3068\u7ba1\u7406 Ctrl-D");
    dbButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dbButtonActionPerformed(evt);
      }
    });

    toolBar.add(dbButton);

    searchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search_icon.gif")));
    searchButton.setText("\u691c\u7d22");
    searchButton.setToolTipText("\u30c7\u30fc\u30bf\u691c\u7d22 Ctrl-F");
    searchButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchButtonActionPerformed(evt);
      }
    });

    toolBar.add(searchButton);

    toolBar.addSeparator();
    spreadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/spread2_icon.gif")));
    spreadButton.setToolTipText("\u5de6\u53f3\u306b\u6574\u5217");
    spreadButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        spreadButtonActionPerformed(evt);
      }
    });

    toolBar.add(spreadButton);

    spreadButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/spread3_icon.gif")));
    spreadButton2.setToolTipText("\u4e09\u9762\u3067\u6574\u5217");
    spreadButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        spreadButton2ActionPerformed(evt);
      }
    });

    toolBar.add(spreadButton2);

    iconizeFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/iconize_icon.gif")));
    iconizeFrameButton.setToolTipText("\u3059\u3079\u3066\u30a2\u30a4\u30b3\u30f3\u5316");
    iconizeFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        iconizeFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(iconizeFrameButton);

    expansionFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/expantion_icon.gif")));
    expansionFrameButton.setToolTipText("\u3059\u3079\u3066\u3092\u5c55\u958b");
    expansionFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        expansionFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(expansionFrameButton);

    closeFrameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/close_icon.gif")));
    closeFrameButton.setToolTipText("\u3059\u3079\u3066\u30af\u30ed\u30fc\u30ba");
    closeFrameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeFrameButtonActionPerformed(evt);
      }
    });

    toolBar.add(closeFrameButton);

    toolBar.addSeparator();
    nowChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/now_transit.gif")));
    nowChartButton.setToolTipText("\u4eca\u306e\u304a\u7a7a\u306e\u304a\u661f\u69d8");
    nowChartButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nowChartButtonActionPerformed(evt);
      }
    });

    toolBar.add(nowChartButton);

    myChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/natal.gif")));
    myChartButton.setToolTipText("\u30de\u30a4\u30c1\u30e3\u30fc\u30c8");
    myChartButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        myChartButtonActionPerformed(evt);
      }
    });

    toolBar.add(myChartButton);

    getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

    fileMenu.setMnemonic('F');
    fileMenu.setText("\u30d5\u30a1\u30a4\u30eb(F)");
    registMenu.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9\u306b\u767b\u9332");
    regNatalMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    regNatalMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c7\u30fc\u30bf\u306e\u767b\u9332");
    regNatalMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        regNatalMenuItemActionPerformed(evt);
      }
    });

    registMenu.add(regNatalMenuItem);

    regEventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c7\u30fc\u30bf\u306e\u767b\u9332");
    regEventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        regEventMenuItemActionPerformed(evt);
      }
    });

    registMenu.add(regEventMenuItem);

    fileMenu.add(registMenu);

    chartMenu.setText("\u767b\u9332\u305b\u305a\u306b\u30c1\u30e3\u30fc\u30c8\u4f5c\u6210");
    natalChartMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c1\u30e3\u30fc\u30c8");
    natalChartMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        natalChartMenuItemActionPerformed(evt);
      }
    });

    chartMenu.add(natalChartMenuItem);

    eventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c1\u30e3\u30fc\u30c8");
    eventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eventMenuItemActionPerformed(evt);
      }
    });

    chartMenu.add(eventMenuItem);

    fileMenu.add(chartMenu);

    chartAddMenu.setText("\u30c1\u30e3\u30fc\u30c8\u306b\u30c7\u30fc\u30bf\u8ffd\u52a0\u5165\u529b");
    chartAddMenu.setEnabled(false);
    addNatalMenuItem.setText("\u30cd\u30fc\u30bf\u30eb\u30c7\u30fc\u30bf\u8ffd\u52a0");
    addNatalMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addNatalMenuItemActionPerformed(evt);
      }
    });

    chartAddMenu.add(addNatalMenuItem);

    addEventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c7\u30fc\u30bf\u8ffd\u52a0");
    addEventMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addEventMenuItemActionPerformed(evt);
      }
    });

    chartAddMenu.add(addEventMenuItem);

    fileMenu.add(chartAddMenu);

    backupMenu.setText("\u30d0\u30c3\u30af\u30a2\u30c3\u30d7");
    exportAllMenuItem.setText("\u5168\u30d0\u30fc\u30b9\u30c7\u30fc\u30bf\u3092\u30a8\u30af\u30b9\u30dd\u30fc\u30c8");
    exportAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportAllMenuItemActionPerformed(evt);
      }
    });

    backupMenu.add(exportAllMenuItem);

    exportMenuItem.setText("\u9078\u629e\u3057\u305f\u30d5\u30a9\u30eb\u30c0\u3092\u30a8\u30af\u30b9\u30dd\u30fc\u30c8");
    exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportMenuItemActionPerformed(evt);
      }
    });

    backupMenu.add(exportMenuItem);

    fileMenu.add(backupMenu);

    menuBar.add(fileMenu);

    windowMenu.setMnemonic('W');
    windowMenu.setText("\u30a6\u30a3\u30f3\u30c9\u30a6(W)");
    windowMenu.addMenuListener(new javax.swing.event.MenuListener() {
      public void menuCanceled(javax.swing.event.MenuEvent evt) {
      }
      public void menuDeselected(javax.swing.event.MenuEvent evt) {
      }
      public void menuSelected(javax.swing.event.MenuEvent evt) {
        windowMenuMenuSelected(evt);
      }
    });

    dbMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
    dbMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/database_icon.png")));
    dbMenuItem.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9");
    dbMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dbMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(dbMenuItem);

    searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
    searchMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search_icon.gif")));
    searchMenuItem.setText("\u691c\u7d22");
    searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(searchMenuItem);

    sabianDictMenuItem.setText("\u30b5\u30d3\u30a2\u30f3\u8f9e\u66f8");
    sabianDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sabianDictMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(sabianDictMenuItem);

    calendarMenu.setText("\u30ab\u30ec\u30f3\u30c0\u30fc");
    calendarMenuItem.setText("\u5929\u4f53\u30ab\u30ec\u30f3\u30c0\u30fc");
    calendarMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        calendarMenuItemActionPerformed(evt);
      }
    });

    calendarMenu.add(calendarMenuItem);

    enneagramMenuItem.setText("\u30a8\u30cb\u30a2\u30b0\u30e9\u30e0\u30ab\u30ec\u30f3\u30c0\u30fc");
    enneagramMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        enneagramMenuItemActionPerformed(evt);
      }
    });

    calendarMenu.add(enneagramMenuItem);

    windowMenu.add(calendarMenu);

    newChartFrameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    newChartFrameMenuItem.setText("\u30c1\u30e3\u30fc\u30c8\u30d5\u30ec\u30fc\u30e0\u3092\u8907\u88fd");
    newChartFrameMenuItem.setEnabled(false);
    newChartFrameMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newChartFrameMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(newChartFrameMenuItem);

    jdayMenuItem.setText("\u30e6\u30ea\u30a6\u30b9\u8a08\u7b97\u6a5f");
    jdayMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jdayMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(jdayMenuItem);

    timeSliderMenuItem.setText("\u6642\u9593\u5236\u5fa1");
    timeSliderMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        timeSliderMenuItemActionPerformed(evt);
      }
    });

    windowMenu.add(timeSliderMenuItem);

    menuBar.add(windowMenu);

    moduleMenu.setMnemonic('C');
    moduleMenu.setText("\u30c1\u30e3\u30fc\u30c8(C)");
    menuBar.add(moduleMenu);

    timeMenu.setMnemonic('T');
    timeMenu.setText("\u6642\u9593(T)");
    menuBar.add(timeMenu);

    prefMenu.setMnemonic('P');
    prefMenu.setText("\u8a2d\u5b9a(P)");
    menuBar.add(prefMenu);

    setJMenuBar(menuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void configMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configMenuItemActionPerformed
    String defTime = Config.data.getProperty("DefaultTime");
    if(MainConfigDialog.showDialog(MainFrame.this,Config.data)) {
      // ��̫����т��ύX���ꂽ�Ƃ���DB�̓V�̈ʒu�̍Čv�Z���s��
      if(! defTime.equals(Config.data.getProperty("DefaultTime"))) {
        RecalculationDialog.showDialog(MainFrame.this);
      }
    }
  }//GEN-LAST:event_configMenuItemActionPerformed

  private void timeSliderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeSliderMenuItemActionPerformed
    if(isEmptyChartFrame()) return;
    timeSliderDialog.pack();
    timeSliderDialog.setVisible(true);
  }//GEN-LAST:event_timeSliderMenuItemActionPerformed

  private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
    TreePath path = editor.
      showFolderSelectDialog("�G�N�X�|�[�g����t�H���_��I�����Ă��������B");
    if(path != null) DataExporterDialog.export(this,path);
  }//GEN-LAST:event_exportMenuItemActionPerformed
//�S�o�[�X�f�[�^���G�N�X�|�[�g
  private void exportAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAllMenuItemActionPerformed
    DataExporterDialog.exportAll(this);
  }//GEN-LAST:event_exportAllMenuItemActionPerformed

  private void jdayMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdayMenuItemActionPerformed
    jdayDialog.setVisible(true);
  }//GEN-LAST:event_jdayMenuItemActionPerformed

  private void sabianDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sabianDictMenuItemActionPerformed
    sabianDialog.setVisible(true);
  }//GEN-LAST:event_sabianDictMenuItemActionPerformed
// ���C���t���[���̃N���[�Y�{�^���������ꂽ�Ƃ�
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    editor.dispose();
    Database.getInstance().setProperties("Default",System.getProperties());
    Ephemeris.getSwissEph().swe_close();
  }//GEN-LAST:event_formWindowClosing
//���������Ă����΂₭�\��   MyData�����è��ID������Ȱ�����Ă�\��
  private void myChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myChartButtonActionPerformed
    //Properties prop = Database.getInstance().getProperties("Default");
    //String value = prop.getProperty("MyData");
    String value = Config.data.getProperty("MyData");
    if(value == null) return;
    int id = Integer.parseInt(value);
    List<Natal> list = new ArrayList<Natal>();
    Natal natal = Database.getInstance().getNatal(id);
    if(natal == null) return;
    list.add(natal);
    openChartFrame( list );
  }//GEN-LAST:event_myChartButtonActionPerformed
//���݂̃`���[�g���o��
  private void nowChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nowChartButtonActionPerformed
// TODO add your handling code here:
    //Natal natal = Natal.getDefault();
    Natal natal = new Natal();
    Place place = Config.data.getPlace("DefaultTransitPlace");
    natal.setPlace(place);
    natal.setCalendar(new GregorianCalendar(),TimePlace.DATE_AND_TIME);

    natal.setChartType(Natal.EVENT);
    natal.setName("����");
    List<Natal> list = new ArrayList<Natal>();
    list.add(natal);
    openChartFrame( list );
  }//GEN-LAST:event_nowChartButtonActionPerformed
  
  private void enneagramMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enneagramMenuItemActionPerformed
    EnneagramCalendar2.exec(false);
  }//GEN-LAST:event_enneagramMenuItemActionPerformed
  
  private void calendarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarMenuItemActionPerformed
    AstroCalendar.exec(false);
  }//GEN-LAST:event_calendarMenuItemActionPerformed
  //�E�B���h�D(W)���I�����ꂽ�Ƃ��̃C�x���g����
  private void windowMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_windowMenuMenuSelected
    //newChartFrameMenuItem.setEnabled(desktopPane.getSelectedFrame() != null);
  }//GEN-LAST:event_windowMenuMenuSelected
  //�E�B���h�E(W) ���Ԑ���p�l��  //�E�B���h�E(W) �`���[�g�t���[���𕡐�
  private void newChartFrameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChartFrameMenuItemActionPerformed
    duplicateChartFrame();
    //java.awt.EventQueue.invokeLater(new ChartFrameDuplicator());
    //newChartFrameActionPerformed();
  }//GEN-LAST:event_newChartFrameMenuItemActionPerformed
  //�E�B���h�E(W) ����
  private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
    searchButton.doClick();
  }//GEN-LAST:event_searchMenuItemActionPerformed
  //�E�B���h�E(W) �f�[�^�x�[�X
  private void dbMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbMenuItemActionPerformed
    dbButton.doClick();
  }//GEN-LAST:event_dbMenuItemActionPerformed
  //�t�@�C��(F)���`���[�g�Ƀf�[�^�ǉ����́��C�x���g�f�[�^�ǉ�
  private void addEventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEventMenuItemActionPerformed
    addNatalToChart(Natal.EVENT);
  }//GEN-LAST:event_addEventMenuItemActionPerformed
  //�t�@�C��(F) �o�^�����`���[�g�쐬���C�x���g�`���[�g
  private void eventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventMenuItemActionPerformed
    createChart(Natal.EVENT);
  }//GEN-LAST:event_eventMenuItemActionPerformed
  //�t�@�C��(F)���`���[�g�Ƀf�[�^�ǉ����́��l�C�^���f�[�^�ǉ�
  private void addNatalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNatalMenuItemActionPerformed
    addNatalToChart(Natal.NATAL);
  }//GEN-LAST:event_addNatalMenuItemActionPerformed
  //�t�@�C��(F) �o�^�����`���[�g�쐬���l�C�^���`���[�g
  private void natalChartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_natalChartMenuItemActionPerformed
    createChart(Natal.NATAL);
  }//GEN-LAST:event_natalChartMenuItemActionPerformed
  //�t�@�C��(F)�u�C�x���g�f�[�^�̓o�^�v
  private void regEventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regEventMenuItemActionPerformed
    editor.registEvent();
  }//GEN-LAST:event_regEventMenuItemActionPerformed
  //�t�@�C��(F)�u�l�C�^���f�[�^�̓o�^�v
  private void regNatalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regNatalMenuItemActionPerformed
    editor.registNatal();
  }//GEN-LAST:event_regNatalMenuItemActionPerformed
  //�R�ʂŐ���
  private void spreadButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spreadButton2ActionPerformed
    frame0 = desktopPane.getSelectedFrame();
    if(frame0 == null) return; // �t���[������I����ԂȂ�I���
    Rectangle rect = desktopPane.getBounds();
    int w = (int)((float)rect.width * 0.618f);
    int w2 = rect.width - w;
    int h = rect.height;
    int h2 = rect.height / 2;
    List<ChartInternalFrame> frameList = new ArrayList<ChartInternalFrame>();
    //�A�C�R��������Ă���t���[���ȊO�̃t���[�������X�g�A�b�v
    //List<ChartInternalFrame> cfList = getChartInternalFrames();
    for(ChartInternalFrame cif : getChartFrames()) {
      if(! cif.isIcon()) frameList.add(cif);
    }
    if(count >= frameList.size()) count = 0;
    //����̂Ƃ��͑I������Ă���t���[�������C��(�傫�ȃT�C�Y)�̈ʒu�ɂ����Ă���B
    if(frameList.size() >= 2) {
      for(int i=0; i<count; i++) {
        ChartInternalFrame fr = frameList.get(0);
        frameList.remove(0);
        frameList.add(fr);
      }
    }
    frameList.get(0).reshape(w2,0,w,h); //�ꖇ�ڂ��X�v���b�h(������)
    frameList.get(0).toFront();
    if(frameList.size() == 1) return;   //�񖇖ڂ������Ȃ�I���
    frameList.get(1).reshape(0,0,w2,h2); //�񖇖ڂ��X�v���b�h(����)
    frameList.get(1).toFront();
    if(frameList.size() == 2) { count++; return; } //�O���ڂ������Ȃ�I���
    frameList.get(2).reshape(0,h2,w2,h2); //�O���ڂ��X�v���b�h(����)
    frameList.get(2).toFront();
    count++;
  }//GEN-LAST:event_spreadButton2ActionPerformed
  //�����t���[�������ׂăN���[�Y
  private void closeFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeFrameButtonActionPerformed
//    this.showTimeController(false);
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
//      ((ChartInternalFrame)frames[i]).doDefaultCloseAction();
      try {
        ((ChartInternalFrame)frames[i]).setClosed(true);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_closeFrameButtonActionPerformed
  //�����t���[�������ׂĔ�A�C�R����
  private void expansionFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expansionFrameButtonActionPerformed
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
      try {
        frames[i].setIcon(false);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_expansionFrameButtonActionPerformed
  //�����t���[�������ׂăA�C�R����
  private void iconizeFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconizeFrameButtonActionPerformed
    JInternalFrame [] frames = getChartFrames(); //desktopPane.getAllFrames();
    if(frames.length == 0) return;
    for(int i=0; i<frames.length; i++) {
      try {
        frames[i].setIcon(true);
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    }
  }//GEN-LAST:event_iconizeFrameButtonActionPerformed
  //�Q�ʂŐ���
  private void spreadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spreadButtonActionPerformed
    //�A�C�R��������Ă�����̂��݂������������
//    for(JInternalFrame f : iFrames) {
//      if(f.isIcon()) {
//        JInternalFrame frame = desktopPane.getSelectedFrame();
//        expansionFrameButtonActionPerformed(null);
//        try {
//          if(frame != null) frame.setSelected(true);
//        } catch (PropertyVetoException ex) {
//          ex.printStackTrace();
//        }
//        System.out.println("�A�C�R����������");
//        break;
//      }
//    }
    frame0 = getSelectedFrame(); //desktopPane.getSelectedFrame();
    if(frame0 == null) return; // �t���[������I����ԂȂ�I���
    Rectangle rect = desktopPane.getBounds();
    int w = rect.width / 2;
    int h = rect.height;
    frame0.reshape(0,0,w,h);
    frame0.toFront(); //�ꖇ�ڂ��X�v���b�h
    ChartInternalFrame [] cFrames = getChartFrames();
    if(cFrames.length == 1) return;//�ꖇ�����Ȃ��Ȃ�I���
    List<ChartInternalFrame> frameList = new ArrayList<ChartInternalFrame>();
    // �ꖇ�ڂ̃t���[���ƃA�C�R��������Ă���t���[���ȊO�̃t���[�������X�g�A�b�v
    for(ChartInternalFrame cif: cFrames) {
      if(cif != frame0 && (! cif.isIcon()))
        frameList.add(cif);
    }
    if(count >= frameList.size()) count = 0;
    ChartInternalFrame frame1 = frameList.get(count);
    frame1.reshape(w,0,w,h);
    frame1.toFront();
    count++;
  }//GEN-LAST:event_spreadButtonActionPerformed
  //�c�[���o�[�́u�����v�{�^���̏���
  private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    if(System.getProperty("SeparateMode","false").equals("true")) {
      editor.setExtendedState(Frame.NORMAL);
      editor.setVisible(true);
    } else {
      bottomSplitPane.setDividerLocation(0.7);
    }
    editor.showSearchFrame();
  }//GEN-LAST:event_searchButtonActionPerformed
  //�c�[���o�[�́u�f�[�^�x�[�X�v�{�^���̏���
  private void dbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbButtonActionPerformed
    if(System.getProperty("SeparateMode","false").equals("true")) {
      editor.setExtendedState(Frame.NORMAL);
      editor.setVisible(true);
      searchButton.setEnabled(true);
    } else {
      if(devider == 0) {
        bottomSplitPane.setDividerLocation(0.7);
        devider = bottomSplitPane.getDividerLocation();
      }
      if(devider == bottomSplitPane.getDividerLocation())
        bottomSplitPane.setDividerLocation(1.0);
      else
        bottomSplitPane.setDividerLocation(0.7);
    }
  }//GEN-LAST:event_dbButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem addEventMenuItem;
  private javax.swing.JMenuItem addNatalMenuItem;
  private javax.swing.JMenu backupMenu;
  private javax.swing.JSplitPane bottomSplitPane;
  private javax.swing.JMenu calendarMenu;
  private javax.swing.JMenuItem calendarMenuItem;
  private javax.swing.JMenu chartAddMenu;
  private javax.swing.JMenu chartMenu;
  private javax.swing.JButton closeFrameButton;
  private javax.swing.JMenuItem configMenuItem;
  private javax.swing.JButton dbButton;
  private javax.swing.JMenuItem dbMenuItem;
  private javax.swing.JSplitPane dbSplitPane;
  private javax.swing.JDesktopPane desktopPane;
  private javax.swing.JMenuItem enneagramMenuItem;
  private javax.swing.JMenuItem eventMenuItem;
  private javax.swing.JButton expansionFrameButton;
  private javax.swing.JMenuItem exportAllMenuItem;
  private javax.swing.JMenuItem exportMenuItem;
  private javax.swing.JMenu fileMenu;
  private javax.swing.JButton iconizeFrameButton;
  private javax.swing.JMenuItem jdayMenuItem;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenu moduleMenu;
  private javax.swing.JButton myChartButton;
  private javax.swing.JMenuItem natalChartMenuItem;
  private javax.swing.JMenuItem newChartFrameMenuItem;
  private javax.swing.JButton nowChartButton;
  private javax.swing.JMenu prefMenu;
  private javax.swing.JMenuItem regEventMenuItem;
  private javax.swing.JMenuItem regNatalMenuItem;
  private javax.swing.JMenu registMenu;
  private javax.swing.JMenuItem sabianDictMenuItem;
  private javax.swing.JButton searchButton;
  private javax.swing.JMenuItem searchMenuItem;
  private javax.swing.JButton spreadButton;
  private javax.swing.JButton spreadButton2;
  private javax.swing.JMenu timeMenu;
  private javax.swing.JMenuItem timeSliderMenuItem;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JMenu windowMenu;
  // End of variables declaration//GEN-END:variables
  
}
