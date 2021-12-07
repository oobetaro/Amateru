/*
 * GLabel.java
 *
 * Created on 2007/10/04, 13:57
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import to.tetramorph.fontchooser.FontChooser;

/**
 * ZodiacPanelに文字を描画するための部品。GLayout(GBoxLayout)にaddして
 * 使用する。文字列中に改行をいれて複数行にすることはできない。
 * @author 大澤義鷹
 */
public class GLabel extends GComponent {
    float fontScale = 1f/60f;
    static Font font;
    Font textFont;
    Rectangle2D.Float textRect = new Rectangle2D.Float();
    Rectangle2D.Float fullRect = new Rectangle2D.Float();
    Rectangle2D boundsRect;
    Point2D.Float insetPoint = new Point2D.Float();
    String text;
    TextLayout textLayout;
    Color color = Color.BLACK;
    static {
        String family = FontChooser.isFamilyExists("メイリオ") ?
                "メイリオ" : "SansSerif";
        font = new Font( family, Font.PLAIN, 10 );
    }
    /**
     * 空のオブジェクトを作成する。
     * valign=TOP,align=LEFT,フォント名="Dialog"。
     */
    public GLabel() {
        setText("");
    }
    /**
     * 文字列を指定してオブジェクトを指定する。
     */
    public GLabel( String text ) {
        setText( text );
    }
    /**
     * 文字列と水平alignを指定してオブジェクトを作成する。
     */
    public GLabel( String text, int align ) {
        this( text );
        setAlign( align );
    }
    /**
     * 水平alignと文字列""を指定してオブジェクトを作成する。
     */
    public GLabel( int align ) {
        this("");
        setAlign(align);
    }
    /**
     * 文字列と水平alignとフォントスケールを指定してオブジェクトを作成する。
     */
    public GLabel( String text, int align, double fontScale ) {
        this( text, align );
        setFontScale(fontScale);
    }
    /**
     * 水平alignとフォントスケールを指定して文字列は""のオブジェクトを作成する。
     */
    public GLabel( int align, double fontScale ) {
        this( "",align);
        setFontScale(fontScale);
    }
    /**
     * 文字列をセットする。
     */
    public final void setText( String text ) {
        this.text = text;
    }
    /**
     * 文字色をセットする。
     */
    public void setColor( Color color ) {
        this.color = color;
    }
    /**
     * 文字色を返す。
     */
    public Color getColor() {
        return color;
    }
//    /**
//     * フォントをセットする。
//     */
//    public void setFont( Font font ) {
//        this.font = font;
//    }

    /**
     * フォントサイズをbaseWidthを1000として千分率で指定する。baseWidthは画面全体の
     * 幅なので、文字は通常1/50〜1/80のサイズとなる。1/50なら20を指定すればよい。
     */
    public final void setFontScale( double fontScalePer ) {
        this.fontScale = (float)( fontScalePer / 1000 );
    }

    @Override
    protected void draw( double x, double y ) {
        //textがセットされていないときは一切描画しない。
        if ( text == null || text.length() == 0 ) return;
        AffineTransform at = new AffineTransform();
        at.translate( x,y + textLayout.getAscent());
        Shape outline = textLayout.getOutline(at);
        boundsRect = outline.getBounds2D();
        if ( bgColor != null ) {
            g.setPaint(bgColor);
            g.fill(boundsRect);
        }
        g.setPaint(color);
        g.fill(outline);
    }

    @Override
    public Rectangle2D.Float getSize() {
        return textRect;
    }

    @Override
    public Rectangle2D.Float getFullSize() {
        return fullRect;
    }
    @Override
    public void setup() {
        textRect = new Rectangle2D.Float();
        fullRect = new Rectangle2D.Float();
        textFont = font.deriveFont( baseWidth * fontScale );
        //textがセットされていないときは、半角スペースでサイズを計算。
        String txt = ( text==null || text.length() == 0 ) ? " " : text;
        textLayout = new TextLayout(txt,textFont,g.getFontRenderContext());
        float w = textLayout.getAdvance();
        float h = textLayout.getAscent();
        //textRect.width = w;
        textRect.width = ( text == null || text.length() == 0) ? 0 : w;
        textRect.height = h;
        float b = (float)baseWidth;
        fullRect.width = w + insets[ LEFT ] * b + insets[ RIGHT ] * b;
        fullRect.height = h + insets[ TOP ] * b + insets[ BOTTOM ] * b;
    }

    /**
     * 指定座標がこのGLableの枠内に含まれる場合はこのオブジェクト自身を返す。
     * 含まれない場合はnullを返す。
     * ただしGComponentListenerが登録されていない場合はつねにnullを返す。
     */
    @Override
    public GComponent contains(int x,int y) {
        if ( getGComponentListener() == null ) return null;
        if ( boundsRect == null ) return null;
        if ( boundsRect.contains(x,y) ) return this;
        return null;
    }

}
