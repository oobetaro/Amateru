/*
 * StringReplacer.java
 * Created on 2011/08/06, 14:08:19.
 */
package to.tetramorph.util;

/**
 * 文字列置換ツール。
 * staticメソッドで文字列置換を行うものと、オブジェクトを作成して連続置換を
 * 行う方法と二種類用意している。
 * @author 大澤義孝
 */
public class StringReplacer {

    StringBuilder buf;
    /**
     * オブジェクトを作成する。連続置換を行うときに使用する。
     * @param text 置換をほどこす文字列
     */
    public StringReplacer(String text) {
        buf = new StringBuilder(text);
    }
    /**
     * このオブジェクトが持つ文字列中からtarget文字列を探し、それをreplacementに
     * 置換する。
     * @param target 置換のターゲットとなる文字列
     * @param replacement
     */
    public void replace(String target,String replacement) {
        int p = 0, e = 0,len = target.length();
        String text = buf.toString();
        buf = new StringBuilder( text.length() * 2 );
        while ( ( p = text.indexOf( target, e ) ) >= 0 ) {
            buf.append( text.substring( e, p ) );
            buf.append( replacement );
            e = p + len;
        }
        buf.append( text.substring(e));
    }

    /**
     * このオブジェクトが持つ文字列中からtarget文字列を探し、それをreplacementに
     * 置換する。
     * @param target 置換のターゲットとなる文字列
     * @param replacement
     */
    public void replace(String target,StringBuilder replacement ) {
        replace( target, replacement.toString() );
    }

    /**
     * このオブジェクトが持つ文字列を返す。(置換後の文字列を返す)
     */
    @Override
    public String toString() {
        return buf.toString();
    }

    /**
     * 文字列を置換する。text中のtargetすべてをreplacementに置換する。
     * String#replaceAll()は、正規表現による置き換えだが、正規表現ゆえに特定の
     * 文字が入っていると置換に失敗することがおきる。このメソッドはごく単純な
     * 置換なので、そのような問題が生じることがない。StringBuilderを使っている
     * ので比較的高速。すくなくとも正規表現の置換よりはずっと速い。
     */

    public static String replace( String text, String target, String replacement) {
        if ( text == null || target == null || replacement == null )
            return text;
        if ( text.length() == 0 || target.length() == 0 )
            return text;

        int p = 0, e = 0,len = target.length();
        StringBuilder sb = new StringBuilder( text.length() * 2 );

        while ( ( p = text.indexOf( target, e ) ) >= 0 ) {
            sb.append( text.substring( e, p ) );
            sb.append( replacement );
            e = p + len;
        }
        sb.append( text.substring(e));
        return sb.toString();
    }

//    public static void main(String [] args ) {
//        StringReplacer su = new StringReplacer("Test %a %b %c");
//        su.replace("%a", "Hoge");
//        su.replace( "%b", "Payo");
//        su.replace("%c", "Fuga");
//        System.out.println(su);
//    }
}
