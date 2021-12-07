/*
 * WordBalloon.java
 *
 * Created on 2006/11/12, 2:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.widget;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import to.tetramorph.util.Preference;
/**
 * ワードバルーン(漫画の吹き出しの意)を表示する。
 * ToolTipsと同じような機能だが、マウスをクリックしたら表示するというような使い方
 * ができる。なにかのオブジェクトにオンカーソルして一定時間以上それがつづいたら
 * 表示するという使い方もできる。
 * ホロスコープ上でサビアンやアスペクトなどの説明表示に使用する。
 *
 * showメソッドに引き渡すのはスクリーン座標(デスクトップのフル画面座標)だという
 * ことに注意。スクリーン座標を求めるのには、SwingUtilities.convertPointScreen()
 * を使用する。MouseInfo.getPointerInfo()でも求まるが、セキュリティマネージャに
 * ひっかかる事があるので、前者のほうを推奨する。特にJava WebStartで実行する場合
 * セキュリティマネージャが働いているので注意。ローカルで直接実行している間は
 * 問題ないが、JWSから実行するとアウトということがある。
 *
 *
 */
public final class WordBalloon {

    WordBalloonHandler handler;
    static Preference pref = null;
    static {
        getPreference();
    }
    /**
     * オーナーとなるフレームを指定してオブジェクトを作成する。
     * @param handler ワードバルーンハンドラー
     */
    public WordBalloon(WordBalloonHandler handler) {
        this.handler = handler;
        getPreference(); // prefの初期化のために呼び出す
    }

    /**
     * ワードバルーンハンドラーをセットする。
     */
    public void setWordBalloonHandler(WordBalloonHandler h) {
        this.handler = h;
    }

    /**
     * テキストを表示する。テキストは一定時間後に自動的に消える。
     * ワードバルーンはマウスカーソルの絶対座標に表示される。
     * @param text 表示するテキスト。(HTML表現も可能)
     * @param p バルーンの表示位置(スクリーン座標)
     */
    public void show( final String text, final Point p) {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                JLabel l = new JLabel(text);
                l.setOpaque( true );
                l.setBackground( pref.getColor("WordBalloon.background") );
                l.setForeground( pref.getColor("WordBalloon.foreground") );
                Border bdr = new CompoundBorder(
                        BorderFactory.createLineBorder( pref.getColor("WordBalloon.border") ),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8));
                l.setBorder( bdr );
                l.setFont( pref.getFont("WordBalloon.font") );
                show(l,p);
            }
        });
    }

    /**
     * 現在のマウスカーソル位置(デスクトップの絶対座標)にワードバルーンを表示。
     */
    private void show( JLabel label, Point p ) {
        if(handler.getSelectedObject() == null) return;
        JWindow balloon = new JWindow(); //ワードバルーンのWindowを用意
        balloon.setBackground( pref.getColor("WordBalloon.background") );
        balloon.getContentPane().add(label);
        balloon.pack();
        p.y -= balloon.getBounds().height;
        balloon.setLocation(p);
        balloon.setVisible(true);
        //10[ms]で消去するかどうかを監視
        Timer timer = new Timer(10,new Closer(balloon));
        timer.start();
    }

    // ワードバルーンを一定時間後に消去するためのアクションリスナ。
    // タイマーからコールされる。
    class Closer implements ActionListener {
        Object selectedObject;
        JWindow win;
        long start_ms;
        // コンストラクタ。ワードバルーンのWindowを教える。
        Closer(JWindow win) {
            this.win = win;
            this.selectedObject = handler.getSelectedObject();
            start_ms = System.currentTimeMillis();
        }

        // タイマーから10msごとに呼び出される。条件が成立すればWindowを消去。

        @Override
        public void actionPerformed(ActionEvent e) {
            long count = System.currentTimeMillis() - start_ms; //count++;
            if ( ( handler.getSelectedObject() != selectedObject) ||
                   count > pref.getLong("WordBalloon.time") ) {
                ((Timer)e.getSource()).stop(); //タイマー停止
                win.setVisible(false); //ワードバルーンを消去
                win.dispose();
                // 消去までオブジェクトの変更がなければnullをセット。
                // まだバルーンが出ている間に他のテキストの表示要求が発生した
                // 場合は、変更される。その場合nullをセットすると、後から要求
                // されたバルーンも消去されることになるため、この判定がある。
                if ( selectedObject == handler.getSelectedObject() )
                    handler.setSelectedObject(null);
            }
        }
    }
    /**
     * ワードバルーンの配色、フォント、表示時間が設定されたプロパティを返す。
     * (参照を返すだけ)。
     * WordBalloon.getPreference().setColor(..)などとすれば、配色を変更できる。
     * 設定プロパティのインスタンスはstaticで一つしかない。このオブジェクトの
     * インスタンスをいくつつくろうが、配色等の設定は一種類しかもてない。
     * @return
     */
    public static Preference getPreference() {
        if ( pref == null ) {
            pref = new Preference();
            pref.setColor("WordBalloon.border", Color.BLACK );
            pref.setColor("WordBalloon.background", new Color(197,214,184) );
            pref.setColor("WordBalloon.foreground", Color.BLACK );
            pref.setFont("WordBalloon.font", new JLabel().getFont());
            pref.setLong("WordBalloon.time", 1800L );
        }
        return pref;
    }
    /**
     * 配色、フォント、表示時間が入ったプロパティをセットする。
     * (オブジェクトをすげかえる。)
     * @param p
     */
    public static void setPreference( Preference p ) {
        pref = p;
    }

    /**
     * srcのプロパティから設定情報をこのオブジェクトのプロパティにコピーする。
     * あるものだけをコピーする。
     * @param src
     */
    public static void copyPreference( Preference src ) {
        String [] name = { "border","background","foreground","font","time" };
        for ( String s : name ) {
            String key = "WordBalloon."+s;
            String srcValue = src.getProperty(key);
            if ( srcValue == null ) continue;
            pref.setProperty(key, srcValue);
        }
    }
//    //一定時間後にワードバルーンを表示するリスナ。これもタイマーからキック。
//    class Schedule implements ActionListener {
//        String text;
//        Object obj;
//        Point p;
//
//        Schedule(String text,Point p) {
//            this.obj = handler.getSelectedObject();
//            this.p = p;
//            if(obj == null) throw new NullPointerException();
//            this.text = text;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            //このリスナをキックしたタイマーを停止
//            ((Timer)e.getSource()).stop();
//            Object o = handler.getSelectedObject();
//            if(o == null) return;
//            if(o == obj) show(text,p);
//        }
//    }
}
