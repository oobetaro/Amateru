/*
 * DataInputDialog.java
 *
 * Created on 2006/07/04, 23:31
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.util.ParentWindow;

/**
 * NATAL,COMPOSIT,EVENTの各種Natalデータを入力したり編集したりするダイアログ。
 * 各種データはそれぞれ入力すべきパラメターが異なるが、ヒストリーとノートの入力は共通。
 * このダイアログは入力するデータタイプの数だけ専用のフォームパネルを用意し、
 * このダイアログにはめ込んで使う事ができきる。
 * 専用のフォームパネルはOccasionInputPanelをextendsして作成し、いくつかの抽象メソッド
 * を実装する。それをこのクラスのshowDialog()メソッドに渡すと、タベッドパネルの
 * 最初にそれが登録されダイアログが開く。入力結果はNatalで受け取ることができる。
 * 入力が中止されたときはnullが返る。
 *
 * ヒストリーデータはNatalオブジェクトから取得している。
 * 2011-7-29 レジストリの使用をやめた。
 * @author 大澤義鷹
 */
class DataInputDialog extends javax.swing.JDialog {
    private static Natal resultNatal = null; //入力結果受け渡し用
    private AbstractNatalInputPanel dataTypePanel = null;
    private HistoryEditPanel historyEditPanel1;
    //インスタンス作成禁止
    private DataInputDialog() { }

    //親がFrameの際のコンストラクタ
    private DataInputDialog( Frame parent, AbstractNatalInputPanel panel ) {
        super(parent, true);
        initComponents();
        init(panel);
    }

    //親がDialogの際のコンストラクタ
    private DataInputDialog( Dialog parent, AbstractNatalInputPanel panel ) {
        super(parent, true);
        initComponents();
        init(panel);
    }
    //初期化。タベッドパネルに切替式入力フォームをセットする
    private void init( AbstractNatalInputPanel panel ) {
        tabbedPane.add(panel,0);
        dataTypePanel = panel;
        historyEditPanel1 = new HistoryEditPanel();
        tabbedPane.addTab("ヒストリー(H)",historyEditPanel1);
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setMnemonicAt(1,KeyEvent.VK_R);
        tabbedPane.setMnemonicAt(2,KeyEvent.VK_P);
        tabbedPane.setMnemonicAt(3,KeyEvent.VK_H);
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aboutButton.doClick();
            }
        });
        tabbedPane.setTitleAt(0,panel.getName()+"(D)");
        tabbedPane.setMnemonicAt(0,KeyEvent.VK_D);
    }

    private void setTreePath(TreePath treePath) {
        if(treePath == null) {
            pathLabel.setText("");
            return;
        }
        pathLabel.setText("場所：/" + DBFactory.getPathString(treePath));
    }

    /**
     * ダイアログを開き、入力内容からNatalを返す。
     * @param dataTypePanel NatalInputPanelやEventInputPanelやCompositInputPanel。
     * @param natal    更新時に各入力フォームにセットするNatalオブジェクト。新規
     * ならnullを指定する。
     * @param parent 親となるコンポーネント
     * @param treePath 編集中のパスを指定する。
     * @param results ダイアログからの戻り値受け取り用配列で、要素数2の配列を指定
     *                する。メソッド実行後、[0]=「連続入力」がONなら1ちがうなら0。
     *                [1]=「即チャート表示」がONなら1ちがうなら0。
     * @return 新規入力または更新されたNatalオブジェクト
     */
    private static Natal showDialog( Component parent,
                                       AbstractNatalInputPanel dataTypePanel,
                                       Natal natal,
                                       String title,
                                       TreePath treePath,
                                       int [] results) {
        Window window = ParentWindow.getWindowForComponent(parent);
        final DataInputDialog dialog;
        if ( window instanceof Frame ) {
            dialog = new DataInputDialog((Frame)window,dataTypePanel);
        } else {
            dialog = new DataInputDialog((Dialog)window,dataTypePanel);
        }
        if ( results == null ) {
            dialog.repeatCheckBox.setEnabled( false );
            dialog.viewCheckBox.setEnabled( false );
        } else {
            dialog.repeatCheckBox.setSelected( results[0] == 1 );
        }
        if ( results != null ) {
            dialog.viewCheckBox.setSelected(
                Config.usr.getBoolean( "DataInputDialog.display_at_once", true ) );
        }
        dialog.setTitle( title );
        dialog.setTreePath( treePath );
        //ヒストリーとノートをセット
        if ( natal != null ) {
            dataTypePanel.setNatal( natal );
            if ( natal.getTransitPlace() != null ) {
                // 経過地が登録されているなら、placePanelにセットしヒストリーリス
                // トから経過地データは除去して、リストをディープコピーして編集
                // パネルに渡す。元ネタから削除してしまうと登録中止のとき経過地が
                // 除去されたままになる。
                Place p = natal.getTransitPlace();
                System.out.println("観測地 = " + p);
                dialog.placePanel.setPlace(natal.getTransitPlace());
                List<Transit> temp = new ArrayList<Transit>();
                for ( Transit t : natal.getHistory() )
                    if ( ! t.getName().equals("TRANSIT_PLACE") )
                        temp.add(new Transit(t));
                dialog.historyEditPanel1.setHistory(temp);
            } else {
                //経過地未登録のときは、ヒストリーリストをそのままわたす
                dialog.historyEditPanel1.setHistory(natal.getHistory());
            }
            dialog.noteTextArea.setText(natal.getNote());
        }
        dialog.setComponentOrientation( window.getComponentOrientation() );
        dialog.pack();
        dialog.setLocationRelativeTo( window );
        dataTypePanel.setFocus();
        dialog.setVisible( true );
        if ( results != null ) {
            results[0] = dialog.repeatCheckBox.isSelected() ? 1 : 0;
            results[1] = dialog.viewCheckBox.isSelected() ? 1 : 0;
        }
        return resultNatal;
    }

    /**
     * ネイタルデータの入力パネルを出しユーザに入力を促す。
     * @param parent 親となるコンポーネント
     * @param treePath 編集中のパスを指定する。
     * @param results ダイアログからの戻り値受け取り用配列で、要素数2の配列を指定
     *                する。メソッド実行後、[0]=「連続入力」がONなら1ちがうなら0。
     *                [1]=「即チャート表示」がONなら1ちがうなら0。
     * @return 新規入力または更新されたNatalオブジェクト
     */
    public static Natal showNatalDialog( Component parent,
                                           TreePath treePath,
                                           int [] results ) {
        AbstractNatalInputPanel panel = new NatalInputPanel();
        return showDialog( parent, panel, null, "ネイタルデータの登録",
                                                            treePath, results );
    }

    /**
     * イベントデータの入力パネルを出しユーザに入力を促す。
     * @param parent 親となるコンポーネント
     * @param treePath 編集中のパスを指定する。
     * @param results ダイアログからの戻り値受け取り用配列で、要素数2の配列を指定
     *                する。メソッド実行後、[0]=「連続入力」がONなら1ちがうなら0。
     *                [1]=「即チャート表示」がONなら1ちがうなら0。
     * @return 新規入力または更新されたNatalオブジェクト
     */
    public static Natal showEventDialog( Component parent,
                                           TreePath treePath,
                                           int [] results ) {
        AbstractNatalInputPanel panel = new EventInputPanel();
        return showDialog( parent, panel, null, "イベントデータの登録",
                            treePath, results );
    }

    /**
     * 既存のNatalの編集用ダイアログを開き編集入力を促す。
     * @param parent 親となるコンポーネント
     * @param natal データの入っている空ではないNatalオブジェクト。
     */
    public static Natal showEditDialog(Component parent,Natal natal,
        TreePath treePath) {
        if ( natal.getChartType().equals( Natal.NATAL ) ) {
            AbstractNatalInputPanel panel = new NatalInputPanel();
            return showDialog( parent, panel, natal,
                                "ネイタルデータの編集", treePath, null );
        } else if ( natal.getChartType().equals( Natal.EVENT ) ) {
            AbstractNatalInputPanel panel = new EventInputPanel();
            return showDialog( parent, panel, natal,
                                "イベントデータの編集", treePath, null );
        } else if ( natal.getChartType().equals(Natal.COMPOSIT) ) {
            AbstractNatalInputPanel panel = new CompositInputPanel();
            return showDialog( parent, panel, natal,
                                "コンポジットデータの編集", treePath, null);
        }
        return null;
    }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;

        buttonPanel = new javax.swing.JPanel();
        viewCheckBox = new javax.swing.JCheckBox();
        repeatCheckBox = new javax.swing.JCheckBox();
        acceptButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        notePanel = new javax.swing.JPanel();
        noteScrollPane = new javax.swing.JScrollPane();
        noteTextArea = new javax.swing.JTextArea();
        transitPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        placePanel = new to.tetramorph.starbase.PlacePanel();
        pathLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        viewCheckBox.setSelected(true);
        viewCheckBox.setText("\u5373\u30c1\u30e3\u30fc\u30c8\u8868\u793a");
        viewCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        viewCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        viewCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCheckBoxActionPerformed(evt);
            }
        });

        buttonPanel.add(viewCheckBox);

        repeatCheckBox.setText("\u9023\u7d9a\u767b\u9332");
        repeatCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        repeatCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        repeatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeatCheckBoxActionPerformed(evt);
            }
        });

        buttonPanel.add(repeatCheckBox);

        acceptButton.setMnemonic('Y');
        acceptButton.setText("\u767b\u9332(Y)");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(acceptButton);

        aboutButton.setMnemonic('N');
        aboutButton.setText("\u4e2d\u6b62(N)");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(aboutButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        notePanel.setLayout(new java.awt.GridLayout(1, 0));

        noteTextArea.setColumns(20);
        noteTextArea.setRows(5);
        noteScrollPane.setViewportView(noteTextArea);

        notePanel.add(noteScrollPane);

        tabbedPane.addTab("\u30ec\u30dd\u30fc\u30c8(R)", notePanel);

        transitPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8\u306e\u89b3\u6e2c\u5730\u3068\u30bf\u30a4\u30e0\u30be\u30fc\u30f3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        transitPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        transitPanel.add(placePanel, gridBagConstraints);

        tabbedPane.addTab("\u89b3\u6e2c\u5730(P)", transitPanel);

        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        pathLabel.setText("\u5834\u6240");
        pathLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));
        getContentPane().add(pathLabel, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void viewCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewCheckBoxActionPerformed
// TODO add your handling code here:
  }//GEN-LAST:event_viewCheckBoxActionPerformed

  private void repeatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repeatCheckBoxActionPerformed
// TODO add your handling code here:
  }//GEN-LAST:event_repeatCheckBoxActionPerformed

  private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
      resultNatal = null;
      dispose();
  }//GEN-LAST:event_aboutButtonActionPerformed
  // 登録ボタンが押されたら、Natalオブジェクトにパラメターをセット。
  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
      if(dataTypePanel.isCompletion()) {
          resultNatal = dataTypePanel.getNatal();
          //ノートとヒストリーはこのダイアログの担当
          resultNatal.setNote( noteTextArea.getText() );
          //
          List<Transit> list = historyEditPanel1.getHistory();
          if ( placePanel.isComplete() ) {
              // 経過地情報が入力されている場合は、ヒストリーリストに追加する
              Transit ev = new Transit();
              //タイムゾーンも変更されるので先にセット
              ev.setCalendar( new GregorianCalendar(), TimePlace.DATE_AND_TIME );
              ev.setPlace( placePanel.getPlace() ); //場所をセット
              // 名前をセット。これは予約語なので、ヒストリー名にユーザが使わな
              // いよう禁則処理を追加しなければならない。
              ev.setName("TRANSIT_PLACE");
              list.add(ev);
          }
          resultNatal.setHistory( list );
          if ( viewCheckBox.isEnabled() ) {
              Config.usr.setBoolean( "DataInputDialog.display_at_once",
                                        viewCheckBox.isSelected() );
          }
          dispose();
      }
  }//GEN-LAST:event_acceptButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JButton acceptButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel notePanel;
    private javax.swing.JScrollPane noteScrollPane;
    private javax.swing.JTextArea noteTextArea;
    private javax.swing.JLabel pathLabel;
    private to.tetramorph.starbase.PlacePanel placePanel;
    private javax.swing.JCheckBox repeatCheckBox;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel transitPanel;
    private javax.swing.JCheckBox viewCheckBox;
    // End of variables declaration//GEN-END:variables

}
