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
 * @author ���V�`��
 */
public class NatalChartColors {
  /** �T�C���w�i�F[0]�`[11] �f�t�H���g��Const.SIGN_COLORS */
  public Color [] signBackgrounds = Const.SIGN_COLORS;
  /** �T�C�������[0]�`[11] �f�t�H���g�͑S��Color.BLACK */
  public Color [] signSymbolBorders = { BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,
    BLACK,BLACK,BLACK,BLACK,BLACK,BLACK };
  /** �T�C�������F[0]�`[11] �f�t�H���g�͑S��Color.WHITE */
  public Color [] signSymbols = { WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,
    WHITE,WHITE,WHITE,WHITE,WHITE };
  /** �T�C����^�̘g���F �f�t�H���g��Color.BLACK */
  public Color signsBorder = Color.BLACK;
  /** �T�C���w�i�F�����Ȃ�true�B�f�t�H���g��false�B */
  public boolean isNoSignBackgrounds = false;
  /** �T�C���V���{���̉���薳���Ȃ�true�B�f�t�H���g��false�B */
  public boolean isNoSignSymbolBorders = false;
  /** �T�C���̐�^�̘g�������Ȃ�true�B�f�t�H���g��false�B */
  public boolean isNoSignBorders = false;
  /** �w�i�F */
  public Color background = Color.WHITE;
  /**
   * p�̒l�����̃I�u�W�F�N�g�̃t�B�[���h�ɃZ�b�g����B
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
   * ���̃I�u�W�F�N�g�̃t�B�[���h�̒l��p�ɃZ�b�g����B
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
