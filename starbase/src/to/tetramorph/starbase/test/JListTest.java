/*
 * JListTest.java
 *
 * Created on 2008/01/09, 22:33
 *
 */

package to.tetramorph.starbase.test;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author 大澤義鷹
 */
public class JListTest extends JFrame {
    
    List<String> dummyList = new ArrayList<String>();
    private MyList jList1;
    private JScrollPane jScrollPane1;
    
    public JListTest() {
        initComponents();
        jList1.clearSelection();
        for ( int i=0; i<512; i++ ) {
            StringBuilder sb = new StringBuilder();
            //int max = (int)(Math.random() * 100 + 1);
            int max = 20;
            for(int j=0; j<max; j++) sb.append("B");
            dummyList.add(sb.toString() + "  " + i);
        }

        jList1.setModel(new AbstractListModel() {
            public int getSize() { return dummyList.size(); }
            public Object getElementAt(int index) {
                return dummyList.get(index);
            }            
        });
        pack();
    }

    class MyList extends JList {
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            int result = super.getScrollableBlockIncrement(visibleRect,orientation,direction);
            System.out.println(result);
            return result;
        }
        public boolean getScrollableTracksViewportHeight() {
            System.out.println("++++");
            return false;
        }
        public boolean getScrollableTracksViewportWidth() {
            System.out.println("***");
            return true;
        } 
    }
    
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new MyList();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jScrollPane1.setViewportView(jList1);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        pack();
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new JListTest().setVisible(true);
            }
        });
    }
    
}
