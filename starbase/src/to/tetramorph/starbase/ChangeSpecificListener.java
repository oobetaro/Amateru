/*
 * ChangeSpecificListener.java
 *
 * Created on 2008/10/03, 18:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;

/**
 * SpecificDialogで仕様が変更されたとき、その通達を受け取るリスナを作るための
 * インターフェイス。ChangeListenerの名前を変更しただけのものと考えて良い。
 * @author 大澤義鷹
 */
public interface ChangeSpecificListener extends EventListener {
    void stateChanged( ChangeEvent e );
}
