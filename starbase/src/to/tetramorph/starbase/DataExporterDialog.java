/*
 * DataExporterDialog.java
 *
 * Created on 2007/01/27, 7:12
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.sql.Statement;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.util.CSVFileFilter;
import to.tetramorph.util.CSVWriter;
import static to.tetramorph.starbase.lib.BirthFileConst.*;
import to.tetramorph.util.ParentWindow;
/**
 * DBのテーブルに格納されている出生データをCSV形式のファイルで出力する。
 * ファイルのエンコーディングはUTF-16LEで、ファイル先頭にはBOMがつく。
 */
class DataExporterDialog extends javax.swing.JDialog {

    Connection con = null;
    String errorMessage = null;
    File file;
    boolean stop = false;
    boolean start = false;
    Frame owner;
    TreePath exportPath;
    static JFileChooser chooser = null;
    static {
        chooser = new JFileChooser();
        chooser.setFileFilter(new CSVFileFilter());
        chooser.setDialogTitle("エキスポートするファイルの指定");
        chooser.setAcceptAllFileFilterUsed(false);
    }
    /**
     * Creates new form DataExporterDialog
     */
    private DataExporterDialog(java.awt.Frame owner,File file,TreePath path) {
        super(owner, true);
        this.owner = owner;
        this.exportPath = path;
        initComponents();
        //setKeyListener();
        ParentWindow.setEscCloseOperation(this, new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                canselButton.doClick();
            }
        });
        this.file = file;
        fileLabel.setText(file.getName());
        pack();
        setLocationRelativeTo(owner);
        con = DBFactory.getInstance().getConnection();
    }
    
    //プログレスバーを更新 (キューに入れるのでかならずnewで別々のインスタンスと
    //しなければならない。
    class Bar extends Thread {
        int count;
        Bar(int count) {
            this.count = count+1;
        }
        public void run() {
            progressBar.setValue(count+1);
        }
    }
    
    // メッセージラベルに文字を書く
    class Messenger extends Thread {
        String value;
        Messenger(String value) {
            this.value = value;
        }
        public void run() {
            messageLabel.setText(value);
        }
    }
    
    // Exporterスレッド終了時に呼び出される
    class Closer implements Runnable {
        public void run() {
            if(stop) {
                StringBuffer sb = new StringBuffer();
                sb.append("<html>エクスポートは中断されました。");
                if(errorMessage != null) {
                    sb.append("<br><font color='RED'>エラーメッセージ：<br>");
                    sb.append(errorMessage);
                    sb.append("</font>");
                }
                sb.append("</html>");
                JOptionPane.showMessageDialog(owner,sb.toString(),
                    "エクスポート",JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(owner,"エクスポート成功。",
                    "エクスポート",JOptionPane.PLAIN_MESSAGE);
            }
            dispose();
        }
    }
    
    /**
     * プログラム実行。これはｲﾍﾞﾝﾄﾃﾞｨｽﾊﾟｯﾁｽﾚｯﾄﾞではないので注意
     */
    class Exporter extends Thread {
        public void run() {
            if ( exportPath == null ) run1();
            else run2();
        }
        // 全データをエクスポート。
        void run1() {
            CSVWriter csvWriter = null;
            ResultSet res = null;
            ResultSet idResult = null;
            Statement stmt = null;
            float div = 0;
            int max = 0;
            try {
                stmt = con.createStatement();
                //出力するID総数を求める
                res = stmt.executeQuery(
                    "SELECT " +
                    "  COUNT(DISTINCT OCCASION.ID) " +
                    "FROM " +
                    "  OCCASION,TREEPATH " +
                    "WHERE " +
                    "  OCCASION.ID = TREEPATH.ID AND " +
                    "  TREEPATH.PATH <> 'ごみ箱'");
                if ( res.next() ) {
                    max = res.getInt(1);
                    div = (float)max / 100f;
                    SwingUtilities.invokeLater(
                        new Messenger("データ総数 : " + max +"件") );
                    idResult = stmt.executeQuery(
                        "SELECT " +
                        "  DISTINCT OCCASION.ID " +
                        "FROM " +
                        "  OCCASION,TREEPATH " +
                        "WHERE " +
                        "  OCCASION.ID = TREEPATH.ID AND " +
                        "  TREEPATH.PATH <> 'ごみ箱'");
                    csvWriter = new CSVWriter(file,'\t',"UTF-16LE");
                    //ByteOrderMarkの出力。これがないとExcelで正しく読めない。
                    csvWriter.write(BOM); 
                    csvWriter.writeCSV(HEADDER);
                    for(int i=1; idResult.next(); i++) {
                        if(stop) break; //打ち切り要求があれば脱出
                        int count = (int)((float)i / div);
                        if ( progressBar.getValue() < count ) {
                            //ﾌﾟﾛｸﾞﾚｽﾊﾞｰ更新
                            SwingUtilities.invokeLater( new Bar(count) ); 
                        }
                        int id = idResult.getInt("ID");
                        Natal natal = DBFactory.getInstance().getNatal(id);
                        String [] buf = natal.getCSV();
                        buf[TREEPATH] = getTreePathString(id);
                        csvWriter.writeCSV(buf);
                        if(stop) {
                            errorMessage = "中断されました";
                            break;
                        }
                    }
                } else {
                    errorMessage = "データがありません";
                    stop = true;
                }
            } catch ( Exception e ) {
                errorMessage = e.getMessage();
                e.printStackTrace();
                stop = true;
            } finally {
                DBFactory.getInstance().close( stmt, res, idResult );
                try { csvWriter.close(); } catch (Exception e) { }
            }
            SwingUtilities.invokeLater(new Closer());
        }
        //部分エクスポート
        void run2() {
            System.out.println("部分エクスポート開始");
            CSVWriter csvWriter = null;
            ResultSet res = null;
            ResultSet idResult = null;
            //Statement stmt = null;
            PreparedStatement ps = null;
            DB db = DBFactory.getInstance();
            float div = 0;
            int max = 0;
            try {
                //出力するID総数を求める
                ps = con.prepareStatement(                    
                    "SELECT " +
                    "  COUNT(DISTINCT OCCASION.ID) " +
                    "FROM " +
                    "  OCCASION,TREEPATH " +
                    "WHERE " +
                    "  OCCASION.ID = TREEPATH.ID AND " +
                    "  TREEPATH.PATH <> 'ごみ箱' AND " +
                    "  TREEPATH.PATH LIKE ?");
                String pathString = DBFactory.getPathString(exportPath);
                ps.setString(1,pathString + "%" );
                res = ps.executeQuery();
                ps.close();
                if ( res.next() ) {
                    max = res.getInt(1);
                    div = (float)max / 100f;
                    SwingUtilities.invokeLater(
                        new Messenger("データ総数 : " + max +"件") );
                    ps = con.prepareStatement(
                        "SELECT " +
                        "  DISTINCT OCCASION.ID " +
                        "FROM " +
                        "  OCCASION,TREEPATH " +
                        "WHERE " +
                        "  OCCASION.ID = TREEPATH.ID AND " +
                        "  TREEPATH.PATH <> 'ごみ箱' AND " +
                        "  TREEPATH.PATH LIKE ?");
                    ps.setString(1,pathString + "%");
                    idResult = ps.executeQuery();
                    csvWriter = new CSVWriter(file,'\t',"UTF-16LE");
                    //ByteOrderMarkの出力。これがないとExcelで正しく読めない。
                    csvWriter.write(BOM); 
                    csvWriter.writeCSV(HEADDER);
                    for(int i=1; idResult.next(); i++) {
                        if(stop) break; //打ち切り要求があれば脱出
                        int count = (int)((float)i / div);
                        if ( progressBar.getValue() < count ) {
                            //ﾌﾟﾛｸﾞﾚｽﾊﾞｰ更新
                            SwingUtilities.invokeLater( new Bar(count) ); 
                        }
                        int id = idResult.getInt("ID");
                        Natal natal = db.getNatal(id);
                        String [] buf = natal.getCSV();
                        buf[TREEPATH] = getTreePathString2(id,pathString);
                        csvWriter.writeCSV(buf);
                        if(stop) {
                            errorMessage = "中断されました";
                            break;
                        }
                    }
                } else {
                    errorMessage = "データがありません";
                    stop = true;
                }
            } catch ( Exception e ) {
                errorMessage = e.getMessage();
                e.printStackTrace();
                stop = true;
            } finally {
                DBFactory.getInstance().close( ps, res, idResult );
                try { csvWriter.close(); } catch (Exception e) { }
            }
            SwingUtilities.invokeLater(new Closer());
        }
    }
    /**
     * TREEPATH表に問い合わせて指定されたIDをもつパス(複数)の情報を返す。
     * ところで同一IDでも複数のフォルダに登録されているNatalデータが存在する。
     * その場合、複数のパスがカンマで区切られた文字列として返る。
     *
     * Natal.getCSV()でえられる配列はTREEPATHの項目は空になっている。
     * その部分を、このメソッドで求めてセットする。
     * @param id 取得するNatalデータのID。
     * @return "a/b/c,x/y/z"などとカンマで区切られ列挙されたパス文字列。
     */
    private String getTreePathString(int id) throws SQLException {
        //ゴミ箱のパスは除外する。
        PreparedStatement ps = con.prepareStatement(
            "SELECT PATH FROM TREEPATH WHERE ID=? AND PATH <> 'ごみ箱'");
        ps.setInt(1,id);
        ResultSet res = ps.executeQuery();
        StringBuffer sb = new StringBuffer();
        while(res.next()) {
            sb.append(res.getString("PATH"));
            sb.append(",");
        }
        try { res.close(); } catch( Exception e ) { }
        try { ps.close(); } catch( Exception e ) { }
        if ( sb.length() > 0 ) sb.deleteCharAt( sb.length() - 1 );
        return sb.toString();
    }
    
    /**
     * TREEPATH表に問い合わせて指定されたIDをもつパス(複数)の情報を取得し、
     * エクスポート指定されたパスの末尾フォルダの名前にリネームして返す。
     * 部分エクスポートでたとえば"a/b/c"というフォルダが指定された場合、
     * CSVに書き出すときのパスは"c"のみ。"a/b"はカットされる。
     * ところで同一IDでも複数のフォルダに登録されているNatalデータが存在する。
     * "a/b/c"と"x/y"両方のフォルダに入っていることになっているデータ。
     * そういう場合エクスポートパスは"a/b/c"が指定されているのだから、"x/y"は
     * リストから除外して返す。
     * "a/b/c"と"a/b/c/d"の両方に登録されている場合は、dはa/b/cの中に含まれる
     * から除外しない。
     * @param id 取得するNatalデータのID。
     * @param exportPath エクスポートするパス名。たとえば"a/b/c"。
     * @return "a/b/c,a/b/c/d"などとカンマで区切られ列挙されたパス文字列。
     */
    private String getTreePathString2(int id,String exportPath ) 
                                                         throws SQLException {
        String s = getTreePathString(id);
        String [] paths = s.split(",");
        //エクスポートパスの最後のフォルダ名をhedに取り出す
        int index = exportPath.lastIndexOf("/");
        String hed = ( index >= 0 ) ? 
            exportPath.substring(index+1) : exportPath;
        System.out.println("hed = " + hed);
        StringBuilder sb = new StringBuilder();
        for ( int i=0; i<paths.length; i++ ) {
            String path = paths[i];
            System.out.println("path["+i+"] = " + path);
            if ( path.startsWith( exportPath ) ) {
                // aaa/bbbとあるとき/bbbとなる。
                path = path.substring( exportPath.length() );
                String exPath = ( path.length() > 0) ? hed + path : hed;
                sb.append( exPath );
                sb.append(",");
            }
        }
        if ( sb.length() > 0 )
            sb.deleteCharAt( sb.length() -1 ); //末尾カンマを落とす
        return sb.toString();
    }
    /**
     * 出力先ファイルをファイルチューザーを開いてユーザに入力させる。
     * 正しく入力された場合はFileを返し、入力されなかったばあいはnullを返す。
     */
    static File getOutputFile(Component owner) {
        int result = chooser.showSaveDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) return null; //選択が中止された
        File  file = chooser.getSelectedFile();
        //拡張子を入力しなかったケース
        if(! file.toString().toLowerCase().endsWith(".csv"))
            file = new File(file.getAbsolutePath().concat(".csv"));
        if(file.exists()) {
            String message = file.getName();
            result = JOptionPane.showConfirmDialog(
                owner,
                file.getName() + 
                "\nこのファイルは既に存在します。\n上書きしてよろしいですか？",
                "エクスポートの警告",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if(result != JOptionPane.YES_OPTION) return null;
        }
        return file;
    }
    
    /**
     * すべてのバースデータをエキスポートする。ファイルチューザーが開きファイル名
     * の入力をユーザに促し、プログレスバーで進行状況を表示しながらOCCASION表の
     * データをCSV形式でファイルに出力する。
     * かならずEDTからアクセスすること。
     * @param owner 親フレーム
     */
    public static void exportAll(final Frame owner) {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        final File file = getOutputFile(owner);
        if(file == null) return;
        new DataExporterDialog(owner,file,null).setVisible(true);
    }
    
    /**
     * 指定されたフォルダにあるバースデータをエクスポートする。
     * ルートフォルダを指定すれば、exportAll()と同じ結果になるが、exportAll()の
     * ほうが少々効率が良い。
     * かならずEDTからアクセスすること。
     * @param owner 親フレーム
     * @param path  バックアップするデータベース上のパス
     */
    public static void export(final Frame owner,final TreePath path) {
        assert SwingUtilities.isEventDispatchThread(): "Not EDT";
        final File file = getOutputFile(owner);
        if(file == null) return;
        new DataExporterDialog(owner,file,path).setVisible(true);
    }
//    /**
//     * テスト
//     */
//    public static void main(String args[]) {
//        System.setProperty("nodb","true");
////        exportAll(null); // EDTではないのでassertによりエラーが出る
//        SwingUtilities.invokeLater(new Runnable() {
//          public void run() {
//            exportAll(null);
//          }
//        });
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel buttonPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;

    jPanel1 = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    fileLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();
    buttonPanel = new javax.swing.JPanel();
    startButton = new javax.swing.JButton();
    canselButton = new javax.swing.JButton();
    messageLabel = new javax.swing.JLabel();

    getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("\u57fa\u790e\u30c7\u30fc\u30bf\u306e\u30a8\u30af\u30b9\u30dd\u30fc\u30c8");
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
    jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jLabel1.setText("\u51fa\u529b\u30d5\u30a1\u30a4\u30eb :");
    jPanel2.add(jLabel1);

    fileLabel.setText("jLabel2");
    jPanel2.add(fileLabel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
    jPanel1.add(jPanel2, gridBagConstraints);

    progressBar.setPreferredSize(new java.awt.Dimension(200, 16));
    progressBar.setStringPainted(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    jPanel1.add(progressBar, gridBagConstraints);

    startButton.setText("\u30b9\u30bf\u30fc\u30c8");
    startButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        startButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(startButton);

    canselButton.setText("\u4e2d\u6b62");
    canselButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        canselButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(canselButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
    jPanel1.add(buttonPanel, gridBagConstraints);

    messageLabel.setText("<html> \u30d0\u30fc\u30b9\u30c7\u30fc\u30bf\u306e\u30a8\u30af\u30b9\u30dd\u30fc\u30c8\u3092\u958b\u59cb\u3057\u307e\u3059\u3002<br> \u30b9\u30bf\u30fc\u30c8\u30dc\u30bf\u30f3\u3092\u62bc\u3057\u3066\u304f\u3060\u3055\u3044\u3002</html>");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
    jPanel1.add(messageLabel, gridBagConstraints);

    getContentPane().add(jPanel1);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // ×ボタンが押されたとき
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

      canselButton.doClick();
      
  }//GEN-LAST:event_formWindowClosing
  // スタートボタンが押された
  private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
      
      startButton.setEnabled(false);
      start = true;
      new Exporter().start();
      
  }//GEN-LAST:event_startButtonActionPerformed
  // 中止ボタンが押された。
  private void canselButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselButtonActionPerformed
      
      if ( ! start ) { //まだ開始してないときに中止された場合
          dispose();
      } else {
          stop = true; //フラグを立てて実行中のスレッドに気づかせる
      }
      
  }//GEN-LAST:event_canselButtonActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton canselButton;
  private javax.swing.JLabel fileLabel;
  private javax.swing.JLabel messageLabel;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JButton startButton;
  // End of variables declaration//GEN-END:variables
  
}
