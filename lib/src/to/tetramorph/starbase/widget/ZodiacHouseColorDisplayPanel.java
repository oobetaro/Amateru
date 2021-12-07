/*
 * ZodiacHouseColorDisplayPanel.java
 *
 * Created on 2007/10/10, 16:34
 */
package to.tetramorph.starbase.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.util.AstroFont;

/**
 * サインやハウスの配色設定をディスプレイするパネル。
 * @author  大澤義孝
 */
public class ZodiacHouseColorDisplayPanel extends javax.swing.JPanel {

    private final int CELL_SIZE = SIGN_COLORS.length;
    public Color[] _backgroundColor = new Color[]{Color.WHITE};
    public Color[] _zodiacBGColors = new Color[CELL_SIZE];
    public Color[] _zodiacFGColors = new Color[CELL_SIZE];
    /**
     * 獣帯リングのゲージ線色。要素数1の配列。参照で書き換え可能なように配列を
     * 使用している。NPTと複数のこのパネルを用意したとき、3つに対して共通の
     * 配列を割り当てておけば、一つの配列を書き換えれば3のパネルすべての色を
     * 変更することができる。
     */
    public Color[] _zodiacGaugeColor = new Color[]{new Color(120, 120, 120)};
    public boolean[] _isNoZodiacGauge = new boolean[]{false};
    /**
     * 獣帯円の各サインのセクターの輪郭線。要素数1の配列。
     */
    public Color[] _zodiacRingBorderColor = new Color[]{Color.BLACK};
    public Color[] _zodiacSymbolBorderColors = new Color[CELL_SIZE];
    public boolean[] _isNoZodiacBG = new boolean[]{true};
    public boolean[] _isNoZodiacSymbolsBorder = new boolean[]{false};
    public boolean[] _isNoZodiacRingBorder = new boolean[]{false};
    //ここよりハウス
    public Color[] _houseBGColors = new Color[CELL_SIZE];
    public Color[] _houseNumberColors = new Color[CELL_SIZE];
    public Color _housesGaugeColor = Color.LIGHT_GRAY;
    public Color _cuspsColor = Color.LIGHT_GRAY;
    public Color _housesBorderColor = Color.LIGHT_GRAY; //縦線はカスプだが、下線はボーダー
    public Color _bodysBorderColor = Color.BLACK;
    public Color _bodysColor = Color.BLACK;
    public Color _bodysDegreeColor = Color.BLACK;
    public Color _outerHousesNumberColor = Color.BLACK;
    public Color _outerCuspsDegreeColor = Color.BLACK;
    public Color _outerCuspsColor = Color.BLACK;
    public Color _bodysHighLightColor = Color.RED;
    public Color _housesHighLightColor = new Color(255, 220, 220);
    public Color _leadingLineColor = Color.BLACK;
    public boolean _isNoHousesGauge = false;
    public boolean _isNoHousesBG = false;
    //public boolean _isNoBodysBorder = true;
    public int _bodysEffect = 0;
    // この色はディスプレイしない。値の保管用。
    public Color _ringTextColor = new Color(0,128,0);

    private Dimension screenSize;
    //画面のサイズが前回のpaintComonent()されたときと異なる場合trueとなるフラグ
    private boolean isResized;
    private Font signFont;
    private FontRenderContext frc;
    private Graphics2D g;
    private static final Stroke normalStroke =
            new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke borderStroke =
            new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int[] bodyCodes = {
        MARS, VENUS, MERCURY, MOON, SUN, MERCURY,
        VENUS, PLUTO, JUPITER, SATURN, URANUS, NEPTUNE};
    private Font bodyFont;
    private Font houseFont = new Font("Times", Font.BOLD, 10);
    private Font cuspFont = new Font("Monospaced", Font.PLAIN, 10);
    private Shape[] signShapes = new Shape[CELL_SIZE];
    private Shape[] bodyShapes = new Shape[CELL_SIZE];
    private Shape[] houseShapes = new Shape[CELL_SIZE];
    private Shape[] houseShapes2 = new Shape[CELL_SIZE];
    private Shape[] cuspShapes = new Shape[CELL_SIZE];
    private Shape[] bodysDegreeShapes = new Shape[CELL_SIZE];
    private Line2D.Double line = new Line2D.Double();
    private GeneralPath leadingPath = new GeneralPath();
    private Rectangle2D.Double[] houseRectangles = new Rectangle2D.Double[12];

    /** Creates new form ZodiacHouseColorDisplayPanel */
    public ZodiacHouseColorDisplayPanel() {
        initComponents();
        Arrays.fill(_houseBGColors, 0, CELL_SIZE, Color.WHITE);
        System.arraycopy(SIGN_COLORS, 0, _zodiacBGColors, 0, CELL_SIZE);
        Arrays.fill(_zodiacFGColors, 0, CELL_SIZE, Color.WHITE);
        Arrays.fill(_houseNumberColors, 0, CELL_SIZE, Color.LIGHT_GRAY);
        Arrays.fill(_zodiacSymbolBorderColors, 0, CELL_SIZE, Color.BLACK);
        MouseHandler mh = new MouseHandler();
        addMouseMotionListener(mh);
        addMouseListener(mh);
        for (int i = 0; i < CELL_SIZE; i++) {
            houseRectangles[i] = new Rectangle2D.Double();
        }
    }
    private boolean isStarted = false;

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        this.g = (Graphics2D) graphics;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (screenSize == null || !screenSize.equals(getSize())) {
            screenSize = getSize();
            //System.out.println("size = " + screenSize);
            isResized = true;
        } else {
            isResized = false;
        }
        g.setPaint(_backgroundColor[0]);
        g.fillRect(0, 0, screenSize.width, screenSize.height);
        fillBoxs(screenSize);
        isStarted = true;
    }

    //獣帯矩形と、ハウス矩形、カスプ線の描画をしたのち、各シンボルや数字を描画
    private void fillBoxs(Dimension size) {
        double h = size.getHeight() - 35;
        double zh = h * 0.4; //サイン矩形の高さ
        double hh = h - zh;  //ハウス矩形の高さ
        double w = size.getWidth() * 0.97;
        double zw = w / 12;
        frc = g.getFontRenderContext();
        if (isResized) {
            signFont = AstroFont.getFont((float) (zw * .5));
            bodyFont = AstroFont.getFont((float) (zw * .4));
            houseFont = houseFont.deriveFont((float) (zw * .3));
            cuspFont = cuspFont.deriveFont((float) (zw * .25));
        }
        Rectangle2D.Double z_rect = new Rectangle2D.Double(0, 0, zw, zh);
        Rectangle2D.Double h_rect = new Rectangle2D.Double(0, 0, zw, hh);
        //Line2D.Double line = new Line2D.Double();
        double x = (size.getWidth() - w) / 2;
        double zy = 20; //size.getHeight() * 0.15;
        double x0 = x;
        double y0 = zy + z_rect.height + h_rect.height;
        double hy = zy + z_rect.height;
        for (int i = 0; i < 13; i++) {
            z_rect.x = x;
            z_rect.y = zy;
            h_rect.x = x;
            h_rect.y = hy;
            if (i < 12) {
                houseRectangles[i].setRect(x, hy, zw, hh); //当たり判定用
                Color c = (houseOnCursor == i) ? _housesHighLightColor : _houseBGColors[i];
                if (houseOnCursor == i || (!_isNoHousesBG)) {
                    g.setPaint(c);
                    g.fill(h_rect);
                }
                if (!_isNoZodiacBG[0]) {
                    g.setPaint(_zodiacBGColors[i]);
                    g.fill(z_rect);
                }
                if (!_isNoZodiacRingBorder[0]) {
                    //獣帯の矩形線
                    g.setPaint(_zodiacRingBorderColor[0]);
                    g.draw(z_rect);
                }
                //サインと天体シンボル
                drawSigns(z_rect.x + z_rect.width / 2, z_rect.y + z_rect.height / 2, i);
                drawBodys(h_rect.x + h_rect.width * 0.3, h_rect.y + h_rect.height * .5, i);
                drawHouseNumbers(h_rect.x + h_rect.width * 0.73, h_rect.y + h_rect.height * .71, i);
                drawHouseNumbers2(h_rect.x + h_rect.width * 0.7, z_rect.y - 11, i);
                drawCuspsDegree(z_rect.x + z_rect.width * 0.2, z_rect.y - 7, i);
                drawBodysDegree(h_rect.x + h_rect.width * 0.3, h_rect.y + h_rect.height * .15, i);
            }
            //ハウスカスプ
            g.setPaint(_cuspsColor);
            line.setLine(h_rect.x, hy + 1, h_rect.x, hy + h_rect.height - 1);
            g.draw(line);
            //ハウス内のグリッド
            if (i < 12) {
                if (!_isNoHousesGauge) {
                    drawGrid(h_rect.x, h_rect.y + h_rect.height, h_rect.width, i, _housesGaugeColor);
                }
                if (!_isNoZodiacGauge[0]) {
                    drawGrid(z_rect.x, z_rect.y + z_rect.height, h_rect.width, i, _zodiacGaugeColor[0]);
                }
            }
            //獣帯円の外のハウスカスプ
            g.setPaint(_outerCuspsColor);
            line.setLine(z_rect.x, z_rect.y - 1, z_rect.x, z_rect.y - 10);
            g.draw(line);
            x += zw;

        }
        //ハウス矩形BOTTOMの線
        g.setPaint(_housesBorderColor);
        line.setLine(x0, y0, x0 + zw * 12, y0);
        g.draw(line);
        //天体引き出し線
        drawLeadingLine(h_rect.y + h_rect.height);
    }

    private void drawGrid(double x, double y, double w, int sign, Color color) {
        double s = w / 10;
        for (int i = 0; i < 10; i++) {
            double ofs = i * s;
            line.setLine(x + ofs, y, x + ofs, y - 3);
            g.setPaint(color);
            g.draw(line);
        }
    }
    //星座シンボルを描画

    private void drawSigns(double x, double y, int sign) {
        if (isResized) {
            signShapes[ sign] = getShape("" + ZODIAC_CHARS[sign], signFont, x, y);
        }
        if (!_isNoZodiacSymbolsBorder[0]) {
            g.setPaint(_zodiacSymbolBorderColors[sign]);
            g.setStroke(borderStroke);
            g.draw(signShapes[ sign]);
        }
        g.setPaint(_zodiacFGColors[sign]);
        g.setStroke(normalStroke);
        g.fill(signShapes[ sign]);
    }

    //天体シンボルを描画
    private void drawBodys(double x, double y, int sign) {
        if (isResized) {
            bodyShapes[ sign] =
                    getShape("" + BODY_CHARS[ bodyCodes[ sign]], bodyFont, x, y);
        }
        if (_bodysEffect == 1) {
            g.setPaint(_bodysBorderColor);
            g.setStroke(borderStroke);
            g.draw(bodyShapes[ sign]);
        } else if (_bodysEffect == 2) {
            AffineTransform at = new AffineTransform();
            at.translate(1, 1);
            Shape shadow = at.createTransformedShape(bodyShapes[sign]);
            g.setPaint(_bodysBorderColor);
            g.setStroke(normalStroke);
            g.fill(shadow);
        }
        Color c = (bodyOnCursor == sign) ? _bodysHighLightColor : _bodysColor;
        g.setPaint(c);
        g.setStroke(normalStroke);
        g.fill(bodyShapes[ sign]);
    }

    //ハウス矩形領域にあるハウス番号を描画
    private void drawHouseNumbers(double x, double y, int sign) {
        if (isResized) {
            houseShapes[ sign] = getShape(HOUSE_NUMBERS[ sign], houseFont, x, y);
        }
        g.setPaint(_houseNumberColors[ sign]);
        g.setStroke(normalStroke);
        g.fill(houseShapes[ sign]);
    }

    //獣帯リングの外側にあるハウス番号を描画
    private void drawHouseNumbers2(double x, double y, int sign) {
        if (isResized) {
            houseShapes2[ sign] = getShape(HOUSE_NUMBERS[ sign], houseFont, x, y);
        }
        g.setPaint(_outerHousesNumberColor);
        g.setStroke(normalStroke);
        g.fill(houseShapes2[ sign]);
    }
    //獣帯リングの外側にあるカスプ度数を描画

    private void drawCuspsDegree(double x, double y, int sign) {
        if (isResized) {
            cuspShapes[ sign] = getShape("" + (sign + 1), cuspFont, x, y);
        }
        g.setPaint(_outerCuspsDegreeColor);
        g.setStroke(normalStroke);
        g.fill(cuspShapes[ sign]);
    }
    //天体の頭につく度数を描画

    private void drawBodysDegree(double x, double y, int sign) {
        if (isResized) {
            bodysDegreeShapes[ sign] = getShape("" + (sign + 1), cuspFont, x, y);
        }
        g.setPaint(_bodysDegreeColor);
        g.setStroke(normalStroke);
        g.fill(bodysDegreeShapes[ sign]);

    }

    //ゲージと天体を結ぶ引き出し線を描画
    private void drawLeadingLine(double bottom) {
        if (isResized) {
            leadingPath.reset();
            for (int i = 0; i < CELL_SIZE; i++) {
                Rectangle2D bb = bodyShapes[ i].getBounds2D();
                Rectangle2D hb = houseRectangles[ i];
                leadingPath.moveTo(bb.getX() + bb.getWidth() / 2,
                        bb.getY() + bb.getHeight() * 1.15);
                leadingPath.lineTo(hb.getX() + hb.getWidth() / 2,
                        hb.getY() + hb.getHeight() - hb.getHeight() * 0.16);
                leadingPath.lineTo(hb.getX() + hb.getWidth() / 2,
                        hb.getY() + hb.getHeight());
            }
            //leadingPath.closePath();
        }
        g.setPaint(_leadingLineColor);
        g.setStroke(normalStroke);
        g.draw(leadingPath);
    }

    /**
     * 指定された文字列、フォント、x,y座標から文字列のShapeを作成して返す。
     * x,yで指定した座標が、文字列を囲む矩形の中心に来るShapeを返す。
     */
    private Shape getShape(String s, Font font, double x, double y) {
        TextLayout tl = new TextLayout(s, font, frc);
        float h = tl.getAscent() / 2f;
        float w = tl.getAdvance() / 2f;
        AffineTransform at = new AffineTransform();
        //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
        at.translate(-w + x, h + y);
        return tl.getOutline(at);
    }
    private int bodyOnCursor = -1;
    private int houseOnCursor = -1;

    class MouseHandler extends MouseAdapter implements MouseMotionListener {

        @Override
        public void mouseMoved(MouseEvent e) {
            //一度はpaintComponent()が実行されている必要がある。
            //それでないとbodyShapesが初期化されておらずNullPointerExceptionになる。
            if (!isStarted) {
                return;
            }
            int onc = bodyOnCursor;
            bodyOnCursor = -1;
            for (int i = 0; i < bodyShapes.length; i++) {
                if (bodyShapes[i].getBounds().contains(e.getPoint())) {
                    bodyOnCursor = i;
                    if (onc >= 0 && onc == bodyOnCursor) {
                        return;
                    }
                    repaint();
                    return;
                }
            }
            if (onc >= 0) {
                repaint();
            }

            onc = houseOnCursor;
            houseOnCursor = -1;
            for (int i = 0; i < houseRectangles.length; i++) {
                if (houseRectangles[i].contains(e.getX(), e.getY())) {
                    houseOnCursor = i;
                    if (onc >= 0 && onc == houseOnCursor) {
                        return;
                    }
                    repaint();
                    return;
                }
            }
            if (onc >= 0) {
                repaint();
            }

        }

        @Override
        public void mouseExited(MouseEvent e) {
            bodyOnCursor = -1;
            houseOnCursor = -1;
            repaint();
        }
    }
    //テスト

    static void createAndShowGUI() {
        if (UIManager.getLookAndFeel().getName().equals("Metal")) {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("サインカラー設定");
        ZodiacHouseColorDisplayPanel panel = new ZodiacHouseColorDisplayPanel();
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    /** テスト */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setPreferredSize(new java.awt.Dimension(400, 100));
    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 80, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
