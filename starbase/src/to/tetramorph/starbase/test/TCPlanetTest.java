/*
 * TCPlanetTest.java
 *
 * Created on 2008/09/14, 4:22
 *
 */

package to.tetramorph.starbase.test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import swisseph.TCPlanet;
import swisseph.TransitCalculator;

/**
 * TCPlanetで所定度数に天体がくる時刻を求める場合に、過去方向に向かって検索する
 * と火星などは間違った時刻が求まる場合がある。つまり180度にくる時を求めたはずが、
 * その時刻に火星は大きくそれた場所にいる。30度くらいは平気でずれる。
 * これが順方向検索の場合は、まだ問題がおきたことがない。また月や太陽など、
 * 逆行しない天体の過去方向への検索も問題ない。
 */
public class TCPlanetTest {
   static SwissEph eph = new SwissEph();
   /**
    * The position of specified Mars of time is displayed. 
    */
   static void printMarsPos( SweDate sd, SwissEph sweph,int planet_id ) {
       double jday = sd.getJulDay();
       double ET = jday + sd.getDeltaT( jday );
       StringBuffer errbuf = new StringBuffer();
       double results[] = new double[ 6 ];
       int flag = SweConst.SEFLG_SPEED;
       int res = sweph.swe_calc( ET, planet_id, flag, results, errbuf );
       if ( res < 0 || res != flag ) {
           System.out.println( errbuf.toString() );
       }
       System.out.println( sweph.swe_get_planet_name( planet_id ) 
                           + " Lon = " + results[0] );
   }
   /**
    * Time when Mars becomes 288 degrees is found. 
    * year,month,day  ...  Search beginning day.
    */
   static void findTime( int year, int month, int day ) {
        int planet_id = SweConst.SE_MARS; // 実験する天体を指定
        SweDate sdate = new SweDate();
        double hour   = 0; //時刻は0時に固定
        double minute = 0;
        double second = 0;
        double time = hour + minute / 60. + second / 3600.;
        TimeZone tz = TimeZone.getDefault();
        double timeDifference = tz.getRawOffset() / ( 24. * 3600 * 1000 );
        sdate.setDate( year, month, day, time );
        sdate.setCalendarType( SweDate.SE_GREG_CAL, SweDate.SE_KEEP_DATE );
        double jday = sdate.getJulDay() - timeDifference;
        double ET = jday + SweDate.getDeltaT( jday );

        int flags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
        TransitCalculator tc = new TCPlanet( eph,
                                             planet_id,
                                             flags,
                                             288.81406599 );
        boolean backward = true;
        double nextTransitET = eph.getTransitET( tc, ET, backward );
        Date foundDate = SweDate.getDate( nextTransitET );
        Calendar cal = Calendar.getInstance();
        cal.setTime( foundDate );
        System.out.printf( "Search start %04d-%02d-%02d ... ", year, month, day );
        System.out.printf( "Found Date : %tF %tT, ",cal, cal );

        SweDate sd = new SweDate( nextTransitET );
        sd.setCalendarType(SweDate.SE_GREG_CAL,SweDate.SE_KEEP_DATE);
        printMarsPos( sd, eph, planet_id );
    }
    /**
     * TEST START
     */
    public static void main( String [] args ) {
        findTime( 2008, 9,  14 ); // error   ,Search start 2008-09-14 ... Found Date : 2007-04-26 14:34:00, Mars Lon = 345.2354217912741
        findTime( 2007, 9,  14 ); // error   ,Search start 2007-09-14 ... Found Date : 2007-03-18 10:01:50, Mars Lon = 315.19931996177513
        findTime( 2007, 12, 31 ); // correct ,Search start 2007-12-31 ... Found Date : 2007-02-11 14:15:42, Mars Lon = 288.81463168074305
   }
}
