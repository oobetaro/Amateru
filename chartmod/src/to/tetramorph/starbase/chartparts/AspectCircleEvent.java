/*
 * AspectCircleEvent.java
 *
 * Created on 2007/08/23, 10:14
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.event.MouseEvent;
import to.tetramorph.starbase.lib.Aspect;

/**
 * AspectCircleで発生するイベント情報クラス。
 * @author 大澤義鷹
 */
public class AspectCircleEvent {
  Aspect aspect;
  MouseEvent mouseEvent;
  Object source;
  /**  
   * AspectCircleEvent オブジェクトを作成する 
   * @param aspect アスペクトオブジェクト
   * @param mouseEvent マウスイベント
   * @param source イベントの起きたオブジェクトを返す。
   */
  public AspectCircleEvent(Aspect aspect,MouseEvent mouseEvent,Object source) {
    this.aspect = aspect;
    this.mouseEvent = mouseEvent;
    this.source = source;
  }
  /**
   * マウスイベントの起きたアスペクトを返す。
   */
  public Aspect getAspect() {
    return aspect;
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
