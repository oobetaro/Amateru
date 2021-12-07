/*
 * NewJFrame2.java
 *
 * Created on 2008/01/09, 16:08
 */

package to.tetramorph.starbase.test;

import java.util.*;
import javax.swing.*;

/**
 *
 */
public class NewJFrame2 extends javax.swing.JFrame {
    List<String> dummyList = new ArrayList<String>();
    
    public NewJFrame2() {
        initComponents();
        jList1.clearSelection();
        for ( int i=0; i<300; i++ ) {
            StringBuilder sb = new StringBuilder();
            int max = (int)(Math.random() * 100 + 1);
            //int max = 20;
            for(int j=0; j<max; j++) sb.append("A");
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
    void init() {
        
    }
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jScrollPane1.setViewportView(jList1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new NewJFrame2().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
