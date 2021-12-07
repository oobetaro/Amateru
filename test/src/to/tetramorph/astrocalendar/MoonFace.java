package to.tetramorph.astrocalendar;
import java.util.Calendar;
/**
 * 日付とムーンフェイス(新月、上弦、満月、下弦)を保存するクラス。
 * エニアグラムの九相を保存したりするのにも使う。
 */
class MoonFace {
    
    private static final String [] faceNames = { "新月","上弦","満月","下弦" };
    public int face;
    public Calendar date;
    /**
     * @param date faceで指定した月相になる日。
     * @param face 0～3で月の新月、上弦、満月、下弦を表す
     */
    public MoonFace(Calendar date,int face) {
        this.date = date;
        this.face = face;
    }
    /**
     * このオブジェクトの日付と月相を文字列で返す。
     */
    public String toString() {
        return String.format("%tY/ %tm/%td %tT",date,date,date,date) ;
    }
    
    /** 24時間制で時刻文字列を返す。"0:00"～"23:59" */
    
    public String getTime() {
        return String.format("%Tk:%TM",date,date,date).replaceAll("午","");
    }
    
    public String getName() {
        return faceNames[face];
    }
}
