/*
 * CSVReader.java
 *
 * Created on 2006/01/01, 0:00
 *
 */
package to.tetramorph.util;
import java.util.Vector;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import to.tetramorph.util.*;
/**
 * Microsoft EXCELのCSVファイルを読込む事ができます。
 * ダブルクォートで囲まれ、その中に改行やカンマのコードが入っていたり、
 * ダブルクォートが""にエスケープされているものも正しく認識します。
 * @author 大澤義鷹
 */
public class CSVReader extends BufferedReader {
  /**
   * CSVでのデータの区切りを識別するセパレータコード。初期値はカンマです。
   */
  protected final char separator;
  /**
   * 指定された入力ストリームからCSVフォーマットのデータを読みこむインスタンスを
   * 作成します。セパレータはカンマ','です。
   * @param stream 入力ストリーム。
   * @param csn 文字コード(SJIS,EUC,UTF-8等)
   * @throws java.io.FileNotFoundException ファイルが存在しない
   * @throws java.io.UnsupportedEncodingException サポートされていないエンコーディング
   */
  public CSVReader(InputStream stream,String csn)
  throws FileNotFoundException,UnsupportedEncodingException {
    super(new InputStreamReader(stream,csn));
    separator = ',';
  }
  /**
   * デフォルトサイズのバッファでバッファリングされた、CSVファイルの入力ストリームを
   * 作成します。filenameはCSVファイルへのパスを指定します。セパレータはカンマ','です。
   * @param file CSVファイルを指定する。
   * @param csn 文字コードセット
   * @throws java.io.FileNotFoundException ファイルが無い。
   * @throws java.io.UnsupportedEncodingException サーポートされていない文字コード。
   */
  public CSVReader(File file,String csn)
  throws FileNotFoundException,UnsupportedEncodingException {
    super(new InputStreamReader(new FileInputStream(file),csn));
    separator = ',';
  }
  
  /**
   * デフォルトサイズのバッファでバッファリングされた、CSVファイルの入力ストリームを
   * 作成します。filenameはCSVファイルへのパスを指定します。セパレータはカンマ','です。
   * @param filename ファイル名を指定する。
   * @param csn 文字コードセットを指定する。
   * @throws java.io.FileNotFoundException ファイルが無い。
   * @throws java.io.UnsupportedEncodingException サポートされていない文字コードセット。
   */
  public CSVReader(String filename,String csn)
  throws FileNotFoundException,UnsupportedEncodingException {
    super(new InputStreamReader(new FileInputStream(new File(filename)),csn));
    separator = ',';
  }
  /**
   * デフォルトサイズのバッファでバッファリングされた、CSVファイルの入力ストリームを
   * 作成します。filenameはCSVファイルへのパスを指定します。
   * CSVファィルのseparatorとして、カンマ','か'\t'を指定できます。
   * @param filename ファイル名
   * @param separator セパレータとする文字
   * @param csn エンコーディング
   * @throws java.io.FileNotFoundException ファイルが無い
   * @throws java.io.UnsupportedEncodingException サポートされてない文字エンコーディング
   */
  public CSVReader(String filename,char separator,String csn)
  throws FileNotFoundException,UnsupportedEncodingException {
    super(new InputStreamReader(new FileInputStream(new File(filename)),csn));
    this.separator =
      (separator == '\t' || separator == ',') ? separator : ',';
  }
  /**
   * デフォルトサイズのバッファでバッファリングされた、CSVファイルの入力ストリームを
   * 作成します。filenameはCSVファイルへのパスを指定します。
   * CSVファィルのseparatorとして、カンマ','か'\t'を指定できます。
   * @param file ファイルオブジェクト
   * @param separator セパレータとする文字
   * @param csn エンコーディング
   * @throws java.io.FileNotFoundException ファイルが無い
   * @throws java.io.UnsupportedEncodingException サポートされてない文字エンコーディング
   */
  public CSVReader(File file,char separator,String csn)   
    throws FileNotFoundException,UnsupportedEncodingException {
    super(new InputStreamReader(new FileInputStream(file),csn));
    this.separator =
      (separator == '\t' || separator == ',') ? separator : ',';    
  }
  //右端の空白(\u0020以下のコード)を除去して返す
  
  private static String right_trim(String str) {
    int i=str.length();
    if(i == 0) return str;
    while(str.charAt(--i) < '\u0020');
    return str.substring(0,i+1);
  }
  
  /**
   * Microsoft EXCELのCSV形式の文字列(1行分)を、トークンに分割して返す。
   * ダブルクォートで囲まれ改行が含まれる場合も、構文解釈される。
   * separatorは通常は","だが、コンストラクタで"\t"を指定可能。
   * 値が入ってない列( ,,,,になっている部分)や中身の無いダブルクォート("")
   * は長さゼロの文字列として表現される。nullにはならない。
   * @param str CSV表現の文字列
   * @param separator セパレータとする文字
   * @return 列ごとに分解された文字列を配列で返す。
   */
  public static String [] csv_split(String str,char separator) {
    str = CSVReader.right_trim(str) + separator; //",";
    Vector<String> v = new Vector<String>();
    int i=0;
    String zero = "";
    do {
      char c = str.charAt(i);
      if (c == separator) {
        v.add(zero);
      } else if (c == '"') {
        StringBuffer sbuf = new StringBuffer();
        i++;	//"からポインタを進め････ ",で終わるとこまで抽出
        while(! str.substring(i,i+2).equals("\"" + separator)) {
          if(str.substring(i,i+2).equals("\"\"")) {
            sbuf.append('"');
            i += 2;
          } else {
            sbuf.append(str.charAt(i++));
          }
        }
        v.add(sbuf.toString());	//sbufが空のとき""が返る。
        i++;
      } else {
        StringBuffer sb = new StringBuffer();
        do {
          sb.append(str.charAt(i));
        } while(str.charAt(++i) != separator ); //',');
        v.add(sb.toString());
      }
    } while(++i < str.length());
    String [] ret = new String[v.size()];
    return v.toArray(ret);
  }
  /**
   * CSVファイルから一行(CSVの文法での一行)読み取り、各列の値を配列で返す。
   * 空白のセルは、nullではなく、""の値が返る。
   * @return 列ごとに分解された文字列を配列で返す。
   * @throws java.io.IOException IOエラー
   */
  public String [] readCSV() throws IOException {
    if(ready()) {
      StringBuffer sb = new StringBuffer();
      int n = 0;
      //以下のdo文はCSVの1行分のデータをlineに抽出する処理
      String line;
      do {
        StringBuffer lb = new StringBuffer();
        while(ready()) {
          char c = (char)read();
          lb.append(c);
          if(c == 0x0a) break;
        }
        line = lb.toString();
        sb.append(line);
        //一行に含まれるダブルクオートの数を求める
        char [] cb = new char[lb.length()];
        lb.getChars(0,lb.length(),cb,0);
        for(int i=0; i<cb.length; i++)
          if( cb[i] == '"' ) n++;;
      } while((n % 2) != 0);	//クオート二つ検出するまでループ
      //一行をトークンに分割
      return CSVReader.csv_split(sb.toString(),separator);
    } else throw (new IOException());
  }
}