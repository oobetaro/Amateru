/*
 *
 */
package to.tetramorph.starbase.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import to.tetramorph.starbase.lib.AngleUtilities;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * 天体のディスポジスターをグラフで表現する。Java2Dを使ったものに変更。
 */
public class Dispositor {
    //十角形で、上方向に月を配し、時計回りに月、水星、金星、、、冥王星
    //という順番に天体を配置するためのテーブル。なぜこの順番にしたのか失念。
    //既存のソフトがそうなっているのをみたからか？
    private static final int [] PLANET_IDs = {
        MOON,MERCURY,VENUS,SUN,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO
    };
    public static final int MODERN = 0;
    public static final int CLASSIC = 1;
    private static final Stroke stroke = new BasicStroke( 1.0f );

    /**
     * 天体IDとX,Y座標を保持する
     */
    private class Planet extends Point2D.Double {
        int id;
        Planet( int id, int x, int y ) {
            super( x, y );
            this.id = id;
        }
    }
    
    Planet [] planet;
    
    /**
     * オブジェクトを作成する。
     */
    public Dispositor() {
        planet = new Planet[10];
    }
    /**
     * 10天体の在泊星座をディスポジスターのグラフにしてビットマップにして返す。
     * staying[]は0番目は太陽で9番目は冥王星となっていて、それぞれの配列に星座番号
     * を入れてコールする。星座番号は牡羊座を0として始まり11が魚座。
     * たとえば太陽が獅子座にあるならstaying[0]=5、月が双子座にあるならstaying[1]=2。
     *
     * @param staying 天体が在泊している星座番号を格納した配列。かならず10天体すべての
     * データがセットされていなければならない。
     * @param imgWidth 生成するグラフの一片の長さ
     * @param rulerType MODERNまたはCLASSICのどちらかを指定。
     * @return ディスポジスターの図。大きさはimgWidth の正方形
     */
    public BufferedImage getGraph( int staying[], int imgWidth, int rulerType ) {
        int width = 190; //190の定数で、図のバランスをとったから190なのだ。
        int radius = (int)( width * 0.84 / 2. ); //天体を配置する円周の半径
        int MAX = rulerType == MODERN ? 10 : 7;
        //天体を配置する座標を生成
        for ( int i = 0; i < MAX; i++ ) {
            double a = Math.toRadians( i * 36. - 72. );
            int x = (int)( Math.cos( a ) * radius + width / 2 );
            int y = (int)( Math.sin( a ) * radius + width / 2 );
            planet[ PLANET_IDs[i] ] = new Planet( PLANET_IDs[i], x, y );
        }
        BufferedImage memImage =
            new BufferedImage( imgWidth, imgWidth, BufferedImage.TYPE_INT_BGR );
        Graphics g1 = memImage.createGraphics();
        Graphics2D g = (Graphics2D)g1;
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, imgWidth, imgWidth );
        g.setColor( Color.BLACK );
        g.setStroke( stroke );
        
        AffineTransform zoom_at = new AffineTransform();
        double sc = imgWidth / 190.;
        zoom_at.setToScale( sc, sc );
        g.transform( zoom_at );
        
        Font font = AstroFont.getFont( 16f );
        FontRenderContext render = g.getFontRenderContext();
        
        Line2D.Double line = new Line2D.Double();
        int [] rulers = ( rulerType == MODERN)  ? 
                                MODERN_RULERS    :    CLASSIC_RULERS;
        for ( int p = 0; p < MAX; p++ ) {
            int z = staying[ p ];
            int p2 = rulers[ z ];
            if ( p == p2 ) {
                g.drawOval( (int)planet[p].x - 12, (int)planet[p].y - 12, 24, 24 );
                continue;
            }
            Point2D.Double plot1 = (Point2D.Double)planet[p].clone();
            Point2D.Double plot2 = (Point2D.Double)planet[p2].clone();
            //天体シンボルに重ならないように線の長さをちぢめる
            AngleUtilities.shrink( plot1, plot2, 12 );
            //ちぢめた線の終端に矢印を描くためにShapeを作る
            Shape aline = getArrowLine( plot1, plot2 );
            //もう一度線を少しちぢめる事で、矢印の先端からはみださないようにする
            //美しく描画するコツ
            AngleUtilities.shrink( plot1, plot2, 4 );
            line.setLine( plot1, plot2 );
            g.draw(line);
            g.fill( aline );
        }

        for ( int i = 0; i < MAX; i++ ) {
            Planet p = planet[i];
            TextLayout tl = new TextLayout(
                "" + BODY_CHARS[ p.id ], font, render );
            float h = tl.getAscent() / 2f;
            float w = tl.getAdvance() / 2f;
            AffineTransform at = new AffineTransform();
            at.translate( p.x - w, p.y + h );
            Shape bodyShape = tl.getOutline(at);
            g.fill( bodyShape );
        }
        g.dispose();
        return memImage;
    }
    
    
    /**
     * 指定された座標に矢印の頭の座標を返す。
     * @param xpos X座標
     * @param ypos Y座標
     * @param direction 矢印の向き(0〜360)
     * @param arrowAngle 矢印の開き角度
     * @param alen       矢印の頭の長さ
     */
    private Point2D.Double [] drawArrow( double xpos, 
                                          double ypos,
                                          double direction, 
                                          double arrowAngle,
                                          double alen ) {
        Point2D.Double [] p = { new Point2D.Double(),
                                new Point2D.Double(), 
                                new Point2D.Double() };
        p[0].x = xpos; 
        p[0].y = ypos;
        double a = Math.toRadians( direction - arrowAngle );
        double b = Math.toRadians( direction + arrowAngle );
        p[1].x = Math.round( Math.cos( a ) * alen + xpos );
        p[1].y = Math.round( Math.sin( a ) * alen + ypos );
        p[2].x = Math.round( Math.cos( b ) * alen + xpos );
        p[2].y = Math.round( Math.sin( b ) * alen + ypos );
        return p;
    }
    /**
     * 線の終端(x2,y2)位置に矢印のShapeを作成して返す。矢印線を描くのに必要。
     */
    Shape getArrowLine( Point2D.Double p1, Point2D.Double p2 ) {
        double angle = ( AngleUtilities.trigon( p1.x, p1.y, p2.x, p2.y ) + 180. ) % 360;
        Point2D.Double [] p = drawArrow( p2.x, p2.y, angle, 24., 8 );
        Path2D.Double path = new Path2D.Double();
        path.moveTo( p2.x, p2.y);
        path.lineTo( p[1].x, p[1].y );
        path.lineTo( p[2].x, p[2].y);
        path.lineTo( p2.x, p2.y );
        return path;
    }

}