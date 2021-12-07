/*
 * OBECalendar.java
 *
 * Created on 2008/03/23, 11:12
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import to.tetramorph.starbase.util.NatalChart;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.TimePlace;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.util.TestConst;
/**
 * 幽体離脱最適日を計算する。
 * @author 大澤義鷹
 */
public class OBECalendar {
    OBECalendarListener listener = null;
    
    static final AspectType [] FIND_ASPECT_TYPES = {
        new AspectType( CONJUNCTION, 2.5, 5 ),
        new AspectType( SEXTILE,     2.5, 5 ),
        new AspectType( SQUARE,      2.5, 5 ),
        new AspectType( TRINE,       2.5, 5 ),
        new AspectType( OPPOSITION,  2.5, 5 ),
        new AspectType( QUINCUNX,    2.5, 5 ),
    };
    
    //アスペクトの種類べつ得点( FIND_ASPECT_TYPESの要素に対応 )
    static final int [] TYPE_SCORES = new int[] { 2, 2, 3, 1, 3, 3 };
    // 太陽、月、、、冥王星までの得点。
    // ただし水星は無視するので0点。ACは含まない。
    static final int [] PLANET_SCORES = 
                                   new int[] { 1, 1, 0, 1, 3, 3, 2, 2, 1, 2 };
    
    // 太陽サインごとのスコア
    static final int [] SIGN_SCORES =
                       new int[] { 16,14,6,5,4,4,0,5,5,2,1,16 };
    
    static final int [] BODYS = 
        new int [] { SUN,MARS,VENUS,JUPITER,SATURN,NEPTUNE,URANUS,PLUTO };
    // 火星用
    static final int [] BODYS2 = 
        new int [] { SUN,VENUS,JUPITER,SATURN,NEPTUNE,URANUS,PLUTO };
    
    // 月相(0-27)のスコア
    static final double [] MOONFACE_SCORES = new double[] { 
          0.5,  1,   1,    2,  2.5,   3,    4,
          5,  5.7,   7,    9,   12,  15,   16,
         10,   16,  15,   12,    9,   7,  5.7, 
          5,    4,   3,  2.5,    2,   1,    0,
    };
    
    /**  
     * OBECalendar オブジェクトを作成する 
     * @param l setTimePlace()の計算結果通達用のリスナ
     */
    public OBECalendar( OBECalendarListener l ) {
        this.listener = l;
    }
    
    /**
     * 与えられたアスペクトリストのスコアを返す。
     */
    static double getScore( Aspect aspect ) {
        
        for ( int i=0; i< FIND_ASPECT_TYPES.length; i++ ) {
            AspectType type = FIND_ASPECT_TYPES[i];
            if ( type.aid == aspect.aid ) {
                double v = type.looseOrb - aspect.error;
                v += TYPE_SCORES[ i ];
                v += ( aspect.p1.id == AC ) ? 
                                      2     :     PLANET_SCORES[ aspect.p1.id ];
                double u = v * 0.9;
                return v * v - u * u;
            }
        }
        return 0;
    }
    
    /**
     * トランジットチャートの月のアスペクトと、Ｔ月とネイタルチャートのアスペクトを
     * １時間指定された日から一時間間隔で求める。指定したトランジットの分・秒・
     * ミリ秒は無条件にゼロと見なされる。つまり二時のアスペクト、三時のアスペクト
     * というように、切りの良い時間帯で計算される。
     * ある時刻にアスペクトが複数発生する場合があるので、その時点のアスペクトは
     * List<Aspect>で表現され、そのリストをさらにリストに入れて指定期間のアスペクト
     * テーブルを作成する。アスペクトが検出されなかった時間は、サイズ０の
     * List<Aspect>で表現されnullではない。
     * 
     * @param transitTimePlace トランジットの時と場所
     * @param natalTimePlace ネイタルの時と場所
     * @param maxHour 何時間分のデータを求めるか
     * @param transitAspectList Ｔ月とトランジットのアスペクト返却用のリスト
     * @param natalAspectList   Ｔ月とネイタルとのアスペクト返却用のリスト
     * @param marsAspectList    Ｔ火星とトランジットのアスペクト返却用リスト
     */
    static void getAspects( TimePlace transitTimePlace,
                              TimePlace natalTimePlace,
                              int maxHour,
                              List< List<Aspect> > transitAspectList,
                              List< List<Aspect> > natalAspectList,
                              List< List<Aspect> > marsAspectList,
                              double [] sunScores ) {
        NatalChart tChart = new NatalChart();
        NatalChart nChart = new NatalChart();
        GregorianCalendar tcal;
        tcal = (GregorianCalendar)transitTimePlace.getCalendar().clone();
        resetMinute( tcal );

        TimePlace tTimePlace = new TimePlace( transitTimePlace );        
        nChart.setTimePlace( natalTimePlace );
        List<Body> nBodyList = nChart.getBodyList( BODYS );
        for ( int i=0; i < maxHour; i++ ) {
            tTimePlace.setCalendar( tcal, TimePlace.DATE_AND_TIME );
            tChart.setTimePlace( tTimePlace );

            Body moon = tChart.getBody( MOON );
            Body sun  = tChart.getBody( SUN );
            Body mars = tChart.getBody( MARS );
            //sunScores[i] = getMoonFaceScore( moon, sun );
            sunScores[i] = SIGN_SCORES[ sun.getSign() ] + 1;
            List<Body> tBodyList = tChart.getBodyList( BODYS );
            List<Body> tBodyList2 = tChart.getBodyList( BODYS2 );
            List<Aspect> tAspectList = new ArrayList<Aspect>();
            AspectFinder.getAspects( 
                              tBodyList, moon, tAspectList, FIND_ASPECT_TYPES );
            
            List<Aspect> nAspectList = new ArrayList<Aspect>();
            AspectFinder.getAspects( 
                              nBodyList, moon, nAspectList, FIND_ASPECT_TYPES );
            
            List<Aspect> mAspectList = new ArrayList<Aspect>();
            AspectFinder.getAspects( 
                             tBodyList2, mars, mAspectList, FIND_ASPECT_TYPES );
            
            transitAspectList.add( tAspectList );
            natalAspectList.add( nAspectList );
            marsAspectList.add( mAspectList );
            tcal.add( Calendar.HOUR_OF_DAY, 1 );
        }
    }
    /**
     * 分、秒、ミリ秒の桁をリセットする。
     */
    static void resetMinute( GregorianCalendar cal ) {
        cal.set( Calendar.MINUTE, 0 );
        cal.set( Calendar.SECOND, 0 );
        cal.set( Calendar.MILLISECOND, 0 );
    }
    /**
     * 月と太陽の離角を返す。
     */
    private static double getMoonFaceScore( Body mon, Body sun ) {
        double a = sun.lon <= mon.lon ?
                    mon.lon - sun.lon    :    mon.lon + 360 - sun.lon;
        int i = (int)( a * 28d / 360d );
        double sm = ( MOONFACE_SCORES[i] + 1 ) 
                   * ( SIGN_SCORES[ sun.getSign() ] + 1);
        return Math.sqrt( sm );
    }

    /**
     * getAspectを呼び出して、1時間刻みの体脱スコアを返す。
     * 戻り値
     * [0][0] = サンサインのスコア
     * [0][1] = ネイタルのスコア 
     * [0][2] = トランジットのスコア
     * [1][]  = 一時間後のネイタルとトランジットのスコア
     * [2][]  = 二時間後の           〃
     */
    public static double [][] getTable( TimePlace transitTimePlace,
                                           TimePlace natalTimePlace,
                                           int maxHour ) {
        List< List<Aspect> > nAspectList = new ArrayList< List<Aspect> >();
        List< List<Aspect> > tAspectList = new ArrayList< List<Aspect> >();
        List< List<Aspect> > mAspectList = new ArrayList< List<Aspect> >();
        double [] sunScores = new double[ maxHour ];
        getAspects( transitTimePlace,
                    natalTimePlace,
                    maxHour,
                    tAspectList, nAspectList, mAspectList, sunScores );
        double [][] result = new double[ maxHour ][ 4 ];
        // もとまった各スコアに補正値(0.5等)をかけ上限値を調整
        // ここらへんは経験則に基づいて決定
        for ( int i=0; i < nAspectList.size(); i++ ) {
            result[ i ][ 0 ] = sunScores[i];
            double score = 0;
            for ( Aspect a : mAspectList.get(i) ) score += getScore( a );
            result[ i ][ 1 ] = score * 0.6;
            score = 0;
            for ( Aspect a : tAspectList.get(i) ) score += getScore( a );
            result[ i ][ 2 ] = score * 0.75;
            score = 0;
            for ( Aspect a : nAspectList.get(i) ) score += getScore( a );
            result[ i ][ 3 ] = score * 0.75;
            
        }
        return result;
    }
    
    /**
     * getAspectを別スレッドで呼び出して計算し、計算結果はOBECalendarListener
     * を呼び出すことで通達する。
     */
    public void setTimePlace( final TimePlace transitTimePlace,
                                final TimePlace natalTimePlace,
                                final int maxHour ) {
        new Thread( new Runnable() {
            public void run() {
                double [][] result = 
                    getTable( transitTimePlace, natalTimePlace, maxHour );
                if ( listener != null ) {
                    GregorianCalendar tcal = 
                        (GregorianCalendar)transitTimePlace.getCalendar().clone();
                    resetMinute( tcal );
                    listener.calcurated( transitTimePlace,
                                         natalTimePlace,
                                         result,
                                         tcal );
                }
            }
        }).start();        
    }
    
    public static void main( String [] args ) {
        System.setProperty("swe.path","c:/users/ephe/");
        System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
        int hourCount = 24 * 7;
        Place place = TestConst.getMyPlace();
        TimePlace transitTimePlace = TestConst.getTimePlace(
            TestConst.AD, 2008, 3-1, 23, 0, 0, 0, place );
        TimePlace natalTimePlace = TestConst.getMyTimePlace();
        double [][] table = OBECalendar.getTable( transitTimePlace,
                                                   natalTimePlace,
                                                   hourCount );
        for ( int i=0; i < table.length; i++ ) {
            System.out.println( table[i][0] + "," + table[i][1] );
        }
    }
}
