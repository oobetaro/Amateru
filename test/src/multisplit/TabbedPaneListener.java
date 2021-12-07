/*
 * TabbedPaneListener.java
 *
 * Created on 2007/10/30, 16:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package multisplit;

import java.awt.Component;

/**
 * InnerTabbedPane�ɃZ�b�g���郊�X�i
 * @author ���V�`��
 */
public interface TabbedPaneListener {
  /**
   * �^�u�̃N���[�Y�{�^���Ń^�u������ꂽ�B
   * @param tabpan �^�u������������InnerTabbedPane
   * @param c ����ꂽ�^�u�ɓ����Ă����R���|�[�l���g
   */
  void closedTab( InnerTabbedPane tabpan, Component c );
  /**
   * �^�u���_�u���N���b�N���ꂽ�B
   * @param tabpan �_�u���N���b�N����������InnerTabbedPane
   * @param c �_�u�N�����ꂽ�^�u�ɓ����Ă����R���|�[�l���g
   */
  void doubleClicked( InnerTabbedPane tabpan, Component c);
}
