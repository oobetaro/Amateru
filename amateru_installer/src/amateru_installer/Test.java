/*
 * Test.java
 * Created on 2011/07/17, 23:08:49.
 */
package amateru_installer;

import java.io.File;

/**
 * 正規表現の動作チェックをするためのテスト用。
 * @author ohsawa
 */
class Test {
    static String [] files = {
        "starbase.jar",
        "lib/applib.jar",
        "lib/lib.jar",
        "smod/searchmod.jar",
        "cmod/chartmod.jar",
        "data/ephe.zip",
        "data/place.zip"
    };
    /*
     *  lib/以下のすべて "lib/.*"
     *  jarファイルのみ  ".*\\.jar"
     *
     */
//    public static void main(String [] args ) throws Exception {
//        List<String> list = new ArrayList<String>();
//        list.addAll(Arrays.asList(files));
//
//        for (String path : list ) {
//            if ( path.matches("lib/.*\\.jar")) {
//                System.out.println( path );
//            }
//        }
//    }
    public static void main(String [] args ) throws Exception {
        File javaw = new File("javaw");
        System.out.println(javaw.getAbsolutePath());
        //C:\Users\ohsawa\Documents\NetBeansProjects\Amateru\amateru_installer\javaw
    }

}
