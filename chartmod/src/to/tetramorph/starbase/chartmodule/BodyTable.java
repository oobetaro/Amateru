/*
 * BodyTable.java
 *
 * Created on 2008/01/15, 14:18
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import to.tetramorph.starbase.chartparts.GBodyLabel;
import to.tetramorph.starbase.chartparts.GBoxLayout;
import to.tetramorph.starbase.chartparts.GComponentAdapter;
import to.tetramorph.starbase.chartparts.GComponentEvent;
import to.tetramorph.starbase.chartparts.GLabel;
import to.tetramorph.starbase.chartparts.HouseRing;
import to.tetramorph.starbase.chartparts.PlanetRing;
import to.tetramorph.starbase.chartparts.ZodiacPanel;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * ZodiacPanel上に天体位置のリストを表示する。
 * @author 大澤義鷹
 */
public class BodyTable {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;
    private GLabel nptLabel = new GLabel( "ネイタル", G_LEFT, 18d );
    private GBoxLayout bodyListLayout;
    private ZodiacPanel zodiacPanel;
    private Map<Integer,GBodyLabel> bodyLabelMap;
    private PlanetRing [] planetRings;
    private Color listHighLightColor = Color.GRAY;
    private int [] bodyIDs;
    Color listOtherColor = Color.BLACK;
    Color listSignColor = Color.BLACK;
    Color listAngleColor =  Color.BLACK;
    Color listBodyColor =  Color.BLACK;
    Color listRevColor =  Color.BLACK;
    /**  
    * BodyTable オブジェクトを作成する 
    */
    public BodyTable( PlanetRing [] planetRings, 
                       ZodiacPanel zodiacPanel) {
        this.zodiacPanel = zodiacPanel;
        this.planetRings = planetRings;
        this.bodyIDs = bodyIDs;
    }
    
    /**
     * 天体リストを表示するためのレイアウトを作成または再作成
     * @param tableName npt[ scp.getBodyListMode() ]として、ネイタル、プログレス
     *        トンジットなどの文字列を与える。
     */
    public void updateBodyList(int [] bodyIDs,String tableName) {
        zodiacPanel.removeGLayout(bodyListLayout);
        //使用している天体をSetに格納
        Set<Integer> useSet = new HashSet<Integer>();
        for ( int id : bodyIDs ) useSet.add(id);
        //ｱﾝﾁﾎﾟｲﾝﾄのある感受点を二つの配列に分けて入れる
        Integer [] list1 = { NODE, APOGEE, VERTEX, AC, MC };
        Integer [] list2 = { SOUTH_NODE, ANTI_APOGEE, ANTI_VERTEX, DC, IC };
        //ｱﾝﾁﾎﾟｲﾝﾄとそうではないほうの両方が使用されてるときはｱﾝﾁﾎﾟｲﾝﾄを抜く
        for ( int i=0; i<list1.length; i++ ) {
            if ( useSet.contains(list1[i]) && useSet.contains(list2[i]))
                 useSet.remove(list2[i]);
        }
        bodyLabelMap = new HashMap<Integer,GBodyLabel>();
        bodyListLayout = new GBoxLayout(GBoxLayout.VERTICAL);
        //String [] npt =  { "ネイタル","プログレス","トランジット" };
        nptLabel = new GLabel( tableName, G_LEFT, 18d );
        nptLabel.setInset(G_BOTTOM,1);
        nptLabel.setColor(listOtherColor);
        bodyListLayout.add(nptLabel);
        BodyLayoutHandler handler = new BodyLayoutHandler();
        for(Integer id : LISTING_BODYS) {
            if( useSet.contains(id) ) {
                GBodyLabel bl = new GBodyLabel();
                bl.setInset(G_BOTTOM,0.5);
                bl.setFontScale(21);
                bl.setSignColor( listSignColor );
                bl.setTextColor( listAngleColor );
                bl.setBodyColor( listBodyColor );
                bl.setRevColor( listRevColor );
                bl.setGComponentListener(handler);
                bodyLabelMap.put(id,bl);
                bodyListLayout.add(bl);
            }
        }
        bodyListLayout.setLocation(15,65,G_LEFT,G_BOTTOM);
        zodiacPanel.addGLayout(bodyListLayout);
    }
    
    /**
     * 天体リストのレイアウトに天体位置データをセット
     */
    public void setBodyList(List<Body> bodyList) {
        for ( Body b : bodyList ) {
            GBodyLabel bl = bodyLabelMap.get(b.id);
            if(bl != null) bl.setBody(b);
        }
    }
    
    /**
     * マウスがオンカーソルしたときのハイライト色をセットする。
     */
    public void setHighLightColor( Color listHighLightColor ) {
        this.listHighLightColor = listHighLightColor;
    }
    
    /**
     * 天体リストの色をセットする。
     */
    public void setBodyListColor( Color listOtherColor,
                                    Color listSignColor,
                                    Color listAngleColor,
                                    Color listBodyColor,
                                    Color listRevColor ) {
        this.listOtherColor = listOtherColor;
        this.listSignColor = listSignColor;
        this.listAngleColor =  listAngleColor;
        this.listBodyColor =  listBodyColor;
        this.listRevColor =  listRevColor;
        nptLabel.setColor(listOtherColor);
        if ( bodyLabelMap == null ) return;
        for ( Iterator ite = bodyLabelMap.values().iterator(); 
                                                 ite.hasNext(); ) {
            GBodyLabel bl = (GBodyLabel)ite.next();
            bl.setSignColor( listSignColor );
            bl.setTextColor( listAngleColor );
            bl.setBodyColor( listBodyColor );
            bl.setRevColor( listRevColor );
        }
    }
    
    class BodyLayoutHandler extends GComponentAdapter {
        
        public void componentOnCursor(GComponentEvent evt) {
            GBodyLabel l = (GBodyLabel)evt.getGComponent();
            l.setBGColor( listHighLightColor );
            Body body = l.getBody();
            PlanetRing r = findRing( body.group );
            HouseRing hring = r.getHouseRing();
            //HouseRing hring = planetRings[ body.group ].getHouseRing();
            hring.setHighLightHouse( body.house - 1 );
        }
        
        public void componentOutCursor(GComponentEvent evt) {
            GBodyLabel l = (GBodyLabel)evt.getGComponent();
            l.setBGColor(null);
            for ( PlanetRing r: planetRings )
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
