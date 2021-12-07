/*
 * SelectTreeCellRenderer.java
 *
 * Created on 2006/07/18, 18:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import to.tetramorph.util.IconLoader;

/**
 * 削除やDnDを行わない選択のみ行うTreeのためのセルレンダラー。
 * @author 大澤義鷹
 */
class SelectTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final Icon openFolderIcon;
  private static final Icon dustboxIcon;
  static {
    openFolderIcon =IconLoader
      .getImageIcon("/resources/images/Tree.openIcon.png");
    dustboxIcon = IconLoader.
      getImageIcon("/resources/images/Tree.dustbox.png");
  }
  public SelectTreeCellRenderer() {
  }
  //このメソッドはツリーが一行づつ描画されるたびに呼び出されるようで、
  //これをオーバーライドして、表示されるフォルダのアイコンを変化させる。
  public Component getTreeCellRendererComponent(
    JTree tree,Object value,boolean sel,boolean expanded,
    boolean leaf,int row,boolean hasFocus) {
    super.getTreeCellRendererComponent(
      tree, value, sel, expanded, leaf, row, hasFocus);
    if(value instanceof DustBoxTreeNode) {
      //ごみ箱ならアイコンを変更
      setIcon(dustboxIcon);
      setText("DustBox");
    }
    else if(sel) setIcon(openFolderIcon);
    else setIcon(UIManager.getIcon("Tree.closedIcon"));
    return this;
  }
}
