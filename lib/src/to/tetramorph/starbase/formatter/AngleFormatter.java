/*
 *
 */
package to.tetramorph.starbase.formatter;
import to.tetramorph.starbase.formatter.AbstractFormatter;
import java.text.ParseException;

/**
 * JFormattedTextFieldで、double型の角度入力に使うフォーマッタ。
 * 天体位置はしばしばサインと0〜29.999の数値で入力したりする。
 * 通常アスペクトは180度以上のアスペクトはありえない。
 * このフォーマッタは、正の値で上限値つきの角度入力を想定している。
 */
public class AngleFormatter extends AbstractFormatter {
  /**
   * コンストラクタの引数typeに指定する定数で、フィールドへの入力値が、
   * maxValue以下ならば入力を受付、ちがうときは""に初期化する指定。<br>
   * "0 >= 入力値 < maxValue"となる。
   */
  public static final int LESS_THAN = 0;
  /**
   * コンストラクタの引数typeに指定する定数で、フィールドへの入力値が、
   * maxValue以下か等しいならば入力を受付、ちがうときは""に初期化する指定。<br>
   * "0 >= 入力値 <= maxValueとなる。
   */
  public static final int LESS_THAN_OR_EQUAL = 1;
  
  private double maxValue;
  private int type;
  /**
   * 入力角度フォーマッターを作成する。maxValueは入力を受け付ける最大値で
   * 正の値であること。typeにLESS_THANを指定すると、フィールドへの入力値は
   * "0 >= value < maxValue"となる。
   * LESS_THAN_OR_EQUALを指定すると"0 >= value <= maxValue"となる。
   */
  public AngleFormatter(double maxValue,int type) {
    super();
    this.maxValue = maxValue;
    this.type = type;
  }
  /** これはJTextFormatterが呼び出すメソッド*/
  public Object stringToValue(String text) throws ParseException {
    text = zenkakuToANK(text);
    try {
      double v = Double.parseDouble(text);
      if(v >= 0d) {
        if(type == LESS_THAN) {
          if( v < maxValue) return new Double(v);
        } else if ( type == LESS_THAN_OR_EQUAL) {
          if( v <= maxValue) return new Double(v);
        }
      }
    }catch(NumberFormatException e) { };
    return new Double(0);
  }
  
  /** これはJTextFormatterが呼び出すメソッド*/
  public String valueToString(Object value) throws ParseException {
    if(value == null) return "0.0";
    return ((Double)value).toString();
  }
}

