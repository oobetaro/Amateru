/*
 * ProgressComboBox.java
 *
 * Created on 2007/07/13, 8:36
 *
 */
package to.tetramorph.starbase.chartparts;

import javax.swing.JComboBox;
import to.tetramorph.starbase.util.NPTChart;

/**
 * 進行法を選択するためのコンボボックス。
 * コンボボックスに設定される進行法名は、NPTChartのPROGRESS_NAME[]を参照すること
 * で取得している。進行法は今後も増える可能性もあるし、またチャートモジュール側
 * で対応するものもしないものもあると思われ、汎用的なパーツとして扱う事がむつか
 * しいため、今の段階では三重円モジュール専用の仕様になっている。
 * もっと整理がついてくれば、libパッケージに移動させることもありうる。
 * 2011-07-30 1度1年法がデフォだったが、1日1年法をデフォにした。
 * @author 大澤義孝
 */
public class ProgressComboBox extends JComboBox {

    /**  ProgressComboBox オブジェクトを作成する */
    public ProgressComboBox() {
        init();
    }
    //コンボボックスに進行法の名前をセットする。

    private void init() {
        for ( int i = 0; i < NPTChart.PROGRESS_NAMES.length; i++ ) {
            addItem( NPTChart.PROGRESS_NAMES[i] );
        }
        this.setSelectedIndex(1); //1日1年法を選択
    }

    /**
     * 選択されている進行法の番号を返す。(順番にならんでいるとおり0,1,2,,)
     * @return NPTChart.PROGRESS_CODES[]内の1文字
     */
    public char getSelectedProgressCode() {
        return NPTChart.PROGRESS_CODES[ getSelectedIndex()];
    }

    /**
     * 番号で指定された進行法を選択する。指定概
     * @param code NPTChartのフィールド変数PRIMARY_PROGRESSION,SECONDARY_PROGRESSION,
     *             SOLAR_ARC_PROGRESSION,COMPOSIT_PROGRESSION等。
     * @exception  IllegalArgumentException サポートされていない進行法コードが
     *             指定された場合。
     */
    public void setSelectedProgressCode( char code ) {
        for ( int i = 0; i < NPTChart.PROGRESS_CODES.length; i++ ) {
            if (NPTChart.PROGRESS_CODES[i] == code) {
                setSelectedIndex(i);
                return;
            }
        }
        throw new IllegalArgumentException(
                "サポートされていない進行法コード : " + code );
    }
}
