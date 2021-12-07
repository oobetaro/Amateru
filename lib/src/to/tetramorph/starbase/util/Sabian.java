/*
 *
 */
package to.tetramorph.starbase.util;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import to.tetramorph.util.CSVReader;
import to.tetramorph.util.CSVWriter;
import static java.lang.System.getProperty;
/**
 * サビアンシンボルのテキストを提供するクラス。
 * テキストはHome.dir + sabian_sjis.csvからロードするが、それが見つからないときは、
 * CLASSPATHのディレクトリ内にある/resourcesの中のsabian_sjis.csvからロードする。
 * (つまり確実なデータとしてjar書庫内にデータをもっているが、その文面が気にいら
 * ないユーザは、独自のサビアンテキストを用意すれば良いということ。)
 *
 * サビアンのファイルは文字コードセットがSJISで、CSVフォーマットのテキストファイル。
 * このクラスがJVMのメモリにロードされたらstaticイニシャライザによって、
 * サビアンファイルが配列にロードされ、メソッドでデータにアクセス可能になる。
 *
 * データを編集した場合はsave()でファイルに書き戻す事ができる。
 */
public class Sabian {

    /** 日本語のサビアンを取得するときの定数。*/
    public static final int JP = 5;
    /** 英語のサビアンを取得するときの定数。*/
    public static final int EN = 6;
    //日本語サビアン用
    //private static String [] text_jp = null;
    //英語サビアン用
    //private static String [] text_en = null;
    private static String[][] sabianTable = new String[360][7];


    static {
        init();
    }

    /**
     * サビアンテキストを返す。
     * <pre>
     * 使い方
     * System.out.println(Sabian.getText(Const.LIBRA,6,Sabian.JP));
     * System.out.println(Sabian.getText(Const.LIBRA,6,Sabian.EN));
     * </pre>
     * @param sign  星座(Const.ARIからConst.PISまで)
     * @param angle 角度(0から29)
     * @param lang  Sabian.JPまたはSabian.ENで日本語/英語の切替
     * @return 指定された度数のサビアンテキスト
     */
    public static String getText(int sign, int angle, int lang) {
        int a = sign * 30 + angle;
        return sabianTable[a][lang];
    }

    /**
     * サビアンテキストを返す。
     * <pre>
     * 使い方
     * System.out.println(Sabian.getText(180,Sabian.JP));
     * System.out.println(Sabian.getText(270,Sabian.EN));
     * </pre>
     * @param angle 角度(0から359)
     * @param lang  Sabian.JPまたはSabian.ENで日本語/英語の切替
     * @return 指定された度数のサビアンテキスト
     */
    public static String getText(int angle, int lang) {
        return sabianTable[angle][lang];
    }
    //サビアンテキストを配列にロードする。

    private static void init() {
        File file = new File( getProperty("app.userdict"),
                               "sabian_sjis.csv" );
        InputStream inputStream = null;
        CSVReader csvReader = null;
        URL url = null;
        try {
            //ホームディレクトリにサビアンファイルがない場合は、jar書庫内からロード
            if ( ! file.exists()) {
                url = Sabian.class.getResource("/resources/sabian_sjis.csv");
                inputStream = url.openStream();
                System.out.print("jar書庫から");
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                System.out.print(file + "から");
            }
            csvReader = new CSVReader(inputStream, "sjis");
            int i = 0;
            while (csvReader.ready()) {
                String[] record = csvReader.readCSV();
                sabianTable[i] = record;
                i++;
            }
            System.out.println("サビアンファイルをロード");
        } catch (FileNotFoundException e) {
            Logger.getLogger( Sabian.class.getName()).log(Level.SEVERE,null,e);
        } catch (IOException e) {
            Logger.getLogger( Sabian.class.getName()).log(Level.SEVERE,null,e);
        } finally {
            try { csvReader.close(); } catch (Exception e) { }
        }
    }

    /**
     * 所定ファイルにサビアンをCSV形式で書きだし。ホームディレクトリ上のファイル。
     * 無ければ作成される。
     * @param c 親コンポーネントを指定しておくとエラーが発生したときダイアログが開く。nullならprintStackTrace()
     * @return saveに失敗したときはfalse。EXCELでファイルを開いていたりすると失敗する。
     */
    public static boolean save(Component c) {
        File file = new File( getProperty("app.userdict"),
                               "sabian_sjis.csv");
        CSVWriter csvWriter = null;
        boolean saved = true;
        try {
            csvWriter = new CSVWriter(file, "sjis");
            for (int i = 0; i < 360; i++) {
                csvWriter.writeCSV(sabianTable[i]);
            }
        } catch (IOException e) {
            if (c != null) {
                JOptionPane.showMessageDialog(c,
                        "<html>ファイルへの保存に失敗しました。<br><table><tr>"
                        + "<td width=400>" + e.toString()
                        + "</td></table></html>",
                        "サビアンシンボルの保存", JOptionPane.ERROR_MESSAGE);
            } else {
                Logger.getLogger( Sabian.class.getName()).log(Level.SEVERE,null,e);
            }
            saved = false;
        } finally {
            try { csvWriter.close(); } catch (Exception e) { }
        }
        return saved;
    }

    /**
     * 指定された度数に新しいサビアンテキストをセットする。
     * @param angle 角度(0-359)
     * @param text サビアンテキスト
     * @param ej   日本語ならJP,英語ならenを指定
     */
    public static void setText(int angle, String text, int ej) {
        sabianTable[angle][ej] = text;
    }
//  public static void main(String[]args) {
//    out.println(Sabian.getText(Const.LIB,6,Sabian.JP));
//    out.println(Sabian.getText(Const.LIB,6,Sabian.EN));
//    out.println(Sabian.getText(Const.LEO,16,Sabian.JP));
//    out.println(Sabian.getText(Const.LEO,16,Sabian.EN));
//  }
}
