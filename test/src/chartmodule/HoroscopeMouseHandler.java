/*
 * HoroscopeMouseHandler.java
 *
 * Created on 2007/01/13, 18:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import to.tetramorph.starbase.widget.WordBalloonHandler;

/**
 * �z���X�R�[�v��\������`���[�g���W���[�����Ŏg���}�E�X�A�_�v�^�[�B
 * �}�E�X�C�x���g�̏������c�[���{�^���Ő؂�ւ��邽�߂ɁA
 * �}�E�X���[�V�������X�i�ł�����}�E�X���X�i�ł����胏�[�h�o���[���n���h��
 * �ł�����N���X������Ɠs�����悩�����̂ł��̃N���X���쐬���ꂽ�B
 * HoroscopeChartModulePanel���̃}�E�X�C�x���g�́A���̃N���X���p�����č쐬
 * ���Ă���B
 * @author ���V�`��
 */
public class HoroscopeMouseHandler extends MouseAdapter
  implements MouseMotionListener,WordBalloonHandler {
  public void mouseDragged(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }
  public Object getSelectedObject() { 
    return null;
  }
  public void setSelectedObject(Object o) {
    
  }
}

