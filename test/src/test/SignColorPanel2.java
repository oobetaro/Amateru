/*
 * SignColorPanel2.java
 *
 * Created on 2007/02/20, 15:35
 */

package to.tetramorph.starbase.widget;

import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import to.tetramorph.starbase.*;
import to.tetramorph.util.Preference;

/**
 * ホロスコープの各サインの配色を設定するパネル。
 */
public class SignColorPanel2 extends javax.swing.JPanel {
  ZodiacDisplayPanel zodiacDisplayPanel = new ZodiacDisplayPanel();
  
  /** Creates new form SignColorPanel2 */
  public SignColorPanel2() {
    initComponents();
    zodiacDisplayPanel.setRadioButtonEnabled(false);
    jPanel1.add(zodiacDisplayPanel);
    cmdButtonGroup.add(cmdRadioButton1);
    cmdButtonGroup.add(cmdRadioButton2);
    cmdButtonGroup.add(cmdRadioButton3);
    cmdButtonGroup.add(cmdRadioButton4);
    cmdButtonGroup.add(cmdRadioButton5);
  }
  public void setBG(Color color) {
    zodiacDisplayPanel.setBackground(color);
  }
  /**
   * このパネルの色情報をprefに書き込みそれを返す。参照書き換えなので、
   * prefがnullであってはならない。
   */
  public Preference getPreference( Preference pref ) {
    pref.setColors("signBackgrounds",zodiacDisplayPanel.getBackgrounds());
    pref.setColors("signSymbols",zodiacDisplayPanel.getForegrounds());
    pref.setColors("signSymbolBorders",zodiacDisplayPanel.getSymbolBorders());
    pref.setColor("signsBorder", zodiacDisplayPanel.getFrame());
    pref.setBoolean("isNoSignBackgrounds", noBgCheckBox.isSelected());
    pref.setBoolean("isNoSignBorders", noBorderCheckBox.isSelected());
    pref.setBoolean("isNoSignSymbolBorders", noSymbolBorderCheckBox.isSelected());
    return pref;
  }
  /** 各サインの背景色を返す*/
  public Color [] getSignBackgrounds() {
    return zodiacDisplayPanel.getBackgrounds();
  }
  /** 各サインのフォントカラーを返す。 */
  public Color [] getSignSymbols() {
    return zodiacDisplayPanel.getForegrounds();
  }
  /** 各サインのフォント縁取り色を返す。 */
  public Color [] getSignSymbolsBorders() {
    return zodiacDisplayPanel.getSymbolBorders();
  }
  /** サインを取り囲むセクター枠線の色を返す。*/
  public Color getSignRingLine() {
    return zodiacDisplayPanel.getFrame();
  }
  /** サインの背景色不要のときはtrue */
  public boolean isNoSignBackgrouns() {
    return noBgCheckBox.isSelected();
  }
  /** サインシンボルの縁取り線が不要のときはtrue */
  public boolean isNoSignSymbolBorders() {
    return noSymbolBorderCheckBox.isSelected();
  }
  /** サインリングの線が不要のときはtrue */
  public boolean isNoSignRingBorders() {
    return noBorderCheckBox.isSelected();
  }
  /**
   * prefの値にしたがって配色情報をパネルにセットする。
   */
  public void setPreference(Preference pref) {
    Color [] signBackgrounds = pref.getColors("signBackgrounds");
    Color [] signSymbols = pref.getColors("signSymbols");
    Color [] signSymbolBorders = pref.getColors("signSymbolBorders");
    for(int i=0; i<12; i++) {
      zodiacDisplayPanel.symdsp[i].setBackground(signBackgrounds[i]);
      zodiacDisplayPanel.symdsp[i].setForeground(signSymbols[i]);
      zodiacDisplayPanel.symdsp[i].setSymbolBorder(signSymbolBorders[i]);
    }
    zodiacDisplayPanel.setFrameVisible(! pref.getBoolean("isNoSignBorders"));
    zodiacDisplayPanel.setFrame(pref.getColor("signsBorder"));
    this.noSymbolBorderCheckBox.setSelected(pref.getBoolean("isNoSignSymbolBorders"));
    zodiacDisplayPanel.setBorderVisible(! pref.getBoolean("isNoSignSymbolBorders"));
    this.noBorderCheckBox.setSelected(pref.getBoolean("isNoSignBorders"));
    this.noBgCheckBox.setSelected(pref.getBoolean("isNoSignBackgrounds"));
  }
  static void createAndShowGUI() {
    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame.setDefaultLookAndFeelDecorated(true);
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
    }
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle("サインカラー設定");
    frame.setResizable(false);
    SignColorPanel2 panel = new SignColorPanel2();
    frame.getContentPane().add(panel);
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
  private static final String title = "黄道十二星座の色設定";
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JPanel gradationPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel sliderPanel1;

    gradationPanel = new javax.swing.JPanel();
    gradButton1 = new javax.swing.JButton();
    gradButton2 = new javax.swing.JButton();
    gradButton3 = new javax.swing.JButton();
    gradButton4 = new javax.swing.JButton();
    cmdButtonGroup = new javax.swing.ButtonGroup();
    jPanel2 = new javax.swing.JPanel();
    repeatComboBox = new javax.swing.JComboBox();
    repeatButton = new javax.swing.JButton();
    sliderPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jSlider1 = new javax.swing.JSlider();
    jLabel2 = new javax.swing.JLabel();
    sliderPanel2 = new javax.swing.JPanel();
    jSlider2 = new javax.swing.JSlider();
    jLabel5 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    tabbedPane = new javax.swing.JTabbedPane();
    globalConfPanel = new javax.swing.JPanel();
    fgButton1 = new javax.swing.JButton();
    bgButton1 = new javax.swing.JButton();
    borderButton1 = new javax.swing.JButton();
    lineButton1 = new javax.swing.JButton();
    noSymbolBorderCheckBox = new javax.swing.JCheckBox();
    noBorderCheckBox = new javax.swing.JCheckBox();
    noBgCheckBox = new javax.swing.JCheckBox();
    singleConfPanel = new javax.swing.JPanel();
    fgButton2 = new javax.swing.JButton();
    bgButton2 = new javax.swing.JButton();
    bprderButton2 = new javax.swing.JButton();
    adjustPanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    cmdRadioButton1 = new javax.swing.JRadioButton();
    cmdRadioButton2 = new javax.swing.JRadioButton();
    cmdRadioButton3 = new javax.swing.JRadioButton();
    cmdRadioButton4 = new javax.swing.JRadioButton();
    cmdRadioButton5 = new javax.swing.JRadioButton();
    jButton2 = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();

    gradationPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

    gradationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("\u7261\u7f8a\u5ea7\u304b\u3089\u6307\u5b9a\uff7b\uff72\uff9d\u307e\u3067\uff78\uff9e\uff97\uff83\uff9e\uff70\uff7c\uff6e\uff9d"));
    gradationPanel.setPreferredSize(new java.awt.Dimension(230, 48));
    gradationPanel.getAccessibleContext().setAccessibleName("\u7261\u7f8a\u5ea7\u3068n\u500b\u307e\u3067\u306e\u8272\u304b\u3089\uff78\uff9e\uff97\uff83\uff9e\uff70\uff7c\uff6e\uff9d\n");
    gradButton1.setText("\u53cc\u5b50");
    gradButton1.setMargin(new java.awt.Insets(2, 8, 2, 8));
    gradationPanel.add(gradButton1);

    gradButton2.setText("\u87f9");
    gradButton2.setMargin(new java.awt.Insets(2, 8, 2, 8));
    gradationPanel.add(gradButton2);

    gradButton3.setText("\u5929\u79e4");
    gradButton3.setMargin(new java.awt.Insets(2, 8, 2, 8));
    gradationPanel.add(gradButton3);

    gradButton4.setText("\u9b5a");
    gradButton4.setMargin(new java.awt.Insets(2, 8, 2, 8));
    gradationPanel.add(gradButton4);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("\u914d\u8272\u306e\u30ea\u30d4\u30fc\u30c8"));
    repeatComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044", "\u7261\u7f8a\u5ea7\u304b\u3089\uff12\u661f\u5ea7\u5206\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059", "\u7261\u7f8a\u5ea7\u304b\u3089\uff13\u661f\u5ea7\u5206\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059", "\u7261\u7f8a\u5ea7\u304b\u3089\uff14\u661f\u5ea7\u5206\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059", "\u7261\u7f8a\u5ea7\u304b\u3089\uff16\u661f\u5ea7\u5206\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059" }));
    jPanel2.add(repeatComboBox, new java.awt.GridBagConstraints());

    repeatButton.setText("\u5b9f\u884c");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    jPanel2.add(repeatButton, gridBagConstraints);

    sliderPanel1.setLayout(new java.awt.GridBagLayout());

    sliderPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("\u80cc\u666f\u8272\u306e\u660e\u308b\u3055"));
    jLabel1.setText("\u6697\u304f");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    sliderPanel1.add(jLabel1, gridBagConstraints);

    jSlider1.setPreferredSize(new java.awt.Dimension(100, 24));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    sliderPanel1.add(jSlider1, gridBagConstraints);

    jLabel2.setText("\u660e\u308b\u304f");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    sliderPanel1.add(jLabel2, gridBagConstraints);

    sliderPanel2.setLayout(new java.awt.GridBagLayout());

    sliderPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("\u6587\u5b57\u8272\u306e\u660e\u308b\u3055"));
    jSlider2.setPreferredSize(new java.awt.Dimension(100, 24));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    sliderPanel2.add(jSlider2, gridBagConstraints);

    jLabel5.setText("\u660e\u308b\u304f");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    sliderPanel2.add(jLabel5, gridBagConstraints);

    jLabel4.setText("\u6697\u304f");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    sliderPanel2.add(jLabel4, gridBagConstraints);

    setLayout(new java.awt.BorderLayout(0, 8));

    setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
    tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        tabbedPaneStateChanged(evt);
      }
    });

    globalConfPanel.setLayout(new java.awt.GridBagLayout());

    fgButton1.setText("\u6587\u5b57\u8272");
    fgButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fgButton1ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
    globalConfPanel.add(fgButton1, gridBagConstraints);

    bgButton1.setText("\u80cc\u666f\u8272");
    bgButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        bgButton1ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
    globalConfPanel.add(bgButton1, gridBagConstraints);

    borderButton1.setText("\u7e01\u53d6\u308a\u8272");
    borderButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        borderButton1ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
    globalConfPanel.add(borderButton1, gridBagConstraints);

    lineButton1.setText("\u7dda\u8272");
    lineButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        lineButton1ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    globalConfPanel.add(lineButton1, gridBagConstraints);

    noSymbolBorderCheckBox.setText("\u7e01\u53d6\u308a\u7121\u3057");
    noSymbolBorderCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    noSymbolBorderCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    noSymbolBorderCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        noSymbolBorderCheckBoxActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
    globalConfPanel.add(noSymbolBorderCheckBox, gridBagConstraints);

    noBorderCheckBox.setText("\u7dda\u7121\u3057");
    noBorderCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    noBorderCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    noBorderCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        noBorderCheckBoxActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    globalConfPanel.add(noBorderCheckBox, gridBagConstraints);

    noBgCheckBox.setText("\u900f\u660e");
    noBgCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    noBgCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    noBgCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        noBgCheckBoxActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    globalConfPanel.add(noBgCheckBox, gridBagConstraints);

    tabbedPane.addTab("\u4e00\u62ec\u6307\u5b9a", globalConfPanel);

    singleConfPanel.setLayout(new java.awt.GridBagLayout());

    fgButton2.setText("\u6587\u5b57\u8272");
    fgButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        fgButton2ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
    singleConfPanel.add(fgButton2, gridBagConstraints);

    bgButton2.setText("\u80cc\u666f\u8272");
    bgButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        bgButton2ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
    singleConfPanel.add(bgButton2, gridBagConstraints);

    bprderButton2.setText("\u7e01\u53d6\u308a\u8272");
    bprderButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        bprderButton2ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    singleConfPanel.add(bprderButton2, gridBagConstraints);

    tabbedPane.addTab("\u9078\u629e\u6307\u5b9a", singleConfPanel);

    adjustPanel.setLayout(new java.awt.GridBagLayout());

    jPanel3.setLayout(new java.awt.GridBagLayout());

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("\u914d\u8272\u304a\u52a9\u3051\u6a5f\u80fd"));
    cmdRadioButton1.setSelected(true);
    cmdRadioButton1.setText("\u7261\u7f8a\u5ea7\u304b\u3089\uff12\u661f\u5ea7\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059");
    cmdRadioButton1.setActionCommand("0");
    cmdRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cmdRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
    jPanel3.add(cmdRadioButton1, gridBagConstraints);

    cmdRadioButton2.setText("\u7261\u7f8a\u5ea7\u304b\u3089\uff13\u661f\u5ea7\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059");
    cmdRadioButton2.setActionCommand("1");
    cmdRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cmdRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
    jPanel3.add(cmdRadioButton2, gridBagConstraints);

    cmdRadioButton3.setText("\u7261\u7f8a\u5ea7\u304b\u3089\uff14\u661f\u5ea7\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059");
    cmdRadioButton3.setActionCommand("2");
    cmdRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cmdRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
    jPanel3.add(cmdRadioButton3, gridBagConstraints);

    cmdRadioButton4.setText("\u7261\u7f8a\u5ea7\u304b\u3089\uff16\u661f\u5ea7\u306e\u8272\u3092\u7e70\u308a\u8fd4\u3059");
    cmdRadioButton4.setActionCommand("3");
    cmdRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cmdRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
    jPanel3.add(cmdRadioButton4, gridBagConstraints);

    cmdRadioButton5.setText("\u7261\u7f8a\u3001\u87f9\u3001\u5929\u79e4\u3001\u5c71\u7f8a\u306e\u8272\u304b\u3089\u30b0\u30e9\u30c7\u30fc\u30b7\u30e7\u30f3\u751f\u6210");
    cmdRadioButton5.setActionCommand("4");
    cmdRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cmdRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 5, 8);
    jPanel3.add(cmdRadioButton5, gridBagConstraints);

    jButton2.setText("\u5b9f\u884c");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
    jPanel3.add(jButton2, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    adjustPanel.add(jPanel3, gridBagConstraints);

    tabbedPane.addTab("\u8272\u8abf\u6574", adjustPanel);

    add(tabbedPane, java.awt.BorderLayout.CENTER);

    jPanel1.setLayout(new java.awt.GridLayout(1, 0));

    jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    add(jPanel1, java.awt.BorderLayout.NORTH);

  }// </editor-fold>//GEN-END:initComponents
  // 選択指定 , 透明色のチェックボックス  // 配色お助け機能
  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    int n = Integer.parseInt( cmdButtonGroup.getSelection().getActionCommand() );
    if( n == 0) zodiacDisplayPanel.repeat(2);
    else if(n == 1) zodiacDisplayPanel.repeat(3);
    else if(n == 2) zodiacDisplayPanel.repeat(4);
    else if(n == 3) zodiacDisplayPanel.repeat(6);
    else if(n == 4) zodiacDisplayPanel.colorRing();
  }//GEN-LAST:event_jButton2ActionPerformed
  // 選択指定 , 縁取り色
  private void bprderButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bprderButton2ActionPerformed
    Color color = JColorChooser.showDialog(this,title,Color.BLACK);
    if(color != null) zodiacDisplayPanel.setSelectedBorder(color);
  }//GEN-LAST:event_bprderButton2ActionPerformed
  // 6色リピート  // 4色リピート  // 2色リピート  // 3色リピート  // タブ切替イベント
  private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
    //選択指定のタブになったときだけラジオボタンは使える
    zodiacDisplayPanel.setRadioButtonEnabled(tabbedPane.getSelectedIndex() == 1);
  }//GEN-LAST:event_tabbedPaneStateChanged
  // 選択指定 , 文字色
  private void fgButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgButton2ActionPerformed
    Color color = JColorChooser.showDialog(this,title,zodiacDisplayPanel.getSelectedFG());
    if(color != null)
      zodiacDisplayPanel.setSelectedFG(color);
  }//GEN-LAST:event_fgButton2ActionPerformed
  // 選択指定 , 背景色
  private void bgButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgButton2ActionPerformed
    Color color = JColorChooser.showDialog(this,title,zodiacDisplayPanel.getSelectedBG());
    if(color != null)
      zodiacDisplayPanel.setSelectedBG(color);
  }//GEN-LAST:event_bgButton2ActionPerformed
  //一括指定 , 四角の枠線表示する/しない
  private void noBorderCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noBorderCheckBoxActionPerformed
    JCheckBox cb = (JCheckBox)evt.getSource();
    lineButton1.setEnabled(! cb.isSelected());
    zodiacDisplayPanel.setFrameVisible(! cb.isSelected());
  }//GEN-LAST:event_noBorderCheckBoxActionPerformed
  //一括指定 , 四角の枠線色
  private void lineButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineButton1ActionPerformed
    Color color = JColorChooser.showDialog(this,title,Color.BLACK);
    if(color != null)
      zodiacDisplayPanel.setFrame(color);
  }//GEN-LAST:event_lineButton1ActionPerformed
  //一括指定 ,「縁取り無し」のチェックボックス
  private void noSymbolBorderCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noSymbolBorderCheckBoxActionPerformed
    JCheckBox cb = (JCheckBox)evt.getSource();
    borderButton1.setEnabled(! cb.isSelected());
    zodiacDisplayPanel.setBorderVisible(! cb.isSelected());
    if(cb.isSelected()) 
      zodiacDisplayPanel.setBorderVisible( false );
  }//GEN-LAST:event_noSymbolBorderCheckBoxActionPerformed
  //一括指定 ,「背景色無し」のチェックボックス
  private void noBgCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noBgCheckBoxActionPerformed
    JCheckBox cb = (JCheckBox)evt.getSource();
    bgButton1.setEnabled(! cb.isSelected());
    if(cb.isSelected())
      zodiacDisplayPanel.setBG( null );
  }//GEN-LAST:event_noBgCheckBoxActionPerformed
  //一括指定 ,「縁取り色」のボタン
  private void borderButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderButton1ActionPerformed
    Color color = JColorChooser.showDialog(this,title,Color.BLACK);
    if(color != null)
      zodiacDisplayPanel.setBorder(color);
  }//GEN-LAST:event_borderButton1ActionPerformed
  //一括指定 ,「背景色」のボタン
  private void bgButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgButton1ActionPerformed
    Color color = JColorChooser.showDialog(this,title,Color.WHITE);
    if(color != null)
      //ラベルのOpaqueはtrueにセットしてないと色がつかないので注意(他のラベルも同じ)
      zodiacDisplayPanel.setBG(color);
  }//GEN-LAST:event_bgButton1ActionPerformed
  //一括指定 , 文字色を設定
  private void fgButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgButton1ActionPerformed
    Color color = JColorChooser.showDialog(this,title,Color.WHITE);
    if(color != null) {
      //fgLabel1.setBackground(color);
      zodiacDisplayPanel.setFG(color);    
    }
  }//GEN-LAST:event_fgButton1ActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel adjustPanel;
  private javax.swing.JButton bgButton1;
  private javax.swing.JButton bgButton2;
  private javax.swing.JButton borderButton1;
  private javax.swing.JButton bprderButton2;
  private javax.swing.ButtonGroup cmdButtonGroup;
  private javax.swing.JRadioButton cmdRadioButton1;
  private javax.swing.JRadioButton cmdRadioButton2;
  private javax.swing.JRadioButton cmdRadioButton3;
  private javax.swing.JRadioButton cmdRadioButton4;
  private javax.swing.JRadioButton cmdRadioButton5;
  private javax.swing.JButton fgButton1;
  private javax.swing.JButton fgButton2;
  private javax.swing.JPanel globalConfPanel;
  private javax.swing.JButton gradButton1;
  private javax.swing.JButton gradButton2;
  private javax.swing.JButton gradButton3;
  private javax.swing.JButton gradButton4;
  private javax.swing.JButton jButton2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JSlider jSlider1;
  private javax.swing.JSlider jSlider2;
  private javax.swing.JButton lineButton1;
  private javax.swing.JCheckBox noBgCheckBox;
  private javax.swing.JCheckBox noBorderCheckBox;
  private javax.swing.JCheckBox noSymbolBorderCheckBox;
  private javax.swing.JButton repeatButton;
  private javax.swing.JComboBox repeatComboBox;
  private javax.swing.JPanel singleConfPanel;
  private javax.swing.JPanel sliderPanel2;
  private javax.swing.JTabbedPane tabbedPane;
  // End of variables declaration//GEN-END:variables
  
}
