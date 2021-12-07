/*
 * ListProperites.java
 *
 * Created on 2006/12/27, 8:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package to.tetramorph.starbase.test;

import java.util.Enumeration;
import java.util.Properties;

/**
 * System.getProperties()のキーと値の一覧を標準出力にプリントする。
 * @author 大澤義鷹
 */
public class ListProperites {

    /** Creates a new instance of ListProperites */
    public ListProperites() {
    }

    public static void main(String args[]) {
        Properties p = System.getProperties();
        Enumeration<Object> enu;
        for (enu = p.keys(); enu.hasMoreElements();) {
            String key = (String) enu.nextElement();
            String value = System.getProperty(key);
            System.out.println(key + " = " + value);
        }
    }
}
