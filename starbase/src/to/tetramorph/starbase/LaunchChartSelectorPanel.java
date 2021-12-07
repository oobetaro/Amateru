/*
 * LaunchChartSelectorPanel.java
 *
 * Created on 2008/04/17, 1:30
 */

package to.tetramorph.starbase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import to.tetramorph.starbase.module.ChartModuleMode;

/**
 * チャートを新しく開くとき、どのチャートモジュール使うかを事前に指定おくための
 * パネル。モジュールの中には、動作モードを複数持つものがあるが、モジュールを
 * 起動する際、どのモードで起動するかも指定できるようになっている。
 * 2011-07-29 レジストリの使用を廃止。
 * @author  大澤義孝
 */
public class LaunchChartSelectorPanel extends javax.swing.JPanel {
    /* モジュールごとに、モジュールモード(複数)を格納する。
     * モジュール0はa,b,c,のモードがある。
     * モジュール1はx,yのモード。というように。
     */
    List< List<ChartModuleMode> >
        itemList = new ArrayList< List<ChartModuleMode> >();
    //チャートモジュール名一覧(NPT三重円,レポート等)
    List<String> moduleNameList;
    //
    ChartsComboBoxHandler
                    chartsComboBoxHandler = new ChartsComboBoxHandler();
    ModeComboBoxHandler
                    modeComboBoxHandler = new ModeComboBoxHandler();
    /**
     * オブジェクトを作成する。
     */
    public LaunchChartSelectorPanel() {
        initComponents();
    }

    /**
     * 与えられたチャートペインから、チャートモジュール名を抽出して、
     * コンボボックスに登録。チャートペインはタブペインに入れるものではなく、
     * ダミーを与えること。
     */
    void setChartPane( ChartPane chartPane ) {
        ChartPane cp = chartPane;
        moduleNameList = cp.getModuleNameList();
        int size = moduleNameList.size();
        //モードメニューを引数から取得して、モード選択コンボに登録
        for ( int i=0; i < size; i++ ) {
            cp.selectModule(i);
            ChartModuleMode [] modes = cp.getSelectedModuleModes();
            if ( modes == null ) {
                itemList.add( null );
                continue;
            }
            List<ChartModuleMode> modeList = new ArrayList<ChartModuleMode>();
            //for ( ChartModuleMode m : modes ) modeList.add( m );
            modeList.addAll(Arrays.asList(modes));
            itemList.add( modeList );
        }
        setupChartsCombo( moduleNameList );
        int n = Config.usr.getInteger( "ChartModuleNumber", 0 );
        setupModeCombo( n, itemList );
    }

    /**
     * 初期チャートモジュールコンボボックスのリスナ。
     * モジュールが選択されたら、そのモジュールの仕様リストを仕様選択コンボに
     * セットする。選択モジュール番号をプロパティに登録する。
     */
    class ChartsComboBoxHandler implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            int n = chartsComboBox.getSelectedIndex();
            setupModeCombo( n, itemList );
            Config.usr.setInteger( "ChartModuleNumber", n );
        }
    }

    /**
     * モード選択コンボボックスのリスナ。
     * モードが選択されたら、仕様番号をプロパティに登録する。このプロパティは、
     * モジュールの数だけ用意されるので配列で保管する。
     */
    class ModeComboBoxHandler implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent evt ) {
            int n = modeComboBox.getSelectedIndex();
            //モードの選択番号をプロパティに保管
            String key = "SelectedModuleModes"; //"ChartsSpecificNumbers";
            int [] temp = Config.usr.getIntArray( key );
            int [] array = new int[ itemList.size() ];
            if ( temp != null ) {
                int len = temp.length <= array.length ?
                    temp.length : array.length;
                System.arraycopy( temp, 0, array, 0, len );
            }
            int i = getSelectedChartModuleIndex();
            if ( i >= 0 ) array[ i ] = n;
            Config.usr.setIntArray( key, array );
        }
    }

    /**
     * コンボボックスで選択されてるモジュール番号を返す。
     */
    public int getSelectedChartModuleIndex() {
        return chartsComboBox.getSelectedIndex();
    }

    /**
     * 仕様コンボボックスで選択されている設定名を返す。
     */
    public String getSelectedSpecificName() {
        return ( String ) modeComboBox.getSelectedItem();
    }

    /**
     * 選択されているChartModuleModeを返す。
     */
    public ChartModuleMode getSelectedModuleMode() {
        int i = chartsComboBox.getSelectedIndex();
        List<ChartModuleMode> list = itemList.get(i);
        if ( list == null ) return null;
        return list.get( modeComboBox.getSelectedIndex() );
    }

    /**
     * チャート選択コンボボックスにアイテムとリスナを登録。
     */
    private void setupChartsCombo( List<String> moduleNameList ) {
        if ( moduleNameList == null )
            throw new IllegalArgumentException("null禁止");
        if ( moduleNameList.isEmpty() ) return;
        chartsComboBox.removeActionListener( chartsComboBoxHandler );
        chartsComboBox.removeAllItems();
        for ( String str : moduleNameList )
            chartsComboBox.addItem( str );
        int n = Config.usr.getInteger( "ChartModuleNumber", 0 );
        if ( n >= itemList.size() ) n = 0;
        chartsComboBox.setSelectedIndex( n );
        chartsComboBox.addActionListener( chartsComboBoxHandler );
    }

    /**
     * モード選択コンボボックスにアイテムを登録。
     * コンボのアイテムを、呼ばれるたびに抹消して再登録するが、その処理に入る前に
     * リスナを削除して、処理がすめば再登録している。アイテム登録のときリスナが
     * 呼び出されるため。
     * モジュールによってはモードを持たないものもあり、そのときはコンボを
     * Disenabledにする。
     * @param n チャートモジュール番号
     * @param itemList 「モジュールごとの仕様リスト」のリスト
     */
    private void setupModeCombo( int n,
                                       List< List<ChartModuleMode> > itemList) {
        if ( itemList == null )
            throw  new IllegalArgumentException("null禁止");
        modeComboBox.removeActionListener( modeComboBoxHandler );
        modeComboBox.removeAllItems();
        List<ChartModuleMode> list = itemList.get( n );
        if ( list == null || list.isEmpty() ) {
            modeComboBox.addItem( "無効" );
            modeComboBox.setEnabled( false );
            return;
        }
        modeComboBox.setEnabled( true );
        for ( int i=0; i < list.size(); i++ ) {
            ChartModuleMode mode = list.get(i);
            modeComboBox.addItem( mode.getTitle() );
        }
        //モジュール番号をプロパティから読み出して対応するコンボを選択
        int [] array = new int[ itemList.size() ];
        array = Config.usr.getIntArray( "SelectedModuleModes", array );
        int m = array[ n ];
        if ( m >= modeComboBox.getItemCount() ) m = 0;
        modeComboBox.setSelectedIndex( m );
        modeComboBox.addActionListener( modeComboBoxHandler );
    }

    /*
     * "SelectedModuleModes"の値は、"3,0,1,0,0,0,,,"といった複数の値からなっていて、
     * 各モジュールの何番目のモードが選択されているかを表現している。
     * 先頭は3だが、0番モジュールのモード3が選択されている、つぎの次は
     * 2番モジュールのモード1が選択されているという構造。
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JLabel jLabel1;

        jLabel1 = new javax.swing.JLabel();
        chartsComboBox = new javax.swing.JComboBox();
        modeComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jLabel1.setText("\u65b0\u898f\u30c1\u30e3\u30fc\u30c8\u306e\u30c7\u30d5\u30a9\u30eb\u30c8");
        add(jLabel1);

        chartsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        chartsComboBox.setToolTipText("\u65b0\u898f\u30c1\u30e3\u30fc\u30c8\u3092\u958b\u304f\u3068\u304d\u3001\u3069\u306e\u30c1\u30e3\u30fc\u30c8\u56f3\u3092\u4f7f\u7528\u3059\u308b\u304b\u3092\u6307\u5b9a");
        add(chartsComboBox);

        modeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        modeComboBox.setToolTipText("\u8907\u6570\u306e\u56f3\u3092\u30ab\u30d0\u30fc\u3057\u3066\u3044\u308b\u5834\u5408\u306f\u3001\u3055\u3089\u306b\u3069\u306e\u56f3\u3092\u4f7f\u7528\u3059\u308b\u304b\u6307\u5b9a");
        add(modeComboBox);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox chartsComboBox;
    private javax.swing.JComboBox modeComboBox;
    // End of variables declaration//GEN-END:variables

}
