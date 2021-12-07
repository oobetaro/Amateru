/*
 * BalloonHandler.java
 *
 * Created on 2008/02/28, 22:52
 *
 */

package to.tetramorph.starbase.chartmodule;

import to.tetramorph.starbase.widget.WordBalloonHandler;

/**
 * 天体の簡略説明をワードバルーンで表示するためのハンドラ。
 * PlanetHandler内の天体オンカーソル/アウトカーソルで使用。
 * @author 大澤義鷹
 */
public class BalloonHandler implements WordBalloonHandler {
    /**
     * オブジェクトを作成する。
     */
    public BalloonHandler() {
        
    }
    Object selectedObject;
    @Override
    public Object getSelectedObject() {
        return selectedObject;
    }
    @Override
    public void setSelectedObject(Object o) {
        selectedObject = o;
    }

}
