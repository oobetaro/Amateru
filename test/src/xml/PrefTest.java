/*
 * PrefTest.java
 *
 * Created on 2008/11/30, 8:21
 *
 */

package xml;

import java.util.prefs.Preferences;

/**
 * Preferences�N���X���g���Ă݂�B
 * @author ���V�`��
 */
public class PrefTest {
    
    /**  PrefTest �I�u�W�F�N�g���쐬���� */
    public PrefTest() {
    }
    
    public static void main(String [] args) {
        Preferences pref = Preferences.systemNodeForPackage( PrefTest.class.getClass() );
        //pref.put("HogeHoge","�ق��ق�");
        System.out.println( pref.get("HogeHoge","unregist") );
    }
}
