/*
 * ImageFileView.java
 *
 * Created on 2008/10/23, 8:10
 *
 */

package to.tetramorph.util;

import java.io.File;
import javax.swing.filechooser.FileView;

/**
 * JFileChooser用のファイルビュー。
 * @author 大澤義鷹
 */
public class ImageFileView extends FileView {
    
    /**  ImageFileView オブジェクトを作成する */
    public ImageFileView() {
    }
    public String getName( File f ) {
        return null; //let the L&F FileView figure this out
    }

    public String getDescription( File f ) {
        return null; //let the L&F FileView figure this out
    }

    public Boolean isTraversable( File f ) {
        return null; //let the L&F FileView figure this out
    }

    public String getTypeDescription( File f ) {
        String extension = FileFilterUtils.getExtension( f );
        String type = null;

        if (extension != null) {
            if ( extension.equals("jpeg") ||
                 extension.equals("jpg")) {
                type = "JPEG Image";
            } else if ( extension.equals("gif") ){
                type = "GIF Image";
            } else if ( extension.equals("tiff") ||
                         extension.equals("tif")) {
                type = "TIFF Image";
            } else if ( extension.equals("png")){
                type = "PNG Image";
            } else if ( extension.equals("bmp")) {
                type = "BMP Image";
            } else if ( extension.equals("wbmp")) {
                type = "WBMP Image";
            }
        }
        return type;
    }
    
}
