/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/28 04:47
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JEditorPaneなどに登録して、画面上のマウスポイントを取得できるハンドラ。
 * リポートやアラビックパーツプラグインで使用している。
 * @author 大澤義鷹
 */
public class GetPointMouseHandler extends MouseAdapter {
    Point point = new Point(0, 0);
    @Override
    public void mousePressed(MouseEvent e) {
        point = e.getPoint();
    }
    public Point getPoint() {
        return point;
    }
}
