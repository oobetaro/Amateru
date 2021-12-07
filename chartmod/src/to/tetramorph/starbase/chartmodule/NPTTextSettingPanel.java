/*
 * NPTTextSettingPanel.java
 *
 * Created on 2007/10/18, 3:36
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * NPT三重円で天体度数や名前等の文字の配色設定を行うパネル。
 * @author  大澤義鷹
 */
public class NPTTextSettingPanel extends javax.swing.JPanel {
  /**
   * 設定情報はこのパネルから取得する。このクラスではget/setメソッドを持たない。
   */
  public NPTTextDisplayPanel _display;
  

  /** 
   * Creates new form NPTTextSettingPanel 
   */
  public NPTTextSettingPanel() {
    initComponents();
    jList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(! e.getValueIsAdjusting()) return;
        MenuNode n = (MenuNode)jList.getSelectedValue();
        if(n != null && n.l != null) {
          n.l.setButtonListener();
          n.l.menuSelected();
        }
      }
    });
    createMenu();
    _display = dispPanel;
  }
  /**
   * メニューのアクションリスナ
   */
  private abstract class MenuListener {
    MenuListener() {
    }
    void setButtonListener() {
      palettePanel.setChangeListener( new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          showColor( palettePanel.getSelectedColor() );
          colorSelected( palettePanel.getSelectedColor() );
          repaint();
        }
      });
    }
    //このメニューが選択されたときの処理を書く
    abstract void menuSelected();
    //パレットで色が選択されたときの処理を書く
    abstract void colorSelected( Color selcol );
  }

  //設定色ラベルに色を反映させる
  
  private void showColor(Color color) {
    colorConfLabel.setBackground( color );
    rgbLabel.setText(String.format("R=%d,G=%d,B=%d",
      color.getRed(),color.getGreen(),color.getBlue()));
  }
  
  // MenuListModelに格納されるノード
  class MenuNode {
    MenuListener l;
    String name;
    //テスト用
    MenuNode(String name) {
      this.name = name;
    }
    MenuNode( String name, MenuListener l) {
      this.name = name;
      this.l = l;
    }
    public String toString() {
      return name;
    }
  }
  
  // JListに格納されるListModel
  class MenuListModel implements ListModel {
    List<MenuNode> list = new ArrayList<MenuNode>();
    void add(MenuNode node) { list.add(node); }
    public int getSize() { return list.size(); }
    public Object getElementAt(int index) { return list.get(index); }
    public void addListDataListener(ListDataListener l) {}
    public void removeListDataListener(ListDataListener l) {}
  }
  /***************************************************************************
   * ここよりメニュー
   ***************************************************************************/
  void createMenu() {
    MenuListModel listModel = new MenuListModel();
    listModel.add( new MenuNode( "名前", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getNameColor() ); }
      void colorSelected( Color color ) { dispPanel.setNameColor( color ); }
    }));
    listModel.add( new MenuNode( "日付", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getDateColor());}
      void colorSelected( Color selcol) { dispPanel.setDateColor( selcol ); }
    }));
    listModel.add( new MenuNode("地名", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getPlaceColor()); }
      void colorSelected( Color selcol) { dispPanel.setPlaceColor( selcol ); }
    }));
    listModel.add( new MenuNode("天体", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getBodyColor()); }
      void colorSelected( Color selcol) { dispPanel.setBodyColor( selcol ); }
    }));
    listModel.add( new MenuNode("サイン", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getSignColor()); }
      void colorSelected( Color selcol) { dispPanel.setSignColor( selcol ); }
    }));
    listModel.add( new MenuNode("度数", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getAngleColor()); }
      void colorSelected( Color selcol) { dispPanel.setAngleColor( selcol ); }
    }));
    listModel.add( new MenuNode("逆行", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getRevColor()); }
      void colorSelected( Color selcol) { dispPanel.setRevColor( selcol ); }
    }));
    listModel.add( new MenuNode("ハウス番号", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getHouseNumberColor()); }
      void colorSelected( Color selcol) { dispPanel.setHouseNumberColor( selcol ); }
    }));
    listModel.add( new MenuNode("ハイライト", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getHighLightColor()); }
      void colorSelected( Color selcol) { dispPanel.setHighLightColor( selcol ); }
    }));
    listModel.add( new MenuNode("その他", new MenuListener() {
      void menuSelected() { showColor( dispPanel.getOtherColor()); }
      void colorSelected( Color selcol) { dispPanel.setOtherColor( selcol ); }
    }));
    jList.setModel( listModel );
  }
  /***************************************************************************
   * ここよりテスト用のコード
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
    frame.setTitle("サインカラー設定");
    NPTTextSettingPanel panel = new NPTTextSettingPanel();
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
  
  /** テスト */
  public static void main(String [] args) {
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
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jPanel1 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jList = new javax.swing.JList();
    palettePanel = new to.tetramorph.starbase.widget.ColorPalettePanel();
    javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
    javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    colorConfLabel = new javax.swing.JLabel();
    rgbLabel = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    dispPanel = new to.tetramorph.starbase.chartmodule.NPTTextDisplayPanel();

    setLayout(new java.awt.BorderLayout());

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jList.setPreferredSize(new java.awt.Dimension(120, 250));
    jScrollPane1.setViewportView(jList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
    jPanel1.add(jScrollPane1, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
    jPanel1.add(palettePanel, gridBagConstraints);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    jLabel2.setText("設定色");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    jPanel2.add(jLabel2, gridBagConstraints);

    colorConfLabel.setText("       ");
    colorConfLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    colorConfLabel.setMaximumSize(new java.awt.Dimension(80, 21));
    colorConfLabel.setMinimumSize(new java.awt.Dimension(80, 21));
    colorConfLabel.setOpaque(true);
    colorConfLabel.setPreferredSize(new java.awt.Dimension(80, 21));
    jPanel2.add(colorConfLabel, new java.awt.GridBagConstraints());

    rgbLabel.setText("R=,G=,B=");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    jPanel2.add(rgbLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
    jPanel1.add(jPanel2, gridBagConstraints);

    add(jPanel1, java.awt.BorderLayout.CENTER);

    jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    jPanel3.setLayout(new java.awt.GridLayout(1, 0));

    dispPanel.setPreferredSize(new java.awt.Dimension(400, 100));
    jPanel3.add(dispPanel);

    add(jPanel3, java.awt.BorderLayout.NORTH);
  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel colorConfLabel;
  private to.tetramorph.starbase.chartmodule.NPTTextDisplayPanel dispPanel;
  private javax.swing.JList jList;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JScrollPane jScrollPane1;
  private to.tetramorph.starbase.widget.ColorPalettePanel palettePanel;
  private javax.swing.JLabel rgbLabel;
  // End of variables declaration//GEN-END:variables
  
}
