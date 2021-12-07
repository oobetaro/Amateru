/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartlib;

import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.module.DictionaryActionFile;
import to.tetramorph.starbase.util.DictionaryRequest;

/**
 * 天体やアスペクトから辞書クエリーデータを作成するツール。
 * @author 大澤義鷹
 */
public class NPTChartDictionaryTool {
    /**
     * 辞書タイプ名。内容は"NPTChart"。
     */
    public static final String DICTIONARY_TYPE = "NPTChart";
    /**
     * NPTChart用辞書アクションファイル
     */
    public static final DictionaryActionFile DICTIONARY_ACTION_FILE =
            new DictionaryActionFile(
                NPTChartDictionaryTool.class.getResource("/resources/npt_action.xml"),
                DICTIONARY_TYPE );
    private static final String [] NPT_TYPES = { "natal","progress","transit" };
    /**
     * 空のNPTChart用のリクエストインスタンスを返す。
     */
    public static DictionaryRequest createRequest() {
        return new DictionaryRequest(DICTIONARY_TYPE);
    }
    /**
     * テキスト選択時の辞書リクエストを返す。
     * ただしキャプションは設定されない。
     * @param text リクエストするテキスト
     * @param req リクエストオブジェクト。nullを指定したときは新しくインスタンス
     * を作成する。
     * @return reqにnullを指定したときは新しいインスタンスを返すが、そうではない
     * 場合は、引数で指定されたものに値をセットしてそのまま返す。
     */
    public static DictionaryRequest getRequest( String text, DictionaryRequest req ) {
        if ( req == null ) {
            req = new DictionaryRequest(DICTIONARY_TYPE);
        }
        if ( ! req.getDictType().equals(DICTIONARY_TYPE)) {
            throw new IllegalArgumentException("辞書タイプ名が違う");
        }
        req.setActionCommand("SelectedText");
        req.put("keyword",text);
        return req;
    }
    /**
     * 天体選択時の辞書リクエストを返す。
     * ただしキャプションは設定されない。Body内のIDが範囲外のときは、天体名は
     * "UnKnown"とする。
     * @param b リクエストを作成する天体
     * @param req リクエストオブジェクト。nullを指定したときは新しくインスタンス
     * を作成する。
     * @return reqにnullを指定したときは新しいインスタンスを返すが、そうではない
     * 場合は、引数で指定されたものに値をセットしてそのまま返す。
     */
    public static DictionaryRequest getRequest( Body b, DictionaryRequest req ) {
        if ( req == null ) {
            req = new DictionaryRequest(DICTIONARY_TYPE);
        }
        if ( ! req.getDictType().equals(DICTIONARY_TYPE)) {
            throw new IllegalArgumentException("辞書タイプ名が違う");
        }

        req.setActionCommand("SelectedBody");
        req.put("sign",  Const.ZODIAC_NAMES[ b.getSign() ] );
        String p = b.id < Const.SYMBOL_NAMES.length ?
            Const.SYMBOL_NAMES[ b.id ] : "UnKnown";
        req.put("planet",p);
        req.put("angle", "" + (int)b.getSignAngle());
        req.put("type",  NPT_TYPES[ b.group ] );
        req.put("isRetrograde", b.lonSpeed < 0 ? "true" : "false" );
        req.put("house", "house" + b.house );
        return req;
    }

    /**
     * アスペクト線選択時の辞書リクエストを返す。
     * ただしキャプションは設定されない。
     * Aspect内のBodyに範囲外のIDを指定している場合、その天体の名前は"UnKnown"
     * とする。
     * @param a リクエストを作成するアスペクト。
     * @param req リクエストオブジェクト。nullを指定したときは新しくインスタンス
     * を作成する。
     * @return reqにnullを指定したときは新しいインスタンスを返すが、そうではない
     * 場合は、引数で指定されたものに値をセットしてそのまま返す。
     */
    public static DictionaryRequest getRequest( Aspect a, DictionaryRequest req ) {
        if ( req == null ) {
            req = new DictionaryRequest(DICTIONARY_TYPE);
        }
        if ( ! req.getDictType().equals(DICTIONARY_TYPE)) {
            throw new IllegalArgumentException("辞書タイプ名が違う");
        }
        req.setActionCommand("SelectedAspect");
        int id1 = a.p1.id;
        int id2 = a.p2.id;
        if ( id1 > id2 ) {  //IDの値は小→大の順で並べ替える
            int temp = id2;
            id2 = id1;
            id1 = temp;
        }
        String p1 = id1 < Const.SYMBOL_NAMES.length ?
            Const.SYMBOL_NAMES[id1] : "UnKnown";
        String p2 = id2 < Const.SYMBOL_NAMES.length  ?
            Const.SYMBOL_NAMES[id2] : "UnKnown";
        req.put("planet",  p1 );
        req.put("planet2", p2 );
        req.put("type",    NPT_TYPES[ a.p1.group ] );
        req.put("type2",   NPT_TYPES[ a.p2.group ] );
        req.put("aspect",  Const.ASPECT_NAMES_EN[ a.aid ] );
        return req;
    }

}
