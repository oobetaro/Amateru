/*
 *
 */
package to.tetramorph.starbase.util;
import java.util.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * 天体間のアスペクトを検出する計算に使う。すべてstaticメソッド。
 */
public class AspectFinder {
    /**
     * デフォルトアスペクトタイプ。0,60,90,120,180,150,72度のアスペクトで、
     * 各オーブが設定されている。
     */
    public static final AspectType [] DEFAULT_ASPECT_TYPES = {
        new AspectType(CONJUNCTION,4,8),
        new AspectType(SEXTILE,3,6),
        new AspectType(SQUARE,4,8),
        new AspectType(TRINE,4,8),
        new AspectType(OPPOSITION,4,8),
        new AspectType(QUINCUNX,3,6),
        new AspectType(QUINTILE,2,4)
    };
    /**
     * 二体の天体間(p1,p2)の離角がangle度ならばtrueを返す。
     * 検査の際にはオーブが考慮される。
     * <b>ただしパラレルの判定には使えない。</b>
     * @param p1 天体1
     * @param p2 天体2
     * @param orb オーブ
     * @param angle 角度は0度から180度まで指定可能。
     * @return 入力された条件のアスペクトが成立していればtrueを返す。
     */
    public static boolean isAspect( Body p1,
                                       Body p2,
                                       double angle,
                                       double orb ) {
        double asp = Math.abs(p1.lon - p2.lon);
        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
        if ( (angle + orb) > 180 ) {
            if ( (angle - orb) <= asp ) return true;
        } else {
            if ( asp >= (angle - orb) && asp <= (angle + orb) ) return true;
        }
        return false;
    }
    
    
//    /**
//     * 廃止予定。
//     * 1重円のアスペクトを求める。天体位置をChartFactorで計算したあと、このメソッドに
//     * それを与えてアスペクトを求める事ができる。結果はAspectのListで求まる。<br>
//     * Aspectにはアスペクトを形成している二つの感受点(Body)、該当するアスペクト番号、
//     * タイトかルーズどちらのアスペクトか、誤差、の情報が登録されている。<br>
//     * それからノーアスペクトの検出も行われる。このときノーアス認定の基準は、太陽から
//     * 冥王星までの天体で検査してアスペクトが無い事が条件で、ノードやＭＣといった
//     * 仮想感受点とのアスペクトは無視される。またルーズアスペクトも無視する。
//     * ノーアスペクトはAspect#isNoAspect()で判定する。
//     * ただし、仮想感受点とのアスペクトは、アスペクトとしては認識される。
//     * それから非常に多くのアスペクトを検出することができるが、検出するアスペクトの種類
//     * を増やせば増やすほど、ノーアスになる確率は低くなる。
//     * <pre>
//     * アスペクトとオーブを定義するリストはたとえば次のようにして作る。
//     * CONJUNCITON等の定数は、Constで宣言されている。
//     *
//     * AspectType [] defAspect = {
//     *  new AspectType(CONJUNCTION,4,8),
//     *  new AspectType(SEXTILE,3,6),
//     *  new AspectType(SQUARE,4,8),
//     *  new AspectType(TRINE,4,8),
//     *  new AspectType(OPPOSITION,4,8),
//     *  new AspectType(QUINCUNX,3,6),
//     *  new AspectType(QUINTILE,2,4)
//     * };
//     * </pre>
//     * @see Aspect
//     * @param aspectTypes Constで定義されているアスペクトの種類
//     * @param cf 計算結果の入ったChartFactorオブジェクト
//     * @param bodys ChartFactorに与えたbodysと同じもの
//     * @return Aspectが入ったListを返す。
//     */
//    public static List<Aspect> getAspects( ChartFactor cf,
//                                             int bodys[],
//                                             AspectType [] aspectTypes ) {
//        List<Aspect> list = new ArrayList<Aspect>();
//        Set<Body> aspectBodys = new HashSet<Body>();
//        for(int y=0; y < bodys.length; y++) {
//            for(int x= y+1; x < bodys.length; x++) {
//                Body p1 = cf.getBody( bodys[y] );
//                Body p2 = cf.getBody( bodys[x] );
//                if(p1 == null || p2 == null) continue;
//                for(int i=0; i<aspectTypes.length; i++) {
//                    int aid = aspectTypes[i].aid;
//                    double angle = Const.ASPECT_ANGLE[ aid ];
//                    if(isAspect(p1,p2,angle,aspectTypes[i].tightOrb)) {
//                        double asp = Math.abs(p1.lon - p2.lon);
//                        if(asp > 180d) asp = 360d - asp; //必ず180度以下の値になる
//                        list.add(new Aspect(p1,p2,aid,Aspect.TIGHT,Math.abs(angle-asp)));
//                        //アスペクトがあったときは、その天体をHashSetに登録しておく
//                        if(p1.id <= Const.PLUTO && p2.id <= Const.PLUTO) {
//                            aspectBodys.add(p1);
//                            aspectBodys.add(p2);
//                        }
//                    } else if(isAspect(p1,p2,angle,aspectTypes[i].looseOrb)) {
//                        double asp = Math.abs(p1.lon - p2.lon);
//                        if(asp > 180d) asp = 360d - asp; //必ず180度以下の値になる
//                        list.add(new Aspect(p1,p2,aid,Aspect.LOOSE,Math.abs(angle-asp)));
//                        //ﾙｰｽﾞｱｽﾍﾟｸﾄはﾉｰｱｽ判定に入れない。
//                        //aspectBodys.add(p1); aspectBodys.add(p2);
//                    }
//                }
//            }
//        }
//        // HashSetから、アスペクトがなかった天体を求める
//        // 要求があってもASC等が求まらない事がありnullになるｹｰｽがある。それは除外。
//        for(int i=0; i<bodys.length; i++) {
//            if(bodys[i] > Const.PLUTO) continue; //太陽から冥王星までがﾉｰｱｽ検査対象
//            Body p = cf.getBody( bodys[i] );
//            if(p==null) continue;
//            if(!aspectBodys.contains(p))
//                list.add(new Aspect(p)); //ﾉｰｱｽ天体をﾘｽﾄに追加
//        }
//        return list;
//    }
    /**
     * 1重円のアスペクトを求める。天体位置をChartFactorで計算したあと、このメソッドに
     * それを与えてアスペクトを求める事ができる。結果はAspectのListで求まる。<br>
     * Aspectにはアスペクトを形成している二つの感受点(Body)、該当するアスペクト番号、
     * タイトかルーズどちらのアスペクトか、誤差、の情報が登録されている。<br>
     * それからノーアスペクトの検出も行われる。このときノーアス認定の基準は、太陽から
     * 冥王星までの天体で検査してアスペクトが無い事が条件で、ノードやＭＣといった
     * 仮想感受点とのアスペクトは無視される。またルーズアスペクトも無視する。
     * ノーアスペクトはAspect#isNoAspect()で判定する。
     * ただし、仮想感受点とのアスペクトは、アスペクトとしては認識される。
     * それから非常に多くのアスペクトを検出することができるが、検出するアスペクトの種類
     * を増やせば増やすほど、ノーアスになる確率は低くなる。
     * <pre>
     * アスペクトとオーブを定義するリストはたとえば次のようにして作る。
     * CONJUNCITON等の定数は、Constで宣言されている。
     *
     * AspectType [] defAspect = {
     *  new AspectType(CONJUNCTION,4,8),
     *  new AspectType(SEXTILE,3,6),
     *  new AspectType(SQUARE,4,8),
     *  new AspectType(TRINE,4,8),
     *  new AspectType(OPPOSITION,4,8),
     *  new AspectType(QUINCUNX,3,6),
     *  new AspectType(QUNTILE,2,4)
     * };
     * </pre>
     * @see Aspect
     * @param aspectTypes アスペクトを定義した配列
     * @param bodys アスペクトを検査する天体(複数)
     * @return Aspectが入ったListを返す。
     */
    public static List<Aspect> getAspects( List<Body> bodys, 
                                             AspectType [] aspectTypes ) {
        List<Aspect> list = new ArrayList<Aspect>();
        Set<Body> aspectBodys = new HashSet<Body>();
        for ( int y=0; y < bodys.size(); y++ ) {
            for ( int x = y+1; x < bodys.size(); x++ ) {
                Body p1 = bodys.get(y);
                Body p2 = bodys.get(x);
                //if(p1 == null || p2 == null) continue; //不要かも
                for ( int i=0; i<aspectTypes.length; i++ ) {
                    int aid = aspectTypes[i].aid;
                    double angle = Const.ASPECT_ANGLE[ aid ];
                    if ( isAspect(p1,p2,angle,aspectTypes[i].tightOrb) ) {
                        double asp = Math.abs(p1.lon - p2.lon);
                        //必ず180度以下の値になる
                        if ( asp > 180d ) asp = 360d - asp;
                        list.add( new Aspect( p1, p2, aid,
                                               Aspect.TIGHT,
                                               Math.abs( angle - asp ) ) );
                        //アスペクトがあったときは、その天体をHashSetに登録しておく
                        if ( p1.id <= Const.PLUTO && p2.id <= Const.PLUTO ) {
                            aspectBodys.add(p1);
                            aspectBodys.add(p2);
                        }
                    } else if ( isAspect( p1, p2, angle, aspectTypes[i].looseOrb ) ) {
                        double asp = Math.abs(p1.lon - p2.lon);
                        //必ず180度以下の値になる
                        if ( asp > 180d ) asp = 360d - asp;
                        list.add( new Aspect( p1, p2, aid,
                                               Aspect.LOOSE,
                                               Math.abs( angle - asp ) ) );
                        //ﾙｰｽﾞｱｽﾍﾟｸﾄはﾉｰｱｽ判定に入れない。
                        //aspectBodys.add(p1); aspectBodys.add(p2);
                    }
                }
            }
        }
        // HashSetから、アスペクトがなかった天体を求める
        // 要求があってもASC等が求まらない事がありnullになるｹｰｽがある。それは除外。
        for ( int i=0; i<bodys.size(); i++ ) {
            if ( bodys.get(i).id > Const.PLUTO ) continue; //太陽から冥王星までがﾉｰｱｽ検査対象
            Body p = bodys.get(i);
            //if(p==null) continue;
            if ( !aspectBodys.contains(p) )
                list.add( new Aspect(p) ); //ﾉｰｱｽ天体をﾘｽﾄに追加
        }
        return list;
    }
    
//    /**
//     * 廃止予定。
//     * 二つのチャート間における天体のアスペクトを求める。
//     * (たとえばネイタル図の太陽とトランジット図の太陽のアスペクトなど)。
//     * @param cf ChartFactorのインスタンス(チャート要素1)
//     * @param bodys アスペクトを計算する天体(複数)
//     * @param cf2 ChartFactorのインスタンス(チャート要素2)
//     * @param bodys2 アスペクトを計算する天体(複数)
//     * @param aspectTypes 認識するアスペクトの種類
//     * @return 検出されたアスペクトのリスト
//     */
//    public static List<Aspect> getAspects(ChartFactor cf,int bodys[],
//        ChartFactor cf2,int bodys2[],AspectType [] aspectTypes ) {
//        List<Aspect> list = new ArrayList<Aspect>();
//        for ( int y=0; y < bodys.length; y++ ) {
//            for ( int x=0; x<bodys2.length; x++ ) { //ここのxの初期値の与え方が違うだけ
//                Body p1 = cf.getBody( bodys[y] );
//                Body p2 = cf.getBody( bodys[x] );
//                if ( p1 == null || p2 == null ) continue;
//                for ( int i=0; i<aspectTypes.length; i++ ) {
//                    int aid = aspectTypes[i].aid;
//                    double angle = Const.ASPECT_ANGLE[ aid ];
//                    if ( isAspect(p1,p2,angle,aspectTypes[i].tightOrb) ) {
//                        double asp = Math.abs(p1.lon - p2.lon);
//                        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
//                        list.add( new Aspect( p1, p2, aid,
//                                               Aspect.TIGHT,
//                                               Math.abs( angle - asp ) ) );
//                    } else if ( isAspect( p1, p2, angle, aspectTypes[i].looseOrb ) ) {
//                        double asp = Math.abs(p1.lon - p2.lon);
//                        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
//                        list.add( new Aspect( p1, p2, aid, 
//                                               Aspect.LOOSE,
//                                               Math.abs( angle - asp ) ) );
//                    }
//                }
//            }
//        }
//        return list;
//    }
    /**
     * 二つのチャート間における天体のアスペクトを求める。
     * (たとえばネイタル図の太陽とトランジット図の太陽のアスペクトなど)。
     * @param bodys  天体リスト1。たとえばネイタル図
     * @param bodys2 天体リスト2。たとえばトランジット図
     * @param aspectTypes 認識するアスペクトの種類
     * @return 検出されたアスペクトのリスト
     */
    public static List<Aspect> getAspects( List<Body> bodys,
                                             List<Body> bodys2,
                                             AspectType [] aspectTypes) {
        List<Aspect> list = new ArrayList<Aspect>();
        for ( int y=0; y<bodys.size(); y++ ) {
            for ( int x=0; x<bodys2.size(); x++ ) {
                //ここのxの初期値の与え方が違うだけ
                Body p1 = bodys.get(y);
                Body p2 = bodys2.get(x);
                if ( p1 == null || p2 == null ) continue;
                for ( int i=0; i<aspectTypes.length; i++ ) {
                    int aid = aspectTypes[i].aid;
                    double angle = Const.ASPECT_ANGLE[ aid ];
                    if ( isAspect( p1, p2, angle, aspectTypes[i].tightOrb ) ) {
                        double asp = Math.abs( p1.lon - p2.lon );
                        //必ず180度以下の値になる
                        if ( asp > 180d ) asp = 360d - asp;
                        list.add( new Aspect( p1, p2, aid, 
                                               Aspect.TIGHT,
                                               Math.abs( angle - asp ) ) );
                    } else if ( isAspect( p1, p2, angle, aspectTypes[i].looseOrb ) ) {
                        double asp = Math.abs( p1.lon - p2.lon );
                        //必ず180度以下の値になる
                        if ( asp > 180d ) asp = 360d - asp;
                        list.add( new Aspect( p1, p2, aid,
                                               Aspect.LOOSE,
                                               Math.abs( angle - asp ) ) );
                    }
                }
            }
        }
        return list;
    }
    /**
     * 二つのチャート間における天体のアスペクトを求める。
     * (たとえばネイタル図の太陽とトランジット図の太陽のアスペクトなど)。
     * このメソッドは結果を格納するリストを引数で指定でき、複数回実行するたびに
     * 結果を一つのリストに追加していける。
     * @param bodys  天体リスト1。たとえばネイタル図
     * @param bodys2 天体リスト2。たとえばトランジット図
     * @param aspectTypes 認識するアスペクトの種類
     * @param list 結果を格納するリスト
     */
    public static void getAspects(List<Body> bodys,
        List<Body> bodys2,
        AspectType [] aspectTypes,
        List<Aspect> list) {
        for ( int y=0; y<bodys.size(); y++ ) {
            for ( int x=0; x<bodys2.size(); x++ ) { //ここのxの初期値の与え方が違うだけ
                Body p1 = bodys.get( y );
                Body p2 = bodys2.get( x );
                if ( p1 == null || p2 == null ) continue;
                for ( int i=0; i < aspectTypes.length; i++ ) {
                    int aid = aspectTypes[i].aid;
                    double angle = Const.ASPECT_ANGLE[ aid ];
                    if ( isAspect( p1, p2, angle, aspectTypes[i].tightOrb ) ) {
                        double asp = Math.abs(p1.lon - p2.lon);
                        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
                        list.add( new Aspect( p1, p2, aid,
                                               Aspect.TIGHT,
                                               Math.abs( angle - asp ) ) );
                    } else if ( isAspect( p1, p2, angle, aspectTypes[i].looseOrb ) ) {
                        double asp = Math.abs( p1.lon - p2.lon );
                        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
                        list.add( new Aspect( p1, p2, aid,
                                               Aspect.LOOSE,
                                               Math.abs( angle - asp) ) );
                    }
                }
            }
        }
    }
    /**
     * ネイタル図などのひと組の天体リストと、トランジット図などの一つの天体(太陽等)間
     * のアスペクトを検出する。検出結果は引数で渡したrsultListに入る。
     * bodyListをプログレス図の天体リストに変更して再度呼びだし、
     * 結果を同じにresultListに追加してゆくことができる。
     *
     * @param body この天体とbodyListの天体とのアスペクトが検出される。
     * もとまったAspect#p2はかならずこのbodyとなる。
     * @param bodyList ネイタルやトランジットの天体リスト
     * @param resultList 検出されたアスペクトが格納されるリスト
     * @param aspectTypes 認識するアスペクトの種類
     * @return resultListの参照アドレスがそのまま返却される。
     */
    public static List<Aspect> getAspects( List<Body> bodyList,
                                             Body body,
                                             List<Aspect> resultList,
                                             AspectType [] aspectTypes ) {
        Body p2 = body;
        for ( int y=0; y < bodyList.size(); y++ ) {
            Body p1 = bodyList.get(y);
            for ( int i=0; i < aspectTypes.length; i++ ) {
                int aid = aspectTypes[i].aid;
                double angle = Const.ASPECT_ANGLE[ aid ];
                if ( isAspect( p1, p2, angle, aspectTypes[i].tightOrb ) ) {
                    double asp = Math.abs(p1.lon - p2.lon);
                    if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
                    resultList.add( new Aspect( p1, p2, aid,
                                                 Aspect.TIGHT,
                                                 Math.abs( angle - asp ) ) );
                } else if ( isAspect( p1, p2, angle, aspectTypes[i].looseOrb ) ) {
                    double asp = Math.abs(p1.lon - p2.lon);
                    if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
                    resultList.add( new Aspect( p1, p2, aid,
                                                Aspect.LOOSE,
                                                Math.abs( angle - asp ) ) );
                }
            }
        }
        return resultList;
    }
//テストメソッド
//  public static void main(String args[]) {
//    Body p1=null,p2=null;
//    double angle=0,orb=0;
//    try {
//      p1 = new Body(0,Double.parseDouble(args[0]));
//      p2 = new Body(1,Double.parseDouble(args[1]));
//      angle = Double.parseDouble(args[2]);
//      orb = Double.parseDouble(args[3]);
//    }catch(NumberFormatException e) {
//      System.out.println(e);
//    }
//    System.out.println("p1 = " + p1);
//    System.out.println("p2 = " + p2);
//    System.out.println("angle = " + angle);
//    System.out.println("orb = " + orb);
//    if(isAspect(p1,p2,angle,orb)) {
//      System.out.println("アスペクトあり");
//    } else {
//      System.out.println("なし");
//    }
//  }
    
}

