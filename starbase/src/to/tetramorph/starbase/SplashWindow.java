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
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * アマテル起動時に表示する、スプラッシュウィンドウ。
 * インストールや起動のプロセスは、EDTで実行しない。しかし挺進バーを表示するとき
 * はEDTでなければならない。このクラスのメソッドは非EDTから呼びだしても、内部的
 * にEDTで処理されるようになっている。
 * @author 大澤義鷹
 */
public class SplashWindow extends JWindow {
    private static SplashWindow INSTANCE;
    SplashPanel splashPanel;
    int count;
    //このクラスはシングルトンクラス
    private SplashWindow() throws IOException {
        splashPanel = new SplashPanel();
        getContentPane().add( splashPanel, BorderLayout.CENTER );
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); //常に最前面
        count = 0;
    }

    /**
     * スプラッシュウィンドウをスクリーン中央に表示して、そのインスタンスを返す。
     * これは非EDTから呼び出す事。EDTから呼び出してはならない。
     * @return SplashWindowのインスタンス。
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public static SplashWindow getInstance() {
        if ( INSTANCE != null ) return INSTANCE;
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return INSTANCE;
    }

    /**
     * このウィンドウを破棄し非表示とする。
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
     * 挺進バーに値を加算する。
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
     * エラー表示の際に表示される「了解ボタン」のインスタンスを返す。
     * 終了処理を書いたアクションリスナを登録すれば良い。
     */
    public JButton getJButton() {
        return splashPanel.getJButton();
    }

    public static void main( String [] args) throws Exception {
        final SplashWindow sp = SplashWindow.getInstance();
        sp.getJButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sp.dispose();
            }
        });
        for ( int i=0; i<40; i++) {
            sp.addValue(1);
            Thread.sleep(10);
        }
        sp.setError("エラー発生、全員避難せよ");
    }
//    /**
//     * 挺進バーの値を返す。
//     */
//    int getValue() {
//        return count;
//    }
    /**
     * 挺進バーに値をセットする。0〜100までの値。
     * @param c
     */
    void setValue( int c ) {
        this.count = c;
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JProgressBar bar = splashPanel.getJProgressBar();
                    bar.setValue( count );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
