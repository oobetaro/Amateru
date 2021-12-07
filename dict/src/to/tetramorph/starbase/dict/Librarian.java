/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import to.tetramorph.starbase.util.DictionaryRequest;

/**
 * 複数のDictionaryを管理するクラス。これはシングルトンのクラス。
 * <pre>
 * ファイル形式には５つのケースがある。
 * ABFファイル形式の場合はプロパティでeditable,need_pwに値がセットされる。
 * ----------------------------------------------------------------------
 *             編集禁止   編集禁止  編集許可  編集許可
 *             PW無し     PW有り    PW無し    PW有り
 * editable    false      false     true      true
 * need_pw     false      true      false     true
 *             ※1        ※2       ※3       ※2
 * ※1 デフォルトPWで暗号化
 * ※2 ユーザ指定PWで暗号化
 * ※3 暗号化せず平文のまま
 * ----------------------------------------------------------------------
 * XMLファイル形式の場合
 * editable,need_pwのフラグは無く、平文で保存
 * ----------------------------------------------------------------------
 * editable=falseとなっているファイルをいじくって、trueに変更しても読みこむ事は
 * できない。なぜならば、デフォルトPWで暗号化されているから。
 * しかし、ソースを調べて専用にプログラムを書けば、ロックを外す事は可能。
 * せいぜい自転車の鍵程度のものでしかないが、無いよりはマシだと考える。
 * </pre>
 * @author 大澤義鷹
 */
class Librarian {

    private List<Book> bookList;
    private DefaultTreeModel model;
    private static Librarian INSTANCE;
    private Window owner;

    //編集禁止モードで書き出す場合のパスワード。編集禁止で、中身が平文だとあまり
    //に能なしなので、一応デフォルトのパスワードで保護するときに使用する。
    private static final byte []
            DEFAULT_PASSWORD = CipherUtils.getDigest("Amateru Book File");

    private TreeModelHandler treeModelHandler = new TreeModelHandler();
    private DictTable table;
    /**
     * 外部からコンストラクタの呼びだしは禁止。
     * インスタンスの取得は、getInstance()を使用します。
     * ブックファイルを全て読みこみ、ツリーモデル上に挿入。
     */
    private Librarian() {
        bookList = new ArrayList<Book>();
        model = new DefaultTreeModel( DictNode.getNewFolder("書庫") );
        //loadBooks();
        model.addTreeModelListener(treeModelHandler);
    }

    /**
     * Librarianオブジェクトを返す。このクラスはシングルトンなので、つねに同じ
     * インスタンスの参照を返す。
     * @return
     */
    public static Librarian getInstance() {
        if ( INSTANCE == null )
            INSTANCE = new Librarian();
        return INSTANCE;
    }
    /**
     * ブックファイルを読みこむ。これは初めてgetInstance()した直後、一度だけ
     * 呼び出す必要がある。引数には辞書アクションを定義したマップを与えなければ
     * ならない。二つのマップはチャートモジュールから、アクションファイルのURL
     * を取得することで作る。モジュールクラス名から直接アクション内容のマップに
     * 変換するようにすると単純で良いのだが、内容マップは少し大きなサイズになる
     * し、複数のチャートモジュールで同じアクションマップを使いたい場合もある。
     * そこでurlMapからURLを求め、そのURLでactionMapを引くという二段構えにして、
     * リソースを節約している。
     * @param urlMap 「チャートモジュールクラス名→アクションファイルのURL」の
     * 変換マップ
     * @param actionMap 「アクションファイルのURL→アクションファイルの内用を
     * 格納したマップ」の変換マップ。
     */
    public void loadBooks( DictTable table ) {
        getInstance();
        this.table = table;
        model.removeTreeModelListener(treeModelHandler);
        loadBooks();
        model.addTreeModelListener(treeModelHandler);
    }
    /**
     * ブックファイルへのパス一覧をprefから取得し、それをFileのリストに
     * して返す。パスが一件も無いときは、デフォルトの辞書のパスを返す。
     *
     * パスは"*"をセパレータとして、単純に文字列として連結したものがprefに登録
     * されている。それを分解して複数のFileオブジェクトを作成する。
     * "*"は決して絶対パスの文字列に含まれることがないので、このコードを使用
     * している。
     * @return パスのリスト
     */
    private List<File> getIndexes() {
        List<File> list = new ArrayList<File>();
        String dictIndexes = Config.usr.getProperty(  "dictIndexes",
                new File(System.getProperty("app.dict"),"std_dict.abf")
                .getAbsolutePath()  );
//        if ( dictIndexes.isEmpty() ) {
//            System.out.println("設定が空でっせ");
//            return list;
//        }
        String [] paths = dictIndexes.split("\\*");
        for ( String path : paths ) {

            System.out.println("#" + path);
            File file = new File( path );
            if ( ! file.exists() ) continue;
            list.add( file );
        }
        return list;
    }
    /**
     * 現在のツリーの状態からブックファイルへのパスを抽出しprefに登録する。
     * ツリーから本を削除したり、挿入したときはこのメソッドを呼び出してpref
     * の内容を更新する。本の順列はツリーのノードの位置に基づく。
     * パスは"*"をセパレータとして、単純に文字列として連結したものを登録。
     * loadIndexesでは、再びそれを分解して複数に戻す。
     */
    public void updateIndexes() {
        Object root = model.getRoot();
        int size = model.getChildCount(root);
        StringBuilder sb = new StringBuilder(200);
        for ( int i = 0; i < size; i++ ) {
            DictNode node = (DictNode)model.getChild(root, i);
            String path =
                 getBook( node.getTreePath() ).getFile().getAbsolutePath();
            sb.append(path);
            sb.append("*"); //末尾の"*"はついていてもsplitで問題は無し
        }
        Config.usr.setProperty("dictIndexes", sb.toString());
    }

    /**
     * あたらしく本を作る。
     * アクションマップや、自然言語変換マップ（テンプレート）などを選択もしくは
     * 設定、ブックファイルの指定、などを行って、内容は空のブックノードを挿入。
     * いまいちこの作業が進まないのは、基本ホロスコープ用のテンプレしかなく、
     * また当面はそれだけあれば事が足りるし、他の占いメソッドのプラグインも存在
     * しないのでテンプレも作りようが無いのが原因。
     * この機能は保留にして、本文の編集や検索機能を先に実装したほうが良いだろう。
     */
    public void createBook() {
        throw new UnsupportedOperationException("まだ未実装");
    }

    /**
     * ブックファイルを読みこみ用のInputStreamを返す。パスワードが設定されている
     * ブックファイルの場合、ダイアログを開きパスワードの入力を求める。
     * ABFファイルなら一旦配列に読みこみ、それをストリームにして返すが、
     * XMLファイルならFileInputStreamを返す。
     * @param file ブックファイル
     * @param p 暗号化、パスワード等の情報が入ったプロパティ。
     * @return ブックファイル読みこみ用のストリーム
     * @throws java.util.zip.ZipException
     * @throws java.io.IOException
     */
    private InputStream getBookInputStream( File file,Properties p )
                                            throws ZipException, IOException {
        String path = file.getPath().toLowerCase();
        if ( path.matches(".*\\.abf$") ) {
            byte[] array = FileUtils.loadZip(file, p);
            boolean need_pw = p.getProperty("need_pw","").equals("true");
            boolean editable = p.getProperty("editable","").equals("true");
            if ( ! editable && ! need_pw ) { //編集禁止、pwなし
                byte [] temp = null;
                try {
                    temp = CipherUtils.decrypt( DEFAULT_PASSWORD, array);
                } catch (Exception e ) {
                    throw new IllegalArgumentException(
                            "内容が異常なファイルなので読みこみできません");
                }
                return new ByteArrayInputStream(temp);
            } else if ( need_pw ) { //編集禁止or許可, pwあり
                byte [] temp = null;
                byte [] pw = null;
                String passwd = Config.usr.getProperty(path, "");
                if ( passwd.equals("")) {
                    String mes = String.format(
                            "ファイル\"%s\"\n" +
                            "「%s」には鍵がかけられています。\n" +
                            "パスワードを入力してください。",
                            file.getName(),p.get("title"));
                    passwd = passwordDialog(mes);
                    if ( passwd == null ) return null;
                    pw = CipherUtils.getDigest(passwd);
                } else {
                    pw = CipherUtils.getBytes(passwd);
                }
                try {
                    temp = CipherUtils.decrypt(pw, array);
                } catch (javax.crypto.BadPaddingException e ) {
                    Config.usr.remove(path);
                    throw new IllegalArgumentException(
                            "パスワードが違います");
                } catch ( Exception e ) {
                    Config.usr.remove(path);
                    Logger.getLogger(Librarian.class.getName())
                            .log(Level.SEVERE,null,e);
                    throw new IllegalArgumentException(
                            "内容が異常なファイルなので読みこみできません");
                }
                //パスワードはハッシュした値を不揮発性プレファランスに保管
                Config.usr.setProperty(path, CipherUtils.getString(pw));
                return new ByteArrayInputStream(temp);
            } else { //編集許可, pwなし
                return new ByteArrayInputStream(array);
            }
        } else if ( path.matches(".*\\.xml$") ) {
            FileInputStream fis = new FileInputStream(file);
            return fis;
        }
        throw new IllegalArgumentException(
                    "アマテルブックファイルではありません");
    }

//    public static void main(String [] args) throws Exception {
//        Librarian lib = Librarian.getInstance();
//        Properties p = new Properties();
//        File file = new File("C:/Documents and Settings/おーさわよしたか/デスクトップ/新しいフォルダ/hoge2.abf");
//        InputStream stream = lib.getBookInputStream(file,p);
//        BookData data = FileUtils.getBookNode2(stream);
//    }

    /**
     * 指定された一つのブックファイルを読みこみBookオブジェクトで返す。
     * @param bookFile *.abf,*.xmlファイルを指定する。
     */
    private Book loadBook( File bookFile )
            throws ParserConfigurationException, SAXException, IOException {
        Properties hedderProp = new Properties();
        InputStream stream = getBookInputStream( bookFile, hedderProp );
        Map<String,DictAction> actionMap = null;
        BookData bookData = FileUtils.getBookNode2(stream);
        String dictType = bookData.prop.getProperty("dictType","NPTChart");

        if ( table == null ) {
            //これはTestFrameから起動した場合。テスト用。
            actionMap = FileUtils.getTestDictActionMap();
        } else {
            //アマテルから起動した場合はtableにアクションの情報がセットされている
            actionMap = table.getDictActionMap(dictType);
            if ( actionMap == null ) {
                actionMap = FileUtils.getTestDictActionMap();
            }
        }
        stream.close();
        return new BookEntity( bookFile, hedderProp, actionMap, bookData );
    }

    /**
     * indexFileに書かれているすべてのブックファイルを読みこみ、Bookオブジェクト
     * にしてbookListに格納する。さらにBookオブジェクト内のツリーは一つの
     * ツリーモデルに挿入する。importBook()から呼び出される。
     */
    private void loadBooks() {
        List<File> list = getIndexes();
        for ( File dictFile : list ) {
            try {
                bookList.add(loadBook(dictFile));
            } catch (Exception ex) {
                Logger.getLogger(Librarian.class.getName())
                        .log(Level.SEVERE,null,ex);
            }
        }
        for ( int i = 0; i < bookList.size(); i++ ) {
            Book book = bookList.get(i);
            DictNode root = (DictNode)model.getRoot();
            model.insertNodeInto(book.getRootNode(), root, i);
        }
        //本を読みこみ中エラーが出る場合も考えられるので読みこみできたリスト
        //を書き戻す。
        updateIndexes();
    }
    /**
     * 指定された本をファイルから読みこみなおす。そのときツリーモデル上の位置は
     * 変更しないように配慮して読みこむ。
     * @param book
     */
    private void reloadBook( Book book ) {
        Book reloadBook = null;
        try {
            reloadBook = loadBook(book.getFile());
        } catch ( Exception e ) {
            Logger.getLogger(Librarian.class.getName())
                    .log(Level.SEVERE,null,e);
            errorDialog("ファイルの再読込ができません : "+e.getMessage());
            return;
        }
        model.removeTreeModelListener(treeModelHandler);
        //現在のブックノードの位置を得る
        int index = model.getIndexOfChild(model.getRoot(), book.getRootNode());
        model.removeNodeFromParent(book.getRootNode()); //ブックノード削除
        //リロードしたものを、同じ位置に再挿入
        model.insertNodeInto( reloadBook.getRootNode(),
                              (DictNode)model.getRoot(),
                              index );
        //リストに入っている本をリロードしたものに置き換え
        int i = bookList.indexOf(book);
        bookList.set(i, reloadBook );
        model.addTreeModelListener(treeModelHandler);
    }
    /**
     * 指定された本を現在のフォーマットと同じ条件で上書きする。
     * @param path
     */
    private void saveBook(Book book) throws NoSuchAlgorithmException,
                                                NoSuchPaddingException,
                                                InvalidKeyException,
                                                IllegalBlockSizeException,
                                                BadPaddingException,
                                                FileNotFoundException,
                                                IOException {
        File file = book.getFile();
        System.out.println("input file = " + file );
        if ( file == null )
            throw new IllegalArgumentException("ファイルがセットされていません");
        int type = FileUtils.getFileType(file);
        boolean editable = book.getBookProperties().
                getProperty("editable","true").equals("true");
        boolean need_pw = book.getBookProperties().
                getProperty("need_pw","false").equals("true");
        if ( ! editable )
            throw new IllegalArgumentException("編集禁止なので保存できません");

        Properties p = book.getBookProperties();
        byte [] doc = FileUtils.getByteArray(book, p);
        p.setProperty( "title", (String)book.getRootNode().getUserObject() );
        if ( type == FileUtils.ABF_FILE ) {
            if (need_pw) {
                String password = Config.usr.getProperty(file.getPath().toLowerCase(), "");
                if ( password.equals(""))
                    throw new IllegalArgumentException(
                            "この本のパスワードが不明です");
                byte[] pw = CipherUtils.getBytes(password);
                doc = CipherUtils.encrypt(pw, doc);
            } else if (!editable) {
                doc = CipherUtils.encrypt(DEFAULT_PASSWORD, doc);
            }
            FileUtils.saveZip(file, p, doc);
        } else { //XMLファイルのとき
                FileUtils.saveByteArray(file, doc);
        }
    }

    /**
     * 開いている本のうち、なんらかの編集があったブックファイルをすべて上書きする。
     * 上書き保存するだけでクローズはしない。
     */
    public void saveBooks() {
        for ( Book book : bookList ) {
            if ( treeModelHandler.contains(book)) { //変更されたものだけを保存
                try {
                    saveBook(book);
                    treeModelHandler.remove(book);
                    System.out.println(book.getTitle()+"を保存");
                } catch (Exception e ) {
                    Logger.getLogger(Librarian.class.getName())
                            .log(Level.SEVERE,null,e);
                    errorDialog(e.getMessage());
                }
            }
        }
    }

    /**
     * ダイアログで保存するかしないかの確認を行いながらすべての本を保存する。
     * 保存しない選択が行われたときは、その本をファイルからリロードし、編集前の
     * 状態に戻す。
     * このメソッドはダイアログを閉じる際に使用する。
     */
    public void confirmSaveOrReloadBooks() {
        for ( Book book : bookList ) {
            if ( treeModelHandler.contains(book) ) { //保存の必要性があるものだけ
                if ( ! confirmSaveBook(book) ) {
                    reloadBook(book);
                }
            }
        }
    }
    /**
     * 編集された本を事前確認したのち保存する。未編集の本が指定されたときは、
     * 保存操作はスキップする。
     * @param book
     * @return 保存された場合はtrue。中止やエラーの場合はfalseを返す。
     * またエラーの場合はエラーダイアログで通知する。
     */
    private boolean confirmSaveBook( Book book ) {
        String mes = String.format("「%s」は変更されています。\n" +
                "編集結果をファイルに上書きしますか？", book.getTitle());
        boolean yes = confermDialog( mes,"本のファイル保存");
        if ( yes ) {
            try {
                saveBook(book);
                treeModelHandler.remove(book);
                System.out.println(book.getTitle()+"を保存した");
                return true;
            } catch ( Exception e ) {
                Logger.getLogger(Librarian.class.getName())
                        .log(Level.SEVERE,null,e);
                errorDialog(e.getMessage());
            }
        }
        return false;
    }
    /**
     * 指定された本を閉じる
     * @param book クローズしたい本
     */
    public void closeBook(Book book) {
        if ( treeModelHandler.contains(book) ) {
            confirmSaveBook( book );
        }
        model.removeNodeFromParent(book.getRootNode());
        bookList.remove(book);
        updateIndexes();
    }
    /**
     * 指定されたパスの本を返す。該当する本がないときは例外を出す。
     * @exception IllegalArgumentException
     * @param path
     */
    public Book getBook( TreePath path ) {
        if ( path == null ) return null;
        DictNode bookTop = (DictNode)path.getPath()[1];
        for ( Book book : bookList ) {
            if ( book.getRootNode() == bookTop )
                return book;
        }
        throw new IllegalArgumentException("指定された本が見つからない "+path);
    }

    /**
     * ファイルチューザーを開き選択された本ファイルを読みこみツリーに挿入する。
     * 引数でツリーを指定するのは、まったく本がツリー上に存在していない状態で、
     * 本を開いたとき、ルートのフォルダが閉じたままになっている。ノブでルート
     * フォルダを開けば本が出現するが、ツリーでルートフォルダを非表示にしている
     * 場合、閉じているルートフォルダを開く手段がなく、画面に本のノードが非表示
     * になったままになり、本を開けなかったように見えてしまう。フォルダの開閉は
     * ツリーモデルからは操作できず、JTreeから行うしかないため。
     * @param tree ツリーオブジェクト
     */
    public void importBook( DictTree tree ) {
        File file = ImportBookDialog.show(owner);
        if ( file == null ) return;
        importBook( file, tree );
    }
    /**
     * 指定された辞書ファイルを開く。treeを指定するのは、辞書を開いたとき、
     * ツリーも展開するために必要。
     * @param file
     * @param tree
     */
    public void importBook( File file, DictTree tree) {
        for ( File dictFile : getIndexes() ) {
            if ( dictFile.equals(file)) {
                System.out.println("すでに開いている");
                return;
            }
        }
        //本を開くときはツリーモデルイベントを抑止。
        //イベントを拾って、ファイル保存の要不要を記録しているが、このメソッドで
        //ファイルを開く行為は、記録の対象ではないから。
        model.removeTreeModelListener(treeModelHandler);
        InputStream byteStream = null;
        try {
            Book book = loadBook(file);
            bookList.add(book);
            DictNode root = (DictNode) model.getRoot();
            model.insertNodeInto(book.getRootNode(), root, 0);
            TreePath tp = new TreePath(model.getPathToRoot(book.getRootNode()));
            tree.expandPath(tp);
            updateIndexes();
        } catch (Exception ex) {
            Logger.getLogger(Librarian.class.getName())
                    .log(Level.SEVERE,null,ex);
            errorDialog(ex.getMessage());
        } finally {
            try { byteStream.close(); } catch (Exception e ) { }
        }
        model.addTreeModelListener(treeModelHandler);
    }

    /**
     * 指定された本をエクスポート（別ファイルに書き出す)する。
     * @param path 本のノード上のパス
     */
    void exportBook( TreePath path) {
        Book book = getBook(path);
        ExportBookDialog exportDialog = ExportBookDialog.show(owner);
        File file = exportDialog.getFile();
        System.out.println("input file = " + file );
        if ( file == null ) return;
        int type = exportDialog.getFileType();
        boolean editable = exportDialog.isEditable();
        boolean need_pw = exportDialog.isNeedPassword();
        Properties p = new Properties();
        p.setProperty("editable", "" + editable );
        byte [] doc = FileUtils.getByteArray(book, p); //XMLの平文を取得
        p.setProperty( "title", (String)book.getRootNode().getUserObject() );
        String password = exportDialog.getPassword();
        if ( type == ExportBookDialog.ABF_FILE ) {
            p.setProperty("need_pw", "" + need_pw);
            try {
                if (need_pw) {
                    byte[] pw = CipherUtils.getDigest(password);
                    doc = CipherUtils.encrypt(pw, doc);
                } else if ( ! editable ) {
                    doc = CipherUtils.encrypt(DEFAULT_PASSWORD, doc);
                }
                FileUtils.saveZip(file, p, doc);
            } catch ( Exception e ) {
                Logger.getLogger(Librarian.class.getName())
                        .log(Level.SEVERE,null,e);
            }
        } else {
            try {
                FileUtils.saveByteArray(file, doc);
            } catch (Exception e ) {
                Logger.getLogger(Librarian.class.getName())
                        .log(Level.SEVERE,null,e);
            }
        }
    }

    /**
     * ファイルチューザーなどダイアログ等を表示するときのための、親Windowをセット
     * する。
     * @param window
     */
    public void setOwnerWindow( Window window ) {
        this.owner = window;
    }

    /**
     * ツリーモデルを返す。
     * @return
     */
    DefaultTreeModel getDefaultTreeModel() {
        return model;
    }

    /**
     * メッセージダイアログでエラーメッセージをユーザに通達する。
     * @param mes
     */
    private void errorDialog(String mes) {
        JOptionPane.showMessageDialog(owner, mes,
                "ファイルアクセスエラー",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * インプットダイアログを開き入力されたパスワードを返す。
     * @param mes
     * @return
     */
    private String passwordDialog( String mes ) {
        return JOptionPane.showInputDialog(owner, mes,
                "本のパスワード入力",
                JOptionPane.QUESTION_MESSAGE);
    }
    /**
     * メッセージを表示してYes/Noの入力を求め結果を返す。
     * @param mes メッセージ
     * @param title ダイアログのタイトル
     * @return YESボタンが押されたときはtrueを返す。
     */
    private boolean confermDialog( String mes, String title ) {
        int result = JOptionPane.showConfirmDialog(owner, mes, title,
                JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
    /**
     * ツリーモデルに変更があった場合に、変更がおきたパスからどの本が編集された
     * かをハッシュセットに記憶する。そしてファイル保存メソッドが呼び出されたとき
     * 変更のあった本のみを上書き保存する。
     */
    class TreeModelHandler implements TreeModelListener {
        Set<Book> bookSet = new HashSet<Book>();
        //イベントが発生したツリーパスをハッシュに保管
        void regist( TreeModelEvent e ) {

            TreePath path = e.getTreePath().pathByAddingChild(e.getChildren()[0]);
            //本の順序交換は編集による変更とはみなさない。=2
            //ページやフォルダがいじられた場合が変更。
            //みなす場合は=1
            if ( path.getPath().length <= 1 ) return;
            System.out.println("変更パス"+path);
            Book book = getBook( path );
            bookSet.add(book);
        }
        //ハッシュセットをクリア。
        void clear() {
            bookSet.clear();
        }
        //指定された本がハッシュセットに存在すればtrueを返す。
        //つまりそのときはその本を上書き保存する必要があるということ。
        boolean contains(Book book) {
            return bookSet.contains(book);
        }
        void remove(Book book) {
            bookSet.remove(book);
        }
        //とにかくなにか変更があれば、ハッシュセットに入れてしまう
        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            System.out.println("ノード変更");
            regist(e);
        }
        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            System.out.println("ノード挿入");
            regist(e);
        }
        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            System.out.println("ノード削除");
            regist(e);
        }
        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            System.out.println("ノード大規模変更");
            regist(e);
        }
    }

    /**
     * 検索を実行し該当したノードのリストを返す。
     * 該当がない場合はサイズ０のリストを返す。
     * @param req 検索リクエスト
     */
    public List<DictNode> search( DictionaryRequest req ) {
        List<DictNode> results = new ArrayList<DictNode>();
        for( Book book : bookList ) {
            Enumeration enu = book.getRootNode().depthFirstEnumeration();
            while ( enu.hasMoreElements() ) {
                DictNode node = (DictNode)enu.nextElement();
                if ( ! node.isPage() ) continue;
                if ( req.getActionCommand().equals(node.get("action","")) ) {
                    boolean found = true;
                    for ( Iterator i = node.iterator(); i.hasNext(); ) {
                        String nodeKey = (String)i.next();
                        if ( nodeKey.equals("action") || nodeKey.equals("title"))
                            continue;
                        String nodeVal = node.get(nodeKey);
                        if ( ! req.get(nodeKey,"").equals(nodeVal) ) {
                            found = false;
                            break;
                        }
                    }
                    if ( found ) {
                        results.add( node );
                    }
                }
            }
        }
        return results;
    }

    /**
     * 開いている本からキーワード検索して、該当ノードを返す。
     * 単純なベタサーチで正規表現検索やアンド検索などはまだ未対応。
     * @param keyword
     * @return 検出されたノードのリスト
     */
    public List<DictNode> search( String keyword ) {
        List<DictNode> results = new ArrayList<DictNode>();
        for( Book book : bookList ) {
            Enumeration enu = book.getRootNode().depthFirstEnumeration();
            while ( enu.hasMoreElements() ) {
                DictNode node = (DictNode)enu.nextElement();
                boolean found = true;
                if ( ! node.isPage() ) continue;
                String text = node.getTitle().concat(node.getBody());
                if ( text.indexOf(keyword) >= 0 ) {
                    results.add(node);
                }
            }
        }
        return results;
    }
}
