/*
 * InstallSkin.java
 * Created on 2011/07/30, 1:56:10.
 */
package to.tetramorph.starbase;

import java.io.File;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.Preference;
import static java.lang.System.getProperty;
/**
 * データベースが新規作成されたとき、複数のスキンの設定情報をフォルダから読み出し、
 * データベースのテーブルにインストールする。設定ファイルはapp.customize/skins/の
 * 中に入っていることが前提。
 * スキンにかぎらず計算設定も同じ要領で読み込めるが、今はもとの設定データがないので
 * 保留にしている。StartからDBテーブルを新規作成するときに一度だけ呼ばれる。
 * @author ohsawa
 */
public class InstallSkin {
    public static void load_skins() {
        DB db = DBFactory.getInstance();
        File [] files = new File( getProperty("app.conf"),"skin").listFiles();
        String [] keys = { "specificName","moduleClassName", "tableName" };
        for ( File file : files ) {
            if ( ! file.getName().endsWith(".xml") ) continue;
            Preference p = new Preference();
            if ( ! FileTools.loadProperties(p, file) ) {
                System.err.println(file + "は読み込めないので無視します。");
                continue;
            }
            for ( String k : keys ) {
                if ( p.getProperty(k,"").isEmpty() ) {
                    System.err.println( file + "の" + k + "の値が取得できないので無視します");
                    continue;
                }
            }
            String specName  = p.getProperty( "specificName" );
            String className = p.getProperty( "moduleClassName" );
            String tableName = p.getProperty( "tableName" );
            //System.out.println("spec = " + specName + ", class = " + className + ", table = " + tableName );
            db.setConfigProperties( specName, className, p, tableName  );
        }
    }
    public static void main( String [] args ) {
        load_skins();
    }
}
