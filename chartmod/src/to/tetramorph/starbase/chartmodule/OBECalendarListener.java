/*
 * OBECalendarListener.java
 *
 * Created on 2008/03/24, 23:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartmodule;

import java.util.GregorianCalendar;
import to.tetramorph.starbase.lib.TimePlace;

/**
 * 幽体離脱予報グラフの計算結果通知用。
 * @author 大澤義鷹
 */
public interface OBECalendarListener {
    /**
     * OBECalendarの計算結果が通達される。
     * OBECalendarのsetTimePlace()を呼び出すと、体脱予想カレンダーが計算によって
     * 生成される。これは少々時間がかかるが、別スレッドで計算される。計算完了
     * するとこのメソッドで計算結果が通達される。
     * @param transitTimePlace トランジットの日時と場所
     * @param natalTimePlace ネイタルの日時と場所
     * @param table 計算結果
     * @param transitCal 実際に計算されたトランジットの日付
     */
    public void calcurated( TimePlace transitTimePlace, 
                              TimePlace natalTimePlace, 
                              double [][] table,
                              GregorianCalendar transitCal );
}
