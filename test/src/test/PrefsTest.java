/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.prefs.Preferences;

/**
 * Preferences�͂��낢��Ȍ^�̃f�[�^��ۊǂł��邪�AputBoolean("hoge")�Ƃ���
 * get("hoge")�Ƃ����Ƃ��A�l�͂ǂ��Ȃ��Ă��܂��̂��낤���B
 * ������e�X�g����B
 * ���łɓo�^�L�[���܂Ƃ߂ēǂݏo���邩���e�X�g����B
 *
 * ���ʂ͌^������Ă�������Ƃ��Ď��o����B������񂷂ׂĂ̌^���e�X�g�͂��Ȃ��B
 * ���ɂ͂��߂Ȃ̂����邩������Ȃ��B���������������A������A�����ABoolean��
 * �g����΂悢�̂ł��ꂾ�����e�X�g�����B
 * @author ohsawa
 */
public class PrefsTest {
    public static void main( String [] args ) throws Exception {
        Preferences data = Preferences.userNodeForPackage( PrefsTest.class );
        data.putBoolean( "TestBoolean", true );
        System.out.println( data.get("TestBoolean", "?") ); // "true"
        data.putInt("TestBoolean",12345);
        System.out.println( data.get("TestBoolean", "?") ); // "12345"
        data.put("Hoge", "�ق��ق����邳��");
        // data.keys()��Exception���o���B���邳��!
        for ( String key : data.keys() ) {
            System.out.println( key + " = " + data.get(key, "?") );
        }
        data.remove("TestBoolean");
        data.remove("Hoge");
    }
}
