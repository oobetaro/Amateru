/*
 * SweTest2.java
 *
 * Created on 2008/05/02, 13:35
 *
 */

package to.tetramorph.starbase.test;

import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import static java.lang.System.*;
import static java.lang.String.format;
/**
 * 一番基本的な方法でスイスエフェメリスにアクセスして、天体位置とアセンダントを
 * 求める。このプログラムは天体位置に狂いが生じる問題が発生したときの、原器的役割
 * を果たす。
 * @author 大澤義鷹
 */
public class SweTest2 {
    
    public static void main(String [] args) {
//        int year = 1964,month = 9, day = 30;
//        double hour = 5.0, minute = 35.0, second = 0.0;
//        // 経度と緯度は十進小数で指定する。
//        double longitude = 135.5, latitude = 34.67;
        int year = 1951, month = 9, day = 3;
        double hour = 3.0, minute = 3.0, second = 0.0;
        double longitude = 139.760388, latitude = 35.69307;
        //--------------------------------------------------------------------
        SweDate sd = new SweDate();		//時を表すオブジェクト
        SwissEph sw = new SwissEph();	//暦算オフジェクト
        double time_utc = hour + minute / 60D;
        sd.setDate(year,month,day,time_utc);
        sd.setCalendarType(sd.SE_GREG_CAL,sd.SE_KEEP_DATE);
        double tjd = sd.getJulDay() - 9d/24d;	//時差9時間を引く
        double dt  = sd.getDeltaT(tjd);
        double te = tjd + dt;
        out.println("jd  = " + tjd);
        out.println("ΔT = " + dt);
        out.println("jd + ΔT = " + te);
        StringBuffer serr = new StringBuffer();
        long iflgret;
        long iflag = SweConst.SEFLG_SPEED;
        double x2[] = new double[6]; //結果が求まる
        
        out.print("planet     \tlongitude\tlatitude\tdistance\tspeed long.\n");
        for(int p = SweConst.SE_SUN; p <= SweConst.SE_CHIRON; p++) {
            if (p == SweConst.SE_EARTH) continue;
            iflgret = sw.swe_calc(tjd + dt,p,(int)iflag,x2,serr);
            if(iflgret<0)
                out.println("error: " + serr.toString()+"\n");
            else if(iflgret != iflag)
                out.print("warning: iflgret != iflag. " + serr.toString()+"\n");
            String snam = sw.swe_get_planet_name(p);
            out.print(
                format("%10s"  ,snam )+"\t"+format("%11.7f",x2[0])+"\t"+
                format("%10.7f",x2[1])+"\t"+format("%10.7f",x2[2])+"\t"+
                format("%10.7f",x2[3])+"\n" );
        }
        double [] cusp  = new double[13];	//1室〜12室の度数が書き込まれる
        double [] ascmc = new double[10];	//AscやMCの値が書き込まれる。
        int ret = sw.swe_houses( tjd,(int)iflag,
            latitude, longitude,(int)'P',cusp,ascmc);
        for(int i=1; i<cusp.length; i++)
            out.println( i + " : " + cusp[i] );
        out.println("ascendant : "+ascmc[0]);
        out.println("       mc : "+ascmc[1]);
        out.println("     armc : "+ascmc[2]);
        out.println("   vertex : "+ascmc[3]);
    }
}
