/*
 * Ephemeris.java
 *
 * Created on 2007/01/23, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.time.DateFormat;
import to.tetramorph.time.JDay;

/**
 * 主にデータベースに入れるためにまとめて天体位置を求めるのに使う。
 * このクラスを使用するためには、System.setProperty("swe.path","天文暦へのパス")
 * として辞書へのパスが設定されている必要がある。
 */
public class Ephemeris {

//  static {
//      //ｽｲｽｴﾌｪﾒﾘｽの天体暦ﾌｧｲﾙへのﾊﾟｽはｾｷｭﾘﾃｨﾏﾈｰｼﾞｬの都合上起動時にﾌﾟﾛﾊﾟﾃｨから与える
//      String path = System.getProperty("swe.path");
//      System.out.println("Ephemeris swe.path = " + path);
//      if(path == null) throw
//          new IllegalStateException("System property 'swe.path' noe found.");
//      //ｽｲｽｴﾌｪﾒﾘｽ用のﾊﾟｽ表現に直す。
//      path = path.replace("\\","/");
//      path = path.replace(":","\\:");
//      swe = new SwissEph(path);
//  }
    private Map<Integer,Body> bodyMap;
    private  SwissEph swe;
    private boolean error;
    private List<String> errorList = null;
    private static Ephemeris INSTANCE = new Ephemeris();
    /**
     * オブジェクトを作成する。
     */
    private Ephemeris() {
        this.error = false;
    }
    
    /**
     * このオブジェクトを返す。
     */
    public static Ephemeris getInstance() {
        return INSTANCE;
    }

    /**
     * SwissEphのインスタンスを返す。
     * 理想的にはシステム終了時にswe_close()で閉じること。
     */
    public SwissEph getSwissEph() {
        if(swe == null) {
            String path = System.getProperty("swe.path");
            if(path == null) throw new IllegalStateException(
                "System property 'swe.path' noe found.");
            //ｽｲｽｴﾌｪﾒﾘｽ用のﾊﾟｽ表現に直す。
            path = path.replace("\\","/");
            path = path.replace(":","\\:");
            swe = new SwissEph( path );
        }
        return swe;
    }
    /**
     * ユリウス日を指定するとその日のスイスエフェメリスが標準でサポートしている
     * すべての天体位置のリストを返す。
     * 黄経、黄緯、距離、黄経角速度、黄緯角速度、相対速度が求まる。
     * 天文暦の範囲外の日付を指定すると算術例外がスルーされる。
     * 1万件の計算に約20秒
     * @throws ArithmeticException
     */
    public Map<Integer,Body> getBodyMap(double jday)  {
        bodyMap = new HashMap<Integer,Body>();
        error = false;
        double ET = jday + SweDate.getDeltaT(jday); //暦表時を求める
        int flag = SweConst.SEFLG_SPEED;
        double result[] = new double[6];
        errorList = new ArrayList<String>();
        int j=0;
        SwissEph swe = getSwissEph();
        for(int p = SweConst.SE_SUN; p <= SweConst.SE_VESTA; p++) {
            if (p == SweConst.SE_EARTH) continue;
            StringBuffer err = new StringBuffer();
            int ret = swe.swe_calc(ET,p,(int)flag,result,err);
            if( ret < 0 || ret != flag) {
                error = true;
                String errmsg = err.toString();
                errorList.add(errmsg);
                //暦ファイルがない場合は例外を出す
                if(errmsg.indexOf("SwissEph file 'se") >= 0)
                    throw new IllegalStateException(errmsg);
            } else
                bodyMap.put(new Integer(p),new Body(p,result));
        }
        putAntiPoint(bodyMap.get(NODE),SOUTH_NODE);
        putAntiPoint(bodyMap.get(APOGEE),ANTI_APOGEE);
        putAntiPoint(bodyMap.get(TRUE_NODE),TRUE_SOUTH_NODE);
        putAntiPoint(bodyMap.get(OSCU_APOGEE),ANTI_OSCU_APOGEE);
        return bodyMap;
    }
    /**
     * アセンダント,MC,バーテックス,ディセンダント、
     * 1室カスプ〜12室カスプまでを求めmapに登録する。
     */
    public Map<Integer,Body> getBodyMap(double jday,double lat,double lon,
        int hsys) {
        bodyMap = new HashMap<Integer,Body>();
        double [] cusps  = new double[13];	//1室〜12室の度数が書き込まれる
        double [] acmc = new double[10];	//AscやMCの値が書き込まれる。
        int ret = getSwissEph().swe_houses( jday,0,lat, lon,hsys,cusps,acmc);
        Body ac = new Body(AC,acmc[0]);
        Body mc = new Body(MC,acmc[1]);
        Body vertex = new Body(VERTEX,acmc[3]);
        bodyMap.put( AC,ac );
        bodyMap.put( MC,mc );
        bodyMap.put( VERTEX,vertex );
        putAntiPoint(ac,DC);
        putAntiPoint(mc,IC);
        putAntiPoint(vertex,ANTI_VERTEX);
        for(int i = 0; i < 12; i++)
            bodyMap.put(i+CUSP1,new Body( i+CUSP1,cusps[i+1] ));
        return bodyMap;
    }
    // bodyの正反対の位置の感受点を作成してbodyMapに登録。idは感受点コードで、
    // サウスノードやアンチアポジーなどを指定することを想定している。
    void putAntiPoint(Body body,int id) {
        if(body == null) return;
        Body b = new Body(body);
        b.id = id;
        b.lon = (body.lon + 180d) % 360d;
        b.lat = (body.lat * -1d) % 360d;
        bodyMap.put(id,b);
    }
    /**
     * 計算エラーや警告があったときはtrueを返す。エラーが出る主な理由は、天文暦の
     * 範囲外の日時が指定されたとき。
     */
    public boolean isError() {
        return error;
    }
    /**
     * isError()がtrueを返したときはこのメソッドでエラーの一覧表を取得できる。
     * ノーエラーならsize()==0のリストが返る。
     */
    public List<String> getErrorList() {
        return errorList;
    }
    /**
     * 指定日の太陽黄経を返す。これはお手軽に太陽の位置を求めるためのメソッド。
     */
    public double getSun( GregorianCalendar gcal ) {
        double jday = JDay.get(gcal);
        double et = SweDate.getDeltaT(jday);
        StringBuffer errbuf = new StringBuffer();
        double xx[] = new double[6]; //結果が求まる
        long report = swe.swe_calc( 
            jday + et, SweConst.SE_SUN, SweConst.SEFLG_SPEED, xx, errbuf );
        if ( report < 0 ) 
            throw new IllegalArgumentException( errbuf.toString() );
        else if( report != SweConst.SEFLG_SPEED )
            System.out.println("warning: " + errbuf.toString() );
        return xx[0];
    }
    
    /**
     * テスト
     */
    public static void main(String [] args) {
        System.setProperty("swe.path",
            "C:/Documents and Settings/おーさわよしたか/.Amateru/ephe");
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.set(Calendar.MILLISECOND,0);
        gcal.setTimeZone(TimeZone.getTimeZone("UTC"));
        gcal.set(gcal.ERA,gcal.BC);
        gcal.set(5390,1-1,1,12,0,0);
        Ephemeris ep = Ephemeris.getInstance(); //new Ephemeris();
        for(int i=0; i<100; i++) {
            double jd = JDay.get(gcal);
            Map<Integer,Body> map = ep.getBodyMap(jd);
            if(ep.isError()) {
                System.out.println("ERROR : " + DateFormat.getDateString(jd));
                for(String mes : ep.getErrorList()) {
                    mes = mes.trim();
                    System.out.println("    " + mes);
                }
            }
            gcal.add(Calendar.YEAR,-1);
        }
        System.out.println("END = " + DateFormat.getDateString(gcal));
    }
    static void test() {
        GregorianCalendar gcal = new GregorianCalendar(1964,9-1,30,5,35,0);
        long start = System.currentTimeMillis();
        Ephemeris ep = Ephemeris.getInstance(); //new Ephemeris();
        for(int j=0; j<1000; j++) {
            Map<Integer,Body> map = ep.getBodyMap(JDay.get(gcal));
            for(int i=SUN; i<= PLUTO; i++) {
                if(PLANET_NAMES[i] == null) continue;
            }
            gcal.add(gcal.DAY_OF_MONTH,1);
        }
        System.out.print("所要時間 : ");
        System.out.println((System.currentTimeMillis() - start)/1000);
    }
}
