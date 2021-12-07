/*
 * Start.java
 * Created on 2011/07/28, 0:27:40.
 */
package to.tetramorph.starbase;

import java.io.PrintStream;
import to.tetramorph.util.LoggerOutputStream;
import java.util.Enumeration;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.UIManager;
import java.util.Properties;
import java.io.File;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.StopWatch;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
/**
 * アマテルのエントリー。JWSを使わないものに書き換えた。
 * アマテルがjavawの起動時に認識するオプションは"i"のみで、"AMATERU.properties"
 * ファイルへのパスを明示的に指定するオプションで、このファイルにはアマテルが参照する
 * 天文暦やデータファイルの場所を宣言したファイル。
 * iオプションをつけなかった場合、starbase.jarがあるフォルダの中に同ファイルが置かれて
 * いるものとして読み込みを試みる。ファイルがなければアマテルは起動できない。
 * @author ohsawa
 */
class Start {
//    static void createTempDir() throws IOException {
//        InputStream stream = null;
//        try {
//            stream = Start.class.getResourceAsStream( "/resources/temp.zip");
//            ZipUtils.extract( stream, new File( getProperty("app.home")) );
//        } catch ( Exception e) {
//            throw new IllegalStateException( getProperty("app.home")
//                    + "/tempフォルダを展開できない :" + e.getMessage());
//        } finally {
//            try { stream.close(); } catch(Exception e2) { }
//        }
//    }

    /**
     * プロパティの初期登録を行う。
     * @param regcopy trueならレジストリからConfigにコピー。falseならコピーしない。
     */
    static void setup_regist() {
        if ( ! Config.fileExists() ) {
            // プロパティファイルが存在しないときは新たに作成し初期値を登録
            Config.setDefaultTime( "00:00:00");
            Config.usr.setInteger( "CuspUnknownHouseSystem", 1 );
            Config.usr.setInteger( "HouseSystemIndex",       0 );
            Config.usr.setBoolean( "PrioritizeSolar",     true );
            Config.usr.setBoolean( "UseMeanNode",        false );
            Config.usr.setBoolean( "UseMeanApogee",      false );
            Config.usr.setInteger( "DefaultTimeButtonIndex", 1 );
            Config.usr.setProperty("DefaultTransitPlace",
                     "神奈川県・横浜市西区┃35.453962┃139.617206┃Asia/Tokyo");
            Config.usr.setProperty( "app.topounit",                    "10" );
            Config.usr.setProperty( "app.angleunit",                   "10" );
            Config.usr.setProperty( "AMATERU_ID",     "My name is AMATERU." );
            Config.usr.setProperty( "db.admin.pw",                "amateru" );
            Config.usr.setProperty( "db.plugin.pw",                "plugin" );
            Config.usr.setLong( "AgeOfAmateru",                0L );
        } else {
            Config.load();
        }
        setProperty( "DefaultTime", Config.getDefaultTime());
        setProperty( "app.topounit",
                      Config.usr.getProperty( "app.topounit", "10" ));
        setProperty( "app.angleunit",
                      Config.usr.getProperty( "app.angleunit", "10" ));
        WordBalloon.copyPreference( Config.usr );
//        Config.usr.setProperty( "ChartModule.dir",
//                new File( getProperty("app.program"),"chartmod").getAbsolutePath() );
//        Config.usr.setProperty( "SearchModule.dir",
//                new File( getProperty("app.program"),"searchmod").getAbsolutePath() );
//
        Config.save();
    }
    //シャットダウン
    private static void shutdown( String msg ) {
        SplashWindow.getInstance().setError(msg);
        System.out.println( msg );
        System.exit(0);
    }
    /**
     * データベースに接続できるか検査。成功すればtrueを返す。
     */
    private static boolean isRunningHsqldb() {
        boolean enabled = false;
        try {
            String driverURL = "jdbc:hsqldb:hsql://localhost";
            String pw = Config.usr.getProperty("db.admin.pw","");
            System.out.println("pw = " + pw);
            Connection con = DriverManager.getConnection( driverURL,"sa", pw );
            con.close();
            enabled = true;
        } catch ( Exception e ) {
        }
        return enabled;
    }


    //HSQLDBを起動
    private static void startHsqldb() {
        String dbfile = "file:" + new File(getProperty("app.database"),"amateru")
                .toURI().getPath();
        //String dbfile = "file:" + Home.database.toURI().getPath();
        // 行末の""はHSQLDBに別名を与える際に使用するが与えないので空。
        String [] options = { "-database.0", dbfile, "-dbname.0", "" };
        System.out.println("DBを起動 db_file = " + dbfile);
        try {
            org.hsqldb.Server.main( options );
        } catch ( Exception e ) {
            SplashWindow.getInstance().setError("DBの起動に失敗");
        }
        /*
         * 空のパスワードでログインできたら、まだDBにテーブルは作成されていないと
         * みなして作成する。ログインできなければ、すでににパスワードも設定され
         * テーブルも作成されているとみなす。
         */
        Connection con = null;
        try {
            String driverURL = "jdbc:hsqldb:hsql://localhost";
            //PWなしでログイン
            con = DriverManager.getConnection( driverURL, "sa", "" );

            //空PWでログインできなければ以下の処理はスキップされる

            String pw = Config.usr.getProperty( "db.admin.pw", "" );
            if ( pw.isEmpty() ) throw
                new IllegalStateException("adminパスワードが未設定");
            String sql = String.format(
                "ALTER USER \"sa\" SET PASSWORD \"%s\"",pw);
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            //ゲストアカウントの作成とプロパティ表の作成
            DBFactory.createTable("/resources/CreateTable0.txt",con);
            //メインとなるテーブルの作成とguestアカウントへ読み取り権限を与える
            DBFactory.createTable("/resources/CreateTable.txt",con);
            //ストアドプロシージャの登録
            DBFactory.createTable("/resources/CreateTable1.txt",con);
            System.out.println("DBにテーブル作成");
            InstallSkin.load_skins();
        } catch ( SQLException e ) {
            if ( ! e.getMessage().matches(".*Access is denied$") ) {
                Logger.getLogger( Start.class.getName() )
                        .log( Level.SEVERE, null, e );
            }
        } catch ( IllegalStateException e ) {
            Logger.getLogger( Start.class.getName() )
                    .log( Level.SEVERE, null, e );
        } finally {
            try { con.close(); } catch ( Exception e ) { }
        }
        SplashWindow.getInstance().addValue(20);
    }
    // アマテルの実行環境プロパティを表示する
    private static void printAppProperties() {
        System.out.println("設定された参照フォルダの表示");
        Enumeration enu = System.getProperties().propertyNames();
        while( enu.hasMoreElements() ) {
            String key = (String)enu.nextElement();
            if ( key.matches("^(app|swe).*") ) {
                String value = System.getProperty(key);
                System.out.printf( "%-20s%s%n",key,value );
            }
        }
        System.out.println("設定された参照フォルダの表示終了");
    }

    // 重要なシステムプロパティを表示する
    private static void printSysProperties() {
        System.out.println("プラットホーム情報");
        String [] keys = { "file.encoding","file.separator",
        "java.runtime.version","java.vm.name",
        "os.name","os.version","user.dir",
        "user.home","user.language" };
        for ( String key : keys ) {
            System.out.printf("%-20s%s%n",key,getProperty(key));
        }
    }

    private static int MUTEX_PORT = 12399;
    /**
     * アマテルを起動する。エントリー。
     * @param args
     * @throws Exception
     */
    public static void main( String [] args ) throws Exception {
        StopWatch.set("起動完了までの時間");
        if ( MutexServer.isRunning( MUTEX_PORT ) ) {
            System.out.println( "すでに動作中です。" );
            System.exit(0);
        }

        /* 標準エラー出力をファイル併用出力ストリームにすげ替える */

        File log = new File(getProperty("user.home"),"AMATERU.log");
        PrintStream ps
             = new PrintStream( new LoggerOutputStream(log,System.out) );
        System.setOut( ps );
        System.setErr( ps );

        /* starbase.jarのフォルダにあるAMATERU.propertiesを取得し
         * システムプロパティにセット */

        File am = null;
        if ( getProperty("i") != null ) {
            // iオプションはAMATERU.prpertiesの場所を明示的に指定するもの
            am = new File( getProperty("i"));
        } else {
            File prog_dir = new File( getProperty("java.class.path") ).getParentFile();
            am = new File(prog_dir,"AMATERU.properties");
        }
        if ( ! am.exists() )
            throw new java.lang.IllegalStateException( am.getAbsolutePath() + "が見つからない");

        Properties p = new Properties();
        FileTools.loadProperties(p, am);
        Enumeration enu = p.propertyNames();
        while( enu.hasMoreElements() ) {
            String key = (String)enu.nextElement();
            String val = (String)p.getProperty(key);
            setProperty( key, val );
            if ( key.matches("nodb|app\\.database|support_url"))
                continue;
            File f = new File(val);
            if ( ! f.exists() ) throw new IllegalArgumentException(
                    "フォルダが存在しない : key = " + key + ", val = "
                    + val + ", " + f.getAbsolutePath());
        }
        File apphome = new File( getProperty("user.home"), ".AMATERU2.0");

        /* AMATERU.propertiesにapp.homeが無いときはuser.home下を調べる
         * デフォルトのapp.home下に設定ファイルを探しリダイレクト指定の有無を確認
         * リダイレクトからさらにリダイレクトも可能。
         */

        if ( getProperty("app.home") == null ) {
            for(;;) {
                Properties user_prop = new Properties();
                File user_prop_file = new File(apphome,"AMATERU_USER.properties");
                if ( user_prop_file.exists() ) {
                    System.out.println("AMATERU_USER.propertiesを検出");
                    user_prop.clear();
                    FileTools.loadProperties( user_prop, user_prop_file );
                    if ( user_prop.getProperty("app.home") != null ) {
                        //次に参照するapphomeを書き換え
                        apphome = new File( user_prop.getProperty("app.home") );
                        System.out.println( "app.homeを"
                                             + apphome.getAbsolutePath()
                                             + "にリダイレクト");
                        continue;
                    }
                }
                Enumeration amu_enu = user_prop.propertyNames();
                while( amu_enu.hasMoreElements() ) {
                    String key = (String)amu_enu.nextElement();
                    String value = user_prop.getProperty(key);
                    File uf = new File( value );
                    if ( ! ( uf.exists() && uf.isDirectory()) ) {
                        throw new IllegalStateException
                                ("フォルダではない: " + uf.getAbsolutePath());
                    } else {
                        setProperty( key, value );
                    }
                }
                break;
            }

            // app.homeをセット
            setProperty("app.home",apphome.getAbsolutePath());
        } else {
            apphome = new File( getProperty("app.home") );
        }
        if ( apphome.isFile() ) throw new IllegalStateException
                ("アプリケーションのホームフォルダがファイルになっている");

        /* app.home下のフォルダは毎回作成しシステムプロパティにも登録 */
        apphome.mkdir();
        String [] subdir = { "properties","userdict","temp","database" };
        for ( String dir : subdir ) {
            File subfol = new File(apphome, dir );
            subfol.mkdir();
            setProperty("app." + dir, subfol.getAbsolutePath() );
        }
        printSysProperties();
        printAppProperties();
        setup_regist();
        //createTempDir();
        java.awt.EventQueue.invokeLater( new LookAndFeel() );
        //DBや検索結果の窓を別フレームにするときはtrueをセット。
        //(この機能は削除したが、まだ残滓が残っているのでそのまま)
        System.setProperty( "SeparateMode", "false" );
        System.out.println( "nodb = " + System.getProperty("nodb") );


        //スプラッシュウィンドウを表示
        SplashWindow splash = SplashWindow.getInstance();
        splash.getJButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                SplashWindow.getInstance().dispose();
                System.exit(0);
            }
        });
        //JDBC をロードする
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            splash.addValue(5);
        } catch ( ClassNotFoundException e ) {
            shutdown("JDBCのロードに失敗。");
        }
        if ( getProperty("nodb").equals("true") ) { //外部DBモード

            System.out.println("外部DBモード");
            if ( ! isRunningHsqldb() )  //DBが動いていないなら
                shutdown("HSQLDBを外部で起動しておいてください。");

        } else { // 通常モード

            System.out.println("内部DBモード");
            if ( isRunningHsqldb() )  //DBが動いているなら
                shutdown(
                     "HSQLDBがすでに動作中なので停止させてから実行してください。");
            startHsqldb();
            splash.addValue(20);

        }
        DBFactory.getInstance();
        ChartPane.loadModules();
        splash.addValue(10);

        java.awt.EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                StopWatch.set("Mainパネル作成時間");
                main = new Main();
                StopWatch.show("Mainパネル作成時間");
            }
        });
        //while ( main == null ) Sleep.exec(500);

        boolean ok = MutexServer.exec(MUTEX_PORT,main);
        if ( ! ok ) shutdown( "MutexServerの起動に失敗");
        // MutexServerのシャットダウン時の終了処理を登録
        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                MutexServer.abort( MUTEX_PORT );
            }
        });

        main.setup();
        MorningAccess.main( null );
        System.out.println("起動処理完了");
        SplashWindow.getInstance().dispose(); //スプラッシュウィンドウ消去

    }

    static Main main = null;

    private static class LookAndFeel implements Runnable {
        @Override
        public void run() {
            if ( System.getProperty("os.name").indexOf("Windows") >= 0) {
                try {
                    UIManager.setLookAndFeel(
                         "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch ( Exception e ) {
                    Logger.getLogger( Main.class.getName())
                            .log( Level.WARNING, null, e);
                }
            }
            //UIManager.put("swing.boldMetal", Boolean.FALSE);
        }
    }
}
