/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ActionSelectionPanel.java
 *
 * Created on 2008/12/15, 19:52:40
 */

package to.tetramorph.starbase.dict;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


/**
 * 文書編集ダイアログの中で、応答するアクションをコンボボックスで選択するパネル。
 * @author 大澤義鷹
 */
class ActionSelectionPanel extends javax.swing.JPanel {
    Map<String,List<ParamPanel>> panelMap =
            new LinkedHashMap<String,List<ParamPanel>>();
    Book book;
    /** 
     * オブジェクトを作成する。
     */
    public ActionSelectionPanel( Book book ) {
        initComponents();
        this.book = book;
        setupActionComboBox();
        selectAction();
    }
    /**
     * 上段にラベル、下段にコンボボックスやテキストフィールドを入れるパネル。
     * コンストラクタでラベルを指定し、setComponentでコンボ等をセットする。
     */
    private class ParamPanel extends JPanel {
        Component c;
        ParamPanel( JLabel label ) {
            super( new GridLayout(2,1) );
            add( label );
        }
        void setComponent( Component c ) {
            this.c = c;
            add( c );
        }
        Component getComponent() {
            return c;
        }
    }
   /**
    * データ入力などの際に使用する、アクション選択のコンボボックスモデルを返す。
    * パネル内の一番左にあるコンボボックスに値を入力。
    * @return
    */
    private void setupActionComboBox() {
        Vector<ActionItem> v = new Vector<ActionItem>();
        v.add( new ActionItem("void","無し"));
        for ( String action : book.getActionList() ) {
            String title = book.getActionTitle( action, action );
            v.add( new ActionItem( action, title ) );
            List<ParamPanel> panelList = getParamPanels( action );
            panelMap.put( action, panelList );
        }
        actionComboBox.setModel( new DefaultComboBoxModel( v ) );
        //コンボが切り替えられたら、それ用のパネルをactionPanelにセットする。
        actionComboBox.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                selectAction();
            }
        });
    }
    /**
     * コンボボックスで選択されているアクション名に対応するパラメターパネルを
     * 表示する。
     */
    private void selectAction() {
        ActionItem item = (ActionItem)actionComboBox.getSelectedItem();
        setParamsPanel( panelMap.get(item.getAction()) );
    }
    /**
     * ParamPanelのリストを元に、paramsPanelに登録。actionComboBoxの選択により、
     * このパネル内の部品を入れ替える。
     * @param list 挿入するParamPanelのリスト。nullを指定するとパネル内の部品を全削除。
     */
    private void setParamsPanel( List<ParamPanel> list ) {
        //まずパネル内の部品をすべて削除
        for ( Component c : paramsPanel.getComponents() )
                paramsPanel.remove(c);
        if ( list != null ) {             //部品挿入
            for ( ParamPanel p : list )  paramsPanel.add(p);
        }
        paramsPanel.revalidate();
        paramsPanel.repaint();
    }
    /**
     * パラメータ選択用のコンボボックス（複数）を動的に生成し、それをリストに
     * いれて、そのリストをアクション名で引けるようにpanelMapに入れる。
     * @param action アクション名
     * @return ParamPanelのリスト
     */

    private List<ParamPanel> getParamPanels( String action ) {
        DictAction da = book.getAction( action );
        if ( da == null )
            throw new IllegalArgumentException("未定義のアクション");
        List<ParamPanel> results = new ArrayList<ParamPanel>();
        if ( da.getParamCount() == 0 ) return results;
        //DictActionのパラメターズの各中身を選択できるコンボを作成して登録
        for ( Iterator<String> i = da.keyIterator(); i.hasNext(); ) {
            String key = i.next();
            JLabel label = new JLabel( book.getActionTitle( action,key ) );
            label.setHorizontalAlignment( SwingConstants.LEADING );
            ParamPanel panel = new ParamPanel(label);
            if ( key.equals("keyword")) {
                JTextField tf = new JTextField();
                tf.setActionCommand( key );
                panel.setComponent(tf);
            } else {
                JComboBox combo = new JComboBox();
                combo.setActionCommand( key );
                combo.addItem( new ActionItem("void","無視"));
                for ( Iterator<String> param = da.paramIterator( key );
                                                     param.hasNext(); ) {
                    String pval = param.next();
                    //ActionItem item = new ActionItem( pval, db.getActionTitle( action, pval ) );
                    combo.addItem( new ActionItem(
                                   pval, book.getActionTitle( action, pval ) )  );
                }
                panel.setComponent( combo );
            }
            results.add( panel );
        }
        return results;
    }

    /**
     * 現在表示されているパラメターで新しいページオブジェクトを返す。
     * ただしbody要素は空。
     * また応答するアクション「無し」の場合は、パラメターの無い空の
     * ページオブジェクトを返す。
     * @return
     */
    public DictNode getNewPage( String title ) {
        DictNode p = DictNode.getNewPage(title);
        ActionItem item = (ActionItem)actionComboBox.getSelectedItem();
        if ( item.getAction().equals("void") ) return p;
        List<ParamPanel> list = panelMap.get( item.getAction() );
        if ( list.isEmpty() ) return p;
        p.put( "action", item.getAction() );
        for ( ParamPanel param : list ) {
            Component c = param.getComponent();
            if ( c instanceof JComboBox ) {
                JComboBox cmb = (JComboBox)c;
                ActionItem ai = (ActionItem) cmb.getSelectedItem();
                if ( ! ai.getAction().equals("void") )
                    p.put( cmb.getActionCommand(), ai.getAction() );
            } else if ( c instanceof JTextField) {
                String text = StringUtils.jtrim(  ((JTextField) c).getText()  );
                if ( ! text.isEmpty() )
                    p.put( "keyword", text );
            } else continue;
        }
        return p;
    }
    /**
     * 現在表示されているパラメターを引数で指定されたDictNodeにセットする。
     * 現在のパラメターはすべて消去され、コンボボックスで選択されているものに
     * 置き換えられる。
     * @exception IllegalArgumentException DictNode#isPage()がfalseのノードを
     * 指定したとき。
     */
    public void getParams( DictNode p ) {
        if( ! p.isPage() )
            throw new IllegalArgumentException("ページではない");
        p.clear();
        ActionItem item = (ActionItem)actionComboBox.getSelectedItem();
        if ( item.getAction().equals("void") ) return;
        List<ParamPanel> list = panelMap.get( item.getAction() );
        if ( list.isEmpty() ) return;
        p.put( "action", item.getAction() );
        for ( ParamPanel param : list ) {
            Component c = param.getComponent();
            if ( c instanceof JComboBox ) {
                JComboBox cmb = (JComboBox)c;
                ActionItem ai = (ActionItem) cmb.getSelectedItem();
                if ( ! ai.getAction().equals("void") )
                    p.put( cmb.getActionCommand(), ai.getAction() );
            } else if ( c instanceof JTextField) {
                String text = StringUtils.jtrim(  ((JTextField) c).getText()  );
                if ( ! text.isEmpty() )
                    p.put( "keyword", text );
            } else continue;
        }
    }
    /**
     * 与えられたページ情報から、応答するアクションに関するパラメターを取り出し、
     * このパネルの各コンボボックスがそれぞれパラメターを選択した状態にする。
     * @param page 編集しようとしているノード
     */
    public void setParams( DictNode page ) {
        String action = page.get("action");
        if ( action == null ) action = "void";
        for ( int i = 0; i < actionComboBox.getItemCount(); i++ ) {
            ActionItem item = (ActionItem)actionComboBox.getItemAt(i);
            if ( item.getAction().equals( action ) ) {
                actionComboBox.setSelectedIndex(i);
            }
        }
        List<ParamPanel> list = panelMap.get( action );
        if ( list == null ) return;
        for ( ParamPanel p : list ) {
            if ( p.getComponent() instanceof JComboBox ) {
                JComboBox cb = (JComboBox)p.getComponent();
                System.out.println("cb.actcmd = " + cb.getActionCommand());
                String paramValue = page.get( cb.getActionCommand() );
                if ( paramValue == null ) continue;
                for ( int i = 0; i < cb.getItemCount(); i++ ) {
                    if ( ((ActionItem)cb.getItemAt(i)).getAction().equals(paramValue) )
                        cb.setSelectedIndex(i);
                }
            } else if ( p.getComponent() instanceof JTextField ) {
                JTextField tf = (JTextField)p.getComponent();
                String paramValue = page.get("keyword");
                if ( paramValue == null ) continue;
                tf.setText( paramValue );
            }
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        actionComboBox = new javax.swing.JComboBox();
        paramsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        jLabel1.setText("応答するアクション");
        jPanel1.add(jLabel1);

        actionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(actionComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jPanel1, gridBagConstraints);

        paramsPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(paramsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox actionComboBox;
    private javax.swing.JPanel paramsPanel;
    // End of variables declaration//GEN-END:variables

}
