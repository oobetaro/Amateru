/*
 * PanelVisibleButton.java
 *
 * Created on 2007/11/08, 0:11
 *
 */

package to.tetramorph.starbase.widget;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import to.tetramorph.util.IconLoader;

/**
 * ShutterPaneなどで使用される「可視化ボタン」。
 * @author 大澤義鷹
 */
public class PanelVisibleButton extends JToggleButton {
  private static final Icon normalIcon;
  private static final Icon pressedIcon;
  private static final Icon enteredIcon;
  private static final Insets zeroInsets = new Insets(0,0,0,0);
  private static final Dimension size = new Dimension(13,14);
  
  static {
    normalIcon = IconLoader.getImageIcon("/resources/visible0.gif");
    pressedIcon = IconLoader.getImageIcon("/resources/visible2.gif");
    enteredIcon =  IconLoader.getImageIcon("/resources/visible1.gif");
  }
    
  /**  PanelVisibleButton オブジェクトを作成する */
  public PanelVisibleButton() {
    super();
    setIcon(normalIcon);
    setSelectedIcon(pressedIcon);
//    setPressedIcon(pressedIcon);
    setRolloverIcon(enteredIcon);
    setRolloverSelectedIcon(pressedIcon);
    setMargin(zeroInsets);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorderPainted(false);
  }
  /**
   * このボタンはテキストをセットしても無効。GUIエディタが勝手に
   * 文字を挿入するので、便宜上オーバーライドして無効化してある。
   */
  public void setText(String text) {
    
  }
  public static void main(String args[]) {
    PanelVisibleButton b = new PanelVisibleButton();
  }
}
