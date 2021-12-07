/*
 * InstallThread.java
 * Created on 2011/07/20, 23:39:33.
 */
package amateru_installer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.util.Properties;
import static amateru_installer.Copy.*;
/**
 * インストール先フォルダの設定にしたがって、インストールを行う。
 * 自身のjar書庫/distに同梱されているファイルを所定のフォルダにコピーしたり、
 * ショートカットを作ったり、「アプリケーションの追加と削除」に登録したりする。
 * このクラスは、別スレッドとして実行されEDTではない。
 * コピーの状況をProgressListenerに通達する。
 * @author ohsawa
 */
class InstallThread implements Runnable {

    ProgressListener pl;
    /**
     * インストールスレッドを作成する。
     * @param pl ProgressListener (進行状況表示器)
     */
    public InstallThread( ProgressListener pl ) {
        this.pl = pl;
    }
    /**
     * スレッドを実行する。
     */
    @Override
    public void run() {
        try {
            if ( Regist.sys.getBoolean( "amateru_exists", false ) )
                uninstall();
            install();
        } catch ( Exception e ) {
            pl.print( "ERROR", e.getMessage() );
            StackTraceElement [] elems = e.getStackTrace();
            for ( int i = 0; i < elems.length; i++ ) {
                pl.print( "", elems[i].toString() );
            }
        }
    }
    /**
     * インストール処理を行う。インデントが深くなるのが嫌なので、run()の中から
     * これを呼び出しているだけ。
     *
     * 実行はいくつかの環境変数がCopy.envにセットされていることが前提で、そのセットは
     * SelectFolderPanelで行われる。
     * <pre>
     *  "PROGRAM"  プログラムフォルダ　(C:\Program Files\...)
     *  "EPHE"     天文暦のパス ( C:\Users\ephe )
     *  "USRHOME"  ユーザーホーム ( C:\Users\ohsawa\... )
     *  "APPHOME"  アプリケーションホーム。通常はUSRHOMEと同じ。USRHOMEはユーザ
     * が変更する場合があるが、APPHOMEはその初期値
     *  "HOME"     user.homeプロパティの値 ( C:\Users\ohsawa )
     * </pre>
     * パスのセパレータはOSごとの決まりに従う。Windowsなら"\"、その他は"/"。
     * @throws Exception
     */
    void install() throws Exception {
        pl.print( "MESSAGE", "アマテルをインストールします" );
        if ( ! Regist.sys.getBoolean("amateru_exists", false ) ) {
            // インストールのときに前にインストールされたときのアンインストーラ
            // や、フォルダが残っている場合は、インストールされたものの削除を試みる。
            // フォルダ一括削除は可能だが、万が一を考え最小限の削除のみ行う
            File prog = new File( env.get("PROGRAM") );
            File uninst = new File( prog,"amateru_uninstaller.jar");
            uninst.delete();
            prog.delete();
        }
//        if ( OSType.isWindows() ) {
//            env.put( "STARTMENU", getSpecialFolder( WinShortcut.JS_AllUsersPrograms ));
//            env.put( "DESKTOP"  , getSpecialFolder( WinShortcut.JS_AllUsersDesktop ));
//            env.put( "ADMIN_STARTMENU", getSpecialFolder( WinShortcut.JS_PROGRAMS ));
//        }
        mkdirs( "%PROGRAM%" );
        //アンインストーラはプログラムフォルダに置く
        start_install( "%PROGRAM%" );
        // "-1"はprogramフォルダを無いことにしてコピーする指定
        copy( "program/.*","%PROGRAM%/", -1 );

        // 天文暦もインストールする場合はコピー
        if ( env.get("NO_EPHE").equals("false") ) {
            mkdirs("%EPHE%");
            // "-1"をつけてepheを無いことにして中身を%EPHE%にコピー
            copy( "ephe/.*" , "%EPHE%/", -1 );
        }

        if ( OSType.isWindows() ) {
            env.put( "STARTMENU", getSpecialFolder( WinShortcut.JS_AllUsersPrograms ));
            env.put( "DESKTOP"  , getSpecialFolder( WinShortcut.JS_AllUsersDesktop ));
            env.put( "ADMIN_STARTMENU", getSpecialFolder( WinShortcut.JS_PROGRAMS ));

            // スタートメニューにフォルダを作りそこにショートカットを作る
            mkdirs( "%STARTMENU%/AMATERU2.0" );
            mkdirs( "%ADMIN_STARTMENU%/AMATERU2.0" );
            // アマテルのショートカット
            mkjlnk( "%STARTMENU%/AMATERU2.0", "AMATERU2.0",
                    "%PROGRAM%/images/niwatori.ico",
                    "-jar \"%PROGRAM%/starbase.jar\"",
                    "統合占星術環境アマテル");
            // ミチテルのショートカット
            mkjlnk( "%STARTMENU%/AMATERU2.0", "MICHITERU2.0",
                    "%PROGRAM%/images/michiteru.ico",
                    "-jar \"%PROGRAM%/michiteru.jar\"",
                    "天象カレンダー・ミチテル");
            mklnk( "%STARTMENU%/AMATERU2.0",
                    "アマテル超図解マニュアル",
                    "%PROGRAM%/images/niwatori.ico",
                    "%PROGRAM%/doc/man.pdf",
                    "",
                    "");
            // 必要に応じてデスクトップにも作成
            if ( env.get("DESKTOP_SCUT").equals("true") ) {
                mkjlnk( "%DESKTOP%", "AMATERU2.0",
                        "%PROGRAM%/images/niwatori.ico",
                        "-Xms64m -Xmx128m -jar \"%PROGRAM%/starbase.jar\"",
                        "統合占星術環境アマテル");
                mkjlnk( "%DESKTOP%", "MICHITERU2.0",
                        "%PROGRAM%/images/michiteru.ico",
                        "-jar \"%PROGRAM%/michiteru.jar\"",
                        "天象カレンダー・ミチテル");
            }
            // アンインストーラへのショートカット
            mkjlnk( "%ADMIN_STARTMENU%/AMATERU2.0",
                     "アンインストール",
                     "%PROGRAM%/images/niwatori.ico",
                     "-jar \"%PROGRAM%/amateru_uninstaller.jar\"",
                     "アマテルのアンインストーラ" );
//            // アンインストーラへのショートカット
//            mklnk( "%ADMIN_STARTMENU%/AMATERU2.0",
//                     "アンインストール",
//                     "%PROGRAM%/images/niwatori.ico",
//                     "%PROGRAM%/UNINSTALL.bat","",
//                     "アマテルのアンインストーラ" );
        }
        // 環境設定ファイルを書きだす
        Properties p = new Properties();
        //String usrhome = env.get("USRHOME");
        String program = env.get("PROGRAM");

        p.setProperty( "swe.path"       , env.get("EPHE"));
        p.setProperty( "app.cmod"       , file( program, "cmod"   ));
        p.setProperty( "app.smod"       , file( program, "smod"   ));
        p.setProperty( "app.dict"       , file( program, "dict"   ));
        p.setProperty( "app.place"      , file( program, "place"  ));
        p.setProperty( "app.doc"        , file( program, "doc"    ));
        p.setProperty( "app.images"     , file( program, "images" ));
        p.setProperty( "app.conf"       , file( program, "conf" ));

        p.setProperty( "support_url", "http://tetramorph.to" );
        p.setProperty( "nodb", "false");

        saveProperties( "%PROGRAM%/AMATERU.properties",
                "このフォルダのアマテルが参照するフォルダ",p );

        if ( ! env.get("USRHOME").equals( env.get("APPHOME") ) ) {
            mkdirs("%APPHOME%");
            p.clear();
            p.setProperty("app.home", env.get("USRHOME"));
            saveProperties( "%APPHOME%/AMATERU_USER.properties",
               "アマテルのユーザーアプリケーションホームはリダイレクトされています。",p);
        }
        if ( OSType.isWindows() ) {
            //「アプリケーションの追加と削除」に登録
            regWindowsAppMenu( "AMATERU2.0", //レジストリ上のフォルダ名
                                 "AMATERU2.0", //表示名
                                 "%PROGRAM%",  //インストールフォルダ
                                 "javaw -jar \"%PROGRAM%\\amateru_uninstaller.jar\"", //アンインストーラの起動コマンド
//                                 "%PROGRAM%\\UNINSTALL.bat",
                                 "大澤義孝",   //発行者
                                 "2.0",       //表示バージョン
                                 "%PROGRAM%\\images\\niwatori.ico"); //表示アイコンのパス
        }
        //アンインストーラ書庫にログを書きだす
        end_install();
    }

    /**
     * インストールログを逆に辿りながらファイルとフォルダを削除する。
     * アマテルがインストールされている状態で、再インストールされた場合は、
     * アンインストールを行ってそれからインストールする。
     * ただしアプリが生成したフォルダやファイルがあると、すべてを完全に消去することは
     * できない。インストール直後のまっさらな状態で実行すれぱ完全に消去されるのが正常。
     * @throws IOException
     */
    public void uninstall() throws Exception {
        String path = Regist.sys.get("amateru_uninstaller", "");
        if ( path.isEmpty() ) return; //アンインストーラのパス登録がない場合は処理しない
        File file = new File( path );
        if ( ! file.exists() ) return; //登録があってもそのファイルが存在しないときは処理しない

        List<String> list = new ArrayList<String>();
        JarUtils.getInstallLog( file, list );
        if ( list.isEmpty() ) return; //ファイルがあっても、ログが空なら処理しない
        pl.print( "MESSAGE", "インストールされているアマテルをアンインストールします" );

        Collections.reverse( list );
        // 最初にファィルとショートカットを削除
        for ( String s: list ) {
            String [] token = s.split(",");
            if ( token[0].equals("copy") ) {
                File f = new File( token[1] );
                String ap = f.getAbsolutePath();
                if ( f.exists() ) {
                    if ( f.delete() )
                        pl.print( "DELTED" , ap );
                    else
                        pl.print( "DELETE FAILED", ap );
                } else {
                    pl.print( "FILE NOT FOUND", ap );
                }
                pl.addProgress(1);
            }
        }
        // 次にフォルダを削除
        for ( String s: list ) {
            String [] token = s.split(",");
            if ( token[0].equals("mkdirs")) {
                File dir = new File( token[1] );   //親のパス
                File dir2 = new File( token[2] );  //親の下にある子のパス(複数)

                // 子のフォルダを末節から上に向かってすべて削除

                for(;;) {
                    File f = new File( dir,dir2.getPath() );
                    String ap = f.getAbsolutePath();
                    if ( f.exists() ) {
                        if ( f.delete() )
                            pl.print( "RMDIR",ap );
                        else
                            pl.print( "RMDIR FAILED",ap );
                    } else {
                        pl.print( "DIR NOT FOUND",ap );
                    }

                    pl.addProgress(1);
                    dir2 = dir2.getParentFile();
                    if ( dir2 == null ) break;
                }
            } else if ( token[0].equals("regist")) {
                if ( OSType.isWindows() ) {
                    // 「アプリケーションの追加と削除」から削除
                    try {
                        WinShortcut.regDelete( token[1] );
                        pl.print( "UNREGIST", token[1] );
                    } catch (Exception e) {
                        pl.print( "UNREGIST FAILED", e.getMessage() );
                    }
                }
            }
        }
        pl.print("MESSAGE", "アンインストール完了" );
    }

    static String file(String parent, String child) {
        return new File(parent,child).getAbsolutePath();
    }

    public static void main( String [] args ) {
        env.put("PROGRAM",  "C:\\Program Files\\AMATERU2.0" );
        String s = parse_env( "%PROGRAM%/starbase.jar");
        System.out.println(s);
    }
}
