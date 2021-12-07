/*
 * CSVFileFilter.java
 *
 * Created on 2007/01/27, 6:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import to.tetramorph.util.*;
/**
 * JFileChooserに使用するファイルフィルターでCSVの拡張子をもったものだけ通す。
 */
public class XMLFileFilter extends FileFilter{
  
    /** 
     * あたえられたファイル名が"*.csv"ならば真を違うなら偽を返す。大文字小文字の
     * 区別なしに判定される。
     */
    public boolean accept( File f ) {
        if ( f.isDirectory() ) return true;
        String ext = FileFilterUtils.getExtension( f );
        if ( ext == null ) return false;
        return ext.equals("xml");
        //return f.toString().toLowerCase().endsWith(".xml");
    }
    /**
     * このフィルタの説明を返す。"XML Document"と返る。
     */
    public String getDescription() {
        return "XML Document";
    }
  
}
