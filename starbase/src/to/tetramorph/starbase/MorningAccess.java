/*
 * MorningAccess.java
 *
 * Created on 2008/11/07, 2:48
 *
 */

package to.tetramorph.starbase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * 　アマテル起動時に"http://tetramorph.to/amateru_log/logger.cgi"にアクセスする。
 * logger.cgiは祝福装置(笑)で、はじめてアクセスしてきたアマテルにユニークIDを
 * 発行する。一度IDをもらったアマテルは以後、起動するたびにもらったIDと
 * ローカルマシンで稼働した時間（単位：秒）を送信する。
 * そのときlogger.cgiは、ID,アクセスがあった時刻、リモートアドレスをログに記録
 * する。記録する情報はこれが全てである。<br>
 * 　送信しているのはIDのみであり、その他一切のプライバシーにかかわる情報は送信
 * しない。<br>
 * 　IDをそれぞれのアマテルに割り当てるのは、アマテルの稼働台数をある程度正確に
 * 知るためである。一日何人がアマテルを使うか、一週間ではどうか、一ヶ月ではどうか。
 * 生きているニワトリの数を概算するためにlogger.cgiがある。<br>
 * <br>
 * これはアマテルの普及度を知るための重要な機能。
 * @author 大澤義孝
 */
class MorningAccess {

    private static final int CONNECTION_TIMEOUT = 20000; // [ms]
    private static final int READ_TIMEOUT       = 20000; // [ms]
    //private static final String amateru_id = "AMATERU_ID";

    //外部からインスタンスを作ることを禁止
    private MorningAccess() {
    }

    /**
     * 内部クラスを別スレッドで実行。祝福サーバへのアクセス開始。
     **/
    private void start() {
        new Thread( new Access() ).start();
    }

    private class Access implements Runnable {
        @Override
        public void run() {
            InputStream stream = null;
            BufferedReader reader = null;
            try {
                String id = Config.usr.getProperty( "AMATERU_ID", "My name is AMATERU.");
                StringBuilder urlBuf = new StringBuilder(150);
                urlBuf.append( "http://tetramorph.to/amateru_log/logger.cgi?ID=" );
                urlBuf.append( URLEncoder.encode( id, "UTF-8" ) );
                urlBuf.append( "&age=" );
                long age = Config.usr.getLong( "AgeOfAmateru", 0L ) ;
                urlBuf.append( age );
                String path = urlBuf.toString();
                System.out.println( "Access to benediction server:" + path );
                System.out.println( "Send ID  :" + id );
                System.out.println( "Send age :" + age + " [sec]" );
                URL url = new URL( path );
                URLConnection con = url.openConnection();
                con.setConnectTimeout( CONNECTION_TIMEOUT );
                con.setReadTimeout( READ_TIMEOUT );
                con.connect();
                stream = con.getInputStream();
                reader = new BufferedReader( new InputStreamReader( stream ));
                String line = "";
                StringBuilder sb = new StringBuilder( 80 );
                while ( ( line = reader.readLine()) != null) {
                    sb.append( line );
                }
                String result = sb.toString();
                System.out.println( "Benediction server response:"+ result );
                if ( result.startsWith("Your name is ") ) {
                    Config.usr.setProperty( "AMATERU_ID", result.substring(13) );
                } else if ( result.equals("I don't know you.") ) {
                    Config.usr.remove( "AMATERU_ID" );
                } else if ( result.equals("busy") ) {

                }
                Config.save();
            } catch ( Exception e ) {
                System.out.println( "Benediction failed." );
                System.out.println( e.toString() );
            } finally {
                try { reader.close(); } catch ( Exception e ) { }
            }
        }
    }

    /**
     * 祝福サーバにアクセスして起きたことを報告。このクラスの唯一のエントリー。
     * 引数はなにを指定しても無視する。
     */
    public static void main( String [] args ) {
        new MorningAccess().start();
    }
}
