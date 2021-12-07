/*
 * FolderTreeNode.java
 *
 * Created on 2006/07/10, 16:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ツリーの中のフォルダを表現するクラス。
 * DefaultMutableTreeNodeと同じものと見てよく、名前だけ違う。
 * @author 大澤義鷹
 */
class FolderTreeNode extends DefaultMutableTreeNode {
  
  /**
   * Creates a new instance of FolderTreeNode
   */
  public FolderTreeNode() {
    super();
  }
  public FolderTreeNode(Object object) {
    super(object);
  }
  public FolderTreeNode(Object object,boolean allowsChildren) {
    super(object,allowsChildren);
  }
}
