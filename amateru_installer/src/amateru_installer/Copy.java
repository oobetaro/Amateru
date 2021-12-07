/*
 * Copy.java
 * Created on 2011/07/17, 22:16:06.
 */
package amateru_installer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * jar書庫のリソースからデータをとりだし、ファイルにコピーする。
 * copy,mkdirの際にアンインストール用としてlogListにログを取る。
 * IOException等の例外はみな外に丸投げする。
 * @author ohsawa
 */
class Copy {
    static boolean TESTMODE = true;
    static ProgressListener pl = null;
    static double prog_step = 1;
    /**
     * load_filelist()によって読み込まれたインストールファイル一覧
     */
    static List<String> fileList = new ArrayList<String>();
    static List<String> logList = new ArrayList<String>();

    public static void setProgressListener( ProgressListener l ) {
        pl = l;
    }

    /**
     * 変数保管。フィールドに直接アクセスする。env.put()など。
     */
    public static Map<String,String> env = new HashMap<String,String>();
    /**
     * 引数で与えられた文字列内の%～%で書かれたキーワードをみつけたら、envマップに
     * 登録されている環境変数に置換して返す。envには%HOME%だけはあらかじめ登録
     * されている。
     * @param str "%home%/hoge"なら
     * @return "C:/Users/ohsawa/hoge"となって戻る。
     */
    static String parse_env( String str ) {
        if ( str.indexOf("%") < 0 ) return str;
        Iterator<String> ite = env.keySet().iterator();
        int i = 0;
        while ( ite.hasNext() ) {
            String var = ite.next();
            // 置換する文字列に円記号が含まれている場合、２つならべてエスケープしな
            // いと置換後、記号が消えてしまう。
            String temp = str.replaceAll( "%" + var + "%", WinShortcut.esc( env.get(var)));
            if ( ! temp.equals( str ) ) i++;
            str = temp;
            if ( str.indexOf("%") < 0 ) return str;

        }
        // 最後まで文字列中に%が残るなら、それは未定義変数なのでエラーを出す
        throw new IllegalArgumentException("Undefined variable:" + str);
    }
//    /**
//     * インストールするファイル一覧が書かれたファイルを読み込む。
//     * このクラスのcopy(src,dst)メソッドのsrcは、この一覧のなかのファイルでなければ
//     * ならない。ローカルディクス上のファイルコピーではない点に注意。
//     */
//    public static void load_filelist() throws IOException  {
//        BufferedReader in = null;
//        in = new BufferedReader( new InputStreamReader(
//                Copy.class.getResourceAsStream("/resources/filelist.txt")));
//        while ( in.ready() ) fileList.add ( in.readLine() );
//        in.close();
//        env.put("HOME", System.getProperty("user.home").replace('\\', '/'));
//        //プログレスバーの増分値を決定
//        prog_step = pl.getBarMaxValue() * 0.95 / fileList.size();
//    }

    /**
     * jar書庫のリソースからファイルをコピーする
     * @param path
     * @param dstfile
     */
    private static void copy( String path, File dst ) throws IOException {
        InputStream in = Copy.class.getResourceAsStream(path);
        if ( in == null ) throw new IllegalArgumentException(
                "リソース" + path + "が見つからない" );
        BufferedOutputStream out = null;
        out = new BufferedOutputStream( new FileOutputStream( dst ) );
        byte [] buf = new byte[64 * 1024];
        for(;;) {
            int size = in.read( buf, 0, buf.length );
            if ( size == -1 ) break;
            out.write( buf, 0, size);
        }
        out.flush();
        out.close();
        in.close();
        pl.print( "COPY", dst.getAbsolutePath() );
        pl.addProgress(prog_step);
        logList.add( "copy," + dst.getAbsolutePath() );
        //テスト用。ゆっくり実行。
        //try { Thread.sleep(1000); } catch ( Exception e ) {}
    }
    /**
     *
     * コピー先のパスの規則
     * ".../"で終わるならフォルダとみなしその中にsrcのファイル名でコピーする。フォルダ
     * が存在しないときは作る。
     *
     * "chartmod.jar","c:/users/ohsawa/.Amateru/chartmod/"
     * コピー先がフォルダでないときは、dstをファイルとみなしその名前でコピーする。
     * コピー先ファイルがすでに存在するときは上書きする。
     * @param files 正規表現で書かれたリソースのパス。
     * @param dst コピー先。変数展開機能あり。たとえば"%HOME%/app"
     * @param p コピー元のフォルダ階層を指定された分だけ上に上げてコピーする。
     * たとえば/dist/programをコピーすると、コピー先にprogramというフォルダが作られ
     * その中にファイルがコピーされるが、pに-1を指定すると、programは作成されず、
     * コピー先の直下にファイルがコピーされる。
     */
    public static void copy( String files, String dst, int p ) throws IOException {
        dst = parse_env(dst);
        File dstFile = new File(dst);
        for ( String src : fileList ) {
            if ( src.matches( files ) ) {
                if ( dst.matches(".*/$") ) {              // コピー先の末尾が"/"で終わるならフォルダ単位のコピー
                    //コピー元のパスをpの値に応じていじる
                    int i;
                    String srctmp = src;
                    //pl.print("SRC = " + srctmp + "\n");
                    int q = p;
                    while ( q < 0 && (i = srctmp.indexOf("/")) >= 0 ) {
                        srctmp = srctmp.substring( i + 1 );
                        q++;
                    }
                    File f = null;
                    if ( srctmp.indexOf("/") < 0 ) {    // 元に"/"が見つからないならそれはルートにあるファイルだから
                        if ( ! dstFile.exists() ) {
                            mkdirs( dstFile );         // コピー先のフォルダを作り
                            //pl.print( "MKDIRS", dstFile.getAbsolutePath() );
                        }
                        f = new File( dstFile, srctmp );    // コピー先フォルダ+ファイル名のパスを作る
                    } else {
                        //コピー元に"/"があればそれはフォルダ下にあるファイルだから、そのフォルダを作り
                        f = new File( dstFile, srctmp );
                        if ( ! f.getParentFile().exists() )
                            mkdirs( f.getParent() );
                    }
                    copy( "/dist/" + src, f );
                    continue;
                }
                copy( "/dist/" + src, dstFile );
            }
        }
    }
    public static void copy( String files, String dst ) throws IOException {
        copy( files, dst, 0 );
    }
    public static void mkdirs( File dir_path ) {
        String dir = dir_path.getAbsolutePath();
        File path = dir_path;
        String add_dir = "";
        for(;;) {
            if ( path.isFile() ) throw new IllegalArgumentException(
                    "パスの中にファイルが存在している:" + path.getAbsolutePath());
            if ( ! ( path.exists() && path.isDirectory() ) ) {
                path = path.getParentFile();
                continue;
            }
            //追加パス
            add_dir = dir.substring( path.getAbsolutePath().length() );
            // 先頭にセパレータがつくときはそれを落とす
            if ( ! add_dir.isEmpty() ) add_dir = add_dir.substring(1);
            break;
        }
        //既存パス
        String exist_path = dir.substring( 0, dir.lastIndexOf( add_dir ) );
        if ( ! exist_path.equals(dir_path.getAbsolutePath())) {
            //事前に存在を確認し、無いときは作成し、再び存在を確認し、
            //作成されたことが確実かつ、ログに重複がなければ記録する
            if ( ! dir_path.exists() ) {
                dir_path.mkdirs();
                if ( dir_path.exists() ) {
                    pl.print( "MKDIRS", dir_path.getAbsolutePath() );
                    pl.addProgress(prog_step);
                    String log = "mkdirs," + exist_path + "," + add_dir;
                    if ( ! logList.contains(log) )
                        logList.add( log );
                } else {
                    throw new IllegalArgumentException
                      ( "フォルダを作成できない:"+dir_path.getAbsolutePath());
                }
            }
        }
    }

    public static void mkdirs( String path ) {
        mkdirs( new File( parse_env(path)));
    }
    /**
     * ショートカットを作る。パスは変数展開される。
     * @param shortcutName タイトル
     * @param arguments 引数
     * @param iconFile アイコンのパス
     * @param description 説明
     * @param createPath 作る場所のパス
     * @param linkFile リンクするファイルのパス
     */
    public static void mklnk( String createPath,
                               String shortcutName,
                               String iconFile,
                               String linkFile,
                               String arguments,
                               String description ) throws IOException, InterruptedException {
        File cpath = new File( parse_env(createPath));
        File scut = WinShortcut.createShortcut(
                cpath, shortcutName,
                new File(parse_env(iconFile)),
//                new File( parse_env(linkFile)), arguments, description, null, 2, null);
                parse_env(linkFile), arguments, description, null, 2, null);
        pl.print( "MKLNK", scut.getAbsolutePath() );
        logList.add( "copy," + scut.getAbsolutePath() );
    }
    /**
     * javawを起動するショートカットを作る。パスは変数展開される。
     * @param shortcutName タイトル
     * @param arguments 引数 たとえば "-jar starbase.jar"。変数展開される。
     * @param iconFile アイコンのパス。変数展開される。
     * @param description 説明
     * @param createPath 作る場所。変数展開される。
     * @return 作成されたショートカットのパス
     */
    public static File mkjlnk( String createPath,
                                String shortcutName,
                                String iconFile,
                                String arguments,
                                String description)
            throws IOException, InterruptedException {
        File cpath = new File( parse_env(createPath));
        File scut = WinShortcut.createShortcutForJavaApp(
                cpath, shortcutName,
                new File( parse_env(iconFile)),
                parse_env(arguments),
                description);
        pl.print( "MKLNK", scut.getAbsolutePath() );
        logList.add( "copy," + scut.getAbsolutePath() );
        return scut;
    }
    /**
     * スペシャルフォルダーを返す。
     * @param key WinShortcutで宣言されている定数を指定。
     * パスのセパレータが"/"に置換されたものを返す。
     */
    public static String getSpecialFolder(String key) throws IOException, InterruptedException {
        File path = WinShortcut.getSpecialFolder(key);
        if ( path == null ) throw new IllegalArgumentException(
                key + "に該当するパスを取得できない" );
        return path.getAbsolutePath().replace("\\", "/");
    }

    /**
     * Windowsの「アプリケーションの追加と削除」のメニューにアプリケーションを
     * 登録する。WinShortcut#setAppMenu()を呼び出しているのだが、
     * インストールログに情報を登録する機能、変数展開の機能が追加されている。
     *
     * @param folderName レジストリ...Uninstall\\の次に続くフォルダの名前
     * @param displayName メニューに表示されるアプリ名
     * @param installLocation アプリがインストールされている場所。通常Program Files/なんとかかんとか。
     * @param uninstallString アンインストーラへのパス。
     * @ohers 省略可能な要素。publisher 発行者名, displayVersion メニューに表示さ
     * れるバージョン, displayIcon メニューに表示されるアイコンへのパス nullなら省略。
     * @return 作成したレジストリ上のパス。この値でregDelete()すれば、登録した
     * アプリ名はメニューから消える。
     * @throws IOException
     * @throws InterruptedException
     */
    public static String regWindowsAppMenu( String folderName,
                                              String displayName,
                                              String installLocation,
                                              String uninstallString,
                                              String ...other )  throws IOException, InterruptedException {
        //System.out.println("oher[2] = " + other[2]);
        //System.out.println(env.get("PROGRAM"));
        if ( other.length == 3 ) {
            other[2] = parse_env(other[2]);
            //System.out.println("other[2](env) = " + other[2]);
        }
        String res = WinShortcut.setAppMenu(  folderName,
                                                displayName,
                                                parse_env(installLocation),
                                                parse_env(uninstallString),
                                                other );
        pl.print( "REGIST", res );
        String reg = "regist," + res;
        //何度もインストールしたときのログへの多重登録回避
        if ( ! logList.contains(reg) ) {
            logList.add( "regist," + res );
        }
        return res;
    }
    private static File uninstFile = null;
    /**
     * インストールを開始する前に必ず呼び出す。このクラスのセットアップを行う。
     * このメソッドを呼び出す前に、環境変数envをセットしておく。
     * インストールの開始は、まずアンインストーラ.jarをどこかに配置することから始まる。
     * このメソッドが呼ばれたとき、すでにアンインストーラが存在しているなら、
     * その書庫の中からインストールログを読み出し、mkdirs情報のみ抽出してログの初期値
     * とする。
     *
     * インストールの終了にはend_install()を必ず呼び出す。
     *
     * このクラスのcopy(src,dst)メソッドのsrcは、この一覧のなかのファイルでなければ
     * ならない。ローカルディクス上のファイルコピーではない点に注意。
     * @param uninst_folder アンインストーラーを配置するフォルダを指定する。
     */
    public static void start_install( String uninst_folder )  throws IOException {
        /*
         * インストールするファイル一覧が書かれたファイルを読み込む。
         * このクラスのcopy(src,dst)メソッドのsrcは、この一覧のなかのファイルでなければ
         * ならない。ローカルディクス上のファイルコピーではない点に注意。
         */
        JarUtils.getFileList(fileList);
        //for ( String s : fileList ) System.out.println(s);
        prog_step = pl.getBarMaxValue() * 0.95 / fileList.size();

        logList.clear();
        List<String> list = new ArrayList<String>();

        uninstFile = new File( parse_env(uninst_folder),
                                 "amateru_uninstaller.jar" );

        if ( uninstFile.exists() ) {
            // アンインストーラがあるときフォルダ情報とレジストリ登録を抽出し(削除に必要)
            JarUtils.getInstallLog( uninstFile, list );
            for ( String s : list ) {
                if ( s.startsWith("mkdirs") ) logList.add(s);
                else if ( s.startsWith("reg") ) logList.add(s);
            }
        }
        // アンインストーラーを書庫からコピー(最新のものに置き換える)
        copy( "/resources/amateru_uninstaller.jar", uninstFile );
//        // インストーラ起動用バッチファイルもコピー
//        copy( "/resources/UNINSTALL.bat", new File( parse_env(uninst_folder),
//                                                     "UNINSTALL.bat") );
    }
    /**
     * インストールの終了のときに必ず呼び出すこと。
     * アンインストーラのjar書庫にインストールログを書きこむ。
     * レジストリシステムノードにアンインストーラのパスを書き込む。
     * @throws IOException
     */
    public static void end_install() throws IOException {
        JarUtils.updateInstallLog(uninstFile, logList);
        Regist.sys.putBoolean("amateru_exists", true );
        Regist.sys.put("amateru_uninstaller", uninstFile.getAbsolutePath());
        pl.installEnd();
    }

    /**
     * インストールログを逆に辿りながらファイルとフォルダを削除する。
     * ただしアプリが生成したフォルダやファイルがあると、すべてを完全に消去することは
     * できない。インストール直後のまっさらな状態で実行すれぱ完全に消去されるのが正常。
     * @param logFile
     * @throws IOException
     */
    public static void uninstall( String logFile ) throws IOException {
        File file = new File( parse_env( logFile ) );
        if ( ! file.exists() ) throw new IllegalArgumentException
                ("アンインストーラが見つからずログが取得できない");

        List<String> list = new ArrayList<String>();
        JarUtils.getInstallLog(file, list);
        Collections.reverse(list);
        for ( String s: list ) {
            String [] token = s.split(",");
            if ( token[0].equals("copy") ) {
                System.out.println("DEL FILE " + new File(token[1]).getAbsolutePath() );
                new File(token[1]).delete();
            } else if ( token[0].equals("mkdirs")) {
                File dir = new File( token[1] );
                File dir2 = new File( token[2] );
                for(;;) {
                    File path = new File( dir,dir2.getPath() );
                    path.delete();
                    System.out.println("DEL FOLDER " + path.getAbsolutePath() );
                    dir2 = dir2.getParentFile();
                    if ( dir2 == null ) break;
                }
            }
        }
    }
    /**
     * 任意のプロパティをファイルに保存する
     * @param path 出力先ファイルのパス。変数展開あり。
     * @param comment プロパティファイルのコメント
     * @param p 出力するプロパティ。
     * @throws IOException
     */
    public static void saveProperties( String path,
                                         String comment,
                                         Properties p ) throws IOException{
        File file = new File( parse_env(path));
        OutputStream s = new FileOutputStream( file );
        p.storeToXML( s, comment );
        s.close();
        pl.print( "COPY", file.getAbsolutePath() );
        logList.add("copy," + file.getAbsolutePath() );
    }
}
