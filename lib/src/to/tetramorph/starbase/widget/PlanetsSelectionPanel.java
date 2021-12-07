/*
 * PlanetsSelectionPanel.java
 *
 * Created on 2007/04/01, 8:59
 */

package to.tetramorph.starbase.widget;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

/**
 * チャート上で使用する天体を選択するためのパネル。
 * @author  大澤義鷹
 */
public class PlanetsSelectionPanel extends javax.swing.JPanel {
  //Const.の天体コードで各ボタンにアクセスするための配列
  protected PlanetToggleButton [] buttons = new PlanetToggleButton[36];
  protected PlanetsSelectionPanel slavePanel;
  //選択状態の天体IDリストで、ボタンを押したりメソッドで選択が変更されるたびに更新される
  private int [] bodys;
  /** 
   * オブジェクトを作成する。 
   */
  public PlanetsSelectionPanel() {
    // selectComboBoxのプロパティの生成後のコードの項目に、initCombo(),
    // initButtons(),makeBodyArray()を呼び出すコードを書いた。
    // GUIエディタでこのパネルを貼り付けようとすると、どうしてもinitComponents()
    //の中でそれを行わなければならないため。(各メソッドがpublicならこれは不要かも)
    initComponents();
  }

  private void setSelected(int id,boolean b) {
    buttons[id].setSelected(b);
    if(slavePanel != null) {
      slavePanel.buttons[id].setEnabled(b);
    }
  }
  // 配列で指定された複数のﾎﾞﾀﾝを選択または非選択の状態に設定する。
  // b = trueなら選択、falseなら非選択となる。array[]には天体番号を指定する。
  public void setSelected(int [] array,boolean b) {
    for(int i=0;  i<array.length; i++) {
      setSelected(array[i],b);
      //buttons[ array[i] ].setSelected(b);
    }
  }
  // 全トグルボタンを選択または非選択に設定する。b=trueなら選択、falseなら非選択。
  private void selectAllButton(boolean b) {
    for(int i=0; i<buttons.length; i++) {
      if(buttons[i] == null) continue;
      //buttons[i].setSelected(b);
      setSelected(i,b);
    }
  }
  // PlanetToggleButtonを配列に複写
  private void initButtons() {
    //配列の要素番号 == 天体ID
    buttons[0] = planetToggleButton1;
    buttons[1] = planetToggleButton2;
    buttons[2] = planetToggleButton3;
    buttons[3] = planetToggleButton4;
    buttons[4] = planetToggleButton5;
    buttons[5] = planetToggleButton6;
    buttons[6] = planetToggleButton7;
    buttons[7] = planetToggleButton8;
    buttons[8] = planetToggleButton9;
    buttons[9] = planetToggleButton10;
    buttons[17] = planetToggleButton21;
    buttons[18] = planetToggleButton22;
    buttons[19] = planetToggleButton23;
    buttons[20] = planetToggleButton24;
    buttons[15] = planetToggleButton25;
    buttons[16] = planetToggleButton26;
    buttons[30] = planetToggleButton11;
    buttons[32] = planetToggleButton12;
    buttons[10] = planetToggleButton13;
    buttons[12] = planetToggleButton14;
    buttons[34] = planetToggleButton15;
    buttons[31] = planetToggleButton16;
    buttons[33] = planetToggleButton17;
    buttons[21] = planetToggleButton18;
    buttons[23] = planetToggleButton19;
    buttons[35] = planetToggleButton20;
    for(int i=0; i<buttons.length; i++) {
      if(buttons[i] == null) continue;
      buttons[i].setActionCommand(""+i);
      buttons[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          PlanetToggleButton button = (PlanetToggleButton)evt.getSource();
          makeBodyArray();
          if(slavePanel != null) {
            int n = Integer.parseInt(button.getActionCommand());
            slavePanel.buttons[n].setEnabled(button.isSelected());
            slavePanel.buttons[n].setSelected(button.isSelected());
            //ボタンを押すたびにbodys配列を作り替える仕組みになっているが、
            //setSelected()では作り替えﾒｿｯﾄﾞがｷｯｸされないため、明示的にそのﾒｿｯﾄﾞ
            //を実行してやる。
            slavePanel.makeBodyArray(); 
          }
        }
      });
    }
  }
  // 選択補助コンボボックスの準備
  private void initCombo() {
    //コンボボックスにJSeparatorを挿入できるようにする。
    //参考にしたサイトhttp://terai.xrea.jp/Swing/ComboBoxSeparator.html
    final ListCellRenderer render = selectComboBox.getRenderer(); 
    selectComboBox.setRenderer(new ListCellRenderer() {   
      public Component getListCellRendererComponent(
        JList list,Object value,int index, 
        boolean isSelected, boolean cellHasFocus) {     
        if(value instanceof JSeparator) {       
          return (JSeparator)value;     
        } else {
          return (JLabel)render.getListCellRendererComponent(
            list,value,index,isSelected,cellHasFocus);     
        }   
      } 
    });
    DefaultComboBoxModel model = new DefaultComboBoxModel() {   
      public void setSelectedItem(Object o) {     
        if(o instanceof JSeparator) return;     
        super.setSelectedItem(o);
      } 
    };
    model.addElement("選択補助");
    model.addElement("モダン標準");       // 0
    model.addElement("古典標準");         // 1
    model.addElement("四大小惑星を追加"); // 2
    model.addElement("全選択");           // 3
    model.addElement(new JSeparator());  //
    model.addElement("外惑星を非選択");   // 4
    model.addElement("小惑星を非選択");   // 5
    model.addElement("全非選択");         // 6
    selectComboBox.setModel(model);
    selectComboBox.addActionListener(new ComboBoxHandler());
  }
  // コンボボックスの選択が変化したら、それに応じてボタンの設定を行うハンドラ。
  private class ComboBoxHandler implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      int i = selectComboBox.getSelectedIndex();
      switch(i) {
        case 1 : // モダン標準
          selectAllButton(false);
          setSelected(new int [] { 0,1,2,3,4,5,6,7,8,9,30,32,10 },true );
          break;
        case 2 : // 古典標準
          selectAllButton(false);
          setSelected(new int [] { 0,1,2,3,4,5,6,30,32,10 },true );
          break;
        case 3 : // 四大小惑星を追加
          setSelected(new int [] { 17,18,19,20 },true );
          break;
        case 4 : // 全選択
          selectAllButton(true);
          break;
        case 6 : // 外惑星を非選択
          setSelected(new int [] { 7,8,9},false);
          break;
        case 7 : // 小惑星を非選択
          setSelected(new int [] { 17,18,19,20,15,16},false);
          break;
        case 8 : // 全非選択
          selectAllButton(false);
          break;
      }
      makeBodyArray();
      selectComboBox.setSelectedIndex(0);
    }
  }
  /**
   * 点灯しているスイッチの天体番号を文字列表現で返す。
   * Disenabled状態にあるボタンは消灯とみなす。
   * @return "1,2,3"などとカンマで区切られた数字の列挙を返す。
   */
  public String getSelected() {
    if(bodys.length == 0) return "";
    StringBuffer sb = new StringBuffer();
    for(int id : bodys) {
      sb.append(id);
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
  /**
   * 指定されたトグルボタンを点灯させる。
   * @param values "1,2,3"などとカンマで区切られた天体番号の列挙を与える
   */
  public void setSelected(String values) {
    selectAllButton(false); //全SWを消灯
    String [] array = values.split(",");
    for(int i=0; i<array.length; i++) {
      int id = Integer.parseInt(array[i]);
      setSelected(id,true);
    }
    makeBodyArray();
  }
  /**
   * 選択されているすべての天体のIDを配列で返す。
   * 一つも選択されていないときは要素数0の配列が返りnullは戻らない。
   * 天体IDの配列はボタンの選択状態が変化するタイミングで更新される仕組みなので、
   * このメソッドを頻繁に参照しても速度低下は起きない仕様になっている。
   */
  public int [] getSelectedBodyIDs() {
    return bodys;
  }
  // ボタンの選択状況からbodys[]にIDリストを作成する。
  protected void makeBodyArray() {
    int n=0;
    for(int i=0; i<buttons.length; i++) { //選択ボタン数をnに求める
      if(buttons[i] == null || ! buttons[i].isEnabled()) continue;
      if(buttons[i].isSelected()) n++;
    }
    bodys = new int[n]; //配列を用意し
    n=0;
    for(int i=0; i<buttons.length; i++) { //配列にIDを代入
      if(buttons[i] == null || ! buttons[i].isEnabled()) continue;
      if(buttons[i].isSelected()) {
        bodys[n] = buttons[i].getBodyID();
        n++;
      }
    }
  }
//  /**
//   * 選択されているボタンの天体コードのリストを返す。
//   */
//  public List<Integer> getSelectedList() {
//    List<Integer> list = new ArrayList<Integer>();
//    for(int i=0; i<buttons.length; i++) {
//      if(buttons[i] == null) continue;
//      if(! buttons[i].isEnabled()) continue;
//      if(buttons[i].isSelected()) {
//        list.add(buttons[i].getBodyID());
//      }
//    }
//    return list;
//  }
//  /**
//   * 指定されたトグルボタンを点灯させる。
//   * @param list 点灯させる天体番号のリストを与える。
//   */
//  public void setSelectedList(List<Integer> list) {
//    selectAllButton(false);
//    for(Integer id : list)
//      setSelected(id,true);
//  }
//  public int [] getBodyIDs() {
//    List<Integer> list = getSelectedList();
//    int [] result = new int[ list.size() ]; //ArrayList<Integer>();
//    for(int i=0; i<list.size(); i++) result[i] = list.get(i);
//    return result;
//  }
  /**
   * アスペクト検出天体を指定するPlanetsSelectionPanelと同期させたい場合、
   * そのパネルを登録しておくと、このパネルで非選択となったボタンと同じボタン
   * がdisenabledになる。
   */
  public void setSlavePanel(PlanetsSelectionPanel slavePanel) {
    this.slavePanel = slavePanel;
  }
  /**
   * このパネルのタイトルラベルに文字列をセット。
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
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel selectPanel;
        javax.swing.JPanel toggleButtonPanel;

        toggleButtonPanel = new javax.swing.JPanel();
        planetToggleButton1 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton2 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton3 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton4 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton5 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton6 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton7 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton8 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton9 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton10 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton11 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton12 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton13 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton14 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton15 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton16 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton17 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton18 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton19 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton20 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton21 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton22 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton23 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton24 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton25 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        planetToggleButton26 = new to.tetramorph.starbase.widget.PlanetToggleButton();
        selectPanel = new javax.swing.JPanel();
        selectComboBox = new javax.swing.JComboBox();
        titleLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        toggleButtonPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 1);
        toggleButtonPanel.add(planetToggleButton1, gridBagConstraints);

        planetToggleButton2.setBodyID(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton2, gridBagConstraints);

        planetToggleButton3.setBodyID(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton3, gridBagConstraints);

        planetToggleButton4.setBodyID(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton4, gridBagConstraints);

        planetToggleButton5.setBodyID(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton5, gridBagConstraints);

        planetToggleButton6.setBodyID(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton6, gridBagConstraints);

        planetToggleButton7.setBodyID(6);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton7, gridBagConstraints);

        planetToggleButton8.setAutoscrolls(true);
        planetToggleButton8.setBodyID(7);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton8, gridBagConstraints);

        planetToggleButton9.setBodyID(8);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton9, gridBagConstraints);

        planetToggleButton10.setBodyID(9);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton10, gridBagConstraints);

        planetToggleButton11.setBodyID(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton11, gridBagConstraints);

        planetToggleButton12.setBodyID(32);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton12, gridBagConstraints);

        planetToggleButton13.setBodyID(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton13, gridBagConstraints);

        planetToggleButton14.setSelected(false);
        planetToggleButton14.setBodyID(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 1);
        toggleButtonPanel.add(planetToggleButton14, gridBagConstraints);

        planetToggleButton15.setSelected(false);
        planetToggleButton15.setBodyID(34);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 2, 0);
        toggleButtonPanel.add(planetToggleButton15, gridBagConstraints);

        planetToggleButton16.setSelected(false);
        planetToggleButton16.setBodyID(31);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton16, gridBagConstraints);

        planetToggleButton17.setSelected(false);
        planetToggleButton17.setBodyID(33);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton17, gridBagConstraints);

        planetToggleButton18.setSelected(false);
        planetToggleButton18.setBodyID(21);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton18, gridBagConstraints);

        planetToggleButton19.setSelected(false);
        planetToggleButton19.setBodyID(23);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton19, gridBagConstraints);

        planetToggleButton20.setSelected(false);
        planetToggleButton20.setBodyID(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 0);
        toggleButtonPanel.add(planetToggleButton20, gridBagConstraints);

        planetToggleButton21.setSelected(false);
        planetToggleButton21.setBodyID(17);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        toggleButtonPanel.add(planetToggleButton21, gridBagConstraints);

        planetToggleButton22.setSelected(false);
        planetToggleButton22.setBodyID(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton22, gridBagConstraints);

        planetToggleButton23.setSelected(false);
        planetToggleButton23.setBodyID(19);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton23, gridBagConstraints);

        planetToggleButton24.setSelected(false);
        planetToggleButton24.setBodyID(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton24, gridBagConstraints);

        planetToggleButton25.setSelected(false);
        planetToggleButton25.setBodyID(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton25, gridBagConstraints);

        planetToggleButton26.setSelected(false);
        planetToggleButton26.setBodyID(16);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        toggleButtonPanel.add(planetToggleButton26, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(toggleButtonPanel, gridBagConstraints);

        selectPanel.setLayout(new java.awt.GridBagLayout());

        selectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        initButtons();
        initCombo();
        makeBodyArray();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        selectPanel.add(selectComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(selectPanel, gridBagConstraints);

        titleLabel.setText("\u30cd\u30a4\u30bf\u30eb");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(titleLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton1;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton10;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton11;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton12;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton13;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton14;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton15;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton16;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton17;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton18;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton19;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton2;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton20;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton21;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton22;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton23;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton24;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton25;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton26;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton3;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton4;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton5;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton6;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton7;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton8;
    private to.tetramorph.starbase.widget.PlanetToggleButton planetToggleButton9;
    private javax.swing.JComboBox selectComboBox;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
  
}
