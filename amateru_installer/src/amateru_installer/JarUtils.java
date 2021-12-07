/*
 * JarUtils.java
 * Created on 2011/07/20, 14:27:29.
 */
package amateru_installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * アンインストーラのjar書庫内に保管されているインストールログの読み出し書き換えを
 * おこなう。
 * インストーラはインストール作業のログ(どんなファイルやフォルダを生成したか)を、
 * アンインストーラのjar書庫に保管する。アンインストーラはこのログを参照して、その
 * 作業を行う。
 * @author ohsawa
 */
public class JarUtils {
    private static final String INST_LOG = "resources/INSTALL.LOG";

    /**
     * アンインストーラのjar書庫内のインストールログ(フィールドINST_LOGで宣言してある
     * )の更新を行う。
     * jar書庫内に"resources/INSTALL.LOG"が存在すれば、listの内容に置換する。
     * 存在しないときは新しく追加し、list内容を書きこむ。
     * マニフェストおよび他のエントリーはそのまま変化させない。
     * 内部的にはTEMP.jarに上記仕様にしたがって改変したjar書庫を書き出し、
     * 最後に元ソースを削除しテンポラリを元ソースのファイル名にリネームする。
     * @param src
     * @param dst
     * @throws IOException 普通の例外に加えて、ログの更新に失敗したときなど。
     */
    static void updateInstallLog( File src, List<String> list ) throws IOException {
        JarInputStream in = new JarInputStream( new FileInputStream(src) );
        File dst = new File( src.getParentFile(),"TEMP.jar" );
        JarOutputStream out = new JarOutputStream(
                             new FileOutputStream(dst), in.getManifest() );
        JarEntry log_entry = new JarEntry( INST_LOG );
        byte [] buf = new byte[ 4096 ];
        JarEntry je = null;
        boolean found = false;
        while ( ( je = in.getNextJarEntry() ) != null ) {
            if ( je.getName().equals( INST_LOG )) {
                out.putNextEntry(log_entry);
                for ( String s : list )
                    out.write( s.concat("\r\n").getBytes() );
                found = true;
            } else {
                out.putNextEntry(je);
                int len = buf.length;
                while( ( len = in.read( buf, 0, len ) ) > 0 )
                    out.write(buf, 0, len);
            }
            out.closeEntry();
        }
        if ( ! found ) {
            out.putNextEntry(log_entry);
            for ( String s : list )
                out.write( s.concat( "\r\n" ).getBytes() );
            found = true;
        }
        in.close();
        out.close();
        if ( src.delete() ) {
            if ( ! dst.renameTo(src) ) {
                throw new java.io.IOException("インストールログの更新にリネームに失敗");
            }
        } else {
                throw new java.io.IOException("インストールログの削除に失敗:" + src);
        }
    }
    /**
     * jar書庫の中の"resources/INSTALL.LOG"の内容を一行ずつリストにいれる。
     * @param srcjar 読み出すjar書庫のファイルを指定。
     * @param list 結果返却用のリスト
     * @return 正常に読み出せたときはtrue。エントリーが存在しないときはfalse。
     * @throws IOException
     */
    public static boolean getInstallLog( File srcjar, List<String> list )
                                                            throws IOException {
        JarFile jarFile = new JarFile( srcjar );
        JarEntry entry = jarFile.getJarEntry( INST_LOG );
        if ( entry == null ) return false;
        BufferedReader br = new BufferedReader(
                new InputStreamReader( jarFile.getInputStream(entry)) );
        while ( br.ready() ) list.add ( br.readLine() );
        br.close();
        jarFile.close();
        return true;
    }
    /**
     * インストーラー自身のjar書庫内にあるファイルエントリーをリストにして返す。
     * 配布するファイルはリソース内で"dist/"の中に集められていてその一覧を返す。
     * ファイルのみ抽出し、フォルダのみのエントリーはリストから除外する。
     * <br><br>
     * インストーラーのjarをダブクリやショートカットから実行したときは、それ自身の
     * jarファィルをプログラム側から取得できるが、NetBeansからの実行はそもそもjar
     * 書庫になったものを実行しているのではなく、./buildの中に展開されたclassファイル
     * を直接実行しているので、jar書庫のパスを取得できない。これでは開発に不便なので、
     * プロジェクトフォルダの./distの中にあるjar書庫を参照するようにしている。
     * その場合、コンソールにその旨のメッセージを出す。
     *
     * @param fileList リストを書きこむ。このリストに値が書き込まれる。
     * なお各パスの先頭の"dist/"は除外したものを返す。
     *
     * @return 引数で与えられたfileListを返す。
     */
    public static List<String> getFileList( List<String> fileList )
                                                           throws IOException {
        String folder = "dist/";
        int folder_len = folder.length();
        String self_jar = System.getProperty("java.class.path");
        File file = null;
        if ( self_jar.endsWith(".jar") ) file = new File(self_jar);
        else {
            file = new File("dist/amateru_installer.jar");
            System.out.println(
                    "NetBeansから実行されたので./distの中のjar書庫から"
                    + "リストを抽出します。\n" + file.getAbsolutePath());
        }
        JarFile jarFile = new JarFile(file);
        for ( Enumeration enu = jarFile.entries(); enu.hasMoreElements();) {
            JarEntry entry = (JarEntry) enu.nextElement();
            if (   entry.getName().startsWith( folder ) &&
                 ! entry.getName().endsWith("/")) {
                fileList.add( entry.getName().substring( folder_len ) );
            }
        }
        jarFile.close();
        return fileList;
    }

//    public static void main(String [] args) throws Exception {
//        test();
//    }
//
//    public static void test() throws Exception {
//        List<String> list = new ArrayList<String>();
//        for ( String s : getFileList( list ) ) System.out.println(s);
//    }
//
//    public static void main1() throws IOException {
//        File src = new File("../amateru_uninstaller/dist/amateru_uninstaller.jar");
//        List<String> list = new ArrayList<String>();
//        getInstallLog(src,list);
//        for(String s: list) System.out.println(s);
//        System.out.println("------------------------------");
//        list.clear();
//        list.add("ふがふが");
//        list.add("ほげほげ");
//
//        updateInstallLog(src,list);
//        list.clear();
//        getInstallLog(src,list);
//        for(String s: list) System.out.println(s);
//
//    }
}
