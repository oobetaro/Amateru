/*
 *
 */
package to.tetramorph.starbase.util;
import to.tetramorph.starbase.*;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Body;
/**
 * 太陽から冥王星までの天体のディグニティ(品位)を求めるメソッド群。
 * 冥王星より大きな天体番号が引数のBodyにセットされていると、
 * ArrayOutOfBoundExceptionが出る。
 */
public class Dignity {
  private static final int [] DIGNITY1   = { LEO,CAN,GEM,TAU,ARI,SAG,CAP,AQU,PIS,SCO };
  private static final int [] DIGNITY2   = { LEO,CAN,VIR,LIB,SCO,PIS,AQU,AQU,PIS,SCO };
  private static final int [] EXALT      = { ARI,TAU,VIR,PIS,CAP,CAN,LIB,SCO,AQU,LEO};
  private static final int [] DETRIMENT1 = { AQU,CAP,SAG,ARI,TAU,GEM,CAN,LEO,VIR,TAU};
  private static final int [] DETRIMENT2 = { AQU,CAP,PIS,SCO,LIB,VIR,LEO,LEO,VIR,TAU};
  private static final int [] FALL       = { LIB,SCO,PIS,VIR,CAN,CAP,ARI,TAU,LEO,AQU};
  /**
   * 天体がディグニティ(品位がある)のサインならtrueを返す。
   * つまりこれは支配星ならtrueということと同じ。
   */
  public static boolean isDignity(Body p) {
    return (p.getSign() == DIGNITY1[p.id] || p.getSign() == DIGNITY2[p.id]);
  }
  /**
   * 天体がイグザルト(高揚)のサインにいるならtrueを返す。
   */
  public static boolean isExalt(Body p) {
    return (p.getSign() == EXALT[p.id]);
  }
  /**
   * 天体がデトリメント(損傷)のサインにいるならtrueを返す。
   */
  public static boolean isDetriment(Body p) {
    return (p.getSign() == DETRIMENT1[p.id] || p.getSign() == DETRIMENT2[p.id]);
  }
  /**
   * 天体がフォール(減退)のサインにいるならtrueを返す。
   */
  public static boolean isFall(Body p) {
    return (p.getSign() == FALL[p.id]);
  }
}
