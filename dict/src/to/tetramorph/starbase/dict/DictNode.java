/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * JTreeに入れるツリーノードで、これは辞書の目次構造を入れるためのクラス。
 * 本文のタイトルはこのノードのisPage()がtrueを返す場合のgetTitle()の値。
 * @author 大澤義鷹
 */
class DictNode extends DefaultMutableTreeNode {
    private Map<String,String> map = null;
    private String body; //本文
    /**
     * フォルダとしてのノードを作成する。
     * @param object
     */
    private DictNode( Object object ) {
        super( object );
        body = "";
    }

    /**
     * ページかフォルダどちらのノードかを指定してオブジェクトを作る。
     * ページの場合はget/putメソッドでパラメターを保管できる。
     * フォルダの場合はget/putメソッドはNullPointerExceptionとなる。
     * @param object
     * @param isPage
     */
    public DictNode( Object object, boolean isPage ) {
        this( object );
        if ( isPage ) map = new LinkedHashMap<String,String>();
    }
    /**
     * ディープコピーでノードの完全な複製を作成する。
     * @param node
     */
    public DictNode( DictNode node ) {
        super(node.getUserObject());
        this.body = node.body;
        if ( node.map == null ) return;
        map = new LinkedHashMap<String,String>();
        for ( Iterator i = node.map.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String value = node.map.get(key);
            map.put(key, value);
        }
    }
    /**
     * ページを表すDictNodeを作成して返す。
     */
    public static DictNode getNewPage( Object o ) {
        return new DictNode( o, true );
    }
    /**
     * フォルダーを表すDictNodeを作成して返す。
     */
    public static DictNode getNewFolder( Object o ) {
        return new DictNode( o );
    }

    /**
     * このノードがページならtrue、フォルダならfalseを返す。
     */
    public boolean isPage() {
        return map != null;
    }

    /**
     * アトリビュートの値を返す。存在しないキーを指定した場合はnullを返す。
     */
    public String get(String key) {
        return map.get(key);
    }

    /**
     * アトリビュートの値を返す。存在しないキーを指定した場合はdefを返す。
     */
    public String get(String key,String def) {
        String temp = get(key);
        if ( temp == null ) return def;
        return temp;
    }
    /**
     * アトリビュートをセットする。
     */
    public void put(String key, String value) {
        map.put( key, value );
    }
    /**
     * アトリビュートをクリアする。
     */
    public void clear() {
        map.clear();
    }
    /**
     * このノードがもつアトリビュートのキーの列挙を返す。
     */
    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }

    public String getPathString() {
        if ( isRoot() ) return "/";
        Object [] array = getPath();
        StringBuilder sb = new StringBuilder(64);
        for ( int i = 1; i < array.length; i++ ) {
            sb.append("/");
            sb.append( array[i].toString() );
        }
        if ( ! isPage() ) sb.append("/");
        return sb.toString();
    }

    /**
     * このノードのタイトルを返す。getUserObject()と等価だがStringへのキャスト
     * が不要。
     */
    public String getTitle() {
        return (String)getUserObject();
    }

    /**
     * ルートノードからこのノードまでのツリーパスを返す。
     */
    public TreePath getTreePath() {
        return new TreePath(getPath());
    }

    /**
     * このノードに本文をセットする。
     * @exception IllegalStateException isPageがfalseのときにメソッドを呼び
     * 出した場合。フォルダに本文はセットできない。
     * @exception IllegalArguementException nullをセットした場合。
     */
    public void setBody( String body ) {
        if ( ! isPage() ) { throw new IllegalStateException(
        "ページではないノードには本文をセットできません");
        }
        if ( body == null ) { throw new IllegalArgumentException(
        "nullはセットできません");
        }
        this.body = body;
    }

    /**
     * このノードの本文を返す。初期値は長さゼロの空文字列。
     * @exception IllegalStateException isPageがfalseのときにメソッドを呼び出し
     * た場合。フォルダは本文を持たない。
     */
    public String getBody() {
        if ( ! isPage() ) { throw new IllegalStateException(
        "ページではないノードに本文は存在しません");
        }
        return body;
    }
}
