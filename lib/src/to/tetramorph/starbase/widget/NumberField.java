/*
 * NumberField.java
 *
 * Created on 2007/11/14, 2:04
 */

package to.tetramorph.starbase.widget;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import to.tetramorph.starbase.formatter.FormatterFactory;

/**
 * 整数値の入力を前提としたスライダーつきの数値入力ボックス。
 * 入力フィールドの右側にあるボタンをクリックすると、スライダーが出現し
 * ノブを上下することで入力値を変更できる。
 * またボタンの上でマウスホイールを上下にころがすと値の加減算ができる。
 * スライダーの上でもホイールで上下に加減できる。
 * 日付の入力用に作ったもので機能は限られている。負数は受け付けない、整数値のみ、
 * 最大値の設定は可能。ボタンについている記号は/resourcesの中のdoubleArrow.gifで
 * 透過gifを使用。
 * @author  大澤義鷹
 */
public class NumberField extends javax.swing.JPanel {
  
  private static final Integer zero = new Integer(0);
  private JPopupMenu popup = new JPopupMenu();
  private int max = 100;
  /** 
   * オブジェクトを作成する。 入力可能な値は0〜100に設定される。
   */
  public NumberField() {
    initComponents();
    popup.addPopupMenuListener(new PopupHandler());
    popup.add(sliderPanel);
    textField.setFormatterFactory(new FormatterFactory(new Formatter()));
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        JSlider slider = (JSlider)evt.getSource();
        textField.setValue(new Integer(slider.getValue()));
      }
    });
  }
  
  /**
   * 入力可能な最大値をセットする。その際、既存の値はゼロにリセットされる。
   */
  public void setMaximum(int max) {
    if(max < 0) throw new IllegalArgumentException("負数は指定できません");
    this.max = max;
    slider.setMaximum(max);
    textField.setValue(0);
    slider.setValue(0);
  }

  /**
   * 入力可能な最大値を返す。
   */
  public int getMaximum() {
    return max;
  }

  /**
   * 入力フォームに値をセットする。
   * @exception IllegalArgumentException 最大値を超えた値を指定したとき。
   */
  public void setValue(int value) {
    if(max < value) throw 
      new IllegalArgumentException("最大値を超過してます");
    textField.setValue(value);
    slider.setValue(value);    
  }

  /**
   * 入力値を返す。
   */
  public int getValue() {
    return ((Integer)textField.getValue()).intValue();
  }
  // ボタンのイベントから呼ばれ、ポップアップでスライダーを表示する。
  
  private void showSlider() {
    Integer value = (Integer)textField.getValue();
    if(value == null) value = zero;
    slider.setValue( value );
    Point p = button.getLocation();
    popup.setVisible(true); //可視化しないとサイズは決まらない
    p.x += 0; //button.getWidth();// - popup.getWidth();
    p.y += button.getHeight() - popup.getHeight();
    popup.show(this,p.x,p.y);
  }
  
  /**
   * ポップアップ表示中はボタンを押せなくする。表示中かどうかの判定は、
   * ポップアップにリスナを登録しなければならない。
   */
  private class PopupHandler implements PopupMenuListener {
    //可視化する前
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      button.setEnabled(false);
    }
    //不可視化する前
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      button.setEnabled(true);
    }
    //メニューとりけし
    public void popupMenuCanceled(PopupMenuEvent e) {
    }    
  }
  
  /**
   * 入力フィールドのフォーマッタ。0以下の値と、整数値以外の値は受け付けない。
   */
  private class Formatter extends JFormattedTextField.AbstractFormatter {
    /** 与えられた整形済み文字列を、値を表すObjectにして返すメソッド */    
    public Object stringToValue(String text) throws ParseException {
      if(text == null) return zero;
      text = text.trim();
      if(text.length() == 0) return zero;
      try {
        Integer value = Integer.parseInt(text);
        if(value < 0 ) value = zero;
        if(value >= max) value = max;
        return value;
      } catch ( NumberFormatException e) {
      }
      return zero;
    }
    
    /** 与えられたvalueを整形された文字列表現にして返すメソッド */
    
    public String valueToString(Object value) throws ParseException {
      if(value == null) return "0";
      return value.toString();
    }
  }
  
  private void mouseWheelMove(int offset) {
    Integer value = (Integer)textField.getValue();
    if(value == null) value = zero;
    value -= offset;
    if(value < 0) value = zero;
    if(value >= max) value = max;
    textField.setValue(value);    
  }


//  // テストメソッド-----------------------------------------------------------
//  static void createAndShowGUI() {
//    UIManager.put("swing.boldMetal", Boolean.FALSE);
//        try {
//                 UIManager.setLookAndFeel(
//                        "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }    final JFrame frame = new JFrame();
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setLayout(new FlowLayout());
//    final NumberField nf = new NumberField();
//    nf.setMaximum(356);
//    frame.getContentPane().add(nf);
//    //JButton testButton = new JButton("Value?");
//    JButton testButton = new JButton("　　　　　　");
//    testButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent evt) {
//        JOptionPane.showMessageDialog(frame,"value = " + nf.getValue());
//      }
//    });
//    frame.add(testButton);
//    frame.pack();
//    frame.setLocationRelativeTo(null);
//    frame.setVisible(true);
//  }
//
//  public static void main(String[] args) {
//    javax.swing.SwingUtilities.invokeLater(new Runnable() {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sliderPanel = new javax.swing.JPanel();
        slider = new javax.swing.JSlider();
        textField = new javax.swing.JFormattedTextField();
        button = new javax.swing.JButton();

        sliderPanel.setLayout(new java.awt.GridLayout(1, 0));

        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sliderMouseWheelMoved(evt);
            }
        });
        sliderPanel.add(slider);

        setLayout(new java.awt.GridBagLayout());

        textField.setColumns(2);
        textField.setText("0");
        add(textField, new java.awt.GridBagConstraints());

        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/doubleArrow.png"))); // NOI18N
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setMaximumSize(null);
        button.setMinimumSize(null);
        button.setPreferredSize(new java.awt.Dimension(20, 24));
        button.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                buttonMouseWheelMoved(evt);
            }
        });
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(button, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void buttonMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_buttonMouseWheelMoved
    mouseWheelMove(evt.getWheelRotation());
  }//GEN-LAST:event_buttonMouseWheelMoved

  private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
    showSlider();
  }//GEN-LAST:event_buttonActionPerformed
  //スライダーをマウスホイールで上下させる。
  private void sliderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sliderMouseWheelMoved
      int value = getValue() - evt.getWheelRotation();
      if ( value < 0 ) value = 0;
      if ( value > getMaximum()) value = getMaximum();
      setValue( value );
  }//GEN-LAST:event_sliderMouseWheelMoved
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button;
    private javax.swing.JSlider slider;
    private javax.swing.JPanel sliderPanel;
    private javax.swing.JFormattedTextField textField;
    // End of variables declaration//GEN-END:variables
  
}
