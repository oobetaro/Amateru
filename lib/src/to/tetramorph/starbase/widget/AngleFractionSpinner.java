/*
 * AngleFractionSpinner.java
 *
 * Created on 2008/11/21, 21:12
 *
 */

package to.tetramorph.starbase.widget;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import to.tetramorph.starbase.lib.Unit;
import to.tetramorph.starbase.util.AngleConverter;

/**
 * 十進モード、六十進モードに応じて、0〜59または0から99までの値を入力可能な
 * スピナー。モードはAngleConverter.getAngleUnit()から取得している。
 * @author 大澤義鷹
 */
public class AngleFractionSpinner extends JSpinner {
    private int max;

    /**  
     * AngleFractionSpinner オブジェクトを作成する 
     */
    public AngleFractionSpinner() {
        super();
        updateUnit();
    }
    
    /**
     * このスピナーの単位系を変更する。現在設定されている単位系プロパティの値に
     * したがって、このスピナーのモデルを作り替えてセットする。
     */
    public void updateUnit() {
        max = ( AngleConverter.getAngleUnit() == AngleConverter.DECIMAL ) ?
            99 : 59;
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, max, 1);
        this.setModel( model );        
    }
    
    /**
     * このスピナーの値を十進の浮動小数にして返す。六十進モードのときは、十進に
     * 変換した値を返す。値は0.99とか0以下の値。
     */
    public double getFraction() {
        double v = (double)((Integer)getValue());
        if ( max == 99 )
            return v / 100.;
        return Unit.decimal( v / 100. );
    }
}
