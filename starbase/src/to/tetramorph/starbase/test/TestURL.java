/*
 * TestURL.java
 *
 * Created on 2008/01/06, 13:15
 *
 */

package to.tetramorph.starbase.test;

import java.io.File;

/**
 * JDK1.6からFile#toURL()が推奨されなくなったので、他の方法を探す。
 * @author 大澤義鷹
 */
public class TestURL {
    
    /**  TestURL オブジェクトを作成する */
    public TestURL() {
    }
    
    public static void main(String [] args) throws Exception {
        File file = new File("test.html");
        File canonicalFile = file.getCanonicalFile();
        System.out.println("file = " + file.toString());
        System.out.println("canonicalFile = " + canonicalFile.toString());
//        System.out.println("filt to URL = " + file.toURL());
//        System.out.println("canonicalFile to URL = " + canonicalFile.toURL());
        System.out.println("filt to URI = " + file.toURI());
        System.out.println("canonicalFile to URI = " + canonicalFile.toURI());

//        System.out.println(file.toURL().toString());
        System.out.println(file.toURI().toURL().toString());
    }
}
