/*
 * HoroscopePanel.java
 *
 * Created on 2006/10/26, 17:48
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import static java.awt.Color.*;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.chartplate.AspectCircle;
import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.ChartFactor;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.chartplate.DialGauge;
import to.tetramorph.starbase.util.Ephemeris;
import to.tetramorph.starbase.chartplate.HouseNumberDial;
import to.tetramorph.starbase.chartplate.HouseRing;
import to.tetramorph.starbase.chartplate.MarkerNeedle;
import to.tetramorph.starbase.chartplate.NumberNeedle;
import to.tetramorph.starbase.chartplate.PlanetNeedle;
import to.tetramorph.starbase.util.Sabian;
import to.tetramorph.starbase.chartplate.SignDial;
import to.tetramorph.starbase.lib.Caption;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.starbase.widget.WordBalloonHandler;

/**
 * ホロスコープ二重円のデザインを決めるテストプログラム
 */
public class DoubleNatalPlugin extends ChartModulePanel {
  WordBalloon wordBalloon;
  Font astrofont;
  Graphics2D g;
  Body selectedPoint;
  Aspect selectedAspect;
  double asc = 0;
  /** Creates new form HoroscopePanel */
  //使用するアスペクトのリスト
  AspectType [] defAspect = {
    new AspectType(CONJUNCTION,4,8),
    new AspectType(SEXTILE,3,6),
    new AspectType(SQUARE,4,8),
    new AspectType(TRINE,4,8),
    new AspectType(OPPOSITION,4,8),
    new AspectType(QUINCUNX,3,6),
    new AspectType(QUINTILE,2,4)
  };
  //
  boolean [] aspectMode = { true,true,true,true,true,true,true };
  //1円目
  int [] bodys1 = { AC,MC,DC,IC,SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
    URANUS,NEPTUNE,PLUTO,NODE };
  int [] bodys11 = { SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,URANUS,
    NEPTUNE,PLUTO,NODE };
  int [] specialBodys = { AC,MC,DC,IC }; 
  //2円目
  int [] bodys2 = { AC,MC,DC,IC,SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
    URANUS,NEPTUNE,PLUTO,NODE };

  ChartFactor chartFactor1;  // 1円目のチャート情報
  ChartFactor chartFactor2; // 2円目のチャート情報
  double [] cusps1;
  double [] cusps2;
  String [] cuspsString1;
  String [] cuspsString2;
  List<Body> bodyList1; //天体リスト
  List<Body> specialList = new ArrayList<Body>(); //AC,MC等のリストで1円目にのみ存在
  List<Body> bodyList2; //天体リスト
  double [] planetsAngle1;
  double [] planetsAngle2;
  String [] planetsAngleString1;
  String [] planetsAngleString2;
  List<Aspect> aspectList1;
  List<Aspect> aspectList2;
  List<Aspect> aspectList12;
  
  ButtonGroup circleButtonGroup = new ButtonGroup();
  ButtonGroup loadButtonGroup = new ButtonGroup();
  ButtonGroup aselButtonGroup = new ButtonGroup();
  boolean ready = false;
  /**
   * Frameを引き渡すのは、WordBalloonに必要だから。最終的には変更するかも。
   */
  public void init() {
    initComponents();
    circleButtonGroup.add(circleRadioButtonMenuItem1);
    circleButtonGroup.add(circleRadioButtonMenuItem2);
    circleButtonGroup.add(circleRadioButtonMenuItem3);
    loadButtonGroup.add(natalRadioButtonMenuItem1);
    loadButtonGroup.add(natalRadioButtonMenuItem2);
    aselButtonGroup.add(aselRadioButtonMenuItem1);
    aselButtonGroup.add(aselRadioButtonMenuItem2);
    aselButtonGroup.add(aselRadioButtonMenuItem3);
    gauge.setOption(DialGauge.NOT_DRAW_ZERO_DEGREES);
    markerNeedle1.setDirection(MarkerNeedle.OUTER);
    markerNeedle2.setDirection(MarkerNeedle.OUTER);
    MouseHandler mouseHandler = new MouseHandler();
    addMouseMotionListener(mouseHandler);
    addMouseListener(mouseHandler);
    wordBalloon = new WordBalloon(mouseHandler);
    zodiacRing.bgColors = Const.SIGN_COLORS;
    zodiacRing.setPaintCondition(
     HouseRing.FILL|HouseRing.SEPARATOR|HouseRing.INNER_ARC|HouseRing.OUTER_ARC);
    houseRing1.separatorLineColor = Color.GRAY;
    houseRing2.separatorLineColor = Color.GRAY;
    houseRing2.innerLineColor = Color.GRAY;
    houseNumberDial1.numberColor = Color.LIGHT_GRAY;
    houseNumberDial2.numberColor = Color.LIGHT_GRAY;
    setBackground(Color.WHITE);
    chartFactor1 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys1);
    chartFactor2 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys2);
  }
  
  public void setData(ChannelData channelData) {
    Transit transit = channelData.getTransit();
    ChartData chartData1 = channelData.get(0);
    ChartData chartData2 = channelData.get(1);
    chartData1.setFrameIcon();
    if(chartData1.getSelectedIndex() < 0) {
      //ダイレクト入力
      return;
    }
    if(chartData2.getSelectedIndex() < 0) {
      return;
    }
    Data  data1 = chartData1.getDataList().get( chartData1.getSelectedIndex() );
    iframe.setTitle(data1.getNatal().getName());
    chartFactor1 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys1);
    chartFactor1.setDateAndPlace(data1.getTimePlace());

    Data  data2 = chartData2.getDataList().get( chartData2.getSelectedIndex() );
    iframe.setTitle(data2.getNatal().getName());
    chartFactor2 = new ChartFactor(Ephemeris.getSwissEph(),config,bodys2);
    chartFactor2.setDateAndPlace(data2.getTimePlace());
    
    //setFrameIcon(data1.getNatal());
    iframe.setTitle(data1.getNatal().getName());

    chartFactor1.setDateAndPlace(data1.getTimePlace());
    chartFactor2.setDateAndPlace(data2.getTimePlace());

    //ホロスコープ1円目を描くのに必要なパラメター
    bodyList1 = chartFactor1.getPlanets(bodys11,ChartFactor.PLOT_ADJUST);         //AC,MC等を除外した天体リスト
    planetsAngle1 = ChartFactor.getPlanetsPlotAngle(bodyList1); //天体の度数リスト
    planetsAngleString1 = ChartFactor.formatSignAngles(chartFactor1.getPlanetsAngle(bodyList1),0); //天体の表示用度数リスト
    specialList = chartFactor1.getPlanets(specialBodys,ChartFactor.PLOT_ADJUST); //AC,MC等の感受点リスト
    cusps1 = chartFactor1.getCusps();                          //カスプのリスト
    cuspsString1 = ChartFactor.formatSignAngles(cusps1,0);     //表示用カスプ度数のリスト
    aspectList1 = AspectFinder.getAspects(chartFactor1.getPlanets(bodys1,ChartFactor.PLOT_NOT_ADJUST),defAspect);
    //ホロスコープ2円目を描くのに必要なパラメター
    bodyList2 = chartFactor2.getPlanets(bodys2,ChartFactor.PLOT_ADJUST);         //AC,MC等を除外した天体リスト
    planetsAngle2 = ChartFactor.getPlanetsPlotAngle(bodyList2); //天体の度数リスト
    planetsAngleString2 = ChartFactor.formatSignAngles(chartFactor2.getPlanetsAngle(bodyList2),0); //天体の表示用度数リスト
    cusps2 = chartFactor2.getCusps();                          //カスプのリスト
    cuspsString2 = ChartFactor.formatSignAngles(cusps2,0);     //表示用カスプ度数のリスト
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

  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    if(!ready) return;
    g = (Graphics2D)graphics;
    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = getSize();
    //fontRenderContext = g.getFontRenderContext();
    double width = (float)size.getWidth();
    double height = (float)size.getHeight();
    double cx = width / 2d;
    double cy = height / 2d;
    double W = width < height ? width : height;
    // キャンバスの幅 W=1に対して・・・
    double d0 = 0.85; //獣帯リングの外円の直径
    double d0w = 0.1; //獣帯リングの幅
    double d9 = 0.47; //一番内側の円の直径
    double d1 = (d0 - d0w - d9)/2.0 + d9;
    double d1p = (d1 - d9) * 0.4 + d9;
    double d2 = (d0-d0w); //2円目惑星軌道の直径
    double d2p = (d2-d1) * 0.4 + d1;
    houseRing1.setPaintCondition(HouseRing.SEPARATOR|HouseRing.INNER_ARC);
    houseRing2.setPaintCondition(HouseRing.SEPARATOR|HouseRing.INNER_ARC);
    houseRing1.setFactor(g,cx,cy,W,asc,d9,d1,cusps1);
    houseRing2.setFactor(g,cx,cy,W,asc,d1,d2,cusps2);
    houseRing9.setPaintCondition(HouseRing.SEPARATOR);
    houseRing9.setFactor(g,cx,cy,W,asc,d0-d0w,d0+0.05,cusps1);

    markerNeedle1.setExtension(d1p - 0.02);
    markerNeedle2.setExtension(d2p - 0.02);
    zodiacRing.setFactor(g,cx,cy,W,asc,d0-d0w,d0,null);
    gauge.setFactor(g,cx,cy,W,asc,d0-d0w);
    gauge1.setFactor(g,cx,cy,W,asc,d9);
    gauge2.setFactor(g,cx,cy,W,asc,d1);
    signDial.setFactor(g,cx,cy,W,asc,d0-d0w/2d,0.03f);
    planetNeedle1.setFactor(g,cx,cy,W,asc,d1p,0.03,bodyList1,selectedAspect);
    planetNeedle2.setFactor(g,cx,cy,W,asc,d2p,0.03,bodyList2,selectedAspect);
    specialPlanetNeedle.setFactor(g,cx,cy,W,asc,d0+0.07,0.04,specialList,selectedAspect);
    //markerNeedle.setFactor(g,cx,cy,W,asc,d0-d0w,bodyList1);
    markerNeedle1.setFactor(g,cx,cy,W,asc,d9,bodyList1);
    markerNeedle2.setFactor(g,cx,cy,W,asc,d1,bodyList2);
    cuspAngleNumberNeedle.setFactor(g,cx,cy,W,asc,d0+0.01,0.015,cusps1,cuspsString1);
    cuspAngleNumberNeedle.setVOffset(0.018);
    cuspAngleNumberNeedle.setAlign(NumberNeedle.OUTER);
    houseNumberDial1.setFactor(g,cx,cy,W,asc,d1p,0.02,cusps1);
    houseNumberDial2.setFactor(g,cx,cy,W,asc,d2p,0.02,cusps2);
    planetAngleNumberNeedle1.setFactor(g,cx,cy,W,asc,d1p + 0.07,0.015,planetsAngle1,planetsAngleString1);
    planetAngleNumberNeedle1.setVOffset(0.0085);
    planetAngleNumberNeedle2.setFactor(g,cx,cy,W,asc,d2p + 0.07,0.015,planetsAngle2,planetsAngleString2);
    planetAngleNumberNeedle2.setVOffset(0.0085);
    aspectCircle.setFactor(g,cx,cy,W,asc,d9,aspectList12,aspectMode,selectedPoint,0.02);
    
    zodiacRing.draw();
    signDial.draw();
    houseRing1.draw();
    houseRing2.draw();
    houseRing9.draw();
    markerNeedle1.draw();
    markerNeedle2.draw();
    planetNeedle1.draw();
    planetNeedle2.draw();
    specialPlanetNeedle.draw();
    cuspAngleNumberNeedle.draw();
    planetAngleNumberNeedle1.draw();
    planetAngleNumberNeedle2.draw();
    houseNumberDial1.draw();
    houseNumberDial2.draw();
    gauge.draw();
    gauge1.draw();
    gauge2.draw();
    aspectCircle.draw();
    System.out.println("ペイントした"+ pcount);
    pcount++;
  }
  int pcount = 0;
  //パネル上でのマウスイベント
  class MouseHandler extends MouseAdapter implements MouseMotionListener,WordBalloonHandler {
    //Point mousePoint = new Point(0,0);
    public void mouseClicked(MouseEvent e) {
      int click = e.getClickCount();
      int x = e.getX();
      int y = e.getY();
      cursor();
      if(e.getButton() == MouseEvent.BUTTON3) {
        System.out.println("中指クリック");
        if(! aspectCircle.isContainCircle(x,y) && click == 1)
          rightClickPopupMenu.show(DoubleNatalPlugin.this,x,y);
      } else if(e.getButton() == MouseEvent.BUTTON1) {
        System.out.println("人差し指クリック");
        Body p = getSelectedBody(x,y);
        if(p != null  && click == 1) {
          selectedPoint = p;
          repaint();
        } else if(p != null && click == 2) {
          wordBalloon.show(Caption.getSabianCaption(p,Sabian.JP));
          selectedPoint = p;
        } else if( aspectCircle.contains(x,y) && click == 1) {
          wordBalloon.show(aspectCircle.getSelectedAspect().getCaption());
        } else if(! aspectCircle.isContainCircle(x,y) && click == 1) {
          selectedPoint = null;
          repaint();
        }
      }
    }
    Body getSelectedBody(int x,int y) {
      for(int i=0; i<planetNeedles.length; i++) {
        if(planetNeedles[i].contains(x,y))
          return planetNeedles[i].getSelectedBody();
      }
      return null;
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    Object selectedObject;
    Object pre;
    boolean paint;
    //オンカーソルイベントを拾う
    public void mouseMoved(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      selectedObject = getSelectedBody(x,y);
      if(selectedObject == null && aspectCircle.contains(x,y)) {
        selectedAspect = aspectCircle.getSelectedAspect();
        selectedObject = selectedAspect;
      } else {
        selectedAspect = null;
        //selectedObject = null;
      }
      repaint();
    }
    public Object getSelectedObject() {
      return selectedObject;
    }
    public void setSelectedObject(Object o) {
      selectedObject = o;
    }
    
  }
  // フレームをリサイズしたときカーソルがリサイズカーソルに変わって戻らなく
  // なるバグがある。マウスクリックやウインドウイベントのタイミングでこのメソッド
  // を読んでもとのカーソル形状にもどすことができる。
  void cursor() {
    Cursor cursor = this.getCursor();
    if(cursor.getType() == Cursor.DEFAULT_CURSOR) return;
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

    public boolean isNeedTransit() {
    return false;
  }
  public String toString() {
    return "ネイタル＋ネイタル";
  }
  public int getChannelSize() {
    return 2;
  }
  static final String channelNames [] = { "ネイタル1","ネイタル2" };
  public String[] getChannelNames() {
    return channelNames;
  }

  HouseRing zodiacRing = new HouseRing();
  DialGauge gauge = new DialGauge();
  DialGauge gauge1 = new DialGauge();
  DialGauge gauge2 = new DialGauge();
  SignDial signDial = new SignDial();
  PlanetNeedle [] planetNeedles;
  PlanetNeedle planetNeedle1 = new PlanetNeedle();
  PlanetNeedle planetNeedle2 = new PlanetNeedle();
  PlanetNeedle specialPlanetNeedle = new PlanetNeedle();

  HouseRing houseRing1 = new HouseRing();
  HouseRing houseRing2 = new HouseRing();
  HouseRing houseRing9 = new HouseRing();
  MarkerNeedle markerNeedle1 = new MarkerNeedle();
  MarkerNeedle markerNeedle2 = new MarkerNeedle();
  NumberNeedle cuspAngleNumberNeedle = new NumberNeedle();
  NumberNeedle planetAngleNumberNeedle1 = new NumberNeedle();
  NumberNeedle planetAngleNumberNeedle2 = new NumberNeedle();
  HouseNumberDial houseNumberDial1 = new HouseNumberDial();
  HouseNumberDial houseNumberDial2 = new HouseNumberDial();
  AspectCircle aspectCircle = new AspectCircle();

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    rightClickPopupMenu = new javax.swing.JPopupMenu();
    visibleAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    aspectMenu = new javax.swing.JMenu();
    tightAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    looseAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat1CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat2CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat3CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    aspectMenu2 = new javax.swing.JMenu();
    aselRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
    aselRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
    aselRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
    swapCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    circleMenu = new javax.swing.JMenu();
    circleRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
    circleRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
    circleRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
    natalRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
    natalRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();

    visibleAspectCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
    visibleAspectCheckBoxMenuItem.setSelected(true);
    visibleAspectCheckBoxMenuItem.setText("\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    visibleAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        visibleAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(visibleAspectCheckBoxMenuItem);

    aspectMenu.setText("\u8868\u793a\u30a2\u30b9\u30da\u30af\u30c8\u9078\u629e");
    tightAspectCheckBoxMenuItem.setSelected(true);
    tightAspectCheckBoxMenuItem.setText("\u30bf\u30a4\u30c8\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    tightAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        tightAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    aspectMenu.add(tightAspectCheckBoxMenuItem);

    looseAspectCheckBoxMenuItem.setSelected(true);
    looseAspectCheckBoxMenuItem.setText("\u30eb\u30fc\u30ba\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    looseAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        looseAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    aspectMenu.add(looseAspectCheckBoxMenuItem);

    cat1CheckBoxMenuItem.setSelected(true);
    cat1CheckBoxMenuItem.setText("\u7b2c1\u7a2e\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat1CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat1CheckBoxMenuItemActionPerformed(evt);
      }
    });

    aspectMenu.add(cat1CheckBoxMenuItem);

    cat2CheckBoxMenuItem.setSelected(true);
    cat2CheckBoxMenuItem.setText("\u7b2c2\u7a2e\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat2CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat2CheckBoxMenuItemActionPerformed(evt);
      }
    });

    aspectMenu.add(cat2CheckBoxMenuItem);

    cat3CheckBoxMenuItem.setSelected(true);
    cat3CheckBoxMenuItem.setText("\u7b2c3\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat3CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat3CheckBoxMenuItemActionPerformed(evt);
      }
    });

    aspectMenu.add(cat3CheckBoxMenuItem);

    rightClickPopupMenu.add(aspectMenu);

    aspectMenu2.setText("\u30a2\u30b9\u30da\u30af\u30c8\u5207\u66ff");
    aselRadioButtonMenuItem1.setSelected(true);
    aselRadioButtonMenuItem1.setText("\u5185\u5186\u5bfe\u5916\u5186");
    aspectMenu2.add(aselRadioButtonMenuItem1);

    aselRadioButtonMenuItem2.setText("\u5185\u5186\u306e\u307f");
    aspectMenu2.add(aselRadioButtonMenuItem2);

    aselRadioButtonMenuItem3.setText("\u5916\u5186\u306e\u307f");
    aspectMenu2.add(aselRadioButtonMenuItem3);

    rightClickPopupMenu.add(aspectMenu2);

    swapCheckBoxMenuItem.setText("\u5185\u5186\u3068\u5916\u5186\u3092\u4ea4\u63db");
    rightClickPopupMenu.add(swapCheckBoxMenuItem);

    rightClickPopupMenu.add(jSeparator1);

    circleMenu.setText("\u5186\u306e\u5c5e\u6027");
    circleMenu.setActionCommand("\u5186\u306e\u5f79\u5272\u8a2d\u5b9a");
    circleRadioButtonMenuItem2.setSelected(true);
    circleRadioButtonMenuItem2.setText("\u5916\u5186\u306b\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8");
    circleRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        circleRadioButtonMenuItem2ActionPerformed(evt);
      }
    });

    circleMenu.add(circleRadioButtonMenuItem2);

    circleRadioButtonMenuItem1.setText("\u5185\u5916\u5186\u5171\u306b\u30cd\u30fc\u30bf\u30eb");
    circleRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        circleRadioButtonMenuItem1ActionPerformed(evt);
      }
    });

    circleMenu.add(circleRadioButtonMenuItem1);

    circleRadioButtonMenuItem3.setText("\u5185\u5186\u306b\u30c8\u30e9\u30f3\u30b7\u30c3\u30c8");
    circleRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        circleRadioButtonMenuItem3ActionPerformed(evt);
      }
    });

    circleMenu.add(circleRadioButtonMenuItem3);

    rightClickPopupMenu.add(circleMenu);

    natalRadioButtonMenuItem1.setSelected(true);
    natalRadioButtonMenuItem1.setText("\u5185\u5186\u306b\u30cd\u30fc\u30bf\u30eb\u8aad\u8fbc");
    natalRadioButtonMenuItem1.setEnabled(false);
    rightClickPopupMenu.add(natalRadioButtonMenuItem1);

    natalRadioButtonMenuItem2.setText("\u5916\u5186\u306b\u30cd\u30fc\u30bf\u30eb\u8aad\u8fbc");
    natalRadioButtonMenuItem2.setEnabled(false);
    natalRadioButtonMenuItem2.setMargin(new java.awt.Insets(2, 16, 2, 2));
    rightClickPopupMenu.add(natalRadioButtonMenuItem2);

    setPreferredSize(new java.awt.Dimension(500, 500));
    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 500, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(0, 500, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

  private void circleRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_circleRadioButtonMenuItem3ActionPerformed
    natalRadioButtonMenuItem1.setEnabled(false);
    natalRadioButtonMenuItem2.setEnabled(false);    
  }//GEN-LAST:event_circleRadioButtonMenuItem3ActionPerformed

  private void circleRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_circleRadioButtonMenuItem2ActionPerformed
    natalRadioButtonMenuItem1.setEnabled(false);
    natalRadioButtonMenuItem2.setEnabled(false);
  }//GEN-LAST:event_circleRadioButtonMenuItem2ActionPerformed

  private void circleRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_circleRadioButtonMenuItem1ActionPerformed
    natalRadioButtonMenuItem1.setEnabled(true);
    natalRadioButtonMenuItem2.setEnabled(true);
  }//GEN-LAST:event_circleRadioButtonMenuItem1ActionPerformed

  private void cat3CheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat3CheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.CATEGORY3] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_cat3CheckBoxMenuItemActionPerformed

  private void cat2CheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat2CheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.CATEGORY2] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_cat2CheckBoxMenuItemActionPerformed

  private void cat1CheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cat1CheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.CATEGORY1] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_cat1CheckBoxMenuItemActionPerformed

  private void looseAspectCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_looseAspectCheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.LOOSE] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_looseAspectCheckBoxMenuItemActionPerformed

  private void tightAspectCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tightAspectCheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.TIGHT] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_tightAspectCheckBoxMenuItemActionPerformed

  private void visibleAspectCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visibleAspectCheckBoxMenuItemActionPerformed
    aspectMode[AspectCircle.SHOW] = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
    repaint();
  }//GEN-LAST:event_visibleAspectCheckBoxMenuItemActionPerformed

  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButtonMenuItem aselRadioButtonMenuItem1;
  private javax.swing.JRadioButtonMenuItem aselRadioButtonMenuItem2;
  private javax.swing.JRadioButtonMenuItem aselRadioButtonMenuItem3;
  private javax.swing.JMenu aspectMenu;
  private javax.swing.JMenu aspectMenu2;
  private javax.swing.JCheckBoxMenuItem cat1CheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem cat2CheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem cat3CheckBoxMenuItem;
  private javax.swing.JMenu circleMenu;
  private javax.swing.JRadioButtonMenuItem circleRadioButtonMenuItem1;
  private javax.swing.JRadioButtonMenuItem circleRadioButtonMenuItem2;
  private javax.swing.JRadioButtonMenuItem circleRadioButtonMenuItem3;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JCheckBoxMenuItem looseAspectCheckBoxMenuItem;
  private javax.swing.JRadioButtonMenuItem natalRadioButtonMenuItem1;
  private javax.swing.JRadioButtonMenuItem natalRadioButtonMenuItem2;
  private javax.swing.JPopupMenu rightClickPopupMenu;
  private javax.swing.JCheckBoxMenuItem swapCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem tightAspectCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem visibleAspectCheckBoxMenuItem;
  // End of variables declaration//GEN-END:variables
  
}
