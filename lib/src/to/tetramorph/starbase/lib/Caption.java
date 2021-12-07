/*
 * Caption.java
 *
 * Created on 2007/10/02, 10:07
 *
 */

package to.tetramorph.starbase.lib;

import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.Sabian;

/**
 * WardBalloonで表示する天体やアスペクトの短い説明文を、AspectやBodyオブジェクト
 * などから生成するstaticメソッド群。
 *
 * @author 大澤義鷹
 */
public class Caption {
    /**  インスタンス作成禁止 */
    private Caption() {
    }
    /**
     * シングルチャートにおけるアスペクトの説明文を返す。グループ名はつかない。
     * サンプル → " 火星−太陽 (60ﾟ)誤差 2ﾟ"
     */
    public static String getAspectCaption( Aspect a ) {
        return getAspectCaption( a, null );
    }
    /**
     * NPT三重円におけるアスペクトの説明文を返す。
     * 引数aが火星と太陽のスクエアのアスペクトを表していて、また火星のgroupは0、
     * 太陽のgroupは2でアスペクトの誤差が2.86で、引数groups[]が { "N","P","T" }
     * のときのサンプル → " N火星−T太陽 スクエア(90ﾟ)誤差 2ﾟ"。
     * @param a アスペクトオブジェクト
     * @param groups 各天体のグループ名を格納した配列。nullの場合、グループ名は
     * 除外される。
     */
    public static String getAspectCaption( Aspect a, String [] groups ) {
        StringBuilder sb = new StringBuilder(35);
        if(groups != null) sb.append( groups[a.p1.group] );
        sb.append( Const.PLANET_NAMES[a.p1.id] );
        sb.append("−");
        if(groups != null) sb.append( groups[a.p2.group] );
        sb.append( Const.PLANET_NAMES[a.p2.id] );
        sb.append( " " ).append( getAspectSymbolCaption(a) );
        return sb.toString();
    }
    /**
     * アスペクトシンボルの名前と角度を返す。
     * "コンジャクション (0ﾟ)"等
     * @param aid Constで定義されるアスペクトID
     */
    public static String getAspectSymbolCaption( int aid ) {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.ASPECT_NAMES[ aid ]);
        sb.append( String.format(" (%dﾟ)",(int)Const.ASPECT_ANGLE[aid]) );
//        sb.append(" (");
//        sb.append((int)Const.ASPECT_ANGLE[ aid ]);
//        sb.append("ﾟ)");
        return sb.toString();
    }
    /**
     * アスペクトシンボルの名前と角度を返す。アラビックパーツ用。
     * "コンジャンクション(0ﾟ) 誤差 1ﾟ"等。
     * @param a アスペクト
     */
    public static String getAspectSymbolCaption( Aspect a ) {
        String tl = a.tight ? "タイト" : "ルーズ";
        return getAspectSymbolCaption(a.aid)
                + String.format(" 誤差 %2.1fﾟ %s",Unit.truncate(a.error, 1),tl );
    }
    /**
     * シングルチャートにおける天体の内容の説明文(日本語)を返す。
     * サンプル → "1室 火星 水瓶12.04 逆行"
     */
    public static String getBodyCaption( Body b ) {
        return getBodyCaption(b,null);
    }
    /**
     * NPT三重円における天体の説明文を返す。
     * 天体IDが範囲外のとき、天体名は""とする。
     * サンプル → "1室 N火星 水瓶12.04 逆行"
     * @param groups {"N","P","T"}などを指定する。
     */
    public static String getBodyCaption( Body b,String [] groups ) {
        String rev = ( b.lonSpeed < 0 ) ? "逆行" : "";
        String house = ( b.house >= 1) ? b.house + "室" : "";
        String group = (groups == null) ? "" : groups[b.group];
        String name = b.id < Const.PLANET_NAMES.length ? Const.PLANET_NAMES[b.id] : "";
        return String.format("%s %s%s %s %s %s",
            house,
            group,
            name,
            Const.SIGN_NAMES[b.getSign()],
            AngleConverter.formatSignAngle(b.getSignAngle(),2),
            rev);
    }
    /**
     * 吹き出し用にHTML書式のサビアンメッセージを返す。<br>
     * 星座名・度数が文頭につき、その後メッセージが続く。<br>
     * 英語モードのときは英語の星座名がつく。<br>
     * @param p 天体位置が入っている感受点オブジェクト
     * @param lang  Sabian.JPまたはSabian.ENで日本語/英語の切替
     * @param groups Body#groupに応じてN,P,T等の識別文字を配列で指定する。
     */
    public static String getSabianCaption( Body p,
                                             int lang,
                                             String [] groups ) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
        .append( Caption.getBodyCaption( p, groups ) )
        .append( "</b><br><div style='width:200'>")
        .append( Sabian.getText( (int)p.lon, lang) )
        .append( "</div></html>" );
        return sb.toString();
    }
    /**
     * 吹き出し用にHTML書式のサビアンメッセージを返す。<br>
     * 星座名・度数が文頭につき、その後メッセージが続く。<br>
     * 英語モードのときは英語の星座名がつく。<br>
     * @param p 天体位置が入っている感受点オブジェクト
     * @param lang  Sabian.JPまたはSabian.ENで日本語/英語の切替
     */
    public static String getSabianCaption(Body p,int lang) {
        return getSabianCaption(p,lang,null);
    }
}
