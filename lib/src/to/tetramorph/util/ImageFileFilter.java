/*
 * ImageFileFilter.java
 *
 * Created on 2008/10/23, 8:18
 *
 */

package to.tetramorph.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * JFileChooser用のファイルフィルタで、jpgやpngなどの拡張子をもったものだけ透過
 * する。
 * @author 大澤義孝
 */
public class ImageFileFilter extends FileFilter {
    /**
     * コンストラクタに指定するファイル形式名 値は"tif" 
     */
    public static final String TIFF = "tif";
    /**
     * コンストラクタに指定するファイル形式名 値は"gif" 
     */
    public static final String GIF  = "gif";
    /**
     * コンストラクタに指定するファイル形式名 値は"jpg" 
     */
    public static final String JPEG = "jpg";
    /**
     * コンストラクタに指定するファイル形式名 値は"png" 
     */
    public static final String PNG =  "png";
    /**
     * コンストラクタに指定するファイル形式名 値は"bmp" 
     */
    public static final String BMP =  "bmp";
    /**
     * コンストラクタに指定するファイル形式名 値は"wbmp" 
     */
    public static final String WBMP =  "wbmp";
    /**
     * フィールドPNG,GIF,BMP,TIFF,JPEG,WBMPを配列化したもの。
     */
    public static final String [] FORMAT_NAMES = {
        PNG,GIF,BMP,TIFF,JPEG,WBMP
    };
    private String format;
    /**
     * このファイルフィルタが透過する拡張子の名前を、
     * ImageFileFilter.TIFF,GIF,JPEG,PNG,BMP,WBMPの中から指定する。
     */
    public ImageFileFilter( String format ) {
        this.format = format;
    }
    
    /**
     * 入力されたファイルfがディレクトリの場合と、ファイルの拡張子が、
     * コンストラクタで指定されたtypeと同じものの場合はtrueを返す。
     * 拡張子の大文字小文字は区別しない。なおjpgはjpegでも同じと認識する。
     * tifはtiffでも同じと認識する。
     */
    public boolean accept( File f ) {
        if ( f.isDirectory() ) {
            return true;
        }
        String ext= FileFilterUtils.getExtension(f);
        if ( ext != null ) {
            if ( format.equals( JPEG ) &&
                 ( ext.equals( JPEG ) || ext.equals("jpeg") ) )
                return true;
            if ( format.equals( TIFF ) && 
                 ( ext.equals( TIFF ) || ext.equals("tiff") ) )
                return true;
            if ( format.equals( ext ) )
                return true;
        }
        return false;
    }
    

    /**
     * このフィルダーが透過する拡張子名を返す。
     */
    public String getDescription() {
        return format.toUpperCase() + " イメージ";
    }
    
    /**
     * コンストラクタで渡されたタイプ名を返す。
     */
    public String getFormatName() {
        return format;
    }
}
