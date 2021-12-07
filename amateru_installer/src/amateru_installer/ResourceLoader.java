/*
 *
 */
package amateru_installer;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * リソース内から画像やアイコンファイル、テキストファイルを読み込むメソッド群。
 * <br>
 * 画像ファイルを読み込みImageIconやImageオブジェクトを作成する。
 * 画像ファイルはクラスパスの位置がルート。つまりclassファイルの置き場所を"/"と
 * する。一般的に"/resources/images"といったフォルダを用意し、そこにまとめて画像
 * ファイルを置く。classファイルはjar書庫にパックされるが、そのとき/resourcesも
 * 一緒に同梱する。そうしておけばこのクラスのメソッドは、自動的にjar書庫から、
 * 指定された画像ファイルをロードする。
 */
public class ResourceLoader {

    /**
     * 画像ファイルからImageIconを返す。画像ファイルにはgif,png,jpgなどが使用可。
     * @param path クラスパスをルートとみなしたファイルのパス。
     * "/resources/images/icon.gif"などと指定する。
     */
    public static ImageIcon getImageIcon(String path) {
        URL imgURL = ResourceLoader.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            //System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * 画像ファイルからImageを返す。画像ファイルにはgif,png,jpgなどが使用可。
     * @param path クラスパスをルートとみなしたファイルのパス。
     * "/resources/images/icon.gif"などと指定する。
     */
    public static Image getImage(String path) {
        URL imgURL = ResourceLoader.class.getResource(path);
        if (imgURL != null) {
            return Toolkit.getDefaultToolkit().createImage(imgURL);
        } else {
            //System.err.println("Couldn't find file: " + path);
            return null;
        }

    }
    /**
     * リソースで指定されたパスからテキストファイルを読み込む。
     * @param path リソースのパス
     * @param enc "UTF-8","sjis","euc"などを指定。
     * @return 読み込まれたテキスト
     */
    public static String getText( String path, String enc ) {
        InputStream is =
            ResourceLoader.class.getResourceAsStream(path);
        InputStreamReader reader = null;
        final StringBuilder sb = new StringBuilder( 8192 );
        try {
            reader = new InputStreamReader( is, enc );
            int c;
            while ( (c = reader.read()) >= 0 ) {
                sb.append((char)c);
            }
        } catch ( IOException e ) {
            Logger.getLogger( ResourceLoader.class.getName() )
                    .log( Level.SEVERE, null, e );
        } finally {
            try { reader.close(); } catch(Exception e) { }
        }
        return sb.toString();
    }
}
