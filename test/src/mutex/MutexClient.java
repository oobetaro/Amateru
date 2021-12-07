/*
 * MutexClient.java
 *
 * Created on 2007/11/16, 23:00
 *
 */

package mutex;

import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 *
 * @author 大澤義鷹
 */
public class MutexClient {
  
  /**  MutexClient オブジェクトを作成する */
  public MutexClient() {
  }
  
  public static void main(String args[]) {
    System.out.println(MutexServer.abort(12399));
//    try {
//      Socket sock = new Socket("localhost" , 12399);
//      OutputStreamWriter writer = new OutputStreamWriter(sock.getOutputStream());
//      writer.write("check\n");
//      writer.write("THREAD_ABORT\n");
//      writer.flush();
//      sock.close();
//    } catch(Exception err) {
//      System.out.println("Client : " + err);
//    }
  }
}
