/*
 * TimePlace.java
 *
 * Created on 2006/12/02, 21:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package to.tetramorph.starbase.lib;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import static java.util.GregorianCalendar.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.time.JDay;

/**
 * 場所であるPlaceに「時」の要素を加え「時と場所」を表すクラス。
 * 完全なホロスコープは少なくとも時と場所が決定しなければ作成できないが、
 * このクラスのパラメターはその条件を満たす。
 * <pre>
 *  時間の与え方
 *    java.sql.DateとTimeを使う。
 * 　 TimePlace tp = new TimePlace();
 *    tp.setDate(Date.valueOf("1964-9-30"),"AD");
 *    tp.setTime(Time.valueOf("5:35:00"));
 *    //グレゴリアンカレンダーを使う方法。月を-1するのを忘れずに。
 *    GregorianCalendar gcal = new GregorianCalendar(1964,9-1,30,5,35,0);
 *    //時間を未設定にしたいときはDATE_ONLYを指定。
 *    tp.setCalendar(gcal,TimePlace.DATE_AND_TIME);
 * </pre>
 * 今のところ、コンストラクタは必要最小限のものしか用意しておらず、各パラタメー
 * はメソッドでセットすることにしている。テストの際は、現時刻でのインスタンス
 * を作るとか、デフォルトのタイムゾーンで作るとかあると便利に思えるのだが、
 * アプリの中ではあまりそういう局面は存在しないため。
 */
public class TimePlace extends Place {

    private Date date = null;
    private Time time = null;
    private int eraInt = AD;
    //日時が変更されたときはnullになる。getJDay()が呼ばれたときは値がセットされる。
    private Double jday = null;

    /**
     * 現在の日付と時刻とタイムゾーンがnullの空オブジェクトを作成
     */
    public TimePlace() {
        super();
    }

    /**
     * TimeAndPlaceからディープコピーで完全な複製を作る。
     */
    public TimePlace(TimePlace o) {
        timeZone = (TimeZone) o.getTimeZone().clone();
        if (o.getDate() != null) {
            date = (Date) o.getDate().clone();
            //サブクラスのNatalでコンポジットの複製が行われたときdateがnullなのではじく。
            eraInt = o.getCalendar().get(Calendar.ERA);
        }
        if (o.getTime() != null) {
            time = new Time(o.getTime().getTime());
        }
        if (o.getLongitude() != null) {
            longitude = (new Double(o.getLongitude()));
        }
        if (o.getLatitude() != null) {
            latitude = (new Double(o.getLatitude()));
        }
        setPlaceName(o.getPlaceName());
    }

    /**
     * 日付を返す。未登録ならnullを返す。
     * @return java.sql.Date
     */
    public Date getDate() {
        return date;
    }

    /**
     * 文字列で日付を返す。頭に半角スペースまたはBCがついた日付文字列が返る。
     * 未登録ならnullを返す。日付が未登録のケースはコンポジットデータの時以外あり
     * えない
     */
    public String getStringDate() {
        if (date == null) {
            return "";
        }
        String era = getERA().equals("BC") ? "BC" : " ";
        return era + getDate().toString();
    }

    /**
     * 時刻を返す。未登録ならnullを返す。
     * @return java.sql.Time
     */
    public Time getTime() {
        return time;
    }

    /**
     * 文字列で時刻を返す。未登録なら""を返す。時刻文字列先頭には半角スペースが
     * 一つつく。
     */
    public String getStringTime() {
        if (time != null) {
            return " " + time.toString();
        }
        return "";
    }

    /**
     * 日付をセットする。java.util.Dateではなく、java.sql.Dateであることに注意。
     * @param date java.sql.Dateの日付
     * @param era  "BC"を指定すると紀元前の日付を表す。他はADとみなされる。
     * 大文字小文字は区別しない。
     */
    public void setDate(Date date, String era) {
        eraInt = era.equalsIgnoreCase("BC") ? BC : AD;
        this.date = date;
        jday = null;
    }

    /**
     * 日付文字列と時間文字列から日時をセットする。時間文字列はnullもしくは""を
     * 指定すると、時刻は未設定に設定される。
     * 日付文字列はバースデータファイルの形式で、"BC 1200-10-17"
     * とか"AD 1964-9-30"とか、" 1964-09-30"など。日付の妥当性検査が行われ、異常な
     * 値をセットしようとするとIllegalArgumentExceptionがスローされる。
     * 異常な日付とは11月31日とかありえない日付のこと。異常な時刻とは24時とか-30分
     * などのこと。
     * CSVファイルのDATEフィールドを直接入力することができ、またその際、不正な日付
     * 入力があればはじくことができる。
     */
    public void setDate(String csvDate, String csvTime) throws IllegalArgumentException {
        String era = "";
        String date = "";
        try {
            csvDate = csvDate.trim();
            if (csvDate.toUpperCase().startsWith("BC")) {
                date = csvDate.substring(2).trim();
                era = "BC";
            } else if (csvDate.toUpperCase().startsWith("AD")) {
                date = csvDate.substring(2).trim();
                era = "AD";
            } else {
                era = "AD";
                date = csvDate.trim();
            }
            eraInt = era.equalsIgnoreCase("BC") ? BC : AD;
            String[] temp = date.split("-");
            int[] values = new int[temp.length];
            for (int i = 0; i < temp.length; i++) {
                values[i] = Integer.parseInt(temp[i]);
            }
            values[1]--; //月の位は-1して入力するお約束
            GregorianCalendar cal = new GregorianCalendar();
            cal.setLenient(false);
            cal.set(values[0], values[1], values[2]);
            cal.get(Calendar.YEAR);
            this.date = Date.valueOf(date);
            //時刻の正当性検査
            if (csvTime == null) {
                this.time = null;
            } else {
                csvTime = csvTime.trim();
            }
            if (csvTime.equals("")) {
                this.time = null;
            } else {
                temp = csvTime.split(":");
                int hour = Integer.parseInt(temp[0]);
                int minute = Integer.parseInt(temp[1]);
                float second = Float.parseFloat(temp[2]);
                if (!(hour >= 0 && hour < 24)) {
                    throw new IllegalArgumentException("hour");
                }
                if (!(minute >= 0 && minute < 60)) {
                    throw new IllegalArgumentException("minute");
                }
                if (!(second >= 0 && second < 60f)) {
                    throw new IllegalArgumentException("second");
                }
                this.time = Time.valueOf(csvTime);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        jday = null;
    }

    /**
     *紀元前、紀元後を表す文字列を返す。紀元前なら"BC",紀元後なら"AD"。
     * @return "BC"または"AD"
     */
    public String getERA() {
        return eraInt == BC ? "BC" : "AD";
    }

    /**
     * 時刻をセットする。フォーマットは"hh:mm:ss"。
     * このメソッドを呼ぶ前にかならずsetDateで日付がセットされていなければならない。
     * それでないとNullPointerExceptionが出る。
     * @param time java.sql.Timeオブジェクト
     */
    public void setTime(Time time) {
        this.time = time;
        jday = null;
    }

    /**
     * このオブジェクトが表すユリウス日を返す。時刻が設定されていない場合は、
     * 地方時における12時で計算する。この値をSwissエフェメリスに入れる。
     * チャートタイプがコンポジットの場合、このメソッドはNullPointerExceptionを出す。
     * @return ユリウス日
     */
    public double getJDay() {
        if (jday == null) {
            jday = JDay.get(getCalendar());
        }
        return jday;
    }

    /**
     * このクラスが保持している日時をカレンダーオブジェクトで返す。時刻が未設定の
     * 場合はConfig.data.getProperty("DefaultTime")の値("hh:mm:dd")をデフォルトタイムと
     * みなしてカレンダーの時刻を設定する。タイムゾーンも登録されている値が設定
     * される。
     * このメソッドが正しく実行されるためには、
     * Config.data.setProperty("DefaultTime","00:00:00")としてデフォルトタイムが事前に
     * 設定されていなければならない。時間が省略された場合の計算時刻を決めておく必要
     * がある。未設定だとIllegalStateExceptionが出る。
     *
     * @see java.util.GregorianCalendar
     * @return ｸﾞﾚｺﾞﾘｱﾝｶﾚﾝﾀﾞｰｵﾌﾞｼﾞｪｸﾄ
     */
    public GregorianCalendar getCalendar() {
        GregorianCalendar dateCal = new GregorianCalendar(timeZone);
        dateCal.set(MILLISECOND, 0); //ゼロにする習慣を！
        int[] ymd = getDateParams(date.toString());
        dateCal.set(ERA, eraInt);
        dateCal.set(ymd[0], ymd[1] - 1, ymd[2]);
        if (time != null) {
            int[] hms = getDateParams(time.toString());
            dateCal.set(HOUR_OF_DAY, hms[0]);
            dateCal.set(MINUTE, hms[1]);
            dateCal.set(SECOND, hms[2]);
        } else {
            //時刻指定がない場合はプロパティから取得
            if (System.getProperty("DefaultTime") == null) {
                throw new IllegalStateException("DefaultTimeプロパティが未設定です");
            }
            int[] hms2 = getDateParams(System.getProperty("DefaultTime"));
            dateCal.set(HOUR_OF_DAY, hms2[0]);
            dateCal.set(MINUTE, hms2[1]);
            dateCal.set(SECOND, hms2[2]);
        }
        return dateCal;
    }

    /**
     * yyyy-mm-dd,hh:mm:ssを各桁に分割して整数配列で返す。
     */
    public static int[] getDateParams(String ymd) {
        String[] temp = ymd.split(":|-");
        int[] values = new int[3];
        for (int i = 0; i < 3; i++) {
            values[i] = Integer.parseInt(temp[i]);
        }
        return values;
    }

    /**
     * このオブジェクトのフォーマットされた日付を返す。日付が未登録なら""を返す。
     * 紀元前ならBCが頭につく。時刻が設定されていないときは省略される。<br>
     * [BC ]yyyy-mm-dd [hh:mm:ss]
     * @return 整形された日付と時間の文字列
     */
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        GregorianCalendar cal = getCalendar();
        String t = "";
        if (getTime() != null) {
            t = " %tT";
        }
        String v = String.format("%tY-%tm-%td" + t, cal, cal, cal, cal);
        if (cal.get(ERA) == BC) {
            return "BC " + v;
        }
        return v;
    }

    /**
     * 日付、時間、地名、緯度、経度、タイムゾーンをセットする。
     * @param tp TimePlaceオブジェクト
     */
    public void setTimePlace(TimePlace tp) {
        boolean dateOnly = tp.getTime() == null;
        setPlace(tp.getPlace());
        setDate(tp.getDate(), tp.getERA());
        setTime(tp.getTime());
        jday = null;
    }

    /**
     * タイムゾーンをセットする
     * @param timeZone TimeZoneオブジェクト
     */
    @Override
    public void setTimeZone(TimeZone timeZone) {
        super.setTimeZone(timeZone);
        jday = null; //これをしたいためにオーバーライド
    }

    /**
     * このオブジェクトの表現を返す。
     * @return "1964-09-30 05:35:00 大阪府箕面市 JST"といった文字列
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String tmp = getFormattedDate();
        if (!tmp.equals("")) {
            sb.append(tmp).append(" ");
        }
        tmp = getPlaceName();
        if (tmp == null) {
            tmp = "";
        }
        if (!tmp.equals("")) {
            sb.append(tmp).append(" ");
        }
        if (longitude != null && latitude != null) {
            sb.append(AngleConverter.getFormattedLongitude(
                    getLongitude().toString())).append(" ");
            sb.append(AngleConverter.getFormattedLatitude(
                    getLatitude().toString())).append(" ");
        }
        if (getTimeZone() != null) {
            sb.append(getTimeZone().getID());
        }
        return sb.toString();
    }

    /**
     * DATE,ERA,TIME,PLACENAME,LATITUDE,LONGITUDE,TIMEZONEをrsから取得して、
     * このオブジェクトにセットする。ERAがNULLならば日付時間ともにnullとなる。
     * ERAは-1にセットされる。
     */
    @Override
    public void setParams(ResultSet rs) throws SQLException {
        super.setParams(rs);
        String era = rs.getString("ERA");
        if (era == null) {
            date = null;
            time = null;
            eraInt = -1;
        } else {
            Date date = rs.getDate("DATE");
            setDate(date, era);
            setTime(rs.getTime("TIME"));
        }
        jday = null;
    }
    /**
     * setCalendar()に与える引数で、Calendar情報の日付のみ採用し時刻は未設定とする。
     */
    public static final int DATE_ONLY = 0;
    /**
     * setCalendar()に与える引数で、Calendar情報の日付と時刻の両方をこのオブジェクトにセットする。
     */
    public static final int DATE_AND_TIME = 1;

    /**
     * カレンダーの値をこのオブジェクトに設定する。日付、時刻、ERA、タイムゾーン、
     * が設定される。日付だけではなくタイムゾーンも設定されることに注意。
     * @param gcal グレゴリアンカレンダーオブジェクト
     * @param mode DATE_ONLYならgcalの日付のみ設定し時刻は設定しない。
     * modeがDATE_AND_TIMEなら時刻もセットする。
     */
    public void setCalendar(GregorianCalendar gcal, int mode) {
        timeZone = gcal.getTimeZone();
        date = Date.valueOf(String.format("%tY-%tm-%td", gcal, gcal, gcal));
        eraInt = gcal.get(ERA);
        if (mode == DATE_ONLY) {
            time = null;
        } else if (mode == DATE_AND_TIME) {
            time = Time.valueOf(String.format("%tT", gcal));
        } else {
            throw new IllegalArgumentException("Unknown mode = " + mode);
        }
        jday = null;
    }

    /**
     * ユリウス日をこのオブジェクトにセットする。日付、時刻、ERA、タイムゾーン、
     * が設定される。ユリウス日からグレゴリオ暦(またはユリウス暦)への変換の際、
     * このオブジェクトにセットされているタイムゾーンが使用される。
     * タイムゾーンが未設定のときは、TimeZone.getDefault()のタイムゾーンがこの
     * オブジェクトにセットされ使用される。
     */
    public void setJDay(double jday) {
        this.jday = jday;
        if (getTimeZone() == null) {
            setTimeZone(TimeZone.getDefault());
        }
        GregorianCalendar gcal = JDay.getCalendar(jday, getTimeZone());
        date = Date.valueOf(String.format("%tY-%tm-%td", gcal, gcal, gcal));
        eraInt = gcal.get(ERA);
        time = Time.valueOf(String.format("%tT", gcal));
    }

    /**
     * 日付・時間・緯度・経度・タイムゾーンが登録されているならtrueを返す。
     * 一つでも欠けている場合はfalseを返す。
     */
    public boolean isCompleteTimePlace() {
        return (date != null && time != null && isCompletePlace());
    }
//  //setJDay()のテスト
//  public static void main(String [] args) {
//    TimePlace tp = new TimePlace();
//    GregorianCalendar gcal = new GregorianCalendar();
//    tp.setCalendar(gcal,TimePlace.DATE_AND_TIME);
//    System.out.println(tp.toString() + "jday = " + tp.getJDay());
//
//    TimePlace tp2 = new TimePlace();
//    tp2.setTimeZone(TimeZone.getDefault());
//    tp2.setJDay(tp.getJDay());
//    System.out.println(tp.toString() + "jday = " + tp.getJDay());
//
//  }
    //ユリウス日計算の最適化について
    // getJDay()は若干コストがかかる。日付や日時やタイムゾーンが変更されたときだけ
    // 値を再計算するようにしてコストはぶきたい。そこでそれらのﾊﾟﾗﾀﾒｰが変更されたら
    // 変数jdayをnullにｾｯﾄするようにして、getJDay()がコールされたとき、jdayがnull
    // ならば値を計算しjdayにセットたのち値を返すようにしている。
    // ﾊﾟﾗﾒﾀｰが変更されていないときは、jdayの値をそのまま返すようにしている。
    // nullをｾｯﾄする処理にあたってTimeZoneだけはPlaceの持ち物なので、setTimeZoneを
    // ｵｰﾊﾞｰﾗｲﾄﾞして、jday=nullを追加している。
}
