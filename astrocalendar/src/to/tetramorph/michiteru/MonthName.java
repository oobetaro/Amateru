/*
 * MonthName.java
 *
 * Created on 2008/02/12, 3:49
 *
 */

package to.tetramorph.michiteru;

import to.tetramorph.almanac.VoidTime;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import static java.util.Calendar.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JLabel;
/**
 * ロケールごとの月の名前や周の名前を返す。
 * @author 大澤義孝
 */
class MonthName {
    public static String [] monthNames;
    public static String [] weekNames;
    /**
     * ロケールに従った「月、週、タイムゾーン」の名前を配列にセットする。
     * voidStampMapに"月.日" => "開始時刻#終了時刻"という変換マップを作る。
     * たとえば"01.20" => "16:35〜#01:30"など。
     * zoneLabelにはプロパティに従ったゾーン名をセットする。
     * voidListはボイドの時間のリストを指定し、これに従ってvoidStampMapに値を
     * 書きこむ。
     * outOfRangeがtrueのときは、voidStampMapの作成を行わない。
     * <pre>
     * masterPropからは次のプロパティを参照する。値は変更しない。
     * monthShortName  yes/no
     * weekShortName   yes/no
     * TimeZoneID      JST,UTC等
     * language        ja
     * country         JP
     * </pre>
     */
    public static void update( List<VoidTime> voidList,
                                  boolean outOfRange,
                                  Properties masterProp,
                                  Map<String,String> voidStampMap,
                                  JLabel zoneLabel ) {
        //12ヶ月の月の名前をロケールに従って取得。デフォルトは日本語。
        //オプションによって略称・フルネーム二通りある。
        Locale locale = getLocaleProperty( masterProp );
        DateFormatSymbols dateSym = new DateFormatSymbols(locale);

        monthNames = ( Design.getProperty("monthShortName","no").equals("yes")) ?
            dateSym.getShortMonths() : dateSym.getMonths();

        weekNames = ( Design.getProperty("weekShortName","no").equals("yes")) ?
            dateSym.getShortWeekdays() : dateSym.getWeekdays();

        TimeZone zone = TimeZone.getTimeZone(
            masterProp.getProperty("TimeZoneID",TimeZone.getDefault().getID()));

        // 夏時間も考慮して値を書き換え
        zoneLabel.setText(zone.getDisplayName( zone.useDaylightTime(),
                                               TimeZone.SHORT,  locale ) );

        //ボイド時間文字列のAM,PM,午前,午後などを更新
        if ( ! outOfRange ) {
            voidStampMap.clear();
            for ( int i=0; i<voidList.size(); i++ ) {
                VoidTime vt = voidList.get(i);
                if ( equalCalendar(vt.begin,vt.end ) ) {
                    String time = vt.getBeginTime() + "#" + vt.getEndTime();
                    voidStampMap.put( "" + vt.begin.get(MONTH)
                                     + "."+vt.begin.get(DAY_OF_MONTH),time);
                } else {
                    voidStampMap.put(
                        "" + vt.begin.get(MONTH) + "."
                           + vt.begin.get(DAY_OF_MONTH),
                        vt.getBeginTime() + "〜" );
                    voidStampMap.put(
                        "" + vt.end.get(MONTH) + "."
                           + vt.end.get(DAY_OF_MONTH),
                        "〜"+vt.getEndTime());
                }
            }
        }
    }

    //カレンダー同士を年月日までが一致してる比較し同じなら真を返す。
    private static boolean equalCalendar(Calendar cal1,Calendar cal2) {
        return cal1.get(DAY_OF_MONTH) == cal2.get(DAY_OF_MONTH) &&
            cal1.get(MONTH) == cal2.get(MONTH) &&
            cal1.get(YEAR) == cal2.get(YEAR);
    }

    /**
     * マスタープロパティからLocale情報を取得して返す。
     */
    public static Locale getLocaleProperty(Properties masterProp ) {
        return new Locale(masterProp.getProperty("language","ja"),
            masterProp.getProperty("country","JP"));
    }

    /**  インスタンスは作成禁止 */
    private MonthName() {
    }

}
