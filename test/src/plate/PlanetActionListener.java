/*
 * PlanetActionListener.java
 *
 * Created on 2007/05/05, 4:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import to.tetramorph.starbase.lib.Body;

/**
 *
 * @author ���V�`��
 */
public interface PlanetActionListener {
  /**
   * PlanetNeedle�̓V�̂��N���b�N���ꂽ���Ƃ�m�点��B
   */
  public void planetClicked(Body b);
  /**
   * PlanetNeedle�̓V�̂��_�u���N���b�N���ꂽ���Ƃ�m�点��B
   */
  public void planetDoubleClicked(Body b);
  /**
   * PlanetNeedle�̓V�̂��h���b�O�ňړ����ꂽ���Ƃ�m�点��B
   */
  //public void planetDragged(Body b);
}
