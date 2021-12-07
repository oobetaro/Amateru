/*
 * DataExporterDialog.java
 *
 * Created on 2007/01/27, 7:12
 */

package to.tetramorph.starbase;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Statement;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.util.CSVFileFilter;
import static to.tetramorph.starbase.lib.BirthFileConst.*;
import to.tetramorph.util.CSVReader;

/**
 * データベースにCSVファイルからデータをインポートする。これをもっとバージョン
 * アップしたものを作りたいと考えていて、そのひな形はPartDataImporterを見よ。
 * @author  大澤義鷹
 */
class DataImporterDialog extends javax.swing.JDialog {
    Statement stmt = null;
    Connection con = null;
    String errorMessage = null;
    File file;
    boolean stop = false; //このフラグがtrueなら読込が中断された事を意味する。
    boolean start = false;
    Frame owner;
    TreePath importPath;
    int max;
    //タイムゾーンのID一覧をハッシュに入れておく。
    static Set<String> timeZoneSet;
    static {
        timeZoneSet = new HashSet<String>();
        String [] ids = TimeZone.getAvailableIDs();
        for(String id: ids) timeZoneSet.add(id);
    }
    /**
     * Creates new form DataExporterDialog
     */
    private DataImporterDialog( Frame owner, File file, TreePath path) {
        super(owner, true);
        this.owner = owner;
        this.importPath = path;
        initComponents();
        setKeyListener();
        this.file = file;
        fileLabel.setText(file.getName());
        pack();
        setLocationRelativeTo(owner);
    }
    ///プログレスバーを更新
    class Bar extends Thread {
        int count;
        Bar(int count) {
            this.count = count;
        }
        public void run() {
            progressBar.setValue(count+1);
        }
    }
    // ESCキーでダイアログを閉じるためのキーリスナ
    void setKeyListener() {
        KeyListener l = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int code = (int)e.getKeyChar();
                if(code == KeyEvent.VK_ESCAPE) canselButton.doClick();
            }
        };
        canselButton.addKeyListener(l);
        startButton.addKeyListener(l);
    }
    // Exporterスレッド終了時に呼び出される。ダイアログの消去。
    void close() {
        if(stop) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html>インポートは中断されました。");
            if(errorMessage != null) {
                sb.append("<br><font color='RED'>エラーメッセージ：<br>");
                sb.append(errorMessage);
                sb.append("</font>");
            }
            sb.append("</html>");
            JOptionPane.showMessageDialog(owner,sb.toString(),
                "インポート",JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(owner,"インポート成功。",
                "インポート",JOptionPane.PLAIN_MESSAGE);
        }
        dispose();
    }
    
    //インポート実行。スタートボタンから、このスレッドがstart()される。
    class Importer extends Thread {
        public void run() {
            max = getFileRows();
            if(max < 0) {
                stop = true;
                close();
            } else loadCSV(max);
        }
    }
    //インポート処理本体
    void loadCSV(int max) {
        CSVReader csvReader = null;
        int c = 2;
        float div = 0;
        progressBar.setMaximum(100);
        div = (float)max / 100f;
        DB db = DBFactory.getInstance();
        String impPath = DBFactory.getPathString(importPath);
        try {
            if ( importPath == null ) { //全インポートのときはテーブルを初期化
                initTable();
            }
            //表示する総数はヘッダーの1行分減らす
            csvReader = new CSVReader(file.getAbsolutePath(),'\t',"UTF-16LE");
            csvReader.readCSV(); //ヘッダー読み飛ばし(BOMも読み飛ばされる)
            int q = 1;
            while(csvReader.ready()) {
                if(stop) break;
                //1行分の列データを取得
                String [] cols = csvReader.readCSV();
                messageLabel.setText(
                    String.format("データ総数 : %d / %d 件",q,max-1) );
                for(int i=0; i<cols.length; i++) cols[i] = cols[i].trim();
                //1行分のNatalデータを作成
                Natal natal = getNatal(cols);
                // 部分インポートのときは登録パスを修正
                String treePath = (importPath == null ) ? 
                    cols[TREEPATH] : graftPath(cols[TREEPATH],impPath );
                // DBに挿入
                db.insertNatal( natal, treePath );
                int count = (int)((float)c / div);
                if(progressBar.getValue() < count)
                    SwingUtilities.invokeLater(new Bar(count)); //ﾌﾟﾛｸﾞﾚｽﾊﾞｰ更新
                c++;
                q++;
            }
        } catch(Exception e) {
            errorMessage = e.getMessage() + "," + c + "行目";
            stop = true;
            e.printStackTrace();
        } finally {
            try { csvReader.close(); } catch (Exception e) { }
        }
        close();
    }
    // 部分インポートのとき、指定されたフォルダパスを接ぎ木する。
    String graftPath(String srcPaths,String importPath) {
        if ( srcPaths.indexOf(",") < 0 ) {
            return importPath + "/" + srcPaths;
        }
        String [] paths = srcPaths.split(",");
        StringBuilder sb = new StringBuilder();
        for ( String s : paths) {
            sb.append( importPath );
            sb.append("/");
            sb.append( s );
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    //CSVﾌｧｲﾙから読みこんだ1行からNatalｵﾌﾞｼﾞｪｸﾄを作って返す。
    Natal getNatal(String cols[]) {
        Natal natal = new Natal();
        natal.setName( cols[NAME] );
        natal.setKana( cols[KANA] );
        natal.setJob( cols[JOB] );
        natal.setMemo( cols[MEMO] );
        natal.setNote( cols[NOTE] );
        natal.setTimestamp( Timestamp.valueOf(cols[TIMESTAMP]) );
        natal.setChartType( cols[CHARTTYPE] ); //変なの入れれば例外が出る
        natal.setHistory( getTransitList(cols[HISTORY]) );
        if (! cols[CHARTTYPE].equals(Natal.COMPOSIT) ) { //NATAL,EVENTの場合
            natal.setDate( cols[DATE], cols[TIME] );
            natal.setPlaceName( cols[PLACENAME] );
            natal.setLongitude( cols[LONGITUDE] );
            natal.setLatitude( cols[LATITUDE] );
            if ( timeZoneSet.contains( cols[TIMEZONE] ) ) {
                natal.setTimeZone( TimeZone.getTimeZone( cols[TIMEZONE] ) );
            } else if ( isGMTOffset( cols[TIMEZONE] ) )  {
                natal.setTimeZone( TimeZone.getTimeZone( cols[TIMEZONE]) );
            } else {
                throw new IllegalArgumentException("Unknown TIMEZONE ID");
            }
            if ( cols[CHARTTYPE].equals(Natal.NATAL) ) {
                if ( cols[GENDER].matches("0|1|2") )
                    natal.setGender(Integer.parseInt(cols[GENDER]));
                else throw new IllegalArgumentException("Illegal GENDER Code");
            }
        } else { //COMPOSITの場合
            natal.setComposit(getTransitList(cols[COMPOSIT]));
        }
        return natal;
    }
    /**
     * 引数で与えられたタイムゾーンオフセット文字列(GMT±hh:mm)の書式を満たす
     * 場合は真を、ちがうなら偽を返す。<br>
     * @param value
     * @return
     */
    static boolean isGMTOffset( String value ) {
        return value.matches("^GMT(\\+|\\-)[0-9]{2}:[0-9]{2}$");
    }

    // HISTORYまたはCOMPOSITのﾌｨｰﾙﾄﾞからTransitのﾘｽﾄを作成して返す。colはnull禁止。
    List<Transit> getTransitList(String col) {
        List<Transit> list = new ArrayList<Transit>();
        if(col.equals("")) return list;
        for(String line : col.split("\n")) {
            String [] fields = line.split(",");
            Transit transit = new Transit();
            transit.setName( fields[EV_NAME] );
            transit.setMemo( fields[EV_MEMO] );
            transit.setDate( fields[EV_DATE],fields[EV_TIME] );
            transit.setPlaceName( fields[EV_PLACENAME] );
            transit.setLatitude( fields[EV_LATITUDE] );
            transit.setLongitude( fields[EV_LONGITUDE] );
            if ( timeZoneSet.contains( fields[EV_TIMEZONE] ) ) {
                transit.setTimeZone( TimeZone.getTimeZone( fields[EV_TIMEZONE] ) );
            } else if ( isGMTOffset( fields[EV_TIMEZONE] ) ) {
                transit.setTimeZone( TimeZone.getTimeZone( fields[EV_TIMEZONE] ) );
            } else {
                throw new IllegalArgumentException
                        ("Unknown TIMEZONE(HISTORY OR COMPOSIT)");
            }
            list.add(transit);
        }
        return list;
    }
    
    // resources/CreateTable.txtのSQL文を実行してテーブルを作成する。
    // 実行とともに既存のテーブルは抹消される。
    void initTable() {
        Connection con = DBFactory.getInstance().getConnection();
        DBFactory.createTable("/resources/CreateTable.txt",con);
    }

  //CSVファイルの行数を数える他ヘッダー等を検査してデータファイルたりえるかの
  //検査を行う。戻り値が-1のときはエラー
    int getFileRows() {
        CSVReader csvReader = null;
        int max = 1;
        int namlen = 0;
        int kanalen = 0;
        try {
            csvReader = new CSVReader(file,'\t',"UTF-16LE");
            if(csvReader.read() != BOM)
                throw new IOException("Byte Order Markの無い不正なファイルです。");
            String cols [] = csvReader.readCSV(); //ヘッダー読み捨て
            if(cols.length != HEADDER.length)
                throw new IOException("ヘッダー不一致。不正なファイルです。");
            while(csvReader.ready()) {
                cols = csvReader.readCSV();
                if(cols.length != HEADDER.length)
                    throw new IOException("CSV列数不足("+(max+1)+"行目)");
                messageLabel.setText(max + "行");
                if(cols[NAME].length() > namlen) namlen = cols[NAME].length();
                if(cols[KANA].length() > kanalen) kanalen = cols[KANA].length();
                max++;
            }
            System.out.println("名前最大長 = " + namlen);
            System.out.println("かな最大長 = " + kanalen);
        } catch(IOException e) {
            JOptionPane.showMessageDialog(owner,e.getMessage());
            max = -1;
        } finally {
            try { csvReader.close(); } catch(Exception e) { }
        }
        if(max < 2) max = -1; //最低2行無いファイルはエラー
        return max;
    }
    
    //出力先ファイルをファイルチューザーを開いて入力させる。正しく入力された場合は
    //Fileを返し、入力されなかったばあいはnullを返す。
    static File getImportFile(Component owner) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new CSVFileFilter());
        chooser.setDialogTitle("インポートするファイルの指定");
        chooser.setAcceptAllFileFilterUsed(false);
        int result = chooser.showOpenDialog(owner);
        if (result != JFileChooser.APPROVE_OPTION) return null; //選択が中止された
        File  file = chooser.getSelectedFile();
        //拡張子を入力しなかったケース
        if(! file.toString().toLowerCase().endsWith(".csv"))
            file = new File(file.getAbsolutePath().concat(".csv"));
        if(! file.exists()) {
            String message = file.getName();
            JOptionPane.showMessageDialog(
                owner,
                file.getName() + "\nこのファイルは存在しません。");
            return null;
        }
        return file;
    }
    
    /**
     * DBのテーブルを消去してバースデータをインポートする。
     * ファイルチューザーが開きバースファイルのの入力をユーザに促し、
     * プログレスバーで進行状況を表示しながらデータをロードする。
     * @param owner 親フレーム
     * @param expl  DataExplorerPanelのインスタンス。このメソッドはインポート
     *              完了時にDataExplorer#treeUpdateを呼び出し、DBの内容をJTreeと
     *              JTableに反映させる。
     */
    public static void importAll( final Frame owner,
        final DataExplorer expl ) {
        final File file = getImportFile(owner);
        if(file == null) return;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DataImporterDialog(owner,file,null).setVisible(true);
                if(expl != null) expl.treeUpdate();
            }
        });
    }
    
    /**
     * 指定フォルダにデータをインポートする。
     * @param owner 親フレーム
     * @param path  インポートするパス
     * @param expl  DataExplorerPanelのインスタンス。このメソッドはインポート
     *              完了時にDataExplorer#treeUpdateを呼び出し、DBの内容をJTreeと
     *              JTableに反映させる。
     */
    public static void load(final Frame owner,
        final TreePath path,
        final DataExplorer expl) {
        final File file = getImportFile(owner);
        if(file == null) return;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DataImporterDialog(owner,file,path).setVisible(true);
                if ( expl != null ) expl.treeUpdate();
            }
        });
    }
    /**
     * テスト
     */
    public static void main(String args[]) {
        System.setProperty("nodb","");
//        Config.data.setProperty("DefaultTime","00:00:00");
//        Config.data.setProperty("HouseSystemIndex","0");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                importAll(null,null);
            }
        });
    }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JPanel buttonPanel;
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JLabel jLabel1;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        canselButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("\u30d0\u30fc\u30b9\u30c7\u30fc\u30bf\u306e\u30a4\u30f3\u30dd\u30fc\u30c8");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("\u5165\u529b\u30d5\u30a1\u30a4\u30eb :");
        jPanel2.add(jLabel1);

        fileLabel.setText("jLabel2");
        jPanel2.add(fileLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(jPanel2, gridBagConstraints);

        progressBar.setPreferredSize(new java.awt.Dimension(200, 16));
        progressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(progressBar, gridBagConstraints);

        startButton.setText("\u30b9\u30bf\u30fc\u30c8");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(startButton);

        canselButton.setText("\u4e2d\u6b62");
        canselButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canselButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(canselButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(buttonPanel, gridBagConstraints);

        messageLabel.setText("<html> \u30d0\u30fc\u30b9\u30c7\u30fc\u30bf\u306e\u30a4\u30f3\u30dd\u30fc\u30c8\u3092\u958b\u59cb\u3057\u307e\u3059\u3002<br> \u30b9\u30bf\u30fc\u30c8\u30dc\u30bf\u30f3\u3092\u62bc\u3057\u3066\u304f\u3060\u3055\u3044\u3002</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel1.add(messageLabel, gridBagConstraints);

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // ×ボタンが押されたとき
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      canselButton.doClick();
  }//GEN-LAST:event_formWindowClosing
  // スタートボタンが押された
  private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
      startButton.setEnabled(false);
      start = true;
      new Importer().start();
  }//GEN-LAST:event_startButtonActionPerformed
  // 中止ボタンが押された。
  private void canselButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canselButtonActionPerformed
      if(! start) { //まだ開始してないときに中止された場合
          dispose();
      } else {
          stop = true; //フラグを立てて実行中のスレッドに気づかせる
      }
  }//GEN-LAST:event_canselButtonActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton canselButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
  
}
