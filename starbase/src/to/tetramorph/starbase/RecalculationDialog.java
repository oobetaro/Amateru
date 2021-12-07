/*
 * RecalculationDialog.java
 *
 * Created on 2007/02/10, 21:15
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.time.DateFormat;

/**
 * データベースのユリウス日と天体位置を再計算する。DefaultTimeが変更された際に
 * 時刻が未登録のデータのユリウス日を計算しなおす必要がある。ユリウス日が変更
 * されれは天体位置データも再計算が必要になる。このダイアログは進行状況を表示
 * しながら再計算を行う。再計算中は他のGUI操作はロックされる。<br>
 * <br>
 * ユリウス日は時刻未登録のものだけを再計算する。天体位置はすべてを計算しなおす。
 * 時刻未登録のものだけを再計算しても良いのだが、天体位置を削除したり挿入すると
 * 手間もがかかるし処理も遅くなるうえバグを入れる可能性も高まるだろうから。
 * ただしHOUSE表は再計算しない。HOUSE表に登録されているデータは時刻登録されてい
 * るデータのみなので再計算は不要。<br>
 * <br>
 * ユリウス日の更新はHSQLDBにストアドプロシージャを登録することで行っている。
 * UPDATE文で大量に一気に更新がかかるとき、通常ならそのまま完了まで待つしか
 * ないが、ストアドプロシージャのgetJDay()に細工をしてあって、static変数で、
 * このメソッドが呼び出された回数を保管し、それを外部から参照することで、
 * UPDATE文の変更状況を得る事ができた。<br>
 * <br>
 * UPDATE中はstatic変数がｶｳﾝﾄｱｯﾌﾟをつづける。それをﾀｲﾏｰで定期的に値を参照して
 * プログレスバーにその値をセットしていく。<br><br>
 *
 * 【注意】このクラスの実行は、HSQLDBが同じプロセスで動作している必要がある。
 * アマテル起動オプションで、-Dnodbを指定せず、アマテルからDBを起動するよう設定
 * し実行すること。別プロセスでDBを動かしている場合、次のようなエラーが出る。
 * SQLException : Error calling function:
 * to.tetramorph.starbase.Function.getJDay :
 *    java.lang.NoClassDefFoundError: to/tetramorph/time/JDay
 * つまりストアドプロシージャで呼び出すメソッドが見つけられない。
 */
class RecalculationDialog extends javax.swing.JDialog {
    Icon checkIcon;       //ﾁｪｯｸﾏｰｸｱｲｺﾝ
    Icon checkNullIcon;   //ﾁｪｯｸﾏｰｸと同ｻｲｽﾞの空白ｱｲｺﾝ
    Connection con;       //DBとのｺﾈｸｼｮﾝ
    DB db;                //ﾃﾞｰﾀﾍﾞｰｽｲﾝｽﾀﾝｽ
    Timer timer = null;  //進行状況監視ﾀｲﾏｰ
    int count = 0;       //進行状況を表すｶｳﾝﾀ

    /** シングルトンクラスのためコンストラクタは保護 */
    private RecalculationDialog(Frame parent) {
        super(parent, true);
        initComponents();
        // ｱｲｺﾝはﾁｪｯｸｱｲｺﾝとﾌﾞﾗﾝｸｱｲｺﾝの二つあり両方保管しておく。
        // 二つあるのはﾗﾍﾞﾙのｻｲｽﾞかわるとﾀﾞｲｱﾛｸﾞ全体が再配置されてﾃｷｽﾄｴﾘｱのｻｲｽﾞが
        // おかしくなるため、ｻｲｽﾞを一定に保つために同ｻｲｽﾞのｱｲｺﾝをつねにｾｯﾄしている。
        checkIcon = stepLabel1.getIcon();     //ﾁｪｯｸﾏｰｸ
        checkNullIcon = stepLabel2.getIcon(); //↑同ｻｲｽﾞの空白ｱｲｺﾝ
        stepLabel1.setIcon(checkNullIcon);
        this.setLocationRelativeTo(parent);
        db = DBFactory.getInstance();
        con = db.getConnection();
        start();
    }
    /**
     * 再計算ダイアログを開いて、データベース上のユリウス日と天体位置を再計算する。
     * 外から呼び出せる唯一のメソッド。
     */
    public static void showDialog(final Frame parent) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RecalculationDialog(parent).setVisible(true); }
        });
    }
    //指定したラベルのEnable/Disenableを設定(EDTにて)
    void setEnabled(final JLabel label,final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                label.setEnabled(b);
            }
        });
    }
    //指定したラベルにチェックアイコンを設定(EDTにて)
    void setCheckIcon(final JLabel label) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                label.setIcon(checkIcon);
            }
        });
    }
    //指定したプログレスバーに値をセットする。
    void setBar(final JProgressBar bar,final int value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                bar.setValue(value);
            }
        });
    }
    //指定したプログレスバーに最大値を設定する。
    void setBarMax(final JProgressBar bar,final int value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                bar.setMaximum(value);
            }
        });
    }
    void start() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //ユリウス日の再計算開始
                //ユリウス日バー用のタイマー開始
                timer = new Timer(10,new JDayObserver());
                timer.start();
                setEnabled(stepLabel1,true);
                updateJDays("HISTORY");
                setCheckIcon(stepLabel1);
                setEnabled(stepLabel1,false);

                setEnabled(stepLabel2,true);
                updateJDays("COMPOSIT");
                setCheckIcon(stepLabel2);
                setEnabled(stepLabel2,false);

                setEnabled(stepLabel3,true);
                updateJDays("OCCASION");
                setCheckIcon(stepLabel3);
                setEnabled(stepLabel3,false);
                timer.stop();
                //終了とともにバーを最大値にする。
                setBar(jdayProgressBar,jdayProgressBar.getMaximum());
                //惑星位置計算を開始
                //惑星バー用のタイマー開始
                timer = new Timer(10,new PlanetsObserver());
                timer.start();
                updatePlanets();
                timer.stop();
                //終了とともにバーを最大値にする。
                setBar(planetsProgressBar,planetsProgressBar.getMaximum());
                endMessage();
            }
        });
        thread.start();
    }
    void endMessage() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                exitButton.setEnabled(true);
                msgLabel1.setText("再計算は正常に終了しました。");
                msgLabel2.setText("OKボタンを押してください。");
                pack();
            }
        });
    }
    //タイマーから定期的に呼び出されユリウスカウンターの値をバーにセットする
    class JDayObserver implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            jdayProgressBar.setValue(Function.getJDayCounter());
        }
    }
    //タイマーから定期的に呼び出されcountの値をバーにセットする
    class PlanetsObserver implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            planetsProgressBar.setValue(count);
        }
    }
    /**
     * 指定されたテーブルのユリウス日を再計算する。
     * OCCASION,EVENT,COMPOSIT。COMPOSIT表のデータ量を半分にしてベンチをとってみ
     * ると、データ数とかかる時間は比例関係にあるらしい。(このSQL文での話)
     */
    void updateJDays(String tableName) {
        long start = System.currentTimeMillis();
        Statement stmt = null;
        ResultSet rs = null;
        String defaultTime = Config.getDefaultTime();
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName +
                " WHERE TIME IS NULL");
            rs.next();
            int rows = rs.getInt(1);
            if(rows == 0) return; //0件なら未処理で帰る
            print(tableName+"表の処理件数 : "+rows + "件 , ");
            setBarMax(jdayProgressBar,rows);
            //jdayProgressBar.setMaximum(rows);
            Function.setJDayCounter(0); //ユリウス日カウンターリセット
            stmt.execute("CREATE ALIAS JULDAY " +
                "FOR \"to.tetramorph.starbase.Function.getJDay\"");
            //WHEREでCOMPOSIT表を除外したくなるが、複数の表が対象なのでだめ。
            stmt.executeUpdate("UPDATE " + tableName + " OCCASION " +
                "SET JDAY = JULDAY(ERA,DATE,TIME,TIMEZONE,'" + defaultTime + "') " +
                "WHERE TIME IS NULL");
        } catch ( SQLException e ) {
            println("SQLException : " + e.getMessage());
            println("SQLState : " + e.getSQLState());
        } finally {
            db.close(stmt,rs);
        }
        println("所要時間 : " + getBenchTime(System.currentTimeMillis()-start));
    }

    /**
     * PLANETS_LONGITUDE表の星の位置を再計算して挿入しなおす。すべて計算しなおす
     * のは無駄が多いようにも感じるが、選別して削除してまた識別しながら挿入する
     * のはかえって時間がかかる可能性もあるし、なにより全部計算しなおせば安心で
     * できるというのが大きい。部分的に計算漏れなどを起こすより、時間を待つほうが
     * いい。
     */
    void updatePlanets() {
        long start = System.currentTimeMillis();
        PreparedStatement ps = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(DISTINCT JDAY) FROM OCCASION");
            rs.next();
            int max = rs.getInt(1);
            if(max == 0) return;
            println("PLANETS_LONGITUDE表の処理件数 : " + max + "件");
            setBarMax(planetsProgressBar,max);
            //planetsProgressBar.setMaximum(max);
            stmt.execute("DELETE FROM PLANETS_LONGITUDE");
            rs = stmt.executeQuery("SELECT DISTINCT JDAY FROM OCCASION");
            Ephemeris eph = Ephemeris.getInstance(); //new Ephemeris();
            ps = con.prepareStatement(
                "INSERT INTO PLANETS_LONGITUDE VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            count = 0;
            while(rs.next()) {
                double jday = rs.getDouble("JDAY");
                Map<Integer,Body> map = eph.getBodyMap(jday);
                if(eph.isError()) {
                    String date = DateFormat.getDateString(jday);
                    println(date);
                    for(String errmsg :eph.getErrorList())
                        println("    " + errmsg.trim());
                }
                ps.setDouble(1,jday);
                for(int i=Const.SUN,j=2; i <= Const.ANTI_OSCU_APOGEE; i++) {
                    if(i == Const.EARTH) continue; //地球は除外
                    Body body = map.get(i);
                    if(body != null) {
                        ps.setFloat(j,(float)body.lon);
                    } else {
                        ps.setNull(j,java.sql.Types.NULL);
                    }
                    j++;
                }
                ps.execute();
                ps.clearParameters();
                count++;
            }
        } catch (SQLException e) {
            println("SQLException : " + e.getMessage());
            println("SQLState : " + e.getSQLState());
        } finally {
            db.close(ps,rs,stmt);
        }
        println("所要時間 : " + getBenchTime(System.currentTimeMillis()-start));
    }

    //テキストエリアに文字列を出力。改行あり。
    private void println(final String value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reportTextArea.append(value);
                reportTextArea.append("\n");
            }
        });
    }
    //テキストエリアに文字列を出力。改行なし。
    private void print(final String value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reportTextArea.append(value);
            }
        });
    }
    //入力秒数を時間表現文字列にして返す。
    static String getBenchTime(long milisec) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(milisec);
        if(milisec < (60 * 1000))
            return String.format("%tS秒",c);
        else if(milisec < (3600 * 1000))
            return String.format("%tM分 %tS秒\n",c,c);
        else
            return String.format("%tH時間 %tM分 %tS秒\n",c,c,c);
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
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel6;
    javax.swing.JLabel jLabel7;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JScrollPane jScrollPane2;

    jPanel1 = new javax.swing.JPanel();
    planetsProgressBar = new javax.swing.JProgressBar();
    jLabel1 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    reportTextArea = new javax.swing.JTextArea();
    jdayProgressBar = new javax.swing.JProgressBar();
    jPanel2 = new javax.swing.JPanel();
    stepLabel1 = new javax.swing.JLabel();
    stepLabel2 = new javax.swing.JLabel();
    stepLabel3 = new javax.swing.JLabel();
    msgLabel1 = new javax.swing.JLabel();
    msgLabel2 = new javax.swing.JLabel();
    exitButton = new javax.swing.JButton();

    getContentPane().setLayout(new java.awt.GridLayout(1, 0));

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("\u5929\u4f53\u4f4d\u7f6e\u306e\u518d\u8a08\u7b97");
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 15, 8, 15));
    planetsProgressBar.setStringPainted(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(planetsProgressBar, gridBagConstraints);

    jLabel1.setText("\u5929\u4f53\u4f4d\u7f6e\u3092\u518d\u8a08\u7b97");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jLabel1, gridBagConstraints);

    jLabel3.setText("\u30e6\u30ea\u30a6\u30b9\u65e5\u306e\u518d\u8a08\u7b97");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jLabel3, gridBagConstraints);

    jLabel6.setText("\u30c7\u30fc\u30bf\u30d9\u30fc\u30b9\u3092\u66f4\u65b0\u4e2d\n");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jLabel6, gridBagConstraints);

    jLabel7.setText("\u30ec\u30dd\u30fc\u30c8");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jLabel7, gridBagConstraints);

    reportTextArea.setColumns(20);
    reportTextArea.setEditable(false);
    reportTextArea.setRows(5);
    reportTextArea.setMinimumSize(new java.awt.Dimension(220, 90));
    jScrollPane2.setViewportView(reportTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jScrollPane2, gridBagConstraints);

    jdayProgressBar.setStringPainted(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    jPanel1.add(jdayProgressBar, gridBagConstraints);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    stepLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/checked.gif")));
    stepLabel1.setText("STEP1");
    stepLabel1.setEnabled(false);
    stepLabel1.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 8);
    jPanel2.add(stepLabel1, gridBagConstraints);

    stepLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/checked_null.gif")));
    stepLabel2.setText("STEP2");
    stepLabel2.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(1, 8, 1, 8);
    jPanel2.add(stepLabel2, gridBagConstraints);

    stepLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/checked_null.gif")));
    stepLabel3.setText("SETP3");
    stepLabel3.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(1, 8, 1, 0);
    jPanel2.add(stepLabel3, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanel1.add(jPanel2, gridBagConstraints);

    msgLabel1.setText("\u51e6\u7406\u3092\u4e2d\u65ad\u3059\u308b\u3053\u3068\u306f\u3067\u304d\u307e\u305b\u3093\u3002");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 25, 3, 3);
    jPanel1.add(msgLabel1, gridBagConstraints);

    msgLabel2.setText("\u3057\u3070\u3089\u304f\u304a\u5f85\u3061\u304f\u3060\u3055\u3044\u3002");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 25, 15, 3);
    jPanel1.add(msgLabel2, gridBagConstraints);

    exitButton.setText("OK");
    exitButton.setEnabled(false);
    exitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
    jPanel1.add(exitButton, gridBagConstraints);

    getContentPane().add(jPanel1);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
      dispose();
  }//GEN-LAST:event_exitButtonActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      //exitButtonActionPerformed(null);
  }//GEN-LAST:event_formWindowClosing

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton exitButton;
  private javax.swing.JProgressBar jdayProgressBar;
  private javax.swing.JLabel msgLabel1;
  private javax.swing.JLabel msgLabel2;
  private javax.swing.JProgressBar planetsProgressBar;
  private javax.swing.JTextArea reportTextArea;
  private javax.swing.JLabel stepLabel1;
  private javax.swing.JLabel stepLabel2;
  private javax.swing.JLabel stepLabel3;
  // End of variables declaration//GEN-END:variables

}
