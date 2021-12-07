/*
 * Design.java
 *
 * Created on 2008/02/12, 1:58
 *
 */

package to.tetramorph.michiteru;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;
import static to.tetramorph.michiteru.Const.*;
import to.tetramorph.util.FileTools;
/**
 * カレンダー描画のための設定色をロードしてフィールド変数にセットする。
 * 単純化するためすべてstatic変数＆メソッド。つまりカレンダーのそのものの
 * インスタンスは一つしか作らない事が前提で、色設定は一つあれば良いという考え方。
 * カレンダーが二つ開いてとなると、こういうやり方はよろしくない。
 * @author 大澤義孝
 */
class Design {
    public static Color [] testcolor = new Color[12];
    public static Color [] signSymColors = new Color[12];
    public static Color cellColor;             //通常の日付セルの背景色
    public static Color todayCellColor;        //今日の日付セルの背景色
    public static Color lineColor;             //セルを描く線の色
    public static Color bgColor;               //カレンダーのさらに外側の背景色
    public static Color weekCellColor;         //曜日ヘッダセルの背景色
    public static Color monthNumberColor;      //月の数の文字色
    public static Color yearNumberColor;       //年の数の文字色
    public static Color voidTimeStringColor;   //ボイド時間を表示する文字色
    public static Color nullCellColor;         //日が存在しないセルの背景色
    public static Color voidBarColor;          //ボイドバーの色
    public static Color moonFaceColor;         //ムーンフェイスの記号と文字色
    public static Font weekHeadderFont = null;
    public static Font dayNumberFont = new Font("SansSerif",Font.PLAIN,20);

    private final static String [] defSignColors = {
        "0xff8080","0xffff80","0x80ff80","0x8080ff","0xff8080","0xffff80",
        "0x80ff80","0x8080ff","0xff8080","0xffff80","0x80ff80","0x8080ff"};

    private final static String [] weekKeys = {
        "sunday","monday","tuesday","wednesday","thursday","friday","saturday"
    };

    private final static String [] defWeekColors = {
        "0xff0000","0","0","0","0","0","0x0000ff"
    };

    public static Color [] weekColors = new Color[7];
    public static Color [] weekHeadderColors = new Color[7];

    static Properties prop;

    /**  インスタンス作成禁止 */
    private Design() {
    }
    /**
     * 指定された番号の色設定をロードする。
     */
    public static boolean loadConfig(String num) {
        prop = new Properties();
        InputStream inputStream = Design.class.getResourceAsStream(
            "/resources/AstroCalendar" + num + ".properties");
        if ( ! FileTools.loadProperties(prop,inputStream) )
            return false;
        String fontName = prop.getProperty("weekHeadderFontName","SansSerif");
        int fontSize    = integer("weekHeadderFontSize","20");
        weekHeadderFont = new Font(fontName,Font.PLAIN,fontSize);
        fontName        = prop.getProperty("dayNumberFontName","SansSerif");
        fontSize        = integer("dayNumberFontSize","20");
        dayNumberFont   = new Font(fontName,Font.PLAIN,fontSize);

        for ( int i=0; i<ZODIAC_NAMES.length; i++ ) {
            testcolor[i] = color(ZODIAC_NAMES[i]+"Color",defSignColors[i]);
            signSymColors[i] = color(ZODIAC_NAMES[i]+"SignColor","0x000000");
        }

        if(prop.getProperty("mixColorMode","no").equals("yes")) mixColor();

        cellColor      = color("cellColor","0xffffff");
        todayCellColor = color("todayCellColor","0xffffc0");
        lineColor      = color("lineColor","0x000000");
        bgColor        = color("bgColor","0xffffff");

        //setBackground(bgColor);

        for ( int i=0; i<weekKeys.length; i++ ) {
            weekColors[i] = color(weekKeys[i]+"Color", defWeekColors[i] );
            weekHeadderColors[i] =
                color(weekKeys[i] + "HeadderColor", defWeekColors[i] );
        }
        weekCellColor       = color("weekCellColor","0xffffff");
        monthNumberColor    = color("monthNumberColor","0");
        voidTimeStringColor = color("voidTimeStringColor","0");
        yearNumberColor     = color("yearNumberColor","0");
        nullCellColor       = color("nullCellColor","0x2e6b4c");
        voidBarColor        = color("voidBarColor","0xFF0000");
        moonFaceColor       = color("moonFaceColor","0x0");
        //setMonthNameByLocale();
        return true;
    }
    public static String getProperty(String key) {
        return prop.getProperty(key);
    }
    public static String getProperty(String key,String defaultValue) {
        return prop.getProperty( key, defaultValue );
    }
    /**
     * デザインプロパティから値をとりだし、Colorオブジェクトで返す。
     * defValueにはプロパティに登録がなかったときに採用する値を指定する。
     */
    static Color color(String propKey,String defValue) {
        int v = intValue(prop.getProperty(propKey,defValue));
        if( (v & 0xff000000) != 0) return new Color(v,true);
        return new Color(v);
    }

    /**
     * デザインプロパティから値を取り出し、Integerオブジェクトで返す。
     */
    static Integer integer(String key,String value) {
        return intValue(prop.getProperty(key,value));
    }

    /**
     * 文字列の数字をIntegerにして返すが、異常な文字列なら0を返す。
     * つまりExceptionを出さないためのもの。
     */
    static Integer intValue(String value) {
        int i = 0;
        try { i = Integer.decode(value); } catch(Exception e) { }
        return i;
    }

    /**
     * 12色で赤黄緑青と変化するグラデーションカラーを作る
     * 4色を指定すると、それらの色を混ぜあわて12色の色の円を作る。
     */
    static void mixColor() {
        //testcolor[0] = RED;
        testcolor[1] = addColor(addColor(testcolor[0],testcolor[3]),testcolor[0]);
        testcolor[2] = addColor(addColor(testcolor[0],testcolor[3]),testcolor[3]);
        //testcolor[3] = YELLOW;
        testcolor[4] = addColor(addColor(testcolor[3],testcolor[6]),testcolor[3]);
        testcolor[5] = addColor(addColor(testcolor[3],testcolor[6]),testcolor[6]);
        //testcolor[6] = GREEN;
        testcolor[7] = addColor(addColor(testcolor[6],testcolor[9]),testcolor[6]);
        testcolor[8] = addColor(addColor(testcolor[6],testcolor[9]),testcolor[9]);
        //testcolor[9] = BLUE;
        testcolor[10] = addColor(addColor(testcolor[9],testcolor[0]),testcolor[9]);
        testcolor[11] = addColor(addColor(testcolor[9],testcolor[0]),testcolor[0]);
    }

    /**
     * 二つの色を合成したカラーを返す。(RGBを別々に足し合わせて平均を取る)。
     */
    static Color addColor(Color color1, Color color2) {
        int r = (color1.getRed() + color2.getRed()) / 2;
        int g = (color1.getGreen() + color2.getGreen()) / 2;
        int b = (color1.getBlue() + color2.getBlue()) /2;
        int a = (color1.getAlpha() + color2.getAlpha())/2;
        return new Color(r,g,b,a);
    }

}
