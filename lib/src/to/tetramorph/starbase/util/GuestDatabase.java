/*
 * GuestDatabase.java
 *
 * Created on 2007/03/11, 9:36
 *
 */

package to.tetramorph.starbase.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.tree.TreePath;

/**
 * チャートモジュールやサーチモジュール側でデータベースと接続する際には、
 * このクラスを使用する。シングルトンクラスでインスタンスはつねに一つ。
 * DBのと接続はシステム終了時に自動的にクローズされる。
 * あらかじめHSQLDBに登録されたguestユーザでDBにログインする。guestアカウントは
 * DBに対してSELECTする権限のみもち、INSERT、DELETE、CREATEなどは禁止されている。
 * 天体位置を検索するときにつかわれるストアドプロシージャは使用可能。
 * @author 大澤義鷹
 */
public class GuestDatabase {
  private Connection con;
  private static GuestDatabase db = new GuestDatabase();
  //  GuestDatabase オブジェクトを作成する
  private GuestDatabase() {
    try {
      Class.forName("org.hsqldb.jdbcDriver");
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
    }
    try {
      String driverURL = "jdbc:hsqldb:hsql://localhost";
      con = DriverManager.getConnection(driverURL,"guest","");
      System.out.println("データベース接続完了(guest account)");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    //シャットダウンのときに呼び出されるスレッドを登録
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          if(con != null) {
            con.close();
            System.out.println("DBとの接続をクローズ(Guest account)");
          }
        } catch( Exception e ) { e.printStackTrace(); }
      }
    });
  }
  /**
   * インスタンスを返す。
   */
  public static GuestDatabase getInstance() {
    return db;
  }
  /**
   * データベースとのConnectionオブジェクトを返す。
   */
  public Connection getConnection() {
    return con;
  }
}
