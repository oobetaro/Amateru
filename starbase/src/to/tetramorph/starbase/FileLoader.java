/*
 * FileLoader.java
 *
 * Created on 2007/11/24, 17:23
 *
 */

package to.tetramorph.starbase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * ネットワークからファイルを取得するstaticのメソッドをまとめたクラス。
 * プログレスバーなどのディスプレイを持たないので、あまり大きなファイルを
 * ダウンロードするのには向かない。その場合はDownloadDialogを使用する。
 * @author 大澤義鷹
 */
class FileLoader {

    private FileLoader() {
    }
    /**
     * 指定されたurlからファイルをロードしfileに出力する。
     * DownloadDialogと違って、contentLengthが取得できないソースでも受信できる。
     * タイムアウトは10秒に設定されており、それをすぎてもロードできない場合は
     * エラーとなる。エラーになった場合でも、filaは削除されないので、呼びだし側
     * でフォローする必要がある。
     * @exception IOException I/Oエラーが発生したとき。
     * @exception java.io.FileNotFoundException サーバにファイルが見つからないとき
     * @exception java.net.SocketTimeoutException タイムアウトしたとき
     */
    public static void load( URL url, File file) throws IOException {
        int BUFFER             = 2048;
        int CONNECTION_TIMEOUT = 10000; // [ms]
        int READ_TIMEOUT       = 5000; // [ms]
        InputStream stream = null;
        BufferedOutputStream bos = null;
        byte [] buf = new byte[ BUFFER ];
        int reclen = 0;
        URLConnection con = url.openConnection();
        con.setConnectTimeout( CONNECTION_TIMEOUT );
        con.setReadTimeout( READ_TIMEOUT );
        con.connect();
        stream = con.getInputStream();
        FileOutputStream fos = new FileOutputStream( file );
        bos = new BufferedOutputStream(fos);
        reclen = 0; //受信した量
        for(;;) {
            int size = stream.read( buf, 0, buf.length );
            if ( size < 0 ) {
                bos.flush();
                break;
            }
            bos.write( buf, 0, size );
            reclen += size;
        }
        bos.close();
        stream.close();
    }
    /**
     * urlからUTF-8のフォーマットのプロパティファイルをダウンロードし、propに
     * 読みこむ。一旦テンポラリファイルに格納してからプロパティを読みこむが、
     * 使用されたテンポラリはシステム終了時に自動削除される。
     * エラーになった場合、プロパティはロードされない。
     * @exception IOException I/Oエラーが発生したとき。
     * @exception java.io.FileNotFoundException サーバにファイルが見つからないとき
     * @exception java.net.SocketTimeoutException タイムアウトしたとき
     */
    public static void loadProp( URL url, Properties prop) throws IOException {
        File temp = File.createTempFile("starbase",null);
        temp.deleteOnExit();
        load( url, temp);
        FileInputStream fis = new FileInputStream(temp);
        prop.loadFromXML(fis);
    }
    /**
     * ファィルをコピーする。
     * @param src コピー元ファイル
     * @param dist コピー先ファィルまたはディレクトリ
     */
    public static void copy(File src,File dist) throws IOException {
        if ( src == null ) throw new IllegalArgumentException(
            "コピー元ファイル指定がnullです");
        if ( dist == null ) throw new IllegalArgumentException(
            "コピー先ファイル指定がnullです");
        if ( src.isDirectory() ) throw new UnsupportedOperationException(
            "ディレクトリのコピーはできません");
        //出力にディレクトリが指定された場合は、コピー元ファイル名をつける
        File distFile = dist.isDirectory() ?
            new File(dist,src.getName()) : dist;
        BufferedInputStream bis = new BufferedInputStream(
            new FileInputStream(src) );
        BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(distFile) );
        //サイズを10倍にすると、コピー速度は約倍になる。
        //この値だと35MBのコピーに3秒、10倍にすると1.5秒。
        byte [] buf = new byte[66000];
        int size = 0;
        while((size = bis.read(buf,0,buf.length)) != -1) {
            bos.write(buf,0,size);
        }
        bos.flush();
        bos.close();
        bis.close();
    }
    /**
     * システムリソースから指定されたテキストファイルをロードして返す。
     * @param resource リソースパス "/resources/..."
     * @param enc 文字エンコーディング
     */
    public static String getTextFile(String resource,String enc) throws IOException {
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder(4096);
        try {
            inputStream = FileLoader.class.getResource( resource ).openStream();
            reader = new BufferedReader( new InputStreamReader( inputStream,enc ) );
            char [] buf = new char[4096];
            int size = 0;
            while ( (size = reader.read(buf,0,buf.length)) != -1 ) {
                sb.append( buf, 0, size);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try { reader.close(); } catch ( Exception e) { }
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        File file = new File("c:/src/html/jws/eph.zip");
        File file2 = new File("c:/src/html");
        try {
            long t = System.currentTimeMillis();
            copy(file,file2);
            System.out.print(System.currentTimeMillis() - t);
            System.out.println(" [ms]");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
//    //テスト
//    public static void main(String [] args) {
//        Properties prop = new Properties();
//        try {
//            URL url = new URL("http://tetramorph.to/applib/update.properties");
//            loadProp(url,prop);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        for(Enumeration enu = prop.keys(); enu.hasMoreElements(); ) {
//            String key = (String)enu.nextElement();
//            String value = prop.getProperty(key);
//            System.out.println( key + " = " + value );
//        }
//    }
}
