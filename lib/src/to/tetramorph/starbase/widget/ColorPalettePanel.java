/*
 * ColorPalettePanel.java
 *
 * Created on 2007/01/04, 9:30
 */
package to.tetramorph.starbase.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import to.tetramorph.util.ColorCalcurator;

/**
 * シンプルなカラーパレット。カラーパレットと他の色ボタンを選択すると、見本色の
 * ところにその色が表示され、getSelectedColor()メソッドで選択色を取得できる。
 */
public class ColorPalettePanel extends javax.swing.JPanel {

    //文字で表現された座標("5,6"等)から、該当位置のJLabelを取得するハッシュ
    //座標値というとPointに入れたくなるが、ここでは文字列のほうが楽できる。
    private Map<String, JLabel> colorMap = new HashMap<String, JLabel>();
    private JLabel selectedLabel; //選択されているラベルを保管
    private JDialog colorDialog; //ｶﾗｰﾁｭｰｻﾞｰ用のﾀﾞｲｱﾛｸﾞ
    private JColorChooser chooser = new JColorChooser();
    private ChangeListener changeListener;
    //非選択状態のカラーパレットのラベルに背景色と同色のボーダー線をつけて、
    //実際の大きさより一回り小さくしてみせておく。選択されて色のついたボーダーが
    //つくと選択パレットがより強調されて見えるため。
    private final Border emptyBorder =
            BorderFactory.createLineBorder(getBackground(), 2);
    private Color selectedColor; //現在選択されているパレットの色
    private JLabel sampleLable = null;

    /** オブジェクトを作成する */
    public ColorPalettePanel() {
        initComponents();
        init();
    }

    private void init() {
        Color[][] table = ColorCalcurator.getColorTable();
        Dimension size = new Dimension(12, 12);
        //ｶﾗｰﾊﾟﾚｯﾄを動的に生成
        for (int y = 8; y >= 0; y--) {
            for (int x = 0; x <= 12; x++) {
                JLabel label = new JLabel();
                label.setPreferredSize(size);
                label.setOpaque(true);
                label.setBackground(table[x][y]);
                label.setBorder(emptyBorder);
                //リスナにラベルの座標を保管しておく
                label.addMouseListener(new MouseHandler(x, y));
                palettePanel.add(label);
                colorMap.put(x + "," + y, label);
                //なぜMapに入れるかというと、座標値からLabelを取得したいから
            }
        }
        colorMap.get("0,8").setBackground(Color.WHITE); //例外。この色は白にする。
        //(0,0)のラベルを初期の選択ラベルとする。
        selectedLabel = colorMap.get("0,0");
        selectedLabel.getMouseListeners()[0].mouseReleased(null);
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                setColorToLabel(chooser.getColor());
            }
        };
        colorDialog = JColorChooser.createDialog(this, "他の色", true, chooser, al, null);

    }
    // ラベルがクリックされても、選択できるようにするためのマウスハンドラ

    class MouseHandler extends MouseAdapter {

        int x, y;
        // オブジェクト作成時にラベルのx,y座標を受け取る(左下が(0,0)

        MouseHandler(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            JLabel label = colorMap.get(x + "," + y);
            if (evt.getClickCount() == 1) {
                if (selectedLabel != null) {
                    selectedLabel.setBorder(emptyBorder); //前回の選択線を消去
                }              //見やすくするため選択ラベルの位置によって選択マーカー線の色を変える
                Color c = (y <= 2) ? Color.RED : Color.BLACK;
                //色変更されるので毎回ﾎﾞｰﾀﾞｰは作り直す必要がある
                label.setBorder(BorderFactory.createLineBorder(c, 2));
                selectedLabel = label;
                setRGBCode(label.getBackground());
            } else if (evt.getClickCount() == 2) {
                setColorToLabel(label.getBackground());
            }
        }
    }

    /**
     * カラーコードと色見本を表示して、その色が選択されたことにする。
     * これにより確定された色を、getSelectedColor()によって返すことができる。
     */
    private void setColorToLabel(Color color) {
//    colorCodeLabel1.setText("R=" + color.getRed());
//    colorCodeLabel2.setText("G=" + color.getGreen());
//    colorCodeLabel3.setText("B=" + color.getBlue());
        setRGBCode(color);
        selectedColor = color;
        if (changeListener != null) {
            ChangeEvent event = new ChangeEvent(this);
            changeListener.stateChanged(event);
        }
    }

    private void setRGBCode(Color color) {
        colorCodeLabel1.setText("R=" + color.getRed());
        colorCodeLabel2.setText("G=" + color.getGreen());
        colorCodeLabel3.setText("B=" + color.getBlue());
    }

    /**
     * 選択されている色を返す。
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * 色選択されたときのリスナを登録する。1個しか登録できない。
     */
    public void setChangeListener(ChangeListener l) {
        changeListener = l;
    }
    //テスト

    static void createAndShowGUI() {
        if (UIManager.getLookAndFeel().getName().equals("Metal")) {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("サインカラー設定");
        ColorPalettePanel panel = new ColorPalettePanel();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * カラーチューザーを開くときの初期カラーを指定する。
     */
    public void setChooserColor(Color color) {
        if (color != null) {
            chooser.setColor(color);
        }
    }

    /** テスト */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
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
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    palettePanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    jButton1 = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
    colorCodeLabel1 = new javax.swing.JLabel();
    colorCodeLabel2 = new javax.swing.JLabel();
    colorCodeLabel3 = new javax.swing.JLabel();

    setBorder(javax.swing.BorderFactory.createEtchedBorder());
    setLayout(new java.awt.GridBagLayout());

    palettePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    palettePanel.setLayout(new java.awt.GridLayout(9, 13));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    add(palettePanel, gridBagConstraints);

    jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));

    jButton1.setText("\u4ed6\u306e\u8272");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });
    jPanel3.add(jButton1);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    colorCodeLabel1.setText("R=");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(colorCodeLabel1, gridBagConstraints);

    colorCodeLabel2.setText("G=");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(colorCodeLabel2, gridBagConstraints);

    colorCodeLabel3.setText("B=");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(colorCodeLabel3, gridBagConstraints);

    jPanel3.add(jPanel2);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    add(jPanel3, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
    //色の作成ボタン
  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      if (selectedLabel != null) {
          selectedLabel.setBorder(emptyBorder); //前回の選択線を消去
      }
      colorDialog.setVisible(true);
      //選択された色は、JColorChooserに登録されているリスナから、setColorToLabel()が
      //呼び出される事で、selectedColorにセットされる。
  }//GEN-LAST:event_jButton1ActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel colorCodeLabel1;
  private javax.swing.JLabel colorCodeLabel2;
  private javax.swing.JLabel colorCodeLabel3;
  private javax.swing.JButton jButton1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel palettePanel;
  // End of variables declaration//GEN-END:variables
}
