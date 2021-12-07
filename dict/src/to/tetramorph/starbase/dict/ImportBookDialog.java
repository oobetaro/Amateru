/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import to.tetramorph.util.AmateruBookFileFilter;
import to.tetramorph.util.XMLFileFilter;

/**
 * ブックファイルを選択する際のファイルチューザー。
 * カレントディレクトリを記憶する機能つき。
 * 拡張子がABFとXML用のファイルフィルタを設定している。
 * @author 大澤義鷹
 */
class ImportBookDialog {
    private static final JFileChooser chooser;

    static {
        chooser = new JFileChooser();
        //すべてを透過するデフォルトのフィルタを禁止にする
        chooser.setAcceptAllFileFilterUsed(false);
        //専用のフィルタを設定
        chooser.addChoosableFileFilter(new XMLFileFilter());
        chooser.addChoosableFileFilter(new AmateruBookFileFilter());
        chooser.setDialogTitle("ブックファイルの選択");
    }

    /**
     * ブックファイル選択用のファイルチューザを開く。
     * @param owner 親となるコンポーネント
     * @return 選択されたファイル。未選択のときはnullを返す。
     */
    public static File show(Component owner) {
        //System.out.println("EDT = " + SwingUtilities.isEventDispatchThread());
        String path = Config.usr.getProperty("BookChooser.path", "");
        if ( ! path.isEmpty() ) {
            chooser.setCurrentDirectory(new File(path));
        }
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) return null; //選択が中止された
        File  file = chooser.getSelectedFile();
        Config.usr.setProperty("BookChooser.path",
                 chooser.getCurrentDirectory().getAbsolutePath());
        return file;
    }
}
