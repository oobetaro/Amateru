/*
 * BasePosition.java
 *
 * Created on 2007/05/22, 23:21
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Component;
import java.awt.Graphics2D;

/**
 * ホロスコープの部品描画オブジェクトに、描画する中心座標や最大幅、Graphics2D
 * オブジェクト、描画するコンポーネントを渡すためのオブジェクト。
 * これらの要素は、このオブジェクトを参照することで引き渡される仕組みで、
 * 他の複数の部品はこのオブジェクトを参照するようになっているので、
 * このオブジェクトのフィールドx,y,w,component,gを直接書き換えれば、その変更は
 * 自動的に部品群に伝達される。
 * @author 大澤義鷹
 */
public class BasePosition {
  /** 中心X座標 */
  public double x;
  /** 中心Y座標 */
  public double y;
  /** 描画領域の幅(単位pixcel) */
  public double w;
  /** グラフィックスオブジェクト */
  public Graphics2D g;
  /**  BasePosition オブジェクトを作成する */
  public BasePosition() {
  }  
}
