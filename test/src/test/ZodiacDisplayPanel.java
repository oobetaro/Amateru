/*
 * ZodiacDisplayPanel.java
 *
 * Created on 2006/12/24, 20:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.widget;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import to.tetramorph.starbase.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.util.ColorCalcurator;

/**
 * �����\�񐯍��̐F�ݒ�p�����v�p�l��
 * @author ���V�`��
 */
public class ZodiacDisplayPanel extends javax.swing.JPanel {
  JRadioButton [] radioButtons = new JRadioButton[12];
  protected SymbolDisplay [] symdsp = new SymbolDisplay[12];
  GridBagLayout layout = new GridBagLayout();
  ButtonGroup buttonGroup = new ButtonGroup();
  boolean enabled = true; // Disenabled�̂Ƃ��A����ق�ϳ��I����ݾق���SW
  /** Creates a new instance of ZodiacDisplayPanel */
  public ZodiacDisplayPanel() {
    for(int i=0; i<12; i++) {
      radioButtons[i] = new JRadioButton();
      radioButtons[i].setActionCommand(""+i);
      buttonGroup.add(radioButtons[i]);
      symdsp[i] = new SymbolDisplay();
      symdsp[i].addMouseListener(new MouseHandler(i));
      symdsp[i].setChar(Const.ZODIAC_CHARS[i]);
    }
    setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();
    for(int i=0; i<12; i++) {
      c.gridx = i; c.gridy = 0; c.gridwidth = 1; c.gridheight = 1;
      layout.setConstraints(radioButtons[i],c);
      add(radioButtons[i]);
    }
    for(int i=0; i<12; i++) {
      c.gridx = i; c.gridy = 1; c.gridwidth = 1; c.gridheight = 1;
      layout.setConstraints(symdsp[i],c);
      add(symdsp[i]);
    }
    radioButtons[0].setSelected(true);
    setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
  }
  //�}�E�X�n���h���[
  class MouseHandler extends MouseAdapter {
    int num;
    // �I�u�W�F�N�g�쐬���ɃV���A���ԍ����󂯎��
    MouseHandler(int num) {
      this.num = num;
    }
    public void mouseReleased(MouseEvent evt) {
      if(enabled)
      radioButtons[num].setSelected(true);
    }
  }
  public void setBackground(Color color) {
    super.setBackground(color);
    if(radioButtons == null) return;
    for(int i=0; i<radioButtons.length; i++) {
      radioButtons[i].setBackground(color);
    }
  }
  /**
   * ���ׂẴT�C���̉������F���ꊇ�Z�b�g
   */
  public void setFG(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setForeground(color);
  }
  /**
   * ���ׂẴT�C���̔w�i�F���ꊇ�Z�b�g
   */
  public void setBG(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setBackground(color);
  }
  /**
   * �����̉����F���ꊇ�Z�b�g
   */
  public void setBorder(Color color) {
    for(int i=0; i<symdsp.length; i++) {
      symdsp[i].setSymbolBorder(color);
    }
  }
  /** �l�p�̘g���̐F���Z�b�g */
  public void setFrame(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setFrame(color);    
  }
  /** �l�p�̘g���̐F��Ԃ��B(����̓T�C���̃Z�N�^�[�̐��F)*/
  public Color getFrame() {
    return symdsp[0].getFrame();
  }
  /** �S�̂̕����̉���������Ƃ���true���Z�b�g */
  public void setBorderVisible(boolean b) {
    for(SymbolDisplay d : symdsp) {
      d.setBorderVisible(b);
      d.repaint();
    }
  }
  /** �l�p�̘g����\������Ƃ���true���Z�b�g */
  public void setFrameVisible(boolean b) {
    for(SymbolDisplay d : symdsp) {
      d.setFrameVisible(b);
      d.repaint();
    }
  }
  /** 12�����̔w�i�F��Ԃ��B*/
  public Color [] getBackgrounds() {
    Color [] bg = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      bg[i] = symdsp[i].getBackground();
    return bg;
  }
  /** 12�����̃V���{���F��Ԃ��B*/
  public Color [] getForegrounds() {
    Color [] fg = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      fg[i] = symdsp[i].getForeground();
    return fg;
  }
  /** 12�����̃V���{�������F��Ԃ��B*/
  public Color [] getSymbolBorders() {
    Color [] b = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      b[i] = symdsp[i].getSymbolBorder();
    return b;
  }
  /** �I��s�̏�Ԃɂ��� */
  public void setRadioButtonEnabled(boolean b) {
    for(JRadioButton radio : radioButtons)
      radio.setEnabled(b);
    enabled = b;
  }  
  /** �I�����ꂽ�T�C���̔w�i�F���Z�b�g */
  public void setSelectedBG(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    symdsp[ Integer.parseInt(n) ].setBackground(color);
  }
  /** �I�����ꂽ�T�C���̔w�i�F��Ԃ� */
  public Color getSelectedBG() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getBackground();
  }
  /** �I�����ꂽ�T�C���̃V���{���F���Z�b�g */
  public void setSelectedFG(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    //int count = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
    symdsp[ Integer.parseInt(n) ].setForeground(color);
  }
  /** �I�����ꂽ�T�C���̃V���{���F��Ԃ� */
  public Color getSelectedFG() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getForeground();
  }
  /** �I�����ꂽ�T�C���̉����F���Z�b�g���� */
  public void setSelectedBorder(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    symdsp[ Integer.parseInt(n) ].setSymbolBorder(color);    
  }
  /** �I�����ꂽ�T�C���̉����F��Ԃ� */
  public Color getSelectedBorder() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getSymbolBorder();
  }
  /** ���Ђ�������R�F���s�[�g */
  public void repeat3() {
    symdsp[3].setColor(symdsp[0]);
    symdsp[6].setColor(symdsp[0]);
    symdsp[9].setColor(symdsp[0]);
    symdsp[4].setColor(symdsp[1]);
    symdsp[7].setColor(symdsp[1]);
    symdsp[10].setColor(symdsp[1]);
    symdsp[5].setColor(symdsp[2]);
    symdsp[8].setColor(symdsp[2]);
    symdsp[11].setColor(symdsp[2]);
  }
  /** ���r������Q�F���s�[�g */
  public void repeat2() {
    for(int i=2; i <= 10; i += 2) {
      symdsp[i].setColor(symdsp[0]);
      symdsp[i+1].setColor(symdsp[1]);
    }
  }
  /** 
   * ���r������w������̐F�ݒ�����s�[�g���Đݒ肷��B 
   */
  public void repeat(int count) {
    for(int i=count; i < 12; i += count) {
      for(int j=0; j<count; j++)
        symdsp[i+j].setColor(symdsp[j]);
    }
  }
  /**
   * �\��F�̉~�𐶐��B�O�C�R�C�U�C�X�Ԗڂ̃V���{���I�u�W�F�N�g�̐F��񂩂�
   * ���������B
   */
  public void colorRing() {
    Color [] c = new Color[12];
    for(int i=0; i<symdsp.length; i++) c[i] = symdsp[i].getBackground();
    ColorCalcurator.getColorRing(c);
    for(int i=0; i<symdsp.length; i++) symdsp[i].setBackground(c[i]);
  }
}
