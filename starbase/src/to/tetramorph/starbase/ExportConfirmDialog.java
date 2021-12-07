/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportConfirmDialog.java
 *
 * Created on 2009/11/02, 8:53:12
 */

package to.tetramorph.starbase;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import to.tetramorph.util.ParentWindow;
import to.tetramorph.util.Preference;

/**
 * スキンと計算条件の設定情報をエクスポートするときの確認用ダイアログ。
 * ファイルチューザーのみの仕様にしていたら誤解を招いたケースがあり、
 * その対策。<br>
 * 　ModuleCustomizerから、必要なパラメターを与えられ呼び出される。<br>
 * 　このダイアログの中で、ファイル保存動作が行われる。<br>
 * 保存先フォルダのパスは、モジュールのフルパッケージ名をキーにして登録される。
 * つまりモジュール事に保管先のフォルダを記憶できる仕様。<br>
 * 　上書き保存のチェックボックスにチェックが入っていると、上書き保存の際に、
 * いちいち確認動作をしない。入っていないときは、確認ダイアログを開いて、
 * ユーザに問いあわせる。
 * 使い方<br>
 * <pre>
 * ExportConfirmDialog dialog =
 *      new ExportConfirmDialog(parent,title,className,nameList);
 * dialog.setVisible(true);
 * 注意：かならずEDTで実行すること。
 * </pre>
 * 2011-07-29 レジストリの使用をやめた。
 * @author 大澤義孝
 */
public class ExportConfirmDialog extends javax.swing.JDialog {
    JFileChooser fileChooser;
    List<Preference> prefList; //保存する複数の設定のリスト
    Dialog parent;
    String moduleClassName;    //モジュールのフルパッケージ名
    /**
     * オブジェクトを作成する。
     * @param parent 親のダイアログ
     * @param title タイトル
     * @param nameList 設定名リスト
     */
    public ExportConfirmDialog( Dialog parent,
                                    String title,
                                    String moduleClassName,
                                    List<Preference> prefList) {
        super(parent, true);
        this.prefList = prefList;
        this.parent = parent;
        this.moduleClassName = moduleClassName;
        System.out.println("#### Module Class Name = " + moduleClassName );
        initComponents();
        setTitle( title );
        StringBuilder sb = new StringBuilder();
        for ( Preference p : prefList )
            sb.append( p.getProperty("specificName") ).append( "\n" );
        sb.append("以上の設定をファイルに保存します。");
        textPane.setText(sb.toString());
        this.getRootPane().setDefaultButton(saveButton);
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("設定をエクスポートするフォルダを指定して下さい");
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        fileChooser.setMultiSelectionEnabled( false );

        String currentPath = Config.usr.getProperty( moduleClassName,
                fileChooser.getCurrentDirectory().getAbsolutePath() );
        fileChooser.setCurrentDirectory(new File(currentPath));
        folderTextField.setText(currentPath);
        String overwrite = Config.usr.getProperty("FileOverWrite","0");
        overwriteCheckBox.setSelected(overwrite.equals("1"));
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                canselButton.doClick();
            }
        });
        pack();
        setLocationRelativeTo(parent);
    }
    /**
     * 保存ボタンが押されたとき呼び出され、ファイルに設定を書き出す。
     * その過程はStringBuilderに書き出され、処理終了後、textPaneにセットされる。
     * 出力先フォルダはfolderTextFieldから取得する。
     * 設定情報（複数）はprefListに格納されているものとする。
     * @return 処理過程のメッセージを格納したStringBuilderオブジェクト。
     */
    StringBuilder save() {
        StringBuilder report = new StringBuilder();
        File dir = new File(folderTextField.getText());
        FileOutputStream fis = null;
        for ( int i = 0; i < prefList.size(); i++ ) {
            Preference pf = prefList.get(i);
            File expoFile = new File( dir, pf.getProperty("specificName") + ".xml");
            // 保存のまえに既存ファイルに上書きになるかどうかの検査
            if ( expoFile.exists() && ! overwriteCheckBox.isSelected() ) {
                int res = JOptionPane.showConfirmDialog( parent,
                          String.format(
                              "「%s」はすでに存在します。上書きしますか？",
                              expoFile.getName() ),
                          "保存の確認",
                          JOptionPane.YES_NO_CANCEL_OPTION );
                if ( res == JOptionPane.NO_OPTION ) continue;
                if ( res == JOptionPane.CANCEL_OPTION ) {
                    report.append("ユーザによって処理は中断されました。");
                    break;
                }
            }
            //保存
            try {
                fis = new FileOutputStream( expoFile );
                pf.storeToXML( fis,"SpecificData" );
                report.append("保存した：").append(expoFile.getPath()).append("\n");
            } catch ( IOException e ) {
                report.append("保存失敗：").append(e.getLocalizedMessage()).append("\n");
            } finally {
                try { fis.close(); } catch ( Exception e ) { }
            }
        }
        return report;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        folderButton = new javax.swing.JButton();
        folderTextField = new javax.swing.JTextField();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        overwriteCheckBox = new javax.swing.JCheckBox();
        saveButton = new javax.swing.JButton();
        canselButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.BorderLayout(0, 2));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        folderButton.setText("保存先変更");
        folderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(folderButton, gridBagConstraints);

        folderTextField.setEditable(false);
        folderTextField.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(folderTextField, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 120));

        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(textPane);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        overwriteCheckBox.setText("すべて上書き保存");
        overwriteCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overwriteCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(overwriteCheckBox, gridBagConstraints);

        saveButton.setMnemonic('Y');
        saveButton.setText("保存(Y)");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(saveButton, gridBagConstraints);

        canselButton.setMnemonic('N');
        canselButton.setText("中止(N)");
        canselButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canselButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel4.add(canselButton, gridBagConstraints);

        getContentPane().add(jPanel4, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        StringBuilder sb = save();
        textPane.setText(sb.toString());
        saveButton.setEnabled(false);
        canselButton.setText("了解(N)");
        //dispose();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void canselButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselButtonActionPerformed
        dispose();
    }//GEN-LAST:event_canselButtonActionPerformed

    private void folderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderButtonActionPerformed
        int result = fileChooser.showDialog(super.getParent(), "選択");
        if ( result != JFileChooser.APPROVE_OPTION ) return;
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if ( ! fileChooser.getSelectedFile().exists() ) {
            JOptionPane.showMessageDialog(super.getParent(),
                    "指定されたフォルダ“" + path
                    + "”が存在しないので変更できません。", "エラー",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }
        folderTextField.setText(path);
        Config.usr.setProperty( moduleClassName, path );
    }//GEN-LAST:event_folderButtonActionPerformed

    private void overwriteCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overwriteCheckBoxActionPerformed

        Config.usr.setProperty( "FileOverWrite",
                overwriteCheckBox.isSelected() ? "1" : "0" );

    }//GEN-LAST:event_overwriteCheckBoxActionPerformed

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                List<String> list = new ArrayList<String>();
//                for ( int i=0; i<20; i++) {
//                    list.add("アルカイック・ブラック"+i);
//                }
//                ExportConfirmDialog dialog = new ExportConfirmDialog(null, "設定保存",list);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    @Override
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton canselButton;
    private javax.swing.JButton folderButton;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox overwriteCheckBox;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables

}
