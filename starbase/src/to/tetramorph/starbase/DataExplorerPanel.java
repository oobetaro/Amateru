/*
 * DataExplorerPanel.java
 *
 * Created on 2007/12/14, 14:35
 */

package to.tetramorph.starbase;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.SearchResultReceiver;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.TableColumnResizer;

/**
 * 出生データ編集やチャート表示操作を行うエクスプローラパネル。
 * @author  大澤義鷹
 */
class DataExplorerPanel extends JPanel implements DataExplorer {
    private DB db;
    private Map<String,JMenuItem> menuItemMap
                           = new HashMap<String,JMenuItem>();
    // テーブル部の宣言
    //テーブル部のヘッダー名
    private static final String [] columnNames =
    { "名前","性別","職業","日時","場所" };
    //テーブル部のヘッダーのDB上の名前
    private static final String [] sortKeys =
    { "KANA","GENDER","JOB","DATE","PLACENAME" };
    private int [] directive; //ソート方向を保管する配列
    private List<Natal> data = new ArrayList<Natal>();
    private TreePath currentTreePath = null;
    // ツリー部の宣言
    static DataFlavor localObjectFlavor;
    static {
        try {
            localObjectFlavor =
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch( ClassNotFoundException cnfe ) {
            cnfe.printStackTrace();
        }
    }
    static DataFlavor[] supportedFlavors = { localObjectFlavor };
    private DragSource dragSource;
    private DragSourceHandler dragSourceHandler = new DragSourceHandler();
    private DropTarget dropTarget;
    private TreeNode dropTargetNode = null;
    private TreeNode draggedNode = null;
    private TreeCellEditor cellEditor;
    private ChartDesktop chartDesktop;
    private SearchResultReceiver resultReceiver;
    private SearchDialog searchDialog;
    private ReturnCalcDialog returnCalcDialog;
    private AspectCalcDialog aspectCalcDialog;
    private SynodicPeriodCalcDialog synodicPeriodCalcDialog;
    /**
     * オブジェクトを作成する。
     * @param chartDesktop MainFrameオブジェクト
     */
    public DataExplorerPanel( ChartDesktop chartDesktop) {
        this.chartDesktop = chartDesktop;
        //検索結果フレーム
        resultReceiver = new SearchResultPanel( this, chartDesktop );
        searchDialog = new SearchDialog( chartDesktop.getFrame(), resultReceiver );
        returnCalcDialog = new ReturnCalcDialog( chartDesktop.getFrame(), resultReceiver );
        aspectCalcDialog = new AspectCalcDialog( chartDesktop.getFrame(), resultReceiver );
        synodicPeriodCalcDialog = new SynodicPeriodCalcDialog( chartDesktop.getFrame(), resultReceiver );
        initComponents();
        db = DBFactory.getInstance();
        initTable(); //テーブル部の設定
        initTree();
        splitPane.setDividerLocation(0.3);
    }
    /**
     * 検索結果のパネル(コンテナを返す。セパレートモードで使用
     */
    public Component getSearchResultPanel() {
        return (Component)resultReceiver;
    }
    /**
     * コピー先や移動先を指定するダイアログで使用するtreeを取得するためのもの。
     * getTreeメソッドを参照のこと。
     * @param dummy コンストラクタ重複を避けるための引数。値はなんでも良い。
     */
    private DataExplorerPanel( ChartDesktop chartDesktop, int dummy ) {
        //同じ引数をもつｺﾝｽﾄﾗｸﾀは作れないのでdummyを入れてある。
        this.chartDesktop = chartDesktop;
        resultReceiver =
            new SearchResultPanel( this, chartDesktop ); //検索結果フレーム
        searchDialog = new SearchDialog( chartDesktop.getFrame(), resultReceiver );
        initComponents();
        db = DBFactory.getInstance();
        initTable(); //テーブル部の設定
        initTree();
        // フォルダ作成のメニュー以外は消してしまう。3を残してあとは削除。
        // 0番から消すとそのつど後の順位番号がかわるので、末尾から消し次に頭を三回消す。
        int [] nums = { 9,8,7,6,5,4,0,0,0 };
        for ( int i : nums ) {
            treePopupMenu.remove( i );
        }
    }

    public void setTreeModel(TreeModel model) {
        tree.setModel( model );
    }
    /**
     * FolderSelectDialogに渡すJTreeのインスタンスを返す。このインスタンスを使うと、
     * ダイアログ中でも新規フォルダの作成や削除などが使えて非常に便利。
     * このJTreeをどうやって作るかというと、非可視状態でもう一つDatabaseFrameを作成し
     * そのなかのJTreeだけを抜き出して使うというやり方。JTableもJTreeもその他の
     * 検索用フレームもDatabaseFrameの中で癒着しているし、切り離すのも手間がかかる上
     * コードが煩雑になるため、まるごし複製して必要なツリーだけを使うことにした。
     */
    private JTree getTree(ChartDesktop chartDesktop) {
        DataExplorerPanel expl = new DataExplorerPanel(chartDesktop,0);
        //ツリーの中身は、すでに可視化されているツリーのものを共用する。
        //expl.getTree().setModel(tree.getModel());
        expl.setTreeModel( tree.getModel() );
        return expl.tree;
    }
    /**
     * 指定IDのNatalデータを返す。存在しないときはnullを返す。
     */
    public Natal getNatal(int id) {
        return db.getNatal(id);
    }

    /**
     * ネイタルデータ入力ダイアログを開きDBに新規登録。
     */
    public void registNatal() {
        int [] result = new int[2];
        do {
            Natal natal = DataInputDialog.showNatalDialog(
                this,tree.getSelectionPath(),result);
            if ( natal == null ) return; //中止された
            db.insertNatal( natal, tree.getSelectionPath() );
            showList( tree.getSelectionPath() );
            if ( result[1] == 1 ) {
                List<Natal> list = new ArrayList<Natal>();
                list.add( natal );
                chartDesktop.openChartPane( list );
            }
        } while( result[0] == 1 );
    }

    /**
     * イベントデータ入力ダイアログを開きDBに新規登録。
     */
    public void registEvent() {
        int [] result = new int[2];
        do {
            Natal natal = DataInputDialog.showEventDialog(
                this, tree.getSelectionPath(), result );
            if ( natal == null ) return;
            db.insertNatal( natal, tree.getSelectionPath() );
            showList( tree.getSelectionPath() );
            if ( result[1] == 1 ) {
                List<Natal> list = new ArrayList<Natal>();
                list.add( natal );
                chartDesktop.openChartPane( list );
            }
        } while ( result[0] == 1 );
    }

    /**
     * NatalをDBに登録する。occのgetId()が1以下なら(ID未登録なら)
     * 新規挿入で挿入時にはツリーのダイアログが開き、保存先フォルダを選択させられる。
     * IDが与えられている場合は更新保存とする。
     * 引数にnullを与えるとNullPointerException。
     * 新規に挿入された場合は、引数のoccにDB上のidが付与される。
     */
    public void registNatal( Natal occ ) {
        //すでに削除されている場合がありうるので存在確認。
        Natal natal = db.getNatal(occ.getId());
        if ( natal == null ) occ.setId(0);
        if ( occ.getId() < 1 ) { //新規登録
            TreePath targetTreePath = FolderSelectDialog.showDialog(
                chartDesktop.getFrame(),
                (TreeNode)tree.getModel().getRoot(),
                currentTreePath, this.getTree( chartDesktop ) );
            if ( targetTreePath == null ) return;
            int id = db.insertNatal( occ, targetTreePath );
            occ.setId( id );
            tree.setSelectionPath( targetTreePath );
        } else { //更新(上書き保存)
            db.updateNatal( occ );
        }
        showList( tree.getSelectionPath() );
    }
    /**
     * フォルダーを選択するダイアログを開き選択されたパスを返す。選択が中止された
     * ときはnullを返す。
     */
    public TreePath showFolderSelectDialog(String message) {
        return FolderSelectDialog.showDialog(
                                            chartDesktop.getFrame(),
                                            (TreeNode)tree.getModel().getRoot(),
                                            currentTreePath,
                                            this.getTree(chartDesktop),
                                            message);
    }
///////////////////////// テーブル部 //////////////////////////////////////////
    /* テーブルヘッダにソート機能をとりけた結果、セルレンダラやイベントリスナが
     *  相互にからみあっているため、それらはすべてjTableの内部に置くか、すべて
     * 外部におくかするのが一番効率がよく、ここではすべて外部に置くことにした
     */
    //JTableの初期化と設定
    private void initTable() {
        directive = new int[columnNames.length];
        for(int i=0; i<directive.length; i++)
            directive[i] = DBFactory.NOT_SORTED;
        JTableHeader tableHeader = table.getTableHeader();
        TableHeaderRenderer render =
            new TableHeaderRenderer(tableHeader.getDefaultRenderer());
        tableHeader.setDefaultRenderer(render);
        tableHeader.addMouseListener(new HeaderMouseHandler());
        TableModel tableModel = new DataListTableModel();
        table.setModel(tableModel);
        TableColumn column = table.getColumn("名前");
        column.setCellRenderer(new DataTableCellRenderer());
        table.addMouseListener(new TableMouseHandler());
    }

    //ヘッダー用のセルレンダラ
    private class TableHeaderRenderer implements TableCellRenderer {
        private TableCellRenderer tableCellRenderer;
        public TableHeaderRenderer(TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,boolean hasFocus,int row,int column) {
            Component c = tableCellRenderer.getTableCellRendererComponent(table,value,
                isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setHorizontalTextPosition(JLabel.LEFT);
                int modelColumn = table.convertColumnIndexToModel(column);
                Arrow arrowIcon = null;
                if(directive[modelColumn] != DBFactory.NOT_SORTED)
                    arrowIcon = new Arrow(
                        directive[modelColumn] == DBFactory.DESCENDING, 18, 1 );
                l.setIcon(arrowIcon);
            }
            return c;
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
                showList(currentTreePath);
            }
        }
    }
    //ヘッダー用の△▽アイコン
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
    //テーブル上のNatalリストがタブクリされたら、そのNatalをmainFrameに送る。
    private class TableMouseHandler extends MouseAdapter {
        //ダブクリでNatalが選択されら、mainFrameにNatalを送る。
        public void mouseClicked( MouseEvent e ) {
            if ( table.getSelectedRow() < 0 ) return;
            Natal occ = data.get( table.getSelectedRow() );
            if ( e.getClickCount() != 2 ) return;
            if ( e.isAltDown() ) {
                if ( ! chartDesktop.isEmptyChartPane() ) {
                    //Altキー併用でダブクリなら、トランジットとしてセット
                    chartDesktop.setTransit( getSelectedList(), null );
                }
            } else {
                //ネイタルをセット。あるいは新規チャートを開きセット。
                if ( chartDesktop.isEmptyChartPane() )
                    chartDesktop.openChartPane( getSelectedList() );
                else
                    chartDesktop.setNatal( getSelectedList(), null );
            }
        }
        //右ｸﾘｯｸでﾎﾟｯﾌﾟｱｯﾌﾟﾒﾆｭｰを出す。このとき「送る」のメニューを再構築
        public void mousePressed(MouseEvent event) {
            if ( event.getButton() == MouseEvent.BUTTON3 ) {
                if ( table.getSelectedRow() < 0 )
                    return;      //ﾃﾞｰﾀ未選択ならﾒﾆｭｰは出さない
                updateSendMenu(); //「送る」のメニューは毎回生成
                //チャートが無いときはトランジットに送るは使えないようにする
                sendTransitMenuItem.setEnabled( ! chartDesktop.isEmptyChartPane() );
                popupMenu.show( (Component) event.getSource(),
                                event.getX(), event.getY()      );
            }
        }
    }
    //ポップアップの「送る」と「追加で送る」メニューを更新。今開いているチャート
    //フレームを検出してそれらフレームにNatalのリストを送るメニューを作る
    private void updateSendMenu() {
        sendMenu.removeAll();
        sendMenu.add(newChartMenuItem); //「新規チャート」はいつも固定
        sendAddMenu.removeAll();
        ChartPane [] frame = chartDesktop.getChartPanes();
        if(frame != null && frame.length > 0) {
            for(int i=0; i < frame.length; i++) {
                JMenuItem sendMenuItem =
                    new JMenuItem("\"" + frame[i].getTitle()+"\"へ");
                sendMenu.add(sendMenuItem);
                sendMenuItem.addActionListener(
                    new SendMenuActionHandler(frame[i]));
                JMenuItem sendAddMenuItem =
                    new JMenuItem("\"" + frame[i].getTitle()+"\"へ");
                sendAddMenu.add(sendAddMenuItem);
                sendAddMenuItem.addActionListener(
                    new SendAddMenuActionHandler(frame[i]));
            }
        }
    }

    // 内部フレーム指定でNatalを送るためのイベントハンドラ
    private class SendMenuActionHandler implements ActionListener {
        ChartPane frame;
        SendMenuActionHandler(ChartPane frame) {
            this.frame = frame;
        }
        //内部フレームのsetNatalに選択されているNatalのリストを渡す
        public void actionPerformed(ActionEvent evt) {
            //frame.setNatal(getSelectedList());
            chartDesktop.setNatal(getSelectedList(),frame);
        }
    }

    // 内部フレーム指定で追加でNatalを送るためのイベントハンドラ
    private class SendAddMenuActionHandler implements ActionListener {
        ChartPane frame;
        SendAddMenuActionHandler(ChartPane frame) {
            this.frame = frame;
        }
        //内部フレームのsetNatalに選択されているNatalのリストを渡す
        public void actionPerformed(ActionEvent evt) {
            chartDesktop.addNatal(getSelectedList(),frame);
        }
    }

    //テーブル上で選択されているデータをListで返す。
    private List<Natal> getSelectedList() {
        int [] rows = table.getSelectedRows();
        List<Natal> list = new ArrayList<Natal>();
        for(int i=0; i<rows.length; i++) {
            Natal occ = (Natal)table.getModel().getValueAt(rows[i],0);
            list.add(occ);
        }
        return list;
    }
    //テーブル上で選択されているデータをIteratorで返す。
    private Iterator getSelectedNatals() {
        return getSelectedList().iterator();
    }
    /**
     * 指定されたフォルダの内容を表示する。
     * @param currentTreePath 表示するパス
     */
    public void showList( TreePath currentTreePath ) {
        this.currentTreePath = currentTreePath;
        table.clearSelection();
        boolean find = false;
        for ( int i=0; i < directive.length; i++ ) {
            if ( directive[i] != DBFactory.NOT_SORTED ) {
                find = true;
                db.getList( currentTreePath, data, sortKeys[i], directive[i] );
            }
        }
        if ( !find ) db.getList( currentTreePath, data, null, 0 );
        sizeLabel.setText( data.size() + "件" );
        pathLabel.setText( DBFactory.getPathString( currentTreePath ) );

//        JDK1.5のときはvalidateでよかったが1.6から振る舞いが変わったらしい

//        SwingUtilities.invokeLater( new Runnable() {
//            public void run() {
//                table.doLayout();
//                tableScrollPane.getVerticalScrollBar().setValue(0);
//                TableColumnResizer.adjust(table);
//                //このJFrameではなくJPanelに変更したらテーブル表示が正しく更新
//                //されなくなった。validate()に変更したら解決した。
//                //table.revalidate();
//                table.validate();
//                table.repaint(); //こっちも必要らしい
//                System.out.println("テーブルリスト更新したぞ");
//            }
//        });
        TableColumnResizer.adjust(table);
        //revalidateは遅延更新なので呼び出してすぐに表が即更新されるわけではない。
        //呼び出し直後にバーの最大値を取得しても古い値しか取得できない。
        table.revalidate();

        //↑それで一応数ミリ秒内には更新されるだろうと仮定して
        // 10ミリ秒後に垂直スクロールバーの値を取得し、一番下までスクロールし、
        // 登録されたデータが見えるようにする。
        // 期待通り動かない可能性はあるが、今のとここれしか思いつかない。
        // 一応期待した動作はしている。
        new Timer( 10, new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                Timer timer = (Timer)evt.getSource();
                timer.stop();
                JScrollBar bar = tableScrollPane.getVerticalScrollBar();
                bar.setValue( bar.getMaximum() );
            }
        }).start();
    }

    /**
     * テーブル中のデータでoccに一致するものを選択する。
     */
    public void selectTable(Natal occ) {
        final int id = occ.getId();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for(int i=0; i<data.size(); i++) {
                    if(data.get(i).getId() == id) {
                        table.setRowSelectionInterval(i,i);
                        Rectangle cellRect = table.getCellRect(i,0,false);
                        table.scrollRectToVisible(cellRect);
                        break;
                    }
                }
            }
        });
    }
    //テーブルの中の選択中の行を削除する(moveやremoveのｱｸｼｮﾝから呼ばれる)
    private void removeRows() {
        for(Iterator ite = getSelectedNatals(); ite.hasNext(); )
            data.remove((Natal)ite.next());
        table.clearSelection();
        table.repaint();
    }
    /**
     * ダイアログを開いてコピー先フォルダを取得し、Natalのリストをそこにコピーする。
     * SearchResultFrameから検索結果の保存で使用する。
     * ただし引数で与えられたlistが、DB未登録データのときは、新規挿入を行う。
     * listの0番目の要素で、挿入かコピーかを判定していて、以降それに従う。
     * つまりこのメソッドは今の時点(2008/09/16)ではかなり中途半端な仕様である。
     * DataExplorerインターフェイスの導入でpublicになったが、元はprotectedだった。
     * DatabasePanelはpublic classだが、publicでなくても良いかもしれない。
     */
    public void copyToFolder( List<Natal> list ) {
        TreePath targetTreePath = FolderSelectDialog.showDialog(
                                            chartDesktop.getFrame(),
                                            (TreeNode)tree.getModel().getRoot(),
                                            currentTreePath,
                                            this.getTree(chartDesktop) );
        if ( targetTreePath == null) return;
        if ( targetTreePath.getLastPathComponent() instanceof DustBoxTreeNode) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(table),
                "ごみ箱へコピーはできません。"
                ,"データのコピー",JOptionPane.ERROR_MESSAGE );
            return;
        }
        if ( db == null ) return;
        // 本当はここで分岐せず、メソッドを二本用意すべきかもしれない。
        //回帰計算など動的に生成されたデータは、新規登録の処理をする。
        if ( list.get(0).getId() == Natal.NEED_REGIST ) {
            for ( Natal n : list ) {
                db.insertNatal( n, targetTreePath );
            }
            showList( targetTreePath );
            tree.setSelectionPath( targetTreePath );
            tree.scrollPathToVisible( targetTreePath );
        } else { //通常の検索結果は登録済みデータのリストなのでコピー処理
            if ( db.copy( list.iterator(), targetTreePath ) ) {
                showList( targetTreePath );
                tree.setSelectionPath( targetTreePath );
                tree.scrollPathToVisible( targetTreePath );
            }
        }
    }

  ////////////////////////////// ツリー部 //////////////////////////////////////

    private void initTree() {
        DefaultTreeCellRenderer cellRenderer = new DnDTreeCellRenderer();
        tree.setCellRenderer( cellRenderer );
        DataTreeCellEditor treeCellEditor =
                                  new DataTreeCellEditor( tree, cellRenderer );
        tree.setCellEditor( treeCellEditor );
        tree.setModel( new DefaultTreeModel( new FolderTreeNode("default") ) );
        dragSource = new DragSource();
        DragGestureRecognizer dgr =
            dragSource.createDefaultDragGestureRecognizer(
                tree, DnDConstants.ACTION_MOVE,new DragGestureHandler() );
        dropTarget = new DropTarget( tree, new DropTargetHandler() );
        tree.setEditable( true );
        setNode( db.getTree() );
        tree.addMouseListener( new TreeMouseHandler() );
        tree.getModel().addTreeModelListener( treeCellEditor );
        tree.setToggleClickCount( 2 ); //重要
        //tree.setShowsRootHandles( false );
        //シングル選択式に
        TreeSelectionModel ts_model = tree.getSelectionModel();
        ts_model.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
    }
    /**
     * インポートが行われたときに、ツリーの表示を更新する。データベースの出生
     * データを、大幅に書き換えた場合に、そのままだとツリーやテーブル表示に
     * 反映されない。このメソッドはTREEPATH表からTreeModelを再構築する。
     */
    public void treeUpdate() {
        setNode(db.getUpdateTree());
        TreePath tp = foundTreePath("データ");
        selectFolder(tp);
    }
    /**
     * 引数で与えられたNatalが登録されているフォルダを開き、該当するデータを
     * セレクトする。テーブル側のデータもセレクトされる。
     */
    public void selectTree(Natal occ) {
        if(occ.getPath() != null) {
            TreePath path = foundTreePath(occ.getPath());
            System.out.println("selected ... " + path.toString());
            tree.scrollPathToVisible(path);
            tree.setSelectionPath(path);
            showList(path);
            selectTable(occ);
            //dataEditor.selectTableElement(occ);
        }
    }
    /**
     * 指定されたフォルダを開きテーブルにはフォルダの中のデータを表示。
     */
    public void selectFolder(TreePath path) {
        //tree.expandPath(path);
        tree.setSelectionPath(path);
        showList(path);
    }
    /**
     * 階層構造上に編み上げたノードオブジェクトをツリーにセットする。
     * @param rootNode 階層を表現するノードオブジェクト
     */
    public void setNode(FolderTreeNode rootNode) {
        //rootNodeからごみ箱の存在を検査し、即参照できるよう変数に保管する
        boolean find = false;
        for(Enumeration enu = rootNode.children(); enu.hasMoreElements(); ) {
            FolderTreeNode node = (FolderTreeNode)enu.nextElement();
            if(node instanceof DustBoxTreeNode) {
                find = true;
                break;
            }
        }
        //ごみ箱がないときは作る
        if(! find) {
            rootNode.insert(new DustBoxTreeNode(),0);
        }
        //ツリーモデルを作成してJTreeにセット
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener((TreeModelListener)cellEditor);
        treeModel.setAsksAllowsChildren(true); // 葉とフォルダの区別をしない設定
        tree.setModel(treeModel);
    }
    /**
     * 文字列表現のパス名からこのJTree内で正しく機能するTreePathを作成して返す。
     * setSelectionPath(treePath)として、コードからフォルダをセレクトしても、
     * フォルダが開かない事がある。その理由はTreePathはTreeNode[]から作成するが、
     * JTreeに表示させるためにsetModelで与えたTreeNodeで作成されたものでなければ
     * ならず、名前だけ同じにして適当にこしらえたTreeNode[]からTreePathを作っても
     * 正しく機能しない。文字列で表現されたパス名から、TreePathを作るときに、
     * JTree内のTreeNodeを配列に入れて、それを元にTreePathを作らなければならない。
     */
    public TreePath foundTreePath(String path) {
        String [] array = path.split("/");
        TreeNode node = (TreeNode)tree.getModel().getRoot();
        List<TreeNode> list = new ArrayList<TreeNode>();
        list.add(node);
        LOOP:
            for(int i=0; i<array.length; i++) {
                boolean found = false;
                for(Enumeration enu = node.children(); enu.hasMoreElements(); ) {
                    TreeNode childNode = (TreeNode)enu.nextElement();
                    if(childNode.toString().equals(array[i])) {
                        list.add(childNode);
                        node = childNode;
                        found = true;
                        break;
                    }
                } //↑このﾙｰﾌﾟ内でかならず見つからなければならない。
                if(! found) return null; //さもなくば不正なパスと見なしエラー
            }
            TreeNode [] temp = new TreeNode[list.size()];
            list.toArray(temp);
            return new TreePath(temp);
    }

    /**
     * パス名でツリーのﾉｰﾄﾞを検索して、該当するﾉｰﾄﾞを見つけて返す。見つからなれ
     * ばnullを返す。
     */
    public static MutableTreeNode findNode( FolderTreeNode root,
                                              TreePath treePath) {
        for ( Enumeration enu = root.breadthFirstEnumeration();
                                                     enu.hasMoreElements(); ) {
            TreeNode [] array = ((FolderTreeNode)enu.nextElement()).getPath();
            TreePath path = new TreePath(array);
            if(path.toString().equals(treePath.toString())) {
                return (MutableTreeNode)path.getLastPathComponent();
            }
        }
        return null;
    }
    /**
     * path[]をrooNodeに追加する。パスからﾂﾘｰ構造にﾉｰﾄﾞを編み上げる。
     * ﾌｧｲﾙｼｽﾃﾑと同様に同一階層に同名のﾌｫﾙﾀﾞは存在できないというﾙｰﾙにのっとる。
     * @param path ﾊﾟｽを表現する配列。
     * @param rootNode ﾙｰﾄにするﾉｰﾄﾞのｲﾝｽﾀﾝｽ。nullは禁止。
     */
    public static void addNode(Object [] path,FolderTreeNode rootNode) {
        FolderTreeNode temp = null;
        for(int i=0; i<path.length; i++) {
            boolean exists = false;
            for(int j=0; j<rootNode.getChildCount(); j++) {
                temp = (FolderTreeNode)rootNode.getChildAt(j);
                if(temp.toString().equals(path[i].toString())) {
                    exists = true;
                    break;
                }
            }
            if(! exists) {
                temp = new FolderTreeNode(path[i]);
                //addするときごみ箱にaddするような事態が発生するとExceptionが出る。
                //それを拾って異常なパス(/ごみ箱/なんとか/)を検出するべかもしれ
                //ないが今のところは未対応
                rootNode.add(temp);
            }
            rootNode = temp;
        }
    }
    /**
     * 選択中のフォルダ内にあるサブフォルダのリストを返す。
     * @return フォルダへのパス
     */
    public List<TreePath> getSubFolders() {
        FolderTreeNode node =
            (FolderTreeNode)tree.getSelectionPath().getLastPathComponent();
        List<TreePath> list = new ArrayList<TreePath>();
        for ( Enumeration enu = node.children(); enu.hasMoreElements(); ) {
            TreePath path =
                new TreePath(((FolderTreeNode)enu.nextElement()).getPath());
            list.add(path);
        }
        return list;
    }
    /**
     * 現在選択されているカレントフォルダーの親のパスを返す。
     * @return フォルダへのパス
     */
    public TreePath getParentFolder() {
        TreePath path = tree.getSelectionPath();
        if(path == null) return null;
        FolderTreeNode current = (FolderTreeNode)path.getLastPathComponent();
        TreeNode parent = current.getParent();
        if(parent == null) return null;
        TreeNode [] tn = ((FolderTreeNode)parent).getPath();
        return new TreePath(tn);
    }
    /**
     * 現在選択されているカレントフォルダーへのパスを返す。
     * @return フォルダへのパス
     */
    public TreePath getCurrentFolder() {
        FolderTreeNode current =
            (FolderTreeNode)tree.getSelectionPath().getLastPathComponent();
        return new TreePath(current.getPath());
    }
    // ドラックジェスチャーハンドラー
    private class DragGestureHandler implements DragGestureListener {
        public void dragGestureRecognized(DragGestureEvent dge) {
            //この(x,y)にあるオブジェクトを検出
            Point clickPoint = dge.getDragOrigin();
            TreePath path = tree.getPathForLocation(clickPoint.x, clickPoint.y);
            if ( path == null ) return; //ノードではない場合
            draggedNode = (TreeNode) path.getLastPathComponent();
            if ( draggedNode instanceof DustBoxTreeNode)
                return; //ゴミ箱は移動できない
            Transferable trans = new RJLTransferable(draggedNode);
            dragSource.startDrag(
                dge,Cursor.getDefaultCursor(),trans,dragSourceHandler);
        }
    }
    // ドラッグソースハンドラー
    private class DragSourceHandler extends DragSourceAdapter {
        public void dragDropEnd(DragSourceDropEvent dsde) {
            dropTargetNode = null;
            draggedNode = null;
            tree.repaint();
        }
    }
    // ドラッグターゲットハンドラー
    private class DropTargetHandler extends DropTargetAdapter {
        //public void dragExit(DropTargetEvent dte) {}
        //public void dropActionChanged(DropTargetDragEvent dtde) {}
        public void dragEnter(DropTargetDragEvent dtde) {
            System.out.println("dragEnter");
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            System.out.println("aceepeted dragEnter");
        }
        public void dragOver(DropTargetDragEvent dtde) {
            //どのセル上にあるかを判断
            Point dragPoint = dtde.getLocation();
            TreePath path = tree.getPathForLocation(dragPoint.x,dragPoint.y);
            if(path == null)
                dropTargetNode = null;
            else
                dropTargetNode = (TreeNode) path.getLastPathComponent();
            tree.repaint();
        }
        public void drop(DropTargetDropEvent dtde) {
            DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
//      if(dtde.getSourceActions() == DnDConstants.ACTION_LINK)
//        System.out.println("ACTION_LINK");
//      if(dtde.getSourceActions() == DnDConstants.ACTION_COPY)
//        System.out.println("ACTION_COPY");
//      if(dtde.getSourceActions() == DnDConstants.ACTION_MOVE)
//        System.out.println("ACTION_MOVE");
            Point dropPoint = dtde.getLocation();
            TreePath path = tree.getPathForLocation(dropPoint.x, dropPoint.y); //ﾄﾞﾛｯﾌﾟされた座標にあったﾊﾟｽを取得
            if(path == null) { //ﾊﾟｽが無い場所にﾄﾞﾛｯﾌﾟされたときは後の処理はせず終わる
                dtde.dropComplete(false);
                return;
            }
            boolean dropped = false;
            try {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                Object droppedObject =
                    dtde.getTransferable().getTransferData(localObjectFlavor);
                FolderTreeNode droppedNode = null;
                if(droppedObject instanceof FolderTreeNode)
                    droppedNode = (FolderTreeNode) droppedObject;
                else { dtde.dropComplete(false); return; }
                FolderTreeNode dropNode =
                    (FolderTreeNode) path.getLastPathComponent();
                if(dropNode != droppedNode &&
                    !((FolderTreeNode)droppedNode).isNodeDescendant(dropNode)) {
                    //ごみ箱への移動が行われた時。つまり移動ではなく削除。
                    if(dropNode instanceof DustBoxTreeNode) {
                        if(db == null) {
                            treeModel.removeNodeFromParent(droppedNode);
                        } else if(db != null) {
                            if(db.removeFolder(
                                new TreePath(droppedNode.getUserObjectPath())))
                                treeModel.removeNodeFromParent(droppedNode);
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                    //ﾌｫﾙﾀﾞの移動
                    for ( Enumeration enu = dropNode.children();
                                                     enu.hasMoreElements(); ) {
                        FolderTreeNode dn = (FolderTreeNode)enu.nextElement();
                        if(dn.toString().equals(droppedNode.toString())) {
                            JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(tree),
                                "名前が重複するので移動できません。",
                                "フォルダの移動",
                                JOptionPane.ERROR_MESSAGE);
                            dtde.dropComplete(false);
                            return;
                        }
                    }
                    if(db == null) {
                        treeModel.removeNodeFromParent(droppedNode);
                        treeModel.insertNodeInto(
                            droppedNode,dropNode,dropNode.getChildCount());
                        tree.expandPath(new TreePath(droppedNode.getPath()));
                    } else {
                        TreePath source =
                            new TreePath(droppedNode.getUserObjectPath());
                        TreePath target =
                            new TreePath(dropNode.getUserObjectPath());
                        if(db.moveFolder(source,target)) {
                            treeModel.removeNodeFromParent(droppedNode);
                            treeModel.insertNodeInto(
                                droppedNode,dropNode,dropNode.getChildCount());
                            tree.expandPath(new TreePath(droppedNode.getPath()));
                        }
                    }
                    dropped = true;
                } else dropped = false;
            } catch(Exception e) {
                e.printStackTrace();
            }
            dtde.dropComplete(dropped);
        }
    }
    // DnDのときにドラッグされるオブジェクトを格納するクラス
    private class RJLTransferable implements Transferable {
        Object object;
        public RJLTransferable(Object o) {
            object = o;
        }
        public Object getTransferData(DataFlavor df)
        throws UnsupportedFlavorException,IOException {
            if(isDataFlavorSupported(df))
                return object;
            else
                throw new UnsupportedFlavorException(df);
        }
        public boolean isDataFlavorSupported(DataFlavor df) {
            return (df.equals(localObjectFlavor));
        }
        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }
    }
    // フォルダ開閉のアニメ、DnDされるノードを枠線で囲む事ができるセルレンダラ
    private class DnDTreeCellRenderer extends DefaultTreeCellRenderer {
        boolean isTargetNode;
        boolean isLastItem;
        Icon openFolderIcon;
        Icon dustboxIcon;

        public DnDTreeCellRenderer() {
            super();
            openFolderIcon =
                IconLoader.getImageIcon("/resources/images/Tree.openIcon.png");
            dustboxIcon =
                IconLoader.getImageIcon("/resources/images/Tree.dustbox.png");
        }

        @Override
        public Component getTreeCellRendererComponent( JTree tree,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean isExpanded,
                                                        boolean isLeaf,
                                                        int row,
                                                        boolean hasFocus) {
            super.getTreeCellRendererComponent
                (tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
            //選択状態に応じてフォルダアイコンの変更
            if ( value instanceof DustBoxTreeNode ) {
                //ﾙｰﾄﾉｰﾄﾞの子の中の"ごみ箱(予約語)"ならゴミ箱アイコンにする
                setIcon( dustboxIcon );
                setText("DustBox");
            } else if ( isSelected && isExpanded ) {
                setIcon( openFolderIcon );
            } else {
                setIcon( UIManager.getIcon("Tree.closedIcon") );
            }
            //DnDの際ノードを枠線で囲むが、そのためのﾌﾗｸﾞﾒﾝﾃｰｼｮﾝ
            isTargetNode = ( value == dropTargetNode );
            boolean showSelected = isSelected & (dropTargetNode == null);
            return this;
        }
        @Override
        public void paintComponent( Graphics g ) {
            super.paintComponent(g);
            if ( isTargetNode ) {
                g.setColor( Color.black );
                g.drawRect( 0, 0, getSize().width - 1, getSize().height - 1 );
            }
        }
    }

    // ノードセルの名前編集にはセルｴﾃﾞｨﾀとTreeModelListenerが同じｸﾗｽだとやりやすい
    private class DataTreeCellEditor extends DefaultTreeCellEditor
                                                implements TreeModelListener {
        String preNodeName = "";   //編集前のノード名を保管
        Object [] prePath = null; //編集前のパス名を保管
        DataTreeCellEditor( JTree tree, DefaultTreeCellRenderer renderer ) {
            super( tree, renderer );
        }

        //ﾄﾘﾌﾟﾙｸﾘｯｸで編集が始まると呼び出される。valueには編集するセルが入っている。
        public Component getTreeCellEditorComponent(
            JTree tree,         //編集を要求している JTree。
            Object value,       //編集されるセル値
            boolean isSelected, //ｾﾙがﾚﾝﾀﾞﾘﾝｸﾞとなり選択部がﾊｲﾗｲﾄされている場合はtrue
            boolean expanded,   //ﾉｰﾄﾞが展開されている場合は true
            boolean leaf,       //ﾉｰﾄﾞが葉ﾉｰﾄﾞの場合は true
            int row) {          //編集中のﾉｰﾄﾞの行ｲﾝﾃﾞｯｸｽ
            preNodeName = value.toString();
            prePath = ((FolderTreeNode)value).getUserObjectPath();
            //ここで編集前の名前が取得できる。
            //System.out.println("getTreeCellEditorComposnet()" + preNodeName);
            return super.getTreeCellEditorComponent(tree, value,isSelected,expanded,
                leaf, row);
        }

        public void valueChanged( TreeSelectionEvent e ) {
            System.out.println("valueChanged() : " + e.getPath().toString() );
            if ( e.getPath().getLastPathComponent() instanceof DustBoxTreeNode ) {
                System.out.println( "ゴミ箱なので編集不可" );
                tree.setEditable( false );
                makeFolderMenuItem.setEnabled(false);
            } else {
                if ( e.getPath().getLastPathComponent() == tree.getModel().getRoot() ) {
                    System.out.println("ルートノードですわ");
                    tree.expandPath( e.getPath() );
                    //tree.expandRow( 0 );
                }
                tree.setEditable( true );
                makeFolderMenuItem.setEnabled(true);
            }
        }

        // フォルダ名の変更(ﾘﾈｰﾑ),  (TreeModelListenerの実装)
        public void treeNodesChanged(TreeModelEvent e) {
            System.out.println("treeNodeChanged() : ");
            Object [] nodes = e.getChildren();
            //編集が終わったノードを取得
            FolderTreeNode editedNode = (FolderTreeNode)nodes[0];
            if(editedNode.toString().equals("")) {
                JOptionPane.showMessageDialog(tree,"名前がありません");
                //TreePath path = new TreePath(editedNode.getPath());
                //editedNode.setUserObject(preNodeName);ではダメ
                //↓この処理でもう一度このtreeNodesChanged(..)が呼ばれる。「変更」したから。
                ((DefaultTreeModel)tree.getModel()).valueForPathChanged(
                    new TreePath(editedNode.getPath()), preNodeName);
                return;
            }
            TreeNode parent = editedNode.getParent();
            for(Enumeration enu = parent.children(); enu.hasMoreElements();) {
                TreeNode childTreeNode = (TreeNode)enu.nextElement();
                //子ノードには編集ノードも混在してるのでそれは除外
                if(editedNode != childTreeNode && childTreeNode.toString().
                    equals(editedNode.toString())) {
                    JOptionPane.showMessageDialog(tree,"同じ名前は指定できません");
                    ((DefaultTreeModel)tree.getModel()).valueForPathChanged(
                        new TreePath(editedNode.getPath()), preNodeName);
                    return; //処理終了
                }
            }
            //変更前ﾌｫﾙﾀﾞ名が無いときはｷｬﾝｾﾙ
            if(prePath == null) return;
            //同名でのﾘﾈｰﾑ要求はｷｬﾝｾﾙ
            if(preNodeName.equals(editedNode.getUserObject().toString()) ) return;
            if(db == null) return;
            db.renameFolder(
                new TreePath(prePath),
                new TreePath(editedNode.getUserObjectPath()));
        }
        //未使用
        public void treeNodesInserted(TreeModelEvent e) {
            //System.out.println("ツリーモデルリスナから通達：挿入");
        }
        //未使用
        public void treeNodesRemoved(TreeModelEvent e) {
            //System.out.println("ツリーモデルリスナから通達：削除");
        }
        //未使用
        public void treeStructureChanged(TreeModelEvent e) {
            //ystem.out.println("ツリーモデルリスナから通達：大幅な構造変更");
        }
    }

    // 右クリックでのポップアップメニューによる削除やコピー機能。左クリックでフォルダ内表示。
    private class TreeMouseHandler extends MouseAdapter {
        public void mousePressed( MouseEvent event ) {
            JMenuItem [] items = { moveFolderMenuItem, renameFolderMenuItem,
                                copyFolderMenuItem, removeFolderMenuItem };

            TreePath path = tree.getSelectionPath();
            if ( event.getButton() == MouseEvent.BUTTON3 && path != null ) {
                FolderTreeNode node =
                (FolderTreeNode)tree.getSelectionPath().getLastPathComponent();
                //ルートフォルダが選択されたときは、一部のメニュー機能を抑止
                boolean flag = node != tree.getModel().getRoot();
                for ( JMenuItem item : items ) {
                    item.setEnabled( flag );
                }
                System.out.println( "node = " + node );
                if ( node instanceof DustBoxTreeNode ) {
                    dustboxPopupMenu.show( (Component) event.getSource(),
                                               event.getX(), event.getY() );
                } else {
                    treePopupMenu.show( (Component) event.getSource(),
                                               event.getX(), event.getY() );
                }
            } else if ( event.getButton() == MouseEvent.BUTTON1 &&
                                                               path != null ) {
                Config.usr.setProperty( "CurrentTreePath",
                               DBFactory.getPathString( path ) );
                searchDialog.setTargetPath( tree.getSelectionPath() );
                showList(path); //テーブルに表示
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////
    /**
     * 検索フレームと検索結果フレーム両方を開く。
     */
    public void showSearchFrame() {
        searchDialog.showFrame( tree.getSelectionPath() );
        chartDesktop.setResultVisible( true ); //検索結果窓を表示
    }

    /**
     * 検索フレーム(SearchFrame)が表示されている状態ならtrueを返す。
     */
    public boolean isSearchFrameShowing() {
        return searchDialog.isShowing();
    }

    /**
     * 回帰日時の検索ダイアログと、検索結果フレーム両方を開く。
     */
    public void showReturnCalcDialog() {
        List<Natal> list = getSelectedList();
        if ( list.size() <= 0 ) return;
        chartDesktop.setResultVisible( true ); //検索結果窓を可視化
        returnCalcDialog.showDialog( list.get(0) );
    }

    /**
     * ネイタルとトランジット間のアスペクト時期検索。回帰検索と似たようなもの。
     */
    public void showAspectCalcDialog() {
        List<Natal> list = getSelectedList();
        if ( list.size() <= 0 ) return;
        chartDesktop.setResultVisible( true ); //検索結果窓を可視化
        aspectCalcDialog.showDialog( list.get(0) );
    }

    public void showSynodicPeriodCalcDialog() {
        List<Natal> list = getSelectedList();
        if ( list.size() <= 0 ) return;
        chartDesktop.setResultVisible( true ); //検索結果窓を可視化
        synodicPeriodCalcDialog.showDialog();
    }

    private static final int OP_MOVE = 0;
    private static final int OP_REMOVE = 1;
    private static final int OP_EDIT = 2;
    private static final int OP_RENAME = 3;
    private static final int OP_COPY = 4;
    private static final String [] OP_TITLES = {
        "移動","削除","編集","リネーム","コピー"
    };
    //ﾃｰﾌﾞﾙの選択ﾃﾞｰﾀがﾁｬｰﾄ表示中ならｴﾗｰﾀﾞｲｱｸﾞで告知しtrueを返す。
    private boolean isBusyForTable(int OP_CODE) {
        for(Natal natal:getSelectedList()) {
            if(chartDesktop.isDataBusy(natal.getId())) {
                String msg = "<html>チャート表示中は実行できません。<br>" +
                    "表示中のデータ : " + natal.getName() + "</html>";
                String title = "データの" + OP_TITLES[OP_CODE];
                JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(table),
                    msg,title,JOptionPane.ERROR_MESSAGE);
                return true;
            }
        }
        return false;
    }
    //ﾂﾘｰで選択されたﾌｫﾙﾀﾞ内にﾁｬｰﾄ表示中のﾃﾞｰﾀがある場合は告知してtrueを返す。
    private boolean isBusyForTree( int OP_CODE ) {
        List<Integer> list = db.getIDinFolder( tree.getSelectionPath() );
        for ( Integer id: list ) {
            if ( chartDesktop.isDataBusy( id ) ) {
                String msg = "<html>チャート表示中は実行できません。<br>" +
                    "表示中のデータ : " + db.getNatal(id).getName() + "</html>";
                String title = "フォルダの" + OP_TITLES[ OP_CODE ];
                JOptionPane.showMessageDialog(
                                    SwingUtilities.getWindowAncestor( tree ),
                                    msg, title, JOptionPane.ERROR_MESSAGE );
                return true;
            }
        }
        return false;
    }
    //ﾌｫﾙﾀﾞを削除・ｺﾋﾟｰ等をしようとした際に発生したｴﾗｰの表示
    private void showFolderError(String msg,int OP_CODE) {
        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(tree),msg,
            "フォルダの" + OP_TITLES[OP_CODE],JOptionPane.ERROR_MESSAGE );
    }
    //ﾃﾞｰﾀを削除・移動等をしようとした際に発生したｴﾗｰの表示
    private void showDataError(String msg,int OP_CODE) {
        JOptionPane.showMessageDialog(
            SwingUtilities.getWindowAncestor(tree),msg,
            "データの" + OP_TITLES[OP_CODE],JOptionPane.ERROR_MESSAGE );
    }
    // Look and Feelの更新用だが、まだ中途半端
    public void updateLookAndFeel() {
        SwingUtilities.updateComponentTreeUI( searchDialog );
        SwingUtilities.updateComponentTreeUI( tree );
        SwingUtilities.updateComponentTreeUI( splitPane );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;

        popupMenu = new javax.swing.JPopupMenu();
        sendMenu = new javax.swing.JMenu();
        newChartMenuItem = new javax.swing.JMenuItem();
        sendAddMenu = new javax.swing.JMenu();
        sendTransitMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        returnMenuItem = new javax.swing.JMenuItem();
        aspcalcMenuItem = new javax.swing.JMenuItem();
        synodicMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        editMenuItem = new javax.swing.JMenuItem();
        moveMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        treePopupMenu = new javax.swing.JPopupMenu();
        moveFolderMenuItem = new javax.swing.JMenuItem();
        copyFolderMenuItem = new javax.swing.JMenuItem();
        renameFolderMenuItem = new javax.swing.JMenuItem();
        makeFolderMenuItem = new javax.swing.JMenuItem();
        removeFolderMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        registNatalMenuItem = new javax.swing.JMenuItem();
        registEventMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        searchMenuItem = new javax.swing.JMenuItem();
        dustboxPopupMenu = new javax.swing.JPopupMenu();
        clearDustboxMenuItem = new javax.swing.JMenuItem();
        splitPane = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        pathLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();

        sendMenu.setText("\u9001\u308b");
        newChartMenuItem.setText("\u65b0\u898f\u30c1\u30e3\u30fc\u30c8");
        newChartMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newChartMenuItemActionPerformed(evt);
            }
        });

        sendMenu.add(newChartMenuItem);

        popupMenu.add(sendMenu);

        sendAddMenu.setText("\u8ffd\u52a0\u3067\u9001\u308b");
        popupMenu.add(sendAddMenu);

        sendTransitMenuItem.setText("\u30c8\u30e9\u30f3\u30b8\u30c3\u30c8\u306b\u9001\u308b");
        sendTransitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendTransitMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(sendTransitMenuItem);

        popupMenu.add(jSeparator3);

        returnMenuItem.setText("\u56de\u5e30\u65e5\u6642\u3092\u691c\u7d22");
        returnMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(returnMenuItem);

        aspcalcMenuItem.setText("\u30a2\u30b9\u30da\u30af\u30c8\u6642\u671f\u691c\u7d22");
        aspcalcMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aspcalcMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(aspcalcMenuItem);

        synodicMenuItem.setText("\u6708\u76f8/\u5929\u4f53\u4f1a\u5408\u691c\u7d22");
        synodicMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synodicMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(synodicMenuItem);

        popupMenu.add(jSeparator4);

        editMenuItem.setText("\u30c7\u30fc\u30bf\u7de8\u96c6");
        editMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(editMenuItem);

        moveMenuItem.setText("\u30c7\u30fc\u30bf\u79fb\u52d5");
        moveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(moveMenuItem);

        removeMenuItem.setText("\u30c7\u30fc\u30bf\u524a\u9664");
        removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(removeMenuItem);

        copyMenuItem.setText("\u30c7\u30fc\u30bf\u30b3\u30d4\u30fc");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });

        popupMenu.add(copyMenuItem);

        moveFolderMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u3092\u79fb\u52d5");
        moveFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveFolderMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(moveFolderMenuItem);

        copyFolderMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u3092\u30b3\u30d4\u30fc");
        copyFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyFolderMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(copyFolderMenuItem);

        renameFolderMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u3092\u30ea\u30cd\u30fc\u30e0");
        renameFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameFolderMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(renameFolderMenuItem);

        makeFolderMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210");
        makeFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeFolderMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(makeFolderMenuItem);

        removeFolderMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664");
        removeFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFolderMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(removeFolderMenuItem);

        treePopupMenu.add(jSeparator1);

        registNatalMenuItem.setText("\u30cd\u30a4\u30bf\u30eb\u30c7\u30fc\u30bf\u306e\u767b\u9332");
        registNatalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registNatalMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(registNatalMenuItem);

        registEventMenuItem.setText("\u30a4\u30d9\u30f3\u30c8\u30c7\u30fc\u30bf\u306e\u767b\u9332");
        registEventMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registEventMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(registEventMenuItem);

        treePopupMenu.add(jSeparator2);

        searchMenuItem.setText("\u30d5\u30a9\u30eb\u30c0\u5185\u3092\u691c\u7d22");
        searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMenuItemActionPerformed(evt);
            }
        });

        treePopupMenu.add(searchMenuItem);

        clearDustboxMenuItem.setText("\u3054\u307f\u7bb1\u3092\u7a7a\u306b\u3059\u308b");
        clearDustboxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearDustboxMenuItemActionPerformed(evt);
            }
        });

        dustboxPopupMenu.add(clearDustboxMenuItem);

        setLayout(new java.awt.BorderLayout());

        tree.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 8, 1, 1));
        treeScrollPane.setViewportView(tree);

        splitPane.setLeftComponent(treeScrollPane);

        jPanel1.setLayout(new java.awt.BorderLayout());

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
        tableScrollPane.setViewportView(table);

        jPanel1.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        pathLabel.setText(" ");
        jPanel2.add(pathLabel, java.awt.BorderLayout.CENTER);

        sizeLabel.setText(" ");
        sizeLabel.setPreferredSize(new java.awt.Dimension(60, 19));
        jPanel2.add(sizeLabel, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        splitPane.setRightComponent(jPanel1);

        add(splitPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void synodicMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_synodicMenuItemActionPerformed
        showSynodicPeriodCalcDialog();
    }//GEN-LAST:event_synodicMenuItemActionPerformed

    private void aspcalcMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aspcalcMenuItemActionPerformed
        showAspectCalcDialog();
    }//GEN-LAST:event_aspcalcMenuItemActionPerformed
    //回帰計算用のダイアログを表示
    private void returnMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnMenuItemActionPerformed
        showReturnCalcDialog();
    }//GEN-LAST:event_returnMenuItemActionPerformed

    private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
        //ツリー窓のポップアップメニュー内から検索パネルを呼び出す
        showSearchFrame(); //検索指定ダイアログを表示
    }//GEN-LAST:event_searchMenuItemActionPerformed
    //トランジットに送る
    private void sendTransitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendTransitMenuItemActionPerformed
        if ( ! chartDesktop.isEmptyChartPane() ) {
            //Altキー併用でダブクリなら、トランジットとしてセット
            chartDesktop.setTransit( getSelectedList(), null );
        }
    }//GEN-LAST:event_sendTransitMenuItemActionPerformed

    private void clearDustboxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearDustboxMenuItemActionPerformed
        int result = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(tree),
            "ごみ箱の内容をすべて消去します。よろしいですか？",
            "ごみ箱の削除",JOptionPane.YES_NO_OPTION );
        if(result == JOptionPane.YES_OPTION) {
            if(db != null) {
                if(db.clearDustBox(tree.getSelectionPath())) {
                    showList(tree.getSelectionPath());
                }
            }
        }
    }//GEN-LAST:event_clearDustboxMenuItemActionPerformed

    private void removeFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFolderMenuItemActionPerformed
        if ( isBusyForTree( OP_REMOVE ) ) return;
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        FolderTreeNode node = (FolderTreeNode)tree.getSelectionPath()
                                                        .getLastPathComponent();
        TreePath parentTreePath = tree.getSelectionPath().getParentPath();
        int result = JOptionPane.showConfirmDialog(
                                         SwingUtilities.getWindowAncestor(tree),
            "<HTML>フォルダ「" + node.toString() +
            "」とその内容をすべて削除します。<BR>よろしいですか？</HTML>",
            "フォルダの削除",JOptionPane.YES_NO_OPTION );
        if ( result == JOptionPane.YES_OPTION ) {
            if ( db == null ) {
                treeModel.removeNodeFromParent(node);
            } else if ( db.removeFolder( tree.getSelectionPath() ) ) {
                treeModel.removeNodeFromParent( node );
                selectFolder( parentTreePath );//削除したﾊﾟｽの一つ上のﾌｫﾙﾀﾞを選択
            }
        }
    }//GEN-LAST:event_removeFolderMenuItemActionPerformed

    private void registEventMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registEventMenuItemActionPerformed
        this.registEvent();
    }//GEN-LAST:event_registEventMenuItemActionPerformed

    private void registNatalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registNatalMenuItemActionPerformed
        this.registNatal();
    }//GEN-LAST:event_registNatalMenuItemActionPerformed

    private void makeFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeFolderMenuItemActionPerformed
        FolderTreeNode parent = (FolderTreeNode)tree.getSelectionPath().getLastPathComponent();
        //作成するフォルダ名が重複しないようにフォルダ名を作成する
        Set<String> nameSet = new HashSet<String>();
        for(Enumeration enu = parent.children(); enu.hasMoreElements(); )
            nameSet.add(((FolderTreeNode)enu.nextElement()).toString());
        String folderName = "新フォルダ";
        int n = 2;
        String num = "";
        while(nameSet.contains(folderName+num)) {
            num = "" + n;
            n++;
        }
        //フォルダを作成。(新ノードを挿入)
        FolderTreeNode newNode = new FolderTreeNode(folderName+num);
        //treeModel.insertNodeInto(newNode,parent,parent.getChildCount());
        ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode,parent,parent.getChildCount());
        //挿入したノードのパスを知るために、挿入後のノードの参照を取得しなおす
        newNode = (FolderTreeNode) parent.getLastChild();
        //挿入したノードのパスを取得
        //(どちらでも同じように思えるがnewNode.getUserObjectPath()だとエラーになる。)
        TreeNode [] path = newNode.getPath();
        TreePath treePath = new TreePath(path);
        tree.expandPath(treePath); //作成したﾌｫﾙﾀﾞを展開(ﾊﾝﾄﾞﾙ消える)
        if(db != null) {
            db.makeFolder(treePath);
        }
        //作成したノードを編集開始状態に設定(ﾄﾘﾌﾟﾙｸﾘｯｸなしにﾕｰｻﾞがお好みで改名できる)
        tree.startEditingAtPath( treePath );
    }//GEN-LAST:event_makeFolderMenuItemActionPerformed

    private void copyFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyFolderMenuItemActionPerformed
        TreePath targetTreePath = FolderSelectDialog.showDialog(chartDesktop.getFrame(),
            (TreeNode)tree.getModel().getRoot(),tree.getSelectionPath(),this.getTree(chartDesktop));
        if(targetTreePath == null) return;
        if(targetTreePath.getLastPathComponent() instanceof DustBoxTreeNode ) {
            showFolderError("ごみ箱へコピーはできません。",OP_COPY);
            return;
        }
        System.out.println("TreePath = " + targetTreePath);
        TreePath currentTreePath = tree.getSelectionPath();
        if(currentTreePath.equals(targetTreePath)) {
            showFolderError("同じフォルダにはコピーできません。",OP_COPY);
            return;
        }
        FolderTreeNode currentNode,targetNode;
        currentNode = (FolderTreeNode)currentTreePath.getLastPathComponent();
        targetNode = (FolderTreeNode)targetTreePath.getLastPathComponent();
        if(targetNode.isNodeAncestor(currentNode)) {
            showFolderError("親を子にコピーすることはできません。",OP_COPY);
            return;
        }
        //子を親フォルダにコピーしたようなケースを同名一致で判定し、
        //同名の場合はリネームしてのコピーを可能にする。
        boolean isRenameCopy;
        String newName = currentNode.toString();
        INPUT_NAME:
            for(;;) {
            isRenameCopy = false;
            for(Enumeration enu = targetNode.children(); enu.hasMoreElements(); ) {
                FolderTreeNode childNode = (FolderTreeNode)enu.nextElement();
                while(childNode.toString().equals( newName )) {
                    String msg = "<HTML>コピー先フォルダ内に「" + childNode.toString() +
                        "」はすでに存在します。<BR>別の名前を指定してください。</HTML>";
                    newName = JOptionPane.showInputDialog(
                        SwingUtilities.getWindowAncestor(tree),msg,childNode);
                    isRenameCopy = true;
                    System.out.println("newName = " + newName);
                }
            }
            if(newName == null) return; //名前入力がキャンセルされたときは終了
            //長さｾﾞﾛの文字列や"/"や","が混入している名前は不可
            if(newName.matches("{0}|.*(/|,).*")) {
                showFolderError("不正な名前なので別の名前を再入力してください",OP_COPY);
//        JOptionPane.showMessageDialog(
//          SwingUtilities.getWindowAncestor(tree),
//          "不正な名前なので、別の名前を再入力してください"
//          ,"フォルダのコピー",JOptionPane.ERROR_MESSAGE );
                newName = currentNode.toString();
                continue;
            } else break;
            }
        List<TreePath> sourceList = new ArrayList<TreePath>();
        FolderTreeNode newNode = new FolderTreeNode( newName );
        targetTreePath = targetTreePath.pathByAddingChild(newNode);
        //コピー元フォルダのパスのリストと、コピー用の複製したノードを作成する。
        for(Enumeration enu = currentNode.breadthFirstEnumeration(); enu.hasMoreElements(); ) {
            FolderTreeNode n = (FolderTreeNode) enu.nextElement();
            if(n instanceof DustBoxTreeNode) continue; //ごみ箱は除外
            TreeNode [] fullPath = n.getPath();
            sourceList.add(new TreePath(fullPath));
            int size = fullPath.length - (currentNode.getLevel()+1);
            if(size>0) {
                TreeNode [] destPath = new TreeNode[size];
                for(int i=0; i<destPath.length; i++) {
                    destPath[i] = fullPath[ i + (currentNode.getLevel()+1) ];
                }
                DBFactory.addNode(destPath,newNode);
            }
        }
        if(db == null) {
            ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode,
                targetNode,targetNode.getChildCount());
            return;
        }
        if(db.copyFolder(currentTreePath,targetTreePath)) {
            ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode,
                targetNode,targetNode.getChildCount());
            tree.setSelectionPath(targetTreePath);    // 移動先フォルダにフォーカスを移動し
            tree.scrollPathToVisible(targetTreePath); //そのフォルダの位置にスクロール
        }
    }//GEN-LAST:event_copyFolderMenuItemActionPerformed

    private void renameFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameFolderMenuItemActionPerformed
        if(isBusyForTree(OP_RENAME)) return;
        FolderTreeNode node = (FolderTreeNode)tree.getSelectionPath().getLastPathComponent();
        TreeNode [] path = node.getPath();
        TreePath treePath = new TreePath(path);
        tree.startEditingAtPath( treePath );
    }//GEN-LAST:event_renameFolderMenuItemActionPerformed

    private void moveFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveFolderMenuItemActionPerformed
        if(isBusyForTree(OP_MOVE)) return;
        TreePath targetTreePath = FolderSelectDialog.showDialog(chartDesktop.getFrame(),
            (TreeNode)tree.getModel().getRoot(),tree.getSelectionPath(),this.getTree(chartDesktop));
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        if(targetTreePath == null) return;
        TreePath currentTreePath = tree.getSelectionPath();
        FolderTreeNode currentNode,targetNode;
        currentNode = (FolderTreeNode)currentTreePath.getLastPathComponent();
        targetNode = (FolderTreeNode)targetTreePath.getLastPathComponent();
        if(targetNode.isNodeAncestor(currentNode)) {
            showFolderError("親を子に移動することはできません。",OP_MOVE);
            return;
        }
        //子を親に移動しても無効
        for(Enumeration enu = targetNode.children(); enu.hasMoreElements(); ) {
            FolderTreeNode childNode = (FolderTreeNode)enu.nextElement();
            if(childNode.toString().equals( currentNode.toString() )) {
                showFolderError("無効な移動です。",OP_MOVE);
                return;
            }
        }
        if(db == null) {
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(currentNode);
            ((DefaultTreeModel)tree.getModel()).insertNodeInto(currentNode,targetNode,targetNode.getChildCount());
            return;
        }
        boolean ok = false;
        if(targetNode instanceof DustBoxTreeNode) {
            //ごみ箱への移動は削除
            ok = db.removeFolder(currentTreePath);
            treeModel.removeNodeFromParent(currentNode);
        } else if(db.moveFolder(currentTreePath,targetTreePath)) {
            //ごみ箱以外なら移動
            treeModel.removeNodeFromParent(currentNode);
            treeModel.insertNodeInto(currentNode,targetNode,
                targetNode.getChildCount());
            TreePath tp = new TreePath(currentNode.getPath());
            tree.setSelectionPath(tp); // 移動先フォルダにフォーカスを移動し
            tree.scrollPathToVisible(tp); //そのフォルダの位置にスクロール
        }
    }//GEN-LAST:event_moveFolderMenuItemActionPerformed

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        TreePath targetTreePath = FolderSelectDialog.showDialog(chartDesktop.getFrame(),
            (TreeNode)tree.getModel().getRoot(),currentTreePath,this.getTree(chartDesktop));
        if(targetTreePath == null) return;
        if(targetTreePath.equals(currentTreePath)) {
            showDataError("同じフォルダにはコピーできません。",OP_COPY);
            return;
        }
        if(targetTreePath.getLastPathComponent() instanceof DustBoxTreeNode) {
            showDataError("ごみ箱へコピーはできません。",OP_COPY);
            return;
        }
        if(db == null) return;
        if(db.copy(getSelectedNatals(),targetTreePath)) {
            showList(targetTreePath);
            tree.setSelectionPath(targetTreePath); // ツリーの選択も移動
            tree.scrollPathToVisible(targetTreePath);
        }
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void removeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMenuItemActionPerformed
        if(isBusyForTable(OP_REMOVE)) return;
        if(db == null) {
            removeRows();
        } else {
            if(db.remove(getSelectedNatals(),currentTreePath)) {
                showList(tree.getSelectionPath());
            }
        }
    }//GEN-LAST:event_removeMenuItemActionPerformed

    private void moveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveMenuItemActionPerformed
        if(isBusyForTable(OP_MOVE)) return;
        TreePath targetTreePath = FolderSelectDialog.showDialog(chartDesktop.getFrame(),
            (TreeNode)tree.getModel().getRoot(),currentTreePath,this.getTree(chartDesktop));
        if(targetTreePath == null) return;
        if(currentTreePath.equals(targetTreePath)) {
            showDataError("同じフォルダ内に移動はできません。",OP_MOVE);
            return;
        }
        if(db == null) return;
        if(db.move(getSelectedNatals(),currentTreePath,targetTreePath)) {
            showList(targetTreePath);
            tree.setSelectionPath(targetTreePath); // ツリーの選択も移動
            tree.scrollPathToVisible(targetTreePath);
        }
    }//GEN-LAST:event_moveMenuItemActionPerformed

    private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
        if(isBusyForTable(OP_EDIT)) return;
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        Natal upocc = null;
        if( row < 0 ) return;
        Natal temp = (Natal)model.getValueAt(row,0);
        //DBフレームのテーブルに表示してあっても、その後のユーザー操作で、編集されて
        //しまっている場合がある。だからDBから読み直す。
        Natal occ = db.getNatal(temp.getId());
        upocc = DataInputDialog.showEditDialog(this,occ,tree.getSelectionPath());
        if(upocc != null) {
            if(db != null) {
                db.updateNatal(upocc);
                showList(tree.getSelectionPath()); //テーブル表示更新
            }
        }
    }//GEN-LAST:event_editMenuItemActionPerformed

    private void newChartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChartMenuItemActionPerformed
        chartDesktop.openChartPane(getSelectedList());
    }//GEN-LAST:event_newChartMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aspcalcMenuItem;
    private javax.swing.JMenuItem clearDustboxMenuItem;
    private javax.swing.JMenuItem copyFolderMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JPopupMenu dustboxPopupMenu;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem makeFolderMenuItem;
    private javax.swing.JMenuItem moveFolderMenuItem;
    private javax.swing.JMenuItem moveMenuItem;
    private javax.swing.JMenuItem newChartMenuItem;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem registEventMenuItem;
    private javax.swing.JMenuItem registNatalMenuItem;
    private javax.swing.JMenuItem removeFolderMenuItem;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JMenuItem renameFolderMenuItem;
    private javax.swing.JMenuItem returnMenuItem;
    private javax.swing.JMenuItem searchMenuItem;
    private javax.swing.JMenu sendAddMenu;
    private javax.swing.JMenu sendMenu;
    private javax.swing.JMenuItem sendTransitMenuItem;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JMenuItem synodicMenuItem;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JTree tree;
    private javax.swing.JPopupMenu treePopupMenu;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables

}
