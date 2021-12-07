/*
 * Test.java
 *
 * Created on 2006/12/26, 10:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.test;

import to.tetramorph.starbase.util.AngleConverter;

/**
 * @author 大澤義鷹
 */
public class Test {
  
  /** Creates a new instance of Test */
  static String conv(Double value) {
        double [] v = AngleConverter.sexagesimal(value);
        String sign = v[0] < 0 ? "S" : "N";
        String sv = String.format("%2.3f",v[3]).replaceAll("\\.","\u2033\\.");
        return String.format("%s%d\u00B0%d\u2032%s",
                            sign,(int)v[1],(int)v[2],sv);  }
  public static void main(String [] args) {
      String s = " 9.55";
      System.out.println("'" + s.replaceAll("^ ","&nbsp;") + "'");
  }
}
