/*
 * JobSelectDialog.java
 *
 * Created on 2006/10/12, 23:27
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import to.tetramorph.util.ParentWindow;

/**
 * 職業選択ダイアログ。
 * データベースに登録されているOCCASION表の中のJOB列からDISTINCT抽出した職業リスト
 * から任意のものを選択することができる。また新しい職業名を入力することもできる。
 * 職業名を入力するための補助ツールのような位置づけ。すでに登録されている職業名
 * があり、それで事がたりるなら新しい職業名を用意するよりそれを使ったほうが能率的。
 * そういうときのために、すでに登録されている職業名一覧を用意してそこから入力できる
 * ように考えたもの。
 * このダイアログは、内部でHSQLDBに接続している。
 */
class JobSelectDialog extends javax.swing.JDialog {
    //決定ボタンが押されたとき、職業名が代入される
    private static String jobName; 
    //レイアウト選択のトグルボタングループ
    ButtonGroup layoutButtonGroup = new ButtonGroup(); 
    static final int [] JLIST_LAYOUT = {
        JList.VERTICAL, JList.VERTICAL_WRAP, JList.HORIZONTAL_WRAP
    };
    //親がダイアログの場合のコンストラクタ
    private JobSelectDialog(java.awt.Dialog parent) {
        super(parent,true);
        initComponents();
        init();
    }
    //親がフレームの場合のコンストラクタ
    private JobSelectDialog(java.awt.Frame parent) {
        super(parent,true);
        initComponents();
        init();
    }
    //初期化メソッド
    private void init() {
        jobName = null;
        setMinimumSize(new Dimension(356,200));
        setPreferredSize(new Dimension(356,300));
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        //新規入力フィールドで改行キーで入力完了させるイベント処理
        jobNameTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int code = (int)e.getKeyChar();
                if(code == KeyEvent.VK_ENTER) {
                    acceptButton.doClick();
                }
            }
        });
        //絞り込フィールドで改行キーで検索ボタンを押した事にするイベント処理
        searchTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int code = (int)e.getKeyChar();
                if(code == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
                }
            }
        });
        //JListがキーとマウスとキーによる選択のイベントを受け取れるようにする
        JobListHandler jobHandler = new JobListHandler();
        jobJList.addMouseListener(jobHandler);
        jobJList.addListSelectionListener(jobHandler);
        jobJList.addKeyListener(jobHandler);
        jobJList.setVisibleRowCount(0);
        layoutButtonGroup.add(layoutToggleButton1);
        layoutButtonGroup.add(layoutToggleButton2);
        layoutButtonGroup.add(layoutToggleButton3);
        layoutToggleButton1.setSelected(true);
        Enumeration<AbstractButton> enu = layoutButtonGroup.getElements();
        for(int i=0; enu.hasMoreElements(); i++) {
            AbstractButton button = enu.nextElement();
            LayoutButtonHandler bh = new LayoutButtonHandler(i);
            button.addActionListener(bh);
        }
        search("");
    }
    //JListのレイアウト方式を選択するトグルボタンのイベントハンドラ
    private class LayoutButtonHandler implements ActionListener {
        int num;
        LayoutButtonHandler(int num) {
            this.num = num;
        }
        public void actionPerformed(ActionEvent evt) {
            jobJList.setLayoutOrientation(JLIST_LAYOUT[num]);
            jobJList.revalidate();
        }
    }
    
    /**
     * ダイアログを開いて職業名を選択または入力させるダイアログ。
     * @param parent 呼び出し元のComponent。
     * @return 決定の場合はその職業名、入力が中止された場合はnull。
     */
    public static String showDialog(Component parent) {
        Window window = ParentWindow.getWindowForComponent(parent);
        JobSelectDialog dialog;
        if(window instanceof Frame) {
            dialog = new JobSelectDialog((Frame)window);
        } else {
            dialog = new JobSelectDialog((Dialog)window);
        }
        dialog.setComponentOrientation(window.getComponentOrientation());
        dialog.pack();
        dialog.setLocationRelativeTo(window); //親コンポーネントに対してセンタリング
        dialog.setVisible(true);
        return jobName;
    }
    
    //検索し結果をJListに表示
    private void search(String key) {
        if(key == null) key = "";
        DB db = DBFactory.getInstance();
        Vector<String> vector = db.searchJobs( key );
        jobJList.setListData(vector);
    }
    
    // JListがクリックされたりカーソル選択されたり改行がタイプされたときのハンドラ
    private class JobListHandler extends MouseAdapter 
                               implements ListSelectionListener,KeyListener {
        //リストの項目がクリックされたとき
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() >= 1) {
                String job = (String)jobJList.getSelectedValue();
                if(job != null) jobNameTextField.setText(job);
            }
            if(e.getClickCount() == 2) {
                acceptButton.doClick();
            }
        }
        //リストがカーソルで選択されたとき
        public void valueChanged(ListSelectionEvent e) {
            String job = (String)jobJList.getSelectedValue();
            if(job != null) jobNameTextField.setText(job);
        }
        //リスト上で改行がタイプされたとき
        public void keyTyped(KeyEvent e) {
            int code = (int)e.getKeyChar();
            if(code == KeyEvent.VK_ENTER) {
                String job = (String)jobJList.getSelectedValue();
                if(job != null) jobNameTextField.setText(job);
                //新規入力のフィールドにフォーカスを移動
                jobNameTextField.requestFocus();
            }
        }
        public void keyPressed(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel controlPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel1;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JPanel layoutButtonPanel;
    javax.swing.JPanel panel;

    controlPanel = new javax.swing.JPanel();
    acceptButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    panel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jobJList = new javax.swing.JList();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    searchTextField = new javax.swing.JTextField();
    searchButton = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    jobNameTextField = new javax.swing.JTextField();
    layoutButtonPanel = new javax.swing.JPanel();
    layoutToggleButton1 = new javax.swing.JToggleButton();
    layoutToggleButton2 = new javax.swing.JToggleButton();
    layoutToggleButton3 = new javax.swing.JToggleButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("\u8077\u696d\u9078\u629e");
    acceptButton.setMnemonic('Y');
    acceptButton.setText("\u6c7a\u5b9a(Y)");
    acceptButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        acceptButtonActionPerformed(evt);
      }
    });

    controlPanel.add(acceptButton);

    cancelButton.setMnemonic('N');
    cancelButton.setText("\u4e2d\u6b62(N)");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });

    controlPanel.add(cancelButton);

    getContentPane().add(controlPanel, java.awt.BorderLayout.SOUTH);

    panel.setLayout(new java.awt.BorderLayout());

    jobJList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 1));
    jobJList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jobJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane1.setViewportView(jobJList);

    panel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 1, 1, 1));
    jLabel1.setText("\u7d5e\u308a\u8fbc\u307f\u30ad\u30fc\u30ef\u30fc\u30c9");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    jPanel1.add(jLabel1, gridBagConstraints);

    searchTextField.setColumns(12);
    searchTextField.setToolTipText("\u6539\u884c\u3067\u691c\u7d22");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel1.add(searchTextField, gridBagConstraints);

    searchButton.setMnemonic('S');
    searchButton.setText("\u691c\u7d22(S)");
    searchButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        searchButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel1.add(searchButton, gridBagConstraints);

    jLabel2.setText("\u8077\u696d\u540d\u5165\u529b");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    jPanel1.add(jLabel2, gridBagConstraints);

    jobNameTextField.setColumns(12);
    jobNameTextField.setToolTipText("\u6539\u884c\u3067\u6c7a\u5b9a");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel1.add(jobNameTextField, gridBagConstraints);

    panel.add(jPanel1, java.awt.BorderLayout.SOUTH);

    layoutButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 4));

    layoutToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/virtical.png")));
    layoutToggleButton1.setMnemonic('1');
    layoutToggleButton1.setToolTipText("\u5782\u76f4\u306b\u4e00\u5217\u306b\u6574\u5217");
    layoutToggleButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
    layoutButtonPanel.add(layoutToggleButton1);

    layoutToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/virtical_wrap.png")));
    layoutToggleButton2.setMnemonic('2');
    layoutToggleButton2.setToolTipText("\u5782\u76f4\u306b\u4e26\u3079\u6298\u308a\u8fd4\u3059");
    layoutToggleButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
    layoutButtonPanel.add(layoutToggleButton2);

    layoutToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/horizontal_wrap.png")));
    layoutToggleButton3.setMnemonic('3');
    layoutToggleButton3.setToolTipText("\u6c34\u5e73\u306b\u4e26\u3079\u6298\u308a\u8fd4\u3059");
    layoutToggleButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
    layoutButtonPanel.add(layoutToggleButton3);

    panel.add(layoutButtonPanel, java.awt.BorderLayout.NORTH);

    getContentPane().add(panel, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  //検索ボタンのイベント処理
  private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    search(searchTextField.getText());
  }//GEN-LAST:event_searchButtonActionPerformed
  //中止ボタンのイベント処理
  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed
  //決定ボタンのイベント処理
  private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
    String job = jobNameTextField.getText();
    if(job == null) job = "";
    job = job.trim();
    if(job.equals("")) {
      JOptionPane.showMessageDialog(this,"職業名を入力してください",
      "職業名入力エラー", JOptionPane.ERROR_MESSAGE );
      return;
    }
    jobName = job;
    dispose();
  }//GEN-LAST:event_acceptButtonActionPerformed
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton acceptButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JList jobJList;
  private javax.swing.JTextField jobNameTextField;
  private javax.swing.JToggleButton layoutToggleButton1;
  private javax.swing.JToggleButton layoutToggleButton2;
  private javax.swing.JToggleButton layoutToggleButton3;
  private javax.swing.JButton searchButton;
  private javax.swing.JTextField searchTextField;
  // End of variables declaration//GEN-END:variables
  
}
