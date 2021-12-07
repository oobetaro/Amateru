/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.test;

import java.util.TimeZone;

/**
 *
 * @author 大澤義鷹
 */
public class TimeZoneTest {
//    public static void main(String [] args) {
//        String [] array = TimeZone.getAvailableIDs();
//        for ( String id : array ) {
//            TimeZone tz = TimeZone.getTimeZone(id);
//            System.out.println(id + ", " + tz.getID());
//        }
//    }
    static boolean isGMTOffset( String value ) {
        return value.matches("^GMT(\\+|\\-)[0-9]{2}:[0-9]{2}$");
    }
    public static void main(String [] args) {
        //System.out.println("GMT Check " + isGMTOffset("GMT-09:30"));
        TimeZone tz = TimeZone.getTimeZone("GMT+01:00");
        System.out.println(tz);
    }
}
