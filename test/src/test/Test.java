/*
 * Test.java
 *
 * Created on 2007/05/25, 23:58
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Font;

/**
 *
 * @author 大澤義鷹
 */
public class Test {
  /**
   * <pre>
   *       (y) 90ﾟ
   * 180ﾟ   |
   * (-x)---+---(x) 0ﾟ
   *        |
   *       (-y)270ﾟ
   * この座標系で角度を返す。
   * </pre>
   */
  private static double trigon(double x,double y) {
    //Javaには負のゼロ(-0.0)が存在し、その値が入ると0度を返すべきときに180度
    //になる。-0.0==0.0はtrueと判定されるので、0または-0が入ったときは強制的に
    //ゼロについている符号を取り去る。
    if(x == 0.0) x = Math.abs(x);
    if(y == 0.0) y = Math.abs(y);
    double a = Math.atan(x/y) * 180d / Math.PI;
    if( y<0 ) a -= 180.0;
    if( a<0 ) a += 360.0;
    a = 180 - a -90;
    if(  a<0 ) a += 360;
    return a;
  }
  /**
   * @param a 角度1
   * @param b 角度2
   */
  public static double angleDistance(double a,double b) {
    a = a % 360;
    b = b % 360;
    double d = Math.abs(a-b);
    if(d > 180) {
      d = 360 - d;
      return (a < b) ? -d : d;
    }
    return (a < b) ? d : -d;
  }
  public static void main(String [] args) {
    test2();
  }
  /**
   * フォント取得にかかる時間を計ってみる。1000回やって50ms程度。1回0.05ms。
   * つまりそんなに時間がかかるようなことはしていない。
   */
  public static void test() {
    Font font = new Font("DialogInput",Font.PLAIN,10);
    float size = 10.0f;
    long t = System.currentTimeMillis();
    for(int i=0; i<1000; i++) {
      Font f = font.deriveFont(size);
      size += 0.01;
    }
    System.out.printf("%s %d %s\n","時間：",System.currentTimeMillis() - t,"[ms]");
  }
  /**
   * 円周上の2点間の角距離を求める。
   * @param a 開始角度(0-359.999...);
   * @param b 終了角度(0-359.999...);
   * @param isBackwards 時計回りに角度を求める場合はfalse。半時計回りの場合はtrue。
   */
  public static double getArcLength(double a,double b,boolean isBackwards) {
    a = a % 360;
    b = b % 360;
    if(isBackwards) {
      if( a < b) return 360 + a - b;
      else if( a > b) return a - b;
    } else {
      if( a < b ) return b-a;
      else if( a > b) return 360 + b - a;
    }
    return 0;
  }

  public static void test2() {
    double b = 10;
    for(double a = 0; a<370; a++) {
      double len = getArcLength(a,b,true);
      System.out.printf("a = %f, b = %f, len = %f\n",a,b,len);
    }
  }
}
