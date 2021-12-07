/*
 * AspectTypePanel.java
 *
 * Created on 2007/04/05, 14:42
 */

package to.tetramorph.starbase.widget;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import to.tetramorph.starbase.formatter.AngleFormatter;
import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.lib.AspectType;

/**
 * フィールドへの角度入力は最大8度まで。
 * @author  大澤義鷹
 */
public class AspectTypePanel extends javax.swing.JPanel {
  //ｱｽﾍﾟｸﾄﾄｸﾞﾙｽｲｯﾁだが、ｱｽﾍﾟｸﾄID順には並んでいない。
  //ｱｽﾍﾟｸﾄIDはﾄｸﾞﾙﾎﾞﾀﾝから取得すること。
  private AspectToggleButton [] buttons;
  // buttons[]の要素番号を2倍したものがﾀｲﾄ用、2倍+1したものがﾙｰｽﾞ用のﾌｨｰﾙﾄﾞとなる。
  private JFormattedTextField [] fields;
  /**
   * Creates new form AspectTypePanel
   */
  public AspectTypePanel() {
    initComponents();
    initButtons();
  }
  // 配列で指定された複数のﾎﾞﾀﾝを選択または非選択の状態に設定する。
  // b = trueなら選択、falseなら非選択となる。array[]には天体番号を指定する。
  // 非選択となったﾎﾞﾀﾝに対応する角度入力ﾌｨｰﾙﾄﾞはDisenabledとなる。
  private void selectButton(int [] array,boolean b) {
    for(int i=0;  i<array.length; i++) {
      buttons[ array[i] ].setSelected(b);
      fields[ array[i] * 2].setEnabled(b);
      fields[ array[i] * 2 + 1].setEnabled(b);
    }
  }
  // 全トグルボタンを選択または非選択に設定する。b=trueなら選択、falseなら非選択。
  private void selectAllButton(boolean b) {
    for(int i=0; i<buttons.length; i++) {
      if(buttons[i] == null) continue;
      buttons[i].setSelected(b);
    }
    for(int i=0; i<fields.length; i++) fields[i].setEnabled(b);
  }
  /**
   * 指定されたアスペクトIDをもつボタンを選択/非選択する。
   * 消灯されているボタンを点灯させることしか想定していない。
   * JButton#doClick()を呼び出すため、アクションリスナも実行され、ボタンに対応
   * している入力フィールドもEnabledにセットされる。
   */
  private void setSelected(int aid,Double tight,Double loose) {
// buttons[番号]の番号は、aidとは一致しないため、このメソッドをつかつて設定する。
    for(int i=0; i<buttons.length; i++) {
      if(buttons[i].getAspectID() == aid) {
        buttons[i].doClick();
        fields[ i * 2 ].setValue(tight);
        fields[ i * 2+1 ].setValue(loose);
      }
    }
  }
  /**
   * 指定されたトグルボタンを点灯させる。
   * @param values "アスペクト番号,タイトオーブ,ルーズオーブ,(...以降繰り返し)"
   * というようにカンマで区切られた文字列データを与える。
   */
  public void setSelected(String values) {
    selectAllButton(false); //全SWを消灯
    String [] array = values.split(",");
    for(int i=0; i<array.length; i +=3 ) {
      int aid = Integer.parseInt(array[i]);
      setSelected(aid,new Double(array[i+1]),
                      new Double(array[i+2]));
    }
  }
  /**
   * このパネルのアスペクト設定情報を文字列で返す。
   * @return "アスペクト番号,タイトオーブ,ルーズオーブ,(...以降繰り返し)"
   * というようにカンマで区切られた文字列データを与える。
   */
  public String getSelected() {
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<buttons.length; i++) {
      if(! buttons[i].isSelected()) continue;
      int aid = buttons[i].getAspectID();
      Double tight = (Double)fields[i*2].getValue();
      Double loose = (Double)fields[i*2+1].getValue();
      if(tight == null) tight = 0d;
      if(loose == null) loose = 0d;
      sb.append(aid);
      sb.append(",");
      sb.append(tight.toString());
      sb.append(",");
      sb.append(loose.toString());
      sb.append(",");
    }
    sb.deleteCharAt(sb.length()-1);
    return sb.toString();
  }
  /**
   * このパネルの設定情報をAspectTypeのリストにして返す。
   */
  public AspectType [] getAspectTypes() {
    List<AspectType> list = new ArrayList<AspectType>();
    for(int i=0; i<buttons.length; i++) {
      if(! buttons[i].isSelected()) continue;
      int aid = buttons[i].getAspectID();
      Double tight = (Double)fields[i*2].getValue();
      Double loose = (Double)fields[i*2+1].getValue();
      if(tight == null) tight = 0d;
      if(loose == null) loose = 0d;
      AspectType a = new AspectType(aid,tight,loose);
      list.add(a);
    }
    AspectType [] array = new AspectType[list.size()];
    for(int i=0; i<list.size(); i++) array[i] = list.get(i);
    return array;
  }
  /**
   * このパネルの各ボタンやフィールドの値を設定する。
   */
  public void setAspectTypes(AspectType [] array) {
    selectAllButton(false); //全SWを消灯
    for(AspectType a : array)
      setSelected(a.aid,a.tightOrb,a.looseOrb);
  }
  /**
   * このパネルのタイトルをセットする。
   * セットするとパネル左上にあるラベルにタイトルが表示される。
   */
  public void setTitle(String title) {
    titleLabel.setText(title);
  }
  /**
   * このパネルのタイトルを返す。
   */
  public String getTitle() {
    return titleLabel.getText();
  }
  // 入力ﾌｨｰﾙﾄﾞとﾄｸﾞﾙﾎﾞﾀﾝを配列にｺﾋﾟｰ
  // 動的にﾎﾞﾀﾝにｱｸｾｽするために配列に入れるのだが、順番はｱｽﾍﾟｸﾄID順とは
  // かぎらない。ｱｽﾍﾟｸﾄIDと一致させたほうがｴﾚｶﾞﾝﾄなのだが、手間がかかるので
  // ﾌﾟﾛｸﾞﾗﾑ側で対処する。
  private void initButtons() {
    int size = 12;
    buttons = new AspectToggleButton[ size ];
    buttons[0] = aspectToggleButton1;
    buttons[1] = aspectToggleButton2;
    buttons[2] = aspectToggleButton3;
    buttons[3] = aspectToggleButton4;
    buttons[4] = aspectToggleButton5;
    buttons[5] = aspectToggleButton6;
    buttons[6] = aspectToggleButton7;
    buttons[7] = aspectToggleButton8;
    buttons[8] = aspectToggleButton9;
    buttons[9] = aspectToggleButton10;
    buttons[10] = aspectToggleButton11;
    buttons[11] = aspectToggleButton12;
    
    for(int i=0; i<buttons.length; i++) {
      buttons[i].setActionCommand(""+i);
      buttons[i].addActionListener(new ActionListener() {
        //ﾎﾞﾀﾝ非選択のときはﾌｨｰﾙﾄﾞもdisenabledにする。
        public void actionPerformed(ActionEvent evt) {
          JToggleButton button = (JToggleButton)evt.getSource();
          int n = Integer.parseInt(button.getActionCommand()) * 2;
          fields[n].setEnabled(button.isSelected());
          fields[n+1].setEnabled(button.isSelected());
        }
      });
    }

    fields = new JFormattedTextField[size * 2];
    fields[0] = jFormattedTextField1;
    fields[1] = jFormattedTextField2;
    fields[2] = jFormattedTextField3;
    fields[3] = jFormattedTextField4;
    fields[4] = jFormattedTextField5;
    fields[5] = jFormattedTextField6;
    fields[6] = jFormattedTextField7;
    fields[7] = jFormattedTextField8;
    fields[8] = jFormattedTextField9;
    fields[9] = jFormattedTextField10;
    fields[10] = jFormattedTextField11;
    fields[11] = jFormattedTextField12;
    fields[12] = jFormattedTextField13;
    fields[13] = jFormattedTextField14;
    fields[14] = jFormattedTextField15;
    fields[15] = jFormattedTextField16;
    fields[16] = jFormattedTextField17;
    fields[17] = jFormattedTextField18;
    fields[18] = jFormattedTextField19;
    fields[19] = jFormattedTextField20;
    fields[20] = jFormattedTextField21;
    fields[21] = jFormattedTextField22;
    fields[22] = jFormattedTextField23;
    fields[23] = jFormattedTextField24;
    //入力ﾌｨｰﾙﾄﾞに入力上限値を与える
    for(int i=0; i<fields.length; i++)
      fields[i].setFormatterFactory(new FormatterFactory(
        new AngleFormatter( 8.0, AngleFormatter.LESS_THAN_OR_EQUAL))); 
  }

  private static void createAndShowGUI() {
    if(UIManager.getLookAndFeel().getName().equals("Metal")) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      JDialog.setDefaultLookAndFeelDecorated(true);
      JFrame.setDefaultLookAndFeelDecorated(true);
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
    }
    JFrame frame = new JFrame();
    AspectTypePanel panel = new AspectTypePanel();
    panel.setSelected( "0,4,8,1,4,8,2,4,8,5,3,6" );
    System.out.println(panel.getSelected());
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
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
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JLabel jLabel6;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel jPanel5;

    jPanel4 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    aspectToggleButton1 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField1 = new javax.swing.JFormattedTextField();
    jFormattedTextField2 = new javax.swing.JFormattedTextField();
    aspectToggleButton2 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField3 = new javax.swing.JFormattedTextField();
    jFormattedTextField4 = new javax.swing.JFormattedTextField();
    aspectToggleButton3 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField5 = new javax.swing.JFormattedTextField();
    jFormattedTextField6 = new javax.swing.JFormattedTextField();
    aspectToggleButton4 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField7 = new javax.swing.JFormattedTextField();
    jFormattedTextField8 = new javax.swing.JFormattedTextField();
    aspectToggleButton5 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField9 = new javax.swing.JFormattedTextField();
    jFormattedTextField10 = new javax.swing.JFormattedTextField();
    jLabel4 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    aspectToggleButton6 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField11 = new javax.swing.JFormattedTextField();
    jFormattedTextField12 = new javax.swing.JFormattedTextField();
    aspectToggleButton7 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField13 = new javax.swing.JFormattedTextField();
    jFormattedTextField14 = new javax.swing.JFormattedTextField();
    aspectToggleButton8 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField15 = new javax.swing.JFormattedTextField();
    jFormattedTextField16 = new javax.swing.JFormattedTextField();
    aspectToggleButton9 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField17 = new javax.swing.JFormattedTextField();
    jFormattedTextField18 = new javax.swing.JFormattedTextField();
    jLabel5 = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    aspectToggleButton10 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField19 = new javax.swing.JFormattedTextField();
    jFormattedTextField20 = new javax.swing.JFormattedTextField();
    aspectToggleButton11 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField21 = new javax.swing.JFormattedTextField();
    jFormattedTextField22 = new javax.swing.JFormattedTextField();
    aspectToggleButton12 = new to.tetramorph.starbase.widget.AspectToggleButton();
    jFormattedTextField23 = new javax.swing.JFormattedTextField();
    jFormattedTextField24 = new javax.swing.JFormattedTextField();
    jLabel6 = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jPanel5 = new javax.swing.JPanel();
    titleLabel = new javax.swing.JLabel();
    selectComboBox = new javax.swing.JComboBox();

    setLayout(new java.awt.BorderLayout(0, 5));

    jPanel4.setLayout(new java.awt.GridBagLayout());

    jPanel1.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(aspectToggleButton1, gridBagConstraints);

    jFormattedTextField1.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(jFormattedTextField1, gridBagConstraints);

    jFormattedTextField2.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel1.add(jFormattedTextField2, gridBagConstraints);

    aspectToggleButton2.setAspectID(1);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(aspectToggleButton2, gridBagConstraints);

    jFormattedTextField3.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(jFormattedTextField3, gridBagConstraints);

    jFormattedTextField4.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel1.add(jFormattedTextField4, gridBagConstraints);

    aspectToggleButton3.setAspectID(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(aspectToggleButton3, gridBagConstraints);

    jFormattedTextField5.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(jFormattedTextField5, gridBagConstraints);

    jFormattedTextField6.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel1.add(jFormattedTextField6, gridBagConstraints);

    aspectToggleButton4.setAspectID(3);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(aspectToggleButton4, gridBagConstraints);

    jFormattedTextField7.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel1.add(jFormattedTextField7, gridBagConstraints);

    jFormattedTextField8.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel1.add(jFormattedTextField8, gridBagConstraints);

    aspectToggleButton5.setAspectID(4);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
    jPanel1.add(aspectToggleButton5, gridBagConstraints);

    jFormattedTextField9.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
    jPanel1.add(jFormattedTextField9, gridBagConstraints);

    jFormattedTextField10.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    jPanel1.add(jFormattedTextField10, gridBagConstraints);

    jLabel4.setText("\uff80\uff72\uff84/\uff99\uff70\uff7d\uff9e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel1.add(jLabel4, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    jPanel4.add(jPanel1, gridBagConstraints);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    aspectToggleButton6.setAspectID(7);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(aspectToggleButton6, gridBagConstraints);

    jFormattedTextField11.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(jFormattedTextField11, gridBagConstraints);

    jFormattedTextField12.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel2.add(jFormattedTextField12, gridBagConstraints);

    aspectToggleButton7.setAspectID(8);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(aspectToggleButton7, gridBagConstraints);

    jFormattedTextField13.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(jFormattedTextField13, gridBagConstraints);

    jFormattedTextField14.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel2.add(jFormattedTextField14, gridBagConstraints);

    aspectToggleButton8.setAspectID(9);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(aspectToggleButton8, gridBagConstraints);

    jFormattedTextField15.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel2.add(jFormattedTextField15, gridBagConstraints);

    jFormattedTextField16.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel2.add(jFormattedTextField16, gridBagConstraints);

    aspectToggleButton9.setAspectID(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
    jPanel2.add(aspectToggleButton9, gridBagConstraints);

    jFormattedTextField17.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
    jPanel2.add(jFormattedTextField17, gridBagConstraints);

    jFormattedTextField18.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    jPanel2.add(jFormattedTextField18, gridBagConstraints);

    jLabel5.setText("\uff80\uff72\uff84/\uff99\uff70\uff7d\uff9e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel2.add(jLabel5, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    jPanel4.add(jPanel2, gridBagConstraints);

    jPanel3.setLayout(new java.awt.GridBagLayout());

    aspectToggleButton10.setAspectID(6);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(aspectToggleButton10, gridBagConstraints);

    jFormattedTextField19.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(jFormattedTextField19, gridBagConstraints);

    jFormattedTextField20.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel3.add(jFormattedTextField20, gridBagConstraints);

    aspectToggleButton11.setAspectID(11);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(aspectToggleButton11, gridBagConstraints);

    jFormattedTextField21.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(jFormattedTextField21, gridBagConstraints);

    jFormattedTextField22.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel3.add(jFormattedTextField22, gridBagConstraints);

    aspectToggleButton12.setAspectID(10);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(aspectToggleButton12, gridBagConstraints);

    jFormattedTextField23.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
    jPanel3.add(jFormattedTextField23, gridBagConstraints);

    jFormattedTextField24.setColumns(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel3.add(jFormattedTextField24, gridBagConstraints);

    jLabel6.setText("\uff80\uff72\uff84/\uff99\uff70\uff7d\uff9e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanel3.add(jLabel6, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    jPanel4.add(jPanel3, gridBagConstraints);

    jLabel1.setText("\u7b2c1\u7a2e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 0);
    jPanel4.add(jLabel1, gridBagConstraints);

    jLabel2.setText("\u7b2c2\u7a2e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 1, 0);
    jPanel4.add(jLabel2, gridBagConstraints);

    jLabel3.setText("\u7b2c3\u7a2e");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 1, 0);
    jPanel4.add(jLabel3, gridBagConstraints);

    add(jPanel4, java.awt.BorderLayout.CENTER);

    jPanel5.setLayout(new java.awt.BorderLayout());

    titleLabel.setText("\u4f7f\u7528\u3059\u308b\u30a2\u30b9\u30da\u30af\u30c8\u3068\u30aa\u30fc\u30d6\u306e\u8a2d\u5b9a");
    jPanel5.add(titleLabel, java.awt.BorderLayout.WEST);

    selectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\u9078\u629e\u88dc\u52a9", "\u6a19\u6e96", "1\u7a2e\u306e\u307f", "2\u7a2e\u3092\u9078\u629e", "3\u7a2e\u3092\u9078\u629e", "\u5168\u9078\u629e", "\u5168\u975e\u9078\u629e" }));
    selectComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectComboBoxActionPerformed(evt);
      }
    });

    jPanel5.add(selectComboBox, java.awt.BorderLayout.EAST);

    add(jPanel5, java.awt.BorderLayout.NORTH);

  }// </editor-fold>//GEN-END:initComponents

  private void selectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectComboBoxActionPerformed
      int i = selectComboBox.getSelectedIndex();
      switch(i) {
        case 1 : // 標準
          selectAllButton(false);
          selectButton(new int [] { 0,1,2,3,4,8 },true );
          break;
        case 2 : // 1種
          selectAllButton(false);
          selectButton(new int [] { 0,1,2,3,4 },true );
          break;
        case 3 : // 2種
          selectButton(new int [] { 5,6,7,8 },true );
          break;
        case 4 : // 3種
          selectButton(new int [] { 9,10,11 },true );
          break;
        case 5 : // 全選択
          selectAllButton(true);
          break;
        case 6 : // 全非選択
          selectAllButton(false);
          break;
      }
      selectComboBox.setSelectedIndex(0);
  }//GEN-LAST:event_selectComboBoxActionPerformed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton1;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton10;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton11;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton12;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton2;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton3;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton4;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton5;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton6;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton7;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton8;
  private to.tetramorph.starbase.widget.AspectToggleButton aspectToggleButton9;
  private javax.swing.JFormattedTextField jFormattedTextField1;
  private javax.swing.JFormattedTextField jFormattedTextField10;
  private javax.swing.JFormattedTextField jFormattedTextField11;
  private javax.swing.JFormattedTextField jFormattedTextField12;
  private javax.swing.JFormattedTextField jFormattedTextField13;
  private javax.swing.JFormattedTextField jFormattedTextField14;
  private javax.swing.JFormattedTextField jFormattedTextField15;
  private javax.swing.JFormattedTextField jFormattedTextField16;
  private javax.swing.JFormattedTextField jFormattedTextField17;
  private javax.swing.JFormattedTextField jFormattedTextField18;
  private javax.swing.JFormattedTextField jFormattedTextField19;
  private javax.swing.JFormattedTextField jFormattedTextField2;
  private javax.swing.JFormattedTextField jFormattedTextField20;
  private javax.swing.JFormattedTextField jFormattedTextField21;
  private javax.swing.JFormattedTextField jFormattedTextField22;
  private javax.swing.JFormattedTextField jFormattedTextField23;
  private javax.swing.JFormattedTextField jFormattedTextField24;
  private javax.swing.JFormattedTextField jFormattedTextField3;
  private javax.swing.JFormattedTextField jFormattedTextField4;
  private javax.swing.JFormattedTextField jFormattedTextField5;
  private javax.swing.JFormattedTextField jFormattedTextField6;
  private javax.swing.JFormattedTextField jFormattedTextField7;
  private javax.swing.JFormattedTextField jFormattedTextField8;
  private javax.swing.JFormattedTextField jFormattedTextField9;
  private javax.swing.JComboBox selectComboBox;
  private javax.swing.JLabel titleLabel;
  // End of variables declaration//GEN-END:variables
  
}
