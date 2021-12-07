/*
 * NPTChart.java
 * Created on 2007/07/03, 23:55
 *
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Unit;

/**
 * ネイタルとトランジットの日時・場所の入力を持ち、ネイタルとトランジットの
 * 天体位置と、さらにプログレスの天体位置を返すメソッドをもつクラス。
 * また各天体の位置を移動させたとき、その日時を逆算するメソッドもある。
 * つまり三重円を描くために必要なパラメターを全て求める事ができるクラス。
 *
 * 指定された日時が暦範囲外で位置が求められない天体は、getBodyList()メソッド
 * の戻り値から消える。そのときgetErrorList()で、求められなかった天体のリスト
 * を取得することができる。
 * @author 大澤義鷹
 */
public class NPTChart {
    public static final int NATAL = 0;
    public static final int PROGRESS = 1;
    public static final int TRANSIT = 2;
    /**
     * 進行法の1度1年法を表す定数'P'
     */
    public static final char PRIMARY_PROGRESSION = 'P';
    /**
     * 進行法の1日1年法を表す定数'S'
     */
    public static final char SECONDARY_PROGRESSION = 'S';
    /**
     * 進行法のソーラーアーク法を表す定数'A'
     */
    public static final char SOLAR_ARC_PROGRESSION = 'A';
    /**
     * 進行法のコンポジット法を表す定数'C'
     */
    public static final char COMPOSIT_PROGRESSION = 'C';
    /**
     * 進行法の名前
     */
    public static final String [] PROGRESS_NAMES = {
        "1度1年法",
        "1日1年法",
        "ソーラーアーク法",
        "松村潔法"//コンポジット法"
    };
    /**
     * 進行法を表すコード表
     */
    public static final char [] PROGRESS_CODES = {
        PRIMARY_PROGRESSION,
        SECONDARY_PROGRESSION,
        SOLAR_ARC_PROGRESSION,
        COMPOSIT_PROGRESSION
    };
    char progMode = SECONDARY_PROGRESSION;
    NatalChart natalChart;
    NatalChart transitChart;
    Data data;
    Transit transit;
    //進行天体はこの配列に格納する。進行位置は、一部の進行法をのぞいてみな人為的
    //なものなので、Chartオブジェクトとしては扱えない。
    Body [] progBodys = new Body[CUSP12+1];
    String [] progBodysError = new String[CUSP12+1];
    //外惑星群
    static final int [] trans_mars = { MARS,JUPITER,SATURN,URANUS,
    NEPTUNE,PLUTO,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA };
    //内惑星群
    static final int [] inner_planets = { SUN,MOON,MERCURY,VENUS };
    
    /**
     * NPTChart オブジェクトを作成する
     */
    public NPTChart() {
        natalChart = new NatalChart();
        transitChart = new NatalChart();
    }
    
    /**
     * プロパティを指定してオブジェクトを作成する。
     */
    public NPTChart( ChartConfig cc ) {
        natalChart = new NatalChart(cc,NATAL);
        transitChart = new NatalChart(cc,TRANSIT);
    }
    
    /**
     * ChartConfigオブジェクトを返す。
     */
    public ChartConfig getChartConfig() {
        return natalChart.getChartConfig();
    }
    
    /**
     * ネイタル(日時・場所)情報をセットする。このタイミングで進行天体も計算される。
     * トランジットには影響なし。
     */
    public void setData( Data data ) {
        this.data = data;
        natalChart.setTimePlace(data.getTimePlace());
        calcProg(); //進行計算
    }
    
    /**
     * トランジット(日時・場所)情報をセットする。このタイミングで進行天体も含めて
     * 計算される。ネイタルには影響なし。
     */
    public void setTransit( Transit transit ) {
        this.transit = transit;
        transitChart.setTimePlace(transit);
        calcProg(); //進行計算
    }
    
    /**
     * ネイタルのDataを返す。
     */
    public Data getData() {
        return data;
    }
    
    /**
     * トランジットを返す。
     */
    public Transit getTransit() {
        return transit;
    }
    
    /**
     * 進行法をセットする。
     * @param mode フィールド定数の中からセット
     * @exception IllegalArgumentException サポートされていないハウス分割法コード
     * が指定されたとき。
     */
    public void setProgressMode( char mode ) {
        for ( int i=0; i<PROGRESS_CODES.length; i++ ) {
            if ( PROGRESS_CODES[i] == mode ) {
                progMode = mode;
                return;
            }
        }
        throw new IllegalArgumentException(
            "サポートされていないハウス分割法コード : " + mode );
    }
    
    /**
     * 進行法を返す。
     */
    public int getProgressMode() {
        return progMode;
    }
    
    /**
     * 進行法の名前を返す。
     */
    public String getProgressMethodName() {
        for ( int i=0; i<PROGRESS_CODES.length; i++ ) {
            if ( PROGRESS_CODES[i] == progMode ) return PROGRESS_NAMES[i];
        }
        return null;
    }
    
    //進行計算
    void calcProg() {
        if ( ! ( data != null && transit != null ) ) return;
        switch( progMode ) {
            case PRIMARY_PROGRESSION   : primaryProgression(); break;
            case SECONDARY_PROGRESSION : secondaryProgression(); break;
            case SOLAR_ARC_PROGRESSION : solarArcProgression(); break;
            case COMPOSIT_PROGRESSION  : compositProgression(); break;
        }
        //出生と経過両方の日時がセットされていなければ計算しない
    }
    
    /**
     * 一度一年法での天体位置を求める。ネイタル天体が暦範囲外で算出できない場合は、
     * 進行位置も算出できず、progBodysにセットされる値もnullとなる。
     * この場合進行計算におけるエラーとはしない。
     * トランジットにネイタルより過去の時刻を指定しても問題なし。
     */
    void primaryProgression() {
        TimePlace natalTimePlace = data.getTimePlace();
        double angle = (transit.getJDay() - natalTimePlace.getJDay()) / SOLAR_YEAR;
        for ( int i=0; i<progBodys.length; i++ ) { //カスプも同じ理屈で進行させる
            progBodysError[i] = natalChart.bodysError[i];
            progBodys[i] = null;
            if ( natalChart.bodys[i] == null ) continue;
            progBodys[i] = new Body(natalChart.bodys[i]); //値を書き換えるのでディープコピー
            progBodys[i].group = PROGRESS;
            progBodys[i].lon = Unit.circularRound( progBodys[i].lon + angle );
        }
    }
    
    /**
     * 1日1年法で進行位置を求めprogBodys[]にセット。進行日時の天体位置を算出できない
     * 場合は、progBodysError[]にエラー情報が格納され、progBodys[]にセットされる値は
     * nullとなる。トランジットにネイタルより過去の時刻を指定しても問題なし。
     */
    void secondaryProgression() {
        TimePlace progTimePlace = 
            natalChart.getSecondaryProgressionTimePlace(transit);
        NatalChart progChart = new NatalChart( natalChart.getChartConfig(), PROGRESS );
        progChart.setTimePlace( progTimePlace );
        //結果をシャローコピー
        for ( int i=0; i<progBodys.length; i++ ) {
            progBodys[i] = progChart.bodys[i];
            progBodysError[i] = progChart.bodysError[i];
        }
    }
    
    /**
     * ソーラーアーク進行法での天体位置を求めprogBodys[]にセット。
     * これはSecondayProgression(1日1年法)で太陽が進行した角度をもとめ、それを
     * ネイタルの全感受点に加算する方法。
     * 現状ではネイタルから約360年以上先または前のプログレスは計算できず、それを超
     * えるとすべての進行位置は計算不能で、progBodys[]はすべてnull、progBodysError[]には
     * エラーメッセージが格納される。
     * ネイタルより過去のトランジットを与えても問題なく計算される。
     */
    void solarArcProgression() {
        TimePlace natalTimePlace = data.getTimePlace();
        if( ( Math.abs(transit.getJDay() 
            - natalTimePlace.getJDay() ) / SOLAR_YEAR) >= 360) {
            //360年以上先の進行は未対応。太陽が何周まわったかを求めるのは少々困難なため。
            for ( int i=0; i < progBodys.length; i++ ) {
                progBodys[i] = null;
                progBodysError[i] = 
                    "The limit of the SolarArc calculation was exceeded. ";
            }
            return;
        }
        try {
            Body sun = natalChart.getBody(SUN); //ネイタルが求まらない時点でアウト
            if(sun == null) throw new IllegalArgumentException(
                "The limit of the SolarArc calculation was exceeded. "); //暦外エラー
            //負の進行で、暦外になったときもエラーがthrowされる。
            double p = natalChart.getBodyBySecondaryProgression(transit,SUN).lon;
            double n = sun.lon;
            double arc = p - n;
            if ( p < n ) arc += 360;
            for ( int i=0; i<progBodys.length; i++ ) { //カスプも同じ理屈で進行させる
                progBodysError[i] = natalChart.bodysError[i];
                progBodys[i] = null;
                if ( natalChart.bodys[i] == null ) continue;
                //値を書き換えるのでディープコピー
                progBodys[i] = new Body( natalChart.bodys[i] );
                progBodys[i].group = PROGRESS;
                progBodys[i].lon = Unit.circularRound(progBodys[i].lon + arc);
            }
        } catch ( IllegalArgumentException e ) {
            for ( int i=0; i<progBodys.length; i++ ) {
                progBodys[i] = null;
                progBodysError[i] = e.getMessage();
            }
        }
    }
    
    /**
     * コンポジット方式の進行法で天体位置を求める。
     * 火星とそれ以遠の天体(小惑星も)は１度１年法(PrimaryProgression)で、
     * 太陽から金星、ハウスカスプ、AC,MC、VERTEX等、仮想的な感受点は、
     * 一日一年法で天体位置を求める。
     * ネイタルより過去の日時を指定しても問題なく計算できる。
     */
    void compositProgression() {
        NatalChart progChart = new NatalChart( natalChart.getChartConfig(), PROGRESS );
        TimePlace prog_tp = natalChart.getSecondaryProgressionTimePlace(transit);
        //内惑星のみ計算するようinner_planetsを指定。
        progChart.setTimePlace(prog_tp,inner_planets);
        for ( int i=0; i<progBodys.length; i++ ) {
            progBodysError[i] = progChart.bodysError[i];
            progBodys[i] = progChart.bodys[i];
        }
        //外惑星は1度1年法で計算
        TimePlace natalTimePlace = data.getTimePlace();
        double angle = (transit.getJDay() - natalTimePlace.getJDay()) / SOLAR_YEAR;
        for ( int id : trans_mars ) {
            progBodysError[id] = natalChart.bodysError[id];
            progBodys[id] = null;
            if(natalChart.bodys[id] == null) continue;
            progBodys[id] = new Body(natalChart.bodys[id]); //ディープコピー
            progBodys[id].group = PROGRESS;
            progBodys[id].lon = Unit.circularRound( progBodys[id].lon + angle );
        }
        houseRecalc( trans_mars );
    }
    
    /**
     * 進行させてlonの値が更新された天体のハウス番号を再計算する。
     * @param body_id 再計算する天体IDを列挙した配列
     */
    private void houseRecalc(int [] body_id) {
        double [] cuspbuf = new double[13]; //カスプは12+1という前提
        //進行カスプを配列にコピー
        for ( int id = CUSP1, i = 1; id <= CUSP12; id++, i++ )
            cuspbuf[i] = getBody( id, PROGRESS).lon;
        cuspbuf[12] = getBody( CUSP1, PROGRESS ).lon; //尻尾くわえたヘビにする
        //指定された天体のハウス番号を順番に計算
        for( int id : body_id) {
            Body b = progBodys[id];
            if( b == null ) continue; //通常ありえないはずだが念のため
            for(int i=1; i < 12; i++) {
                if(cuspbuf[i] > cuspbuf[i+1]) { // 魚から牡羊にまたがるケース
                    if(( b.lon >= cuspbuf[i] && b.lon < 360d) ||
                        (b.lon >= 0 && b.lon < cuspbuf[i+1] )) b.house = i;
                } else {
                    if(b.lon >= cuspbuf[i] && b.lon < cuspbuf[i+1]) {
                        b.house = i;
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * 指定された天体IDの天体位置を返す。
     * @param id 天体ID
     * @param npt NATAL,PROGRESS,TRANSITのいずれか。
     */
    public Body getBody(int id,int npt) {
        switch(npt) {
            case NATAL    : return natalChart.getBody(id);
            case PROGRESS : return progBodys[id];
            case TRANSIT  : return transitChart.getBody(id);
        }
        throw new java.lang.IllegalArgumentException("Bad npt argument " + npt);
    }
    
    /**
     * 指定した複数の天体位置を返す。
     * 日時がセットされていないときはサイズ０のリストを返す。
     * @param planets 天体IDを格納したリスト
     * @param npt     NATAL,PROGRESS,TRANSITのいずれか
     */
    public List<Body> getBodyList(int [] planets,int npt) {
        switch(npt) {
            case NATAL    : return natalChart.getBodyList(planets);
            case PROGRESS :
                List<Body> list = new ArrayList<Body>();
                for(int id : planets) {
                    if(progBodys[id] != null) list.add(progBodys[id]);
                }
                return list;
            case TRANSIT  :
                return transitChart.getBodyList(planets);
        }
        throw new java.lang.IllegalArgumentException("Bad npt argument " + npt);
    }
    
    /**
     * 指定した天体の中で位置が求められなかった天体のエラー情報を返す。
     * @param planets 天体IDの配列
     * @param npt NATAL,PROGRESS,TRANSITのいずれか
     */
    public List<ErrorBody> getErrorList(int [] planets,int npt) {
        switch(npt) {
            case NATAL    : return natalChart.getErrorList(planets);
            case PROGRESS :
                List<ErrorBody> list = new ArrayList<ErrorBody>();
                for ( int id : planets ) {
                    if ( progBodys[id] == null && progBodysError[id] != null )
                        list.add(new ErrorBody(id,progBodysError[id]));
                }
                return list;
            case TRANSIT  : return transitChart.getErrorList(planets);
        }
        throw new IllegalArgumentException("Bad npt argument " + npt);
    }
    
    /**
     * カスプを返す。
     * 日時がセットされていない場合は牡羊座から30度づつ等分割されたカスプを返す。
     * @param npt NATAL,PROGRESS,TRANSITのいずれかを指定する。
     */
    public List<Body> getCuspList(int npt) {
        switch(npt) {
            case NATAL    : return natalChart.getCuspList();
            case PROGRESS :
                List<Body> list = new ArrayList<Body>();
                if ( progBodys[CUSP1] == null ) {
                    for ( int i = CUSP1, j = 0; i <= CUSP12; i++,j++ ) {
                        list.add( new Body(i, j*30, PROGRESS , j+1) );
                    }                    
                    return list;
                }
                for ( int i=CUSP1; i <= CUSP12; i++ ) list.add(progBodys[i]);
                return list;
            case TRANSIT  : return transitChart.getCuspList();
        }
        throw new IllegalArgumentException( "Bad npt argument " + npt );
    }
    
    /**
     * ハウス分割法の名前を返す。
     */
    public String getHouseSystemName() {
        return natalChart.getHouseSystemName();
    }
    
    /**
     * ノードタイプを表す文字列を返す。(平均値|真位置)
     */
    public String getNodeTypeName() {
        return natalChart.getChartConfig().isUseMeanNode() ? 
            "平均値" : "真位置";
    }
    
    /**
     * アポジータイプを表す文字列を返す。(平均値|密接位置)
     */
    public String getApogeeTypeName() {
        return natalChart.getChartConfig().isUseMeanApogee() ? 
            "平均値" : "密接位置";
    }
    /**
     * 現在セットされているTimePlaceの日時から計算して、指定された天体が指定の位置
     * にくる日時を計算して返す。
     * @param id 天体ID
     * @param targetAngle 移動させたい黄経
     * @param isBackwards 過去に検索するときはtrue、未来ならfalse。
     * @param npt NATAL,PROGRESS,TRANSITのいずれか
     * @exception UnsupportedOperationException nptにPROGRESSが指定され
     * た場合。現在プログレスの検索は未対応。
     * @exception IllegalArgumentException 計算結果が暦の範囲外で計算不能のとき。
     */
    public TimePlace search( int id,
                              double targetAngle,
                              boolean isBackwards,
                              int npt ) throws UnsupportedOperationException {
        switch ( npt ) {
            case NATAL :
                return natalChart.search(id,targetAngle,isBackwards);
            case PROGRESS :
                switch(getProgressMode()) {
                    case SECONDARY_PROGRESSION :
                        return getTransitBySecondaryProgression(
                            id, targetAngle, isBackwards );
                    case PRIMARY_PROGRESSION :
                        return getTransitByPrimaryProgression(
                            id, targetAngle, isBackwards );
                    case COMPOSIT_PROGRESSION :
                        return getTransitByCompositProgression(
                            id, targetAngle, isBackwards );
                    case SOLAR_ARC_PROGRESSION :
                        return getTransitBySolarArcProgression(
                            id, targetAngle, isBackwards );
                }
                throw new UnsupportedOperationException(
                    "ProgressMode '" + getProgressMode() + "' is unsupported.");
            case TRANSIT :
                return transitChart.search(id,targetAngle,isBackwards);
        }
        return null;
    }
    
    /**
     * 天体をドラッグしてその位置にくる時を計算できない感受点IDのリスト。
     */
    //仮想点、ノードやリリスのアンチポイントはドラッグできない
    public static final int [] NOT_DRAG_BODYS = { 
        AC,DC,MC,IC,VERTEX,ANTI_VERTEX,ANTI_APOGEE,
        ANTI_OSCU_APOGEE,SOUTH_NODE,TRUE_SOUTH_NODE 
    };
    
    // 太陽から火星までしかドラックはできない
    static final int [] SECONDARY_NOT_DRAG_BODYS = {
        AC,DC,MC,IC,VERTEX,ANTI_VERTEX,ANTI_APOGEE,ANTI_OSCU_APOGEE,
        NODE,TRUE_NODE,SOUTH_NODE,TRUE_SOUTH_NODE,
        JUPITER,SATURN,URANUS,NEPTUNE,PLUTO,
        CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA
    };
    
    /**
     * bodysで与えられた天体のうち、ドラッグ操作によってトランジット予測が可能な
     * 感受点のみ抽出して、Integer[]で返す。今のところAC,DC,MC,IC,VERTEX,ANTI_VERTEX,
     * ANTI_APOGEE,ANTI_OSC_APOGEE,ANIT_NODE,ANTI_NODE,ANTI_TRUE_NODEは計算がサポート
     * されていないためbodys[]に含まれていた場合は除外される。
     * @param bodys 天体IDのリスト
     * @param npt NATAL,PROGRESS,TRANSITのいずれか
     * @exception java.lang.UnsupportedOperationException nptにPROGRESSが指定され
     * た場合。現在プログレスの検索は未対応。
     */
    public int [] getDragBodys(int [] bodys,int npt) {
        switch ( npt ) {
            case NATAL :
                return excludeArray(bodys,NOT_DRAG_BODYS);
            case PROGRESS :
                switch ( getProgressMode() ) {
                    case SECONDARY_PROGRESSION :
                        return excludeArray(bodys,SECONDARY_NOT_DRAG_BODYS );
                    case PRIMARY_PROGRESSION :
                        return excludeArray(bodys,NOT_DRAG_BODYS );
                    case COMPOSIT_PROGRESSION :
                        return excludeArray(bodys,NOT_DRAG_BODYS );
                    case SOLAR_ARC_PROGRESSION :
                        return excludeArray(bodys,NOT_DRAG_BODYS );
                }
                throw new UnsupportedOperationException(
                    "ProgressMode '" + getProgressMode() + "' is unsupported.");
            case TRANSIT :
                return excludeArray(bodys,NOT_DRAG_BODYS);
        }
        return null;
    }
    
    /**
     * ドラッグ移動がサポートされない天体IDのリストを返す。
     * @param npt NATAL,PROGRESS,TRANSITのいずれか
     * @exception java.lang.UnsupportedOperationException nptにPROGRESSが指定され
     * た場合。現在プログレスの検索は未対応。
     */
    public int [] getNotDragBodys(int npt) {
        switch ( npt ) {
            case NATAL : return NOT_DRAG_BODYS;
            case PROGRESS :
                switch ( getProgressMode() ) {
                    case SECONDARY_PROGRESSION :
                        return SECONDARY_NOT_DRAG_BODYS;
                    case PRIMARY_PROGRESSION :
                        return NOT_DRAG_BODYS;
                    case COMPOSIT_PROGRESSION :
                        return NOT_DRAG_BODYS;
                    case SOLAR_ARC_PROGRESSION :
                        return NOT_DRAG_BODYS;
                }
                throw new UnsupportedOperationException(
                    "ProgressMode '" + getProgressMode() + "' is unsupported.");
            case TRANSIT : return NOT_DRAG_BODYS;
        }
        return null;
    }
    // bodys[]のリストからbodys2[]の天体を除外
    private int [] excludeArray(int [] bodys,int [] bodys2) {
        Set<Integer> set = new HashSet<Integer>();
        List<Integer> list = new ArrayList<Integer>();
        for ( int id : bodys2 ) set.add(id);
        for ( int id : bodys ) {
            if ( ! set.contains(id) ) list.add(id);
        }
        int [] result = new int[ list.size() ];
        for ( int i=0; i<list.size(); i++ ) result[i] = list.get(i);
        return result;
    }
    //1日1年法で天体が1度進むのに必要な日数。太陽〜土星まで。-1は未定義を表す。
    //実際に使用しているのは火星まで。
    private static final double [] ORBITAL_PERIODS = {
        365, 29, 248, 304, 700, 1825, 5110 };
    /**
     * 1日1年法の進行天体が指定された位置にくるトランジットの日時を求める。
     * @param id 天体ID(太陽〜火星まで)
     * @param ta 緯度先黄経(0-359.99)
     * @param isBackwards 過去に検索する場合はtrue、未来に検索する場合はfalse
     * @return トランジットの日時。
     */
    private TimePlace getTransitBySecondaryProgression( int id,
                                                         double ta,
                                                         boolean isBackwards )
                                    throws java.lang.IllegalArgumentException {
        Body progBody = getBody(id,PROGRESS);
        if ( progBody == null ) {
            throw new IllegalArgumentException("Out of range");
        }
        double prglon = progBody.lon;
        Transit transit = new Transit(getTransit());
        double tjd = transit.getJDay();
        // angleに進行天体からドラッグポイントまでの角距離を求める
        double angle = getArcLength(prglon,ta,isBackwards);
        tjd += ORBITAL_PERIODS[id] * angle * 0.8; //角距離から精密計算にかける前の概算を求める。
        double delta = ORBITAL_PERIODS[id];
        Body body = null;
        //今のとこあまり高精度で計算していない。
        if ( isBackwards ) { 
            // 過去への検索はターゲットを3度とびこした位置の時間を求め、
            // 後の処理は順方向検索で求める
            for (;;) {
                tjd -= delta;
                transit.setJDay(tjd);
                body = natalChart.getBodyBySecondaryProgression(transit,id);
                double len = getArcLength(body.lon,(ta-3) % 360,isBackwards);
                if ( len >= 359 ) break;
            }
        }
        for ( int i=0; i<5; i++ ) {
            for (;;) {
                tjd += delta;
                transit.setJDay(tjd);
                body = natalChart.getBodyBySecondaryProgression(transit,id);
                double len = getArcLength(body.lon,ta,false);
                if(len >= 359) break;
            }
            tjd -= delta; //ターゲットに達したら時間を引き戻し
            delta /= 3; //オフセットを減らして再計算。これを5回くりかえす。
        }
        System.out.println("移動　進行位置" + body);
        return transit;
    }
    /**
     * 1度1年法で指定された天体が指定位置にくるトランジットの日時を求める。
     * @param id 天体ID(太陽〜火星まで)
     * @param ta 緯度先黄経(0-359.99)
     * @param isBackwards 過去に検索する場合はtrue、未来に検索する場合はfalse
     * @return トランジットの日時
     */
    private TimePlace getTransitByPrimaryProgression( int id,
                                                       double ta,
                                                       boolean isBackwards ) {
        TimePlace natalTimePlace = data.getTimePlace();
        double prglon = getBody(id,PROGRESS).lon; //現在の進行位置を求め
        double angle = getArcLength(prglon,ta,isBackwards); //その位置から何度移動しかを求め
        double sign = isBackwards ? -1 : 1;
        TimePlace tp = new TimePlace(getTransit());
        //移動した距離に太陽年を乗じて、検索方向に応じて足すかまたは引く。それが答え。
        double jd = transit.getJDay() + angle  * SOLAR_YEAR * sign;
        jd += 0.00002; //約2秒ごまかす。これをしないとﾀｰｹﾞｯﾄに0.01度足りなくなる。
        tp.setJDay(jd); //どうやらﾕﾘｳｽ日からｶﾚﾝﾀﾞｰへの変換誤差らしい
        return tp;
    }
    
    /**
     * コンポジット法で指定された天体が指定位置にくるトランジットの日時を求める。
     * 火星とそれ以遠の天体(小惑星も)は１度１年法で、太陽から金星、ハウスカスプは
     *  一日一年法で求める。
     * @param id 天体ID。惑星、小惑星、NODE,APOGEEのID番号。
     * @param ta 緯度先黄経(0-359.99)
     * @param isBackwards 過去に検索する場合はtrue、未来に検索する場合はfalse
     * @return トランジットの日時
     */
    private TimePlace getTransitByCompositProgression( int id,
                                                        double ta,
                                                        boolean isBackwards ) {
        if ( id <= VENUS ) 
            return getTransitBySecondaryProgression( id, ta, isBackwards );
        return getTransitByPrimaryProgression( id, ta, isBackwards );
    }
    
    /**
     * ソーラーアーク法で、指定された進行天体が指定位置にくるトランジットの日時を求める。
     * @param id 天体ID。惑星、小惑星、NODE,APOGEEのID番号。
     * @param ta 緯度先黄経(0-359.99)
     * @param isBackwards 過去に検索する場合はtrue、未来に検索する場合はfalse
     * @return トランジットの日時
     */
    private TimePlace getTransitBySolarArcProgression( int id,
                                                        double ta,
                                                        boolean isBackwards ) {
        double prglon = getBody( id, PROGRESS ).lon; //現在の進行位置を求め
        //その位置から何度移動しかを求め
        double angle = getArcLength( prglon, ta, isBackwards );
        double sunlon = getBody( SUN, PROGRESS ).lon; //進行の太陽の位置を求め
        double sign = isBackwards ? -1 : 1;
        //その移動量を進行の太陽の位置に加算して
        sunlon = sunlon + (angle * sign) % 360;
        //一日一年法でその位置に太陽が来る時を求める
        return getTransitBySecondaryProgression(SUN,sunlon,isBackwards);
    }
    
    /**
     * 円周上の2点間の角距離を求める。
     * @param a 開始角度(0-359.999...)。360度より大きな値を入れても360度内にまるめて解釈される。
     * @param b 終了角度(0-359.999...)。　　　　　　　　　　　〃
     * @param isBackwards 時計回りに角度を求める場合はfalse。半時計回りの場合はtrue。
     * @return 0-359.999の値。負数が戻ることはない。
     */
    private double getArcLength(double a,double b,boolean isBackwards) {
        a = a % 360;
        b = b % 360;
        if ( isBackwards ) {
            if ( a < b ) return 360 + a - b;
            else if ( a > b ) return a - b;
        } else {
            if ( a < b ) return b-a;
            else if ( a > b) return 360 + b - a;
        }
        return 0;
    }
    
    
    
    
    //テスト用　天体位置とエラー天体を表示する。
    private static void echo(String caption,List<Body>list,List<ErrorBody> errList) {
        for(Body body : list)
            System.out.println(caption + " : " + body.toString());
        if(errList.size()>0) {
            System.out.println(caption + " : エラーで取得できなかった天体");
            for(ErrorBody eb : errList)
                System.out.println(caption + " : " + eb.toString());
        }
    }
    
    public static void test() {
        System.setProperty("swe.path","c:/users/ephe/");
        System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
        NPTChart c = new NPTChart();
        c.setProgressMode(NPTChart.SECONDARY_PROGRESSION);
        Data natalData = TestConst.getMyData(TestConst.AD,5390,12-1,31,0,0,0);
        c.setData(natalData);
        c.setTransit(TestConst.getMyTransit(TestConst.AD,5401,1-1,1,0,0,0));
        
        System.out.println(c.getChartConfig().toString());
        System.out.println("PROGRESS METHOD = " + c.getProgressMethodName());
        System.out.println("NATAL DATETIME = "+ c.getData().getNatal() + " JDAY = " + natalData.getNatal().getJDay());
        System.out.println("TRANSIT DATETIME = " + c.getTransit());
        
        int [] bodys = { SUN,MOON,MARS,NODE,CHIRON,PHOLUS };
        
        List<ErrorBody> natalErrList = c.getErrorList(bodys,NPTChart.NATAL); //new ArrayList<ErrorBody>();
        List<Body> list = c.getBodyList(bodys,NPTChart.NATAL);
        echo("NATAL",list,natalErrList);
        
        List<ErrorBody> progErrList = c.getErrorList(bodys,NPTChart.PROGRESS); //new ArrayList<ErrorBody>();
        List<Body> progList = c.getBodyList(bodys,NPTChart.PROGRESS);
        echo("PROGRESS",progList,progErrList);
        
        System.out.println("------------天体移動------------");
        TimePlace prog_tp = c.search(SUN,300,false,PROGRESS);
        System.out.println("移動先TRANSIT " + prog_tp);
        Transit t = c.getTransit();
        t.setTimePlace(prog_tp);
        c.setTransit(t);
        progErrList = c.getErrorList(bodys,NPTChart.PROGRESS); //new ArrayList<ErrorBody>();
        progList = c.getBodyList(bodys,NPTChart.PROGRESS);
        echo("PROGRESS",progList,progErrList);
    }
//    public static void main(String [] args) {
//        test();
//    }
}
