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
 * �W���o�͂��p�C�v�œ��̓X�g���[���ɐڑ����A���̃X���b�h���炻���ǂݏo����
 * �悤�ɂ���B
 * �W���o�͂����̏o�̓X�g���[���ɒu������B���̃N���X��getReader()���g���āA
 * System.out.println()���ŏo�͂��ꂽ���̂���M�ł���B
 * ���̕W���o�͕͂ۑ�����Ă��āA����ɂ������悤�ɏo�͂����B
 * @author ���V�`��
 */
public class SplashOutputStream extends OutputStream {

    PipedInputStream pis;
    InputStreamReader inputStream;
    PipedOutputStream pos;
    PrintStream sysout;
    /**
     * �I�u�W�F�N�g���쐬����B
     * @param sysout System.out�����̂܂ܓn���B�����ŕێ�����ArevoverSystemOut()
     * �Ō��̏�Ԃɕ����ł���B
     * @throws IOException
     */
    public SplashOutputStream(PrintStream sysout) throws IOException {
        this.sysout = sysout;
        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        inputStream = new InputStreamReader(pis);
    }

    /**
     * System.out.print���ꂽ���̂��󂯎�郊�[�_�[��Ԃ��B
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
