/*
 * GLayout.java
 *
 * Created on 2007/10/04, 13:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * GLayouterはGLabelと同じ仲間の部品でもあって、このレイアウトに、他の
 * レイアウトをaddすることことができる。これにより複雑に入れ子構造を作って、
 * 文字や天体シンボルを自在にレイアウトすることができる。
 * @author 大澤義鷹
 */
public interface GLayout {
    public static final int G_CENTER = GLabel.CENTER;
    public static final int G_RIGHT  = GLabel.RIGHT;
    public static final int G_BOTTOM = GLabel.BOTTOM;
    public static final int G_LEFT = GLabel.LEFT;
    public static final int G_TOP = GLabel.TOP;

    /**
     * 部品の位置を指定する。BasePointを原点とする座標系。
     */
    public void setLocation( double x, double y );
    /**
     * 基準とする画面幅を指定する。
     */
    public void setBaseWidth( float baseWidth );
    /**
     * 原点となる座標を指定する。デフォルトはゼロだが任意の場所にtranslate可能。
     */
    public void setBasePoint( Point2D basePoint );
    public void setGraphics( Graphics2D g );
    public void setup();
    public void draw();
    public GComponentListener getGComponentListener();
    public GComponent contains(int x,int y);
}
