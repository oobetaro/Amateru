package to.tetramorph.astrocalendar;
import java.io.DataInputStream;
import java.io.IOException;
import static java.util.Calendar.*;
import static java.lang.System.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
/**
 * toolパッケージ内の天文暦作成プログラムで作った天文暦を読み出すためのstaticメソッド群。
 * 引数であたえたcalendarは内部でコピーして使用するため値は書き換えられてしまうことはない。
 * Calendarで引き渡すのは、タイムゾーン情報がいっしょに引き渡されるから。
 * タイムゾーンを日本以外の場所にセットしたカレンダーを渡せば、その時刻系でデータは返る。
 */
class Almanac {

    /**
     * <pre>
     * 指定されたcalendarの年(西暦年)月(0～11)に該当するボイドタイムのリストを返す。
     * 入力日付は年と月のみを認識し日は必ず1日とみなす。
     * MoonVoidAlmanacで作成した天文暦ファイルを参照する。
     * 天文暦の範囲外が指定されたときはnullを返す。
     * 
     * 天文暦ファイルへのパス。<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/MoonVoidAlmanac.bin");
     * </pre>
     */
    public static List<VoidTime> getVoidOfCourseMoonList(Calendar calendar) {
        List<VoidTime> list = new ArrayList<VoidTime>();
        Calendar cal = (Calendar)calendar.clone();
        int year = cal.get(YEAR);
        int month = cal.get(MONTH);
        DataInputStream stream = null;
        try {
            java.net.URL fileURL = Almanac.class.getResource("/resources/MoonVoidAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean find = false;
            do {
                long beginTime = stream.readLong();
                long endTime = stream.readLong();
                int ingSign = stream.readByte();
                cal.setTimeInMillis(endTime);
                if( !find && cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH)) {
                    rec = 13; find = true;
                }
                if( find ) {
                    Calendar begin = (Calendar)calendar.clone();
                    Calendar end = (Calendar)calendar.clone();
                    begin.setTimeInMillis(beginTime);
                    end.setTimeInMillis(endTime);
                    list.add(new VoidTime(begin,end,ingSign));
                    rec--;
                }
            } while(rec>=0);
        } catch(IOException e) {
            list = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return list;
    }
    

    /**
     * <pre>
     * calendarで指定された年(西暦年)月(0～11)に該当する
     * 月齢マップを返す。
     * 入力日付は年と月のみを認識し日は必ず1日とみなす。
     * MoonFaceAlmanacで作成した天文暦ファイルを参照する。
     * 天文暦の範囲外が指定されたときはnullを返す。
     * 
     * この戻り値のMapに対して、Map#get(日)とすると、新月、上弦、満月、下弦の日なら、
     * そのMoonFaceオブジェクトが返る。違うときはnullが返る。
     * 
     * MoonVoidAlmanacで作成したムーンフェイス天文暦ファイルを参照して、指定年月のリストを返す。
     * 天文暦ファイルへのパス。<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/MoonFaceAlmanac.bin");
     * </pre>
     */
    public static Map<Integer,MoonFace> getMoonFaceMap(Calendar calendar) {
        Map<Integer,MoonFace> moonFaceMap = new HashMap<Integer,MoonFace>();
        DataInputStream stream = null;
        Calendar cal = (Calendar)calendar.clone();
        try {
            //Calendar cal = Calendar.getInstance();
            java.net.URL fileURL = Almanac.class.getResource("/resources/MoonFaceAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            for(;;) {
                long time = stream.readLong();
                int face = stream.readByte();
                cal.setTimeInMillis(time);
                if( cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH) ) {
                    found = true;
                    int day = cal.get(DAY_OF_MONTH);
                    moonFaceMap.put(day,new MoonFace((Calendar)cal.clone(),face));
                }else if(found) break;
            }
        } catch(IOException e) {
            moonFaceMap = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return moonFaceMap;
    }

    /**
     * 指定されたcalendarの年(西暦年)月(0～11)に該当する
     * 天体イベント(イングレス、逆行、順行)のハッシュを返す。
     * 入力日付は年と月のみを認識し日は必ず1日とみなす。
     * PlanetAlmanacで作成した天文暦ファイル/resources/PlanetAlmanac.binを参照する。
     * 天文暦の範囲外が指定されたときはnullを返す。
     * 
     * 同日に複数のイベントが重なる事が想定されてるので、ハッシュの戻り値はListオブジェクト。
     * ListにはPlanetEventオブジェクトが入っている。
     * List#size()==0ということはありえない。イベントが無い日はnullが返る。
     * 
     * 天文暦ファイルへのパス。<br>
     * java.net.URL fileURL = MoonVoid.class.getResource("/resources/PlanetAlmanac.bin");
     */
    public static Map<Integer,List<PlanetEvent>> getPlanetEventMap(Calendar calendar) {
        Map<Integer,List<PlanetEvent>> map = new HashMap<Integer,List<PlanetEvent>>();
        DataInputStream stream = null;
        try {
            Calendar cal = (Calendar)calendar.clone();
            java.net.URL fileURL = Almanac.class.getResource("/resources/PlanetAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            for(;;) {
                long time = stream.readLong();
                int planet = stream.readByte();
                int state = stream.readByte();
                int sign = stream.readByte();
                cal.setTimeInMillis(time);
                if( cal.get(YEAR) == calendar.get(YEAR) && cal.get(MONTH) == calendar.get(MONTH) ) {
                    found = true;
                    int day = cal.get(DAY_OF_MONTH);
                    PlanetEvent pe = new PlanetEvent((Calendar)cal.clone(),planet,state,sign);
                    if(map.containsKey(day)) {
                        map.get(day).add(pe);
                    }else {
                        List<PlanetEvent> list = new ArrayList<PlanetEvent>();
                        list.add(pe);
                        map.put(day,list);
                    }
                }else if(found) break;
            }
        } catch(IOException e) {
            map = null;
        } finally {
            try { stream.close(); } catch(Exception e) { }
        }
        return map;
    }
    /**
     * 指定されたcalendarの日時から過去に向かって検索し一番近い新月の日を求め、
     * そこから次の新月の日までのエニアグラムのムーンフェイスをリスト返す。
     * 次の新月までなのでリストの要素は10個。
     * EnneaMoonAlmanacで作成した天文暦ファイル/resource/EnneaMoonAlmanac.binを参照している。
     * 天文暦の範囲外が指定されたときはnullを返す。
     */
    public static List<MoonFace> getEnneaMoonFace(Calendar calendar) {
        List<MoonFace> faceList = new ArrayList<MoonFace>();
        List<MoonFace> bufList = new ArrayList<MoonFace>();
        DataInputStream stream = null;
        //天文暦をbufListに一気に読みこむ
        try {
            URL fileURL = Almanac.class.getResource("/resources/EnneaMoonAlmanac.bin");
            stream = new DataInputStream(fileURL.openStream());
            int rec = 0;
            boolean found = false;
            while(stream.available() > 0 ) {
                long time = stream.readLong();
                int face = stream.readByte();
                Calendar cal = (Calendar)calendar.clone();
                cal.setTimeInMillis(time);
                bufList.add(new MoonFace(cal,face));
            }
        }catch(IOException e) {
            out.println(e);
        }finally {
            try {stream.close(); } catch(Exception e) { }
        }
        //引数calendarで指定された日時を越える地点までcountを進める
        int count = 0;
        for(;;) {
            MoonFace face = bufList.get(count);
            if(face.date.getTimeInMillis() > calendar.getTimeInMillis()) break;
            if(count++ >= bufList.size()) return null;
        }
        //見つけた日から遡って新月の日までcountを戻す
        try {
            do {
                count--;
            } while(bufList.get(count).face != 0);
        }catch(ArrayIndexOutOfBoundsException e) {
            return null;
        }               
        //新月から次の新月までの月の暦を戻り値用のリストに格納
        for(int i=0; i<10; i++) faceList.add(bufList.get(count+i));;
        return faceList;
    }
    /**
     * エニアグラムカレンダー用の天文暦全てをリストに入れて返す。
     */
    public static List<MoonFace> getEnneaAlmanac(TimeZone zone) {
      List<MoonFace> bufList = new ArrayList<MoonFace>();
      DataInputStream stream = null;
      try {
        URL fileURL = Almanac.class.getResource("/resources/EnneaMoonAlmanac.bin");
        stream = new DataInputStream(fileURL.openStream());
        while(stream.available() > 0 ) {
          long time = stream.readLong();
          int face = stream.readByte();
          Calendar cal = Calendar.getInstance(zone);
          cal.setTimeInMillis(time);
          bufList.add(new MoonFace(cal,face));
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try { stream.close(); } catch(Exception e) { }
      }
      return bufList;
    }
}

