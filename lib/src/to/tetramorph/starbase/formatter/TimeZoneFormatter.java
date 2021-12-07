/*
 *
 */
package to.tetramorph.starbase.formatter;
import javax.swing.*;
import java.text.ParseException;
import java.util.*;
/**
 * テキストフィールドに入力されたタイムゾーンIDをフォーマットする。
 * タイムゾーンIDというのは、java.util.TimeZoneで指定するIDのこと。
 * メソッドの動作についはGregorianDateFormatterを見よ。原理は同じ。
 * @see GregorianDateFormatter
 * @see java.util.TimeZone
 */
public class TimeZoneFormatter extends AbstractFormatter {
    TimeZone z = TimeZone.getDefault();
    /**
     * @param zoneid TimeZone IDを与える。
     */
    public Object stringToValue(String zoneid) 
                                             throws IllegalArgumentException {
        if(zoneid == null) return null;
        z = TimeZone.getTimeZone(zoneid);
        return z;
    }
    /**
     * @param value TimeZoneオブジェクトを与える
     */
    public String valueToString(Object value)
                                             throws IllegalArgumentException {
        if(value == null) return null;
        return ((TimeZone)value).getID();
    }
}
