/*
 * NameReporter.java
 *
 * Created on 2007/06/17, 8:18
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Font;
import to.tetramorph.starbase.lib.TimePlace;

/**
 * ホロスコープの名前、生年月日、出生地等の表示
 * @author 大澤義鷹
 */
public class NameReporter extends ChartParts {
  Font font = new Font("Monospaced",Font.PLAIN,10);
  float fontSize = 0;
  String name;
  TimePlace timePlace;
  /**  NameReporter オブジェクトを作成する */
  public NameReporter() {
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setTimePlace(TimePlace timePlace) {
    this.timePlace = timePlace;
  }
  public void draw() {
    float sz = (float)(bp.w * 0.04);
    if(sz != fontSize) {
      fontSize = sz;
      font = font.deriveFont(fontSize);
    }
    
  }
}
