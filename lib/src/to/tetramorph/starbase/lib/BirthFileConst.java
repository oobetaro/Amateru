/*
 * BirthFileConst.java
 *
 * Created on 2007/01/29, 12:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

/**
 * バックアップに使用するCSVファイルの各フィールドのオフセット値を定義している。
 * 出生データファイル(DBのOCCASION,TREEPATH,COMPOSIT,HISTORY表に入れるデータ)は
 * CSV形式のファイルでバックアップされる。
 * このクラスの定数はstatic importして使用する。
 */
public class BirthFileConst {
  /**
   * エンコーディングUTF-16LEのファイルの先頭にBOM(ByteOrderMark)という、
   * 識別コードが埋め込まれる。これがないとExcelで正しく読めない。
   * 値は0xFEFF。4byteで表現されたint型の定数。
   */
  public static final int BOM = 0xFEFF;
  /**
   * CSVファイルの先頭に書き出されるヘッダー名の配列で要素数は17ある。
   */
  public static final String [] HEADDER = { 
    "CHARTTYPE","NAME","KANA","GENDER","JOB","MEMO","DATE","TIME","PLACENAME",
    "LATITUDE","LONGITUDE","TIMEZONE","TREEPATH","HISTORY","COMPOSIT","NOTE",
    "TIMESTAMP" };
  //エスケープが必要なﾌｨｰﾙﾄﾞは1。不要なのは0。
  public static final int CHARTTYPE = 0;
  public static final int NAME = 1;
  public static final int KANA = 2;
  public static final int GENDER = 3;
  public static final int JOB = 4;
  public static final int MEMO = 5;
  public static final int DATE = 6;
  public static final int TIME = 7;
  public static final int PLACENAME = 8;
  public static final int LATITUDE = 9;
  public static final int LONGITUDE = 10;
  public static final int TIMEZONE = 11;
  public static final int TREEPATH = 12;
  public static final int HISTORY = 13;
  public static final int COMPOSIT = 14;
  public static final int NOTE = 15;
  public static final int TIMESTAMP = 16;
  //
  public static final int EV_NAME = 0;
  public static final int EV_MEMO = 1;
  public static final int EV_DATE = 2;
  public static final int EV_TIME = 3;
  public static final int EV_PLACENAME = 4;
  public static final int EV_LATITUDE = 5;
  public static final int EV_LONGITUDE = 6;
  public static final int EV_TIMEZONE = 7;
  public static final int EV_SERIAL = 8;
}
