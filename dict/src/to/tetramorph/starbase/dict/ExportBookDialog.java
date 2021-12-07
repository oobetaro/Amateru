/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportBookDialog.java
 *
 * Created on 2009/01/04, 8:40:55
 */

package to.tetramorph.starbase.dict;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import to.tetramorph.util.AmateruBookFileFilter;
import to.tetramorph.util.ParentWindow;
import to.tetramorph.util.XMLFileFilter;

/**
 * 辞書保存時のフォーマットおよびオプションを設定するダイアログ。
 * @author 大澤義鷹
 */
class ExportBookDialog extends javax.swing.JDialog {
    private JFileChooser chooser;
    private AmateruBookFileFilter abf_filter;
    private XMLFileFilter xml_filter;
    JComponent [] distComponents;
    ButtonGroup formatButtonGroup = new ButtonGroup();
    private File file;
    /** Creates new form ExportBookDialog */
    public ExportBookDialog(java.awt.Window parent) {
        super( parent, Dialog.ModalityType.APPLICATION_MODAL );
        initComponents();
        chooser = new JFileChooser();
        abf_filter = new AmateruBookFileFilter();
        xml_filter = new XMLFileFilter();

        init();
    }
    private void dictEnabled( boolean b ) {
        for( JComponent c : distComponents ) {
            c.setEnabled(b);
        }
    }

    //パスワード保護のチェックボックスの値に応じて
    //パスワード入力欄のEnable/Disenabledを切り替える。
    private void passwordState() {
        boolean b = passwordCheckBox.isSelected();
        pwLabel1.setEnabled(b);
        pwLabel2.setEnabled(b);
        passwordField1.setEnabled(b);
        passwordField2.setEnabled(b);
    }

    //フォーマット選択ラジオボタンの選択状態に応じて、ファイルチューザーの
    //ファイルフィルターを切り替える
    private void setFileFilter() {
        //既存のフィルターを削除
        chooser.removeChoosableFileFilter(chooser.getFileFilter());
        if ( formatRadioButton1.isSelected() ) {
            chooser.setFileFilter(abf_filter);
        } else {
            chooser.setFileFilter(xml_filter);
        }
    }

    //各コンポーネントの初期化とイベントリスナのセットアップ
    private void init() {
        distComponents = new JComponent[] {
            distLabel,editableCheckBox,passwordCheckBox,
            passwordField1,passwordField2,pwLabel1,pwLabel2
        };
        formatButtonGroup.add(formatRadioButton2);
        formatButtonGroup.add(formatRadioButton1);
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ( evt.getSource() == formatRadioButton1 ) {
                    dictEnabled(true);
                    passwordState();
                } else {
                    dictEnabled(false);
                }
                setFileFilter();
                pathTextField.setText("");
            }
        };
        formatRadioButton2.addActionListener(al);
        formatRadioButton1.addActionListener(al);
        passwordCheckBox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                passwordState();
            }
        });
        dictEnabled( formatRadioButton1.isSelected() );
        passwordState();
        chooser.removeChoosableFileFilter(chooser.getFileFilter());
        setFileFilter();
        ParentWindow.setEscCloseOperation( this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                file = null;
                dispose();
            }
        });
    }

    public boolean isEditable() {
        return ! editableCheckBox.isSelected();
    }

    private boolean isCanonicalPassword() {
            char [] pw1 = passwordField1.getPassword();
            if ( pw1 == null || pw1.length < 6 ) return false;
            char [] pw2 = passwordField2.getPassword();
            if ( pw2 == null ) return false;
            return new String(pw1).equals(new String(pw2));
    }

    public File getFile() {
        return file;
    }
    public boolean isNeedPassword() {
        return passwordCheckBox.isSelected();
    }
    public String getPassword() {
        return new String(passwordField1.getPassword());
    }
    public static final int ABF_FILE = 0;
    public static final int XML_FILE = 1;
    public int getFileType() {
        if ( formatRadioButton1.isSelected() )
            return ABF_FILE;
        else if ( formatRadioButton2.isSelected() )
            return XML_FILE;
        return -1;
    }

    private static ExportBookDialog INSTANCE;
    /**
     * ダイアログを開く。決定・中止が押されたらダイアログを閉じ、
     * このダイアログのインスタンスを返す。インスタンスから、
     * 選択されたオプション情報を取得できる。
     * このダイアログはこのメソッドでのみ開くようにする。
     * コンストラクタは直接使わないこと。
     * @param owner 親となるウィンドウ。
     * @return このクラスのインスタンス。
     */
    public static ExportBookDialog show(Window owner) {
        System.out.println("ExportBookDialog EDT = " + SwingUtilities.isEventDispatchThread());
        if ( INSTANCE == null ) {
            INSTANCE = new ExportBookDialog(owner);
            INSTANCE.pack();
            INSTANCE.setLocationRelativeTo(owner);
        }
        INSTANCE.setVisible(true);
        return INSTANCE;
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

        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        formatRadioButton1 = new javax.swing.JRadioButton();
        formatRadioButton2 = new javax.swing.JRadioButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        distLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        pwLabel1 = new javax.swing.JLabel();
        passwordField1 = new javax.swing.JPasswordField();
        passwordField2 = new javax.swing.JPasswordField();
        editableCheckBox = new javax.swing.JCheckBox();
        pwLabel2 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        fileButton = new javax.swing.JButton();
        pathTextField = new javax.swing.JTextField();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        acceptButton = new javax.swing.JButton();
        canselButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("本のエクスポート");
        setResizable(false);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 1, 8));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("ファイルフォーマット");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel1, gridBagConstraints);

        formatRadioButton1.setSelected(true);
        formatRadioButton1.setText("アマテルブックファイル(ABF形式)");
        formatRadioButton1.setToolTipText("<html>\nアマテル専用の辞書フォーマットで書き出します。<br>\n暗号によるプロテクションも可能です。<br>\n</html>\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel1.add(formatRadioButton1, gridBagConstraints);

        formatRadioButton2.setText("XML形式");
        formatRadioButton2.setToolTipText("UTF-8によるプレインテキストのXMLファイルで書き出します。");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel1.add(formatRadioButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        distLabel.setText("辞書配布用オプション");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(distLabel, gridBagConstraints);

        passwordCheckBox.setText("パスワードで保護する");
        passwordCheckBox.setToolTipText("パスワードを知らないユーザは辞書を開く事ができなくなります。");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel2.add(passwordCheckBox, gridBagConstraints);

        pwLabel1.setText("パスワード");
        pwLabel1.setToolTipText("最低６文字以上のパスワードを指定してください。\n");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel2.add(pwLabel1, gridBagConstraints);

        passwordField1.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel2.add(passwordField1, gridBagConstraints);

        passwordField2.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel2.add(passwordField2, gridBagConstraints);

        editableCheckBox.setText("編集を禁止する");
        editableCheckBox.setToolTipText("<html>\n編集禁止で書き出したファイルを後から編集許可にすることはできません。<br>\nご注意ください。\n</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel2.add(editableCheckBox, gridBagConstraints);

        pwLabel2.setText("パスワード確認");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel2.add(pwLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel3.add(jPanel2, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        fileButton.setText("ファイル指定");
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });
        jPanel4.add(fileButton, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel4.add(pathTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jPanel4, gridBagConstraints);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        acceptButton.setMnemonic('Y');
        acceptButton.setText("保存(Y)");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });
        jPanel5.add(acceptButton);

        canselButton.setMnemonic('N');
        canselButton.setText("中止(N)");
        canselButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canselButtonActionPerformed(evt);
            }
        });
        jPanel5.add(canselButton);

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        String current = Config.usr.getProperty("currentPath", System.getProperty("user.home"));
        chooser.setCurrentDirectory(new File(current));
        int result = chooser.showSaveDialog(this);
        if ( result == JFileChooser.CANCEL_OPTION ) return;
        Config.usr.setProperty( "currentPath",
                chooser.getCurrentDirectory().getAbsolutePath());
        File f = chooser.getSelectedFile();
        String path = f.getPath();
        String temp = path.toLowerCase();
        if ( formatRadioButton1.isSelected() ) {
            if ( ! temp.matches(".*(\\.abf)$"))
                path += ".abf";
        } else {
            if ( ! temp.matches(".*(\\.xml)$"))
                path += ".xml";
        }
        pathTextField.setText(path);
    }//GEN-LAST:event_fileButtonActionPerformed

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        if ( passwordCheckBox.isSelected() ) {
            if ( ! isCanonicalPassword() ) {
                JOptionPane.showMessageDialog(this,
                        "パスワードに誤りがあります。\n" +
                        "パスワードは6文字以上で、二つのフィールドに\n" +
                        "同じパスワードが入力されていなければなりません。",
                        "パスワードの入力エラー",JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        File f = new File(pathTextField.getText());
        if ( f.exists() ) {
            int result = JOptionPane.showConfirmDialog(
                this,
                String.format("\"%s\"に上書きしてよろしいですか？",f.getPath()),
                "ファイルに保存",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.NO_OPTION ) return;
        } else {
            try {
                f.createNewFile();
                f.delete();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this, e.getMessage(),
                        "ファイル名入力エラー",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        this.file = f;
        dispose();
    }//GEN-LAST:event_acceptButtonActionPerformed

    private void canselButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselButtonActionPerformed
        file = null;
        dispose();
    }//GEN-LAST:event_canselButtonActionPerformed

//    /**
//    * @param args the command line arguments
//    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                ExportBookDialog dialog = new ExportBookDialog(new javax.swing.JFrame());
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JButton canselButton;
    private javax.swing.JLabel distLabel;
    private javax.swing.JCheckBox editableCheckBox;
    private javax.swing.JButton fileButton;
    private javax.swing.JRadioButton formatRadioButton1;
    private javax.swing.JRadioButton formatRadioButton2;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField1;
    private javax.swing.JPasswordField passwordField2;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel pwLabel1;
    private javax.swing.JLabel pwLabel2;
    // End of variables declaration//GEN-END:variables

}
