/*
 * FileFilterUtils.java
 *
 * Created on 2008/10/23, 12:43
 *
 */

package to.tetramorph.util;

import java.io.File;

/**
 * JFileChooser用のファイルフィルタを作るためのスーパークラス。
 * @author 大澤義孝
 */
public class FileFilterUtils {
    
    /**  FileFilterUtils オブジェクトを作成する */
    private FileFilterUtils() {
    }
    /**
     * ファイルの拡張子を小文字で返す。拡張子がついてない名前のときはnullを返す。
     */
    public static String getExtension( File f ) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if ( i > 0 &&  i < s.length() - 1 ) {
            ext = s.substring( i + 1 ).toLowerCase();
        }
        return ext;
    }
    
}
