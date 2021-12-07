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
 * �A�v���̓�d�N����}�~���邽�߂̃T�[�o�B�|�[�g���J���ăR�}���h��҂��󂯁A
 * ����̃R�}���h������΁A���X�i�����s������A�T�[�o��~���s���B
 * �A�v���̃V���g�_�E�����ɂ́A���̃T�[�o��abort()���g���ăT�[�o���~�����邱�ƁB
 * @author ���V�`��
 */
final class MutexServer implements Runnable {
  private static final String ABORT = "THREAD_ABORT";
  private static final String CHECK = "LISTENER EXECUTE";
  private MutexListener l = null;
  private int port;

  private MutexServer() {}
  /**  
   * MutexServer �I�u�W�F�N�g���쐬���� 
   */
  public MutexServer(int port,MutexListener l) {
    this.l = l;
    this.port = port;
  }
  /**
   * �T�[�o�̖{�́B
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
   * �T�[�o���N������B
   * @param port �|�[�g�ԍ�
   * @param l ���X�i
   * @return �N���ɐ��������true�A���s�����false��Ԃ��B
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
   * �T�[�o���~����B�T�[�o���ғ����Ă��Ȃ��Ƃ��ɌĂяo����false���Ԃ�B
   */
  public static boolean abort(int port) {
    return send(port,ABORT);
  }
  
  /**
   * �T�[�o�ɃV�O�i���𑗂�B�V�O�i�����󂯎�����T�[�o�[�́A���X�i�����s����B
   */
  public static boolean check(int port) {
    return send(port,CHECK);
  }
  
  //�T�[�o�[�ɃR�}���h�𑗐M
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
      return false; //�T�[�o�͓����ĂȂ�
    }
    return true;    //�T�[�o�������Ă���
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
      else System.out.println("MutexServer : �ǂ������킯�����s�s�I");
    } else {
      MutexServer.check(p);
    }
  }

}
