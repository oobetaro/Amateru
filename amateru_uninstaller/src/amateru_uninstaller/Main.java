/*
 * Main.java
 * Created on 2011/07/17, 22:27:21.
 */
package amateru_uninstaller;

import amateru_installer.OSType;
import amateru_installer.WinShortcut;
/**
 * アマテルアンインストーラのエントリー
 * @author ohsawa
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

//        if ( getProperty("os.name").indexOf("Windows") >= 0 &&
//             Float.parseFloat( getProperty("os.version") ) >= 6 ) {
        if ( OSType.isWindows() && OSType.getOSVersion() >= 6F ) {
            /* Windows Vista,Windows 7以上なら昇格が必要。
               Vista以降のOSバージョンは6以上の値を取る
               greadup()のあとJScriptから別のJVMが起動しMain2.mainが呼ばれる */
            WinShortcut.greadup("/resources/uac.js", Main2.class.getName() );
        } else {
            // それ以外のOSは昇格せずにMain2を実行
            Main2.main(null);
        }
    }

}
