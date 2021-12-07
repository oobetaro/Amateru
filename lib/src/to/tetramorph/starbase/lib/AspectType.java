/*
 * AspectType.java
 *
 * Created on 2006/08/16, 22:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import to.tetramorph.starbase.*;

/**
 * アスペクトの種類を表現するクラス
 */
public class AspectType {
  /** アスペクトID */
  public int aid;
  /** タイトオーブの角度 */
  public double tightOrb;
  /** ルーズオーブの角度 */
  public double looseOrb;
  /**
   * aidにアスペクト番号、tightOrbにタイトオーブの角度、looseOrbにルーズオーブの
   * 角度を指定する。
   * @param aid アスペクト種別番号
   * @param tightOrb タイトオーブの許容角度
   * @param looseOrb ルーズオーブの許容角度
   */
  public AspectType(int aid,double tightOrb,double looseOrb) {
    this.aid = aid;
    this.tightOrb = tightOrb;
    this.looseOrb = looseOrb;
  }
  /**
   * このオブジェクトの文字列表現を返す。
   */
  public String toString() {
    return "aid="+aid+",tightOrb="+tightOrb+",looseOrb="+looseOrb;
  }
}
