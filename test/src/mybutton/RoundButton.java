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
 * �p���܂邭�����{�^�����������B
 * BasicButtonUI���p�����ēƎ���UI�����AJButton���p�����āA����UI��g�ݍ���
 * �K�v������B�܂�ButtonBorders���p�����āA�p���ۂ������{�[�_�[��`�����\�b�h��
 * �p�ӂ��A�����UI�̒��ɑg�ݍ��ޕK�v������B
 *
 * ���Ȃ�߂�ǂ������d���̂����A�{�^����Ȃ�Ƃ������A���̕��i���ׂĂɂ�������
 * �葱�����ӂ�ł����̂͋C�������Ȃ��Ƃ������肷��B
 *
 * ���̎��_�̂ł́A�����邪�����Ƃ�����ہB�X�L���������ō��Ƃ����̂́A���Ȃ�
 * ��ςȍ�ƂŁA�����i������̂ł����(�قƂ�ǂȂ���)������g���ق����}�V����
 * �v����B�Ƃ����킯�ŁA���̃e�[�}����͑ދp�B
 * @author ���V�`��
 */
public class RoundButton extends JButton {
  
  /**  RoundButton �I�u�W�F�N�g���쐬���� */
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
//    "package javax.swing.praf.metal.MetalUtils��protected�N���X�ŁA
//    ���[�U������A�N�Z�X���邱�Ƃ͂ł��Ȃ��BIDE����\�[�X�����鎖���ł��Ȃ��B
//    ���ڃ\�[�X�t�@�C����ǂނ����Ȃ��������A�����������Ƃ͂��Ă��Ȃ��B
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
    //���ǂ̂Ƃ���AUIManager��Button.border��ݒ肷��ƁA���ׂẴ{�^���ɂ��ꂪ
    //���f����Ă��܂�����A�S�̂ɂ����K�p�������Ȃ�킴�킴��p�̃{�^�������
    //�K�v�͂Ȃ���������Ȃ��B�������A�F�X�߂�ǂ��������Ƃ�����A���ǁA�J�X�^��
    //�{�^���ɂ͓Ǝ��̃L�[��p�ӂ��Ă�����Ɛ؂蕪����̂��ǂ��Ǝv����B
    //���������Ȃ�߂�ǂ������B
    
//    Color darkShadow = UIManager.getColor("Button.darkShadow");
//    Color highlight = UIManager.getColor("Button.highlight");
//    Color shadow = UIManager.getColor("Button.shadow");
//    Color light = UIManager.getColor("Button.light");
//    RBorders.ButtonBorder border = new RBorders.ButtonBorder(shadow, darkShadow, highlight, light);
//    UIManager.put("Button.border", border );
    
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    JFrame frame = new JFrame("�J�X�^���{�^��");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());
    RoundButton button = new RoundButton();
    button.setText("���V�`��");
    frame.getContentPane().add( button );
//    frame.getContentPane().add( new JButton("���V�`��"));
//    frame.getContentPane().add( new JButton("�l�C�^��"));
//    frame.getContentPane().add( new JButton("�g�����W�b�g"));
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
