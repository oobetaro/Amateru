/*
 * ImageSizeMenu.java
 *
 * Created on 2008/10/24, 9:37
 *
 */

package to.tetramorph.starbase;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * クリップボードやファイルに画像を保存するときのサイズを選択するメニュー。
 * ChartPaneの中でのみ使用されている。
 * 2011-07-29 レジストリの使用をやめた。
 * @author 大澤義孝
 */
class ImageSizeMenu extends JMenu {

    //ラジオ式メニューアイテムを格納するボタングループ
    private ButtonGroup buttonGroup = new ButtonGroup();
    private float height_per = -1;
    /**
     * ImageSizeMenu オブジェクトを作成する
     */
    public ImageSizeMenu() {
        super("画像サイズ選択");
    }

    /**
     * このメニューに格納されているサイズ選択用のメニューアイテムを作り直す。
     * @param height_per 横幅１に対して高さの比率値。
     * @param moduleName チャートモジュールのフルクラス名。選択番号を保存するとき
     * のキーに使用するため。
     * このメニューがDisenabledのとき、このメソッドはなにもしない。
     * Enabledのときはメニューを再作成する。
     * 選択できる画像サイズの横幅は640,800,1024,1280,1600から選択でき、縦の
     * ピクセル数はheight_perを掛けた値になる。
     */
    public void update( float height_per, final String moduleName ) {
        if ( ! isEnabled() ) return;
        this.height_per = height_per;
        removeAll();
        buttonGroup = new ButtonGroup();
        final String KEY = "ImageSizeSelection:" + moduleName;
        int [] widthArray = { 640,800,1024,1280,1600 };
        for ( int i = 0; i < widthArray.length; i++ ) {
            int width = widthArray[i];
            int height = (int)( width * height_per );
            String text = String.format( "%d x %d",width, height );
            MenuItem item = new MenuItem( text, new Dimension( width, height ) );
            item.setActionCommand( "" + i );
            item.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent evt ) {
                    String num = ((MenuItem)evt.getSource()).getActionCommand();
                    Config.usr.setProperty( KEY , num );
                }
            });
            add( item );
            buttonGroup.add( item );
        }
        int num = Config.usr.getInteger(KEY, 2);
        ((MenuItem)getItem( num )).setSelected( true ); //暫定
    }

    public float getHeightPer() {
        return height_per;
    }
    // 画面サイズ選択のためのメニューアイテムで、Dimensionの保持が目的。
    private class MenuItem extends JRadioButtonMenuItem {
        Dimension size;
        MenuItem( String text, Dimension size ) {
            super(text);
            this.size = size;
        }
    }

    /**
     * このメニューの中で選択されている画面サイズを返す。
     */
    public Dimension getSelectedDimension() {
        for ( int i = 0; i < getItemCount(); i++ ) {
            MenuItem item = (MenuItem)getItem(i);
            if ( item.isSelected() ) return item.size;
        }
        return null;
    }
}
