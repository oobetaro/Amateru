/*
 * DataListTable.java
 *
 * Created on 2006/06/22, 22:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.TableColumnResizer;

/**
 * 検索結果のNatalのリストを表示するためのテーブル。
 * DataTableと似たクラスだが、DataTableはテーブルソートをDBのSQLで行っているが、
 * このクラスはNatalのListをCollectionsのメソッドでソートしていること。
 * 検索結果はDBでテーブルにして残すより配列とて保管したほうが簡単と思われるので。
 */
class SearchResultTable extends JTable {
    protected List<Natal> data = new ArrayList<Natal>();
    private static final String [] columnNames =
    { "パス","名前","性別","職業","日時","場所" };
    private int [] directive; //ソートの向き(ソートなし、登り順、降り順)
    //テーブルをソートするための比較器
    private NatalComparator comparator = new NatalComparator();
    //表示するデータ。showList()で外部から提供される。
    //これをdataにコピーしたものをテーブルで表示する。
    private List<Natal> originalList = null;
  
    /**
     * 検索結果出力用のテーブルを作成する
     */
    public SearchResultTable() {
        super();
        directive = new int[columnNames.length];
        for ( int i=0; i<directive.length; i++ ) 
            directive[i] = DBFactory.NOT_SORTED;
        JTableHeader tableHeader = getTableHeader();
        TableHeaderRenderer render =
            new TableHeaderRenderer( tableHeader.getDefaultRenderer() );
        tableHeader.setDefaultRenderer(render);
        tableHeader.addMouseListener(new HeaderMouseHandler());
        setModel(new DataListTableModel());
        TableColumn column = getColumn("名前");
        column.setCellRenderer(new SearchResultTableCellRenderer());
        setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
    }
    /**
     * 最初に選択されたデータを一つ返す。そのときDBから再読込して最新のデータ
     * を返す。その際、すでに選択されたデータがデータベースから削除されている
     * 場合エラーダイアログを出した後で、nullを返す。
     */
    public Natal getSelectedNatal() {
        if(getSelectedRow() < 0) return null;
        Natal temp = data.get(getSelectedRow());
        return temp;
//        Natal natal = DBFactory.getInstance().
//            getNatal(temp.getId(),temp.getPath());
//        if(natal == null) {
//            showErrorMessage(temp.getName());
//            return null;
//        }
//        natal.setPath(temp.getPath()); //パスは未登録状態なので再登録
//        return natal;
    }
    // エラーメッセージダイアログを開く
    private void showErrorMessage(String name) {
        String msg = "「" + name + "」はデータベースからすでに削除されています。";
        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(this),msg,
            "データ選択エラー",JOptionPane.ERROR_MESSAGE );
    }
    /**
     * 検索結果のListを表示する。
     */
    public void showList(List<Natal> list) {
        for ( int i=0; i<directive.length; i++ )
            directive[i] = DBFactory.NOT_SORTED;
        clearSelection();
        originalList = list;
        sortTableModel(0);
    }
    // テーブルソート用の比較器
    private class NatalComparator implements Comparator<Natal> {
        int col = 0;
        //ソートしたいカラム(列)を指定する。
        void setSortColumn(int col) {
            this.col = col;
        }
        public int compare(Natal occ1, Natal occ2) {
            int c = 0;
            switch(col) {
                case 0 : return occ1.getPath().compareTo(occ2.getPath());
                case 1 : return occ1.getName().compareTo(occ2.getName());
                case 2 : return ((Integer)occ1.getGender()).
                                      compareTo((Integer)occ2.getGender());
                case 3 : return occ1.getJob().compareTo(occ2.getJob());
                case 4 : return occ1.getCalendar().
                                      compareTo(occ2.getCalendar());
                case 5 : return occ1.getPlaceName().
                                      compareTo(occ2.getPlaceName());
                default : return 0;
            }
        }
    }

    // columで指定された列でソートする。(比較器を呼び出す)
    private void sortTableModel(int colum) {
        if(directive[colum] == 0) { //ソート無し
            // ソートしてない状態originalListをdataにコピー
            data.clear();
            if(originalList != null)
                for(Natal o: originalList) data.add(o);
        } else if(directive[colum] == 1) { //昇り順でソート
            comparator.setSortColumn(colum);
            Collections.sort(data,comparator);
        } else { // directiveが1の次はかならず2だからひっくり返すだけで良い
            Collections.reverse(data);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TableColumnResizer.adjust(SearchResultTable.this);
                revalidate();
            }
        });
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
            if(name.equals("パス"))
                return ( el.getPath() == null ) ? "" : el.getPath();
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
        public boolean isCellEditable(int row, int col) { return false; }
        public void setValueAt(Object aValue, int row, int column) {
        }
    }
    //セルレンダラ
    private class SearchResultTableCellRenderer extends JLabel 
                                               implements TableCellRenderer {
        ImageIcon [] icons = new ImageIcon[7];
        public SearchResultTableCellRenderer() {
            icons[0] = IconLoader.getImageIcon("/resources/images/List.clock.png");
            icons[1] = IconLoader.getImageIcon("/resources/images/List.male.png");
            icons[2] = IconLoader.getImageIcon("/resources/images/List.female.png");
            icons[3] = IconLoader.getImageIcon("/resources/images/List.composit.png");
            //icons[4] = 組織図用のアイコンになる予定
            icons[5] = IconLoader.getImageIcon("/resources/images/List.none.png");
            icons[6] = IconLoader.getImageIcon("/resources/images/List.parentfolder.png");
        }
        
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,     // JTableのセルに入っているオブジェクトが渡される
            boolean isSelected,        // is the cell selected
            boolean cellHasFocus,      // the list and the cell have the focus
            int row,
            int column ) {
            Natal el = (Natal)value;
            if(el == null) return this;
            int type = 0;
            if(el.equalsChartType(Natal.COMPOSIT)) type = 3;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.MALE ) type = 1;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.FEMALE ) type = 2;
            else if(el.equalsChartType(Natal.NATAL) &&
                el.getGender() == Natal.NONE ) type = 5;
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
    //テーブルヘッダーがマウスクリックされたのを拾う
    private class HeaderMouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            if (column != -1) {
                directive[column]++;
                if(directive[column] >= 3) directive[column] = 0;
                for(int i=0; i<columnNames.length; i++) {
                    if(i == column) continue;
                    directive[i] = 0;
                }
                sortTableModel(column);
            }
        }
    }
    //ヘッダー用の△アイコン
    private static class Arrow implements Icon {
        private boolean descending;
        private int size;
        private int priority;
        public Arrow(boolean descending, int size, int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color color = c == null ? Color.GRAY : c.getBackground();
            // In a compound sort, make each succesive triangle 20%
            // smaller than the previous one.
            int dx = (int)(size/2*Math.pow(0.8, priority));
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline.
            y = y + 5*size/6 + (descending ? -dy : 0);
            int shift = descending ? 1 : -1;
            g.translate(x, y);
            
            // Right diagonal.
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);
            
            // Left diagonal.
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);
            
            // Horizontal line.
            if (descending) {
                g.setColor(color.darker().darker());
            } else {
                g.setColor(color.brighter().brighter());
            }
            g.drawLine(dx, 0, 0, 0);
            
            g.setColor(color);
            g.translate(-x, -y);
        }
        
        public int getIconWidth() {
            return size;
        }
        
        public int getIconHeight() {
            return size;
        }
    }
    //ヘッダー用のセルレンダラ
    private class TableHeaderRenderer implements TableCellRenderer {
        private TableCellRenderer tableCellRenderer;
        
        public TableHeaderRenderer(TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }
        
        public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            Component c = tableCellRenderer.getTableCellRendererComponent(
                table,value, isSelected, hasFocus, row, column );
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setHorizontalTextPosition(JLabel.LEFT);
                int modelColumn = table.convertColumnIndexToModel(column);
                Arrow arrowIcon = null;
                if(directive[modelColumn] != DBFactory.NOT_SORTED)
                    arrowIcon = new Arrow( 
                        directive[modelColumn] == DBFactory.DESCENDING, 18,1);
                l.setIcon(arrowIcon);
            }
            return c;
        }
    }
    /**
     * テーブルの中の選択中の行を削除する。
     * showList()で与えられたListからも削除される。
     */
    public void removeSelectedList() {
        for(Iterator ite = getSelectedNatalList().iterator(); ite.hasNext(); ) {
            Natal occ = (Natal)ite.next();
            for(Natal o : originalList) {
                if(o.getId() == occ.getId()) {
                    originalList.remove(o);
                    break;
                }
            }
            data.remove(occ);
        }
        clearSelection();
        revalidate(); //tableの更新はrevalidateが正しい
    }
    /**
     * このテーブルに表示されているNatalデータのリストを返す。
     */
    public List<Natal> getNatalList() {
        return data;
    }
    /**
     * このテーブルの選択されたNatalのリストを返す。その際、データベースに問い合わ
     * せて最新のデータを返す。
     * 選択が0件のときときはsize()==0のListを返す。
     * 選択されたデータがすでに削除されている場合(一つでも)はnullを返す。
     * size==0とnull二種類あるのは、「検索結果の保存」のときに区別が必要になるため。
     * nullの場合はエラーがあった場合。0はエラーではない。
     */
    public List<Natal> getSelectedList() {
        int [] rows = getSelectedRows();
        List<Natal> list = new ArrayList<Natal>();
        for ( int i = 0; i < rows.length; i++ ) {
            Natal temp = data.get(rows[i]);
            list.add(temp);
        }
        return list;
    }
    //テーブルから選択したとき、選択されたデータをDBから読み直して返していたが、
    //それをやめにしてみた。データに相違が現れる事はほとんどないから。
//    public List<Natal> getSelectedList() {
//        int [] rows = getSelectedRows();
//        List<Natal> list = new ArrayList<Natal>();
//        for(int i=0; i<rows.length; i++) {
//            Natal temp = data.get(rows[i]);
//            Natal natal = DBFactory.getInstance().
//                getNatal(temp.getId(),temp.getPath());
//            if(natal == null) {
//                showErrorMessage(temp.getName());
//                return null;
//            }
//            natal.setPath(temp.getPath()); //パスは未登録状態なので再登録
//            list.add(natal);
//        }
//        return list;
//    }
    //テーブル上での削除用
    // 選択が0件のときと選択されたデータがすでに削除されている場合(一つでも)、
    // size()==0のListを返す。
    private List<Natal> getSelectedNatalList() {
        int [] rows = getSelectedRows();
        List<Natal> list = new ArrayList<Natal>();
        for(int i=0; i<rows.length; i++)
            list.add(data.get(rows[i]));
        return list;
    }
}
