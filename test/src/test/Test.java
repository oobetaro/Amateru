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
 * @author ���V�`��
 */
public class Test {
  /**
   * <pre>
   *       (y) 90�
   * 180�   |
   * (-x)---+---(x) 0�
   *        |
   *       (-y)270�
   * ���̍��W�n�Ŋp�x��Ԃ��B
   * </pre>
   */
  private static double trigon(double x,double y) {
    //Java�ɂ͕��̃[��(-0.0)�����݂��A���̒l�������0�x��Ԃ��ׂ��Ƃ���180�x
    //�ɂȂ�B-0.0==0.0��true�Ɣ��肳���̂ŁA0�܂���-0���������Ƃ��͋����I��
    //�[���ɂ��Ă��镄������苎��B
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
   * @param a �p�x1
   * @param b �p�x2
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
   * �t�H���g�擾�ɂ����鎞�Ԃ��v���Ă݂�B1000������50ms���x�B1��0.05ms�B
   * �܂肻��ȂɎ��Ԃ�������悤�Ȃ��Ƃ͂��Ă��Ȃ��B
   */
  public static void test() {
    Font font = new Font("DialogInput",Font.PLAIN,10);
    float size = 10.0f;
    long t = System.currentTimeMillis();
    for(int i=0; i<1000; i++) {
      Font f = font.deriveFont(size);
      size += 0.01;
    }
    System.out.printf("%s %d %s\n","���ԁF",System.currentTimeMillis() - t,"[ms]");
  }
  /**
   * �~�����2�_�Ԃ̊p���������߂�B
   * @param a �J�n�p�x(0-359.999...);
   * @param b �I���p�x(0-359.999...);
   * @param isBackwards ���v���Ɋp�x�����߂�ꍇ��false�B�����v���̏ꍇ��true�B
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
