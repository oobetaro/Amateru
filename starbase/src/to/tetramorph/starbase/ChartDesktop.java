/*
 * ChartDesktop.java
 *
 * Created on 2007/11/08, 20:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase;

import java.awt.Frame;
import java.util.List;
import to.tetramorph.starbase.lib.Natal;

/**
 * メインフレームと、データベースを切り分けるために用意したインターフェイス。
 * Mainにimplementされている。
 * JFrameを継承したメインフレームをそのままDatabasePanelに引き渡すやり方をしている
 * とメインフレームのGUIを別クラスで作り直した場合、DatabasePanel側も大きな修正
 * にせまられる。それを回避するためのもの。
 * @author 大澤義鷹
 */
interface ChartDesktop {
    
    public boolean isEmptyChartPane();
    
    public ChartPane openChartPane(List<Natal> list);
    
    public void setNatal( List<Natal> list,
                           ChartPane targetPane );
    
    public ChartPane[] getChartPanes();
    
    public void addNatal( List<Natal> list,
                            ChartPane targetPane );
    
    public void setTransit( List<Natal> list,
                              ChartPane targetChartPane );
    
    public boolean isDataBusy(int id);
    
    public Frame getFrame();
    public void setResultVisible( boolean b );
    
}
