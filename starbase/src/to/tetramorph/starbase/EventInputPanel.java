/*
 * NatalFormPanel.java
 *
 * Created on 2006/07/05, 1:18
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.formatter.LimitedDocument;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.lib.Natal;

/**
 * DataInputDialogにはめこんでつかう部品で、これはネイタルチャートのデータ入力用。
 * 他にもコンポジットやイベントチャート用が用意される予定。
 * かならず抽象クラスのDataTypePanelをextendsしてgetNatalとsetNatalを
 * オーバーライドしていなければならない。
 */
class EventInputPanel extends AbstractNatalInputPanel {
  private Natal natal;
  /** 
   * オブジェクトを作成する。
   */
  public EventInputPanel() {
    initComponents();
    init();
  }
  public void setFocus() {
    nameTextField.requestFocusInWindow();
  }
  /**
   * コンポジット用の入力パネルにデザインを変更する。といっても「出来事」と「よみがな」
   * が「名前」と「なまえ」に変更されるだけ。なおこのメソッドでセットしたものを
   * もとに戻す機能は用意していない。
   */
  protected void setCompositDesign() {
    nameLabel.setText("名前");
    kanaLabel.setText("なまえ");
  }
  //名前、なまえ、メモのTextFieldに文字数制限をかける。
  private void init() {
    nameTextField.setDocument(new LimitedDocument(28));
    kanaTextField.setDocument(new LimitedDocument(28));
    memoTextField.setDocument(new LimitedDocument(28));
  }
  /**
   * このフォームに入力されたデータをNatalオブジェクトとして返す。
   * なお入力フォームに存在しないパラメター(ヒストリーやノート)は、未登録のまま
   * で返される。またNatalのChartTypeかならずEVENTにセットされている。
   * Jobは"",GenderはNONEにセットされる。
   * @return　Natalオブジェクト
   */
  public Natal getNatal() {
    if(natal == null) natal = new Natal();
    natal.setChartType(Natal.EVENT);
    natal.setName(nameTextField.getText().trim());
    natal.setKana(kanaTextField.getText().trim());
    natal.setMemo(memoTextField.getText().trim());
    natal.setJob("");
    natal.setGender(Natal.NONE);
    timePlacePanel.getTimePlace(natal);
    return natal;
  }
  /**
   * 「出来事名、よみがな、メモ」と、「日付、時刻、地名、緯度、経度、
   * タイムゾーン」を各入力フィールドにセットする。
   */
  public void setNatal(Natal natal) {
    this.natal = natal;
    nameTextField.setText(natal.getName());
    kanaTextField.setText(natal.getKana());
    memoTextField.setText(natal.getMemo());
    timePlacePanel.setTimePlace(natal);
  }
  /**
   * このDataTypePanelは少なくとも「名前」「日付」が満たされていれば登録しても
   * 良いとみなしtrueを返す。満たされていないときはエラーメッセージをダイアログ
   * で表示したのちfalseを返す。
   */
  public boolean isCompletion() {
    String name = nameTextField.getText();
    if(name == null) name = "";
    if(timePlacePanel.getDate() == null || name.equals("")) {
      JOptionPane.showMessageDialog(this,"出来事名と日付は両方とも入力が必要です。"
        ,"イベントデータの入力",JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel memoLabel;

    timePlacePanel = new to.tetramorph.starbase.TimePlacePanel();
    eventPanel = new javax.swing.JPanel();
    nameLabel = new javax.swing.JLabel();
    nameTextField = new javax.swing.JTextField();
    kanaLabel = new javax.swing.JLabel();
    kanaTextField = new javax.swing.JTextField();
    memoLabel = new javax.swing.JLabel();
    memoTextField = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 1));
    setName("\u30a4\u30d9\u30f3\u30c8");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    add(timePlacePanel, gridBagConstraints);

    eventPanel.setLayout(new java.awt.GridBagLayout());

    nameLabel.setText("\u51fa\u6765\u4e8b\u540d");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    eventPanel.add(nameLabel, gridBagConstraints);

    nameTextField.setColumns(12);
    eventPanel.add(nameTextField, new java.awt.GridBagConstraints());

    kanaLabel.setText("\u3088\u307f\u304c\u306a");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    eventPanel.add(kanaLabel, gridBagConstraints);

    kanaTextField.setColumns(12);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    eventPanel.add(kanaTextField, gridBagConstraints);

    memoLabel.setText("\u30e1\u30e2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    eventPanel.add(memoLabel, gridBagConstraints);

    memoTextField.setColumns(12);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    eventPanel.add(memoTextField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    add(eventPanel, gridBagConstraints);

  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel eventPanel;
  private javax.swing.JLabel kanaLabel;
  private javax.swing.JTextField kanaTextField;
  private javax.swing.JTextField memoTextField;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private to.tetramorph.starbase.TimePlacePanel timePlacePanel;
  // End of variables declaration//GEN-END:variables
  
}