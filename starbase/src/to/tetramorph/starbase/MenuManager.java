/*
 * MenuManager.java
 *
 * Created on 2007/11/16, 10:11
 *
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * メニューオブジェクトは名前をつけてこのオブジェクトに入れておく。
 * メニューが増えてくると、いくつかのクラスからメニューを相互参照する必要が出て
 * くるが、いちいちコンストラクタで参照を渡すのも煩わしい。名前をつけてグローバル
 * 変数のようなものとみなして対処する。
 * @author 大澤義鷹
 */
class MenuManager {
    private static final Map<String,Object> map = new HashMap<String,Object>();
    
    public static JMenuItem getMenuItem(String name) {
        return (JMenuItem)map.get(name);
    }
    
    public static JMenu getMenu(String name) {
        return (JMenu)map.get(name);
    }
    
    public static JButton getButton(String name) {
        return (JButton)map.get(name);
    }
    
    public static JComboBox getComboBox( String name ) {
        return (JComboBox)map.get( name );
    }
    public static Object get( String name ) {
        return map.get( name );
    }
    public static void put(String key,Object object) {
        map.put(key, object);
    }
}
