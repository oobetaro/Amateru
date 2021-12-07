/*
 * Transit.java
 *
 * Created on 2006/06/18, 8:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import to.tetramorph.starbase.*;

/**
 * TimePlaceの「時と場所」に加えて「名前、メモ」というテキスト情報を付加したクラス。
 * HISTORY表やCOMPOSIT表に格納するデータ等を表現するのに使用しており、個人の履歴情報
 * (いつ学校に入学したとか就職したとか死去したとか)やコンポジットチャートでの
 * 合成する人物の出生データなどを保管するためのもので、「名前、メモ、日付、時間
 * 地名、緯度、けど、タイムゾーン」で構成され、それぞれの値のset/getのメソッド
 * が用意されている。(このクラスはEventからTransitに改名したバージョン)
 */
public class Transit extends TimePlace implements java.io.Serializable {
    /** セットできる名前文字列の最大文字数 */
    public static final int NAME_MAX_LENGTH = 40;
    /** セットできるメモの最大文字数 */
    public static final int MEMO_MAX_LENGTH = 60;
    String name = "";
    String memo = "";
    int key = 0;
    /**
     * 値を持たない空のオブジェクトを作成する。値の設定はメソッドで行う。
     */
    public Transit() {
        super();
    }
    /**
     * ディープコピーで複製を作る
     */
    public Transit(Transit t) {
        super(t);
        name = t.name;
        memo = t.memo;
        key = t.key;
    }
    /**
     * 名前を返す。名前は人物の名前だったり、出来事の名前だったりする。
     * @return 名前の文字列
     */
    public String getName() {
        return name;
    }
    
    /**
     * 人名や出来事の名前をセットする。
     * null,"",28文字以上の値はIllegalArgumentException。
     * @param name 文字列
     */
    public void setName(String name) {
        if(name == null) throw new IllegalArgumentException("NAME");
        //if(name.length() > NAME_MAX_LENGTH || name.length() == 0)
        //チャート複製のときにひっかかった
        if(name.length() > NAME_MAX_LENGTH )
            throw new IllegalArgumentException("NAME length too long;" + name);
        this.name = name;
    }
    
    /**
     * このオブジェクトを識別するユニークキーをセットする。
     * これはDB上でヒストリーやコンポジットのEventを編集する際に必要なもので、
     * OCCASION表のフィールドに登場するIDとは異なるもの。ゆえにsetIDとはせず、
     * setPrimaryKeyとして区別している。
     * @param key 正の整数
     */
    public void setPrimaryKey(int key) {
        this.key = key;
    }
    /**
     * ユニークキーを返す。
     * @return 正の整数
     */
    public int getPrimaryKey() {
        return key;
    }
    /**
     * メモを返す。
     * @return メモの文字列
     */
    public String getMemo() {
        return memo;
    }
    
    /**
     * メモをセットする。null,""はセット可能。28文字以上の値は禁止。
     * @param memo 文字列
     */
    public void setMemo(String memo) {
        if(memo != null) {
            if(memo.length() > MEMO_MAX_LENGTH)
                throw new IllegalArgumentException("MEMO length too long;" + memo);
        }
        this.memo = memo;
    }
    
    /**
     * このオブジェクトの文字列表現を返す。
     * @return 「おーさわ 2006-07-06 12:00:00 E141.335193 N43.052212 日本標準時
     * 〔UTC+09:00〕このひとはこのソフトの作者です」といった文字列。
     */
    public String toString() {
        return getName() + " " + super.toString() + getMemo();
    }
    /**
     * NAME,MEMO,DATE,ERA,TIME,PLACENAME,LATITUDE,LONGITUDE,TIMEZONEを
     * ResultSetから読み取りこのオブジェクトにセットする。primaryKeyなどすべての
     * パラメターがセットされるわけではない。
     */
    public void setParams(ResultSet rs) throws SQLException {
        super.setParams(rs);
        setName(rs.getString("NAME"));
        setMemo(rs.getString("MEMO"));
    }
    /**
     * このオブジェクトのCSV表現を返す。セパレータはカンマ。
     */
    public String getCSVTransit() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(",");
        sb.append(getMemo());
        sb.append(",");
        sb.append(getStringDate());
        sb.append(",");
        sb.append(getStringTime());
        sb.append(",");
        sb.append(getPlaceName());
        sb.append(",");
        sb.append(getStringLatitude());
        sb.append(",");
        sb.append(getStringLongitude());
        sb.append(",");
        sb.append(getTimeZone().getID());
        return sb.toString();
    }
}
