/*
 * AffinityChart.java
 *
 * Created on 2008/03/03, 5:24
 *
 */

package to.tetramorph.starbase.util;

import java.util.List;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;

/**
 * NPTChartにもう一人追加して、二人分のNPと、共通のTを取得できるようにしたもの。
 * @author 大澤義鷹
 */
public class AffinityChart extends NPTChart {
    public static final int NATAL2 = 3;
    public static final int PROGRESS2 = 4;
    private static final int [] NPT = {
        NATAL, PROGRESS, TRANSIT, NATAL, PROGRESS
    };
    private static final int [] NPTNP = {
        NATAL, PROGRESS, TRANSIT, NATAL2, PROGRESS2
    };
    NPTChart nptChart2; //二人目のNPTChart。一人目は親クラスが担当。
    
    public AffinityChart( ChartConfig cc) {
        super(cc);
        nptChart2 = new NPTChart( cc );
    }
    /**
     * 二人目の出生データをセットする。
     */
    public void setData2( Data data) {
        nptChart2.setData( data );
    }
    /**
     * トランジットをセットする。一人目と二人目の両方のプログレスが正しく計算さ
     * れるためにはこのメソッドを使用する。
     */
    public void setTransit( Transit transit ) {
        super.setTransit( transit );
        nptChart2.setTransit( transit );
    }
    /**
     * 進行法をセットする。
     */
    public void setProgressMode( char mode ) {
        super.setProgressMode( mode );
        nptChart2.setProgressMode( mode );
    }
    /**
     * 天体位置を返す。
     * @param id 天体ID
     * @param npt NATAL,PROGRESS,TRANSIT,NATAL2,PROGRESS2のどれかを指定する。
     */
    public Body getBody( int id, int npt ) {
        if ( npt < NATAL2 )
            return super.getBody( id, npt );
        if ( npt > PROGRESS2 ) 
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        Body body = nptChart2.getBody( id, NPT[ npt ] );
        body.group = NPTNP[ npt ];
        return body;
    }
    
    public List<Body> getBodyList( int [] planets, int npt ) {
        if ( npt < NATAL2 )
            return super.getBodyList( planets, npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        List<Body> list = nptChart2.getBodyList( planets, NPT[ npt ] );
        setGroup( list, NPTNP[ npt ] );
        return list;
    }
    
    public List<ErrorBody> getErrorList( int [] planets, int npt ) {
        if ( npt < NATAL2 )
            return super.getErrorList( planets, npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        List<ErrorBody> list = nptChart2.getErrorList( planets, NPT[ npt ] );
        setGroup2( list, NPTNP[ npt ] );
        return list;
    }
    
    public List<Body> getCuspList( int npt ) {
        if ( npt < NATAL2 )
            return super.getCuspList( npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        List<Body> list = nptChart2.getCuspList( NPT[ npt ] );
        setGroup( list, NPTNP[ npt ] );
        return list;
    }
    /**
     * Bodyリストにgroupコードをセットする。
     */
    private void setGroup( List<Body> list, int group ) {
        if ( list == null ) return;
        for ( int i=0; i<list.size(); i++ )
            list.get(i).group = group;
    }
    /**
     * ErrorBodyのリストにgroupコードをセットする。
     */
    private void setGroup2( List<ErrorBody> list, int group) {
        if ( list == null ) return;
        for ( int i=0; i<list.size(); i++ )
            list.get(i).group = group;        
    }
    
    public TimePlace search( int id,
                              double targetAngle,
                              boolean isBackwards,
                              int npt ) throws UnsupportedOperationException {
        if ( npt < NATAL2 )
            return super.search( id, targetAngle, isBackwards, npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        return nptChart2.search( id,targetAngle, isBackwards, NPT[ npt ] );
    }
    
    public int [] getDragBodys( int [] bodys, int npt ) {
        if ( npt < NATAL2 )
            return super.getDragBodys( bodys, npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        return nptChart2.getDragBodys( bodys, NPT[ npt ] );
    }

    public int [] getNotDragBodys( int npt ) {
        if ( npt < NATAL2 )
            return super.getNotDragBodys( npt );
        if ( npt > PROGRESS2 )
            throw new IllegalArgumentException( "Bad npt argument " + npt );
        return nptChart2.getNotDragBodys( NPT[ npt ] );
    }
    
//    public static void main(String [] args) {
//        
//    }
}
