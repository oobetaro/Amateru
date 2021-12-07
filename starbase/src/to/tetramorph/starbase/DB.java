/*
 *
 */
package to.tetramorph.starbase;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
/**
 * Databaseに別名を与えるために用意されたインターフェイス。
 * DatabaseはHSQLDBと接続して、メソッドごとに決められたデータ操作を行うが、
 * 他のPostgreSQLやMySQL等を使うものを後から作る事もあるかもれしない。
 * それぞれのクラスにはこのインターフェイスをimplementして、インターフェイス名
 * でアクセスする。要はカプセル化のために用意したもの。
 */
interface DB {
    /**
     * データベースとの接続を返す。
     */
    public Connection getConnection();
    
    /**
     * 引数でまとめてStatementやResultSetをクローズする。
     * いちいちtry,catchで囲まなくてもクローズできる。
     *   <pre>例 close(rs,stmt);</pre>
     */
    public void close(Object ...o);
    
    /**
     * 指定されたグループ名のプロパティをDBから取り出し、Propertiesオブジェクトで
     * 返す。グループ名が存在しなくても空のPropertiesオブジェクトが戻るので、
     * このメソッドがnullを返すことはない。Properties#size()等で、空かどうかの
     * 識別を行うことができる。
     */
    public Properties getProperties(String name);
    /**
     * Propertiesオブジェクトを指定された名前でDBに登録する。登録前に以前のデータ
     * は消去され新しいデータに置き換わる。
     */
    public void setProperties(String name,Properties prop);
    
    /**
     * 指定されたフォルダにNatalオブジェクトを登録する。
     * @param occ 登録するNatalオブジェクト。
     * @param treePath 登録するフォルダのパス
     * @return 挿入したときのID番号。1かそれ以上の値が返る。0のときはなんらかの
     * エラーがあったことを意味する。
     */
    public int insertNatal( Natal occ, TreePath treePath );
    
    /**
     * 複数ﾊﾟｽをもつNatalの登録。これは主にインポート用で、その場合パスが複数
     * 登録されている場合がある。複数のときはパスのうち最初の一つを
     * insertNatal(Natal,TreePath)を呼び出して登録し、のこりのパスはTREEPATH表に
     * 追加で登録する。挿入に成功した場合はその際にDBから付与されたIDを返す。
     * 0が戻ったときはなんらかのエラーが発生した。
     */
    public int insertNatal( Natal natal, String csvPath );
    
    /**
     * フォルダのツリーを返す。
     * @return ノードで編まれたツリー
     */
    public FolderTreeNode getTree();
    
    /**
     * DBインスタンスが保持しているTREEPATH表のツリー構造を表現したFolderTreeNode
     * を再作成して返す。これはインポートが行われたときFolderTreeNodeを作り直す
     * 必要があり、そのときに使用される。
     */
    public FolderTreeNode getUpdateTree();
    
    /**
     * 指定されたNatalの値にDB上のNatalの値を更新する。与えられたNatalのIDと
     * 同じIDをもつDB上のレコードを更新する。
     * @param natal 内容を変更されたNatal
     */
    public void updateNatal( Natal natal );
    
    /**
     * フォルダからNatalデータを削除する。
     * SearchResultTableの中の行が選択され削除が実行されたとき呼び出される。
     * データベース内のデータ削除をこのメソッド内で実行し、削除に成功すればtrue
     * を返す。するとDataListTable内のリストも消去される。削除に失敗したときは
     * falseを返すと消去されない。つまり戻り値のtrue/falseは、JTableの行を削除す
     * るかしないかの差でしかない。
     * @param ite     削除するNatalの列挙を返すIterator
     * @param current 削除するNatalが入っているフォルダのパス
     * @return        削除に成功すればtrueを、失敗すればfalseを返す。
     */
    public boolean remove( Iterator ite, TreePath current );

    /**
     * フォルダ内のNatalデータを移動する。
     * SearchResultTableの中の行が選択され移動が実行されたとき呼び出される。
     * データベース内のデータ移動をこのメソッド内で実行し、移動に成功すればtrue
     * を返す。するとSearchResultTable内のリストも消去される。移動に失敗した
     * ときはfalseを返し消去されない。戻り値のtrue/falseは、JTableの行を削除す
     * るかしないかの差。
     * @param ite     移動するNatalの列挙を返すIterator
     * @param current 移動元フォルダへのパス
     * @param target  移動先フォルダへのパス
     * @return        移動に成功すればtrueを失敗はfalseを返す。
     */
    public boolean move( Iterator ite, TreePath current, TreePath target );
    
    /**
     * Natalデータを指定フォルダにコピーする。
     * ただしこのコピーはTREEPATH表に追加のパスを登録するだけで、データを複製
     * するわけではない。逆にいうと、複数のフォルダに同じ名前の人が登録されていて、
     * そのうちの一つを編集したら、他のフォルダのデータも変化する。
     * コピーに成功すればtrue、失敗のときはfalseを返す。
     * @param ite    コピーすべきNatalを列挙するIterator。
     * @param target コピー先フォルダへのパス
     * @return       成功したときはtrue、失敗はfalseを返す。
     */
    public boolean copy( Iterator ite, TreePath target );
    
    /**
     * フォルダをリネームする。
     * @param source リネーム前のフォルダパス
     * @param target リネーム後のフォルダパス
     * @return 成功したらtrue、失敗したらfalseを返す。
     */
    public boolean renameFolder( TreePath source, TreePath target);
    
    /**
     * フォルダをツリー上の別の位置に移動する。
     * @param source 移動元フォルダ
     * @param target 移動先フォルダ
     * @return 移動に成功したらtrueを失敗したらfalseを返す。
     */
    public boolean moveFolder( TreePath source, TreePath target );
    
    /**
     * フォルダを削除する。
     * @param source DBから削除すべきフォルダのパス。
     * @return 削除に成功したときはtrueを、失敗はfalseを返す。
     */
    public boolean removeFolder( TreePath source );
    
    /**
     * フォルダを再帰的にコピーする。
     * targetのフォルダの中に、sourceのフォルダを再帰的にコピーする。
     * ソースが"/aa/bb/cc"でターゲットが/aa/ee/ff"なら、/aa/ee/ffの中にccが
     * コピーされ、/aa/ee/ff/ccとなる。
     * @return コピーに成功したときはtrueを、失敗ならfalseを返す。
     * @param source コピー元フォルダのパス
     * @param target コピー先フォルダのパス
     */
    public boolean copyFolder( TreePath source, TreePath target );
    
    /**
     * フォルダを作成する。
     * @param newPath DB上に作成すべきフォルダのパス
     * @return 作成に成功したときはtrueを失敗はfalseを返す。
     */
    public boolean makeFolder( TreePath newPath );
    
    /**
     * ごみ箱を削除する。
     * @param dustBoxPath ごみ箱のパス
     * @return 削除に成功したときはtrueを失敗したらfalseを返す。
     */
    public boolean clearDustBox( TreePath dustBoxPath );
    
    /**
     * 指定されたフォルダのNatalデータのリストを取得する。
     * @param treePath      パス
     * @param list          データが格納されるリスト。事前に消去される。
     * @param columnName    ソートするときの列名
     * @param sortDirection ソートの向き。Database.の定数を参照。
     *                       これがNOT_SORTEDならば、columnNameは無視される。
     */
    public void getList( TreePath treePath,
                           List<Natal> list,
                           String columnName,
                           int sortDirection );
    /**
     * 指定されたidをもつNatalオブジェクトを返す。
     */
    public Natal getNatal( int id );
    
    /**
     * 指定されたIDとパスに該当するNatalデータを返す。パスが一致しないとIDが存在
     * してもnullが返る。
     */
    public Natal getNatal( int id, String path );
    
    /**
     * 指定されたフォルダの中にあるNatalデータのIDリストを返す。
     */
    public List<Integer> getIDinFolder( TreePath treePath );
    
    /**
     * *_PROPERTIES表の設定名のリストを返す。
     * @param type   フルパッケージ名つきのクラス名
     * @param vector 結果を書き出すVector。JListに入れて使うことが前提。
     * @param table 表名
     */
    public Vector<String> getConfigNames( String type,
                                           Vector<String> vector,
                                           String table );
    /**
     * *_PROPERTIES表から設定情報を取り出す。
     * @param name 設定名
     * @param type フルパッケージ名つきのクラス名
     * @param prop 設定情報を書き込むProperties。nullを指定すると新たに
     * インスタンスを作成してそこに書き込みそれを返す。
     * nullでない場合はそのPropertiesに書き込み、その参照を返す。
     * @return 正常にロードできたときはtrue。
     *         設定名がDBに見つからなかったときはfalse。
     */
    public boolean getConfigProperties( String name,
                                           String type,
                                           Properties prop,
                                           String table );
    /**
     * *_PROPERTIES表に設定情報を書き出す。書き出す前に無条件にname,typeに
     * 一致するレコードの削除を試みてその後でINSERTする。つまり同名のデータは
     * 上書き保存されたことになり、同名データが重複登録されることはない。
     * @param name 設定名
     * @param type フルパッケージ名つきのクラス名
     * @param prop 色設定情報の入ったProperties。
     */
    public void setConfigProperties( String name,
                                       String type,
                                       Properties prop,
                                       String table );
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
                                          String table );
    /**
     * nameVectorで指定された順番に色設定の順序を並べ替える。
     * @param type       フルパッケージ名つきのクラス名
     * @param nameVector 名前のリストが格納されたVector
     * @param table      表名
     */
    public void sortConfigProperties( Vector<String> nameVector,
                                        String type,
                                        String table );
    /**
     * 指定された設定情報を削除する。
     * @param name 色設定名
     * @param type フルパッケージ名つきのクラス名
     * @param table 表名
     */
    public void removeConfigProperties( String name, 
                                          String type, 
                                         String table);

    /**
     * 所定IDのヒストリーを更新する。内部的には一旦DBから削除して再登録する。
     * ChartInternalFrameから一度だけ呼ばれている。
     * @param id Occason.getID()で得られるユニークID
     * @param historyList Eventを格納したList。
     */
    public void updateHistory( int id,List<Transit> historyList );

    /**
     * OCCASION表の職業フィールドを検索して職業リストを返す。
     * 指定キーで中間一致検索してDISTINCT抽出する。
     * @param key 検索する職業名。SQLで%職業名%として中間一致検索する。
     */
    public Vector<String> searchJobs( String key );

//////////////////////////////////////////////////////////////////////////////
//             ここよりヒストリーやコンポジットデータの編集メソッド         //
//////////////////////////////////////////////////////////////////////////////
    
    /**
     * イベント情報(ヒストリーやコンポジット情報でTransitオブジェクトで表現され
     * るデータ)を格納するメモリテーブルを作成する。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。DB内にはHISTORYまたはCOMPOSIT
     * のメモリテーブルが作成させる。すでに作成されている場合は削除され再作成され
     * る。
     */
    public void createEventTable( int tableCode );
    
    /**
     * tableCodeで指定されたテーブル(HISTROY|COMPOSIT)表に保管されているデータを
     * リストにして返す。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。
     */
    public List<Transit> getEventList( int tableCode );
    
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
                                List<Transit> eventList );
    
    /**
     * tableCodeで指定されたテーブルに、Transitオブジェクトの内容を登録する。
     * @param tableCode DBFactoryクラスで定義されている、HISTORY_TABLE_CODEまた
     * は、COMPOSIT_TABLE_CODEのどちらかを指定する。
     */
    public void addEvent( int tableCode, Transit event );
    
    /**
     * tableCodeで指定された表内のeventに該当するレコードを削除する。
     * @param tableCode    DBFactoryクラスで定義されている、HISTORY_TABLE_CODE
     *                     または、COMPOSIT_TABLE_CODEのどちらかを指定する。
     * @param event        削除するデータ。内部的にはevent.getPrimaryKey()のIDを
     *                     もつ行を削除する。
     */
    public void removeEvent( int tableCode, Transit event);
    
    /**
     * tableCodeで指定された表のevent.getPrimaryKey()のIDを持つ行を、eventの
     * データに刷新する。
     */
    public void updateEvent( int tableCode, Transit event );
}
