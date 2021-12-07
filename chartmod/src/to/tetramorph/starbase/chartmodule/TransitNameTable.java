/*
 * TransitNameTable.java
 *
 * Created on 2008/01/15, 18:25
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.chartparts.ZodiacPanel;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.util.AngleConverter;

/**
 * ZodiacPanelにトランジットの名前や観測地を表示する。
 * @author 大澤義鷹
 */
public class TransitNameTable {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;
    private ZodiacPanel zodiacPanel;
    private GLabel tNameLabel = new GLabel();
    private GLabel tLocationLabel = new GLabel( G_RIGHT, 11 );
    private GLabel tDateLabel = new GLabel();
    private GLabel tTimeZoneLabel = new GLabel( G_RIGHT, 11 );
    private GLabel tPlaceLabel = new GLabel( G_RIGHT );
    private GBoxLayout layout;
    /**
     * TransitNameTable オブジェクトを作成する
     */
    public TransitNameTable( ZodiacPanel zodiacPanel ) {
        this.zodiacPanel = zodiacPanel;
        createTransitLayout();
    }
    
    /**
     * トランジットの日時、観測地を表示するためのレイアウトを作成
     */
    private void createTransitLayout() {
        tNameLabel.setInset( G_BOTTOM, 0.1 );
        tDateLabel.setInsets( 0, 2, 0.1, 0);
        tTimeZoneLabel.setInset( G_LEFT, 5 );
        tPlaceLabel.setInsets( 0, 5, 0.1,0);
        tLocationLabel.setInset( G_LEFT, 5 );
        layout = new GBoxLayout( GBoxLayout.VERTICAL );
        layout.add( tNameLabel,tDateLabel,tTimeZoneLabel,tPlaceLabel,tLocationLabel);
        layout.setLocation(100,0,G_RIGHT,G_TOP);
        zodiacPanel.addGLayout(layout);
    }
    
    /**
     * このオブジェクトをZodiacPanelから削除する。
     */
    public void remove() {
        zodiacPanel.removeGLayout( layout );
    }
    
    /**
     * このオブジェクトを表示する。
     */    
    public void show() {
        if ( zodiacPanel.contain( layout ) ) return;
        zodiacPanel.addGLayout( layout );
    }
    
    /**
     * トランジットの日時、観測地等のパラメタをトランジット用レイアウトにセット
     */
    public void setTransit( Transit transit ) {
        String tName = transit.getName();
        if(tName.length() == 0) tName = "トランジット";
        tNameLabel.setText( tName );
        String lat = AngleConverter.getFormattedLatitude(transit.getLatitude());
        String lon = AngleConverter.getFormattedLongitude(transit.getLongitude());
        String loc = (transit.getLatitude() == null) ?
            "〔観測点不明〕" : lat + ", " + lon;
        tLocationLabel.setText( loc );
        tTimeZoneLabel.setText( transit.getTimeZone().getDisplayName() );
        String date = transit.getFormattedDate();
        if( transit.getTime() == null )
            date += "〔時刻不明〕";
        tDateLabel.setText( date );
        String tPName = transit.getPlaceName().length() == 0 ?
            "〔地名未登録〕" : transit.getPlaceName();
        tPlaceLabel.setText( tPName );
    }
    
    public void setColors( Color nameColor,
                             Color dateColor,
                             Color placeColor ) {
        tNameLabel.setColor( nameColor );
        tDateLabel.setColor( dateColor );
        tLocationLabel.setColor( placeColor );
        tPlaceLabel.setColor( placeColor );
        tTimeZoneLabel.setColor( placeColor );
    }
}
