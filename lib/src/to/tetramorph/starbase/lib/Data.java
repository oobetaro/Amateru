/*
 * Data.java
 *
 * Created on 2006/09/21, 16:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import to.tetramorph.starbase.*;

/**
 * NatalとTransitオブジェクトのラッパークラスで、TimePlaceの情報を複製し二重に持つ。
 * チャートモジュールに提供される、データベースからの基礎データ(Natalオブジェクト)
 * は、Dataオブジェクトにしたうえで引き渡される。
 * というのは、与えられたデータの特に日時や場所を修正する場合があり、元のNatalを
 * 書き換えるのではなく、複製された日時を書き換えてもらい、必要があればいつでも
 * もとのNatalに戻す事ができるようにするためだ。
 *
 * TransitはNatalに変換されてラップされ、それを取得するときはgetNatal()を使う。
 * getTimePlace()は、コンストラクタで与えたnatalもしくはtransitのデータ
 * から複製したTimePlaceオブジェクトを返し、チャートの描画などで実際に使うのは、
 * このメソッドで取得された時間と場所を使う。setTimePlace()は任意の日時・場所を
 * セットすることもできる。
 *
 * 時間調節をする際にオリジナルのデータを変更するのではなく、複製されたものを使う
 * 仕組みになっていて、時間をいじくってもresetTimePlace()でまた元の値に戻す事ができる。
 */
public class Data  implements java.io.Serializable {
    public Natal natal;
    public TimePlace timePlace;
    /**
     * Natalをラップし(シャーローコピー)、日付と時間と場所情報のクローンを追加する。
     */
    public Data( Natal natal ) {
        this.natal = natal;
        timePlace = new TimePlace(natal);
    }
    /**
     * EventからNatalを作成し、日時と場所情報のクローンを追加する。これはコンポジット
     * のときに使用する。
     */
    public Data( Transit transit ) {
        natal = new Natal();
        natal.setChartType(Natal.EVENT);
        natal.setTimePlace(transit);
        natal.setName(transit.getName());
        natal.setMemo(transit.getMemo());
        timePlace = new TimePlace(transit);
    }
    /**
     * このオブジェクトの複製を作る。Natal dataはシャーローコピー。
     * TimePlaceはデープコピー。
     */
    public Data( Data data ) {
        this.natal = data.natal;
        this.timePlace = new TimePlace(data.timePlace);
    }
    /**
     * 名前を返す。ラップされているNatalのgetName()メソッドを呼び出す。
     * つまりデータの「名前」が返る。ただし名前が7文字を越える場合は、
     * カットして末尾に".."をつけた省略名を返す。
     */
    public String toString() {
        String name = natal.getName();
        if(name.length() < 8) return name;
        return name.substring(0,7) + "..";
        //return occasion.getName();
    }
    /**
     * ラップされているNatalを返す。
     */
    public Natal getNatal() {
        return natal;
    }
    /**
     * 新しいNatalをセットする。getTimePlace()の値も更新される。
     * 参照アドレスが変わらない点をのぞいて、new Data(Natal natal)と同じ。
     */
    public void setNatal( Natal natal ) {
        this.natal = natal;
        this.timePlace = new TimePlace(natal);
    }
    /**
     * Natalから複製されたTimePlaceオブジェクトを返す。
     * この日時と場所はTimeControlPanelでユーザによって変更されるときがある。
     * チャートモジュールはこの日時と場所をもとに、ホロスコープ等を描画する。
     * resetTimePlace()によって、変更された時間を元に戻すことができる。
     */
    public TimePlace getTimePlace() {
        return timePlace;
    }
    /**
     * TimePlaceをセットする。
     */
    public void setTimePlace( TimePlace timePlace ) {
        this.timePlace = timePlace;
    }
    /**
     * 日付情報をオリジナルに戻す。
     */
    public void resetTimePlace() {
        timePlace = new TimePlace(natal);
    }
}
