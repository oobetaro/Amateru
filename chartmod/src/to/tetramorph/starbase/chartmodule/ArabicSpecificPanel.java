/*
 * ReportSpecificPanel.java
 *
 * Created on 2008/10/25, 20:45
 */

package to.tetramorph.starbase.chartmodule;

import java.io.InputStream;
import javax.swing.JRadioButton;
import to.tetramorph.starbase.chartparts.ArabicParts;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.Preference;

/**
 * レポートプラグインの仕様設定パネル
 * @author  大澤義鷹
 */
public class ArabicSpecificPanel extends CustomizePanel {
    //デフォルトのアスペクト設定 ( ｱｽﾍﾟｸﾄID,ﾀｲﾄｵｰﾌﾞ,ﾙｰｽﾞｵｰﾌﾞ )
    private static final String defaultAspects =
        "0,3,6," +  //CONJUNCTION
        "1,3,6," +   //SEXTILE
        "2,3,6," +   //SQUARE
        "3,3,6," +   //TRINE
        "4,3,6"  ;   //OPPOSITION
        //"5,3,6";    //QUINCUNX

    /** Creates new form ReportSpecificPanel */
    public ArabicSpecificPanel() {
        initComponents();
        aspectTypePanel.setSelected( defaultAspects );
        resetSourceButtonActionPerformed(null);
    }


    @Override
    public Preference getPreference( Preference pref ) {
        String sys = new Boolean( rulerRadioButton1.isSelected() ).toString();
        pref.setProperty( "isModern", sys );
        //pref.setProperty( "NatalBodys", basicPlanetsSelectionPanel.getSelected() );
        pref.setProperty( "NatalAspectBodys", planetsSelectionPanel2.getSelected() );
        pref.setProperty( "AspectTypes", aspectTypePanel.getSelected() );
        pref.setProperty( "HouseSystemCode", "" +housePreferencePanel1.getHouseSystemCode());
        pref.setProperty( "CuspUnknownHouseSystem", housePreferencePanel1.getCuspUnkownHouseSystem() );
        pref.setProperty( "PrioritizeSolar", housePreferencePanel1.getPrioritizeSolar() );
        pref.setProperty("ProgressCode","" + progressComboBox.getSelectedProgressCode());
        pref.setProperty( "ArabicPartsSource", srcTextPane.getText().trim() );
        pref.setProperty("NPTGroup", ""+getGroup());
        pref.setProperty("SymbolDisplayMode", "" + getDisplayMode());
        pref.setProperty("ShowVariableTable", "" + isShowVariableTable());
//        pref.setProperty("IntervalHeaader","" + isIntervalHeadder());
        return pref;
    }

    @Override
    public void setPreference( Preference pref ) {
        boolean b = pref.getProperty( "isModern","true" ).equals("true");
        rulerButtonGroup.setSelected(rulerRadioButton1.getModel(), b);
        //basicPlanetsSelectionPanel.setModernSystem( b );
        //basicPlanetsSelectionPanel.setSelected( pref.getProperty("NatalBodys") );
        planetsSelectionPanel2.setSelected( pref.getProperty("NatalAspectBodys") );
        aspectTypePanel.setSelected( pref.getProperty( "AspectTypes" ) );
        housePreferencePanel1.setHouseSystemCode( pref.getProperty("HouseSystemCode").charAt(0) );
        housePreferencePanel1.setCuspUnkownHouseSystem( pref.getProperty("CuspUnknownHouseSystem") );
        housePreferencePanel1.setPrioritizeSolar( pref.getProperty("PrioritizeSolar") );
        progressComboBox.setSelectedProgressCode(pref.getProperty("ProgressCode","P").charAt(0));
        srcTextPane.setText(pref.getProperty("ArabicPartsSource"));
        setGroup(Integer.parseInt(pref.getProperty("NPTGroup")));
        setDisplayMode(Integer.parseInt(pref.getProperty("SymbolDisplayMode")));
        setShowVariableTable(Boolean.parseBoolean(pref.getProperty("ShowVariableTable")));
//        setIntervalHeadder(Boolean.parseBoolean(pref.getProperty("IntervalHeadder")));
    }

    @Override
    public boolean isCorrect( String[] errmsg ) {
        AspectType [] atype = aspectTypePanel.getAspectTypes();
        for ( int i = 0; i < atype.length; i++ ) {
            if ( ( atype[i].tightOrb <= 0 || atype[i].looseOrb <= 0 ) ||
                 ( atype[i].tightOrb >= atype[i].looseOrb) ) {
                errmsg[0] = "アスペクト\"" + Const.ASPECT_NAMES[i]
                          + "\"のオーブの設定値が未入力か異常です。";
                return false;
            }
        }
        return true;
    }

//    /**
//     * 選択されているネイタル天体IDsを返す。
//     */
//    public int [] getNatalBodyIDs() {
//        return basicPlanetsSelectionPanel.getSelectedBodyIDs();
//    }

    /**
     * ネイタルでアスペクトを検出する天体のIDsを返す。
     */
    public int [] getAspectNatalBodyIDs() {
        return planetsSelectionPanel2.getSelectedBodyIDs();
    }

    /**
     * アスペクトタイプの配列を返す。
     */
    public AspectType [] getAspectTypes() {
        return aspectTypePanel.getAspectTypes();
    }

    /**
     * モダン十惑星式が選択されているときはtrueを返す。
     */
    public boolean isModernSystem() {
        return rulerRadioButton1.isSelected();
    }

    /**
     * NPTの選択グループを返す。N=0, P=1, T=2。
     */
    public int getGroup() {
        String cmd = groupButtonGroup.getSelection().getActionCommand();
        return Integer.parseInt(cmd);
    }
    /**
     * NPTの選択グループをセットする。
     * @param group 0=N, 1=P, 2=T。
     */
    public void setGroup(int group) {
        JRadioButton [] buttons = new JRadioButton[] { groupRadioButton1,groupRadioButton2,groupRadioButton3 };
        buttons[ group ].setSelected(true);
    }
    /**
     * カスプ計算できないときはハウス分割法をセットする。1か0を文字列でセット。
     */
    public void setCuspUnkownHouseSystem( String value ) {
        housePreferencePanel1.setCuspUnkownHouseSystem( value );
    }

    /**
     * カスプが計算できないときのハウス分割法(ソーラーかソーラーサイン)コード
     * を帰す。
     */
    public char getCuspUnknownHouseSystem() {
        return housePreferencePanel1.getCuspUnkownHouseSystem().charAt(0);
    }

    /**
     * ハウス分割法をセットする
     */
    public void setHouseSystemCode( char code ) {
        housePreferencePanel1.setHouseSystemCode( code );
    }

    /**
     * 選択されている進行法のコード(NPTChartに有効な)を返す。
     */
    public char getProgressCode() {
        return progressComboBox.getSelectedProgressCode();
    }

    /**
     * 選択されているハウス分割法のスイスエフェメリス用のコードを返す。
     */
    public char getHouseSystemCode() {
        return housePreferencePanel1.getHouseSystemCode();
    }
    public boolean getPrioritizeSolar() {
        return housePreferencePanel1.getPrioritizeSolar().equals("true");
    }
    /**
     * アラビックパーツの計算式の設定テキストを返す。
     */
    public String getArabicPartsSource() {
        return srcTextPane.getText();
    }
    /**
     * シンボルを記号／テキストどちらで表示するかを返す。
     * @return 記号なら0、テキストなら1。
     */
    public int getDisplayMode() {
        String cmd = dispButtonGroup.getSelection().getActionCommand();
        return Integer.parseInt(cmd);
    }
    /**
     * シンボルの表示方式をセットする。
     * @param mode 記号なら0、テキストなら1。
     */
    public void setDisplayMode(int mode) {
        JRadioButton [] buttons =
                new JRadioButton[] { dispRadioButton1,dispRadioButton2 };
        buttons[ mode ].setSelected(true);
    }
    //変数テーブルを表示／非表示フラグ
    public boolean isShowVariableTable() {
        return variableCheckBox.isSelected();
    }

    //変数テーブルを表示／非表示フラグのセット
    public void setShowVariableTable(boolean b) {
        variableCheckBox.setSelected(b);
    }
//    //10行間隔でテーブルヘッダーを入れる／入れないのフラグ
//    public boolean isIntervalHeadder() {
//        return headderCheckBox.isSelected();
//    }
//
//    //10行間隔でテーブルヘッダーを入れる／入れないのフラグを設定
//    public void setIntervalHeadder(boolean b) {
//        headderCheckBox.setSelected(b);
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rulerButtonGroup = new javax.swing.ButtonGroup();
        groupButtonGroup = new javax.swing.ButtonGroup();
        dispButtonGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        planetsSelectionPanel2 = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        jPanel4 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        rulerRadioButton1 = new javax.swing.JRadioButton();
        rulerRadioButton2 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        groupRadioButton1 = new javax.swing.JRadioButton();
        groupRadioButton2 = new javax.swing.JRadioButton();
        groupRadioButton3 = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        aspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        housePreferencePanel1 = new to.tetramorph.starbase.widget.HousePreferencePanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        progressComboBox = new to.tetramorph.starbase.chartparts.ProgressComboBox();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        srcTextPane = new javax.swing.JTextPane();
        jPanel6 = new javax.swing.JPanel();
        resetSourceButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        dispRadioButton1 = new javax.swing.JRadioButton();
        dispRadioButton2 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        variableCheckBox = new javax.swing.JCheckBox();

        rulerButtonGroup.add(rulerRadioButton1);
        rulerButtonGroup.add(rulerRadioButton2);

        groupButtonGroup.add( groupRadioButton1 );
        groupButtonGroup.add( groupRadioButton2 );
        groupButtonGroup.add( groupRadioButton3 );

        dispButtonGroup.add( dispRadioButton1 );
        dispButtonGroup.add( dispRadioButton2 );

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        planetsSelectionPanel2.setTitle("アスペクト検出天体");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 0);
        jPanel1.add(planetsSelectionPanel2, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("ハウスルーラー決定法");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel4.add(jLabel2, gridBagConstraints);

        rulerRadioButton1.setSelected(true);
        rulerRadioButton1.setText("モダン十惑星式");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel4.add(rulerRadioButton1, gridBagConstraints);

        rulerRadioButton2.setText("古典七惑星式");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel4.add(rulerRadioButton2, gridBagConstraints);

        jLabel3.setText("表示チャート");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 4, 0);
        jPanel4.add(jLabel3, gridBagConstraints);

        groupRadioButton1.setSelected(true);
        groupRadioButton1.setText("ネイタル");
        groupRadioButton1.setActionCommand("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        jPanel4.add(groupRadioButton1, gridBagConstraints);

        groupRadioButton2.setText("プログレス");
        groupRadioButton2.setActionCommand("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        jPanel4.add(groupRadioButton2, gridBagConstraints);

        groupRadioButton3.setText("トランジット");
        groupRadioButton3.setActionCommand("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        jPanel4.add(groupRadioButton3, gridBagConstraints);

        jPanel1.add(jPanel4, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab("天体", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        aspectTypePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel2.add(aspectTypePanel, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("アスペクト", jPanel2);
        jTabbedPane1.addTab("ハウス計算法", housePreferencePanel1);

        jPanel3.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel3.add(progressComboBox, gridBagConstraints);

        jLabel1.setText("進行法");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel3.add(jLabel1, gridBagConstraints);

        jTabbedPane1.addTab("進行法", jPanel3);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setRequestFocusEnabled(false);

        srcTextPane.setFont(new java.awt.Font("ＭＳ ゴシック", 0, 14));
        srcTextPane.setPreferredSize(getPreferredSize());
        jScrollPane1.setViewportView(srcTextPane);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        resetSourceButton.setText("初期値に戻す");
        resetSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetSourceButtonActionPerformed(evt);
            }
        });
        jPanel6.add(resetSourceButton);

        jPanel5.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab("ソース", jPanel5);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        dispRadioButton1.setSelected(true);
        dispRadioButton1.setText("記号表示");
        dispRadioButton1.setActionCommand("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel7.add(dispRadioButton1, gridBagConstraints);

        dispRadioButton2.setText("テキスト表示");
        dispRadioButton2.setActionCommand("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel7.add(dispRadioButton2, gridBagConstraints);

        jLabel4.setText("表示方式");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel7.add(jLabel4, gridBagConstraints);

        jLabel5.setText("その他");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel7.add(jLabel5, gridBagConstraints);

        variableCheckBox.setSelected(true);
        variableCheckBox.setText("変数テーブルを表示");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel7.add(variableCheckBox, gridBagConstraints);

        jTabbedPane1.addTab("表示", jPanel7);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void resetSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetSourceButtonActionPerformed
        InputStream is = ArabicParts.class.getResourceAsStream("/resources/arabic_parts.txt");
        srcTextPane.setText( FileTools.loadText(is, "sjis").toString() );
    }//GEN-LAST:event_resetSourceButtonActionPerformed

//    public static void main( String args [] ) {
//        Preference p = new Preference();
//        InputStream is = ArabicParts.class.getResourceAsStream("/resources/arabic_parts.txt");
//        String str = FileTools.loadText(is, "sjis").toString();
//        p.setProperty("hoge", str);
//        System.out.println(p.getProperty("hoge"));
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private to.tetramorph.starbase.widget.AspectTypePanel aspectTypePanel;
    private javax.swing.ButtonGroup dispButtonGroup;
    private javax.swing.JRadioButton dispRadioButton1;
    private javax.swing.JRadioButton dispRadioButton2;
    private javax.swing.ButtonGroup groupButtonGroup;
    private javax.swing.JRadioButton groupRadioButton1;
    private javax.swing.JRadioButton groupRadioButton2;
    private javax.swing.JRadioButton groupRadioButton3;
    private to.tetramorph.starbase.widget.HousePreferencePanel housePreferencePanel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel planetsSelectionPanel2;
    private to.tetramorph.starbase.chartparts.ProgressComboBox progressComboBox;
    private javax.swing.JButton resetSourceButton;
    private javax.swing.ButtonGroup rulerButtonGroup;
    private javax.swing.JRadioButton rulerRadioButton1;
    private javax.swing.JRadioButton rulerRadioButton2;
    private javax.swing.JTextPane srcTextPane;
    private javax.swing.JCheckBox variableCheckBox;
    // End of variables declaration//GEN-END:variables


}
