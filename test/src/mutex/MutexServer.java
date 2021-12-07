/*
 * MutexServer.java
 *
 * Created on 2007/11/16, 22:25
 *
 */

package mutex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * アプリの二重起動を抑止するためのサーバ。ポートを開いてコマンドを待ち受け、
 * 所定のコマンドが来れば、リスナを実行したり、サーバ停止を行う。
 * アプリのシャトダウン時には、このサーバのabort()を使ってサーバを停止させること。
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
   */
  public MutexServer(int port,MutexListener l) {
    this.l = l;
    this.port = port;
  }
  /**
   * サーバの本体。
   */
  public void run() {
    //int count = 0;
    try {
      ServerSocket server = new ServerSocket(port);
      LOOP:
      for(;;) {
        Socket connect = server.accept();
        BufferedReader in = new BufferedReader(
          new InputStreamReader(connect.getInputStream()));
        for(;;) {
          String echo = in.readLine();
          if (echo == null || echo.equals("")) break;
          //System.out.println("#" + echo );
          if ( echo.equalsIgnoreCase( CHECK ) && l != null)
            l.mutexPerformed();
          if ( echo.equalsIgnoreCase( ABORT )) {
            connect.close();
            server.close();
            break LOOP;
          }
        }
        //System.out.println("MutexServer : count " + count); count++;
        connect.close();
      }
    } catch(Exception err) {
      System.out.println("MutexServer : ");
      err.printStackTrace();
    }
    
  }
  /**
   * サーバを起動する。
   * @param port ポート番号
   * @param l リスナ
   * @return 起動に成功すればtrue、失敗すればfalseを返す。
   */
  public static boolean exec(int port,MutexListener l) {
    try {
        new Thread(new MutexServer(port,l)).start();
    } catch ( Exception e ) {
        e.printStackTrace();
        return false;
    }
    return true;
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
  public static boolean check(int port) {
    return send(port,CHECK);
  }
  
  //サーバーにコマンドを送信
  private static boolean send(int port,String cmd) {
    try {
      Socket sock = new Socket("localhost" , port);
      OutputStreamWriter writer = new OutputStreamWriter(sock.getOutputStream());
      writer.write( cmd + "\n" );
      writer.flush();
      sock.close();
    } catch(Exception err) {
      System.out.println("Client : " + err);
      return false;
    }
    return true;
  }
  
  public static boolean isRunning(int port) {
    try {
      Socket sock = new Socket("localhost" , port);
      sock.close();
    } catch ( Exception e) {
      return false; //サーバは動いてない
    }
    return true;    //サーバが動いている
  }
  
  public static void main(String [] args) {
    int p = 12399;
    boolean running = MutexServer.isRunning(p);
    System.out.println("running = " + running);
    if(! running) {
      boolean ok = exec( p, new MutexListener() {
        public void mutexPerformed() {
          System.out.println("MutexListener : HOGE HOGE");
        }
      });
      if(ok) System.out.println("MutexServer :  hello!!");
      else System.out.println("MutexServer : どういうわけが実行不可！");
    } else {
      MutexServer.check(p);
    }
  }

}
