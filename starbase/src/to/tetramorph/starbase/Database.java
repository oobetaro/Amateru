/*
 * Database.java
 *
 * Created on 2006/06/18, 8:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.tree.TreePath;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.starbase.util.StringEscape;
import to.tetramorph.time.DateFormat;
/**
 * DBからﾃﾞｰﾀを取り出す様々なﾒｿｯﾄﾞをもつｸﾗｽ。DBは階層構造でデータを管理している
 * が、そのときのパスの指定はTreePathクラスを使う。普通は次のようにする。
 * 文字列の配列からTreePathオブジェクトを指定する。また最初のrootはかならず必要。
 * <pre>
 * TreePath path = new String [] { "root","MyData" };
 * getList(path);
 * </pre>
 * <P>
 * ○　チャートモジュールの設定情報を管理するためのメソッド<br><br>
 * getConfigProperties(),setConfigProperties(),sortConfigProperties(),
 * renameConfigProperties(),removeConfigProperties()<br>
 * これらのメソッドは、COLOR_PROPERTIES表と同じ構造を持つテーブルに対して、
 * 所定の操作を行うメソッド群。設定情報によってテーブルを分けているので、
 * 操作したいテーブル名を指定して使用する。
 * ○　はじめてこのクラスのインスタンスが作成されるとき、Functionクラスのメソッド
 * をストアドプロシージャとして登録される。
 * @author 大澤義鷹
 */
class Database implements DB {
    /** 降り順ソート */
    public static final int DESCENDING = 2;
    /** ソートしない */
    public static final int NOT_SORTED = 0;
    /** 登り順ソート */
    public static final int ASCENDING = 1;
    private static final Database INSTANCE = new Database();
    private static Connection con;
    // insertNatal()等で天体位置計算でエラーがあったときにこれに保管される。
    private OutputStream ephErrorStream = null;

    private Database() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

        try {
            String driverURL = "jdbc:hsqldb:hsql://localhost";
            String pw = Config.usr.getProperty( "db.admin.pw","" );
            System.out.println("db.admin.pw = " + pw);
            con = DriverManager.getConnection( driverURL, "sa", pw );
            System.out.println("データベース接続完了");
            registFunction();
        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new IllegalStateException( e );
        }

        //シャットダウンのときに呼び出されるスレッドを登録
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //本番はHSQLDBにシャットダウン命令を送り正しく停止させる
                if(System.getProperty("nodb") == null && con != null) {
                    try {
                        PreparedStatement ps = con.prepareStatement("SHUTDOWN");
                        ps.executeUpdate();
                        System.out.println("HSQLDBにSHUTDOWNコマンド送信");
                    }catch(SQLException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if(con != null) {
                        con.close();
                        System.out.println("DBとの接続をクローズ");
                    }
                } catch( Exception e ) { e.printStackTrace(); }
            }
        });
    }

    public Connection getConnection() {
        return con;
    }
    /**
     * Databaseｵﾌﾞｼﾞｪｸﾄを返す。ｼﾝｸﾞﾙﾄﾝｸﾗｽなので、どこから呼び出されても
     * つねに同じｲﾝｽﾀﾝｽを返す。
     * @return ただ１つのDatabaseｵﾌﾞｼﾞｪｸﾄ
     */
    public static Database getInstance() {
        return INSTANCE;
    }
    /**
     * 引数でまとめてStatementやResultSetをクローズできる。
     * いちいちtry,catchで囲まなくてもクローズできる。
     *   <pre>例 close(rs,stmt);</pre>
     */
    public void close(Object ...o) {
        for(int i=0; i<o.length; i++) {
            if(o[i] instanceof Statement) {
                try { ((Statement)o[i]).close(); } catch( Exception e ) { }
            } else if(o[i] instanceof ResultSet) {
                try { ((ResultSet)o[i]).close(); } catch( Exception e ) { }
            } else if(o[i] instanceof Connection) {
                try { ((Connection)o[i]).close(); } catch( Exception e ) { }
            }
        }
    }
    /**
     * 指定されたグループ名のプロパティをDBから取り出し、Propertiesオブジェクトで
     * 返す。グループ名が存在しなくても空のPropertiesオブジェクトが戻るので、
     * このメソッドがnullを返すことはない。Properties#size()等で、空かどうかの
     * 識別を行うことができる。
     */
    public Properties getProperties(String name) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Properties p = new Properties();
        try {
            ps = con.prepareStatement("SELECT KEY,VALUE FROM PROPERTIES WHERE NAME=?");
            ps.setString(1,name);
            rs = ps.executeQuery();
            while(rs.next()) {
                String key = rs.getString("KEY");
                String value = rs.getString("VALUE");
                p.setProperty(key,value);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { rs.close(); } catch (Exception e) { }
            try { ps.close(); } catch (Exception e) { }
        }
        return p;
    }
    /**
     * Propertiesオブジェクトを指定された名前でDBに登録する。登録前に以前のデータ
     * は消去され新しいデータに置き換わる。
     */
    public void setProperties(String name,Properties prop) {
        PreparedStatement ps = null;
        try {
            //既存のものは削除。存在しなくても削除。
            ps = con.prepareStatement("DELETE FROM PROPERTIES WHERE NAME=?");
            ps.setString(1,name);
            ps.execute();
            ps.close();
            //登録
            ps = con.prepareStatement("INSERT INTO PROPERTIES(NAME,KEY,VALUE) VALUES (?,?,?)");
            Enumeration enu = prop.keys(); //キーのリストを取得
            while(enu.hasMoreElements()) { //プロパティを読み出しながらINSERT
                String key = (String)enu.nextElement();
                String value = prop.getProperty(key);
                ps.setString(1,name);
                ps.setString(2,key);
                ps.setString(3,value);
                ps.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch (Exception e) { }
        }
    }
    //getHistory,getCompositから呼ばれる。nullは返らない。
    private List<Transit> getEventList(int id,String table) {
        List<Transit> list = new ArrayList<Transit>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM " + table + " WHERE ID=?");
            ps.setInt(1,id);
            rs = ps.executeQuery();
            while(rs.next()) {
                Transit event = new Transit();
                event.setParams(rs);
                list.add(event);
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally { close(rs,ps); }
        return list;
    }
    //該当するIDをもつヒストリーのリストを返す。
    private List<Transit> getHistory(int id) {
        return getEventList(id,"HISTORY");
    }
    //該当するIDをもつコンポジットのリストを返す。
    private List<Transit> getComposit(int id) {
        return getEventList(id,"COMPOSIT");
    }
    /**
     * ﾕﾆｰｸIDで指定されたNatalﾃﾞｰﾀをDBのOCCASIONﾃｰﾌﾞﾙから取り出して返す。
     * 存在しない場合はnullを返す。
     * @param id ﾃﾞｰﾀを識別するﾕﾆｰｸID
     * @return Natalｵﾌﾞｼﾞｪｸﾄ
     */
    public Natal getNatal(int id) {
        Natal occ = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM OCCASION WHERE ID=?");
            ps.setInt(1,id);
            rs = ps.executeQuery();
            if(rs.next()) {
                occ = new Natal();
                occ.setParams(rs);
                occ.setHistory(getHistory(id));
                occ.setComposit(getComposit(id));
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(rs,ps);
        }
        return occ;
    }
    /**
     * 指定されたIDとパスに該当するNatalデータを返す。パスが一致しないとIDが存在
     * してもnullが返る。
     */
    public Natal getNatal(int id,String path) {
        Natal natal = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(
                "SELECT * FROM OCCASION,TREEPATH " +
                "WHERE " +
                " ID=? AND" +
                " OCCASION.ID = TREEPATH.ID AND" +
                " PATH = ?");
            ps.setInt(1,id);
            ps.setString(2,path);
            rs = ps.executeQuery();
            if(rs.next()) {
                natal = new Natal();
                natal.setParams(rs);
                natal.setHistory(getHistory(id));
                natal.setComposit(getComposit(id));
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(rs,ps);
        }
        return natal;

    }
    /**
     * TreePathで指定したﾌｫﾙﾀﾞの中にあるNatalﾃﾞｰﾀをすべて抽出して返す。
     * DBのﾃｰﾌﾞﾙTREEPATHから該当するIDsを調べ、のIDsでﾃｰﾌﾞﾙOCCASIONから抽出。
     * @param treepath ﾌｫﾙﾀﾞのﾊﾟｽ
     * @return NatalのList
     */
    public List<Natal> getList(TreePath treepath) {
        List<Natal> list = new ArrayList<Natal>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //ﾊﾟｽに含まれているID(複数)を求めて、そのIDをもつOCCASIONのﾃﾞｰﾀを抽出
            //String sql = "SELECT * FROM OCCASION WHERE ID IN ( SELECT ID FROM TREEPATH WHERE PATH='"+getTreePathString(treepath)+"')";
            String sql = "SELECT * FROM OCCASION WHERE ID IN "
                + "( SELECT ID FROM TREEPATH WHERE PATH=?)";
            ps = con.prepareStatement(sql);
            ps.setString(1,DBFactory.getPathString(treepath));
            rs = ps.executeQuery();
            while(rs.next()) {
                Natal occ = new Natal();
                occ.setParams(rs);
                list.add(occ);
            }
            for(int i=0; i<list.size(); i++) {
                Natal o = list.get(i);
                o.setHistory(getHistory(o.getId()));
                o.setComposit(getComposit(o.getId()));
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }finally{
            close(rs,ps);
        }
        return list;
    }

    /**
     * 指定されたパスのNatalデータのリストを取得する。
     * @param treePath パス
     * @param list このリストにデータは格納される。事前に消去される。
     * @param columnName ソートするときの列名
     * @param sortDirection ソートの向き。Database.の定数を参照。これがNOT_SORTED
     * ならば、columnNameは無視される。
     */
    public void getList(TreePath treePath,List<Natal> list,
        String columnName,int sortDirection) {
        //日付はERA、日、時、と列が分かれていてシリアルタイムを簡単に取得する方法が
        //なくDBにソートをまかせようとすると、シリアル値を保管している別の列を増やす
        //などの必要がある。しかし別表現のデータを二重にもつのは無駄だしテーブル構造
        //を変えると全体に影響が大きく大変だったりする。
        //そこで日付ソートのリクエストが来たときは、DBのソートは使わずDBから取得した
        //リストをユリウス日でソートしたものを返す。
        if(sortDirection != NOT_SORTED) {
            if(columnName.equals("DATE")) {
                List<Natal> temp = getList(treePath);
                //登り順(1,2,3,,,)にソート
                Collections.sort(temp,new NumericComparator());
                if(sortDirection == DESCENDING) Collections.reverse(temp);
                list.clear();
                for(int i=0; i<temp.size(); i++) list.add(temp.get(i));
                return;
            }
        }
        //日付以外はDBのソート機能にお願いする。
        list.clear();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM OCCASION ");
        sb.append("WHERE ID IN ( SELECT ID FROM TREEPATH WHERE PATH=?)");
        if(sortDirection != NOT_SORTED ) {
            sb.append(" ORDER BY ");
            sb.append(columnName);
            sb.append(" ");
            String dir = sortDirection == DESCENDING ? "DESC" : "ASC";
            sb.append(dir);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sb.toString());
            ps.setString(1,DBFactory.getPathString(treePath));
            rs = ps.executeQuery();
            while(rs.next()) {
                Natal occ = new Natal();
                occ.setParams(rs);
                list.add(occ);
            }
            for(int i=0; i<list.size(); i++) {
                Natal o = list.get(i);
                o.setHistory(getHistory(o.getId()));
                o.setComposit(getComposit(o.getId()));
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(rs,ps);
        }
    }
    //Natal.getCalendar()の値でソートするために必要なクラス。
    //はじめは.getJDay()を使ったがとても時間がかかったのでシリアルタイムを使用
    private class NumericComparator implements Comparator<Natal> {
        public int compare( Natal n1, Natal n2 ) {
            // コンポジットデータはカレンダーがnullなので特別処理
            boolean b1 = n1.getChartType().equals( Natal.COMPOSIT );
            boolean b2 = n2.getChartType().equals( Natal.COMPOSIT );
            if (   b1 &&   b2 ) return  0;
            if (   b1 && ! b2 ) return -1;
            if ( ! b1 &&   b2 ) return  1;
            // どちらもコンポジットではないなら日付を比較
            Long v1 = n1.getCalendar().getTimeInMillis();
            Long v2 = n2.getCalendar().getTimeInMillis();
            return v1.compareTo(v2);
        }
    }

    /**
     * 指定パスの下にあるサブフォルダ一覧を返す。このメソッドは未使用。
     */
    public List<String> getSubFolders(TreePath treepath) {
        List<String> list = new ArrayList<String>();
        ResultSet rs = null;
        Statement st = null;
        try {
            String path = DBFactory.getPathString(treepath);
            String sql = "SELECT DISTINCT PATH FROM TREEPATH WHERE ";
            if(path.equals(""))
                sql += "PATH = ''";
            else
                sql += "PATH LIKE '" + path + "%'";
            rs = con.createStatement().executeQuery(sql);
            while(rs.next()) {
                String p = rs.getString("PATH"); //NULLが返らないことは保証されている
                if(p.equals("")) continue;
                String token[] = p.substring(path.length()).split("/");
                if(token.length >= 2) list.add(token[1]);
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(rs,st);
        }
        return list;
    }
    private static FolderTreeNode rootNode = null;
    /**
     * このインスタンスが保持しているTREEPATH表のツリー構造を表現するノードを返す。
     * 別窓でツリーを表示するダイアログを開く場合があるが、共通のインスタンスを
     * 使っておかないと、equalsをつかった同値判定や、親子判定メソッドで困ったこ
     * とになる。
     */
    public FolderTreeNode getTree() {
        if(rootNode != null) return rootNode;
        return getUpdateTree();
    }
    /**
     * このインスタンスが保持しているTREEPATH表のツリー構造を表現するノードを
     * 再作成して返す。これはインポートが行われたとき、ツリーを作り直す必要があり、
     * そのときに使用される。
     */
    public FolderTreeNode getUpdateTree() {
        rootNode = new FolderTreeNode("データ");
        rootNode.add(new DustBoxTreeNode()); //最初に「ごみ箱」をかならず入れる
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery(
                "SELECT DISTINCT PATH FROM TREEPATH WHERE PATH !=''");
            while ( result.next() ) {
                String path = result.getString("PATH");
                DBFactory.addNode(path.split("/"),rootNode);
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(result,stmt);
        }
        return rootNode;
    }
//    /**
//     * path[]をrooNodeに追加する。パスからﾂﾘｰ構造にﾉｰﾄﾞを編み上げる。
//     * ﾌｧｲﾙｼｽﾃﾑと同様に同一階層に同名のﾌｫﾙﾀﾞは存在できないというﾙｰﾙにのっとる。
//     * @param path ﾊﾟｽを表現する配列。
//     * @param rootNode ﾙｰﾄにするﾉｰﾄﾞのｲﾝｽﾀﾝｽ。nullは禁止。
//     * ※このメソッドはDBFactoryに移動。このクラスからは廃止予定。
//     */
//    public static void addNode(Object [] path,FolderTreeNode rootNode) {
//        FolderTreeNode temp = null;
//        for(int i=0; i<path.length; i++) {
//            boolean exists = false;
//            for(int j=0; j<rootNode.getChildCount(); j++) {
//                temp = (FolderTreeNode)rootNode.getChildAt(j);
//                if(temp.toString().equals(path[i].toString())) {
//                    exists = true;
//                    break;
//                }
//            }
//            if(! exists) {
//                temp = new FolderTreeNode(path[i]);
//                //addするときごみ箱にaddするような事態が発生するとExceptionが出る。
//                //それを拾って異常なパス(/ごみ箱/なんとか/)を検出するべかもしれないが
//                //今のところは未対応
//                rootNode.add(temp);
//            }
//            rootNode = temp;
//        }
//    }
    /**
     * getPathString()で作成された文字列のPATHをTreePathに変換して返す。
     * 取り去られていたrootが頭に追加されたTreePathを返す。
     * TreePathの中身はFolderTreeNodeの配列が格納されている。
     * insertNatal()に指定するTreePathを作成するのには使えるが、JTreeには使えない。
     * ただ名前だけつじつまがあっているTreePathにすぎない。正しいTreePathを得たい
     * 場合は、DatabaseFrame.foundTreePath(String path)を使用する。
     */
    protected static TreePath getTreePath(String path) {
        String [] temp = path.split("/");
        FolderTreeNode [] tokens = new FolderTreeNode[temp.length + 1];
        for(int i=0; i<temp.length; i++)
            tokens[i+1] = new FolderTreeNode(temp[i]);
        tokens[0] = new FolderTreeNode("root");
        return new TreePath(tokens);
    }

    /**
     * DBにNatalを登録する。OCCASION表、TREEPATH表,HISTORY表にわけて挿入。
     * @param occ 登録するNatalオブジェクト
     * @param treePath 登録するパス
     * @return 挿入したときのID番号。1かそれ以上の値が返る。0のときはなんらかの
     * エラーがあったことを意味する。
     */
    public int insertNatal(Natal occ,TreePath treePath) {
        String sql = "INSERT INTO OCCASION(CHARTTYPE,NAME,KANA,GENDER,JOB,MEMO,"
            + "ERA,DATE,TIME,PLACENAME,LATITUDE,LONGITUDE,TIMEZONE,NOTE,"
            + "TIMESTAMP,ID,JDAY) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        int id = 0;
        boolean isComposit = occ.getChartType().equals(Natal.COMPOSIT);
        try {
            //ユニークIDを求める
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT MAX(ID)+1 FROM OCCASION");
            //int id = -1;
            if(rs.next()) id = rs.getInt(1);
            else throw new SQLException("ID Not Found");
            //SQL文にパラメターセットして挿入
            ps = con.prepareStatement(sql);
            ps.setString(1,occ.getChartType());
            ps.setString(2,occ.getName());
            ps.setString(3,occ.getKana());
            ps.setInt(4,occ.getGender());
            ps.setString(5,occ.getJob());
            ps.setString(6,occ.getMemo());
            if(isComposit) {
                ps.setNull(7,Types.NULL);
                ps.setNull(8,Types.NULL);
                ps.setNull(9,Types.NULL);
                ps.setNull(13,Types.NULL);
            } else {
                ps.setString(7,occ.getERA());
                ps.setDate(8,occ.getDate());
                ps.setTime(9,occ.getTime());
                ps.setString(13,occ.getTimeZone().getID());
            }
            ps.setString(10,occ.getPlaceName());
            if(occ.getLatitude() == null || occ.getLongitude() == null ||
                occ.getChartType() == Natal.COMPOSIT) {
                ps.setNull(11,Types.NULL);
                ps.setNull(12,Types.NULL);
            } else {
                ps.setDouble(11,occ.getLatitude());
                ps.setDouble(12,occ.getLongitude());
            }
            ps.setString(14,occ.getNote());
            ps.setTimestamp(15,new Timestamp(System.currentTimeMillis()));
            ps.setInt(16,id);
            double jd = 0;
            if(isComposit) ps.setNull(17,Types.NULL);
            else {
                jd = occ.getJDay();
                ps.setDouble(17,jd);
            }
            ps.executeUpdate();
            //TREEPATH表に登録
            ps2 = con.prepareStatement("INSERT INTO TREEPATH(ID,PATH) VALUES(?,?)");
            ps2.setInt(1,id);
            ps2.setString(2,DBFactory.getPathString(treePath));
            ps2.executeUpdate();
            //HISTORY表に登録
            insertEvent(occ.getHistory(),id,"HISTORY");
            //コンポジット表もほぼ同上
            if(isComposit) {
                insertEvent(occ.getComposit(),id,"COMPOSIT");
            } else {
                this.registPlanets(jd); //惑星位置を登録
                if(occ.isCompleteTimePlace()) //ハウスまで計算可能ならハウスも登録
                    registHouse(id,jd,occ.getLatitude(),occ.getLongitude());
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
            id = 0;
        } finally {
            close(rs,stmt,ps,ps2);
        }
        return id;
    }
    /**
     * 複数ﾊﾟｽをもつNatalの登録。これは主にインポート用。csvPathにはカンマで区切
     * られパスが複数登録されている場合がある。
     * 複数のときはパスのうち最初の一つをinsertNatal(Natal,TreePath)を呼び出して
     * 登録し、のこりのパスはTREEPATH表に追加で登録する。
     * 挿入に成功した場合はその際にDBから付与されたIDを返す。
     * 0が戻ったときはなんらかのエラーが発生した。
     */
    public int insertNatal(Natal natal,String csvPath) {
        String [] paths = csvPath.split(",");
        int id = insertNatal(natal,getTreePath(paths[0]));
        if(paths.length == 1) return id;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("INSERT INTO TREEPATH(ID,PATH) VALUES(?,?)");
            for(int i=1; i<paths.length; i++) {
                ps.setInt(1,id);
                ps.setString(2,paths[i].trim());
                ps.executeUpdate();
                ps.clearParameters();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            id = 0;
        } finally {
            close(ps);
        }
        return id;
    }
    //Natalの中のEventのListを指定の表に挿入。
    private void insertEvent(List<Transit> historyList,int id,String tableName) {
        String sql = "INSERT INTO " + tableName + "(ID,NAME,MEMO,ERA,DATE,TIME,"
            + "PLACENAME,LATITUDE,LONGITUDE,TIMEZONE,JDAY) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            for(Transit ev : historyList) {
                ps.setInt(1,id);
                ps.setString(2,ev.getName());
                ps.setString(3,ev.getMemo());
                ps.setString(4,ev.getERA());
                ps.setDate(5,ev.getDate());
                ps.setTime(6,ev.getTime());
                ps.setString(7,ev.getPlaceName());
                if(ev.getLatitude() == null) ps.setNull(8,Types.NULL);
                else ps.setDouble(8,ev.getLatitude());
                if(ev.getLongitude() == null) ps.setNull(9,Types.NULL);
                else ps.setDouble(9,ev.getLongitude());
                ps.setString(10,ev.getTimeZone().getID());
                ps.setDouble(11,ev.getJDay());
                //System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }
    //OCCASION表、HISTORY表、COMPOSIT表に対して、時間と場所を更新する。
    //が、しかし今のとこOCCASION表のみにしか使っていない。
    private void updateTimePlace(Natal occ,String tableName) {
        if(occ.getChartType().equals(Natal.COMPOSIT)) return; //コンポジットは変更不要
        String sql = "UPDATE " + tableName
            + " SET ERA=?,DATE=?,TIME=?,PLACENAME=?,LATITUDE=?,LONGITUDE=?,"
            + "TIMEZONE=?,JDAY=? WHERE ID=?";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            if(occ.getChartType() == Natal.COMPOSIT) {
                ps.setNull(8,Types.NULL);
                ps.setNull(1,Types.NULL);
                ps.setNull(2,Types.NULL);
                ps.setNull(3,Types.NULL);
                ps.setNull(7,Types.NULL);
            } else {
                ps.setDouble(8,occ.getJDay());
                ps.setString(1,occ.getERA());
                ps.setDate(2,occ.getDate());
                ps.setTime(3,occ.getTime());
                ps.setString(7,occ.getTimeZone().getID());
            }
            ps.setString(4,occ.getPlaceName());
            if(occ.getLatitude() == null || occ.getLongitude() == null ||
                occ.getChartType().equals( Natal.COMPOSIT)) {
                ps.setNull(5,Types.NULL);
                ps.setNull(6,Types.NULL);
            } else {
                ps.setDouble(5,occ.getLatitude());
                ps.setDouble(6,occ.getLongitude());
            }
            ps.setInt(9,occ.getId());
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }
    /**
     * DB上のNatalを更新する。occ.getId()で更新対象を識別。
     */
    public void updateNatal(Natal occ) {
        String sql = "UPDATE OCCASION SET NAME=?,KANA=?,GENDER=?,JOB=?,MEMO=?,"
            + "NOTE=?,TIMESTAMP=? WHERE ID=?";
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        try {
            //OCCASION表を更新
            ps = con.prepareStatement(sql);
            ps.setString(1,occ.getName());
            ps.setString(2,occ.getKana());
            ps.setInt(3,occ.getGender());
            ps.setString(4,occ.getJob());
            ps.setString(5,occ.getMemo());
            ps.setString(6,occ.getNote());
            ps.setTimestamp(7,new Timestamp(System.currentTimeMillis()));
            ps.setInt(8,occ.getId());
            ps.executeUpdate();
            updateTimePlace(occ,"OCCASION"); //時と場所も更新
            //ヒストリーは一旦削除して再度挿入する。
            ps2 = con.prepareStatement("DELETE FROM HISTORY WHERE ID=?");
            ps2.setInt(1,occ.getId());
            ps2.executeUpdate();
            insertEvent(occ.getHistory(),occ.getId(),"HISTORY");
            //コンポジットも同じ
            ps3 = con.prepareStatement("DELETE FROM COMPOSIT WHERE ID=?");
            ps3.setInt(1,occ.getId());
            ps3.executeUpdate();
            insertEvent(occ.getComposit(),occ.getId(),"COMPOSIT");
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(ps,ps2,ps3);
        }
    }
    /**
     * insertNatal()等でスイスエフェメリスのエラーが発生した際に、その内容を出力
     * するストリームをセットする。nullをセットするとSystem.outに出力する。
     * デフォルトはnull。
     */
    public void setEphemerisErrorStream(OutputStream os) {
        ephErrorStream = os;
    }
    //ユリウス日の惑星位置を登録(ハウスは別)
    private void registPlanets( double jday ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            //データが登録済か確認
            ps = con.prepareStatement("SELECT COUNT(JDAY) FROM PLANETS_LONGITUDE WHERE JDAY=?");
            ps.setDouble(1,jday);
            rs = ps.executeQuery();
            rs.next();
            if(rs.getInt(1) >= 1) return;
            ps.close();
            rs.close();
            //未登録なら登録する
            ps = con.prepareStatement(
                "INSERT INTO PLANETS_LONGITUDE VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            Ephemeris eph = Ephemeris.getInstance(); //new Ephemeris();
            ps.setDouble(1,jday);
            Map<Integer,Body> map = eph.getBodyMap(jday);
            //エラーが起きた日付とエラー内容を出力
            if(eph.isError()) {
                for(String errmsg :eph.getErrorList()) {
                    String date = DateFormat.getDateString(jday) + " " + errmsg.trim();
                    if(ephErrorStream != null) {
                        try {
                            byte [] strbuf = date.getBytes();
                            ephErrorStream.write(strbuf,0,strbuf.length);
                        } catch(IOException e) {}
                    } else System.out.println(date);
                }
            }
            //
            for(int i=Const.SUN,j=2; i <= Const.ANTI_OSCU_APOGEE; i++) {
                if(i == Const.EARTH) continue; //地球は除外
                Body body = map.get(i);
                if(body != null) {
                    ps.setFloat(j,(float)body.lon);
                } else {
                    ps.setNull(j,java.sql.Types.NULL);
                }
                j++;
            }
            ps.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try { ps.close(); } catch(Exception e) { }
            try { rs.close(); } catch(Exception e) { }
        }
    }
    //指定IDでHOUSE表にハウスの位置を登録。"HouseSystemIndex"プロパティが設定され
    //ていないとIllegalStateExceptonがでる。ﾌﾟﾛﾊﾟﾃｨにｿｰﾗｰとｿｰﾗｰﾊｳｽはダメ。
    private void registHouse(int id,double jday,double lat,double lon) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT ID FROM HOUSE WHERE ID=?");
            ps.setInt(1,id);
            rs = ps.executeQuery();
            boolean exist = rs.next();
            if(exist) return;
            ps.close();
            rs.close();
            ps = con.prepareStatement(
                "INSERT INTO HOUSE VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            Ephemeris eph = Ephemeris.getInstance(); //new Ephemeris();
            ps.setDouble(1,id);
            //ハウスシステムをプロパティから取得
            String hsys = Config.usr.getProperty( "HouseSystemIndex", "" );
            if ( hsys.isEmpty() )
                throw new IllegalStateException("HouseSystemIndexプロパティが未設定。");
            int hsc = HOUSE_SYSTEM_CODES[ Integer.parseInt(hsys) ];
            //ハウスの位置を求める
            Map<Integer,Body> map = eph.getBodyMap(jday,lat,lon,hsc);
            for(int i=AC,j=2; i<=CUSP12; i++) {
                if(PLANET_NAMES[i] == null) continue;
                if(eph.isError()) { //エラーが起きた日付とエラー内容を出力
                    System.out.println(DateFormat.getDateString(jday));
                    for(String errmsg :eph.getErrorList()) {
                        System.out.println("\t" + errmsg);
                    }
                }
                Body body = map.get(i);
                if(body != null) ps.setFloat(j,(float)body.lon);
                else ps.setNull(j,java.sql.Types.NULL);
                j++;
            }
            ps.execute();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch(Exception e) { }
            try { rs.close(); } catch(Exception e) { }
        }
    }

    /**
     * 所定IDのヒストリーを更新する。内部的には一旦DBから削除して再登録する。
     * ChartInternalFrameから一度だけ呼ばれている。
     * @param id Occason.getID()で得られるユニークID
     * @param historyList Eventを格納したList。
     */
    public void updateHistory(int id,List<Transit> historyList) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("DELETE FROM HISTORY WHERE ID=?");
            ps.setInt(1,id);
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
        insertEvent(historyList,id,"HISTORY");
    }
    /**
     * フォルダがリネームされたときに呼び出される。\x87B
     */
    public boolean renameFolder(TreePath oldTreePath,TreePath newTreePath) {
        System.out.printf("rename old %s --> new %s\n",oldTreePath,newTreePath);
        String oldPath = DBFactory.getPathString( oldTreePath );
        String newPath = DBFactory.getPathString( newTreePath );
//        String sql = "";
        List<String> list = new ArrayList<String>();
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        PreparedStatement ps0 = null;
        boolean renamed = true;
        try {
            stmt = con.createStatement();
//            rs = stmt.executeQuery(
//                "SELECT DISTINCT PATH FROM TREEPATH WHERE PATH LIKE '" +
//                oldPath + "%'" );
            //rs = stmt.executeQuery( sql );
            ps0 = con.prepareStatement("SELECT DISTINCT PATH FROM TREEPATH WHERE PATH = ? OR PATH LIKE ?");
            ps0.setString( 1, oldPath );
            ps0.setString( 2, oldPath + "/%" );
            rs = ps0.executeQuery();
            while ( rs.next() )
                list.add( rs.getString("PATH") );
            ps = con.prepareStatement(
                "UPDATE TREEPATH SET PATH=? WHERE PATH=?" );
            for ( String oldpath : list ) {
                String subpath = oldpath.substring( oldPath.length() );
                String newpath = newPath.concat( subpath );
                ps.setString( 1, newpath );
                ps.setString( 2, oldpath );
                ps.executeUpdate();
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
            renamed = false;
        } finally {
            close(rs,stmt,ps,ps0);
        }
        return renamed;
    }
    /**
     * /aaa/bbb というフォルダが /ccc/dddに移動しろというように引数で指示され
     * ると、/ccc/ddd/の中に./bbbを入れるから、/ccc/ddd/bbbとなる。
     * \x87E番目に実装
     */
    //
    public boolean moveFolder(TreePath oldTreePath,TreePath newTreePath) {
        TreePath targetTreePath = newTreePath.pathByAddingChild(oldTreePath.getLastPathComponent());
        System.out.printf("move old %s --> new %s\n",oldTreePath,targetTreePath);
        //フォルダの移動は実のところリネームと同じでパス名を置換するだけ
        return renameFolder(oldTreePath,targetTreePath);
    }
    /**
     * 指定されたフォルダの中にあるデータのIDのリストを返す。フォルダを削除・移動
     * リネームする際に、チャート表示中のIDがないか確認するひつようがあり、そのと
     * きに使用する。
     */
    public List<Integer> getIDinFolder( TreePath targetTreePath ) {
        String targetPath = DBFactory.getPathString( targetTreePath );
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Integer> list = new ArrayList<Integer>();
        try {
            ps = con.prepareStatement(
                "SELECT ID FROM TREEPATH WHERE ID IS NOT NULL AND PATH LIKE ?");
            ps.setString( 1, targetPath + "%" );
            rs = ps.executeQuery();
            while ( rs.next() )
                list.add( rs.getInt("ID") );
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close( rs, ps );
        }
        return list;
    }
    //フォルダごと削除(削除したデータはゴミ箱へ)\x87F
    public boolean removeFolder(TreePath targetTreePath) {
        System.out.printf("remove %s\n",targetTreePath);
        String targetPath = DBFactory.getPathString(targetTreePath);
        String sql = "";
        boolean result = false;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            //ビュー表を作る前に存在確認し、存在するなら削除する
            stmt.executeUpdate("DROP VIEW FOLDER IF EXISTS RESTRICT");
            //削除するデータを仮想表(TREEPATH表と構造は同じ)にする。パスは前方一致で判定する。つまり多階層をまとめて削除もできる。
            sql = "CREATE VIEW FOLDER AS SELECT * FROM TREEPATH " +
                  "WHERE PATH = '" + targetPath + "' OR " +
                  "PATH LIKE '" + targetPath + "/%'";
            stmt.executeUpdate(sql);
            //「ゴミ箱」のパスを持つ同IDが、複数生じるのを回避したい。(ゴミ箱,ID=5,ゴミ箱,ID=5等)
            //そのために今回ゴミ箱に移動させる予定のデータ(IDで識別)を、ゴミ箱の中から削除する。
            stmt.executeUpdate("DELETE FROM TREEPATH WHERE PATH='ごみ箱' AND ID IN ( SELECT ID FROM FOLDER)");
            //仮想表のパスを全部「ごみ箱」にする(これによって削除したことになる)
            //はずだが、HSQLDBではビューに対して更新等は未対応らしいので普通に
            //ID未登録の行、つまり空フォルダを表す行だけ先に削除
            sql = "DELETE FROM TREEPATH WHERE PATH = '" + targetPath + "' OR " +
                  "PATH LIKE '" + targetPath + "/%' AND ID IS NULL";
            stmt.executeUpdate(sql);
            //のこりはパス名をゴミ箱に置換。つまりゴミ箱に移す。
            sql = "UPDATE TREEPATH SET PATH='ごみ箱' " +
                  "WHERE PATH = '" + targetPath + "' OR " +
                        "PATH LIKE '" + targetPath + "/%'";
            stmt.executeUpdate( sql );
            stmt.executeUpdate( "DROP VIEW FOLDER RESTRICT" );//仮想表削除
            result = true;
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(stmt);
        }
        return result;
    }
    /**
     * コピー。
     * source = /aaa/bbb, target = /aaa/cccというようにパスが与えられた場合、
     * ./bbbを./cccとしてコピーすることを意味する
     * \x87G番目に実装
     */
    public boolean copyFolder(TreePath source,TreePath target) {
        System.out.printf("copy old %s --> new %s\n",source,target);
        String sourcePath = DBFactory.getPathString(source);
        String targetPath = DBFactory.getPathString(target);
        Statement stmt = null;
        boolean copyed = false;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("DROP TABLE TEMP_TREEPATH IF EXISTS");
            stmt.executeUpdate("CREATE MEMORY TABLE TEMP_TREEPATH( ID INTEGER,PATH VARCHAR DEFAULT '' NOT NULL)");
            stmt.executeUpdate(String.format("INSERT INTO TEMP_TREEPATH SELECT * FROM TREEPATH WHERE PATH='%s'",sourcePath));
            stmt.executeUpdate(String.format("UPDATE TEMP_TREEPATH SET PATH='%s'",targetPath));
            stmt.executeUpdate("INSERT INTO TREEPATH SELECT * FROM TEMP_TREEPATH");
            stmt.executeUpdate("DROP TABLE TEMP_TREEPATH");
            copyed = true;
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(stmt);
        }
        return true;
    }
    /**
     * TREEPATH表でごみ箱と他の名前というように複数のパスをもつIDはゴミ箱のパス
     * のみを削除し、ごみ箱のみのパスしかもたないIDはOCCASION表からも削除する。
     */
    public boolean clearDustBox(TreePath dustBoxTreePath) {
        String dustBoxPath = DBFactory.getPathString(dustBoxTreePath);
        System.out.println(dustBoxPath);
        Statement stmt = null;
        boolean cleared = false;
        try {
            stmt = con.createStatement();
            StringBuffer sb = new StringBuffer();
            //ID_A表を作りそこにごみ箱に入っているIDsを集める
            sb.append("DROP TABLE ID_A IF EXISTS ");
            sb.append("CREATE MEMORY TABLE ID_A(ID INTEGER) ");
            sb.append("INSERT INTO ID_A ");
            sb.append("  SELECT DISTINCT ID FROM TREEPATH ");
            sb.append("  WHERE PATH='" + dustBoxPath + "' AND ID >= 0");
            //ID_B表を作りごみ箱以外のパスをもっているIDをID_A表から抽出。
            sb.append("DROP TABLE ID_B IF EXISTS\n");
            sb.append("CREATE MEMORY TABLE ID_B(ID INTEGER)\n");
            sb.append("INSERT INTO ID_B ");
            sb.append("  SELECT DISTINCT ID FROM TREEPATH ");
            sb.append("  WHERE PATH<>'" + dustBoxPath + "' AND ID IN (SELECT * FROM ID_A)\n");
            //ごみ箱のパスをもつIDをTREEPATH表から削除
            sb.append("DELETE FROM TREEPATH WHERE PATH='" + dustBoxPath + "' AND ID >= 0");
            //ID_A表からID_B表にあるIDを削除。これでID_A表が本当に削除すべきIDsとなる。
            sb.append("DELETE FROM ID_A WHERE ID IN (SELECT * FROM ID_B)\n");
            //OCCASION表からの削除前に、HISTROY,COMPOSIT表からも削除
            sb.append("DELETE FROM HISTORY WHERE ID IN(SELECT * FROM ID_A)\n");
            sb.append("DELETE FROM COMPOSIT WHERE ID IN(SELECT * FROM ID_A)\n");
            sb.append("DELETE FROM OCCASION WHERE ID IN(SELECT * FROM ID_A)\n");
            //ハウス表からも削除
            sb.append("DELETE FROM HOUSE WHERE ID IN(SELECT * FROM ID_A)\n");
            sb.append("DROP TABLE ID_A\n");
            sb.append("DROP TABLE ID_B\n");
            stmt.executeUpdate(sb.toString());
            cleared = true;
        }catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(stmt);
        }
        //event.getTable().showList(event.getTree().getSelectionPath());
        return true;
    }
    /** フォルダを作成する。\x87A */
    public boolean makeFolder(TreePath newPath) {
        System.out.printf("mkdir %s\n",newPath);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("INSERT INTO TREEPATH(PATH) VALUES(?)");
            ps.setString(1,DBFactory.getPathString(newPath));
            ps.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
        return true;
    }

    /**
     * フォルダの中にあるNatalデータを削除する。実際にはごみ箱に移したことになる
     * だけでOCCASION表からは削除はされていない。最初に実装\x87@
     */
    public boolean remove(Iterator ite,TreePath currentPath) {
        String sql = "";
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        PreparedStatement ps5 = null;
        ResultSet rs = null;
        boolean removed = false;
        try {
            ps = con.prepareStatement("DELETE FROM TREEPATH WHERE ID=? AND PATH=?");
            ps2 = con.prepareStatement("SELECT COUNT(*) FROM TREEPATH WHERE ID=?");
            ps3 = con.prepareStatement("INSERT INTO TREEPATH VALUES(?,'ごみ箱')");
            ps4 = con.prepareStatement("SELECT COUNT(*) FROM TREEPATH WHERE PATH=?");
            ps5 = con.prepareStatement("INSERT INTO TREEPATH(PATH) VALUES(?)");
            while(ite.hasNext()) {
                Natal occ = (Natal)ite.next();
                String path = DBFactory.getPathString(currentPath);
                ps.setInt(1,occ.getId());
                ps.setString(2,DBFactory.getPathString(currentPath));
                ps.executeUpdate();
                ps2.setInt(1,occ.getId());
                rs = ps2.executeQuery();
                if(rs.next()) {
                    if(rs.getInt(1) == 0) {
                        ps3.setInt(1,occ.getId());
                        ps3.executeUpdate();
                    }
                }
            }
            //パスを全部消してしまうとフォルダが消滅するので再挿入
            ps4.setString(1,DBFactory.getPathString(currentPath));
            rs = ps4.executeQuery();
            if(rs.next()) {
                if(rs.getInt(1) == 0) {
                    ps5.setString(1,DBFactory.getPathString(currentPath));
                    ps5.executeUpdate();
                }
            }
            removed = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            close(rs,ps,ps2,ps3,ps4,ps5);
        }
        return removed;
    }
    /** 移動 */
    public boolean move(Iterator ite,TreePath currentTreePath,
        TreePath targetTreePath) {
        String sql = "";
        String currentPath = DBFactory.getPathString(currentTreePath);
        String targetPath = DBFactory.getPathString(targetTreePath);
        Statement stmt = null;
        boolean moved = false;
        try {
            stmt = con.createStatement();
            while(ite.hasNext()) {
                Natal occ = (Natal)ite.next();
                if(exists(occ.getId(),targetPath)) {
                    //異動先にすでに存在している場合(複数のフォルダに同じデータが存在
                    //している場合は、移動元のデータを削除する
                    sql = String.format("DELETE FROM TREEPATH WHERE ID=%d AND PATH='%s'",
                        occ.getId(),currentPath);
                } else {
                    //異動先に存在しない場合は、パス名を変更
                    sql = String
                        .format("UPDATE TREEPATH SET PATH='%s' WHERE ID=%d AND PATH='%s'",
                        targetPath,occ.getId(),currentPath);
                }
                stmt.executeUpdate(sql);
            }
            moved = true;
        }catch(SQLException e) {
            e.printStackTrace();
        }finally {
            close(stmt);
        }
        return moved;
    }
    // rows[]をforでまわしながら、
    // SELECT COUNT(*) FROM TREEPATH WHERE ID=occ.id AND PATH='oldPath'
    // の値が0であることを確認したら、
    // INSERT INTO TREEPATH VALUES(occ.id,'newPath)'
    //\x87D
    /**
     * このコピーは、コピーもとフォルダ内のいくつかのデータが選択されたものを、指定
     * フォルダにコピーすることしか考慮していない。
     */
    public boolean copy(Iterator ite,TreePath targetTreePath) {
        String sql = "";
        String targetPath = DBFactory.getPathString(targetTreePath);
        PreparedStatement ps = null;
        boolean copyed = false;
        try {
            ps = con.prepareStatement("INSERT INTO TREEPATH VALUES(?,?)");
            while(ite.hasNext()) {
                Natal occ = (Natal)ite.next();
                if(! exists(occ.getId(),targetPath)) {
                    ps.setInt(1,occ.getId());
                    ps.setString(2,targetPath);
                    ps.executeUpdate();
                }
            }
            copyed = true;
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
        return copyed;
    }
    //TREEPATH表にidとpathを共にもった行が１つかそれ以上存在するならtrueを返す。
    private boolean exists(int id,String path) {
        int count = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT COUNT(*) FROM TREEPATH WHERE ID=? AND PATH=?");
            ps.setInt(1,id);
            ps.setString(2,path);
            rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt(1);
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(rs,ps);
        }
        return count > 0;
    }
    //ストアドプロシージャを登録。このクラスのインスタンス作成時に一度だけ呼ばれる。
    private static void registFunction() {
        DBFactory.createTable("/resources/CreateTable1.txt",con);
        System.out.println("ストアドプロシージャ登録完了。");
    }

    /**
     * *_PROPERTIES表の設定名のリストを返す。
     * @param type   フルパッケージ名つきのクラス名
     * @param vector 結果を書き出すVector。JListに入れて使うことが前提。
     * @param table 表名
     */
    public Vector<String> getConfigNames( String type,
        Vector<String> vector,
        String table ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Properties p = new Properties();
        try {
            ps = con.prepareStatement( "SELECT NAME FROM " + table
                + " WHERE TYPE=? ORDER BY PRIORITY");
            ps.setString(1,type);
            rs = ps.executeQuery();
            while(rs.next())
                vector.add(rs.getString("NAME"));
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { rs.close(); } catch (Exception e) { }
            try { ps.close(); } catch (Exception e) { }
        }
        return vector;
    }
    //プロパティデータファイルを識別するためのIDコード。ファイル先頭にこのコードが
    //あれば、正しいファイルだと認識する。
    private static final int CONFIG_FILE_ID = 19640930;
    /**
     * (table)_PROPERTIES表の内容をOutputStremに書き出す。ただしPRIORITYの値は
     * 除外する。内部ではDataOutputStreamが作成されて、始めに設定データファイルで
     * あることの識別用ID、次にテーブル名が出力され、あとはNAME,TYPE,PROPERTIESの
     * 順番で出力がくり返される。
     * なお処理完了後、出力ストリームは閉じない。
     * @param os 出力ストリーム
     * @param table *_PROPERTIES表の名前
     * @exception IOException
     */
    public void storeConfigProperties( OutputStream os, String table )
    throws IOException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement( "SELECT NAME,TYPE,PROPERTIES FROM "
                + table + " ORDER BY PRIORITY" );
            rs = ps.executeQuery();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeInt( CONFIG_FILE_ID );
            dos.writeUTF( table );
            while ( rs.next() ) {
                dos.writeUTF( rs.getString("NAME") );
                dos.writeUTF( rs.getString("TYPE") );
                dos.writeUTF( rs.getString("PROPERTIES") );
            }
        } catch( SQLException e ) {
            e.printStackTrace();
        } finally {
            try { rs.close(); } catch (Exception e) { }
            try { ps.close(); } catch (Exception e) { }
        }
    }

    /**
     * 入力ストリームからデータ(storeConfigProperties()で書き出されたデータ)を
     * 読みこんで、(table)_PROPERTIES表に書きこむ。
     * 処理完了後入力ストリームは閉じない。
     * @param is 入力ストリーム
     */
    public void loadConfigProperties( InputStream is ) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int file_id = ( dis.available() > 0 ) ? dis.readInt() : -1;
        if( file_id != CONFIG_FILE_ID )
            throw new IOException("ファイルフォーマットが異常");
        String table = dis.readUTF();
        while( dis.available() > 0) {
            String name = dis.readUTF();
            String type = dis.readUTF();
            String prop = dis.readUTF();
            String [] lines = prop.split("\n");
            Properties p = new Properties();
            for(String s : lines) {
                String [] temp = s.split("=");
                p.setProperty( temp[0], temp[1] );
            }
            setConfigProperties( name, type, p, table );
        }
    }

    /**
     * (table)_PROPERTIES表の設定情報の最大優先順位+1を返す。
     */
    private int getConfigMaxPriority( String type, String table ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int priority = 0;
        try {
            ps = con.prepareStatement( "SELECT MAX(PRIORITY) FROM " + table
                + " WHERE TYPE=?" );
            ps.setString(1,type);
            rs = ps.executeQuery();
            if(rs.next()) priority = rs.getInt(1);
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { rs.close(); } catch (Exception e) { }
            try { ps.close(); } catch (Exception e) { }
        }
        return priority + 1;
    }
    /**
     * *_PROPERTIES表に設定情報を書き出す。書き出す前に無条件にname,typeに
     * 一致するレコードの削除を試みてその後でINSERTする。つまり同名のデータは上書き
     * 保存されたことになり、同名データが重複登録されることはない。
     * @param name 設定名
     * @param type フルパッケージ名つきのクラス名
     * @param prop 色設定情報の入ったProperties。
     */
    @Override
    public void setConfigProperties( String name,
                                          String type,
                                          Properties prop,
                                          String table ) {
        removeConfigProperties( name, type, table );
        StringBuffer sb = new StringBuffer();
        for ( Enumeration<Object> enu = prop.keys(); enu.hasMoreElements(); ) {
            String key = enu.nextElement().toString();
            String value = prop.getProperty(key);
            sb.append( StringEscape.escape(key) );
            sb.append("=");
            sb.append( StringEscape.escape(value) );
            sb.append("\n");
        }
        int priority = getConfigMaxPriority(type,table);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("INSERT INTO " + table + " VALUES(?,?,?,?)");
            ps.setString(1,name);
            ps.setString(2,type);
            ps.setInt(3,priority);
            ps.setString(4,sb.toString());
            ps.execute();
        } catch(SQLException e) {
            e.printStackTrace();
            try { ps.close(); } catch(Exception ex) { }
        }
    }
    /**
     * *_PROPERTIES表から設定情報を取り出す。
     * @param name 設定名
     * @param type フルパッケージ名つきのクラス名
     * @param prop 設定情報を書き込むProperties。nullを指定すると新たにインスタンス
     * を作成してそこに書き込みそれを返す。nullでない場合はそのPropertiesに書き込み、
     * その参照を返す。
     * @return 正常にロードできたときはtrue。設定名がDBに見つからなかったときはfalse。
     */
    @Override
    public boolean getConfigProperties( String name,
        String type,
        Properties prop,
        String table ) {
        if(prop == null) prop = new Properties();
        String propString = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean notFound = false;
        try {
            ps = con.prepareStatement( "SELECT PROPERTIES FROM " + table
                + " WHERE NAME=? AND TYPE=?");
            ps.setString(1,name);
            ps.setString(2,type);
            rs = ps.executeQuery();
            if ( rs.next() ) propString = rs.getString("PROPERTIES");
            else notFound = true;
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch(Exception ex) { }
            try { rs.close(); } catch(Exception ex) { }
        }
        if(notFound) return false;
        //String [] lines = propString.split("\n");
        List<String> lines = StringEscape.enterSplit( propString );
        for ( String line : lines ) {
            //String [] tokens = line.split("=");
            List<String> tokens = StringEscape.equalSplit( line );
            if (tokens.size() <= 1) continue;
            //prop.setProperty(tokens[0],tokens[1]);
            prop.setProperty( StringEscape.unescape( tokens.get(0) ),
                              StringEscape.unescape( tokens.get(1) ) );
        }
        return true;
    }
    /**
     * 指定された設定情報を削除する。
     * @param name 色設定名
     * @param type フルパッケージ名つきのクラス名
     * @param table 表名
     */
    public void removeConfigProperties( String name, String type, String table) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement( "DELETE FROM " + table
                + " WHERE NAME=? AND TYPE=?");
            ps.setString(1,name);
            ps.setString(2,type);
            ps.execute();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch( Exception ex ) { }
        }
    }
    /**
     * nameVectorで指定された順番に色設定の順序を並べ替える。
     * @param type       フルパッケージ名つきのクラス名
     * @param nameVector 名前のリストが格納されたVector
     * @param table      表名
     */
    public void sortConfigProperties( Vector<String> nameVector,
        String type,
        String table ) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement( "UPDATE " + table
                + " SET PRIORITY=? WHERE TYPE=? AND NAME=?");
            for(int i=0; i< nameVector.size(); i++) {
                ps.setInt(1,i+1);
                ps.setString(2,type);
                ps.setString(3,nameVector.get(i));
                ps.execute();
                ps.clearParameters();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch(Exception ex) { }
        }
    }
    /**
     * 設定名の名前の変更
     * @param name    変更前の名前
     * @param type    チャートタイプ
     * @param newName 新しい名前
     * @param table   表名
     */
    public void renameConfigProperties( String name,
        String type,
        String newName,
        String table ) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement( "UPDATE " + table
                + " SET NAME=? WHERE TYPE=? AND NAME=?");
            ps.setString(1,newName);
            ps.setString(2,type);
            ps.setString(3,name);
            ps.execute();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            try { ps.close(); } catch(Exception ex) { }
        }
    }
    /**
     * OCCASION表の職業フィールドを検索して職業リストを返す。
     * 指定キーで中間一致検索してDISTINCT抽出する。
     * @param key 検索する職業名。SQLで%職業名%として中間一致検索する。
     */
    public Vector<String> searchJobs( String key ) {
        ResultSet rs = null;
        Statement st = null;
        Vector<String> vector = new Vector<String>();
        try {
            st = con.createStatement();
            rs = st.executeQuery(
                "SELECT DISTINCT JOB FROM OCCASION " +
                "WHERE JOB LIKE '%"+ key + "%' ORDER BY JOB");
            while ( rs.next() ) {
                vector.add(rs.getString(1));
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(st,rs);
        }
        return vector;
    }

    /**
     * イベント情報(ヒストリーやコンポジット情報でTransitオブジェクトで表現され
     * るデータ)を格納するメモリテーブルを作成する。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。DB内にはHISTORYまたはCOMPOSIT
     * のメモリテーブルが作成させる。すでに作成されている場合は削除され再作成され
     * る。
     */
    public void createEventTable( int tableCode ) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        String sql =
          "CREATE MEMORY TABLE " + tableName + "_TEMP("
        + " ID INTEGER "
        + " GENERATED BY DEFAULT AS IDENTITY(START WITH 1,INCREMENT BY 1)"
        + " NOT NULL PRIMARY KEY,"
        + " NAME      VARCHAR(20)  DEFAULT '' NOT NULL,"
        + " MEMO      VARCHAR(20)  DEFAULT '' NOT NULL,"
        + " ERA       VARCHAR(2)   DEFAULT 'AD' NOT NULL,"
        + " DATE      DATE         NOT NULL,"
        + " TIME      TIME,"
        + " PLACENAME VARCHAR(20)  DEFAULT '' NOT NULL,"
        + " LATITUDE  DOUBLE,"
        + " LONGITUDE DOUBLE,"
        + " TIMEZONE  VARCHAR(20)  DEFAULT '' NOT NULL,"
        + " JDAY      DOUBLE )";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate( "DROP TABLE " + tableName + "_TEMP IF EXISTS" );
            stmt.executeUpdate( sql );
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(stmt);
        }
    }
    /**
     * tableCodeで指定されたテーブル(HISTROY|COMPOSIT)表に保管されているデータを
     * リストにして返す。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。
     */
    public List<Transit> getEventList( int tableCode ) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        List<Transit> historyList = new ArrayList<Transit>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                "SELECT * FROM " + tableName + "_TEMP ORDER BY JDAY ASC");
            rs = ps.executeQuery();
            while(rs.next()) {
                Transit event = new Transit();
                event.setParams(rs);
                event.setPrimaryKey(rs.getInt("ID"));
                historyList.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps,rs);
        }
        return historyList;
    }
    /**
     * tableCodeで指定されたテーブルの情報をソートしてリストに格納する。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     *                  は、COMPOSIT_TABLE_CODEのどちらかを指定する。
     * @param direction ソート方法。DBFactoryのDESCENDING,NOT_SORTED,ASCENDINGを
     *                  指定する。
     * @param fieldCode DBFactoryのEVENT_FIELD_(NAME|JDAY|PLACENAME|MEMO)のいず
     *                  れかの定数を指定する。どのフィールドでソートするかの指定。
     * @param eventList ソートされたデータが格納されるリスト。
     */
    public void getEventList( int tableCode,
                                int direction,
                                int fieldCode,
                                List<Transit> eventList ) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        String sql = "SELECT * FROM " + tableName + "_TEMP";
        // NOT_SORTEDでなければ、ソート条件をSQLに追加
        if ( direction != -1 ) {
            String fieldName = DBFactory.EVENT_FIELDS[ fieldCode ];
            String dir = direction == DBFactory.DESCENDING ? "DESC" : "ASC";
            sql += " ORDER BY " + fieldName + " " + dir;
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                Transit event = new Transit();
                event.setPrimaryKey(rs.getInt("ID"));
                event.setName(rs.getString("NAME"));
                event.setMemo(rs.getString("MEMO"));
                Date date = rs.getDate("DATE");
                String era = rs.getString("ERA");
                event.setDate(date,era);
                event.setTime(rs.getTime("TIME"));
                event.setPlaceName(rs.getString("PLACENAME"));
                // getDoubleは戻り値がDoubleではなくdoubleなので
                // 事前確認(NULLが代入できない)
                if(rs.getString("LATITUDE") != null)
                    event.setLatitude(rs.getDouble("LATITUDE"));
                if(rs.getString("LONGITUDE") != null)
                    event.setLongitude(rs.getDouble("LONGITUDE"));
                event.setTimeZone(TimeZone.getTimeZone(rs.getString("TIMEZONE")));
                eventList.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(stmt,rs);
        }
    }
    /**
     * tableCodeで指定されたテーブルに、Transitオブジェクトの内容を登録する。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。
     */
    public void addEvent( int tableCode, Transit event ) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        PreparedStatement ps = null;
        try {
            String sql = "INSERT INTO " + tableName
                + "_TEMP(NAME,MEMO,ERA,DATE,TIME,PLACENAME,"
                + "LATITUDE,LONGITUDE,TIMEZONE,JDAY) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1,event.getName());
            ps.setString(2,event.getMemo());
            ps.setString(3,event.getERA());
            ps.setDate(4,event.getDate());
            if(event.getTime() == null) ps.setNull(5,Types.NULL);
            else ps.setTime(5,event.getTime());
            ps.setString(6,event.getPlaceName());
            if(event.getLatitude() == null) ps.setNull(7,Types.NULL);
            else ps.setDouble(7,event.getLatitude());
            if(event.getLongitude() == null) ps.setNull(8,Types.NULL);
            else ps.setDouble(8,event.getLongitude());
            ps.setString(9,event.getTimeZone().getID());
            ps.setDouble(10,event.getJDay());
            ps.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }
    /**
     * tableCodeで指定された表内のeventに該当するレコードを削除する。
     * @param tableCode    DBFactoryクラスで定義されている、HISTORY_TABLE_CODE
     *                     または、COMPOSIT_TABLE_CODEのどちらかを指定する。
     * @param event        削除するデータ。内部的にはevent.getPrimaryKey()のIDを
     *                     もつ行を削除する。
     */
    public void removeEvent( int tableCode, Transit event) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                "DELETE FROM " + tableName + "_TEMP WHERE ID=?");
            ps.setInt(1,event.getPrimaryKey());
            ps.executeUpdate();
        } catch( SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }
    /**
     * tableCodeで指定された表のevent.getPrimaryKey()のIDを持つ行を、eventの
     * データに刷新する。
     */
    public void updateEvent( int tableCode, Transit event ) {
        String tableName = DBFactory.EVENT_TABLE_NAMES[ tableCode ];
        String sql = "UPDATE " + tableName + "_TEMP " +
            "SET NAME=?,MEMO=?,DATE=?,TIME=?,"
            + "PLACENAME=?,LATITUDE=?,LONGITUDE=?,TIMEZONE=?,JDAY=? "
            + "WHERE ID=?";
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement( sql );
            ps.setString( 1,event.getName() );
            ps.setString( 2,event.getMemo() );
            ps.setDate( 3,event.getDate());
            if ( event.getTime() == null ) ps.setNull( 4, Types.NULL );
            else ps.setTime( 4, event.getTime() );
            ps.setString( 5, event.getPlaceName() );
            if ( event.getLatitude() == null ) ps.setNull( 6, Types.NULL );
            else ps.setDouble( 6, event.getLatitude() );
            if ( event.getLongitude() == null ) ps.setNull( 7, Types.NULL );
            else ps.setDouble( 7, event.getLongitude() );
            ps.setString( 8, event.getTimeZone().getID() );
            ps.setDouble( 9, event.getJDay() );
            ps.setInt( 10, event.getPrimaryKey());
            ps.executeUpdate();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }
}
