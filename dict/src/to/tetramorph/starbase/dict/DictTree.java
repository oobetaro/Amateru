/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import to.tetramorph.util.IconLoader;

/**
 * DnDによるノードの編集をサポートしたツリー。
 * ノードの挿入・削除等のメソッド内部では、基本的にはエラー検査はせずに簡単に
 * すませる。メニューを開くときに、その状況で使えない機能を判定して選択できない
 * ようにして、例外の発生を抑える方針。
 * @author 大澤義鷹
 */
public class DictTree extends JTree {
    private DictTreeCellEditor treeCellEditor;
    //private Book db = DictionaryFactory.getInstance();
    private TreeShell treeShell = new TreeShell();
    private DefaultTreeModel model;
    private static DataFlavor localObjectFlavor;
    static {
        localObjectFlavor =
                new DataFlavor(List.class, null);
    }
    private static DataFlavor[] supportedFlavors = { localObjectFlavor };
    private Librarian lib = Librarian.getInstance();
    private List<TreePath> clipTreePathList;
    TreeTransferHandler transferHandler;

    /**
     * このツリーがサポートするDnD用のフレーバーを返す。
     */
    public DataFlavor getDataFlavor() {
        return localObjectFlavor;
    }

    /**
     * このツリーがサポートするDnD用のフレーバーを配列で返す。getDataFalvor()の
     * 値を配列に入れただけのもの。
     */
    public DataFlavor[] getSupportedFlavors() {
        return supportedFlavors;
    }

    /**
     * 辞書ツリーオブジェクトを作成する。
     */
    public DictTree() {
        super();
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
        TreeCellRenderer renderer = new TreeCellRenderer();
        setCellRenderer( renderer );
        treeCellEditor = new DictTreeCellEditor( this, renderer );
        setCellEditor( treeCellEditor );
        setEditable( true );
        setToggleClickCount(2);
        transferHandler = new TreeTransferHandler();
        this.setTransferHandler( transferHandler );
        setDragEnabled( true );
        setRootVisible( false );
    }
    /**
     * ツリーモデルをセットする。
     */
    public void setDefaultTreeModel( DefaultTreeModel treeModel ) {
        super.setModel(treeModel);
        treeModel.addTreeModelListener( treeCellEditor );
        this.model = treeModel;
    }
    /**
     * 選択されているパスをリストで返す。一つも選択されていないときは空のリスト
     * を返す。
     */
    public List<TreePath> getSelectionPathList() {
        return TreeUtils.arrayToList( getSelectionPaths() );
    }
    /**
     * ポイントpが矩形領域rectの中の下側付近に位置するときはtrueを返す。
     * 下側と判定するのは下側からrectの高さの1/4の範囲なら下側付近と判定する。
     */
    private boolean isAperture( Rectangle rect, Point p ) {
        int h = rect.height / 6;
        Rectangle r = new Rectangle( rect.x, rect.y + h*4, rect.width, h*2 );
        return r.contains(p);
    }

    /**
     * ノードがリネームされるなど編集開始前に呼び出される。編集が禁止されている
     * 本の場合はfalseを返しリネームを禁止する。
     */
    @Override
    public boolean isPathEditable(TreePath path ) {
        System.out.println("編集されようとしているパス " + path);
        Book book = lib.getBook(path);
        return book.isEditable();
    }
    /**
     * JTreeのデフォルトは葉はすべて書類、葉以外はフォルダとみなして各セルを表示
     * するが、これはDictNodeがページかどうかに応じて書類とフォルダのアイコンを
     * 表示するようにしたセルレンダラ。またドラッグ中のセル上における
     * マウスカーソルの位置から、セルとセルのすき間に挿入しようとしているのか、
     * フォルダや書類の位置に挿入しているのかを判定し、すき間に挿入しようとして
     * いる場合は、セルの底辺に赤線を表示してそれをユーザに示す機能をもつ。
     */
    private class TreeCellRenderer extends DefaultTreeCellRenderer {
        boolean isTarget; //DnDの際ノードを枠線で囲むが、そのためのフラグ
        int row = 0;
        Icon book_open = IconLoader.getImageIcon("/resources/book_open.png");
        Icon book_closed = IconLoader.getImageIcon("/resources/book_close.png");
        public TreeCellRenderer() {
        }
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean isSelected, boolean isExpanded,
            boolean isLeaf, int row, boolean hasFocus )
        {
            super.getTreeCellRendererComponent(
                    tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
            //枠線の表示／非表示を決定。ドロップ先ノードの時は表示。
            if ( value instanceof DictNode ) {
                DictNode node = (DictNode)value;
                if ( ! node.isPage() ) {
                    DictNode parent = (DictNode)node.getParent();
                    if ( parent != null && parent.isRoot() ) {
                        setIcon(isExpanded ? book_open : book_closed);
                    } else {
                        //親クラスにあるアイコンをセット
                        setIcon( isExpanded ? openIcon : closedIcon );
                    }
                }
                //rect = getRowBounds(row); //これは再帰呼出になってしまう
                this.row = row;            //ので、行番号だけ保存しておく
            }

            if ( transferHandler != null && transferHandler.isDragging() ) {
                isTarget = transferHandler.getDropTargetObject() == value;
            } else isTarget = false;
            return this;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if ( isTarget ) {
                Color temp = g.getColor();
                g.setColor(Color.RED);
                if ( isAperture(getRowBounds(row),transferHandler.getDropPoint()) ) {
                    //矩形領域の下側にカーソルがあるときはすき間に挿入のサインを表示
                    int h = getSize().height - 1;
                    int w = getSize().width - 1;
                    g.drawLine(0,h,w,h);
                    g.drawLine(0,h-1,w,h-1);
                }
//                else {
//                    //そうでないときはそのフォルダに挿入のサインを表示
//                    g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
//                }
                g.setColor( temp );
            }
        }

    }

    /**
     * DnDによるツリーノードの移動処理を行う
     */
    class TreeTransferHandler extends TransferHandler {
        boolean dragging = false;
        Point mousePoint = new Point();
        Object targetObject;
        /**
         * createTransferable()の後、ドラッグ中のマウスカーソルの移動のたびに
         * 呼び出される。info.getDropLoaction()で移動中のマウス座標を取得できる。
         * @param info
         * @return
         */
        @Override
        public boolean canImport(TransferHandler.TransferSupport info ) {
            mousePoint = info.getDropLocation().getDropPoint();
            JTree tree = (JTree)info.getComponent();
            TreePath path = tree.getSelectionPath();
            targetObject = ( path == null ) ? null : path.getLastPathComponent();
            return info.isDataFlavorSupported(localObjectFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            System.out.println("ドラッグ開始");
            dragging = true;
            DictTree tree = (DictTree)c;
            //TreePath [] paths = tree.getSelectionPaths();
            //List<TreePath> pathList = TreeUtils.arrayToList(paths);
            List<TreePath> pathList = getSelectionPathList();
            return new TransferableNodes( pathList, tree );
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            System.out.println("ドロップ");
            if ( ! info.isDrop() ) return false;
            dragging = false;
            DictTree tree = (DictTree)info.getComponent();
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            DictTree.DropLocation dl = (DictTree.DropLocation)info.getDropLocation();
            TreePath targetPath = dl.getPath();
            if ( targetPath == null ) return false;
            DictNode targetNode = (DictNode)targetPath.getLastPathComponent();
            Rectangle rect = getRowBounds( tree.getRowForPath(targetPath) );
            boolean aperture = isAperture( rect, getDropPoint());
            Book targetBook = lib.getBook(targetPath);
            //ドラッグされたパスを取得
            Transferable t = info.getTransferable();
            List<TreePath> pathList = null;
            try {
                //無検査キャストの警告が出るが、@SuppressWarningsで黙らせている
                pathList = (List<TreePath>)t.getTransferData(localObjectFlavor);
            } catch ( Exception e) {
                e.printStackTrace();
                return false;
            }
            //編集禁止本の中にページをドロップすることを禁止
            //ただし本を禁止本のなかにすき間挿入するのはトラップしない。
            if ( ! targetBook.isEditable() &&  ! isBookNode(pathList) ) {
                error("ページまたはフォルダの移動",
                      "ドロップ先の本は" +
                      "編集が禁止されているため移動できません");
                return false;
            }
            //編集禁止本のページを他の本の中にドロップすることを禁止
            //ただし本の順序交換の場合はトラップしない。
            if ( ! isEditable(pathList) && ! isBookNode(pathList) ) {
                error("ページまたはフォルダの移動",
                      "移動しようとしたページは編集が禁止されています");
                return false;
            }
            TreeUtils.removeDescendantPath( pathList );
            if ( isBookNode(pathList) ) {
                if ( ! booksCanBeMoved( pathList,targetPath) ) {
                    error("本の移動",
                          "本を他の本の中に移動することはできません");
                    return false;
                }
                moveNode( pathList, targetNode, true );
                lib.updateIndexes();
            } else {
                //ブックノードがある位置にすき間挿入することを禁止
                if ( targetPath.getPath().length == 2 && aperture ) {
                    error("ページまたはフォルダの移動",
                          "ページまたはフォルダを本の階層に移動することはできません");
                    return false;
                }
                if ( TreeUtils.isRootNodeContains(pathList)) {
                    error( "ページの移動", "ルートは移動できません");
                    return false;
                }
                if ( TreeUtils.isDescendantNodeContain(pathList,targetNode)) {
                    error( "ページの移動",
                           "親ページを子ページに移動することはできません");
                    return false;
                }
                moveNode( pathList, targetNode, aperture );
            }
            return true;
        }
        /**
         * 与えられたパスのリストから本を求め、編集禁止本が含まれるときはfalse。
         * すべてが編集許可の場合はtrueを返す。
         * @param pathList
         * @return
         */
        boolean isEditable(List<TreePath> pathList ) {
            for ( TreePath path : pathList ) {
                if (! lib.getBook(path).isEditable() ) return false;
            }
            return true;
        }
        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            System.out.println("転送完了");
            DictTree tree = (DictTree)c;
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            if ( action == TransferHandler.MOVE ) {

            }
            dragging = false;
        }
        //ドラッグ中はtrueを返す。
        boolean isDragging() {
            return dragging;
        }
        //ドラッグ中のマウス座標を返す。
        Point getDropPoint() {
            return mousePoint;
        }
        Object getDropTargetObject() {
            return targetObject;
        }

        //本を移動できる条件がそろっている場合はtrueを返す。
        boolean booksCanBeMoved(List<TreePath> srcList, TreePath target ) {
            for( TreePath path : srcList ) {
                if ( path.getPathCount() != 2 ) return false;
                if ( path == target ) return false;
            }
            if ( target.getPathCount() > 2 ) return false;
            return true;
        }
        /**
         * リスト中のパスがブックノードのみの場合はtrueを返す。
         * 一つでもページやフォルダノードが混入している場合はfalseを返す。
         */
        boolean isBookNode( List<TreePath> srcList ) {
            for( TreePath path : srcList ) {
                if ( path.getPathCount() != 2 ) return false;
            }
            return true;
        }

    }

    /**
     * ノードを移動する。
     * @param paths 移動するツリーパスのリスト
     * @param targetNode 異動先ノード
     * @param aperture 異動先ノードと同じ親の子として移動する場合はtrue、
     * 移動先ノードの子として移動する場合はfalseを指定する。
     */
    public void moveNode( List<TreePath> paths,
                     DictNode targetNode,
                     boolean aperture ) {
        DictNode parentNode = (DictNode) targetNode.getParent();
        for ( int i=0; i<paths.size(); i++ ) {
            TreePath path = paths.get(i);
            expandPath( path );
            DictNode srcNode = TreeUtils.getDictNode(path);
            ShellCommand cmd = new DeleteCommand( this,srcNode);
            treeShell.execute(cmd);
            int row = model.getIndexOfChild(parentNode, targetNode);
            if ( aperture ) { //すき間へ挿入
                if ( parentNode == null ) { //ルートの下に入れる場合
                    cmd = new InsertCommand( this, srcNode, targetNode,0);
                    treeShell.execute(cmd);
                } else { //サブフォルダのrow番目に挿入
                    if ( (row+1) <= parentNode.getChildCount() ) row++;
                    cmd = new InsertCommand( this, srcNode, parentNode, row );
                    treeShell.execute(cmd);
                }
            } else { //フォルダや葉の位置に挿入
                if ( targetNode.isPage() ) {
                    cmd = new InsertCommand( this, srcNode, parentNode, row);
                    treeShell.execute(cmd);
                } else {
                    cmd = new InsertCommand( this, srcNode, targetNode, 0);
                    treeShell.execute(cmd);
                }
            }
        }
        treeShell.end();
    }
    /**
     * 既存のノードを新しいノードと置き換える。これはノードの文書やアトリビュート
     * が書き換えられたときに、その情報を更新するのに使用する。
     * pathの最後のノードが、newNodeに置き換わる。
     * @param path 編集対象となっているパス
     * @param newNode 編集後、新しく作成されたノード
     */
    public void updateNode( TreePath path, DictNode newNode ) {
        DictNode srcNode = TreeUtils.getDictNode(path);
        DictNode parentNode = (DictNode) srcNode.getParent();
        int row = model.getIndexOfChild(parentNode, srcNode);
        //既存ノードを削除し
        ShellCommand cmd = new DeleteCommand( this,srcNode);
        treeShell.execute(cmd);
        //新しいノードを同じ位置に挿入
        cmd = new InsertCommand( this, newNode, parentNode, row );
        treeShell.execute(cmd);
        treeShell.end();
    }
    /**
     * ツリーノードのリネーム時の処理を行う。
     * DnDのときノードに枠線を表示したりするためのセルエディタでもある。
     */
    private class DictTreeCellEditor extends DefaultTreeCellEditor
                                          implements TreeModelListener {

        String preNodeName = "";
        //編集前のノード名を保管
        Object[] prePath = null;

        DictTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }

        /**
         * トリプルクリックやF2キーで編集が始まる前に呼び出される。valueの値を知る
         * のにつかっているだけで、親のComponentを取得して返しているだけ。
         * つまりvalueをこちらで知る必要がなければ、オーバーライドは不要。
         * @param tree 編集を要求している JTree
         * @param value 編集するセル
         * @param isSelected 選択部がハイライトされている場合はtrue
         * @param expanded ノードが展開されている場合は true
         * @param leaf ノードが葉の場合は true
         * @param row 編集中のノードの行インデックス
         * @return
         */
        @Override
        public Component getTreeCellEditorComponent(
                JTree tree, Object value, boolean isSelected, boolean expanded,
                boolean leaf, int row)
        {
            //リネームする前の名前を保存しておく
            preNodeName = value.toString();
            prePath = ((DictNode) value).getUserObjectPath();
            cansel = false; //編集が開始されたので、リスナメソッドの入口を開ける。
            return super.getTreeCellEditorComponent(
                    tree, value, isSelected, expanded, leaf, row);
        }

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            System.out.println("valueChanged() : " + e.getPath().toString());
            if (e.getPath().getLastPathComponent() == getModel().getRoot()) {
                System.out.println("ルートノードです");
                tree.expandPath(e.getPath());
            }
            tree.setEditable(true);
            //dropTargetNode = (DictNode)e.getPath().getLastPathComponent();
        }

        boolean cansel = true;

        //TreeModelListenerの実装
        // ノードのリネームが行われたら呼び出される
        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            if ( cansel ) return;
            System.out.println("treeNodeChanged() : ");
            Object[] nodes = e.getChildren();
            //編集が終わったノードを取得
            DictNode editedNode = (DictNode) nodes[0];
            if ( editedNode.toString().isEmpty()) {
                //空の文字列が入力された場合
                error("ページのリネーム","名前がありません");
                //↓この処理でもう一度このtreeNodesChanged(..)が呼ばれる。
                //「変更」したから。
                ((DefaultTreeModel)getModel()).valueForPathChanged(
                        editedNode.getTreePath(), preNodeName );
                return;
            }
            if ( prePath == null ) return;
            cansel = true;
            // コマンドを実行するとツリーモデルが変更され、変更されると、モデルの
            //リスナ（つまりこのメソッド）が呼び出され、このメソッドはまたコマンド
            //を実行し、と無限ループに落ちてしまうため、canselスイッチを用意して、
            //編集開始のときにスイッチをOFFにし、処理を中に入れ、すぐにスイッチは
            //ON(つまりキャンセル）にする。なんか不細工なやり方で他に良いやり方
            //があるような気もするが、今はこのまま。
            RenameCommand cmd = 
                    new RenameCommand( DictTree.this, editedNode, preNodeName );
            treeShell.execute(cmd);
            treeShell.end();
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {}
        @Override
        public void treeNodesRemoved(TreeModelEvent e) {}
        @Override
        public void treeStructureChanged(TreeModelEvent e) {}
    }
    
    public TreeShell getTreeShell() {
        return treeShell;
    }

    /**
     * 選択されているノードをすべて削除する。
     * @exception IllegalArgumentException 選択された中にルートノードがある場合
     * 、選択ノードがまったく無い場合
     */
    public void removeSelectedNodes() {
        List<TreePath> list = getSelectionPathList();
        if ( list.isEmpty() )
            throw new IllegalArgumentException("パスが選択されていません");
        TreeUtils.removeDescendantPath(list);
        if ( TreeUtils.isRootNodeContains(list) )
            throw new IllegalArgumentException("ルートは削除できません");
        for ( TreePath path : list ) {
            DictNode node = TreeUtils.getDictNode(path);
            DeleteCommand cmd = new DeleteCommand(this, node);
            treeShell.execute(cmd);
        }
        treeShell.end();
    }

    /**
     * 選択されているパスを内部リストにコピーする。
     * クリップボードを経由してのコピペ風に処理するためのもの。
     */
    public void copyNode(List<TreePath> list, DictNode targetNode ) {
        TreeUtils.removeDescendantPath(list);
        for ( int i=0; i < list.size(); i++ ) {
            DictNode node = TreeUtils.getDictNode( list.get(i) );
            if ( node.isPage() ) {
                DictNode newNode = new DictNode( node );
                insertNode( newNode, targetNode );
            } else {
                DictNode newNode = TreeUtils.copyNode(node);
                insertNode( newNode, targetNode );
            }
        }
    }

    /**
     * newNodeをtargetNodeに挿入する。newNodeはページであってもフォルダであっても
     * かまわない。targetNode#isPage()のときは、そのページの位置にnewNodeが挿入
     * され、targetNodeは一つ下に移動する。
     * ! targetNode#isPage()のとき、それはフォルダなので、そのフォルダの子として
     * 0番目の位置に挿入する。
     * @param newNode
     * @param targetNode
     */
    void insertNode( DictNode newNode, DictNode targetNode) {
        ShellCommand cmd = null;
        if ( targetNode.isPage() ) {
            DictNode parentNode = (DictNode)targetNode.getParent();
            int index = parentNode.getIndex(targetNode);
            cmd = new InsertCommand( this, newNode, parentNode, index );
        } else {
            cmd = new InsertCommand( this, newNode, targetNode, 0);
        }
        treeShell.execute(cmd);
        treeShell.end();
        //this.startEditingAtPath( newNode.getTreePath() );
    }

    /**
     * 選択されているノードに、新規ページを追加する。
     * @exception IllegalArgumentException ノードが選択されていないとき。
     */
    public void makePage() {
        TreePath targetPath = getSelectionPath();
        if ( targetPath == null )
            throw new IllegalArgumentException("パスが選択されていません");
        DictNode newNode = DictNode.getNewPage("新規ページ");
        DictNode targetNode = TreeUtils.getDictNode(targetPath);
        insertNode( newNode, targetNode);
        this.startEditingAtPath( newNode.getTreePath() );
    }

    /**
     * 選択されているノードに新規フォルダを追加する。
     */
    public void makeFolder() {
        TreePath targetPath = getSelectionPath();
        if ( targetPath == null )
            throw new IllegalArgumentException("パスが選択されていません");
        DictNode targetNode = TreeUtils.getDictNode(targetPath);
        DictNode newNode = DictNode.getNewFolder( "新規フォルダ" );
        insertNode( newNode, targetNode );
        this.startEditingAtPath( newNode.getTreePath() );
    }

    /**
     * JOptionPaneでエラーメッセージを表示する。
     */
    void error( String title,String message ) {
        Window w = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(
                w,
                message,
                title,
                JOptionPane.ERROR_MESSAGE );
    }

}
