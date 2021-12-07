/*
 * AspectCombinationPanel.java
 *
 * Created on 2007/09/29, 6:36
 */

package to.tetramorph.starbase.chartparts;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 * NPT相性円の3種類の組合せ各状況でのアスペクト描画の組合せパターンと、
 * 選択天体のアスペクト標的を設定するパネル。
 * 3枚の設定パネル(チェックボックスパネルという)があり、コンボボックスでで切り替
 * えて表示する。パネルの切替にはCardLayoutを使用している。
 * またチェックボックスパネルに対応する、7枚のメニューが用意されていて、
 * チャートモジュールはこのメニューをメニューバーに追加し使用することができる。
 * メニュー側のチェックを変更すると、チェックボックスパネルも同じように変更され
 * そのときは設定パネルの「保存せず適用」を実行したのと同じ結果になる。
 * このコンポーネントはAspectDisplaySettingPanel内の部品。
 *
 * 基本的にはGUIエディタで、チェックボックスパネルをデザインして、ActionCommand
 * を設定し、MenuItemを配列に定義してやれば、必要なメニューなどは自動で生成する
 * 仕組みになっている。
 */
public class AspectCombinationPanel2 extends CombinationSelectorPanel {
    
    //アスペクトコンビネーションのアイテム
    private JCheckBox [][] combiBoxs;
    private JCheckBoxMenuItem [][] boxItems;
    private int aspectSwitch; //選択されているパネルのコンビネーションチェクボックスの値
    private JMenu combiMenu = new JMenu("アスペクト検出");
    
    //アスペクトターゲットのアイテム
    
    JCheckBox [][] targetBoxs;
    JCheckBoxMenuItem [][] targetBoxItems;
    int targetSwitch;  // 選択されているパネルのターゲットチェクボックスの値
    JMenu targetMenu = new JMenu("選択天体ターゲット");
    
    //リング選択用のアイテム
    
    JRadioButtonMenuItem [] ringRadioItems;
    ButtonGroup ringButtonGroup = new ButtonGroup();
    int cardIndex;         // 選択されているチェックボックスパネルの番号
    JMenu ringMenu = new JMenu("ホロスコープの種類選択");
    
    //天体リスト用のアイテム
    
    JRadioButtonMenuItem [][] listRadioItems;
    ButtonGroup [] listGroups;
    ButtonGroup [] listGroups2;
    JRadioButton [][] listRadioButtons;
    JMenu listMenu = new JMenu("天体リストの種類選択");
    int bodyListSwitch; // 天体リスト表示選択フラグ
    //
    CustomizePanel customizePanel; //保存せず適用を実行するために必要
    
    /** コンストラクタ */
    public AspectCombinationPanel2() {
        initComponents();
        initMenuItems();
        initRingMenu();
        initCombiMenu();
        initTargetMenu();
        initListMenu();
        setSelectedIndex(0);
    }
    private void initMenuItems() {
        // メモ 将来の拡張にそなえてコメント化している部品がある。
        // ただしこれを有効にしたときはsetValues内のデフォルト値も修正が必要。
        combiBoxs = new JCheckBox[][] {
            { combiCheckBox1, combiCheckBox2, combiCheckBox3,
              combiCheckBox4, combiCheckBox5, combiCheckBox6,
              combiCheckBox7, combiCheckBox8, combiCheckBox9,
              combiCheckBoxA, combiCheckBoxB, combiCheckBoxC,
              combiCheckBoxD, combiCheckBoxE, combiCheckBoxF },
              { combiCheckBox11, combiCheckBox12, combiCheckBox13 },
              { combiCheckBox21, combiCheckBox22, combiCheckBox23,
                combiCheckBox24,combiCheckBox25,combiCheckBox26 },
//              { combiCheckBox31 },
//              { combiCheckBox41, combiCheckBox42, combiCheckBox43 },
//              { combiCheckBox51, combiCheckBox52, combiCheckBox53 },
//              { combiCheckBox61, combiCheckBox62, combiCheckBox63 }
        };
        targetBoxs = new JCheckBox[][] {
            { targetCheckBox1,  targetCheckBox2, targetCheckBox3,
              targetCheckBox4,  targetCheckBox5 },
            { targetCheckBox11, targetCheckBox12 },
            { targetCheckBox21, targetCheckBox22, targetCheckBox23 },
//            { targetCheckBox31 },
//            { targetCheckBox41, targetCheckBox42 },
//            { targetCheckBox51, targetCheckBox52 },
//            { targetCheckBox61, targetCheckBox62 }
        };
        listRadioButtons = new JRadioButton[][] {
            { listRadioButton1, listRadioButton2, listRadioButton3,
              listRadioButton4, listRadioButton5 },
            { listRadioButton11, listRadioButton12 },
            { listRadioButton21, listRadioButton22, listRadioButton23 },
//            { listRadioButton31 },
//            { listRadioButton41, listRadioButton42 },
//            { listRadioButton51, listRadioButton52 },
//            { listRadioButton61, listRadioButton62 }
        };        
    }
    //リング選択コンボボックスに対応するラジオメニューの作成と設定。(一つ用意される)
    private void initRingMenu() {
        ActionListener al3 = new ActionListener() {
            //選択されたラジオボタンに合わせて、コンボボックスを選択。
            //コンボボックスは自身のリスナから、setResult()をキックする。
            public void actionPerformed(ActionEvent evt) {
                JRadioButtonMenuItem item = (JRadioButtonMenuItem)evt.getSource();
                setShowRings( Integer.parseInt( item.getActionCommand() ) );
                customizePanel.doClickUseButton();
            }
        };
        ringRadioItems = new JRadioButtonMenuItem[ ringComboBox.getItemCount() ];

        // メニューの順番を交換したい。まじめにやると大変なのでちょっと手抜き
        int [] nums = { 1, 2, 0 };
        //for ( int i=0; i<ringRadioItems.length; i++ ) {
        for ( int i : nums ) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.setText( (String)ringComboBox.getItemAt(i) ); //コンボの内容を複写
            item.setActionCommand("" + i); //シリアルを設定
            item.addActionListener(al3);
            ringButtonGroup.add(item);
            ringRadioItems[i] = item;
            ringMenu.add(item);
        }
    }
    //アスペクトコンビネーション選択メニューの設定(6種類用意される)
    private void initCombiMenu() {
        boxItems = new JCheckBoxMenuItem[ combiBoxs.length ][];
        //
        //コンビネーション選択パネルのチェックボックスのリスナ
        //
        ActionListener al2 = new ActionListener() {
            //対応するメニュー側のチェックボックスにも選択を反映させる
            public void actionPerformed(ActionEvent evt) {
                JCheckBox cb = (JCheckBox)evt.getSource();
                //選択されたチェックボックスを見つける
                for ( int j=0; j < combiBoxs.length; j++ ) {
                    for ( int i=0; i<combiBoxs[j].length; i++ ) {
                        if ( cb == combiBoxs[j][i] ) {
                            boxItems[j][i].setSelected(cb.isSelected());
                            break;
                        }
                    }
                }
                setResult(); //変更状態を変数resultに反映させる
            }
        };
        //
        //アスペクトコンビネーション選択メニューのリスナ。メニューアイテムに対応する
        //パネル側のチェックボックスに選択を反映し「登録せず適用」を行う。
        //
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)evt.getSource();
                String [] v = item.getActionCommand().split(",");
                int cbp = Integer.parseInt(v[0]); //チェックボックスパネル番号を得る
                int idx = Integer.parseInt(v[1]); //パネル内の何番目のチェックボックスかを得る
                combiBoxs[cbp][idx].setSelected(item.isSelected());
                setResult(); //変更状態を変数resultに反映させる
                customizePanel.doClickUseButton();
            }
        };
        //コンビネーション選択メニューの動的生成と、メニューとパネル両方にリスナを登録
        for ( int j=0; j < combiBoxs.length; j++ ) {
            boxItems[j] = new JCheckBoxMenuItem[ combiBoxs[j].length ];
            for ( int i=0; i < combiBoxs[j].length; i++ ) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem();
                JCheckBox cb = combiBoxs[j][i];
                cb.addActionListener(al2);
                item.setText( cb.getText() );
                item.setSelected( cb.isSelected() );
                item.setEnabled( cb.isEnabled() );
                item.setActionCommand( j + "," + i ); //パネル番号,ボックス番号
                item.addActionListener(al);
                boxItems[j][i] = item;
            }
        }
    }
    //選択天体のアスペクト標的メニューの作成(6種類用意される)
    private void initTargetMenu() {
        targetBoxItems = new JCheckBoxMenuItem[ targetBoxs.length ][];
        //
        //コンビネーション選択パネルのターゲットチェックボックスのリスナ
        //
        ActionListener al4 = new ActionListener() {
            //対応するターゲットメニュー側のチェックボックスにも選択を反映させる
            public void actionPerformed(ActionEvent evt) {
                JCheckBox cb = (JCheckBox)evt.getSource();
                //選択されたチェックボックスを見つける
                for ( int j=0; j<targetBoxs.length; j++ ) {
                    for ( int i=0; i<targetBoxs[j].length; i++ ) {
                        if ( cb == targetBoxs[j][i] ) {
                            targetBoxItems[j][i].setSelected(cb.isSelected());
                            break;
                        }
                    }
                }
                setResult(); //変更状態を変数resultに反映させる
            }
        };
        //
        //ターゲット選択メニューのリスナ。メニューアイテムに対応するターゲット
        //チェックボックスに選択を反映し「登録せず適用」を行う。
        //
        ActionListener al5 = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)evt.getSource();
                String [] v = item.getActionCommand().split(",");
                int cbp = Integer.parseInt(v[0]); //チェックボックスパネル番号を得る
                int idx = Integer.parseInt(v[1]); //パネル内の何番目のチェックボックスかを得る
                targetBoxs[cbp][idx].setSelected(item.isSelected());
                setResult(); //変更状態を変数resultに反映させる
                customizePanel.doClickUseButton();
            }
        };
        for ( int j=0; j < targetBoxs.length; j++ ) {
            //targetMenus[j] = new JMenu("選択天体のアスペクト標的");
            targetBoxItems[j] = new JCheckBoxMenuItem[ targetBoxs[j].length ];
            for ( int i=0; i < targetBoxs[j].length; i++ ) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem();
                JCheckBox cb = targetBoxs[j][i];
                cb.addActionListener(al4);
                item.setText(cb.getText());
                item.setSelected( cb.isSelected() );
                item.setEnabled( cb.isEnabled() );
                item.setActionCommand( j + "," + i ); //パネル番号,ボックス番号
                item.addActionListener(al5);
                targetBoxItems[j][i] = item;
            }
        }
    }
    //天体リストの選択メニューの設定
    private void initListMenu() {
        listGroups = new ButtonGroup[ listRadioButtons.length ];
        listGroups2 = new ButtonGroup[ listRadioButtons.length ];
        listRadioItems = new JRadioButtonMenuItem[ listRadioButtons.length ][];
        //リスト選択ラジオボタンのリスナ。選択とともにラジオメニューも同期選択
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                JRadioButton rb = (JRadioButton)evt.getSource();
                for ( int j=0; j < listRadioButtons.length; j++ ) {
                    for ( int i=0; i < listRadioButtons[j].length; i++ ) {
                        if ( rb == listRadioButtons[j][i] ) {
                            listRadioItems[j][i].setSelected( true );
                            break;
                        }
                    }
                }
                setResult(); //変更状態を変数に反映させる
            }
        };
        //メニューのラジオアイテムのリスナ
        ActionListener al2 = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JRadioButtonMenuItem item = (JRadioButtonMenuItem)evt.getSource();
                //この手法で検出すると、メニュー側のActionCommandは不要
                for ( int j=0; j < listRadioItems.length; j++ ) {
                    for ( int i=0; i < listRadioItems[j].length; i++ ) {
                        if ( item == listRadioItems[j][i] ) {
                            listRadioButtons[j][i].setSelected( true );
                            break;
                        }
                    }
                }
                setResult(); //変更状態を変数resultに反映させる
                customizePanel.doClickUseButton();
            }
        };
        //天体リストメニューの動的生成と、リスナの登録
        for ( int j=0; j<listRadioButtons.length; j++ ) {
            listGroups[j] = new ButtonGroup(); //ただしﾎﾞﾀﾝ1つの場合は生成しない
            listGroups2[j] = new ButtonGroup();
            listRadioItems[j] = 
                new JRadioButtonMenuItem[ listRadioButtons[j].length ];
            for ( int i=0; i<listRadioButtons[j].length; i++ ) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem();
                JRadioButton rb = listRadioButtons[j][i];
                rb.addActionListener(al);
                item.addActionListener(al2);
                listGroups[j].add(rb);
                listGroups2[j].add(item);
                item.setText(rb.getText());
                item.setSelected(rb.isSelected());
                item.setEnabled(rb.isEnabled());
                listRadioItems[j][i] = item;
            }
        }
    }
    
    /**
     * CustomizePanelをセットする。
     */
    public void setCustomizePanel( CustomizePanel cp ) {
        this.customizePanel = cp;
    }
    
    /**
     * 7種類の組円に対応するチェックボックスパネルを表示する。
     * @param cardIndex 0=NPT,1=N,2=P,3=T,4=N-T,5=N-P,6=P-T。
     */
    private void setSelectedIndex( int cardIndex ) {
        this.cardIndex = cardIndex;
        CardLayout l = (CardLayout)cardPanel.getLayout();
        l.show( cardPanel, "card" + cardIndex );
        ringRadioItems[ cardIndex ].setSelected( true );
        setResult();
        //コンビネーションメニューを選択に応じて差し替え
        combiMenu.removeAll();
        for ( int i=0; i < boxItems[ cardIndex ].length; i++ )
            combiMenu.add( boxItems[ cardIndex ][i] );
        //ターゲットメニューを選択に応じて差し替え
        targetMenu.removeAll();
        for ( int i=0; i < targetBoxItems[ cardIndex ].length; i++ )
            targetMenu.add( targetBoxItems[ cardIndex ][i] );
        //リストメニューを選択に応じて差し替え
        listMenu.removeAll();
        for ( int i=0; i < listRadioItems[ cardIndex ].length; i++ )
            listMenu.add( listRadioItems[ cardIndex ][i] );
    }
    
    /**
     * 表示する天体リングを返す。
     * ( 現在選択中のチェックボックスパネルの番号を返す。 )
     * <pre>
     * 0 = N1,P1,N2,P2,T
     * 1 = N1,N2
     * 2 = N1,N2,T
     * </pre>
     */
    public int getShowRings() {
        return cardIndex;
    }
    
    public Preference getPreference( Preference pref ) {
        pref.setProperty("AspectCombinations",getValues());
        //pref.setProperty("ShowRings", "" + getShowRings());    
        
        return pref;
    }
    // ShowRingsのプロパティは保管しないように方針変更。
    //詳しくはAspectCombinationPaneのコメントを参照せよ。
    
    public void setPreference(Preference pref) {
        setValue( pref.getProperty("AspectCombinations") );
        //setShowRings( pref.getProperty("ShowRings") );
    }
    
    /**
     * 7枚のチェックボックスパネルの状態をまとめて文字列で返す。
     * プロパティ保存用のメソッド。このメソッドの戻り値の文字列をsetValues()に
     * 与えると、設定情報がパネルに復元される。
     */
    private String getValues() {
        StringBuffer sb = new StringBuffer();
        for ( int j=0; j < combiBoxs.length; j++ ) {
            for ( int i=0; i<combiBoxs[j].length; i++ )
                sb.append( combiBoxs[j][i].isSelected() ? "1," : "0," );
        }
        for ( int j=0; j < targetBoxs.length; j++ ) {
            for ( int i=0; i < targetBoxs[j].length; i++ )
                sb.append( targetBoxs[j][i].isSelected() ? "1," : "0," );
        }
        //ラジオボタンは選択されているボタンのみを抽出
        for ( int j=0; j < listGroups.length; j++ )
            sb.append( listGroups[j].getSelection().getActionCommand() + "," );
        sb.deleteCharAt( sb.length() - 1 ); //末尾カンマを落とす
        //System.out.println("#getValues = " + sb.toString());
        return sb.toString();
    }
    
    /**
     * getValues()の戻り値でもある数字列情報に従って、このパネル内の各チェック
     * ボックスを設定する。nullや""や48個の要素がない文字列を与えると、
     * デフォルト値が設定される。
     */
    private void setValue( String values ) {
        if ( values == null || values.length() == 0 || 
                                               values.split(",").length != 37) {
            values = "0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,2,0,0";
            // 58  values = "0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,0,0,2,2,1,2";
        }
        String [] v = values.split(",");
        int k=0;
        for ( int j=0; j < combiBoxs.length; j++ ) {
            for ( int i=0; i < combiBoxs[j].length; i++,k++ ) {
                combiBoxs[j][i].setSelected(v[k].equals("1"));
                boxItems[j][i].setSelected(v[k].equals("1"));
            }
        }
        for ( int j=0; j < targetBoxs.length; j++ ) {
            for ( int i=0; i < targetBoxs[j].length; i++,k++ ) {
                targetBoxs[j][i].setSelected(v[k].equals("1"));
                targetBoxItems[j][i].setSelected(v[k].equals("1"));
            }
        }
        for ( int j=0; j < listRadioButtons.length; j++ ) {
            for ( int i=0; i < listRadioButtons[j].length; i++ ) {
                if ( listRadioButtons[j][i].getActionCommand().equals(v[k]) ) {
                     listRadioButtons[j][i].setSelected(true);
                     listRadioItems[j][i].setSelected(true);
                    break;
                }
            }
            k++;
        }
        setSelectedIndex(cardIndex);
    }
    //デフォルト値を決める文字列を求める
    public static void main( String [] args) {
        AspectCombinationPanel2 p = new AspectCombinationPanel2();
//        String v = "0,0,0,1,0,0,1,1,1,0,0,1,0,0,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,2,0,0,0";
//        p.setValue(v);
//        System.out.println(v);
        System.out.println(p.getValues());
        System.out.println(p.getValues().split(",").length );
    }
    
    /**
     * 現在選択されているチェックボックスパネルのアスペクトの組合を返す。
     * 値は15bitのフラグ式。
     * <pre>
     * bit0  N1       bit5  N1xN2       bit10  P2xT
     * bit1  N2       bit6  P1xP2       bit11  N1xP1
     * bit2  P1       bit7  N1xT        bit12  N1xP2
     * bit3  P2       bit8  N2xT        bit13  N2xP2
     * bit4  T        bit9  P1xT        bit14  N2xP1
     * </pre>
     */
    public int getAspectCombinations() {
        return aspectSwitch;
    }
    
    /**
     * 現在選択されているチェックボックスパネルに対応するコンビネーションメニューを返す。
     */
    public JMenu getAspectCombinationMenu() {
        return combiMenu;
    }
    
    /**
     * 現在選択されているチェックボックスパネルの選択天体アスペクト標的の値を返す。
     * 値は3bitで0-7までの値を取りうる。各bitの意味はb0=N,b1=P,b2=Tで、各bitが
     * 1なら表示を0なら非表示を表す。
     */
    public int getAspectTargets() {
        return targetSwitch;
    }
    
    /**
     * 現在選択されているチェックボックスパネルに対応するターゲットメニューを返す。
     */
    public JMenu getAspectTargetMenu() {
        return targetMenu;
    }
    
    /**
     * 選択されている天体リスト表示モードを返す。
     * @return 0ならNatalのリスト、1ならProgress、2ならトランジットのリストが
     * 選択中。
     */
    public int getBodyListMode() {
        return bodyListSwitch;
    }
    
    /**
     * 現在選択されている天体リスト表示ラジオボタンに対応するメニューを返す。
     */
    public JMenu getBodyListModeMenu() {
        return listMenu;
    }
    
    /**
     * 現在選択されているパネルの中の天体リストのラジオボタンを選択する。
     */
    public void setBodyListMode( int value ) {
        JRadioButton [] rbs = listRadioButtons[cardIndex];
        String ac = "" + value;
        for ( int i=0; i<rbs.length; i++ ) {
            if ( rbs[i].getActionCommand().equals(ac) ) {
                rbs[i].setSelected( true );
                listRadioItems[cardIndex][i].setSelected( true );
                break;
            }
        }
        setResult();
    }
    
    /**
     * 組合せ円選択メニューを返す。
     */
    public JMenu getRingMenu() {
        return ringMenu;
    }
    
    /**
     * 表示リングをセットする。
     * @param value 0-6の値で、0=NPT,1=N,2=P,3=T,4=NT,5=NP,6=PT
     */
    private void setShowRings(int value) {
        if ( value >= ringComboBox.getItemCount() ) value = 0;
        ringComboBox.setSelectedIndex(value);
        //これによりコンボボックスのイベントが発生し、ringComboBoxActionPerformed()
        //が実行され、その結果this.setSelectedIndex()がキックされる。
    }
    
    /**
     * 天体リング選択コンボボックスを選択する。
     * @param value コンボボックスのアイテム数までの数字0〜n
     */    
    public void setShowRings(String value) {
        if(value == null || value.length() == 0) value = "0";
        setShowRings( Integer.parseInt(value) );
    }
    
    /**
     * 選択されているパネル(コンビ＆ターゲット)のチェックボックスの値を
     * 結果返却用変数にセット。
     * aspectSwitch   表示アスペクトの組合せを入れたフラグ
     * targetSwitch   選択天体とどのグループ(N1,N2,P1,P2,T)の天体と
     *                アスペクト検出するかのフラグ。
     * bodyListSwitch どの天体リストを表示するか。
     *                フラグではなく番号式0〜4(N,P,T,N2,P2)
     */
    private void setResult() {
        aspectSwitch = 0;
        for ( int i=0; i < combiBoxs[ cardIndex ].length; i++ ) {
            if ( combiBoxs[ cardIndex ][i].isSelected() )
                aspectSwitch |= Integer.parseInt(
                    combiBoxs[ cardIndex ][i].getActionCommand() );
        }
        targetSwitch = 0;
        for ( int i=0; i< targetBoxs[cardIndex].length; i++ ) {
            if ( targetBoxs[ cardIndex ][i].isSelected() ) {
                targetSwitch |= Integer.parseInt(
                    targetBoxs[ cardIndex ][i].getActionCommand() );
            }
        }
        bodyListSwitch = Integer.parseInt(
                listGroups[ cardIndex ].getSelection().getActionCommand() );
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
        javax.swing.JLabel jLabel10;
        javax.swing.JLabel jLabel11;
        javax.swing.JLabel jLabel12;
        javax.swing.JLabel jLabel13;
        javax.swing.JLabel jLabel14;
        javax.swing.JLabel jLabel15;
        javax.swing.JLabel jLabel16;
        javax.swing.JLabel jLabel17;
        javax.swing.JLabel jLabel18;
        javax.swing.JLabel jLabel19;
        javax.swing.JLabel jLabel2;
        javax.swing.JLabel jLabel20;
        javax.swing.JLabel jLabel21;
        javax.swing.JLabel jLabel3;
        javax.swing.JLabel jLabel4;
        javax.swing.JLabel jLabel5;
        javax.swing.JLabel jLabel6;
        javax.swing.JLabel jLabel7;
        javax.swing.JLabel jLabel8;
        javax.swing.JLabel jLabel9;

        ringComboBox = new javax.swing.JComboBox();
        cardPanel = new javax.swing.JPanel();
        combiPanel0 = new javax.swing.JPanel();
        combiCheckBox1 = new javax.swing.JCheckBox();
        combiCheckBox2 = new javax.swing.JCheckBox();
        combiCheckBox3 = new javax.swing.JCheckBox();
        combiCheckBox4 = new javax.swing.JCheckBox();
        combiCheckBox5 = new javax.swing.JCheckBox();
        combiCheckBox6 = new javax.swing.JCheckBox();
        combiCheckBox7 = new javax.swing.JCheckBox();
        combiCheckBox8 = new javax.swing.JCheckBox();
        combiCheckBox9 = new javax.swing.JCheckBox();
        combiCheckBoxA = new javax.swing.JCheckBox();
        combiCheckBoxB = new javax.swing.JCheckBox();
        combiCheckBoxC = new javax.swing.JCheckBox();
        combiCheckBoxD = new javax.swing.JCheckBox();
        combiCheckBoxE = new javax.swing.JCheckBox();
        combiCheckBoxF = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        listRadioButton1 = new javax.swing.JRadioButton();
        listRadioButton2 = new javax.swing.JRadioButton();
        listRadioButton3 = new javax.swing.JRadioButton();
        listRadioButton4 = new javax.swing.JRadioButton();
        listRadioButton5 = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        targetCheckBox1 = new javax.swing.JCheckBox();
        targetCheckBox2 = new javax.swing.JCheckBox();
        targetCheckBox3 = new javax.swing.JCheckBox();
        targetCheckBox4 = new javax.swing.JCheckBox();
        targetCheckBox5 = new javax.swing.JCheckBox();
        combiPanel1 = new javax.swing.JPanel();
        combiCheckBox11 = new javax.swing.JCheckBox();
        combiCheckBox12 = new javax.swing.JCheckBox();
        combiCheckBox13 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        targetCheckBox11 = new javax.swing.JCheckBox();
        targetCheckBox12 = new javax.swing.JCheckBox();
        listRadioButton11 = new javax.swing.JRadioButton();
        listRadioButton12 = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        combiPanel2 = new javax.swing.JPanel();
        combiCheckBox21 = new javax.swing.JCheckBox();
        combiCheckBox22 = new javax.swing.JCheckBox();
        combiCheckBox23 = new javax.swing.JCheckBox();
        combiCheckBox24 = new javax.swing.JCheckBox();
        combiCheckBox25 = new javax.swing.JCheckBox();
        combiCheckBox26 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        targetCheckBox21 = new javax.swing.JCheckBox();
        targetCheckBox22 = new javax.swing.JCheckBox();
        targetCheckBox23 = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        listRadioButton21 = new javax.swing.JRadioButton();
        listRadioButton22 = new javax.swing.JRadioButton();
        listRadioButton23 = new javax.swing.JRadioButton();
        combiPanel3 = new javax.swing.JPanel();
        combiCheckBox31 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        targetCheckBox31 = new javax.swing.JCheckBox();
        listRadioButton31 = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        combiPanel4 = new javax.swing.JPanel();
        combiCheckBox41 = new javax.swing.JCheckBox();
        combiCheckBox42 = new javax.swing.JCheckBox();
        combiCheckBox43 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        targetCheckBox41 = new javax.swing.JCheckBox();
        targetCheckBox42 = new javax.swing.JCheckBox();
        listRadioButton41 = new javax.swing.JRadioButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        listRadioButton42 = new javax.swing.JRadioButton();
        combiPanel5 = new javax.swing.JPanel();
        combiCheckBox51 = new javax.swing.JCheckBox();
        combiCheckBox52 = new javax.swing.JCheckBox();
        combiCheckBox53 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        targetCheckBox51 = new javax.swing.JCheckBox();
        targetCheckBox52 = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        listRadioButton51 = new javax.swing.JRadioButton();
        listRadioButton52 = new javax.swing.JRadioButton();
        jLabel19 = new javax.swing.JLabel();
        combiPanel6 = new javax.swing.JPanel();
        combiCheckBox61 = new javax.swing.JCheckBox();
        combiCheckBox62 = new javax.swing.JCheckBox();
        combiCheckBox63 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        targetCheckBox61 = new javax.swing.JCheckBox();
        targetCheckBox62 = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        listRadioButton61 = new javax.swing.JRadioButton();
        listRadioButton62 = new javax.swing.JRadioButton();
        jLabel21 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        ringComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\uff2e\uff30\uff34\u76f8\u6027\u5186", "\u76f8\u6027\u5186", "\uff34\u76f8\u6027\u5186" }));
        ringComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ringComboBoxActionPerformed(evt);
            }
        });

        add(ringComboBox, new java.awt.GridBagConstraints());

        cardPanel.setLayout(new java.awt.CardLayout());

        cardPanel.setPreferredSize(new java.awt.Dimension(260, 250));
        combiPanel0.setLayout(new java.awt.GridBagLayout());

        combiPanel0.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox1.setText("N1");
        combiCheckBox1.setActionCommand("1");
        combiCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox1, gridBagConstraints);

        combiCheckBox2.setText("N2");
        combiCheckBox2.setActionCommand("2");
        combiCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox2, gridBagConstraints);

        combiCheckBox3.setText("P1");
        combiCheckBox3.setActionCommand("4");
        combiCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox3, gridBagConstraints);

        combiCheckBox4.setText("P2");
        combiCheckBox4.setActionCommand("8");
        combiCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox4, gridBagConstraints);

        combiCheckBox5.setText("T");
        combiCheckBox5.setActionCommand("16");
        combiCheckBox5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox5, gridBagConstraints);

        combiCheckBox6.setText("N1\u00d7N2");
        combiCheckBox6.setActionCommand("32");
        combiCheckBox6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox6, gridBagConstraints);

        combiCheckBox7.setText("P1\u00d7P2");
        combiCheckBox7.setActionCommand("64");
        combiCheckBox7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox7, gridBagConstraints);

        combiCheckBox8.setSelected(true);
        combiCheckBox8.setText("N1\u00d7T");
        combiCheckBox8.setActionCommand("128");
        combiCheckBox8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox8, gridBagConstraints);

        combiCheckBox9.setSelected(true);
        combiCheckBox9.setText("N2\u00d7T");
        combiCheckBox9.setActionCommand("256");
        combiCheckBox9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBox9, gridBagConstraints);

        combiCheckBoxA.setText("P1\u00d7T");
        combiCheckBoxA.setActionCommand("512");
        combiCheckBoxA.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxA.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxA, gridBagConstraints);

        combiCheckBoxB.setText("P2\u00d7T");
        combiCheckBoxB.setActionCommand("1024");
        combiCheckBoxB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxB, gridBagConstraints);

        combiCheckBoxC.setText("N1\u00d7P1");
        combiCheckBoxC.setActionCommand("2048");
        combiCheckBoxC.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxC.setMargin(new java.awt.Insets(0, 0, 0, 0));
        combiCheckBoxC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combiCheckBoxCActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxC, gridBagConstraints);

        combiCheckBoxD.setText("N1\u00d7P2");
        combiCheckBoxD.setActionCommand("4096");
        combiCheckBoxD.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxD.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxD, gridBagConstraints);

        combiCheckBoxE.setText("N2\u00d7P2");
        combiCheckBoxE.setActionCommand("8192");
        combiCheckBoxE.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxE.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxE, gridBagConstraints);

        combiCheckBoxF.setText("N2\u00d7P1");
        combiCheckBoxF.setActionCommand("16384");
        combiCheckBoxF.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBoxF.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(combiCheckBoxF, gridBagConstraints);

        jLabel9.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel0.add(jLabel9, gridBagConstraints);

        jLabel1.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel0.add(jLabel1, gridBagConstraints);

        jLabel8.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel0.add(jLabel8, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        listRadioButton1.setText("\u30cd\u30a4\u30bf\u30eb1");
        listRadioButton1.setActionCommand("0");
        listRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel1.add(listRadioButton1, gridBagConstraints);

        listRadioButton2.setText("\u30d7\u30ed\u30b0\u30ec\u30b91");
        listRadioButton2.setActionCommand("1");
        listRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel1.add(listRadioButton2, gridBagConstraints);

        listRadioButton3.setSelected(true);
        listRadioButton3.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        listRadioButton3.setActionCommand("2");
        listRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel1.add(listRadioButton3, gridBagConstraints);

        listRadioButton4.setText("\u30cd\u30a4\u30bf\u30eb2");
        listRadioButton4.setActionCommand("3");
        listRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel1.add(listRadioButton4, gridBagConstraints);

        listRadioButton5.setText("\u30d7\u30ed\u30b0\u30ec\u30b92");
        listRadioButton5.setActionCommand("4");
        listRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel1.add(listRadioButton5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        combiPanel0.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        targetCheckBox1.setSelected(true);
        targetCheckBox1.setText("\u30cd\u30a4\u30bf\u30eb1");
        targetCheckBox1.setActionCommand("1");
        targetCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel2.add(targetCheckBox1, gridBagConstraints);

        targetCheckBox2.setSelected(true);
        targetCheckBox2.setText("\u30d7\u30ed\u30b0\u30ec\u30b91");
        targetCheckBox2.setActionCommand("2");
        targetCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel2.add(targetCheckBox2, gridBagConstraints);

        targetCheckBox3.setSelected(true);
        targetCheckBox3.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        targetCheckBox3.setActionCommand("4");
        targetCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel2.add(targetCheckBox3, gridBagConstraints);

        targetCheckBox4.setSelected(true);
        targetCheckBox4.setText("\u30cd\u30a4\u30bf\u30eb2");
        targetCheckBox4.setActionCommand("8");
        targetCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel2.add(targetCheckBox4, gridBagConstraints);

        targetCheckBox5.setSelected(true);
        targetCheckBox5.setText("\u30d7\u30ed\u30b0\u30ec\u30b92");
        targetCheckBox5.setActionCommand("16");
        targetCheckBox5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel2.add(targetCheckBox5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        combiPanel0.add(jPanel2, gridBagConstraints);

        cardPanel.add(combiPanel0, "card0");

        combiPanel1.setLayout(new java.awt.GridBagLayout());

        combiPanel1.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox11.setText("\u30cd\u30a4\u30bf\u30eb1");
        combiCheckBox11.setActionCommand("1");
        combiCheckBox11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(combiCheckBox11, gridBagConstraints);

        combiCheckBox12.setText("\u30cd\u30a4\u30bf\u30eb2");
        combiCheckBox12.setActionCommand("2");
        combiCheckBox12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(combiCheckBox12, gridBagConstraints);

        combiCheckBox13.setSelected(true);
        combiCheckBox13.setText("\u30cd\u30a4\u30bf\u30eb1\u00d7\u30cd\u30a4\u30bf\u30eb2");
        combiCheckBox13.setActionCommand("32");
        combiCheckBox13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(combiCheckBox13, gridBagConstraints);

        jLabel2.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel1.add(jLabel2, gridBagConstraints);

        targetCheckBox11.setSelected(true);
        targetCheckBox11.setText("\u30cd\u30a4\u30bf\u30eb1");
        targetCheckBox11.setActionCommand("1");
        targetCheckBox11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(targetCheckBox11, gridBagConstraints);

        targetCheckBox12.setSelected(true);
        targetCheckBox12.setText("\u30cd\u30a4\u30bf\u30eb2");
        targetCheckBox12.setActionCommand("8");
        targetCheckBox12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(targetCheckBox12, gridBagConstraints);

        listRadioButton11.setSelected(true);
        listRadioButton11.setText("\u30cd\u30a4\u30bf\u30eb1");
        listRadioButton11.setActionCommand("0");
        listRadioButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(listRadioButton11, gridBagConstraints);

        listRadioButton12.setText("\u30cd\u30a4\u30bf\u30eb2");
        listRadioButton12.setActionCommand("3");
        listRadioButton12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel1.add(listRadioButton12, gridBagConstraints);

        jLabel10.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel1.add(jLabel10, gridBagConstraints);

        jLabel11.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel1.add(jLabel11, gridBagConstraints);

        cardPanel.add(combiPanel1, "card1");

        combiPanel2.setLayout(new java.awt.GridBagLayout());

        combiPanel2.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox21.setText("\u30cd\u30a4\u30bf\u30eb1");
        combiCheckBox21.setActionCommand("1");
        combiCheckBox21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox21, gridBagConstraints);

        combiCheckBox22.setText("\u30cd\u30a4\u30bf\u30eb2");
        combiCheckBox22.setActionCommand("2");
        combiCheckBox22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox22.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox22, gridBagConstraints);

        combiCheckBox23.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox23.setActionCommand("16");
        combiCheckBox23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox23, gridBagConstraints);

        combiCheckBox24.setSelected(true);
        combiCheckBox24.setText("\u30cd\u30a4\u30bf\u30eb1\u00d7\u30cd\u30a4\u30bf\u30eb2");
        combiCheckBox24.setActionCommand("32");
        combiCheckBox24.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox24.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox24, gridBagConstraints);

        combiCheckBox25.setSelected(true);
        combiCheckBox25.setText("\u30cd\u30a4\u30bf\u30eb1\u00d7\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox25.setActionCommand("128");
        combiCheckBox25.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox25.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox25, gridBagConstraints);

        combiCheckBox26.setSelected(true);
        combiCheckBox26.setText("\u30cd\u30a4\u30bf\u30eb2\u00d7\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox26.setActionCommand("256");
        combiCheckBox26.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox26.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(combiCheckBox26, gridBagConstraints);

        jLabel3.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel2.add(jLabel3, gridBagConstraints);

        targetCheckBox21.setSelected(true);
        targetCheckBox21.setText("\u30cd\u30a4\u30bf\u30eb1");
        targetCheckBox21.setActionCommand("1");
        targetCheckBox21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(targetCheckBox21, gridBagConstraints);

        targetCheckBox22.setSelected(true);
        targetCheckBox22.setText("\u30cd\u30a4\u30bf\u30eb2");
        targetCheckBox22.setActionCommand("16");
        targetCheckBox22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox22.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(targetCheckBox22, gridBagConstraints);

        targetCheckBox23.setSelected(true);
        targetCheckBox23.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        targetCheckBox23.setActionCommand("4");
        targetCheckBox23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(targetCheckBox23, gridBagConstraints);

        jLabel12.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel2.add(jLabel12, gridBagConstraints);

        jLabel13.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel2.add(jLabel13, gridBagConstraints);

        listRadioButton21.setSelected(true);
        listRadioButton21.setText("\u30cd\u30a4\u30bf\u30eb1");
        listRadioButton21.setActionCommand("0");
        listRadioButton21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(listRadioButton21, gridBagConstraints);

        listRadioButton22.setText("\u30cd\u30a4\u30bf\u30eb2");
        listRadioButton22.setActionCommand("3");
        listRadioButton22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton22.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(listRadioButton22, gridBagConstraints);

        listRadioButton23.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        listRadioButton23.setActionCommand("2");
        listRadioButton23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel2.add(listRadioButton23, gridBagConstraints);

        cardPanel.add(combiPanel2, "card2");

        combiPanel3.setLayout(new java.awt.GridBagLayout());

        combiPanel3.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox31.setSelected(true);
        combiCheckBox31.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox31.setActionCommand("4");
        combiCheckBox31.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox31.setEnabled(false);
        combiCheckBox31.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel3.add(combiCheckBox31, gridBagConstraints);

        jLabel4.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel3.add(jLabel4, gridBagConstraints);

        targetCheckBox31.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        targetCheckBox31.setActionCommand("4");
        targetCheckBox31.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox31.setEnabled(false);
        targetCheckBox31.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel3.add(targetCheckBox31, gridBagConstraints);

        listRadioButton31.setSelected(true);
        listRadioButton31.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        listRadioButton31.setActionCommand("2");
        listRadioButton31.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton31.setEnabled(false);
        listRadioButton31.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel3.add(listRadioButton31, gridBagConstraints);

        jLabel14.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel3.add(jLabel14, gridBagConstraints);

        jLabel15.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel3.add(jLabel15, gridBagConstraints);

        cardPanel.add(combiPanel3, "card3");

        combiPanel4.setLayout(new java.awt.GridBagLayout());

        combiPanel4.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox41.setText("\u30cd\u30a4\u30bf\u30eb");
        combiCheckBox41.setActionCommand("1");
        combiCheckBox41.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox41.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(combiCheckBox41, gridBagConstraints);

        combiCheckBox42.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox42.setActionCommand("4");
        combiCheckBox42.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox42.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(combiCheckBox42, gridBagConstraints);

        combiCheckBox43.setSelected(true);
        combiCheckBox43.setText("\u30cd\u30a4\u30bf\u30eb\u00d7\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox43.setActionCommand("8");
        combiCheckBox43.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox43.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(combiCheckBox43, gridBagConstraints);

        jLabel5.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel4.add(jLabel5, gridBagConstraints);

        targetCheckBox41.setSelected(true);
        targetCheckBox41.setText("\u30cd\u30a4\u30bf\u30eb");
        targetCheckBox41.setActionCommand("1");
        targetCheckBox41.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox41.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(targetCheckBox41, gridBagConstraints);

        targetCheckBox42.setSelected(true);
        targetCheckBox42.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        targetCheckBox42.setActionCommand("4");
        targetCheckBox42.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox42.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(targetCheckBox42, gridBagConstraints);

        listRadioButton41.setText("\u30cd\u30a4\u30bf\u30eb");
        listRadioButton41.setActionCommand("0");
        listRadioButton41.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton41.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(listRadioButton41, gridBagConstraints);

        jLabel16.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel4.add(jLabel16, gridBagConstraints);

        jLabel17.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel4.add(jLabel17, gridBagConstraints);

        listRadioButton42.setSelected(true);
        listRadioButton42.setText("\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8");
        listRadioButton42.setActionCommand("2");
        listRadioButton42.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton42.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel4.add(listRadioButton42, gridBagConstraints);

        cardPanel.add(combiPanel4, "card4");

        combiPanel5.setLayout(new java.awt.GridBagLayout());

        combiPanel5.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox51.setText("\u30cd\u30a4\u30bf\u30eb");
        combiCheckBox51.setActionCommand("1");
        combiCheckBox51.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox51.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(combiCheckBox51, gridBagConstraints);

        combiCheckBox52.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        combiCheckBox52.setActionCommand("2");
        combiCheckBox52.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox52.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(combiCheckBox52, gridBagConstraints);

        combiCheckBox53.setSelected(true);
        combiCheckBox53.setText("\u30cd\u30a4\u30bf\u30eb\u00d7\u30d7\u30ed\u30b0\u30ec\u30b9");
        combiCheckBox53.setActionCommand("16");
        combiCheckBox53.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox53.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(combiCheckBox53, gridBagConstraints);

        jLabel6.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel5.add(jLabel6, gridBagConstraints);

        targetCheckBox51.setSelected(true);
        targetCheckBox51.setText("\u30cd\u30a4\u30bf\u30eb");
        targetCheckBox51.setActionCommand("1");
        targetCheckBox51.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox51.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(targetCheckBox51, gridBagConstraints);

        targetCheckBox52.setSelected(true);
        targetCheckBox52.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        targetCheckBox52.setActionCommand("2");
        targetCheckBox52.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox52.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(targetCheckBox52, gridBagConstraints);

        jLabel18.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel5.add(jLabel18, gridBagConstraints);

        listRadioButton51.setText("\u30cd\u30a4\u30bf\u30eb");
        listRadioButton51.setActionCommand("0");
        listRadioButton51.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton51.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(listRadioButton51, gridBagConstraints);

        listRadioButton52.setSelected(true);
        listRadioButton52.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        listRadioButton52.setActionCommand("1");
        listRadioButton52.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton52.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel5.add(listRadioButton52, gridBagConstraints);

        jLabel19.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel5.add(jLabel19, gridBagConstraints);

        cardPanel.add(combiPanel5, "card5");

        combiPanel6.setLayout(new java.awt.GridBagLayout());

        combiPanel6.setPreferredSize(new java.awt.Dimension(190, 240));
        combiCheckBox61.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        combiCheckBox61.setActionCommand("2");
        combiCheckBox61.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox61.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(combiCheckBox61, gridBagConstraints);

        combiCheckBox62.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox62.setActionCommand("4");
        combiCheckBox62.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox62.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(combiCheckBox62, gridBagConstraints);

        combiCheckBox63.setText("\u30d7\u30ed\u30b0\u30ec\u30b9\u00d7\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        combiCheckBox63.setActionCommand("32");
        combiCheckBox63.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        combiCheckBox63.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(combiCheckBox63, gridBagConstraints);

        jLabel7.setText("\u9078\u629e\u5929\u4f53\u306e\u30a2\u30b9\u30da\u30af\u30c8\u6a19\u7684");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel6.add(jLabel7, gridBagConstraints);

        targetCheckBox61.setSelected(true);
        targetCheckBox61.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        targetCheckBox61.setActionCommand("2");
        targetCheckBox61.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox61.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(targetCheckBox61, gridBagConstraints);

        targetCheckBox62.setSelected(true);
        targetCheckBox62.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        targetCheckBox62.setActionCommand("4");
        targetCheckBox62.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        targetCheckBox62.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(targetCheckBox62, gridBagConstraints);

        jLabel20.setText("\u5929\u4f53\u30ea\u30b9\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel6.add(jLabel20, gridBagConstraints);

        listRadioButton61.setText("\u30d7\u30ed\u30b0\u30ec\u30b9");
        listRadioButton61.setActionCommand("1");
        listRadioButton61.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton61.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(listRadioButton61, gridBagConstraints);

        listRadioButton62.setSelected(true);
        listRadioButton62.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8");
        listRadioButton62.setActionCommand("2");
        listRadioButton62.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listRadioButton62.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 1, 0);
        combiPanel6.add(listRadioButton62, gridBagConstraints);

        jLabel21.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        combiPanel6.add(jLabel21, gridBagConstraints);

        cardPanel.add(combiPanel6, "card6");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(cardPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void combiCheckBoxCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combiCheckBoxCActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_combiCheckBoxCActionPerformed
  
  private void ringComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ringComboBoxActionPerformed
      setSelectedIndex(ringComboBox.getSelectedIndex());
  }//GEN-LAST:event_ringComboBoxActionPerformed
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardPanel;
    private javax.swing.JCheckBox combiCheckBox1;
    private javax.swing.JCheckBox combiCheckBox11;
    private javax.swing.JCheckBox combiCheckBox12;
    private javax.swing.JCheckBox combiCheckBox13;
    private javax.swing.JCheckBox combiCheckBox2;
    private javax.swing.JCheckBox combiCheckBox21;
    private javax.swing.JCheckBox combiCheckBox22;
    private javax.swing.JCheckBox combiCheckBox23;
    private javax.swing.JCheckBox combiCheckBox24;
    private javax.swing.JCheckBox combiCheckBox25;
    private javax.swing.JCheckBox combiCheckBox26;
    private javax.swing.JCheckBox combiCheckBox3;
    private javax.swing.JCheckBox combiCheckBox31;
    private javax.swing.JCheckBox combiCheckBox4;
    private javax.swing.JCheckBox combiCheckBox41;
    private javax.swing.JCheckBox combiCheckBox42;
    private javax.swing.JCheckBox combiCheckBox43;
    private javax.swing.JCheckBox combiCheckBox5;
    private javax.swing.JCheckBox combiCheckBox51;
    private javax.swing.JCheckBox combiCheckBox52;
    private javax.swing.JCheckBox combiCheckBox53;
    private javax.swing.JCheckBox combiCheckBox6;
    private javax.swing.JCheckBox combiCheckBox61;
    private javax.swing.JCheckBox combiCheckBox62;
    private javax.swing.JCheckBox combiCheckBox63;
    private javax.swing.JCheckBox combiCheckBox7;
    private javax.swing.JCheckBox combiCheckBox8;
    private javax.swing.JCheckBox combiCheckBox9;
    private javax.swing.JCheckBox combiCheckBoxA;
    private javax.swing.JCheckBox combiCheckBoxB;
    private javax.swing.JCheckBox combiCheckBoxC;
    private javax.swing.JCheckBox combiCheckBoxD;
    private javax.swing.JCheckBox combiCheckBoxE;
    private javax.swing.JCheckBox combiCheckBoxF;
    private javax.swing.JPanel combiPanel0;
    private javax.swing.JPanel combiPanel1;
    private javax.swing.JPanel combiPanel2;
    private javax.swing.JPanel combiPanel3;
    private javax.swing.JPanel combiPanel4;
    private javax.swing.JPanel combiPanel5;
    private javax.swing.JPanel combiPanel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton listRadioButton1;
    private javax.swing.JRadioButton listRadioButton11;
    private javax.swing.JRadioButton listRadioButton12;
    private javax.swing.JRadioButton listRadioButton2;
    private javax.swing.JRadioButton listRadioButton21;
    private javax.swing.JRadioButton listRadioButton22;
    private javax.swing.JRadioButton listRadioButton23;
    private javax.swing.JRadioButton listRadioButton3;
    private javax.swing.JRadioButton listRadioButton31;
    private javax.swing.JRadioButton listRadioButton4;
    private javax.swing.JRadioButton listRadioButton41;
    private javax.swing.JRadioButton listRadioButton42;
    private javax.swing.JRadioButton listRadioButton5;
    private javax.swing.JRadioButton listRadioButton51;
    private javax.swing.JRadioButton listRadioButton52;
    private javax.swing.JRadioButton listRadioButton61;
    private javax.swing.JRadioButton listRadioButton62;
    private javax.swing.JComboBox ringComboBox;
    private javax.swing.JCheckBox targetCheckBox1;
    private javax.swing.JCheckBox targetCheckBox11;
    private javax.swing.JCheckBox targetCheckBox12;
    private javax.swing.JCheckBox targetCheckBox2;
    private javax.swing.JCheckBox targetCheckBox21;
    private javax.swing.JCheckBox targetCheckBox22;
    private javax.swing.JCheckBox targetCheckBox23;
    private javax.swing.JCheckBox targetCheckBox3;
    private javax.swing.JCheckBox targetCheckBox31;
    private javax.swing.JCheckBox targetCheckBox4;
    private javax.swing.JCheckBox targetCheckBox41;
    private javax.swing.JCheckBox targetCheckBox42;
    private javax.swing.JCheckBox targetCheckBox5;
    private javax.swing.JCheckBox targetCheckBox51;
    private javax.swing.JCheckBox targetCheckBox52;
    private javax.swing.JCheckBox targetCheckBox61;
    private javax.swing.JCheckBox targetCheckBox62;
    // End of variables declaration//GEN-END:variables
  
}
