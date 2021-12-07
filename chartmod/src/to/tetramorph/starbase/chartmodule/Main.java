/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/17
 */

package to.tetramorph.starbase.chartmodule;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 検索モジュール"chartmod.jar"の一覧を標準出力にダンプする。<br>
 * このクラスをプロジェクトの「主クラス」として指定しておくこと。<br>
 * アマテルの実行には関係のないクラスだが、主クラスをプロジェクトのプロパティで
 * 指定しておかないと、NetBeansがこのプロジェクトをjarファイルにするとき、
 * このプロジェクトのトップディレクトリにあるmanifest.mfファイルを使わずに、
 * 中身が空のマニフェストを入れてしまう。(NetBeans6.5ではこのような事は無かった、
 * のだが6.7.1からそのような仕様になったらしい)<br>
 * manifest.mfには、検索モジュールの一覧情報が入っており、
 * アマテルはそれを参照して、chartmod.jarからクラスファイルを動的に読みこむ。<br>
 * 　マニフェストが空で一覧を取得できなければ、アマテルはモジュールを読みこむ事が
 * できず、エラーを出してしまう。<br>
 * 　このクラスは、マニフェストからモジュールクラス名のリストを読みこみ、
 * jar書庫内にそれに該当するクラスファイルが存在するかのチェックも行う。<br>
 * >java -jar chartmod.jarとすれば、その一覧が表示され、マニフェストで宣言され
 * ているファイルが見つからないときは、その情報も表示する。<br>
 * @author 大澤義鷹
 */
public class Main {

    public static void main( String [] args ) throws Exception {
        System.out.println("\"chartmod.jar\"内のMANIFEST.MFに記述されている" +
                           "アマテルチャートモジュール一覧");
        File file = new File( System.getProperty("java.class.path") );
        JarFile jarFile = new JarFile(file);
        //jarファイル内のclassファイル名一覧を、ハッシュセットに格納
        Set<String> set = new HashSet<String>();
        for ( Enumeration enu = jarFile.entries(); enu.hasMoreElements(); ) {
            JarEntry entry = (JarEntry) enu.nextElement();
            set.add(entry.getName().replace('/', '.')); //ちょっと加工して入れる
        }
        //マニフェストファイルを読みこみ、定義されているクラスファイルが存在
        //するかチェックする。
        Manifest mf = jarFile.getManifest();
        Attributes att = mf.getMainAttributes();
        for ( int j = 1; ; j++ ) {
            String className = att.getValue("ChartModule-Class" + j);
            if ( className == null ) break;
            if ( set.contains( className.concat(".class") ) ) {
                System.out.println( className );
            } else {
                System.out.println( className + "...Class File Not Found." );
            }
        }
    }
}
