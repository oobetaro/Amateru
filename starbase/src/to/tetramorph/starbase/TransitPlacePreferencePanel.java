/*
 * TransitPlacePreferencePanel.java
 *
 * Created on 2006/09/24, 9:31
 */
package to.tetramorph.starbase;

import to.tetramorph.starbase.lib.Place;

/**
 * トランジットにおける観測地を入力するためのパネル。
 * @author  大澤義鷹
 */
class TransitPlacePreferencePanel extends PreferencePanel {

    /** Creates new form TransitPlacePreferencePanel */
    public TransitPlacePreferencePanel() {
        initComponents();
        init();
    }

    private void init() {
        //Place p = PrefUtils.getPlace(pref, "DefaultTransitPlace");
        Place p = Config.usr.getPlace("DefaultTransitPlace");
        placePanel.setPlace(p);
    }

    @Override
    public void regist() {
        if ( placePanel.isComplete() ) {
            Place p = placePanel.getPlace();
            //PrefUtils.setPlace(pref, "DefaultTransitPlace", p);
            //pref.setPlace("DefaultTransitPlace", p);
            Config.usr.setPlace( "DefaultTransitPlace", p );
        } else {
            System.out.println("経過地設定エラー。項目が満たされていない");
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

        placePanel = new to.tetramorph.starbase.PlacePanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        add(placePanel, gridBagConstraints);

        jLabel1.setText("\u30c7\u30d5\u30a9\u30eb\u30c8\u3067\u306e\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8\u306e\u89b3\u6e2c\u5730\u3068\u30bf\u30a4\u30e0\u30be\u30fc\u30f3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jLabel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private to.tetramorph.starbase.PlacePanel placePanel;
    // End of variables declaration//GEN-END:variables
}