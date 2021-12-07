/*
 * SearchResultPanel.java
 *
 * Created on 2007/12/14, 16:06
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.lib.SearchResultReceiver;
import to.tetramorph.starbase.multisplit.InnerTabbedPane;

/**
 * 検索結果を表示するパネル。
 * @author 大澤義鷹
 */
class SearchResultPanel extends javax.swing.JPanel 
                             implements SearchResultReceiver {
    
    private DataExplorer explorer;
    private ChartDesktop mainFrame;
    private TableMouseHandler tableMouseHandler = new TableMouseHandler();
    
    /**
     * 検索結果フレームを作成する。
     * @param explorer DataExplorerオブジェクト
     * @param mainFrame MainFrameオブジェクト
     */
    public SearchResultPanel( DataExplorer explorer,
                               ChartDesktop mainFrame) {
        this.explorer = explorer;
        this.mainFrame = mainFrame;
        initComponents();
    }
    
    /**
     * 検索結果をこのパネルに書きこむ（出力する）＞
     * @param result
     */
    @Override
    public void write( SearchResult result ) {
        String tabName = result.getTabName();
        if ( tabName == null )
            tabName = "No.";
        tabName = ( tabbedPane.getTabCount() + 1 ) + "." + tabName;
        String message = result.getTitle();
        if ( message == null ) message = "";
        
        SearchResultTabPanel tabPanel = new SearchResultTabPanel();
        SearchResultTable table = tabPanel.getSearchResultTable();
        table.addMouseListener( tableMouseHandler );
        Icon icon = new ImageIcon(
          InnerTabbedPane.class.getResource("/resources/images/search_icon.gif"));
        String count;
        if ( result.getCount() < 0 ) {
            count = "該当件数 " + result.getNatalList().size() + "件";
        } else {
            int hit = result.getNatalList().size();
            int size = result.getCount();
            float par = 0F;
            if ( size > 0 )
                par = ((float)hit / (float)size) * 100F;
            count = String.format("%d 件中 %d件, %4.1f %%",size,hit,par);
        }
        tabPanel.getSizeLabel().setText( count );
        tabPanel.getMessageLabel().setText( message );
        tabPanel.getPathLabel().setText( result.getPathName() );
        tabbedPane.addTab( tabName, icon, tabPanel );
        tabbedPane.setSelectedComponent( tabPanel );
        table.showList( reloadNatalList(result.getNatalList()) );
        
    }

    /**
     * 検索で求まったネイタルデータにはヒストリーやコンポジット情報が入っていない
     * ので正式に読み直す。
     * @param list
     * @return
     */
    private List<Natal> reloadNatalList( List<Natal> list ) {
        if ( list.size() == 0 ) return list;
        List<Natal> results = new ArrayList<Natal>();
        for( Natal n : list ) {
            if ( n.getId() < 0 ) {
                //回帰時など動的生成物およびヒストリーが空ならそのままコピー
                results.add( n );
            } else {
                results.add( explorer.getNatal(n.getId()));
            }
        }
        return results;
    }

    //マウスハンドラ
    private class TableMouseHandler extends MouseAdapter {
        //ダブクリされたら選択されてるNatal(一つしかない)をmainFrameに送る。
        @Override
        public void mouseClicked( MouseEvent e ) {
            if ( e.getClickCount() != 2 ) return;
            if ( e.getButton() != MouseEvent.BUTTON1 ) return;
            SearchResultTable table = (SearchResultTable)e.getSource();
            List<Natal> list = table.getSelectedList();
            if ( list == null || list.size() == 0 ) return;
            if ( e.isAltDown() && ! mainFrame.isEmptyChartPane() ) {
                mainFrame.setTransit( list, null );
            } else {
                if ( mainFrame.isEmptyChartPane() ) 
                    mainFrame.openChartPane(list);
                else 
                    mainFrame.setNatal( list, null );
            }
        }
        //右ｸﾘｯｸでﾎﾟｯﾌﾟｱｯﾌﾟﾒﾆｭｰを出す
        @Override
        public void mousePressed( MouseEvent event ) {
            SearchResultTable table = (SearchResultTable)event.getSource();
            int row = table.getSelectedRowCount();
            Natal natal = table.getSelectedNatal();
            //回帰計算などで作成されたデータの場合は、ジャンプ機能は無効にする
            boolean b = ( natal != null && natal.getId() == Natal.NEED_REGIST );
            jumpMenuItem.setEnabled( ! b );
            if ( event.getButton() == MouseEvent.BUTTON3 && row > 0 ) {
                
                updateSendMenu();
                popupMenu.show( (Component) event.getSource(), 
                                event.getX(), event.getY() );
            }
        }
    }

    //ポップアップメニューの「送る」とそのサブメニュー更新
    private void updateSendMenu() {
        sendMenu.removeAll();
        sendMenu.add(newChartMenuItem);
        sendAddMenu.removeAll();
        ChartPane [] frame = mainFrame.getChartPanes();
        if ( frame.length > 0 ) {
            for ( int i = 0; i < frame.length; i++ ) {
                JMenuItem sendMenuItem =
                    new JMenuItem("\"" + frame[i].getTitle()+"\"へ");
                sendMenu.add( sendMenuItem );
                sendMenuItem.addActionListener(
                    new SendMenuActionHandler( frame[i] ) );
                JMenuItem sendAddMenuItem
                    = new JMenuItem("\"" + frame[i].getTitle()+"\"へ");
                sendAddMenu.add( sendAddMenuItem );
                sendAddMenuItem.addActionListener(
                    new SendAddMenuActionHandler( frame[i] ) );
            }
        }
    }
    
    // 内部フレーム指定でNatalを送るためのイベントハンドラ
    private class SendMenuActionHandler implements ActionListener {
        ChartPane frame;
        SendMenuActionHandler(ChartPane frame) {
            this.frame = frame;
        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            SearchResultTable table = getSelectedTable();
            List<Natal> list = table.getSelectedList();
            if ( list == null ) return;
            mainFrame.setNatal( list, frame );
        }
    }
    
    // 内部フレーム指定で追加でNatalを送るためのイベントハンドラ
    private class SendAddMenuActionHandler implements ActionListener {
        ChartPane frame;
        SendAddMenuActionHandler(ChartPane frame) {
            this.frame = frame;
        }
        //内部フレームのsetNatalに選択されているNatalのリストを渡す
        @Override
        public void actionPerformed(ActionEvent evt) {
            SearchResultTable table = getSelectedTable();
            List<Natal> list = table.getSelectedList();
            if ( list == null ) return;
            mainFrame.addNatal( list, frame );
        }
    }
    
    // 現在選択されているタブの中のSearchResultTableを返す。
    private SearchResultTable getSelectedTable() {
        SearchResultTabPanel panel = 
            (SearchResultTabPanel)tabbedPane.getSelectedComponent();
        return panel.getSearchResultTable();        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        popupMenu = new javax.swing.JPopupMenu();
        sendMenu = new javax.swing.JMenu();
        newChartMenuItem = new javax.swing.JMenuItem();
        sendAddMenu = new javax.swing.JMenu();
        sendTransitMenuItem = new javax.swing.JMenuItem();
        jumpMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        saveAllMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        tabbedPane = new to.tetramorph.starbase.multisplit.InnerTabbedPane();

        sendMenu.setText("\u9001\u308b");
        newChartMenuItem.setText("\u65b0\u898f\u30c1\u30e3\u30fc\u30c8");
        newChartMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newChartMenuItemActionPerformed(evt);
            }
        });

        sendMenu.add(newChartMenuItem);

        popupMenu.add(sendMenu);

        sendAddMenu.setText("\u8ffd\u52a0\u3067\u9001\u308b");
        sendAddMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendAddMenuActionPerformed(evt);
            }
        });

        popupMenu.add(sendAddMenu);

        sendTransitMenuItem.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8\u306b\u9001\u308b");
        sendTransitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendTransitMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(sendTransitMenuItem);

        jumpMenuItem.setText("\u30b8\u30e3\u30f3\u30d7");
        jumpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(jumpMenuItem);

        popupMenu.add(jSeparator1);

        saveAllMenuItem.setText("\u3059\u3079\u3066\u4fdd\u5b58");
        saveAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(saveAllMenuItem);

        saveMenuItem.setText("\u9078\u629e\u3092\u4fdd\u5b58");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(saveMenuItem);

        removeMenuItem.setText("\u9078\u629e\u3092\u524a\u9664");
        removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(removeMenuItem);

        setLayout(new java.awt.BorderLayout());

        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        add(tabbedPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    //すべて保存
    private void saveAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllMenuItemActionPerformed
        SearchResultTable table = getSelectedTable();
        List<Natal> list = table.getNatalList();
        explorer.copyToFolder(list);
    }//GEN-LAST:event_saveAllMenuItemActionPerformed

    // トランジットに送る
    private void sendTransitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendTransitMenuItemActionPerformed
        if ( ! mainFrame.isEmptyChartPane() ) {
            SearchResultTable table = getSelectedTable();
            //Altキー併用でダブクリなら、トランジットとしてセット
            mainFrame.setTransit( table.getSelectedList(), null );
        }
    }//GEN-LAST:event_sendTransitMenuItemActionPerformed

    private void removeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMenuItemActionPerformed
        SearchResultTable table = getSelectedTable();
        table.removeSelectedList();
    }//GEN-LAST:event_removeMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        //saveButtonActionPerformed(null);
        SearchResultTable table = getSelectedTable();
        List<Natal> list = table.getSelectedList();
        if ( list == null ) return;
        if ( list.size() == 0 ) {
            JOptionPane.showMessageDialog(
                this, "保存するデータを選択してください",
                "検索結果の保存",JOptionPane.INFORMATION_MESSAGE );
            return;
        }
        explorer.copyToFolder(list);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void jumpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpMenuItemActionPerformed
       SearchResultTable table = getSelectedTable();
       Natal occ = table.getSelectedNatal();
        if ( occ == null ) return;
        explorer.selectTree(occ);
    }//GEN-LAST:event_jumpMenuItemActionPerformed

    private void sendAddMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendAddMenuActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_sendAddMenuActionPerformed

    private void newChartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChartMenuItemActionPerformed
        SearchResultTable table = getSelectedTable();
        List<Natal> list = table.getSelectedList();
        if ( list == null ) return;
        mainFrame.openChartPane( list );
    }//GEN-LAST:event_newChartMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuItem jumpMenuItem;
    private javax.swing.JMenuItem newChartMenuItem;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu sendAddMenu;
    private javax.swing.JMenu sendMenu;
    private javax.swing.JMenuItem sendTransitMenuItem;
    private to.tetramorph.starbase.multisplit.InnerTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
