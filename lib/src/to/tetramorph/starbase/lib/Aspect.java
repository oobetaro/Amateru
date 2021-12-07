/*
 *
 */
package to.tetramorph.starbase.lib;

import to.tetramorph.starbase.*;

/**
 * 2体間のアスペクトを表現するためのクラス。
 * 二つの感受点(p1,p2)がどんなアスペクト(aid=アスペクト番号)を形成していて、
 * それはタイトかルーズなのかを表す。
 * このオブジェクトがなんのアスペクトなのかは、aidフィールドを参照する。
 * Const.ASPECT_NAMES[aspect.aid]などとすれば、アスペクト名もわかる。
 * このオブジェクトは、アスペクトの種類を表現していて、離角を保持するものではない。
 * アスペクトを形成する二つの天体と、そのアスペクトの種類(番号で表現)、そのアスペクト
 * がタイトかルーズか、アスペクトの誤差が何度あるかという情報から構成される。
 * ただしノーアスペクトのときは、一つの天体の情報(p1)のみ保存され、isNoAspectメソッド
 * でそれを判定することができる。
 */
public class Aspect {
  /** タイトアスペクトを表す定数で、コンストラクタのtigheパラメターに適用する */
  public static final boolean TIGHT = true;
  /** ルーズアスペクトを表す定数で、コンストラクタのtigheパラメターに適用する */
  public static final boolean LOOSE = false;
  /** 感受点1 */
  public Body p1;
  /** 感受点2 */
  public Body p2;
  /** タイトアスペクトならtrue。ルーズアスペクトならfalse */
  public boolean tight;
  /** アスペクトID Constクラス内で宣言されている */
  public int aid;
  /** アスペクトの誤差角度 */
  public double error;
  /**
   * aidとtightからオブジェクトを作成。p1,p2はnull。errorは0が設定される。
   */
  public Aspect(int aid,boolean tight) {
    this.aid = aid;
    this.tight = tight;    
  }
  /**
   *アスペクトオブジェクトを作成する。
   * @param aid Constで定義されているアスペクト番号
   * @param tight タイトアスペクトならtrueにセット
   * @param p1 感受点1
   * @param p2 感受点2
   * @param error アスペクトの誤差(120度のアスペクト(トライン)のところが118度で、
   * 許容オーブ内でアスペクト有りと判定された場合、errorの値は2度を設定する。)
   */
  public Aspect(Body p1,Body p2,int aid,boolean tight,double error) {
    this.aid = aid;
    this.p1 = p1;
    this.p2 = p2;
    this.tight = tight;
    this.error = error;
  }
  /**
   * このコンストラクタで作ったオブジェクトはノーアスペクトを表す。
   */
  public Aspect(Body p1) {
    this.p1 = p1;
  }
  /**
   * このオブジェクトがノーアスペクトを表しているときはtrueを返す。
   */
  public boolean isNoAspect() {
    return p2 == null;
  }
  /**
   * このアスペクトに指定された天体が含まれている場合はtrueを返す。
   * ただしこのアスペクトがノーアスペクトのときはかならずfalseを返す。
   * 含まれているかいないかの判定は(p1 == p || p2 == p)として判定される。
   * また上記の判定でfalseとなったあと、p1.isSame(p) || p2.isSame(p)で判定される。
   * つまり同じ参照アドレスなら一致。同じidとgroupに属する天体であれば一致とする。
   */
  public boolean contains(Body p) {
    if(isNoAspect()) return false;
    if(p1 == p || p2 == p) return true;
    return p1.isSame(p) || p2.isSame(p);
  }
  /**
   * 感受点pがこのアスペクトに含まれているとき、指定された感受点とは別のほうの
   * 感受点を返す。感受点pがこのアスペクトに含まれていないときはnullをかえす。
   * このオブジェクトがノーアスペクトの場合もnullを返す。引数pがnullのときもnullを返す。
   */
  public Body getOther(Body p) {
    if(p == null) return null;
    if(isNoAspect()) return null;
    if (p.id == p1.id && p.id != p2.id) return p2;
    if (p.id != p1.id && p.id == p2.id) return p1;
    return null;
  }
  /**
   * このオブジェクトが表しているアスペクトの文字列表現を返す。
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(p1.getName());
    sb.append(" ");
    sb.append(Const.ASPECT_NAMES[aid]);
    sb.append(" ");
    sb.append(p2.getName());
    sb.append(",");
    sb.append((int)error);
    return sb.toString();
  }
  /**
   * このオブジェクトが表しているアスペクトの説明文を返す。
   * "金星−土星 オポジション(180ﾟ) 誤差 4゜"等。
   */
  public String getCaption() {
    StringBuffer sb = new StringBuffer();
    sb.append(Const.PLANET_NAMES[p1.id]);
    sb.append("−");
    sb.append(Const.PLANET_NAMES[p2.id]);
    sb.append(" ");
    sb.append(Const.ASPECT_NAMES[aid]);
    sb.append("(");
    sb.append((int)Const.ASPECT_ANGLES[aid]);
    sb.append("ﾟ)");
    sb.append("誤差 ");
    sb.append((int)error);
    sb.append("ﾟ");
    return sb.toString();    
  }
}