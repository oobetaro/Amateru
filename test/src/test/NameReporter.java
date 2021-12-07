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
 * �z���X�R�[�v�̖��O�A���N�����A�o���n���̕\��
 * @author ���V�`��
 */
public class NameReporter extends ChartParts {
  Font font = new Font("Monospaced",Font.PLAIN,10);
  float fontSize = 0;
  String name;
  TimePlace timePlace;
  /**  NameReporter �I�u�W�F�N�g���쐬���� */
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
