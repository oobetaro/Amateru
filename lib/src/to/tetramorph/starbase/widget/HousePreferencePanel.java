/*
 * HousePreferencePanel.java
 *
 * Created on 2006/09/15, 6:14
 */

package to.tetramorph.starbase.widget;

import javax.swing.JPanel;
import to.tetramorph.starbase.util.ChartConfig;

/**
 * ハウス分割法や、カスプが計算できない場合のハウス分割法を設定
 * するためのパネル。MainConfigDialog内で使用している同名のパネルより汎用性を持
 * たせている。(同名のものが二つあることに注意）
 * 最終的にはMainConfigDialogで使っているものと置換する予定。
 *<pre>
 * CuspUnknownHouseSystem = 1 or 2
 * 　　1ならソーラー、2ならソーラーサイン
 * RegistPlace = true/false
 *     trueなら「ただし時間が未登録だが場所は指定されているときは、
 * 　　デフォルトタイムで計算しデフォルトのハウス分割法を使用
 * HouseSystemIndex = 0 .. 7
 * 　　ハウス分割法コンボボックスの選択中インデックス番号。
 * PrioritizeSolar = true/false
 *   時刻や緯度・経度が登録されていないときソーラーハウス(1)か、ソーラーサイン
 * を使用する。
 *</pre>
 * @author 大澤義鷹
 */
public class HousePreferencePanel extends JPanel {
    
    /** オブジェクトを作成する。 */
    public HousePreferencePanel() {
        initComponents();
        init();
    }
    private void init() {
        solarButtonGroup.add(solarRadioButton1);
        solarButtonGroup.add(solarRadioButton2);
        cuspButtonGroup.add(unknownCuspRadioButton1);
        cuspButtonGroup.add(unknownCuspRadioButton2);
    }
    /**
     * 選択されているハウス分割法のコードを返す。
     */
    public char getHouseSystemCode() {
        return houseSystemComboBox.getSelectedCode();
    }
    /**
     * ハウス分割法のコンボボックスを指定コードで選択する。
     */
    public void setHouseSystemCode( char hsc ) {
        houseSystemComboBox.setSelectedCode( hsc );
    }
    /**
     * 選択されているハウスシステムを番号文字列で返す。この番号を元に、
     * 対応するハウスシステム名は、Const.HOUSE_SYSTEM_NAMES[]で得られる。
     * スイスエフェメリスに与えるハウスシステムコードはConst.HOUSE_SYSTEM_CODES[]で得られる。
     */
    public String getHouseSystemIndex() {
        return "" + houseSystemComboBox.getSelectedIndex();
    }
    /**
     * ハウスシステム番号をセットする。これによりコンボボックスのハウスメソッドが
     * 選択される。
     */
    public void setHouseSystemIndex( String intValue ) {
        houseSystemComboBox.setSelectedIndex( Integer.parseInt( intValue ) );
    }
    /**
     * 時間や場所が不明でカスプ計算ができない場合のハウス分割法で、ソーラーまたは
     * ソーラーサインを使用するが選択されている場合は"true"、
     * 「デフォルトの地方時と観測地とハウス分割法で計算する。」が選択されている
     * 場合は"false"を返す。
     */
    public String getPrioritizeSolar() {
        return "" + solarRadioButton1.isSelected();
    }
    /**
     * 「ソーラーまたはソーラーサインを使用する」を選択する場合は"true"を、
     * 「デフォルトの地方時と観測地とハウス分割法で計算する。」を選択する場合は"false"
     * をセットする。
     */
    public void setPrioritizeSolar( String booleanValue ) {
        if ( Boolean.parseBoolean( booleanValue ) ) 
            solarRadioButton1.doClick();
        else solarRadioButton2.doClick();
    }
    /**
     * カスプが計算できない場合のハウス分割法(ソーラー="1",ソーラーサイン="2")を返す。
     */
    public String getCuspUnkownHouseSystem() {
        return cuspButtonGroup.getSelection().getActionCommand();
    }
    /**
     * カスプ計算ができない場合のハウス分割法(ソーラー="1",ソーラーサイン="2")をセットする。
     * 「ソーラーまたはソーラーサインを使用する」のラジオボタンは自動的に選択状態にする。
     * ゆえにこのメソッドでどちらかを選択すれば、setPrioritizeSolar()で明示的に"true"
     * を設定する必要はない。"false"を設定する局面はあるあもしれない。
     */
    public void setCuspUnkownHouseSystem(String intValue) {
        int select = Integer.parseInt(intValue);
        if ( select == 1 ) unknownCuspRadioButton1.doClick();
        else unknownCuspRadioButton2.doClick();
        solarRadioButton1.doClick();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;

        solarButtonGroup = new javax.swing.ButtonGroup();
        cuspButtonGroup = new javax.swing.ButtonGroup();
        borderPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        unknownCuspRadioButton1 = new javax.swing.JRadioButton();
        unknownCuspRadioButton2 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        houseSystemComboBox = new to.tetramorph.starbase.widget.HouseSystemComboBox();
        solarRadioButton1 = new javax.swing.JRadioButton();
        solarRadioButton2 = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridLayout(1, 0));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 16, 1, 16));
        borderPanel.setLayout(new java.awt.GridBagLayout());

        borderPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 16, 16, 1));
        jLabel3.setText("\u6642\u9593\u3084\u5834\u6240\u304c\u4e0d\u660e\u3067\u30ab\u30b9\u30d7\u8a08\u7b97\u304c\u3067\u304d\u306a\u3044\u5834\u5408\u306e\u30cf\u30a6\u30b9\u5206\u5272\u65b9\u6cd5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        borderPanel.add(jLabel3, gridBagConstraints);

        unknownCuspRadioButton1.setSelected(true);
        unknownCuspRadioButton1.setText("\u592a\u967d\u30b5\u30a4\u30f3\u306e0\u5ea6\u3092\u4e00\u5ba4\u30ab\u30b9\u30d7\u3068\u3057\u3066\u30a4\u30b3\u30fc\u30eb\u5206\u5272 ( \u30bd\u30fc\u30e9\u30fc\u30b5\u30a4\u30f3 )");
        unknownCuspRadioButton1.setActionCommand("1");
        unknownCuspRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        unknownCuspRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 3, 0);
        borderPanel.add(unknownCuspRadioButton1, gridBagConstraints);

        unknownCuspRadioButton2.setText("\u592a\u967d\u306e\u4f4d\u7f6e\u3092\u4e00\u5ba4\u30ab\u30b9\u30d7\u3068\u3057\u3066\u30a4\u30b3\u30fc\u30eb\u5206\u5272 ( \u30bd\u30fc\u30e9\u30fc )");
        unknownCuspRadioButton2.setActionCommand("2");
        unknownCuspRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        unknownCuspRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 3, 0);
        borderPanel.add(unknownCuspRadioButton2, gridBagConstraints);

        jLabel4.setText("\u3000\u3000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        borderPanel.add(jLabel4, gridBagConstraints);

        jLabel5.setText("\u30c7\u30d5\u30a9\u30eb\u30c8\u306e\u30cf\u30a6\u30b9\u5206\u5272\u6cd5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        borderPanel.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        borderPanel.add(houseSystemComboBox, gridBagConstraints);

        solarRadioButton1.setSelected(true);
        solarRadioButton1.setText("\uff83\uff9e\uff8c\uff6b\uff99\uff84\u306e\u5730\u65b9\u6642\u3068\u89b3\u6e2c\u5730\u3067\u8a08\u7b97\u3057\u30bd\u30fc\u30e9\u30fc\u307e\u305f\u306f\u30bd\u30fc\u30e9\u30fc\u30b5\u30a4\u30f3\u3092\u4f7f\u7528\u3059\u308b\u3002");
        solarRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        solarRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        solarRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solarRadioButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        borderPanel.add(solarRadioButton1, gridBagConstraints);

        solarRadioButton2.setText("\u30c7\u30d5\u30a9\u30eb\u30c8\u306e\u5730\u65b9\u6642\u3068\u89b3\u6e2c\u5730\u3068\u30cf\u30a6\u30b9\u5206\u5272\u6cd5\u3067\u8a08\u7b97\u3059\u308b\u3002");
        solarRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        solarRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        solarRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                solarRadioButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        borderPanel.add(solarRadioButton2, gridBagConstraints);

        add(borderPanel);

    }// </editor-fold>//GEN-END:initComponents
  
  private void solarRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_solarRadioButton2ActionPerformed
      unknownCuspRadioButton1.setEnabled( false );
      unknownCuspRadioButton2.setEnabled( false );
  }//GEN-LAST:event_solarRadioButton2ActionPerformed
  
  private void solarRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_solarRadioButton1ActionPerformed
      unknownCuspRadioButton1.setEnabled( true );
      unknownCuspRadioButton2.setEnabled( true );
  }//GEN-LAST:event_solarRadioButton1ActionPerformed
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel borderPanel;
    private javax.swing.ButtonGroup cuspButtonGroup;
    private to.tetramorph.starbase.widget.HouseSystemComboBox houseSystemComboBox;
    private javax.swing.ButtonGroup solarButtonGroup;
    private javax.swing.JRadioButton solarRadioButton1;
    private javax.swing.JRadioButton solarRadioButton2;
    private javax.swing.JRadioButton unknownCuspRadioButton1;
    private javax.swing.JRadioButton unknownCuspRadioButton2;
    // End of variables declaration//GEN-END:variables
  
}