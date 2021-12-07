/*
 * PlotAdjuster.java
 *
 * Created on 2006/09/14, 20:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import to.tetramorph.starbase.lib.AngleUtilities;
import to.tetramorph.starbase.lib.Body;

/**
 * 感受点の位置調整を行うクラス。天体をホロスコープ上にプロットする際、たがいに
 * 接近しすぎていると天体のシンボルが重なりあってしまう時がある。そのときは天体
 * 同士を適当に引き離してやると見やすくなる。このクラスはその計算を行う。
 * ただしAC,MC,DC,IC,VERTEX,ANTI_VERTEXは移動させない。これらは地上的な座標なの
 * で、カスプと同じ位置に不動であるのが正しいというポリシー。
 * @author 大澤義鷹
 */
public class PlotAdjuster {
    List<Body> sp = new ArrayList<Body>();
    int serial = 0;
    double space;
    AngleComparator sortByAngle = new AngleComparator();
    SerialComparator sortBySerial = new SerialComparator();
    
    /**
     * オブジェクトを作成。
     * @param space 天体が接近しているとき何度まで引き離すか。
     */
    public PlotAdjuster( double space ) {
        this.space = space;
    }
    
    /**
     * オブジェクトを作成。
     * 天体が接近しているときは10度まで引き離す。
     */
    public PlotAdjuster() {
        this.space = 10d;
    }
    
    /**
     * 計算すべき感受点を追加する。getResult()を呼ぶ前に、このメソッドで計算したい
     * 感受点をすべて追加する。その際、p.lonの値がp.plotに複写され、またp.serialに
     * はシリアル番号が付与される。addした順番でシリアル番号は0,1,2,,,と増えていく。
     */
    public void add( Body p ) {
        p.plot = p.lon;
        p.serial = serial++;
        sp.add(p);
    }
    
    /**
     * リストに格納されたBodyオブジェクトの表示位置を調整して返す。
     * 引数で渡した内容を参照書き換え。
     * このメソッドを呼ぶときは、add()メソッドは使わないことを推奨。add()メソッドは
     * 廃止予定。
     * @param bodyList Bodyオブジェクトのリスト
     * @param space 何度まで引き離すか
     */
    public List<Body> getAdustedList( List<Body> bodyList, double space ) {
        this.space = space;
        this.sp = bodyList;
        for ( int i=0; i<bodyList.size(); i++ ) {
            Body p = bodyList.get(i);
            p.plot = p.lon;
            p.serial = i; //シリアル番号を打つ
        }
        return getResult();
    }
//    /**
//     * 円周上のa点からb点の小さいほうの角度を返す。
//     * a,bともデグリーでの角度指定。
//     */
//    static double arc( double a, double b ) {
//        double angle = ( a - b ) % 360;
//        if ( angle > 180 ) {
//            angle = angle - 360;
//        } else if ( angle < -180 ){
//            angle = 360 + angle;
//        }
//        return Math.abs( angle );
//    }
    /**
     * このオブジェクトにadd()で登録された天体を表示する際に、たがいのシンボルが
     * かさならないよう必要に応じて表示位置を調整し、結果をListで返す。
     * @return 表示位置plotが決まった天体のリスト。addした順番でならんでいる。
     */
    public List<Body> getResult() {
        int adjustcount = 0; //無限ループ回避のための安全弁
        boolean update = false; //表示位置更新フラグ
        Collections.sort( sp, sortByAngle );
        do {
            for ( int i=0; i < sp.size(); i++ ) {
                int id = sp.get(i).id;
                //AC,MC等は位置調整から除外することにしてみたが見かけ上かっこ
                //悪い事もあるのでやめた
                //if(id >= Const.AC && id <= Const.ANTI_VERTEX) continue;
                int p = (( i + 1 ) == sp.size()) ?  0 :  i + 1;
                int m = (( i - 1 ) < 0 ) ? sp.size() - 1 : i - 1;
//                double dp = sp.get(p).plot - sp.get(i).plot;
//                if ( dp < 0 ) dp += 360d;
//                double dm = sp.get(i).plot - sp.get(m).plot;
//                if ( dm < 0 ) dm += 360d;
                double dp = AngleUtilities.arc( sp.get(p).plot, sp.get(i).plot ); //大-小
                double dm = AngleUtilities.arc( sp.get(i).plot, sp.get(m).plot );
                if ( dp < dm && dp < space ) {
                    //天体の追い越しが発生するときは移動させない
//                    if ( ( sp.get(i).plot - 1) > sp.get(m).plot )
                        sp.get(i).plot--;
                    if ( sp.get(i).plot < 0 ) {
                         sp.get(i).plot += 360d;
                         Collections.sort( sp, sortByAngle );
                    }
                    update = true;
                } else if ( dp > dm && dm < space ) {
                    //天体の追い越しが発生するときは移動させない
//                    if ( (sp.get(i).plot + 1) < sp.get(p).plot )
                        sp.get(i).plot++;
                    if ( sp.get(i).plot >= 360d ) {
                         sp.get(i).plot -= 360d; //sp.get(i).plot %= 360d;
                         Collections.sort( sp, sortByAngle );
                    }
                    update = true;
                } else if ( dp == dm && dp < space ) {
                    if ( sp.get(p).serial > sp.get(i).serial )
                        sp.get(i).plot -= 0.001f;
                    else sp.get(i).plot += 0.001f;
                    update = true;
                }
//                Body s = sp.get(i);
//                if ( s.id == 4 ) {
//                    System.out.println("mars plot = " + s.plot );
//                }
            }
            adjustcount++;
        } while ( adjustcount < 200 && update );
        //位置調整が終わったら、シリアル番号でソートすることでaddされた順番に戻し、
        Collections.sort( sp, sortBySerial );
        return sp;
    }
    // Pointを角度で降り順でソートする比較器
    private class AngleComparator implements Comparator<Body> {
        public int compare( Body o1, Body o2 ) {
            return Double.compare( o1.plot, o2.plot );
        }
    }
    // Pointをシリアル番号で降り順でソートする比較器
    private class SerialComparator implements Comparator<Body> {
        public int compare( Body o1, Body o2 ) {
            return o1.serial - o2.serial;
        }
    }
//    public static void main(String args[]) {
//        PlotAdjuster pa = new PlotAdjuster();
//        pa.add( new Body( 0, 345.36d ) );
//        pa.add( new Body( 1, 345.62d ) );
//        pa.add( new Body( 2, 347.73d ) );
//        pa.add( new Body( 3, 349.7d  ) );
//        pa.add( new Body( 4, 348.99d ) );
//        List<Body> list = pa.getResult();
//        for ( Body p : list ) {
//            System.out.println( p.toString() + ", plot = " + p.plot );
//        }
//    }
}
