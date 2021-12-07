/*
 *
 */
package to.tetramorph.starbase.lib;
import java.awt.Color;
import static swisseph.SweConst.*;
/**
 * システム全体で使う定数群で天体番号はじめ様々な定数が宣言されている。
 * このクラスは、感受点を表す定数を宣言している。SUNからPLUTO、EARTHからVESTAまでの
 * 値は、スイスエフェメリスのSweConstで宣言されている惑星番号SE_SUN〜SE_VESTAと等価。
 * NODEはSE_MEAN_NODEと同じ値。APOGEEはSE_MEAN_APOGと同じ値。
 * この値に+1すると、それぞれSE_TRUE_NODEやSE_OSCU_APOGと同じ値になる。
 * だがその値に対しては、変数を割り当てていない。<P>
 *
 * スイスエフェメリスにはサウスノードやディセンダントを計算する機能は無いが、
 * 占星術では比較的問題にされる事が多く、感受点として必要に応じて利用できる事が望ま
 * しい。ChartFactorクラスでは、このConstで宣言されている感受点を与えれば、それらの
 * 位置を求められるようになっている。
 */
public class Const {
    private Const() { }
    /** 太陽 */
    public static final int SUN = SE_SUN;
    /** 月 */
    public static final int MOON = SE_MOON;
    /** 水星 */
    public static final int MERCURY = SE_MERCURY;
    /** 金星 */
    public static final int VENUS = SE_VENUS;
    /** 火星 */
    public static final int MARS = SE_MARS;
    /** 木星 */
    public static final int JUPITER = SE_JUPITER;
    /** 土星 */
    public static final int SATURN = SE_SATURN;
    /** 天王星 */
    public static final int URANUS = SE_URANUS;
    /** 海王星 */
    public static final int NEPTUNE = SE_NEPTUNE;
    /** 冥王星 */
    public static final int PLUTO = SE_PLUTO;
    /**
     *ノード。SweConst.SE_MEAN_NODEと同じ値(10)で、
     * これに+1するとSE_TRUR_NODEと同じ値になる。
     * MEAN,TRUEどちらのノードを使うかは、プロパティで決まる。
     * プロパティの取得は、(Boolean)SysHash.get("UseMeanNode")で、
     * 初期値はMainFrame内で設定される。
     */
    public static final int NODE = SE_MEAN_NODE;
    /** 真ノード */
    public static final int TRUE_NODE = SE_TRUE_NODE;
    /**
     * アポジー(リリス)。SweConst.SE_MEAN_APOGと同じ値(12)で、
     * これに+1とするSE_OSCU_APOGと同じ値になる。
     * プロパティの取得は、(Boolean)SysHash.get("UseMeanApogee")で、
     * 初期値はMainFrame内で設定される。
     */
    public static final int APOGEE = SE_MEAN_APOG;
    /** オスカレーションアポジー */
    public static final int OSCU_APOGEE = SE_OSCU_APOG;

    /** 地球(ヘリオセントリックでは地球が登場する) */
    public static final int EARTH = SE_EARTH;
    /** 小惑星キローン */
    public static final int CHIRON = SE_CHIRON;
    /** 小惑星ホルス */
    public static final int PHOLUS = SE_PHOLUS;
    /** 小惑星セレス */
    public static final int CERES = SE_CERES;
    /** 小惑星パラス */
    public static final int PALLAS = SE_PALLAS;
    /** 小惑星ジュノー */
    public static final int JUNO = SE_JUNO;
    /** 小惑星ベスタ */
    public static final int VESTA = SE_VESTA;

    /** ミーンサウスノード(ドラゴンテイル) */
    public static final int SOUTH_NODE = 21;
    /** トルーサウスノード(ドラゴンテイル) */
    public static final int TRUE_SOUTH_NODE = 22;
    /** アンチアポジー(アンチリリス) */
    public static final int ANTI_APOGEE = 23;
    /** アンチオスカレーションアポジー(アンチオスカレーションリリス)　*/
    public static final int ANTI_OSCU_APOGEE = 24;

    /** アセンダント */
    public static final int AC = 30;
    /** ディセンダント */
    public static final int DC = 31;
    /** MC(天頂) */
    public static final int MC = 32;
    /** IC(天底) */
    public static final int IC = 33;
    /** バーテックス */
    public static final int VERTEX = 34;
    /** アンチバーテックス */
    public static final int ANTI_VERTEX = 35;

    /** 1室カスプ */
    public static final int CUSP1 = 41;
    /** 2室カスプ */
    public static final int CUSP2 = 42;
    /** 3室カスプ */
    public static final int CUSP3 = 43;
    /** 4室カスプ */
    public static final int CUSP4 = 44;
    /** 5室カスプ */
    public static final int CUSP5 = 45;
    /** 6室カスプ */
    public static final int CUSP6 = 46;
    /** 7室カスプ */
    public static final int CUSP7 = 47;
    /** 8室カスプ */
    public static final int CUSP8 = 48;
    /** 9室カスプ */
    public static final int CUSP9 = 49;
    /** 10室カスプ */
    public static final int CUSP10 = 50;
    /** 11室カスプ */
    public static final int CUSP11 = 51;
    /** 12室カスプ */
    public static final int CUSP12 = 52;

    /**
     * 感受点名のリスト。
     */
    public static final String [] PLANET_NAMES = {
        "太陽","月","水星","金星","火星","木星","土星",
        "天王星","海王星","冥王星",
        "ノード","真ノード","リリス","密接リリス",
        "地球","キロン","フォラス","セレス",
        "パラス","ジュノー","ベスタ","サウスノード","真サウスノード",
        "アンチリリス","密接アンチリリス",null,null,null,null,null,
        "アセンダント","ディセンダント","天頂","天底",
        "バーテクス","アンチバーテックス",
        null,null,null,null,null,
        "1室カスプ","2室カスプ","3室カスプ","4室カスプ","5室カスプ","6室カスプ",
        "7室カスプ","8室カスプ","9室カスプ","10室カスプ","11室カスプ",
        "12室カスプ",
    };

    /**
     * 英語名の感受点名のリスト。PLANETS_LONGITUDE表のフィールド名でもある。
     */
    public static final String [] PLANET_NAMES_EN = {
        "SUN","MOON","MERCURY","VENUS","MARS","JUPITER","SATURN","URANUS",
        "NEPTUNE","PLUTO","NODE","TRUE_NODE","APOGEE","OSCU_APOGEE","EARTH",
        "CHIRON","PHOLUS","CERES","PALLAS","JUNO","VESTA","SOUTH_NODE",
        "TRUE_SOUTH_NODE","ANTI_APOGEE","ANTI_OSCU_APOGEE",
        null,null,null,null,null,
        "AC","DC","MC","IC","VERTEX","ANTI_VERTEX",
        null,null,null,null,null,
        "CUSP1","CUSP2","CUSP3","CUSP4","CUSP5","CUSP6",
        "CUSP7","CUSP8","CUSP9","CUSP10","CUSP11","CUSP12"
    };

    /**
     * 天体リストを表示するときの優先順序でならんでいる天体IDのリスト
     */
    public static final Integer [] LISTING_BODYS = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,NEPTUNE,PLUTO,
        AC,MC,DC,IC,
        NODE,TRUE_NODE,APOGEE,OSCU_APOGEE,
        EARTH,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,
        SOUTH_NODE,ANTI_APOGEE,ANTI_OSCU_APOGEE,VERTEX,ANTI_VERTEX
    };

    /**
     * シンボル略称であるとともにGIFファイル名のリスト。
     * ただしACS,MC,DC,ICを除いて、カスプを表すシンボルは用意されてない。
     */
    public static final String [] SYMBOL_NAMES = {
        "sun","mon","mer","ven","mar","jup","sat","ura","nep","plu",
        "nod","nod","apg","apg","ear","chi","pho","cer","pal","jun","ves",
        "snd","snd","aapg","aapg",null,null,null,null,null,
        "ac","dc","mc","ic","v","av",
        null,null,null,null,null,
        "cusp1","cusp2","cusp3","cusp4","cusp5","cusp6","cusp7",
        "cusp8","cusp9","cusp10","cusp11","cusp12" };

    /**
     *オリジナル占星術フォントにおける感受点の記号で、天体番号順にならんでいる。
     */
    public static final char [] BODY_CHARS = {
        0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A,0x4B,
        0x4C,0x4C,0x4E,0x4E,0x41,0x51,0x52,0x53,0x54,0x55,0x56,
        0x4D,0x4D,
        0x3C,0x3C, // 23,24 apogee,oscu.apogee
        0,0,0,0,0,
        0x58,0x5D,0x59,0x5E,0x5A,0x57,
        0,0,0,0,0,
        0x20,0x20,0x20,0x20,0x20,0x20,0x20,
        0x20,0x20,0x20,0x20,0x20 };
    /**
     * 12星座の3文字略称であるとともに、GIFファイル名のリスト。
     */
    public static final String [] ZODIAC_NAMES =
    { "ari","tau","gem","can","leo","vir","lib","sco","sag","cap","aqu","pis" };
    /**
     *オリジナル占星術フォントにおける星座記号のcharコード
     */
    public static final char [] ZODIAC_CHARS =
    {
        0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2A,0x2B,0x2C
    };


    /** 0度のアスペクト番号 */
    public static final int CONJUNCTION = 0; //0
    /** 60度のアスペクト番号*/
    public static final int SEXTILE = 1; //60
    /** 90度のアスペクト番号*/
    public static final int SQUARE = 2;  //90
    /** 120度のアスペクト番号*/
    public static final int TRINE = 3;   //120
    /** 180度のアスペクト番号*/
    public static final int OPPOSITION = 4; //180
    /** 150 度のアスペクト番号*/
    public static final int QUINCUNX = 5;
    /** 72 度のアスペクト番号*/
    public static final int QUINTILE = 6;
    /** 30度のアスペクト番号 */
    public static final int SEMI_SEXTILE = 7;
    /** 45度のアスペクト番号 */
    public static final int SEMI_SQUARE = 8;
    /** 135度のアスペクト番号 */
    public static final int SESQIQUADRATE = 9;
    /** 144度のアスペクト番号 */
    public static final int BIQUINTILE = 10;
    /** 36度のアスペクト番号 */
    public static final int DECILE = 11;
    /** パラレルのアスペクト番号*/
    public static final int PARALLEL = 12;
    /**
     * アスペクト番号に対応した、アスペクト名の配列。コンジャクション、トライン等。
     */
    public static final String [] ASPECT_NAMES = {
        "コンジャンクション","セクスタイル","スクエア","トライン","オポジション",
        "クインカンクス","クインタイル","セミセクスタイル","セミスクエア",
        "セスクアドレイト","バイクインタイル","デシル","パラレル"
    };
    /**
     * アスペクト番号に対応した、アスペクトの角度のリスト。0,60,90...等。
     */
    public static final double [] ASPECT_ANGLES = {
        0d,60d,90d,120d,180d,150d,72d,30d,45d,135d,144d,36d,0d
    };
    /** アスペクト番号に対応するGIFアイコンファイル名 */
    public static final String [] ASPECT_SYMBOL_NAMES = {
        "a0","a60","a90","a120","a180",
        "a150","a72","a30","a45","a135","a144","a36","apar"	};
    /**
     * アスペクト番号に対応した、アスペクトの英名の配列。
     * "conjunction","sextile"等。
     */
    public static final String [] ASPECT_NAMES_EN = {
        "conjunction","sextile","square","trine","opposition",
        "quincunx","quintile","semi-sextile","semi-square",
        "sesquiquadrate","bi-quintile","decile","parallel"
    };
    /**
     * アスペクト番号に対応した、アスペクトの英名3文字の配列。
     * "con", "sex", "squ", "tri", "opp"等。
     */
    public static final String [] ASPECT_SHORT_NAMES = {
        "con", "sex", "squ", "tri", "opp",
        "qcu", "qti", "sse", "ssq",
        "ses", "biq", "dec", "par"
    };
    /** オリジナルフォントによるアスペクト記号のcharコード */
    public static final char [] ASPECT_CHARS = {
        0x2E,0x2F,0x30,0x31,0x32,
        0x35,0x38,0x33,0x34,0x39,0x3A,0x36,0x2D };

    /** アスペクトの角度定義 */
    public static final double [] ASPECT_ANGLE = {
        0d,60d,90d,120d,180d,
        150d,72d,30d,45d,135d,144d,36d,0d };
    /**
     * 第1種のアスペクトのIDリスト。
     * CONJUNCTION,SEXTILE,SQUARE,TRINE,OPPOSITION
     */
    public static final int [] ASPECTS_CATEGORY1 = {
        CONJUNCTION, SEXTILE, SQUARE, TRINE, OPPOSITION
    };
    /**
     * 第2種のアスペクトのIDリスト。
     * SEMI_SEXTILE, SEMI_SQUARE, SESQIQUADRATE, QUINCUNX
     */
    public static final int [] ASPECTS_CATEGORY2 = {
        SEMI_SEXTILE, SEMI_SQUARE, SESQIQUADRATE, QUINCUNX
    };
    /**
     * 第3種のアスペクトのIDリスト。
     * QUINTILE, DECILE, BIQUINTILE
     */
    public static final int [] ASPECTS_CATEGORY3 = {
        QUINTILE, DECILE, BIQUINTILE
    };
    /**
     * 特殊のアスペクトのIDリスト。
     */
    public static final int [] ASPECTS_CATEGORY4 = {
        PARALLEL
    };

    /** 牡羊座を表す定数 */
    public static final int ARI = 0;
    /** 牡牛座を表す定数 */
    public static final int TAU = 1;
    /** 双子座を表す定数 */
    public static final int GEM = 2;
    /** 蟹座を表す定数 */
    public static final int CAN = 3;
    /** 獅子座を表す定数 */
    public static final int LEO = 4;
    /** 乙女座を表す定数 */
    public static final int VIR = 5;
    /** 天秤座を表す定数 */
    public static final int LIB = 6;
    /** 蠍座を表す定数 */
    public static final int SCO = 7;
    /** 射手座を表す定数 */
    public static final int SAG = 8;
    /** 山羊座を表す定数 */
    public static final int CAP = 9;
    /** 水瓶座を表す定数 */
    public static final int AQU = 10;
    /** 魚座を表す定数 */
    public static final int PIS = 11;

    /**
     * 日本語の星座名 「牡羊」から「魚」まで
     */
    public static final String [] SIGN_NAMES = {
        "牡羊","雄牛","双子","蟹","獅子","乙女",
        "天秤","蠍","射手","山羊","水瓶","魚"
    };

    /**
     * 英語の星座名「Aries」から「Pisces」まで
     */
    public static final String [] SIGN_NAMES_EN = {
        "Aries","Tauras","Gemini","Cancer","Leo","Virgo",
        "Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces"
    };

    /**
     * 占星術フォントの中の月相を表す文字コード(新月、上弦、満月、下弦)
     */
    public static final char [] MOON_CHARS = { 0x3D,0x3E,0x3F,0x40 };
    /**
     * 逆行シンボルの文字コード
     */
    public static final char RETROGRADE_CHAR = 0x50;
    /**
     * ハウスシステムを表すコード。'P','K','O','R','C','E','1','2'
     */
    public static final int [] HOUSE_SYSTEM_CODES = {
        'P','K','O','R','C','E','1','2'
    };

    /**
     * ハウスシステム名
     */
    public static final String [] HOUSE_SYSTEM_NAMES = {
        "プラシーダス", "コッホ", "ポルフュリオス", "レギオモンタナス",
        "キャンパナス", "イコール", "ソーラーサイン", "ソーラー"
    };
    /**
     * サインカラー 12色の色相環
     */
    public static final Color [] SIGN_COLORS = {
        new Color(255,0,0),new Color(255,63,0),new Color(255,191,0),
        new Color(255,255,0),new Color(191,255,0),new Color(63,255,0),
        new Color(0,255,0),new Color(0,191,63),new Color(0,63,191),
        new Color(0,0,255),new Color(63,0,191),new Color(191,0,63)
    };
    /**
     * 太陽年365.2424の定数
     */
    public static final double SOLAR_YEAR = 365.2424;
    /**
     * 恒星時23.9344696の定数
     */
    public static final double SIDEREAL_DAY = 23.9344696;
    /**
     * ローマ数字表記のハウス番号(I〜XII)
     */
    public static final String [] HOUSE_NUMBERS = {
        "I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"
    };
    /**
     * モダン十惑星システムにおける星座番号→支配星(天体番号)への変換テーブル。
     * 要素番号[0]〜[11]は牡羊座から魚座に対応している。
     * 格納されている値は天体番号で、[0]から順番に、MARS,VENUS,MERCURY,MOON,SUN,
     * MERCURY,VENUS,PLUTO,JUPITER,SATURN,URANUS,NEPTUNEとなっている。
     */
    public static final int [] MODERN_RULERS = {
        MARS,VENUS,MERCURY,MOON,SUN,MERCURY,VENUS,
        PLUTO,JUPITER,SATURN,URANUS,NEPTUNE
    };
    /**
     * 古典七惑星システムにおける星座番号→支配星(天体番号)への変換テーブル。
     * 要素番号[0]〜[11]は牡羊座から魚座に対応している。
     * 格納されている値は天体番号で、[0]から順番に、MARS,VENUS,MERCURY,MOON,SUN,
     * MERCURY,VENUS,MARS,JUPITER,SATURN,SATURN,JUPITERとなっている。
     */
    public static final int [] CLASSIC_RULERS = {
        MARS,VENUS,MERCURY,MOON,SUN,MERCURY,VENUS,
        MARS,JUPITER,SATURN,SATURN,JUPITER
    };


}
