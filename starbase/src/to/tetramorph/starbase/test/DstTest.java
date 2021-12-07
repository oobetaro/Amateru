/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.test;

import java.util.Calendar;
import to.tetramorph.time.JDay;

/**
 * 夏時間の判定のテスト
 * @author 大澤義鷹
 */
public class DstTest {

    static void dump2(Calendar cal) {
        for ( int i=0; i<8; i++ ) {
            System.out.println(
                String.format(" %tF %tT,%d,%d",
                    cal,
                    cal,
                    cal.get(Calendar.DST_OFFSET),
                    cal.getTimeInMillis()));
            cal.add(Calendar.MINUTE, 30);
        }
        System.out.println("");
    }

    static void dump(Calendar cal) {
        for ( int i=0; i<8; i++ ) {
            System.out.println(
                String.format(" %tF %02d:%02d:%02d,%d,%d",
                    cal,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND),
                    cal.get(Calendar.DST_OFFSET),
                    cal.getTimeInMillis()));
            cal.add(Calendar.MINUTE, 30);
        }
        System.out.println("");
    }

    public static void main(String [] args) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND,0);
        cal.set(1948, 5-1, 1,23,0,0);
        dump(cal);
        cal.set(1948,9-1,10,23,0,0);
        dump(cal);

        cal.set(1949,4-1,2,23,0,0);
        dump(cal);
        cal.set(1949,9-1,9,23,0,0);
        dump(cal);

        cal.set(1950,5-1,6,23,0,0);
        dump(cal);
        cal.set(1950,9-1,8,23,0,0);
        dump(cal);
        
        //1951年6月15日は日本における夏時間である。
        cal.set(1951, 5-1, 5,23,0,0);
        dump(cal);
        cal.set(1951,9-1,7,23,0,0);
        dump(cal);
        JDay.get(cal);
    }
}
