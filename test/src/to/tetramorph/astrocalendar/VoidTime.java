package to.tetramorph.astrocalendar;
import java.util.Calendar;
import java.util.Locale;
/**
 * ボイドタイムを表現するクラスで、ボイド開始と終了時刻、
 * 終了したときの月のサイン(イングレスサイン)からなる。
 */
class VoidTime {
  int ingSign;
  Calendar begin;
  Calendar end;
  /**
   * ボイドタイムを表現するオブジェクトを作成する。
   * @param begin ボイド開始時刻
   * @param end ボイド終了時刻
   * @param ingSign イングレスサイン番号(0-11)
   */
  public VoidTime(Calendar begin,Calendar end,int ingSign) {
    this.begin = begin;
    this.end = end;
    this.ingSign = ingSign;
  }
  /** ボイドの開始時刻と終了時刻、イングレスサイン番号文字列を返す。*/
  public String toString() {
    return String.format("%tY/%tm/%td %tT",begin,begin,begin,begin) + "  "
      + String.format("%tY/%tm/%td %tT",end,end,end,end)
      + "  " + ingSign;
  }
  /**
   * 24時間制でボイド開始時刻の文字列を返す。"0:00"～"23:59"といった表現
   */
  public String getBeginTime() {
    return String.format("%Tk:%TM",begin,begin,begin);
  }
  
  /**
   * 24時間制でボイド終了時刻の文字列を返す。"0:00"～"23:59"といった表現
   */
  public String getEndTime() {
    return String.format("%Tk:%TM",end,end,end);
  }
  /**
   * イングレスサインの前のサイン番号(0-11)を返す。
   */
  public int getPrevSign() {
    int temp = ingSign -1;
    if(temp < 0) temp = 11;
    return temp;
  }
}