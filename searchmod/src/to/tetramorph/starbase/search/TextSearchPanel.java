/*
 * TextSearchPanel.java
 *
 * Created on 2006/07/21, 17:52
 */

package to.tetramorph.starbase.search;

import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.module.SearchModulePanel;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.util.Preference;

/**
 * テキスト検索を行うためのパネル。
 */
public class TextSearchPanel extends SearchModulePanel {
    
    /** Creates new form TextSearchPanel */
    JCheckBox [] fieldCheckBox = new JCheckBox[6];
    private static final String [] fieldNames =
    {"NAME2","KANA2","JOB","MEMO","NOTE","PLACENAME" };
    //JButton searchButton;
    public TextSearchPanel() {
        super();
        //this.searchButton = searchButton;
        initComponents();
        fieldCheckBox[0] = nameCheckBox;
        fieldCheckBox[1] = kanaCheckBox;
        fieldCheckBox[2] = jobCheckBox;
        fieldCheckBox[3] = memoCheckBox;
        fieldCheckBox[4] = noteCheckBox;
        fieldCheckBox[5] = placeCheckBox;
    }
    public void init(Preference configData) { }
    /**
     * 検索開始の前に呼び出され入力パラメターに不正がある場合はfalseを返す。
     */
    public boolean begin() {
        String title = "テキスト検索のエラー";
        String key = searchTextField.getText().trim();
        if(key.length()==0) {
            JOptionPane.showMessageDialog(this,"一文字は入力が必要です",title,
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        boolean selected = false;
        for(JCheckBox cb : fieldCheckBox ) {
            if(cb.isSelected()) {
                selected = true; break;
            }
        }
        if(! selected ) {
            JOptionPane.showMessageDialog(this,"検索対象を一つは選択してください",
                title,JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    void count( SearchOption option ) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) ");
        sb.append("FROM OCCASION,TREEPATH ");
        sb.append("WHERE TREEPATH.ID = OCCASION.ID AND ");
        sb.append( option.getExpression());
        Connection con = option.getConnection();
        PreparedStatement ps = con.prepareStatement(sb.toString());
        ResultSet rs = ps.executeQuery();
        rs.next();
        System.out.println(sb.toString());
        int count = rs.getInt(1);
        System.out.println("検索対象総数 = " + count);
        rs.close();
        ps.close();
    }

    @Override
    public SearchResult search( SearchOption option ) {
        try {
            count(option);
            Connection con = option.getConnection();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT TREEPATH.PATH,OCCASION.*,");
            // 名前とよみがなは、空白を除去し、英字は小文字にして検索
            sb.append("REPLACE(LOWER(OCCASION.NAME),' ','') AS NAME2,");
            sb.append("REPLACE(LOWER(OCCASION.KANA),' ','') AS KANA2 ");
            sb.append("FROM OCCASION,TREEPATH ");
            sb.append("WHERE TREEPATH.ID = OCCASION.ID AND ");
            sb.append( option.getExpression() );
            sb.append("AND (");
            for ( int i = 0; i < fieldCheckBox.length; i++) {
                if ( fieldCheckBox[i].isSelected() )
                    //入力されたキーワードも空白除去し、英字は小文字で検索
                    sb.append(fieldNames[i] + " LIKE REPLACE(LOWER(?),' ','') OR ");
            }
            sb.delete( sb.length() - 4, sb.length() );
            sb.append(")");
            PreparedStatement ps = con.prepareStatement(sb.toString());
            String key = "%"+searchTextField.getText()+"%";
            for ( int i = 0, j = 1; i < fieldCheckBox.length; i++ ) {
                if ( fieldCheckBox[i].isSelected() ) ps.setString( j++, key );
            }
            System.out.println( "SQL : " );
            System.out.println( ps.toString() );
            ResultSet rs = ps.executeQuery();
            List<Natal> list = new ArrayList<Natal>();
            while ( rs.next() ) {
                Natal o = new Natal();
                o.setParams( rs );
                o.setPath( rs.getString("PATH") );
                list.add( o );
            }
            rs.close();
            ps.close();
            String tabName = searchTextField.getText().split(" ")[0];
            String title = searchTextField.getText().trim();
//            option.getResultReceiver().write( new SearchResult(
//                                                       list,
//                                                       tabName,
//                                                       title,
//                                                       option.getCurrentPath(),
//                                                       toString()) );
            return new SearchResult(list,
                                      tabName,
                                      title,
                                      option.getCurrentPath(),
                                      toString());

        } catch ( SQLException e ) { e.printStackTrace(); }
        return null;
    }
    @Override
    public String toString() {
        return "テキスト検索";
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

    jLabel1 = new javax.swing.JLabel();
    searchTextField = new javax.swing.JTextField();
    nameCheckBox = new javax.swing.JCheckBox();
    kanaCheckBox = new javax.swing.JCheckBox();
    jobCheckBox = new javax.swing.JCheckBox();
    memoCheckBox = new javax.swing.JCheckBox();
    placeCheckBox = new javax.swing.JCheckBox();
    noteCheckBox = new javax.swing.JCheckBox();
    jLabel2 = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("\u691c\u7d22\u30ad\u30fc\u30ef\u30fc\u30c9");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
    add(jLabel1, gridBagConstraints);

    searchTextField.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        searchTextFieldKeyPressed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(searchTextField, gridBagConstraints);

    nameCheckBox.setSelected(true);
    nameCheckBox.setText("\u540d\u524d");
    nameCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    nameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(nameCheckBox, gridBagConstraints);

    kanaCheckBox.setSelected(true);
    kanaCheckBox.setText("\u306a\u307e\u3048");
    kanaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    kanaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(kanaCheckBox, gridBagConstraints);

    jobCheckBox.setText("\u8077\u696d");
    jobCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    jobCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(jobCheckBox, gridBagConstraints);

    memoCheckBox.setText("\u30e1\u30e2");
    memoCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    memoCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(memoCheckBox, gridBagConstraints);

    placeCheckBox.setText("\u5730\u540d");
    placeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    placeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(placeCheckBox, gridBagConstraints);

    noteCheckBox.setText("\u30ce\u30fc\u30c8");
    noteCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    noteCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    add(noteCheckBox, gridBagConstraints);

    jLabel2.setText("\u691c\u7d22\u5bfe\u8c61");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 5);
    add(jLabel2, gridBagConstraints);

  }// </editor-fold>//GEN-END:initComponents
//テキストフィールドで改行キーが押されたら検索開始
  private void searchTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchTextFieldKeyPressed
      if ( evt.getKeyCode() == KeyEvent.VK_ENTER ) {
          //searchButton.doClick();
          // 検索ボタンをdefaultButtonとして定義したので、このメソッドは不要になった
      }
  }//GEN-LAST:event_searchTextFieldKeyPressed
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox jobCheckBox;
  private javax.swing.JCheckBox kanaCheckBox;
  private javax.swing.JCheckBox memoCheckBox;
  private javax.swing.JCheckBox nameCheckBox;
  private javax.swing.JCheckBox noteCheckBox;
  private javax.swing.JCheckBox placeCheckBox;
  private javax.swing.JTextField searchTextField;
  // End of variables declaration//GEN-END:variables
  
}
