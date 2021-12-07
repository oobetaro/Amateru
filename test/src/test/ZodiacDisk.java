/*
 * ZodiacDisk.java
 *
 * Created on 2007/05/05, 3:29
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;
import to.tetramorph.starbase.chartplate.DialGauge;
import to.tetramorph.starbase.chartplate.HouseNumberDial;
import to.tetramorph.starbase.chartplate.HouseRing;
import to.tetramorph.starbase.chartplate.NumberNeedle;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
import to.tetramorph.starbase.chartplate.SignDial;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChartFactor;

/**
 * 獣帯リングとそれについているゲージを描く
 * また獣帯上にかぶるカスプ線や獣帯リングの外に配置されるAC,MC,DC,ICの感受点表示機能もある。
 * @author 大澤義鷹
 */
public class ZodiacDisk implements HoroscopeDisk {
  double cx,cy,W;
  Graphics2D g;
  double asc;
  
  public HouseRing zodiacRing = new HouseRing();
  public DialGauge gauge = new DialGauge(); //獣帯リングのゲージ
  public SignDial signDial = new SignDial(); //獣帯リング
  public HouseRing houseRing2 = new HouseRing(); //ハウスリングをカスプ線として使う
  public NumberNeedle cuspAngleNumberNeedle = new NumberNeedle(); //カスプ番号針
  public HouseNumberDial houseNumberDial = new HouseNumberDial(); //ハウス番号ダイアル
  public PlanetNeedle specialPlanetNeedle = new PlanetNeedle(); //AC,MC,DC,IC用の針
  Component component;
  double [] cusps;     //カスプの黄経座標
  String [] cuspAngles; //カスプ度数の文字列表現
  List<Body> acmcList;  //AC,MC,IC,DCを保管用
  Aspect selectedAspect;
    
  /**  ZodiacDisk オブジェクトを作成する */
  public ZodiacDisk(Component c) {
    this.component = c;
  }
  /** 描画するグラフィックスオブジェクトをセットする */
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }  
  /** 
   * 描画する基準とする座標をセットする。
   * @param cx 中心座標x
   * @param cy 中心座標y
   * @param W 基準となる描画エリアの幅(つねに正方形とみなす)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }  
  /**
   * アセンダント点を設定する。
   */
  public void setAC(double ac) {
    this.asc = ac;
  }
  /**
   * 指定されたアスペクトを持つ天体を赤点灯する。
   */
  public void setSelectedAspect(Aspect aspect) {
    this.selectedAspect = aspect;
  }
  /**
   * AC,MC,DC,ICの感受点のリストをセットする。
   */
  public void setACList(List<Body> acmcList) {
    this.acmcList = acmcList;
  }  
  /**
   * ハウスカスプの黄道座標を配列で指定する。[0～11]まで。
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps;
    cuspAngles = ChartFactor.formatSignAngles(cusps,0); //表示用カスプ度数のリスト
  }
  double wi,wo;
  public void setSize(double wi,double wo) {
    this.wi = wi;
    this.wo = wo;
  }
  /**
   * 描画する
   */
  public void draw() {
    // キャンバスの幅 W=1に対して・・・
    double ww = wo - wi;
    zodiacRing.setFactor(g,cx,cy,W,asc,wi,wo,null);
    signDial.setFactor(g,cx,cy,W,asc,wo-ww/2d,0.03f);
    gauge.setFactor(g,cx,cy,W,asc,wi);
    houseRing2.setPaintCondition(HouseRing.SEPARATOR);
    houseRing2.setFactor(g,cx,cy,W,asc,wi,wo+0.05,cusps);
    cuspAngleNumberNeedle.setFactor(g,cx,cy,W,asc,wo+0.01,0.015,cusps,cuspAngles);
    specialPlanetNeedle.setFactor(g,cx,cy,W,asc,wo+0.07,0.04,acmcList,selectedAspect);
    cuspAngleNumberNeedle.setVOffset(0.018);
    cuspAngleNumberNeedle.setAlign(NumberNeedle.OUTER);
    houseNumberDial.setFactor(g,cx,cy,W,asc,wo+0.03,0.02,cusps);
    //獣帯リングとその目盛り
    zodiacRing.draw();
    gauge.draw();
    signDial.draw();  
    //カスプやAC,MC等の特例オブジェクトの表示
    houseRing2.draw();
    cuspAngleNumberNeedle.draw();
    houseNumberDial.draw();
    specialPlanetNeedle.draw();
  }

}
