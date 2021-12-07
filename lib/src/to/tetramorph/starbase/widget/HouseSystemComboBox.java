/*
 * HouseSystemComboBox.java
 *
 * Created on 2007/07/12, 23:39
 *
 */

package to.tetramorph.starbase.widget;

import javax.swing.JComboBox;
import to.tetramorph.starbase.lib.Const;

/**
 * ハウス分割法を選択するためのコンボボックス。
 * @author 大澤義鷹
 */
public class HouseSystemComboBox extends JComboBox {
  
  /**
   *  HouseSystemComboBox オブジェクトを作成する
   */
  public HouseSystemComboBox() {
    init();
  }
  private void init() {
    removeAllItems();
    for(int i=0; i < Const.HOUSE_SYSTEM_NAMES.length; i++)
      addItem(Const.HOUSE_SYSTEM_NAMES[i]);
  }
  /**
   * スイスエフェメリス用のハウス分割法の指定コードを返す。
   * ただしソーラーとソーラーサインはスイスエフェメリスには無い。
   * ソーラーは(int)'1'、ソーラーサインは(int)'2'が返る。
   */
  public char getSelectedCode() {
    return (char)Const.HOUSE_SYSTEM_CODES[getSelectedIndex()];
  }
  /**
   * スイスエフェメリス用のハウス分割法の指定コードによって、該当するアイテムを
   * 選択する。
   */
  public void setSelectedCode(char hsc) {
    for(int i=0; i < Const.HOUSE_SYSTEM_CODES.length; i++)
      if(Const.HOUSE_SYSTEM_CODES[i] == (int)hsc) {
        setSelectedIndex(i);
        break;
      }
  }
}
