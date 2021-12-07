/*
 * TestChartModulePanel.java
 *
 * Created on 2006/09/13, 4:27
 */

package to.tetramorph.starbase.chartmodule;

import java.util.List;
import java.util.Vector;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * DBから送られてきたデータをJListで表示するだけのモジュール。
 * @author  大澤義鷹
 */
public class TestPlugin2 extends ChartModulePanel {
  Vector<String> vector = new Vector<String>();
  Vector<String> vector2 = new Vector<String>();
  /** 
   *
   */
  public void init() {
    initComponents();
    jList.setListData(vector);
    jList2.setListData(vector2);
  }
  /**
   * Natal(複数)を受け取ったら、その文字列表現をJListで表示する。
   */
  public void setData(ChannelData data) {
//    System.out.println(this.toString() + "が受信");
    Transit transitEvent = data.getTransit();
    if(transitEvent == null) transitLabel.setText("NULL");
    else transitLabel.setText(transitEvent.toString());
    vector.clear();
    ChartData chartData = data.get(0);
    chartData.setTabIcon();
    if(chartData.getSelectedIndex() < 0 ) {
        //直入力フォームは廃止
//      statusLabel.setText("直入力");
//      vector.addElement(chartData.getTransit().toString());
    }
    else {
      statusLabel.setText("データ");
      for(Data d:chartData.getDataList()) {
        String tap = d.getTimePlace().toString();
        String name = d.getNatal().getName();
        vector.addElement(name + "," + tap);
      }
    }
    jList.setListData(vector);
    jList.setSelectedIndex(chartData.getSelectedIndex());
    List<Data> list = chartData.getDataList();
    if(list.size() > 0 ) iframe.setTitle(list.get(0).getNatal().getName());    
    jList.repaint();
    //-----------------
    vector2.clear();
    ChartData chartData2 = data.get(1);    
    if(chartData2.getSelectedIndex() < 0 ) {
//      statusLabel2.setText("直入力");
//      vector2.addElement(chartData2.getTransit().toString());
    }
    else {
      statusLabel2.setText("データ");
      for(Data d:chartData2.getDataList()) {
        String tap = d.getTimePlace().toString();
        String name = d.getNatal().getName();
        vector2.addElement(name + "," + tap);
      }
    }
    jList2.setListData(vector2);
    jList2.setSelectedIndex(chartData2.getSelectedIndex());
    jList2.repaint();
  }
  public String toString() {
    return "テスト用2ch";
  }
  static final String [] channelNames = { "CH1","CH2" };
  public int getChannelSize() {
    return channelNames.length;
  }
  public String [] getChannelNames() {
    return channelNames;
  }
  public boolean isNeedTransit() {
    return false;
  }
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    javax.swing.JLabel ch1Label;
    javax.swing.JPanel ch1Panel;
    javax.swing.JPanel ch1StatusPanel;
    javax.swing.JLabel ch2Label;
    javax.swing.JPanel ch2Panel;
    javax.swing.JPanel ch2StatusPanel;
    java.awt.GridBagConstraints gridBagConstraints;
    javax.swing.JLabel jLabel1;
    javax.swing.JScrollPane jScrollPane2;
    javax.swing.JScrollPane jScrollPane3;
    javax.swing.JPanel mainPanel;
    javax.swing.JPanel transitPanel;

    transitPanel = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    transitLabel = new javax.swing.JLabel();
    mainPanel = new javax.swing.JPanel();
    ch1Panel = new javax.swing.JPanel();
    ch1StatusPanel = new javax.swing.JPanel();
    ch1Label = new javax.swing.JLabel();
    statusLabel = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    jList = new javax.swing.JList();
    ch2Panel = new javax.swing.JPanel();
    ch2StatusPanel = new javax.swing.JPanel();
    ch2Label = new javax.swing.JLabel();
    statusLabel2 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    jList2 = new javax.swing.JList();

    setLayout(new java.awt.BorderLayout());

    transitPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    transitPanel.setBackground(new java.awt.Color(255, 204, 204));
    jLabel1.setText("Transit : ");
    transitPanel.add(jLabel1);

    transitLabel.setText("\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8\u5185\u5bb9");
    transitPanel.add(transitLabel);

    add(transitPanel, java.awt.BorderLayout.NORTH);

    mainPanel.setLayout(new java.awt.GridLayout(2, 0));

    ch1Panel.setLayout(new java.awt.BorderLayout());

    ch1StatusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    ch1Label.setText("CH1 :");
    ch1StatusPanel.add(ch1Label);

    statusLabel.setText("Status1");
    ch1StatusPanel.add(statusLabel);

    ch1Panel.add(ch1StatusPanel, java.awt.BorderLayout.NORTH);

    jList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane2.setViewportView(jList);

    ch1Panel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

    mainPanel.add(ch1Panel);

    ch2Panel.setLayout(new java.awt.BorderLayout());

    ch2StatusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    ch2Label.setText("CH2 : ");
    ch2StatusPanel.add(ch2Label);

    statusLabel2.setText("Status2");
    ch2StatusPanel.add(statusLabel2);

    ch2Panel.add(ch2StatusPanel, java.awt.BorderLayout.NORTH);

    jList2.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane3.setViewportView(jList2);

    ch2Panel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

    mainPanel.add(ch2Panel);

    add(mainPanel, java.awt.BorderLayout.CENTER);

  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JList jList;
  private javax.swing.JList jList2;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JLabel statusLabel2;
  private javax.swing.JLabel transitLabel;
  // End of variables declaration//GEN-END:variables
  
}
