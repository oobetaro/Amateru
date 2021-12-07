/*
 * SweTest.java
 *
 * Created on 2008/01/13, 0:04
 *
 */

package to.tetramorph.starbase.test;

import java.util.GregorianCalendar;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import to.tetramorph.time.JDay;
import static java.util.GregorianCalendar.*;
import java.util.TimeZone;
/**
 *
 * @author 大澤義鷹
 */
public class SweTest {
    private static GregorianCalendar beginGregorian;
    private static final String [] youbi = { "無効","日","月","火","水","木","金","土" };
    static {
        beginGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        beginGregorian.set(1582,10-1,15,0,0,0);
        beginGregorian.set(MILLISECOND,0);
    }
    static void test() {
        GregorianCalendar cal = new GregorianCalendar(1540,7-1,4,0,0,0);
        //GregorianCalendar cal = new GregorianCalendar(1964,9-1,30,0,0,0);
        System.out.println("JDay = " + get(cal));
    }
    /**
     * グレゴリアンカレンダーの値(タイムゾーンも含む)からユリウス日を返す。
     * 夏時間制度を導入している地域で、夏時間実施期間中であればその分の時差補正が
     * 考慮されユリウス日が決定する。
     */
    public static double get(GregorianCalendar cal) {
        int hour = cal.get(HOUR_OF_DAY);
        int minute = cal.get(MINUTE);
        int second = cal.get(SECOND);
        double t = ( second / 60.0 + minute ) / 60.0 + hour;
        int year = cal.get(YEAR);
        int month = cal.get(MONTH) + 1;
        int day = cal.get(DAY_OF_MONTH);
        System.out.printf("%d年%d月%d日\n",year,month,day);
        System.out.println("入力日のミリ秒 = "+cal.getTimeInMillis());
        System.out.println("切り替え日のミリ秒 = "+beginGregorian.getTimeInMillis());
        
        SweDate sd = new SweDate();
        if(cal.getTimeInMillis() >= beginGregorian.getTimeInMillis() ) { //グレゴリオ暦
        //if(cal.before(beginGregorian)) {
            System.out.println("グレゴリオ暦です");
            sd.setCalendarType(sd.SE_GREG_CAL,sd.SE_KEEP_DATE);
        } else {
            System.out.println("ユリウス暦です");
            sd.setCalendarType(sd.SE_JUL_CAL,sd.SE_KEEP_DATE);
            //SweDateの紀元前は0年からなのでBC 1年なら0年、BC2年なら-1年。だから調整。
            if(cal.get(ERA) == BC) year = 1 - year;
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
    
    public static void main(String [] args) {
        test();
        SweDate sd = new SweDate();		//時を表すオブジェクト
        SwissEph sw = new SwissEph();	//暦算オフジェクト
        double time_utc = 0; //5d + 35d/60d; //35d/60d;//UTCによる時刻表現
        int year = 1540,month = 7, day = 4;
        //int year = 1964,month = 9, day = 30;
        sd.setDate(year,month,day,time_utc);
        sd.setCalendarType(sd.SE_GREG_CAL,sd.SE_KEEP_DATE);
        double tjd = sd.getJulDay() - 9d/24d;
        System.out.println("JDay Original = " + tjd);
        double te = tjd + sd.getDeltaT(tjd);
        
        StringBuffer serr=new StringBuffer();
        long iflgret;
        long iflag = SweConst.SEFLG_SPEED;
        double x2[] = new double[6]; //結果が求まる
        
        System.out.print("planet     \tlongitude\tlatitude\tdistance\tspeed long.\n");
        
        for(int p = SweConst.SE_SUN; p <= SweConst.SE_MOON; p++) {
            if (p == SweConst.SE_EARTH) continue;
            iflgret = sw.swe_calc(te,p,(int)iflag,x2,serr);
            if(iflgret<0)
                System.out.println("error: " + serr.toString()+"\n");
            else if(iflgret != iflag)
                System.out.print( "warning: iflgret != iflag. " + serr.toString() + "\n" );
            String snam = sw.swe_get_planet_name(p);
            System.out.printf("%10s,%3.2f,%3.2f,%3.2f,\n",x2[0],x2[1],x2[2],x2[3]);
        }
    }
    
}
