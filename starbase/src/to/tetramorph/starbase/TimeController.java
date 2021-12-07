/*
 * TimeController.java
 *
 * Created on 2006/11/28, 1:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * ChartDataPanelがTimePanelにアクセスするときに使用するインターフェイス。
 * @author 大澤義鷹
 */
interface TimeController {
  public void set();
  public ChartModulePanel getModule();
  public void setTransit(Transit transit);
}
