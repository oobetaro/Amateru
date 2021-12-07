/*
 * CSVWriter.java
 *
 * Created on 2006/01/01, 0:00
 *
 */
package to.tetramorph.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import to.tetramorph.util.*;
/**
 * Microsof ExcelのCSV形式でString[]に格納された値を書き出すことができます。
 * @author 大澤義鷹
 */
public class CSVWriter extends BufferedWriter {
  private char separator;
  /**
   * ﾊﾞｯﾌｧﾘﾝｸﾞされた出力ｽﾄﾘｰﾑを作成します。
   * 作成します。
   * @param file 出力するファイル
   * @param csn 文字コードセット(ECU,SJIS,UTF-8等)
   * @throws java.io.IOException IOエラー
   */
  public CSVWriter(File file,String csn)
  throws IOException {
    super(
      new OutputStreamWriter(
      new FileOutputStream(file),csn));
    separator = ',';
  }
  
  /**
   * ﾊﾞｯﾌｧﾘﾝｸﾞされた出力ｽﾄﾘｰﾑを作成します。
   * @param filename ファイル名
   * @param csn 文字コードセット(SJIS,EUC,UTF-8等)
   * @throws java.io.IOException IOエラー
   */
  public CSVWriter(String filename,String csn) throws IOException {
    super(
      new OutputStreamWriter(
      new FileOutputStream(new File(filename)),csn));
    separator = ',';
  }
  /**
   * ﾊﾞｯﾌｧﾘﾝｸﾞされた出力ｽﾄﾘｰﾑを作成します。
   * @param file ファイル
   * @param separator ','か'\t'等。
   * @param csn 文字コードセット(SJIS,EUC,UTF-8等)
   * @throws java.io.IOException IOエラー
   */
  public CSVWriter(File file,char separator,String csn) throws IOException {
    this(file,csn);
    this.separator = separator;
  }
//  private static boolean escape(String str) {
//    if(str.indexOf(',') >= 0 ||
//      str.indexOf('"') >= 0 ) return true;
//    for(int i=0; i<str.length(); i++) {
//      if( str.charAt(i) < '\u0020' ) return true;
//    }
//    return false;
//  }
  // エスケープが必要な文字列ならtrueを返す
  private static boolean escape(String str,char separator) {
    //ｾﾊﾟﾚｰﾀ文字が含まれている場合と、ﾀﾞﾌﾞﾙｸｫｰﾄが含まれている場合はｴｽｹｰﾌﾟが必要
    if(str.indexOf(separator) >= 0 || str.indexOf('"') >= 0 ) return true;
    //さらにｺﾝﾄﾛｰﾙｺｰﾄﾞが含まれている場合のｴｽｹｰﾌﾟが必要
    for(int i=0; i<str.length(); i++) {
      if( str.charAt(i) < '\u0020' ) return true;
    }
    return false;
  }
  /**
   * 文字配列をストリームに書き出す。
   * @param cols 1行分の列データを配列で指定する。
   * @throws java.io.IOException IOエラー
   */
  public void writeCSV(String [] cols) throws IOException {
    String str = CSVWriter.getCSVString(cols,separator);
    write(str,0,str.length());
    newLine();	//システムで設定されている改行コードを出力
  }
  /**
   * 文字配列をCSV形式の文字列表現にして返す。
   * @param cols 1行分の列データ
   * @return colsがCSV表現にされた文字列。
   */
  public static String getCSVString(String [] cols,char separator ) {
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<cols.length; i++) {
      if(CSVWriter.escape(cols[i],separator)) {
        //ｴｽｹｰﾌﾟが必要な文字列はﾀﾞﾌﾞﾙｸｫｰﾄで囲むのだが、文字列中にﾀﾞﾌﾞﾙｸｫｰﾄが含
        //まれている場合は、""二つならべてｴｽｹｰﾌﾟとする。
        cols[i] = cols[i].replaceAll("\"","\"\"");
        //その後、全体をﾀﾞﾌﾞﾙｸｫｰﾄで囲む。
        sb.append( "\"" );
        sb.append( cols[i] );
        sb.append( "\"" );
      } else sb.append( cols[i] );
      sb.append( separator ); //ｾﾊﾟﾚｰﾀで連結
    }
    sb.deleteCharAt(sb.length()-1);	//末尾の一文字(ｾﾊﾟﾚｰﾀ)を削除
    return sb.toString();
  }
}

