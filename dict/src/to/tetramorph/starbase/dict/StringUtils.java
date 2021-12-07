/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import javax.swing.tree.TreePath;

/**
 * 文字列操作ユーティリティズ。
 * @author 大澤義鷹
 */
class StringUtils {
    /**
     * 先頭と末尾から、連続した空白文字列を除去して返す。String.trim()と同じ機能
     * だが、これは全角の空白も除去する。
     */
    public static String jtrim( String s ) {
        return s.replaceAll("^(\\s|　)*", "").replaceAll("(\\s|　)*$", "");
    }

    /**
     * プレインテキストをHTMLで表示する際のエスケープ処理。
     */
    public static String escape( String text ) {
        text = text.replaceAll("&","&amp;");
        text = text.replaceAll("<","&lt;");
        text = text.replaceAll("\n","<br>");
        text = text.replaceAll(" ","&nbsp;");
        return text;
    }
    
    /**
     * ツリーパスを文字列表現にして返す。ただしルートは含めない。
     * @param path TreePathオブジェクト
     * @return "/名前/名前/..."といった文字列
     */
    public static String getPathString( TreePath path ) {
        DictNode node = TreeUtils.getDictNode(path);
        if ( node.isRoot() ) return "/";
        Object [] array = path.getPath();
        StringBuilder sb = new StringBuilder(64);
        for ( int i = 1; i < array.length; i++ ) {
            sb.append("/");
            sb.append( array[i].toString() );
        }
        if ( ! node.isLeaf() ) sb.append("/");
        return sb.toString();
    }
    /**
     * ツリー上のパス名を表す文字列として適切な名前ならtrueを返す。
     * 文字列中に"/",半角または全角スペースが混入しているならfalseを返す。
     * つまりパスといってもフォルダ名やタイトルの入力に使用するもので、
     * パス全体の検証を行うメソッドではない。
     */
    public static boolean isAppropriatePathName( String name ) {
        if ( name.matches(".*(\\s|/|　).*") ) return false;
        return true;
    }

    public static void main ( String [] args ) {
        if ( isAppropriatePathName("abcdd ")) {
            System.out.println("ok");
        } else {
            System.out.println("ng");
        }
    }
}
