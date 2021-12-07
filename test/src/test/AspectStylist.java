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
 * 廃止予定。使用されていない。
 * アスペクト線の種類別にスタイルを保管していて、要求に応じてそれを教えてくれる。
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
   * スタイルの登録情報を全て削除する。
   */
  public void clear() {
    colorMap = new HashMap<Integer,Color>();
    strokeMap = new HashMap<Integer,Stroke>();    
  }
  /**
   * スタイルを登録する。
   * @param aid アスペクトID
   * @param accuracy タイトならture,ルーズならfalse
   * @param color 線色
   * @param stroke 線のスタイルをBasicStrokeで指定。
   */
  public void putStyle(int aid,boolean accuracy,Color color,Stroke stroke) {
    putStyle(new Aspect(aid,accuracy),color,stroke);
  }
  /**
   * スタイルを登録する。
   * @param aid アスペクトID
   * @param accuracy タイトならture,ルーズならfalse
   * @param r 線色 赤チャンネル
   * @param g 線色 青チャンネル
   * @param b 線色 緑チャンネル
   * @param a 線色 アルファチャンネル
   * @param stroke 線のスタイルをBasicStrokeで指定。
   */
  public void putStyle(int aid,boolean accuracy,int r,int g,int b,int a,Stroke stroke) {
    putStyle(new Aspect(aid,accuracy),new Color(r,g,b,a),stroke);
  }
  /**
   * スタイルを登録する。
   * @param a アスペクト (aid,tightのフィールドが判定対象)
   * @param color 線色
   * @param stroke 線のスタイルをBasicStrokeで指定。
   */
  public void putStyle(Aspect a,Color color,Stroke stroke) {
    colorMap.put(getKey(a),color);
    strokeMap.put(getKey(a),stroke);    
  }
  /**
   * アスペクトに対応するカラーを返す。
   * @param a a.aidとa.tightからアスペクトの種類が認識される。
   */
  public Color getColor(Aspect a) {
    return colorMap.get( getKey(a) );
  }
  /**
   * アスペクトに対応する線のストローク(線幅、スタイル(破線・実線等)を返す。
   * @param a a.aidとa.tightからアスペクトの種類が認識される。
   */
  public Stroke getStroke(Aspect a) {
    return strokeMap.get( getKey(a) );
  }
  //a.aidとa.tightからハッシュ用のキーを生成して返す。
  private Integer getKey(Aspect a) {
    //ﾀｲﾄなら正、ﾙｰｽﾞなら負にするのだが、aidはわから始まり0はかけても0なので
    //aid+1しておく。
    return new Integer((a.tight ? 1 : -1 ) * (a.aid + 1) );
  }
  //
  private void init() {
    BasicStroke solid = new BasicStroke(1.0f);
    BasicStroke dot = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1.0f,new float[] { 3f,3f,3f,3f, },0f);
    BasicStroke strong = new BasicStroke(2.0f);
    //タイト
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
    //ルーズ
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
