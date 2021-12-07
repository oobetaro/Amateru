/*
 * VerticalJButton.java
 *
 * Created on 2007/11/12, 23:19
 *
 */

package vbutton;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
/**
 * 縦型ボタンができないか2chで質問したら返ってきたソースコード
 */
public class VerticalJButton extends JComponent {
  private JButton button=new JButton(){
    public void repaint(long tm,int x,int y,int width,int height){
      VerticalJButton.this.repaint(tm,y,1-x,height,width);
    }
  };

  public VerticalJButton(String text) {
    add(button);
    button.setText(text);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }
  public void setIcon(Icon icon) {
    button.setIcon(icon);
  }
  public Dimension getPreferredSize(){
    Dimension d=button.getPreferredSize();
    return new Dimension(d.height,d.width);
  }
  
  public void doLayout(){
    button.setBounds(0,1-getWidth(),getHeight(),getWidth());
  }
  
  protected void paintChildren(Graphics g){
    Graphics2D g2=(Graphics2D) g.create();
    g2.rotate(Math.PI/2);
    g2.translate( button.getX(), button.getY() );
    button.paint(g2);
    g2.dispose();
  }
  
  protected void processMouseEvent(MouseEvent e){
    e=new MouseEvent( button,
                       e.getID(),
                       e.getWhen(),
                       e.getModifiers(),
                       e.getY()-button.getX(),
                       -e.getX()-button.getY(),
                       e.getClickCount(),
                       e.isPopupTrigger(),
                       e.getButton());
    button.dispatchEvent(e);
  }
  
  protected void processMouseMotionEvent(MouseEvent e){
    e = new MouseEvent( button,
                        e.getID(),
                        e.getWhen(),
                        e.getModifiers(),
                        e.getY()-button.getX(),
                        -e.getX()-button.getY(),
                        e.getClickCount(),
                        e.isPopupTrigger(),
                        e.getButton());
    button.dispatchEvent(e);
  }
}
