/*
 * AspectStyle.java
 *
 * Created on 2007/10/21, 20:02
 *
 */

package to.tetramorph.starbase.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * アスペクト線の線種(アスペクトID、タイトorルーズ、色、線のストローク)を表現
 * するクラス。
 * @see to.tetramorph.starbase.widget.AspectsPanel アスペクト設定パネル
 * @see to.tetramorph.starbase.chartparts.AspectCircle アスペクト円
 * @author 大澤義鷹
 */
public class AspectStyle {
  /** 実線 */
  public static final int SOLID_LINE = 0;
  /** 点線 */
  public static final int DOT_LINE = 1;
  /** 一点鎖線 */
  public static final int DASH_LINE = 2;
  /** 二点鎖線 */
  public static final int DOUBLE_DASH_LINE = 3;
  /** 破線 */
  public static final int LONG_DASH_LINE = 4;
  /** 太実線 */
  public static final int BOLD_LINE = 5;

  private static final int CAP_BUTT = BasicStroke.CAP_BUTT;
  private static final int JOIN_ROUND = BasicStroke.JOIN_ROUND;
  
  /**
   * SOLID_LINE,DOT_LINE,DASH_LINE,DOUBLE_DASH_LINE,LONG_DASH_LINE,BOLD_LINEの順に
   * Strokeが格納されている配列。今のところ線種は固定されていて、カスタマイズは
   * できないことにしている。線種情報をプロパティで書き出すのがわずらわしいため。
   */
  public static final Stroke [] strokes = {
    new BasicStroke(1.0f),                             //実線
    new BasicStroke(1.0f,CAP_BUTT,JOIN_ROUND,1.0f,
      new float[] { 3f,3f,3f,3f, },0f),               //点線
    new BasicStroke(1.0f,CAP_BUTT,JOIN_ROUND,
      1.0f,new float[] { 10f,3f,2f,3f },0f),          //一点鎖線
    new BasicStroke(1.0f,CAP_BUTT,JOIN_ROUND,
      1.0f,new float[] { 10f,2f,2f,1f,2f,2f },0f),    //二点鎖線
    new BasicStroke(1.0f,CAP_BUTT,JOIN_ROUND,1.0f,
      new float[] { 12f,3f,12f,3f },0f),              //破線
    new BasicStroke(2.0f)                              //太線
  };
  
  
  private int aid;           //アスペクトID
  private boolean isTight;  //タイト|ルーズ
  private Color color;       //色
  private int strokeCode;   //ストローク番号

  /**
   * aidとisTightからAspectStyle オブジェクトを作成する。
   * 線色は白、線種は実線がデフォルト。
   * @param aid アスペクトID
   * @param isTight タイトアスペクトならtrue、ルーズならfalseを指定。
   * @exception IllegalArgumentException colorがnull。ありえないaidやstrokeCodeが
   * 指定された場合。
   */
  public AspectStyle( int aid, boolean isTight) {
    setColor(Color.BLACK);
    setStrokeCode( SOLID_LINE );
    setAspectID( aid );
    setTight( isTight );
  }
  
  /**  
   * AspectStyle オブジェクトを作成する。
   * @param aid アスペクトID
   * @param isTight タイトならtrue、ルーズならfalseを指定
   * @param color 色
   * @param strokeCode フィールド定数の中から指定。
   * @exception IllegalArgumentException colorがnull。ありえないaidやstrokeCodeが
   * 指定された場合。
   */
  public AspectStyle( int aid, boolean isTight, Color color, int strokeCode ) {
    setColor( color );
    setStrokeCode( strokeCode );
    setAspectID( aid );
    this.isTight = isTight;
  }
  
  /**  
   * AspectStyle オブジェクトを作成する。
   * @param aid アスペクトID
   * @param isTight タイトならtrue、ルーズならfalseを指定
   * @param r 赤の値を0-255までの値で指定
   * @param g 緑　　〃
   * @param b 青　　〃
   * @param a α　　〃
   * @param strokeCode フィールド定数の中から指定
   * @exception IllegalArgumentException colorがnull。ありえないaidやstrokeCodeが
   * 指定された場合。
   */  
  public AspectStyle( int aid, boolean isTight, int r, int g, int b, int a,
    int strokeCode ) {
    this( aid, isTight, new Color(r,g,b,a), strokeCode );
  }
  
  /**
   * ストロークを返す。
   */
  public Stroke getStroke() {
    return strokes[ strokeCode ];
  }
  
  /**
   * 色をセットする。
   * @exception IllegalArgumentException colorがnullのとき。
   */
  public void setColor(Color color) {
    if(color == null) throw 
      new IllegalArgumentException("colorのnullは禁止");
    this.color = color;
  }
  
  /** 
   * 色を返す。
   */
  public Color getColor() {
    return color;
  }
  
  /** 
   * ストローク番号をセットする。SOLID_LINEからBOLD_LINEまでのいずれか。
   * @exception IllegalArgumentException 範囲外の値が指定された。
   */
  public void setStrokeCode(int strokeCode) {
    if(strokeCode < 0 || strokeCode >= strokes.length)
      new IllegalArgumentException("strokeNumの値が範囲外");
    this.strokeCode = strokeCode;
  }
  
  /**
   * ストローク番号を返す。
   */
  public int getStrokeCode() {
    return strokeCode;
  }
  
  /**
   * アスペクトIDをセットする。
   * @param aid Const.CONJUNCTION〜Const.PARALLELまでのいずれか。
   * @exception IllegalArgumentException 存在しないaidが指定された。
   */
  public void setAspectID( int aid ) {
    if(aid < Const.CONJUNCTION || aid > Const.PARALLEL)
      throw new IllegalArgumentException("aidが範囲外");
    this.aid = aid;
  }
  public int getAspectID() {
    return aid;
  }
  /**
   * アスペクトのタイトorルーズを指定する。
   */
  public void setTight(boolean b) {
    this.isTight = b;
  }
  
  /**
   * アスペクトがタイトの場合はtrueを返す。
   */
  public boolean isTight() {
    return isTight;
  }
//  /**
//   * このオブジェクトにsの内容をコピーする。
//   */
//  public void copy(AspectStyle s) {
//    this.aid = s.aid;
//    this.isTight = s.isTight;
//    this.color = s.color;
//    this.strokeCode = s.strokeCode;
//  }

  private static final String [] lineNames = { "SOLID_LINE","DOT_LINE",
    "DASH_LINE","DOUBLE_DASH_LINE","LONG_DASH_LINE","BOLD_LINE" };

  /**
   * このオブジェクトの文字列表現を返す。
   * "トライン,tight,LONG_DASH_LINE,R=240,G=204,B=240,A=128"等。
   */
  public String toString() {
    StringBuilder sb = new StringBuilder(80);
    sb.append( Const.ASPECT_NAMES[ aid ] );
    sb.append(",");
    sb.append( isTight ? "tight" : "loose" );
    sb.append(",");
    sb.append( lineNames[ strokeCode ] );
    sb.append(",");
    sb.append(String.format("R=%d,G=%d,B=%d,A=%d",
      color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha()));
    return sb.toString();
  }

}
