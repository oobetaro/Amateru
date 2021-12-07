/*
 * Test2.java
 *
 * Created on 2007/10/08, 15:45
 *
 */

package to.tetramorph.starbase.chartparts;

/**
 *
 * @author 大澤義鷹
 */
public class Test2 {
  
  /**  Test2 オブジェクトを作成する */
  
  public static void main(String [] args) {
    for(int i=0,j=1; i<3; i++) {
      System.out.println("j = " + j);
      j = j << 1;
    }
  }
}
