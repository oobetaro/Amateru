/*
 * ZodiacSearchPanel.java
 *
 * Created on 2006/07/22, 3:18
 */

package to.tetramorph.starbase.search;

import to.tetramorph.starbase.formatter.FormatterFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.formatter.AngleFormatter;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.module.SearchModulePanel;
import to.tetramorph.util.Preference;

/**
 * 黄道上の天体位置でデータを検索すめためのパネル。
 */
public class ZodiacSearchPanel extends SearchModulePanel {
    private Preference configPref;
    /** Creates new form ZodiacSearchPanel */
    public ZodiacSearchPanel() {
        initComponents();
        startFTextField.setFormatterFactory( new FormatterFactory(
            new AngleFormatter( 30.0, AngleFormatter.LESS_THAN)));
        orbFTextField.setFormatterFactory(new FormatterFactory(
            new AngleFormatter( 4.0, AngleFormatter.LESS_THAN)));
        startFTextField.setValue( new Double( 0.0 ) );
        orbFTextField.setValue( new Double( 0.5 ) );       
    }
    public void init( Preference configPref ) {
        this.configPref = configPref;
    }
    private int getSelectedBody() {
        Integer planet = (Integer)planetComboBox.getSelectedBody();
        if ( planet == Const.NODE && 
             configPref.getProperty("UseMeanNode","false").equals("false")) 
            planet++;
        else if ( planet == Const.APOGEE && 
                   configPref.getProperty("UseMeanApgee","false")
                   .equals("false")) 
            planet++;
        return planet;
    }
    
    public boolean begin() {
        return true;
    }
    
    public SearchResult search( SearchOption option ) {
        Integer sign1 = (Integer)zodiacComboBox.getSelectedItem();
        Integer planet = getSelectedBody();
        Double angle = (Double)startFTextField.getValue();
        Double orb = (Double)orbFTextField.getValue();
        
        double startAngle = angle + sign1 * 30 - orb;
        if(startAngle < 0) startAngle += 360.0;
        double endAngle = angle + sign1 * 30 + orb;
        if(endAngle >= 360.0) endAngle -= 360.0;
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
        if(startAngle > endAngle ) {
            sb.append("(( " + planetName + " >= ? AND " + planetName);
            sb.append(" < 360 ) OR " + planetName + " < ?)");
        } else {
            sb.append(planetName + " >= ? AND " + planetName + " < ?");
        }
        sb.append(" AND " + option.getExpression());
        String title = String.format( "%s %s%4.1f度 (オーブ±%3.1f)",
                                      Const.PLANET_NAMES[ planet ],
                                      Const.SIGN_NAMES[ sign1 ],
                                      angle, orb );
        String tabName = String.format( "%s %s%4.1f度",
                                        Const.PLANET_NAMES[ planet ],
                                        Const.SIGN_NAMES[ sign1 ],
                                        angle );
        try {
            //GuestDatabase db = GuestDatabase.getInstance();
            //Connection con = db.getConnection();
            Connection con = option.getConnection();
            PreparedStatement ps = con.prepareStatement(sb.toString());
            ps.setDouble(1,startAngle);
            ps.setDouble(2,endAngle);
            ResultSet rs = ps.executeQuery();
            System.out.println("SQL: ");
            System.out.println(ps.toString());
            List<Natal> list = new ArrayList<Natal>();
            while ( rs.next() ) {
                Natal o = new Natal();
                o.setParams(rs);
                o.setPath(rs.getString("PATH"));
                list.add(o);
            }
            rs.close();
            ps.close();
//            option.getResultReceiver().write( new SearchResult(
//                                                        list,
//                                                        tabName,
//                                                        title ,
//                                                        option.getCurrentPath(),
//                                                        toString()) );
            return new SearchResult(list,
                                      tabName,
                                      title,
                                      option.getCurrentPath(),
                                      toString());
        } catch( SQLException e ) {
            e.printStackTrace();
        }
        return null;
    }
    public String toString() {
        return "高精度獣帯検索";
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
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;

    jLabel1 = new javax.swing.JLabel();
    startFTextField = new javax.swing.JFormattedTextField();
    orbFTextField = new javax.swing.JFormattedTextField();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    orbLabel = new javax.swing.JLabel();
    zodiacComboBox = new to.tetramorph.starbase.widget.ZodiacComboBox();
    planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();

    setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("\u304c");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(jLabel1, gridBagConstraints);

    startFTextField.setText("0.0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.ipadx = 15;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(startFTextField, gridBagConstraints);

    orbFTextField.setText("0.0");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(orbFTextField, gridBagConstraints);

    jLabel2.setText("\u5ea6");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(jLabel2, gridBagConstraints);

    jLabel3.setText("\u5ea6");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(jLabel3, gridBagConstraints);

    orbLabel.setText("\u30aa\u30fc\u30d6\u00b1");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    add(orbLabel, gridBagConstraints);

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
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JFormattedTextField orbFTextField;
  private javax.swing.JLabel orbLabel;
  private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
  private javax.swing.JFormattedTextField startFTextField;
  private to.tetramorph.starbase.widget.ZodiacComboBox zodiacComboBox;
  // End of variables declaration//GEN-END:variables
  
}
