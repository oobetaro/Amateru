/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * ドキュメントの暗号（というよりは難読化）を行う。
 * @author 大澤義鷹
 */
public class CipherUtils {
    /**
     * getDigestで作成したバイト配列を16進数文字列に変換して返す。
     * getDigestメソッドで作成したもの以外は考慮していない。
     * @param array
     */
    public static String getString( byte [] array ) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for ( byte b : array ) {
            sb.append( String.format("%02x", b) );
        }
        return sb.toString();
    }
    /**
     * getStringで作成されたパスワードの16進数文字列を元のbyte配列に変換して返す。
     */
    public static byte [] getBytes( String password ) {
        if ( (password.length() % 2) != 0 )
            throw new java.lang.IllegalArgumentException("異常な16進数文字列");
        byte [] temp = new byte[ password.length() / 2 ];
        for ( int i = 0,j = 0; i < password.length(); i+=2,j++ ) {
            String value = password.substring(i, i+2);
            temp[j] = Integer.decode("0x"+ value).byteValue();
        }
        return temp;
    }
    /**
     * パスワード文字列からダイジェストを生成しbyte配列で返す。
     * これはパスワードをハッシュするための関数で、
     * MD5のアルゴリズムでハッシュされた値を返す。
     */
    public static byte[] getDigest(String password) {
        byte [] temp = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update( password.getBytes() );
            temp = md.digest();
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * テキストを暗号化する。
     * @param password 復号化のときに使用するパスワード
     * @param text 暗号化したいテキスト。
     * @return 暗号化されたテキストを格納したbyte配列。文字コードセットはUTF-8
     * である。
     */
    public static byte[] encrypt(byte [] password, byte [] text) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec sksSpec = new SecretKeySpec( password, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
        return cipher.doFinal( text );
    }

    /**
     * テキストを複合化する。
     * @param password パスワード
     * @param encryptedBytes 暗号化されている文字配列
     * @return 復号か化されたバイト文字配列
     */
    public static byte[] decrypt( byte[] password,
                                     byte [] encryptedBytes) throws
                                         NoSuchAlgorithmException,
                                         NoSuchPaddingException,
                                         InvalidKeyException,
                                         IllegalBlockSizeException,
                                         BadPaddingException {
        SecretKeySpec sksSpec =
                new SecretKeySpec( password, "Blowfish" );
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, sksSpec);
        return cipher.doFinal(encryptedBytes);
    }

//    public static void main( String [] args ) throws Exception {
//        byte [] encrypted = encrypt("HogeHoge","今日はちっとも仕事がはかどらないっす！");
//        String text = new String( decrypt("HogeHoge",encrypted), Charset.forName("UTF-8"));
//        System.out.println( text );
//    }

}
