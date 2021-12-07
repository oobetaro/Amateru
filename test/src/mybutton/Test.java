/*
 * Test.java
 *
 * Created on 2007/12/13, 14:20
 *
 */

package to.tetramorph.starbase;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import to.tetramorph.util.IconLoader;

/**
 * 2chにLaFの違いよるレイアウトのズレ問題を投げたときに返ってきたコード。
 * 相手の環境ではズレは生じないらしいが、こっちの環境ではずれる。
 */
public class Test extends JFrame {
    static public void main(String args[]){
        //new Test().createUI("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        new Test().createUI("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
    }
    
    private void createUI(String laf){
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
        }
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(gbl);
        c.weightx = c.weighty = 1;
        JTextField t0 = new JTextField(10);
        c.gridx = c.gridy = 0;
        gbl.setConstraints(t0, c);
        p.add(t0);
        JTextField t1 = new JTextField(10);
        c.gridy = 1;
        gbl.setConstraints(t1, c);
        p.add(t1);
        JButton b = new JButton(IconLoader.getImageIcon("/resources/images/jisaNormal.png"));
        b.setPreferredSize(new Dimension(21,21));
        b.setMargin(new Insets(0,0,0,0));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setBorder(null);
        b.setRolloverIcon(IconLoader.getImageIcon("/resources/images/jisaRollover.png"));
        b.setPressedIcon(IconLoader.getImageIcon("/resources/images/jisaPressed.png"));
        c.gridx = 1;
        gbl.setConstraints(b, c);
        p.add(b);
        JTextField t2 = new JTextField(10);
        c.gridx = 0;
        c.gridy = 2;
        gbl.setConstraints(t2, c);
        p.add(t2);
        JTextField t3 = new JTextField(10);
        c.gridy = 3;
        gbl.setConstraints(t3, c);
        p.add(t3);
        getContentPane().add(p);
        this.pack();
        this.setVisible(true);
    }
}
