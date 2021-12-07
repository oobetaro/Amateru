/*
 *
 */
package to.tetramorph.starbase;

import java.sql.Date;
import java.sql.Time;
import to.tetramorph.time.JDay;

/**
 * HSQLDBのSQL文からストアドプロシージャとして呼び出すための占星術用関数群。
 * すべてstaticメソッド。このクラスはプラグイン側で使用しても問題はありませんが、
 * ユリウス日などを求めるのであればJDayを使うことをおすすめします。
 * <br>
 * SQL側では別名を付与してやると使いやすい
 * <pre>
 * CREATE ALIAS SIGN FOR "to.tetramorph.starbase.Function.getSign";
 * CREATE ALIAS ANGLE FOR "to.tetramorph.starbase.Function.getAngle";
 * CREATE ALIAS ASPECT FOR "to.tetramorph.starbase.Function.isAspect";
 * CREATE ALIAS JULDAY FOR "to.tetramorph.starbase.Function.getJDay"
 * </pre>
 */
final public class Function {
    
    // このクラスはHSQLDBから呼び出されるので、かならずpublic classでなければ
    //ならない。
    
    
    // getJDay()が呼び出されるごとにカウントアップされる変数。UPDATE中の進行状況
    private static int jdayCount = 0;
    private static final String [] SIGN =
    { "ARI","TAU","GEM","CAN","LEO","VIR",
      "LIB","SCO","SAG","CAP","AQU","PIS" };
    
    private Function() { }
    /**
     * 黄経(0-360)を入力に対して星座名を返す。360度以上の値を入れても、360度以内に
     * 丸めて計算する。nullを入力するとnullを返す。負の値を入力するとエラー。
     */
    public static String getSign(Double angle) {
        if(angle == null) return null;
        double a = angle % 360.0;
        return SIGN[(int)(a / 30.0)];
    }
    /**
     *サインの中の角度を返す。値の範囲は(0 >= 値 < 30)となる。
     *nullを入れるとnullを返す。
     */
    public static Double getAngle(Double angle) {
        if(angle == null) return null;
        return angle % 30.0;
    }
    /**
     * 二体の天体黄経p1,p2が、angleで指定されたアスペクトを形成しているか判定する
     * orbは許容誤差を指定する。各入力パラメターの１つでもnullが入ると
     * このメソッドはnullを返す。
     */
    public static Boolean isAspect( Double p1,
                                      Double p2,
                                      Double angle,
                                      Double orb ) {
        if (p1 == null || p2 == null || angle == null || orb == null) 
            return null;
        double asp = Math.abs(p1 - p2);
        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
        if ( (angle + orb) > 180 ) {
            if ( (angle - orb) <= asp) return true;
        } else {
            if ( asp >= (angle - orb) && asp <= (angle + orb) ) return true;
        }
        return false;
    }
    
    /**
     * 二つの確度を比較して、アスペクトの有無を検査する。
     * @param p1 天体1の黄経
     * @param p2 天体2の黄経
     * @param angle アスペクトの離角(60度や90度などを指定)
     * @param orb 離角の許容値(5度くらいを指定)
     */
    private static boolean isAspect( double p1,
                                        double p2,
                                        double angle,
                                        double orb ) {
        double asp = Math.abs( p1 - p2 );
        if ( asp > 180d ) asp = 360d - asp; //必ず180度以下の値になる
        //asp = asp % 360d;    //必ず180度以下の値になる
        if ( (angle + orb) > 180 ) {
            if ( (angle - orb) <= asp) return true;
        } else {
            if ( asp >= (angle - orb) && asp <= (angle + orb) ) return true;
        }
        return false;
    }
    /**
     * p0の天体に対してp1-p9までの天体とアスペクトの有無を検査して、
     * p0が他の全天体とノーアスペクトのときはtrueを、アスペクトがあるときはfalse
     * を返す。アスペクトはメジャーアスペクト(0,60,90,120,180)のみを検査する。
     * その際のオーブは引数orbで指定する。
     * p0がnullなら、このメソッドはかならずtrueを返す。非存在の位置にアスペクト
     * など発生しないから、それはノーアスである。
     * p1-p9までの天体のうちnullのものは検査の際無視する。つまり外惑星３つを
     * nullにしておけば、その枠内で計算される。
     * orbにnullを指定すると、デフォルトで4度のオーブとみなす。<pre>
     * 【実例】
     * 例えば太陽がノーアスかどうかを検査したいとき、p0には太陽黄経を指定。
     * p1からp9までは月、水星、金星、、、冥王星までの黄経を指定。
     * アスペクトを認めるオーブ角をorbで指定。
     * 【実例2】
     * 火星がノーアスかどうか、古典７惑星のシステムで検査したいときは、
     * p0に火星黄経を指定。p1-p6まで太陽、月、水星、金星、木星、土星を指定。
     * 天体の順序はどうでも良い。p7-p9まではnullを指定。
     * アスペクトを認めるオーブ角をorbで指定。</pre>
     */
    public static Boolean isNoAspect( Double p0,
                                        Double p1, Double p2, Double p3, 
                                        Double p4, Double p5, Double p6,
                                        Double p7, Double p8, Double p9,
                                        Double orb ) {
         if ( p0 == null ) return true;
         if ( orb == null ) orb = 4.;
         Double [] planets = new Double [] { p1,p2,p3,p4,p5,p6,p7,p8,p9 };
         double [] aspects = { 0.0, 60.0, 90.0, 120.0, 180.0 };
         double targetPlanet = p0.doubleValue();
         double orbAngle = orb.doubleValue();
         for ( int i = 0; i < planets.length; i++ ) { //全天体との組合せ検査
             if ( planets[i] == null ) continue;
             for ( int j = 0; j < aspects.length; j++ ) { //全座相の存在検査
                 if ( isAspect( targetPlanet, 
                                planets[i].doubleValue(), 
                                aspects[j], 
                                orbAngle ) ) {
                     return false; //一つでも座相を検出したらノーアスではない
                 }
             }
         }
         return true;
    }
    public static void main(String [] args) {
        //Boolean b = isNoAspect( 2.01,351.99,315.48, 29.34, 24.55, 47.67,206.23,104.47,203.31,141.36,4.0);
        Boolean b = isNoAspect(   2.01,315.48,351.99,29.34, 24.55, 47.67,206.23,104.47,203.31,141.36,4.0 );
        System.out.println("isNoAspect = " + b );
    }
    /**
     * ERA,日付,時刻,タイムゾーンからユリウス日を返す。
     * @param era ADまたはBCを指定。
     * @param date 日付
     * @param time 時刻
     * @param timeZone タイムゾーンID
     * @param defaultTime 時刻がnullの場合に採用する地方時(hh:mm:dd)
     */
    public static Double getJDay( String era,
                                    Date date,
                                    Time time,
                                    String timeZone,
                                    String defaultTime) {
        jdayCount++;
        if ( date == null || timeZone == null ) return null;
        String t = (time == null) ? null : time.toString();
        return new Double(
            JDay.get( era, date.toString(), t, timeZone, defaultTime ) );
    }
    /**
     * getJDay()が呼び出されるごとにカウントアップする変数に値をセットする。
     * これはデフォルトタイムの変更が起き、UPDATE文でユリウス日を更新する際に、
     * 進行状況を外から監視するために使用する。
     * UPDATE発行前にゼロをセットして、別のスレッドから値を監視する。
     * ただしこの方法はHSQLDBが同じJVM上で動いてなければstatic変数といえども
     * 絶縁されてしまうので使えない。つまり別窓でHSQLDBを起動している開発中の
     * 環境では使えない。
     */
    protected static void setJDayCounter(int value) {
        jdayCount = value;
    }
    /**
     * getJDay()が呼び出されるごとにカウントアップする変数の値を返す。
     * これはUPDATE文でユリウス日を更新する際に、進行状況を外から監視するために
     * 使用する。
     */
    protected static int getJDayCounter() {
        return jdayCount;
    }
}
