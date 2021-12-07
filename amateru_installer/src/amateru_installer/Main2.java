/*
 * Main2.java
 * Created on 2011/08/17, 23:44:43.
 */
package amateru_installer;

import java.net.URL;
import java.awt.Desktop;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import static java.lang.System.getProperty;
/**
 * インストーラの第2エントリー。
 * @author ohsawa
 */
public class Main2 {
    private static final String mac     = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    private static final String metal   = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final String motif   = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    private static final String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static final String gtk     = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String nimbus  = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
    /**
     * インストーラの第2エントリー。引数は受け取らない。
     * WinVista,Win7の場合は、WinShortcut#greadup()が起動したアカウント昇格
     * スクリプトからこのメソッドが呼び出される。
     * それ以外のOS(Win2K,WinXP,Mac,Linux)はMain#main()から直接呼び出される。
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if ( OSType.isWindows() ) {
                    try {
                        UIManager.setLookAndFeel(windows);
                    } catch ( Exception e ) {
                        Logger.getLogger( InstallerFrame.class.getName())
                                .log( Level.WARNING, null, e);
                    }
                }

                float ver = Float.parseFloat(
                        getProperty( "java.specification.version","1.5") );
                if ( ver < 1.6F ) {
                    JOptionPane.showMessageDialog( null,
                    "JREのバージョンが1.6以下です。"
                            + "新しいJREをインストールしてください。",
                    "アマテルのインストール",
                    JOptionPane.ERROR_MESSAGE );
                    try {
                        Desktop.getDesktop().browse(
                                new URL("http://www.java.com").toURI() );
                    } catch( Exception e ) { }
                    System.exit(0);
                }

                if ( ! Regist.isAdminUser() ) {
                    JOptionPane.showMessageDialog( null,
                    "インストールには管理者権限が必要です。",
                    "アマテルのインストール",
                    JOptionPane.ERROR_MESSAGE );
                    System.exit(0);
                }
                InstallerFrame f = new InstallerFrame();
                f.pack();
                f.setup();
                f.setVisible(true);
            }
        });
    }

}
