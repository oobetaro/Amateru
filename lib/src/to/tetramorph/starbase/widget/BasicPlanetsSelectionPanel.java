/*
 * BasicPlanetsSelectionPanel.java
 *
 * Created on 2008/10/27, 12:06
 */

package to.tetramorph.starbase.widget;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import to.tetramorph.starbase.lib.Const;

/**
 * レポートプラグイン用に用意した天体選択パネル。
 * モダンと古典の二つの方式を選択できる。
 * 小惑星や実体をもたない感受点の選択ができる。
 * @author 大澤義鷹
 */
public class BasicPlanetsSelectionPanel extends javax.swing.JPanel {
    protected PlanetsSelectionPanel slavePanel;

    Map<Integer,PlanetToggleButton> buttonMap 
                                   = new HashMap<Integer,PlanetToggleButton>();
    
    int [] bodys;
    
    /** 
     * Creates new form BasicPlanetsSelectionPanel 
     */
    public BasicPlanetsSelectionPanel() {
        initComponents();
        initButtons();
    }
    
    /**
     * アスペクト検出天体を指定するPlanetsSelectionPanelと同期させたい場合、
     * そのパネルを登録しておくと、このパネルで非選択となったボタンと同じボタン
     * がdisenabledになる。
     */
    public void setSlavePanel( PlanetsSelectionPanel slavePanel ) {
        this.slavePanel = slavePanel;
    }    

    public void reset() {
        setSelected( MODERN_IDs, true );
        makeBodyArray();
        slavePanel.makeBodyArray();
    }
    
    private void initButtons() {
        //配列の要素番号 == 天体ID
        buttonMap.put( Const.CERES,       planetToggleButton21 );
        buttonMap.put( Const.PALLAS,      planetToggleButton22 );
        buttonMap.put( Const.JUNO,        planetToggleButton23 );
        buttonMap.put( Const.VESTA,       planetToggleButton24 );
        buttonMap.put( Const.CHIRON,      planetToggleButton25 );
        buttonMap.put( Const.PHOLUS,      planetToggleButton26 );
        //buttonMap.put( Const.NODE,        planetToggleButton13 );
        buttonMap.put( Const.APOGEE,      planetToggleButton14 );
        buttonMap.put( Const.VERTEX,      planetToggleButton15 );
        buttonMap.put( Const.DC,          planetToggleButton16 );
        buttonMap.put( Const.IC,          planetToggleButton17 );
        buttonMap.put( Const.SOUTH_NODE,  planetToggleButton18 );
        buttonMap.put( Const.ANTI_APOGEE, planetToggleButton19 );
        buttonMap.put( Const.ANTI_VERTEX, planetToggleButton20 );
        ButtonAction ba = new ButtonAction();
        for ( Iterator ite = buttonMap.keySet().iterator(); ite.hasNext(); ) {
            int i = (Integer)ite.next();
            PlanetToggleButton button = buttonMap.get(i);
            button.setActionCommand( "" + i );
            button.addActionListener( ba );
        }
        systemRadioButton1.addActionListener( ba );
        systemRadioButton2.addActionListener( ba );
    }
    private static final int [] MODERN_IDs = {
        0,1,2,3,4,5,6,7,8,9,30,32
    };
    private static final int [] CLASSIC_IDs = {
        0,1,2,3,4,5,6,30,32
    };
    
    private class ButtonAction extends AbstractAction {
        public void actionPerformed( ActionEvent evt ) {
            makeBodyArray();

            if ( evt.getSource() == systemRadioButton1 ||
                 evt.getSource() == systemRadioButton2 ) {
                if ( slavePanel == null ) return;
                setSelected( MODERN_IDs, false );
                if ( isModernSystem() ) {
                    setSelected( MODERN_IDs, true );
                }  else {
                    setSelected( CLASSIC_IDs, true );
                }
                slavePanel.makeBodyArray();
            } else {
                if ( slavePanel != null ) {
                    PlanetToggleButton button = (PlanetToggleButton)evt.getSource();
                    int n = Integer.parseInt( button.getActionCommand() );
                    slavePanel.buttons[n].setEnabled( button.isSelected() );
                    slavePanel.buttons[n].setSelected( button.isSelected() );
                    slavePanel.makeBodyArray();
                } 
            }
        }
    }
        //ボタンを押すたびにbodys配列を作り替える仕組みになっているが、
        //setSelected()では作り替えﾒｿｯﾄﾞがｷｯｸされないため、明示的にそのﾒｿｯﾄﾞ
        //を実行してやる。
    /**
     * 点灯しているスイッチの天体番号を文字列表現で返す。
     * Disenabled状態にあるボタンは消灯とみなす。
     * @return "15,16,17"などとカンマで区切られた数字の列挙を返す。
     */
    public String getSelected() {
        StringBuilder sb = new StringBuilder( 30 );
        for ( Iterator<PlanetToggleButton> 
               ite = buttonMap.values().iterator();      ite.hasNext(); ) {
            PlanetToggleButton button = ite.next();
            if ( ! button.isEnabled() ) continue;
            if ( button.isSelected() ) {
                sb.append( button.getActionCommand() );
                sb.append(",");
            }
        }
        if ( sb.length() > 0 ) sb.deleteCharAt( sb.length() - 1 );
        return sb.toString();
    }
    
    /**
     * すべてのボタンを点灯／消灯させる。
     */
    private void selectAllButton( boolean b ) {
        
        for ( Iterator<PlanetToggleButton> 
               ite = buttonMap.values().iterator();     ite.hasNext(); ) {
            ite.next().setSelected( b );
        }
    }
    
    /**
     * 指定天体IDのボタンを選択/非選択にセットする。
     */
    private void setSelected( int id, boolean b ) {
        //10天体、ノード、AC、MCが指定されたときは無視
        if ( ( id >= 0 && id <= 10 ) || id == 30 || id== 32 ) {
            if ( slavePanel != null ) { //しかしスレーブパネルは別
                slavePanel.buttons[ id ].setEnabled( b );
                slavePanel.buttons[ id ].setSelected( b );
            }
            return;
        }
        PlanetToggleButton button = buttonMap.get(id);
        if ( button != null ) {
            button.setSelected( b );
            if ( slavePanel != null ) {
                slavePanel.buttons[ id ].setEnabled( b );
                slavePanel.buttons[ id ].setSelected( b );
            }
        }
    }
    
    public void setSelected( int [] array, boolean b ) {
        for ( int id : array ) setSelected( id, b );
    }
    /**
     * 指定されたトグルボタンを点灯させる。
     * @param values "15,16,17"などとカンマで区切られた天体番号の列挙を与える
     */
    public void setSelected( String values ) {
        //System.out.println("setSelected(" + values + ")");
        String defaultPlanets = isModernSystem() ?
            "0,1,2,3,4,5,6,7,8,9,10,30,32," : "0,1,2,3,4,5,6,10,30,32,";
        slaveAllDisenabled();
        selectAllButton( false ); //全SWを消灯
        String [] array = defaultPlanets.concat( values ).split(",");
        for ( int i = 0; i < array.length; i++ ) {
            int id = Integer.parseInt( array[i] );
            setSelected( id, true );
        }
        makeBodyArray();
    }
    
    private void slaveAllDisenabled() {
        if ( slavePanel == null ) return;
        for ( int i=0; i < slavePanel.buttons.length; i++ ) {
            if ( slavePanel.buttons[i] == null ) continue;
            slavePanel.buttons[i].setEnabled( false );
        }
    }
    
    //パネルがモダンか古典かに応じて、bodys[]に使用天体IDの配列を作成する。
    protected void makeBodyArray() {
        String defaultPlanets = isModernSystem() ?
            "0,1,2,3,4,5,6,7,8,9,10,30,32," : "0,1,2,3,4,5,6,10,30,32,";
        String [] temp = defaultPlanets.concat( getSelected() ).split(",");
        bodys = new int[ temp.length ];
        for ( int i = 0; i < temp.length; i++ ) {
            bodys[i] = Integer.parseInt( temp[i] );
        }
    }
    // makeBodyArray()によって作成されたbodys[]を返す。
    public int [] getSelectedBodyIDs() {
        return bodys;
    }
    
    public void setModernSystem( boolean b ) {
        if ( b )
            systemRadioButton1.doClick();
        else
            systemRadioButton2.doClick();
            
    }
    /**
     * モダン１０惑星式が選択されているときはtrueをかえす。
     */
    public boolean isModernSystem() {
        return systemRadioButton1.isSelected();
    }

    // getSelectedBodyIDs()は、プラグインが描画する際に頻繁に呼び出される
    // 可能性がたかい。毎回bodysを生成すると、リソースを消耗する。
    // だからボタンの状態が変更されたとき、つまり設定のときに作成しておく。
    // メソッド呼びだしのときはbodysの参照を返すだけ。
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel jPanel3;
        javax.swing.JPanel toggleButtonPanel;

        buttonGroup1 = new javax.swing.ButtonGroup();
        planetToggleButton13 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        jPanel3 = new javax.swing.JPanel();
        systemRadioButton1 = new javax.swing.JRadioButton();
        systemRadioButton2 = new javax.swing.JRadioButton();
        toggleButtonPanel = new javax.swing.JPanel();
        planetToggleButton14 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton15 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton16 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton17 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton18 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton19 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton20 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton21 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton22 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton23 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton24 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton25 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton26 = new to.tetramorph.starbase.widget.PlanetToggleButton();

        buttonGroup1.add( systemRadioButton1 );
        buttonGroup1.add( systemRadioButton2 );

        planetToggleButton13.setBodyID(10);

        setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        systemRadioButton1.setSelected(true);
        systemRadioButton1.setText("\u30e2\u30c0\u30f3\u5341\u60d1\u661f\u5f0f");
        systemRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        systemRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(systemRadioButton1, gridBagConstraints);

        systemRadioButton2.setText("\u53e4\u5178\u4e03\u60d1\u661f\u5f0f");
        systemRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        systemRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(systemRadioButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(jPanel3, gridBagConstraints);

        toggleButtonPanel.setLayout(new java.awt.GridBagLayout());

        planetToggleButton14.setSelected(false);
        planetToggleButton14.setBodyID(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton14, gridBagConstraints);

        planetToggleButton15.setSelected(false);
        planetToggleButton15.setBodyID(34);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton15, gridBagConstraints);

        planetToggleButton16.setSelected(false);
        planetToggleButton16.setBodyID(31);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton16, gridBagConstraints);

        planetToggleButton17.setSelected(false);
        planetToggleButton17.setBodyID(33);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton17, gridBagConstraints);

        planetToggleButton18.setSelected(false);
        planetToggleButton18.setBodyID(21);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton18, gridBagConstraints);

        planetToggleButton19.setSelected(false);
        planetToggleButton19.setBodyID(23);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton19, gridBagConstraints);

        planetToggleButton20.setSelected(false);
        planetToggleButton20.setBodyID(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(9, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton20, gridBagConstraints);

        planetToggleButton21.setSelected(false);
        planetToggleButton21.setBodyID(17);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton21, gridBagConstraints);

        planetToggleButton22.setSelected(false);
        planetToggleButton22.setBodyID(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton22, gridBagConstraints);

        planetToggleButton23.setSelected(false);
        planetToggleButton23.setBodyID(19);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton23, gridBagConstraints);

        planetToggleButton24.setSelected(false);
        planetToggleButton24.setBodyID(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton24, gridBagConstraints);

        planetToggleButton25.setSelected(false);
        planetToggleButton25.setBodyID(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton25, gridBagConstraints);

        planetToggleButton26.setSelected(false);
        planetToggleButton26.setBodyID(16);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        toggleButtonPanel.add(planetToggleButton26, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(toggleButtonPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton13;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton14;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton15;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton16;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton17;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton18;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton19;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton20;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton21;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton22;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton23;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton24;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton25;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton26;
    private javax.swing.JRadioButton systemRadioButton1;
    private javax.swing.JRadioButton systemRadioButton2;
    // End of variables declaration//GEN-END:variables
    
}
