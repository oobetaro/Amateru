/*
 * Chart.java
 *
 * Created on 2008/11/14, 16:31
 *
 */

package to.tetramorph.starbase;

import java.util.List;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModuleMode;
import to.tetramorph.starbase.util.TestConst;

/**
 * チャートの図を復元するために必要な情報をワンセットにしたクラス。
 *
 * @author 大澤義鷹
 */
public class Chart {
    private String chartName;
    private String calcName;
    private String skinName;
    private String moduleName;
    private ChartModuleMode chartModuleMode;
    private Transit transit;
    private List<List<Natal>> natalList;
    /**  Chart オブジェクトを作成する */
    public Chart() {
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "chartName=" + chartName );
        sb.append(",");
        sb.append( "moduleName=" + moduleName );
        sb.append(",");
        if ( chartModuleMode == null ) {
            sb.append("chartModuleMode=null,");
        } else {
            sb.append("chartModuleMode=[" + chartModuleMode.toString());
            sb.append("],");
        }
        if ( transit == null ) {
            sb.append("transit=null,");
        } else {
            sb.append( "transit=[");
            sb.append( transit.toString() );
            sb.append( "]" );
        }
        return sb.toString();
    }
    public static void main( String [] args ) {
        Chart c = new Chart();
        c.setTransit( TestConst.getMyNowTransit());
        c.setChartModuleMode(new ChartModuleMode("NPT","0,N"));
        System.out.println( c );
    }
    public String getCalcName() {
        return calcName;
    }

    public void setCalcName( String calcName ) {
        this.calcName = calcName;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName( String skinName ) {
        this.skinName = skinName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName( String moduleName ) {
        this.moduleName = moduleName;
    }

    public ChartModuleMode getChartModuleMode() {
        return chartModuleMode;
    }

    public void setChartModuleMode( ChartModuleMode chartModuleMode ) {
        this.chartModuleMode = chartModuleMode;
    }

    public Transit getTransit() {
        return transit;
    }

    public void setTransit( Transit transit ) {
        this.transit = transit;
    }

    public List<List<Natal>> getNatalList() {
        return natalList;
    }

    public void setNatalList( List<List<Natal>> natalList ) {
        this.natalList = natalList;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }
    
}
