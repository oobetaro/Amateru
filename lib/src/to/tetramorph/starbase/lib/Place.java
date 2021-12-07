/*
 * Place.java
 *
 * Created on 2006/07/04, 1:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;
import to.tetramorph.starbase.util.AngleConverter;

/**
 * 観測地を表現するクラスで、「緯度、経度、タイムゾーン、地名」の５つの要素からなる。
 * PlaceChooserの選択結果はこのオブジェクトで返る。
 * @author 大澤義鷹
 */
public class Place implements java.io.Serializable {
  public static final int PLACE_NAME_MAX_LENGTH = 28;
  /** 10進表記を表す定数 */
  public static final int DECIMAL = 0;
  /** 60進表記を表す定数 */
  public static final int SEXAGESIMAL = 1;
  protected String placeName;
  protected Double latitude;
  protected Double longitude;
  protected TimeZone timeZone = null;
  
  /**
   * 地名オブジェクトを作成する。
   * @param placeName 地名
   * @param latitude 観測地の緯度
   * @param longitude 観測地の経度
   * @param timeZone タイムゾーン
   */
  public Place(String placeName,Double latitude,Double longitude,
    TimeZone timeZone) {
    this.setPlaceName(placeName);
    this.setLatitude(latitude);
    this.setLongitude(longitude);
    this.timeZone = timeZone;
  }

  /**
   * 地名、緯度、経度、タイムゾーンがnullのオブジェクトを作成する。
   */
  public Place() {
  }

  /**
   * 地名を返す
   * @return 地名
   */
  public String getPlaceName() {
    return placeName;
  }

  /**
   * 地名をこのオブジェクトにセットする。nullと""はセット可能。
   * PLACE_NAME_MAX_LENGTH以上の文字数をもつ値をセットしようとすると、
   * IllegalArgumentException。
   * @param placeName 地名
   */
  public void setPlaceName(String placeName) {
    if(placeName != null) {
      if(placeName.length() > PLACE_NAME_MAX_LENGTH) 
        throw new IllegalArgumentException(
          "PLACENAME length too long;" + placeName);
    }
    this.placeName = placeName;
  }

  /**
   * 緯度を返す。
   * @return 緯度
   */
  public Double getLatitude() {
    return latitude;
  }
  /**
   * 文字列で緯度を返す。緯度が未登録のときは""を返す。
   */
  protected String getStringLatitude() {
    if(latitude != null) return latitude.toString();
    return "";
  }
  /**
   * 土地の緯度をセットする。北緯なら正数、南緯なら負数の十進数で。"35.5"とか"-80.4"等。
   * 0度なら赤道。+90度なら北極点、-90度なら南極点を意味する。
   * @param latitude 緯度
   */
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }
  /**
   * 文字列で表現された緯度をセットする。nullと""はセットできる。""はnullをセット
   * したのと同じ。数値と解釈できない値や、±90度の範囲を超える値が入力されると、
   * IllegalArgumentException。
   */
  public void setLatitude(String value) {
    if(value == null) this.latitude = null;
    else if(value.equals("")) this.latitude = null;
    else {
      String err = "Illegal LATITUDE value;" + value;
      try {
        Double d = new Double(value);
        if(d >= -90d && d <= 90)
          this.latitude = d;
        else throw new IllegalArgumentException(err);
      }catch(Exception e) {
        throw new IllegalArgumentException(err);
      }
    }
  }
  /**
   * 観測地の経度を返す。
   * @return 経度
   */
  public Double getLongitude() {
    return longitude;
  }
  /**
   * 文字列で経度を返す。経度が未登録のときは""を返す。
   */
  protected String getStringLongitude() {
    if(longitude != null) return longitude.toString();
    return "";
  }
  /**
   * 土地の経度をセットする。東経なら正数、西経なら負数の十進数で。"138.5"とか"-125.5"等。
   * 0度ならイギリスのグリニッジ基準点。そこから東に180度までは東経、西に180度までが西経。
   * @param longitude 経度
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
  /**
   * 文字列で表現された土地の経度をセットする。nullと""はセットできる。
   * ""はnullをセットしたのと同じ。数値と解釈できない値や、±180度の範囲を超える
   * 値が入力されるとIllegalArgumentException。
   */
  public void setLongitude(String value) {
    if(value == null) this.longitude = null;
    else if(value.equals("")) this.longitude = null;
    else {
      String err = "Illegal LONGITUDE value;" + value;
      try {
        Double d = new Double(value);
        if(d >= -180d && d <= 180)
          this.longitude = d;
        else throw new IllegalArgumentException(err);
      }catch(Exception e) {
        throw new IllegalArgumentException(err);
      }
    }
  }
  /**
   * タイムゾーンを返す
   * @return TimeZoneオブジェクト
   */
  public TimeZone getTimeZone() {
    return timeZone;
  }

  /**
   * タイムゾーンをセットする
   * @param timeZone TimeZoneオブジェクト
   */
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }
//  /** 
//   * setTimeZone()の入口を一本化するために廃止。setTimeZone()はTimePlaceでオーバーライドして、
//   * TimeZoneの変更があったときはjdayを再計算させる。入口が二つあるとオーバーライドも二つ必要になり煩雑化するため。
//   * タイムゾーンを設定する。これはDBやCSVファイルからの値セット用。
//   * @see java.util.TimeZone
//   * @param id TimeZone#getAvailableIDs()で取得できるリストにあるIDのみ指定可能。
//   */
//  public void setTimeZone(String id) {
//    timeZone = TimeZone.getTimeZone(id);
//  }
  
  /**
   * このオブジェクトが保持するデータの文字列表現を返す。
   * 「新潟県・新潟市, LAT = 37.874866, LON = 139.023620, TimeZone = 日本標準時」等。
   * @return このオブジェクトの文字列表現
   */
  public String toString() {
    return String.format("%s, LAT = %f, LON = %f, TimeZone = %s",
      placeName,latitude,longitude,timeZone.getDisplayName());
  }
  /**
   * "日本標準時〔UTC-09:30〕"といった文字列を返す。
   * @return 整形されたﾀｲﾑｿﾞｰﾝの文字表現
   */
  public String getFormattedTimeZone() {
    int v = timeZone.getRawOffset()/1000;
    String n = timeZone.getDisplayName();
    String s = v < 0 ? "-" : "+";
    v = Math.abs(v);
    int hour = v / 3600;
    int minute = (v - hour * 3600)/60;
    return String.format("%s〔UTC%s%02d:%02d〕",n,s,hour,minute);
  }
//  以下のメソッドはAngleConverterに移動した
//  /**
//   * 経度を整形して返す。東経なら"135.5E" 西経なら"135.5W"などとなる。
//   * @param longitude 文字列表現の経度
//   * @return 整形された経度
//   */
//  public static String getFormattedLongitude(String longitude) {
//    longitude = (longitude.startsWith("-")) ?
//      "W".concat(longitude.substring(1)) : "E".concat(longitude);
//    return longitude;
//  }
//  /**
//   * このオブジェクトの位置(Lon,Lat)を整形して返す。
//   * 位置が未登録のときは、"位置未登録"を返す。
//   */
//  public String getFormattedLocation() {
//    if(this.getLatitude() != null) {
//      return getFormattedLatitude(getStringLatitude()) + " " +
//        getFormattedLongitude(getStringLongitude());
//    } else return "位置未登録";
//  }
//  /**
//   * 緯度を整形して返す。北緯なら"35.5N" 南緯なら"-45.5S"などとなる。
//   * @param latitude 文字列表現の緯度
//   * @return 整形された緯度
//   */
//  public static String getFormattedLatitude(String latitude) {
//    latitude = (latitude.startsWith("-")) ?
//      "S".concat(latitude.substring(1)) : "N".concat(latitude);
//    return latitude;
//  }
  /**
   * 観測地情報(地名、緯度、経度、タイムゾーン)をセットする。
   * @param place Placeオブジェクト
   */
  public void setPlace(Place place) {
    timeZone = place.getTimeZone();
    longitude = place.getLongitude();
    latitude = place.getLatitude();
    placeName = place.getPlaceName();
  }
  /**
   * 観測地情報を返す。
   */
  public Place getPlace() {
    return this; //new Place(placename,latitude,longitude,timezone);
  }
  /**
   * 観測地情報を読みこむ。LATITUDE,LONGITUDEどれちかがNULLなら両方NULLと見なす。
   */
  public void setParams(ResultSet rs) throws SQLException {
    setPlaceName(rs.getString("PLACENAME"));
    //getDoubleは戻り値がDoubleではなくdoubleなので事前確認(NULLが代入できない)
    String lat = rs.getString("LATITUDE");
    String lon = rs.getString("LONGITUDE");
    if(lat != null && lon != null) {
      setLatitude(lat);
      setLongitude(lon);
    }
    if(rs.getString("TIMEZONE") != null)
      setTimeZone(TimeZone.getTimeZone(rs.getString("TIMEZONE")));
  }
  /**
   * 緯度・経度・タイムゾーンがすべて登録されている場合はtrueを返す。
   * 一つでもnullになっているならfalseを返す。
   */
  public boolean isCompletePlace() {
    return (latitude != null && longitude != null && timeZone != null);
  }
//  /**
//   * 10進小数を60進数表現にして返す。
//   * <pre>
//   * 戻り値を表示する際の例
//   *    System.out.printf("%d %d %f\n",(int)v[1],(int)v[2],v[3]);
//   * </pre>
//   * @param decimal 十進小数
//   * @return 戻り値の[0]は符号で、正の値のときは1.0、負のときは-1.0、ゼロのときは0.0。
//   * [1]は度、または時の位。[2]は分、[3]は秒とその端数。
//   * [0],[1],[2]はかならず整数が戻る。
//   */
//  public static double [] sexagesimal(double decimal) {
//    double sign = Math.signum(decimal);
//    decimal = Math.abs(decimal);
//    double th,tm,ts,ws;
//    th=(int) decimal;			//整数部抽出→時
//    ws = (decimal-th) * 3600d;	//小数部×1時間秒
//    ws = ws/60d;			//分に換算
//    tm = (int)ws;			//整数部抽出→分
//    ws -= tm;				//小数部抽出
//    ts = ws * 60d;			//×1分秒
//    //ts = round(ts,3);		//s.ddxで、xを四捨五入
//    //ws = ts/10000d + tm/100d + th;
//    return new double[] { sign,th,tm,ts };
//  }
//  /**
//   * このオブジェクトの経度を書式整形して返す。引数で十進表記、六十進表記を指定
//   * できる。
//   * @param unit DECIMAL または SEXAGESIMAL
//   */
//  public String getFormattedLongitude(int unit) {
//    if(longitude == null) return "";
//    if(unit == DECIMAL) {
//      String lon = longitude.toString();
//      lon = (lon.startsWith("-")) ?
//        "W".concat(lon.substring(1)) : "E".concat(lon);
//      return lon;
//    }
//    double [] v = sexagesimal(longitude);
//    String sign = v[0] < 0 ? "W" : "E";
//    return String.format("%s%dﾟ%d'%g",sign,(int)v[1],(int)v[2],v[3]);
//  }
//  /**
//   * このオブジェクトの緯度を書式整形して返す。引数で十進表記、六十進表記を指定
//   * できる。
//   * @param unit DECIMAL または SEXAGESIMAL
//   */  
//  public String getFormattedLatitude(int unit) {
//    if(latitude == null) return "";
//    if(unit == DECIMAL) {
//      String lat = latitude.toString();
//      lat = (lat.startsWith("-")) ?
//        "S".concat(lat.substring(1)) : "N".concat(lat);
//      return lat;
//    }
//    double [] v = sexagesimal(latitude);
//    String sign = v[0] < 0 ? "S" : "N";
//    return String.format("%s%dﾟ%d'%g",sign,(int)v[1],(int)v[2],v[3]);
//  }
  
  public static void main(String [] args) {
    Place place = new Place("TEST",-34.521313,0.0,TimeZone.getDefault());
    String deciLat = AngleConverter.getFormattedLatitude(AngleConverter.DECIMAL,place.getLatitude());
    String deciLon = AngleConverter.getFormattedLongitude(AngleConverter.DECIMAL,place.getLongitude());
    String sexaLat = AngleConverter.getFormattedLatitude(AngleConverter.SEXAGESIMAL,place.getLatitude());
    String sexaLon = AngleConverter.getFormattedLongitude(AngleConverter.SEXAGESIMAL,place.getLongitude());
    System.out.printf("deciLat = %s, deciLon = %s\n",deciLat,deciLon);
    System.out.printf("sexaLat = %s, sexaLon = %s\n",sexaLat,sexaLon);    
  }
}
