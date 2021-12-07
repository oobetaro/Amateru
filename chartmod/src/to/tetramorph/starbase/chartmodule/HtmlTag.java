/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/28 02:40
 */

package to.tetramorph.starbase.chartmodule;

import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import static to.tetramorph.starbase.lib.Const.*;

/**
 * HTMLのタグで、サインや天体シンボルのgifファイルにアクセスするタグを作成して
 * 返すユーティリティ。テキストとして返すモードも用意。
 * あまり汎用性を持たせる事ができないクラスなので、chartmoduleパッケージの中に
 * おいている。
 * レポートやアラビックパーツプラグインが使用。
 * @author 大澤義鷹
 */
public class HtmlTag {
    public static final int IMAGE_MODE = 0;
    public static final int TEXT_MODE = 1;
    private static final String IMG =
            "<img width=15 height=15 border=0 src='./symbols/";
    int mode;
    public HtmlTag() {
        this.mode = IMAGE_MODE;
    }

    public void setViewMode( int mode ) {
        this.mode = mode;
    }
    /**
     * 表示モードを返す。デフォルトは"IMAGE_MODE"。
     */
    public int getViewMode() {
        return mode;
    }
    public String getSignTag( Body p ) {
        if ( mode == TEXT_MODE )
            return  SIGN_NAMES[p.getSign()] + " ";
        //return IMG + ZODIAC_NAMES[ p.getSign() ] + ".gif'>";
        return "<span class='asym'>" + Const.ZODIAC_CHARS[ p.getSign() ] + "</span>";
    }
    public String getPlanetTag(Body p) {
        if ( mode == TEXT_MODE )
            return PLANET_NAMES[p.id] + " ";
        //return IMG + SYMBOL_NAMES[p.id]+ ".gif'>";
        return "<span class='asym'>" + Const.BODY_CHARS[ p.id ] + "</span>";
    }
    public String getPlanetTag(int id) {
        if ( mode == TEXT_MODE )
            return PLANET_NAMES[id] + " ";
        //return IMG + SYMBOL_NAMES[id]+ ".gif'>";
        return "<span class='asym'>" + Const.BODY_CHARS[ id ] + "</span>";
    }
    public String getAspectTag( int id ) {
        if ( mode == TEXT_MODE )
            return Const.ASPECT_NAMES[id];
        return "<span class='asym'>" + Const.ASPECT_CHARS[id] + "</span>";
    }
    //アスペクトのHTMLタグを返す。
    public String getAspectTag(Aspect a) {
        if ( mode == TEXT_MODE )
            return Const.ASPECT_NAMES[a.aid];
        //return IMG + ASPECT_SYMBOL_NAMES[a.aid]+ ".gif'>";
        return "<span class='asym'>" + Const.ASPECT_CHARS[ a.aid ] + "</span>";
    }
    public String getRetrogradeTag() {
        if ( mode == TEXT_MODE ) return "R";
        return "<span class='asym'>" + Const.RETROGRADE_CHAR + "</span>";
    }
}
