/*
 * TestConst.java
 *
 * Created on 2007/07/28, 22:49
 *
 */

package to.tetramorph.starbase.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;

/**
 * これはテスト用の日時や場所のオブジェクトを返すstatic methodからなる。<br>
 * このクラスから得られるPlaceやDataのオブジェクトは、定数のようなものだから、
 * メソッドで返さずともstatic finalの定数として宣言しておいたのでも良いように
 * 思えるが、オブジェクトをいくらfinalに宣言しても参照アドレスが固定されるだけで、
 * そのオブジェクトのメソッドを呼び出して値が変更されることは防げない。<br>
 * もちろんそういうことをしないように気をつければ良いのだが、部外者が作った
 * プラグインが悪意をもって書き換える場合もあるかもしれない。毎回生成式にすれば
 * そういうことは防げる。
 * @author 大澤義鷹
 */
public class TestConst {
    /**
     * GregorianCalendar.ADと等価
     */
    public static final int AD = GregorianCalendar.AD;
    /**
     * GregorianCalendar.BCと等価
     */
    public static final int BC = GregorianCalendar.BC;
    private static TimeZone defaultTimeZone = TimeZone.getTimeZone("Asia/Tokyo");
    /**  オブジェクトを作成は禁止 */
    private TestConst() {
    }
    /**
     * おーさわの出生データをNatalオブジェクトで返す。
     */
    public static Natal getMyNatal() {
        getNatal(AD,1964,9-1,30,5,35,0,getMyPlace());
        GregorianCalendar gcal = new GregorianCalendar(1964,9-1,30,5,35,0);
        TimePlace tp = new TimePlace();
        tp.setCalendar(gcal,TimePlace.DATE_AND_TIME);
        tp.setPlace(getMyBirthPlace());
        Natal natal = new Natal();
        natal.setTimePlace(tp);
        natal.setName("大澤義鷹");
        natal.setKana("おおさわよしたか");
        natal.setJob("プログラマー");
        natal.setGender(Natal.MALE);
        return natal;
    }
    /**
     * おーさわの出生地をPlaceオブジェクトで返す。箕面市の緯度経度を表している。
     */
    public static Place getMyBirthPlace() {
        return new Place("箕面市",34.833549,135.483978,defaultTimeZone);
    }
    
    /**
     * おーさわの現在地をPlaceオブジェクトで返す。横浜の緯度経度を表している。
     */
    public static Place getMyPlace() {
        return new Place("横浜市",35.453962,139.617206,defaultTimeZone);
    }
    /**
     * おーさわの出生日時場所をTimePlaceオブジェクトで返す。
     */
    public static TimePlace getMyTimePlace() {
        return getTimePlace(AD,1964,9-1,30,5,35,0,getMyBirthPlace());
    }
    /**
     * おーさわの出生日時場所をDataオブジェクトで返す。
     */
    public static Data getMyData() {
        return new Data(getMyNatal());
    }
    /**
     * おーさわの現在の住処(タイムゾーンも含め)での現時刻でのTransitオブジェクトを返す。
     */
    public static Transit getMyNowTransit() {
        return getNowTransit(getMyPlace());
    }
    /**
     * おーさわの現在の住処(タイムゾーンも含め)での指定日時でのTransitオブジェクトを返す。
     */
    public static Transit getMyTransit(int era,int year,int month,int day,int hour,int minute,int second) {
        Transit transit = new Transit();
        transit.setTimePlace(getTimePlace(era,year,month,day,hour,minute,second,getMyPlace()));
        return transit;
    }
    /**
     * おーさわの現在の住処(タイムゾーンも含め)で指定日時でDataオブジェクトを返す。
     * 名前は「名無しさん」になる。
     */
    public static Data getMyData(int era,int year,int month,int day,int hour,int minute,int second) {
        Natal natal = new Natal();
        natal.setName("名無しさん");
        natal.setKana("ななしさん");
        natal.setGender(Natal.NONE);
        natal.setTimePlace(getTimePlace(era,year,month,day,hour,minute,second,getMyPlace()));
        return new Data(natal);
    }
    /**
     * おーさわの現在の住処(タイムゾーンも含め)で指定日時でTimePlaceオブジェクトを返す。
     */
    public static TimePlace getMyTimePlace(int era,int year,int month,int day,int hour,int minute,int second) {
        return getTimePlace(era,year,month,day,hour,minute,second,getMyPlace());
    }
    /**
     * 指定された場所情報を持つ、現在の日時のTransitオブジェクトを返す。
     */
    public static Transit getNowTransit(Place place) {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTimeZone(place.getTimeZone());
        Transit transit = new Transit();
        transit.setPlace(place);
        transit.setCalendar(gcal,TimePlace.DATE_AND_TIME);
        return transit;
    }
    /**
     * 指定日、指定場所でNatalオブジェクトを返す。
     */
    public static Natal getNatal(int era,int year,int month,int day,int hour,int minute,int second,Place place) {
        Natal natal = new Natal();
        natal.setTimePlace(getTimePlace(era,year,month,day,hour,minute,second,place));
        return natal;
    }
    /**
     * 指定日、指定場所でTimePlaceオブジェクトを返す。
     */
    public static TimePlace getTimePlace(int era,int year,int month,int day,
        int hour,int minute,int second,Place place) {
        GregorianCalendar gcal = new GregorianCalendar(year,month,day,hour,minute,second);
        gcal.set(GregorianCalendar.ERA,era);
        TimePlace tp = new TimePlace();
        tp.setCalendar(gcal,TimePlace.DATE_AND_TIME);
        tp.setPlace(place);
        return tp;
    }
    /**
     * 世界標準時(UTC)でTimePlaceを返す。場所はグリニッジ天文台。
     */
    public static TimePlace getTimePlaceByUTC(int era,int year,int month,
        int day,int hour,int minute,int second) {
        return getTimePlace(era,year,month,day,hour,minute,second,getGreenwitchPlace());
    }
    /**
     * グリニッジ天文台の場所を返す。緯度51.4776、経度0。タイムゾーンはUTC。
     */
    public static Place getGreenwitchPlace() {
        return new Place( "グリニッジ天文台", 51.47776002440376, 0.0,
                            TimeZone.getTimeZone("UTC") );
    }
    
    /**
     * 日本の皇居の場所を生成して返す。
     * 緯度35.676211, 経度139.754031。Asia/Tokyo。
     */
    public static Place getImperialPalaceOfJapan() {
        return new Place("皇居",35.676211, 139.754031, defaultTimeZone );
    }
    
}
