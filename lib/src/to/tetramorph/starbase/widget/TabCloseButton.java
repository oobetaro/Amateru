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
import to.tetramorph.starbase.widget.TabCloseButton;
import to.tetramorph.util.IconLoader;

/**
 * JTabbedPaneのタブ内に取り付けるための[×]ボタン。
 * @author 大澤義鷹
 */
public class TabCloseButton extends JRadioButton {
  private static final Icon closeIcon;
  private static final Icon pressedIcon;
  private static final Icon focusIcon;
  private static final Insets zeroInsets = new Insets(0,0,0,0);
  private static final Dimension size = new Dimension(11,10);
  
  static {
    closeIcon = IconLoader.getImageIcon("/resources/tabButton.gif");
    pressedIcon = IconLoader.getImageIcon("/resources/tabButtonPressed.gif");
    focusIcon =  IconLoader.getImageIcon("/resources/tabButtonFocus.gif");
  }
  
  /**  TabCloseButton オブジェクトを作成する */
  public TabCloseButton() {
    super();
    setIcon(closeIcon);
    setPressedIcon(pressedIcon);
    setRolloverIcon(focusIcon);
    setRolloverSelectedIcon(focusIcon);
    setMargin(zeroInsets);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setContentAreaFilled(false);
  }
  
}
