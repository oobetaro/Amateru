/*
 * RoundButton.java
 *
 * Created on 2007/11/11, 2:15
 *
 */

package mybutton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * 角をまるくしたボタンを作る実験。
 * BasicButtonUIを継承して独自のUIを作り、JButtonを継承して、そのUIを組み込む
 * 必要がある。またButtonBordersも継承して、角を丸くしたボーダーを描くメソッドを
 * 用意し、これもUIの中に組み込む必要がある。
 *
 * かなりめんどくさい仕事のうえ、ボタン一つならともかく、他の部品すべてにそういう
 * 手続きをふんでいくのは気が遠くなる作業だったりする。
 *
 * 今の時点のでは、負けるが勝ちという印象。スキンを自分で作るというのは、かなり
 * 大変な作業で、既製品があるのであれば(ほとんどないが)それを使うほうがマシだと
 * 思われる。というわけで、このテーマからは退却。
 * @author 大澤義鷹
 */
public class RoundButton extends JButton {
  
  /**  RoundButton オブジェクトを作成する */
  public RoundButton() {
    super();
    setUI( new UI() );
  }
  
  class UI extends MetalButtonUI {
    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;
    
    public void installDefaults(AbstractButton b) {
      Color darkShadow = UIManager.getColor("Button.darkShadow");
      Color highlight = UIManager.getColor("Button.highlight");
      Color shadow = UIManager.getColor("Button.shadow");
      Color light = UIManager.getColor("Button.light");
//      BasicBorders.ButtonBorder border = new BasicBorders.ButtonBorder(
//        shadow, darkShadow, highlight, light);
      //AbstractBorder bdr = (AbstractBorder)UIManager.get("Button.border");
      //ButtonBorder border = new ButtonBorder();
      //Border border = new BasicBorders.ButtonBorder();
      RBorders.ButtonBorder border = new RBorders.ButtonBorder(shadow, darkShadow, highlight, light);
      UIManager.put("Button.border", border );
      super.installDefaults(b);
    }

//    public void update(Graphics g, JComponent c) {
//        AbstractButton button = (AbstractButton)c;
//        if ((c.getBackground() instanceof UIResource) &&
//                  button.isContentAreaFilled() && c.isEnabled()) {
//            ButtonModel model = button.getModel();
//            if (!MetalUtils.isToolBarButton(c)) {
//                if (!model.isArmed() && !model.isPressed() &&
//
//    "package javax.swing.praf.metal.MetalUtilsはprotectedクラスで、
//    ユーザ側からアクセスすることはできない。IDEからソースを見る事もできない。
//    直接ソースファイルを読むしかなかったが、たいしたことはしていない。
//                        MetalUtils.drawGradient(
//                        c, g, "Button.gradient", 0, 0, c.getWidth(),
//                        c.getHeight(), true)) {
//                    paint(g, c);
//                    return;
//                }
//            }
//            else if (model.isRollover() && MetalUtils.drawGradient(
//                        c, g, "Button.gradient", 0, 0, c.getWidth(),
//                        c.getHeight(), true)) {
//                paint(g, c);
//                return;
//            }
//        }
//        super.update(g, c);
//    }

    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        if ( b.isContentAreaFilled() ) {
            Dimension size = b.getSize();
	    g.setColor(getSelectColor());
	    g.fillRect(2, 2, size.width-3, size.height-3);
	}
    }
    public void update(Graphics g, JComponent c) {
	if (c.isOpaque()) {
	    g.setColor(c.getBackground());
	    g.fillRect(2, 2, c.getWidth()-3,c.getHeight()-3);
	}
	paint(g, c);
    }    
  }

  public void updateUI() {
    setUI( new UI());
    revalidate();
  }
  
  private static void createAndShowGUI() {
    //結局のところ、UIManagerにButton.borderを設定すると、すべてのボタンにそれが
    //反映されてしまうから、全体にそれを適用したいならわざわざ専用のボタンを作る
    //必要はないかもしれない。しかし、色々めんどくさいことがあり、結局、カスタム
    //ボタンには独自のキーを用意してきちんと切り分けるのが良いと思われる。
    //ただしかなりめんどくさい。
    
//    Color darkShadow = UIManager.getColor("Button.darkShadow");
//    Color highlight = UIManager.getColor("Button.highlight");
//    Color shadow = UIManager.getColor("Button.shadow");
//    Color light = UIManager.getColor("Button.light");
//    RBorders.ButtonBorder border = new RBorders.ButtonBorder(shadow, darkShadow, highlight, light);
//    UIManager.put("Button.border", border );
    
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    JFrame frame = new JFrame("カスタムボタン");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());
    RoundButton button = new RoundButton();
    button.setText("大澤義鷹");
    frame.getContentPane().add( button );
//    frame.getContentPane().add( new JButton("大澤義鷹"));
//    frame.getContentPane().add( new JButton("ネイタル"));
//    frame.getContentPane().add( new JButton("トランジット"));
    frame.pack();
    frame.setVisible(true);
  }
  
  public static void main(String [] args) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
}
