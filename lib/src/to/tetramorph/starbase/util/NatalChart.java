/*
 * NatalChart.java
 *
 * Created on 2007/06/22, 9:55
 *
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TCPlanet;
import swisseph.TransitCalculator;
import to.tetramorph.starbase.lib.Body;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.time.JDay;

/**
 * TimePlaceから天体位置を求め、getBody()メソッドで天体位置を取得する。
 * setTimePlace()を実行したタイミングで、すべての天体位置、可能であればハウスカスプ
 * やAC,MC,バーテックスなどの感受点も計算する。天体は太陽から冥王星、フォルスと
 * 四大小惑星の位置を求める。
 * ノードおよびリリスを取得する際、指定するIDはNODEまたはAPOGEEで、TRUE_NODEや
 * OSC_APOGEEは指定できない。TRUEまたはMEANどちらのタイプを使用するかは、
 * ChartConfigによって指定する。
 *
 * このクラスを単体で動作させるためには次のプロパティの設定が必要。
 * <pre>
 *  System.setProperty("swe.path","c:/users/ephe/"); //必須。スイスエフェメリスの辞書ファイルの場所。
 *  System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
 * </pre>
 */
public class NatalChart {
    /**
     * getBody()で得られる天体オブジェクトはこの配列に格納されている。
     * SUNからはじまりCUSP12で終わる。
     */
    protected Body [] bodys = new Body[CUSP12+1];
    protected String [] bodysError = new String[CUSP12+1];
    private SwissEph swissEph;
    private TimePlace timePlace;
    String houseSystemName = null;
    static final int ASC_OFFSET = 0;
    static final int MC_OFFSET = 1;
    static final int ARMC_OFFSET = 2;
    static final int VERTEX_OFFSET = 3;
    //地球を除く惑星と小惑星。ノードやリリスは除外したリスト。
    static final int [] planets = { SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
    URANUS,NEPTUNE,PLUTO,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA };

    ChartConfig cc;
    int group = 0;
    /**
     * デフォルトのプロパティ設定でオブジェクトを作成する。
     * groupは0が設定される。
     */
    public NatalChart() {
        cc = new ChartConfig();
        swissEph = Ephemeris.getInstance().getSwissEph();
    }
    /**
     * 計算設定情報を指定してオブジェクトを作成する。
     * @param cc
     * @param group N,P,Tを表す値。このクラスによって求まるBodyオブジェクトのgroup
     * フィールドにはこの値がセットされる。
     */
    public NatalChart( ChartConfig cc, int group ) {
        this.cc = cc;
        this.group = group;
        Ephemeris eph = Ephemeris.getInstance();
        swissEph = eph.getSwissEph();
    }
    /**
     * このオブジェクトの設定オブジェクトを返す。
     */
    public ChartConfig getChartConfig() {
        return cc;
    }
    /**
     * このオブジェクトに設定オブジェクトをセットする。再計算は行わないので、
     * 設定を反映したデータを取得したい場合はsetTimePlace()を再実行すること。
     */
    public void setChartConfig( ChartConfig cc ) {
        this.cc = cc;
    }
    /**
     * 日時と場所をセットする。このタイミングで天体位置は計算され、それをgetBody()
     * で取得可能となる。
     */
    public void setTimePlace( TimePlace timePlace ) {
        setTimePlace( timePlace, this.planets );
    }
//    public void setTimePlace( TimePlace timePlace ) {
//        this.timePlace = timePlace;
//        if ( timePlace.getLongitude() != null ) { //場所がセットされているとき
//            boolean isSetPlace = true;
//            boolean isSetTime = timePlace.getTime() != null;
//            calc( timePlace.getJDay(), timePlace.getLatitude(), timePlace.getLongitude(),
//                  isSetPlace, isSetTime, planets );              //場所つきで計算
//        } else if ( ! cc.isPrioritizeSolar() ) {
//            //場所が非セットで、ソーラー系ハウスが指定されていないときは、
//            //デフォルトの観測地と地方時で計算する。
//            TimePlace tp = new TimePlace( timePlace );
//            tp.setPlace( cc.getDefaultPlace() );
//            boolean isSetPlace = true;
//            boolean isSetTime = true;
//            isSetPlace = true;
//            calc( tp.getJDay(), tp.getLatitude(), tp.getLongitude(),
//                  isSetPlace, isSetTime, planets );
//        } else { //場所なしで計算
//            boolean isSetTime = timePlace.getTime() != null;
//            boolean isSetPlace = false;
//            calc( timePlace.getJDay(), 0, 0, isSetPlace, isSetTime, planets );
//        }
//    }
    /**
     * 日時と場所をセットする。天体IDを配列で指定することで、必要な天体のみを計算し、
     * 計算時間を稼ぐことができる。指定できる天体は、１０惑星と小惑星に限られる。
     * またこの配列の中で太陽はかならず指定しなければならない。
     * AC,MC,バーテックスなどは指定してはならない。
     */
    public void setTimePlace( TimePlace timePlace, int [] planets ) {
        this.timePlace = timePlace;
        //double jday = timePlace.getJDay();
        if ( timePlace.getLongitude() != null && timePlace.getTime() != null ) {
            //     時刻と場所の両方がセットされているとき
            boolean isSetPlace = true;
            boolean isSetTime = timePlace.getTime() != null;
            calc( timePlace.getJDay(),
                  timePlace.getLatitude(),
                  timePlace.getLongitude(),
                  isSetPlace,
                  isSetTime,
                  planets );
        } else if ( ! cc.isPrioritizeSolar() ) {
            //     場所が非セットで、ソーラー系ハウスが指定されていないときは、
            //     デフォルトの観測地と地方時で計算する。
            TimePlace tp = new TimePlace( timePlace );
            tp.setPlace( cc.getDefaultPlace() );
            boolean isSetPlace = true;
            boolean isSetTime = true;
            isSetPlace = true;
            calc( tp.getJDay(),
                  tp.getLatitude(),
                  tp.getLongitude(),
                  isSetPlace,
                  isSetTime,
                  planets );
        } else {
            //     場所なしで計算
            boolean isSetTime = timePlace.getTime() != null;
            boolean isSetPlace = false;
            calc( timePlace.getJDay(), 0, 0, isSetPlace, isSetTime, planets );
        }
    }
    /**
     * このオブジェクトに日時と場所をTimePlaceオブジェクトで返す。
     * (setTimePlace()でセットしたものと同じもの)。
     */
    public TimePlace getTimePlace() {
        return timePlace;
    }
    /**
     * 1日1年法での天体の位置を返す。このオブジェクトにセットされている日時を出生
     * 時刻とみなし、引数で与えられたtransit時刻での天体の進行位置を求める。
     * このメソッドは実行速度が速いのが利点。
     * @param transit 日時をTimePlaceオブジェクトで指定する。場所は無視される。
     * @param body_id 天体ID。スイスエフェメリスが対応している天体IDに限る。
     * @exception IllegalArgumentException 天体暦範囲外の日時がセットされた場合や、
     * 天文暦ファイルが見つからない(これも範囲外ということだが)場合
     */
    public Body getBodyBySecondaryProgression( TimePlace transit, int body_id )
                                              throws IllegalArgumentException {
        double natalJDay = timePlace.getJDay();
        //System.out.println("natalJDay = " + natalJDay);
        double dayofs = (transit.getJDay() - natalJDay) / SOLAR_YEAR;
        TimePlace progTimePlace = new TimePlace(timePlace);
        GregorianCalendar cal = JDay.getCalendar(
            natalJDay + dayofs, timePlace.getTimeZone() );
        progTimePlace.setCalendar( cal, TimePlace.DATE_AND_TIME );
        //System.out.println("progTimePlace = " + progTimePlace);
        double jd = progTimePlace.getJDay();
        double ET = jd + SweDate.getDeltaT(jd);
        //System.out.println("ET = " + ET);
        double [] results = new double[6];
        StringBuffer err_sb = new StringBuffer();
        int flag = SweConst.SEFLG_SPEED;
        int ret_flag = swissEph.swe_calc( ET, body_id, flag, results, err_sb );
        if ( ret_flag<0 || ret_flag != flag ) {
            throw new IllegalArgumentException( err_sb.toString() );
        }
        return new Body(body_id,results,group);
    }
    /**
     * 指定された天体の位置を求める。AC,MC,VERTEXなどの仮想天体は求める事ができない。
     * SweConstのSE_SUN等、SE_で始まる天体のみ可能。 このメソッドの利点は、
     * 一つの天体だけを計算するので速いこと。
     * @param timePlace 日時と場所
     * @param se_body_id SweConstのSE_SUN等、SE_で始まる天体のみ可能。
     * @return 求まったBodyオブジェクトのgroupフィールドは0。
     */
    public static Body getBody( TimePlace timePlace, int se_body_id ) {
        double jd = timePlace.getJDay();
        double ET = jd + SweDate.getDeltaT(jd);
        double [] results = new double[6];
        StringBuffer err_sb = new StringBuffer();
        int flag = SweConst.SEFLG_SPEED;
        int ret_flag = Ephemeris.getInstance().getSwissEph().
            swe_calc(ET,se_body_id,flag,results,err_sb);
        if ( ret_flag<0 || ret_flag != flag ) {
            throw new IllegalArgumentException(err_sb.toString());
        }
        return new Body( se_body_id, results );
    }

    /**
     * このオブジェクトにセットされている日時を出生日時とみなし、引数で与えられた
     * transit時刻から進行計算用の日時を求める。
     * 出生日時より過去の日時を指定しても問題なく計算できる。
     */
    public TimePlace getSecondaryProgressionTimePlace( TimePlace transit ) {
        double natalJDay = timePlace.getJDay();
        double dayofs = (transit.getJDay() - natalJDay) / SOLAR_YEAR;
        TimePlace progTimePlace = new TimePlace(timePlace);
        GregorianCalendar cal = JDay.getCalendar(
            natalJDay + dayofs, timePlace.getTimeZone() );
        progTimePlace.setCalendar(cal,TimePlace.DATE_AND_TIME);
        return progTimePlace;
    }

    /**
     * ユリウス日、[緯度、経度]から天体位置を計算して、bodys[]に格納する。
     * @param isSetPlace 緯度・経度に有効な値を渡したときはtrueを指定する。
     * 緯度経度が不明なときはfalseを指定する。
     * @param isSetTime 日付は確定していても時刻が不明の場合はfalseを指定する。
     * 確定しているならtrueを指定する。
     */
    private void calc( double jd,
                         double lat,
                         double lon,
                         boolean isSetPlace,
                         boolean isSetTime,
                         int [] planets ) {
        for ( int i=0; i<bodys.length; i++ ) {
            bodys[i] = null;
            bodysError[i] = null;
        }
        double ET = jd + SweDate.getDeltaT(jd); //暦表時(ET) = jday + ΔT
        //System.out.println("calc() ET = " + ET);
        double [] results = new double[6]; // (ﾙｰﾌﾟ内で毎回newしたほうが無難だが)
        //惑星と小惑星の感受点を一気に求める
        for ( int id : planets ) {
            StringBuffer err_sb = new StringBuffer();
            int flag = SweConst.SEFLG_SPEED;
            int ret_flag = swissEph.swe_calc(ET,id,flag,results,err_sb);
            if ( ret_flag<0 || ret_flag != flag ) { //エラー発生
                bodysError[id] = err_sb.toString();
            } else bodys[id] = new Body( id, results, group );
        }
        //ノードとリリスと、そのアンチポイントを求める
        int [] p = new int[2];
        p[0] = cc.isUseMeanNode() ? NODE : TRUE_NODE;
        p[1] = cc.isUseMeanApogee() ? APOGEE : OSCU_APOGEE;
        int [] q = new int [] { NODE,APOGEE };
        int [] Q = new int [] { SOUTH_NODE,ANTI_APOGEE };
        for ( int i=0; i<p.length; i++ ) {
            int id = p[i];
            StringBuffer err_sb = new StringBuffer();
            int flag = SweConst.SEFLG_SPEED;
            int ret_flag = swissEph.swe_calc(ET,id,flag,results,err_sb);
            //System.out.println("flag = " + flag + " , ret_flag = " + ret_flag);
            if ( ret_flag<0 || ret_flag != flag ) { //エラー発生
                //bodys[q[i]] = null;
                bodysError[q[i]] = err_sb.toString();
                bodysError[Q[i]] = err_sb.toString();
                //System.out.println(bodysError[q[i]]);
            } else {
                bodys[q[i]] = new Body(q[i],results,group);
                bodys[Q[i]] = new Body(Q[i],getAntiPoints(results),group); //アンチポイントも求める
            }
        }
        //ここよりハウスカスプやバーテックスの計算
        double [] cusps = new double[13]; //カスプ保管用[1〜12]
        double [] ascmc = new double[10];	//AscやMCの値が書き込まれる。

        char houseSystemCode = cc.getHouseSystemCode();
        //System.out.println("HouseSystemCode = " + cc.getHouseSystemCode());
        char cuspUnkownHouseSystem = cc.getCuspUnknownHouseSystem();
        //System.out.println("cuspUnkownHouseSystem = " + cuspUnkownHouseSystem );
        //ソーラーまたはソーラーサインが明示的に指定されているときはtrueのフラグ
        boolean isSolarHouse = houseSystemCode == '2' || houseSystemCode == '1';
        if ( isSetPlace) {
            if ( ! isSolarHouse ) {
                swissEph.swe_houses( jd,0,lat,lon,houseSystemCode,cusps,ascmc);
                for ( int i=1,j = CUSP1; i<cusps.length; i++,j++ )
                    bodys[j] = new Body(j,cusps[i],group,i);
                for ( int i=0; i<HOUSE_SYSTEM_CODES.length; i++ ) {
                    if ( HOUSE_SYSTEM_CODES[i] == houseSystemCode )
                        houseSystemName = HOUSE_SYSTEM_NAMES[i];
                }
            } else swissEph.swe_houses( jd,0,lat,lon,'E',cusps,ascmc); //AC,MCは求める
        }
        if ( houseSystemCode == '1' || (cuspUnkownHouseSystem == '1' && ! isSetPlace) ) { //ソーラーサイン分割のカスプを求める
            double angle = ((int)( bodys[ SUN ].lon / 30d) ) * 30d; //必ずサイン0度の位置でカスプを求める
            for ( int i = CUSP1, j = 1; i <= CUSP12; i++ ) {
                bodys[ i ] = new Body(i,angle, group, i - CUSP1 + 1);
                cusps[ j++ ]= angle;
                angle = (angle + 30d) % 360d;
            }
            houseSystemName = HOUSE_SYSTEM_NAMES[ 6 ];
        } else if ( houseSystemCode == '2' || ( cuspUnkownHouseSystem == '2' && ! isSetPlace) ) { //ソーラー分割のカスプを求める
            double angle = bodys[ SUN ].lon;
            for ( int i = CUSP1, j = 1; i <= CUSP12; i++ ) {
                bodys[ i ] = new Body( i, angle, group, i - CUSP1 + 1);
                cusps[ j++ ] = angle;
                angle = ( angle + 30d ) % 360d;
            }
            houseSystemName = HOUSE_SYSTEM_NAMES[ 7 ];
        }
        if ( isSetPlace ) { //VERTEXは特例ではじめにセット。AC,MCは後で。
            bodys[VERTEX] = new Body( VERTEX, ascmc[ VERTEX_OFFSET ], group );
            bodys[ANTI_VERTEX] = new Body(
                ANTI_VERTEX, ( ascmc[ VERTEX_OFFSET ] + 180 ) % 360, group ) ;
        }
        //各天体にハウス番号をセット
        double [] cuspbuf = new double[ cusps.length + 1 ];
        System.arraycopy( cusps, 0, cuspbuf, 0, cusps.length );
        cuspbuf[ cusps.length ] = cusps[1];
        for ( int id = 0; id < bodys.length; id++ ) {
            Body b = bodys[id];
            if ( b == null ) continue;
            for ( int i=1; i < cusps.length; i++ ) {
                if ( cuspbuf[i] > cuspbuf[i+1] ) { // 特例
                    if ( ( b.lon >= cuspbuf[i] && b.lon < 360d) ||
                        (b.lon >= 0 && b.lon < cuspbuf[i+1] )) b.house = i;
                } else {
                    if ( b.lon >= cuspbuf[i] && b.lon < cuspbuf[i+1] ) {
                        b.house = i;
                        break;
                    }
                }
            }
        }
        if ( isSetPlace ) {
            //場所情報が設定されているならAC,MCは確実に求まってるのでコピー
            //スペシャルポイントをセット
            bodys[AC] = new Body(AC,ascmc[ASC_OFFSET],group,1);
            bodys[MC] = new Body(MC,ascmc[MC_OFFSET],group,10);
            //bodys[VERTEX] = new Body(VERTEX,ascmc[VERTEX_OFFSET],group);
            bodys[DC] = new Body(DC,(ascmc[ASC_OFFSET] + 180) % 360,group,7);
            bodys[IC] = new Body(IC,(ascmc[MC_OFFSET] + 180) % 360,group,4);
            //bodys[ANTI_VERTEX] = new Body(ANTI_VERTEX,(ascmc[VERTEX_OFFSET] + 180) % 360,group);
        }
    }

    //黄経、黄緯のアンチポイントを計算。参照書き換え。
    private double [] getAntiPoints( double [] results ) {
        results[0] = ( results[0] + 180 ) % 360;
        results[1] = ( results[1] + 180 ) % 360;
        return results;
    }
    /**
     * 指定された天体IDの天体をBodyオブジェクトで返す。戻り値がnullの場合は、
     * getbodysError()で、取得できない理由を知る事ができる。
     */
    public Body getBody( int id ) {
        return bodys[id];
    }
    /**
     * 指定された天体の位置が算出できていない場合は、ErrorBodyオブジェクトを返す。
     * 算出できている場合はnullを返す。
     * @param id 天体ID
     */
    public ErrorBody getErrorBody( int id ) {
        if ( bodys[id] != null ) return null;
        return new ErrorBody( id, bodysError[id] );
    }
//  /**
//   * 指定された天体IDのリストから、天体オブジェクト(Body)のリストを返す。
//   */
//  public List<Body> getBodyList(List<Integer> idList) {
//    List<Body> list = new ArrayList<Body>();
//    for(int id : idList) {
//      Body body = getBody(id);
//      if(body == null) continue;
//      list.add(body);
//    }
//    return list;
//  }
    /**
     * 指定された天体IDの配列から、天体オブジェクト(Body)のリストを返す。
     * 日時(TimePlace)がセットされていない場合はサイズ０のリストを返す。
     * 天文暦の範囲外で位置が求められなかった天体は、返されるリストからは除外され
     * る。除外された天体のエラー情報は、getErrorList()で取得できる。
     * @param ids 取得する天体のIDリスト
     */
    public List<Body> getBodyList( int [] ids ) {
        List<Body> list = new ArrayList<Body>();
        if ( timePlace == null ) return list;
        for ( int id : ids ) {
            Body body = getBody(id);
            if ( body == null ) continue;
            list.add(body);
        }
        return list;
    }
    /**
     * 指定された天体IDから天体オブジェクト(Body)を取得し、ハッシュマップに
     * 格納して返す。
     */
    public Map<Integer,Body> getBodyMap( int [] ids ) {
        Map<Integer,Body> map = new HashMap<Integer,Body>();
        if ( timePlace == null ) return map;
        for ( int id : ids ) {
            Body body = getBody(id);
            if ( body == null ) continue;
            map.put( id, body );
        }
        return map;
    }
    /**
     * 指定された天体IDの配列から、エラーが発生して位置を求められなかった天体が
     * あれば、そのリストを返す。エラーが無い場合は、size()が0のリストが返る。
     * @param ids 天体IDの配列
     * @return ErrorBodyのリスト
     */
    public List<ErrorBody> getErrorList(int [] ids) {
        List<ErrorBody> errList = new ArrayList<ErrorBody>();
        for ( int id : ids ) {
            Body body = getBody(id);
            if ( body == null && bodysError[id] != null )
                errList.add( new ErrorBody( id, bodysError[id], group) );
        }
        return errList;
    }

    /**
     * ハウスカスプをBodyオブジェクトで返す。
     * 日時がセットされていない場合は、牡羊座から30度づつ分割されたリストを返す。
     */
    public List<Body> getCuspList() {
        List<Body> list = new ArrayList<Body>();
        if ( timePlace == null ) {
            for ( int i = CUSP1, j = 0; i <= CUSP12; i++,j++ ) {
                list.add( new Body(i, j*30, group , j+1) );
            }
            return list;
        }
        for ( int i = CUSP1; i <= CUSP12; i++ ) list.add( getBody(i) );
        return list;
    }
    /**
     * ハウス分割法の名前を返す。時刻や場所の不在によって、指定されたハウス分割法
     * が使用できない場合があり、そのときは分割法がソーラー系のものに自動的に切り替わる。
     * ゆえにこのメソッドは一度はsetTimePlace()を実行されて、位置計算が行われるまで
     * 確定せず、確定していない状態のときこのメソッドはnullを返す。
     */
    public String getHouseSystemName() {
        return houseSystemName;
    }
    /**
     * 現在セットされているTimePlaceの日時から計算して、指定された天体が指定の位置
     * にくる日時を計算して返す。
     * @param id 天体ID
     * @param targetAngle 移動させたい黄経
     * @param isBackwards 過去に向かって検索する場合はtrueを指定する。
     */
    public TimePlace search( int id, double targetAngle, boolean isBackwards) {
        //ﾉｰﾄﾞとｱﾎﾟｼﾞｰの場合は、ﾄﾙｰ&ﾐｰﾝの設定値のﾀｲﾌﾟに応じてIDを変更
        if ( id == NODE && ! cc.isUseMeanNode() ) id++;
        else if ( id == APOGEE && ! cc.isUseMeanApogee() ) id++;
        double jd = timePlace.getJDay();
        double ET = jd + SweDate.getDeltaT(jd); //暦表時(ET) = jday + ΔT
        StringBuffer sb = new StringBuffer();
        int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
        TransitCalculator tc = new TCPlanet(swissEph,id,flags, targetAngle);
        double nextTransitET = swissEph.getTransitET(tc, ET, isBackwards); //日時計算
        //求まった日時からTimePlaceオブジェクトを作成
        SweDate tDate = new SweDate(nextTransitET);
        GregorianCalendar gcal = JDay.getCalendar(tDate.getJulDay(),timePlace.getTimeZone());
        TimePlace tp = new TimePlace(timePlace);
        tp.setCalendar(gcal,TimePlace.DATE_AND_TIME);
        return tp;
    }



//    public static void test() {
//
//    }
    public static void main(String args[]) {
        System.setProperty("swe.path","c:/users/ephe/");
        System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
        NatalChart c = new NatalChart();
        c.group = 1;
        //暦の一番最後の日時を指定
        TimePlace tp = TestConst.getTimePlaceByUTC(TestConst.AD,1540,7-1,4,0,0,0);
        //TimePlace tp = TestConst.getTimePlaceByUTC(TestConst.AD,5400,1-1,19,12,54,48);
//    System.out.println(NatalChart.getBody(tp,SUN));
//    System.exit(0);

        c.setTimePlace(tp);
        System.out.println(c.getChartConfig().toString());
        System.out.println(tp.toString());
        int [] bodys = { AC,MC,SUN,MOON,NODE,CHIRON,PHOLUS };
        List<ErrorBody> errList = c.getErrorList(bodys);
        List<Body> list = c.getBodyList(bodys);
        for(Body body : list)
            System.out.println(body.toString());
        if(errList.size()>0) {
            System.out.println("エラーで取得できなかった天体");
            for(ErrorBody eb : errList) {
                System.out.println(eb.toString());
            }
        }
        System.out.println("----------------------");
        TimePlace timePlace = TestConst.getTimePlaceByUTC(TestConst.AD,5419,12-1,31,12,58,11);
        Body body = c.getBodyBySecondaryProgression(timePlace,SUN);
        System.out.println(body);
    }
    // AD5399-12-31 12:58:10 UTCは天体位置が求まるが1秒先は暦範囲外となる。
    // ところがgetBodyBySecondaryProgression()でAD5419-12-31,12:58:11 UTCを求めると、
    // 進行日時は5400-01-20 12:54:49UTCで、なぜかこの日付の太陽の位置を求めることができる。
    // しかしこの日付をsetTimePlace()で指定しても暦範囲外となる。
    // 進行は限界値より少しオーバーしても天体位置が求まるのが謎。
    // 進行位置がもとまってその日付をsetTimePlace()すると今度は求まらないということになる。
    // これだと暦の範囲外に出たときの判定チェックができないことを意味する。
    // getBody(TimePlace,body_id)メソッドでも、AD5419-12-31 12:58:11 UTCはエラーになる。
    // なぜ進行計算のとき、オーバーしても計算できるのか謎。

// getBodyList()するたびに、新たにArrayListが生成されヒープを消費してしまう。
// これを改善したいが、getBodyList()に与えられる引数が変化することもあり、その
// ときは配列を作りなおす必要がある。また引数が変化したことを事前に検査するのは
// それはそれでコストがかかる。そんなわけで今の状態にある。

}
