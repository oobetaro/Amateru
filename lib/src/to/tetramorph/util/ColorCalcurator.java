/*
 * ColorCalcurator.java
 */
package to.tetramorph.util;

import java.awt.Color;
import to.tetramorph.util.*;
/**
 * 色の混ぜ合わせ計算をしたり、特有のカラーパターンを求めるのに使う
 * staticメソッド群。
 */
public class ColorCalcurator {
  private ColorCalcurator() { }
  public static Color darker(Color color,float par) {
    int r = (int)(color.getRed() * par) % 256;
    int g = (int)(color.getGreen() * par) % 256;
    int b = (int)(color.getBlue() * par) % 256;
    return new Color(r,g,b);
  }
  /**
   * 二つの色を合成した新しいカラーオブジェクトを作成して返す。
   */
  public static Color addColor(Color color1,Color color2) {
    int r = (color1.getRed() + color2.getRed()) / 2;
    int g = (color1.getGreen() + color2.getGreen()) / 2;
    int b = (color1.getBlue() + color2.getBlue()) / 2;
    int a = (color1.getAlpha() + color2.getAlpha())/ 2;
    return new Color(r,g,b,a);    
  }
  /**
   * 色のR,G,Bチャンネル個別に色を加算(または減算)する。
   * 負数を指定すると減算できる。加算して255以上になるときは255。
   * 減算して0以下になるときは0。
   */
  public static Color addColor(Color color,int r,int g,int b) {
    int r1,g1,b1;
    if(r >= 0)
      r1 = (color.getRed() + r) < 256 ? color.getRed()+r : 255;
    else
      r1 = (color.getRed() + r) >= 0 ? color.getRed()+r : 0;
    if(g >= 0)
      g1 = (color.getGreen() + g) < 256 ? color.getGreen() + g : 255;
    else
      g1 = (color.getGreen() + g) >= 0 ? color.getGreen() + g : 0;
    if(b >= 0)
      b1 = (color.getBlue() + b) < 256 ? color.getBlue() + b : 255;
    else
      b1 = (color.getBlue() + b) >= 0 ? color.getBlue() + b : 0;
    return new Color(r1,g1,b1);
  }
  /**
   * 4色の色を混合して、12色の円環状の色のグラデーションにして返す。
   * 配列c[]は要素がかならず12であること。
   * 初期値として4色の色を与えなければならず、c[0],c[3],c[6],c[9]に与える。
   * たとえばc[0]=Color.RED,c[3]=Color.YELLOW,c[6]=Color.GREEN,c[9]=Color.BLUE
   * とすると、虹のグラデーションができる。
   * 配列のその他の要素はnullで良く、なにか入っていても新しいオブジェクトに置き換わる。
   * 戻り値は引数の参照と同じものがもどる。つまりc[]は直接書きかわる。
   */
  public static Color [] getColorRing(Color [] c) {
    //c[0] = RED;
    c[1] = addColor(addColor(c[0],c[3]),c[0]);
    c[2] = addColor(addColor(c[0],c[3]),c[3]);
    //c[3] = YELLOW;
    c[4] = addColor(addColor(c[3],c[6]),c[3]);
    c[5] = addColor(addColor(c[3],c[6]),c[6]);
    //c[6] = GREEN;
    c[7] = addColor(addColor(c[6],c[9]),c[6]);
    c[8] = addColor(addColor(c[6],c[9]),c[9]);
    //c[9] = BLUE;
    c[10] = addColor(addColor(c[9],c[0]),c[9]);
    c[11] = addColor(addColor(c[9],c[0]),c[0]);
    return c;
  }
  /**
   * カラーテーブルを作成して返す。グレースケール9段階＋12色9段階のカラーテーブル
   * で、table[x][y]とすると、xは0〜12,yは0〜8の二次元配列でカラーテーブルを返す。
   * y軸は0が一番暗く8が一番明るい。
   */
  public static Color [][] getColorTable() {
    Color [][] table = new Color[13][9];
    Color [] gra = new Color[12];
    gra[0] = Color.CYAN;
    gra[2] = Color.BLUE;
    gra[4] = Color.MAGENTA;
    gra[6] = Color.RED;
    gra[8] = Color.YELLOW;
    gra[10] = Color.GREEN;
    gra[1] = ColorCalcurator.addColor(gra[0],gra[2]);
    gra[3] = ColorCalcurator.addColor(gra[2],gra[4]);
    gra[5] = ColorCalcurator.addColor(gra[4],gra[6]);
    gra[7] = ColorCalcurator.addColor(gra[6],gra[8]);
    gra[9] = ColorCalcurator.addColor(gra[8],gra[10]);
    gra[11] = ColorCalcurator.addColor(gra[10],gra[0]);
    Color c = new Color(0,0,0);
    for(int i=0; i<9; i++) {
      table[0][i] = c;
      c = ColorCalcurator.addColor(c,30,30,30);
    }
    int o = 48;
    for(int x=1; x<=12; x++) table[x][4] = gra[x-1];
    for(int y=3; y >= 0; y--) {
      for(int x=1; x<=12; x++)
        table[x][y] = ColorCalcurator.addColor(table[x][y+1],-o,-o,-o); 
    }
    for(int y=5; y <= 8; y++) {
      for(int x=1; x <= 12; x++)
        table[x][y] = ColorCalcurator.addColor(table[x][y-1],o,o,o);
    }
    return table;
  }
  /**
   * 補色を返す。
   */
  public static Color getComplementaryColor(Color color) {
    int r = 255 - color.getRed();
    int g = 255 - color.getGreen();
    int b = 255 - color.getBlue();
    return new Color(r,g,b);
  }
}
