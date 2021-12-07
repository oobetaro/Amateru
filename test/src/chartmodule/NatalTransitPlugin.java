/*
 * NatalTransitPlugin.java
 *
 * Created on 2006/12/05, 13:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartmodule;

import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.ChartFactor;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
/**
 * �l�C�^���~�ƃg�����V�b�g�~���d�˂���d�~�B�l�C�^���~�l�C�^���̓�d�~��
 * �p�����č���Ă���B
 * @author ���V�`��
 */
public class NatalTransitPlugin extends DoubleNatalPlugin {
  
  public void setData(ChannelData channelData) {
    Transit transit = channelData.getTransit();
    ChartData chartData1 = channelData.get(0);
    chartData1.setFrameIcon();
    if(chartData1.getSelectedIndex() < 0) {
      //�_�C���N�g����
      return;
    }
    Data  data1 = chartData1.getDataList().get( chartData1.getSelectedIndex() );
    iframe.setTitle(data1.getNatal().getName());
    chartFactor1 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys1);
    chartFactor1.setDateAndPlace(data1.getTimePlace());

    chartFactor2 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys2);
    chartFactor2.setDateAndPlace(transit);
    
    //setFrameIcon(data1.getNatal());
    iframe.setTitle(data1.getNatal().getName());

    chartFactor1.setDateAndPlace(data1.getTimePlace());

    //�z���X�R�[�v1�~�ڂ�`���̂ɕK�v�ȃp�����^�[
    bodyList1 = chartFactor1.getPlanets(bodys11,ChartFactor.PLOT_ADJUST);         //AC,MC�������O�����V�̃��X�g
    planetsAngle1 = ChartFactor.getPlanetsPlotAngle(bodyList1); //�V�̂̓x�����X�g
    planetsAngleString1 = ChartFactor.formatSignAngles(chartFactor1.getPlanetsAngle(bodyList1),0); //�V�̂̕\���p�x�����X�g
    specialList = chartFactor1.getPlanets(specialBodys,ChartFactor.PLOT_ADJUST); //AC,MC���̊���_���X�g
    cusps1 = chartFactor1.getCusps();                          //�J�X�v�̃��X�g
    cuspsString1 = ChartFactor.formatSignAngles(cusps1,0);     //�\���p�J�X�v�x���̃��X�g
    aspectList1 = AspectFinder.getAspects(chartFactor1.getPlanets(bodys1,ChartFactor.PLOT_NOT_ADJUST),defAspect);
    //�z���X�R�[�v2�~�ڂ�`���̂ɕK�v�ȃp�����^�[
    bodyList2 = chartFactor2.getPlanets(bodys2,ChartFactor.PLOT_ADJUST);         //AC,MC�������O�����V�̃��X�g
    planetsAngle2 = ChartFactor.getPlanetsPlotAngle(bodyList2); //�V�̂̓x�����X�g
    planetsAngleString2 = ChartFactor.formatSignAngles(chartFactor2.getPlanetsAngle(bodyList2),0); //�V�̂̕\���p�x�����X�g
    cusps2 = chartFactor2.getCusps();                          //�J�X�v�̃��X�g
    cuspsString2 = ChartFactor.formatSignAngles(cusps2,0);     //�\���p�J�X�v�x���̃��X�g
    aspectList2 = AspectFinder.getAspects(bodyList2,defAspect);
    aspectList12 = AspectFinder.getAspects(
      chartFactor1.getPlanets(bodys1,ChartFactor.PLOT_NOT_ADJUST),
      bodyList2,defAspect);
    //
    planetNeedles = new PlanetNeedle[3];
    planetNeedles[0] = specialPlanetNeedle;
    planetNeedles[1] = planetNeedle1;
    planetNeedles[2] = planetNeedle2;
    //
    Body p = chartFactor1.getBody(AC);
    if(p == null) {
      Body sun = chartFactor1.getBody(SUN);
      asc = sun.getSign() * 30f;
    } else asc = p.lon;
    ready = true;
    repaint();      
  }
  public boolean isNeedTransit() {
    return true;
  }
  public String toString() {
    return "�l�C�^���{�g�����V�b�g";
  }
  public int getChannelSize() {
    return 1;
  }
  private static final String channelNames [] = { "�l�C�^��" };
  public String[] getChannelNames() {
    return channelNames;
  }
  
}
