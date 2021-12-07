/*
 * AngleUtilities.java
 *
 * Created on 2008/04/07, 17:43
 *
 */

package to.tetramorph.starbase.lib;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * 角度を計算するためのstaticメソッド群。
 * @author 大澤義鷹
 */
public class AngleUtilities {
    
    /**  AngleUtilities オブジェクトを作成する */
    private AngleUtilities() {
    }
    
    /**
     * 円の上で、a地点からb地点までの移動角度を求める。
     * aが0度のときbが10度だったとすれば、10度を返す。反対にaが10度でbが0度であれば
     * -10度を返す。aとbの差が180度の場合は180度を、181度離れた場合は、-179度を返す。
     * @param a 角度(0〜360〜それ以上の値も可)
     * @param b 角度(0〜360〜それ以上の値も可)
     * @return 距離角
     */
    public static double angleDistance(double a,double b) {
        a = a % 360;
        b = b % 360;
        double d = Math.abs(a-b);
        if ( d > 180 ) {
            d = 360 - d;
            return ( a < b ) ? -d : d;
        }
        return ( a < b ) ? d : -d;
    }
    
    /**
     * 与えられた直交座標が、原点からみたとき何度の方向になるかを返す。
     *
     * <pre>
     * 座標系
     *       (y) 90ﾟ
     * 180ﾟ   |
     * (-x)---+---(x) 0ﾟ
     *        |
     *       (-y)270ﾟ
     * </pre>
     * グラフィック座標はy座標がひっくりかえっているため、事前にy座標の符号を
     * 反転させて与える必要がある。
     */
    public static double trigon( double x, double y ) {
        //Javaには負のゼロ(-0.0)が存在し、その値が入ると0度を返すべきときに180度
        //になる。-0.0==0.0はtrueと判定されるので、0または-0が入ったときは強制的に
        //ゼロについている符号を取り去る。
        if ( x == 0.0 ) x = Math.abs(x);
        if ( y == 0.0 ) y = Math.abs(y);
        //double a = Math.atan(x/y) * 180d / Math.PI;
        double a = Math.toDegrees( Math.atan( x / y ) );
        if ( y < 0 ) a -= 180.0;
        if ( a < 0 ) a += 360.0;
        a = 180 - a -90;
        if(  a < 0 ) a += 360;
        return a;
    }
    
    /**
     * zx,zyを原点とし、x,y点との相対角度を求める。
     */
    public static double trigon(double zx,double zy,double x,double y) {
        x -= zx; y -= zy;
        return trigon(x,y);
    }
    
    /**
     * 円周上のa点からb点までの小さいほうの角度を返す。
     * a,bともデグリーでの角度指定。
     * @return 必ず正の値
     */
    public static double arc( double a, double b ) {
        double angle = ( a - b ) % 360;
        if ( angle > 180 ) {
            angle = angle - 360;
        } else if ( angle < -180 ){
            angle = 360 + angle;
        }
        return Math.abs( angle );
    }
    
    /**
     * 二点間の距離を、角度を保ったまま指定された長さだけ縮める。
     * 値は参照で書き換えられる。つまりp1,p2は入力であるとともに出力でもある。
     */
    public static void shrink( Point2D.Double p1, 
                                     Point2D.Double p2,
                                     double len ) {
        double a = Math.toRadians( trigon( p1.x, p1.y, p2.x, p2.y ) );
        double b = Math.toRadians( trigon( p2.x, p2.y, p1.x, p1.y ) );
        p1.x = Math.cos( a ) * len + p1.x;
        p1.y = Math.sin( a ) * len + p1.y;
        p2.x = Math.cos( b ) * len + p2.x;
        p2.y = Math.sin( b ) * len + p2.y;        
    }
}
