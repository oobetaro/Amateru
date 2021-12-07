/*
 * RBorders.java
 *
 * Created on 2007/11/11, 5:06
 *
 */

package mybutton;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * �p���ۂ߂��{�^�������ɂ́A�{�^���̘g���`��̕��������AButtonUI�̒���
 * �o�^����K�v������BBasicBorder�N���X�̓{�^���ȊO�ɂ����W�I�{�^����g�O���{�^��
 * ���j���[�o�[�₻�̑��l�X�ȃ{�[�_�[�`�惁�\�b�h���L�q����Ă���B���i���Ƃ�
 * �T�u�N���X������Ă���B
 * ���̓{�^�������̃J�X�^�}�C�Y��ڎw���Ă���̂ŁA���ʂ̃{�^���̕`��N���X��
 * �I�[�o�[���C�h���Ă���������B
 * �{�^���̏ꍇ�A�����܂ł��{�[�_�[���݂̂�`�悷��K�v�������āA���̍ہA�h���
 * ���悤�Ȃ��Ƃ�����ƁA�{�^���ɏ�����镶���܂ŏ����Ă��܂��B
 * �ǂ����`�揇���Ƃ��āA�͂��߂ɕ�����������A���̂��ƂŃ{�[�_�[�����`�����
 * �d�g�݂炵���B
 * 
 * �{�^����h��Ԃ��̂́AButtonUI�̒��ōs���K�v������B
 * ���Ǐ����Ƃ��ẮAButtonUI�̒��œh��Ԃ��A���ɕ�����������A�Ō�Ƀ{�[�_�[
 * �Ƃ������Ƃ��낾�낤�B
 * @author ���V�`��
 */
public class RBorders  extends BasicBorders {
  
    public static class ButtonBorder extends AbstractBorder implements UIResource {
        protected Color shadow;
        protected Color darkShadow;
        protected Color highlight;
        protected Color lightHighlight;

        public ButtonBorder(Color shadow, Color darkShadow, 
                            Color highlight, Color lightHighlight) {
            this.shadow = shadow;
            this.darkShadow = darkShadow;
            this.highlight = highlight; 
            this.lightHighlight = lightHighlight;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, 
                            int width, int height) {
            boolean isPressed = false;
            boolean isDefault = false;
            if (c instanceof AbstractButton) {
	        AbstractButton b = (AbstractButton)c;
	        ButtonModel model = b.getModel();
	
   	        isPressed = model.isPressed() && model.isArmed();

                if (c instanceof JButton) {
                    isDefault = ((JButton)c).isDefaultButton();
                }
            }	
//            BasicGraphicsUtils.drawBezel(g, x, y, width, height, 
//				   isPressed, isDefault, shadow,
//                                   darkShadow, highlight, lightHighlight);
            //isDefault�Ƃ����̂́A�p�l�����ł̃f�t�H���g�{�^���Ƃ����Ӗ��ŁA
            //������Ă��Ȃ��{�^���̂��Ƃł͂Ȃ��B
            int r = 6;
            drawRect(g,x,y,width,height,isPressed);
        }
        void drawRect(Graphics g,int x,int y,int width,int height,boolean pressed) {
          Color rise = pressed ? shadow : lightHighlight;
          Color lower = pressed ? lightHighlight : shadow;
          int hx1 = x + 2;
          int hx2 = x + width -3;
          int hy1 = y;
          int hy2 = y + height - 1;
          int vx1 = x;
          int vx2 = x + width - 1;
          int vy1 = y + 2;
          int vy2 = y + height -3;
          g.setColor(darkShadow);
          g.drawLine(hx1,hy1,hx2,hy1);
          g.drawLine(hx1,hy2,hx2,hy2);
          g.drawLine(vx1,vy1,vx1,vy2);
          g.drawLine(vx2,vy1,vx2,vy2);
          g.drawLine( hx1-1, hy1+1, hx1-1, hy1+1);
          g.drawLine( hx2+1, hy1+1, hx2+1, hy1+1);
          g.drawLine( vx1+1, vy2+1, vx1+1, vy2+1);
          g.drawLine( vx2-1, vy2+1, vx2-1, vy2+1);
          //
          g.setColor(rise);
          g.drawLine(hx1,hy1+1,hx2,hy1+1);
          g.drawLine(vx1+1,vy1,vx1+1,vy2);
          g.setColor(lower);
          g.drawLine(vx2-1,vy1,vx2-1,vy2);
          g.drawLine(hx1,hy2-1,hx2,hy2-1);
        }
        public Insets getBorderInsets(Component c)       {
            return getBorderInsets(c, new Insets(0,0,0,0));
        }

        public Insets getBorderInsets(Component c, Insets insets)       {
            // leave room for default visual
            insets.top = 3;
            insets.bottom = 3;
            insets.left = insets.right = 12;
	    return insets;
        }

    }    

}
