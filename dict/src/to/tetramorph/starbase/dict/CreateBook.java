/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import to.tetramorph.util.AmateruBookFileFilter;

/**
 * 本の新規作成を実行するクラス。
 * ファイルチューザーを開いて、新規作成するファイル名をユーザに尋ねる。<br>
 * jar書庫内ならか、指定されたテンプレートファイルを、指定された名前でファイルに
 * コピーする。といったメソッドからなる。
 * @author 大澤義鷹
 */
class CreateBook {
    static JFileChooser chooser = new JFileChooser();
    static AmateruBookFileFilter abf_filter = new AmateruBookFileFilter();
    /**
     * 指定されたリソースを、指定されたファイルにコピーする。
     * @param resource_path jar書庫内のパス
     * @param targetFile 出力先のファイルパス
     * @return trueならコピー成功、falseなら失敗
     */
    static boolean copy( String resource_path, File targetFile) {
        URL url = CreateBook.class.getResource(resource_path);
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        boolean result = true;
        try {
            outputStream = new FileOutputStream( targetFile );
            inputStream = url.openStream();
            int len = 0;
            byte [] buf = new byte[4096];
            while ( (len = inputStream.available()) > 0 ) {
                if ( len > 4096 ) len = 4096;
                inputStream.read( buf, 0, len);
                outputStream.write(buf, 0, len);
            }
        } catch ( IOException ex ) {
            result = false;
            Logger.getLogger(CreateBook.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try { inputStream.close(); } catch (Exception e ) { }
            try { outputStream.close();} catch (Exception e ) { }
        }
        return result;
    }
    /**
     * 辞書のテンプレートファイルを、指定された名前で複製する。
     * ファイルチューザーを開き、ユーザにファイル名を入力させる。
     * resourcePathで指定したファイルをコピーする。
     * @param parent 親コンポーネント
     * @param createName チューザーに表示するデフォルト名。たとえば「新規文書」
     * とか「サビアン辞書」とか、ファイルの名前を指定する。
     * @param resourcePath jar書庫内にあるテンプレートファイル.abfへのパス
     * @return チューザーで指定されたファイル。中止された場合はnull。
     */
    public static File createBook( Component parent,
                                       String createName,
                                       String resourcePath) {
        String PATH_KEY = "FileChooserPath";
        String defaultPath = Config.usr.getProperty( PATH_KEY,
                                          System.getProperty("app.userdict"));
        if ( ! defaultPath.isEmpty() ) {
            File dir = new File(defaultPath);
            if ( dir.isDirectory() )
                chooser.setCurrentDirectory(dir);
        }
        chooser.removeChoosableFileFilter( chooser.getFileFilter() );
        chooser.setFileFilter( abf_filter );
        chooser.setDialogTitle( "本の新規作成" );
        chooser.setDialogType( JFileChooser.SAVE_DIALOG );
        //chooser.setCurrentDirectory( Home.dir );
        File defaultFile = new File( createName );
        chooser.setSelectedFile(defaultFile);
        int result = chooser.showSaveDialog(null);
        if ( result != JFileChooser.APPROVE_OPTION )
            return null;
        File file = chooser.getSelectedFile();
        File dir = chooser.getCurrentDirectory();
        //ファイル名ボックスが空のとき保存ボタンは押せないからnullが来る事はない
        String name = file.getName();
        if ( ! name.matches(".*(\\.abf)$")) name += ".abf";
        File targetFile = new File( dir, name );
        if ( targetFile.exists() ) {
            int res = JOptionPane.showConfirmDialog(parent,
                    String.format("“%s”はすでに存在します。\n" +
                    "上書きしてもよろしいですか？", name),
                    "本の新規作成",JOptionPane.YES_NO_OPTION );
            if ( res != JOptionPane.YES_OPTION )
                return null;
        }
        if ( ! copy( resourcePath, targetFile )  ) return null;
        if ( targetFile.getParentFile() != null ) {
            Config.usr.setProperty( PATH_KEY,
                      targetFile.getParentFile().getAbsolutePath());
        }
        return targetFile;
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                File file = createBook(null,"サビアン辞典","/resources/dict_sabian_template.abf");
                System.out.println(file);
            }
        });
    }
}
