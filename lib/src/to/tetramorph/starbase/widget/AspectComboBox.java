/*
 * AspectComboBox.java
 *
 * Created on 2006/09/04, 18:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.widget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.util.IconLoader;

/**
 * アスペクトを選択するためのコンボボックス。
 * 感受点はGIF画像で作られたアイコンで表示する。
 * GIF画像はclassファイルの置き場所の中にある/resources/symbols/の中にある事が前提
 * で、これを変更することはできない。
 * GIF画像のファイル名は、Const.ASPECT_SYMBOL_NAMES[]に入っている名前に".gif"を付けたもの
 * となる。コンストラクタの呼び出しの際に、必要なシンボル画像を読みこむ。
 * static配列にロードするのでいくつコンボボックスを増やしてもイメージアイコンは一つ。
 * <pre>
 * 使い方
 * コンボボックスに入れる惑星シンボルは整数配列で指定する。
 * (Constをstatic importして、SUNやMOONといった定数を直書きできるようにした上で)
 *
 * Integer [] dummy = { 0,1,2 ... 11 };
 * ({ CONJUNCTION,SEXTILE,SQUARE,TRINE,,,, }でもよい。要は11まである。)
 * AspectComboBox aspectComboBox = new AspectComboBox(dummy);
 * </pre>
 */
public class AspectComboBox extends JComboBox {
  private static final Map<Integer,ImageIcon> 
    map = new HashMap<Integer,ImageIcon>();
  //シンボルのImageIconはstaticで作成しハッシュマップに入れて共有。
  static {
    for(int i=0; i<Const.ASPECT_SYMBOL_NAMES.length; i++) {
      ImageIcon imageIcon = 
        IconLoader.getImageIcon("/resources/symbols/" + Const.ASPECT_SYMBOL_NAMES[i] + ".gif");
      if( imageIcon != null) {
        imageIcon.setDescription(Const.ASPECT_SYMBOL_NAMES[i]);
        map.put(i,imageIcon);
      }
    }
  }
  /**
   * 引数なしのコンストラクタで、パラレル以外の12種類のアスペクト記号を選択可能
   * なコンボボックスを作成する。
   */
  public AspectComboBox() {
    this(new Integer [] { 0,1,2,3,4,5,6,7,8,9,10,11 } );
  }
  /** 
   * 指定のアスペクト群の中から選択できるコンボボックスを作成する。
   * @param aspectArray 0から11までの定数。Const内では、CONJUNCTION,SEXTILE,
   * SQUARE,TRINEなどと名前で宣言されているので、それを使って指定する。
   */
  public AspectComboBox(Integer [] aspectArray) {
    super(aspectArray);
    ComboBoxRenderer renderer= new ComboBoxRenderer();
    //renderer.setPreferredSize(new Dimension(15, 16));
    renderer.setBorder( BorderFactory.createEmptyBorder(2,2,2,2));
    setRenderer(renderer);
    setMaximumRowCount(aspectArray.length);
  }
  /**
   * コンボボックスにアスペクトシンボルを追加する。追加後、MaximumRowContの値が更新
   * される。
   * @param aspectNumber Integer型のアスペクト番号
   * @see Const SQUARE,TRINE等の天体番号。
   */
  public void addItem(Object aspectNumber) {
    Integer num = (Integer)aspectNumber;
    super.addItem(aspectNumber);
    setMaximumRowCount(dataModel.getSize());
  }
  /**
   * 複数のアスペクトシンボルを一度にセットする。セット前に既存のシンボルを削除する。
   * @param aspectArray アスペクト番号を格納した配列
   * @see Const 内のSQUARE,TRINE等の天体番号。
   */
  public void setItems(Integer [] aspectArray) {
    ((DefaultComboBoxModel)dataModel).removeAllElements();
    for(Integer i : aspectArray) addItem(i);
  }
  
  private class ComboBoxRenderer extends JLabel
    implements ListCellRenderer {
    private Font uhOhFont;
    
    public ComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }
    public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus) {
      if(value == null) return this; //nullは相手しない
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      Integer i = (Integer)value;
      ImageIcon icon = map.get(i); //imageIcons[selectedIndex];
      String name = Const.ASPECT_NAMES[i.intValue()];
      setIcon(icon);
      if (icon != null) {
        //setText(name); 画像に加え文字も必要ならテキストをセット
        //setFont(list.getFont());
      } else {
        setUhOhText(name,list.getFont());
      }
      return this;
    }
    //GIFのシンボルがちゃんと存在すればこれは不要ではあるが、シンボルファイルが
    //無いときはどのシンボルが無いか文字で教えてくれるからエラー表示として便利。
    private void setUhOhText(String uhOhText, Font normalFont) {
      if (uhOhFont == null) { //lazily create this font
        uhOhFont = normalFont.deriveFont(Font.ITALIC);
      }
      setFont(uhOhFont);
      setText(uhOhText);
    }
  }
  private static void createAndShowGUI() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("AspectComboBoxDemo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new JPanel(new BorderLayout());
    Integer [] dummy = { 0,1,2,3,4,5,6,7,8,9,10,11 };
    AspectComboBox comboBox = new AspectComboBox(dummy);
    panel.add(comboBox,BorderLayout.NORTH);
    frame.setContentPane(panel);
    frame.pack();
    frame.setVisible(true);
  }
  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
  
}
