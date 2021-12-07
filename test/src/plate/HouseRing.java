/*
 * HouseRing.java
 *
 * Created on 2006/10/28, 18:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import java.awt.Color;
import static java.lang.Math.*;
import java.awt.Graphics2D;
import to.tetramorph.starbase.*;

/**
 * ホロスコープの輪を表現するクラス。ハウスやサインの扇形で組まれた車輪を描く。
 * @author 大澤義鷹
 */
public class HouseRing {
  public static final int OUTER_ARC = 1;
  public static final int INNER_ARC = 2;
  public static final int SEPARATOR = 4;
  public static final int FILL = 8;
  protected int condition = OUTER_ARC | INNER_ARC | SEPARATOR;
  public static final double [] ZODIAC_ANGLES =
    { 0d,30d,60d,90d,120d,150d,180d,210d,240d,270d,300d,330d };
  double x,y;
  double w;
  double ri;
  double ro;
  double [] cusps;
  double asc;
  public Sector [] sectors;
  Graphics2D g;
  public Color [] bgColors;
  public Color innerLineColor = Color.BLACK;
  public Color outerLineColor = Color.BLACK;;
  public Color separatorLineColor = Color.BLACK;;
  public boolean isNoBorder = false;
  public boolean isNoBackground = false;
  /** 
   * リングが未設定の空のオブジェクトを作成する。
   */
  public HouseRing() {
  }

  /**
   * ホロスコープ用の分割されたリングを用意する。
   * リングはキャッシュされて、前回と同じパラメターならキャッシュされているものを使う。
   * @param x リングの中心になるx座標
   * @param y リングの中心になるy座標
   * @param w リングの最大幅(pixcel)
   * @param ri リングの内円(wに対するパーセンテージで指定し0～1の値を取る)
   * @param ro リングの外円(wに対するパーセンテージで指定し0～1の値を取る)
   * @param cusps カスプのリスト
   * @param asc リングの回転角。獣帯なら通常はアセンダントの値をセットする。
   */
  public Sector [] getRing(double x,double y,double w,double ri,double ro,
    double [] cusps,double asc) {
    boolean changed = false;
    if(sectors != null) {
      if(this.x == x && this.y == y && this.w == w && this.ri == ri 
         && this.ro == ro && this.asc == asc) {
        for(int i=0; i<cusps.length; i++) {
          if(this.cusps[i] != cusps[i]) { changed = true; break; } 
        }
      } else changed = true;
    } changed = true;
    if(changed) {
      this.x = x; this.y = y; this.w = w; this.ri = ri; this.ro = ro;
      this.cusps = cusps; this.asc = asc;
      sectors = Sector.getRing(x,y,w,ri,ro,cusps,asc);
    }
    return sectors;
  }
  /**
   * ホロスコープ用の分割されたリングのパラメターをセットする
   * @param x リングの中心になるx座標
   * @param y リングの中心になるy座標
   * @param w リングの最大幅(pixcel)
   * @param ri リングの内円(wに対するパーセンテージで指定し0～1の値を取る)
   * @param ro リングの外円(wに対するパーセンテージで指定し0～1の値を取る)
   * @param cusps カスプのリスト。nullを指定すると0,30,60..というように12星座の度数リストを指定したことになる。
   * @param asc リングの回転角。獣帯なら通常はアセンダントの値をセットする。
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double ri,double ro,double [] cusps) {
    this.g = g;
    this.x = x; this.y = y; 
    this.w = w; this.asc = asc; this.ri = ri;
    this.ro = ro;
    this.cusps = cusps == null ? ZODIAC_ANGLES : cusps;
  }
  public void draw() {
    Sector [] array = Sector.getRing(x,y,w,ri,ro,cusps,asc);
    if((condition & 8) != 0 && (! isNoBackground )) {
      for(int i=0; i<array.length; i++) {
        if(bgColors != null) {
          int c = i % bgColors.length;
          g.setPaint(bgColors[c]);
          g.fill( array[i].sector );
        }
      }
    }
    if(isNoBorder) return;
    g.setPaint(Color.BLACK);
    for(int i=0; i<array.length; i++) {
      if((condition & 1) != 0) {
        g.setPaint(outerLineColor);
        g.draw(array[i].outer);
      }
      if((condition & 2) != 0) {
        g.setPaint(innerLineColor);
        g.draw( array[i].inner );
      }
      if((condition & 4) != 0) {
        g.setPaint(separatorLineColor);
        g.draw( array[i].line1 );
      }
    }
  }
  /**
   * 塗りつぶしの有無、外円弧の描画の有無、内円弧の描画の有無、分割線の有無
   */
  public void setPaintCondition(int condition) {
    this.condition = condition;
  }
}
