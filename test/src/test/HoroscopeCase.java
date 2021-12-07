/*
 * HoroscopeCase.java
 *
 * Created on 2007/05/07, 6:57
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.chartplate.AspectCircle;

/**
 * 1重円から多重円を表示するための部品。HouseDisk,ZodiacDiscを複数追加することで
 * 多重円の描画ができる。
 * @author 大澤義鷹
 */
public class HoroscopeCase {
  AspectCircle aspectCircle;
  List<HoroscopeDisk> horoList = new ArrayList<HoroscopeDisk>();
  double cx,cy,maxdia;
  double dia;
  Graphics2D g;
  // 各円の幅(内→外の順で宣言)
  double [][] diskSizes  = { 
    { 0.85,0.75 }, //円が一つ 
    { 0.85,0.75,0.57 }, //円が二つ ネイタル1重円はここから
    { 0.85,0.75,0.6,0.45 },
    { 0.85,0.75,0.61,0.47,0.33 }
  };

  /**  HoroscopeCase オブジェクトを作成する */
  public HoroscopeCase() {
  }
  /**
   * 描画位置と最大直径を指定する。
   */
  public void setPos(double cx,double cy,double maxdia) {
    this.cx = cx;
    this.cy = cy;
    this.maxdia = maxdia; //フレームに密接する円の直径
  }
  /** 
   * 描画するグラフィックスオブジェクトをセットする 
   */
  public void setGraphiecs2D(Graphics2D g) {
    this.g = g;
  }
  /**
   * ホロスコープの一番外側の円(たいがいは黄道リング)の直径をセットする。
   * setPos()のmaxdiaを1としてその比率で指定する。かならず1以下であること。
   */
  public void setDiameter(double dia) {
    this.dia = dia;
  }
  /**
   * 1重円,2重円,3重円,,,と円の数に応じての、それぞれの大きさを指定する。
   * setDiameterの値を1として、その比率で幅が決定し、その幅の総和の残りが、
   * アスペクト円のサイズとなる。
   */
  public void setDiskSizes(double [][] diskSizes) {
    this.diskSizes = diskSizes;
  }
  /**
   * このケースにホロスコープディスクを追加する。
   */
  public void addHoroscopeDisk(HoroscopeDisk disk) {
    horoList.add(disk);
  }
  /**
   * このケースからホロスコープディスクを削除する
   */
  public boolean remveHoroscopeDisk(HoroscopeDisk disk) {
    return horoList.remove(disk);
  }
  /**
   * 指定されたホロスコープの位置を交換する。
   */
  public void swapHoroscopeDisk(HoroscopeDisk disk1,HoroscopeDisk disk2) {
    
  }
  /**
   * アスペクト円をこのケースにセットする
   */
  public void setAspectCircle(AspectCircle aspectCircle) {
    this.aspectCircle = aspectCircle;
  }
  /**
   * このケース内のアスペクト円を削除する
   */
  public void removeAspectCircle() {
    this.aspectCircle = null;
  }
  /**
   * ホロスコープを描画する。
   */
  public void draw() {
    double [] ws = diskSizes[ horoList.size() - 1];
    for(int i=0; i<horoList.size(); i++) {
      HoroscopeDisk disk = horoList.get(i);
      disk.setGraphics2D(g);
      disk.setPos(cx,cy,maxdia);
      disk.setSize(ws[i+1],ws[i]);
      disk.draw();
    }
//    if(aspectCircle != null) {
//      aspectCircle.setGraphics2D(g);
//      aspectCircle.setPos(cx,cy,dia);
//      aspectCircle.setSize(ws[0]);
//      aspectCircle.draw();
//    }
  }
}
