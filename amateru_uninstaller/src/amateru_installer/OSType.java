/*
 * OSType.java
 * Created on 2011/07/27, 20:40:08.
 */
package amateru_installer;
import static java.lang.System.getProperty;
/**
 * プラトッホーム判定用のメソッド
 * @author ohsawa
 */
public class OSType {

    private OSType() { }

    /**
     * このプログラムが実行されているOSの系統を返す。
     * @return "windows","mac","unix"の三種類のみ。
     */
    public static String getName() {
        String os = getProperty("os.name","");
        if ( os.matches( "^Windows.*" ) ) {
            return "Windows";
        } else if ( os.toLowerCase().matches( "^mac.*" ) ) {        // Mac
            return "Mac OS";
        }
        return "Unix";
    }
    /**
     * @return OSがWindowsならtrueを返す。
     */
    public static boolean isWindows() {
        return getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    }
    /**
     * @return OSがMacならtrueを返す。
     */
    public static boolean isMac() {
        return getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }
    /**
     * @return OSがUnixならtrueを返す。だが実際のところはウインドウズでもマック
     * でもなければすべてUnixとみなしているだけ。
     */
    public static boolean isUnix() {
        return ! isWindows() && ! isMac();
    }

    /**
     * OS Versionを返す。たとえばWindows Vista以降なら6以上の値。
     * XPなら5.1。Win7は6.1。
     * @return
     */
    public static float getOSVersion() {
        return Float.parseFloat( getProperty("os.version") );
    }

    public static void main(String args[] ) {
        System.out.println(getOSVersion());
    }
}
