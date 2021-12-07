/*
 * PrefTest.java
 *
 * Created on 2008/11/30, 8:21
 *
 */

package xml;

import java.util.prefs.Preferences;

/**
 * Preferencesクラスを使ってみる。
 * @author 大澤義鷹
 */
public class PrefTest {
    
    /**  PrefTest オブジェクトを作成する */
    public PrefTest() {
    }
    
    public static void main(String [] args) {
        Preferences pref = Preferences.systemNodeForPackage( PrefTest.class.getClass() );
        //pref.put("HogeHoge","ほげほげ");
        System.out.println( pref.get("HogeHoge","unregist") );
    }
}
