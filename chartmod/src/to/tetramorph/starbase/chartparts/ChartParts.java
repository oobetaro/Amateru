/*
 * ChartParts.java
 *
 * Created on 2007/05/25, 2:19
 *
 */

package to.tetramorph.starbase.chartparts;

/**
 * ホロスコープを描画するための部品クラスを作るときの親クラス。
 * @author 大澤義鷹
 */
public abstract class ChartParts {
    protected BasePosition bp;
    protected double roll = 0;
    protected double ascendant = 0;
    protected double diameter = 0;
    protected double diameterOffset = 0;
    /**
     * オブジェクトを作成する
     */
    public ChartParts() {
    }
    /**
     * オブジェクトを作成する
     * @param bp BasePositionオブジェクト
     */
    public ChartParts(BasePosition bp) {
        this.bp = bp;
    }
    /**
     * BasePositionオブジェクトをセットする。
     */
    public void setBasePosition(BasePosition bp) {
        this.bp = bp;
    }
    public BasePosition getBasePosition() {
        return bp;
    }
    /**
     * この部品の回転角をセットする。この値はチャートの向きを変更したりする場合に
     * 使用する。
     * ChartPartsオブジェクトは、この値とascendatの値を加算した角度に回転する。
     */
    public void setRoll(double roll) {
        this.roll = roll;
    }
    public double getRoll() {
        return roll;
    }
    /**
     * 上昇角度を指定する。ホロスコープのアセンダントの度数をセットすれば、
     * その位置が左側の上昇点の位置になる。
     */
    public void setAscendant(double ascendant) {
        this.ascendant = ascendant;
    }
    public double getAscendant() {
        return ascendant;
    }
    /**
     * この部品の直径を指定する。
     * @param diameter 0〜1の値でBasePosition.wの値に対するパーセンテージで指定する。
     */
    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }
    
    public double getDiameter() {
        return diameter;
    }
    /**
     * この部品を描画する。
     */
    public abstract void draw();
    /**
     * 一時的に円の直径を変更するためのオフセットをセットする。
     * この値はdiameterの値と加算されて直径とみなされる。
     */
    public void setDiameterOffset(double offset) {
        diameterOffset = offset;
    }
    public double getDiameterOffset() {
        return diameterOffset;
    }
    
//    /**
//     * 円の上で、a地点からb地点までの移動角度を求める。
//     * aが0度のときbが10度だったとすれば、10度を返す。反対にaが10度でbが0度であれば
//     * -10度を返す。aとbの差が180度の場合は180度を、181度離れた場合は、-179度を返す。
//     * @param a 角度(0〜360〜それ以上の値も可)
//     * @param b 角度(0〜360〜それ以上の値も可)
//     * @return 距離角
//     */
//    public static double angleDistance(double a,double b) {
//        a = a % 360;
//        b = b % 360;
//        double d = Math.abs(a-b);
//        if(d > 180) {
//            d = 360 - d;
//            return (a < b) ? -d : d;
//        }
//        return (a < b) ? d : -d;
//    }
//    
//    /**
//     * この座標系で角度を返す。
//     * <pre>
//     *       (y) 90ﾟ
//     * 180ﾟ   |
//     * (-x)---+---(x) 0ﾟ
//     *        |
//     *       (-y)270ﾟ
//     * </pre>
//     * グラフィック座標はy座標がひっくりかえっているため、事前にy座標の符号を
//     * 反転させて与える必要がある。
//     */
//    public static double trigon(double x,double y) {
//        //Javaには負のゼロ(-0.0)が存在し、その値が入ると0度を返すべきときに180度
//        //になる。-0.0==0.0はtrueと判定されるので、0または-0が入ったときは強制的に
//        //ゼロについている符号を取り去る。
//        if(x == 0.0) x = Math.abs(x);
//        if(y == 0.0) y = Math.abs(y);
//        double a = Math.atan(x/y) * 180d / Math.PI;
//        if( y<0 ) a -= 180.0;
//        if( a<0 ) a += 360.0;
//        a = 180 - a -90;
//        if(  a<0 ) a += 360;
//        return a;
//    }
//    /**
//     * zx,zyを原点とし、x,y点との相対角度を求める。
//     */
//    public static double trigon(double zx,double zy,double x,double y) {
//        x -= zx; y -= zy;
//        return trigon(x,y);
//    }
    
}
