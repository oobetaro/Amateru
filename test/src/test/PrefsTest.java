/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.prefs.Preferences;

/**
 * Preferencesはいろいろな型のデータを保管できるが、putBoolean("hoge")として
 * get("hoge")としたとき、値はどうなってしまうのだろうか。
 * それをテストする。
 * ついでに登録キーをまとめて読み出せるかもテストする。
 *
 * 結果は型が違っても文字列として取り出せる。もちろんすべての型をテストはしない。
 * 中にはだめなのもあるかもしれない。しかしせいぜい、文字列、整数、Booleanが
 * 使えればよいのでそれだけをテストした。
 * @author ohsawa
 */
public class PrefsTest {
    public static void main( String [] args ) throws Exception {
        Preferences data = Preferences.userNodeForPackage( PrefsTest.class );
        data.putBoolean( "TestBoolean", true );
        System.out.println( data.get("TestBoolean", "?") ); // "true"
        data.putInt("TestBoolean",12345);
        System.out.println( data.get("TestBoolean", "?") ); // "12345"
        data.put("Hoge", "ほげほげうるさい");
        // data.keys()はExceptionを出す。うるさい!
        for ( String key : data.keys() ) {
            System.out.println( key + " = " + data.get(key, "?") );
        }
        data.remove("TestBoolean");
        data.remove("Hoge");
    }
}
