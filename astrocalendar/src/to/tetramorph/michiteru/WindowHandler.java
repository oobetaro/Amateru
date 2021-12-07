/*
 * WindowHandler.java
 *
 * Created on 2008/02/12, 1:41
 *
 */

package to.tetramorph.michiteru;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

/**
 * フレームの移動やリサイズの際に、それらのパラメターをプロパティに書きこむ。
 * このクラスのインスタンスは親となるFrameのComponentListenerとして登録して使う。
 * @author 大澤義孝
 */
class WindowHandler extends ComponentAdapter {
    Frame component;
    Properties masterProp;
    WindowHandler(Frame owner,Properties masterProp) {
        this.component = owner;
        this.masterProp = masterProp;
    }
    void setRect(ComponentEvent e) {
        Rectangle rect = e.getComponent().getBounds();
        masterProp.setProperty( "frame.rect", String.format("%d,%d,%d,%d",
            rect.x,rect.y,rect.width,rect.height));
        component.setPreferredSize(rect.getSize());
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        setRect(e);
    }
    @Override
    public void componentResized(ComponentEvent e) {
        setRect(e);
    }
    public void setBounds() {
        String value = masterProp.getProperty("frame.rect");
        if ( value == null ) {
            //component.setPreferredSize(new Dimension(600,600));
            component.setLocationRelativeTo(null);
        } else {
            String [] v = value.split(",");
            Rectangle r = new Rectangle( Integer.parseInt(v[0]),
                Integer.parseInt(v[1]),
                Integer.parseInt(v[2]),
                Integer.parseInt(v[3]) );
            component.setBounds(r);
            component.setPreferredSize(r.getSize());
        }
    }
}
