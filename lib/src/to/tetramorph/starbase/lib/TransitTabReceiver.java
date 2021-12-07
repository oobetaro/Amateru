/*
 * TransitTabReceiver.java
 *
 * Created on 2007/07/18, 17:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

/**
 * TimePanelをChannelDataに引き渡す際、TransitTabReceiverとして引き渡す。
 * なぜかというと、TimePanelはチャートモジュールからは直接はアクセスできないように
 * 保護されたクラスだから。しかしTimePanel内のトランジットタブにモジュール側の
 * 天体ドラッグ操作などで変更された日時を反映させる必要性があり、ChannelDataに
 * その機能を持たせたかった。そこでこのインターフェイスを実装することで、
 * TimePanelをChannelDataのコンストラクタで渡し、ChannelDataから更新されたTransit
 * をTimePanelに送る仕組みにしている。
 * @author 大澤義鷹
 */
public interface TransitTabReceiver {
  public void updateTransit(Transit transit);
}
