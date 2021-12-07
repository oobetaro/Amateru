/*
 * ImageSelection.java
 *
 * Created on 2008/10/21, 11:24
 *
 */

package to.tetramorph.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Imageをクリップボードにコピーするためのもの。
 * @author 大澤義鷹
 */
public class ImageSelection implements Transferable {

    private BufferedImage image;
    private DataFlavor[] flavors;
    
    public ImageSelection( BufferedImage image ) {
        this.image = image;
        flavors = new DataFlavor[] { DataFlavor.imageFlavor };
    }
    
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }
    
    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        return this.flavors[0].equals(flavor);
    }
    
    public synchronized Object getTransferData( DataFlavor flavor ) 
                              throws UnsupportedFlavorException, IOException {
        if ( ! flavors[0].equals( flavor ) ) {
            throw new UnsupportedFlavorException( flavor );
        }
        return image;
    }
}

