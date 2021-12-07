/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import static java.lang.Math.*;
import java.awt.geom.Line2D;
import java.util.List;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;

/**
 * ホロスコープの感受点の位置を明確に指し示すマーカーを描画する。
 * マーカーは感受点とダイヤルゲージを結ぶ線。
 * @author 大澤義鷹
 */
public class MarkerNeedle extends ChartParts {
    public static final int INNER = 0;
    public static final int CENTER = 1;
    public static final int OUTER = 2;
    double size = 0.003;
    List<Body> bodys;
    int direction = OUTER;
    double extensionDiameter = 0;
    Color color = Color.BLACK;
    private static final Stroke stroke = new BasicStroke(1f);
    private Line2D line = new Line2D.Double();
    /**
     * 引き出し線オブジェクトを作成する。
     */
    public MarkerNeedle() {
        
    }
    /**
     * 天体のリストをセットする。
     */
    public void setBodyList( List<Body> bodys ) {
        this.bodys = bodys;
    }
    /**
     * このオブジェクトにセットされているパラメターに従ってマーカーを描画する。
     */
    public void draw() {
        double extensionRadius = extensionDiameter * bp.w / 2d;
        double radius = (diameter + diameterOffset) * bp.w / 2d;
        double sz = (float)(size * bp.w);
        for ( Body sp : bodys ) {
            if ( ! acmcVisible ) {
                if ( sp.id >= Const.AC && sp.id <= Const.IC ) continue; 
            }
            double a = -(sp.lon + 180 - ( roll + ascendant ) );
            double cv = cos( a * PI / 180d);
            double sv = sin( a * PI / 180d);
            double sx,sy,ex,ey;
            if ( direction == INNER ) {
                sx = cv * radius;
                sy = sv * radius;
                ex = cv * (radius - sz);
                ey = sv * (radius - sz);
            } else if ( direction == OUTER ) {
                sx = cv * radius;
                sy = sv * radius;
                ex = cv * (radius + sz);
                ey = sv * (radius + sz);
            } else {
                double l = sz / 2d;
                sx = cv * (radius - l);
                sy = sv * (radius - l);
                ex = cv * (radius + l);
                ey = sv * (radius + l);
            }
            sx += bp.x;
            sy += bp.y;
            ex += bp.x;
            ey += bp.y;
            bp.g.setPaint(color);
            bp.g.setStroke( stroke );
            line.setLine(sx,sy,ex,ey);
            bp.g.draw(line);
            if ( extensionDiameter > 0 ) {
                double a2 = -( sp.plot + 180 - ( roll + ascendant ) );
                sx = cos( a2 * PI / 180d) * extensionRadius + bp.x;
                sy = sin( a2 * PI / 180d) * extensionRadius + bp.y;
                line.setLine( ex, ey, sx, sy );
                bp.g.draw( line );
            }
        }
    }
    private boolean acmcVisible = true;
    /**
     * AC,DC,MC,ICに対するマーカー表示/非表示をセット。trueなら表示。
     */
    public void setAcMcVisible( boolean b ) {
        acmcVisible = b;
    }
    private double angleAbs( double a1, double a2 ) {
        double angle = abs( a1 - a2 );
        if ( angle >= 180d ) angle = 360d - angle;
        return angle;
    }
    
    /**
     * マーカーのサイズを0-1の値で指定する。デフォルトは0.008。
     */
    public void setMarkerSize( double size ) {
        this.size = size;
    }
    
    /**
     * マーカー線(ゲージ上に出る印線)を直径(setDiameter()でセットした値)から
     * 内に伸ばすか外側にのばすか。
     * INNERを指定すると内、OUTERを指定すると外、CENTERを指定すると内と外に伸ばす。
     * デフォルトはOUTER。
     */
    public void setDirection( int direction ) {
        this.direction = direction;
    }
    
    /**
     * マーカーと天体を結ぶ線の直径をセットする。
     * <pre>
     *     ◎ ←天体
     *     /　←ナナメの線の天体側の先端の値
     * | | | | | |　←ゲージ
     * </pre>
     * ゲージからナナメ線の始まりの長さは、setMarkerSize()で指定する。
     * 0をセットするとマーカー線は引かない。デフォルトは0。
     * @param diameter 直径を0-1で指定する。
     */
    public void setExtensionDiameter( double diameter ) {
        this.extensionDiameter = diameter;
    }
    
    /**
     * 線色をセットする。
     */
    public void setColor( Color color ) {
        this.color = color;
    }
}
