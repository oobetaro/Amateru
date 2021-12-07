/*
 * ZodiacRing.java
 *
 * Created on 2007/05/28, 3:09
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Component;

/**
 * 獣帯円のリングを描画する。
 * @author 大澤義鷹
 */
public class ZodiacRing extends ChartParts {
    private double ringWidth;
    private GaugeDial gaugeDial;
    private double outerDiameter;
    private HouseRing houseRing;
    private SignDial signDial;
    private Component component;
    /**
     * デフォルトの直径と幅でZodiacRingオブジェクトを作成する。
     * 直径は0.8,幅は0.0.5。
     */
    public ZodiacRing() {
        this( 0.8, 0.05 );
    }
    /**
     * リング直径と幅を指定してZodiacRingオブジェクトを作成する。
     */
    public ZodiacRing( double diameter, double ringWidth ) {
        gaugeDial = new GaugeDial();
        houseRing = new HouseRing();
        houseRing.setPaintFormula(15);
        signDial = new SignDial();
        setDiameter( diameter, ringWidth );
    }

    public void setBasePosition( BasePosition bp ) {
        super.setBasePosition(bp);
        gaugeDial.setBasePosition(bp);
        houseRing.setBasePosition(bp);
        signDial.setBasePosition(bp);
    }

    public void setDiameter( double diameter, double ringWidth ) {
        super.setDiameter(diameter);
        this.ringWidth = ringWidth;
        gaugeDial.setDiameter(diameter);
        this.outerDiameter = diameter + ringWidth * 2;
        houseRing.setDiameter(diameter);
        houseRing.setRingWidth(ringWidth);
        signDial.setDiameter( diameter + ringWidth); //幅の中央に
        signDial.setSymbolSize((float)ringWidth * 0.6f);
    }
    public double getRingWidth() {
        return ringWidth;
    }
    public void setRoll(double roll) {
        super.setRoll(roll);
        houseRing.setRoll(roll);
        gaugeDial.setRoll(roll);
        signDial.setRoll(roll);
    }
    public void setAscendant(double ascendant) {
        super.setAscendant(ascendant);
        houseRing.setAscendant(ascendant);
        gaugeDial.setAscendant(ascendant);
        signDial.setAscendant(ascendant);
    }
    public void setParentComponent(Component component) {
        this.component = component;
    }

    public void draw() {
        houseRing.draw();
        gaugeDial.draw();
        signDial.draw();
    }

    /**
     * 獣帯リングにx,y座標が重なるときはtrueを返す。
     */
    public boolean isRingContains(int x,int y) {
        Sector [] sectors = houseRing.getSectors();
        for(Sector sect : sectors) {
            if(sect.sector.contains(x,y)) return true;
        }
        return false;
    }
    /**
     * リング全体のハイライトを行うか行わないかを指定する。
     * trueを設定すると、獣帯円全体の輪郭線がハイライトカラーで表示される。
     */
    public void setHighLight(boolean b) {
        houseRing.setHighLight(b);
    }
    /**
     * リング全体の輪郭線がハイライト表示されている状態のときはtrueを返す。
     * つまりsetHighLight(true)を指定したとき、このメソッドはtrueを返す。
     */
    public boolean isHighLight() {
        return houseRing.isHighLight();
    }
    /**
     * 黄道十二星座の各背景色を指定する。
     * @param colors 12の要素をもったColor配列で、[0]は牡羊座で[11]は魚座の背景色
     * を意味する。背景色にnullをセットしてはならない。
     */
    public void setBackgroundColors(Color [] colors) {
        houseRing.setBGColors(colors);
    }
    /**
     * 獣帯リングの輪郭線を描く(flase)か描かないか(true)を指定する。
     */
    public void setNoSignRingBorder(boolean b) {
        houseRing.setNoBorder( b );
    }
    /**
     * 獣帯リングの輪郭線の色を指定する。
     */
    public void setSignRingLineColor(Color color) {
        houseRing.setInnerLineColor(color);
        houseRing.setOuterLineColor(color);
        houseRing.setCuspColor(color);
    }
    /**
     * 各サインすべての背景色を塗りつぶす(false)か塗りつぶさない(true)かを指定する。
     * 塗りつぶす指定をすると、setBackgroundColors()で設定した色で塗りつぶされる。
     * 塗りつぶさない指定をすると、設定されている色にかかわらず塗りつぶさないため、
     * 背景色が透けて見える描画となる。
     */
    public void setNoSignBackgrounds(boolean b) {
        houseRing.setNoBackground( b );
    }
    /**
     * 各サインのシンボルの色を指定する。
     * @param colors 12の要素をもつ配列で、[0]は牡羊座で[11]は魚座のシンボル色を
     * 意味する。各要素にnullをセットしてはならない。
     */
    public void setSignSymbolColors(Color [] colors) {
        signDial.setSymbolColors(colors);
    }
    /**
     * 各サインのシンボルに縁取りをつける場合の、縁取り色を指定する。
     * @param colors [0]を牡羊座として順番に12の要素で指定する。
     */
    public void setSymbolBorderColors(Color [] colors) {
        signDial.setSymbolBorderColors(colors);
    }
    /**
     * 各サインすべのシンボルに対して、縁取りをつける(false)かつけない(true)かを
     * 指定する。
     */
    public void setNoSymbolBorders(boolean b) {
        signDial.setNoSymbolBorders(b);
    }
    /**
     * ゲージの可視/不可視をセットする。
     */
    public void setNoZodiacGauge(boolean b) {
        gaugeDial.setVisible(! b);
    }
    /**
     * ゲージ色をセットする。
     */
    public void setZodiacGaugeColor(Color color) {
        gaugeDial.setGaugeColor(color);
    }

}
