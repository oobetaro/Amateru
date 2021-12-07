/*
 * MutexListener.java
 *
 * Created on 2007/11/16, 22:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.michiteru;

/**
 * MutexServerからの通達を受け取るリスナ。
 * @author 大澤義孝
 */
interface MutexListener {
    /**
     * MutexServerが二重起動を検出したとき呼び出される。
     */
    public void mutexPerformed();
}
