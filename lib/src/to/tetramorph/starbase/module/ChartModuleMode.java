/*
 * ChartModuleMode.java
 *
 * Created on 2008/09/27, 22:07
 *
 */

package to.tetramorph.starbase.module;

/**
 * チャートモジュールのモードを指定するために使用するクラス。
 * 一つのチャートモジュールで、複数の図を提供するような場合、モジュール起動の
 * 際に、どの図を描画するモードか初期設定を与えなければならないときがある。
 * たとえばNPT三重円モジュールは、一つのモジュールでネイタル1重円からNPT三重円
 * まで7種類の図版をカバーするが、最初にこのモジュールでチャートを表示するとき、
 * 7種類のうちどれを使うか指定したいときがある。そのときこのクラスを使う。
 * @author 大澤義孝
 */
public class ChartModuleMode {
    private String title;
    private String command;
    /**  ChartModuleMode オブジェクトを作成する */
    public ChartModuleMode( String title, String command ) {
        this.title = title;
        this.command = command;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }
    public String toString() {
        return "title=" + title + ",command=" + command;
    }
}
