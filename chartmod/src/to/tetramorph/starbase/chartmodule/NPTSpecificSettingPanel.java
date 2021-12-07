/*
 * NatalCalcConfPanel.java
 *
 * Created on 2007/04/01, 8:01
 */

package to.tetramorph.starbase.chartmodule;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import to.tetramorph.starbase.chartparts.CombinationSelectorPanel;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;
import to.tetramorph.starbase.lib.Const;
/**
 * NPT三重円プラグイン用の計算設定パネル。ただし相性円プラグインの設定パネルも
 * かねている。NPTであれ、相性円であれ、設定が異なるのは「アスペクト表示」タブの
 * 中にある、アスペクト組合せ設定パネルのみで、他の部分は共通。
 * NPT三重円の場合「アスペクト表示タブ」の中に、NPT各円の組合せごとのアスペクト設定
 * をするパネル(AspectCombinationPanel)をコンストラクタで渡してはめ込む方式にした。
 * 相性円にもそれ独自の組合せ設定パネルがあり、同様にこのクラスのnewの際にそれを渡す
 * 仕組み。
 *
 * はじめは相性円用の設定パネルのことはまったく考慮しておらず、全てのプロパティ取得
 * メソッドをこのクラスに用意していた。そのほうがまとまりがあって良いともいえるの
 * だが、相性円用の部品として流用することができなくなる。ここはトレードオフで、
 * 設定情報の取得は二つのインスタンスに問い合わせる事になるが、流用できる方を選択
 * した。
 * @author 大澤義鷹
 */
public class NPTSpecificSettingPanel extends CustomizePanel {

    //デフォルトのアスペクト設定 ( ｱｽﾍﾟｸﾄID,ﾀｲﾄｵｰﾌﾞ,ﾙｰｽﾞｵｰﾌﾞ )
    private static final String defaultAspects =
        "0,3,6," +  //CONJUNCTION
        "1,3,6," +   //SEXTILE
        "2,3,6," +   //SQUARE
        "3,3,6," +   //TRINE
        "4,3,6," +   //OPPOSITION
        "5,3,6";    //QUINCUNX
    /**
     * アスペクト・チャート円の組合せパネルを指定してオブジェクトを作成する。
     */
    public NPTSpecificSettingPanel( CombinationSelectorPanel combi ) {
        initComponents();
        if ( combi != null ) {
            combi.setCustomizePanel( this );
            aspectDisplaySettingPanel.setCustomizePanel(this);
            aspectDisplaySettingPanel.setCombinationSelectorPanel( combi );
        }
        // 使用天体パネルとアスペクト検出天体パネルを同期させる設定
        natalPlanetsSelectionPanel.setSlavePanel( natalAspectPlanetsSelectionPanel );
        progressPlanetsSelectionPanel.setSlavePanel( progressAspectPlanetsSelectionPanel );
        transitPlanetsSelectionPanel.setSlavePanel( transitAspectPlanetsSelectionPanel );
        // ｱｽﾍﾟｸﾄのﾃﾞﾌｫﾙﾄ値を設定する。
        nnAspectTypePanel.setSelected( defaultAspects );
        ppAspectTypePanel.setSelected( defaultAspects );
        ttAspectTypePanel.setSelected( defaultAspects );
        ntAspectTypePanel.setSelected( defaultAspects );
        npAspectTypePanel.setSelected( defaultAspects );
        ptAspectTypePanel.setSelected( defaultAspects );
//        this.getImageSizeMenu();
    }
    /**
     * オブジェクトを作成する。( GUIエディタの都合上必要なコンストラクタ )
     */
    public NPTSpecificSettingPanel() {
        this( null );
    }

    @Override
    public Preference getPreference(Preference pref) {
        pref.setProperty("NatalBodys",natalPlanetsSelectionPanel.getSelected());
        pref.setProperty("ProgressBodys",progressPlanetsSelectionPanel.getSelected());
        pref.setProperty("TransitBodys",transitPlanetsSelectionPanel.getSelected());
        pref.setProperty("NatalAspectBodys",natalAspectPlanetsSelectionPanel.getSelected());
        pref.setProperty("ProgressAspectBodys",progressAspectPlanetsSelectionPanel.getSelected());
        pref.setProperty("TransitAspectBodys",transitAspectPlanetsSelectionPanel.getSelected());
        pref.setProperty("nnAspectTypes",nnAspectTypePanel.getSelected()); //元は"UseAspects"というキーだった。
        pref.setProperty("ppAspectTypes",ppAspectTypePanel.getSelected());
        pref.setProperty("ttAspectTypes",ttAspectTypePanel.getSelected());
        pref.setProperty("ntAspectTypes",ntAspectTypePanel.getSelected());
        pref.setProperty("npAspectTypes",npAspectTypePanel.getSelected());
        pref.setProperty("ptAspectTypes",ptAspectTypePanel.getSelected());
        pref.setProperty("HouseSystemCode","" + housePreferencePanel1.getHouseSystemCode());
        //以下二行を追加(アラビックパーツを作るときに無い事に気づいた
        pref.setProperty( "CuspUnknownHouseSystem", housePreferencePanel1.getCuspUnkownHouseSystem() );
        pref.setProperty( "PrioritizeSolar", housePreferencePanel1.getPrioritizeSolar() );
        pref.setProperty("ProgressCode","" + progressComboBox.getSelectedProgressCode());
        pref.setProperty("AspectCategorys",aspectDisplaySettingPanel.getAspectCategorysString());
        pref.setBoolean("AspectDirectLineMode",aspectDisplaySettingPanel.getDirectLineMode());
        CombinationSelectorPanel csp = aspectDisplaySettingPanel.getCombinationSelectorPanel();
        if ( csp != null ) {
            csp.getPreference( pref );
        }
        return pref;
    }

    @Override
    public void setPreference(Preference pref) {
        natalPlanetsSelectionPanel.setSelected(pref.getProperty("NatalBodys"));
        progressPlanetsSelectionPanel.setSelected(pref.getProperty("ProgressBodys"));
        transitPlanetsSelectionPanel.setSelected(pref.getProperty("TransitBodys"));
        natalAspectPlanetsSelectionPanel.setSelected(pref.getProperty("NatalAspectBodys"));
        progressAspectPlanetsSelectionPanel.setSelected(pref.getProperty("ProgressAspectBodys"));
        transitAspectPlanetsSelectionPanel.setSelected(pref.getProperty("TransitAspectBodys"));
        nnAspectTypePanel.setSelected(pref.getProperty("nnAspectTypes"));
        ppAspectTypePanel.setSelected(pref.getProperty("ppAspectTypes"));
        ttAspectTypePanel.setSelected(pref.getProperty("ttAspectTypes"));
        ntAspectTypePanel.setSelected(pref.getProperty("ntAspectTypes"));
        npAspectTypePanel.setSelected(pref.getProperty("npAspectTypes"));
        ptAspectTypePanel.setSelected(pref.getProperty("ptAspectTypes"));
        housePreferencePanel1.setHouseSystemCode(pref.getProperty("HouseSystemCode").charAt(0));
        //以下二行を追加(アラビックパーツを作るときに無い事に気づいた
        housePreferencePanel1.setCuspUnkownHouseSystem( pref.getProperty("CuspUnknownHouseSystem") );
        housePreferencePanel1.setPrioritizeSolar( pref.getProperty("PrioritizeSolar") );
        progressComboBox.setSelectedProgressCode(pref.getProperty("ProgressCode","P").charAt(0));
        aspectDisplaySettingPanel.setAspectCategorys(pref.getProperty("AspectCategorys"));
        aspectDisplaySettingPanel.setDirectLineMode(pref.getBoolean("AspectDirectLineMode"));
        CombinationSelectorPanel csp = aspectDisplaySettingPanel.getCombinationSelectorPanel();
        if ( csp != null ) {
            csp.setPreference( pref );
        }
    }

    /**
     * このパネルの設定が正しく行われている状態ならtrue。フィールドに異常な値が
     * 入力されている場合などはfalseを返す。
     * @param errmsg 要素数1の文字配列を与える。戻り値がfalseの場合エラーメッセージが参照書き込みされる。
     */
    @Override
    public boolean isCorrect(String [] errmsg) {
        AspectType [] atype = nnAspectTypePanel.getAspectTypes();
        for(int i=0; i<atype.length; i++) {
            if((atype[i].tightOrb <= 0 || atype[i].looseOrb <= 0) ||
                (atype[i].tightOrb >= atype[i].looseOrb) ) {
                errmsg[0] = "アスペクト\"" + Const.ASPECT_NAMES[i] + "\"のオーブの設定値が未入力か異常です。";
                return false;
            }
        }
        return true;
    }

    /**
     * 選択されているネイタル天体IDsを返す。
     */
    public int [] getNatalBodyIDs() {
        return natalPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * 選択されているプログレス天体IDsを返す。
     */
    public int [] getProgressBodyIDs() {
        return progressPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * 選択されているトランジット天体IDsを返す。
     */
    public int [] getTransitBodyIDs() {
        return transitPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * 選択されている天体IDsを返す。
     * get(Natal|Progress|Transit)BodyIDs()と機能は同じ。引数でNPTの選択ができる
     * ようにしたメソッド。
     * @param npt 0=N,1=P,2=T
     */
    public int [] getBodyIDs(int npt) {
        switch(npt) {
            case 0 : return natalPlanetsSelectionPanel.getSelectedBodyIDs();
            case 1 : return progressPlanetsSelectionPanel.getSelectedBodyIDs();
            case 2 : return transitPlanetsSelectionPanel.getSelectedBodyIDs();
        }
        return null;
    }

    /**
     * ネイタルでアスペクトを検出する天体のIDsを返す。
     */
    public int [] getAspectNatalBodyIDs() {
        return natalAspectPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * プログレスでアスペクトを検出する天体のIDsを返す。
     */
    public int [] getAspectProgressBodyIDs() {
        return progressAspectPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * トランジットでアスペクトを検出する天体のIDsを返す。
     */
    public int [] getAspectTransitBodyIDs() {
        return transitAspectPlanetsSelectionPanel.getSelectedBodyIDs();
    }

    /**
     * カスプが計算できないときのハウス分割法(ソーラーかソーラーサイン)コード
     * を帰す。
     */
    public char getCuspUnknownHouseSystem() {
        return housePreferencePanel1.getCuspUnkownHouseSystem().charAt(0);
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
     * アスペクトカテゴリー(1種2種3種、タイト、ルーズ)の表示/非表示を設定する
     * メニューを返す。チャートモジュール側でこのメニューをメニューバーにaddして
     * 使用する。
     */
    public JMenu getAspectCategorysMenu() {
        return aspectDisplaySettingPanel.getAspectCategorysMenu();
    }
    /**
     * アスペクトのカテゴリー表示モードを返す。
     * 1種2種3種、タイト、ルーズのそれぞれを表示するかしないかを表す配列を返す。
     * AspectCircle#setMode()で与える、表示モード設定である。
     * @return 配列にはshow,tight,loose,cat1,cat2,cat3,cat4の順で格納されているが、
     * 現在showはtrue、cat4はfalseに固定されている。
     */
    public boolean [] getAspectCategorys() {
        return aspectDisplaySettingPanel.getAspectCategorys();
    }
    /**
     * N,P,Tの各円の組合せごとのアスペクトタイプの配列を返す。
     * @param npt 1 = NN, 2=PP, 3=NP, 4=TT, 5=NT, 6=PTとしてNPT各円の組みあわせ
     * を指定する。b0=N,b1=P,b2=Tで、その組合せで値は決まっている。
     * @exception IllegalArgumentException 1から6以外の値を指定したとき。
     */
    public AspectType [] getAspectTypes( int npt ) {
        switch ( npt ) {
            case 1: return nnAspectTypePanel.getAspectTypes(); // NN
            case 2: return ppAspectTypePanel.getAspectTypes(); // PP
            case 3: return npAspectTypePanel.getAspectTypes(); // NP
            case 4: return ttAspectTypePanel.getAspectTypes(); // TT
            case 5: return ntAspectTypePanel.getAspectTypes(); // NT
            case 6: return ptAspectTypePanel.getAspectTypes(); // PT
        }
        throw new IllegalArgumentException("引数は1-6まで");
    }

    /**
     * アスペクトダイレクトラインモードのときはtrueを返す。
     */
    public boolean getDirectLineMode() {
        return aspectDisplaySettingPanel.getDirectLineMode();
    }

    /**
     * アスペクトダイレクトラインモードを選択するためのメニューアイテムを返す。
     */
    public JMenuItem getDirectLineModeMenuItem() {
        return aspectDisplaySettingPanel.getDirectLineModeMenuItem();
    }
    public static final int TAB_PLANET = 0;
    public static final int TAB_ORB = 1;
    public static final int TAB_RING = 2;
    public static final int TAB_HOUSE = 3;
    public static final int TAB_PROGRESSION = 4;
    /**
     * tabNumがTAB_ORBのとき、subTabにはgetShowRings()の値を指定する。
     * tabNumがTAB_PLANETのとき、subTabに0を指定すると表示天体、1を指定すると
     * アスペクト検出天体の設定パネルが選択される。
     */
    public void setSelectedTab( int tabNum, int subTab) {
        mainTabbedPane.setSelectedIndex( tabNum );
        if ( tabNum == TAB_ORB ) {
            subTab--;
            if ( subTab < 0 ) subTab = 0;
            if ( subTab >= 7 ) subTab = 0;
            orbTabbedPane.setSelectedIndex( subTab );
        } else if ( tabNum == TAB_PLANET ) {
            planetTabbedPane.setSelectedIndex( subTab & 1 );
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel10;
        javax.swing.JPanel jPanel11;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;
        javax.swing.JPanel jPanel4;
        javax.swing.JPanel jPanel5;
        javax.swing.JPanel jPanel6;
        javax.swing.JPanel jPanel7;
        javax.swing.JPanel jPanel8;
        javax.swing.JPanel jPanel9;

        mainTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        planetTabbedPane = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        natalPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        progressPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        transitPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        jPanel5 = new javax.swing.JPanel();
        natalAspectPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        progressAspectPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        transitAspectPlanetsSelectionPanel = new to.tetramorph.starbase.widget.PlanetsSelectionPanel();
        jPanel2 = new javax.swing.JPanel();
        orbTabbedPane = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        nnAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        jPanel7 = new javax.swing.JPanel();
        ppAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        jPanel8 = new javax.swing.JPanel();
        ttAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        jPanel9 = new javax.swing.JPanel();
        ntAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        jPanel10 = new javax.swing.JPanel();
        npAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        jPanel11 = new javax.swing.JPanel();
        ptAspectTypePanel = new to.tetramorph.starbase.widget.AspectTypePanel();
        aspectDisplaySettingPanel = new to.tetramorph.starbase.chartparts.AspectDisplaySettingPanel();
        housePreferencePanel1 = new to.tetramorph.starbase.widget.HousePreferencePanel();
        jPanel3 = new javax.swing.JPanel();
        progressComboBox = new to.tetramorph.starbase.chartparts.ProgressComboBox();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 1, 16, 1));
        natalPlanetsSelectionPanel.setMinimumSize(null);
        jPanel4.add(natalPlanetsSelectionPanel, new java.awt.GridBagConstraints());

        progressPlanetsSelectionPanel.setMinimumSize(null);
        progressPlanetsSelectionPanel.setTitle("\u30d7\u30ed\u30b0\u30ec\u30b9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel4.add(progressPlanetsSelectionPanel, gridBagConstraints);

        transitPlanetsSelectionPanel.setMinimumSize(null);
        transitPlanetsSelectionPanel.setTitle("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel4.add(transitPlanetsSelectionPanel, gridBagConstraints);

        planetTabbedPane.addTab("\u4f7f\u7528\u5929\u4f53", jPanel4);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 1, 16, 1));
        jPanel5.add(natalAspectPlanetsSelectionPanel, new java.awt.GridBagConstraints());

        progressAspectPlanetsSelectionPanel.setTitle("\u30d7\u30ed\u30b0\u30ec\u30b9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanel5.add(progressAspectPlanetsSelectionPanel, gridBagConstraints);

        transitAspectPlanetsSelectionPanel.setTitle("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel5.add(transitAspectPlanetsSelectionPanel, gridBagConstraints);

        planetTabbedPane.addTab("\u30a2\u30b9\u30da\u30af\u30c8\u691c\u51fa\u5929\u4f53", jPanel5);

        jPanel1.add(planetTabbedPane);

        mainTabbedPane.addTab("\u5929\u4f53", jPanel1);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jPanel6.setLayout(new java.awt.GridLayout(1, 0));

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel6.add(nnAspectTypePanel);

        orbTabbedPane.addTab("N", jPanel6);

        jPanel7.setLayout(new java.awt.GridLayout(1, 0));

        jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel7.add(ppAspectTypePanel);

        orbTabbedPane.addTab("P", jPanel7);

        jPanel8.setLayout(new java.awt.GridLayout(1, 0));

        jPanel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel8.add(ttAspectTypePanel);

        orbTabbedPane.addTab("T", jPanel8);

        jPanel9.setLayout(new java.awt.GridLayout(1, 0));

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel9.add(ntAspectTypePanel);

        orbTabbedPane.addTab("N-T", jPanel9);

        jPanel10.setLayout(new java.awt.GridLayout(1, 0));

        jPanel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel10.add(npAspectTypePanel);

        orbTabbedPane.addTab("N-P", jPanel10);

        jPanel11.setLayout(new java.awt.GridLayout(1, 0));

        jPanel11.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        jPanel11.add(ptAspectTypePanel);

        orbTabbedPane.addTab("P-T", jPanel11);

        jPanel2.add(orbTabbedPane);

        mainTabbedPane.addTab("\u30aa\u30fc\u30d6", jPanel2);

        mainTabbedPane.addTab("\u8868\u793a\u5186\u7a2e\u3068\u30a2\u30b9\u30da\u30af\u30c8", aspectDisplaySettingPanel);

        mainTabbedPane.addTab("\u30cf\u30a6\u30b9\u8a08\u7b97\u6cd5", housePreferencePanel1);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel3.add(progressComboBox, gridBagConstraints);

        jLabel1.setText("\u9032\u884c\u6cd5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel3.add(jLabel1, gridBagConstraints);

        mainTabbedPane.addTab("\u9032\u884c\u6cd5", jPanel3);

        add(mainTabbedPane, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private to.tetramorph.starbase.chartparts.AspectDisplaySettingPanel aspectDisplaySettingPanel;
    private to.tetramorph.starbase.widget.HousePreferencePanel housePreferencePanel1;
    private javax.swing.JTabbedPane mainTabbedPane;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel natalAspectPlanetsSelectionPanel;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel natalPlanetsSelectionPanel;
    private to.tetramorph.starbase.widget.AspectTypePanel nnAspectTypePanel;
    private to.tetramorph.starbase.widget.AspectTypePanel npAspectTypePanel;
    private to.tetramorph.starbase.widget.AspectTypePanel ntAspectTypePanel;
    private javax.swing.JTabbedPane orbTabbedPane;
    private javax.swing.JTabbedPane planetTabbedPane;
    private to.tetramorph.starbase.widget.AspectTypePanel ppAspectTypePanel;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel progressAspectPlanetsSelectionPanel;
    private to.tetramorph.starbase.chartparts.ProgressComboBox progressComboBox;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel progressPlanetsSelectionPanel;
    private to.tetramorph.starbase.widget.AspectTypePanel ptAspectTypePanel;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel transitAspectPlanetsSelectionPanel;
    private to.tetramorph.starbase.widget.PlanetsSelectionPanel transitPlanetsSelectionPanel;
    private to.tetramorph.starbase.widget.AspectTypePanel ttAspectTypePanel;
    // End of variables declaration//GEN-END:variables


}
