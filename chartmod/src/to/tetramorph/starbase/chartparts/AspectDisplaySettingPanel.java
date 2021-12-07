/*
 * AspectDisplaySettingPanel.java
 *
 * Created on 2007/09/09, 5:26
 */

package to.tetramorph.starbase.chartparts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import to.tetramorph.starbase.widget.*;

/**
 * アスペクトの表示の際に、1種〜3種のどれを表示するかを設定するパネル。
 * NPTSpecificSettingPanelに集約されている部品で、タイトとルーズ、どちらか
 * または両方を表示するかを設定するためのパネル。
 * またCombinationSelectorPanelを継承したパネル(たとえばAspectCombinationPanel)を、
 * このパネルにはめ込む機能がある。はめ込まれるパネルは、このパネルがもっている
 * 機能とは直接は関係がないが、GUIのデザインの都合上、そういう仕様になっている。
 * 
 * @author 大澤義鷹
 */
public class AspectDisplaySettingPanel extends javax.swing.JPanel {
    
    /**
     * mode[SHOW]がtrueならアスペクトを表示。falseなら非表示。
     */
    
    public static final int SHOW = 0;
    
    /**
     * mode[TIGHT]がtrueならタイトアスペクトを表示。falseならタイトアスペクト非表示。
     */
    public static final int TIGHT = 1;
    
    /**
     * mode[LOOSE]がtrueならルーズアスペクトを表示。falseならルーズアスペクト非表示。
     */
    public static final int LOOSE = 2;
    
    /**
     * mode[CATEGORY1]がtrueなら第1種アスペクト表示。falseなら非表示。
     * 1種とはCONJUNCTION,SEXTILE,SQUARE,TRINE,OPPOSITION。
     */
    public static final int CATEGORY1 = 3;
    
    /**
     * mode[CATEGORY2]がtrueなら第2種アスペクト表示。falseなら非表示。
     * 2種とはSEMI_SEXTILE,SEMI_SQUARE,SESQUIQUADRATE(135),ENCONJUNCT(150)
     */
    public static final int CATEGORY2 = 4;
    
    /**
     * mode[CATEGORY3]がtrueなら第3種アスペクト表示。falseなら非表示。
     * QUINTILE(72),SEMI_QUINTILE(36),BI_QUINTILE(144)
     */
    public static final int CATEGORY3 = 5;
    
    /**
     * mode[CATEGORY4]がtrueなら特殊アスペクト表示。falseなら非表示。
     * PARALLELやその他のアスペクト
     */
    public static final int CATEGORY4 = 6;
    
    private boolean [] flags = new boolean[7];
    private JCheckBox [] rangeCheckBoxs;
    private CustomizePanel customizePanel;
    private JCheckBoxMenuItem directLineModeCheckBoxMenuItem;
    
    /**
     * オブジェクトを作成する。
     */
    public AspectDisplaySettingPanel() {
        initComponents();
        initComponents2();
    }
    
    private void initComponents2() {
        //1種〜3種のカテゴリー選択メニューにリスナ登録
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int selnum = Integer.parseInt(
                    ((JRadioButtonMenuItem)evt.getSource()).getActionCommand());
                categoryComboBox.setSelectedIndex(selnum);
                //setCategoryAndRange()はコンボのリスナがキックされるので不要
                customizePanel.doClickUseButton();
            }
        };
        for(Enumeration en = categoryButtonGroup.getElements(); en.hasMoreElements(); )
            ((JRadioButtonMenuItem)en.nextElement()).addActionListener(al);
        //タイト・ルーズの選択ラジオボタンにリスナ登録
        ActionListener al2 = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String acmd = ((JRadioButton)evt.getSource()).getActionCommand();
                for(Enumeration en = rangeButtonGroup2.getElements(); en.hasMoreElements(); ) {
                    JRadioButtonMenuItem item = (JRadioButtonMenuItem)en.nextElement();
                    if(item.getActionCommand().equals(acmd)) {
                        item.setSelected(true);
                        setCategoryAndRange();
                    }
                }
            }
        };
        for(Enumeration en = rangeButtonGroup.getElements(); en.hasMoreElements(); )
            ((JRadioButton)en.nextElement()).addActionListener(al2);
        //タイト・ルーズの選択ラジオボタンメニューにリスナ登録
        ActionListener al3 = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String acmd = ((JRadioButtonMenuItem)evt.getSource()).getActionCommand();
                for(Enumeration en = rangeButtonGroup.getElements(); en.hasMoreElements(); ) {
                    JRadioButton item = (JRadioButton)en.nextElement();
                    if(item.getActionCommand().equals(acmd)) {
                        item.setSelected(true);
                        setCategoryAndRange();
                        customizePanel.doClickUseButton();
                        break;
                    }
                }
            }
        };
        for(Enumeration en = rangeButtonGroup2.getElements(); en.hasMoreElements(); )
            ((JRadioButtonMenuItem)en.nextElement()).addActionListener(al3);
        
        directLineModeCheckBoxMenuItem = new JCheckBoxMenuItem("ダイレクト結線");
        directLineModeCheckBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                boolean b = directLineModeCheckBoxMenuItem.isSelected();
                directLineModeCheckBox.setSelected(b);
                customizePanel.doClickUseButton();
            }
        });
        directLineModeCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                boolean b = directLineModeCheckBox.isSelected();
                directLineModeCheckBoxMenuItem.setSelected(b);
            }
        });
    }
//カテゴリーとタイト・ルーズ選択------------------------------------------------
    private void setCategoryAndRange() {
        flags[ SHOW ] = true;
        String cmd = rangeButtonGroup.getSelection().getActionCommand();
        int i = Integer.parseInt( cmd );
        flags[ TIGHT ] = (i & 1) != 0;
        flags[ LOOSE ] = (i & 2) != 0;
        i= new int [] { 7,1,2,4,3,5,6 } [ categoryComboBox.getSelectedIndex() ];
        flags[ CATEGORY1 ] = (i & 1) != 0;
        flags[ CATEGORY2 ] = (i & 2) != 0;
        flags[ CATEGORY3 ] = (i & 4) != 0;
        flags[ CATEGORY4 ] = false;
    }
    
    /**
     * AspectCircleのsetMode()に与えるスイッチを返す。
     * @return 要素数7の配列で返る。show,tight,loose,cat1,cat2,cat3,cat4の順だが、
     * showはtrue、cat4はfalseに固定されている。
     */
    public boolean [] getAspectCategorys() {
        return flags;
    }
    
    /**
     * アスペクト表示で「タイト、ルーズ、1種、2種、3種」の表示/非表示を指定する。
     * これはAspectCircleのsetMode()に与えるスイッチとみなせる。
     * 実際には「表示,タイト,ルーズ,1種、2種、3種、4種」なのだが、現在表示と4種は
     * 無視つねにOFFである。
     * @return "0,1,0,1,0,0,0"といった文字列で"1"はtrueを"0"はfalseを表す。
     */
    public String getAspectCategorysString() {
        StringBuffer sb = new StringBuffer();
        for ( int i=0; i<flags.length; i++ ) {
            sb.append( flags[i] ? "1" : "0");
            sb.append(",");
        }
        sb.deleteCharAt( sb.length() - 1 );
        return sb.toString();
    }
    
    /**
     * タイト、ルーズ、1種、2種、3種の表示/非表示を文字列で指定する。
     * 実際には「表示,タイト,ルーズ,1種、2種、3種、4種」なのだが、現在表示と4種は
     * 無視つねにOFFである。
     * @param value "1,0,0,1,0,0,0"といった文字列を与える。null,"",7つに分割できな
     * い文字列が入力されると、
     * 表示と4種以外はすべてONに設定される。つまり"1,1,1,1,1,1,0"を与えたのと同じ。
     */
    public void setAspectCategorys(String value) {
        //System.out.println("AspectShowMode = " + value);
        if ( value == null || 
              value.length() == 0 || 
              value.split(",").length != 7 ) {
            value = "1,1,1,1,1,1,0";
        }
        String [] values = value.split(",");
        for ( int i=0; i<values.length; i++ ) 
            flags[i] = values[i].equals("1");
        flags[ SHOW ] = true;
        flags[ CATEGORY4 ] = false;
        setAspectCategorys(flags);
    }
    
    /**
     * タイト、ルーズ、1種、2種、3種の表示/非表示を文字列で指定する。
     * 実際には「表示,タイト,ルーズ,1種、2種、3種、4種」なのだが、現在表示と4種は
     * 無視つねにOFFである。
     * @param flags 要素7の配列で、show,tight,loose,cat1,cat2,cat3,cat4の順だが、
     * show,cat4はどのような値を指定してもshowはtrue、cat4はfalseとなる。
     */
    public void setAspectCategorys(boolean [] flags) {
        int [] a = new int[flags.length];
        for ( int i=0; i<flags.length; i++ ) {
            if ( flags[i] ) a[i] = 1;
        }
        int n = ( a[ TIGHT ]  + a[ LOOSE ] * 2 );
        String s = (n == 0) ? "3" : "" + n;
        //String s = "" + (a[ TIGHT ]  + a[ LOOSE ] * 2);
        for ( Enumeration en = 
                      rangeButtonGroup.getElements(); en.hasMoreElements(); ) {
            JRadioButton rb = (JRadioButton)en.nextElement();
            if ( rb.getActionCommand().equals(s) ) { 
                rb.setSelected(true); 
                break; 
            }
        }
        for ( Enumeration en = 
                      rangeButtonGroup2.getElements(); en.hasMoreElements(); ) {
            JRadioButtonMenuItem rb = (JRadioButtonMenuItem)en.nextElement();
            if ( rb.getActionCommand().equals(s) ) { 
                rb.setSelected(true); 
                break;
            }
        }
        int v = a[ CATEGORY1 ] + a[ CATEGORY2] * 2 + a[ CATEGORY3 ] * 4;
        int i= new int [] { 0,1,2,4,3,5,6,0 }[ v ];
        categoryComboBox.setSelectedIndex(i);
        //これでComboBoxのActionListenerがトリガされる
        //RadioButtonMenuItemはsetSelected()ではトリガされないがComboBoxはトリガされる。
    }
    
    /**
     * アスペクトカテゴリーメニューを返す。(1,2,3種,タイト,ルーズの選択メニュー)
     */
    public JMenu getAspectCategorysMenu() {
        return rangeMenu;
    }
//-----------------------------------------------------------------------------
    /**
     * アスペクトダイレクトラインモードを設定する。
     * @param b trueを設定すると、ダイレクトラインモード。
     */
    public void setDirectLineMode(boolean b) {
        directLineModeCheckBox.setSelected(b);
        directLineModeCheckBoxMenuItem.setSelected(b);
    }
    
    /**
     *　アスペクトダイレクトラインモードのときはtrueを返す。
     */
    public boolean getDirectLineMode() {
        return directLineModeCheckBox.isSelected();
    }
    
    /**
     * アスペクトダイレクトラインモードを設定する。
     * @param b "true"をセットするとダイレクトラインモード。
     */
    public void setDirectLineMode(String b) {
        setDirectLineMode(Boolean.parseBoolean(b));
    }
    
    /**
     * アスペクトダイレクトラインモードを選択するためのJMenuItemを返す。
     * JMenuItemで返るが中身はJCheckBoxMenuItem。
     */
    public JMenuItem getDirectLineModeMenuItem() {
        return directLineModeCheckBoxMenuItem;
    }

//-----------------------------------------------------------------------------
    
    /**
     * CustomizePanelをセットする。このパネルが動作するために、かならずセットが必要。
     */
    public void setCustomizePanel(CustomizePanel cp) {
        this.customizePanel = cp;
    }
    
    CombinationSelectorPanel combi;
    
    /**
     * このパネルにCobbinationSelectorPanelをセットする。
     * 今のところ話を簡単にするために、一回のみしか呼ばれず、その後パネルが
     * 削除されることもないものとする。要するに追加のみで削除の機能を用意していない。
     * どのみち抜き差しする必要性は無いからだ。
     */
    public void setCombinationSelectorPanel( CombinationSelectorPanel combi ) {
        this.combi = combi;
        selectorPanel.add( combi );
    }
    
    /**
     * このパネルにセットされているCombinationSelectorPanelを返す。
     * 未セットならnullを返す。
     */
    public CombinationSelectorPanel getCombinationSelectorPanel() {
        return combi;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem1;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem2;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem3;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem4;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem5;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem6;
        javax.swing.JRadioButtonMenuItem categoryRadioButtonMenuItem7;
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel7;
        javax.swing.JLabel jLabel8;
        javax.swing.JMenuBar jMenuBar1;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JSeparator jSeparator1;
        javax.swing.JRadioButton rangeRadioButton1;
        javax.swing.JRadioButton rangeRadioButton2;
        javax.swing.JRadioButton rangeRadioButton3;
        javax.swing.JRadioButtonMenuItem rangeRadioButtonMenuItem1;
        javax.swing.JRadioButtonMenuItem rangeRadioButtonMenuItem2;
        javax.swing.JRadioButtonMenuItem rangeRadioButtonMenuItem3;

        jMenuBar1 = new javax.swing.JMenuBar();
        rangeMenu = new javax.swing.JMenu();
        rangeRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        rangeRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        rangeRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        categoryRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem6 = new javax.swing.JRadioButtonMenuItem();
        categoryRadioButtonMenuItem7 = new javax.swing.JRadioButtonMenuItem();
        rangeButtonGroup = new javax.swing.ButtonGroup();
        rangeButtonGroup2 = new javax.swing.ButtonGroup();
        categoryButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        categoryComboBox = new javax.swing.JComboBox();
        rangeRadioButton1 = new javax.swing.JRadioButton();
        rangeRadioButton2 = new javax.swing.JRadioButton();
        rangeRadioButton3 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        selectorPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        directLineModeCheckBox = new javax.swing.JCheckBox();

        rangeMenu.setText("\u7a2e\u5225\u3068\u30aa\u30fc\u30d6");
        rangeRadioButtonMenuItem1.setText("\u30bf\u30a4\u30c8\u3068\u30eb\u30fc\u30ba");
        rangeRadioButtonMenuItem1.setActionCommand("3");
        rangeMenu.add(rangeRadioButtonMenuItem1);

        rangeRadioButtonMenuItem2.setText("\u30bf\u30a4\u30c8\u306e\u307f");
        rangeRadioButtonMenuItem2.setActionCommand("1");
        rangeMenu.add(rangeRadioButtonMenuItem2);

        rangeRadioButtonMenuItem3.setText("\u30eb\u30fc\u30ba\u306e\u307f");
        rangeRadioButtonMenuItem3.setActionCommand("2");
        rangeMenu.add(rangeRadioButtonMenuItem3);

        rangeMenu.add(jSeparator1);

        categoryRadioButtonMenuItem1.setText("1\u7a2e\u304b\u30893\u7a2e");
        categoryRadioButtonMenuItem1.setActionCommand("0");
        rangeMenu.add(categoryRadioButtonMenuItem1);

        categoryRadioButtonMenuItem2.setText("1\u7a2e\u306e\u307f");
        categoryRadioButtonMenuItem2.setActionCommand("1");
        rangeMenu.add(categoryRadioButtonMenuItem2);

        categoryRadioButtonMenuItem3.setText("2\u7a2e\u306e\u307f");
        categoryRadioButtonMenuItem3.setActionCommand("2");
        rangeMenu.add(categoryRadioButtonMenuItem3);

        categoryRadioButtonMenuItem4.setText("3\u7a2e\u306e\u307f");
        categoryRadioButtonMenuItem4.setActionCommand("3");
        rangeMenu.add(categoryRadioButtonMenuItem4);

        categoryRadioButtonMenuItem5.setText("1\u7a2e\u30682\u7a2e");
        categoryRadioButtonMenuItem5.setActionCommand("4");
        rangeMenu.add(categoryRadioButtonMenuItem5);

        categoryRadioButtonMenuItem6.setText("1\u7a2e\u30683\u7a2e");
        categoryRadioButtonMenuItem6.setActionCommand("5");
        rangeMenu.add(categoryRadioButtonMenuItem6);

        categoryRadioButtonMenuItem7.setText("2\u7a2e\u30683\u7a2e");
        categoryRadioButtonMenuItem7.setActionCommand("6");
        rangeMenu.add(categoryRadioButtonMenuItem7);

        jMenuBar1.add(rangeMenu);

        rangeButtonGroup.add(rangeRadioButton1);
        rangeButtonGroup.add(rangeRadioButton2);
        rangeButtonGroup.add(rangeRadioButton3);

        rangeButtonGroup2.add(rangeRadioButtonMenuItem1);
        rangeButtonGroup2.add(rangeRadioButtonMenuItem2);
        rangeButtonGroup2.add(rangeRadioButtonMenuItem3);

        categoryButtonGroup.add(categoryRadioButtonMenuItem1);
        categoryButtonGroup.add(categoryRadioButtonMenuItem2);
        categoryButtonGroup.add(categoryRadioButtonMenuItem3);
        categoryButtonGroup.add(categoryRadioButtonMenuItem4);
        categoryButtonGroup.add(categoryRadioButtonMenuItem5);
        categoryButtonGroup.add(categoryRadioButtonMenuItem6);
        categoryButtonGroup.add(categoryRadioButtonMenuItem7);

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8\u306e\u7a2e\u5225"));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/aspcat1.gif")));
        jLabel3.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/aspcat2.gif")));
        jLabel4.setText("  ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/aspcat3.gif")));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel1.setText("1\u7a2e");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel6.setText("2\u7a2e");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel7.setText("3\u7a2e");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(jLabel7, gridBagConstraints);

        categoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1\u7a2e\u304b\u30893\u7a2e", "1\u7a2e\u306e\u307f", "2\u7a2e\u306e\u307f", "3\u7a2e\u306e\u307f", "1\u7a2e\u30682\u7a2e", "1\u7a2e\u30683\u7a2e", "2\u7a2e\u30683\u7a2e" }));
        categoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(categoryComboBox, gridBagConstraints);

        rangeRadioButton1.setSelected(true);
        rangeRadioButton1.setText("\u30bf\u30a4\u30c8\u3068\u30eb\u30fc\u30ba");
        rangeRadioButton1.setActionCommand("3");
        rangeRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rangeRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel1.add(rangeRadioButton1, gridBagConstraints);

        rangeRadioButton2.setText("\u30bf\u30a4\u30c8\u306e\u307f");
        rangeRadioButton2.setActionCommand("1");
        rangeRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rangeRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel1.add(rangeRadioButton2, gridBagConstraints);

        rangeRadioButton3.setText("\u30eb\u30fc\u30ba\u306e\u307f");
        rangeRadioButton3.setActionCommand("2");
        rangeRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rangeRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel1.add(rangeRadioButton3, gridBagConstraints);

        jLabel8.setText("\u30aa\u30fc\u30d6\u9078\u629e");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        jPanel1.add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);

        selectorPanel.setLayout(new java.awt.GridLayout(1, 0));

        selectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("\u8868\u793a\u3059\u308b\u30db\u30ed\u30b9\u30b3\u30fc\u30d7\u306e\u7a2e\u985e\u3068\u8a2d\u5b9a"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(selectorPanel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        directLineModeCheckBox.setSelected(true);
        directLineModeCheckBox.setText("\u30c0\u30a4\u30ec\u30af\u30c8\u7d50\u7dda");
        directLineModeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directLineModeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel2.add(directLineModeCheckBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(jPanel2, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
// カテゴリー選択コンボボックスのイベント
  private void categoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryComboBoxActionPerformed
      for ( Enumeration en = 
                    categoryButtonGroup.getElements(); en.hasMoreElements(); ) {
          JRadioButtonMenuItem rb = (JRadioButtonMenuItem)en.nextElement();
          int i = Integer.parseInt( rb.getActionCommand() );
          if ( i == categoryComboBox.getSelectedIndex())
              rb.setSelected(true);
      }
      setCategoryAndRange();
  }//GEN-LAST:event_categoryComboBoxActionPerformed
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup categoryButtonGroup;
    private javax.swing.JComboBox categoryComboBox;
    private javax.swing.JCheckBox directLineModeCheckBox;
    private javax.swing.ButtonGroup rangeButtonGroup;
    private javax.swing.ButtonGroup rangeButtonGroup2;
    private javax.swing.JMenu rangeMenu;
    private javax.swing.JPanel selectorPanel;
    // End of variables declaration//GEN-END:variables
    
}
