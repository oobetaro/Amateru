/*
 * ZodiacActionListener.java
 *
 * Created on 2007/05/29, 21:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

/**
 *
 * @author 大澤義鷹
 */
public interface ZodiacActionListener {
  /**
   * マウスドラッグまたはマウスホイールで獣帯リングが回転されたときに呼び出される。
   * リングが回転中は連続的に呼び出される。
   * @param rollOffset 符号つきの回転角
   */
  public void ringRolled(double rollOffset);
}
