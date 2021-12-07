/*
 * DateHandler.java
 *
 * Created on 2007/01/15, 15:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.astrocalendar;

/**
 * 日付を先に送ったり戻したり現在時刻にリセットする操作。
 * モードによってそれらをすげかえるためのもの。
 * エニアグラムカレンダーで使用する。
 * @author 大澤義鷹
 */
interface DateHandler {
    public void next();
    public void back();
    public void now();
    public void set();
}
