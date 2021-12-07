/*
 * ImageSaveFileChooser.java
 *
 * Created on 2008/10/23, 8:09
 *
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import to.tetramorph.util.ImageFileFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import to.tetramorph.util.FileFilterUtils;
import to.tetramorph.util.ImageFileView;

/**
 * 画像ファイル保存用チューザー
 * 2011-07-29 レジストリの使用をやめた。
 * @author 大澤義孝
 */
public class ImageSaveFileChooser {

    private JFileChooser fc = new JFileChooser();
    private Set<String> suffixes = new HashSet<String>();
    private Map<String,ImageFileFilter> filterMap =
                                         new HashMap<String,ImageFileFilter>();
    /**
     *  ImageSaveFileChooser オブジェクトを作成する
     */
    public ImageSaveFileChooser() {
//        for( String name : ImageIO.getWriterFormatNames() ) {
//            suffixes.add( name );
//        }
        suffixes.addAll(Arrays.asList(ImageIO.getWriterFormatNames()));
        for ( int i=0; i < ImageFileFilter.FORMAT_NAMES.length; i++ ) {
            String name = ImageFileFilter.FORMAT_NAMES[ i ];
            if ( suffixes.contains( name ) ) {
                ImageFileFilter filter = new ImageFileFilter( name );
                //filterList.add( filter );
                filterMap.put( name, filter );
                fc.addChoosableFileFilter( filter );
            }
        }
        fc.setFileFilter( filterMap.get("png") );
        fc.setAcceptAllFileFilterUsed( false );
        fc.setFileView( new ImageFileView() );
        fc.setApproveButtonMnemonic('Y');
        fc.setApproveButtonText("保存(Y)");

        fc.setDialogTitle("画像ファイルの保存");
    }

    /**
     * ファイルチューザーを開き出力ファイル名を入力させ画像を保存する。
     * @param owner 親コンポーネント
     * @param dir   カレントディレクトリ(nullならデフォルト値を使用)
     * @param defaultName ファイル名のデフォルト値
     * @param image 保存したいイメージオブジェクト
     */
    public void showSaveDialog( Component owner,
                                  File dir,
                                  String defaultName,
                                  BufferedImage image ) {
        String path = Config.usr.getProperty( "ImageSaveDialog.currentDirectory","" );
        if ( dir != null )
            fc.setCurrentDirectory( dir );
        else if ( ! path.isEmpty() ) {
            fc.setCurrentDirectory( new File(path) );
        }

        if ( defaultName != null)
            fc.setSelectedFile( new File( defaultName ));
        String filter = Config.usr.getProperty("ImageSaveDialog.format","png");
        ImageFileFilter f = filterMap.get( filter );
        if ( f == null ) f = filterMap.get("png");
        fc.setFileFilter( f );

        int result = fc.showSaveDialog( owner );
        if ( result == JFileChooser.CANCEL_OPTION ) return;
        String format = ((ImageFileFilter)fc.getFileFilter()).getFormatName();
        File inputFile = fc.getSelectedFile();
        String input_ext = FileFilterUtils.getExtension( inputFile );
        if ( input_ext == null ) {
            inputFile = new File( inputFile.getAbsolutePath() + "." + format );
        } else if ( suffixes.contains( input_ext ) ) {
            if ( ! input_ext.equals( format ) ) {
                if ( input_ext.equals("jpeg") || input_ext.equals("tiff") ) {
                } else {
                    inputFile = new File( inputFile.getAbsolutePath() + "." + format );
                }
            }
        } else {
            inputFile = new File( inputFile.getAbsolutePath() + "." + format );
        }
        //以上ここまででinputFileに入力ファイルが求まる
        if ( inputFile.exists() ) {
            String mes = String.format( "「%s」に上書きしますか？",inputFile.getName() );
            int res = JOptionPane.showConfirmDialog( owner, mes,
                                                     "画像ファイルの保存",
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.WARNING_MESSAGE );
            if ( res == JOptionPane.NO_OPTION ) return;
        }
        Config.usr.setProperty( "ImageSaveDialog.currentDirectory",
                  fc.getCurrentDirectory().getAbsolutePath() );
        Config.usr.setProperty( "ImageSaveDialog.format", format );
        //File currentFile = fc.getCurrentDirectory();
        try {
            ImageIO.write( image, format, inputFile );
        } catch ( IOException e ) {
            Logger.getLogger(ImageSaveFileChooser.class.getName())
                    .log(Level.SEVERE,null,e);
        }
    }
}
