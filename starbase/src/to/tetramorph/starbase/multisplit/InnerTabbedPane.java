/*
 * InnerTabbedPane.java
 *
 * Created on 2007/10/30, 1:53
 *
 */
package to.tetramorph.starbase.multisplit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import to.tetramorph.starbase.test.TreeOfLifePanel;
import to.tetramorph.starbase.widget.TabCloseButton;

/**
 * MultiTabbedPaneの中で使用されるカスタマイズされたJTabbedPane。
 * @author 大澤義鷹
 */
public class InnerTabbedPane extends JTabbedPane {

    /**  InnerTabbedPane オブジェクトを作成する */
    public InnerTabbedPane() {
        super();
        setUI(new MyUI());
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT); //これを入れないとタブの高さが変化
        setBorder(new ShallowBevelBorder());
        //別のTabbedPaneをクリックしたとき一度目ではフォーカスが来ないが、このリスナ
        //でフォーカスを要求する。
        addMouseListener(new MouseAdapter() {

            @Override
            //タブがクリックされたらフォーカスを呼ぶ
            public void mouseReleased(MouseEvent evt) {
                boolean b = requestFocusInWindow();
            //System.out.println("フォーカスを要求したら = " + b);
            //実際しばしばfalseが返るが、動作に問題は見受けられない
            }

            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int i = indexAtLocation(evt.getX(), evt.getY());
                    if (i >= 0 && l != null) {
                        l.doubleClicked(THIS, getSelectedComponent());
                    }
                }
            }
        });
    }

    public void updateUI() {
        setUI(new MyUI());
        revalidate();
    }

    public void addTab(String title, Component c) {
        super.addTab(null, c);
        int tc = getTabCount() - 1;
        setTabComponentAt(tc, new TabComponent(title, null));
    }

    public void addTab(String title, Icon icon, Component c) {
        super.addTab(title, icon, c);
        int tc = getTabCount() - 1;
        setTabComponentAt(tc, new TabComponent(title, icon));
    }

    public void insertTab(String title, Icon icon, Component c, int index) {
        super.insertTab(title, icon, c, null, index);
        setTabComponentAt(index, new TabComponent(title, icon));
    }

    public void setIconAt(int index, Icon icon) {
        TabComponent tabComp = (TabComponent) getTabComponentAt(index);
        tabComp.setIcon(icon);
        super.setIconAt(index, icon);
    }

    public void setTitleAt(int index, String title) {
        TabComponent tabComp = (TabComponent) getTabComponentAt(index);
        tabComp.setTitle(title);
        super.setTitleAt(index, title);
    }

    public void setTabComponentAt(int index, Component c) {
        if (c instanceof TabComponent) {
            TabComponent tabc = (TabComponent) c;
            super.setIconAt(index, tabc.getIcon());
            super.setTitleAt(index, tabc.getTitle());
        }
        super.setTabComponentAt(index, c);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("InnerTabbedPane (");
        for (int i = 0; i < getTabCount(); i++) {
            sb.append(getTitleAt(i));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }
    protected TabbedPaneListener l;
    protected boolean hide = false;

    public void setTabbedPaneListener(TabbedPaneListener l) {
        this.l = l;
    }
    private InnerTabbedPane THIS = this;

    /**
     * JTabbedPaneの中に入れる×ボタンつきラベル
     */
    class TabComponent extends JComponent {

        JLabel label;
        TabCloseButton button = new TabCloseButton();

        TabComponent(String title, Icon icon) {
            setLayout(new BorderLayout(5, 0));
            setBorder(null);
            label = new JLabel(title, JLabel.LEFT);
            if (icon != null) {
                label.setIcon(icon);
            }
            add(label, BorderLayout.CENTER);
            TabCloseButton button = new TabCloseButton();
            add(button, BorderLayout.EAST);
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    Component tabComp = ((Component) event.getSource()).getParent();
                    //TabComponentから、何番目のタブが閉じたのかを求める
                    int tabCompIndex = indexOfTabComponent(tabComp);
                    //System.out.println("indexOfTabComponent = " +  tabCompIndex );
                    // addTabされて登録されたときのComponent(ChartPaneなど)を取り出す
                    Component c = InnerTabbedPane.this.getComponentAt(tabCompIndex);
                    InnerTabbedPane.this.remove(tabCompIndex);
                    if (l != null) {
                        l.closedTab(THIS, c);
                    }
                }
            });
        }

        void setIcon(Icon icon) {
            label.setIcon(icon);
        }

        void setTitle(String title) {
            label.setText(title);
        }

        Icon getIcon() {
            return label.getIcon();
        }

        String getTitle() {
            return label.getText();
        }
    }
    protected Rectangle glassBounds = new Rectangle();
    protected boolean onCursor = false;

    //主にタブ区画のカスタマイズ
    class MyUI extends BasicTabbedPaneUI {

        Color focusColor = new Color(72, 143, 251);

        protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                int selectedIndex,
                int x, int y, int w, int h) {
        }
        //コンテンツ区画の下のボーダー表示をやめる

        protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                int selectedIndex,
                int x, int y, int w, int h) {
        }
        //コンテンツ区画の右のボーダー表示をやめる

        protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                int selectedIndex,
                int x, int y, int w, int h) {
        }

        protected void paintTabBackground(Graphics g, int tabPlacement,
                int tabIndex,
                int x, int y, int w, int h,
                boolean isSelected) {
            Color selcol = UIManager.getColor("TabbedPane.selected");
            Color bgcol = Color.LIGHT_GRAY; //isSelected? selcol : tabPane.getBackgroundAt(tabIndex);
            //LaFによってはnullのことがあるのでその対策
            if (selcol == null) {
                selcol = bgcol;
            }
            Graphics2D g2 = (Graphics2D) g;
            int x2, y2;
            if (tabPane.hasFocus() && isSelected) {
                x2 = x + w;
                y2 = y;
                GradientPaint gp = new GradientPaint(x, y, focusColor, x2, y2, selcol, false);
                g2.setPaint(gp);
                g2.fill(new Rectangle(x + 1, y + 1, w - 3, h - 1));
            } else if (isSelected) {
                g2.setPaint(selcol);
                g2.fill(new Rectangle(x + 1, y + 1, w - 3, h - 1));
            } else {
                g2.setPaint(bgcol);
                g2.fill(new Rectangle(x + 1, y + 1, w - 3, h - 1));
            }

        }
        //タブ選択時に点線の矩形でタイトルが囲まれるのをやめる

        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                Rectangle[] rects, int tabIndex,
                Rectangle iconRect, Rectangle textRect,
                boolean isSelected) {
        }

        //タブ選択されたときタイトルが1pixcel上に上がるのをやめる
        protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
            Rectangle tabRect = rects[tabIndex];
            int nudge = 0;
            switch (tabPlacement) {
                case BOTTOM:
                    nudge = isSelected ? 1 : -1;
                    break;
                case LEFT:
                case RIGHT:
                    nudge = tabRect.height % 2;
                    break;
                case TOP:
                default:
                    nudge = 1;
            }
            return nudge;
        }
        //×ボタンの右横を少し詰める

        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(0, 5, 1, 2);
            contentBorderInsets = new Insets(1, 0, 0, 0);
        }
    }

    //テスト
    private static void createAndShowGUI() {

        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("カスタムTabbedPane");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        InnerTabbedPane tabpan = new InnerTabbedPane();
        TreeOfLifePanel[] trees = new TreeOfLifePanel[10];
        for (int i = 0; i < trees.length; i++) {
            trees[i] = new TreeOfLifePanel();
        }

        Icon maleIcon = new ImageIcon(
                InnerTabbedPane.class.getResource("/resources/sabianIcon.png"));
        Icon femaleIcon = new ImageIcon(
                InnerTabbedPane.class.getResource("/resources/Julius.png"));
        Icon[] icons = new Icon[]{maleIcon, femaleIcon};
        for (int i = 0; i < trees.length; i++) {
            trees[i].setBorder(null);
            trees[i].setNumber(i + 1);
            trees[i].setBackground(Color.LIGHT_GRAY);
            tabpan.addTab("Tree " + (i + 1), icons[i & 1], trees[i]);
        }
        frame.getContentPane().add(tabpan);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }
}
