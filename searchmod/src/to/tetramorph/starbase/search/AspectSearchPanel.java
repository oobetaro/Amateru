/*
 * AspectSearchPanel.java
 *
 * Created on 2006/07/23, 4:13
 */

package to.tetramorph.starbase.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.module.SearchModulePanel;
import to.tetramorph.starbase.widget.PlanetComboBox;
import to.tetramorph.util.Preference;
/**
 * アスペクト検索フォームパネル
 */
public class AspectSearchPanel extends SearchModulePanel {
    private Preference configPref;
    /** Creates new form AspectSearchPanel */
    public AspectSearchPanel() {
        initComponents();
        angleSpinner.setModel( new SpinnerNumberModel(0d,0d,180d,1d) );
        orbSpinner1.setModel( new SpinnerNumberModel(0d,0d,8d,0.5) );
        orbSpinner2.setModel( new SpinnerNumberModel(0d,0d,8d,0.5) );
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);
        radioButton1.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                radioButtonActionPerformed();
            }
        });
        radioButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                radioButtonActionPerformed();
            }
        });
        
    }
    public void init( Preference configPref ) {
        this.configPref = configPref;
    }
    // ラジオボタンでアスペクト指定の方法を切り替える
    private void radioButtonActionPerformed() {
        if ( radioButton1.isSelected() ) { //コンボボックス式
            aspectComboBox.setEnabled( true );
            angleSpinner.setEnabled( false );
            orbSpinner1.setEnabled( true );
            orbSpinner2.setEnabled( false );
        } else { //スピナー式
            aspectComboBox.setEnabled( false );
            angleSpinner.setEnabled( true );
            orbSpinner1.setEnabled( false );
            orbSpinner2.setEnabled( true );
        }
    }
    private int getSelectedBody( PlanetComboBox pcb ) {
        Integer planet = (Integer)pcb.getSelectedBody();
        if ( planet == Const.NODE 
             && configPref.getProperty("UseMeanNode","false").equals("false")) 
            planet++;
        else if ( planet == Const.APOGEE 
                   && configPref.getProperty("UseMeanApgee","false")
                      .equals("false")) planet++;
        return planet;
    }
    /**
     * 検索開始前のパラメター入力エラー検査
     */
    public boolean begin() {
        int p1 = getSelectedBody(planetComboBox1);
        int p2 = getSelectedBody(planetComboBox2);
        if ( p1 == p2 ) {
            JOptionPane.showMessageDialog(this,
                "同じ感受点は指定できません",
                "アスペクト検索のエラー",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Double orb;
        if ( radioButton1.isSelected() )
            orb = (Double)orbSpinner1.getValue();
        else
            orb = (Double)orbSpinner2.getValue();
        if ( orb.doubleValue() == 0d ) {
            JOptionPane.showMessageDialog(this,
                "オーブは0度以上でなければなりません",
                "アスペクト検索のエラー",
                JOptionPane.ERROR_MESSAGE );
            return false; //後から追加
        }
        return true;
    }
    /**
     * 検索開始
     */
    public SearchResult search( SearchOption option ) {
        int p1 = getSelectedBody(planetComboBox1);
        int p2 = getSelectedBody(planetComboBox2);
        double aspectAngle = 0d;
        double orb = 0d;
        if ( radioButton1.isSelected() ) {
            Integer i = (Integer)aspectComboBox.getSelectedItem();
            aspectAngle = Const.ASPECT_ANGLE[i];
            orb = (Double)orbSpinner1.getValue();
        } else {
            aspectAngle = (Double)angleSpinner.getValue();
            orb = (Double)orbSpinner2.getValue();
        }
//    System.out.println("p1 = " + Const.SYMBOL_NAMES[p1]);
//    System.out.println("p2 = " + Const.SYMBOL_NAMES[p2]);
//    System.out.println("aspect = " + aspectAngle);
//    System.out.println("orb = " + orb);
//    String planetName1 = Const.SYMBOL_NAMES[p1].toUpperCase();
//    String planetName2 = Const.SYMBOL_NAMES[p2].toUpperCase();
        //　惑星だけなら良いのだけど、ノードやリリスはミーンかトルーか判定して、
        // オフセット値を変更する必要がある。
        String planetName1 = PLANET_NAMES_EN[p1];
        String planetName2 = PLANET_NAMES_EN[p2];
        StringBuffer sb = new StringBuffer();
        sb.append( "SELECT TREEPATH.PATH,OCCASION.*,PLANETS_LONGITUDE." );
        sb.append( planetName1 );
        sb.append(",PLANETS_LONGITUDE.");
        sb.append( planetName2 );
        sb.append(" FROM OCCASION,PLANETS_LONGITUDE,TREEPATH");
        sb.append(" WHERE OCCASION.JDAY = PLANETS_LONGITUDE.JDAY AND ");
        sb.append(" TREEPATH.ID = OCCASION.ID AND ");
        sb.append(" ASPECT( ");
        sb.append( planetName1 );
        sb.append(",");
        sb.append( planetName2 );
        sb.append(",");
        sb.append( aspectAngle );
        sb.append(",");
        sb.append(orb);
        sb.append(")");
        sb.append(" AND " + option.getExpression());
        PreparedStatement ps = null;
        ResultSet rs = null;
        String aspName = "";
        if ( radioButton1.isSelected() ) {
            int aid = (Integer)aspectComboBox.getSelectedItem();
            aspName = Const.ASPECT_NAMES[ aid ];
        } else {
            aspName = aspectAngle + "度";
        }
        String title = String.format( "%s−%s %s",
                                      Const.PLANET_NAMES[ p1 ], 
                                      Const.PLANET_NAMES[ p2 ],
                                      aspName );
        String tabName = String.format( "%s-%s",
                                        Const.PLANET_NAMES[ p1 ], 
                                        Const.PLANET_NAMES[ p2 ] );
        try {
            Connection con = option.getConnection();
            ps = con.prepareStatement( sb.toString() );
            System.out.println("SQL: ");
            System.out.println( ps.toString() );
            rs = ps.executeQuery();
            List<Natal> list = new ArrayList<Natal>();
            while ( rs.next() ) {
                Natal o = new Natal();
                o.setParams(rs);
                o.setPath( rs.getString("PATH") );
                list.add(o);
            }
//            option.getResultReceiver().write( new SearchResult(
//                                                        list,
//                                                        tabName,
//                                                        title ,
//                                                        option.getCurrentPath(),
//                                                        toString()));
            return new SearchResult(list,
                                      tabName,
                                      title,
                                      option.getCurrentPath(),
                                      toString());
        } catch ( SQLException e ) {
            e.printStackTrace();
            System.out.println("SQL: ");
            System.out.println(ps.toString());
        } finally {
            try {
                rs.close();
                ps.close();
            } catch ( Exception e ) { }
        }
        return null;
    }
    /**
     * この検索パネルの機能名を返す。
     */
    public String toString() {
        return "アスペクト検索";
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JLabel spLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel spLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel orbLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel angleLabel2 = new javax.swing.JLabel();
        orbSpinner1 = new javax.swing.JSpinner();
        radioButton1 = new javax.swing.JRadioButton();
        radioButton2 = new javax.swing.JRadioButton();
        javax.swing.JLabel orbLabel1 = new javax.swing.JLabel();
        orbSpinner2 = new javax.swing.JSpinner();
        javax.swing.JLabel angleLabel1 = new javax.swing.JLabel();
        planetComboBox1 = new to.tetramorph.starbase.widget.PlanetComboBox();
        planetComboBox2 = new to.tetramorph.starbase.widget.PlanetComboBox();
        aspectComboBox = new to.tetramorph.starbase.widget.AspectComboBox();
        angleSpinner = new javax.swing.JSpinner();

        setLayout(new java.awt.GridBagLayout());

        spLabel1.setText("感受点1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(spLabel1, gridBagConstraints);

        spLabel2.setText("感受点2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(spLabel2, gridBagConstraints);

        orbLabel2.setText("オーブ");
        orbLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(orbLabel2, gridBagConstraints);

        angleLabel2.setText("度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(angleLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(orbSpinner1, gridBagConstraints);

        radioButton1.setSelected(true);
        radioButton1.setText("記号指定");
        radioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 3));
        radioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(radioButton1, gridBagConstraints);

        radioButton2.setText("角度指定");
        radioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(radioButton2, gridBagConstraints);

        orbLabel1.setText("オーブ");
        orbLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(orbLabel1, gridBagConstraints);

        orbSpinner2.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(orbSpinner2, gridBagConstraints);

        angleLabel1.setText("度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(angleLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(planetComboBox1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(planetComboBox2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(aspectComboBox, gridBagConstraints);

        angleSpinner.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(angleSpinner, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner angleSpinner;
    private to.tetramorph.starbase.widget.AspectComboBox aspectComboBox;
    private javax.swing.JSpinner orbSpinner1;
    private javax.swing.JSpinner orbSpinner2;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox1;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox2;
    private javax.swing.JRadioButton radioButton1;
    private javax.swing.JRadioButton radioButton2;
    // End of variables declaration//GEN-END:variables
  
}
