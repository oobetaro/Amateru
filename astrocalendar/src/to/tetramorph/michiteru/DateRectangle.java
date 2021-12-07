/*
 * DateRectangle.java
 *
 * Created on 2008/02/13, 6:37
 *
 */

package to.tetramorph.michiteru;

import java.awt.Rectangle;
import java.util.Calendar;

/**
 * カレンダーの日にちセルの、当たり判定用。
 * @author 大澤義孝
 */
class DateRectangle extends Rectangle {
    Calendar dateCal;
    /**
     * DateRectangle オブジェクトを作成する。
     * Rectangleと同じだが、最後にカレンダーフィールドが追加されている。
     */
    public DateRectangle( int x, int y,int width, int height, Calendar cal ) {
        super( x, y, width, height);
        this.dateCal = cal;
    }
    @Override
    public String toString() {
        String s = "時刻未登録";
        if ( dateCal != null ) s = String.format("%tF %tT",dateCal,dateCal);
        return s + " " + super.toString();
    }
}
