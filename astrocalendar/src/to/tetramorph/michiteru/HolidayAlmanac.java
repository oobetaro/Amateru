/*
 * HolidayAlmanac.java
 *
 * Created on 2008/02/12, 0:01
 *
 */

package to.tetramorph.michiteru;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 国民の祝日を取得する暦。
 * @author 大澤義孝
 */
class HolidayAlmanac {
    static final Map<String,String> map;
    static {
        map = new LinkedHashMap<String,String>();
        BufferedReader reader = null;
        try {
            InputStream is = HolidayAlmanac.class.
                getResource("/resources/holidays.txt").openStream();
            InputStreamReader isr = new InputStreamReader(is,"sjis");
            reader = new BufferedReader(isr);
            while( reader.ready() ) {
                String line = reader.readLine().trim();
                String [] tokens = line.split(" ");
                map.put( tokens[0].trim(), tokens[1].trim() );
            }
        } catch ( IOException e ) {
            Logger.getLogger("to.tetramorph.michiteru")
                    .log( Level.SEVERE, null, e );
        } finally {
            try { reader.close(); } catch ( Exception e ) { }
        }
    }

    /**
     * "yyyy-mm-dd"というグレゴリオ暦の日付文字列に対して該当する祝日があるなら
     * その祝日名を返す。なければnullを返す。
     */
    public static String get(String date) {
        return map.get(date);
    }

    /**
     * カレンダーの年,月,日のフィールドから一致する祝日の日があるときはその名前を
     * 返す。無いときはnullを返す。
     */
    public static String get(Calendar cal) {
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        return get( y, m + 1, d );
    }

    /**
     * 年月日から該当する祝日の日があるときはその名前を返す。無いときはnullを返す。
     * @param year 年
     * @param month 月(1-12)
     * @param day 日
     */
    public static String get( int year, int month, int day) {
        return get( String.format("%04d-%02d-%02d",year,month,day) );
    }
    /**  HolidayAlmanac オブジェクトを作成する */
    private HolidayAlmanac() {
    }

    public static void main(String [] args) {
        System.out.println( HolidayAlmanac.get("2008-02-11") );
    }
}
