/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreePath;

/**
 * 主に複数のTreePathを扱うためのスタティックメソッド群。
 * @author 大澤義鷹
 */
class TreeUtils {
    private TreeUtils() {
    }
    /**
     * パスのリストの中からフォルダを一つ取り出し、リストの残りの葉と
     * フォルダを調べ先に取り出したフォルダの子である場合は子を削除する。
     * 複数ノードの移動処理の前に、このメソッドで不要なパスを除外する。
     * 移動ならこれで良いが、コピーの場合はまた話しが違う。
     * @param paths
     */
    public static void removeDescendantPath( List<TreePath> paths ) {
        for( int j=0; j<paths.size(); j++ ) {
            DictNode  node = getDictNode(paths.get(j));
            if ( node.isPage() ) continue;
            for ( int i=0; i<paths.size(); i++) {
                DictNode  n = getDictNode(paths.get(i));
                if ( n == node ) continue;
                if ( node.isNodeDescendant(n) ) {
                    paths.remove(i);
                }
            }
        }
    }
    /**
     * TreePathの配列をリストにして返す。Arrays.asList()で作成したリストは削除が
     * できないので、このメソッドがある。
     */
    public static List<TreePath> arrayToList( TreePath [] array ) {
        List<TreePath> list = new ArrayList<TreePath>();
        if ( array == null ) return list;
        for ( TreePath path : array )
            list.add(path);
        return list;
    }

    /**
     * リストの中にルートを指すパスが含まれる場合はtrueを返す。
     * 辞書機能はJTreeのルートノード表示をオフにすることが前提となっている。
     * だからこのメソッドを呼び出して判定する機会は無くてもいいのだが、開発途中
     * での都合により、このメソッドを呼び出しているところがある。
     */
    public static boolean isRootNodeContains( List<TreePath> paths ) {
        for ( TreePath p : paths ) {
            if( getDictNode(p).isRoot() ) return true;
        }
        return false;
    }
    /**
     * TreePath内のDictNodeの配列の最後のノードを返す。
     * (DictNode)path.getLastPathComponent()と等価。
     */
    public static DictNode getDictNode( TreePath path ) {
        return (DictNode)path.getLastPathComponent();
    }
    /**
     * DictNodeからTreePathを作成して返す。
     */
    public static TreePath getTreePath( DictNode node ) {
        return new TreePath(node.getPath());
    }
    /**
     * 親を子のノードに移動する、自分自身に移動するようなパターンがpathsに含まれ
     * ているときはtrueを返す。
     * @param paths
     * @param targetNode
     */
    public static boolean isDescendantNodeContain( List<TreePath> paths,
                                                          DictNode targetNode) {
        for ( TreePath path : paths ) {
            DictNode node = getDictNode(path);
            if ( node.isNodeDescendant(targetNode)) return true;
            if ( node == targetNode ) return true;
        }
        return false;
    }
    /**
     * 本のフォルダのパスがリスト中に含まれている場合はtrueを返す。
     */
    public static boolean isBookFolderContains( List<TreePath> list ) {
        for ( TreePath path : list ) {
            if ( path.getPathCount() == 2 ) return true;
        }
        return false;
    }
    /**
     * DictNodeを再帰コピーする
     * @param srcNode コピーするノード
     * @param distNode コピー先ノード
     */
    private static void duplicate( DictNode srcNode, DictNode distNode ) {
        for( Enumeration e = srcNode.children(); e.hasMoreElements(); ) {
            DictNode node = (DictNode)e.nextElement();
            if ( node.isPage() ) {
                distNode.add( new DictNode(node) );
            } else {
                DictNode newFolder = new DictNode(node);
                distNode.add( newFolder );
                duplicate( node, newFolder ); //再帰
            }
        }
    }
    /**
     * 指定されたDictNodeを再帰コピーして返す。
     */
    public static DictNode copyNode( DictNode node ) {
        DictNode distNode = new DictNode(node);
        duplicate( node, distNode );
        return distNode;
    }
}
