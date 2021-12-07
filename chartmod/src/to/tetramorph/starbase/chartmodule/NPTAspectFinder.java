/*
 * NPTAspectFinder.java
 *
 * Created on 2008/02/24, 18:04
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.util.NPTChart;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AspectFinder;

/**
 * NPT各円同士のアスペクトを求めるメソッド。普通に各円の組合せを網羅する求め方と
 * 一つの天体が他の天体と形成しているアスペクトのみを抽出するケースと二種類ある。
 * @author 大澤義鷹
 */
public class NPTAspectFinder {
    static final int N_N = 1;
    static final int P_P = 2;
    static final int N_P = 3;
    static final int T_T = 4;
    static final int N_T = 5;
    static final int P_T = 6;
    static final int NATAL = NPTChart.NATAL;
    static final int PROGRESS = NPTChart.PROGRESS;
    static final int TRANSIT = NPTChart.TRANSIT;
    
    /**  NPTAspectFinder オブジェクトを作成する */
    private NPTAspectFinder() {
    }
// はじめこのメソッドが入口で、内部で二つに分岐していたが別々に呼ぶことにした
//    public static List<Aspect> getAspectList( int value,
//                                                Body body,
//                                                NPTChart nptChart,
//                                                NPTSpecificSettingPanel scp,
//                                                int aspectTarget ) {
//        if ( body == null ) 
//            return getAspectList( value, nptChart, scp );
//        else
//            return getTargetAspectList( value, body, nptChart, scp, aspectTarget );
//    }


    /**
     * N,P,Tの組合せアスペクトを求める。
     * flagのbit0〜bit5がスイッチになっていている。b0が1ならネイタル円のアスペクト
     * を計算する。b3が1ならネイタル円とトランジット円の天体同士のアスペクトを
     * 求める。b0〜b5すべてが1なら、すべてのアスペクトを求める。
     * <pre>
     * b0   N
     * b1   P
     * b2   T
     * b3   N-T
     * b4   N-P
     * b5   P-T
     * </pre>
     * @param flag 6bitのフラグで、bitが1ならそのアスペクトを求める。
     * @param nptChart NPTChartオブジェクト
     * @param scp NPT計算設定パネルオブジェクト
     */
    public static List<Aspect> getAspectList( int flag,
                                                NPTChart nptChart,
                                                NPTSpecificSettingPanel scp) {
        int [] nBodys = scp.getAspectNatalBodyIDs();
        int [] tBodys = scp.getAspectTransitBodyIDs();
        int [] pBodys = scp.getAspectProgressBodyIDs();
        List<Aspect> aspectList = new ArrayList<Aspect>(); //結果を入れるリスト
        
        if ( (flag & 1) != 0 ) { //Natal
            List<Aspect> tempList = AspectFinder.getAspects(
                nptChart.getBodyList( nBodys, NATAL ),
                scp.getAspectTypes( N_N ) );
            for ( Aspect asp : tempList ) aspectList.add(asp);
        }
        if ( (flag & 2) != 0 ) { //Progress
            List<Aspect> tempList = AspectFinder.getAspects(
                nptChart.getBodyList( pBodys, PROGRESS),
                scp.getAspectTypes( P_P ));
            for ( Aspect asp : tempList ) aspectList.add(asp);
        }
        if ( (flag & 4) != 0 ) { //Transit
            List<Aspect> tempList = AspectFinder.getAspects(
                nptChart.getBodyList( tBodys, TRANSIT ),
                scp.getAspectTypes( T_T ));
            for ( Aspect asp : tempList ) aspectList.add(asp);
        }
        if ( (flag & 8) != 0 ) { //N-T
            List<Body> list = nptChart.getBodyList( tBodys, TRANSIT );
            AspectFinder.getAspects( nptChart.getBodyList( nBodys, NATAL),
                                     nptChart.getBodyList( tBodys, TRANSIT),
                                     scp.getAspectTypes( N_T ),
                                     aspectList);
        }
        if ( (flag & 16) != 0 ) { //N-P
            AspectFinder.getAspects( nptChart.getBodyList( nBodys, NATAL ),
                                     nptChart.getBodyList( pBodys, PROGRESS ),
                                     scp.getAspectTypes( N_P ),
                                     aspectList );
        }
        if ( (flag & 32) != 0 ) { //P-T
            AspectFinder.getAspects( nptChart.getBodyList( pBodys, PROGRESS ),
                                     nptChart.getBodyList( tBodys, TRANSIT ),
                                     scp.getAspectTypes( P_T ),
                                     aspectList );
        }
        return aspectList;
    }
    
    /**
     * bodyで指定された天体と他の天体とのアスペクト(複数)を求める。
     * @param npt bodyがネイタル天体なら0、プログレスなら1、トランジットなら2を
     * 指定する。
     * @param body     焦点となる天体
     * @param nptChart NPTChartオブジェクト
     * @param scp      NPT計算設定パネル
     * @param target   N,P,Tのどの円の天体群とのアスペクトを検出するか。b0,b1,b2の組合せ
     * で指定する。
     */
    public static List<Aspect> getTargetAspectList( int npt,
                                                       Body body,
                                                       NPTChart nptChart,
                                                       NPTSpecificSettingPanel scp,
                                                       int target ) {
        int [] nBodys = scp.getAspectNatalBodyIDs();
        int [] tBodys = scp.getAspectTransitBodyIDs();
        int [] pBodys = scp.getAspectProgressBodyIDs();
        List<Aspect> aspectList = new ArrayList<Aspect>();
        //int target = scp.getAspectTargets();
        if( npt == 0 ) { //選択天体がNatalのとき、NN,NP,NTの座相を検出
            if ( (target & 1) != 0 ) { //N-N
                List<Body> list = nptChart.getBodyList(nBodys,NATAL);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( N_N ) );
            }
            if ( (target & 2) != 0 ) { //N-P
                List<Body> list = nptChart.getBodyList(pBodys,PROGRESS);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( N_P ) );
            }
            if ( (target & 4) != 0 ) { //N-T
                List<Body> list = nptChart.getBodyList(tBodys,TRANSIT);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( N_T ) );
            }
        } else if ( npt == 1 ) { //Progress
            if ( (target & 1) != 0) { //P-N
                List<Body> list = nptChart.getBodyList(nBodys,NATAL);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( N_P ) );
            }
            if ( (target & 2) != 0 ) { //P-P
                List<Body> list = nptChart.getBodyList(pBodys,PROGRESS);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( P_P ) );
            }
            if ( (target & 4) != 0 ) { //P-T
                List<Body> list = nptChart.getBodyList(tBodys,TRANSIT);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( P_T ) );
            }
        } else if ( npt == 2 ) {
            if ( (target & 1) != 0 ) { //N-T
                List<Body> list = nptChart.getBodyList(nBodys,NATAL);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( N_T ) );
            }
            if ( (target & 2) != 0 ) { //P-T
                List<Body> list = nptChart.getBodyList(pBodys,PROGRESS);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( P_T ) );
            }
            if ( (target & 4) != 0 ) { //T-T
                List<Body> list = nptChart.getBodyList(tBodys,TRANSIT);
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( T_T ) );
            }
        }
        return aspectList;
    }
    // 見てのとおり、if文の羅列が冗長見えるし、ビットシフト演算を使ってループで
    // 処理する方法もあるが、ある種のわかりやすさと、間違えようのない確実さもある
    // と考える。
}
