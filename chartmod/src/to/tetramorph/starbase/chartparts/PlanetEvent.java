/*
 * PlanetEvent.java
 *
 * Created on 2007/06/06, 18:43
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.event.MouseEvent;
import to.tetramorph.starbase.lib.Body;

/**
 * ホロスコープ円上の天体のイベント情報。
 * @author 大澤義鷹
 */
public class PlanetEvent {
  Body body;
  MouseEvent mouseEvent;
  Object source;
  double draggedAngle;
  /**  PlanetEvent オブジェクトを作成する */
  public PlanetEvent(Body body,MouseEvent mouseEvent,Object source) {
    this.body = body;
    this.mouseEvent = mouseEvent;
    this.source = source;
  }
  public PlanetEvent(Body body,MouseEvent mouseEvent,Object source,double draggedAngle) {
    this.body = body;
    this.mouseEvent = mouseEvent;
    this.source = source;
    this.draggedAngle = draggedAngle;
  }
  
  public Body getBody() {
    return body;
  }
  public MouseEvent getMouseEvent() {
    return mouseEvent;
  }
  public Object getSource() {
    return source;
  }
  public double getDraggedAngle() {
    return draggedAngle;
  }
}
