/*
 * OccasionInputPanel.java
 *
 * Created on 2006/07/05, 1:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import javax.swing.JPanel;
import to.tetramorph.starbase.lib.Natal;

/**
 * チャートタイプ別に複数のデータ入力パネルを作るのに使う。
 * DataInputDialogで、Natal.getChartType()が、"NATAL","EVENT","COMPOSIT"に応じて入
 * 力Panelを切り替えながら使えるようにするため、それら入力フォームの部品やメソッド
 * を定義した抽象クラス。
 */
abstract class AbstractNatalInputPanel extends JPanel {
  
  /**
   * Creates a new instance of OccasionInputPanel
   */
  public AbstractNatalInputPanel() {
    super();
  }
  /**
   * 各フィールドの値からOccasionを作成または上書きして返す。
   */
  public abstract Natal getNatal();
  /**
   * データ入力で必須項目がすべて満たされている場合はtrueを返す。
   */
  public abstract boolean isCompletion();
  /**
   * 各入力フィールドに初期値をセットする。
   */
  public abstract void setNatal(Natal occ);
  /**
   * 最初の入力フィールドにフォーカスを渡す。
   */
  public abstract void setFocus();
}
