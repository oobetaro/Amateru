/*
 *
 */
package to.tetramorph.starbase.formatter;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.util.AngleConverter;

/**
 * JFormattedTextFieldに使用する緯度・経度入力用フォーマッタ。
 * <pre>
 * "+35.555" 北緯・東経  (+はなくなても良い。)
 * "-35.555"　南緯・西経
 * "35.55N"  北緯
 * "35.55S"  南緯
 * "135.50E" 東経 E,Wで符号とななす。e,wでも可。頭でも後ろでも可。
 * "135.50W" 西経
 * "北緯41度46.7分、東経144度4.7分"  これも認識する。
 * 60進数入力にも対応。
 * "35 31 10.22N"  35度31分10秒22とみなして、10進表現に変換。+-NEWSの規則は同じ。
 * "35 30"         35度30分とみなして10進数表現に変換。
 * "緯度 35度39分32.166秒(35.658935), 経度 139度44分43.543秒(139.745429)"
 *                 Geocodingの書式を認識する。括弧内は読み飛ばす
 *                 漢字入力に限っては、緯度,経度を経度,緯度と入力しても良いし、
 *                 カンマも省略できる。
 * </pre>
 *
 * 全角で入力しても認識される。<br>
 * 緯度は90度以上、経度は180度以上の値を入力するとエラーが表示される。<br>
 * TextFieldに表示されるのは十進表現で小数点以下6位まで。<br>
 * 六十進表現の場合は 度、分、秒.000まで。<br>
 * <p>
 * System.getProperty("app.topounit")を参照して、nullまたは"60"以外なら10進表記
 * モード、"60"なら60進表記モードで動作する。<br>
 * </p>
 * メソッドの動作についはGregorianDateFormatterを見よ。原理は同じ。
 * @see GregorianDateFormatter
 *
 */
public class TopoFormatter extends AbstractFormatter {
    public static final int LONGITUDE = 0;
    public static final int LATITUDE = 1;
    private int type;
    private JFormattedTextField lonFTextField = null;
    
    /**
     * @param type 緯度入力用(LONGITUDE)か経度入力用(LATITUDE)かを指定する。
     */
    public TopoFormatter( int type ) {
        super();
        this.type = type;
    }
    
    /**
     * 経度の入力のフォームをあらかじめ登録しておくと、カンマで緯度,経度と入力
     * したとき、経度の入力をリレーしてくれる。
     */
    public TopoFormatter( int type, JFormattedTextField lonFTextField ) {
        this(type);
        this.lonFTextField = lonFTextField;
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
    private String parse2( String text ) {
        String [] keys = { "東経","西経","北緯","南緯","緯度","経度" };
        String [] keys2 = { "E","W","N","S","","" };
        for ( int i=0; i < keys.length; i++ ) {
            text = text.replaceAll( keys[i], keys2[i] );
        }
        text = text.replaceAll("[度分秒]"," ");
        return text.trim();
    }
    /**
     * 漢字で北緯、東経と二値がはいったとき、二つの値を分割して返す。
     * 一つしかないときや漢字じゃないときはそのまま引数を返す。
     */
    private String [] split( String text ) {
        String [] array = text.split(",");
        //うまくカンマで切れるときはその値を返す
        if ( array.length >= 2 ) return array;
        // 問題はスペースで区切られている場合
        String [] keys = { "東経","西経","北緯","南緯","緯度","経度" };
        for ( String key : keys ) {
            text = text.replaceAll( key, "#" + key );
        }
        text = text.replaceFirst("^#","");
        System.out.println( "### text = " + text );
        String [] pos = text.split("#");
        if ( pos.length == 2 ) return pos;
        return new String [] { text };
    }
    /**
     * 北緯30度50分など漢字で入力が入ったときに適切な形に変換する。
     */
    private String parser( String text ) {
        String [] pos = split( text );
        if ( pos.length < 2 ) {
            return parse2( text );
        }
        int p1 = text.indexOf("緯");
        int p2 = text.indexOf("経");
        if ( p1 >= 0 && p2 >= 0 && p1 > p2 ) { 
            //経度・緯度の順で入力された時は順位を入替
            String temp = pos[0];
            pos[0] = pos[1];
            pos[1] = temp;
        }
        pos[0] = parse2( pos[0] );
        pos[1] = parse2( pos[1] );
        return pos[0] + "," + pos[1];        
    }
    /**
     * 文字列を受け取り、それをDoubleオブジェクトに変換して返す。
     * 60進数表現の文字列はDoubleに変換されたとき10進に変換されている。
     */
    public Object stringToValue( String text ) throws ParseException {
        if ( text == null ) return null;
        if ( text.trim().length()==0 ) return null;
        String inputString = text;
        double sign = 1d;
        text = 括弧除去(text);
        text = zenkakuToANK(text);
        text = parser( text );
        //リレー先のフィールドがセットされていて、二つの値を入力されたときは、
        //リレー先に二つめの値を転送する。
        if ( lonFTextField != null && text.indexOf(",") >= 0 ) {
            String [] values = text.split(",");
            text = values[0];
            lonFTextField.setValue(
                (Double)lonFTextField.getFormatter().stringToValue(values[1]));
        }
        //符号や記号を読み取り符号フラグをセットしたのち記号を除去
        if ( text.matches(".*[ENen\\+].*") ) {
            text = text.replaceAll("[ENen\\+]","");
        } else if ( text.matches(".*[WSws\\-].*") ) {
            text = text.replaceAll("[WSws\\-]","");
            sign = -1d;
        }
        text = text.trim();
//        System.out.println("text = " + text);
        String [] values = text.split("[^0-9\\.]");
//        dump(values);
//        System.out.println("value.length = " + values.length );
        double value = 0;
        try {
            if ( values.length == 0 ) {
                throw new NumberFormatException();
            } else if ( values.length == 1 ) {
                //System.out.println("デシマル値と認識");
                value = Double.parseDouble(values[0]);
            } else {
                System.out.println("60進数と認識");
                values = text.split("( * )"); //連続する空白をセパレータとして分割
//                for(int i=0; i<values.length; i++) {
//                    System.out.println(i + " : " + values[i]);
//                }
//                System.out.println("--------------------");
                value = Double.parseDouble(values[0]);
                if ( Double.parseDouble(values[1]) >= 60 )
                    throw new NumberFormatException();
                value += (Double.parseDouble(values[1]) / 60d);
                if ( values.length >= 3 ) {
                    if ( Double.parseDouble(values[2]) >= 60 )
                        throw new NumberFormatException();
                    value += (Double.parseDouble(values[2]) / 60d / 60d);
                }
            }
            //System.out.println("value = "+value + ", sign = " + sign);
            if ( type == LATITUDE && value > 90d ) {
                throw new NumberFormatException("");
            } else if ( type == LONGITUDE && value > 180d ) {
                throw new NumberFormatException("");
            }
        } catch ( NumberFormatException e ) {
            showMessage(inputString);
            return null;
        }
        return new Double( value * sign);
    }
    
    /**
     * エラーメッセージをダイアログで通知する。
     */
    private void showMessage( String value ) {
        JOptionPane.showMessageDialog(null,"\"" + value+ "\"は異常な値です。",
            "緯度・経度の入力",JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Doubleで与えられたオブジェクトを整形された文字列に変換して返す。
     * System.getProperty("app.topounit")が"60"のときは60進表現の文字列を返す。
     * それ以外の場合は10進表現の文字列を返す。
     */
    public String valueToString( Object value ) throws ParseException {
        if ( value == null ) return null;
        Double dValue = (Double)value;
        String unit = System.getProperty("app.topounit");
        if ( unit == null || (! unit.equals("60")) ) {
            return decimal(dValue);
        }
        return sexagesimal(dValue);
    }
    
    // stringToValueのときは、文字列の10進/60進を識別して10進の数値に変換する。
    // valueToStringのときは、10進の数値を10進/60進のモードに応じて、数字列に
    // 変換する。
    
    /**
     * 入力数値を10進表現の文字列で返す。
     */
    private String decimal( Double dValue ) {
        String flag = "";
        if ( type == LONGITUDE ) {
            if ( dValue > 180d || dValue < -180d ) return null;
            flag = (dValue >= 0) ? "E" : "W";
        } else if( type == LATITUDE ) {
            if ( dValue > 90d || dValue < - 90d ) return null;
            flag = (dValue >= 0)  ? "N" : "S";
        } else return null;
        dValue = Math.abs(dValue);
        return String.format("%3.6f%s",dValue,flag);        
    }
    
    /**
     * 入力数値を60進数表現の文字列で返す。
     */
    private String sexagesimal( Double dValue ) {
        String flag;
        if ( type == LONGITUDE ) {
            if ( dValue > 180d || dValue < -180d ) return null;
            flag = (dValue >= 0) ? "E" : "W";
        } else if ( type == LATITUDE ) {
            if ( dValue > 90d || dValue < - 90d ) return null;
            flag = (dValue >= 0)  ? "N" : "S";
        } else return null;
        double [] v = AngleConverter.sexagesimal( dValue.doubleValue() );
        return String.format("%d  %02d  %2.3f%s\n",(int)v[1],(int)v[2],v[3],flag);
    }
    public static void main ( String [] args ) {
        String text = "35 36 ";
        String [] values = text.split("[^0-9\\.]");
        dump( values );
    }
}
