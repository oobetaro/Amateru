/*
 * ColorSaveDialog.java
 *
 * Created on 2007/01/08, 19:28
 */

package to.tetramorph.starbase;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import to.tetramorph.util.ParentWindow;

/**
 * 色設定または仕様設定(使用天体等)情報を削除するときのダイアログ。
 * 設定名のリストを表示し削除したい名前を選択して削除できる。
 * 複数の名前をまとめて削除することもできる。
 */
final class SpecificChangerDialog extends JDialog {
    String type;
    static SpecificChangerDialog dialog;
    Vector<String> nameVector = new Vector<String>();
    String confName;
    ModuleCustomizer moduleCustomizer;

    // コンストラクタで渡されるこのダイアログの動作モード
    private DB db = DBFactory.getInstance();
    String tableName;
    JListHandler jListHandler = new JListHandler();
    /**
     * 外部からインスタンスを作成できないコンストラクタ
     * @param parent 親フレーム
     * @param type チャートタイプ名で事実上、パッケージ名こみのクラスファイル名
     * @param confName 設定名を指定する
     */
    private SpecificChangerDialog( Frame parent,
                                    String type,
                                    String confName,
                                    ModuleCustomizer moduleCustomizer) {
        super(parent, true);
        this.moduleCustomizer = moduleCustomizer;
        tableName = moduleCustomizer.getProperty("tableName");
        initComponents();
        setTitle( moduleCustomizer.getProperty("changerDialogTitle") );
        this.type = type;
        this.confName = confName;
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                abortButton.doClick();
            }
        });
        // TEST
        jList.addListSelectionListener( jListHandler );

        // DBから取得した設定名をnameVectorに格納しJListにセットする
        db.getConfigNames( type,
                           nameVector,
                           tableName );
        nameVector.add( ModuleCustomizer.DEFAULT );
        jList.setListData(nameVector);
        // ユーザが現在使用中の設定名をJListの中で選択状態にする。
        if ( confName.equals("") ) { // 標準選択時
            jList.setSelectedIndex( nameVector.size() - 1 );
        } else {
            for ( int i = 0; i < nameVector.size(); i++ ) {
                if ( nameVector.get(i).equals( confName ) ) {
                    jList.setSelectedIndex(i);
                    break;
                }
            }
        }
        pack();
        setLocationRelativeTo(parent);
    }
    /**
     * JListでアイテムが選択されたときのイベントハンドラ
     */
    class JListHandler implements ListSelectionListener {
        @Override
        public void valueChanged( ListSelectionEvent evt ) {
            int num = jList.getSelectedIndex();
            if ( num == -1 ) {
                // Ctrlキー併用選択などで、完全にリストが選択解除されたときは
                // 標準を選択する。
                for ( int i=0; i<nameVector.size(); i++) {
                    if ( nameVector.get(i).equals( ModuleCustomizer.DEFAULT ) ) {
                        jList.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                String name = (String)jList.getSelectedValue();
                if ( name.equals( ModuleCustomizer.DEFAULT ) ) { //「標準」は編集禁止
                    upButton.setEnabled( false );
                    downButton.setEnabled( false );
                    removeMenuItem.setEnabled( false );
                    renameMenuItem.setEnabled( false );
                    exportMenuItem.setEnabled( false );
                } else {
                    upButton.setEnabled( true );
                    downButton.setEnabled( true );
                    removeMenuItem.setEnabled( true );
                    int [] ixs = jList.getSelectedIndices();
                    renameMenuItem.setEnabled( ixs.length == 1 );
                    exportMenuItem.setEnabled( true );
                }
            }
            String name = ( num < 0) ? "" : nameVector.get(num);
            confName = name.equals( ModuleCustomizer.DEFAULT ) ? "" : name;
            // 別スレで呼び出さないと、JEditPaneを使ったReportPluginで障害が出る
            EventQueue.invokeLater( new Runnable() {
                @Override
                public void run() {
                    moduleCustomizer.load( confName );
                }
            });

        }
    }
    /**
     * @param owner 親フレーム
     * @param type チャートタイプ名。パッケージ名こみのフルクラス名。
     * @param confName 色設定名
     * @return 最後に選択されていた設定名
     */
    public static String showDialog( Frame owner,
                                       String type,
                                       String confName,
                                       ModuleCustomizer moduleCustomizer,
                                       boolean isEditMode ) {
        assert SwingUtilities.isEventDispatchThread();
        dialog = new SpecificChangerDialog( owner, type, confName, moduleCustomizer );
        dialog.editCheckBox.setSelected( isEditMode );
        dialog.editCheckBoxActionPerformed( null );
        dialog.setVisible( true );
        return dialog.confName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel consolePanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        editButtonPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        downButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        acceptButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();
        resizeIconPanel1 = new to.tetramorph.widget.ResizeIconPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        editCheckBox = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        importMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("設定の削除");
        setMinimumSize(new java.awt.Dimension(270, 200));
        getContentPane().setLayout(new java.awt.BorderLayout(0, 5));

        consolePanel.setLayout(new java.awt.BorderLayout());

        controlPanel.setLayout(new java.awt.BorderLayout());

        editButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 8, 1));
        editButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("<html>ShiftまたはCtrlキーを併用すると<br>\n複数の名前を選択できます。</html>");
        jPanel6.add(jLabel1, new java.awt.GridBagConstraints());

        editButtonPanel.add(jPanel6);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        downButton.setMnemonic('L');
        downButton.setText("▽");
        downButton.setToolTipText("下に移動(Alt-L)");
        downButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        jPanel1.add(downButton);

        upButton.setMnemonic('U');
        upButton.setText("△");
        upButton.setToolTipText("上に移動(Alt-U)");
        upButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        jPanel1.add(upButton);

        editButtonPanel.add(jPanel1);

        controlPanel.add(editButtonPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        acceptButton.setMnemonic('Y');
        acceptButton.setText("決定(Y)");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });
        jPanel4.add(acceptButton);

        abortButton.setMnemonic('N');
        abortButton.setText("中止(N)");
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });
        jPanel4.add(abortButton);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);
        jPanel3.add(resizeIconPanel1, java.awt.BorderLayout.EAST);

        editCheckBox.setSelected(true);
        editCheckBox.setText("整理");
        editCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        editCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCheckBoxActionPerformed(evt);
            }
        });
        jPanel2.add(editCheckBox);

        jPanel3.add(jPanel2, java.awt.BorderLayout.WEST);

        controlPanel.add(jPanel3, java.awt.BorderLayout.SOUTH);

        consolePanel.add(controlPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(consolePanel, java.awt.BorderLayout.SOUTH);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4));
        jPanel5.setLayout(new java.awt.GridLayout(1, 0));

        jList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
        jList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListMouseClicked(evt);
            }
        });
        scrollPane.setViewportView(jList);

        jPanel5.add(scrollPane);

        getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

        jMenu1.setMnemonic('F');
        jMenu1.setText("ファィル(F)");

        importMenuItem.setText("インポート");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(importMenuItem);

        exportMenuItem.setText("エクスポート");
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exportMenuItem);

        jMenuBar2.add(jMenu1);

        jMenu2.setMnemonic('E');
        jMenu2.setText("編集(E)");

        renameMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        renameMenuItem.setText("名前の変更");
        renameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(renameMenuItem);

        removeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        removeMenuItem.setText("選択を削除");
        removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(removeMenuItem);

        jMenuBar2.add(jMenu2);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
        moduleCustomizer.importConfigure();
        int index = jList.getSelectedIndex();
        nameVector.clear();
        db.getConfigNames( type, nameVector, tableName );
        nameVector.add( ModuleCustomizer.DEFAULT );
        jList.setListData( nameVector );
        jList.setSelectedIndex(index);
        jList.repaint();
    }//GEN-LAST:event_importMenuItemActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        int [] nums = jList.getSelectedIndices();
        if ( nums.length < 0 ) return;
        List<String> list = new ArrayList<String>();
        for ( int i : nums ) {
            String name = nameVector.get(i);
            if ( name.equals( ModuleCustomizer.DEFAULT) ) continue;
            list.add( name );
        }
        if ( list.isEmpty() ) return;
        moduleCustomizer.exportConfigure( list );
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void removeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMenuItemActionPerformed
      if ( jList.isSelectionEmpty() ) {
          JOptionPane.showMessageDialog(
              this,"削除する設定名を選択してください。" );
          return;
      }
      String message = "選択した設定を削除します。よろしいですか？";
      String title = "設定の削除の確認";
      int result = JOptionPane
          .showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION );
      if ( result == JOptionPane.NO_OPTION ) return;
      Object [] names = jList.getSelectedValues();
      for ( int i = 0; i < names.length; i++ ) {
          String name = (String)names[i];
          if ( name.equals( ModuleCustomizer.DEFAULT) ) continue;
          db.removeConfigProperties( name, type, tableName );
          nameVector.remove( name );
      }
      jList.setSelectedIndex( 0 );
      if ( jList.getModel().getSize() == 1 ) {
          jListHandler.valueChanged( null );
      }
      jList.repaint();
// TODO add your handling code here:
    }//GEN-LAST:event_removeMenuItemActionPerformed

    private void renameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameMenuItemActionPerformed
        int i = jList.getSelectedIndex();
        if ( i < 0 ) return;
        String name = nameVector.get(i);
        String newName = JOptionPane.showInputDialog(this,
            "「" + name + "」の新しい名前を入力してください。");
        if ( newName == null|| newName.trim().equals("") ) return; //中止
        boolean found = false;
        for ( String n : nameVector ) {
            if ( n .equals( newName ) ) { found = true; break; }
        }
        if ( found ) {
            JOptionPane.showMessageDialog(this,
                "「"+newName+"」は重複するものがすでに存在します。",
                "設定名入力エラー",JOptionPane.ERROR_MESSAGE );
            return;
        }
        if ( ModuleCustomizer.isIllegalName(newName) ) {
            JOptionPane.showMessageDialog(this,
                "<html>「" + newName + "」は使用できない文字が含まれて" +
                "いるか<br>長すぎるので別の名前を入力してください。</html>",
                "設定名入力エラー",JOptionPane.ERROR_MESSAGE );
            return;
        }
        nameVector.setElementAt(newName,i);
        jList.setSelectedIndex(i);
        jList.repaint();
        db.renameConfigProperties(name,type,newName, tableName );
// TODO add your handling code here:
    }//GEN-LAST:event_renameMenuItemActionPerformed

    // 整理チェックボックスON/OFFによる編集ボタンパネルの表示/非表示

    private void editCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCheckBoxActionPerformed
        BorderLayout layout = (BorderLayout)controlPanel.getLayout();
        if ( editCheckBox.isSelected() ) {
            setJMenuBar( jMenuBar2 );
            controlPanel.add( editButtonPanel, BorderLayout.CENTER );
            jList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        } else {
            layout.removeLayoutComponent( editButtonPanel );
            controlPanel.remove( editButtonPanel );
            setJMenuBar( null );
            jList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        }
        validate();
        repaint();

    }//GEN-LAST:event_editCheckBoxActionPerformed

    //中止ボタンが押された

    private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
        confName = null;
        dispose();
    }//GEN-LAST:event_abortButtonActionPerformed

  //設定名をひとつ上に移動
  private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
      int i= jList.getSelectedIndex();
      int max = nameVector.size();
      if(i <= 0 ) return;
      String name = nameVector.get(i-1);
      nameVector.setElementAt(nameVector.get(i),i-1);
      nameVector.setElementAt(name,i);
      jList.setSelectedIndex(i-1);
      jList.repaint();
      System.out.println( "type = " + type );
      System.out.println(" tableName = " + tableName );
      db.sortConfigProperties(nameVector,type, tableName );
  }//GEN-LAST:event_upButtonActionPerformed

  //設定名をひとつ下に移動
  private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
      int i = jList.getSelectedIndex();
      int max = nameVector.size();
      if((i < 0) || (i==(max-2))) return; //非選択または最終データを選択時は終了
      String name = nameVector.get(i+1);
      nameVector.setElementAt(nameVector.get(i),i+1);
      nameVector.setElementAt(name,i);
      jList.setSelectedIndex(i+1);
      jList.repaint();
      db.sortConfigProperties(nameVector,type, tableName );
  }//GEN-LAST:event_downButtonActionPerformed


  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
      int i = jList.getSelectedIndex();
      String name = (i < 0) ? "" : nameVector.get(i);
      confName = name.equals( ModuleCustomizer.DEFAULT ) ? "" : name;
      dispose();
  }//GEN-LAST:event_acceptButtonActionPerformed

  // JList上でのマウスダブルクリック
  private void jListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMouseClicked
      if ( evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1 ) {
          acceptButtonActionPerformed( null );
      }
  }//GEN-LAST:event_jListMouseClicked
  //メニューが選択

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private javax.swing.JButton acceptButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton downButton;
    private javax.swing.JPanel editButtonPanel;
    private javax.swing.JCheckBox editCheckBox;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JMenuItem renameMenuItem;
    private to.tetramorph.widget.ResizeIconPanel resizeIconPanel1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

}
