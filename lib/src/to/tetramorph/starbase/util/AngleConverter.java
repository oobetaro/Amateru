/*
 * AngleConverter.java
 *
 * Created on 2007/06/16, 1:33
 *
 */

package to.tetramorph.starbase.util;

import to.tetramorph.starbase.lib.Unit;

/**
 * 天体位置や観測地の緯度経度を文字列表現に変換するstaticメソッド群。
 * @author 大澤義鷹
 */
public class AngleConverter {
    /** 
     * 10進表記を表す定数 
     */
    public static final int DECIMAL = 0;
    /** 
     * 60進表記を表す定数 
     */
    public static final int SEXAGESIMAL = 1;
    
    
    /** シングルトンクラス*/
    private AngleConverter() {
    };
    /**
     * 星座度数の値 ( 0 >= x < 30) を指定小数点で「切り捨て」て文字列で返す。
     * つまりvalue=23.3999 のときdegits=2で切り捨てると23.39。
     * @param value 天体度数
     * @param degits 小数点何位で切り捨てるか(0〜4)
     */
    public static String formatSignAngle(double value,int degits) {
        String sv = String.format("%f%05d",value,0); //確実に10進数表記にする
        int i = (degits > 0) ? 3 + degits : 2;
        if(degits > 0) sv = sv.substring(0,sv.indexOf(".") + degits + 1);
        else sv = sv.substring(0,sv.indexOf("."));
        return String.format("%"+i+"s",sv);
    }
    /**
     * 配列で与えられた星座度数の値 ( 0 >= x < 30) を指定小数点で「切り捨て」て
     * 文字列で返す。つまりvalue=23.3999 のときdegits=2で切り捨てると23.39。
     * @param values 天体度数(複数)
     * @param degits 小数点何位で切り捨てるか(0〜4)
     */
    public static String [] formatSignAngles(double [] values,int degits) {
        String [] temp = new String[values.length];
        for(int i=0; i < values.length; i++) {
            temp[i] = formatSignAngle(values[i] % 30d,degits);
        }
        return temp;
    }
    /**
     * 浮動小数を指定桁数で切り捨て、左詰めに整形して返す。
     * たとえば小数点2位で整形した場合次のようになる。
     * <pre>
     * " 6.71"
     * "12.33"
     * "12.0 "
     * " 0.0 "
     * つまり整数部が10以下のときは行頭がスペースで埋まり、小数部2位がゼロなら、
     * そこもスペースで埋まり、つねに5文字に整形される。
     * 整数部が0以下または30かそれ以上の場合は、IllegalArgumentExceptionが出る。
     * </pre>
     * @param value 浮動小数
     * @param precision 精度。2を指定するとコンマ二桁まで。
     * @exception java.lang.IllegalArgumentException
     */
    public static String getSignAngleConstantLength(double value,
                                                          int precision) {
        double v = Unit.truncate(value,precision);
        if(v >= 30 || v < 0)
            throw new IllegalArgumentException("The value exceeds 30");
        String sv = (v<10) ? " " + v : "" + v;
        char [] c = new char [ precision + 3 ];
        for(int i=0; i<c.length; i++) c[i] = ' ';
        for(int i=0; i<sv.length(); i++)
            c[i] = sv.charAt(i);
        return new String(c);
    }
    /**
     * システムプロパティ"app.angleunit"のキーから10進/60進数を判定して、
     * 天体の獣帯座標用に数値を書式整形して返す。
     * 10進の場合は、getSignAngleConstantLength()でprecisionに2を指定した
     * 文字列を返す。60進数の場合は" 2.23'"という文字列を返す。
     * いずれにせよ小数点以下は2位で固定されている。
     * 負数を入力した場合の動作は保証されない。
     */
    public static String getSignAngle(double value) {
        if ( angleUnit() == SEXAGESIMAL ) {
            double [] v = sexagesimal(value);
            return String.format("%02d.%02d'",(int)v[1],(int)v[2]).
                    replaceAll("^0"," ");
        }
        return getSignAngleConstantLength(value,2);
    }
    /**
     * システムプロパティ"app.angleunit"のキーから10進/60進数を判定して、
     * 天体の獣帯座標用に数値を書式整形して返す。
     * 整数部、小数部ともに二桁で10進の場合"02.33"等。60進の場合は"02.33'"等。
     * 60進である識別は末尾のシングルクォート。
     */
    public static String getSignAngle2(double value) {
        if ( angleUnit() == SEXAGESIMAL ) {
            double [] v = sexagesimal(value);
            return String.format("%02d.%02d'",(int)v[1],(int)v[2]);
        }
        String s = formatSignAngle(value,2).replaceAll("^ ","0");
        return s;
    }
    /**
     * 10進小数を60進数表現にして返す。
     * <pre>
     * 戻り値を表示する際の例
     *    System.out.printf("%d %d %f\n",(int)v[1],(int)v[2],v[3]);
     * </pre>
     * @param decimal 十進小数
     * @return 戻り値の[0]は符号で、正の値のときは1.0、負のときは-1.0、ゼロのときは0.0。
     * [1]は度、または時の位。[2]は分、[3]は秒とその端数。
     * [0],[1],[2]はかならず整数が戻る。
     */
    public static double [] sexagesimal(double decimal) {
        double sign = Math.signum(decimal);
        decimal = Math.abs(decimal);
        double th,tm,ts,ws;
        th=(int) decimal;               //整数部抽出→時
        ws = (decimal-th) * 3600d;      //小数部×1時間秒
        ws = ws/60d;                    //分に換算
        tm = (int)ws;                   //整数部抽出→分
        ws -= tm;                       //小数部抽出
        ts = ws * 60d;                  //×1分秒
        return new double[] { sign,th,tm,ts };
    }
    
    /**
     * このオブジェクトの経度を書式整形して返す。
     * 引数で十進表記、六十進表記を指定できる。
     * @param unit DECIMAL または SEXAGESIMAL
     * @param longitude 経度
     */
    public static String getFormattedLongitude(int unit,Double longitude) {
        if ( longitude == null ) return "";
        if ( unit == DECIMAL ) {
            String lon = longitude.toString();
//            lon = (lon.startsWith("-")) ?
//                "W".concat(lon.substring(1)) : "E".concat(lon);
            lon = (lon.startsWith("-")) ?
                lon.substring(1).concat("W") : lon.concat("E");
            return lon;
        }
        double [] v = sexagesimal(longitude);
        String sign = v[0] < 0 ? "W" : "E";
        String sv = String.format("%2.3f",v[3]).replaceAll("\\.","\\\"\\.");
        return String.format("%d\u00B0%d'%s%s",
                              (int)v[1],(int)v[2],sv,sign);
//        String sv = String.format("%2.3f",v[3]).replaceAll("\\.","\u2033\\.");
//        return String.format("%d\u00B0%d\u2032%s%s",
//                              (int)v[1],(int)v[2],sv,sign);
        //return String.format("%s%dﾟ%d'%g",sign,(int)v[1],(int)v[2],v[3]);
    }
    
    /**
     * このオブジェクトの緯度を書式整形して返す。
     * 引数で十進表記、六十進表記を指定できる。
     * @param unit DECIMAL または SEXAGESIMAL
     * @param latitude 緯度
     */
    public static String getFormattedLatitude(int unit,Double latitude) {
        if ( latitude == null ) return "";
        if ( unit == DECIMAL ) {
            String lat = latitude.toString();
            lat = (lat.startsWith("-")) ?
                lat.substring(1).concat("S") : lat.concat("N");
            return lat;
        }
        double [] v = sexagesimal(latitude);
        String sign = v[0] < 0 ? "S" : "N";
        String sv = String.format("%2.3f",v[3]).replaceAll("\\.","\\\"\\.");
        return String.format("%d\u00B0%d'%s%s",
                              (int)v[1],(int)v[2],sv,sign);
    }

    /**
     * システムプロパティの"app.topounit"のキーの値を参照して、単位系を返す。
     * @return SEXAGESIMAL, DECIMALのどちらか。
     */
    private static int defaultUnit() {
        return  ( System.getProperty("app.topounit","10").equals("60") ) ?
                          SEXAGESIMAL : DECIMAL;        
    }
    private static int angleUnit() {
        return  ( System.getProperty("app.angleunit","10").equals("60") ) ?
                          SEXAGESIMAL : DECIMAL;        
    }
    /**
     * システムプロパティの"app.topounit"のキーの値を参照して、単位系を返す。
     * @return SEXAGESIMAL, DECIMALのどちらか。
     */
    public static int getAngleUnit() {
        return angleUnit();
    }
    /**
     * システムプロパティ"app.topounit"のキーから10進/60進数を判定して、
     * 緯度を書式整形して返す。
     */
    public static String getFormattedLatitude(Double latitude) {
        return getFormattedLatitude( defaultUnit(), latitude );
    }
    
    /**
     * システムプロパティ"app.topounit"のキーから10進/60進数を判定して、
     * 経度を書式整形して返す。
     */
    public static String getFormattedLongitude(Double longitude) {
        return getFormattedLongitude( defaultUnit(), longitude );
    }
    
    /**
     * 経度を整形して返す。東経なら"135.5E" 西経なら"135.5W"などとなる。
     * @param longitude 文字列表現の経度
     * @return 整形された経度
     */
    public static String getFormattedLongitude(String longitude) {
        longitude = (longitude.startsWith("-")) ?
            longitude.substring(1).concat("W") : longitude.concat("E");
            //"W".concat(longitude.substring(1)) : "E".concat(longitude);
        return longitude;
    }
    
    /**
     * 緯度を整形して返す。北緯なら"35.5N" 南緯なら"-45.5S"などとなる。
     * @param latitude 文字列表現の緯度
     * @return 整形された緯度
     */
    public static String getFormattedLatitude(String latitude) {
        latitude = (latitude.startsWith("-")) ?
            latitude.substring(1).concat("S") : latitude.concat("N");
            //"S".concat(latitude.substring(1)) : "N".concat(latitude);
        return latitude;
    }
    /**
     * 角度入力で cosを計算し値を返す
     */
    public static double fnc(double x) {
        return Math.cos(x * Math.PI/180d);
    }
    /**
     *角度入力で sinを計算し値を返す
     */
    public static double fns(double x) {
        return Math.sin(x * Math.PI/180d);
    }
    /**
     *角度入力でtanを計算し値を返す
     */
    public static double fnt(double x) {
        return Math.tan(x * Math.PI/180d);
    }
    /**
     *ラジアンで入力された値のアークコサインを角度で返す
     */
    public static double fnac(double x) {
        return
            (Math.PI/2d - Math.atan(x / Math.sqrt(1.0 - x * x))) * 180d/Math.PI;
    }
    
    /**
     *ラジアンで入力された値のアークサインを角度で返す
     */
    public static double fnas(double x) {
        return Math.atan(x/Math.sqrt(1.0-x*x))*180/Math.PI;
    }
    /**
     *タンジェントを角度で返す
     */
    public static double fnat(double x,double y) {
        return Math.atan(x/y)*180d/Math.PI;
    }

}
