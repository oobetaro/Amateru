/*
 * SplitPane.java
 * Created on 2007/10/26, 0:23
 */

package multisplit;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * JSplitPane�𑽏d�ɓ���q�ɂ��Ďg�����Ƃ����Ƃ��A�f�o�C�_�̃f�U�C����{�[�_�[
 * �̐ݒ肪�ז��ɂȂ�B������������Ȃ�̂��������Ȃ����̂ɂ�����p��SplitPane�B
 * @author ���V�`��
 */
public class SplitPane extends JSplitPane {
  static final Border emptyBorder = new EmptyBorder(0,0,0,0);
  /**
   * ����SplitPane�̃f�o�C�_�̃T�C�Y�Œl��4(pixcel)�B����ȏ㏬�����ƁA�}�E�X����
   * �������炭�Ȃ�B
   */
  public static final int DIVIDER_SIZE = 4;
  /**  
   * SplitPane �I�u�W�F�N�g���쐬���� 
   */
  public SplitPane() {
    super();
    setDividerSize( DIVIDER_SIZE );
    setResizeWeight(1.0);
    setBorder(emptyBorder);
    setContinuousLayout( true );
    setUI( new MyUI() );
  }
  /**
   * SplitPane �I�u�W�F�N�g���쐬����B
   */
  public SplitPane( int orientation, Component left, Component right) {
    super(orientation,left,right);
    setDividerSize( DIVIDER_SIZE );
    setResizeWeight(1.0);
    setContinuousLayout( true );
    setBorder(emptyBorder);
    setUI( new MyUI() );
  }
  /**
   * UI���X�V
   */
  public void updateUI() {
//    setDividerSize( DIVIDER_SIZE );
//    setBorder(emptyBorder);
    setUI( new MyUI() );
    revalidate();
  }
  
  class MyUI extends BasicSplitPaneUI {
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new Divider(this);
    }
  }
  // �e�N���X�ł̓{�[�_�[����`�悷�鏈�������Ă���̂����A
  // paint()���I�[�o�[���C�h���ĂȂɂ����Ȃ��B
  
  class Divider extends BasicSplitPaneDivider {
    
    Divider(BasicSplitPaneUI ui) {
      super(ui);
    }
    @Override
    public void paint(Graphics g) {
    }
  }
}
