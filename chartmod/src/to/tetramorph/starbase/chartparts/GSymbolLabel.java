/*
 * GSymbolLabel.java
 *
 * Created on 2007/10/07, 19:44
 *
 */
package to.tetramorph.starbase.chartparts;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import to.tetramorph.starbase.util.AstroFont;

/**
 * GLabelと似たようなものだが、頭に占星術記号を追加できる。
 * @author 大澤義鷹
 */
public class GSymbolLabel extends GLabel {

    String symbol = null;
    Font bodyFont;
    float symWidth;
    TextLayout symTextLayout;

    /**
     *  GSymbolLabel オブジェクトを作成する
     */
    public GSymbolLabel() {
        super();
        //this.symbol = symbol;
    }

    public GSymbolLabel(String symbol, String text) {
        super(text);
        this.symbol = symbol;
    }

    public GSymbolLabel(String symbol, String text, int align) {
        super(text, align);
        this.symbol = symbol;
    }

    public GSymbolLabel(String symbol, String text, int align, double fontScale) {
        super(text, align, fontScale);
        this.symbol = symbol;
    }

    protected void draw(double x, double y) {
        if (symbol == null) { //シンボルが設定されていないときは普通のGLabelと同じ
            super.draw(x, y);
            return;
        }
        AffineTransform symat = new AffineTransform();
        symat.translate(x, y + symTextLayout.getAscent());
        //Shape symOutline = symTextLayout.getOutline(symat);
        Shape boundsShape = symat.createTransformedShape(getSize());
        boundsRect = boundsShape.getBounds2D();
        if (bgColor != null) {
            g.setPaint(bgColor);
            g.fill(boundsShape);
        }
        g.setPaint(color);
        g.fill(symTextLayout.getOutline(symat));

        if (text == null || text.length() == 0) {
            return;
        }
        AffineTransform at = new AffineTransform();
        at.translate(x + symTextLayout.getAdvance(), y + symTextLayout.getAscent());
        g.fill(textLayout.getOutline(at));

    }

    public void setup() {
        super.setup();
        if (symbol == null) {
            return;
        }
        //シンボルが存在するときは、フォントを作成し、textRect,fullRectの親setup()で
        //決定されたサイズを再調整する。
        FontRenderContext frc = g.getFontRenderContext();
        float sz = (float) (baseWidth * fontScale);
        bodyFont = AstroFont.getFont(sz);
        symTextLayout = new TextLayout(symbol, bodyFont, frc);
        float w = symTextLayout.getAdvance();
        float h = symTextLayout.getAscent();
        textRect.width += w;
        textRect.height = h;
        float b = (float) baseWidth;
        fullRect.width += w;
        fullRect.height = h + insets[ TOP] * b + insets[ BOTTOM] * b;
    }
}
