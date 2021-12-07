/*
 * HoroscopePanel.java
 *
 * Created on 2006/10/26, 17:48
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.Frame;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import static to.tetramorph.starbase.lib.Const.*;
import static java.lang.Math.*;
import static java.awt.Color.*;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.chartplate.AspectCircle;
import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.ChartFactor;
import to.tetramorph.starbase.module.ChartModulePanel;
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
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.util.Preference;

/**
 * ホロスコープ一重円のチャートモジュール
 */
public class NatalChartPlugin extends ChartModulePanel {
  WordBalloon wordBalloon;
  Graphics2D g;
  boolean [] aspectMode = { true,true,true,true,true,true,true };
  Body selectedPoint;
  AspectType [] useAspectTypes; //使用するｱｽﾍﾟｸﾄの種類の設定
  Aspect selectedAspect;
  int [] bodys;       //表示する天体ID
  int [] aspBodys;    //アスペクト検出する天体ID
  ChartData chartData; //calc()の入力パラメター
  ChartFactor chartFactor;
  private static final String [] channelNames = { "ネイタル" };

  double [] cusps;
  String [] cuspsString;
  List<Body> bodyList; //天体リスト
  List<Body> specialList = new ArrayList<Body>(); //AC,MC等のリスト
  double [] planetsAngle;
  String [] planetsAngleString;
  List<Aspect> aspectList;
  double asc;
  Frame parentFrame;
  //-------------------------------------------------------------------------
  HouseRing zodiacRing = new HouseRing();
  DialGauge gauge = new DialGauge();
  DialGauge gauge2 = new DialGauge();
  SignDial signDial = new SignDial();
  PlanetNeedle planetNeedle = new PlanetNeedle(this,null);
  PlanetNeedle specialPlanetNeedle = new PlanetNeedle();
  HouseRing houseRing = new HouseRing();
  HouseRing houseRing2 = new HouseRing();
  MarkerNeedle markerNeedle = new MarkerNeedle();
  MarkerNeedle markerNeedle2 = new MarkerNeedle();
  NumberNeedle cuspAngleNumberNeedle = new NumberNeedle();
  NumberNeedle planetAngleNumberNeedle = new NumberNeedle();
  HouseNumberDial houseNumberDial = new HouseNumberDial();
  AspectCircle aspectCircle = new AspectCircle();  

  HoroscopeMouseHandler mouseHandler = null;
  SabianMouseHandler sabianMouseHandler = new SabianMouseHandler();
  MouseHandler aspectMouseHandler = new MouseHandler();

  //デフォルトの色設定名
  String colorConfName;
  //色設定フィールド (数が多くてうざいので外に追い出した)
  NatalChartColors defaultColors = new NatalChartColors();
  //色設定パネル
  CustomizePanel colorCustomizePanel = new NatalColorConfPanel();
  // --------------------------------------------------------------------
  //デフォルトの仕様設定名
  String spcificConfName;
  //仕様設定パネル
  NatalCalcConfPanel specificCustomizePanel = new NatalCalcConfPanel();
  /** 
   * チャートモジュールを作成する。
   * Frameを引き渡すのは、WordBalloonに必要だから。最終的には変更するかも。
   * 事実上parentにはMainFrameのインスタンスが入っている。引数は今後も増えそうな
   * 予感がするので、専用のクラスを作ってラップしたほうが良いかもしれない。
   */
  public void init() {
    initComponents();
    //for(int i : bodys) bodySet.add(i);
    gauge.setOption(DialGauge.NOT_DRAW_ZERO_DEGREES);
    markerNeedle2.setDirection(MarkerNeedle.OUTER);
    zodiacRing.setPaintCondition(
    HouseRing.FILL|HouseRing.SEPARATOR|HouseRing.INNER_ARC|HouseRing.OUTER_ARC);
    houseRing.separatorLineColor = Color.GRAY;
    setBackground(Color.WHITE);
    colorConfName = config.getProperty("DefaultColorConfName","");
    System.out.println("Init , colorConfName = " + colorConfName);
    toolInit();
    sabianToggleButton.doClick();
    // 色設定のデフォルト値をセット
    Preference pref = new Preference();
    defaultColors.getPreference(pref);
    setDefaultColor(pref);
    // 仕様設定のデフォルト値をセット
    Preference specificPref = new Preference();
    specificCustomizePanel.getPreference(specificPref);
    setDefaultSpecific(specificPref);
  }
  /** 
   * 出生データを受け取る。出生データや出来事データなど日時場所の基礎データを
   * 受け取り、それに基づいて星の位置を計算しホロスコープを描画する。
   */
  public void setData(ChannelData channelData) {
    //ChartData chartData = channelData.get(0);
    chartData = channelData.get(0);
    chartData.setFrameIcon();
    if(chartData.getSelectedIndex() < 0) {
      //トランシットを禁止してるのであり得ない。エラーでもレポートせよ。
      return;
    }
    //setSpcificConfigがsetDataより先に呼び出され、そのときにbodys[]がセットされ
    //るため、ここではchartDataだけを引き渡している。
    calc(); 
  }
  //色の設定が変更されたときに呼び出される
  //またinit()の次にも呼び出される。
  public void setColorConfig(Preference colorPref) {
    System.out.println("setColorConfig(..)");
    NatalChartColors c = new NatalChartColors();
    c.setPreference(colorPref);  
    zodiacRing.bgColors = c.signBackgrounds;
    zodiacRing.isNoBackground = c.isNoSignBackgrounds;
    zodiacRing.isNoBorder = c.isNoSignBorders;
    zodiacRing.innerLineColor = c.signsBorder;
    zodiacRing.outerLineColor = c.signsBorder;
    zodiacRing.separatorLineColor = c.signsBorder;
    signDial.borderColors = c.signSymbolBorders;
    signDial.symbolColors = c.signSymbols;
    signDial.isNoSymbolBorders = c.isNoSignSymbolBorders;
    setBackground(c.background);
    repaint();
  }
  // 仕様設定が変更されたときに呼び出される
  //またsetColorConfig()の次にも呼び出される。
  public void setSpecificConfig(Preference specPref) {
    System.out.println("setSpecificConfig(..)");
    bodys = specPref.getIntArray("NatalBodys"); //specificCustomizePanel.getBodyIDs();
    aspBodys = specPref.getIntArray("NatalAspectBodys");
    useAspectTypes = specPref.getAspectTypes("UseAspects"); //specificCustomizePanel.getAspectTypes();
    for(int i=0; i<useAspectTypes.length; i++) {
      System.out.println(useAspectTypes[i].toString());
    }
    calc();
  }
  // 描画に必要な天体位置を計算したのち描画トリガをかける。
  // 入力パラメターはchartDataの他にbodys[]が必要だが、それはフィールド変数から
  // 取得する。bodys[]はsetSpecificConfig()でセットされる。
  void calc() {
    if(bodys == null) return; //setSpecificConfit()で設定される
    if(chartData == null) return; //setData()で設定される。
    List<Integer> acmcList = new ArrayList<Integer>();
    List<Integer> planetList = new ArrayList<Integer>();
    for(int i : bodys) {
      if(i == AC || i == MC || i==DC || i==IC) acmcList.add(i);
      else planetList.add(i);
    }
    Data  data = chartData.getDataList().get( chartData.getSelectedIndex() );

    //ホロスコープ一重円を描くのに必要なパラメター
    chartFactor = new ChartFactor(Ephemeris.getSwissEph(),config,bodys);
    chartFactor.setDateAndPlace(data.getTimePlace());
    bodyList = chartFactor.getPlanets(planetList,true); //bodys11,AC,MC等を除外した天体リスト
    planetsAngle = ChartFactor.getPlanetsPlotAngle(bodyList);
    planetsAngleString = ChartFactor.formatSignAngles(chartFactor.getPlanetsAngle(bodyList),0); //天体の表示用度数リスト
    specialList = chartFactor.getPlanets(acmcList,true); //specialBodysAC,MC等の感受点リスト
    cusps = chartFactor.getCusps(); //カスプのリスト
    cuspsString = ChartFactor.formatSignAngles(cusps,0); //表示用カスプ度数のリスト
    aspectList = AspectFinder.getAspects(chartFactor.getPlanets(aspBodys,false),useAspectTypes);
    //
    Body p = chartFactor.getBody(AC);
    if(p == null) {
      Body sun = chartFactor.getBody(SUN);
      asc = sun.getSign() * 30f;
    } else asc = p.lon;
    repaint();
  }
  // このモジュール用のカラー設定パネルを返す。
  public CustomizePanel getColorCustomizePanel() {
    return colorCustomizePanel;
  }
  public CustomizePanel getSpecificCustomizePanel() {
    return specificCustomizePanel;
  }
  /** ホロスコープを描画する */
  public void paintComponent(Graphics graphics) {
    if(chartFactor == null) return;
    super.paintComponent(graphics);
    g = (Graphics2D)graphics;
    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = getSize();
    double width = (float)size.getWidth();
    double height = (float)size.getHeight();
    double cx = width / 2d;
    double cy = height / 2d;
    double W = width < height ? width : height;
    // キャンバスの幅 W=1に対して・・・
    double d0 = 0.85; //獣帯リングの外円の直径
    double d0w = 0.1; //獣帯リングの幅
    double d1 = 0.57; //一番内側の円の直径
    double d2 = d1 + (d0-d0w-d1)/2.0; //惑星軌道の直径

    //この辺の処理はのちのち別クラスにしてカプセル化するだろう

    houseRing.setPaintCondition(HouseRing.SEPARATOR|HouseRing.INNER_ARC);
    houseRing.setFactor(g,cx,cy,W,asc,d1,d0-d0w,cusps);
    houseRing2.setPaintCondition(HouseRing.SEPARATOR);
    houseRing2.setFactor(g,cx,cy,W,asc,d0-d0w,d0+0.05,cusps);
    markerNeedle2.setExtension(d2 - 0.05);
    zodiacRing.setFactor(g,cx,cy,W,asc,d0-d0w,d0,null);
    gauge.setFactor(g,cx,cy,W,asc,d0-d0w);
    gauge2.setFactor(g,cx,cy,W,asc,d1);
    signDial.setFactor(g,cx,cy,W,asc,d0-d0w/2d,0.03f);
    planetNeedle.setFactor(g,cx,cy,W,asc,d2,0.03,bodyList,selectedAspect);
    specialPlanetNeedle.setFactor(g,cx,cy,W,asc,d0+0.07,0.04,specialList,selectedAspect);
    markerNeedle.setFactor(g,cx,cy,W,asc,d0-d0w,bodyList);
    markerNeedle2.setFactor(g,cx,cy,W,asc,d1,bodyList);
    cuspAngleNumberNeedle.setFactor(g,cx,cy,W,asc,d0+0.01,0.015,cusps,cuspsString);
    cuspAngleNumberNeedle.setVOffset(0.018);
    cuspAngleNumberNeedle.setAlign(NumberNeedle.OUTER);
    houseNumberDial.setFactor(g,cx,cy,W,asc,d0+0.03,0.02,cusps);
    planetAngleNumberNeedle.setFactor(g,cx,cy,W,asc,d2 + 0.07,0.015,planetsAngle,planetsAngleString);
    planetAngleNumberNeedle.setVOffset(0.0085);
    aspectCircle.setFactor(g,cx,cy,W,asc,d1,aspectList,aspectMode,selectedPoint,0.02);
    zodiacRing.draw();
    //signDial.draw();
    houseRing.draw();
    houseRing2.draw();
    markerNeedle2.draw();
    planetNeedle.draw();
    specialPlanetNeedle.draw();
    markerNeedle.draw();
    cuspAngleNumberNeedle.draw();
    planetAngleNumberNeedle.draw();
    houseNumberDial.draw();
    gauge.draw();
    gauge2.draw();
    aspectCircle.draw();
    signDial.draw();
    System.out.println("ペイントした");
  }
  //サビアンモードのマウスハンドラ
  class SabianMouseHandler extends HoroscopeMouseHandler {
    Object selobj;
    public void mouseReleased(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      if(e.getClickCount() != 1) return;
      if(e.getButton() == MouseEvent.BUTTON1) {
        boolean planetSelected = 
          planetNeedle.contains(x,y) || specialPlanetNeedle.contains(x,y);
        Body p = planetNeedle.getSelectedBody();
        if(p == null) p = specialPlanetNeedle.getSelectedBody();
        if(planetSelected) {
          SabianDialogHandler dialog = getSabianDialogHandler();
          if(dialog.isVisible()) {
            dialog.setChartFactor(chartFactor);
            dialog.setSelect((int)p.lon);
          } else {
            wordBalloon.show(Caption.getSabianCaption(p,Sabian.JP));
          }
        }
      }
    }
    public void mouseClicked(MouseEvent e) {
      if(e.getButton() != MouseEvent.BUTTON1) return;
      int click = e.getClickCount();
      int x = e.getX();
      int y = e.getY();
      if(click == 2) {
        SabianDialogHandler dialog = getSabianDialogHandler();
        dialog.setVisible(true);
        Body p = planetNeedle.getSelectedBody();
        if(p == null) p = specialPlanetNeedle.getSelectedBody();
        if(p != null) {
          dialog.setChartFactor(chartFactor);
          dialog.setSelect((int)p.lon);
        }
      }
    }
    public void mouseMoved(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      boolean selected = planetNeedle.contains(x,y) || specialPlanetNeedle.contains(x,y);
      if(selected) {
        selobj = planetNeedle.getSelectedBody();
        if(selobj == null) selobj = specialPlanetNeedle.getSelectedBody();
        if(selobj != null) repaint();
      } else selobj = null;
    }
    //選択されているときはそのオブジェクト、されてないときはnullをワードバルーンに教える
    public Object getSelectedObject() {
      return selobj;
    }    
  }

  //アスペクトモードのマウスハンドラ
  class MouseHandler extends HoroscopeMouseHandler {
    Point mousePoint = new Point(0,0);
    Object selectedObject;
    Body preBody;
    public void mouseClicked(MouseEvent e) {
      System.out.println("アスペクトモードでマウスがクリックされたよ");
      int click = e.getClickCount();
      int x = e.getX();
      int y = e.getY();
      if(e.getButton() == MouseEvent.BUTTON3) {
        //System.out.println("中指クリック");
        if(! aspectCircle.isContainCircle(x,y) && click == 1) {
          rightClickPopupMenu.show(NatalChartPlugin.this,x,y);
        }
      } else if(e.getButton() == MouseEvent.BUTTON1) {
        //System.out.println("人差し指クリック");
        boolean planetSelected = planetNeedle.contains(x,y) || specialPlanetNeedle.contains(x,y);
        Body p = planetNeedle.getSelectedBody();
        if(p == null) p = specialPlanetNeedle.getSelectedBody();
        if(planetSelected && click == 1) {
          selectedPoint = p;
          repaint();
        } else if( aspectCircle.contains(x,y) && click == 1) {
          //ｱｽﾍﾟｸﾄ線が選択
          wordBalloon.show(aspectCircle.getSelectedAspect().getCaption());
        } else if(! aspectCircle.isContainCircle(x,y) && click == 1) {
          selectedPoint = null;
          repaint();
        }
      }
    }
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      Body p = null;
      if(planetNeedle.contains(x,y)) {
        p = planetNeedle.getSelectedBody();
      }
      if(p != null) {
        selectedObject = p;
        repaint();
        return;
      }
      Aspect asp = null;
      if(aspectCircle.contains(x,y)) {
        asp = aspectCircle.getSelectedAspect();
      }
      if(asp != selectedAspect) {
        selectedAspect = asp;
        selectedObject = asp;
        repaint();        
      }
    }
    //オンカーソルイベントを拾う
    public Object getSelectedObject() {
      return selectedObject;
    }
  }
  //---------------------------------------------------------------------------
  /** このチャートモジュール名を返す。*/
  public String toString() {
    return "ネイタル円プラグイン";
  }  
  /** このチャートモジュールのチャンネル数を返す。*/
  public int getChannelSize() {
    return 1;
  }
  /** このチャートモジュールがトランシットを受け取るか返す。*/
  public boolean isNeedTransit() {
    return false;
  }
  /** このチャートモジュールの各チャンネルの名前を返す。*/
  public String[] getChannelNames() {
    return channelNames;
  }
  /**
   * ツールバーに入れる制御ボタンパネルを返す。
   */
  public JComponent getToolComponent() {
    return toolButtonPanel;
  }
  
  void toolInit() {
    toolButtonGroup.add(aspectToggleButton);
    toolButtonGroup.add(transitToggleButton);
    toolButtonGroup.add(sabianToggleButton);
  }
  void setMouseHandler(HoroscopeMouseHandler h) {
    if(mouseHandler != null) {
      removeMouseListener(mouseHandler);
      removeMouseMotionListener(mouseHandler);
    }
    if(wordBalloon == null) 
      wordBalloon = new WordBalloon(h);
    else
      wordBalloon.setWordBalloonHandler(h);
    addMouseListener(h);
    addMouseMotionListener(h);
    mouseHandler = h;
  }
 
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    rightClickPopupMenu = new javax.swing.JPopupMenu();
    visibleAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    tightAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    looseAspectCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat1CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat2CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    cat3CheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    toolButtonPanel = new javax.swing.JPanel();
    aspectToggleButton = new javax.swing.JToggleButton();
    transitToggleButton = new javax.swing.JToggleButton();
    sabianToggleButton = new javax.swing.JToggleButton();
    toolButtonGroup = new javax.swing.ButtonGroup();

    visibleAspectCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
    visibleAspectCheckBoxMenuItem.setSelected(true);
    visibleAspectCheckBoxMenuItem.setText("\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    visibleAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        visibleAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(visibleAspectCheckBoxMenuItem);

    tightAspectCheckBoxMenuItem.setSelected(true);
    tightAspectCheckBoxMenuItem.setText("\u30bf\u30a4\u30c8\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    tightAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        tightAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(tightAspectCheckBoxMenuItem);

    looseAspectCheckBoxMenuItem.setSelected(true);
    looseAspectCheckBoxMenuItem.setText("\u30eb\u30fc\u30ba\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    looseAspectCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        looseAspectCheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(looseAspectCheckBoxMenuItem);

    cat1CheckBoxMenuItem.setSelected(true);
    cat1CheckBoxMenuItem.setText("\u7b2c1\u7a2e\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat1CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat1CheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(cat1CheckBoxMenuItem);

    cat2CheckBoxMenuItem.setSelected(true);
    cat2CheckBoxMenuItem.setText("\u7b2c2\u7a2e\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat2CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat2CheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(cat2CheckBoxMenuItem);

    cat3CheckBoxMenuItem.setSelected(true);
    cat3CheckBoxMenuItem.setText("\u7b2c3\u30a2\u30b9\u30da\u30af\u30c8\u8868\u793a");
    cat3CheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cat3CheckBoxMenuItemActionPerformed(evt);
      }
    });

    rightClickPopupMenu.add(cat3CheckBoxMenuItem);

    toolButtonPanel.setLayout(new java.awt.GridLayout(2, 2, 0, 2));

    aspectToggleButton.setText("\u30a2\u30b9\u30da\u30af\u30c8");
    aspectToggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aspectToggleButtonActionPerformed(evt);
      }
    });

    toolButtonPanel.add(aspectToggleButton);

    transitToggleButton.setText("\u5929\u4f53\u79fb\u52d5");
    transitToggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        transitToggleButtonActionPerformed(evt);
      }
    });

    toolButtonPanel.add(transitToggleButton);

    sabianToggleButton.setSelected(true);
    sabianToggleButton.setText("\u30b5\u30d3\u30a2\u30f3");
    sabianToggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sabianToggleButtonActionPerformed(evt);
      }
    });

    toolButtonPanel.add(sabianToggleButton);

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

  private void transitToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitToggleButtonActionPerformed
//    if(transitToggleButton.isSelected())
//      setMouseHandler(transitMouseHandler);
  }//GEN-LAST:event_transitToggleButtonActionPerformed

  private void aspectToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aspectToggleButtonActionPerformed
    if(aspectToggleButton.isSelected())
      setMouseHandler(aspectMouseHandler);
  }//GEN-LAST:event_aspectToggleButtonActionPerformed

  private void sabianToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sabianToggleButtonActionPerformed
    if(sabianToggleButton.isSelected())
      setMouseHandler(sabianMouseHandler);
  }//GEN-LAST:event_sabianToggleButtonActionPerformed

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
  private javax.swing.JToggleButton aspectToggleButton;
  private javax.swing.JCheckBoxMenuItem cat1CheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem cat2CheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem cat3CheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem looseAspectCheckBoxMenuItem;
  private javax.swing.JPopupMenu rightClickPopupMenu;
  private javax.swing.JToggleButton sabianToggleButton;
  private javax.swing.JCheckBoxMenuItem tightAspectCheckBoxMenuItem;
  private javax.swing.ButtonGroup toolButtonGroup;
  private javax.swing.JPanel toolButtonPanel;
  private javax.swing.JToggleButton transitToggleButton;
  private javax.swing.JCheckBoxMenuItem visibleAspectCheckBoxMenuItem;
  // End of variables declaration//GEN-END:variables
  
}
