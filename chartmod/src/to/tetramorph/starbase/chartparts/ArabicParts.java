/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/21
 */

package to.tetramorph.starbase.chartparts;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Unit;
import to.tetramorph.starbase.util.ChartAnalyzer;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.util.NPTChart;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * アラビックパーツの計算。
 * まずインスタンスを作成。進行法、ハウス分割法などはChartConfigで指定する。<br>
 * setRulerSystemでルーラーの方式を設定。<br>
 * setGroupでN,P,Tのどのチャートの天体を計算対象にするか設定。<br>
 * setSourceで、計算式を与える。<br>
 * setData,setTransitで計算する日時を与える。<br>
 * calcで答えが求まる。<br>
 * アラビックパーツの計算設定ソースは次のように書く
 * <pre>
 * # アラビックパーツ計算設定
 * # "#"から右辺はコメントと見なす。
 * # 不要な式は行頭に"#"をつけてコメントにすると表示が抑止されすっきりする。
 * # "","   "等、空白行は無視する。
 * # "="の左辺はタイトル、右辺は計算式と見なす。
 * # "="が三つある行は式が計算されたあと、最右辺の変数に代入される。
 * # "="が三つある行は最初に評価される。
 * # 変数名と演算子(+,-)は半角で記述する。大文字小文字は区別されない。
 * # 実は(*,/)や括弧、定数、数学関数すら記述可能だが、必要性は無いだろう。
 * # 一行につき一つのパートで、複数行に渡る(改行で分断された）式は認識しない。
 * # 調子にのって変な式を入れると、予期せぬエラーが出るかも。
 * Part of Fortune = AC + Moon - Sun = POF
 * Part of Spirit = AC + Sun - Moon = POS
 * 母,女性の友人    = AC + Moon - Venus
 * 支配者,使用人    = AC + Moon - Mercury
 * 国内旅行(Journey)= AC + Cusp9 - H9Ruler
 * 個人的な敵       = AC + Cusp12 - H12Ruler
 * </pre>
 * @author 大澤義鷹
 */
public class ArabicParts extends NPTChart {
    public static final int MODERN = 1;
    public static final int CLASSIC = 0;
    ScriptEngineManager factory = new ScriptEngineManager();
    List<ArabicNode> resultList = null;
    ChartConfig cc;
    int rulerSystem = MODERN;
    int group = NATAL;
    Map<String,Body> bodyMap;
    public ArabicParts( ChartConfig cc ) {
        super(cc);
    }
    public ArabicParts() {
        super();
    }
    /**
     * ハウスルーラーの決め方を指定する。デフォルトはArabicParts.MODERN。
     * @param rs 古典式(CLASSIC)かモダン式(MODERN)式かのどちらかを指定する。
     */
    public void setRulerSystem( int rs ) {
        if ( rs >=0 && rs <= 1 )
            this.rulerSystem = rs;
        else
            throw new IllegalArgumentException( "異常な引数 : " + rs );
    }
    /**
     * このオブジェクトに設定されているルーラーシステムを返す。
     * @return
     */
    public int getRulerSystem() {
        return rulerSystem;
    }
    boolean isModern() {
        return ( rulerSystem == MODERN ) ? true : false;
    }

    public void setGroup(int npt) {
        this.group = npt;
    }
    public int getGroup() {
        return group;
    }
    /**
     * アラビックパーツ計算設定情報を文字列で与える。
     * @param src
     * @throws IOException
     */
    public void setSource( InputStream stream )  throws IOException {
        setSource( new InputStreamReader(stream) );
    }
    /**
     * アラビックパーツ計算設定情報を文字列で与える。
     * @param src ソース文字列
     * @exception srcにnullを指定したとき。
     */
    public void setSource( String src ) {
        if ( src == null )
            throw new java.lang.IllegalArgumentException("nullは禁止");
        try {
            setSource(new StringReader(src));
        } catch (IOException ex) {
            Logger.getLogger(ArabicParts.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    /**
     * アラビックパーツ計算設定をストリームで与える。
     * @param r リーダを渡す。ファイルリーダもあれば、他もありうる。
     * @throws IOException
     */
    public void setSource( Reader r ) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        resultList = new ArrayList<ArabicNode>();
        String line;
        // reader.ready()を使うと、StringReaderで入ってきた場合、正常に機能しない。
        for( int row = 1; (line = reader.readLine()) != null; row++ ) {
            int cutpos = line.indexOf('#');
            if ( cutpos >= 0 )
                line = line.substring(0,cutpos);
            if ( ! line.isEmpty() ) {
                //処理できる構文は、分解してノードに格納
                String [] temp = line.split("=");
                if ( temp.length >= 2 ) {
                    String title = temp[0].trim();
                    String exp   = temp[1].trim();
                    String variable = null;
                    if ( temp.length >=3  ) {
                        variable = temp[2].trim();
                    }
                    resultList.add( new ArabicNode( title, exp, variable, row ) );
                } else {
                    //エラーのある行はノードのerrmsgに内容をセット
                    ArabicNode ln = new ArabicNode( line,"","", row );
                    ln.errmsg = "Syntax Error.";
                    resultList.add( ln );
                    continue;
                }
            }
        }
    }
    private void exec_script( ArabicNode n, ScriptEngine engine ) {
        n.errmsg = null;
        try {
            double lon = (Double)engine.eval( n.exp.toUpperCase() );
            lon = Unit.circularRound(lon);
            n.arabicBody = new Body( 10000, lon, n.group );
            //n.lon = Math.toDegrees( Math.asin( Math.sin( Math.toRadians(n.lon) ) ) );
        } catch (ScriptException ex) {
            String msg = ex.getMessage();
            int s = msg.indexOf('"');
            int e = msg.indexOf('.', s+1);
            n.errmsg = msg.substring(s,e+1);
        }
    }
    //アラビックパーツの計算に使えるすべての感受点のIDリスト
    //Ture Nodeやoscu.ApogeeはNPTChart内で自動判定するため、Node,Apogeeのみで良い。
    private static final int [] planetTable = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,
        NEPTUNE,PLUTO,NODE,APOGEE,SOUTH_NODE,ANTI_APOGEE,
        AC,DC,MC,IC,VERTEX,ANTI_VERTEX,
        CUSP1,CUSP2,CUSP3,CUSP4,CUSP5,CUSP6,
        CUSP7,CUSP8,CUSP9,CUSP10,CUSP11,CUSP12
    };
    /**
     * 構文を実行する。このメソッド実行前に、setSource()で設定ファイルを与える。
     * またsetNPTChart()で計算したい日時・場所をセットしたNPTChartオブジェクトを
     * 与えておく。
     * @param group N,P,Tどのグループで計算するか。
     * @param isModern trueならモダン式、falseなら古典式でハウスルーラーを決める。
     */
    public List<ArabicNode> calc() {
        if ( getData() == null && group == NPTChart.NATAL )
            throw new IllegalStateException("ネイタルデータがセットされていない");
        if ( ( getData() == null || getTransit() == null ) && group == NPTChart.PROGRESS )
            throw new IllegalStateException("ネイタルデータ、トランジットの" +
                    "どちらか、または両方がセットされていない");
        if ( getTransit() == null && group == NPTChart.TRANSIT )
            throw new IllegalStateException("トランジットがセットされていない");
        bodyMap = new LinkedHashMap<String,Body>();
        Map<Integer,Body> map = new HashMap<Integer,Body>();
        ScriptEngine scriptEngine = factory.getEngineByName("JavaScript");
        // 感受点の度数に、変数名をつけてスクリプトエンジンにセット
        System.out.println("GROUP = " + group);
        for( int id : planetTable ) {
            Body body = super.getBody( id, group );
//            if ( body == null ) throw new IllegalArgumentException(
//                    "必要な天体が求められない : " + PLANET_NAMES_EN[id]);
            if ( body == null ) continue;
            scriptEngine.put( PLANET_NAMES_EN[ id ], body.lon );
            map.put( id, body );
            bodyMap.put( PLANET_NAMES_EN[id],body);
        }
        //NPTChartだけでは求まらない変数、サインルーラーなどもエンジンにセット
        ChartAnalyzer anlz = new ChartAnalyzer(map);
        for ( int i=0; i < 12; i++ ) {
            Body body = anlz.getHouseRuler( isModern(), Const.CUSP1 + i );
            String var = String.format("H%sRULER",i+1);
            scriptEngine.put(var, body.lon );
            bodyMap.put( var,body);
        }
        if ( map.get( AC ) != null ) { //ACが求まるならMCも求まる
            scriptEngine.put( "ACRULER", anlz.getHouseRuler( isModern(), AC ).lon );
            scriptEngine.put( "MCRULER", anlz.getHouseRuler( isModern(), MC ).lon );
        }
        //構文を実行
        for ( ArabicNode n : resultList ) { //変数に入れるものだけ先に計算
            n.group = group;
            if ( n.errmsg != null ) continue; //エラーのある行はスキップ
            if ( n.variable == null ) continue; //変数代入不要行もスキップ
            exec_script( n, scriptEngine);
            //POF,POSなどの変数をエンジンに追加
            if ( n.errmsg == null )
                scriptEngine.put( n.variable, n.arabicBody.lon );
        }
        //残りのもを計算
        for ( ArabicNode n : resultList ) {
            if ( n.errmsg != null ) continue;
            if ( n.variable != null ) continue;
            exec_script( n, scriptEngine );
        }
        return resultList;
    }
    /**
     * 計算で使用されたすべての感受点リストを返す。
     * @return
     */
    public Map<String,Body> getBodyMap() {
        return bodyMap;
    }
//    public static void main(String [] args ) throws Exception {
//        System.setProperty("swe.path","c:/users/ephe/"); //必須。スイスエフェメリスの辞書ファイルの場所。
//        System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
//        ArabicParts ap = new ArabicParts();
//        InputStream is = ArabicParts.class.getResourceAsStream("/resources/arabic_parts.txt");
//        try { ap.setSource(is); }
//        catch(Exception e) { e.printStackTrace(); }
//
//        ap.setData( TestConst.getMyData() );
//        ap.setTransit(TestConst.getMyNowTransit());
//        List<ArabicNode> resultList = ap.calc();
//        for ( ArabicNode n : resultList ) {
//            System.out.println(n.getCSV());
//        }
//    }
//    public static void main(String [] args ) throws Exception {
//        ScriptEngineManager factory = new ScriptEngineManager();
//        ScriptEngine engine = factory.getEngineByName("JavaScript");
//        engine.put("val", 1.5);
//        engine.put("val", 2.);
//        Object o = engine.eval("1.23 + val");
//        if ( o instanceof Double ) {
//            System.out.println("戻ったのはDouble!!");
//        }
//        Double v = (Double)o;
//        System.out.println(v);
//    }
}
