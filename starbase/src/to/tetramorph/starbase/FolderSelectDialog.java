/*
 * FolderSelectDialog.java
 *
 * Created on 2006/06/26, 22:21
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import to.tetramorph.util.ParentWindow;

/**
 * データツリーを表示して異動先やコピー先のフォルダをユーザに選択させるダイアログ。
 * showDialogメソッドをstaticで呼び出して使用する。
 * FrameやDialogをオーナーとして指定しなくても自動判定で開いてくれる。
 * このダイアログを直接使うよりも、DataExplorerPane#showFolderSelectDialog()を
 * つかうほうが簡単。(DataExplorerPanelは結局このクラスを呼び出しているのだが)。
 */
class FolderSelectDialog extends javax.swing.JDialog {

    private static TreePath selectedTreePath = null;
    
    //親がFrameだったときのコンストラクタ
    private FolderSelectDialog(java.awt.Frame parent,JTree tree) {
        super(parent, true);
        initComponents();
        this.tree = tree;
        scrollPane.setViewportView(tree);
        //addKeyListener(new KeyHandler());
        setEscCloseOperation();
    }
    //親がDialogだったときのコンストラクタ
    private FolderSelectDialog(java.awt.Dialog parent,JTree tree) {
        super(parent, true);
        initComponents();
        this.tree = tree;
        scrollPane.setViewportView(tree);
        //addKeyListener(new KeyHandler());
        setEscCloseOperation();
    }
    private void setEscCloseOperation() {
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                abortButton.doClick();
            }
        });
    }
    /**
     * ダイアログを開きフォルダーリストから選択を要求し、選択されたフォルダのTreePath
     * を返す。このダイアログで使用するツリーはDatabaseFrame#getTree()で取得したものを
     * 指定する。このメソッドで取得したJTreeにはフォルダ作成や削除の機能が加えられてい
     * るので操作上便利である。
     * @param parent 親コンポーネント(通常はthisを指定)
     * @param treeNode 階層構造状に編み上げたTreeNode群
     * @param current デフォルトで選択したことにしておくパス
     * @param tree DatabaseFrame#getTree()で取得したJTree
     * @return 選択されたパス
     */
    public static TreePath showDialog(Component parent,TreeNode treeNode,TreePath current,JTree tree) {
        return showDialog(parent,treeNode,current,tree,null);
    }
    
    public static TreePath showDialog(Component parent,TreeNode treeNode,TreePath current,JTree tree,String message) {
        Window window = ParentWindow.getWindowForComponent(parent);
        FolderSelectDialog dialog;
        if(window instanceof Frame) {
            dialog = new FolderSelectDialog((Frame)window,tree);
        } else {
            dialog = new FolderSelectDialog((Dialog)window,tree);
        }
        dialog.setCurrent(current);
        dialog.setComponentOrientation(window.getComponentOrientation());
        dialog.pack();
        dialog.setLocationRelativeTo(window); //親コンポーネントに対してセンタリング
        if(message != null) dialog.messageLabel.setText(message);
        dialog.setVisible(true);
        return selectedTreePath;
    }
    
    private void setCurrent(TreePath current) {
        if(current == null) return;
        tree.setSelectionPath(current);
        //tree.expandPath(current);
        tree.scrollPathToVisible(current);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel jPanel1;

    jPanel1 = new javax.swing.JPanel();
    acceptButton = new javax.swing.JButton();
    abortButton = new javax.swing.JButton();
    messageLabel = new javax.swing.JLabel();
    scrollPane = new javax.swing.JScrollPane();
    tree = new javax.swing.JTree();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("\u30d5\u30a9\u30eb\u30c0\u306e\u9078\u629e");
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    acceptButton.setMnemonic('Y');
    acceptButton.setText("\u6c7a\u5b9a(Y)");
    acceptButton.setToolTipText("ALT+Enter\u3067\u3082\u6c7a\u5b9a\u3067\u304d\u307e\u3059");
    acceptButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        acceptButtonActionPerformed(evt);
      }
    });

    jPanel1.add(acceptButton);

    abortButton.setMnemonic('N');
    abortButton.setText("\u4e2d\u6b62(N)");
    abortButton.setToolTipText("ALI+I\u3067\u3082\u4e2d\u6b62\u3067\u304d\u307e\u3059");
    abortButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        abortButtonActionPerformed(evt);
      }
    });

    jPanel1.add(abortButton);

    getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

    messageLabel.setText("\u79fb\u52d5\u307e\u305f\u306f\u30b3\u30d4\u30fc\u5148\u30d5\u30a9\u30eb\u30c0\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002");
    messageLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
    getContentPane().add(messageLabel, java.awt.BorderLayout.NORTH);

    tree.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
    scrollPane.setViewportView(tree);

    getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      selectedTreePath = null;
  }//GEN-LAST:event_formWindowClosing
  
  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
      if(tree.getSelectionPath() != null) {
          selectedTreePath = tree.getSelectionPath();
          dispose();
      }
  }//GEN-LAST:event_acceptButtonActionPerformed
  
  private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
      selectedTreePath = null;
      dispose();
  }//GEN-LAST:event_abortButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton abortButton;
  private javax.swing.JButton acceptButton;
  private javax.swing.JLabel messageLabel;
  private javax.swing.JScrollPane scrollPane;
  private javax.swing.JTree tree;
  // End of variables declaration//GEN-END:variables
  
}
