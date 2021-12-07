/*
 * TextDial.java
 *
 * Created on 2007/06/03, 14:54
 *
 */
package to.tetramorph.starbase.chartparts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import to.tetramorph.fontchooser.FontChooser;

/**
 * リング上に円の説明文(NATAL,PROGRESS,TRANSI)等を出す。
 * @author 大澤義孝
 */
public class TextDial extends ChartParts {

    static Font font;
    static {
        String family = FontChooser.isFamilyExists("メイリオ") ?
                "メイリオ" : "SansSerif";
        font = new Font( family, Font.PLAIN, 10 );
    }
    //Font font = new Font("Monospaced",Font.PLAIN,10);
    float fontSize;
    Color textColor = new Color(0, 128, 0);
    private double ringWidth;
    String text;
    static Stroke borderStroke =
            new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static Stroke lineStroke =
            new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public TextDial() {
    }

    @Override
    public void draw() {
        if (text != null) {
            for (double a = 45; a <= 315; a += 90) {
                drawString(text, a);
            }
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    // 現在のﾘﾝｸﾞの幅と直径から文字のｻｲｽﾞを返す

    float symsize() {
        double h = ringWidth * bp.w * 0.4;
        double w = diameter * bp.w * Math.PI / 48 * 0.7; //直径の48分の1。48は適当に決めた値。
        return (float) Math.min(h, w); //小さい方のサイズを採用する。
    }

    /**
     * 円周上に指定された文字列を描画する。
     * @param title 描画する文字列
     * @param plotAngle 描画する位置角(円中央から左側を0度とし、反時計回りにすすむ
     * 座標系。
     */
    void drawString(String title, double plotAngle) {
        float sz = symsize();
        if (sz != fontSize) {
            fontSize = sz;
            font = font.deriveFont(fontSize);
        }
        double dw = (diameter + diameterOffset) * bp.w;
        double circleLength = dw * Math.PI;
        double radius = dw / 2;
        FontRenderContext render = bp.g.getFontRenderContext();
        char[] chars = title.toCharArray();
        //指定角plotAngleに対して文字列をｾﾝﾀﾘﾝｸﾞするためのｵﾌｾｯﾄmidOffsetを求める
        double midOffset = 0;
        TextLayout[] textLayouts = new TextLayout[chars.length];
        double[] angles = new double[chars.length];
        for (int i = chars.length - 1; i >= 0; i--) {
            textLayouts[i] = new TextLayout("" + chars[i], font, render);
            midOffset += textLayouts[i].getAdvance() / circleLength * 360;
            angles[i] = midOffset;
        }
        midOffset /= 2;
        //逆まわりの座標系なので、文字列末尾から順に描画する。
        for (int i = textLayouts.length - 1; i >= 0; i--) {
            double a = -(angles[i] - midOffset + plotAngle - roll + 180); //反時計まわりの獣帯座標に変換
            float txw = textLayouts[i].getAdvance();
            //1文字分のShapeを回転
            AffineTransform at = new AffineTransform();
            at.rotate((a + 90) * Math.PI / 180, txw / 2f, 0);
            Shape s = textLayouts[i].getOutline(at);
            //回転した文字を円周上に移動
            double x = Math.cos(a * Math.PI / 180) * radius + bp.x;
            double y = Math.sin(a * Math.PI / 180) * radius + bp.y;
            AffineTransform strAT = new AffineTransform();
            strAT.translate(x - txw / 2f, y);
            bp.g.setPaint(Color.LIGHT_GRAY);
            bp.g.setPaint(textColor);
            bp.g.fill(strAT.createTransformedShape(s));
        }
    }

    public double getRingWidth() {
        return ringWidth;
    }

    public void setRingWidth(double ringWidth) {
        this.ringWidth = ringWidth;
    }

    public void setColor(Color color) {
        this.textColor = color;
    }

    public Color getColor() {
        return textColor;
    }
}
