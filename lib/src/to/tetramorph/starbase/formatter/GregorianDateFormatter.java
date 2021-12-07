/*
 *
 */
package to.tetramorph.starbase.formatter;
import javax.swing.*;
import java.text.ParseException;
import java.util.*;
import to.tetramorph.time.JDay;

/**
 * 入力は全角数字でも半角数字でもどちらでも受け付ける。<br>
 * 不正な日付(11月31日とかうるう年ではない2月29日)を入れるとエラーとみなし
 * メッセージダイアログが出て入力は消去される<p><pre>
 * "19640930"　　 8桁の数字で指定する方法。(全角でも良い。
 * "1964-9-30"　　年月日の順番で区切って指定。セパレータは数字以外ならなんでも良い。
 * "BC 500-9-30　 紀元前は先頭にBCをつけて指定。ADも指定できるがあまり意味はない。<br>
 * "紀元前 500-9-30　 BCの変わりに紀元前も可。<br>
 * "s39-09-30"　　頭にs(大小区別なし)をつけると昭和年と認識する。
 * "s390930"　　　同様。
 * "jd2382838"    ユリウス日で入力
 * "昭和４１年（１９６６）９月２４日" 括弧は無視して昭和をＧ暦に変換し認識する。
 * "1995年（平成7年）　1月17日　5時46分" 括弧や空白は無視して正しく認識する。
 * </pre>
 * 和暦は昭和しか対応していない。将来的にはもっと対応したい。厳密な切替日のデータ
 * を用意しなければならないのだが、それがまだ。
 * 余談だがJFormatedTextFieldにFormatterをセットしたとき、フィールドに対して未入力
 * の状態でgetValue()を呼び出すと、nullが返る。Formatterでnullを返さない
 * stringToValueを実装していたとしてもである。一度フォーカスが移り、再びフォーカス
 * がはなれ、未入力という場合は違うが、まったくの未入力のときはFormatterの実装に関
 * 係なくnullが返る。<br>
 * したがってFormatter側で、nullが返らない仕様にすることは無理と心得よ。getValue()
 * してnull判定してnullなら""を返すなどの処理をすること。それからJTextFieldの
 * getText()は、未入力のとき""を返す。
 * <pre>
 * 次のようにしてフォーマッタをセットする。FormatterFactoryをかます必要がある。
 * dateFTextField1.setFormatterFactory(new FormatterFactory(new GregorianDateFormatter()));
 * </pre>
 */
public class GregorianDateFormatter extends AbstractFormatter {
    GregorianCalendar cal = new GregorianCalendar();
    JFormattedTextField timeFTextField = null;
    TimeZone timeZone = null;
    
    /**
     * これで作ると、カンマで区切った時刻文字列をリレーする。
     * 時刻フィールドを指定しておくと、"1964-09-30,5:35"などと入力したとき、時刻の
     * "5:35"の部分をtimeFTextFieldに送る仕組み。まとめていれたい場合もある。
     */
    public GregorianDateFormatter( JFormattedTextField timeFTextField ) {
        this.timeFTextField = timeFTextField;
    }
    
    /**
     * 普通のコンストラクタ
     */
    public GregorianDateFormatter() { }
    
    /**
     * タイムゾーンを登録する。登録されていれば"JD xxxx"などとしてユリウス日を入力
     * できるようになる。デフォルトは未登録。
     */
    public void setTimeZone( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }
    /**
     * 日付と時間が一行に入力されているか判定し、入力されてない場合は引数を
     * のまま返す。入力されている場合は、日付と時間を分離して、時間フィールドに
     * 時間部分をセットし、日付部分だけを返す。
     */
    private String test( String text ) throws ParseException {
        if ( timeFTextField == null ) return text;
        String resultText = null;
        JFormattedTextField.AbstractFormatter formatter;
        formatter = timeFTextField.getFormatter();
        GregorianCalendar cal = null;
        int len = text.indexOf( "日" );
        
        if ( text.indexOf( "," ) >= 0 ) {
            
            String [] values = text.split( "," );
            resultText = values[0];
            cal = (GregorianCalendar) formatter.stringToValue( values[1] );
            
        } else if ( len >= 0 && ( text.length() - 1 ) > len ) {
            
            resultText = text.substring( 0, len );
            String rightText = text.substring( len + 1 );
            cal = (GregorianCalendar) formatter.stringToValue( rightText );
            
        } else {
            return text;
        }
        timeFTextField.setValue( cal );
        return resultText;
    }
    /**
     * 数字を整数に変換して返すが、前後に空白文字があっても除去して変換する。
     */
    private static int parseInt( String value ) {
        int i = 0;
        try {
            i = Integer.parseInt( value.trim() );
        } catch ( NumberFormatException e ) {
        }
        return i;
    }
    private static void dump( String [] array ) {
        if ( array == null ) {
            System.out.println("null");
            return;
        }
        for( int i=0; i<array.length; i++) {
            System.out.print( array[i] + ",");
        }
        System.out.println();
    }
    /**
     * 1965年8月30日などの文字列から半角数字以外の文字をセパレータとして分解して、
     * 数字だけにして配列で返す。
     */
    private static String [] tokens( String value ) {
        String [] array = value.split("[^0-9]");
        List<String> list = new ArrayList<String>();
        for ( int i=0; i < array.length; i++ ) {
            if ( array[i].length() >= 1 ) list.add( array[i] );
        }
        String [] result = new String[ list.size() ];
        for ( int i=0; i<list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
    static final String [][] GENGOU_TABLE = new String [][] { 
        { "平成","H" }, { "昭和","S" }, { "大正","T" }, { "明治","M" },
        { "紀元前","BC" }, { "紀元後","AD" }, { "西暦","AD" }, { "元年","1年" }
    };
    /**
     * AD,BC,和暦元号つきの日付文字列を解釈しグレゴリオ暦の年月日に変換して返す。
     */
    private static int [] parseYMD( String text ) {
        int sign = 1;
        int day, month, year, gengou_ofset;
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("H",1988);
        map.put("S",1925);
        map.put("T",1911);
        map.put("M",1867);
        int gengouOffset = 0;
        for ( int i=0; i < GENGOU_TABLE.length; i++ ) {
            text = text.replaceAll( GENGOU_TABLE[i][0],
                                    GENGOU_TABLE[i][1] );
        }
        if ( text.matches("^AD.*|^ad.*") ) {
            text = text.substring(2).trim();
        } else if ( text.matches("^BC.*|^bc.*") ) {
            sign = -1;
            text = text.substring(2).trim();
        } else if ( text.matches("^[HSTMhstm].*") ) {
            gengouOffset = map.get( text.substring(0,1) );
            text = text.substring(1).trim();
        }
        try {
                                            //1月1日は101なので最低3桁は存在する
            if ( text.matches("^[0-9]{3,8}") ) { 
                int dv = Integer.parseInt(text);
                day = dv % 100;
                dv = dv / 100;
                month = dv % 100;
                year = dv / 100;
            } else {
                String [] ymd = tokens( text );
                dump( ymd );
                year = parseInt( ymd[0] );
                month = parseInt( ymd[1] );
                day = parseInt( ymd[2] );
            }
        } catch ( NumberFormatException e ) {
            return null;
        } catch ( ArrayIndexOutOfBoundsException e ) {
            return null;
        }
        return new int [] { sign, year + gengouOffset, month, day };
    }
    /**
     * 日付の文字列表現を解釈してGregorianCalendarオブジェクトを返す。
     * MyTimeFormatterはjava.sql.Timeをやりとりするので、DateFormatterもjava.sql
     * java.sql.Dateでやりとりしたいところだが、それだと紀元前の日付を扱えない。
     * そのためGregorianCalendarでやりとりする。
     */
    public Object stringToValue( String text ) throws ParseException {
        if ( text == null ) return null;
        if ( text.trim().length() == 0 ) return null;
        text = 括弧除去( text );
        text = zenkakuToANK( text );
        text = test( text );
        int sign = 1;
        int gengou_ofset = 0;
        int year,month,day;
        //タイムゾーンが登録されていればユリウス日による入力が可能になる。
        if ( text.matches( "^JD.*|^jd.*" ) && timeZone != null ) {
            try {
                GregorianCalendar gc =
                    JDay.getCalendar( Double.parseDouble( text.substring(2) ),
                                      timeZone );
                if ( timeFTextField != null ) timeFTextField.setValue(gc);
                return gc;
            } catch ( Exception e ) {
                JOptionPane.showMessageDialog( null, "異常なユリウス日です。" );
            }
            return null;
        }
//        text = text.replaceFirst("^紀元前","BC");
//        if ( text.matches("^AD.*|^ad.*") ) {
//            text = text.substring(2).trim();
//        } else if ( text.matches("^BC.*|^bc.*") ) {
//            sign = -1;
//            text = text.substring(2).trim();
//        } else if ( text.matches("^S.*|^s.*|^昭和.*") ) {
//            sign = 1;
//            text = text.substring(1).trim();
//            gengou_ofset = 1925;
//        }
//        try {
//            if ( text.matches("^[0-9]{3,8}") ) {
//                int dv = Integer.parseInt(text);
//                day = dv % 100;
//                dv = dv/100;
//                month = dv % 100;
//                year = dv/ 100;
//            } else {
//                String [] ymd = tokens( text );
//                dump( ymd );
//                year = parseInt( ymd[0] );
//                month = parseInt( ymd[1] );
//                day = parseInt( ymd[2] );
//            }
//        } catch ( NumberFormatException e ) {
//            return null;
//        } catch ( ArrayIndexOutOfBoundsException e ) {
//            return null;
//        }
        int [] ymd = parseYMD( text );
        if ( ymd == null ) {
            showMessage( "入力エラー : \"" + text + "\"" );
            return null;
        }
        sign = ymd[0];
        year = ymd[1];
        month = ymd[2];
        day = ymd[3];
        cal.setLenient( false );
        
        int value;
        if ( year * sign <= 0 ) {
            cal.set(Calendar.ERA,GregorianCalendar.BC);
            if ( year <= 1 ) {
                year = 0;
                text = String.format( "BC %d-%02d-%02d", year + 1, month, day );
                value = ( year * 10000 + month * 100 + day );
                cal.set( 1, month - 1, day );
            } else {
                text = String.format("BC %d-%02d-%02d",year,month,day);
                value = ((year-1) * 10000 + month * 100 + day)*sign;
                cal.set(year,month-1,day);
            }
        } else {
            cal.set( Calendar.ERA, GregorianCalendar.AD );
            year += gengou_ofset;
            cal.set( year, month - 1, day );
            text = String.format( "%d-%02d-%02d", year, month, day );
            value = ( year * 10000 + month * 100 + day );
        }
        try {
            cal.get( Calendar.YEAR );
        } catch ( IllegalArgumentException e ) {
            showMessage( "そのような日付は存在しません。" );
            return null;
        }
        return cal;
    }
    private void showMessage( String msg ) {
        JOptionPane.showMessageDialog( null, msg,"日付入力エラー",
            JOptionPane.ERROR_MESSAGE );
    }
    /**
     * GregorianCalendarオブジェクトから、その文字列表現を返す。
     */
    public String valueToString( Object value ) throws ParseException {
        if ( value == null ) return "";
        cal = (GregorianCalendar)value;
        String era = "";
        int val = cal.get(Calendar.ERA);
        if ( val == GregorianCalendar.BC ) era = "BC ";
        return era + String.format( "%tY-%tm-%td", cal, cal, cal );
    }
    public static void main ( String [] args ) {
        String text = "1964() 年（昭和39年）9月30日(水)5時35分05秒";
        System.out.println( 括弧除去(text));
    }
}
