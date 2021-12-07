/*
 * MutexServer.java
 *
 * Created on 2007/11/16, 22:25
 *
 */

package to.tetramorph.starbase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * アプリの二重起動を抑止するためのサーバ。ポートを開いてコマンドを待ち受け、
 * 所定のコマンドが来れば、リスナを実行したり、サーバ停止を行う。
 * アプリのシャトダウン時には、このサーバのabort()を使ってサーバを停止させるべき
 * だが、シャットダウンのときは、そのVMのスレッドは全て停止するから停止させる
 * 必要はないかもしれない。
 * @author 大澤義鷹
 */
final class MutexServer implements Runnable {
    private static final String ABORT = "THREAD_ABORT";
    private static final String CHECK = "LISTENER EXECUTE";
    private MutexListener l = null;
    private int port;
    
    private MutexServer() {}
    
    /**
     * MutexServer オブジェクトを作成する
     * @param port 適当なポート番号
     * @param l MutexListenerを実装したオブジェクト
     */
    private MutexServer(int port,MutexListener l) {
        this.l = l;
        this.port = port;
    }
    /**
     * サーバの本体。プログラマは呼び出す必要はない。
     */
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            LOOP:
                for (;;) {
                Socket connect = server.accept();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(connect.getInputStream()));
                    for (;;) {
                        String echo = in.readLine();
                        if ( echo == null || echo.equals("")) break;
                        if ( echo.equalsIgnoreCase( CHECK ) && l != null)
                            l.mutexPerformed();
                        if ( echo.equalsIgnoreCase( ABORT )) {
                            connect.close();
                            server.close();
                            break LOOP;
                        }
                    }
                connect.close();
                }
        } catch ( Exception err ) {
            System.out.println("MutexServer : ");
            err.printStackTrace();
        }
    }
    
    /**
     * サーバを起動する。
     * @param port ポート番号
     * @param l リスナ
     * @return 起動に成功すればtrue、失敗またはすでに起動してるならfalseを返す。
     */
    public static boolean exec(int port,MutexListener l) {
        try {
            new Thread( new MutexServer(port,l) ).start();
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * MutexServerが動いているときはtrueを返すとともに、サーバーに対して、
     * 登録されているリスナを実行するよう指示を出す。
     * @param port ポート番号 exec()で指定したものと同じものを使用すること。
     */
    public static boolean isRunning(int port) {
        try {
            Socket sock = new Socket("localhost" , port);
            sock.close();
        } catch ( Exception e) {
            return false; //サーバは動いてない
        }
        check(port);
        return true;    //サーバが動いている
    }
    
    /**
     * サーバを停止する。サーバが稼働していないときに呼び出すとfalseが返る。
     */
    public static boolean abort(int port) {
        return send(port,ABORT);
    }
    
    /**
     * サーバにシグナルを送る。シグナルを受け取ったサーバーは、リスナを実行する。
     */
    private static boolean check(int port) {
        return send(port,CHECK);
    }
    
    //サーバーにコマンドを送信
    private static boolean send(int port,String cmd) {
        try {
            Socket sock = new Socket("localhost" , port);
            OutputStreamWriter writer = 
                new OutputStreamWriter(sock.getOutputStream());
            writer.write( cmd + "\n" );
            writer.flush();
            sock.close();
        } catch(Exception err) {
            System.out.println("Client : " + err);
            return false;
        }
        return true;
    }
    /**
     * テスト。サーバーが動作しているかisRunningで検査し、動作していない場合は
     * execでサーバを起動する。動作している場合は、二重起動であると判断できる。
     */
    public static void main(String [] args) {
        int p = 12399;
        boolean running = MutexServer.isRunning(p);
        System.out.println("running = " + running);
        if(! running) {
            boolean ok = MutexServer.exec( p, new MutexListener() {
                public void mutexPerformed() {
                    System.out.println("MutexListener : HOGE HOGE");
                }
            });
            if(ok) System.out.println("MutexServer :  hello!!");
            //これはまずありえないしあってはいけない
            else System.out.println("MutexServer : どういうわけが実行不可！");
        } else {
            System.out.println("二重起動です");
        }
    }
    
}
