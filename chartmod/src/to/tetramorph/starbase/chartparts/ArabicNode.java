/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 2009/10/22 20:05:00
 */

package to.tetramorph.starbase.chartparts;

import to.tetramorph.starbase.lib.Body;

/**
 * アラビックパーツ一つ分のデータを格納するクラス。
 * 各フィールド変数にアクセスするget/setメソッドは用意していない。
 * 直接参照する。<br>
 * アラビックパーツ計算式の一覧を設定ファイルから読みこみ、構文解析して、一行
 * ごとにこのオブジェクトに格納し計算する。
 * 計算結果やエラー情報もこのオブジェクトに格納される。
 * @author 大澤義鷹
 */
public class ArabicNode {
    /**
     * アラビックパーツの呼び名 "Part of Fortune"等。
     * 構文解析でエラーがあったときは、その行が格納される。
     */
    public String title;
    /**
     * アラビックパーツの計算式。JavaScriptで解釈できる式である事。
     */
    public String exp;
    /**
     * 計算式の結果を書きこむJavaScriptの変数名。nullの場合は、JavaScriptに
     * その値は渡さない。
     */
    public String variable;
    /**
     * 計算式の結果が書きこまれる天体オブジェクト。アラビックパーツに天体IDは
     * 無いので、IDコードはつねに10000が書きこまれる。
     */
    public Body arabicBody;
    /**
     * 構文解析や、式の計算でエラーがあった場合のエラーメッセージ。
     * ノーエラーのときはnull。
     */
    public String errmsg;
    /**
     * このノードが設定ソースコードの何行目か。
     */
    public int row;
    /**
     * N,P,Tどのグループの天体を使って計算されたかを示す。
     * NPTChartのフィールド定数、NATAL,PROGRESS,TRANSITのいずれかを指定。
     */
    public int group;
    /**
     * オブジェクトを作成する。
     * @param title このアラビックパーツの呼び名
     * @param exp 計算式
     * @param row 設定ファイル中の行番号
     * @param variable 計算式の結果をさらに変数と見なすときの変数名
     */
    ArabicNode( String title, String exp, String variable, int row ) {
        this.title = title;
        this.exp = exp;
        this.variable = variable;
        this.row = row;
    }
    /**
     * このオブジェクトの内容をCSV形式で返す。<br>
     * 'row,"title",exp,varibale,lon,errmsg'の順番。
     * @return
     */
    public String getCSV() {
        String lon = (arabicBody == null ) ? "null" : "" + arabicBody.lon;
        return String.format( "%d,\"%s\",%s,%s,%s,%s",
                               row,title,exp,variable,lon,errmsg);
    }
    /**
     * このオブジェクトを表現する文字列を返す。
     * @return
     */
    @Override
    public String toString() {
        String lon = (arabicBody == null ) ? "null" : "" + arabicBody.lon;
        return String.format(
                "row=%d, title=\"%s\", exp=%s, variable=%s, lon=%s, errmsg%s",
                row, title, exp, variable, lon, errmsg );
    }
}
