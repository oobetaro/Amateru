/*
 * Sector.java
 *
 * Created on 2006/10/26, 18:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
/**
 * 一星座とか一室分とかホロスコープにおける扇形の１区画のShapeを表現する。
 */
public class Sector {
  /** 扇形の始点側の直線 */
  public Shape line1;
  /** 扇形の終点側の直線 */
  public Shape line2;
  /** 扇形の外側の円弧 */
  public Shape outer;
  /** 扇形の内側の円弧 */
  public Shape inner;
  /** 扇形の全体のパス */
  public Shape sector;
  /** Creates a new instance of Sector */
  public Sector() {
  }
  /**
   * オブジェクトを作成する。
   * @param sector 全体を表現したShape
   * @param line1 始点側の直線
   * @param line2 終点側の直線
   * @param inner 内側の円弧
   * @param outer 外側の円弧
   */
  public Sector(Shape line1,Shape line2,Shape inner,Shape outer,Shape sector) {
    this.line1 = line1;
    this.line2 = line2;
    this.outer = outer;
    this.inner = inner;
    this.sector = sector;
  }
  /**
   * 分割されたドーナッツ円のShape配列を返す。
   * ドーナッツは扇形のShapeから構成されている。扇形のShapeはSectorオブジェクト
   * として管理されていて、扇形の全体を表すShape、外側の円弧、内側の円弧、
   * 両サイドの線、というよう各パーツごとにShapeをもっている。
   * cusps[]にはドーナッツ円をどこで分割するかを指定する。獣帯リングであれば、
   * cusps [] = { 0,30d,60d,90d...330d };といった配列を与える。ハウスならもっと
   * 中途半端な値が渡されるだろうし、数だって12個とは限らない。
   * cuspsの値は、部屋の順番にならんでいなければならない。つまり1室のカスプの次は2室
   * のカスプが指定されているという前提にたっていて、1室の次に3室を指定したりするこ
   * とはできない。
   * @param asc 原点角から何度回転させるか。
   * @param x 原点x
   * @param y 原点y
   * @param w 幅(pixcel) 外円の最大直径
   * @param r1 内円半径(0〜1) 幅(w)に対してのパーセンテージで指定
   * @param r2 外円半径(0〜1) 幅(w)に対してのパーセンテージで指定
   * @param cusps 分割度数の配列 黄道座標での度数
   * @return cusps.lengthと同じサイズのSector配列
   */
  public static Sector [] getRing(double x,double y,double w,double r1,double r2,double [] cusps,double asc) {
    //
    Sector [] sectorArray = new Sector[cusps.length];
    //カスプの配列を一つ増やし、最初の値を入れたものを作成
    double [] c = new double[cusps.length+1];
    System.arraycopy(cusps,0,c,0,cusps.length);
    c[cusps.length] = cusps[0];
    double li = (r1 * w);
    double lo = (r2 * w);
    double ri = (r1 * w)/2; //内円 実効半径
    double ro = (r2 * w)/2; //外円    〃    
    Area ellipseArea = new Area(new Ellipse2D.Double(x-ri,y-ri,li,li));
    for(int i=0; i<cusps.length; i++) {
      double a0 = ((c[i] + 180d) - asc) % 360d;   //円弧の始点(360度内にまるめる)
      double a1 = ((c[i+1] + 180d) - asc) % 360d; //円弧の終点
      if(a0 > a1) a1 += 360;
      double da = a1 - a0;
      Arc2D ai = new Arc2D.Double(x-ri,y-ri,li,li,a0,da,Arc2D.OPEN); //内円Shape
      //↓この条件が成立するとき、引き算に誤差がでて線がはみ出す事がある。
      if((a0 + da) == 360d) da += 0.001; //それで微妙に値を狂わせバグ(?)を回避する。
      Arc2D ao = new Arc2D.Double(x-ro,y-ro,lo,lo,a0,da,Arc2D.OPEN); //外円Shape
      Area sector = new Area(new Arc2D.Double(x-ro,y-ro,lo,lo,a0,da,Arc2D.PIE));
      sector.subtract(ellipseArea);
      //扇形の両サイドの直線のShapeを作成
      Line2D.Float line1 = new Line2D.Float(ai.getStartPoint(),ao.getStartPoint()); //内円と外円の始点二つを結ぶ線
      Line2D.Float line2 = new Line2D.Float(ai.getEndPoint(),ao.getEndPoint()); //同じく終点を結ぶ線
      sectorArray[i] = new Sector(line1,line2,ai,ao,sector);
    }
    return sectorArray;
  }
  static void dumpShape(Shape area) {
    System.out.println("-----------------");
    PathIterator ite = area.getPathIterator(null);
    for(; ! ite.isDone(); ite.next()) {
       float [] v = new float[6];
       int result = ite.currentSegment(v);
       System.out.print(result + " : ");
       for(float f : v) System.out.print(f + ",  ");
       System.out.println();
       
    }
  }

}
