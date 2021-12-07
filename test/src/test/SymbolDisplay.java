/*
 * SymbolDisplay.java
 *
 * Created on 2006/12/22, 16:56
 */

package to.tetramorph.starbase.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.starbase.lib.Const;

/**
 * 占星術のシンボルを表示するためのディスプレイでカラー設定の際に使用している。
 * グラフィックによるラベルのようなもの。
 * @author  大澤義鷹
 */
public class SymbolDisplay extends javax.swing.JPanel {
  char symbol = Const.ZODIAC_CHARS[0];
  FontRenderContext render;
  Graphics2D g;
  Color borderColor = Color.BLACK;
  Color frameColor = Color.BLACK;
  static Font font;
  boolean borderVisible = true;
  boolean frameVisible = true;
  /**
   * デフォルトのオブジェクトを作成する。占星術フォントがセットされており、デフォルト
   * は牡羊座のシンボル。
   */
  public SymbolDisplay() {
    initComponents();
    //基本はstatic変数で他のｲﾝｽﾀﾝｽと共有するが、setFont()で指定してやれば個々に指定可能。
    font = AstroFont.getFont(18f);
    setFont(font);
    setForeground(Color.WHITE);
  }
  /**
   * 表示すべき文字コードを指定する。
   */
  public void setChar(char symbol) {
    this.symbol = symbol;
  }
  /**
   * 文字コードを返す。
   */
  public char getChar() {
    return symbol;
  }
  /**
   * 使用するフォントをセットする。
   */
  public void setFont(Font font) {
    super.setFont(font);
  }
  
  /**
   * シンボルを縁取りする色を返す。
   */
  public Color getSymbolBorder() {
    return borderColor;
  }
  /**
   * シンボルの縁取りするボーダーカラーをセット。
   */
  public void setSymbolBorder(Color borderColor) {
    this.borderColor = borderColor;
  }
  /** シンボルの縁取りをするときはtrueをセットする */
  public void setBorderVisible(boolean b) {
    borderVisible = b;
  }
  /** 文字の縁取りを表示しているときはtrueを返す */
  public boolean isBorderVisible() {
    return borderVisible;
  }
  /** 四角の枠線の色をセットする */
  public void setFrame(Color color) {
    frameColor = color;
  }
  /** 四角の枠線の色を返す */
  public Color getFrame() {
    return frameColor;
  }
  /** 四角の枠線を表示するときはtrueをセット */
  public void setFrameVisible(boolean b) {
    frameVisible = b;
  }
  /** 四角の枠線を表示しているときはtrueを返す */
  public boolean isFrameVisible() {
    return frameVisible;
  }
  
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    g = (Graphics2D)graphics;
    render = g.getFontRenderContext();
    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    float sx = getSize().width / 2f;
    float sy = getSize().height / 2f;
    TextLayout textlayout =
      new TextLayout("" + symbol,getFont(),render);
    //そのグラフィック文字の(高さ/2)と(幅/2)を得る
    float h = textlayout.getAscent()/2f;
    float w = textlayout.getAdvance()/2f;
    //シンボルの中心が原点に来るように移動させる
    AffineTransform at = new AffineTransform();
    at.translate(-w,h); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
    Shape signSymbol = textlayout.getOutline(at);
    //シンボルを獣帯円の所定の場所に移動
    AffineTransform at2 = new AffineTransform();
    at2.translate(sx,sy);
    signSymbol = at2.createTransformedShape(signSymbol);
    g.setPaint(borderColor);
    if(borderVisible) {
      //角を丸める処理を指定しないとトゲトゲが飛び出して美しくない
      g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND));
      g.draw(signSymbol); //太い線でサインの輪郭を描き、次に・・・
    }
    g.setStroke(new BasicStroke(1f));
    g.setPaint(getForeground());
    g.fill(signSymbol); //細い線でサインを塗りつぶす。くっきりしたサインが描ける。
    //四角の枠線で囲む
    if(frameVisible) {
      g.setColor(frameColor);
      g.drawRect(0,0,getSize().width-1,getSize().height-1);
    }
  }
  /** 
   * 指定された他のSymbolDisplayオブジェクトのカラー情報(背景色,文字色,縁取線色,
   * 枠線色,縁取り可視/不可視,枠線可視/不可視)をコピー
   */
  public void setColor(SymbolDisplay sym) {
    setBackground(sym.getBackground());
    setForeground(sym.getForeground());
    setSymbolBorder(sym.getSymbolBorder());
    setFrame(sym.getFrame());
    setBorderVisible(sym.isBorderVisible());
    setFrameVisible(sym.isFrameVisible());
  }
//  static void createAndShowGUI() {
//    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
//      UIManager.put("swing.boldMetal", Boolean.FALSE);
//      JDialog.setDefaultLookAndFeelDecorated(true);
//      JFrame.setDefaultLookAndFeelDecorated(true);
//      Toolkit.getDefaultToolkit().setDynamicLayout(true);
//    }
//    JFrame frame = new JFrame();
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setTitle("サインカラー設定");
//    Font astrofont = AstroFont.getFont(20f);
//    SymbolDisplay [] sympan = new SymbolDisplay[12];
//    Dimension size = new Dimension(32,32);
//    for(int i=0; i<sympan.length; i++) {
//      sympan[i] = new SymbolDisplay();
//      sympan[i].setFont(astrofont);
//      sympan[i].setChar(Const.ZODIAC_CHARS[i]);
//      sympan[i].setPreferredSize(size);
//      sympan[i].setForeground(Color.WHITE);
//      sympan[i].setBackground(Color.BLACK);
//    }
//    JPanel panel = new JPanel();
//    panel.setLayout(new FlowLayout());
//    for(int i=0; i<sympan.length; i++) {
//      panel.add(sympan[i]);
//    }
//    frame.getContentPane().add(panel);
//    frame.pack();
//    frame.setVisible(true);
//  }
//  public static void main(String [] args) {
//    java.awt.EventQueue.invokeLater(new Runnable() {
//      public void run() {
//        createAndShowGUI();
//      }
//    });    
//  }    
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setMinimumSize(new java.awt.Dimension(24, 24));
    setRequestFocusEnabled(false);
    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 34, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 32, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
  
}
