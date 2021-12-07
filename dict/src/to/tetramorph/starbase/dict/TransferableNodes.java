package to.tetramorph.starbase.dict;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * DnDのときにドラッグされるオブジェクトを格納するクラス
 */
class TransferableNodes implements Transferable {

    Object object;
    DictTree tree;

    public TransferableNodes(Object o, DictTree tree) {
        super();
        this.tree = tree;
        object = o;
    }

    public Object getTransferData(DataFlavor df)
            throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(df)) {
            return object;
        } else {
            throw new UnsupportedFlavorException(df);
        }
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        return df.equals( tree.getDataFlavor() );
    }

    public DataFlavor[] getTransferDataFlavors() {
        return tree.getSupportedFlavors();
    }
}
