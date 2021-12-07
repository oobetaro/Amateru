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
 * JFileChooserに使用するファイルフィルターでフォルダと、
 * CSVの拡張子をもったものだけ通す。
 */
public class CSVFileFilter extends FileFilter{
  
  /** 
   * あたえられたファイル名が"*.csv"または"*.CSV"ならば真を違うなら偽を返す。
   */
  public boolean accept(File f) {
        if ( f.isDirectory() ) return true;
        String ext = FileFilterUtils.getExtension( f );
        if ( ext == null ) return false;
        return ext.equals("csv");
//      if (f.isDirectory()) return true;
//    return f.toString().toLowerCase().endsWith(".csv");
  }
  /** 
   * このフィルタの説明を返す。"Microsoft Excel CSV"と返る。
   */
  public String getDescription() {
    return "Microsoft Excel CSV";
  }
  
}
