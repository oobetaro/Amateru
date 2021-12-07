/*
 * TimePreferencePanel.java
 *
 * Created on 2006/09/15, 6:04
 */
package to.tetramorph.starbase;

import to.tetramorph.starbase.formatter.FormatterFactory;
import to.tetramorph.starbase.formatter.TimeFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.util.GregorianCalendar;
import javax.swing.ButtonGroup;

/**
 * 時間が不明なときのデフォルトタイムを設定するパネル。
 * 次のプロパティを設定する。
 * <pre>
 * DefaultTimeButton = 1 .. 3
 * DefaultTime = "12:00:00"等、java.sql.Timeの時刻表現文字列
 * DefaultTimeButtonが1のときはDefaultTimeは"00:00:00"。
 * 2のときは"12:00:00"。3のときはそれ以外の時間が入っている。
 * DefaultTimeButtonのパラメラーはラジオボタンの選択復元のためにある。
 *</pre>
 * @author  大澤義鷹
 */
class TimePreferencePanel extends PreferencePanel {

    ButtonGroup timeButtonGroup = new ButtonGroup();

    /** Creates new form TimePreferencePanel */
    public TimePreferencePanel() {
        initComponents();
        init();
    }
    // 時間設定の初期化

    private void init() {
        timeButtonGroup.add(defaultTimeRadioButton1);
        timeButtonGroup.add(defaultTimeRadioButton2);
        timeButtonGroup.add(defaultTimeRadioButton3);
        defaultTimeRadioButton1.setActionCommand("1");
        defaultTimeRadioButton2.setActionCommand("2");
        defaultTimeRadioButton3.setActionCommand("3");

        timeFormattedTextField.setFormatterFactory(new FormatterFactory(
                new TimeFormatter()));
        Integer select = Config.usr.getInteger("DefaultTimeButtonIndex", 1);
        if (select == 1) { //1番がデフォで設定されていた場合
            defaultTimeRadioButton1.doClick();
            timeFormattedTextField.setEnabled(false);
        } else if (select == 2) { //2番が　〃
            defaultTimeRadioButton2.doClick();
            timeFormattedTextField.setEnabled(false);
        } else { //3番が
            defaultTimeRadioButton3.doClick();
            Time t = Time.valueOf( Config.usr.getProperty("DefaultTime", "12:00:00"));
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(t);
            timeFormattedTextField.setValue(cal);
        }
        ActionListener l = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == defaultTimeRadioButton3) {
                    timeFormattedTextField.setEnabled(true);
                } else {
                    timeFormattedTextField.setEnabled(false);
                }
            }
        };
        defaultTimeRadioButton1.addActionListener(l);
        defaultTimeRadioButton2.addActionListener(l);
        defaultTimeRadioButton3.addActionListener(l);
        if ( System.getProperty("nodb","false").equals("true") ) {
            errorLabel.setText("<html><font color=red>外部DBモードでは変更"
                    + "できません。</font></html>");
            defaultTimeRadioButton1.setEnabled(false);
            defaultTimeRadioButton2.setEnabled(false);
            defaultTimeRadioButton3.setEnabled(false);
            timeFormattedTextField.setEnabled(false);
        }
    }

    /**
     * 設定をプレファランスにセットする。
     */
    @Override
    public void regist() {
        String cmd = timeButtonGroup.getSelection().getActionCommand();
        if (cmd.equals("1")) {
            Config.usr.setProperty( "DefaultTime", "00:00:00" );
            Config.usr.setInteger( "DefaultTimeButtonIndex", 1 );
        } else if ( cmd.equals("2") ) {
            Config.usr.setProperty( "DefaultTime", "12:00:00" );
            Config.usr.setInteger( "DefaultTimeButtonIndex", 2 );
        } else {
            GregorianCalendar t = (GregorianCalendar) timeFormattedTextField.getValue();
            Config.usr.setProperty( "DefaultTime", String.format("%tT", t));
            Config.usr.setInteger( "DefaultTimeButtonIndex", 3 );
        }
        //例外的にDefaultTimeはSystemプロパティにも設定する。
        String dt = Config.usr.getProperty("DefaultTime", "");
        if ( dt.isEmpty() ) {
            throw new IllegalStateException("DefaultTimeプロパティが未設定");
        }
        System.setProperty("DefaultTime", dt);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        timeFormattedTextField = new javax.swing.JFormattedTextField();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        defaultTimeRadioButton3 = new javax.swing.JRadioButton();
        defaultTimeRadioButton2 = new javax.swing.JRadioButton();
        defaultTimeRadioButton1 = new javax.swing.JRadioButton();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        timeFormattedTextField.setMinimumSize(new java.awt.Dimension(20, 22));
        timeFormattedTextField.setPreferredSize(new java.awt.Dimension(80, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(timeFormattedTextField, gridBagConstraints);

        jLabel1.setText("時間が不明のときのデフォルトタイム");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("　　");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jLabel2, gridBagConstraints);

        defaultTimeRadioButton3.setText("指定した地方時刻で計算");
        defaultTimeRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultTimeRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(defaultTimeRadioButton3, gridBagConstraints);

        defaultTimeRadioButton2.setText("地方時の12時で計算");
        defaultTimeRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultTimeRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(defaultTimeRadioButton2, gridBagConstraints);

        defaultTimeRadioButton1.setText("地方時の0時で計算");
        defaultTimeRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultTimeRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(defaultTimeRadioButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(errorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton defaultTimeRadioButton1;
    private javax.swing.JRadioButton defaultTimeRadioButton2;
    private javax.swing.JRadioButton defaultTimeRadioButton3;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JFormattedTextField timeFormattedTextField;
    // End of variables declaration//GEN-END:variables
}
