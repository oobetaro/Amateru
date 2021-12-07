/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * プラグインモジュールが送ってくる辞書アクションフラグはsun,mon等の略語だが、
 * それをユーザ用の自然な言葉に変換する。アクション一個分の変換テーブルをこの
 * オブジェクトが担当する。
 * @author 大澤義鷹
 */
class ActionTranslator {
    Map<String,String> map = new LinkedHashMap<String,String>();

    /**
     * オブジェクトを作成する。指定された変換表をハッシュマップに格納。
     * 文字列中の改行や空白は除去される。
     * 変換表のフォーマットは次のようにカンマとコロンで区切られている。
     *
     * <pre>
     * planet:天体,sun:太陽,mon:月,mer:水星
     * </pre>
     * @param table 変換テーブル文字列
     */
    public ActionTranslator( String table ) {
        table = table.replaceAll("(\r|\n|\\s)", "");
        String [] tokens = table.split(",");
        for ( String token : tokens ) {
            String [] kv = token.split(":");
            if ( kv.length == 2 )
                map.put( kv[0].trim(), kv[1].trim() );
        }
    }

    /**
     * 問い合わされたキーに対して、翻訳された名前を返す。該当する名前がないとき
     * はそのまま入力されたキーを返す。
     */
    public String get( String key ) {
        String v = map.get(key);
        if ( v == null ) return key;
        return v;
    }

    /**
     * マップの内容を文字列表現して返す。
     * "key:value,,,,"というフォーマットで4つごとに改行を入れる。
     * @param map
     * @return
     */
    private static String textValues( Map<String,String> map ) {
        if ( map.size() == 0 ) return "";
        StringBuilder sb = new StringBuilder();
        int cols = 0;
        for ( Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            String value = map.get( key );
            sb.append(key);
            sb.append(":");
            sb.append(value);
            sb.append(",");
            if ( cols++ >= 4 ) {
                sb.append("\n");
                cols = 0;
            }
        }
        //末尾のカンマや改行を落としてから返す。
        return sb.toString().replaceAll( "(,|\\s)$", "");
    }

    public String getTextValues() {
        return textValues(map);
    }
//    public static void main( String [] args ) {
//        String text="hoge,hoge,\n";
//        System.out.println( text.replaceAll("(,|\\s)$", "") + "###");
//    }
}
