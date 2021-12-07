/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FileTest.java
 *
 * Created on 2009/11/17, 1:51:21
 */

package to.tetramorph.starbase.test;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author 大澤義鷹
 */
public class FileTest {
    public static void main(String [] args) throws IOException {
        File file = new File("c:/jkdfjdfd.txt");
        System.out.println("exsist = " + file.exists());
        System.out.println("len = " + file.length());
    }
}
