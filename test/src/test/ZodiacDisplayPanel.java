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
 * 黄道十二星座の色設定用ランプパネル
 * @author 大澤義鷹
 */
public class ZodiacDisplayPanel extends javax.swing.JPanel {
  JRadioButton [] radioButtons = new JRadioButton[12];
  protected SymbolDisplay [] symdsp = new SymbolDisplay[12];
  GridBagLayout layout = new GridBagLayout();
  ButtonGroup buttonGroup = new ButtonGroup();
  boolean enabled = true; // Disenabledのとき、ｼﾝﾎﾞﾙのﾏｳｽ選択をｷｬﾝｾﾙするSW
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
  //マウスハンドラー
  class MouseHandler extends MouseAdapter {
    int num;
    // オブジェクト作成時にシリアル番号を受け取る
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
   * すべてのサインの縁取り線色を一括セット
   */
  public void setFG(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setForeground(color);
  }
  /**
   * すべてのサインの背景色を一括セット
   */
  public void setBG(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setBackground(color);
  }
  /**
   * 文字の縁取り色を一括セット
   */
  public void setBorder(Color color) {
    for(int i=0; i<symdsp.length; i++) {
      symdsp[i].setSymbolBorder(color);
    }
  }
  /** 四角の枠線の色をセット */
  public void setFrame(Color color) {
    for(SymbolDisplay d : symdsp)
      d.setFrame(color);    
  }
  /** 四角の枠線の色を返す。(これはサインのセクターの線色)*/
  public Color getFrame() {
    return symdsp[0].getFrame();
  }
  /** 全体の文字の縁取りをするときはtrueをセット */
  public void setBorderVisible(boolean b) {
    for(SymbolDisplay d : symdsp) {
      d.setBorderVisible(b);
      d.repaint();
    }
  }
  /** 四角の枠線を表示するときはtrueをセット */
  public void setFrameVisible(boolean b) {
    for(SymbolDisplay d : symdsp) {
      d.setFrameVisible(b);
      d.repaint();
    }
  }
  /** 12星座の背景色を返す。*/
  public Color [] getBackgrounds() {
    Color [] bg = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      bg[i] = symdsp[i].getBackground();
    return bg;
  }
  /** 12星座のシンボル色を返す。*/
  public Color [] getForegrounds() {
    Color [] fg = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      fg[i] = symdsp[i].getForeground();
    return fg;
  }
  /** 12星座のシンボル縁取り色を返す。*/
  public Color [] getSymbolBorders() {
    Color [] b = new Color[12];
    for(int i=0; i<symdsp.length; i++)
      b[i] = symdsp[i].getSymbolBorder();
    return b;
  }
  /** 選択不可の状態にする */
  public void setRadioButtonEnabled(boolean b) {
    for(JRadioButton radio : radioButtons)
      radio.setEnabled(b);
    enabled = b;
  }  
  /** 選択されたサインの背景色をセット */
  public void setSelectedBG(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    symdsp[ Integer.parseInt(n) ].setBackground(color);
  }
  /** 選択されたサインの背景色を返す */
  public Color getSelectedBG() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getBackground();
  }
  /** 選択されたサインのシンボル色をセット */
  public void setSelectedFG(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    //int count = Integer.parseInt(buttonGroup.getSelection().getActionCommand());
    symdsp[ Integer.parseInt(n) ].setForeground(color);
  }
  /** 選択されたサインのシンボル色を返す */
  public Color getSelectedFG() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getForeground();
  }
  /** 選択されたサインの縁取り色をセットする */
  public void setSelectedBorder(Color color) {
    String n = buttonGroup.getSelection().getActionCommand();
    symdsp[ Integer.parseInt(n) ].setSymbolBorder(color);    
  }
  /** 選択されたサインの縁取り色を返す */
  public Color getSelectedBorder() {
    String n = buttonGroup.getSelection().getActionCommand();
    return symdsp[ Integer.parseInt(n) ].getSymbolBorder();
  }
  /** おひつじ座から３色リピート */
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
  /** 牡羊座から２色リピート */
  public void repeat2() {
    for(int i=2; i <= 10; i += 2) {
      symdsp[i].setColor(symdsp[0]);
      symdsp[i+1].setColor(symdsp[1]);
    }
  }
  /** 
   * 牡羊座から指定個数分の色設定をリピートして設定する。 
   */
  public void repeat(int count) {
    for(int i=count; i < 12; i += count) {
      for(int j=0; j<count; j++)
        symdsp[i+j].setColor(symdsp[j]);
    }
  }
  /**
   * 十二色の円環を生成。０，３，６，９番目のシンボルオブジェクトの色情報から
   * 自動生成。
   */
  public void colorRing() {
    Color [] c = new Color[12];
    for(int i=0; i<symdsp.length; i++) c[i] = symdsp[i].getBackground();
    ColorCalcurator.getColorRing(c);
    for(int i=0; i<symdsp.length; i++) symdsp[i].setBackground(c[i]);
  }
}
