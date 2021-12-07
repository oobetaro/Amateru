/*
 * SweTest6.java
 *
 * Created on 2007/07/02, 21:51
 *
 */

package to.tetramorph.starbase.chartparts;

import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

/**
 * �ԓ����W�œV�̈ʒu�����߂�
 */
public class SweTest6 {
  
  public static void main(String [] args) {
    SweDate date = new SweDate();		//����\���I�u�W�F�N�g
    SwissEph eph = new SwissEph();	//��Z�I�t�W�F�N�g
    //�v�Z����1964-09-30 AM 05:35:00�Ōv�Z
    double hour = 5d;
    double minute = 35d;
    double second = 0d;
    double timeByUTC = hour + minute/60d + second/3600;
    double timeDifference = 9d / 24d;
    int year = 1964,month = 9, day = 30;
    
    date.setDate(year,month,day,timeByUTC);
    date.setCalendarType(date.SE_GREG_CAL,date.SE_KEEP_DATE);
    double jday = date.getJulDay() - timeDifference;
    double te = jday + SweDate.getDeltaT(jday);
    
    StringBuffer serr=new StringBuffer();
    int flgret;
    //�ԓ����W�ŋ��߂�
    int flg;
    flg  = SweConst.SEFLG_SPEED;
    flg |= SweConst.SEFLG_EQUATORIAL;
    double x2[] = new double[6]; //���ʂ����܂�

    System.out.printf("%4s %9s %8s %9s %7s %8s %7s\n",
      "�V��","�Ԍo","�Ԉ�","����","�Ԍo��","�Ԉܑ�","������");
    for(int p=SweConst.SE_SUN; p <= SweConst.SE_PLUTO; p++) {
      if(p == SweConst.SE_EARTH) continue;
      
      flgret = eph.swe_calc(te,p,flg,x2,serr);
      if(flgret<0)
        System.out.println("error: " + serr.toString()+", flgret = " + flgret);
      else if(flgret != flg) {
        System.out.println("warning: flgret != flag. " + serr.toString());
        System.out.printf("%s%x%s%x\n","flg = ", flg, ", flgret = ", flgret);
      }
      String snam = eph.swe_get_planet_name(p);
      
      System.out.printf("%5s %11.4f %10.2f %10.2f %10.2f %10.2f %10.2f\n",
        snam.substring(0,3),x2[0],x2[1],x2[2],x2[3],x2[4],x2[5]);
    }
  }
  
}
