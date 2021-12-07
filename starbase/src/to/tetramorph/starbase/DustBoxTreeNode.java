/*
 * DustBoxTreeNode.java
 *
 * Created on 2006/07/02, 0:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import javax.swing.tree.DefaultMutableTreeNode;
import to.tetramorph.starbase.*;

/**
 * ツリーの中でごみ箱を表現するクラス。ごみ箱は他のフォルダとは区別できるように
 * しておきたい。つまりinstanceof演算で識別できるようにするため、別クラスとして
 * TreeNodeを用意している。
 */
class DustBoxTreeNode extends FolderTreeNode {
  
  /**
   * Creates a new instance of DustBoxTreeNode
   */
  public DustBoxTreeNode() {
    super("ごみ箱",false); //子を持つことは禁止
  }
  
}
