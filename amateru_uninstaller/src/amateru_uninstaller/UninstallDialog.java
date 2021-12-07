/*
 * UninstallDialog.java
 * Created on 2011/07/22, 20:09:38.
 */

/*
 * UninstallDialog.java
 *
 * Created on 2011/07/22, 20:09:38
 */
package amateru_uninstaller;

import amateru_installer.OSType;
import amateru_installer.ResourceLoader;
import amateru_installer.Regist;
import amateru_installer.WinShortcut;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * アンインストールを行うダイアログ。
 * アンインストールが行われたあと、ふたたび実行されたときそれを検出する必要があるが
 * 未実装。
 * 天文暦や共有データを残したい場合もあるかもしれない。
 * 「アプリの追加と削除」のメニューが消去するとこもまだ作ってない。
 * @author ohsawa
 */
public class UninstallDialog extends javax.swing.JDialog implements Runnable {

    /** Creates new form UninstallDialog */
    public UninstallDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setIconImage( ResourceLoader.getImage("/resources/niwatori16.png") );
    }

    /**
     * jar書庫の中の"resources/INSTALL.LOG"の内容を一行ずつリストにいれる。
     * @param list 結果返却用のリスト
     * @throws IOException リソースを取得できなかったとき。通常ありえない。
     */
    public static void getInstallLog( List<String> list ) throws IOException {

        InputStream is = UninstallDialog.class
                .getResourceAsStream( "/resources/INSTALL.LOG" );

        if ( is == null ) throw new IllegalArgumentException
                ("インストールログが存在しない");

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        while ( r.ready() ) list.add( r.readLine() );
        r.close();

    }

    /**
     * インストールログを逆に辿りながらファイルとフォルダを削除する。
     * ただしアプリが生成したフォルダやファイルがあると、すべてを完全に消去することは
     * できない。インストール直後のまっさらな状態で実行すれぱ完全に消去されるのが正常。
     * @param logFile
     * @throws IOException
     */
    public void uninstall( List<String> list ) throws IOException {
        Collections.reverse(list);
        // 最初にファィルとショートカットを削除
        for ( String s: list ) {
            String [] token = s.split(",");
            if ( token[0].equals("copy") ) {
                File f = new File( token[1] );
                String ap = f.getAbsolutePath();
                if ( f.exists() ) {
                    if ( f.delete() )
                        print( "DELTED" , ap );
                    else
                        print( "DELETE FAILED", ap );
                } else {
                    print( "FILE NOT FOUND", ap );
                }
                count_bar();
            }
        }
        // 次にフォルダを削除
        for ( String s: list ) {
            String [] token = s.split(",");
            if ( token[0].equals("mkdirs")) {
                File dir = new File( token[1] );   //親のパス
                File dir2 = new File( token[2] );  //親の下にある子のパス(複数)

                // 子のフォルダを末節から上に向かってすべて削除

                for(;;) {
                    File f = new File( dir,dir2.getPath() );
                    String ap = f.getAbsolutePath();
                    if ( f.exists() ) {
                        if ( f.delete() )
                            print( "RMDIR",ap );
                        else
                            print( "RMDIR FAILED",ap );
                    } else {
                        print( "DIR NOT FOUND",ap );
                    }

                    count_bar();
                    dir2 = dir2.getParentFile();
                    if ( dir2 == null ) break;
                }
            } else if ( token[0].equals("regist")) {
                if ( OSType.isWindows() ) {
                    // 「アプリケーションの追加と削除」から削除
                    try {
                        WinShortcut.regDelete( token[1] );
                        print( "UNREGIST", token[1] );
                    } catch (Exception e) {
                        print( "UNREGIST FAILED", e.getMessage() );
                    }
                }
            }
        }
    }


    double prog = 0;         /* プログレスバーの値を実数で保管する */
    double prog_step = 0;   /* プログレスバーの値に加算するステップ値 */

    /**
     * 削除処理を行うスレッド。start()メソッドから起動される。
     */
    @Override
    public void run() {
        try {
            List<String> logList = new ArrayList<String>();
            getInstallLog(logList);
            if ( logList.isEmpty() ) throw new IllegalArgumentException
                    ("インストールログが空です");

            prog = 0;
            prog_step = (double)progressBar.getMaximum() / logList.size();
            uninstall( logList );
            Regist.sys.remove( "amateru_exists" );
            Regist.sys.remove( "amateru_uninstaller" );
            print( "END","アンインストール完了" );

        } catch ( Exception e ) {
            print( "ERROR",e.getMessage());
        }
        end();
    }

    /**
     * プログレスバーをすすめる
     */
    void count_bar() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                prog += prog_step;
                progressBar.setValue((int)prog);
            }
        });
    }

    /**
     * 削除処理中にテキストエリアにメッセージを表示する。
     * @param msg
     */
    void print( final String head, final String msg ) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append( String.format( "%-15s%s%n", head, msg ) );
            }
        });
    }

    void end() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                endButton.setEnabled(true);
            }
        });
    }

    /**
     * 削除処理を開始する。
     */
    public void start() {
        new Thread(this).start();
    }

//    void message( final String msg,
//                   final String title,
//                   final int type ) throws InterruptedException,
//                                           InvocationTargetException {
//        java.awt.EventQueue.invokeAndWait(new Runnable() {
//            @Override
//            public void run() {
//                JOptionPane.showMessageDialog(
//                        UninstallDialog.this, msg, title, type);
//            }
//        });
//    }
//    public static void main(String [] args) {
//        File dir = new File("ohsawa/.AMATERU2.0/conf");
//        for (;;) {
//            System.out.println(dir);
//            dir = dir.getParentFile();
//        }
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        endButton = new javax.swing.JButton();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("アマテルのアンインストール");

        jPanel2.setPreferredSize(new java.awt.Dimension(400, 300));
        jPanel2.setLayout(new java.awt.BorderLayout());

        progressBar.setMaximum(400);
        progressBar.setPreferredSize(new java.awt.Dimension(200, 14));
        jPanel2.add(progressBar, java.awt.BorderLayout.NORTH);

        endButton.setText("終了");
        endButton.setEnabled(false);
        endButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endButtonActionPerformed(evt);
            }
        });
        jPanel3.add(endButton);

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void endButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endButtonActionPerformed
        dispose();
        System.exit(1);
    }//GEN-LAST:event_endButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton endButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
