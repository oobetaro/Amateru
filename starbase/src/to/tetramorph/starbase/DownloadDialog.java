/*
 * DownloadDialog.java
 *
 * Created on 2007/01/27, 7:12
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import static to.tetramorph.starbase.lib.BirthFileConst.*;
import to.tetramorph.util.ParentWindow;
/**
 * 指定されたファイルをネットワークからダウンロードする。
 * ダウンロード過程をプログレスバーで表示する。
 * 途中で中断された場合、ダウンロード中のファイルは削除する。
 */
class DownloadDialog extends javax.swing.JDialog {
    public static final int DOWNLOAD_COMPLEATE = 0;
    public static final int DOWNLOAD_ABORT     = 1;
    public static final int DOWNLOAD_ERROR     = 2;
    String ERROR_MESSAGE = null;
    File file;
    boolean stop  = false;
    int result     = DOWNLOAD_COMPLEATE;
    Frame owner;
    URL url;
    /**
     * @param owner 親フレーム
     * @param url   ダウンロードするファイル名 "http://tetramorph.to/test.zip"等
     * @param file  ダウンロードしたデータを格納するファイル
     * @param title タイトルバーに出す文字列
     * @param message ダイアログ内に表示するメッセージ(HTML可能)
     */
    private DownloadDialog( Frame owner,
                             URL url,
                             File file,
                             String title,
                             String message) {
        super(owner, true);
        this.owner = owner;
        this.url = url;
        this.file = file;
        initComponents();
        setTitle(title);
        messageLabel.setText( message );
        ParentWindow.setEscCloseOperation(this,new AbstractAction("了解") {
            public void actionPerformed(ActionEvent evt) {
                canselButton.doClick();
            }
        });
        pack();
        setLocationRelativeTo(owner);
        new Downloader().start();
        setVisible(true);
    }
      
  //プログレスバーを更新 (キューに入れるのでかならずnewで別々のインスタンスと
  //しなければならない。
  class Bar extends Thread {
    int count;
    int recSize,totalSize;
    Bar(int count,int recSize,int totalSize) { 
      this.count = count;
      this.recSize = recSize;
      this.totalSize = totalSize;
    }
    public void run() {
      progressBar.setValue(count+1);
      String unit = " [ bytes ]";
      totalSizeLabel.setText( totalSize + unit );
      receiveSizeLabel.setText( recSize + unit );
    }
  }

  // DL完了時に呼び出され、終了メッセージを出したあとダイアログを閉じる
  class Closer implements Runnable {
      public void run() {
          if (result != DOWNLOAD_COMPLEATE ) {
              if ( file.exists() ) {
                  file.delete(); //未完に終わったファイルは削除する
              }
              if ( result == DOWNLOAD_ERROR ) { //障害発生のとき
                  String msg = "<html>ダウンロードに失敗しました。<br>" +
                      "エラーの詳細 : " + ERROR_MESSAGE + "<br>" +
                      "再試行しますか？</html>";
                  int res = errorDialog( msg, getTitle() );
                  if ( res == JOptionPane.YES_OPTION ) {
                      new Downloader().start();
                      return;
                  }
              } 
          } 
          dispose();
      }
  }

  /**
   * プログラム実行。これはｲﾍﾞﾝﾄﾃﾞｨｽﾊﾟｯﾁｽﾚｯﾄﾞではないので注意
   * java.net.UnknownHostExceptionがERROR_MESSAGEに格納された場合、それは接続先の
   * ホストが無いか、回線がオフラインになっていて接続できなかった可能性が高い。
   */
  class Downloader extends Thread {
      int BUFFER             = 2048; // *3
      int CONNECTION_TIMEOUT = 5000; // [ms]
      int READ_TIMEOUT       = 5000; // [ms]
      
      public void run() {
          InputStream stream = null;
          BufferedOutputStream bos = null;
          byte [] buf = new byte[ BUFFER ];
          int reclen = 0;
          float div = 0;
          int length = 0;                 //受信するファイルサイズ
          result = DOWNLOAD_COMPLEATE;    //DLの成否を示すフラグ
          SwingUtilities.invokeLater( new Bar(0,0,0) );
          try {
              URLConnection con = url.openConnection();
              con.setConnectTimeout( CONNECTION_TIMEOUT );
              con.setReadTimeout( READ_TIMEOUT );
              con.connect();      // *1
              length = con.getContentLength();
              if ( length < 0 ) throw 
                  new IOException("ファイルサイズが取得できません。"); // *2
              div = (float)length / 100f;
              stream = con.getInputStream(); // *4
              FileOutputStream fos = new FileOutputStream( file );
              bos = new BufferedOutputStream(fos); // *5
              reclen = 0; //受信した量
              for(;;) {
                  int count = (int)((float)reclen / div);
                  //バー更新
                  SwingUtilities.invokeLater( new Bar( count, reclen, length));
                  if(stop) {              //中止ボタンが中止要求フラグを立てた
                      result = DOWNLOAD_ABORT;
                      break;
                  }
                  int size = stream.read( buf, 0, buf.length );
                  if ( size < 0 ) {
                      bos.flush();
                      break;
                  }
                  bos.write( buf, 0, size );
                  reclen += size;
              }
          } catch ( Exception e ) {
              ERROR_MESSAGE = e.toString();
              result = DOWNLOAD_ERROR;
              stop = true;
          } finally {
              try { bos.close(); } catch ( Exception e ) { };
              try { stream.close(); } catch ( Exception e ) { };
          }
          SwingUtilities.invokeLater(new Closer());
      }
  }
  
//   *1 タイムアウトやパラメター設定しなかった場合、connectせずgetContentLength
//      しても値を取得できるが、設定するとconnectしないと値を取得できない。
//      おそらくconnect()で接続しなおしているのだと思われる。
//
//   *2 読み取り先がファィルの場合はサイズを取得できるだろうが、CGIのように動的
//      にデータを送ってくるような場合はサイズを取得できない。サイズ不明でも、
//      読みこむ事は可能だが、このプログラムはファイルのDLを目的としているのと、
//      プログレスバーでパーセント表示するためには最初にサイズが判明している必要
//      があり、ファイルサイズ不明のものは、DLは認めないというポリシーとする。
//   *3 実測すると、1460 bytes以上にはならなかった。
//   *4 BufferedInputStreamをかませてもパフォーマンス向上は無かった。
//   *5 これもあまり意味はないかもしれない。

  int getResult() {
      return result;
  }
  /**
   * 指定されたファイルをネットワークからダウンロードする。
   * @param owner 親フレーム
   * @param url   ダウンロードするファイル名 "http://tetramorph.to/test.zip"等
   * @param file  ダウンロードしたデータを格納するファイル
   * @param title タイトルバーに出す文字列
   * @param msg ダイアログ内に表示するメッセージ(HTML可能)
   * @return ダウンロードが成功したときはtrueを失敗ならfalseを返す。
   */
  public static int showDialog( Frame owner,
                                       URL url,
                                       File file,
                                       String title,
                                       String msg) {
      DownloadDialog dialog = new DownloadDialog( owner, url, file, title, msg);
      return dialog.getResult();
  }
  
  public static void main(String [] args) {
      java.awt.EventQueue.invokeLater(new Runnable() {
          public void run() {
              int res = 0;
              try {
                  URL url = new URL("http://tetramorph.to/eph.zip");                  
                  File file = new File("c:/src/html/jws/eph.zip");
//                  URL url = new URL("http://tetramorph.to/index.html");                  
//                  File file = new File("c:/src/html/jws/index.html");
                  res = showDialog(null,url,file,
                      "ダウンロード","天文暦のダウンロード中。");
              } catch (java.net.MalformedURLException e) {
                  e.printStackTrace();
              }
              System.out.println("Result = " + res);
          }
      });
  }
  // システム障害が起きたときのメッセージを出しユーザからの応答を受け取る
  private int errorDialog( String message, String title ) {
      return JOptionPane.showConfirmDialog( this, message, title, 
          JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE );

  }
  // 中止要求があったときの警告ダイアログを表示しユーザからの応答を受け取る。
  private int warningDialog( String message, String title ) {
      return JOptionPane.showConfirmDialog( this, message, title, 
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
  }
  private void messageDialog( String message, String title ) {
      JOptionPane.showMessageDialog( this, message, title, 
                                     JOptionPane.INFORMATION_MESSAGE );
  }
  
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
        receiveSizeLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        totalSizeLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonPanel = new javax.swing.JPanel();
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
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 160));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("\u53d7\u4fe1\u3057\u305f\u91cf : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        receiveSizeLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel2.add(receiveSizeLabel, gridBagConstraints);

        jLabel2.setText("\u53d7\u4fe1\u3059\u308b\u30b5\u30a4\u30ba : ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel2, gridBagConstraints);

        totalSizeLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(totalSizeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(jPanel2, gridBagConstraints);

        progressBar.setPreferredSize(new java.awt.Dimension(200, 16));
        progressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(progressBar, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(buttonPanel, gridBagConstraints);

        messageLabel.setText("       ");
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
 // 中止ボタンが押された。
  private void canselButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselButtonActionPerformed
    int res = warningDialog( "本当にダウンロードを中止しますか？",getTitle());
    if(res == JOptionPane.YES_OPTION)
        stop = true; //フラグを立てて実行中のスレッドに気づかせる
  }//GEN-LAST:event_canselButtonActionPerformed
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton canselButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel receiveSizeLabel;
    private javax.swing.JLabel totalSizeLabel;
    // End of variables declaration//GEN-END:variables
  
}
