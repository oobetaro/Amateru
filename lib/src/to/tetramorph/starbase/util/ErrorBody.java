/*
 * ErrorBody.java
 *
 * Created on 2007/07/28, 20:49
 *
 */

package to.tetramorph.starbase.util;

import to.tetramorph.starbase.lib.Const;

/**
 * スイスエフェメリスの天体暦外などのエラーが発生したとき、その天体とそのエラーの
 * 内容をワンセットにしたクラス。
 * ChartやNPTChartクラスで使用されている。
 * @author 大澤義鷹
 */
public class ErrorBody {
    private int body_id;
    private String errmes;
    protected int group;
    
    /**  ErrorBody オブジェクトを作成する */
    public ErrorBody(int body_id,String errmes) {
        this.body_id = body_id;
        this.errmes = errmes;
    }
    public ErrorBody(int body_id,String errmes,int group) {
        this(body_id,errmes);
        this.group = group;
    }
    /**
     * エラーメッセージを返す。
     */
    public String getMessage() {
        return errmes;
    }
    /**
     * 天体IDを返す。
     */
    public int getBodyID() {
        return body_id;
    }
    /**
     * グループコード(N,P,T等を表す)を返す。
     */
    public int getGroup() {
        return group;
    }
    /**
     * このオブジェクトの文字表現を返す。
     */
    public String toString() {
        return Const.PLANET_NAMES[body_id] + "," + errmes;
    }
}
