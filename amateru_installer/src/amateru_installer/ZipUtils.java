/*
 * ZipUtils.java
 *
 * Created on 2007/11/21, 16:46
 *
 */

package amateru_installer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ZIP書庫の展開処理を行うstaticメソッド群でファイルコピーやフォルダの再帰的削除
 * メソッドなどもある。
 * @author 大澤義鷹
 */
public class ZipUtils {

    static final int BUFFER = 2048;
    /**
     * 入力ストリームからZIP形式のデータを読込、指定されたフォルダに展開する。
     * 展開し終わったあと、streamはクローズしない。
     * @param stream ZIP形式のデータを返すストリーム
     * @param distDir 展開先フォルダ
     * @exception IOException
     * @exception DataFormatException CRC32のチェックサムに不一致がある場合
     */
    public static void extract(InputStream stream,File distDir)
                                     throws IOException,DataFormatException  {

        BufferedInputStream bis = new BufferedInputStream(stream);
        ZipInputStream zipStream = new ZipInputStream(bis);
        ZipEntry entry = null;
        byte[] buf = new byte[ BUFFER ];
        CRC32 crc32 = new CRC32();
        while ((entry = zipStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            if ( entry.isDirectory() ) {
                new File( distDir, entryName ).mkdirs();
                //System.out.println("mkdir1 : " + new File(distDir,entryName));
            } else {
                File entryFile = new File( distDir, entryName );
                //System.out.println("entryFile : " + entryFile);
                File parentFile = entryFile.getParentFile();
                if ( ! parentFile.exists() ) {
                    //通常一度しか実行されない。最初のdistDiを作成する場合。*1
                    parentFile.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream( entryFile );
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                CheckedOutputStream out =
                              new CheckedOutputStream(bos, crc32); // *2
                int total = 0;
                //書きだし中のエラーはfinallyで捕捉されてからthrowされる。
                try {
                    for ( int size = 0;;) {
                        size = zipStream.read(buf,0,buf.length); // *3
                        if(size == -1) break;
                        total += size;
                        out.write(buf, 0, size);
                    }
                    out.flush();
                } catch ( IOException e ) {
                    throw e;
                } finally {
                    try { out.close(); } catch ( Exception e ) { }
                }
                if ( entry.getCrc() != out.getChecksum().getValue()) {
                    zipStream.closeEntry();
                    zipStream.close();
                    throw new DataFormatException("CRC discrepancy");
                }
                crc32.reset(); // *2'
            }
            zipStream.closeEntry();
        }
    }
    //
    // *1  たとえば1度目のwhile()ループで、entryがファィルだったとする。この時点
    //     では、if文のentry.isDirectory()は実行されていない。このファィルの親
    //     フォルダは、ほかならぬdistDirである。distDirはこの時点で作成されてない
    //     から、parentFile.exists()はfalseを返す。そしたらmkdirs()する。
    //
    // *2  この位置でnew CRC32()とはせず、*2'のように使い終わったら
    //     reset()してやれば、newしなくても済む。
    //
    // *3  一度に読み込まれるサイズは時には上限の2048まで増えることもあるが、
    //     ほとんどは550bytes程度であり、このバッファサイズは適切だと思える。

    /**
     * ZIP形式のファィルを、展開先フォルダに展開する。例外発生の際、使用した
     * ストリームはクローズされるが、途中まで展開したファイルは削除されない。
     * @param srcZip ZIPファイル
     * @param distDir 展開先フォルダ
     * @exception IOException
     * @exception DataFormatException CRC32のチェックサムに不一致がある場合
     */
    public static void extract(File srcZip,File distDir)
                                    throws IOException, DataFormatException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcZip);
            extract( fis, distDir );
        } catch ( IOException e ) {
            throw e;
        } catch ( DataFormatException e ) {
            throw e;
        } finally {
            try { fis.close(); } catch ( Exception e ) { }
        }
    }

    /**
     *再帰処理でディレクトリ内のファイルパスをListに格納する。
     */
    private static void getFiles( List<File> fileBuffer, File dir) {
        File cdir = new File(dir,".");
        String files[] = cdir.list();
        for ( int i=0; i<files.length; i++) {
            File file = new File( dir, files[i] );
            if ( file.isDirectory() ) {
                fileBuffer.add( file );
                getFiles( fileBuffer, file );    //再帰
            } else {
                fileBuffer.add( file );
            }
        }
    }
    /**
     * 指定されたディレクトリ内に存在しているファイルやサブディレクトリを再帰的
     * に検索したリストを返す。
     */
    public static List<File> getFiles( File dir ) {
        List<File> list = new ArrayList<File>();
        getFiles(list,dir);
        return list;
    }
    /**
     * 指定されたディレクトリとそのサブディレクトリをまとめて削除する。
     * @param dir 削除するディレクトリ
     * @param last ディレクトリの名前 (dirだけあれば削除できるのだが、バグや設定
     * ミスによってルートであるとかとんでもないパスが与えられたりする悲惨な事故を
     * 多少なりとも防止するための安全キー。)<br>
     * dirで指定したパスの最後のディレクトリ名を与える。
     */
    public static void rmdirs( File dir, String last ) {
        if ( ! dir.getAbsolutePath().endsWith(last) )
            throw new java.lang.IllegalArgumentException(
                "引数で与えられたパス名が一致しないため削除はできません。");
        List<File> list = getFiles( dir );
        for ( int i=list.size() - 1; i >= 0; i-- )
            list.get(i).delete();
    }
     /**
     * ファイルをコピーする。ただしフォルダの一括コピーはできない。
     * @param src コピー元ファイル
     * @param dist コピー先ファイルまたはフォルダ。フォルダを指定した場合
     * そのフォルダにコピー元ファイルの名前でコピーされる。
     *
     * @exception IllegalArgumentException srcやdistがnullの場合。
     * @exception IOException I/Oエラーが発生したケース。ただし内部のストリーム
     * は閉じたあとスローされる。
     */
    public static void copy( File src, File dist ) throws IOException {
        if(src == null || src.isDirectory() || dist == null)
            throw new java.lang.IllegalArgumentException("");
        if(dist.isDirectory())
            dist = new File(dist,src.getName());

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream ( new FileInputStream ( src  ) );
            bos = new BufferedOutputStream( new FileOutputStream( dist ) );
            byte [] buf = new byte[64 * 1024];
            for(;;) {
                int size = bis.read( buf, 0, buf.length );
                if ( size == -1 ) break;
                bos.write( buf, 0, size);
            }
            bos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            try { bis.close(); } catch ( Exception e ) { }
            try { bos.close(); } catch ( Exception e ) { }
        }
    }

//    public static void main(String args[]) {
//        List<File> list = getFiles(new File("c:/sb/jws"));
//        for ( int i=0; i<list.size(); i++) {
//            System.out.println( list.get(i) );
//        }
//    }
//    public static void main(String args[]) {
//        String homedir = System.getProperty("user.home");
//        File file = new File("c:/src/html/jws/eph.zip");
//        File distDir = new File("c:/src/html/jws/hoge");
//        long t = System.currentTimeMillis();
//        try {
//            extract( file, distDir);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(System.currentTimeMillis() - t);
//    }

}
