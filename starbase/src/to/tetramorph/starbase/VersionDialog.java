/*
 * VersionDialog.java
 *
 * Created on 2007/12/11, 3:46
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import static java.lang.System.getProperty;
import java.util.logging.Logger;
import to.tetramorph.util.ParentWindow;
/**
 * アプリケーションのバージョン情報等を表示するためのダイアログ。
 * @author 大澤義鷹
 */
class VersionDialog extends javax.swing.JDialog {
    private static final String VERSION = "2.0.5";
    private static final String [] PROPERTY_KEYS = {
        "java.version","java.runtime.version","java.vm.name","java.vendor",
        "java.specification.version","java.class.version","os.name",
        "os.arch","os.version","user.name","user.home"
    };
    public VersionDialog() { }
    /**
     * バージョン情報ダイアログを作成。ランチングメソッドは用意していない。
     * 単純に次のようにして表示する。
     * new VersionDialog(owner,true).setVisible(true)
     */
    private VersionDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        editorPane.setBackground( iconPanel.getBackground() );
        systemEditorPane.setBackground( iconPanel.getBackground() );
        getRootPane().setDefaultButton(acceptButton);
        ParentWindow.setEscCloseOperation( this, new AbstractAction("了解") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                acceptButton.doClick();
            }
        });
        if(System.getProperty("java.version").startsWith("1.6")) {
//            Image img = IconLoader.getImage("/resources/niwatori2.png");
            setIconImage(AppIcon.TITLE_BAR_ICON);
        }
        String ctype = "text/html";
        editorPane.setContentType(ctype);
        systemEditorPane.setContentType(ctype);
        announceEditorPane.setContentType(ctype);

        editorPane.setText(getMessage());
        systemEditorPane.setText(getSystemMessage());
        announceEditorPane.setText(getAnnounceMessage());

        acceptButton.requestFocusInWindow();
        this.setLocationRelativeTo(null);
        reloadButtonActionPerformed(null);
    }
    /**
     * AMATERUのバージョンを返す。
     */
    public static String getVersion() {
        return VERSION;
    }
    private String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"font-family:'MS Pゴシック'\">");
        sb.append("<center>");
        sb.append("AMATERU<br>");
        sb.append("Version " + VERSION  + "<br>");
        sb.append("Copyright(C) 2008-2011 大澤義孝");
        sb.append("</center>");
        sb.append("<p align=right style='font-size:12pt'>");
        sb.append("サポート : http://tetramorph.to/</p>");
        sb.append("<font style='font-size:12pt'>");
        sb.append("次のAPIおよびデータを使用させて頂いてます。ありがーと！</font><br>");
        sb.append("<dl style='font-size:12pt'>");
        sb.append("<dt>Swiss Ephemeris (C) Astrodienst AG, Switzerland.</dt>");
        sb.append("<dd>http://www.astro.com/</dd>");
        sb.append("<dt>Swiss Ephemeris for Java (C) Thomas Mack</dt>");
        sb.append("<dd>http://th-mack.de/international/download/</dd>");
        sb.append("<dt>HSQLDB (C) The HSQL Development Group</dt>");
        sb.append("<dd>http://hsqldb.org/</dd>");
        sb.append("<dt>世界の緯度経度データ (C) From FLand</dt>");
        sb.append("<dd>http://gpscycling.net/fland/index.html</dd>");
        sb.append("<dt>日本国内の緯度経度データ<br>"
                 + "(『街区レベル位置参照情報　国土交通省』を使用して作成)");
        sb.append("<dd>http://nlftp.mlit.go.jp/");
        return sb.toString();
    }

    private String getSystemMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=0 cellspacing=1 cellpadding=0>");
        addRow("Application Home", getProperty("app.home"), sb);
        for(String key : PROPERTY_KEYS) {
            addRow(key, getProperty(key), sb );
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private void addRow(String key,String value,StringBuilder sb) {
        sb.append("<tr><td style='font-size:12pt'>");
        sb.append(key);
        sb.append("</td><td style='font-size:12pt'>");
        sb.append(value);
        sb.append("</td></tr>");
    }

    private String getAnnounceMessage() {
        String html = null;
        try {
            html = FileLoader.getTextFile("/resources/announce.html","sjis");
        } catch (Exception e) {
            Logger.getLogger( VersionDialog.class.getName() )
                    .log(Level.SEVERE,null,e);
            html = "お知らせはありません。";
        }
        return html;
    }
    /**
     * バージョン表示ダイアログを出す。
     */
    public static void showDialog( final Frame owner ) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VersionDialog(owner).setVisible(true);
            }
        });
    }
    /**
     *　お知らせタブを選択した状態にしてバージョン表示ダイアログを出す。
     * これは内部でEDTを作成して出す。
     */
    public static void showAnnounce( final Frame owner ) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                VersionDialog dialog = new VersionDialog(owner);
                dialog.tabbedPane.setSelectedIndex(2);
                dialog.setVisible(true);
            }
        });
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        iconPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        acceptButton = new javax.swing.JButton();
        resizeIconPanel1 = new to.tetramorph.widget.ResizeIconPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        systemEditorPane = new javax.swing.JEditorPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        announceEditorPane = new javax.swing.JEditorPane();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane4 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        reloadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AMATERUについて");

        iconPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 15));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/niwatori32.png"))); // NOI18N
        iconPanel.add(jLabel1);

        getContentPane().add(iconPanel, java.awt.BorderLayout.WEST);

        jPanel2.setLayout(new java.awt.BorderLayout());

        acceptButton.setMnemonic('Y');
        acceptButton.setText("了解(Y)");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });
        jPanel1.add(acceptButton);

        jPanel2.add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel2.add(resizeIconPanel1, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 360));

        editorPane.setBackground(new java.awt.Color(192, 192, 192));
        editorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        editorPane.setEditable(false);
        jScrollPane1.setViewportView(editorPane);

        tabbedPane.addTab("AMATERUについて", jScrollPane1);

        jScrollPane2.setViewportView(systemEditorPane);

        tabbedPane.addTab("システム情報", jScrollPane2);

        jScrollPane3.setViewportView(announceEditorPane);

        tabbedPane.addTab("お知らせ", jScrollPane3);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setViewportView(jEditorPane1);

        jPanel3.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        reloadButton.setText("リロード");
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });
        jPanel4.add(reloadButton);

        jPanel3.add(jPanel4, java.awt.BorderLayout.SOUTH);

        tabbedPane.addTab("ニワトリの数", jPanel3);

        getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        try {
            jEditorPane1.setText( "サーバに情報をとりにいってます");
            int i = (int)( System.currentTimeMillis() / 1000L & 15 );
            jEditorPane1.setPage( "http://tetramorph.to/amateru_log/report.cgi?"+i);
            jEditorPane1.validate();
            System.out.println("リロード");
        } catch ( IOException e ) {
            jEditorPane1.setText( "サーバーから情報を取得できませんでした。<br>"+
                                  "サーバーのメンテ中か回線障害でしょう。<br>" +
                                  "また後日ためしてみてくだされ。");
            //e.printStackTrace();
        }
        //同じURLだとリロードされないので、適当に毎回異なる引数を与えている。
        //report.cgiは引数を受け取らず無視するのでなにを指定しても問題ない。
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        dispose();
    }//GEN-LAST:event_acceptButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //showDialog(null);
                showAnnounce(null);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JEditorPane announceEditorPane;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JPanel iconPanel;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton reloadButton;
    private to.tetramorph.widget.ResizeIconPanel resizeIconPanel1;
    private javax.swing.JEditorPane systemEditorPane;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

}
