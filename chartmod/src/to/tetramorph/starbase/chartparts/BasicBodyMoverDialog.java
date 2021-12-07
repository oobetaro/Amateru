/*
 * BasicBodyMoverDialog.java
 *
 * Created on 2007/07/16, 4:26
 */

package to.tetramorph.starbase.chartparts;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Unit;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.util.ParentWindow;

/**
 * 天体がドラッグ操作で移動されときに異動先の天体位置をこのダイアログに反映して、
 * 移動操作をしてもよいかどうかの確認を行う。このとき変更があればユーザーは、
 * 各ウィジェットを操作して望みの位置を再設定できる。
 * このダイアログは初期値を受け取り、その修正値を返すだけで天体の移動計算は行わない。
 * 天体の初期値設定はsetValues()で行う。値の取得はgetBodyID(),getSignAngle(),
 * getSign(),getTimeDirection()で行う。
 *
 * @author  大澤義鷹
 */
public class BasicBodyMoverDialog extends javax.swing.JDialog {
    /**
     * 未来にむかって検索する場合に指定
     */
    public static final boolean FORWARDS = false;
    /**
     * 過去にむかって検索する場合に指定
     */
    public static final boolean BACKWARDS = true;
    //数値入力制限をかけたスピナーモデルを用意
    private SpinnerNumberModel spinModel1 = new SpinnerNumberModel(0, 0, 29, 1);
    private boolean isAccepted = false;
    /**
     * デフォルトのオブジェクトを作成する。
     */
    public BasicBodyMoverDialog(java.awt.Frame parent) {
        super(parent, true);
        init();
    }
    private int fractionByUnit( double value ) {
        int v = 0;
        if ( AngleConverter.getAngleUnit() == AngleConverter.DECIMAL ) {
            v = (int)( (value - (int)value)  * 100. );
        } else {
            double [] val = Unit.sexagesimals( value );
            v = (int)val[2];
            System.out.println("v = " + v);
        }
        return v;
    }
    
    private void setUnit() {
        angleFractionSpinner.updateUnit();
        if ( AngleConverter.getAngleUnit() == AngleConverter.DECIMAL )
            unitLabel.setText("");
        else
            unitLabel.setText("分");
    }
    
    /**
     * ダイアログ内の部品に初期値を指定する。
     * @param bodyID 最初に選択済みにする天体ID。
     * @param sign 最初に選択済みにするサイン。定数 (0-12,またはConst.ARI〜PIS)
     * @param signAngle サインの中の天体度数(0〜29.99)
     * @param timeDirection AFTERなら未来に向けて、BEFOREなら過去に向けてをラジオボタンから選択
     */
    public void setValues( int bodyID, 
                             int sign,
                             double signAngle,
                             boolean timeDirection ) {
        setUnit();
        planetComboBox.setSelectedBody( bodyID );
        zodiacComboBox.setSelectedIndex( sign );
        spinModel1.setValue( (int)signAngle );
        angleFractionSpinner.setValue( fractionByUnit( signAngle ));
        if ( timeDirection == FORWARDS ) 
            radioButton1.setSelected( true );
        else 
            radioButton2.setSelected( true );
    }
    
    /**
     * ダイアログ内の部品に初期値を指定する。
     * @param bodyID 最初に選択済みにする天体ID。
     * @param lon 天体の黄経をセットすることでサインと度数がダイアログにセットされる。
     * @param timeDirection AFTERなら未来に向けて、BEFOREなら過去に向けてをラジオボタンから選択
     */
    public void setValues( int bodyID, double lon, boolean timeDirection ) {
        setUnit();
        int sign = (int)(lon / 30);
        int signAngle = (int)(lon % 30);
        int fraction = fractionByUnit( lon % 30 );
        planetComboBox.setSelectedBody( bodyID );
        zodiacComboBox.setSelectedIndex( sign );
        angleFractionSpinner.setValue( fraction );
        spinModel1.setValue( signAngle );
        if(timeDirection == FORWARDS) radioButton1.setSelected(true);
        else radioButton2.setSelected(true);
    }
    
    /**
     * 選択されている天体のIDを返す。
     */
    public int getBodyID() {
        return planetComboBox.getSelectedBody();
    }
    
    /**
     * 天体のサインを返す。
     */
    public int getSign() {
        return zodiacComboBox.getSelectedIndex();
    }
    
    /**
     * 天体のサイン内の角度(0-29.xx)を返す。十進小数で値は返る。
     */
    public double getSignAngle() {
        //return (double)((Integer)spinModel1.getValue());
        return (double)((Integer)spinModel1.getValue())
                + angleFractionSpinner.getFraction();
    }
    
    /**
     * 異動先の黄経を返す。
     */
    public double getAngle() {
        return getSign() * 30 + getSignAngle();
    }
    
    /**
     * 未来に向けて検索が選択されている場合はtrue、過去に向けて検索が選択されてい
     * る場合はfalseを返す。
     */
    public boolean isBackwards() {
        return radioButton2.isSelected();
    }
    
    /**
     * 天体コンボボックスに入れる惑星群を一括セットする。既存の惑星は消去される。
     */
    public void setBodys(int [] bodyArray) {
        Integer [] array = new Integer[bodyArray.length];
        for(int i=0; i<bodyArray.length; i++) array[i] = bodyArray[i];
        planetComboBox.removeAllItems();
        planetComboBox.setItems(array);
    }
    
    private void init() {
        initComponents();
        //setKeyListener();
        ParentWindow.setEscCloseOperation(this,new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                abortButton.doClick();
            }
        });
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);
        angleSpinner1.setModel(spinModel1);
        getRootPane().setDefaultButton(okButton);
    }
    
    /**
     * 決定ボタンが押されてクローズした場合はtrue、中止ボタンや[×]ボタンでクローズ
     * した場合はfalseを返す。
     */
    public boolean isAccepted() {
        return isAccepted;
    }
    
    
    
    //--------------------------　ここよりテストメソッド -----------------------
    private static BasicBodyMoverDialog dialog = null;
    private static final String [] timeNames = { "未来" , "過去" };
    private static void createAndShowGUI() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        JButton button = new JButton("OPEN");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dialog = new BasicBodyMoverDialog(frame);
                dialog.setValues(Const.JUPITER,Const.PIS,23.40,FORWARDS);
                dialog.setVisible(true);
                System.out.println("決定ボタン : " + dialog.isAccepted());
                System.out.println("選択天体 : " + Const.PLANET_NAMES[dialog.getBodyID()]);
                System.out.println("選択星座 : " + Const.ZODIAC_NAMES[dialog.getSign()]);
                System.out.println("星座度数 : " + dialog.getSignAngle());
                System.out.println("移動方向 : " + (dialog.isBackwards() ? "過去" : "未来") );
            }
        });
        frame.add(button);
        frame.pack();
        frame.setVisible(true);
    }
    /**
     * テスト
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
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
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel jPanel1;
        javax.swing.JPanel jPanel2;
        javax.swing.JPanel jPanel3;
        javax.swing.JPanel jPanel4;

        buttonGroup = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        planetComboBox = new to.tetramorph.starbase.widget.PlanetComboBox();
        zodiacComboBox = new to.tetramorph.starbase.widget.ZodiacComboBox();
        angleSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        angleFractionSpinner = new to.tetramorph.starbase.widget.AngleFractionSpinner();
        unitLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        radioButton1 = new javax.swing.JRadioButton();
        radioButton2 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        abortButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("\u5929\u4f53\u79fb\u52d5");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel1.add(planetComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel1.add(zodiacComboBox, gridBagConstraints);

        angleSpinner1.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel1.add(angleSpinner1, gridBagConstraints);

        jLabel2.setText("\u5ea6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel4.setText("\u7570\u52d5\u5148");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(jLabel4, gridBagConstraints);

        angleFractionSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        jPanel1.add(angleFractionSpinner, gridBagConstraints);

        unitLabel.setText("\u5206");
        jPanel1.add(unitLabel, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel4.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        radioButton1.setSelected(true);
        radioButton1.setText("\u672a\u6765\u306b\u5411\u3051\u3066\u691c\u7d22");
        radioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(radioButton1, gridBagConstraints);

        radioButton2.setText("\u904e\u53bb\u306b\u5411\u3051\u3066\u691c\u7d22");
        radioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(radioButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel4.add(jPanel2, gridBagConstraints);

        okButton.setText("\u6c7a\u5b9a");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel3.add(okButton);

        abortButton.setText("\u4e2d\u6b62");
        abortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortButtonActionPerformed(evt);
            }
        });

        jPanel3.add(abortButton);

        jButton1.setText("\u7aef\u6570\u30af\u30ea\u30a2");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel4.add(jPanel3, gridBagConstraints);

        getContentPane().add(jPanel4);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        angleFractionSpinner.setValue(0);
    }//GEN-LAST:event_jButton1ActionPerformed
    
  private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
      isAccepted = true;
      dispose();
  }//GEN-LAST:event_okButtonActionPerformed
  
  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      abortButton.doClick();
  }//GEN-LAST:event_formWindowClosing
  
  private void abortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortButtonActionPerformed
      isAccepted = false;
      dispose();
  }//GEN-LAST:event_abortButtonActionPerformed
  
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton abortButton;
    private to.tetramorph.starbase.widget.AngleFractionSpinner angleFractionSpinner;
    private javax.swing.JSpinner angleSpinner1;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton okButton;
    private to.tetramorph.starbase.widget.PlanetComboBox planetComboBox;
    private javax.swing.JRadioButton radioButton1;
    private javax.swing.JRadioButton radioButton2;
    private javax.swing.JLabel unitLabel;
    private to.tetramorph.starbase.widget.ZodiacComboBox zodiacComboBox;
    // End of variables declaration//GEN-END:variables
    
}
