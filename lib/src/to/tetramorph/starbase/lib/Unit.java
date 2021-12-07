/*
 * Unit.java
 *
 * Created on 2007/07/16, 8:48
 *
 */

package to.tetramorph.starbase.lib;

/**
 * 60進数と10進数の相互変換を行う。
 * staticメソッド群
 * @author 大澤義鷹
 */
public class Unit {
    
    // インスタンス作成禁止
    private Unit() {
    }
    
    /**
     * 10進小数を60進数表現にして返す。
     * <pre>
     * 戻り値を表示する際の例
     *    System.out.printf("%d %d %f\n",(int)v[1],(int)v[2],v[3]);
     * </pre>
     * @param decimal 十進小数
     * @return 戻り値の[0]は符号で、正の値のときは1.0、負のときは-1.0、
     * ゼロのときは0.0。
     * [1]は度、または時の位。[2]は分、[3]は秒とその端数。
     * [0],[1],[2]はかならず整数が戻る。つまり分の位なら0-59が戻る。
     */
    public static double [] sexagesimals( double decimal ) {
        double sign = Math.signum(decimal);
        decimal = Math.abs(decimal);
        double th,tm,ts,ws;
        th=(int) decimal;			//整数部抽出→時
        ws = (decimal-th) * 3600d;	//小数部×1時間秒
        ws = ws/60d;			//分に換算
        tm = (int)ws;			//整数部抽出→分
        ws -= tm;				//小数部抽出
        ts = ws * 60d;			//×1分秒
        //ts = round(ts,3);		//s.ddxで、xを四捨五入
        //ws = ts/10000d + tm/100d + th;
        return new double[] { sign,th,tm,ts };
    }
    
    /**
     * 10進表現の浮動小数点値を60進表現の浮動小数値にして返す。
     */
    public static double sexagesimal( double decimal ) {
        double sign = Math.signum(decimal);
        decimal = Math.abs(decimal);
        double th,tm,ts,ws;
        th=(int) decimal;			//整数部抽出→時
        ws = (decimal-th) * 3600d;	//小数部×1時間秒
        ws = ws/60d;			//分に換算
        tm = (int)ws;			//整数部抽出→分
        ws -= tm;				//小数部抽出
        ts = ws * 60d;			//×1分秒
        //ts = round(ts,3);		//s.ddxで、xを四捨五入
        return ts/10000d + tm/100d + th;
    }
    
    /**
     * 60進表現の浮動小数点値を10進表現の浮動小数値にして返す。
     * たとえば10.594099という値は、10度59分40秒99を表し、それをこのメソッドで
     * 10進小数に変換すると、11.677453703703698という値になる。
     * それを再びsexagesimal()で変換すると、10.594099という値に戻る。
     * ただし場合によっては多少の誤差が発生するかもしれない。
     */
    public static double decimal( double sexagesimal ) {
        double v = sexagesimal;
        int i = (int)v;
        v = (v - i) * 100;
        int m = (int)v;
        double s = (v - m) * 100;
        return ((s/60d + m)/60d + i);
    }
    
    /**
     * 浮動小数値を指定された精度で切り捨てる
     * @param value 切り捨てを行う値
     * @param degit 小数点何位以降を切り捨てるか。2を指定すればxx.xx0000...という
     * 値になる。0ならは小数部は0になる。
     * @exception java.lang.IllegalArgumentException degitに負数を指定したとき。
     */
    public static double truncate( double value, int degit ) {
        if ( degit < 0 ) throw 
            new java.lang.IllegalArgumentException("degitの値が負数です");
        double v = (int)value;
        double m = Math.pow(10,degit);
        return (int)(value * m) / m;
    }
    /**
     * 入力された値を、0-360度の範囲に丸める。-30が入力されたら330になるし、
     * 370が入力されたら10になる。
     */
    public static double circularRound( double value ) {
        value %= 360;
        if(value < 0) value += 360;
        return value;
    }
    
    //truncate()のテスト
    public static void main(String [] args) {
        double v = 1.5932307822575922;
        System.out.println("元値 : " + v);
        System.out.println("切り捨て後 : " + Unit.truncate(v,2));
        System.out.printf("%02.2f\n",Unit.truncate(v,2));
        System.out.printf("%04d\n",23);
    }
//decimalのテスト
//  public static void main(String [] args) {
//    double v = 10.593058;
//    System.out.println("元値60進数: " + v);
//    double v2 = Unit.decimal(v);
//    System.out.println("10進数 : " + Unit.decimal(v2));
//    System.out.println("再変換 : " + Unit.sexagesimal(v2));
//  }
}
