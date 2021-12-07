/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/25 01:00
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 文字列中の改行"\n"とイコール"="をエスケープ／アンエスケープ、およびsplitする。
 * '='をエスケープするとは、"\="というように、前に"\"を挿入すること。<br>
 * "\n"なら"\\n"とする。"\"の文字がエスケープされるときは"\\"となる。
 * このようにしてエスケープされていない"=","\n"と区別がつくようにする。<br>
 * splitとはエスケープされた文字列中から、エスケープされていない"\n","="を
 * 見つけて、それをセパレータとしてトークンに分解する。<br>
 * <br>
 * セパレータが"="としたとき、<br>
 * "="という文字列をsplitすると、"",""という二つのトークンに分かれる。<br>
 * "=Hoge"なら"","Hoge"に分かれる。<br>
 * "Hoge="なら"Hoge",""に分かれる。<br>
 * "Hoge=Payo"なら"Hoge","Payo"に分かれる。<br>
 * ""なら、""のまま。<br>
 * "Hoge"なら"Hoge"のまま。<br>
 * <br>
 * 　このクラスはDBのSPECIFIC_PROPERTIES表などにプロパティを書き出すために書かれた。
 * 最初は一行で表現できるプロパティを保管することしか想定していなかったが、
 * 長い設定ソースコードなどを保管する必要が出たためエスケープが必要になった。<br>
 * 　最初、プロパティの保管は、次のようにキーと値を連結して一本の文字列にして、
 * それを表に登録していた。
 * <pre>
 * key1=value1\n
 * key2=value2\n
 * </pre>
 * 　DBから上記文字列を取り出したら、まず'\n'でsplitして各行に分割。
 * さらに'='でsplitしてキーと値に分割し、Propertiesにセットしていた。<br>
 * 　ところが、この方法だと、valueやkeyに改行やイコールが含まれていた場合、
 * あとから元のプロパティに復元するときに困る。'\n'で分割しても、それは長い
 * 改行入りテキスト文字列の途中で分割してしまうことも起きる。<br>
 * だから双方の文字列に含まれるイコールと改行コードをエスケープし、
 * 真の'='と'\n'と区別がつくようにする。<br>
 * @author 大澤義鷹
 */
public class StringEscape {


    /**
     * エスケープ処理をする。
     * "\n","="を"\\\n","\="に置換する。
     */
    public static String escape( String str ) {
        char [] buf = str.toCharArray();
        char [] escbuf = new char[buf.length];
        for ( int i = 0; i < buf.length; i++) {
            char c = buf[i];
            if ( c == '\n' || c == '=' || c == '\\' ) {
                escbuf[i] = '\\';
            }
        }
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < buf.length; i++ ) {
            if ( escbuf[i] != '\0' )
                sb.append( '\\' );
            sb.append( buf[i] );
        }
        return sb.toString();
    }

//    正規表現を使う事もできるが、円記号をたくさん並べることになりわかりにくい。
//    またforで回す方法と比べて処理速度も５倍近く遅くなる。
//    public static String escape( String str ) {
//        if ( str.indexOf('\n') >= 0 )
//            str = str.replaceAll("\\n", "\\\\\n");
//        if ( str.indexOf('=') >= 0 )
//            str = str.replaceAll("\\=","\\\\=");
//        return str;
//    }

    /**
     * escape()でエスケープしたものを元に戻して返す。
     */
    public static String unescape( String escstr ) {
        char[] escbuf = escstr.concat(".").toCharArray();
        char[] buf = new char[escbuf.length];
        int len = escstr.length();
        int j = 0;
        for ( int i = 0; i < len; j++ ) {
            if ( escbuf[i] == '\\' ) {
                boolean found = false;
                for ( char c : new char[]{ '\n', '\\', '=' } ) {
                    if ( escbuf[ i + 1 ] == c ) {
                        buf[j] = c;
                        i += 2;
                        found = true;
                        break;
                    }
                }
                if ( ! found ) buf[j] = escbuf[ i++ ];
            } else {
                buf[j] = escbuf[ i++ ];
            }
        }
        return new String( buf, 0, j );
    }
//    11倍遅い
//    public static String unescape( String escstr ) {
//        escstr = escstr.replaceAll("\\\\\n", "\\\n");
//        return escstr.replaceAll("\\\\=", "\\=");
//    }

    /**
     * エスケープされた文字列中から、指定のコードをセパレータとしてトークンに
     * 分割して返す。
     * @param escstr エスケープされた文字列
     * @param c セパレータコード。基本的に'\n'または'='を指定する。
     * @return トークンに分解されたエスケープ文字列。
     */
    private static List<String> split ( String escstr, char c ) {
        //マイナス方向に最大３つ分、参照する事があるので半角スペースを３つつける。
        char [] buf = "   ".concat(escstr).toCharArray();
        int i = 3;
        int j = i;
        List<String> list = new ArrayList<String>();
        for ( ; i < buf.length; i++ ) {
            if ( buf[i] == c ) {
                // たとえば'hoge=payo'から'='を見つけたとき'oge'を取り出す。
                char x = buf[i-1];
                char y = buf[i-2];
                char z = buf[i-3];
                if ( x != '\\' || ( x == '\\' && y == '\\' && z != '\\') ) {
                    String token = new String( buf, j, i - j );
                    list.add(token);
                    j = i + 1; //セパレータの次の位置に合わせる
                }
            }
        }
        list.add( new String( buf,j, i - j ));
        return list;
    }
    // 文字列を先頭から一文字ずつチェックしていきセパレータをみつけたとする。
    // それはエスケープされたセパレータかもしれないし、そうではないかもしれない。
    // どちらかを判定したい。セパレータコードを見つけたら、3キャラ遡り検査する。
    // ここで△は、エスケープ識別コード'\'以外の文字を表す。
    // この説明ではセパレータは'='とする。
    //
    //　 △=      非エスケープ  セパレータの一つ前が'\'ではないから非エスケープ。
    //    \=      エスケープ    一つ前が'\'ならエスケープされた'='だ。
    // △\\=      非エスケープ  エスケープされた'\'、続いて非エスケープの'='だ。
    //  \\\=      エスケープ     エスケープされた'\'、エスケープされた'='だ。
    //
    // 非エスケープのパターンだけ検出すれば判別できる。
    /**
     * エスケープされた文字列中からエスケープされていない'='キャラをみつけて、
     * それをセパレータとして文字列をトークンに分解して返す。
     * @param escstr escape()を使ってエスケープされた文字列。
     * @return トークンに分解された文字列のリスト。
     */
    public static List<String> equalSplit(String escstr ) {
        return split(escstr,'=');
    }

    /**
     * エスケープされた文字列中からエスケープされていない'\n'キャラをみつけて、
     * それをセパレータとして文字列をトークンに分解して返す。
     * @param escstr escape()を使ってエスケープされた文字列。
     * @return トークンに分解された文字列のリスト。
     */
    public static List<String> enterSplit(String escstr) {
        return split(escstr,'\n');
    }

//    public static void main( String args[] ) {
//        String a = escape( "ＰＡＹＯ" );
//        String b = escape( "ＨＯＧＥ" );
//        //String c = a + "=" + b + "=" + b + "==";
//        String c = "Hoge";
//        System.out.println("a :" + a);
//        System.out.println("b :" + b);
//        System.out.println("c :" + c);
//        List<String> list = split(c,'=');
//        for ( int i=0; i < list.size(); i++) {
//            System.out.println(i + ":" + unescape(list.get(i)));
//        }
//    }

}
