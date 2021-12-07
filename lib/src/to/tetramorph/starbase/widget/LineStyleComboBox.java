/*
 * LineStyleComboBox.java
 *
 * Created on 2006/12/29, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.widget;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import to.tetramorph.starbase.lib.AspectStyle;

/**
 * ラインスタイルを選択するオリジナルのコンポボックス。
 * 線種が視覚的にリスト表示され、getSelectedStroke()で、選択した線のストロークを
 * 取得できる。
 * 線種は6種類用意されていて、これ以上追加することも削除することも想定していない。
 * したがってインスタンスを作成すればそのまま使用できなにも設定はいらない。
 */
public class LineStyleComboBox extends JComboBox {
  static final Integer [] items = { 0,1,2,3,4,5 };
  /** 
   * オブジェクトを作成する。 
   */
  public LineStyleComboBox() {
    ComboBoxRenderer renderer = new ComboBoxRenderer();
    renderer.setPreferredSize(new Dimension(70, 16));
    setRenderer(renderer);
    setMaximumRowCount( AspectStyle.strokes.length );
    //数値を入れておかないと、レンダラ側でアイテムの番号を取得できない様子
    for(int i=0; i<items.length; i++) addItem(items[i]);
    //for(int i=0; i<stroke.length; i++) addItem(stroke[i]); ←これはダメ
  }
  // 線を描く自前レンダラー
  class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    int selectedIndex = 0;
    public ComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }    
    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {
      //Get the selected index. (The index param isn't
      //always valid, so just use the value.)
      selectedIndex = ((Integer)value).intValue();
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      return ComboBoxRenderer.this;
    }
    public void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      Graphics2D g = (Graphics2D)graphics;
      Dimension size = getSize();
      double width = (float)size.getWidth();
      double height = (float)size.getHeight();
      Line2D line = new Line2D.Double(3,height/2,width-10,height/2);
      g.setStroke( AspectStyle.strokes[ selectedIndex ] );
      g.draw(line);
    }
  }
  /**
   * 選択されたストロークを返す。
   */
  public Stroke getSelectedStroke() {
    return AspectStyle.strokes[ getSelectedIndex() ];
  }

  private static void createAndShowGUI() {
    JFrame frame = new JFrame("CustomComboBoxDemo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    panel.add(new LineStyleComboBox());
    panel.add(new LineStyleComboBox());
    frame.setContentPane(panel);
    frame.pack();
    frame.setVisible(true);
  }
  /** テスト */
  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
  
}
