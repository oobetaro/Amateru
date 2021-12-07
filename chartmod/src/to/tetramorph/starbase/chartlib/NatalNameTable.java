/*
 * NatalNameTable.java
 *
 * Created on 2008/01/15, 17:53
 *
 */

package to.tetramorph.starbase.chartlib;

import java.awt.Color;
import java.util.Calendar;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.util.AngleConverter;

/**
 * ZodiacPanelにネイタルの人物名や観測地名などを表示する。
 * @author 大澤義鷹
 */
public class NatalNameTable extends GBoxLayout {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;

    private GLabel nNameLabel = new GLabel();
    private GLabel nLocationLabel = new GLabel( G_LEFT, 11 );
    private GLabel nDateLabel = new GLabel();
    private GLabel nTimeZoneLabel = new GLabel( G_LEFT, 11 );
    private GLabel nPlaceLabel = new GLabel();
    //private ZodiacPanel zodiacPanel;
    //private GBoxLayout layout;
    /**
     * NatalNameTable オブジェクトを作成する
     */
    //public NatalNameTable( ZodiacPanel zodiacPanel, 
    public NatalNameTable( double xper,
                            double yper ) {
        super( GBoxLayout.VERTICAL );
        //this.zodiacPanel = zodiacPanel;
        createNatalLayout();
        setLocation(xper, yper);
    }
    /**
     * デフォルト位置(0,0)で
     */
    public NatalNameTable( ) {
        //this( zodiacPanel, 0, 0 );
        this( 0, 0 );
    }
    
    /**
     * ネイタルの名前、日時、観測地を表示するためのラベルを作成しzodiacPanelに
     * addする。init()から1度しか呼ばれない。
     */
    private void createNatalLayout() {
        nNameLabel.setInset(G_BOTTOM,0.1);
        nDateLabel.setInsets( 0, 2, 0.1, 0 );
        nTimeZoneLabel.setInsets( 0, 2, 0.1, 0 );
        nPlaceLabel.setInsets( 0, 2, 0.1, 0 );
        nLocationLabel.setInsets( 0, 2, 0.1, 0 );
        //layout = new GBoxLayout( GBoxLayout.VERTICAL );
        add( nNameLabel, nDateLabel, nTimeZoneLabel, 
             nPlaceLabel, nLocationLabel);
        //zodiacPanel.addGLayout(layout);
    }
    
//    /**
//     * このオブジェクトをZodiacPanelから削除(非表示に)する。
//     */
//    public void remove() {
//        //zodiacPanel.removeGLayout( layout );
//    }
    
//    /**
//     * このオブジェクトを表示する。
//     */
//    public void show() {
//        if ( zodiacPanel.contain( layout ) ) return;
//        zodiacPanel.addGLayout( layout );
//    }
    
    /**
     * ネイタルの名前、日時、観測地を表示するラベルにデータをセット
     */
    public void setData( Data natalData ) {
        TimePlace tp = natalData.getTimePlace();
        nNameLabel.setText( natalData.getNatal().getName() );
        String lon = AngleConverter.getFormattedLongitude(tp.getLongitude());
        String lat = AngleConverter.getFormattedLatitude(tp.getLatitude());
        String loc = (tp.getLatitude() == null) ?
            "〔観測点未登録〕" : lat + ", " + lon;
        nLocationLabel.setText( loc );
        int dst_offset = tp.getCalendar().get(Calendar.DST_OFFSET);
        String displayName = tp.getTimeZone().getDisplayName();
        if ( dst_offset > 0 ) {
            displayName = displayName.concat("〔夏時間実施中〕");
        }
        nTimeZoneLabel.setText( displayName );
        String date = tp.getFormattedDate();
        if ( natalData.getNatal().getTime() == null )
            date += "〔時刻未登録〕";
        nDateLabel.setText( date );
        String nPName = tp.getPlaceName().length() == 0 ?
            "〔地名未登録〕" : tp.getPlaceName();
        nPlaceLabel.setText(nPName);
    }
    
    /**
     * 配色をセットする。
     */
    public void setColors( Color nameColor,
                             Color dateColor,
                             Color placeColor ) {
        nNameLabel.setColor( nameColor );
        nDateLabel.setColor( dateColor );
        nLocationLabel.setColor( placeColor );
        nPlaceLabel.setColor( placeColor );
        nTimeZoneLabel.setColor( placeColor );
    }
}
