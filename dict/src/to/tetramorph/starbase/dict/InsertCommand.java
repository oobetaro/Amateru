/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import javax.swing.tree.DefaultTreeModel;

/**
 * ツリーノードの「挿入」命令。
 * @author 大澤義鷹
 */
class InsertCommand implements ShellCommand {

    DefaultTreeModel model;
    DictNode newChild;
    DictNode parentNode;
    int index;
    DictTree tree;
    public InsertCommand( DictTree tree, DictNode newNode, DictNode parentNode, int index ) {
        this.tree = tree;
        this.model = (DefaultTreeModel)tree.getModel();
        this.newChild = newNode;
        this.parentNode = parentNode;
        this.index = index;
    }
    @Override
    public void redo() {
        model.insertNodeInto(newChild, parentNode, index);
        tree.expandPath(parentNode.getTreePath());
        if ( parentNode.isRoot() )
            Librarian.getInstance().updateIndexes();
    }

    @Override
    public void undo() {
        model.removeNodeFromParent(newChild);
        if ( parentNode.isRoot() )
            Librarian.getInstance().updateIndexes();
    }

}
