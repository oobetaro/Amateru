/*
 * HouseRing.java
 *
 * Created on 2006/10/28, 18:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.BasicStroke;
import java.awt.Color;
import static java.lang.Math.*;
import to.tetramorph.starbase.chartparts.Sector;

/**
 * ホロスコープの輪を表現するクラス。ハウスやサインの扇形で組まれた車輪を描く。
 * 数ある部品の中でこれは一番根っこの部分。
 * @author 大澤義鷹
 */
public class HouseRing extends ChartParts {
  public static final int DRAW_OUTER_ARC = 1;
  public static final int DRAW_INNER_ARC = 2;
  public static final int DRAW_CUSP = 4;
  public static final int DRAW_FILL = 8;
  private int condition = DRAW_OUTER_ARC | DRAW_INNER_ARC | DRAW_CUSP;
  private static final double [] ZODIAC_ANGLES =
    { 0d,30d,60d,90d,120d,150d,180d,210d,240d,270d,300d,330d };
  private double [] cusps = ZODIAC_ANGLES;
  private Color [] bgColors;
  private Color innerLineColor = Color.BLACK;
  private Color outerLineColor = Color.BLACK;;
  private Color cuspLineColor = Color.BLACK;;
  private boolean isNoBorder = false;
  private boolean isNoBackground = false;
  private double outerDiameter;
  private double ringWidth;
  private Sector [] array;
  static BasicStroke stroke = new BasicStroke();
  private Color highLightHouseColor = new Color(255,240,240);
  private int highLightHouse = -1;
  /**
   * ハイライトされているセクター番号を返す。負数なら非ハイライト状態。
   */
  public int getHighLightHouse() {
    return highLightHouse;
  }
  /**
   * 指定のハウスセクターをハイライト表示する。
   */
  public void setHighLightHouse(int hn) {
    highLightHouse = hn;
  }
  /**
   * ハウスセクターのハイライトカラーをセットする。
   */
  public void setHighLightHouseColor(Color color) {
    if(color == null) throw new java.lang.IllegalArgumentException("null禁止");
    highLightHouseColor = color;
  }
  public Color getHighLightHouseColor() {
    return highLightHouseColor;
  }
  public void setBGColors(Color [] colors) {
    bgColors = colors;
  }
  public Color [] getBGColors() {
    return bgColors;
  }
  public void setInnerLineColor(Color color) {
    innerLineColor = color;
  }
  public Color getInnerLineColor() {
    return innerLineColor;
  }
  public void setOuterLineColor(Color color) {
    outerLineColor = color;
  }
  public Color getOuterLineColor() {
    return outerLineColor;
  }
  public void setCuspColor(Color color) {
    cuspLineColor = color;
  }
  public Color getCuspColor() {
    return cuspLineColor;
  }
  public double getRingWidth() {
    return ringWidth;
  }
  public void setNoBorder(boolean b) {
    isNoBorder = b;
  }
  public boolean isNoBorder() {
    return isNoBorder;
  }
  public void setNoBackground(boolean b) {
    isNoBackground = b;
  }
  public boolean isNoBackground() {
    return isNoBackground;
  }
  public HouseRing() {
    
  }
  /** 
   * リングが未設定の空のオブジェクトを作成する。
   */
  public HouseRing( BasePosition bp ) {
    super(bp);
  }
  /**
   * ホロスコープ用の分割されたリングのパラメターをセットする
   * @param cusps カスプのリスト。nullを指定すると0,30,60..というように12星座の
   * 度数リストを指定したことになる。デフォルトはnullをセットしたのと同じ。
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps == null ? ZODIAC_ANGLES : cusps;
  }
  public double [] getCusps() {
    return cusps;
  }
  /**
   * 塗りつぶしの有無、外円弧の描画の有無、内円弧の描画の有無、分割線の有無を
   * セットする。DRAW_OUTER_ARC,DRAW_INNER_ARC,DRAW_SEPARATER,DRAW_FILLの定数を
   * ORで結合して指定する。
   */
  public void setPaintFormula(int condition) {
    this.condition = condition;
  }
  public int getPaintFormula() {
    return condition;
  }
//  /**
//   * ハウス(または獣帯)円の幅をセットする。
//   * 内径はsetDiameter()を使用する。
//   */
//  public void setDiameter(double diameter,double ringWidth) {
//    this.ringWidth = ringWidth;
//    super.setDiameter(diameter);
//  }
  public void setRingWidth(double ringWidth) {
    this.ringWidth = ringWidth;
  }
  //カスプ配列の値をキャッシュと比較して違うときはfalseを返す。
  private boolean isEquals() {
    for(int i=0; i<cusps.length; i++) {
      if(cusps[i] != ca_cusps[i]) return false;
    }
    return true;
  }
  private double ca_diameter,ca_outerDiameter,ca_x,ca_y,ca_w,ca_roll,ca_ascendant,ca_diameterOffset;
  private double [] ca_cusps = new double[16]; //少し多めにとっておく。
  private boolean isHighLight = false;
  /**
   * ハイライトモードをセットする。trueをセットすると、リング線全体が赤点灯する。
   */
  public void setHighLight(boolean b) {
    isHighLight = b;
  }
  public boolean isHighLight() {
    return isHighLight;
  }
  /**
   * このオブジェクトにセットされているパラメターに従ってハウスリングを描画する。
   */
  public void draw() {
    this.outerDiameter = diameterOffset + diameter + ringWidth * 2;
    // キャッシュしてパラメターに変更があったときだけSectorを生成する。
    if(ca_diameter != diameter || ca_outerDiameter != outerDiameter || ca_ascendant != ascendant ||
      ca_x != bp.x || ca_y != bp.y || ca_w != bp.w || ca_roll != roll || (!isEquals()) || ca_diameterOffset != diameterOffset) {
      ca_x = bp.x; ca_y = bp.y; ca_w = bp.w; ca_roll = roll;
      ca_diameter = diameter; ca_outerDiameter = outerDiameter;
      ca_ascendant = ascendant; ca_diameterOffset = diameterOffset;
      System.arraycopy(cusps,0,ca_cusps,0,cusps.length);
      array = Sector.getRing(bp.x,bp.y,bp.w,diameter+diameterOffset,outerDiameter,cusps,roll + ascendant);
    }
    bp.g.setStroke(stroke);
    if((condition & DRAW_FILL) != 0 && (! isNoBackground )) {
      for(int i=0; i<array.length; i++) {
        if(bgColors != null) {
          int c = i % bgColors.length;
          if(bgColors[c] != null) {
            bp.g.setPaint(bgColors[c]);
            bp.g.fill( array[i].sector );
          }
        }
      }
    }
    if(highLightHouse >= 0) {
      bp.g.setPaint(highLightHouseColor);
      bp.g.fill( array[ highLightHouse ].sector);
    }
    if(isHighLight) { //ハイライトのときは全体を赤点灯して終了
      bp.g.setPaint(Color.RED);
      for(Sector s : array)
        bp.g.draw(s.sector);
      return;
    } 
    if(isNoBorder) return;
    bp.g.setPaint(Color.BLACK);
    for(int i=0; i<array.length; i++) {
      if((condition & DRAW_OUTER_ARC) != 0) {
        bp.g.setPaint(outerLineColor);
        bp.g.draw(array[i].outer);
      }
      if((condition & DRAW_INNER_ARC) != 0) {
        bp.g.setPaint(innerLineColor);
        bp.g.draw( array[i].inner );
      }
      if((condition & DRAW_CUSP) != 0) {
        bp.g.setPaint(cuspLineColor);
        bp.g.draw( array[i].line1 );
      }
    }
  }
  /**
   * セクター分けされたリングの配列を返す。
   */
  public Sector [] getSectors() {
    return array;
  }
}
