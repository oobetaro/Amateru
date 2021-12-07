/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import javax.swing.tree.DefaultTreeModel;

/**
 * ツリーノードの「リネーム」命令。
 * @author 大澤義鷹
 */
class RenameCommand implements ShellCommand {
    DefaultTreeModel model;
    DictNode renamedNode;
    String newName;
    String oldName;
    DictTree tree;
    public RenameCommand( DictTree tree, DictNode renamedNode, String oldName ) {
        this.tree = tree;
        this.model = (DefaultTreeModel)tree.getModel();
        this.renamedNode = renamedNode;
        this.newName = renamedNode.toString();
        this.oldName = oldName;
        System.out.println("renamedNode = " + renamedNode + ", oldName = " + oldName);
    }

    @Override
    public void redo() {
        renamedNode.setUserObject(newName);
        model.nodeChanged(renamedNode);
    }

    @Override
    public void undo() {
        renamedNode.setUserObject(oldName);
        model.nodeChanged(renamedNode);
        tree.expandPath(renamedNode.getTreePath());
    }

}
