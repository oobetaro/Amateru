/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.test;

/**
 *
 * @author 大澤義鷹
 */
public class StringTest {
    public static void main(String args[]) {
        char[] array = "大澤義孝".toCharArray();
        StringBuilder sb = new StringBuilder();
        for ( int i=0; i<array.length; i++ ) {
            int value = (int)array[i];
            sb.append( String.format("&#%d;", value) );
        }
        System.out.println(sb.toString());
    }
}
