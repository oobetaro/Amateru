/*
 *
 */
package to.tetramorph.starbase.formatter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * 入力は全角数字でも半角数字でもどちらでも受け付ける。
 * <pre>
 * 11          → 11:00:00
 * 5:35        → 05:35:00
 * 5 35 25     → 05:35:25  区切り記号は数字以外ならなんでもよい。ｽﾍﾟｰｽ,ｶﾝﾏ,ﾋﾟﾘｵﾄﾞ等。
 * 5時35分25秒 → 05:35:25に整形してくれる。
 * pm 5:35     → 17:35:00
 * 5:35 pm     → 17:35:00 amやpmは前でも後ろでもどちらでもかまわない。
 * </pre>
 * 24時以上の値を指定すると、エラーとなり入力は無効になり空白となる。<br>
 *
 * メソッドの動作についはMyDateFormatterを見よ。原理は同じ。
 * @see GregorianDateFormatter
 * setValue/getValueであたえるオブジェクトは最初java.sql.Timeだったが、GregorianCalendarに変更。
 * MyDateFormatterと同じものを使えるようにするため。
 */
public class TimeFormatter extends AbstractFormatter {
  GregorianCalendar cal = new GregorianCalendar();
  /**
   * java.sql.Timeの規約に基づく時刻文字列に対して、GregorianCalendarを返す。
   */
  public Object stringToValue(String text) throws ParseException {
    if(text == null) return null;
    if(text.trim().length()==0) return null;
    text = zenkakuToANK(text);
    boolean pm = false;
    if(text.matches(".*(AM|am).*")) {
      text = text.replaceAll("AM|am","");
    } else if(text.matches(".*(PM|pm).*")) {
      text = text.replaceAll("PM|pm","");
      pm = true;
    }
    text = text.trim();
    if(text.matches("[0-9]{3,4}")) {
      int t = Integer.parseInt(text);
      text = (t/100) + ":" + (t % 100);
    }else if(text.matches("[0-9]{5,6}")) {
      int t = Integer.parseInt(text);
      int s = t % 100;
      t = t /100;
      text = (t/100) + ":" + (t % 100) + ":" + s;
    }
    
    //System.out.println("text = "+text);
    char [] buf = text.toCharArray();
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<buf.length; i++) {
      if(buf[i] >= '0' && buf[i] <= '9') sb.append(buf[i]);
      else sb.append(':');
    }
    String [] values = sb.toString().split(":");
    int [] time = new int[3];
    try {
      for(int i=0; i<values.length; i++) {
        time[i] = Integer.parseInt(values[i]);
      }
    }catch(NumberFormatException e) {
      return null;
    }
    if(time[0] < 12 && pm) time[0] += 12;
    if(time[0] < 24 && time [1] < 60 && time[2] < 60) {
      //String result = String.format("%02d:%02d:%02d",time[0],time[1],time[2]);
      //GregorianCalendar cal = new GregorianCalendar(1970,0,1,time[0],time[1],time[2]);
      //return new Time(cal.getTimeInMillis());
      cal.set(Calendar.HOUR_OF_DAY,time[0]);
      cal.set(Calendar.MINUTE,time[1]);
      cal.set(Calendar.SECOND,time[2]);
      return cal;
    }
    return null;
  }
  /**
   * 
   */
  public String valueToString(Object value) throws ParseException {
    if(value == null) return null;
    cal = (GregorianCalendar)value;
    return String.format("%tT",cal);
//    Time time = (Time)value;
//    return time.toString();
  }
  
}
