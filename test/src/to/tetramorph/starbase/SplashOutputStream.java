/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SplashOutputStream.java
 *
 * Created on 2009/11/15, 20:32:25
 */

package to.tetramorph.starbase;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * 標準出力をパイプで入力ストリームに接続し、他のスレッドからそれを読み出せる
 * ようにする。
 * 標準出力をこの出力ストリームに置換する。このクラスのgetReader()を使って、
 * System.out.println()等で出力されたものを受信できる。
 * 元の標準出力は保存されていて、それにも同じように出力される。
 * @author 大澤義鷹
 */
public class SplashOutputStream extends OutputStream {

    PipedInputStream pis;
    InputStreamReader inputStream;
    PipedOutputStream pos;
    PrintStream sysout;
    /**
     * オブジェクトを作成する。
     * @param sysout System.outをそのまま渡す。内部で保持され、revoverSystemOut()
     * で元の状態に復元できる。
     * @throws IOException
     */
    public SplashOutputStream(PrintStream sysout) throws IOException {
        this.sysout = sysout;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        inputStream = new InputStreamReader(pis);
    }

    /**
     * System.out.printされたものを受け取るリーダーを返す。
     * @return
     */
    public InputStreamReader getReader() {
        return inputStream;
    }
    
    @Override
    public void write(int c) throws IOException {
        pos.write(c);
        sysout.write(c);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        pos.write(buf,off,len);
        sysout.write(buf,off,len);
    }

}
