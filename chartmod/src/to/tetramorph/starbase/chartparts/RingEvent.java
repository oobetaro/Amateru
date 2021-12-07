/*
 * RingEvent.java
 *
 * Created on 2007/07/07, 5:46
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.event.MouseEvent;

/**
 * ホロスコープの天体が配置されるリングのイベントリスナ。
 * @author 大澤義鷹
 */
public class RingEvent {
  Object source;
  MouseEvent mouseEvent;
  /**  RingEvent オブジェクトを作成する */
  public RingEvent(MouseEvent mouseEvent,Object source) {
    this.mouseEvent = mouseEvent;
    this.source = source;
  }
  public MouseEvent getMouseEvent() {
    return mouseEvent;
  }
  public Object getSource() {
    return source;
  }  
}
