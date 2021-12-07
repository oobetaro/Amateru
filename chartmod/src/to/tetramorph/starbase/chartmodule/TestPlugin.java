/*
 * TestChartModulePanel.java
 *
 * Created on 2006/09/13, 4:27
 */
package to.tetramorph.starbase.chartmodule;

import java.util.List;
import java.util.Vector;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * DBから送られてきたデータをJListで表示するだけのモジュール。
 * @author  大澤義鷹
 */
public class TestPlugin extends ChartModulePanel {
  Vector<String> vector = new Vector<String>();
  /**
   * チャートモジュールを作成する。
   */
  public TestPlugin() {
    super();
    initComponents();
    jList.setListData(vector);
  }
  public boolean isNeedTransit() {
    return true;
  }
  public void init() {

  }
  /**
   * Natal(複数)を受け取ったら、その文字列表現をJListで表示する。
   */
  //public void setData(List<Data> list,int num,Event transit,boolean transitUpdate) {
  public void setData(ChannelData data) {
    assert SwingUtilities.isEventDispatchThread() : "Not EDT";
    System.out.println(this.toString() + "が受信");
    Transit transitEvent = data.getTransit();
    if(transitEvent == null) transitTextField.setText("NULL");
    else transitTextField.setText(transitEvent.toString());
    ChartData chartData = data.get(0);
    vector.clear();
    chartData.setTabIcon();
    if(chartData.getSelectedIndex() < 0 ) {
      statusLabel.setText("直入力");
      vector.addElement(chartData.getTransit().toString());
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
  }
  public String toString() {
    return "テストプラグイン";
  }
  public int getChannelSize() {
    return 1;
  }
  static final String [] channelNames = { "ネイタル" };
  public String [] getChannelNames() {
    return channelNames;
  }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        transitTextField = new javax.swing.JTextField();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList.setFocusable(false);
        scrollPane.setViewportView(jList);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout(5, 0));

        jLabel1.setText("トランシット :");
        jLabel1.setFocusable(false);
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

        transitTextField.setColumns(20);
        transitTextField.setEditable(false);
        jPanel1.add(transitTextField, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("CH1");
        jPanel2.add(jLabel2);

        statusLabel.setText("Status");
        jPanel2.add(statusLabel);

        add(jPanel2, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField transitTextField;
    // End of variables declaration//GEN-END:variables

}
