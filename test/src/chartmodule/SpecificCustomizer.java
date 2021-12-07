/*
 * SpecificCustomizer.java
 *
 * Created on 2007/03/31, 8:21
 *
 */

package chartmodule;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 * �F�̐ݒ��A�v�Z�ݒ���Ǘ����邽�߂̃N���X�ŁA�ݒ�ɕK�v��JMenu��JMenuItem
 * ��񋟂��A���̃��j���[���I�����ꂽ�ۂɁASpecificSaveDialo,SpecificRemoveDialog,
 * SpecificDialog�Ȃǂ��Ăт����A�ۑ��A����(�폜�A���Ԍ�����)�A�ݒ�A�ݒ胍�[�h
 * �Ȃǂ̏������s���B
 * ���̃N���X�́A�ݒ�Ǘ��𑍍��I�ɍs���N���X�ŁA�O�q�̃_�C�A���O���g������܂Ƃߖ��B
 * �ݒ�͐F�ݒ�Ƒ��̎g�p�V�̂�v�Z�d�l�̓��ނ�����A���̃N���X���쐬����ۂ�
 * �ǂ���̐ݒ���s�������̃��[�h���w�肷��B
 * ���[�h�̈Ⴂ�ɉ����ăe�[�u������ύX����̂ƁAChartModulePanel���̌Ăт���
 * ���\�b�h��؂�ւ��Ă��邾���B
 * @author ���V�`��
 */
class SpecificCustomizer {

  private ChartModulePanel module;
  private MainFrame mainFrame;
  private boolean update = false;
  /**
   * �F�ݒ�ҏW���[�h��\���萔�ŁA�R���X�g���N�^�̈����Ɏg�p����B
   */
  public static final int COLOR_EDIT_MODE = 0;
  /**
   * �d�l�ҏW���[�h��\���萔�ŁA�R���X�g���N�^�̈����Ɏg�p����B
   */
  public static final int SPECIFIC_EDIT_MODE = 1;
  /**
   * [0]�͐F�ݒ胂�[�h�̂Ƃ��Ɏg�p�����DB��̃e�[�u�����B
   * [1]�͎g�p�ݒ胂�[�h�̂Ƃ��Ɏg�p�����DB��̃e�[�u�����B
   */
  protected static final String [] tableNames = 
    { "COLOR_PROPERTIES","SPECIFIC_PROPERTIES" };
  private int mode;
  private Database db = Database.getInstance();
  // �I������Ă���ݒ���Bload()�̍ۂɍX�V�����B
  private Preference selectedPref = new Preference();
  // �I������Ă���ݒ薼�B�ݒ薼���X�g���I�����ꂽ�Ƃ��X�V�����B""�Ȃ�f�t�H���g�ݒ���Ӗ�����B
  private String selectedConfName = "";
  private JMenu selectionMenu;               // �I�����j���[
  private JRadioButtonMenuItem defaultRadioButtonMenuItem; //�u�W���v�ƭ�����
  private JMenuItem customizeMenuItem;       //�u���ϲ�ށv�ƭ�����
  private JMenuItem arrangmentMenuItem;      //�u�����v�ƭ�����
  private JMenuItem makeDefaultMenuItem; //�u����l�ɂ���v�ƭ�����

  /**
   * �I�u�W�F�N�g���쐬����B
   * @param mainFrame �e�ƂȂ�t���[���Œʏ�MainFrame���w�肷��B
   * @param mode �t�B�[���h�萔��COLOR_EDIT_MODE�܂���SPECIFIC_EDIT_MODE�̂ǂ�
   * �炩���w�肷��B
   */
  public SpecificCustomizer(MainFrame mainFrame,int mode) {
    this.mainFrame = mainFrame;
    this.mode = mode;
  }
  
  /**
   * ���[�h�ɉ�����DB��̃e�[�u������Ԃ��B
   */
  public String getTableName() {
    return tableNames[mode];
  }
  /**
   * ���̃I�u�W�F�N�g��COLOR_EDIT_MODE��SPECIFIC_EDIT_MODE����Ԃ��B
   * @return COLOR_EDIT_MODE�܂���SPECIFIC_EDIT_MODE���̂ǂ��炩�B
   */
  public int getMode() {
    return mode;
  }
  /**
   * �����l�Ƃ��ēo�^����Ă���ݒ薼����ݒ��ChartModulePanel�Ƀ��[�h����B
   * ChartInternalFrame����ŏ��ɃR�[������A������͌Ăяo����Ȃ��B
   */
  public void loadDefault(ChartModulePanel module) {
    this.module = module;
    String confName = (getMode() == COLOR_EDIT_MODE) ?
      ".DefaultColorConfName" : ".DefaultSpecificConfName";
    selectedConfName =
      Config.data.getProperty(module.getClassName() + confName,"");
    load(selectedConfName);
    update = true;
  }
  //���j���[���炢�����̐ݒ�̒�����I�������Ƃ��̏����B
  //�ݒ�(Properties�ɂȂ�������)��DB����۰�ނ���Ӽޭ�قɔ��f������
  //Ӽޭ�ق������Ă��鶽�ϲ�����قɂ�۰�ޓ��e�𔽉f������B
  private void load(String confName) {
    assert module != null;
    this.module = module;
    String className = module.getClassName();
    //�F�ݒ�Ӱ�ނ̂Ƃ�
    if(getMode() == COLOR_EDIT_MODE) {
      if(module.getDefaultColor() == null) return; //��̫�Đݒ肪�����Ɩ���
      //selectedPref = new Preference(module.getDefaultColor());
      selectedPref.clear();
      selectedPref.copy( module.getDefaultColor() );
      boolean found = db.getConfigProperties(
        confName, className, selectedPref, getTableName());
      module.getColorCustomizePanel().setPreference(selectedPref);
      module.setColorConfig(selectedPref);
    }
    //�d�l�ݒ�Ӱ�ނ̂Ƃ�
    else {
      if(module.getDefaultSpecific() == null) return; //��̫�Đݒ肪�����Ɩ���
      //selectedPref = new Preference(module.getDefaultSpecific());
      selectedPref.clear();
      selectedPref.copy( module.getDefaultSpecific() );
      boolean found = db.getConfigProperties(
        confName, className, selectedPref, getTableName()); 
      //if( ! found ) selectedPref.copy( module.getDefaultSpecific() ); �ŏ��ɓǂ݂���ł邩��s�v
      module.getSpecificCustomizePanel().setPreference(selectedPref);
      module.setSpecificConfig(selectedPref); //�����͎��̂Ƃ��s�v
    }
  }
  // �ƭ��̖��O�BӰ�ނɂ���Ė��O�͐؂�ւ��B
  private static final String [] selectionMenuNames = { 
    "�F�ݒ�̐ؑ�","�v�Z�d�l�̐ؑ�" };
  private static final String [] customizeMenuNames = { 
    "�z�F�̃J�X�^�}�C�Y","�v�Z�d�l�̃J�X�^�}�C�Y" };
  private static final String [] arrangmentMenuNames = { 
    "�F�ݒ�̐���","�v�Z�d�l�̐���" };
  /**
   * �@�\���j���[���쐬���ĕԂ��B���̃��j���[��MainFrame�̃��j���[�o�[�ɑg�ݍ��܂�
   * GUI�Ƃ��Ďg�p�����BChartModulePanel��getDefaultColor()�܂���
   * getDefaultSpecific()��null��Ԃ��ꍇ��Disenabled�ɂȂ���JMenu��Ԃ��B
   */
  public JMenu getMenu() {
    assert module != null;
    this.module = module;
    // module���ύX���ꂽ�Ƃ��̂݃��j���[����蒼���Bmodule���ύX�����̂́A
    // loadDefault()���O������Ă΂ꂽ�Ƃ��̂݁B
    if(! update ) return selectionMenu;
    update = false;
    selectionMenu = new JMenu(selectionMenuNames[getMode()]);
    if(getMode() == COLOR_EDIT_MODE) {
      if(module.getDefaultColor() == null) {
        selectionMenu.setEnabled(false);
        return selectionMenu;
      }
    } else {
      if(module.getDefaultSpecific() == null) {
        selectionMenu.setEnabled(false);
        return selectionMenu;
      }      
    }
    //�u�I��������l�Ɂv���j���[���쐬
    makeDefaultMenuItem = new JMenuItem("�I��������l��");
    makeDefaultMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String confName = (getMode() == COLOR_EDIT_MODE) ? 
          ".DefaultColorConfName" : ".DefaultSpecificConfName";
        Config.data
          .setProperty(module.getClassName() + confName, selectedConfName);
        update = true;
      }
    });
    // �u�W���v���j���[���쐬
    defaultRadioButtonMenuItem = new JRadioButtonMenuItem("�W��");
    defaultRadioButtonMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectedConfName = "";
        load("");
        update = true;
      }
    });
    // �u�ݒ�̐����v���j���[���쐬
    arrangmentMenuItem = new JMenuItem( arrangmentMenuNames[ mode ]);
    arrangmentMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        selectedConfName = SpecificRemoveDialog.showDialog(
          mainFrame,
          module.getClassName(),
          selectedConfName,
          SpecificCustomizer.this);
        load(selectedConfName);
        update = true;
      }
    });

    // �u�F�̃J�X�^�}�C�Y�v���j���[���쐬
    customizeMenuItem = new JMenuItem( customizeMenuNames[ mode ]);
    customizeMenuItem.addActionListener(new SpecificDialogHandler());
    
    //�A�N�Z�����[�^�[�L�[��ݒ�
    if(mode == 0) //�F�ݒ�_�C�A���O��Ctrl+Shift+S�ŌĂт���
      customizeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | 
        java.awt.event.InputEvent.CTRL_MASK));
    else //�v�Z�ݒ�_�C�A���O��Ctrl+S�ŌĂт���
      customizeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));

    Vector<String> vector = new Vector<String>();
    String className = module.getClassName();
    db.getConfigNames(className,vector,getTableName());
    ButtonGroup bGroup = new ButtonGroup();
    //�ݒ薼�̈ꗗ���ƭ����тɂ����ƭ��ɓo�^
    for(String name:vector) {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
      if(name.equals(selectedConfName)) item.setSelected(true);
      bGroup.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          JMenuItem item = (JMenuItem)evt.getSource();
          selectedConfName = item.getText();
          load( selectedConfName );
        }
      });
      selectionMenu.add(item);
    }
    if(selectedConfName.equals("")) 
      defaultRadioButtonMenuItem.setSelected(true);
    bGroup.add(defaultRadioButtonMenuItem);
    selectionMenu.addSeparator();                  // |---------------------
    selectionMenu.add(defaultRadioButtonMenuItem); // | �W��
    selectionMenu.addSeparator();                  // |---------------------
    selectionMenu.add(makeDefaultMenuItem);        // | �I��������l��
    selectionMenu.add(customizeMenuItem);          // | �J�X�^�}�C�Y
    selectionMenu.add(arrangmentMenuItem);         // | �ݒ�̐���
    selectionMenu.setEnabled(true);
    return selectionMenu;
  }
  //���j���[���L�b�N���ꂽ��ݒ�p�l����\������
  //�ݒ�p�l�����J�����тɃC���X�^���X����蒼���Ă����̂���߂ɂ��邽�ߕʃN���X��p�ӂ����B
  
  class SpecificDialogHandler implements ActionListener {
    SpecificDialog dialog;
    
    SpecificDialogHandler() {
      CustomizePanel customPanel = (getMode() == COLOR_EDIT_MODE) ?
        module.getColorCustomizePanel() : module.getSpecificCustomizePanel();
      dialog = new SpecificDialog(  mainFrame,
                                    selectedPref,
                                    selectedConfName,
                                    customPanel,
                                    module.getClassName(),
                                    new ColorChangeHandler(),
                                    SpecificCustomizer.this);
    }
    
    public void actionPerformed(ActionEvent evt) {
      dialog.setConfName( selectedConfName );
      dialog.setVisible(true);
      update = true;
    }
  }
  // �ݒ�ύX�n���h��
  //�{�^���������ꂽ�Ƃ��ɁA�T�u�N���X�ŕ\������Ă���`���[�g�̐ݒ�
  //���\�b�h���Ăяo���B����ɂ��`���[�g�̕\���F��v�Z�d�l�ς��B

  class ColorChangeHandler implements ChangeListener {
    public void stateChanged(ChangeEvent evt) {
      assert module != null;
      SpecificDialog dialog = (SpecificDialog)evt.getSource();
      //�v���r���[�̏ꍇ�ASpecificDialog����v���r���[�p��Preference���ăZ�b�g
      int state = dialog.getState();
      if(state == SpecificDialog.PREVIEW) {
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(dialog.getPreviewPreference());
        } else {
          module.setSpecificConfig(dialog.getPreviewPreference());
        }
      }
      else if(state == SpecificDialog.CANCELED) {
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(selectedPref);
        } else {
          module.setSpecificConfig(selectedPref);
        }
      }
      else if(state == SpecificDialog.SAVE || state == SpecificDialog.USE ) {
        //���̎��_��Save������SpesificDialog�ŏ����ς݂ɂȂ��Ă���
        if(getMode() == COLOR_EDIT_MODE) {
          module.setColorConfig(selectedPref);
        } else {
          module.setSpecificConfig(selectedPref);
        }
      } else 
        throw new UnsupportedOperationException(
          "SpecificDialog�͂���state=" + state + "���T�|�[�g���܂���");
    }
  }
}
