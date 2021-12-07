/*
 * TabCloseButton.java
 *
 * Created on 2007/10/30, 4:16
 *
 */

package split;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

/**
 * JTabbedPane�̃^�u���Ɏ��t���邽�߂�[�~]�{�^���B
 * @author ���V�`��
 */
public class TabCloseButton extends JRadioButton {
  private static Icon closeIcon;
  private static Icon pressedIcon;
  private static Icon focusIcon;
  private static final Insets zeroInsets = new Insets(0,0,0,0);
  private static final Dimension size = new Dimension(9,9);
//button.setMargin(new Insets(0,0,0,0));
//button.setPreferredSize(new Dimension(30,16));
  
  static {
    closeIcon = new ImageIcon(
      TabCloseButton.class.getResource("/resources/tabButton.gif"));
    pressedIcon = new ImageIcon(
      TabCloseButton.class.getResource("/resources/tabButtonPressed.gif"));
    focusIcon = new ImageIcon(
      TabCloseButton.class.getResource("/resources/tabButtonFocus.gif"));
  }
  
  /**  TabCloseButton �I�u�W�F�N�g���쐬���� */
  public TabCloseButton() {
    super();
    setIcon(closeIcon);
    setPressedIcon(pressedIcon);
    setRolloverIcon(focusIcon);
    setRolloverSelectedIcon(focusIcon);
    setMargin(zeroInsets);
    setPreferredSize(size);
    setMinimumSize(size);
  }
  
  public static void main(String [] args) {
    
  }
}
