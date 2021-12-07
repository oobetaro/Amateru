/*
 *
 */
package to.tetramorph.starbase.widget;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.util.IconLoader;
/**
 * 12星座シンボルを選択するためのコンボボックス。
 */
public class ZodiacComboBox extends JComboBox {
  //星座シンボルImageIconを格納するList
  private static final List<ImageIcon>
    zodiacIconList = new ArrayList<ImageIcon>();
  //12星座分の0〜11の配列。初期化に仕様する。
  private static final Integer []
    zodiacNumbers = { 0,1,2,3,4,5,6,7,8,9,10,11 };
  //星座シンボルImageIcon(12個分)はstatic型で1度だけ作成する
  static {
    for(int i=0; i < Const.ZODIAC_NAMES.length; i++) {
      String symName = Const.ZODIAC_NAMES[i];
      ImageIcon imageIcon =
        IconLoader.getImageIcon("/resources/symbols/" + symName + ".gif");
      if (imageIcon != null) {
        imageIcon.setDescription(symName);
        zodiacIconList.add(imageIcon);
      }
    }    
  }
  /**
   * GIF画像で星座シンボルが表示されるコンボボックスを作成する。
   * getSelectedItem()で取得されるのはInteger型オブジェクトで、
   * 牡羊=0〜魚=11の値。
   */
  public ZodiacComboBox() {
    super( zodiacNumbers ); // ここで12星座分の0から11を登録
    ComboBoxRenderer renderer= new ComboBoxRenderer();
//    renderer.setPreferredSize(new Dimension(15, 16));
    renderer.setBorder( BorderFactory.createEmptyBorder(2,2,2,2));
    setRenderer(renderer);
    setMaximumRowCount(12);
  }
  
  private class ComboBoxRenderer extends JLabel
    implements ListCellRenderer {
    private Font uhOhFont;
    
    public ComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }
    public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      Integer i = (Integer)value;
      ImageIcon icon = zodiacIconList.get(i);
      String name = Const.ZODIAC_NAMES[i.intValue()];
      setIcon(icon);
      if (icon != null) {
        //setText(name); 画像に加え文字も必要ならテキストをセット
        //setFont(list.getFont());
      } else {
        setUhOhText(name,list.getFont());
      }
      return this;
    }
    //Set the font and text when no image was found.
    private void setUhOhText(String uhOhText, Font normalFont) {
      if (uhOhFont == null) { //lazily create this font
        uhOhFont = normalFont.deriveFont(Font.ITALIC);
      }
      setFont(uhOhFont);
      setText(uhOhText);
    }
  }
}
