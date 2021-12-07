/*
 * AspectsPanel.java
 *
 * Created on 2007/01/01, 7:14
 */

package to.tetramorph.starbase.widget;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import to.tetramorph.starbase.lib.AspectStyle;
import to.tetramorph.starbase.lib.Caption;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * アスペクトを種類別に色設定するパネル。
 */
public class AspectsPanel extends javax.swing.JPanel {
  private int STYLE_LENGTH = 25;
  private JRadioButton [] radioButtons = new JRadioButton[ STYLE_LENGTH ];
  private LineStylePanel [] lineStylePanels = new LineStylePanel[ STYLE_LENGTH ];
  private AspectStyle [] aspectStyles = new AspectStyle[ STYLE_LENGTH ];
  // ラインスタイパネル → ラジオボタン
  private Map<LineStylePanel,ButtonModel> p2b =
    new HashMap<LineStylePanel,ButtonModel>();
  // ラジオボタン → ラインスタイル
  private Map<ButtonModel,LineStylePanel> b2p =
    new HashMap<ButtonModel,LineStylePanel>();
  // ラジオボタン → アスペクトスタイル
  private Map<ButtonModel,AspectStyle> b2s =
    new HashMap<ButtonModel,AspectStyle>();
  
  private Color bgColor = Color.WHITE;      //背景色
  private Color circleColor = Color.WHITE;  //アスペクト円の背景色

  // 0個目はハイライト用、1個目からがアスペクト用
  private int [] aspectIDs = { CONJUNCTION,
    CONJUNCTION, SEXTILE, SQUARE, TRINE, OPPOSITION,
    SEMI_SEXTILE, SEMI_SQUARE, SESQIQUADRATE, QUINCUNX,
    QUINTILE, DECILE, BIQUINTILE
  };
  private JLabel [] symbolLabels;           //アスペクトシンボル格納用
  /** 
   * アスペクトパネルを作成する。 
   */
  public AspectsPanel() {
    initComponents();
    //アスペクトシンボルのラベルにツールTipsをセットする。
    symbolLabels = new JLabel[] { null,
    a0Label,a60Label,a90Label,a120Label,a180Label,a30Label,a45Label,a135Label,
    a150Label,a72Label,a36Label,a144Label };
    for(int i=1; i<aspectIDs.length; i++)
      symbolLabels[i].setToolTipText(
        Caption.getAspectSymbolCaption(aspectIDs[i]));
    
    // アスペクト選択ラジオボタンを配列化
    radioButtons = new JRadioButton[] { hRadioButton,
    jRadioButton1,jRadioButton2,jRadioButton3,jRadioButton4,jRadioButton5,
    jRadioButton6,jRadioButton7,jRadioButton8,jRadioButton9,jRadioButton10,
    jRadioButton11,jRadioButton12,jRadioButton13,jRadioButton14,jRadioButton15,
    jRadioButton16,jRadioButton17,jRadioButton18,jRadioButton19,jRadioButton20,
    jRadioButton21,jRadioButton22,jRadioButton23,jRadioButton24 };
    
    // アスペクト選択と背景色選択のラジオボタンをボタングループに登録
    for(int i=0; i<radioButtons.length; i++) buttonGroup.add(radioButtons[i]);
    buttonGroup.add( bgRadioButton );
    
    //ラインスタイルパネルを配列化
    lineStylePanels = new LineStylePanel [] { hLineStylePanel,
    lineStylePanel1,lineStylePanel2,lineStylePanel3,lineStylePanel4,
    lineStylePanel5,lineStylePanel6,lineStylePanel7,lineStylePanel8,
    lineStylePanel9,lineStylePanel10,lineStylePanel11,lineStylePanel12,
    lineStylePanel13,lineStylePanel14,lineStylePanel15,lineStylePanel16,
    lineStylePanel17,lineStylePanel18,lineStylePanel19,lineStylePanel20,
    lineStylePanel21,lineStylePanel22,lineStylePanel23,lineStylePanel24 };
    
    //アスペクトスタイルの配列を初期化
    aspectStyles[0] = new AspectStyle( CONJUNCTION, true ); //ハイライト用
    for(int i=1; i <= 12; i++) {
      aspectStyles[i] = new AspectStyle( aspectIDs[i], true );
      aspectStyles[i+12] = new AspectStyle( aspectIDs[i], false );
    }
    
    // ラジオボタンにリスナを登録
    // アスペクト選択ラジオボタンが押されたときは、コンボをEnabledにし、
    // 背景色ラジオボタンが押されたときはDisenabledにする。
    ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            comboBox.setEnabled(evt.getSource() != bgRadioButton);
        }
    };
    for ( Enumeration enu = buttonGroup.getElements(); enu.hasMoreElements();)
      ((JRadioButton)enu.nextElement()).addActionListener(al);
    
    // アスペクトパネルにリスナを登録
    // アスペクトパネルがクリックされたら対応するラジオボタンを選択
    MouseAdapter m = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent evt) {
            p2b.get(evt.getSource()).setSelected(true);
            comboBox.setEnabled(true);
        }
    };
    //リスナへの登録と、変換用ハッシュの初期化を同時に行う
    for(int i=0; i<lineStylePanels.length; i++) {
      p2b.put( lineStylePanels[i], radioButtons[i].getModel() );
      b2p.put( radioButtons[i].getModel(), lineStylePanels[i] );
      b2s.put( radioButtons[i].getModel(), aspectStyles[i] );
      lineStylePanels[i].addMouseListener( m );
    }
    colorPalettePanel.setChangeListener( new PaletteHandler() );
    setAspectBGColor( getBGColor() ); //アスペクト円背景色をセット
    setDefaults();                    //デフォルトのアスペクトスタイルを登録
  }
  
  //ｶﾗｰﾊﾟﾚｯﾄで選択が起きた際のイベントハンドラ
  
  private class PaletteHandler implements ChangeListener {
      @Override
      public void stateChanged(ChangeEvent e) {
          Color c = colorPalettePanel.getSelectedColor();
          //αチャンネルを若干透過させる(描画時に線がなじむ)
          Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), 240);
          LineStylePanel p = b2p.get(buttonGroup.getSelection());
          AspectStyle s = b2s.get(buttonGroup.getSelection());
          // ハッシュには背景色のボタンは登録されていないので
          // nullのときは背景色への設定とみなせるが、
          if (p == null) {
              //ただし背景色なしのﾁｪｯｸがあるときは設定しない
              if (!bgCheckBox.isSelected()) {
                  setAspectBGColor(color);
              }
          } else {
              p.setForeground(color);
              s.setColor(color);
          }
      }
  }

  /** 
   * アスペクト円の背景色をセット。
   * @exception IllegalArgumentException 引数にnullが指定されたとき。
   */
  public void setAspectBGColor(Color color) {
    if(color == null) throw new IllegalArgumentException("null禁止");
    circleColor = color;
    if(! isNoAspectBG()) {
      for(int i=0; i<lineStylePanels.length; i++) 
        lineStylePanels[i].setBackground( circleColor );
    }
  }
  
  /** 
   * アスペクト円の背景色を返す。デフォルトは背景色と同じ。
   */
  public Color getAspectBGColor() {
    return circleColor;
  }
  
  /** 
   * 背景色をセットする。これはアスペクト円の背景色が無く透過状態のときの色。
   * デフォルトは白。
   * @exception IllegalArgumentException 引数にnullが指定されたとき。
   */
  public void setBGColor(Color color) {
    if(color == null) throw new IllegalArgumentException("null禁止");
    bgColor = color;
  }
  
  /** 
   * 背景色を返す。これはアスペクト円の背景色が無く透過状態のときの色。
   * デフォルトは白。
   */  
  public Color getBGColor() {
    return bgColor;
  }
  
  /**
   * 背景色なしのときはtrueを返す。
   */
  public boolean isNoAspectBG() {
    return bgCheckBox.isSelected();
  }
  
  /**
   * 背景色無しのときはtrueを、有りのときはfalseをセットする。
   */
  public void setNoAspectBG( boolean b ) {
    bgCheckBox.setSelected(b);
    Color c = b ? getBGColor() : getAspectBGColor();
    for(int i=0; i<lineStylePanels.length; i++) 
      lineStylePanels[i].setBackground( c );
  }
  /**
   * このパネルで設定されたAspectStyleの配列を返す。要素数25の配列で、0番目は
   * ハイライト用のスタイル。
   * 。[1]〜[12]はタイトアスペクトのスタイル、[13]〜[24]はルーズアスペクトの
   * スタイル。
   */
  public AspectStyle [] getAspectStyles() {
    return aspectStyles;
  }
  /**
   * アスペクトスタイルを配列で指定する。nullの配列要素は無視する。
   * 引数で与えられたオブジェクトのAspectIDは無視され、タイト/ルーズ、色、ストロ
   * ーク番号が、このパネルにならんでいる順番で内部の配列にコピーされる。
   * つまりgetAspectStyles()が返す配列の参照は、このメソッドで与えた配列の参照
   * とは異なる。
   * [0]の要素はハイライト用で、[1]から[12]はタイトアスペクトのスタイルで、
   * コンジャクションからはじまり、バイクインタイルで終わる。
   * [13]〜[24]はルーズのスタイルで、アスペクトの順番は同じ。
   *
   * @exception IllegalArgumentException 引数にnullを指定した場合、要素数が25では
   * ないとき
   */
  public void setAspectStyles(AspectStyle [] styles) {
    if(styles == null || styles.length != STYLE_LENGTH) 
      throw new IllegalArgumentException("null禁止");
    for(int i=0; i < aspectStyles.length; i++ ) {
      if(styles[i] == null) continue; //例外を出さないための安全策
      // aid以外をコピー
      aspectStyles[i].setTight( styles[i].isTight() );
      aspectStyles[i].setColor( styles[i].getColor() );
      aspectStyles[i].setStrokeCode( styles[i].getStrokeCode() );
      lineStylePanels[i].setForeground( aspectStyles[i].getColor() );
      lineStylePanels[i].setStroke( aspectStyles[i].getStroke());
    }
  }
  /**
   * このパネルで設定されているアスペクトスタイルの情報を文字列表現で返す。
   * プロパティ保管用。setAspectStyles()で復元できる。
   * 文字列のフォーマットは"タイト/ルーズ,ストローク番号, 色R,G,B,A"の順番でひと
   * 組で、タイトは1ルーズは0で表される。ストローク番号は0-5まであり、AspectStyle
   * のライン定数と同じ。色の各チャンネルは0〜255まで。
   * <br><br>
   * "1,5,255,0,0,240," はタイト、ストローク5番(太線)、R=255,G=0,B=0,A=240である。
   * このくり返しで12個のタイトアスペクト線種の定義が続き、そのあとルーズアスペクト
   * 線種が12個続く。値はカンマで区切られ、スペース等の文字は混入してはならない。
   * ハイライトのスタイルからはじまり、12種類のタイトアスペクトスタイルが続き、
   * つづけて12種類のルーズアスペクトスタイルが続く。
   */
  public String getAspectStylesString() {
    StringBuilder sb = new StringBuilder(700);
    for(AspectStyle a : aspectStyles) {
      sb.append( a.isTight() ? "1," : "0," ); //タイト|ルーズ
      sb.append( a.getStrokeCode() );         //ストローク番号
      sb.append(",");
      Color c = a.getColor();
      sb.append( c.getRed() );                //色コード
      sb.append(",");
      sb.append( c.getGreen() );
      sb.append(",");
      sb.append( c.getBlue() );
      sb.append(",");
      sb.append( c.getAlpha() );      
      sb.append(",");
    }
    sb.deleteCharAt(sb.length()-1);
    return sb.toString();
  }
  /**
   * 文字列で表現されたアスペクトスタイルの情報をこのパネルに反映させる。
   * プロパティ保管用。getAspectStyles()の値からパネル内の設定を復元する。
   * getAspectStylesSting()の逆を行うわけだが、nullや""、カンマで分割したとき、
   * 6*25=150個の要素がない文字列なら設定せず無視する。
   * 文字列中に整数ではない文字や、異常な値が混入している場合も設定せず無視する。
   */
  public void setAspectStyles(String value) {
    if(value == null || value.length() == 0 ) return;
    String [] v = value.split(",");
    if(v.length != ( 6 * STYLE_LENGTH )) return;
    AspectStyle [] as = new AspectStyle[STYLE_LENGTH];
    try {
      for(int i=0,j=0; i<aspectStyles.length; i++) {
        boolean isTight = v[j].equals("1");
        int stroke = Integer.parseInt(v[j+1]);
        int r = Integer.parseInt(v[j+2]);
        int g = Integer.parseInt(v[j+3]);
        int b = Integer.parseInt(v[j+4]);
        int a = Integer.parseInt(v[j+5]);
        int id = i <= 12 ? i : i - 12; //aspectIDsは13個しかないので調整
        as[i] = new AspectStyle(aspectIDs[id],isTight,new Color(r,g,b,a),stroke);
        j += 6;
      }
    } catch(Exception e) {
      return;
    }
    setAspectStyles( as );
  }
  //デフォルトのアスペクト線種をセットする。
  private void setDefaults() {
    setAspectStyles(
      "1,5,255,0,0,240," +
      "1,0,255,0,0,240," +
      "1,0,0,159,0,240," +
      "1,0,60,60,60,240," +
      "1,0,255,138,21,240," +
      "1,0,255,0,0,240," +
      "1,0,127,0,255,240," +
      "1,0,159,0,0,240," +
      "1,0,159,0,0,240," +
      "1,0,0,159,0,240," +
      "1,0,68,162,255,240," +
      "1,0,0,0,255,240," +
      "1,0,68,162,255,240," +
      "0,1,255,144,144,240," +
      "0,1,0,207,0,240,0," +
      "1,100,100,100,240," +
      "0,1,255,175,48,240," +
      "0,1,255,0,0,240," +
      "0,1,175,48,255,240," +
      "0,1,159,31,0,240," +
      "0,1,159,31,0,240," +
      "0,1,31,159,0,240," +
      "0,1,0,207,207,240," +
      "0,1,0,0,255,240," +
      "0,1,0,207,207,240");
  }
  /***************************************************************************
   * ここよりテストメソッド
   ***************************************************************************/
  static void createAndShowGUI() {
    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame.setDefaultLookAndFeelDecorated(true);
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
    }
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle("アスペクトカラー設定");
    frame.setResizable(false);
    final AspectsPanel panel = new AspectsPanel();
    frame.setLayout(new BorderLayout());
    frame.getContentPane().add(panel,BorderLayout.CENTER);
    JButton button = new JButton("System.out.println( getAspectStyles() )");
    button.addActionListener( new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            System.out.println(panel.getAspectStylesString());
        }
    });
    frame.add(button,BorderLayout.SOUTH);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
  public static void main(String [] args) {
      java.awt.EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
              createAndShowGUI();
          }
      });
  }  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        javax.swing.JPanel linesPanel = new javax.swing.JPanel();
        a0Label = new javax.swing.JLabel();
        a120Label = new javax.swing.JLabel();
        a135Label = new javax.swing.JLabel();
        a144Label = new javax.swing.JLabel();
        a150Label = new javax.swing.JLabel();
        a180Label = new javax.swing.JLabel();
        a30Label = new javax.swing.JLabel();
        a36Label = new javax.swing.JLabel();
        a45Label = new javax.swing.JLabel();
        a60Label = new javax.swing.JLabel();
        a72Label = new javax.swing.JLabel();
        a90Label = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        jRadioButton11 = new javax.swing.JRadioButton();
        jRadioButton12 = new javax.swing.JRadioButton();
        lineStylePanel1 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel2 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel3 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel4 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel5 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel6 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel7 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel8 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel9 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel10 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel11 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel12 = new to.tetramorph.starbase.widget.LineStylePanel();
        jRadioButton13 = new javax.swing.JRadioButton();
        jRadioButton14 = new javax.swing.JRadioButton();
        jRadioButton15 = new javax.swing.JRadioButton();
        jRadioButton16 = new javax.swing.JRadioButton();
        jRadioButton17 = new javax.swing.JRadioButton();
        jRadioButton18 = new javax.swing.JRadioButton();
        jRadioButton19 = new javax.swing.JRadioButton();
        jRadioButton20 = new javax.swing.JRadioButton();
        jRadioButton21 = new javax.swing.JRadioButton();
        jRadioButton22 = new javax.swing.JRadioButton();
        jRadioButton23 = new javax.swing.JRadioButton();
        jRadioButton24 = new javax.swing.JRadioButton();
        lineStylePanel13 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel14 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel15 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel16 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel17 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel18 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel19 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel20 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel21 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel22 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel23 = new to.tetramorph.starbase.widget.LineStylePanel();
        lineStylePanel24 = new to.tetramorph.starbase.widget.LineStylePanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        javax.swing.JPanel colorPanel = new javax.swing.JPanel();
        javax.swing.JPanel comboPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        comboBox = new to.tetramorph.starbase.widget.LineStyleComboBox();
        colorPalettePanel = new to.tetramorph.starbase.widget.ColorPalettePanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        bgRadioButton = new javax.swing.JRadioButton();
        bgCheckBox = new javax.swing.JCheckBox();
        hRadioButton = new javax.swing.JRadioButton();
        hLineStylePanel = new to.tetramorph.starbase.widget.LineStylePanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        linesPanel.setLayout(new java.awt.GridBagLayout());

        a0Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a0.gif"))); // NOI18N
        a0Label.setToolTipText("コンジャクション");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a0Label, gridBagConstraints);

        a120Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a120.gif"))); // NOI18N
        a120Label.setToolTipText("トライン");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a120Label, gridBagConstraints);

        a135Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a135.gif"))); // NOI18N
        a135Label.setToolTipText("セスクアドレイト");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a135Label, gridBagConstraints);

        a144Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a144.gif"))); // NOI18N
        a144Label.setToolTipText("バイクインタイル");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a144Label, gridBagConstraints);

        a150Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a150.gif"))); // NOI18N
        a150Label.setToolTipText("クインカンクス");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a150Label, gridBagConstraints);

        a180Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a180.gif"))); // NOI18N
        a180Label.setToolTipText("オポジション");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a180Label, gridBagConstraints);

        a30Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a30.gif"))); // NOI18N
        a30Label.setToolTipText("セミセクスタイル");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a30Label, gridBagConstraints);

        a36Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a36.gif"))); // NOI18N
        a36Label.setToolTipText("デシル");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a36Label, gridBagConstraints);

        a45Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a45.gif"))); // NOI18N
        a45Label.setToolTipText("セミスクエア");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a45Label, gridBagConstraints);

        a60Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a60.gif"))); // NOI18N
        a60Label.setToolTipText("セクステイル");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a60Label, gridBagConstraints);

        a72Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a72.gif"))); // NOI18N
        a72Label.setToolTipText("クインタイル");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a72Label, gridBagConstraints);

        a90Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/symbols/a90.gif"))); // NOI18N
        a90Label.setToolTipText("スクエア");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        linesPanel.add(a90Label, gridBagConstraints);

        jRadioButton1.setSelected(true);
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton1, gridBagConstraints);

        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton2, gridBagConstraints);

        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton3, gridBagConstraints);

        jRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton4, gridBagConstraints);

        jRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton5, gridBagConstraints);

        jRadioButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton6, gridBagConstraints);

        jRadioButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton7, gridBagConstraints);

        jRadioButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton8, gridBagConstraints);

        jRadioButton9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton9, gridBagConstraints);

        jRadioButton10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton10, gridBagConstraints);

        jRadioButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton11, gridBagConstraints);

        jRadioButton12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton12.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 0);
        linesPanel.add(jRadioButton12, gridBagConstraints);

        lineStylePanel1.setBorder(null);
        lineStylePanel1.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        linesPanel.add(lineStylePanel1, gridBagConstraints);

        lineStylePanel2.setBorder(null);
        lineStylePanel2.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        linesPanel.add(lineStylePanel2, gridBagConstraints);

        lineStylePanel3.setBorder(null);
        lineStylePanel3.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        linesPanel.add(lineStylePanel3, gridBagConstraints);

        lineStylePanel4.setBorder(null);
        lineStylePanel4.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        linesPanel.add(lineStylePanel4, gridBagConstraints);

        lineStylePanel5.setBorder(null);
        lineStylePanel5.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        linesPanel.add(lineStylePanel5, gridBagConstraints);

        lineStylePanel6.setBorder(null);
        lineStylePanel6.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        linesPanel.add(lineStylePanel6, gridBagConstraints);

        lineStylePanel7.setBorder(null);
        lineStylePanel7.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        linesPanel.add(lineStylePanel7, gridBagConstraints);

        lineStylePanel8.setBorder(null);
        lineStylePanel8.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        linesPanel.add(lineStylePanel8, gridBagConstraints);

        lineStylePanel9.setBorder(null);
        lineStylePanel9.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        linesPanel.add(lineStylePanel9, gridBagConstraints);

        lineStylePanel10.setBorder(null);
        lineStylePanel10.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        linesPanel.add(lineStylePanel10, gridBagConstraints);

        lineStylePanel11.setBorder(null);
        lineStylePanel11.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        linesPanel.add(lineStylePanel11, gridBagConstraints);

        lineStylePanel12.setBorder(null);
        lineStylePanel12.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        linesPanel.add(lineStylePanel12, gridBagConstraints);

        jRadioButton13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton13.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton13, gridBagConstraints);

        jRadioButton14.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton14.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton14, gridBagConstraints);

        jRadioButton15.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton15.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton15, gridBagConstraints);

        jRadioButton16.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton16.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton16, gridBagConstraints);

        jRadioButton17.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton17.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton17, gridBagConstraints);

        jRadioButton18.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton18.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton18, gridBagConstraints);

        jRadioButton19.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton19.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton19, gridBagConstraints);

        jRadioButton20.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton20.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton20, gridBagConstraints);

        jRadioButton21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton21.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton21, gridBagConstraints);

        jRadioButton22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton22.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton22, gridBagConstraints);

        jRadioButton23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton23.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton23, gridBagConstraints);

        jRadioButton24.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton24.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        linesPanel.add(jRadioButton24, gridBagConstraints);

        lineStylePanel13.setBorder(null);
        lineStylePanel13.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        linesPanel.add(lineStylePanel13, gridBagConstraints);

        lineStylePanel14.setBorder(null);
        lineStylePanel14.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        linesPanel.add(lineStylePanel14, gridBagConstraints);

        lineStylePanel15.setBorder(null);
        lineStylePanel15.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        linesPanel.add(lineStylePanel15, gridBagConstraints);

        lineStylePanel16.setBorder(null);
        lineStylePanel16.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        linesPanel.add(lineStylePanel16, gridBagConstraints);

        lineStylePanel17.setBorder(null);
        lineStylePanel17.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        linesPanel.add(lineStylePanel17, gridBagConstraints);

        lineStylePanel18.setBorder(null);
        lineStylePanel18.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        linesPanel.add(lineStylePanel18, gridBagConstraints);

        lineStylePanel19.setBorder(null);
        lineStylePanel19.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        linesPanel.add(lineStylePanel19, gridBagConstraints);

        lineStylePanel20.setBorder(null);
        lineStylePanel20.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        linesPanel.add(lineStylePanel20, gridBagConstraints);

        lineStylePanel21.setBorder(null);
        lineStylePanel21.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        linesPanel.add(lineStylePanel21, gridBagConstraints);

        lineStylePanel22.setBorder(null);
        lineStylePanel22.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        linesPanel.add(lineStylePanel22, gridBagConstraints);

        lineStylePanel23.setBorder(null);
        lineStylePanel23.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        linesPanel.add(lineStylePanel23, gridBagConstraints);

        lineStylePanel24.setBorder(null);
        lineStylePanel24.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        linesPanel.add(lineStylePanel24, gridBagConstraints);

        jLabel1.setText("タイト");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        linesPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText("ルーズ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        linesPanel.add(jLabel2, gridBagConstraints);

        jLabel6.setText("第2種");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        linesPanel.add(jLabel6, gridBagConstraints);

        jLabel7.setText("第1種");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        linesPanel.add(jLabel7, gridBagConstraints);

        jLabel8.setText("第3種");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        linesPanel.add(jLabel8, gridBagConstraints);

        add(linesPanel, new java.awt.GridBagConstraints());

        colorPanel.setLayout(new java.awt.GridBagLayout());

        comboPanel.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("線の種類");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        comboPanel.add(jLabel5, gridBagConstraints);

        comboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                comboBoxPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        comboPanel.add(comboBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(comboPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        colorPanel.add(colorPalettePanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        bgRadioButton.setText("アスペクト円の背景色");
        bgRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bgRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 4, 0);
        jPanel1.add(bgRadioButton, gridBagConstraints);

        bgCheckBox.setText("背景色なし");
        bgCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bgCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bgCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel1.add(bgCheckBox, gridBagConstraints);

        hRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(hRadioButton, gridBagConstraints);

        hLineStylePanel.setBorder(null);
        hLineStylePanel.setPreferredSize(new java.awt.Dimension(60, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(hLineStylePanel, gridBagConstraints);

        jLabel3.setText("ハイライト");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 17, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        colorPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(colorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
// 「背景無し」のチェックボックスハンドラ
  private void bgCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgCheckBoxActionPerformed
    if(((JCheckBox)evt.getSource()).isSelected())
      setNoAspectBG(true);
    else
      setAspectBGColor( getAspectBGColor() );
  }//GEN-LAST:event_bgCheckBoxActionPerformed
//コンボボックスのハンドラ
  private void comboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_comboBoxPopupMenuWillBecomeInvisible
    int i = comboBox.getSelectedIndex();
    b2p.get( buttonGroup.getSelection() ).setStroke(AspectStyle.strokes[ i ]);
    b2s.get( buttonGroup.getSelection() ).setStrokeCode( i );
  }//GEN-LAST:event_comboBoxPopupMenuWillBecomeInvisible
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel a0Label;
    private javax.swing.JLabel a120Label;
    private javax.swing.JLabel a135Label;
    private javax.swing.JLabel a144Label;
    private javax.swing.JLabel a150Label;
    private javax.swing.JLabel a180Label;
    private javax.swing.JLabel a30Label;
    private javax.swing.JLabel a36Label;
    private javax.swing.JLabel a45Label;
    private javax.swing.JLabel a60Label;
    private javax.swing.JLabel a72Label;
    private javax.swing.JLabel a90Label;
    private javax.swing.JCheckBox bgCheckBox;
    private javax.swing.JRadioButton bgRadioButton;
    private javax.swing.ButtonGroup buttonGroup;
    private to.tetramorph.starbase.widget.ColorPalettePanel colorPalettePanel;
    private to.tetramorph.starbase.widget.LineStyleComboBox comboBox;
    private to.tetramorph.starbase.widget.LineStylePanel hLineStylePanel;
    private javax.swing.JRadioButton hRadioButton;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton11;
    private javax.swing.JRadioButton jRadioButton12;
    private javax.swing.JRadioButton jRadioButton13;
    private javax.swing.JRadioButton jRadioButton14;
    private javax.swing.JRadioButton jRadioButton15;
    private javax.swing.JRadioButton jRadioButton16;
    private javax.swing.JRadioButton jRadioButton17;
    private javax.swing.JRadioButton jRadioButton18;
    private javax.swing.JRadioButton jRadioButton19;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton20;
    private javax.swing.JRadioButton jRadioButton21;
    private javax.swing.JRadioButton jRadioButton22;
    private javax.swing.JRadioButton jRadioButton23;
    private javax.swing.JRadioButton jRadioButton24;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel1;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel10;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel11;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel12;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel13;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel14;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel15;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel16;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel17;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel18;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel19;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel2;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel20;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel21;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel22;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel23;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel24;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel3;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel4;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel5;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel6;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel7;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel8;
    private to.tetramorph.starbase.widget.LineStylePanel lineStylePanel9;
    // End of variables declaration//GEN-END:variables
  
}
