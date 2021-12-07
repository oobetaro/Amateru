/*
 * AspectCirclePopupMenu.java
 *
 * Created on 2008/10/01, 15:06
 *
 */

package to.tetramorph.starbase.chartmodule;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * アスペクト円内でアスペクト線を操作するメニュー。
 * @author 大澤義孝
 */
public class AspectCirclePopupMenu extends JPopupMenu {
//    JCheckBoxMenuItem item0 = new JCheckBoxMenuItem("アスペクト線を表示");
    /**
     *アスペクト線を全て表示
     */
    protected JMenuItem item1 = new JMenuItem("アスペクト線を全て表示");
    /**
     *アスペクト線を非表示
     */
    protected JMenuItem item2 = new JMenuItem("アスペクト線を非表示");
    /**
     *選択線を隠す
     */
    protected JMenuItem item3 = new JMenuItem("選択線を隠す");
    /**
     *選択線以外を隠す
     */
    protected JMenuItem item4 = new JMenuItem("選択線以外を隠す");
    /**
     *すべての選択を解除
     */
    protected JMenuItem item5 = new JMenuItem("すべての選択を解除");
    /**
     *天体リングとアスペクトの設定
     */
    protected JMenuItem item6 = new JMenuItem("天体リングとアスペクトの設定");
    /**
     *オーブの設定
     */
    protected JMenuItem item7 = new JMenuItem("オーブの設定");
    
    /**  AspectCirclePopupMenu オブジェクトを作成する */
    public AspectCirclePopupMenu() {
        super();
        add( item1 );
        add( item2 );
//        item0.setSelected(true);
//        add( item0 );
        add( new JSeparator() );
        add( item3 );
        add( item4 );
        add( item5 );
        add( new JSeparator() );
        add( item6 );
        add( item7 );
    }
    
}
