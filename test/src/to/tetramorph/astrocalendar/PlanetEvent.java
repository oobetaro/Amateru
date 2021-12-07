
package to.tetramorph.astrocalendar;

import java.util.*;
/**
 * 月を除いた惑星のイングレスの時期や、逆行や順行に転じる時期を保管するクラス。
 */
class PlanetEvent {
  int sign;
  int state;
  int planet;
  Calendar date;
  /**
   * 日付、天体番号(SweConstに準じる)、
   * stateは0,1ならこのデータがイングレスの時期を表現していて、1なら逆行であることを表す。
   * stateが2,3ならこのデータが逆行順行の切替時期を表現していて、
   * 3なら逆行に2なら順行に転じた事を表す。
   * signはその時の星座サインを表す。
   *
   * このクラスはフィールド変数に直接アクセスして使う。
   */
  public PlanetEvent(Calendar date,int planet,int state,int sign) {
    this.date = date;
    this.state = state;
    this.sign = sign;
    this.planet = planet;
  }
}
