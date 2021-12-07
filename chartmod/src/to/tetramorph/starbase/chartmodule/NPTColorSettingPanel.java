/*
 * NatalColorConfPanel.java
 *
 * Created on 2007/02/21, 13:20
 */
package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import to.tetramorph.starbase.lib.AspectStyle;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.util.Preference;

/**
 * NPT三重円の色設定を行うパネル。
 * @author 大澤義鷹
 */
public class NPTColorSettingPanel extends CustomizePanel {

    private static final String BGColor = "BGColor";
    private static final String ZodiacBGColors = "ZodiacBGColors";
    private static final String ZodiacSymbolColors = "ZodiacSymbolColors";
    private static final String ZodiacSymbolBorderColors = "ZodiacSymbolBorderColors";
    private static final String ZodiacRingBorderColor = "ZodiacRingBorderColor";
    private static final String ZodiacGaugeColor = "ZodiacGaugeColor";
    private static final String isZodiacGauge = "isZodiacGauge";
    private static final String isNoZodiacGauge = "isNoZodiacGauge";
    private static final String isNoSignSymbolBorders = "isNoSignSymbolBorders";
    private static final String isNoZodiacBackground = "isNoZodiacBackground";
    private static final String isNoZodiacRingBorder = "isNoZodiacRingBorder";
    private static final String HouseBGColors = "HouseBGColors";
    private static final String HouseNumberColors = "HouseNumberColors";
    private static final String HousesGaugeColor = "HousesGaugeColor";
    private static final String CuspsColor = "CuspsColor";
    private static final String HousesBorderColor = "HousesBorderColor";
    private static final String BodysBorderColor = "BodysBorderColor";
    private static final String BodysColor = "BodysColor";
    private static final String BodysDegreeColor = "BodysDegreeColor";
    private static final String OuterHousesNumberColor = "OuterHousesNumberColor";
    private static final String OuterCuspsDegreeColor = "OuterCuspsDegreeColor";
    private static final String OuterCuspsColor = "OuterCuspsColor";
    private static final String BodysHighLightColor = "BodysHighLightColor";
    private static final String HousesHighLightColor = "HousesHighLightColor";
    private static final String RingTextColor = "RingTextColor";
    private static final String LeadingLineColor = "LeadingLineColor";
    private static final String isNoHousesGuage = "isNoHousesGuage";
    private static final String isNoHousesBG = "isNoHousesBG";
    private static final String isNoBodysBorder = "isBodysBorder";
    private static final String BodysEffect = "BodysEffect";
    //
    private static final String NameColor = "NameColor";
    private static final String DateColor = "DateColor";
    private static final String PlaceColor = "PlaceColor";
    private static final String ListBodyColor = "ListBodyColor";
    private static final String ListSignColor = "ListSignColor";
    private static final String ListAngleColor = "ListAngleColor";
    private static final String ListRevColor = "ListRevColor";
    private static final String ListHouseNumberColor = "ListHouseNumberColor";
    private static final String ListOtherColor = "ListOtherColor";
    private static final String ListHighLightColor = "ListHighLightColor";
    //
    private static final String AspectStyles = "AspectStyles";
    private static final String AspectCircleBGColor = "AspectCircleBGColor";
    private static final String isNoAspectCircleBGColor = "isNoAspectCircleBGColor";
    //
    private NPTTextDisplayPanel textPanel;
    private Color[] _backgroundColor = new Color[]{Color.WHITE};

    /** Creates new form NatalColorConfPanel */
    public NPTColorSettingPanel() {
        initComponents();
        textPanel = textSettingPanel._display;
        ringsPanel.setBGColor(_backgroundColor);
        textPanel._backgroundColor = _backgroundColor;
    }
    //パネル内の設定情報を引数のprefに書きこんで返す。

    @Override
    public Preference getPreference(Preference pref) {
        pref.setColor(BGColor, getBGColor());
        pref.setColors(ZodiacBGColors, getZodiacBGColors());
        pref.setColors(ZodiacSymbolColors, getZodiacSymbolColors());
        pref.setColors(ZodiacSymbolBorderColors, getZodiacSymbolBorderColors());
        pref.setColor(ZodiacRingBorderColor, getZodiacRingBorderColor());
        pref.setColor(ZodiacGaugeColor, getZodiacGaugeColor());
        pref.setBoolean(isZodiacGauge, isNoZodiacGauge());
        pref.setBoolean(isNoZodiacGauge, isNoZodiacGauge());
        pref.setBoolean(isNoSignSymbolBorders, isNoSignSymbolBorders());
        pref.setBoolean(isNoZodiacBackground, isNoZodiacBackground());
        pref.setBoolean(isNoZodiacRingBorder, isNoZodiacRingBorder());
        for (int npt = 0; npt < 3; npt++) {
            pref.setColors(HouseBGColors + npt, getHouseBGColors(npt));
            pref.setColors(HouseNumberColors + npt, getHouseNumberColors(npt));
            pref.setColor(HousesGaugeColor + npt, getHousesGaugeColor(npt));
            pref.setColor(CuspsColor + npt, getCuspsColor(npt));
            pref.setColor(HousesBorderColor + npt, getHousesBorderColor(npt));
            pref.setColor(BodysBorderColor + npt, getBodysBorderColor(npt));
            pref.setColor(BodysColor + npt, getBodysColor(npt));
            pref.setColor(BodysDegreeColor + npt, getBodysDegreeColor(npt));
            pref.setColor(OuterHousesNumberColor + npt, getOuterHousesNumberColor(npt));
            pref.setColor(OuterCuspsDegreeColor + npt, getOuterCuspsDegreeColor(npt));
            pref.setColor(OuterCuspsColor + npt, getOuterCuspsColor(npt));
            pref.setColor(BodysHighLightColor + npt, getBodysHighLightColor(npt));
            pref.setColor(HousesHighLightColor + npt, getHousesHighLightColor(npt));
            pref.setColor(LeadingLineColor + npt, getLeadingLineColor(npt));
            pref.setBoolean(isNoHousesGuage + npt, isNoHousesGuage(npt));
            pref.setBoolean(isNoHousesBG + npt, isNoHousesBG(npt));
            pref.setInteger(BodysEffect + npt, getBodysEffect(npt));
            pref.setColor(RingTextColor + npt, getRingTextColor(npt));
        }
        pref.setColor(NameColor, getNameColor());
        pref.setColor(DateColor, getDateColor());
        pref.setColor(PlaceColor, getPlaceColor());
        pref.setColor(ListBodyColor, getListBodyColor());
        pref.setColor(ListSignColor, getListSignColor());
        pref.setColor(ListAngleColor, getListAngleColor());
        pref.setColor(ListRevColor, getListRevColor());
        pref.setColor(ListHouseNumberColor, getListHouseNumberColor());
        pref.setColor(ListOtherColor, getListOtherColor());
        pref.setColor(ListHighLightColor, getListHighLightColor());
        pref.setProperty(AspectStyles, getAspectStylesString());
        pref.setColor(AspectCircleBGColor, getAspectCircleBGColor());
        pref.setBoolean(isNoAspectCircleBGColor, isNoAspectCircleBGColor());
        return pref;
    }
    //prefの値をパネルに反映させる。

    @Override
    public void setPreference(Preference pref) {
        setBGColor(pref.getColor(BGColor, getBGColor()));
        setZodiacBGColors(pref.getColors(ZodiacBGColors, getZodiacBGColors()));
        setZodiacSymbolColors(pref.getColors(ZodiacSymbolColors, getZodiacSymbolColors()));
        setZodiacSymbolBorderColors(pref.getColors(ZodiacSymbolBorderColors, getZodiacSymbolBorderColors()));
        setZodiacRingBorderColor(pref.getColor(ZodiacRingBorderColor, getZodiacRingBorderColor()));
        setZodiacGaugeColor(pref.getColor(ZodiacGaugeColor, getZodiacGaugeColor()));
        setNoZodiacGauge(pref.getBoolean(isZodiacGauge, isNoZodiacGauge()));
        setNoZodiacGauge(pref.getBoolean(isNoZodiacGauge, isNoZodiacGauge()));
        setNoSignSymbolBorders(pref.getBoolean(isNoSignSymbolBorders, isNoSignSymbolBorders()));
        setNoZodiacBackground(pref.getBoolean(isNoZodiacBackground, isNoZodiacBackground()));
        setNoZodiacRingBorder(pref.getBoolean(isNoZodiacRingBorder, isNoZodiacRingBorder()));
        for (int npt = 0; npt < 3; npt++) {
            setHouseBGColors(npt, pref.getColors(HouseBGColors + npt, getHouseBGColors(npt)));
            setHouseNumberColors(npt, pref.getColors(HouseNumberColors + npt, getHouseNumberColors(npt)));
            setHousesGaugeColor(npt, pref.getColor(HousesGaugeColor + npt, getHousesGaugeColor(npt)));
            setCuspsColor(npt, pref.getColor(CuspsColor + npt, getCuspsColor(npt)));
            setHousesBorderColor(npt, pref.getColor(HousesBorderColor + npt, getHousesBorderColor(npt)));
            setBodysBorderColor(npt, pref.getColor(BodysBorderColor + npt, getBodysBorderColor(npt)));
            setBodysColor(npt, pref.getColor(BodysColor + npt, getBodysColor(npt)));
            setBodysDegreeColor(npt, pref.getColor(BodysDegreeColor + npt, getBodysDegreeColor(npt)));
            setOuterHousesNumberColor(npt, pref.getColor(OuterHousesNumberColor + npt, getOuterHousesNumberColor(npt)));
            setOuterCuspsDegreeColor(npt, pref.getColor(OuterCuspsDegreeColor + npt, getOuterCuspsDegreeColor(npt)));
            setOuterCuspsColor(npt, pref.getColor(OuterCuspsColor + npt, getOuterCuspsColor(npt)));
            setBodysHighLightColor(npt, pref.getColor(BodysHighLightColor + npt, getBodysHighLightColor(npt)));
            setHousesHighLightColor(npt, pref.getColor(HousesHighLightColor + npt, getHousesHighLightColor(npt)));
            setLeadingLineColor(npt, pref.getColor(LeadingLineColor + npt, getLeadingLineColor(npt)));
            setNoHousesGuage(npt, pref.getBoolean(isNoHousesGuage + npt, isNoHousesGuage(npt)));
            setNoHousesBG(npt, pref.getBoolean(isNoHousesBG + npt, isNoHousesBG(npt)));
            setBodysEffect(npt, pref.getInteger(BodysEffect + npt, getBodysEffect(npt)));
            setRingTextColor(npt, pref.getColor(RingTextColor + npt, getRingTextColor(npt)));
        }
        setNameColor(pref.getColor(NameColor, getNameColor()));
        setDateColor(pref.getColor(DateColor, getDateColor()));
        setPlaceColor(pref.getColor(PlaceColor, getPlaceColor()));
        setListBodyColor(pref.getColor(ListBodyColor, getListBodyColor()));
        setListSignColor(pref.getColor(ListSignColor, getListSignColor()));
        setListAngleColor(pref.getColor(ListAngleColor, getListAngleColor()));
        setListRevColor(pref.getColor(ListRevColor, getListRevColor()));
        setListHouseNumberColor(pref.getColor(ListHouseNumberColor, getListHouseNumberColor()));
        setListOtherColor(pref.getColor(ListOtherColor, getListOtherColor()));
        setListHighLightColor(pref.getColor(ListHighLightColor, getListHighLightColor()));
        setAspectStyles(pref.getProperty(AspectStyles, getAspectStylesString()));
        setAspectCircleBGColor(pref.getColor(AspectCircleBGColor, getAspectCircleBGColor()));
        setNoAspectCircleBGColor(pref.getBoolean(isNoAspectCircleBGColor, isNoAspectCircleBGColor()));
    }

    @Override
    public boolean isCorrect(String[] errmsg) {
        return true;
    }

    /***************************************************************************
     * ここよりgetメソッド
     ***************************************************************************/
    /** 全体の背景色を返す。*/
    public Color getBGColor() {
        return _backgroundColor[0];
    }

    /** 各サインの背景色を返す*/
    public Color[] getZodiacBGColors() {
        return ringsPanel._zodiacBGColors;
    }

    /** 各サインのフォントカラーを返す。 */
    public Color[] getZodiacSymbolColors() {
        return ringsPanel._zodiacFGColors;
    }

    /** 各サインのフォント縁取り色を返す。 */
    public Color[] getZodiacSymbolBorderColors() {
        return ringsPanel._zodiacSymbolBorderColors;
    }

    /** 獣帯リングの線色 */
    public Color getZodiacRingBorderColor() {
        return ringsPanel._zodiacRingBorderColor[0];
    }

    /** 獣帯リングのゲージ色 */
    public Color getZodiacGaugeColor() {
        return ringsPanel._zodiacGaugeColor[0];
    }

    /** 獣帯リングのゲージが不要のときはtrueを返す。*/
    public boolean isNoZodiacGauge() {
        return ringsPanel._isNoZodiacGauge[0];
    }

    /** サインシンボルの縁取り線が不要のときはtrue */
    public boolean isNoSignSymbolBorders() {
        return ringsPanel._isNoZodiacSymbolsBorder[0];
    }

    /** サインの背景色不要のときはtrue */
    public boolean isNoZodiacBackground() {
        return ringsPanel._isNoZodiacBG[0];
    }

    /** サインリングの線が不要のときはtrue */
    public boolean isNoZodiacRingBorder() {
        return false;
    }

    /** 1ハウス背景色を返す(12室分)。*/
    public Color[] getHouseBGColors(int npt) {
        return ringsPanel._displayPanels[ npt]._houseBGColors;
    }

    /** ハウス番号色を返す(12室分)。*/
    public Color[] getHouseNumberColors(int npt) {
        return ringsPanel._displayPanels[ npt]._houseNumberColors;
    }

    /** ハウスのゲージ色を返す。*/
    public Color getHousesGaugeColor(int npt) {
        return ringsPanel._displayPanels[ npt]._housesGaugeColor;
    }

    /** ハウスカスプの色を返す。*/
    public Color getCuspsColor(int npt) {
        return ringsPanel._displayPanels[ npt]._cuspsColor;
    }

    /** ハウスのボーダー色を返す。(ハウスの円周線) */
    public Color getHousesBorderColor(int npt) {
        return ringsPanel._displayPanels[ npt]._housesBorderColor;
    }

    /** 天体縁取り色を返す。*/
    public Color getBodysBorderColor(int npt) {
        return ringsPanel._displayPanels[ npt]._bodysBorderColor;
    }

    /** 天体シンボル色を返す。*/
    public Color getBodysColor(int npt) {
        return ringsPanel._displayPanels[ npt]._bodysColor;
    }

    /** 天体度数色を返す。*/
    public Color getBodysDegreeColor(int npt) {
        return ringsPanel._displayPanels[ npt]._bodysDegreeColor;
    }

    /** 獣帯円外のハウス番号色を返す。*/
    public Color getOuterHousesNumberColor(int npt) {
        return ringsPanel._displayPanels[ npt]._outerHousesNumberColor;
    }

    /** 獣帯円外のカスプ度数色を返す。*/
    public Color getOuterCuspsDegreeColor(int npt) {
        return ringsPanel._displayPanels[ npt]._outerCuspsDegreeColor;
    }

    /** 獣帯円外のカスプ線色を返す。*/
    public Color getOuterCuspsColor(int npt) {
        return ringsPanel._displayPanels[ npt]._outerCuspsColor;
    }

    /** 天体ハイライト(オンカーソル)色を返す。*/
    public Color getBodysHighLightColor(int npt) {
        return ringsPanel._displayPanels[ npt]._bodysHighLightColor;
    }

    /** ハウスハイライト(オンカーソル)色を返す。*/
    public Color getHousesHighLightColor(int npt) {
        return ringsPanel._displayPanels[ npt]._housesHighLightColor;
    }

    /** 天体とゲージを結ぶ引き出し線色を返す。*/
    public Color getLeadingLineColor(int npt) {
        return ringsPanel._displayPanels[ npt]._leadingLineColor;
    }

    /** ハウス上のゲージが不要のときはtrueを返す。*/
    public boolean isNoHousesGuage(int npt) {
        return ringsPanel._displayPanels[ npt]._isNoHousesGauge;
    }

    /** ハウス背景色が不要のときはtrueを返す。*/
    public boolean isNoHousesBG(int npt) {
        return ringsPanel._displayPanels[ npt]._isNoHousesBG;
    }

    /** 天体縁取りが不要のときはtrueを返す。*/
    public int getBodysEffect(int npt) {
        return ringsPanel._displayPanels[ npt]._bodysEffect;
    }

    /** この円の説明(Natal,Transit等)テキスト色 */
    public Color getRingTextColor(int npt) {
        return ringsPanel._displayPanels[ npt]._ringTextColor;
    }
    //-------------------------------------------------------------------------

    public Color getNameColor() {
        return textPanel.getNameColor();
    }

    public Color getDateColor() {
        return textPanel.getDateColor();
    }

    public Color getPlaceColor() {
        return textPanel.getPlaceColor();
    }

    public Color getListBodyColor() {
        return textPanel.getBodyColor();
    }

    public Color getListSignColor() {
        return textPanel.getSignColor();
    }

    public Color getListAngleColor() {
        return textPanel.getAngleColor();
    }

    public Color getListRevColor() {
        return textPanel.getRevColor();
    }

    public Color getListHouseNumberColor() {
        return textPanel.getHouseNumberColor();
    }

    public Color getListOtherColor() {
        return textPanel.getOtherColor();
    }

    public Color getListHighLightColor() {
        return textPanel.getHighLightColor();
    }

    //-------------------------------------------------------------------------
    private String getAspectStylesString() {
        return aspectsPanel.getAspectStylesString();
    }

    public AspectStyle[] getAspectStyles() {
        return aspectsPanel.getAspectStyles();
    }

    public Color getAspectCircleBGColor() {
        return aspectsPanel.getAspectBGColor();
    }

    public boolean isNoAspectCircleBGColor() {
        return aspectsPanel.isNoAspectBG();
    }

    /***************************************************************************
     * ここよりsetメソッド
     ***************************************************************************/
    public void setBGColor(Color color) {
        _backgroundColor[0] = color;
        ringsPanel.setBGColor(_backgroundColor);
        textPanel._backgroundColor[0] = color;
    }

    public void setZodiacBGColors(Color[] colors) {
        copy(colors, ringsPanel._zodiacBGColors);
    }

    /** 各サインのフォントカラーを返す。 */
    public void setZodiacSymbolColors(Color[] colors) {
        copy(colors, ringsPanel._zodiacFGColors);
    }

    /** 各サインのフォント縁取り色を返す。 */
    public void setZodiacSymbolBorderColors(Color[] colors) {
        copy(colors, ringsPanel._zodiacSymbolBorderColors);
    }

    public void setZodiacRingBorderColor(Color color) {
        ringsPanel._zodiacRingBorderColor[0] = color;
    }

    public void setZodiacGaugeColor(Color color) {
        ringsPanel._zodiacGaugeColor[0] = color;
    }

    public void setNoZodiacGauge(boolean b) {
        ringsPanel._isNoZodiacGauge[0] = b;
    }

    public void setNoSignSymbolBorders(boolean b) {
        ringsPanel._isNoZodiacSymbolsBorder[0] = b;
    }

    public void setNoZodiacBackground(boolean b) {
        ringsPanel._isNoZodiacBG[0] = b;
    }

    public void setNoZodiacRingBorder(boolean b) {
        ringsPanel._isNoZodiacRingBorder[0] = b;
    }

    public void setHouseBGColors(int npt, Color[] colors) {
        copy(colors, ringsPanel._displayPanels[ npt]._houseBGColors);
    }

    public void setHouseNumberColors(int npt, Color[] colors) {
        copy(colors, ringsPanel._displayPanels[ npt]._houseNumberColors);
    }

    public void setHousesGaugeColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._housesGaugeColor = color;
    }

    public void setCuspsColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._cuspsColor = color;
    }

    public void setHousesBorderColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._housesBorderColor = color;
    }

    public void setBodysBorderColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._bodysBorderColor = color;
    }

    public void setBodysColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._bodysColor = color;
    }

    public void setBodysDegreeColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._bodysDegreeColor = color;
    }

    public void setOuterHousesNumberColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._outerHousesNumberColor = color;
    }

    public void setOuterCuspsDegreeColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._outerCuspsDegreeColor = color;
    }

    public void setOuterCuspsColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._outerCuspsColor = color;
    }

    public void setBodysHighLightColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._bodysHighLightColor = color;
    }

    public void setHousesHighLightColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._housesHighLightColor = color;
    }

    public void setLeadingLineColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._leadingLineColor = color;
    }

    public void setNoHousesGuage(int npt, boolean b) {
        ringsPanel._displayPanels[ npt]._isNoHousesGauge = b;
    }

    public void setNoHousesBG(int npt, boolean b) {
        ringsPanel._displayPanels[ npt]._isNoHousesBG = b;
    }

    public void setRingTextColor(int npt, Color color) {
        ringsPanel._displayPanels[ npt]._ringTextColor = color;
    }

    /**
     * 1なら縁取り、2ならドロップシャドウ、0なら効果なし
     */
    public void setBodysEffect(int npt, int value) {
        ringsPanel._displayPanels[ npt]._bodysEffect = value;
    }
    //---------------------------------------------------------------------------

    public void setNameColor(Color color) {
        textPanel.setNameColor(color);
    }

    public void setDateColor(Color color) {
        textPanel.setDateColor(color);
    }

    public void setPlaceColor(Color color) {
        textPanel.setPlaceColor(color);
    }

    public void setListBodyColor(Color color) {
        textPanel.setBodyColor(color);
    }

    public void setListSignColor(Color color) {
        textPanel.setSignColor(color);
    }

    public void setListAngleColor(Color color) {
        textPanel.setAngleColor(color);
    }

    public void setListRevColor(Color color) {
        textPanel.setRevColor(color);
    }

    public void setListHouseNumberColor(Color color) {
        textPanel.setHouseNumberColor(color);
    }

    public void setListOtherColor(Color color) {
        textPanel.setOtherColor(color);
    }

    public void setListHighLightColor(Color color) {
        textPanel.setHighLightColor(color);
    }
    //--------------------------------------------------------------------------

    public void setAspectStyles(AspectStyle[] styles) {
        aspectsPanel.setAspectStyles(styles);
    }

    public void setAspectCircleBGColor(Color color) {
        aspectsPanel.setAspectBGColor(color);
    }

    public void setNoAspectCircleBGColor(boolean b) {
        aspectsPanel.setNoAspectBG(b);
    }

    private void setAspectStyles(String value) {
        aspectsPanel.setAspectStyles(value);
    }

    private void copy(Color[] src, Color[] dist) {
        System.arraycopy(src, 0, dist, 0, dist.length);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        tabbedPane = new javax.swing.JTabbedPane();
        ringsPanel = new to.tetramorph.starbase.chartmodule.NPTRingsColorSettingPanel();
        textSettingPanel = new to.tetramorph.starbase.chartmodule.NPTTextSettingPanel();
        aspectsPanel = new to.tetramorph.starbase.widget.AspectsPanel();

        setLayout(new java.awt.GridLayout(1, 0));

        tabbedPane.addTab("\u5929\u4f53\u30ea\u30f3\u30b0", ringsPanel);

        tabbedPane.addTab("\u30c6\u30ad\u30b9\u30c8", textSettingPanel);

        tabbedPane.addTab("\u30a2\u30b9\u30da\u30af\u30c8", aspectsPanel);

        add(tabbedPane);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private to.tetramorph.starbase.widget.AspectsPanel aspectsPanel;
    private to.tetramorph.starbase.chartmodule.NPTRingsColorSettingPanel ringsPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private to.tetramorph.starbase.chartmodule.NPTTextSettingPanel textSettingPanel;
    // End of variables declaration//GEN-END:variables
}
