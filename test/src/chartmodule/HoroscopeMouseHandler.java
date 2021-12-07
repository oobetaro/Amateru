/*
 * HoroscopeMouseHandler.java
 *
 * Created on 2007/01/13, 18:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import to.tetramorph.starbase.widget.WordBalloonHandler;

/**
 * ホロスコープを表示するチャートモジュール内で使うマウスアダプター。
 * マウスイベントの処理をツールボタンで切り替えるために、
 * マウスモーションリスナでもありマウスリスナでもありワードバルーンハンドラ
 * でもあるクラスがあると都合がよかったのでこのクラスが作成された。
 * HoroscopeChartModulePanel等のマウスイベントは、このクラスを継承して作成
 * している。
 * @author 大澤義鷹
 */
public class HoroscopeMouseHandler extends MouseAdapter
  implements MouseMotionListener,WordBalloonHandler {
  public void mouseDragged(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }
  public Object getSelectedObject() { 
    return null;
  }
  public void setSelectedObject(Object o) {
    
  }
}

