/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ViewerPanel.java
 *
 * Created on 2008/12/17, 21:12:28
 */

package to.tetramorph.starbase.dict;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import to.tetramorph.util.ParentWindow;

/**
 * 辞書機能の本体。
 * @author 大澤義鷹
 */
class ViewerPanel extends javax.swing.JPanel {
    Librarian lib = Librarian.getInstance();
    Window owner = ParentWindow.getWindowForComponent(this);
    List<TreePath> clipTreePath;
    int clipMode = 0;
    CopyAction copyAction = new CopyAction();
    PasteAction pasteAction = new PasteAction();
    CutAction cutAction = new CutAction();
    UndoAction undoAction = new UndoAction();
    RedoAction redoAction = new RedoAction();
    DeleteAction deleteAction = new DeleteAction();
    SaveAction saveAction = new SaveAction();
    PageWriter pageWriter;
    FocusHandler focusHandler;
    /** Creates new form ViewerPanel */
    public ViewerPanel() {
        initComponents();
        dictTree.setDefaultTreeModel( lib.getDefaultTreeModel() );
        dictTree.addMouseListener(new MouseHandlerOfTree());
        dictTree.addTreeSelectionListener(new TreeSelectionHandler());
        editorPane.setContentType("text/html");
        editorPane.addHyperlinkListener(new HyperlinkHandler());
        dictTree.getTreeShell().setShellListener( new ShellHandler());
        dictTree.getTreeShell().clear();
        lib.setOwnerWindow( ParentWindow.getWindowForComponent(this) );
        keybind_setup();
        pageWriter = new PageWriter(editorPane);
        searchPanel.setPageWriter(pageWriter);
        searchPanel.getJList().addMouseListener(new ResultListHandler());
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_I);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_S);
        focusHandler = new FocusHandler();
        editorPane.addFocusListener(focusHandler);
        dictTree.addFocusListener(focusHandler);
    }
    void keybind_setup() {
        ActionMap amap = dictTree.getActionMap();
        InputMap imap = dictTree.getInputMap();
//        Object name = imap.get(KeyStroke.getKeyStroke( KeyEvent.VK_V,
//                                                       InputEvent.CTRL_MASK));
//        System.out.println("Object name = " + name);

        amap.put("copy",copyAction);
        copyMenuItem.addActionListener(copyAction);
        amap.put("paste", pasteAction);
        pasteMenuItem.addActionListener(pasteAction);
        amap.put("cut", cutAction );
        cutMenuItem.addActionListener(cutAction);

        imap.put(KeyStroke.getKeyStroke( KeyEvent.VK_Z,InputEvent.CTRL_MASK), "undo");
        amap.put("undo", undoAction);
        undoMenuItem.addActionListener(undoAction);

        imap.put(KeyStroke.getKeyStroke( KeyEvent.VK_A,InputEvent.CTRL_MASK), "redo");
        amap.put("redo", redoAction);
        redoMenuItem.addActionListener(redoAction);

        imap.put(KeyStroke.getKeyStroke( KeyEvent.VK_DELETE,0), "delete");
        amap.put("delete", deleteAction);
        deleteMenuItem.addActionListener(deleteAction);

        imap.put(KeyStroke.getKeyStroke( KeyEvent.VK_S,0), "save");
        amap.put("save", saveAction);
        saveMenuItem.addActionListener(saveAction);

        imap.put(KeyStroke.getKeyStroke( KeyEvent.VK_F,InputEvent.CTRL_MASK), "find");
        amap.put("find", new SearchAction());

    }
    class SearchAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(1);
        }

    }
    class CopyAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            editMenuState();
            if ( copyMenuItem.isEnabled() ) {
                if ( focusHandler.getFocusedComponent() == dictTree ) {
                    System.out.println("ページコピー");
                    List<TreePath> list = dictTree.getSelectionPathList();
                    TreeUtils.removeDescendantPath(list);
                    clipTreePath = list;
                    clipMode = 0;
                } else {
                    System.out.println("テキストコピー");
//                    Toolkit kit = Toolkit.getDefaultToolkit();
//            		Clipboard cb = kit.getSystemClipboard();
//                    StringSelection ss = new StringSelection(str);
//                    cb.setContents(ss, ss);
                    editorPane.copy();
                }
            } else {
                beep();
            }
        }
    }

    class CutAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            editMenuState();
            if ( cutMenuItem.isEnabled() ) {
                System.out.println("カット");
                List<TreePath> list = dictTree.getSelectionPathList();
                TreeUtils.removeDescendantPath(list);
                clipTreePath = list;
                clipMode = 1;
            } else {
                beep();
            }
        }
    }

    class PasteAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            editMenuState();
            if ( pasteMenuItem.isEnabled() ) {
                System.out.println("ペースト");
                if ( clipTreePath == null ) return;
                TreePath path = dictTree.getSelectionPath();
                DictNode targetNode = TreeUtils.getDictNode(path);
                if ( clipMode == 1 ) { //移動処理
                    dictTree.moveNode( clipTreePath, targetNode , false);
                    clipTreePath = null;
                } else { //コピー処理
                    dictTree.copyNode( clipTreePath, targetNode );
                }
            } else {
                beep();
            }
        }
    }

    //アンドゥ／リドゥメニューのenable/disenableを制御
    class ShellHandler implements ShellListener {
        @Override
        public void commandExecuted( TreeShell shell ) {
            undoMenuItem.setEnabled( shell.canUndo() );
            redoMenuItem.setEnabled( shell.canRedo() );
        }
    }

    class UndoAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            dictTree.getTreeShell().undo();
        }
    }

    class RedoAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            dictTree.getTreeShell().redo();
        }
    }

    class SaveAction extends AbstractAction {
         @Override
        public void actionPerformed(ActionEvent e) {
            lib.saveBooks();
            clearHistory();
        }
    }

    class DeleteAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                dictTree.removeSelectedNodes();
                pageWriter.cls();
            } catch( IllegalArgumentException ex ) {
                error("辞書ページの削除",ex.getMessage());
            }
        }
    }
    class HyperlinkHandler implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if ( e.getEventType() != HyperlinkEvent.EventType.ACTIVATED )
                return;
            String desc = e.getDescription().substring(1);
            editorPane.scrollToReference(desc);
            System.out.println("Description = " + desc);
        }

    }
//    //テキスト表示画面を消去
//    private void cls() {
//        editorPane.setText("");
//    }
//
//    private void writePage( TreePath path ) {
//        if ( path == null ) { cls(); return; }
//        DictNode page = TreeUtils.getDictNode(path);
//        if ( ! page.isPage() ) { cls(); return; }
//        HtmlFormatter html = new HtmlFormatter();
//        html.addPage(page);
//        html.setFooter();
//        editorPane.setText(html.toString());
//        editorPane.setCaretPosition(0); //スクロール位置を先頭に
//    }
    public void writePages( String caption, List<DictNode> list ) {
        StringBuilder sb = new StringBuilder();
        HtmlFormatter html = new HtmlFormatter();
        html.addSearchResult(caption,list);
        html.setFooter();
        editorPane.setText(html.toString());
        editorPane.setCaretPosition(0); //スクロール位置を先頭に
    }

    //ツリー上でマウスがクリックされたときメニューをだしたり、その位置にある
    //フォルダが選択されたことにしてそのフォルダの中身を表示する。
    private class MouseHandlerOfTree extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY();
            if ( e.getButton() == MouseEvent.BUTTON3 ) {
                setMenuState();
                //右ボタンでポップアップメニュー
                //treePopupMenu.show(ViewerPanel.this,x,y);
                treePopupMenu.show((Component)e.getSource(),x,y);
            } else if ( e.getButton() == MouseEvent.BUTTON1 ) {
                //左ボタンでページ閲覧
                TreePath path = dictTree.getClosestPathForLocation(x,y);
                if ( ! dictTree.isPathSelected(path) ) {
                    pageWriter.cls();
                }
                if ( e.getClickCount() == 2 ) {
                    editMenuItemActionPerformed(null); //編集開始
                }
            }
        }
    }
    /**
     * ページが選択された際、その内容を表示する。
     */
    private class TreeSelectionHandler implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            pageWriter.writePage( e.getNewLeadSelectionPath() );
        }

    }

    /**
     * ポップアップメニュー内アイテムのEnable/Disenabledを
     * ノードの選択状態に応じてセット
     */
    private void setMenuState() {
        List<TreePath> list = dictTree.getSelectionPathList();
        if ( list.size() >= 1 ) { //編集禁止の本のときはメニューすべてが無効
            if( ! lib.getBook(list.get(0)).isEditable()) {
                deleteMenuItem.setEnabled(false);
                editMenuItem.setEnabled(false);
                makeFolderMenuItem.setEnabled(false);
                insertMenuItem.setEnabled(false);
                return;
            }
        }
        boolean state = TreeUtils.isBookFolderContains( list ) ||
                         list.isEmpty();
        deleteMenuItem.setEnabled( ! state );
        //ページ編集メニューの状態を設定
        editMenuItem.setEnabled(false);
        if ( list.size() == 1 ) {
            editMenuItem.setEnabled(
                    TreeUtils.getDictNode(list.get(0)).isPage());
        }
        //ページ作成メニューの状態を設定
        state = list.size() == 1;
        makeFolderMenuItem.setEnabled(state);
        insertMenuItem.setEnabled(state);
    }

    /**
     * 編集メニューのEnable/Disenableをセット
     */
    private void editMenuState() {

        List<TreePath> list = dictTree.getSelectionPathList();
        boolean cliped = clipTreePath == null ? false : true;
        if ( list.isEmpty() ) { //選択が無いとき
            editEnabled(false,false,false);
            return;
        }
        if ( list.size() >= 2 ) { //複数ノードが選択されている場合
            for ( TreePath path : list ) {
                //選択リスト中に編集禁止本が含まれる場合と、本ノードが選択されてる場合
                if ( ! lib.getBook(path).isEditable() ||
                        path.getPath().length == 2 ) {
                    editEnabled(false,false,false);
                    return;
                }
            }
            editEnabled(true,true,false);
        } else { //一つだけパスが選択された場合
            if ( ! lib.getBook(list.get(0)).isEditable() ) { //編集禁止の場合
                editEnabled(false,false,false);
                return;
            }
            if ( list.get(0).getPath().length == 2 ) { //本が選択された場合
                editEnabled(false,false,cliped);
            } else {
                //ページやフォルダが一つだけ選択されている場合
                editEnabled(true,true,cliped);
            }
        }
    }

    //エラーメッセージダイアログを出す
    private void error( String title,String errmsg ) {
        JOptionPane.showMessageDialog( owner,
                                       errmsg,
                                       title,
                                       JOptionPane.ERROR_MESSAGE);
    }

    /**
     * ファイル(F)メニューを返す。
     * @return
     */
    public JMenu getFileMenu() {
        return fileMenu;
    }
    /**
     * 履歴を消去。本を閉じた後でUndoされ、それが閉じてしまった本への
     * Undoだとエラーになってしまうなどの局面がある。たとえば「本を閉じる」を
     * 実行した後は、このメソッドで履歴を消去している。他にも「本を保存」
     * 「ダイアログを閉じる」といった場合もこれで消去する。
     */
    public void clearHistory() {
        dictTree.getTreeShell().clear();
    }

    /**
     * 編集(E)メニューを返す。
     * @return
     */
    public JMenu getEditMenu() {
        return editMenu;
    }
    private static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }
    public JSplitPane getSplitPane() {
        return splitPane1;
    }

    private class ResultListHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY();
            if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                JList jList = searchPanel.getJList();
                DictNode node = (DictNode)jList.getSelectedValue();
                tabbedPane.setSelectedIndex(0);
                TreePath path = TreeUtils.getTreePath(node);
                dictTree.setSelectionPath( path );
                dictTree.scrollPathToVisible( path );
                System.out.println("ダブクリ " + node );
            }
        }

    }

    private class FocusHandler implements FocusListener {
        Component c;
        @Override
        public void focusGained(FocusEvent e) {
            c = e.getComponent();
            if ( e.getComponent() == dictTree ) {
                System.out.println("JTreeがフォーカス取得");
            } else if ( e.getComponent() == editorPane ) {
                System.out.println("JEditorPaneがフォーカス取得");
            } else {
                System.out.println( "それ以外 " + e.getComponent());
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
        //現在フォーカスのpageTree,editorPane,nullのいずれかを返す。
        Component getFocusedComponent() {
            return c;
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treePopupMenu = new javax.swing.JPopupMenu();
        editMenuItem = new javax.swing.JMenuItem();
        insertMenuItem = new javax.swing.JMenuItem();
        makeFolderMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        createBookMenu = new javax.swing.JMenu();
        createMenuItem1 = new javax.swing.JMenuItem();
        createMenuItem2 = new javax.swing.JMenuItem();
        bookMenu = new javax.swing.JMenu();
        bookMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        editMenu = new javax.swing.JMenu();
        copyMenuItem = new javax.swing.JMenuItem();
        cutMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        splitPane1 = new javax.swing.JSplitPane();
        tabbedPane = new javax.swing.JTabbedPane();
        treeScrollPane = new javax.swing.JScrollPane();
        dictTree = new to.tetramorph.starbase.dict.DictTree();
        searchPanel = new to.tetramorph.starbase.dict.SearchPanel();
        viewerScrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        editMenuItem.setText("ページ編集");
        editMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(editMenuItem);

        insertMenuItem.setText("ページ作成");
        insertMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(insertMenuItem);

        makeFolderMenuItem.setText("フォルダ作成");
        makeFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeFolderMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(makeFolderMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setText("削除");
        treePopupMenu.add(deleteMenuItem);

        fileMenu.setMnemonic('F');
        fileMenu.setText("ファイル(F)");
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        openMenuItem.setText("本を開く");
        openMenuItem.setActionCommand("本を出す");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("本を上書き保存");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        closeMenuItem.setText("本を閉じる");
        closeMenuItem.setActionCommand("本をしまう");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        exportMenuItem.setText("本を別ファイルに保存");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportMenuItem);

        createBookMenu.setText("本の新規作成");

        createMenuItem1.setText("西洋占星術辞典テンプレート");
        createMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMenuItem1ActionPerformed(evt);
            }
        });
        createBookMenu.add(createMenuItem1);

        createMenuItem2.setText("サビアン辞典テンプレート");
        createMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMenuItem2ActionPerformed(evt);
            }
        });
        createBookMenu.add(createMenuItem2);

        fileMenu.add(createBookMenu);

        bookMenu.setText("標準辞典を開く");

        bookMenuItem1.setText("アマテル西洋占星術辞典");
        bookMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookMenuItem1ActionPerformed(evt);
            }
        });
        bookMenu.add(bookMenuItem1);

        fileMenu.add(bookMenu);
        fileMenu.add(jSeparator1);

        editMenu.setMnemonic('E');
        editMenu.setText("編集(E)");
        editMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                editMenuMenuSelected(evt);
            }
        });

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setText("コピー");
        editMenu.add(copyMenuItem);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setText("カット");
        editMenu.add(cutMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setText("ペースト");
        editMenu.add(pasteMenuItem);
        editMenu.add(jSeparator2);

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setText("アンドゥ");
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setText("リドゥ");
        editMenu.add(redoMenuItem);

        setPreferredSize(new java.awt.Dimension(600, 400));
        setLayout(new java.awt.GridLayout(1, 0));

        splitPane1.setContinuousLayout(true);
        splitPane1.setOneTouchExpandable(true);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        dictTree.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        treeScrollPane.setViewportView(dictTree);

        tabbedPane.addTab("目次( I )", treeScrollPane);
        tabbedPane.addTab("検索(S)", searchPanel);

        splitPane1.setLeftComponent(tabbedPane);

        editorPane.setEditable(false);
        viewerScrollPane.setViewportView(editorPane);

        splitPane1.setRightComponent(viewerScrollPane);

        add(splitPane1);
    }// </editor-fold>//GEN-END:initComponents
    //編集 (ノードのダブクリでも呼ばれる)
    private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
        TreePath path = dictTree.getSelectionPath();
        if ( lib.getBook(path) == null ) return;
        if ( ! lib.getBook(path).isEditable() ) return;
        int [] rows = dictTree.getSelectionRows();
//        if ( rows.length != 1 )
//            throw new java.lang.IllegalStateException("パスが複数選択状態のときは編集不可");
        DictNode node = TreeUtils.getDictNode(path);
        if ( ! node.isPage() ) return;
        DictNode result = EditDialog.showDialog(
                                    ParentWindow.getWindowForComponent(this),
                                    path);
        if ( result != null ) {
            dictTree.updateNode(path, result);
            dictTree.setSelectionRow(rows[0]);
        }
        pageWriter.writePage( dictTree.getSelectionPath() );
    }//GEN-LAST:event_editMenuItemActionPerformed
    //新規ページの作成
    private void insertMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertMenuItemActionPerformed
        try {
            dictTree.makePage();
        } catch ( Exception e ) {
            error("新規ページの作成",e.getMessage());
        }
    }//GEN-LAST:event_insertMenuItemActionPerformed
    //フォルダを作成
    private void makeFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeFolderMenuItemActionPerformed
        try {
            dictTree.makeFolder();
        } catch ( Exception e ) {
            error("新規フォルダの作成",e.getMessage());
        }
}//GEN-LAST:event_makeFolderMenuItemActionPerformed
    //ページまたはフォルダの削除    //本を閉じる
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        System.out.println("本閉じた");
        Book book = lib.getBook(dictTree.getSelectionPath());
        lib.closeBook(book);
        clearHistory();
    }//GEN-LAST:event_closeMenuItemActionPerformed
    //本を開く
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        lib.importBook(dictTree);
    }//GEN-LAST:event_openMenuItemActionPerformed
    //別ファイルに保存（エクスポート）
    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        lib.exportBook(dictTree.getSelectionPath());
}//GEN-LAST:event_exportMenuItemActionPerformed
    //上書き保存
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
//        lib.saveBooks();
//        clearHistory();
    }//GEN-LAST:event_saveMenuItemActionPerformed
    /**
     * ファイルメニューが選択されたとき編集禁止の本が選択されているなら、
     * exportMenuItemを無効化する。
     */
    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
        tabbedPane.setSelectedIndex(0); //タブをページツリーに切り替える
        List<TreePath> list = dictTree.getSelectionPathList();
        if ( list.size() >= 1 ) {
            exportMenuItem.setEnabled( lib.getBook(list.get(0)).isEditable() );
            closeMenuItem.setEnabled( list.size() == 1 );
        } else {
            exportMenuItem.setEnabled(false);
            closeMenuItem.setEnabled(false);
        }
    }//GEN-LAST:event_fileMenuMenuSelected

    private void editMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_editMenuMenuSelected
        editMenuState();
    }//GEN-LAST:event_editMenuMenuSelected

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        //JMenuItem [] items = { copyMenuItem,cutMenuItem,pasteMenuItem,undoMenuItem,redoMenuItem };
        editMenu.setEnabled( tabbedPane.getSelectedIndex() == 0 );
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void bookMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookMenuItem1ActionPerformed
//        System.out.println("app.home = " + System.getProperty("app.home") );
//        System.out.println("Home.dir = " + Home.dir );
        File file = new File( System.getProperty("app.dict"), "std_dict.abf");
        if ( ! file.exists() ) {
            JOptionPane.showMessageDialog(this,"辞典ファイルが見つかりません。",
                    "標準辞典を開く",JOptionPane.ERROR_MESSAGE );
            return;
        }
        lib.importBook(file, dictTree);
    }//GEN-LAST:event_bookMenuItem1ActionPerformed

    private void createMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMenuItem1ActionPerformed
        File file = CreateBook.createBook(this, "新規・西洋占星術辞典",
                                            "/resources/dict_std_template.abf");
        if ( file != null ) {
            lib.importBook(file, dictTree);
        }
    }//GEN-LAST:event_createMenuItem1ActionPerformed

    private void createMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMenuItem2ActionPerformed
        File file = CreateBook.createBook(this, "新規・サビアン辞典",
                                         "/resources/dict_sabian_template.abf");
        if ( file != null ) {
            lib.importBook(file, dictTree);
        }
    }//GEN-LAST:event_createMenuItem2ActionPerformed

    private void editEnabled(boolean copy,boolean cut, boolean paste ) {
        copyMenuItem.setEnabled(copy);
        cutMenuItem.setEnabled(cut);
        pasteMenuItem.setEnabled(paste);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu bookMenu;
    private javax.swing.JMenuItem bookMenuItem1;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenu createBookMenu;
    private javax.swing.JMenuItem createMenuItem1;
    private javax.swing.JMenuItem createMenuItem2;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private to.tetramorph.starbase.dict.DictTree dictTree;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem insertMenuItem;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem makeFolderMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private to.tetramorph.starbase.dict.SearchPanel searchPanel;
    private javax.swing.JSplitPane splitPane1;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPopupMenu treePopupMenu;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JScrollPane viewerScrollPane;
    // End of variables declaration//GEN-END:variables

}
