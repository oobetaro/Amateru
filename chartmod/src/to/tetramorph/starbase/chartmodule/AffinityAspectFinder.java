/*
 * NPTAspectFinder.java
 *
 * Created on 2008/02/24, 18:04
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.util.ArrayList;
import java.util.List;
import to.tetramorph.starbase.util.AffinityChart;
import to.tetramorph.starbase.util.NPTChart;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AspectFinder;

/**
 * NPT相性円でのアスペクトを求めるメソッド。普通に各円の組合せを網羅する求め方と
 * 一つの天体が他の天体と形成しているアスペクトのみを抽出するケースと二種類ある。
 * @author 大澤義鷹
 */
public class AffinityAspectFinder {
    static final int N_N = 1;
    static final int P_P = 2;
    static final int N_P = 3;
    static final int T_T = 4;
    static final int N_T = 5;
    static final int P_T = 6;
    static final int NATAL     = AffinityChart.NATAL;
    static final int PROGRESS  = AffinityChart.PROGRESS;
    static final int TRANSIT   = AffinityChart.TRANSIT;
    static final int NATAL2    = AffinityChart.NATAL2;
    static final int PROGRESS2 = AffinityChart.PROGRESS2;
    
    static final int [][] ELEMENTS = {
        { NATAL,             -1,  N_N },
        { NATAL2,            -1,  N_N },
        { PROGRESS,          -1,  P_P },
        { PROGRESS2,         -1,  P_P },
        { TRANSIT,           -1,  T_T },
        { NATAL,         NATAL2,  N_N },
        { PROGRESS,   PROGRESS2,  P_P },
        { NATAL,        TRANSIT,  N_T },
        { NATAL2,       TRANSIT,  N_T },
        { PROGRESS,     TRANSIT,  P_T },
        { PROGRESS2,    TRANSIT,  P_T },
        { NATAL,       PROGRESS,  N_P },
        { NATAL,      PROGRESS2,  N_P },
        { NATAL2,     PROGRESS2,  N_P },
        { NATAL2,      PROGRESS,  N_P }
    };
    
    //オブジェクトの作成禁止
    private AffinityAspectFinder() {
    }

    private static void singleAspects( List<Body> bodyList, 
                                          AspectType [] atypes,
                                          List<Aspect> result ) {
        List<Aspect> tempList = AspectFinder.getAspects( bodyList, atypes );
        for ( Aspect asp : tempList ) result.add(asp);        
    }

    
    
    
    private static void crossAspects( List<Body> bodyList1,
                                         List<Body> bodyList2,
                                         AspectType [] atypes,
                                         List<Aspect> result ) {
        
        List<Aspect> tempList = 
            AspectFinder.getAspects( bodyList1, bodyList2, atypes );
        
        for ( Aspect asp : tempList ) result.add(asp);
    }
    
    
    /**
     * N,P,T,N2,P2の組合せアスペクトを求める。
     * flagのbit0〜bit14がスイッチになっていている。
     * <pre>
     * b0   N1     b5   N1xN2   b10   P2xT
     * b1   N2     b6   P1xP2   b11   N1xP1
     * b2   P1     b7   N1xT    b12   N1xP2
     * b3   P2     b8   N2xT    b13   N2xP2
     * b4   T      b9   P1xT    b14   N2xP1
     * </pre>
     * @param flag 6bitのフラグで、bitが1ならそのアスペクトを求める。
     * @param chart NPTChartオブジェクト
     * @param scp NPT計算設定パネルオブジェクト
     */
    
    public static List<Aspect> getAspectList( int flag,
                                                AffinityChart chart,
                                                NPTSpecificSettingPanel scp) {
        int [] nBodys  = scp.getAspectNatalBodyIDs();
        int [] pBodys  = scp.getAspectProgressBodyIDs();
        int [] tBodys  = scp.getAspectTransitBodyIDs();
        int [][] bodys = new int[][] { nBodys, pBodys, tBodys };
        List<Aspect> aspectList = new ArrayList<Aspect>(); //結果を入れるリスト
        
        for ( int i=0; i < ELEMENTS.length; i++ ) {
            if ( ( flag & 1 ) == 1 ) {
                int left  = ELEMENTS[i][0];
                int right = ELEMENTS[i][1];
                int atype = ELEMENTS[i][2];
                AspectType [] atypes = scp.getAspectTypes( atype );
                if ( right < 0 ) { // シングル
                    List<Body> bodyList = 
                        chart.getBodyList( bodys[ left % 3 ], left );
                    singleAspects( bodyList, atypes, aspectList );
                } else {           // クロス
                    List<Body> bodyList1 =
                        chart.getBodyList( bodys[ left % 3 ], left );
                    List<Body> bodyList2 =
                        chart.getBodyList( bodys[ right % 3 ], right );
                    crossAspects( bodyList1, bodyList2, atypes, aspectList );
                }
            }
            flag = flag >>> 1;
        }
        return aspectList;
    }
    
    
    static final int [][] ELEMENTS2 = new int[][] {
        { N_N, N_P, N_T },
        { N_P, P_P, P_T },
        { N_T, P_T, T_T },
        { N_N, N_P, N_T },
        { N_P, P_P, P_T },
    };
    /**
     * bodyで指定された天体と他の天体とのアスペクト(複数)を求める。
     * @param npt      bodyがN1=0,P1=1,T=2,N2=3,P2=4を指定する。
     * @param body     選択された天体
     * @param chart    AffinityChartオブジェクト
     * @param scp      NPT計算設定パネル
     * @param target   N,P,T,N2,P2のどの円の天体群とのアスペクトを検出するか。
     *                 b0,b1,b2,b3,b4の組合せで指定する。
     */
    public static List<Aspect> getTargetAspectList( int npt,
                                                       Body body,
                                                       AffinityChart chart,
                                                       NPTSpecificSettingPanel scp,
                                                       int target ) {
        int [] nBodys = scp.getAspectNatalBodyIDs();
        int [] pBodys = scp.getAspectProgressBodyIDs();
        int [] tBodys = scp.getAspectTransitBodyIDs();
        int [][] bodys = new int[][] { nBodys, pBodys, tBodys };
        
        List<Aspect> aspectList = new ArrayList<Aspect>();
        int [] tokens = ELEMENTS2[ npt ];
        for ( int i=0; i < ELEMENTS2.length; i++ ) {
            if ( ( target & 1 ) == 1 ) {
                int n = i % 3;
                List<Body> list = chart.getBodyList( bodys[ n ], i );
                aspectList = AspectFinder.getAspects(
                    list, body, aspectList, scp.getAspectTypes( tokens[n] ) );
            }
            target = target >>> 1;
        }
        return aspectList;
    }
    // このプログラムの中で、i % 3 としている部分についての解説
    // N,P,T,N2,P2は0〜4の値が定義されている。
    // 0〜4の値の3の剰余を取ることで、N2=0,P2=1に変換される。
    // N,P,TはそのままN=0,P=1,T=2の値で変化しない。
}
