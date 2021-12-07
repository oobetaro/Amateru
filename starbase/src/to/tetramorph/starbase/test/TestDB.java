/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/11/15 02:30
 */

package to.tetramorph.starbase.test;

import to.tetramorph.starbase.SplashOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author 大澤義鷹
 */
public class TestDB {
    public TestDB() {

    }
    class DBExecuter implements Runnable {
         @Override
        public void run() {
            try {
//                String dbfile = "file:" + Home.database.toURI().getPath();
                String dbfile = "file:" +
                        new File(System.getProperty("app.database"))
                        .toURI().getPath();
                System.out.println("dbfile = " + dbfile);
                org.hsqldb.Server
                .main( new String [] { "-database.0", dbfile, "-dbname.0", "" } );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private void startDB() throws IOException {
        SplashOutputStream sos = new SplashOutputStream(System.out);
        System.setOut(new PrintStream(sos));
        new Thread(new DBExecuter()).start();
        BufferedReader reader = new BufferedReader(sos.getReader());
        String line;
        while( (line = reader.readLine()) != null ) {
            System.err.println("##" + line);
        }
    }

//    public static void main(String [] args) throws IOException {
//        TestDB testdb = new TestDB();
//        testdb.startDB();
//    }
    public static void main(String [] args) throws IOException {
//        String file2 = "file:" + Home.database.toURI().getPath();
        String file2 = "file:" +
                new File(System.getProperty("app.database"))
                .toURI().getPath();
        System.out.println(file2);

    }
}
