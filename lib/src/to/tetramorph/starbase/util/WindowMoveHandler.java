/*
 * WindowMoveHandler.java
 *
 * Created on 2006/08/09, 15:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package to.tetramorph.starbase.util;

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


/**
 * フレームやダイアログのサイズと位置が変化したら、その位置情報をPreferncesに
 * 保管する。
 * JFrameなどのコンポーネントにaddComponentListener()をつかって、このクラスの
 * インスタンスを登録する。
 * リスナに登録しておけば勝手にウィンドウの位置を保管してくれて、setBounsを使えば
 * 保管されていた位置を、コンポーネントに再セットしてくれる。
 * <pre>
 * 使い方：
 * public class ☆ extends JFrame {
 *   WindowMoveHandler winmove = new WindowMoveHandler("☆.BOUNDS", this);
 *   addComponentListener(winmove);
 *   winmove.setBounds();
 * }
 * </pre>
 * 2009/02/27 Config.systemに値を保持するのをやめPreferencesを使うようにした。
 * これにより設定値はレジストリに書きこまれることになる。
 * 2011/07/29 レジストリを使うのをやめた。
 * 2011/07/30 ComponentではなくWindowをコンストラクタで受け取るようにした。
 */
public class WindowMoveHandler extends ComponentAdapter {
    // starbaseのConfigではなく、starbase.utilのConfigであることに注意
    static {
        Config.load();
    }

    /** コンストラクタで指定されたkeyの写し */
    protected final String key;
    /** コンストラクタで指定されたwindowの写し */
    protected final Window window;

    /**
     * オブジェクトを作成する。
     * @param key レジストリに登録するときのキー名
     * @param window JFrameかJDialogのどちらか
     */
    public WindowMoveHandler(String key, Window component) {
        this.key = key;
        this.window = component;
    }

    private void setRectangle(String key,Rectangle rect) {
        Config.usr.setRectangle(key, rect);
        Config.save();
    }

    /**
     * ウィンドウの位置が変わると呼び出される。
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        Rectangle rect = e.getComponent().getBounds();
        setRectangle(key, rect);
        window.setPreferredSize(rect.getSize());
    }

    /**
     * ウィンドウがリサイズされると呼び出される。
     */
    @Override
    public void componentResized(ComponentEvent e) {
        Rectangle rect = e.getComponent().getBounds();
        setRectangle(key, rect);
        window.setPreferredSize(rect.getSize());
    }

    /**
     * コンストラクタで指定したコンポーネントに、コンストラクタで指定したキーの
     * プロパティ値(Rectangleによるウィンドウの位置とサイズ)をセットする。
     * このメソッドはsetBoundsとsetPreferredSizeを実行する。
     */
    public void setBounds() {
        Rectangle rect = Config.usr.getRectangle(key);
        if (rect != null) {
            window.setBounds(rect);
            window.setPreferredSize(rect.getSize());
        } else {
            window.setLocationRelativeTo(null);
        }
    }

    public void setLocation() {
        Rectangle rect = Config.usr.getRectangle(key);
        if (rect != null) {
            window.setLocation( rect.x, rect.y);
        }
    }

}
