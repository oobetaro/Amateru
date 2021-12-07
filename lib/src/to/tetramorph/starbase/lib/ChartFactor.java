/*
 *
 */
package to.tetramorph.starbase.lib;
import swisseph.*;
import java.util.*;
import static java.lang.System.*;
import static java.util.Calendar.*;
import static java.util.GregorianCalendar.*;
import static swisseph.SweConst.*;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.starbase.util.PlotAdjuster;
import to.tetramorph.util.Preference;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.time.JDay;

/**
 * スイスエフェメリスを使ってホロスコープ描画に必要なデータを取り出し扱いやすい形
 * に整形する。スイスエフェメリスが提供してくれるのは、プリミティブ型の天体位置だ
 * が、天体位置やアスペクトを表すクラスを用意して、それに値を入れるようにする。<p>
 * 求めたい天体名のリスト、日時、時間を渡すと、天体位置を計算して取り出せるよう
 * になるという方法。<p>
 * ノードにはmeanとtrueの二つのタイプがあるが、
 * Preferenceの"UseMeanNode"の値が真ならmeanノードで計算し、
 * 違うならtrueノードで計算する。<p>
 * アポジーにはmeanとoscu.の二つのタイプがあるが、
 * これは"UseMeanApogee"の値が真ならmeanアポジーで計算し、
 * 違うならoscu.アポジーで計算する。<p>
 * このクラスは後々には廃止する予定なのだが、ephtoolプロジェクトの
 * SolarAspectCalendar(技評の仕事用)が使用しているため、削除するときはその対処
 * も忘れてはいけない。
 */
public class ChartFactor {
  /** アンギュラー (1,4,7,10宮) を表す定数 */
  public static final int ANGULAR = 0;
  /** サクシデント (2,5,8,11宮)を表す定数 */
  public static final int SUCCEDENT = 1;
  /** キャデント (3,6,9,12宮) を表す定数 */
  public static final int CADENT = 2;
  /**
   * 感受点位置をホロスコープ描画用に調整する。getPlanets()で指定する定数。
   */
  public static final boolean PLOT_ADJUST = true;
  /**
   * 感受点位置をホロスコープ描画用に調整しない。getPlanets()で指定する定数。
   */
  public static final boolean PLOT_NOT_ADJUST = false;

  SwissEph swissEph;	//暦算オブジェクト
  double [] cusps = null;
  Set<Integer> planetSet = new HashSet<Integer>();
  Map<Integer, Body> map = new HashMap<Integer,Body>();
  Body sunBody = null; //太陽の位置はﾘｸｴｽﾄになくても計算し保管しておく
  static final int ASC_OFFSET = 0;
  static final int MC_OFFSET = 1;
  static final int ARMC_OFFSET = 2;
  static final int VERTEX_OFFSET = 3;
  
  Preference pref;
  /**
   * @param swissEph new SwissEph()したインスタンス。
   * @param pref 各種設定情報が入ったPreferenceオブジェクト
   * 詳細はswisseph.SwissEph.swe_houses()を参照。
   * @param bodys 計算対象とする天体番号を格納した配列。Constを参照せよ。
   * @see swisseph.SwissEph#swe_houses(double tjd_ut,int iflag,double geolat,double geolon,int hsys,double[] cusp,double[] ascmc)
   */
  public ChartFactor(SwissEph swissEph,Preference pref,int [] bodys ) {
    this(swissEph,pref);
    this.pref = pref;
    for(int v: bodys) planetSet.add(v);
  }
  
  /**
   * @param swissEph new SwissEph()したインスタンス。
   * @param pref 各種設定情報が入ったPreferenceオブジェクト
   * 詳細はswisseph.SwissEph.swe_houses()を参照。
   * @param planetSet 計算対象とする天体番号を格納したリスト。Constを参照。
   * @see swisseph.SwissEph#swe_houses(double tjd_ut,int iflag,double geolat,double geolon,int hsys,double[] cusp,double[] ascmc)
   */
  public ChartFactor(SwissEph swissEph,Preference pref,
    Set<Integer> planetSet ) {
    this(swissEph,pref); // ↓下のコンストラクタを呼びだし
    this.planetSet = planetSet;
    this.pref = pref;
  }
  /**
   * @param swissEph new SwissEph()したインスタンス。
   * @param pref 各種設定情報が入ったPreferenceオブジェクト
   * 詳細はswisseph.SwissEph.swe_houses()を参照。
   * @see swisseph.SwissEph#swe_houses(double tjd_ut,int iflag,double geolat,
   * double geolon,int hsys,double[] cusp,double[] ascmc)
   */
  public ChartFactor(SwissEph swissEph,Preference pref ) {
    this.swissEph = swissEph;
    this.pref = pref;
  }
  /**
   * swissEph,pref,planetSetのみをシャローコピーしてオブジェクトを複製作成する。
   */
  public ChartFactor(ChartFactor cf) {
    this.swissEph = cf.swissEph;
    this.pref = cf.pref;
    this.planetSet = cf.planetSet;
  }
  /**
   * 日時と場所を与えると天体位置やアセンダントやMCの座標やハウスカスプを計算する。
   * これはﾄﾗﾝｼｯﾄの天体位置を計算するという前提にたっていて、時刻はかならずｾｯﾄ
   * されているものと想定している。
   * @param datetime 日時をカレンダーオブジェクトで指定
   * @param isSetPlace lat,lonに有効な値をセットしたときはtrueを、場所情報を無視
   * させたいときはfalseをセットする。
   * @param lat 観測地の緯度
   * @param lon 観測地の経度
   */
  public void setDateAndPlace(GregorianCalendar datetime,double lat,double lon,boolean isSetPlace,boolean isSetTime ) {
    //スイスエフェメリスAPIの「時」を表すオブジェクト
    SweDate sd = new SweDate();
    double hour = datetime.get(HOUR_OF_DAY);
    double minute = datetime.get(MINUTE);
    double second = datetime.get(SECOND);
    double time = ( second / 60.0 + minute ) / 60.0 + hour;
    int year = datetime.get(YEAR);
    int month = datetime.get(MONTH) + 1;
    int day = datetime.get(DAY_OF_MONTH);
    //SweDateの紀元前は0年からなのでBC 1年なら0年、BC2年なら-1年。だから調整。
    if(datetime.get(ERA) == GregorianCalendar.BC) year = -(year-1);
    sd.setDate( year, month, day, time );
    sd.setCalendarType(sd.SE_GREG_CAL,sd.SE_KEEP_DATE);
    double timeDifference = (double)(datetime.getTimeZone().getRawOffset()/1000) / 3600d;
    double jd = sd.getJulDay() - timeDifference / 24.0;
    setDateAndPlace(jd,lat,lon,isSetPlace,isSetTime );
  }
  /**
   * 日時と場所をNatalオブジェクトで与えると、天体位置やアセンダントやMCや
   * ハウスカスプを計算する。Natalオブジェクトに時刻が設定されていない場合は、
   * デフォルトの計算時刻で計算される。観測地の緯度経度が設定されていない場合は、
   * アセンダントやMCやハウスカスプは計算されない。
   * デフォルトの計算時刻はSystem.getProperties().get("DefaultCalculateTime")で
   * 定義されている。
   */
  public void setDateAndPlace(TimePlace timePlace) {
    boolean isSetTime = timePlace.getTime() != null;
    if(timePlace.getLongitude() != null) {
      setDateAndPlace(timePlace.getJDay(),
                      timePlace.getLatitude(),
                      timePlace.getLongitude(),true,isSetTime ); //場所つきで計算
    } else {
      setDateAndPlace(timePlace.getJDay(),0,0,false,isSetTime );	//場所なしで計算
    }
    //どちらも時刻未登録のｹｰｽはgetJDay()の中でﾃﾞﾌｫﾙﾄ時刻でJDayを算出している
  }
  // 暦表時と天体番号から天体位置を計算しmapに登録。ハウスは計算しない。
  private Body calc(double ET,int id) {
    //ﾉｰﾄﾞの時は、ﾌﾟﾛﾊﾟﾃｨを調べてtrue/meanの切替をする。
    //ｱﾎﾟｼﾞｰの時も、ﾌﾟﾛﾊﾟﾃｨを調べてoscu./meanの切替をする。
    int shift = 0;
    boolean useMeanApogee = pref.getBoolean("UseMeanApogee");
    boolean useMeanNode = pref.getBoolean("UseMeanNode");
    if(id == NODE && ! useMeanNode ) shift++;
    if(id == APOGEE && ! useMeanApogee) shift++;
    double [] results = new double[6];
    StringBuffer errStrBuf = new StringBuffer();
    int flag = SweConst.SEFLG_SPEED;
    int ret_flag = swissEph.swe_calc(ET,id+shift,flag,results,errStrBuf);
    if(ret_flag<0 || ret_flag != flag) {
      throw new IllegalArgumentException
        ("flag = " + flag + ", ret_flag = " + ret_flag
        + ", message = " + errStrBuf.toString());
    }
    //map.put(id,new Body(id,results));
    return new Body(id,results);
  }
  /**
   * ユリウス日と観測地の緯度経度から天体位置とハウスを計算
   */
  protected void setDateAndPlace(double jd,double lat,double lon,boolean isSetPlace,boolean isSetTime ) {
    double ET = jd + SweDate.getDeltaT(jd); //暦表時(ET) = jday + ΔT
    //ｻｳｽﾉｰﾄﾞのﾘｸｴｽﾄがあるときは、ﾉｰﾄﾞのﾘｸｴｽﾄがなくてもﾉｰﾄﾞを計算
    if(  planetSet.contains(SOUTH_NODE) && ! planetSet.contains(NODE)) {
      map.put(NODE,calc(ET,NODE)); //calc(ET,NODE);
    }
    //ｱﾝﾁﾘﾘｽのﾘｸｴｽﾄがあるときは、ﾘﾘｽのﾘｸｴｽﾄがなくてもﾘﾘｽを計算
    if( planetSet.contains(ANTI_APOGEE) && ! planetSet.contains(APOGEE)) {
      map.put(APOGEE,calc(ET,APOGEE));
      //calc(ET,APOGEE);
    }
    sunBody = calc(ET,SUN); //太陽はかならず計算
    if(planetSet.contains(SUN)) map.put(SUN,sunBody); //必要ならmapにも登録
    //惑星位置を求める
    for(Iterator ite = planetSet.iterator(); ite.hasNext(); ) {
      Integer id = (Integer)ite.next();
      if(id == null) continue;
      if (id == EARTH || id > VESTA || id == SUN ) continue;
      map.put(id,calc(ET,id));
      //calc(ET,id);
    }
    if( planetSet.contains(SOUTH_NODE)) {
      Body snode = new Body(map.get(NODE));
      snode.lon = (snode.lon + 180d) % 360d;
      snode.id = SOUTH_NODE;
      map.put(SOUTH_NODE,snode);
    }
    if( planetSet.contains(ANTI_APOGEE)) {
      Body anap = new Body(map.get(APOGEE));
      anap.lon = (anap.lon + 180d) % 360d;
      anap.id = ANTI_APOGEE;
      map.put(ANTI_APOGEE,anap);
    }
    cusps = new double[13]; //カスプ保管用[1〜12]
    double [] ascmc = new double[10];	//AscやMCの値が書き込まれる。
    int houseSystem = HOUSE_SYSTEM_CODES[pref.getInteger("HouseSystemIndex")];
    //System.out.println("HouseSystemCode = " + HOUSE_SYSTEM_CODES[pref.getInteger("HouseSystemIndex")]);
    Body sun = getBody(SUN);
    boolean isSolar = pref.getBoolean("PrioritizeSolar"); //カスプ計算不能のときソーラー＆ソーラーサイン優先するフラグ
    //System.out.println("ソーラー優先フラグ = " + isSolar);
    int ret = swissEph.swe_houses( jd,0,lat,lon,houseSystem,cusps,ascmc);
    if( ((isSetPlace && isSetTime) || (isSetPlace && ! isSolar)) && ( houseSystem > (int)'2')) { 
      // (時間と場所が設定されている) or (場所が設定 and ソーラー優先フラグOFF)
      // and デフォのハウス分割がソーラー＆ソーラーサイン以外なら
      for(int i=1,j = CUSP1; i<cusps.length; i++,j++)
        map.put(j,new Body(j,cusps[i])); // カスプもmapに登録
      Integer i = pref.getInteger("HouseSystemIndex"); //ハウスシステム名を報告
      houseSystemName = HOUSE_SYSTEM_NAMES[i]; 
    } else if(houseSystem == (int)'2' || 
               pref.getInteger("CuspUnknownHouseSystem") == 2 ) {
      //System.out.println("ソーラーサインでカスプ計算");
      //場所が設定されていないか、ハウスシステムがソーラーサイン
      double angle = ((int)(sun.lon / 30d)) * 30d;
      for(int i=1,j = CUSP1; i<cusps.length; i++,j++) {
        //if(angle >= 360d) angle = angle - 360d;
        //System.out.println("angle = " + angle);
        map.put(j,new Body(j,angle));
        cusps[i] = angle;
        angle = (angle + 30d) % 360d; //((i-1) * 30d);
      }
      houseSystemName = HOUSE_SYSTEM_NAMES[6];
    } else if(houseSystem == (int)'1' || 
               pref.getInteger("CuspUnknownHouseSystem") == 1 ) {
      //場所が設定されていないか、ハウスシステムがソーラー
      //System.out.println("ソーラーでカスプ計算");
      double angle = sun.lon;
      for(int i=1,j = CUSP1; i<cusps.length; i++,j++) {
        //double angle = sun.lon + (30d * (i-1));
        if(angle >= 360d) angle = angle - 360d;
        map.put(j,new Body(j,angle));
        cusps[i] = angle;
        angle = (angle + 30d) % 360d;
      }
      houseSystemName = HOUSE_SYSTEM_NAMES[7];
    }
    double [] cuspbuf = new double[ cusps.length + 1 ];
    arraycopy(cusps,0,cuspbuf,0,cusps.length);
    cuspbuf[cusps.length]=cusps[1];
    //各天体にハウス番号をセット
    for(Iterator ite = planetSet.iterator(); ite.hasNext(); ) {
      Body p = map.get(ite.next());
      if(p == null) continue;
      for(int i=1; i<cusps.length; i++) {
        if(cuspbuf[i] > cuspbuf[i+1]) { // 特例
          if(( p.lon >= cuspbuf[i] && p.lon < 360d) ||
            (p.lon >= 0 && p.lon < cuspbuf[i+1] )) p.house = i;
        } else {
          if(p.lon >= cuspbuf[i] && p.lon < cuspbuf[i+1]) {
            p.house = i;
            break;
          }
        }
      }
    }
    if(isSetPlace && (houseSystem > (int)'2')) { //場所情報が設定されているならハウスも計算
      //マップにスペシャルポイントをセット
      map.put(AC,new Body(AC,ascmc[ASC_OFFSET]));
      map.put(MC,new Body(MC,ascmc[MC_OFFSET]));
      map.put(VERTEX,new Body(VERTEX,ascmc[VERTEX_OFFSET]));
      double dsc = (ascmc[ASC_OFFSET] + 180d) % 360d;
      map.put(DC,new Body(DC,dsc));
      double ic  = (ascmc[MC_OFFSET] + 180d) % 360d;
      map.put(IC,new Body(IC,ic));
      double av = (ascmc[VERTEX_OFFSET] + 180d) % 360d;
      map.put(ANTI_VERTEX,new Body(ANTI_VERTEX,av));
    }
  }
  /**
   * 一日一年法での天体の進行位置を求める。平均春分太陽年で出生日時から経過日時
   * までの時間を割り、年数を日数に変換する方法。
   */
  static final double SOLAR_YEAR = 365.2424;
  static final double SIDEREAL_DAY = 23.9344696;
  public void setSecondaryProgression(TimePlace natalTimePlace,TimePlace transitTimePlace) {
    double natalJDay = natalTimePlace.getJDay();
    double dayofs = (transitTimePlace.getJDay() - natalJDay) / SOLAR_YEAR;
    TimePlace timePlace = new TimePlace(natalTimePlace);
    GregorianCalendar cal = JDay.getCalendar( natalJDay + dayofs, natalTimePlace.getTimeZone());
    timePlace.setCalendar(cal,TimePlace.DATE_AND_TIME);
    setDateAndPlace(timePlace);
  }
  /**
   * 一日一年法で天体の進行位置を求めるが、AC,MCについては別の方式で計算する。
   * AC,MCの値はMeanSecondayProgressionとは大きくことなる。出生から1年目の進行は
   * 丸1日(24時間)後の天体位置と同じだが、このときアセンダントは1周よりわずに先に
   * 進む。たとえばここで0.8度多く進んだとする。では1年目ではなくちょうど半年後の
   * 進行はどうなるかというと、出生から12時間後の天体位置がそれになり惑星や小惑星は
   * その値を適用するが、AC,MCについては、先にあげた移動量0.8度の半分である0.4度
   * 進んだものとし、出生のACに0.4度加えた値を出す。
   * 
   * MeanSecondayProgressionならこのとき出生から12時間後のAC,MCの値をそのまま採
   * 用する。
   */
  public void setSecondaryProgression2(TimePlace natalTimePlace,TimePlace transitTimePlace) {
    double natalJDay = natalTimePlace.getJDay();
    double dayofs = (transitTimePlace.getJDay() - natalJDay) / SOLAR_YEAR;
    TimeZone tz = natalTimePlace.getTimeZone();
    TimePlace timePlace = new TimePlace(natalTimePlace);
    GregorianCalendar cal = JDay.getCalendar( natalJDay + dayofs, tz);
    timePlace.setCalendar(cal,TimePlace.DATE_AND_TIME);
    setDateAndPlace(timePlace); //1日1年法で進行を求める
    // AC,MCは別枠で計算する。
    double d = (long)dayofs;
//    System.out.println("dayofs = " + dayofs);
//    System.out.println("d = " + d);
    double jd1 = natalJDay + d; //小数部つまり時刻部を切り捨て
    double jd2 = (dayofs < 0) ? jd1 - 1 : jd1 + 1;

    int [] points = new int [] { AC,MC,DC,IC };

    ChartFactor cf1 = new ChartFactor(this.swissEph,this.pref, points);
    timePlace.setCalendar(JDay.getCalendar( jd1, tz),TimePlace.DATE_AND_TIME);
    cf1.setDateAndPlace(timePlace);

    ChartFactor cf2 = new ChartFactor(this.swissEph,this.pref, points );
    timePlace.setCalendar(JDay.getCalendar( jd2, tz),TimePlace.DATE_AND_TIME);
    cf2.setDateAndPlace(timePlace);
    double time = dayofs - d; //小数部のみ取り出しこれを時刻とみなす
//    System.out.println("time = " + time);
    //計算したAC,MCを再セットする。
    for(int id : points) {
      double size = (cf2.getBody(id).lon - cf1.getBody(id).lon) * time;
      Body b = cf1.getBody(id);
      if(b == null) continue;
      b.lon = (b.lon + size) % 360;
      map.put(id,b);
    }
  }
  /**
   * 1日1年法での進行位置を計算する。
   * MeanSecondayProgressionは平均太陽年で経過時間を割って進行させる日数に変換
   * するが、平均値である以上ずれが生じる。たとえば出生から40年後の進行位置を求める
   * 場合、1年につき1日うごかすのだとすれば、出生日に40日くわえるわけだが、
   * 満40歳までに経過した時間を太陽年で割ったとしても、きっちり40日という値には
   * ならず秒単位でズレが生じる。このメソッドはCalendarオブジェクトの日付計算
   * を使って進行日を求めるため、誤差が発生しない。
   * このメソッドでの一年は、毎年の誕生日から次の誕生日までの時間をいう。
   */
  public void setTrueSecondaryProgression(TimePlace natalTimePlace,TimePlace transitTimePlace) {
    GregorianCalendar ncal = natalTimePlace.getCalendar();
    GregorianCalendar tcal = transitTimePlace.getCalendar();
    int [] values = getTrueSecondaryOffset(ncal,tcal);
    int day = values[0];
    int daysec = values[1];
    //ネイタル日時に、進行の日数と、1年を1日の秒数に変換したものを加える
    ncal.add(Calendar.DAY_OF_MONTH,day);
    ncal.add(Calendar.SECOND,daysec);
//    System.out.println("DATE : " + JDay.getDateString(ncal));
    TimePlace timePlace = new TimePlace(natalTimePlace);
    timePlace.setCalendar(ncal,TimePlace.DATE_AND_TIME);
    setDateAndPlace(timePlace);
  }
  /**
   * TrueSecondaryProgressionを求めるための日と、時刻のオフセットを返す。
   * ネイタルの日時に加算する日数と、秒数を返す。
   * @return int[0]には日のオフセット。ネイタルから何日進めるか。
   *         int[1]には時刻のオフセットで単位は秒。最大86399秒。
   */
  private int [] getTrueSecondaryOffset(GregorianCalendar ncal,GregorianCalendar tcal) {
    TimeZone natalTimeZone = ncal.getTimeZone(); 
    //ﾈｰﾀﾙの年とERAの位をﾄﾗﾝｼｯﾄの年とERAにした複製を作る。
    //出生が1964/5/1で、ﾄﾗﾝｼｯﾄが2000/6/1なら、2000/5/1のCalendarｵﾌﾞｼﾞｪｸﾄを作る。
    //つまりﾄﾗﾝｼｯﾄに一番近い誕生日のCalendarを作るということ。
    GregorianCalendar cal = new GregorianCalendar(natalTimeZone);
    cal.set(Calendar.ERA,tcal.get(Calendar.ERA)); //ERAはﾄﾗﾝｼｯﾄから複写
    cal.set(Calendar.YEAR,tcal.get(Calendar.YEAR)); //年はﾄﾗﾝｼｯﾄから複写
    cal.set(Calendar.MONTH,ncal.get(Calendar.MONTH));
    cal.set(Calendar.DAY_OF_MONTH,ncal.get(Calendar.DAY_OF_MONTH));
    cal.set(Calendar.HOUR_OF_DAY,ncal.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE,ncal.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND,ncal.get(Calendar.SECOND));
    cal.set(Calendar.MILLISECOND,ncal.get(Calendar.MILLISECOND));
    int day = 0; //ﾈｰﾀﾙからの経過年数保管する変数。1年につき1日とみなす。
    long ms0 = cal.getTimeInMillis(); //ﾄﾗﾝｼｯﾄに一番近い誕生日のｼﾘｱﾙﾐﾘ秒を取得
    //double dayofs = 0;
    int daysec = 0;
    if(tcal.before(ncal)) { //ﾄﾗﾝｼｯﾄでﾈｰﾀﾙ以前の日が指定された
      if(tcal.after(cal)) {
        day = ncal.get(Calendar.YEAR) - tcal.get(Calendar.YEAR) - 1 ;
        cal.add(Calendar.YEAR,1);
        long ms1 = cal.getTimeInMillis();
        long year_ms = ms1 - ms0; //誕生日から誕生日までの1年の時間[ms]
        long ms = ms1 - tcal.getTimeInMillis(); //去年の誕生日から経過日までの時間を求める
        //dayofs = -((double)ms / (double)year_ms + day);
        daysec = (int)-((double)ms / (double)year_ms * 86400);
      } else {
        day = ncal.get(Calendar.YEAR) - tcal.get(Calendar.YEAR); //
        cal.add(Calendar.YEAR,-1); //1年前の誕生日に進める
        long year_ms = ms0 - cal.getTimeInMillis(); //1年間の時間を求める
        long ms = ms0 - tcal.getTimeInMillis(); //誕生日から経過日までの時間を求める
        //dayofs = -((double) ms / (double)year_ms + day);
        daysec = (int)-((double) ms / (double)year_ms * 86400);
      }
    } else { //通常はこっちの処理が実行される
      if(tcal.before(cal)) { //tcalはcal以前 (ﾄﾗﾝｼｯﾄは誕生日以前のとき)
        day = tcal.get(Calendar.YEAR) - 1 - ncal.get(Calendar.YEAR);
        cal.add(Calendar.YEAR,-1); //1年前に戻す。
        long ms1 = cal.getTimeInMillis();
        long year_ms = ms0 - ms1; //誕生日から誕生日までの1年の時間[ms]
        long ms = tcal.getTimeInMillis() - ms1; //去年の誕生日から経過日までの時間を求める
        //dayofs = (double)ms / (double)year_ms + day;
        daysec = (int)((double)ms / (double)year_ms * 86400);
        System.out.println("*******");
      } else { //tcalはcal以後
        day = tcal.get(Calendar.YEAR) - ncal.get(Calendar.YEAR);
        cal.add(Calendar.YEAR,1); //1年先の誕生日に進める
        long year_ms = cal.getTimeInMillis() - ms0; //1年間の時間を求める
        long ms = tcal.getTimeInMillis() - ms0; //誕生日から経過日までの時間を求める
        //dayofs = (double) ms / (double)year_ms + day;
        daysec = (int)((double)ms / (double)year_ms * 86400);
      }
    }
    return new int [] { day,daysec };
  }
  
  /**
   * 一度一年法での天体位置を求める。
   */
  public void setPrimaryProgression(TimePlace natalTimePlace,TimePlace transitTimePlace) {
    setDateAndPlace(natalTimePlace); //普通にﾈｰﾀﾙを求める
    //太陽がﾄﾗﾝｼｯﾄの日時までに何度移動したかを求める
    double angle = (transitTimePlace.getJDay() - natalTimePlace.getJDay()) / SOLAR_YEAR;
    //すべての感受点に、太陽の移動角を加算する
    for(Iterator ite = map.keySet().iterator(); ite.hasNext(); ) {
      Body body = map.get((Integer)ite.next());
      body.lon = (body.lon + angle) % 360;
    }
  }
  /**
   * ソーラーアーク進行法での天体位置を求める。これはSecondayProgression(1日1年法)
   * で太陽が進行した角度をもとめ、それをネイタルの全感受点に加算する方法。
   * ただし現状ではネイタルから365年以上先のプログレスは計算できず、それを超える
   * とIllegalArgumentExceptionが出る。
   */
  public void setSolarArcProgression(TimePlace natalTimePlace, TimePlace transitTimePlace) {
    double t = Math.abs(transitTimePlace.getJDay() - natalTimePlace.getJDay()) / SOLAR_YEAR;
    if( t > 365)
      throw new IllegalArgumentException("The range of the calculation was exceeded.");
    setDateAndPlace(natalTimePlace);
    ChartFactor prg_cf = new ChartFactor(this.swissEph,this.pref,new int [] { SUN } );
    prg_cf.setSecondaryProgression(natalTimePlace,transitTimePlace); //1日1年法で進行太陽を求める
    double p = prg_cf.getBody(SUN).lon;
    double n = sunBody.lon; //getBody(SUN).lon;
    double arc = p - n;
    if(p < n) arc += 360;      
    for(Iterator ite = map.keySet().iterator(); ite.hasNext(); ) {
      Body body = map.get((Integer)ite.next());
      body.lon = (body.lon + arc) % 360;
    }
  }

  //火星以遠の天体リスト (コンポジット進行法で使用)
  static final int [] trans_mars_bodys = { MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA };
  Set<Integer> transMarsBodySet = null;

  /**
   * コンポジット方式の進行法で天体位置を求める。
   * 火星とそれ以遠の天体(小惑星も)は１度１年法(PrimaryProgression)で、
   * 太陽から金星、ハウスカスプ、AC,MC、VERTEX等、仮想的な感受点は、
   * 一日一年法で天体位置を求める。
   */
  public void setCompositProgression(TimePlace natalTimePlace, TimePlace transitTimePlace) {
    Set<Integer> bodySet2 = new HashSet<Integer>(); //火星とそれより遠くにある天体を入れる
    Set<Integer> bodySet = new HashSet<Integer>(); //火星より近くにある天体を入れる
    if(transMarsBodySet == null) { //初期化されてない場合は初期化する。つまり1度だけ実行される。
      transMarsBodySet = new HashSet<Integer>();
      for(int i : trans_mars_bodys) transMarsBodySet.add(i);
    }
    //火星以遠の天体とそれ以外の天体の二種類のSetを用意する。
    for(Iterator ite = planetSet.iterator(); ite.hasNext(); ) {
      Integer i = (Integer)ite.next();
      if(transMarsBodySet.contains(i)) bodySet2.add(i);
      else bodySet.add(i);
    }
    ChartFactor prg_cf = new ChartFactor(this.swissEph,this.pref,bodySet);
    prg_cf.setSecondaryProgression(natalTimePlace,transitTimePlace); //1日1年法で計算
    for(Iterator ite = prg_cf.map.keySet().iterator(); ite.hasNext(); ) { //全計算結果をmapに移す(カスプやAC,MCもコピーされる)
      Integer i = (Integer)ite.next();
      map.put(i,prg_cf.getBody(i));
    }
    if( ! bodySet2.isEmpty()) { //火星より外側の天体が存在するときは計算
      ChartFactor cf2 = new ChartFactor(this.swissEph,this.pref,bodySet2);
      cf2.setPrimaryProgression(natalTimePlace,transitTimePlace);
      for(Iterator ite = bodySet2.iterator(); ite.hasNext(); ) {
        Integer i = (Integer)ite.next();
        map.put(i,cf2.getBody(i));
      }
    }
  }

  public Set getPlanetsSet() {
    return planetSet;
  }
  /**
   * 天体番号から天体位置を返す。
   */
  public Body getBody(int id) {
    return map.get(id);
  }
  /**
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
  private Body[] getClassifiedPlanets(int div,int element) {
    List<Body> list = new ArrayList<Body>();
    for(int i=SUN; i<= PLUTO; i++) {
      Body p = getBody(i);
      if(p == null) continue;
      int ele = p.getSign() % div;
      if(ele == element) list.add(p);
    }
    Body [] points = new Body[list.size()];
    for(int i=0; i<points.length; i++) points[i] = list.get(i);
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
    for(int i=0; i<4; i++)
      list.add(getClassifiedPlanets(4,i));
    for(int i=0; i<3; i++)
      list.add(getClassifiedPlanets(3,i));
    list.add(getClassifiedPlanets(2,0));
    list.add(getClassifiedPlanets(2,1));
    return list;
  }
  /**
   * アンギュラー、サクシデント、キャデント別に在泊天体(複数)を求める。
   * @param type ANGULAR,SUCCEDENT,CADENTの三種類を指定できる。
   */
  public List<Body> getPlanetsByHouseType(int type) {
    List<Body> list = new ArrayList<Body>();
    if(getBody(AC) == null) return null;
    for(int i=SUN; i<=PLUTO; i++) {
      Body p = getBody(i);
      if(p == null) continue;
      if((p.house-1) % 3 == type) list.add(p);
    }
    return list;
  }
  /**
   * 上昇星(AC±5度以内にある天体で、ASCに一番近い天体を返す。
   * ただし対象となるのは太陽から冥王星までのいずれか。
   * ASCが求まっていない等、算出不能のときはnullを返す。
   */
  public Body getRisingPlanet() {
    Body a = getBody(AC);
    if(a == null) return null;
    double angle=5.1;
    Body m = null;
    for(int i=SUN; i<=PLUTO; i++) {
      Body p = getBody(i);
      if(p == null) continue;
      double asp = Math.abs(a.lon - p.lon);
      if(angle > asp && asp <= 5) { //ASCに一番近いものをコピーしておく
        angle = asp;
        m = p;
      }
    }
    return m;
  }
  /**
   * カルミネイトしている天体(エレベートしている惑星の中でもっともMCに近い惑星を返す。
   * 存在しないときはnullを返す。
   */
  public Body getCulminatedPlanet() {
    Body mc = getBody(MC);
    if(mc == null) return null; //MCが存在しないときはnull
    double max = 90;
    Body cp = null;
    List<Body> list = getElevatedPlanets();
    for(int i=0; i<list.size(); i++) {
      Body p = list.get(i);
      double asp = Math.abs(mc.lon - p.lon);
      if(asp > 180d) asp = 360d - asp; //必ず180度以下の値になる
      //out.println(p.toString() + " asp = " + asp);
      if(max>asp) {
        max = asp;
        cp = p;
      }
    }
    return cp;
  }
  /**
   * エレベートしている惑星のリストを返す。7室から12室にある惑星のリストを返すという
   * こと。一つもないときは戻り値のlist.size()が0を返す。
   */
  public List<Body> getElevatedPlanets() {
    Body a = getBody(AC);
    List<Body>list = new ArrayList<Body>();
    if(a == null) return list;
    //　↓aの値を直接書き換えると後々困る。別オブジェクトを用意する。
    //a.lon -= 90d;
    Body asc = new Body(AC,a.lon-90d);
    for(int i=SUN; i<=PLUTO;i++) {
      Body p = getBody(i);
      if(p == null) continue;
      double asp = Math.abs(asc.lon - p.lon);
      if(asp > 180d) asp = 360d - asp; //必ず180度以下の値になる
      if(asp < 90d ) list.add(p);
    }
    return list;
  }
  /**
   * 各ハウスに入っている惑星のリストを返す。小惑星は無視する。
   * 一つも入っていないときはsize()==0のリストが返る。
   * @param house ハウス番号
   */
  public List<Body> getHouseInPlanets(int house) {
    //Body a = getBody(AC);
    List<Body>list = new ArrayList<Body>();
    //if(a == null) return list;
    for(int i=SUN; i<=PLUTO;i++) {
      Body p = getBody(i);
      if(p == null) continue;
      if(p.house == house) list.add(p);
    }
    return list;
  }
  /**
   * 支配星(ASCサインのルーラー)を返す。
   * ASCが求まっていない等、算出不能のときはnullを返す。
   */
  public Body getRulerPlanet() {
    Body a = getBody(AC);
    if(a == null) return null;
    return getBody( MODERN_RULERS[ a.getSign() ] );
  }
  /**
   * 太陽と月の離角から月齢を求める。divには月相を何分割するかを与える。
   * たとえば28を与えれば、戻り値は0〜27の値になる。
   * 値はObject[]で返り、[0]はInteger型で月相、[1]にはDouble型で離角
   */
  public Object [] getMoonFace(int div) {
    Body sun = getBody(SUN);
    Body mon = getBody(MOON);
    if(sun == null||mon == null) return null;
    double a = sun.lon <= mon.lon ? mon.lon - sun.lon : mon.lon + 360 - sun.lon;
    
    return new Object[] { new Integer((int)(a * div/360d)),
    new Double(a) };
  }
  String houseSystemName = null;
  /**
   * ハウスシステム名を返す。「プラシーダス」「イコール」等。通常はPreferenceで
   * 与えられたハウスシステム名が返るが、時間や緯度経度の不備からカスプの計算が
   * できなかったときは「ソーラーサイン」や「ソーラー」の名前が返る。
   */
  public String getHouseSystemName() {
    return houseSystemName;
  }
  /**
   * ハウスカスプのリストを返す。1室から順番に要素がならんだリストを返す。
   * Angle#valueにカスプの計算値(0 >= value < 360)、
   * nameには獣帯座標に整形された度数(0 >= value < 30)がセットされている。 
   * 整形された度数はデフォルトでは小数点以下は切り捨てだが、
   * Preferenceに"NumberOfDecimals"のプロパティが設定されていればその値に
   * 従って小数点以下を切り捨てる。2なら小数点以下二位以下は切り捨てる。
   */
  public double [] getCusps() {
    double [] temp = new double[12];
    for(int i=0; i<temp.length; i++)
      temp[i] = getBody(CUSP1+i).lon;
    return temp;
  }
//  public double [] getCusps() {
//    double [] temp = new double[12];
//    System.arraycopy(cusps,1,temp,0,12);
//    return temp;
//  }
  /**
   * 引数で指定された天体(複数)の位置を返す。このメソッドが返す天体位置は、
   * ホロスコープ描画用に感受点の表示位置が調整されている。
   * @param bodys 求めたい天体番号の配列 (Constクラスで宣言されているもの)
   * @param plotAdjust 感受点の表示位置調整を行う場合はtrueをセット。ただしtrueに
   * すると処理に時間がかかる。falseならBody#plotはセットされない。
   */
  public List<Body> getPlanets(int [] bodys,boolean plotAdjust) {
    if(plotAdjust) {
      PlotAdjuster pa = new PlotAdjuster(6d);
      for(int i=0; i<bodys.length; i++) {
        Body sp = getBody(bodys[i]);
        if(sp == null) continue;
        pa.add(sp);
      }
      return pa.getResult();
    } else {
      List<Body> list = new ArrayList<Body>();
      for(int i=0; i<bodys.length; i++) {
        Body sp = getBody(bodys[i]);
        if(sp == null) continue;
        list.add(sp);
      }
      return list;
    }
  }
  /**
   * 引数で指定された天体(複数)の位置を返す。このメソッドが返す天体位置は、
   * ホロスコープ描画用に感受点の表示位置が調整されている。
   * @param bodyList 求めたい天体番号 (Constクラスで宣言されているもの)リスト
   * @param plotAdjust 感受点の表示位置調整を行う場合はtrueをセット。ただしtrueに
   * すると処理に時間がかかる。falseならBody#plotはセットされない。
   */
  public List<Body> getPlanets(List<Integer> bodyList,boolean plotAdjust) {
    int [] args = new int[bodyList.size()];
    for(int i=0; i<bodyList.size(); i++) args[i] = bodyList.get(i);
    return getPlanets(args,plotAdjust);
  }
//  /**
//   *
//   */
//  public static void setGroupCode(List<Body> list,int code) {
//    
//  }
  /**
   * 引数で指定された天体リストの黄経だけを取り出してdouble[]にして返す。
   */
  public static double [] getPlanetsAngle(List<Body> planetList) {
    double [] temp = new double[planetList.size()];
    for(int i=0; i<planetList.size(); i++) {
      temp[i] = planetList.get(i).lon;
    }
    return temp;
  }
  /**
   * 引数で指定された天体リストのplot黄経だけを取り出してdouble[]にして返す。
   */
  public static double [] getPlanetsPlotAngle(List<Body> planetList) {
    double [] temp = new double[planetList.size()];
    for(int i=0; i<planetList.size(); i++) {
      temp[i] = planetList.get(i).plot;
    }
    return temp;
  }
  static void test() {
    SwissEph swissEph = Ephemeris.getInstance().getSwissEph();
    int [] bodys = { AC,MC,SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO };
    Set<Integer> bodySet = new HashSet<Integer>();
    for(int i : bodys) bodySet.add(i);

    Preference pref = new Preference();
    pref.setProperty("UseMeanNode","false");
    pref.setProperty("UseMeanApogee","false");
    pref.setProperty("HouseSystemIndex","0");
    pref.setProperty("PrioritizeSolar","false");
    pref.setProperty("CuspUnknownHouseSystem","2");

    TimePlace natalTimePlace = new TimePlace();
    natalTimePlace.setPlace(new Place("箕面市",34.833549,135.483978,TimeZone.getDefault()));
    natalTimePlace.setCalendar(new GregorianCalendar(1964,9-1,30,5,35,0),TimePlace.DATE_AND_TIME);
    TimePlace transitTimePlace = new TimePlace();
    //transitTimePlace.setPlace(new Place("箕面市",34.833549,135.483978,TimeZone.getDefault()));
    //transitTimePlace.setCalendar(new GregorianCalendar(2007,6-1,10,0,40,0),TimePlace.DATE_AND_TIME);
    //transitTimePlace.setCalendar(new GregorianCalendar(1965,9-1,30,5,35,0),TimePlace.DATE_AND_TIME);
    transitTimePlace.setCalendar(new GregorianCalendar(2007,9-1,30,5,35,0),TimePlace.DATE_AND_TIME);

    ChartFactor natcf = new ChartFactor( 
        Ephemeris.getInstance().getSwissEph(), pref, bodySet );
    natcf.setDateAndPlace(natalTimePlace);
    for(int i : bodys)
      System.out.println(natcf.getBody(i));    
    System.out.println("プログレス");
    ChartFactor progcf = new ChartFactor(
        Ephemeris.getInstance().getSwissEph(), pref, bodySet );
    progcf.setTrueSecondaryProgression(natalTimePlace,transitTimePlace);
    //progcf.setTrueSecondaryProgression(natalTimePlace,transitTimePlace);
    //progcf.setCompositProgression(natalTimePlace,transitTimePlace);
    //progcf.setMeanSecondaryProgression(natalTimePlace,transitTimePlace);
    //progcf.setSecondaryProgression(natalTimePlace,transitTimePlace);
    //progcf.setPrimaryProgression( natalTimePlace,transitTimePlace );
    //progcf.setSolarArcProgresssion( natalTimePlace,transitTimePlace );
    for(int i : bodys)
      System.out.println(progcf.getBody(i));
    for(int i=0; i<12; i++)
      System.out.println(progcf.getBody(CUSP1+i));
  }
  public static void main(String args[]) {
    test();
  }
//  public static void main(String args[]) {
//    SwissEph swissEph = Ephemeris.getSwissEph();
//    GregorianCalendar cal = new GregorianCalendar(1964,9-1,30,5,35,0);
//    double lon = 135.483978;
//    double lat = 34.833549;
//    int [] bodys = { SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO };
//    Set<Integer> bodySet = new HashSet<Integer>();
//    for(int i : bodys) bodySet.add(i);
//    //Properties prop = Database.getInstance().getProperties("Default");
//    Preference pref = new Preference();
//    pref.setProperty("UseMeanNode","false");
//    pref.setProperty("UseMeanApogee","false");
//    pref.setProperty("HouseSystemIndex","0");
//    pref.setProperty("PrioritizeSolar","false");
//    pref.setProperty("CuspUnknownHouseSystem","2");
//    
//    //PreferencePanel.getDefault(pref);
//    ChartFactor cf = new ChartFactor(swissEph,pref,bodySet);
//    cf.setDateAndPlace(cal,lat,lon,true,false);
//    for(int i=0; i<bodys.length; i++) {
//      out.println(SYMBOL_NAMES[bodys[i]] + " : " + cf.getBody(bodys[i]));
//    }
//    
//    for(int i=CUSP1; i<= CUSP12; i++) {
//      out.println((i-CUSP1+1) + "室 " + cf.getBody(i));
//    }
//    
//    String [] tableHeadder = { "火","地","風","水","Ｃ","Ｆ","Ｍ","陽","陰" };
//    List<Body[]> table = cf.getElementsTable();
//    for(int i=0; i<table.size(); i++ ) {
//      out.print(tableHeadder[i] + " : ");
//      Body[] points = table.get(i);
//      for(int j=0; j<points.length; j++)
//        out.print(SYMBOL_NAMES[points[j].id] + ",");
//      out.println();
//    }
//    
//    AspectType [] defAspect = {
//      new AspectType(CONJUNCTION,4,8),
//      new AspectType(SEXTILE,3,6),
//      new AspectType(SQUARE,4,8),
//      new AspectType(TRINE,4,8),
//      new AspectType(OPPOSITION,4,8),
//      new AspectType(QUINCUNX,3,6),
//      new AspectType(QUINTILE,2,4)
//    };
//    List<Aspect> list = AspectFinder.getAspects(cf,bodys,defAspect);
//    for(Aspect a:list) {
//      out.println(a.toString());
//    }
//    out.println("エレベートしている天体");
//    List<Body> elev = cf.getElevatedPlanets();
//    for(int i=0; i<elev.size(); i++) {
//      Body p = elev.get(i);
//      out.println(p.toString());
//    }
//    
//    out.println("カルミネイトしている天体");
//    out.println(cf.getCulminatedPlanet());
//    
//    out.println("ハウスごとの天体");
//    for(int i=1; i<=12; i++) {
//      out.print(i + " : ");
//      List<Body> plist = cf.getHouseInPlanets(i);
//      for(int j=0; j<plist.size(); j++) {
//        out.print(SYMBOL_NAMES[plist.get(j).id] + ",");
//      }
//      out.println();
//    }
//  }
  
  /**
   * 星座度数の値 ( 0 >= x < 30) を指定小数点で「切り捨て」て文字列で返す。
   * つまりvalue=23.3999 のときdegits=2で切り捨てると23.39。
   * @param value 天体度数
   * @param degits 小数点何位で切り捨てるか(0〜4)
   */
  public static String formatSignAngle(double value,int degits) {
    String sv = String.format("%f%05d",value,0); //確実に10進数表記にする
    int i = (degits > 0) ? 3 + degits : 2;
    if(degits > 0) sv = sv.substring(0,sv.indexOf(".") + degits + 1);
    else sv = sv.substring(0,sv.indexOf("."));
    return String.format("%"+i+"s",sv);
  }
  /**
   * 配列で与えられた星座度数の値 ( 0 >= x < 30) を指定小数点で「切り捨て」て文字列で返す。
   * つまりvalue=23.3999 のときdegits=2で切り捨てると23.39。
   * @param values 天体度数(複数)
   * @param degits 小数点何位で切り捨てるか(0〜4)
   */
  public static String [] formatSignAngles(double [] values,int degits) {
    String [] temp = new String[values.length];
    for(int i=0; i < values.length; i++) {
      temp[i] = formatSignAngle(values[i] % 30d,degits);
    }
    return temp;
  }
}

