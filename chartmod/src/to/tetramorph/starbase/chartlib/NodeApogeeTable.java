/*
 * NodeApogeeTable.java
 *
 * Created on 2008/01/16, 0:26
 *
 */

package to.tetramorph.starbase.chartlib;

import java.awt.Color;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.chartparts.GSymbolLabel;
import to.tetramorph.starbase.chartparts.ZodiacPanel;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * ZodiacPanelにノードのtrue/meanの種別を表示する。
 * @author 大澤義鷹
 */
public class NodeApogeeTable extends GBoxLayout {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;
    private GSymbolLabel nodeLabel = new GSymbolLabel("\u00CB"," 真位置");
    private GSymbolLabel apogeeLabel = new GSymbolLabel("\u00CD"," 真位置");
    private Color listOtherColor;

    /**
     * NodeApogeeTable オブジェクトを作成する
     */
    public NodeApogeeTable() {
        super( GBoxLayout.HORIZONTAL );
    }
    
    /**
     * ノードのタイプ(トルー,ミーン)の表示レイアウトとリリスのタイプ(密接/平均)
     * の表示レイアウトを作成する。
     * 使用天体の中にノードやリリスが無い場合はそれを除外する。
     * 両方無い場合はnullを返す。
     * setSpecificConfig()からのみの呼びだしのみでＯＫなので、
     * zodiacPanelへの登録/リメイクも行う。
     * @param bodyIDs         scp.getBodyIDs( scp.getBodyListMode() )
     * @param nodeTypeName    nptChart.getNodeTypeName()
     * @param apogeeTypeName  nptChart.getApogeeTypeName()
     */
    public void updateNodeApogee( int [] bodyIDs,
                                    String nodeTypeName,
                                    String apogeeTypeName) {
        int [] bodys = bodyIDs;
        this.removeAll();
        for ( int id : bodys ) {
            if ( id == NODE || id == SOUTH_NODE ) {
                nodeLabel = new GSymbolLabel("\u00CB", " " + nodeTypeName );
                nodeLabel.setColor( listOtherColor );
                add( nodeLabel );
                break;
            }
        }
        for ( int id: bodys ) {
            if ( id == APOGEE || id == ANTI_APOGEE ) {
                apogeeLabel =
                    new GSymbolLabel("\u00CD"," " + apogeeTypeName );
                apogeeLabel.setColor(listOtherColor);
                apogeeLabel.setInset(G_LEFT,1);
                add( apogeeLabel );
                break;
            }
        }
//        if ( getComponentCount() > 0 ) {
//            setLocation( 100, 70, G_RIGHT, G_BOTTOM);
//        }
    }
    
    /**
     * 色をセットする。
     */
    public void setColor( Color listOtherColor ) {
        this.listOtherColor = listOtherColor;
        if ( nodeLabel != null ) {
            nodeLabel.setColor(listOtherColor);
            apogeeLabel.setColor(listOtherColor);
        }
    }
}
