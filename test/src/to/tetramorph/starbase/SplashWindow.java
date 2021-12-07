/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * Created on 2009/11/14, 19:50:18
 */

package to.tetramorph.starbase;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * �A�}�e���N�����ɕ\������A�X�v���b�V���E�B���h�E�B
 * �C���X�g�[����N���̃v���Z�X�́AEDT�Ŏ��s���Ȃ��B��������i�o�[��\������Ƃ�
 * ��EDT�łȂ���΂Ȃ�Ȃ��B���̃N���X�̃��\�b�h�͔�EDT����Ăт����Ă��A�����I
 * ��EDT�ŏ��������悤�ɂȂ��Ă���B
 * @author ���V�`��
 */
public class SplashWindow extends JWindow {
    static SplashWindow INSTANCE;
    SplashPanel splashPanel;
    int count;
    PrintStream sysout;
    SplashOutputStream sos;
    //���̃N���X�̓V���O���g���N���X
    private SplashWindow() throws IOException {
        splashPanel = new SplashPanel();
        getContentPane().add( splashPanel, BorderLayout.CENTER );
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); //��ɍőO��
        count = 0;
        sysout = System.out;
        sos = new SplashOutputStream(sysout);
        System.setOut(new PrintStream(sos));
        Thread thread = new Thread(new Watcher()); //.start();
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    //�W���o�͂��Ď����āA�v���O���X�o�[��i�߂�
    class Watcher implements Runnable {
        @Override
        public void run() {
            try {
                InputStreamReader reader = sos.getReader();
                LOOP: for(;;) {
                    StringBuilder sb = new StringBuilder();
                    while( reader.ready() ) {
                        int c = reader.read();
                        if ( c == '\n' ) break;
                        sb.append((char)c);
                    }
                    //trim()�͕K�v�B'\n'�ȊO�ɂ�����R�[�h�����邱�Ƃ�����B
                    String line = sb.toString().trim();
                    if ( line.length() >= 1 ) {
                        addValue(2);
                        if ( line.trim().equals("�N����������")) {
                            sos.flush();
                            System.setOut(sysout);
                            dispose();
                            break LOOP;
                        }
                    }
                    try { Thread.sleep(5); }
                    catch (InterruptedException ex) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * �X�v���b�V���E�B���h�E���X�N���[�������ɕ\�����āA���̃C���X�^���X��Ԃ��B
     * ����͔�EDT����Ăяo�����BEDT����Ăяo���Ă͂Ȃ�Ȃ��B
     * @return SplashWindow�̃C���X�^���X�B
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static SplashWindow showWindow() throws InterruptedException,
                                                InvocationTargetException {
        if ( INSTANCE != null ) return INSTANCE;
        SwingUtilities.invokeAndWait( new Runnable() {
            @Override
            public void run() {
                try {
                    INSTANCE = new SplashWindow();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                INSTANCE.setVisible(true);
            }
        });
        return INSTANCE;
    }

    /**
     * ���̃E�B���h�E��j������\���Ƃ���B
     */
    @Override
    public void dispose() {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SplashWindow.super.dispose();
                    INSTANCE = null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ��i�o�[�ɒl�����Z����B
     * @param c
     */
    void addValue( int c ) {
        this.count += c;
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JProgressBar bar = splashPanel.getJProgressBar();
                    bar.setValue( count );
                    bar.repaint();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setError( final String msg ) {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    splashPanel.setError(msg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * �G���[�\���̍ۂɕ\�������u�����{�^���v�̃C���X�^���X��Ԃ��B
     * �I���������������A�N�V�������X�i��o�^����Ηǂ��B
     */
    public JButton getJButton() {
        return splashPanel.getJButton();
    }

    public static void main( String [] args) throws Exception {
        final SplashWindow sp = SplashWindow.showWindow();
        sp.getJButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sp.dispose();
            }
        });
        for ( int i=0; i<40; i++) {
//            sp.addValue(1);
            System.out.println("HogeHoge");
            Thread.sleep(100);
        }
        System.out.println("�N����������");
        //sp.setError("�G���[�����A�S������");
    }
//    /**
//     * ��i�o�[�̒l��Ԃ��B
//     */
//    int getValue() {
//        return count;
//    }
//    /**
//     * ��i�o�[�ɒl���Z�b�g����B0�`100�܂ł̒l�B
//     * @param c
//     */
//    void setValue( int c ) {
//        this.count = c;
//        try {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    JProgressBar bar = splashPanel.getJProgressBar();
//                    bar.setValue( count );
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
