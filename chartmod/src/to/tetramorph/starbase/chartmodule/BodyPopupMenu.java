/*
 * BodyPopupMenu.java
 *
 * Created on 2008/10/01, 15:51
 *
 */

package to.tetramorph.starbase.chartmodule;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * 天体を右クリックして出すメニュー。
 * @author 大澤義孝
 */
public class BodyPopupMenu extends JPopupMenu {
    /**
     *この天体とのアスペクトを表示
     */
    protected JMenuItem item1 = new JMenuItem("この天体とのアスペクトを表示");
    /**
     *サビアン辞書を表示
     */
    protected JMenuItem item2 = new JMenuItem("サビアン辞書を表示");
    /**
     *天体の表示/非表示の設定
     */
    protected JMenuItem item3 = new JMenuItem("天体の表示/非表示の設定");
    /**
     *アスペクトを検出する天体を設定
     */
    protected JMenuItem item4 = new JMenuItem("アスペクトを検出する天体を設定");
    /**
     *進行法の設定
     */
    protected JMenuItem item5 = new JMenuItem("進行法の設定");
    /**
     *ハウスの設定
     */
    protected JMenuItem item6 = new JMenuItem("ハウスの設定");
    
    /**  BodyPopupMenu オブジェクトを作成する */
    public BodyPopupMenu() {
        super();
        add( item1 );
        add( item2 );
        add( new JSeparator() );
        add( item3 );
        add( item4 );
        add( item5 );
        add( item6 );
    }
    
}
