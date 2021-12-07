/*
 * GaugeDial.java
 *
 * Created on 2006/10/29, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;
import java.awt.BasicStroke;
import static java.lang.Math.*;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import swisseph.DblObj;

/**
 * ホロスコープの部品で、目盛りのリングを描画する。
 * 最終的には描画色や様々な条件をプロパティで受け取る
 * @author 大澤義鷹
 */
public class GaugeDial extends ChartParts {
  static Stroke stroke = new BasicStroke(
    0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
  boolean isVisible = true;
  static final int MAX_LENGTH = 360;
  Line2D [] lines = new Line2D.Double[ MAX_LENGTH ];
  /**
   * 各サインの0度の目盛りを描画しない。setOptionメソッドで指定する引数。
   */
  public static final int NOT_DRAW_ZERO_DEGREES = 1;
  /**
   * 各サインの0度の目盛りを描画する。setOptionメソッドで指定する引数。
   */
  public static final int DRAW_ZERO_DEGREES = 0;
  int option = 0;
  
  public GaugeDial() {
    for(int i=0; i<MAX_LENGTH; i++) lines[i] = new Line2D.Double();
  }
  public GaugeDial(BasePosition bp) {
    super(bp);
  }
  /**
   * 描画オプションの設定。NOT_DRAW_ZERO_DEGREES等のフィールド定数を指定。
   */
  public void setOption(int option) {
    this.option = option;
  }
  
  private Color gaugeColor = Color.GRAY;
  
  public void setGaugeColor(Color color) {
    gaugeColor = color;
  }
  public Color getGaugeColor() {
    return gaugeColor;
  }
  /**
   * 可視/不可視を設定する。trueなら可視。
   */
  public void setVisible(boolean b) {
    isVisible = b;
  }
  /**
   * 可視/不可視を返す。
   */
  public boolean isVisible() {
    return isVisible;
  }
// GernalPathを使うと、パス変更のとき大きなインスタンスを作り直すことに
// なるので別の方法に変更
//  
//  GeneralPath path = new GeneralPath();
//  private void getGeneralPath() {
//    path = new GeneralPath();
//    double d = diameter + diameterOffset;
//    double r0 = (d * bp.w) / 2;         //内円 実効半径
//    double r1 = ((d + 0.008) * bp.w) / 2; //
//    double r2 = ((d + 0.02) * bp.w) / 2;
//    double r3 = ((d + 0.012) * bp.w) / 2;
//    for(int a=0; a<360; a++) {
//      double a0 = -(a + 180d - (roll + ascendant));
//      double cv = cos(a0 * PI / 180d);
//      double sv = sin(a0 * PI / 180d);
//      float x0 = (float)(cv * r0 + bp.x);
//      float y0 = (float)(sv * r0 + bp.y);
//      double r = 0;
//      if(a % 10 == 0) r = r2;
//      else if( a % 5 == 0) r = r3;
//      else r = r1;
//      float x1 = (float)(cv * r + bp.x);
//      float y1 = (float)(sv * r + bp.y);
//      if(a % 30 == 0 && ((option & NOT_DRAW_ZERO_DEGREES) != 0)) continue;
//      if(bp.w < 350 && r == r1) continue;
//      path.moveTo(x0,y0);
//      path.lineTo(x1,y1);
//    }
//    path.closePath();
//  }
  /**
   * 360のゲージ線を用意して配列に格納
   * 30度ごとの線を非表示にしたり、画面サイズが小さすぎるときはゲージの歯を減らす
   * ことが必要だが、その場合描画しない歯の配列にはnullを入れ、g.draw()するときに
   * 判別する。
   */
  private void createPaths() {
    double d = diameter + diameterOffset;
    double r0 = (d * bp.w) / 2;         //内円 実効半径
    double r1 = ((d + 0.008) * bp.w) / 2; //
    double r2 = ((d + 0.02) * bp.w) / 2;
    double r3 = ((d + 0.012) * bp.w) / 2;
    for(int a=0; a < MAX_LENGTH; a++) {
      double a0 = -(a + 180d - (roll + ascendant));
      double cv = cos(a0 * PI / 180d);
      double sv = sin(a0 * PI / 180d);
      double x0 = (cv * r0 + bp.x);
      double y0 = (sv * r0 + bp.y);
      double r = 0;
      if(a % 10 == 0) r = r2;
      else if( a % 5 == 0) r = r3;
      else r = r1;
      double x1 = (cv * r + bp.x);
      double y1 = (sv * r + bp.y);
      // 30度おきの歯を描かないモードのとき
      if(a % 30 == 0 && ((option & NOT_DRAW_ZERO_DEGREES) != 0)) {
        lines[a] = null;
        continue;
      }
      // 画面サイズが小さいとき
      if(bp.w < 350 && r == r1) {
        lines[a] = null;
        continue;
      }
      //配列がnullになっているものは再作成する
      if(lines[a] == null) lines[a] = new Line2D.Double();
      lines[a].setLine(x0,y0,x1,y1);
    }
  }
  //ゲージのパラメターをキャッシュする変数
  private double ca_diameter, ca_x, ca_y, ca_w,
                   ca_roll, ca_ascendant, ca_diameterOffset;
  private int ca_option;
  /**
   * このオブジェクトにセットされている描画パラメターにしたがって、ダイアルゲージ
   * を描画する。
   */
  public void draw() {
    if( ! isVisible) return;
    bp.g.setStroke(stroke);
    bp.g.setPaint(gaugeColor);
    //パラメターに変更があったときだけパスを作成。速度とリソースを稼ぐ
    if(ca_diameter != diameter || ca_option != option || ca_ascendant != ascendant ||
      ca_x != bp.x || ca_y != bp.y || ca_w != bp.w || ca_roll != roll || ca_diameterOffset != diameterOffset ) {
      //getGeneralPath();
      createPaths();
      ca_x = bp.x; ca_y = bp.y; ca_w = bp.w; ca_roll = roll;
      ca_diameter = diameter; ca_option = option;
      ca_ascendant = ascendant; ca_diameterOffset = diameterOffset;
    }
    for(int i=0; i<MAX_LENGTH; i++) {
      if(lines[i] == null) continue; //nullのものはスキップ
      bp.g.draw(lines[i]);
    }
    //bp.g.draw(path);
  }
//  public static void main(String args[]) {
//    Runtime rt = Runtime.getRuntime();
//    GaugeDial dg = new GaugeDial();
//    BasePosition bp = new BasePosition();
//    bp.x = 200; bp.y = 200; bp.w = 400;
//    dg.setBasePosition(bp);
//    long t = System.currentTimeMillis();
//    long mem = rt.freeMemory();
//    for(int i=0; i<1000; i++) {
//      //dg.getGeneralPath();
//      dg.createPaths();
//    }
//    System.out.printf("使用メモリ : %d bytes\n",mem - rt.freeMemory());
//    System.out.printf("%d [ms]\n",System.currentTimeMillis() - t);
//  }
}
