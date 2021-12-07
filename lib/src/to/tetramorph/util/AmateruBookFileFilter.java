/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.util;
import java.io.File;
import javax.swing.filechooser.FileFilter;
/**
 * JFileChooserに使用するファイルフィルターでABFの拡張子をもったものだけを通す。
 * アマテルの辞書ファイル(*.abf)を透過するフィルタ。
 * @author 大澤義鷹
 */
public class AmateruBookFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if ( f.isDirectory() ) return true;
        String ext = FileFilterUtils.getExtension(f);
        if ( ext == null ) return false;
        return ext.equals("abf");
    }

    @Override
    public String getDescription() {
        return "Amateru Book File";
    }

}
