/*
 * Test.java
 *
 * Created on 2007/09/26, 6:42
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.util.HashMap;
import java.util.Map;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;

/**
 *
 * @author 大澤義鷹
 */
public class Test {
  
  /**  Test オブジェクトを作成する */
  public Test() {
  }
  /**
   * ハッシュに入れると参照アドレスが変わる疑いがあってテストしてみたが
   * 変化しない。
   */
  public static void main(String [] args) {
    System.out.println(System.getProperty("java.version"));
    System.out.println(System.getProperty("java.vm.version"));
  }
}
