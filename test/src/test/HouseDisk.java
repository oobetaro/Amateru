/*
 * HouseDisk.java
 *
 * Created on 2007/05/02, 20:13
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;
import to.tetramorph.starbase.chartplate.DialGauge;
import to.tetramorph.starbase.chartplate.HouseNumberDial;
import to.tetramorph.starbase.chartplate.HouseRing;
import to.tetramorph.starbase.chartplate.MarkerNeedle;
import to.tetramorph.starbase.chartplate.NumberNeedle;
import to.tetramorph.starbase.chartplate.PlanetActionListener;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChartFactor;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * アスペクト以外のホロスコープの部品を集約して、一つの部品とするクラス。
 * ハウスとその中の天体、ゲージ、度数表示などから構成されるマクロ的な部品。
 * @author 大澤義鷹
 */
public class HouseDisk implements HoroscopeDisk {
  // ホロスコープを構成するサインや天体やカスプ線描画オブジェクト
  public DialGauge gauge2 = new DialGauge();
  public PlanetNeedle planetNeedle;
  public HouseRing houseRing = new HouseRing();
  public HouseRing houseRing2 = new HouseRing();
  public MarkerNeedle markerNeedle = new MarkerNeedle();
  public MarkerNeedle markerNeedle2 = new MarkerNeedle();
  public NumberNeedle cuspAngleNumberNeedle = new NumberNeedle();
  public NumberNeedle planetAngleNumberNeedle = new NumberNeedle();
  public HouseNumberDial houseNumberDial = new HouseNumberDial();
  Component component;
  // 外部から提供される変数
  Graphics2D g;
    double cx,cy,W;
  double asc;
  double [] cusps;
  Aspect selectedAspect;
  //  内部で生成するデータ
  String [] cuspsString;
  List<Body> planetList; //AC,MC,IC,DC以外の保管用
  double [] planetsAngle;
  String [] planetsAngleString;

  /**
   *  
   * HouseDisk オブジェクトを作成する
   */
  public HouseDisk(Component c,PlanetActionListener l) {
    this.component = c;
    planetNeedle = new PlanetNeedle(c,l);
  }
  /** 描画するグラフィックスオブジェクトをセットする */
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }
  /** 
   * 描画する基準とする座標をセットする。
   * @param cx 中心座標x
   * @param cy 中心座標y
   * @param W 基準となる描画エリアの幅(単位pixel つねに正方形とみなす)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }
  /**
   * ハウスカスプの黄道座標を配列で指定する。[0～11]まで。
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps;
    cuspsString = ChartFactor.formatSignAngles(cusps,0); //表示用カスプ度数のリスト
  }
  /**
   * 指定されたアスペクトを持つ天体を赤点灯する。
   */
  public void setSelectedAspect(Aspect aspect) {
    this.selectedAspect = aspect;
  }
  public void setAC(double ac) {
    this.asc = ac;
  }
  /**
   * 表示する天体のリストをセットする。
   */
  public void setPlanetList(List<Body> planetList) {
    this.planetList = planetList;
    planetsAngle = ChartFactor.getPlanetsPlotAngle(planetList);
    planetsAngleString = ChartFactor.formatSignAngles(
      ChartFactor.getPlanetsAngle(planetList),0); //天体の表示用度数リスト    
  }
  double wi,wo;
  public void setSize(double wi,double wo) {
    this.wi = wi;
    this.wo = wo;
  }
  /**
   * このオブジェクトに登録されている条件で描画を実行する。
   */
  public void draw() {
    // キャンバスの幅 W=1に対して・・・
    double ww = wo - wi;
    double d2 = wi + ww / 2.0;
    houseRing.setPaintCondition(HouseRing.SEPARATOR|HouseRing.INNER_ARC);
    houseRing.setFactor(g,cx,cy,W,asc,wi,wo,cusps);
    markerNeedle2.setExtension(d2 - 0.05);

    gauge2.setFactor(g,cx,cy,W,asc,wi);
    planetNeedle.setFactor(g,cx,cy,W,asc,d2,0.03,planetList,selectedAspect);
    markerNeedle.setFactor(g,cx,cy,W,asc,wo,planetList); //外のゲージ
    markerNeedle2.setFactor(g,cx,cy,W,asc,wi,planetList);//内のゲージ
    planetAngleNumberNeedle.setFactor(g,cx,cy,W,asc,d2 + 0.07,0.015,planetsAngle,planetsAngleString);
    planetAngleNumberNeedle.setVOffset(0.0085);
    houseRing.draw();
    markerNeedle2.draw();
    planetNeedle.draw();
    markerNeedle.draw();
    planetAngleNumberNeedle.draw();
    gauge2.draw();
  }
}
