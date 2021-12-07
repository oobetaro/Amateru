/*
 * GComponentAdapter.java
 *
 * Created on 2007/10/05, 9:48
 *
 */

package to.tetramorph.starbase.chartparts;

/**
 * GComponentListenerのアダプタークラス。
 * @author 大澤義鷹
 */
public abstract class GComponentAdapter implements GComponentListener {
    /**
     * GComponentがクリックされた時に呼び出される。
     */
    public void componentClicked(GComponentEvent evt) {}
    
    /**
     * GComponentがダブルクリックされた時に呼び出される。
     */
    public void componentDoubleClicked(GComponentEvent evt) {}
    
    /**
     * GComponentにマウスカーソルが接触したときに呼び出される。
     */
    public void componentOnCursor(GComponentEvent evt) {}
    
    /**
     * GComponentからマウスカーソルが離れたときに呼び出される。
     */
    public void componentOutCursor(GComponentEvent evt) {}
}
