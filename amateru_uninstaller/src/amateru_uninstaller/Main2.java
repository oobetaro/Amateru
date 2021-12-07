/*
 * Main2.java
 * Created on 2011/07/19, 19:02:15.
 */
package amateru_uninstaller;

import amateru_installer.Regist;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import static java.lang.System.getProperty;

/**
 * アマテルアンインストーラのエントリー
 * @author ohsawa
 */
public class Main2 {
    static void errmsg( final String msg ) throws Exception {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog( null, msg,
                        "アマテル・アンインストーラ",
                        JOptionPane.ERROR_MESSAGE );
            }
        });
    }

    /**
     * 引数なしで起動したときはアンインストーラ。
     * 適当な引数を与えて起動したときは、内部にあるINSTALL.LOGを表示する。
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception{
        if ( args.length >= 1 ) {
            List<String> list = new ArrayList<String>();
            UninstallDialog.getInstallLog(list);
            if ( list.isEmpty() ) {
                System.out.println("INSTALL.LOGは空です");
            } else {
                for ( String s : list ) System.out.println(s);
            }
            System.exit(0);
        }

//        /* アンインストール済みの場合は終了。starbase.jarの存在の有無で判定)
//           ただしjarファイルを直接実行したときのみ判定する。                   */
//
//        if ( getProperty("java.class.path").endsWith("amateru_uninstaller.jar") ) {
//            File parent = new File( getProperty("java.class.path") ).getParentFile();
//            if ( ! new File( parent, "starbase.jar").exists() ) {
//                java.awt.EventQueue.invokeAndWait(new Runnable() {
//                    @Override
//                    public void run() {
//                        JOptionPane.showMessageDialog(null,
//                                "<html>アマテルはアンインストール済です。<br>"
//                                + "このフォルダは手作業で削除してください。</html>");
//                    }
//                });
//                System.exit(0);
//            }
//        }
        float ver = Float.parseFloat(
                getProperty( "java.specification.version","1.5") );
        if ( ver < 1.6F ) {
            errmsg( "JREのバージョンが1.6以下です。"
                    + "新しいJREをインストールしてください。" );
            System.exit(0);
        }
        // Win2KやXPなどUACを持たないOSでは権限チェックが必要
        if ( ! Regist.isAdminUser() ) {
            errmsg("アマテルのアンインストールには管理者権限が必要です");
            System.exit(0);
        }
        if ( ! Regist.sys.getBoolean("amateru_exists", false ) ) {
            errmsg("アマテルはすでにアンインストール済です");
            System.exit(0);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                int res = JOptionPane.showConfirmDialog(null,
                        "アマテルをアンインストールしますか？","確認",
                        JOptionPane.YES_NO_OPTION);
                if ( res == JOptionPane.NO_OPTION ) return;

                UninstallDialog dialog = new UninstallDialog(
                        new javax.swing.JFrame(), false );

                dialog.setLocationRelativeTo(null);
                dialog.addWindowListener( new WindowAdapter() {
                    @Override
                    public void windowClosing( WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
                dialog.start();
            }
        });
    }
}
