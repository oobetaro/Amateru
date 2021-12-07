/*
 * JDayTest.java
 *
 * Created on 2008/05/02, 13:53
 *
 */

package to.tetramorph.starbase.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import swisseph.SweDate;
import static java.util.GregorianCalendar.*;
import to.tetramorph.time.JDay;

/**
 *
 * @author 大澤義鷹
 */
public class JDayTest {
    
    /**  JDayTest オブジェクトを作成する */
    public JDayTest() {
    }
    private static GregorianCalendar beginGregorian;
    static {
        beginGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        beginGregorian.set(1582,10-1,15,0,0,0);
        beginGregorian.set(MILLISECOND,0);
    }
    /**
     * グレゴリアンカレンダーの値(タイムゾーンも含む)からユリウス日を返す。
     * 夏時間制度を導入している地域で、夏時間実施期間中であればその分の時差補正が
     * 考慮されユリウス日が決定する。
     */
    public static double get( GregorianCalendar cal ) {
        int hour = cal.get(HOUR_OF_DAY);
        int minute = cal.get(MINUTE);
        int second = cal.get(SECOND);
        double t = ( second / 60.0 + minute ) / 60.0 + hour;
        int year = cal.get(YEAR);
        int month = cal.get(MONTH) + 1;
        int day = cal.get(DAY_OF_MONTH);
        SweDate sd = new SweDate();
        if ( cal.getTimeInMillis() >= beginGregorian.getTimeInMillis() ) { 
            //グレゴリオ暦
            //System.out.println("グレゴリオ暦です");
            sd.setCalendarType(sd.SE_GREG_CAL,sd.SE_KEEP_DATE);
        } else {
            //System.out.println("ユリウス暦です");
            sd.setCalendarType(sd.SE_JUL_CAL,sd.SE_KEEP_DATE);
            //SweDateの紀元前は0年からなのでBC 1年なら0年、BC2年なら-1年。だから調整。
            if ( cal.get(ERA) == BC ) year = 1 - year;
        }
        sd.setDate(year,month,day,t);
        //夏時間制度のある地域の場合は、夏時間実施中か調べて時差にオフセットを加算
        TimeZone tz = cal.getTimeZone();
        double daylight = 0;
        if(tz.useDaylightTime()) {
            if(tz.inDaylightTime(new java.util.Date(cal.getTimeInMillis()))) {
                daylight = tz.getDSTSavings();
            }
        }
        double timeDifference =
            (double)((cal.getTimeZone().getRawOffset() + daylight) / 1000 ) / 3600d;
        return sd.getJulDay() - timeDifference / 24.0;
    }
    public static void test() {
        for ( int y = 1964; y > 1945; y-- ) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("JST"));
        cal.set(y,9-1,3,0,0,0);
        long t = cal.getTimeInMillis();
        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal2.set(y,9-1,3,0,0,0);
        long t2 = cal2.getTimeInMillis();
        System.out.println("year = " + y);
        System.out.println("cal = " + t);
        System.out.println("cal2= " + t2);
        System.out.println(  Math.abs(t) - Math.abs( t2 ) );
        }
    }
    public static void main(String [] args) {
        test();
//        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("JST"));
//        cal.set(1951,9-1,3,0,0,0);
//        cal.set( Calendar.MILLISECOND, 0);
//        for ( int i=0; i<24; i++ ) {
//            long ms = cal.getTimeInMillis();
//            System.out.print( i + " : " + ms + " : " + get(cal) + "       ");
//            System.out.println( JDay.get(cal) );
//            cal.add( Calendar.HOUR_OF_DAY, 1);
//        }
    }
}
