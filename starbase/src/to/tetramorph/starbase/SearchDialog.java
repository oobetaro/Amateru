/*
 * SearchDialog.java
 *
 * Created on 2006/12/19, 3:26
 */

package to.tetramorph.starbase;

import to.tetramorph.starbase.util.WindowMoveHandler;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilePermission;
import java.net.URL;
import java.security.Permission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.PropertyPermission;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.SearchOption;
import to.tetramorph.starbase.module.SearchModulePanel;
import to.tetramorph.starbase.util.GuestDatabase;
import static java.lang.System.getProperty;
import javax.swing.AbstractAction;
import to.tetramorph.starbase.lib.SearchResult;
import to.tetramorph.starbase.lib.SearchResultReceiver;
import to.tetramorph.util.IconLoader;
import to.tetramorph.util.ParentWindow;

/**
 * 検索キー入力ダイアログ。
 * 2011-07-29 レジストリの使用をやめた。
 * @author  大澤義孝
 */
class SearchDialog extends javax.swing.JDialog {

    //複数の入力フォームのうち、選択されている入力フォーム
    private SearchModulePanel selectedModulePanel;
    //検索時の性別や日付の制限をオプションで指定するためのパネル
    private SearchOptionPanel optionPanel;
    //検索を開始するフォルダ
    private TreePath currentTreePath = null;

    private SearchResultReceiver resultReceiver;
    private Connection con = GuestDatabase.getInstance().getConnection();

    /**
     * 検索ダイアログを作成する。
     * @param parent 親となるフレーム
     * @param resultReceiver 検索結果を出力するオブジェクト
     */
    public SearchDialog( Frame parent, SearchResultReceiver resultReceiver ) {
        super(parent, false); //モーダレス
        this.resultReceiver = resultReceiver;
        initComponents();
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        if(System.getProperty("java.version").startsWith("1.6")) {
//            Image img = IconLoader.getImage("/resources/niwatori2.png");
            setIconImage(AppIcon.TITLE_BAR_ICON);
        }
        getRootPane().setDefaultButton(searchButton);
        optionPanel = new SearchOptionPanel();
        loadSearchModules();
        selectedModulePanel = (SearchModulePanel)menuComboBox.getItemAt(0);
        menuComboBox.setSelectedIndex(0);
        menuComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                searchTypeSelectActionPerformed();
            }
        });
        mainPanel.add(selectedModulePanel,BorderLayout.CENTER);
        //「その他オプション」の状態を再現する。フレームのサイズに影響している。
        if ( Config.usr.getBoolean("SearchFrame.OPTION",false) ) {
            optionCheckBox.doClick();
        }
        WindowMoveHandler winmove =
            new WindowMoveHandler("SearchFrame.BOUNDS",this);
        addComponentListener(winmove);
        setPreferredSize(null);
        pack();
        winmove.setBounds();
    }
    //検索モジュールのロード
    void loadSearchModules() {
        String moddir = System.getProperty("app.smod"); //Config.usr.getProperty( "SearchModule.dir","" );
        if ( moddir.isEmpty() ) throw
            new IllegalStateException("'app.smod' property not found.");
        try {
            File modFile = new File( moddir );
            String[] files = modFile.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".jar")) {
                    File file = new File( modFile, files[i] );
                    JarFile jarFile = new JarFile(file);
                    Manifest mf = jarFile.getManifest();
                    Attributes att = mf.getMainAttributes();
                    URL url = file.getCanonicalFile().toURI().toURL();
                    ModuleClassLoader loader = new ModuleClassLoader(
                        new URL[] { url } );
                    for ( int j = 1; ;j++ ) {
                        String className = att.getValue("SearchModule-Class" + j);
                        if ( className == null ) break;
                        System.out.println("Load SearchModule: " + className);
                        Class plugin =
                            loader.loadModule( className, getPermissions() );
                        SearchModulePanel smodpane =
                            (SearchModulePanel)plugin.newInstance();
                        menuComboBox.addItem(smodpane);
                        //Preference pf = new Preference();
                        //PrefUtils.copy(Conf.data, pf);
                        //smodpane.init(pf);
                        smodpane.init( Config.usr ); //モジュール初期化
                    }
                }
            }
        } catch ( ClassNotFoundException e ) {
            System.out.println("SearchModule Not Found : " + e);
        } catch ( Exception ex ) {
            Logger.getLogger(SearchDialog.class.getName())
                    .log(Level.SEVERE,null,ex);
        }
    }

    private static Permission [] getPermissions() {
        String sp = File.separator;
        String tempPath = getProperty("app.home") + sp + "temp" + sp + "-";
        String swePath  = getProperty("swe.path") + sp + "-";
//        System.out.println("##### tempPath = " + tempPath );
//        System.out.println("##### swePath  = " + swePath );
        Permission [] perms = new Permission [] {
            new FilePermission( tempPath, "read,write,delete" ),
            new FilePermission( swePath,  "read" ),
            new PropertyPermission( "swe.path",      "read" ),
            new PropertyPermission( "DefaultTime",   "read" ),
            new PropertyPermission( "app.topounit",  "read" ),
            new PropertyPermission( "app.angleunit", "read" )
        };
        return perms;
    }

    // *1 JWSの場合だと、URLClassLoaderだけでは、うまくモジュールを見つける事が
    //    できない。リソース取得用のクラスローダーを親として与えて、
    //    URLClassLoaderを作成しなければならない。

    /**
     * 検索対象のフォルダ(ツリー上で選択されているフォルダ)をTextFieldに表示する。
     * OccasionEditorでツリーがセレクトされると呼び出され、カレントパスが通達される。
     * @param path カレントパス
     */
    public void setTargetPath(TreePath path) {
        currentTreePath = path;
        pathTextField.setText( DBFactory.getPathString(path) );
    }

    /**
     * 検索フォームウィンドウを画面に出す。
     */
    public void showFrame(TreePath currentTreePath) {
        this.currentTreePath = currentTreePath;
        // DefaultButtonの設定はdisposeされたときクリアされてしまうので、
        // 再セットが必要。
        getRootPane().setDefaultButton(searchButton);
        setTargetPath(currentTreePath);
        pack();
        setVisible(true);
        toFront();
    }

    // 検索開始日付をjava.sql.Dateで返す。
    private GregorianCalendar getBeginDate() {
        return  (GregorianCalendar)optionPanel.dateFTextField1.getValue();
    }

    // 検索終了日付をjava.sql.Dateで返す。
    private GregorianCalendar getEndDate() {
        return  (GregorianCalendar)optionPanel.dateFTextField2.getValue();
    }

    private void searchTypeSelectActionPerformed() {
        LayoutManager layout = mainPanel.getLayout();
        //ﾚｲｱｳﾄﾏﾈｰｼﾞｬから検索ﾓｼﾞｭｰﾙを削除
        layout.removeLayoutComponent(selectedModulePanel);
        mainPanel.remove(selectedModulePanel); //ﾒｲﾝﾊﾟﾈﾙからも削除
        //ｾﾚｸﾄされた検索ﾓｼﾞｭｰﾙを取得して
        selectedModulePanel = (SearchModulePanel)menuComboBox.getSelectedItem();
        mainPanel.add(selectedModulePanel,BorderLayout.CENTER); //ﾒｲﾝﾊﾟﾈﾙに追加
        setPreferredSize(null);
        pack();
        repaint();
        validate();
    }
    /**
     * 検索対象が何件あるかを返す。
     * @param option 対象フォルダやその他の条件を指定したオブジェクト
     * @return 件数。エラーがおきたときは-1を返す。
     */
    int count( SearchOption option ) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) ");
            sb.append("FROM OCCASION,TREEPATH ");
            sb.append("WHERE TREEPATH.ID = OCCASION.ID AND ");
            sb.append( option.getExpression());
            Connection c = option.getConnection();
            ps = c.prepareStatement(sb.toString());
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch ( SQLException e ) {
            Logger.getLogger(SearchDialog.class.getName())
                    .log(Level.SEVERE,null,e);
        } finally {
            try { rs.close(); } catch(Exception ex) { }
            try { ps.close(); } catch(Exception ex) { }
        }
        return -1;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        northPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        javax.swing.JPanel comboPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        menuComboBox = new javax.swing.JComboBox();
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        timeCheckBox = new javax.swing.JCheckBox();
        subFolderCheckBox = new javax.swing.JCheckBox();
        optionCheckBox = new javax.swing.JCheckBox();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JLabel searchLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("検索");
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        mainPanel.setOpaque(false);
        mainPanel.setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.BorderLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/search.gif"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 1, 1));
        northPanel.add(jLabel2, java.awt.BorderLayout.WEST);

        comboPanel.setAlignmentY(1.0F);

        jLabel1.setText("検索方式");
        comboPanel.add(jLabel1);

        comboPanel.add(menuComboBox);

        northPanel.add(comboPanel, java.awt.BorderLayout.EAST);

        mainPanel.add(northPanel, java.awt.BorderLayout.NORTH);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        timeCheckBox.setText("時刻不明のデータは除外する");
        timeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        timeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(timeCheckBox, gridBagConstraints);

        subFolderCheckBox.setSelected(true);
        subFolderCheckBox.setText("サブフォルダも検索");
        subFolderCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        subFolderCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(subFolderCheckBox, gridBagConstraints);

        optionCheckBox.setText("その他のオプション");
        optionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        optionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(optionCheckBox, gridBagConstraints);

        buttonPanel.add(jPanel1, java.awt.BorderLayout.WEST);

        jPanel2.setLayout(new java.awt.BorderLayout());

        searchButton.setText("検索");
        searchButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        jPanel2.add(searchButton, java.awt.BorderLayout.SOUTH);

        buttonPanel.add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        searchLabel.setText("検索ﾌｫﾙﾀﾞ");
        jPanel3.add(searchLabel);

        pathTextField.setColumns(20);
        pathTextField.setEditable(false);
        pathTextField.setBorder(null);
        pathTextField.setOpaque(false);
        jPanel3.add(pathTextField);

        buttonPanel.add(jPanel3, java.awt.BorderLayout.NORTH);

        mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents
  //検索開始
  private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    String title = "検索オプションのエラー";
    if((! optionPanel.maleCheckBox.isSelected()) &&
      (! optionPanel.femaleCheckBox.isSelected()) &&
      (! optionPanel.noneCheckBox.isSelected())) {
      JOptionPane.showMessageDialog(this,"性別の項目を一つは選択してください",
        title,JOptionPane.ERROR_MESSAGE);
      return;
    }
    if((! optionPanel.natalCheckBox.isSelected()) &&
      (! optionPanel.orgCheckBox.isSelected()) &&
      (! optionPanel.eventCheckBox.isSelected()) &&
      (! optionPanel.compositCheckBox.isSelected())
      ) {
      JOptionPane.showMessageDialog(this,"データタイプを一つは選択してください",
        title,JOptionPane.ERROR_MESSAGE);
      return;
    }
    if(getBeginDate() != null && getEndDate() != null) {
      if(getBeginDate().after(getEndDate())) {
        JOptionPane.showMessageDialog(this,"日付は昇順に指定してください",
          title,JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    //検索オプション情報を引き渡す
    //treePathやSearchResultTableなども渡すことになるだろう
    SearchOption option = new SearchOption();
    option.setBeginDate(getBeginDate());
    option.setEndDate(getEndDate());
    option.setOption(SearchOption.MALE,optionPanel.maleCheckBox.isSelected());
    option.setOption(SearchOption.FEMALE,optionPanel.femaleCheckBox.isSelected());
    option.setOption(SearchOption.NONE,optionPanel.noneCheckBox.isSelected());
    option.setOption(SearchOption.NATAL,optionPanel.natalCheckBox.isSelected());
    option.setOption(SearchOption.ORG,optionPanel.orgCheckBox.isSelected());
    option.setOption(SearchOption.EVENT,optionPanel.eventCheckBox.isSelected());
    option.setOption(SearchOption.COMPOSIT,optionPanel.compositCheckBox.isSelected());
    //
    option.setOption(SearchOption.OTHER_OPTIONS,optionCheckBox.isSelected());
    option.setOption(SearchOption.EXCLUDE_TIME_UNCERTAINTY,timeCheckBox.isSelected());
    option.setOption(SearchOption.SEARCH_SUB_FOLDERS,subFolderCheckBox.isSelected());
    //
    System.out.println("currentTreePath = " + currentTreePath);
    option.setCurrentTreePath(currentTreePath);
    //option.setResultReceiver( resultReceiver );
    option.setConnection(con);
    System.out.println(option.toString());
    if ( selectedModulePanel.begin() ) {
        SearchResult searchResult = selectedModulePanel.search(option);
        searchResult.setCount(count(option));
        resultReceiver.write(searchResult);
    }
  }//GEN-LAST:event_searchButtonActionPerformed

  private void optionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionCheckBoxActionPerformed
    if(optionCheckBox.isSelected()) {
      getContentPane().add(optionPanel);
      Config.usr.getBoolean("SearchFrame.OPTION",true);
    } else {
      LayoutManager layout = getContentPane().getLayout();
      layout.removeLayoutComponent(optionPanel);
      getContentPane().remove(optionPanel);
      Config.usr.setBoolean("SearchFrame.OPTION",false);
    }
    //推奨サイズを指定しないことでサイズをpack()に決定させる
    setPreferredSize(null);
    pack();
    repaint();
    validate();
  }//GEN-LAST:event_optionCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox menuComboBox;
    private javax.swing.JPanel northPanel;
    private javax.swing.JCheckBox optionCheckBox;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JButton searchButton;
    private javax.swing.JCheckBox subFolderCheckBox;
    private javax.swing.JCheckBox timeCheckBox;
    // End of variables declaration//GEN-END:variables

}
