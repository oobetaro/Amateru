/*
 * ChartAnalyzer.java
 *
 * Created on 2008/10/29, 2:09
 *
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import to.tetramorph.starbase.lib.AngleUtilities;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * 天体の四大分布、カルミネイト、支配星、エレベートなどを求めるユーティリティ。
 * @author 大澤義孝
 */
public class ChartAnalyzer {
    
    protected Map<Integer,Body> bodyMap;
    
    /**
     * ChartAnalyzer オブジェクトを作成する
     * 解析したい天体を外部から入力してやる。
     * ＜天体ID, Body＞という構造で、天体をハッシュマップに入れたものを引数として
     * 与える。この時、モダン式なら十惑星とＡＣ，ＭＣが入っていればいいし、古典式
     * なら七惑星とＡＣ，ＭＣが入っていればいい。
     */
    public ChartAnalyzer( Map<Integer,Body> bodyMap ) {
        this.bodyMap = bodyMap;
    }
    
    /**
     * 指定したハウスに入っている惑星を抽出して返す。小惑星も惑星と判断して抽出
     * するが、ＡＣ，ＭＣ，ノードなど実体のない感受点はカウントしない。
     * @param house ハウス番号
     */
    public List<Body> getHouseInPlanets( int house ) {
        List<Body> list = new ArrayList<Body>();
        for ( Iterator<Body> ite = bodyMap.values().iterator();
               ite.hasNext(); ) {
            Body p = ite.next();
            if ( p.house != house ) continue;
            if ( ( p.id >= SUN && p.id <= PLUTO ) || 
                 ( p.id >= CHIRON && p.id <= VESTA )) {
                list.add( p );
            }
        }
        return list;
    }

    /**
     * 十惑星のみしかカウントしない
     * <pre>
     * div|element
     * 2  | 0 = 陽の星座にある天体のリストを返す。
     * 2  | 1 = 陰の星座にある天体の数
     * 3  | 0 = Cerdinal
     * 3  | 1 = Fixed
     * 3  | 2 = Mutable
     * 4  | 0 = 火
     * 4  | 1 = 地
     * 4  | 2 = 風
     * 4  | 3 = 水
     * </pre>
     */  
    private Body[] getClassifiedPlanets( int div, 
                                          int element ) {
        List<Body> result = new ArrayList<Body>();
        for ( Iterator<Body> ite = bodyMap.values().iterator(); 
              ite.hasNext(); ) {
            Body p = ite.next();
            if ( ! ( p.id >= SUN && p.id <= PLUTO ) ) continue;
            int ele = p.getSign() % div;
            if ( ele == element) result.add(p);
        }
        //Body [] points = list.toArray( new Body[0] ); //このほうが良い？
        Body [] points = new Body[ result.size() ];
        for ( int i = 0; i < points.length; i++) points[i] = result.get(i);
        return points;
    }
    
    /**
     * 火,地,風,水,C,F,M,陽,陰の星座にそれぞれどんな天体(複数)が入っているかを返す。
     * <pre>
     * List#get(0〜3) 火,地,風,水の順で天体(Body)の配列が返る。
     * List#get(4〜6) C,F,Mの順で天体リスト
     * List#get(7〜8) 陽,陰の天体リスト
     * </pre>
     */
    public List<Body[]> getElementsTable() {
        List<Body[]> list = new ArrayList<Body[]>();
        for ( int i = 0; i < 4; i++ )
            list.add( getClassifiedPlanets( 4, i ) );
        for ( int i = 0; i < 3; i++ )
            list.add( getClassifiedPlanets( 3, i ) );
        list.add( getClassifiedPlanets( 2, 0 ) );
        list.add( getClassifiedPlanets( 2, 1 ) );
        return list;
    }
    
    /**
     * 上昇星(AC±5度以内にある天体で、ASCに一番近い天体を返す。
     * ACが見つからないときはnullを返す。
     * ただし対象となるのは太陽から冥王星までのいずれか。
     */
    public Body getRisingPlanet() {
        Body a = bodyMap.get( AC );
        if ( a == null ) return null;
        double angle = 5.1;
        Body m = null;
        for ( int i = SUN; i <= PLUTO; i++ ) {
            Body p = bodyMap.get(i);
            if ( p == null ) continue;
            //double asp = Math.abs( a.lon - p.lon );
            double asp = Math.abs( AngleUtilities.angleDistance( a.lon, p.lon ));
            if ( angle > asp && asp <= 5 ) { 
                //ASCに一番近いものをコピーしておく
                angle = asp;
                m = p;
            }
        }
        return m;
    }

    public static final int MODERN_SYSTEM = 0;
    public static final int CLASSIC_SYSTEM = 1;
    
    /**
     * 支配星(ACサインのルーラー)を返す。
     * ACが見つからないときはnullを返す。
     * @param isModern モダン式の支配星(つまり外惑星が支配星になる事がある)を
     * 求めるときはtrueを指定する。古典式ならfalseを指定する。
     */
    public Body getRulerPlanet( boolean isModern ) {
        int [] RULERS = isModern ? MODERN_RULERS : CLASSIC_RULERS;
        Body a = bodyMap.get( AC );
        if ( a == null ) return null;
        return bodyMap.get( RULERS[ a.getSign() ] );
    }
    /**
     * 指定した天体のサイン、またはカスプのサインが示す支配星を返す。
     * @param isModernモダン式の支配星(つまり外惑星が支配星になる事がある)を
     * 求めるときはtrueを指定する。古典式ならfalseを指定する。
     * @param id 天体またはカスプのID。Const.Cusp1,Const.SUN等。
     * @return 支配星に該当する天体。
     * @exception 古典システム(isModern==false)で、idに外惑星を指定したとき。
     * 惑星(太陽〜冥王星)、カスプ(CUSP1〜CUSP12)以外をidに指定したとき。
     */
    public Body getHouseRuler( boolean isModern, int id ) {
        if ( ! isModern && (id >= URANUS && id <= PLUTO))
            throw new IllegalArgumentException("古典システムでは外惑星は指定できない");
        if ( ! ( id >= SUN && id <= PLUTO ||
               (id >= CUSP1 && id <= CUSP12 ) || id == MC || id == AC ) )
            throw new IllegalArgumentException("惑星またはカスプ以外の感受点が指定された");
        int [] RULERS = isModern ? MODERN_RULERS : CLASSIC_RULERS;
        Body b = bodyMap.get(id);
        if ( b == null ) return null;
        return bodyMap.get( RULERS[ b.getSign() ] );
    }
    /**
     * 太陽と月の離角から月相(28相)を求める。
     * 値はdouble[]で返り、[0]は月相、[1]には離角。[0]はかならず整数0-27。
     */
    public double [] getMoonFace() {
        Body sun = bodyMap.get( SUN );
        Body mon = bodyMap.get( MOON );
        if ( sun == null || mon == null ) return null;
        double [] results = new double[2];
        double a = ( sun.lon <= mon.lon ) ?
                       mon.lon - sun.lon   :   mon.lon + 360 - sun.lon;
        results[0] = (int)(a * 28. / 360.);
        results[1] = a;
        return results;
    }

    /**
     * エレベートしている惑星のリストを返す。
     * ACが検出できないときはサイズ０のリストを返す。
     * 7室から12室にある惑星のリストを返すということ。
     * 一つもないときは戻り値のlist.size()が0を返す。
     * ※このメソッドはちょい怪しい。部屋番号はスイスエフェメリスで求まっているのに
     * なぜこんなやり方で求めようとする？古い実装だ。
     */
    public List<Body> getElevatedPlanets() {
        Body a = bodyMap.get( AC );        
        List<Body>list = new ArrayList<Body>();
        if ( a == null ) return list;
        //　↓aの値を直接書き換えると後々困る。別オブジェクトを用意する。
        //a.lon -= 90d;
        Body asc = new Body( AC, a.lon - 90d );
        for ( int i = SUN; i <= PLUTO; i++ ) {
            Body p = bodyMap.get( i );
            if ( p == null ) continue;
            if ( p.house >= 7 && p.house <= 12 )
                list.add( p);
//            double asp = Math.abs( asc.lon - p.lon );
//            if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
//            if ( asp < 90d  ) list.add( p );
        }
        return list;
    }

    /**
     * カルミネイトしている天体(エレベートしている惑星の中でもっともMCに近い惑星を返す。
     * 存在しないときはnullを返す。
     * ※これもgetElevatedPlanets()を呼ぶので同様に妖しい
     */
    public Body getCulminatedPlanet() {
        Body mc = bodyMap.get( MC );
        if ( mc == null ) {
            return null; //MCが存在しないときはnull
        }
        double max = 90;
        Body cp = null;
        List<Body> list = getElevatedPlanets();
        //System.out.println( "MC lon = " + mc.lon );
        for ( int i = 0; i < list.size(); i++ ) {
            Body p = list.get(i);
            double asp =  AngleUtilities.angleDistance( mc.lon, p.lon );
            asp = Math.abs( asp );
            //System.out.println( p.getName() + ", lon = " + p.lon + ", 角距離 = " + asp );
            if ( max > asp ) {
                max = asp;
                cp = p;
            }
        }
        return cp;
    }
    /** アンギュラー (1,4,7,10宮) を表す定数 */
    public static final int ANGULAR = 0;
    /** サクシデント (2,5,8,11宮)を表す定数 */
    public static final int SUCCEDENT = 1;
    /** キャデント (3,6,9,12宮) を表す定数 */
    public static final int CADENT = 2;
    
    /**
     * アンギュラー、サクシデント、キャデント別に在泊天体(複数)を求める。
     * @param type ANGULAR,SUCCEDENT,CADENTの三種類を指定できる。
     */
    public List<Body> getPlanetsByHouseType( int type ) {
        List<Body> list = new ArrayList<Body>();
        for ( int i = SUN; i <= PLUTO; i++ ) {
            Body p = bodyMap.get( i );
            if ( p == null ) continue;
            if ( ( p.house - 1 ) % 3 == type ) list.add( p );
        }
        return list;
    }

}
