/*
 * SingleSideBorder.java
 *
 * Created on 2007/11/12, 1:41
 *
 */
package to.tetramorph.starbase.widget;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.border.EtchedBorder;

/**
 * ボーダーは通常矩形を囲むように描画されるが、これは矩形の上の線だけを描画する。
 * @author 大澤義鷹
 */
public class SingleSideBorder extends EtchedBorder {

    /**
     * 矩形の上方にエッチング線を引く。
     */
    public static final int TOP = 0;
    /**
     * 矩形の左方にエッチング線を引く。
     */
    public static final int LEFT = 1;
    int orientation;

    /**
     * SingleSideBorder オブジェクトを作成する
     * @param orientation LEFT,TOPのいずれか。
     */
    public SingleSideBorder(int orientation) {
        super();
        this.orientation = orientation;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int w = width;
        int h = height;

        g.translate(x, y);
        if (orientation == TOP) {
            g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
            g.drawLine(0, 0, w - 1, 0);
            g.setColor(etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
            g.drawLine(0, 1, w - 1, 1);
        } else if (orientation == LEFT) {
            g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
            g.drawLine(0, 0, 0, h - 1);
            g.setColor(etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
            g.drawLine(1, 0, 1, h - 1);
        }
        g.translate(-x, -y);
    }
}
