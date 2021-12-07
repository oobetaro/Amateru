/*
 *
 */
package to.tetramorph.starbase.lib;

import to.tetramorph.starbase.*;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * Bodyは天体や感受点の位置を表すのに使う。
 * 名前、黄経、黄緯、距離、速度等といったフィールド変数からなる。 
 * スイスエフェメリスで計算した感受点は、１点づつこのクラスに格納して使用する。
 * 黄経(lon)黄緯(lat)などを表す各フィールド変数には直接アクセスして使用する
 * (フィールドの数も多く、また一括セットすれば後は変更する事は少なく、読み出し
 * オンリーなのでメソッドでラップはしていない。
 */
public class Body {
  /** 感受点の黄経や赤経 */
  public double lon;
  /** 感受点の黄緯や赤緯 */
  public double lat;
  /** 感受点の距離 */
  public double dist;
  /** 感受点の距離の速度 */
  public double distSpeed;
  /** 感受点の黄経の速度 */
  public double lonSpeed;
  /** 感受点の黄緯の速度 */
  public double latSpeed;
  /** 感受点のグラフィック座標 */
  public double plot;
  /** 
   * シリアル番号。PlotAdusterが感受点の位置調整を行うときに、天体にシリアル
   * ナンバーを割り当てる必要があり、そのときに使われるフィールド。
   */
  public int serial;
  /**
   * 感受点のハウス。今のところ室番号は必ず1から始まると想定していて、houseの
   * 初期値は0なので、もし0のままなら、ハウス番号は未定義だと判定できる。
   */
  public int house = 0;
  /** 感受点の天体番号。未定義のときは-1 */
  public int id = -1;
  /**
   * Natal,Progress,Transit等の識別用フィールド。
   */
  public int group = 0;

  
  private final int LON = 0;
  private final int LAT = 1;
  private final int DIST = 2;
  private final int LON_SPEED = 3;
  private final int LAT_SPEED = 4;
  private final int DIST_SPEED = 5;
  
  /**
   * 感受点オブジェクトから感受点を作成する。つまりクローンを作成する。
   */
  public Body(Body point) {
    this.id = point.id;
    this.lon = point.lon;
    this.lat = point.lat;
    this.dist = point.dist;
    this.lonSpeed = point.lonSpeed;
    this.latSpeed = point.latSpeed;
    this.distSpeed = point.distSpeed;
    this.house = point.house;
    this.plot = point.plot;
    this.group = point.group;
  }
  /**
   * 天体番号とvalues[]に格納された値から天体オブジェクトを作成する。
   * plotにはlonの値がセットされる。
   * @param values values[0]からlon,lat,dist,lonSpeed,latSpeed,distSpeedの順番で、
   * values[Body.LON] = ...というように指定することも可能。ちなみにこの配列は、
   * SwissEphe.swe_calc()が、配列に計算結果をセットして返すものと同じ。
   */
  public Body(int id,double [] values) {
    this.id = id;
    setValues(values);
    this.plot = this.lon;
  }
  /**
   * 天体番号とグループ番号、values[]に格納された値から天体オブジェクトを作成する。
   * plotにはlonの値がセットされる。
   * @param values values[0]からlon,lat,dist,lonSpeed,latSpeed,distSpeedの順番で、
   * values[Body.LON] = ...というように指定することも可能。ちなみにこの配列は、
   * SwissEphe.swe_calc()が、配列に計算結果をセットして返すものと同じ。
   */
  public Body(int id,double [] values,int group) {
    this(id,values);
    this.group = group;
  }
  /**
   * 天体番号と経度から天体オブジェクトを作成する。plotにはlonの値がセットされる。
   */
  public Body(int id,double lon) {
    this.id = id;
    this.lon = lon;
    this.plot = lon;
  }
  /**
   * 天体番号、黄経、グループ番号からオブジェクトを作成する。カスプやAC等の
   * 感受点作成用。
   */
  public Body(int id,double lon,int group) {
    this(id,lon);
    this.group = group;
  }
  /**
   * 天体番号、黄経、グループ番号、ハウス番号からオブジェクトを作成する。
   * カスプやAC等の感受点作成用。
   */
  public Body(int id,double lon,int group, int house) {
    this(id,lon);
    this.house = house;
    this.group = group;
  }
  private void setValues(double [] values) {
    lon = values[LON];
    lat = values[LAT];
    dist = values[DIST];
    lonSpeed = values[LON_SPEED];
    latSpeed = values[LAT_SPEED];
    distSpeed = values[DIST_SPEED];
  }
  /** 
   * この天体のサイン内における角度を返す。格納されている黄経(lon)が186度の場合、
   * これは天秤6度であるので6度を返す。
   * @return サイン内における度数 (0 >= value < 30) 
   */
  public double getSignAngle() {
    return lon % 30d;
  }
  private static String formatSignAngle(double value,int degits) {
    String sv = String.format("%f%05d",value,0); //確実に10進数表記にする
    int i = (degits > 0) ? 3 + degits : 2;
    if(degits > 0) sv = sv.substring(0,sv.indexOf(".") + degits + 1);
    else sv = sv.substring(0,sv.indexOf("."));
    return String.format("%"+i+"s",sv);
  }  
  
  /** サインのオフセット(0-11)を返す。*/
  public int getSign() {
    return (int)lon / 30;
  }
  /** サインの名前を返す */
  public String getSignName() {
    return ZODIAC_NAMES[getSign()];
  }
  /** 
   * 感受点の名前(略称)を返す 
   * @return "sun","mon"等、Const.SYMBOL_NAMES[]で宣言されている天体略称。
   */
  public String getName() {
    return SYMBOL_NAMES[id];
  }
  /**
   * 感受点位置の文字表現を返す。
   *"Sun 天秤 6.71  H1 group=0"とか、"Mer 乙女 24.5 R H12 group=2"といった表現。
   */
    @Override
  public String toString() {
    String rev = "";
    if( lonSpeed < 0 ) rev = "R";
    String h = "";
    if( house >= 1) h = "H" + house;
    return String.format("%s %s %s %s %s %s",
      PLANET_NAMES_EN[id],getSignName(),formatSignAngle(getSignAngle(),2),
      rev,h,"group = " + group);
  }
  /**
   * この天体と指定された天体のidとgroupが同じ場合は真を返す。
   * 判定は次のように行われる。<br>
   * (id == body.id && group == body.group)
   */
  public boolean isSame(Body body) {
    return (id == body.id && group == body.group);
  }
}
