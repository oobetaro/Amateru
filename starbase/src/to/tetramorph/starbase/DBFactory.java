/*
 * DBFactory.java
 *
 * Created on 2007/12/16, 10:57
 *
 */

package to.tetramorph.starbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;

/**
 * データベースにアクセスするDBインターフェイスをもったオブジェクトを返す。
 * このクラスはアプリ全体で共通の名前でDBオブジェクトのインスタンスを取得できるよ
 * うにするためにある。今のところDBインターフェイスを実装したクラスはDatabaseただ
 * 一つで、それはHSQLDBをターゲットにして書かれたものだが、もし他のDB製品に変更する
 * 必要が出たらどうなるだろう。
 * Databaseオブジェクトへのアクセスは多くのクラスから発生するため、
 * あらたにDatabaseMySQLなどというクラスができたら、
 * それら多数のクラスにリファクタリングが必要になってしまう。
 * Database db = Database.getInstance()などあちこちのクラスで参照していると、
 * それをDatabaseMySQL db = new DatabaseMySQL.getInstance()などとする変更作業が
 * 膨大になってしまう。そこでこのクラスを用意して、インスタンスの取得は
 * DB db = DBFactory.getInstance()とするようにしておけば、新たなDatabaseクラスが
 * 用意されても、最小限の修正で対応することができる。
 * DatabaseMySQLクラスはDBインターフェイスを実装し、MySQL固有のSQL文で必要な処理を
 * 行う。インスタンスの取得はDBFactoryで行う。こうすれば異なるDBに対してもまぁまぁ
 * 柔軟に対処できる。というのは一面的な話でそう甘い話はころがっていない。
 *
 * 検索モジュール群は独自にSQLでデータにアクセスする。様々な検索式が考えられるから、
 * それらをあらかじめメソッドとして用意しておくことできず、モジュールでそれぞれ独自
 * のSQL文を発行するしかない。もしデータベースが他の製品に変更になるとSQLには方言
 * があるしHSQLDB固有の方言もかなりあるのでおそらく問題が起きる。
 * この問題を吸収するのは非常に困難だ。
 * またRecalculationDialog,DataExporterDialogは、プログレスバーで進行状況を
 * ディスプレイしながらDBにアクセスするため、クラス内部でSQL文を発行している。
 * これではDBの製品が変更されたらそれまで。
 *
 * つまり完全にDBの差異を吸収できるような構造にはまだなっていない。
 * インターフェイスを使って予期せぬ変更に対しての保守性を確保しているにすぎない。
 * 必要に応じて本格的なDBを接続できるような仕様も考えられない事はないが、
 * ニーズはほとんど無いだろうし、その仕様を実現するのもかなり難しいと思われる。
 *
 * それから今のところHSQLDBの使用しか想定しておらず、このクラスの実装はただの
 * ラッパーに等しい。
 * @author 大澤義鷹
 * @see Database
 * @see DB
 */
class DBFactory {
    /**
     * 降り順ソートを表す定数で値は2。
     */
    public static final int DESCENDING = 2;
    /**
     * ソートしないことを表す定数で値は0。
     */
    public static final int NOT_SORTED = 0;
    /**
     * 登り順ソートを表す定数で値は1。
     */
    public static final int ASCENDING = 1;

    public static final int HISTORY_TABLE_CODE = 0;
    public static final int COMPOSIT_TABLE_CODE = 1;
    public static final String [] EVENT_TABLE_NAMES = {
        "HISTORY","COMPOSIT"
    };
    public static final String  [] EVENT_FIELDS = {
        "NAME","JDAY","PLACENAME","MEMO"
    };
    public static final int EVENT_FIELD_NAME = 0;
    public static final int EVENT_FIELD_JDAY = 1;
    public static final int EVENT_FIELD_PLACENAME = 2;
    public static final int EVENT_FIELD_MEMO = 3;

    //インスタンス作成は禁止
    private DBFactory() {
    }
    /**
     * データベースオブジェクトを返す。
     */
    public static DB getInstance() {
        return Database.getInstance();
    }
    /**
     * TreePathをTREEPATH表のPATH用文字列にフォーマットして返す。<br>
     * treePathにnullが入った場合は""を返す。<br>
     * [root , MyChart] といったTreePathの場合rootは取り去られ "MyChart"を返す。
     * <br>
     * [root ,MyChart ,Hoge]なら"MyChart/Hoge"を返す。
     * いずれにせよ頭のrootは省略される。
     * 逆変換はDatabaseFrame.foundTreePath(String path)を使用する。
     */
    public static String getPathString(TreePath treePath) {
        if(treePath == null) return "";
        Object [] path = treePath.getPath();
        StringBuilder sb = new StringBuilder();
        for(int i=1; i < path.length; i++) {
            sb.append(path[i].toString());
            sb.append("/");
        }
        if(sb.length() > 0 ) sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    /**
     * path[]をrooNodeに追加する。パスからﾂﾘｰ構造にﾉｰﾄﾞを編み上げる。
     * ﾌｧｲﾙｼｽﾃﾑと同様に同一階層に同名のﾌｫﾙﾀﾞは存在できないというﾙｰﾙにのっとる。
     * @param path ﾊﾟｽを表現する配列。
     * @param rootNode ﾙｰﾄにするﾉｰﾄﾞのｲﾝｽﾀﾝｽ。nullは禁止。
     */
    public static void addNode(Object [] path,FolderTreeNode rootNode) {
        FolderTreeNode temp = null;
        for(int i=0; i<path.length; i++) {
            boolean exists = false;
            for(int j=0; j<rootNode.getChildCount(); j++) {
                temp = (FolderTreeNode)rootNode.getChildAt(j);
                if(temp.toString().equals(path[i].toString())) {
                    exists = true;
                    break;
                }
            }
            if(! exists) {
                temp = new FolderTreeNode(path[i]);
                //addするときごみ箱にaddするような事態が発生するとExceptionが出る。
                //それを拾って異常なパス(/ごみ箱/なんとか/)を検出するべかもしれ
                //ないが今のところは未対応
                rootNode.add(temp);
            }
            rootNode = temp;
        }
    }
    /**
     * SQL文が書かれたファイルをリソースから取得してDBに送る。
     * SQL文のテキストファイル内では行頭に#をつけることでコメントを記入可能。
     * セミコロンで区切る事で複数のステートメントを列挙できる。
     * @param resource "/resources/CreateTable.txt"等
     * @param con       DBとのConnectionオブジェクト
     */
    public static void createTable( String resource ,Connection con) {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            //コメントを除去しながら読込
            url = DBFactory.class.getResource( resource );
            InputStream stream = url.openStream();
            if ( stream != null )
                reader = new BufferedReader( new InputStreamReader(stream,"sjis") );
            while ( reader.ready() ) {
                String line = reader.readLine().trim();
                if(! (line.startsWith("#") || line.equals(""))) {
                    sb.append( line );
                    sb.append( "\n" );
                }
            }
        } catch ( IOException e ) {
            Logger.getLogger(DBFactory.class.getName()).log(Level.SEVERE,null,e);
        } finally {
            try { reader.close(); } catch( Exception e ) { }
        }
        String [] sql = sb.toString().split(";"); //SQLの1文づつに分割
        String cmd = "";
        try {
            Statement stmt = con.createStatement();
            for(int i=0; i<sql.length; i++) {
                cmd = sql[i].trim();
                if(! cmd.equals("")) {
                    stmt.executeUpdate(cmd);
                }
            }
        } catch( SQLException e ) {
            Logger.getLogger(DBFactory.class.getName()).log(Level.SEVERE,null,e);
        }
    }
}
