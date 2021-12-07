/*
 * AstroFont.java
 *
 * Created on 2006/10/31, 1:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package to.tetramorph.starbase.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 占星術記号のフォントオブジェクトを返す。フォントファイルは"AMATERU.ttf"で、
 * リソース内から取得される。OhsawaAstrology.ttfとは文字コードが後ろに一つずれ
 * ていることに注意。
 */
public class AstroFont {

    /**
     * 初期化時にロードされるオリジナルの占星術フォント
     */
    protected static Font astroFont;
    //初期化時に書庫からロード

    static {
        InputStream s = null;
        try {
            s = AstroFont.class.getResourceAsStream("/resources/AMATERU.ttf");
            astroFont = Font.createFont(Font.TRUETYPE_FONT, s );
            System.out.println("占星術フォントをロード");
        } catch (IOException e) {
            Logger.getLogger(AstroFont.class.getName()).log(Level.SEVERE, null, e);
        } catch ( FontFormatException e ) {
            Logger.getLogger(AstroFont.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try { s.close(); } catch(Exception ex) { }
        }
    }

    /**
     * 指定されたサイズで占星術フォントを返す。
     * astroFont.deriveFont(size)を実行しているだけ。
     * @see java.awt.Font
     */
    public static Font getFont(float size) {
        return astroFont.deriveFont(size);
    }

    /**
     * 指定されたスタイルとサイズで占星術フォントを返す。
     * astroFont.deriveFont(style,size)を実行しているだけ。
     * @see java.awt.Font
     */
    public static Font getFont(int style, float size) {
        return astroFont.deriveFont(style, size);
    }

    /**
     * 占星術フォントをJava環境に登録する。これによってJEditorPaneなどで、
     * 占星術記号を表示できるようになる。
     * @return 登録に成功したときはtrue。すでにマシンにフォントがインストール
     * されている場合はfalseを返す。
     */
    public static boolean registEnvironment() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .registerFont(astroFont);
    }
    // コンストラクタ禁止のシングルトンクラスとする。

    private AstroFont() {
    }
}
