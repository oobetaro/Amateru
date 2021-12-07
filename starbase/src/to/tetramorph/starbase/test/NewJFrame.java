/*
 * NewJFrame.java
 *
 * Created on 2007/12/27, 7:46
 */

package to.tetramorph.starbase.test;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.util.TestConst;
import to.tetramorph.util.IconLoader;

/**
 *
 * @author  大澤義鷹
 */
public class NewJFrame extends javax.swing.JFrame {
    
    //テーブル部のヘッダー名
    private static final String [] columnNames =
    { "名前","性別","職業","日時","場所" };

    private List<Natal> data = new ArrayList<Natal>();
    DataListTableModel tableModel;

    /** 
     * Creates new form NewJFrame 
     */
    public NewJFrame() {
        initComponents();
        createData();
        tableModel = new DataListTableModel();
        table.setModel( tableModel );
        TableColumn column = table.getColumn("名前");
        column.setCellRenderer(new DataTableCellRenderer());
    }
    private void createData() {
        for(int i=0; i<20; i++) {
            Natal natal = TestConst.getNatal(
                TestConst.AD,1964+i,i % 12,i % 28,0,0,0,TestConst.getMyPlace());
            natal.setName("名無しさん" + i);
            int gender = (i & 1) == 0 ? Natal.MALE : Natal.FEMALE;
            natal.setGender( gender );
            data.add(natal);
        }
    }
    //テーブルモデル
    private class DataListTableModel extends AbstractTableModel {
        public int getColumnCount() { return columnNames.length; }
        public int getRowCount() { return data.size();}
        //何のオブジェクトがセルに入っているかを返す
        public Object getValueAt(int row, int col) {
            Natal el = data.get(row);
            if(el == null) return null;
            String name = columnNames[col];
            if(name.equals("名前")) return el;
            if(name.equals("職業")) return el.getJob();
            if(name.equals("性別")) {
                if(el.getGender() == 1) return "♂";
                if(el.getGender() == 2) return "♀";
                return "";
            }
            if(name.equals("日時")) return el.getFormattedDate();
            if(name.equals("場所")) return el.getPlaceName();
            if(name.equals("メモ")) return el.getMemo();
            return "あれー？";
        }
        public String getColumnName(int column) { 
            return columnNames[column];
        }
        public Class getColumnClass(int c) { 
            return getValueAt(0, c).getClass();
        }
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        public void setValueAt(Object aValue, int row, int column) {
        }
    }
    //セルレンダラー
    private class DataTableCellRenderer extends JLabel implements TableCellRenderer {
        ImageIcon [] icons = new ImageIcon[7];
        public DataTableCellRenderer() {
            //これはデータの順列をかえて、static finalで切り直したほうがよさげ
            icons[0] = IconLoader.getImageIcon("/resources/images/List.clock.png");
            icons[1] = IconLoader.getImageIcon("/resources/images/List.male.png");
            icons[2] = IconLoader.getImageIcon("/resources/images/List.female.png");
            icons[3] = IconLoader.getImageIcon("/resources/images/List.composit.png");
            //icons[4] = 組織図用のアイコンになる予定
            //icons[5] = IconLoader.getImageIcon("/resources/images/List.folder.png");
            icons[5] = IconLoader.getImageIcon("/resources/images/List.none.png");
            icons[6] = IconLoader.getImageIcon("/resources/images/List.parentfolder.png");
        }
        
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,               // JTableのセルに入っているオブジェクトが渡される
            boolean isSelected,        // is the cell selected
            boolean cellHasFocus,      // the list and the cell have the focus
            int row,
            int column ) {
            Natal el = (Natal)value;
            if(el == null) return this;
            int type = 0;
            if(el.equalsChartType(Natal.COMPOSIT))
                type = 3;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.MALE )
                type = 1;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.FEMALE )
                type = 2;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.NONE)
                type = 5;
            setText(el.getName());
            setIcon( icons[ type ] );
            //選択のときの背景色が変わるのも自分で管理してやらないといけない。
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setEnabled(table.isEnabled());
            setFont(table.getFont());
            setOpaque(true);
            return this;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(table);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("jLabel1");
        jPanel1.add(jLabel1, java.awt.BorderLayout.EAST);

        jScrollPane2.setViewportView(jTextPane1);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
    
}
