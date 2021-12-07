/*
 * ZodiacSearchPanel.java
 *
 * Created on 2006/07/22, 3:18
 */

package to.tetramorph.starbase.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.module.SearchModulePanel;
import to.tetramorph.util.Preference;
/**
 * どの天体が獣帯の何度にあるかでデータを検索すめためのパネル。
 * ZodiacSearchPanelと異なり、これは整数度数でデータを識別する。
 */
public class ZodiacIntegerSearchPanel extends SearchModulePanel {
    private Preference configPref;
    /** Creates new form ZodiacSearchPanel */
    public ZodiacIntegerSearchPanel() {
        initComponents();
        angleSpinner.setModel(new SpinnerNumberModel(0,0,29,1));
        orbSpinner.setModel(new SpinnerNumberModel(0,0,5,1));
    }
    public void init( Preference configPref ) {
        this.configPref = configPref;
    }
    public boolean begin() {
        return true;
    }
    private int getSelectedBody() {
        Integer planet = (Integer)planetComboBox.getSelectedBody();
        //System.out.println("Search : UseMeanApogee = " + configPref.getProperty("UseMeanApogee") );
        if ( planet == NODE &&
            configPref.getProperty("UseMeanNode","false").equals("false"))
            planet++;
        else if ( planet == APOGEE &&
            configPref.getProperty("UseMeanApogee","false").equals("false"))
            planet++;
        return planet;
    }
    
    public SearchResult search(SearchOption option) {
        Integer sign1 = (Integer)zodiacComboBox.getSelectedItem();
        Integer planet = getSelectedBody();
//    Integer planet = (Integer)planetComboBox.getSelectedItem();
//    //ノードまたはリリスの場合、タイプに応じて値を+1する
//    if(planet == NODE && Config.data.getProperty("UseMeanNode","false").equals("false")) planet++;
//    else if(planet == APOGEE && Config.data.getProperty("UseMeanApgee","false").equals("false")) planet++;
        int angle = (Integer)angleSpinner.getValue();
        int orb = (Integer)orbSpinner.getValue();
        int startAngle=0,endAngle = 0;
        if ( angleVoidCheckBox.isSelected() ) { //度数無視でサインのみ
            //startAngle = angle + sign1 * 30;
            startAngle = sign1 * 30;
            endAngle = startAngle + 30;
        } else {
            startAngle = angle + sign1 * 30 - orb;
            if ( startAngle < 0 ) startAngle += 360;
            endAngle = angle + sign1 * 30 + orb;
            if ( endAngle >= 360 ) endAngle -= 360;
            endAngle++;
        }
        //String planetName = Const.SYMBOL_NAMES[planet].toUpperCase();
        String planetName = Const.PLANET_NAMES_EN[planet];
        System.out.println("planetName = " + planetName);
        System.out.println("startAngle = " + startAngle);
        System.out.println("orbAngle = " + orb);
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT TREEPATH.PATH,OCCASION.*,PLANETS_LONGITUDE.");
        sb.append(planetName);
        sb.append(" FROM OCCASION,PLANETS_LONGITUDE,TREEPATH");
        sb.append(" WHERE OCCASION.JDAY = PLANETS_LONGITUDE.JDAY AND ");
        sb.append(" TREEPATH.ID = OCCASION.ID AND ");
        if ( startAngle > endAngle ) {
            sb.append("(( " + planetName + " >= ? AND " + planetName);
            sb.append(" < 360 ) OR " + planetName + " < ?)");
        } else {
            sb.append(planetName + " >= ? AND " + planetName + " < ?");
        }
        sb.append(" AND " + option.getExpression());
        String title = String.format( "%s %s",
                                      Const.PLANET_NAMES[ planet ],
                                      Const.SIGN_NAMES[ sign1 ] );
        String tabName = String.format( "%s %s",
                                        Const.PLANET_NAMES[ planet ],
                                        Const.SIGN_NAMES[ sign1 ] );
        if ( ! angleVoidCheckBox.isSelected() ) {
            tabName += String.format( "%d度",angle );
            title += String.format( "%d度",angle );
        } 
        try {
            Connection con = option.getConnection();
            PreparedStatement ps = con.prepareStatement(sb.toString());
            ps.setInt(1,startAngle);
            ps.setInt(2,endAngle);
            System.out.println("SQL: ");
            System.out.println(ps.toString());
            ResultSet rs = ps.executeQuery();
            List<Natal> list = new ArrayList<Natal>();
            while ( rs.next() ) {
                Natal o = new Natal();
                o.setParams( rs );
                o.setPath( rs.getString("PATH") );
                list.add( o );
            }
            rs.close();
            ps.close();
//            option.getResultReceiver().write( new SearchResult(
//                                                       list,
//                                                       tabName,
//                                                       title ,
//                                                       option.getCurrentPath(),
//                                                       toString()));
            return new SearchResult(list,
                                      tabName,
                                      title,
                                      option.getCurrentPath(),
                                      toString());
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
        return null;
    }
    //度数無視にしたときスピナー等不要なラベルをDisEnabledにする。
    void setAngleEnabled(boolean b) {
        angleSpinner.setEnabled(b);
        orbSpinner.setEnabled(b);
        orbLabel.setEnabled(b);
        angleLabel1.setEnabled(b);
        angleLabel2.setEnabled(b);
    }
    public String toString() {
        return "簡易獣帯検索";
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        angleLabel1 = new javax.swing.JLabel();
        angleLabel2 = new javax.swing.JLabel();
        orbLabel = new javax.swing.JLabel();
        angleSpinner = new javax.swing.JSpinner();
        orbSpinner = new javax.swing.JSpinner();
        angleVoidCheckBox = new javax.swing.JCheckBox();
        zodiacComboBox = new to.tetramorph.starbase.widget.ZodiacComboBox();
        planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("が");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jLabel1, gridBagConstraints);

        angleLabel1.setText("度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(angleLabel1, gridBagConstraints);

        angleLabel2.setText("度");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(angleLabel2, gridBagConstraints);

        orbLabel.setText("オーブ±");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(orbLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(angleSpinner, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(orbSpinner, gridBagConstraints);

        angleVoidCheckBox.setText("度数無視");
        angleVoidCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        angleVoidCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        angleVoidCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                angleVoidCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        add(angleVoidCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(zodiacComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(planetComboBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
  
  private void angleVoidCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_angleVoidCheckBoxActionPerformed
      setAngleEnabled(! angleVoidCheckBox.isSelected());
  }//GEN-LAST:event_angleVoidCheckBoxActionPerformed
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel angleLabel1;
    private javax.swing.JLabel angleLabel2;
    private javax.swing.JSpinner angleSpinner;
    private javax.swing.JCheckBox angleVoidCheckBox;
    private javax.swing.JLabel orbLabel;
    private javax.swing.JSpinner orbSpinner;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
    private to.tetramorph.starbase.widget.ZodiacComboBox zodiacComboBox;
    // End of variables declaration//GEN-END:variables
  
}
