/*
 * ReportEvent.java
 *
 * Created on 2007/10/05, 9:31
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.event.MouseEvent;

/**
 * GComponentのイベント情報クラス。
 * @author 大澤義鷹
 */
public class GComponentEvent {
  GComponent component;
  MouseEvent mouseEvent;
  Object source;
  
  /**  GComponentEvent オブジェクトを作成する */
  public GComponentEvent() {
  }
  /**  
   * AspectCircleEvent オブジェクトを作成する 
   * @param component イベントの発生したGComponentオブジェクト
   * @param mouseEvent マウスイベント
   * @param source イベントの起きたオブジェクトを返す。
   */
  public GComponentEvent(GComponent component,MouseEvent mouseEvent,Object source) {
    this.component = component;
    this.mouseEvent = mouseEvent;
    this.source = source;
  }
  /**
   * マウスイベントの起きたGComponentオブジェクトを返す。
   */
  public GComponent getGComponent() {
    return component;
  }
  /**
   * マウスイベント情報を返す。
   */
  public MouseEvent getMouseEvent() {
    return mouseEvent;
  }
  /**
   * イベントの起きたオブジェクトを返す。
   */
  public Object getSource() {
    return source;
  }
  
}
