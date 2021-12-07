/*
 * DefaultPath.java
 * Created on 2011/07/18, 18:44:57.
 */
package amateru_installer;
import java.io.File;
import static java.lang.System.getProperty;
/**
 * プラットフォームの違いに応じたインストールフォルダのパラメターを返す。
 * @author ohsawa
 */
class DefaultPath {
    private DefaultPath() { }
    /**
     * プラットフォームごとにデフォルトのインストール先のパス名を返す。
     * @return [0]=プログラム本体, [1]=天体暦, [2]=user.home+.AMATERU2.0
     */
    public static void getDefaultFolders( String [] res ) {
        String home = new File(
                getProperty("user.home"),".AMATERU2.0").getAbsolutePath();
        if ( OSType.isWindows() ) {
            if ( OSType.getOSVersion() < 6F ) {              //Win2000

                res[0] = "C:\\Program Files\\AMATERU2.0";
                res[1] = "C:\\Users\\ephe";
                res[2] = home;

            } else {                                          // たとえばVista,7

                res[0] = "C:\\Program Files\\AMATERU2.0";
                res[1] = "C:\\Users\\ephe";
                res[2] = home;
            }

        } else if ( OSType.isMac() ) {                      // Mac

            res[0] = "/Applications/AMATERU2.0";
            res[1] = "/Library/ephe";
            res[2] = home;

        } else {                                             // UNIX系

            res[0] = "/usr/local/AMATERU2.0";
            res[1] = "/usr/ephe";
            res[2] = home;

        }
    }
}
