/*
 * CuspTable.java
 *
 * Created on 2008/01/15, 1:11
 *
 */

package to.tetramorph.starbase.chartlib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GComponentAdapter;
import to.tetramorph.starbase.chartparts.GComponentEvent;
import to.tetramorph.starbase.chartparts.GCuspLabel;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.chartparts.HouseRing;
import to.tetramorph.starbase.chartparts.PlanetRing;
import to.tetramorph.starbase.chartparts.ZodiacPanel;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * ZodiacPanel上にカスプの一覧表を表示するための部品。
 * @author 大澤義鷹
 */
public class CuspTable extends GBoxLayout {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;
    
    PlanetRing [] planetRings;
    private GLabel houseCuspLabel = new GLabel("ハウスカスプ", G_LEFT, 16d);
    List<GCuspLabel> cuspLabelList;
    Color listHighLightColor = Color.GRAY;
    //GBoxLayout cuspsLayout;
    
    /**
     * CuspTable オブジェクトを作成する
     */
    public CuspTable( PlanetRing [] planetRings ) {
        super( GBoxLayout.VERTICAL );
        this.planetRings = planetRings;
//        //this.zodiacPanel = zodiacPanel;
//        createCuspListLayout();
//    }
//    
//    private void createCuspListLayout() {
        cuspLabelList = new ArrayList<GCuspLabel>();
        houseCuspLabel.setInset( G_BOTTOM, 1 );
        add( houseCuspLabel );
        CuspLayoutHandler handler = new CuspLayoutHandler();
        for ( int i = 0; i < 12; i++ ) {
            GCuspLabel cuspLabel = new GCuspLabel( G_RIGHT, 17 );
            cuspLabel.setInset( G_BOTTOM, 0.5 );
            cuspLabel.setGComponentListener( handler );
            cuspLabelList.add( cuspLabel );
            add( cuspLabel );
        }
        setLocation( 2, 65, G_LEFT, G_BOTTOM );
    }
    
//    /**
//     * カスプ表の画面上の位置と配置方法を指定する。
//     * @param xper   水平位置をパーセントで指定する。60なら60%
//     * @param yper   垂直位置をパーセントで指定する。
//     * @param alignx 水平方向の配置方法 GLabel.LEFT,RIGHT,CENTER
//     * @param aligny 垂直方法の配置方法 GLabel.TOP,CENTER,BOTTOM
//     */
//    public void setLocation(double xper,double yper,
//                              int alignx,int aligny) {
//        setLocation( xper, yper, alignx, aligny);
//    }
    
//    /**
//     * ZodiacPanelからこのカスプテーブルを削除する。
//     */
//    public void remove() {
//        zodiacPanel.removeGLayout( cuspsLayout );
//    }
    
    /**
     * カスプリストに色を設定する。
     * @param listOtherColor       「ハウスカスプ」のタイトルの色
     * @param listAngleColor        角度の色
     * @param listSignColor         サインの色
     * @param listHouseNumberColor  ハウス番号の色
     */
    public void setCuspListColor( Color listOtherColor,
                                    Color listAngleColor,
                                    Color listSignColor,
                                    Color listHouseNumberColor) {
        houseCuspLabel.setColor( listOtherColor );
        for ( GCuspLabel cuspLabel : cuspLabelList ) {
            cuspLabel.setTextColor( listAngleColor );
            cuspLabel.setSignColor( listSignColor );
            cuspLabel.setHouseColor( listHouseNumberColor );
        }
    }
    
    /**
     * マウスがオンカーソルしたときのハイライト色をセットする。
     */
    public void setHighLightColor( Color listHighLightColor ) {
        this.listHighLightColor = listHighLightColor;
    }
    
    /**
     * カスプのリストをセットする。
     */
    public void setCuspList( List<Body> cuspList ) {
        for(int i=0; i<cuspList.size(); i++) {
            cuspLabelList.get(i).setBody(cuspList.get(i));
        }
    }
    /**
     * カスプリストのハンドラ。リストのマウスイベントを処理する。
     */
    class CuspLayoutHandler extends GComponentAdapter {
        public void componentOnCursor(GComponentEvent evt) {
            GCuspLabel l = (GCuspLabel)evt.getGComponent();
            l.setBGColor( listHighLightColor );
            Body body = l.getBody();
            PlanetRing r = findRing( body.group );
            HouseRing hring = r.getHouseRing();
            //HouseRing hring = planetRings[ body.group ].getHouseRing();
            hring.setHighLightHouse( body.id - CUSP1 );
        }
        
        public void componentOutCursor(GComponentEvent evt) {
            GCuspLabel l = (GCuspLabel)evt.getGComponent();
            l.setBGColor(null);
            for(PlanetRing r: planetRings)
                r.getHouseRing().setHighLightHouse(-1);
        }
        PlanetRing findRing(int group) {
            for ( PlanetRing r : planetRings ) {
                if ( r.getGroup() == group ) return r;
            }
            throw new IllegalArgumentException(
                "指定されたグループ番号を持つPlanetRingが見つからない");
        }
    }
}
