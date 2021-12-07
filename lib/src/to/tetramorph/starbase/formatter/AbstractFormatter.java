/*
 *
 */
package to.tetramorph.starbase.formatter;

import javax.swing.JFormattedTextField;

/**
 * 日付や時刻や緯度経度等を入力するTextFieldにセットするフォーマッタは
 * このクラスを継承して作成する。
 * それによって全角→半角への変換メソッドを共有できる。
 */
public abstract class AbstractFormatter 
                                extends JFormattedTextField.AbstractFormatter {
    /**
     * 文字列中の全角数字を半角に変換
     * ANK → Alphabet Numeric Kana
     */
    protected static String zenkakuToANK(String value) {
        char [] buf = value.toCharArray();
        StringBuilder sb = new StringBuilder();
        for ( int i=0; i < buf.length; i++ ) {
            if ( buf[i] >= '０' && buf[i] <= '９' ) {
                sb.append( (char)( buf[i] - '０' + '0' ) );
            } else if ( buf[i] >= 'Ａ' && buf[i] <= 'Ｚ' ) {
                sb.append( (char)( buf[i] - 'Ａ' + 'A' ) );
            } else if ( buf[i] >= 'ａ' && buf[i] <= 'ｚ' ) {
                sb.append((char)(buf[i] - 'ａ' + 'a'));
            } else if ( buf[i] == '　' ) {
                sb.append(' ');
            } else if ( buf[i] == '．' ) {
                sb.append('.'); 
            } else if ( buf[i] == '，' || buf[i] == '、' || buf[i] == '。' ) {
                sb.append(',');
            } else sb.append( buf[i] );
        }
        return sb.toString();
    }
    
    /**
     * テキストの中に括弧でかこまれた部分を検出したら除去する。
     * 1999年(平成○年)6月2日なら、1999年6月2日。
     * 1999年6月2日(木曜日)なら、1999年6月2日。
     * ただし完全なアルゴリズムではない。二重に括弧が続くとダメ(hoge(payo))など。
     */
    protected static String 括弧除去( String text ) {
        StringBuilder sb = new StringBuilder( text.length() );
        boolean mask = false;
        for ( int i=0; i < text.length(); i++ ) {
            String c = text.substring(i,i+1);
            if ( c.matches("[\\(（［\\[]") ) mask = true;
            if ( ! mask ) sb.append(c);
            if ( c.matches("[\\)）］\\]]")) mask = false;
        }
        return sb.toString();
    }
    
}
