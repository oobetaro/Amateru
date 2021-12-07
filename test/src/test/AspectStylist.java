/*
 * AspectStylist.java
 *
 * Created on 2006/11/11, 2:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * �p�~�\��B�g�p����Ă��Ȃ��B
 * �A�X�y�N�g���̎�ޕʂɃX�^�C����ۊǂ��Ă��āA�v���ɉ����Ă���������Ă����B
 */
public class AspectStylist {
  public static final boolean TIGHT = true;
  public static final boolean LOOSE = false;
  Map<Integer,Color> colorMap;
  Map<Integer,Stroke> strokeMap;
  /** 
   * Creates a new instance of AspectStylist 
   */
  public AspectStylist() {
    clear();
    init();
  }
  /**
   * �X�^�C���̓o�^����S�č폜����B
   */
  public void clear() {
    colorMap = new HashMap<Integer,Color>();
    strokeMap = new HashMap<Integer,Stroke>();    
  }
  /**
   * �X�^�C����o�^����B
   * @param aid �A�X�y�N�gID
   * @param accuracy �^�C�g�Ȃ�ture,���[�Y�Ȃ�false
   * @param color ���F
   * @param stroke ���̃X�^�C����BasicStroke�Ŏw��B
   */
  public void putStyle(int aid,boolean accuracy,Color color,Stroke stroke) {
    putStyle(new Aspect(aid,accuracy),color,stroke);
  }
  /**
   * �X�^�C����o�^����B
   * @param aid �A�X�y�N�gID
   * @param accuracy �^�C�g�Ȃ�ture,���[�Y�Ȃ�false
   * @param r ���F �ԃ`�����l��
   * @param g ���F �`�����l��
   * @param b ���F �΃`�����l��
   * @param a ���F �A���t�@�`�����l��
   * @param stroke ���̃X�^�C����BasicStroke�Ŏw��B
   */
  public void putStyle(int aid,boolean accuracy,int r,int g,int b,int a,Stroke stroke) {
    putStyle(new Aspect(aid,accuracy),new Color(r,g,b,a),stroke);
  }
  /**
   * �X�^�C����o�^����B
   * @param a �A�X�y�N�g (aid,tight�̃t�B�[���h������Ώ�)
   * @param color ���F
   * @param stroke ���̃X�^�C����BasicStroke�Ŏw��B
   */
  public void putStyle(Aspect a,Color color,Stroke stroke) {
    colorMap.put(getKey(a),color);
    strokeMap.put(getKey(a),stroke);    
  }
  /**
   * �A�X�y�N�g�ɑΉ�����J���[��Ԃ��B
   * @param a a.aid��a.tight����A�X�y�N�g�̎�ނ��F�������B
   */
  public Color getColor(Aspect a) {
    return colorMap.get( getKey(a) );
  }
  /**
   * �A�X�y�N�g�ɑΉ�������̃X�g���[�N(�����A�X�^�C��(�j���E������)��Ԃ��B
   * @param a a.aid��a.tight����A�X�y�N�g�̎�ނ��F�������B
   */
  public Stroke getStroke(Aspect a) {
    return strokeMap.get( getKey(a) );
  }
  //a.aid��a.tight����n�b�V���p�̃L�[�𐶐����ĕԂ��B
  private Integer getKey(Aspect a) {
    //��ĂȂ琳�Aٰ�ނȂ畉�ɂ���̂����Aaid�͂킩��n�܂�0�͂����Ă�0�Ȃ̂�
    //aid+1���Ă����B
    return new Integer((a.tight ? 1 : -1 ) * (a.aid + 1) );
  }
  //
  private void init() {
    BasicStroke solid = new BasicStroke(1.0f);
    BasicStroke dot = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1.0f,new float[] { 3f,3f,3f,3f, },0f);
    BasicStroke strong = new BasicStroke(2.0f);
    //�^�C�g
    putStyle(CONJUNCTION  ,TIGHT,255,  0,  0,240,solid);
    putStyle(SEXTILE      ,TIGHT,255,138, 21,240,solid);
    putStyle(SQUARE       ,TIGHT, 83, 83, 83,240,solid);
    putStyle(TRINE        ,TIGHT,255,138, 21,240,solid);
    putStyle(OPPOSITION   ,TIGHT,255,  0,  0,240,solid);
    putStyle(QUINCUNX     ,TIGHT,255,106,255,240,solid);
    putStyle(QUINTILE     ,TIGHT, 68,162,255,240,solid);
    putStyle(SEMI_SEXTILE ,TIGHT,255,106,255,240,solid);
    putStyle(SEMI_SQUARE  ,TIGHT,192,192,192,240,solid);
    putStyle(SESQIQUADRATE,TIGHT,192,192,192,240,solid);
    putStyle(BIQUINTILE   ,TIGHT, 68,162,255,240,solid);
    putStyle(DECILE       ,TIGHT,202,149,255,240,solid);
    putStyle(PARALLEL     ,TIGHT, 64,128,128,240,solid);
    //���[�Y
    putStyle(CONJUNCTION  ,LOOSE,255,  0,  0,240,dot);
    putStyle(SEXTILE      ,LOOSE,255,138, 21,240,dot);
    putStyle(SQUARE       ,LOOSE, 83, 83, 83,240,dot);
    putStyle(TRINE        ,LOOSE,255,138, 21,240,dot);
    putStyle(OPPOSITION   ,LOOSE,255,  0,  0,240,dot);
    putStyle(QUINCUNX     ,LOOSE,255,106,255,240,dot);
    putStyle(QUINTILE     ,LOOSE, 68,162,255,240,dot);
    putStyle(SEMI_SEXTILE ,LOOSE,255,106,255,240,dot);
    putStyle(SEMI_SQUARE  ,LOOSE,192,192,192,240,dot);
    putStyle(SESQIQUADRATE,LOOSE,192,192,192,240,dot);
    putStyle(BIQUINTILE   ,LOOSE, 68,162,255,240,dot);
    putStyle(DECILE       ,LOOSE,202,149,255,240,dot);
    putStyle(PARALLEL     ,LOOSE, 64,128,128,240,dot);
  }
}
