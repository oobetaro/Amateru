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
 * �萯�p�̃V���{����\�����邽�߂̃f�B�X�v���C�ŃJ���[�ݒ�̍ۂɎg�p���Ă���B
 * �O���t�B�b�N�ɂ�郉�x���̂悤�Ȃ��́B
 * @author  ���V�`��
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
   * �f�t�H���g�̃I�u�W�F�N�g���쐬����B�萯�p�t�H���g���Z�b�g����Ă���A�f�t�H���g
   * �͉��r���̃V���{���B
   */
  public SymbolDisplay() {
    initComponents();
    //��{��static�ϐ��ő��̲ݽ�ݽ�Ƌ��L���邪�AsetFont()�Ŏw�肵�Ă��ΌX�Ɏw��\�B
    font = AstroFont.getFont(18f);
    setFont(font);
    setForeground(Color.WHITE);
  }
  /**
   * �\�����ׂ������R�[�h���w�肷��B
   */
  public void setChar(char symbol) {
    this.symbol = symbol;
  }
  /**
   * �����R�[�h��Ԃ��B
   */
  public char getChar() {
    return symbol;
  }
  /**
   * �g�p����t�H���g���Z�b�g����B
   */
  public void setFont(Font font) {
    super.setFont(font);
  }
  
  /**
   * �V���{��������肷��F��Ԃ��B
   */
  public Color getSymbolBorder() {
    return borderColor;
  }
  /**
   * �V���{���̉���肷��{�[�_�[�J���[���Z�b�g�B
   */
  public void setSymbolBorder(Color borderColor) {
    this.borderColor = borderColor;
  }
  /** �V���{���̉���������Ƃ���true���Z�b�g���� */
  public void setBorderVisible(boolean b) {
    borderVisible = b;
  }
  /** �����̉�����\�����Ă���Ƃ���true��Ԃ� */
  public boolean isBorderVisible() {
    return borderVisible;
  }
  /** �l�p�̘g���̐F���Z�b�g���� */
  public void setFrame(Color color) {
    frameColor = color;
  }
  /** �l�p�̘g���̐F��Ԃ� */
  public Color getFrame() {
    return frameColor;
  }
  /** �l�p�̘g����\������Ƃ���true���Z�b�g */
  public void setFrameVisible(boolean b) {
    frameVisible = b;
  }
  /** �l�p�̘g����\�����Ă���Ƃ���true��Ԃ� */
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
    //���̃O���t�B�b�N������(����/2)��(��/2)�𓾂�
    float h = textlayout.getAscent()/2f;
    float w = textlayout.getAdvance()/2f;
    //�V���{���̒��S�����_�ɗ���悤�Ɉړ�������
    AffineTransform at = new AffineTransform();
    at.translate(-w,h); //�ړ��O�����̌��_�͍����ɂ��邩��A���ɔ����A��ɔ������������΂悢
    Shape signSymbol = textlayout.getOutline(at);
    //�V���{�����b�щ~�̏���̏ꏊ�Ɉړ�
    AffineTransform at2 = new AffineTransform();
    at2.translate(sx,sy);
    signSymbol = at2.createTransformedShape(signSymbol);
    g.setPaint(borderColor);
    if(borderVisible) {
      //�p���ۂ߂鏈�����w�肵�Ȃ��ƃg�Q�g�Q����яo���Ĕ������Ȃ�
      g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND));
      g.draw(signSymbol); //�������ŃT�C���̗֊s��`���A���ɁE�E�E
    }
    g.setStroke(new BasicStroke(1f));
    g.setPaint(getForeground());
    g.fill(signSymbol); //�ׂ����ŃT�C����h��Ԃ��B�������肵���T�C�����`����B
    //�l�p�̘g���ň͂�
    if(frameVisible) {
      g.setColor(frameColor);
      g.drawRect(0,0,getSize().width-1,getSize().height-1);
    }
  }
  /** 
   * �w�肳�ꂽ����SymbolDisplay�I�u�W�F�N�g�̃J���[���(�w�i�F,�����F,������F,
   * �g���F,������/�s��,�g����/�s��)���R�s�[
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
//    frame.setTitle("�T�C���J���[�ݒ�");
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
