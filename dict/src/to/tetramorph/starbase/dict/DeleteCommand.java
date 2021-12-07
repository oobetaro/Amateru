/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import javax.swing.tree.DefaultTreeModel;


/**
 * ツリーノードの「削除」命令。
 * @author 大澤義鷹
 */
class DeleteCommand implements ShellCommand {
    DefaultTreeModel model;
    DictNode removeNode;
    DictNode parentNode;
    int index;
    DictTree tree;
    public DeleteCommand( DictTree tree, DictNode removeNode ) {
        this.tree = tree;
        this.model = (DefaultTreeModel)tree.getModel();
        this.removeNode = removeNode;
        //削除するノードの親を求める
        parentNode = (DictNode)removeNode.getParent();
        //削除ノードが親の子配列の何番目にあるかを知る
        index = parentNode.getIndex( removeNode );
    }
    @Override
    public void redo() {
        model.removeNodeFromParent( removeNode );
        if ( parentNode.isRoot() ) {
            //本が丸ごと削除された場合は本の管理ファイルを更新
            Librarian.getInstance().updateIndexes();
        }
    }

    @Override
    public void undo() {
        model.insertNodeInto(removeNode, parentNode, index);
        tree.expandPath(parentNode.getTreePath());
        if ( parentNode.isRoot() ) {
            Librarian.getInstance().updateIndexes();
        }
    }

}
