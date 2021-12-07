/*
 * Natal.java
 *
 * Created on 2006/06/18, 8:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package to.tetramorph.starbase.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 個人や組織のデータを管理するときデータベースはこのオブジェクトを１ユニットと
 * して扱う。
 * Transitクラスの「時と場所、名前、メモ」に加えて「DB上のUID、チャートタイプ、
 * 性別、ヒストリーリスト、職業、名前の読み仮名、ノート、階層パス、タイムスタンプ、
 * トランジットの観測地」の要素を追加したクラス。
 */
public class Natal extends Transit implements java.io.Serializable {

    /**
     * コンポジットチャートを表す定数
     */
    public static final String COMPOSIT = "COMPOSIT";
    /**
     * イベントチャートを表す定数
     */
    public static final String EVENT = "EVENT";
    /**
     * ネイタルチャートを表す定数
     */
    public static final String NATAL = "NATAL";
    /**
     * 男性を表す定数
     */
    public static final int MALE = 1;
    /**
     * 女性を表す定数
     */
    public static final int FEMALE = 2;
    /**
     * 無性別を表す定数
     */
    public static final int NONE = 0;
    /**
     * IDがDB上には登録されていないことを表す定数で値は0。インスタンス作成時の初期値。
     */
    public static final int UNREGISTERED = 0;
    /**
     * このIDを持つNatalはDBに登録する必要がある事を表す定数で値は-1。
     */
    public static final int NEED_REGIST = -1;
    /** よみがな文字列の最大文字数 */
    public static final int KANA_MAX_LENGTH = 40;
    /** 職業名の最大も字数 */
    public static final int JOB_MAX_LENGTH = 28;
    int id = UNREGISTERED;
    String job = "";
    String kana = "";
    int gender = NONE;
    String chartType = Natal.NATAL;
    String note = "";
    Timestamp timestamp;
    List<Transit> historyList;
    List<Transit> compositList;
    String path = "";

    /**
     * 空のオブジェクトを作成する。
     * ヒストリーとコンポジットのリストはサイズ0のものが作成される。
     * タイムスタンプは現在時刻の値がセットされる。
     */
    public Natal() {
        super();
        historyList = new ArrayList<Transit>();
        compositList = new ArrayList<Transit>();
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    /**
     * デープコピーでオブジェクトの複製を作成する。
     * @param natal コピーするNatalオブジェクト
     */
    public Natal(Natal natal) {
        super(natal);
        id = natal.id;
        name = natal.name;
        job = natal.job;
        memo = natal.memo;
        kana = natal.kana;
        gender = natal.gender;
        chartType = natal.chartType;
        note = natal.note;
        timestamp = new Timestamp(natal.timestamp.getTime());
        path = natal.path;
        //ヒストリー情報を複製
        historyList = new ArrayList<Transit>();
        for (Transit evt : natal.historyList) {
            historyList.add(new Transit(evt));
        }
        //コンポジット情報を複製
        compositList = new ArrayList<Transit>();
        for (Transit evt : natal.compositList) {
            compositList.add(new Transit(evt));
        }
    }

    /**
     * データベース上で与えられているユニークIDを返す。デフォルトはゼロ。
     * データベース上でのIDは1番から始まる。
     */
    public int getId() {
        return id;
    }

    /**
     * データベース上で与えられるユニークIDをセットする
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 職業名を返す。
     */
    public String getJob() {
        return job;
    }

    /**
     * 職業名をセットする。""はセット可能。nullはセットしても""となる。
     * JOB_MAX_LENGTH以上の値をセットしようとするとIllegalArgumentException。
     */
    public void setJob(String job) {
        if (job != null) {
            if (job.length() > JOB_MAX_LENGTH) {
                throw new IllegalArgumentException("JOB length too long;" + job);
            }
        }
        this.job = (job == null) ? "" : job;
    }

//  /**
//   * メモを返す。
//   */
//  public String getMemo() {
//    return memo;
//  }
//
//  /**
//   * メモをセットする。
//   */
//  public void setMemo(String memo) {
//    this.memo = memo;
//  }
    /**
     * 名前のよみがなを返す。
     */
    public String getKana() {
        return kana;
    }

    /**
     * 名前のよみがなをセットする。""はセット可能。nullはセットしても""となる。
     * 28文字以上の値はIllegalArgumentException。
     */
    public void setKana(String kana) {
        if (kana != null) {
            if (kana.length() > KANA_MAX_LENGTH) {
                throw new IllegalArgumentException("KANA length too long;" + kana);
            }
        }
        this.kana = (kana == null) ? "" : kana;
    }

    /**
     * 性別を返す。戻り値はMALE,FEMALE,NONEのいずれか。
     */
    public int getGender() {
        return gender;
    }

    /**
     * 性別をセットする。MALE,FEMALE,NONEのいずれかをセットする。それ以外の値を
     * セットするとIllegalArgumentException。
     * @param gender MALE,FEMALE,NONEのいずれかをセットする。
     */
    public void setGender(int gender) {
        if (gender == MALE || gender == FEMALE || gender == NONE) {
            this.gender = gender;
        } else {
            throw new IllegalArgumentException("Illegal GENDER Code;" + gender);
        }
    }

    /**
     * 文字で性別をセットする。"0","1","2"のいずれかをセットする。それ以外の値を
     * セットするとIllegalArgumentException。
     */
    public void setGender(String value) {
        if (value.matches("0|1|2")) {
            this.gender = Integer.parseInt(value);
        } else {
            throw new IllegalArgumentException("Illegal GENDER Code;" + value);
        }
    }

    /**
     * チャートタイプを返す。NATAL,EVENT,COMPOSITのいずれかが返る。
     */
    public String getChartType() {
        return chartType;
    }

    /**
     * チャートタイプ名が同じならtrueを返す。
     * @param type "NATAL","COMPOSIT","EVENT"のいずれか。
     * @return 一致した場合はtrueを返す。
     */
    public boolean equalsChartType(String type) {
        return chartType.equalsIgnoreCase(type);
    }

    /**
     * チャートタイプをセットする。NATAL,EVENT,COMPOSITのいずれかをセットする。
     * それ以外の値をセットしようとするとIllegalArgumentException。
     */
    public void setChartType(String chartType) {
        if (chartType.equals(EVENT) || chartType.equals(NATAL) || chartType.equals(COMPOSIT)) {
            this.chartType = chartType;
        } else {
            throw new IllegalArgumentException("Illegal CHARTTYPE Code;" + chartType);
        }
    }

    /**
     * ノートを返す。
     */
    public String getNote() {
        return note;
    }

    /**
     * ノートをセットする。nullはセットしても""となる。
     */
    public void setNote(String note) {
        this.note = (note == null) ? null : note;
    }

    /**
     * タイムスタンプを返す。
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * タイムスタンプをセットする。nullをセットするとそのときの時刻のタイムスタンプ
     * がセットされる。
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = (timestamp == null)
                ? new Timestamp(System.currentTimeMillis()) : timestamp;
    }

    /**
     * このオブジェクトの文字列表現を返す。
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getChartType());
        sb.append(",");
        sb.append(getName());
        sb.append(",");
        sb.append(getKana());
        sb.append(",");
        if (getGender() == MALE) {
            sb.append("MALE");
        } else if (getGender() == FEMALE) {
            sb.append("FEMALE");
        } else {
            sb.append("NONE");
        }
        sb.append(",");
        sb.append(getJob());
        sb.append(",");
        sb.append(getMemo());
        sb.append(",");
        sb.append(super.toString());
        sb.append(",");
        sb.append(getNote());
        sb.append(",");
        sb.append(getTimestamp());
        return sb.toString();
    }

    /**
     * ヒストリーのリストをセットする。nullをセットしても無視される。
     */
    public void setHistory(List<Transit> historyList) {
        if (historyList != null) {
            this.historyList = historyList;
        }
    }

    /**
     * ヒストリーのリストを返す。
     * ヒストリーデータが空のときnullが返ることはなくsize()==0のリストが返る。
     */
    public List<Transit> getHistory() {
        return historyList;
    }

    /**
     * コンポジットのリストをセットする。nullをセットしても無視される。
     */
    public void setComposit(List<Transit> compositList) {
        if (compositList != null) {
            this.compositList = compositList;
        }
    }

    /**
     * コンポジットデータが空のときnullが返ることはなくsize()==0のリストが返る。
     */
    public List<Transit> getComposit() {
        return compositList;
    }

    /**
     * トランジットで観測地情報を返す。このデータはヒストリーの中に登録されていて、
     * getHistory()で取得したEventリストの中に"TRANSIT_PLACE"という名前をもつデータが
     * あればそれ。
     * この名前は、ヒストリーの編集パネル(HistoryEditPanel)の中では不可視になっていて、
     * リストに入っていても見えないようになっている(隠しファイルのような扱い)。
     * このメソッドはヒストリーのリストから"TRANSIT_PLCAE"という名前のEventデータ
     * を見つけてそのオブジェクトからPlaceオブジェクトを返す。
     */
    public Place getTransitPlace() {
        if ( historyList == null )
            return null;
        for ( int i = 0; i < historyList.size(); i++ ) {
            Transit ev = historyList.get(i);
            if ( ev.getName().equals("TRANSIT_PLACE") ) {
                return ev.getPlace();
            }
        }
        return null;
    }

    /**
     * JDBCのResultSetから所定のパラメターを取得してオブジェクト内にセットする。
     * ResultSetから取得する列名は次のとおり。
     * ID,CHARTTYPE,NAME,KANA,GENDER,JOB,MEMO,NOTE,TIMESTAMP,DATE,ERA,TIME,
     * PLACENAME,LONGITUDE,LATITUDE,TIMZONE。
     * PATHはセットされないので注意。
     */
    @Override
    public void setParams(ResultSet rs) throws SQLException {
        setId(rs.getInt("ID"));
        setChartType(rs.getString("CHARTTYPE"));
        setName(rs.getString("NAME"));
        setKana(rs.getString("KANA"));
        setGender(rs.getInt("GENDER"));
        setJob(rs.getString("JOB"));
        setMemo(rs.getString("MEMO"));
        setNote(rs.getString("NOTE"));
        setTimestamp(rs.getTimestamp("TIMESTAMP"));
        super.setParams(rs);
    }

    /**
     * DB内での階層パスをセットする。
     * パスというのはデータベースのTREEPAHT表で管理されているパスの名前。
     * これは検索したとき、このNatalがどの階層にあるかをセットするためのもの。
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * DB内での階層パスを返す。
     * これは検索したとき、このNatalがどの階層にあるかをセットするためのもの。
     */
    public String getPath() {
        return path;
    }
    // コンポジットまたはヒストリーのリストをCSV表現の文字列にして返す。

    private String getCSV(List<Transit> list) {
        StringBuilder sb = new StringBuilder();
        for ( Transit t : list ) {
            sb.append( t.getCSVTransit() );
            sb.append( "\n" );
        }
        if ( sb.length() > 0 )
            sb.deleteCharAt( sb.length() - 1) ;
        return sb.toString();
    }

    /**
     * このオブジェクトのCSV表現を返す。ただしTREEPATHのフィールド値はセット
     * されない。これは複数のTREEPATHが入りうるためで、TREEPATH表にIDで問い合わせて
     * 該当するパスをカンマで連結してセットする。このフィールドは概念的にNatalの外
     * にあるもの。
     */
    public String[] getCSV() {
        String[] cols = new String[17];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = "";
        }
        cols[BirthFileConst.CHARTTYPE] = getChartType();
        cols[BirthFileConst.NAME] = getName();
        cols[BirthFileConst.KANA] = getKana();
        cols[BirthFileConst.GENDER] = "" + getGender();
        cols[BirthFileConst.JOB] = getJob();
        cols[BirthFileConst.MEMO] = getMemo();
        cols[BirthFileConst.TREEPATH] = "";
        if (!getChartType().equals(Natal.COMPOSIT)) {
            cols[BirthFileConst.DATE] = getStringDate();
            cols[BirthFileConst.TIME] = getStringTime();
            cols[BirthFileConst.PLACENAME] = getPlaceName();
            cols[BirthFileConst.LATITUDE] = getStringLatitude();
            cols[BirthFileConst.LONGITUDE] = getStringLongitude();
            cols[BirthFileConst.TIMEZONE] = getTimeZone().getID();
            cols[BirthFileConst.COMPOSIT] = "";
        } else {
            cols[BirthFileConst.COMPOSIT] = getCSV(getComposit());
        }
        cols[BirthFileConst.HISTORY] = getCSV(getHistory());
        cols[BirthFileConst.NOTE] = getNote();
        cols[BirthFileConst.TIMESTAMP] = " " + getTimestamp().toString();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] == null) {
                System.out.println(BirthFileConst.HEADDER[i] + "がnullです");
            }
        }
        return cols;
    }
}
