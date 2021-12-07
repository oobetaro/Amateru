/*
 * TabCloseButton.java
 *
 * Created on 2007/10/30, 4:16
 *
 */

package to.tetramorph.starbase.widget;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import to.tetramorph.util.IconLoader;

/**
 * [×]ボタン。
 * @author 大澤義鷹
 */
public class PanelCloseButton extends JRadioButton {
  private static final Icon normalIcon;
  private static final Icon pressedIcon;
  private static final Icon enteredIcon;
  private static final Insets zeroInsets = new Insets(0,0,0,0);
  private static final Dimension size = new Dimension(13,14);
  
  static {
    normalIcon = IconLoader.getImageIcon("/resources/close0.gif");
    pressedIcon = IconLoader.getImageIcon("/resources/close2.gif");
    enteredIcon =  IconLoader.getImageIcon("/resources/close1.gif");
  }
  
  /**  オブジェクトを作成する */
  public PanelCloseButton() {
    super();
    setIcon(normalIcon);
    setPressedIcon(pressedIcon);
    setRolloverIcon(enteredIcon);
    setRolloverSelectedIcon(enteredIcon);
    setMargin(zeroInsets);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setContentAreaFilled(false);
  }
  
}
