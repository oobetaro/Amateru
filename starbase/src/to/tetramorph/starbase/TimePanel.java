/*
 * TimePanel.java
 *
 * Created on 2007/11/13, 22:12
 *
 */

package to.tetramorph.starbase;

import java.util.List;
import javax.swing.JMenu;
import javax.swing.JPanel;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.lib.TransitTabReceiver;
import to.tetramorph.starbase.module.ChartModulePanel;

/**
 * チャート表示状態のときネイタルとトランジットの日時や観測場所をユーザが
 * 変更するためのパネル。
 * クラスの切り離し用に用意した抽象クラス。旧TimePanelを作り直そうとしたとき、
 * 別名のクラスで作ると、TimePanelを使用しているクラス(ChartInternalFrameとMain)
 * 側で修正が必要になる。そこでTimePanelに必要なメソッドとインターフェイスを
 * abstractクラスに用意し、それを継承して実装クラスを作る方式にした。
 * TimePanelという名前は変更したくなかったので、旧TimePanelはTimePanel2に変更し、
 * TimePanelを継承するように変更した。
 *
 * ChartInternalFrameでのTimePanelのインスタンスを作成している部分は、
 * これまではTimePanel timePanel = new TimePanel(this);としていたが、このabstract
 * クラスの導入によって、TimePanel timePanel = new TimePanel2(this);と書き換えた。
 * TimePane2をさらに新しくした場合は、TimePane3とするなどしてこのクラスを継承し、
 * 必要なメソッドを実装し、ChartInternalFrame内では、
 * TimePanel timePanel = new TimePanel3(this);などとすればよい。
 *
 * 実際のところ、このクラスを継承した具象クラスは最終的には一つしかない。
 * 派生クラスを複数作るためにこのクラスがあるわけではない。
 * @author 大澤義鷹
 */
abstract class TimePanel extends JPanel
    implements TimeController,TransitTabReceiver {

    ChannelData channelData;    
    public abstract boolean isComprise(int id);
    public abstract void setModule(ChartModulePanel module);
    public abstract void stopTimer();
    public abstract void stop();
    public abstract void setNatal(List<Natal> list);
    public abstract void addNatal(List<Natal> list);
    public abstract JMenu getHistoryMenu();
    public abstract Transit getTransit();
    public abstract void dataCopy(TimePanel tp);
    public abstract ChartData getSelectedChartData();
    public abstract boolean isSelectedManeuverButton();
    public abstract void setSelectedManeuver(boolean b);
    public abstract void setSelectedButton(int index);

}
