/*
 *
 */
package to.tetramorph.starbase.widget;

import to.tetramorph.starbase.lib.Const;
import to.tetramorph.util.IconLoader;
import java.awt.Component;
import java.awt.FlowLayout;
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
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import static to.tetramorph.starbase.lib.Const.*;
/**
 * 感受点を選択するためのコンボボックス。<br>
 * 感受点はGIF画像で作られたアイコンで表示する。
 * GIF画像はclassファイルの置き場所の中にある/resources/symbols/の中にある事が前提
 * で、これを変更することはできない。
 * GIF画像のファイル名は、Const#SYMBOL_NAMES[]に入っている名前に".gif"を付けたもの
 * となる。コンストラクタの呼び出しの際に、必要なシンボル画像を読みこむ。
 * 値の取得はgetSelectedBody()を使用する。
 * <pre>
 * 使い方
 * コンボボックスに入れる惑星シンボルは整数配列で指定する。
 * (Constをstatic importして、SUNやMOONといった定数を直書きできるようにした上で)
 *
 * Integer [] dummy = {
 * SUN,MOON,MERCURY,VENUS,JUPITER,SATURN,
 * URANUS,NEPTUNE,PLUTO,
 * MEAN_NODE,TRUE_NODE,MEAN_APOG,OSCU_APOG,
 * EARTH,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,
 * ASC,DSC,MC,IC,VERTEX };
 * 
 * PlanetComboBox planetComboBox = new PlanetComboBox(dummy);
 * </pre>
 */
public class PlanetComboBox extends JComboBox {
  //惑星シンボルのImageIconを格納するハッシュマップ。
  private static final Map<Integer,ImageIcon> 
    map = new HashMap<Integer,ImageIcon>();
  //シンボルのImageIconはstaticで作成しハッシュマップに入れて共有。
  static {
    for(int i=0; i<SYMBOL_NAMES.length; i++) {
      if(SYMBOL_NAMES[i] == null) continue;
      ImageIcon imageIcon = 
        IconLoader.getImageIcon("/resources/symbols/" + SYMBOL_NAMES[i] + ".gif");
      if( imageIcon != null) {
        imageIcon.setDescription(SYMBOL_NAMES[i]);
        map.put(i,imageIcon);
      }
    }
  }
  /**
   * 感受点番号(Constで宣言されている感受点番号)のリストから、オブジェクトを
   * 作成する。このコンボボックスはGIF画像で、感受点シンボルが表示される。
   * getSelectedItem()で取得されるのは与えた天体番号。
   * @param planetArray Constで定義されている惑星番号を配列にしたもの。
   */
  public PlanetComboBox(Integer [] planetArray) {
    super(planetArray);
    ComboBoxRenderer renderer= new ComboBoxRenderer();
    //renderer.setPreferredSize(new Dimension(15, 16));
    renderer.setBorder( BorderFactory.createEmptyBorder(2,2,2,2));
    setRenderer(renderer);
    setMaximumRowCount(planetArray.length);
  }
  /**
   * GUIエディタ用のコンストラクタ。デフォルトでいくつかの天体シンボルを代入。
   */
  public PlanetComboBox() {
    //this(new Integer [] { 0,1,2,3,4,5,6,7,8,9 });
    this(new Integer [] { SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,
      NEPTUNE,PLUTO,NODE,APOGEE,CHIRON,PHOLUS,CERES,PALLAS,JUNO,VESTA,
      SOUTH_NODE,ANTI_APOGEE });
  }
  /**
   * コンボボックスに惑星シンボルを追加する。追加後、MaximumRowContの値が更新
   * される。
   * @param planetNumber Integer型の惑星番号
   * @see Const SUN,MOON等の天体番号。
   */
  public void addItem(Object planetNumber) {
    Integer num = (Integer)planetNumber;
    super.addItem(planetNumber);
    setMaximumRowCount(dataModel.getSize());
  }
  /**
   * 複数の惑星シンボルを一度にセットする。セット前に既存のシンボルを削除する。
   * @param planetArray 惑星番号を格納した配列
   * @see Const 中のSUN,MOON等の天体番号。
   */
  public void setItems(Integer [] planetArray) {
    ((DefaultComboBoxModel)dataModel).removeAllElements();
    for(Integer i : planetArray) addItem(i);
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
      String name = Const.SYMBOL_NAMES[i.intValue()];
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
  /**
   * 選択されている惑星シンボルに対応する惑星ID(Const内で定義されている)を返す。
   */
  public int getSelectedBody() {
    int planet = ((Integer)getSelectedItem()).intValue();
    return planet;
  }
  /**
   * 指定された天体IDで選択する。
   */
  public void setSelectedBody(int bodyID) {
    setSelectedItem(new Integer(bodyID));
  }
  // テストメソッド-----------------------------------------------------------
  static void createAndShowGUI() {
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    try {
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch ( Exception e ) {
        e.printStackTrace();
    }
    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());
    final PlanetComboBox widget = new PlanetComboBox();
    frame.getContentPane().add( widget );
    frame.pack();
    frame.setLocationRelativeTo(null);
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

