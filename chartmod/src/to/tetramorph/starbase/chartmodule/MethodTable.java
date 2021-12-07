/*
 * MethodTable.java
 *
 * Created on 2008/01/15, 22:31
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.chartparts.ZodiacPanel;

/**
 * ZodiacPanel上にハウス分割法や進行法を表示する部品。
 * @author 大澤義鷹
 */
public class MethodTable {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;

    private ZodiacPanel zodiacPanel;
    private GLabel houseMethodLabel = new GLabel("ハウス分割法：",G_RIGHT );
    private GLabel progressMethodLabel = new GLabel("進行法：",G_RIGHT);
    private GLabel houseMethodNameLabel = new GLabel();
    private GLabel progMethodNameLabel = new GLabel();
    private GBoxLayout methodLayout;

    /**
     * MethodTable オブジェクトを作成する。
     */
    public MethodTable( ZodiacPanel zodiacPanel ) {
        this.zodiacPanel = zodiacPanel;
    }
    
    /**
     * ハウス分割法や進行法を表示するためのレイアウトを作成。
     */
    public void updateMethodTable(int showRing) {
        zodiacPanel.removeGLayout(methodLayout);
        int r = showRing; //scp.getShowRings();
        if(r == 1 || r == 3 || r == 4) { //P円がないときは進行法は非表示
            houseMethodNameLabel.setInset(G_BOTTOM,0);
            methodLayout = new GBoxLayout(GBoxLayout.HORIZONTAL);
            methodLayout.add( houseMethodLabel, houseMethodNameLabel);
        } else {
            methodLayout = new GBoxLayout(GBoxLayout.HORIZONTAL);
            GBoxLayout lay1 = new GBoxLayout(GBoxLayout.VERTICAL);
            houseMethodNameLabel.setInset(G_BOTTOM,0.5);
            houseMethodLabel.setInset(G_BOTTOM,0.5);
            lay1.add( houseMethodLabel, progressMethodLabel );
            GBoxLayout lay2 = new GBoxLayout(GBoxLayout.VERTICAL);
            lay2.add( houseMethodNameLabel, progMethodNameLabel);
            methodLayout.add( lay1, lay2 );
        }
        methodLayout.setLocation( 0, 70, G_LEFT, G_BOTTOM);
        zodiacPanel.addGLayout( methodLayout );
    }
    
    /**
     * ZodiacPanelからこのオブジェクトを削除する。
     */
    public void remove() {
        zodiacPanel.removeGLayout( methodLayout );
    }
    
    /**
     * 色をセットする。
     */
    public void setColor( Color listOtherColor ) {
        houseMethodNameLabel.setColor( listOtherColor );
        houseMethodLabel.setColor( listOtherColor );
        progMethodNameLabel.setColor( listOtherColor );
        progressMethodLabel.setColor( listOtherColor );        
    }
    
    /**
     * ハウス分割法と進行法の名前をセットする。
     */
    public void setMethodNames(String hmeth,String pmeth) {
        houseMethodNameLabel.setText( hmeth );
        progMethodNameLabel.setText( pmeth );
    }
}
