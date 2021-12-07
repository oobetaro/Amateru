/*
 * HistoryEditPanel.java
 *
 * Created on 2006/10/03, 21:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import to.tetramorph.starbase.lib.Transit;

/**
 * ヒストリーを編集するパネル
 */
class HistoryEditPanel extends EventEditPanel {
  
  /**
   * オブジェクトを作成する。
   */
  protected HistoryEditPanel() {
    super("HISTORY");
    //中にずらずら書くとスコープの問題でわずらわしいのでメソッドは外に置く
    updateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateButtonActionPerformed();
      }
    });
    registButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        registButtonActionPerformed();
      }
    });
  }
  protected List<Transit> getHistory() {
    return getEventList();
  }
  protected void setHistory(List<Transit> list) {
    setEventList(list);
  }
  protected void insertHistory(Transit event) {
    insert(event);
  }
  //更新ボタンの処理
  private void updateButtonActionPerformed() {                                             
    int row = table.getSelectedRow();
    if( row < 0 ) return;
    Transit event = eventList.get(row);
    if(EventInputDialog.showDialog(this,event,"ヒストリーデータを編集")) {
      super.updateButtonActionPerformed(event);
    }
  }
 //登録ボタンの処理
 private void registButtonActionPerformed() {
    Transit event = EventInputDialog.showDialog(this,"ヒストリーデータの登録");
    if(event != null) super.registButtonActionPerformed(event);
 }
}
