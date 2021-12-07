/*
 * ChannelData.java
 *
 * Created on 2006/11/27, 21:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * ChartDataを複数ラップして保管するクラスで、ChartModuleへ受け渡されるデータは、
 * この中にしまわれている。
 */
public class ChannelData implements java.io.Serializable {
    protected List<ChartData> list = new ArrayList<ChartData>();
    protected Transit transit;
    private TransitTabReceiver transitTabReceiver;
    
    /**
     * 中身が空のオブジェクトを作成
     */
    public ChannelData( TransitTabReceiver tr ) {
        transitTabReceiver = tr;
    }
    
    /**
     * ChartDataを登録する。
     */
    public void add( ChartData data ) {
        list.add(data);
    }
    
    /**
     * チャンネル数を返す
     */
    public int size() {
        return list.size();
    }
    
    /**
     * 指定番号のチャンネルに登録されているChartDataを返す。
     */
    public ChartData get( int num ) {
        return list.get( num );
    }
    
    /**
     * トランジットの日時場所を返す。
     */
    public Transit getTransit() {
        return transit;
    }
    
    /**
     * トランジットの日時場所をセットする。
     */
    public void setTransit( Transit transit ) {
        this.transit = transit;
    }

    /**
     * トランジットの日時をチャートモジュール側から更新するときに使用する。
     * トランジットタブに更新された日付が反映される。
     */
    public void updateTransit( Transit transit ) {
        transitTabReceiver.updateTransit( transit );
    }
}
