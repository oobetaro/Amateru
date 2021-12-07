package amateru_installer;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.getProperty;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2011-06-21
 */

/**
 * Windows環境でJavaからショートカットを作る。スタートメニューやデスクトップなど
 * のスペシャルフォルダーのパスを取得できる。レジストリを読み書き削除できる。
 * Windowsの「アプリケーションと追加と削除」メニューに簡単に登録できる。
 * VistaやWin7でUACで保護された環境で、アカウントを昇格させる機能もある。
 * <br><br>
 * JScriptを使った方法なので、ネイティブコードを使ったものより機種依存しにくい。
 * ネイティブコードを使ったこの手のAPIを使ってみたら、64bitのWinではエラーが出て
 * 使えなかった。JScriptならそのようなことはおきない。
 * JScriptはWin2K,XP,Vista,7で標準で入っている。<br><br>
 *
 * 外部プログラムの呼び出しなので実行速度はあまり速くないが、速度が要求される
 * 局面はまずないと考える。<br><br>
 *
 * メソッドはすべてスタティックメソッド。スレッドセーフではない。
 * @author 大澤義孝
 */
public class WinShortcut {

    // インスタンス作成禁止
    private WinShortcut() { }

    /**
     * デスクトップ
     */
    public static final String JS_DESKTOP = "DESKTOP";

    /**
     * お気に入り
     */
    public static final String JS_FAVORITES = "FAVORITES";

    /**
     * フォント
     */
    public static final String JS_FONTS = "FONTS";

    /**
     * マイドキュメント
     */
    public static final String JS_MYDOCUMENTS = "MYDOCUMENTS";

    /**
     * NETHOOD
     */
    public static final String JS_NETHOOD = "NETHOOD";

    /**
     * PRINTHOOD
     */
    public static final String JS_PRINTHOOD = "PRINTHOOD";

    /**
     * スタートメニューのプログラム
     *
     */
    public static final String JS_PROGRAMS = "PROGRAMS";

    /**
     * 最近使ったファイル
     */
    public static final String JS_RECENT = "RECENT";

    /**
     * 送る
     */
    public static final String JS_SENDTO = "SENDTO";

    /**
     * スタートメニュー
     */
    public static final String JS_STARTMENU = "STARTMENU";

    /**
     * スタートアップ
     */
    public static final String JS_STARTUP = "STARTUP";

    /**
     * テンプレート
     */
    public static final String JS_TEMPLATES = "TEMPLATES";

    /**
     * アプリケーションデータ
     */
    public static final String JS_Appdata = "APPDATA";

    /**
     * デスクトップ（全ユーザ共通）
     */
    public static final String JS_AllUsersDesktop = "ALLUSERSDESKTOP";

    /**
     * スタートメニュー（全ユーザ共通）
     */
    public static final String JS_AllUsersStartMenu = "ALLUSERSSTARTMENU";

    /**
     * スタートメニューのプログラム（全ユーザ共通）
     */
    public static final String JS_AllUsersPrograms = "ALLUSERSPROGRAMS";

    /**
     * スタートアップ（全ユーザ共通）
     */
    public static final String JS_AllUsersStartup  = "ALLUSERSSTARTUP";
    /**
     * ウィンドウ通常サイズ。ショートカット作成時に指定するウィンドウスタイル。値は2。
     */
    public static final int WSTYL_NORMAL    = 2;
    /**
     * ウィンドウ最大化。ショートカット作成時に指定するウィンドウスタイル。値は3。
     */
    public static final int WSTYL_MAXIMAIZE = 3;
    /**
     * ウィンドウ最小化。ショートカット作成時に指定するウィンドウスタイル。値は7。
     */
    public static final int WSTYL_MINIMIZE  = 7;

    /**
     * ショートカットを作成する。
     * ショートカットを作るのはテンポラリフォルダにJScriptのファイルを作り、
     * それをRuntime#execで実行することによって行う。
     * ショートカットも一旦テンポラリフォルダに作成し、無事に作成できたら指定された
     * 場所にコピーする。これはエラーなのに既存のショートカットを削除してしまったり、
     * 上書きしてしまうリスクを減らすための措置。
     * メソッド終了後、テンポラリに作成されたJScriptやショートカットファイルは削除
     * される。
     *
     * @param shortcutFileName ショートカットファイル名(".lnk"はつけないこと)
     *
     * @param arguments アプリに渡す引数。引数をコマンドラインに打ち込むときと同様に
     * だらだらと書く。ダブルクォートで一部のパラメーターを囲んで
     * もかまわないし、必要におうじてそうしなければならない。
     *
     * @param iconFile "*.ico"ファイルオブジェクト。このファイルを削除すると、
     * ショートカットのアイコンは白紙になる。永続的なファイルを指定すること。
     * ショートカットを作り終わったらアイコン画像ファイルを削除してよいという性質では
     * ないことに注意。
     * アイコンが無いときはnullを指定する。exeファイルへのショートカットなら、その
     * exeファイルのデフォルトのアイコンで作成される。存在しないアイコンファイルを
     * 指定するとIllegalArgumentException。
     *
     * @param description ショートカットプロパティのコメント欄またはToolTipに表示
     * される説明文。
     *
     * @param parentFolder ショートカットをどこのフォルダに作るか。
     * たとえばgetSpecialFolder()で取得したパスを指定する。
     *
     * @param linkFile ショートカットを作成するファイルやフォルダのパス。
     * このメソッドはこのファイルへのショートカットを作る。String型なのには理由
     * があり、必ずしも完全なFileオブジェクトを渡せばよいというものではないから。
     * c:/Program File内のjavaw.exeに直リンクしたとき、管理者アカウントならそれで
     * もよいが、標準ユーザアカウントではWin7,WinVistaではUACによって実行が制限
     * されてしまうことがある。管理者パスワードを毎回求められたりする。
     * ではどうするのが正しいかというと、
     * C:/Windows/system32/javaw.exeにリンクを貼るのが正しい。しかしJava側の
     * システムプロパティからこのパスを取得することはできない。Win2KやXPでは
     * C:/WINNT/system32/javaw.exeになる。OSによって異なる。
     * このときフルパスを指定せず、"javaw"とだけ指定すれば、JScriptは自動的に
     * 期待するjavaw.exeにリンクを作ってくれる。つまりそういうことをしたいがため
     * にあえてStringで渡すわけ。
     *
     * @param workingDirectory 作業フォルダ。無指定はnullを指定。
     *
     * @param windowStyle 2=通常,3=最大化,7=最小化。それ以外の値は無指定となる。
     * ただしjavawを指定してJavaアプリを実行するような場合、この指定は反映されない。
     * ネイティブコードで書かれたプログラムのみに有効。
     *
     * @param hotkey ホットキー。nullなら無指定。
     *
     * @throws IllegalStateException Windows以外のプラットホームで実行されたとき。
     * @throws InterruptedException JScriptの実行でエラーが起きたとき。
     * @throws IllegalArguementException 異常な引数が指定されたとき。およびそれに
     * よってショートカットが作成できなかったとき。
     *
     * @return 作成されたショートカットファイルのパス。
     * 2011-08-09 linkFile,workingDirectory他、パスを指定をURL表記にしていたのを
     * 普通の絶対パス名に変更。Win7では動作してもWin2Kでは動作しなかったため。
     * 2011-08-19 linkFileをStringで渡すように修正。
     */

    public static File createShortcut(  final File parentFolder,
                                          final String shortcutFileName,
                                          final File iconFile,
                                          final String linkFile,
                                          final String arguments,
                                          final String description,
                                          final File workingDirectory,
                                          final int windowStyle,
                                          final String hotkey )

                                     throws IOException, InterruptedException {

        if ( ! getProperty("os.name").matches("^Windows.*") )
            throw new IllegalStateException("プラットホームがWindowsではない");

        if ( linkFile == null || linkFile.isEmpty() ) throw
                new IllegalArgumentException( linkFile + "が無指定" );

        if ( ! parentFolder.exists() ) throw new IllegalArgumentException
                ( parentFolder + "が無い" );

        if ( parentFolder.isFile() ) throw new IllegalArgumentException
                ( parentFolder + "はファイルです" );

        File scutFile = new File( parentFolder, shortcutFileName + ".lnk" );

        // テンポラリフォルダに作られる予定のショートカットファイルへのパス
        final File temp_scut = new File( getProperty( "java.io.tmpdir" ),
                                    shortcutFileName + ".lnk");
        execute( new Script() {

            @Override
            public void write(PrintWriter w) {
                w.println( "shell = new ActiveXObject('WScript.Shell')" );
                w.println( "createPath = '"
                        + esc(temp_scut.getParentFile().getAbsolutePath()) + "'");
                w.println("link = shell.createShortcut( createPath + '\\\\' + '"
                        + shortcutFileName + ".lnk')");

//                w.println( "link.TargetPath = '" + esc(linkFile.getAbsolutePath()) + "'");
                w.println( "link.TargetPath = '" + esc(linkFile) + "'");
                w.println( "link.Arguments = '" + esc(arguments) + "'" );
                w.println( "link.Description = '" + description + "'");

                if ( iconFile != null ) {
                    if ( ! iconFile.exists() )
                        throw new IllegalArgumentException
                            ( iconFile + "が存在しない");

                    w.println( "link.IconLocation = '"
                                + esc(iconFile.getAbsolutePath()) + "'" );

                }
                if ( workingDirectory != null ) {
                    if ( ! workingDirectory.exists() )
                        throw new IllegalArgumentException
                            ( workingDirectory + "が存在しない");

                    w.println( "link.WorkingDirectory = '"
                            + esc(workingDirectory.getAbsolutePath()) + "'");

                }

                if ( windowStyle == 2 || windowStyle == 3 || windowStyle == 7 )
                    w.println( "link.WindowStyle = " + windowStyle );

                if ( hotkey != null )
                    w.println( "link.HotKey = '" + hotkey + "'");
                w.println( "link.save()" );
            }

        });
        if ( ! temp_scut.exists() ) throw new IllegalArgumentException
                ("ショートカットの作成に失敗");

        /* テンポラリに無事に作成できたら本来の場所にコピー。こうすることで既存の
           ショートカットが存在しても壊す可能性を極力排除している。*/

        ZipUtils.copy( temp_scut, scutFile );
        temp_scut.delete();
        if ( ! scutFile.exists() ) throw new IllegalArgumentException
                ("ショートカットの作成に失敗");
        return scutFile;
    }

    /**
     * javaw.exeをつかって、jarファイルを実行するショートカットをデスクトップに作成する。
     * ショートカットを作るのはテンポラリディレクトリにJScriptのファイルを作り、
     * それをRuntime#execで実行することによって行う。
     * メソッド終了後、JScriptのファイルは削除される。
     *
     * @param shortcutName ショートカットファイル名(".lnk"はつけないこと)
     * @param arguments javaw.exeに渡す引数。javaw ...以降に書く引数をコマンドライン
     * に打ち込むときと同様にだらだらと書く。ダブルクォートで一部のパラメーターを囲んで
     * もかまわない。
     * @param iconFile "*.ico"ファイルオブジェクト。このファイルを削除すると、
     * ショートカットのアイコンは白紙になる。永続的なファイルを指定すること。
     * ショートカットを作り終わったら削除してよいという性質ではない。
     * @param description ショートカットプロパティのコメント欄またはToolTipに表示
     * される説明文。
     * @param createPath ショートカットを作成するフォルダのパス。
     * たとえばgetSpecialFolder()で取得したパスを指定する。
     *
     * @throws IllegalStateException javaw.exeが見つからなかったとき。
     *
     * Windows以外のプラットホームで実行されたとき。
     */
    public static File createShortcutForJavaApp( File createPath,
                                                     String shortcutName,
                                                     File iconFile,
                                                     String arguments,
                                                     String description ) throws IOException, InterruptedException {

        if ( ! getProperty("os.name").matches("^Windows.*") )
            throw new IllegalStateException("プラットホームがWindowsではない");
        return createShortcut( 
                createPath,
                shortcutName,
                iconFile, 
                getJavawPath(),           
                arguments,
                description,
                null,
                WSTYL_NORMAL,
                null );
    }
    /**
     * Windowsのシステムフォルダ下に配置されたjavaw.exeのパスを返す。
     * @exception IllegalStateException javaw.exeが見つからないとき
     * @return javaw.exeの絶対パス
     */
    public static String getJavawPath() {
        File f = new File( System.getProperty("java.home"));
        f = new File(f,"bin");
        f = new File(f,"javaw.exe");
        if ( ! f.exists() ) 
            throw new IllegalStateException("javaw.exeの絶対パスを取得できず");
        return f.getAbsolutePath();
    }

    /**
     * Windowsのスペシャルフォルダーのパスを返す。
     * @param key "Desttop",AllUsersPrograms"などWindows固有のキーワードを指定。
     * 大文字小文字の区別はなし。このクラスのフィールド定数JS_xxxxのものはすべて
     * 取得できる。他のキーワードもあるかもしれないが未調査。
     * @return Windowsのパス。セパレータは円記号。取得できなかったときはnull。
     * @throws IllegalStateException Windows以外のプラットホームで実行されたとき。
     */
    public static File getSpecialFolder( final String key)
                                    throws IOException, InterruptedException {
        String s = execute( new Script() {
            @Override
            public void write( PrintWriter w ) {
                w.println( "shell = new ActiveXObject('WScript.Shell')" );
                w.println( "WScript.echo( shell.SpecialFolders('" + key + "') )" );
            }
        });
        return new File( s.trim() );
    }

    /**
     * JScriptを実行する。
     * @param script スクリプトをファイルに出力する実装コード
     * @return JScriptを実行したときのプログラムの出力メッセージ
     * @throws IOException スクリプトをテンポラリフォルダに書き出せないとき
     * @throws InterruptedException JScriptを起動すること自体に失敗したとき
     * @throws IllegalStateException Windows以外のプラットホームで呼び出されたとき
     * @throws IllegalArgumentException 恐らくスクリプトにエラーがある
     */
    static String execute( Script script )
                                    throws IOException, InterruptedException {
        if ( ! getProperty("os.name").matches("^Windows.*") )
            throw new IllegalStateException("プラットホームがWindowsではない");

        File scriptFile = new File(
                getProperty( "java.io.tmpdir" ), "script.js" );
        PrintWriter w = null;
        InputStreamReader r = null;
        StringBuilder result = new StringBuilder();
        InputStreamReader err = null;
        try {

            w = new PrintWriter( scriptFile,"sjis" );
            script.write(w);
            w.close();
            Process proc = Runtime.getRuntime()
                .exec( "cscript -Nologo " + scriptFile.getAbsolutePath() );
            proc.waitFor();
            // エラーストリームになにか戻ったときはエラーを出す
            err = new InputStreamReader( proc.getErrorStream(),"sjis");
            StringBuilder errbut = new StringBuilder();
            while( err.ready() ) errbut.append( (char)err.read() );
            if ( errbut.length() > 0 )
                throw new IllegalArgumentException( errbut.toString() );

            // スクリプト実行結果を読み取る
            r = new InputStreamReader( proc.getInputStream(),"sjis" );
            while ( r.ready() ) result.append( (char)r.read() );
            return result.toString();

        } catch ( IOException e ) {
            throw e; /* たとえここでスローしてもfinallyブロックは処理される */
        } catch ( InterruptedException e ) {
            throw e;
        } finally {
            try { w.close(); } catch ( Exception ex ) {}
            try { r.close(); } catch ( Exception ex ) {}
            try { err.close(); } catch ( Exception ex ) {}
            //scriptFile.delete(); //デバッグの都合上、削除してない
        }
    }
    /**
     * 文字列中の"\"記号を"\\"に置換する。
     * たとえばレジストリのパスをソースコード中に書くとき、
     * "HKEY_LOCAL_MACHINE\\SOFTWARE\\..."などと記述する。
     * Javaのソースコードで文字列中に"\"記号を書くときは"\\"と書かないといけない。
     * このように書かれた文字列を一文字ごとにchartAt()すれば、記号は一つしか検出
     * されない。例外的に生じる制約だ。この制約はJScriptにもある。
     * JScriptのソースコードをJavaで生成するとき、Javaで書かれた上記のレジストリキー
     * を単純に書きだせば、ソースファイルの中ではセパレータは一つの円記号になる。
     * これではまずいので、事前に２つ連続させた文字列に置換してやる必要がある。
     * しかし、String#replaceAll()は正規表現なので、円記号の扱いがややこしい。
     * このメソッドは単純なやり方で間違いがない。
     */
    public static String esc( String s ) {
        StringBuilder sb = new StringBuilder();
        for ( char c : s.toCharArray() ) sb.append( ( c == '\\') ? "\\\\" : c );
        return sb.toString();
    }

    /**
     * レジストリにキーと値を書きこむ
     * @param key レジストリのキー
     * @param value レジストリの値
     * @param type レジストリの型 nullを指定するとREG_SZ(文字列)とみなされる。
     * @throws IOException
     * @throws InterruptedException
     */
    public static void regWrite( final String key,
                                  final String value,
                                  final String type )
                                    throws IOException, InterruptedException {
        //System.out.println("value = " + value);
        execute( new Script() {
            @Override
            public void write( PrintWriter w ) {
                w.println( "var shell = new ActiveXObject('WScript.Shell')" );
                w.print( "shell.RegWrite( '" + esc( key ) );
                w.print( "','" + esc( value ) );
                w.print( "','" + ( ( type == null ) ? "REG_SZ" : type ) );
                w.println("' )");
                //System.out.println("ESC VALUE = " + esc(value));
            }
        } );
    }

    /**
     * レジストリにキーと値を書きこむ。文字列型のみ。
     * @param key レジストリのキー
     * @param value レジストリの値
     */
    public static void regWrite( String key,
                                  String value ) throws IOException, InterruptedException {
        regWrite( key, value, null );
    }

    /**
     * レジストリを読み出す
     * @param key キー名
     * @return レジストリの値
     * @throws IOException
     * @throws InterruptedException
     */
    public static String regRead( final String key )  throws IOException, InterruptedException {
        if ( key == null )
            throw new IllegalArgumentException("引数が不正です");
        String res = execute( new Script() {
            @Override
            public void write( PrintWriter w ) {
                w.println( "var shell = new ActiveXObject('WScript.Shell')" );
                w.println( "WScript.Echo( shell.RegRead('" + esc(key) + "') )");
            }
        } );
        return res.replaceAll(".*\n$", "");
    }

    /**
     * レジストリを削除する
     * @param key
     * @throws IOException
     * @throws InterruptedException
     */
    public static void regDelete( final String key )  throws IOException, InterruptedException {
        if ( key == null )
            throw new IllegalArgumentException("引数が不正です");
        String res = execute( new Script() {
            @Override
            public void write( PrintWriter w ) {
                w.println( "var shell = new ActiveXObject('WScript.Shell')" );
                w.println( "shell.RegDelete('" + esc(key) + "')");
            }
        } );
    }

    /**
     * 「アプリケーションの追加と削除」のメニューの中に、アプリケーションの情報を
     * 登録する。これは次のレジストリに値を登録することで行われる。<br>
     * "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\"<br>
     * @param folderName レジストリ...Uninstall\\の次に続くフォルダの名前
     * @param displayName メニューに表示されるアプリ名
     * @param installLocation アプリがインストールされている場所。通常Program Files/なんとかかんとか。
     * @param uninstallString アンインストーラへのパス。
     * @param others 省略可能な要素。publisher 発行者名, displayVersion メニューに表示さ
     * れるバージョン, displayIcon メニューに表示されるアイコンへのパス nullなら省略。
     * @return 作成したレジストリ上のパス。この値でregDelete()すれば、登録した
     * アプリ名はメニューから消える。
     * @throws IOException
     * @throws InterruptedException
     */
    public static String setAppMenu( String folderName,
                                      String displayName,
                                      String installLocation,
                                      String uninstallString,
                                      String ...others  )
                                    throws IOException, InterruptedException {

        String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\"
                   + "CurrentVersion\\Uninstall\\" + folderName + "\\";
        regWrite( key + "DisplayName"    , displayName );
        regWrite( key + "InstallLocation", installLocation );
        regWrite( key + "UninstallString", uninstallString );

        String [] names = { "Publisher", "DisplayVersion", "DisplayIcon"};

        for ( int i = 0; i < others.length; i++ ) {
            if ( others[i] == null || others[i].isEmpty() ) continue;
            //System.out.println("key = " + key+names[i] + ", val = " + others[i] );
            regWrite( key + names[i], others[i] );
        }
        return key;
    }

    /**
     * UACを制御してアカウントを自動昇格させるスクリプトを実行する。
     * なおこのメソッドはjar化された実行ファイル上で実行されることを前提にして
     * いるので、NetBeansからの実行では不都合が起きる可能性がある。
     * UACはVista以降のOSにしかないので、このメソッド実行前にOSバージョンの判定
     * を行いUACがないWindowsで呼ばないように注意すること。
     * @param script リソース内のjsソースファイルのパス。
     * たとえば"/resources/uac.js"など。uac.jsには昇格用のコードが書かれている
     * ことが前提。
     * @param className 昇格後にjavaw.exeで起動するクラスファイル名。
     * たとえばMain2.class.getName()などで指定する。
     *
     * このクラスにはエントリーとなるmainメソッドが実装されていること。
     * @throws IOException
     * @throws InterruptedException
     */
    public static void greadup( final String script,
                                 final String className )
                                     throws IOException, InterruptedException {
        String res = execute( new Script() {
            @Override
            public void write( PrintWriter w ) {
                // classPathは実行のされ方によってフルパスで戻る場合と相対パス
                // で戻る場合があるため、判定しないと正しいパスが得られない
                File classPath = new File( getProperty("java.class.path") );
                String cp = new File( getProperty("user.dir"),
                         getProperty("java.class.path") ).getAbsolutePath();
                if ( classPath.isAbsolute() ) {
                    cp = classPath.getAbsolutePath();
                }
                String text = ResourceLoader.getText("/resources/uac.js", "sjis");
                w.println( text );
                w.println( "Wsh.Run( \"javaw.exe -cp \\\"" + esc(cp) + "\\\" "
                          +  className + "\", 1, false );" );
            }
        } );
    }

    static void test2() throws Exception {
        setAppMenu( "AMATERU2.0",
                    "AMATERU2.0",
                    "C:\\Program Files\\Amateru2.0",
                    "javaw -jar C:\\Program Files\\Amateru2.0\\uninstaller.jar",
                    "大澤義孝",
                    "2.0",
                    null );
    }

    static void test3() throws Exception {
        regDelete("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\"
                + "CurrentVersion\\Uninstall\\AMATERU2.0\\");
    }
    public static void main(String [] args) throws Exception {
//        String key = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\AMATERU2.0\\";
//        System.out.println( regRead( key + "Publisher") );
//        regWrite( key + "Publisher","大沢義孝");
//        System.out.println( regRead( key + "Publisher") );
//        regDelete( key );
        //test2();
        //test3();
//        System.out.println( getSpecialFolder(JS_DESKTOP) );
//        main2();
        //main3();
        System.out.println( getJavawPath() );
    }

    /**
     * テスト。HSQLDBのクライアントを起動するショートカットをデスクトップに
     * 作成する。
     */
    public static void main2() throws Exception {
        String arguments = "-classpath "
                + "\"C:/Users/Public/Documents/hsqldb_1.8.0.7/lib/hsqldb.jar;"
                + "C:/Users/Public/Documents/mysql-connector-java-5.1.16/"
                + "mysql-connector-java-5.1.16-bin.jar\" "
                + "org.hsqldb.util.DatabaseManagerSwing";
        File icon = new File("./src/resources/test.ico");
        File scut = createShortcutForJavaApp(
                getSpecialFolder(JS_DESKTOP), "SQL CLIENT",
                icon, arguments,
                "MySQLやHSQLDBの簡易クライアント");
        System.out.println( scut != null ? "作成成功"+scut : "失敗" );
    }

    public static void main3() throws Exception {
        File icon = null; //new File("./src/resources/test.ico");
//        File linkFile = new File("c:/Program Files/Hidemaru/Hidemaru.exe");
        String linkFile = "c:/Program Files/Hidemaru/Hidemaru.exe";
        File scut = createShortcut(getSpecialFolder(JS_DESKTOP),
                "ひでまる",icon,linkFile, "", "ひでまるエディター", null, 2, null);
        System.out.println( scut != null ? "作成成功" + scut : "失敗" );
    }
}
