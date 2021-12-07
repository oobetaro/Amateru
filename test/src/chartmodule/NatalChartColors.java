/*
 * NatalChartColors.java
 *
 * Created on 2007/02/20, 11:39
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import to.tetramorph.starbase.lib.Const;
import static java.awt.Color.*;
import to.tetramorph.util.Preference;
/**
 *
 * @author 大澤義鷹
 */
public class NatalChartColors {
  /** サイン背景色[0]～[11] デフォルトはConst.SIGN_COLORS */
  public Color [] signBackgrounds = Const.SIGN_COLORS;
  /** サイン縁取り[0]～[11] デフォルトは全部Color.BLACK */
  public Color [] signSymbolBorders = { BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,
    BLACK,BLACK,BLACK,BLACK,BLACK,BLACK };
  /** サイン文字色[0]～[11] デフォルトは全部Color.WHITE */
  public Color [] signSymbols = { WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,
    WHITE,WHITE,WHITE,WHITE,WHITE };
  /** サイン扇型の枠線色 デフォルトはColor.BLACK */
  public Color signsBorder = Color.BLACK;
  /** サイン背景色無しならtrue。デフォルトはfalse。 */
  public boolean isNoSignBackgrounds = false;
  /** サインシンボルの縁取り無しならtrue。デフォルトはfalse。 */
  public boolean isNoSignSymbolBorders = false;
  /** サインの扇型の枠線無しならtrue。デフォルトはfalse。 */
  public boolean isNoSignBorders = false;
  /** 背景色 */
  public Color background = Color.WHITE;
  /**
   * pの値をこのオブジェクトのフィールドにセットする。
   */
  public void setPreference(Preference p) {
    signBackgrounds = p.getColors("signBackgrounds",Const.SIGN_COLORS);
    signSymbolBorders = p.getColors("signSymbolBorders",signSymbolBorders);
    signSymbols = p.getColors("signSymbols",signSymbols);
    signsBorder = p.getColor("signsBorder",signsBorder);
    isNoSignBackgrounds = p.getBoolean("isNoSignBackgrounds",isNoSignBackgrounds);
    isNoSignSymbolBorders = p.getBoolean("isNoSignSymbolBorders",isNoSignSymbolBorders);
    isNoSignBorders = p.getBoolean("isNoSignBorders",isNoSignBorders);
    background = p.getColor("background",background);
  }
  /**
   * このオブジェクトのフィールドの値をpにセットする。
   */
  public void getPreference(Preference p) {
    p.setColors("signBackgrounds",signBackgrounds);
    p.setColors("signSymbolBorders",signSymbolBorders);
    p.setColors("signSymbols",signSymbols);
    p.setColor("signsBorder",signsBorder);
    p.setBoolean("isNoSignBackgrounds",isNoSignBackgrounds);
    p.setBoolean("isNoSignSymbolBorders",isNoSignSymbolBorders);
    p.setBoolean("isNoSignBorders",isNoSignBorders);
    p.setColor("background",background);
  }
}
