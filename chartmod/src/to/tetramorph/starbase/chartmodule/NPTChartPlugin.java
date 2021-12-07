/*
 * NPTChartPlugin.java
 *
 * Created on 2007/07/06, 1:42
 */

package to.tetramorph.starbase.chartmodule;
import to.tetramorph.starbase.chartlib.NatalNameTable;
import to.tetramorph.starbase.chartlib.TransitNameTable;
import to.tetramorph.starbase.chartlib.BodyTable;
import to.tetramorph.starbase.chartlib.CuspTable;
import to.tetramorph.starbase.chartlib.MethodTable;
import to.tetramorph.starbase.chartlib.NodeApogeeTable;

import to.tetramorph.starbase.chartparts.PlanetEvent;
import static to.tetramorph.starbase.lib.Const.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.chartlib.NPTChartDictionaryTool;
import to.tetramorph.starbase.chartparts.AspectCircle;
import to.tetramorph.starbase.chartparts.AspectCircleAdapter;
import to.tetramorph.starbase.chartparts.AspectCircleEvent;
import to.tetramorph.starbase.chartparts.AspectCombinationPanel;
import to.tetramorph.starbase.chartparts.BasicBodyMoverDialog;
import to.tetramorph.starbase.chartparts.GLayout;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.util.NPTChart;
import to.tetramorph.starbase.chartparts.PlanetActionAdapter;
import to.tetramorph.starbase.chartparts.PlanetMotionListener;
import to.tetramorph.starbase.chartparts.PlanetRing;
import to.tetramorph.starbase.chartparts.RingActionListener;
import to.tetramorph.starbase.chartparts.RingEvent;
import to.tetramorph.starbase.chartparts.ZodiacPanel;
import to.tetramorph.starbase.chartparts.ZodiacRing;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Caption;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.module.ChartModuleMode;
import to.tetramorph.starbase.module.DictionaryActionFile;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.util.DictionaryRequest;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.util.Preference;
/**
 * NPT3重円プラグイン。
 * @author 大澤義鷹
 */
public class NPTChartPlugin extends ChartModulePanel {
    // 惑星リング幅の初期値
    private static final double DEFAULT_PLANET_RING_WIDTH = 0.07;
    ChartData chartData; //calc()の入力パラメター
    NPTChart nptChart;
    private static final String [] channelNames = { "ネイタル","トランジット" };
//    static final String DICT_TYPE = "NPTChart";
//    static final DictionaryActionFile DICTIONARY_ACTION_FILE =
//            new DictionaryActionFile(
//                NPTChartPlugin.class.getResource("/resources/npt_action.xml"),
//                DICT_TYPE );

    double asc;
    ZodiacRing zodiacRing;
    PlanetRing natalPlanetRing;
    PlanetRing progressPlanetRing;
    PlanetRing transitPlanetRing;
    PlanetRing [] planetRings; //ﾙｰﾌﾟからの参照用で、各NPTのﾘﾝｸﾞが格納されている。
    ZodiacPanel zodiacPanel;
    Transit transit;
    AspectCircle aspectCircle;
    int aspectCommand = 0;
    AspectCombinationPanel acp = new AspectCombinationPanel(); //アスペクト組合せ選択パネル
    NPTSpecificSettingPanel scp = new NPTSpecificSettingPanel(acp);//仕様設定パネル
    NPTColorSettingPanel ccp = new NPTColorSettingPanel();      //色設定パネル

    ChartConfig cc = new ChartConfig();
    BasicBodyMoverDialog moverDialog;
    ChannelData channelData;
    //アスペクト線を引くように選択された天体、非選択時はnull
    Body selectedAspectBody;
    Body actionBody; //PlanetHadlerで使用。イベントの発生した天体が入る。
    boolean isAspectVisible = true;

    BalloonHandler wbh = new BalloonHandler();
    WordBalloon wordBalloon = new WordBalloon(wbh);
    int showRings = 0;
    static final String [] groups = new String[] { "N","P","T" };
    // calc()計算直後の各天体のリスト
    List<List<Body>> nptBodyList = new ArrayList<List<Body>>();
    List<List<Body>> nptCuspList = new ArrayList<List<Body>>();
    List<Component> viewMenuList = new ArrayList<Component>();
    CuspTable cuspTable;
    BodyTable bodyTable;
    NatalNameTable natalNameTable;
    TransitNameTable transitNameTable;
    MethodTable methodTable;
    NodeApogeeTable nodeApogeeTable;

    AspectCirclePopupMenu aspectCirclePopupMenu = new AspectCirclePopupMenu();
    BodyPopupMenu bodyPopupMenu = new BodyPopupMenu();

    /**
     * チャートモジュールを作成する。
     * Frameを引き渡すのは、WordBalloonに必要だから。最終的には変更するかも。
     * 事実上parentにはMainFrameのインスタンスが入っている。引数は今後も増えそうな
     * 予感がするので、専用のクラスを作ってラップしたほうが良いかもしれない。
     */
    @Override
    public void init() {
        initComponents();
        //アスペクト円内のアスペクト選択メニュー処理

//        aspectCirclePopupMenu.item0.addActionListener( new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent evt) {
//                JCheckBoxMenuItem item = (JCheckBoxMenuItem)evt.getSource();
//                if ( item.isSelected() ) {
//                    selectedAspectBody = null;
//                    isAspectVisible = true;
//                    aspectCircle.clearHideAspects();
//                } else {
//                    isAspectVisible =false;
//                }
//                calc();
//            }
//        });

        aspectCirclePopupMenu.item1.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                selectedAspectBody = null;
                isAspectVisible = true;
                aspectCircle.clearHideAspects();
                calc();
            }
        });

        aspectCirclePopupMenu.item2.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                isAspectVisible =false;
                calc();
            }
        });
        aspectCirclePopupMenu.item3.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aspectCircle.hideMarkingAspects();
                zodiacPanel.repaint();
            }
        });

        aspectCirclePopupMenu.item4.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aspectCircle.hideUnmarkingAspects();
                zodiacPanel.repaint();
            }
        });
        aspectCirclePopupMenu.item5.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aspectCircle.clearMarkingAspects();
                zodiacPanel.repaint();
            }
        });
        aspectCirclePopupMenu.item6.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_RING, 0 );
                showSpecificCustomizeDialog();
            }
        });
        aspectCirclePopupMenu.item7.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_ORB,
                                    acp.getShowRings() );
                showSpecificCustomizeDialog();
            }
        });

        //天体を右クリックで表示されるポップアップメニューの設定
        bodyPopupMenu.item1.addActionListener( new BodyAspectHandler() );
        bodyPopupMenu.item2.addActionListener( new SabianHandler() );
        bodyPopupMenu.item3.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PLANET, 0 );
                showSpecificCustomizeDialog();
            }
        });

        bodyPopupMenu.item4.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PLANET, 1 );
                showSpecificCustomizeDialog();
            }
        });

        bodyPopupMenu.item5.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PROGRESSION, 0 );
                showSpecificCustomizeDialog();
            }
        });

        bodyPopupMenu.item6.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                scp.setSelectedTab( NPTSpecificSettingPanel.TAB_HOUSE, 0 );
                showSpecificCustomizeDialog();
            }
        });



        setBackground(Color.WHITE);
        for ( int i=0; i < 3; i++ ) {
            nptBodyList.add(new ArrayList<Body>());
            nptCuspList.add(new ArrayList<Body>());
        }
        moverDialog = new BasicBodyMoverDialog( parentFrame );
        super.getChartConfig( cc );
        scp.setHouseSystemCode( cc.getHouseSystemCode() );
        nptChart = new NPTChart(cc);

        zodiacPanel = new ZodiacPanel();
        // 画面がクリックされたときにシャッターを閉じる
        zodiacPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                iframe.contentSelected();
            }
        });
        zodiacRing = new ZodiacRing( 0.8, 0.04 ); //new ZodiacRing();
        zodiacPanel.setZodiacRing(zodiacRing);

        natalPlanetRing    = new PlanetRing( DEFAULT_PLANET_RING_WIDTH );
        transitPlanetRing  = new PlanetRing( DEFAULT_PLANET_RING_WIDTH );
        progressPlanetRing = new PlanetRing( DEFAULT_PLANET_RING_WIDTH );
        planetRings = new PlanetRing[] {
            natalPlanetRing, progressPlanetRing, transitPlanetRing
        };
        natalPlanetRing.setZodiacRing( zodiacRing );
        PlanetHandler planetHandler = new PlanetHandler();
        BodyMotionHandler bmh = new BodyMotionHandler();
        RingHandler rh = new RingHandler();
        for ( int i = 0; i < 3; i++ ) {
            planetRings[i].setPlanetActionListener(planetHandler);
            planetRings[i].setPlanetMotionListener(bmh);
            planetRings[i].setRingActionListener(rh);
            planetRings[i].setNotDragBodys(nptChart.getNotDragBodys(i));
            planetRings[i].setGroup(i);
        }

        aspectCircle = new AspectCircle();
        aspectCircle.setAspectCircleListener( new AspectCircleHandler() );
        aspectCircle.setPlanetRings( planetRings );
        zodiacPanel.setAspectCircle( aspectCircle );
        add( zodiacPanel );


        // 仕様設定のデフォルト値をセット
        Preference specificPref = new Preference();

        // NPTCalcConfiPanel()(scp)の初期化時の設定値をデフォルトとしてspecificConfigにコピーする
        scp.getPreference( specificPref );
        // 時刻または場所が不明の時のハウス分割法を、環境設定の値(config)から引き継ぐ
        // ために、取得したデフォルト値を書き換える
        specificPref.setProperty( "CuspUnknownHouseSystem", config.getProperty("CuspUnknownHouseSystem"));
        specificPref.setProperty( "PrioritizeSolar", config.getProperty("PrioritizeSolar"));
        // デフォルト値をモジュールの親に登録する
        setDefaultSpecific( specificPref );


        Preference colorPref = new Preference();
        ccp.getPreference( colorPref );
        setDefaultColor( colorPref );
        //
        viewMenuList.add( acp.getRingMenu() );
        viewMenuList.add( acp.getBodyListModeMenu() );
        viewMenuList.add( new JSeparator() );
        viewMenuList.add( acp.getAspectCombinationMenu() );
        viewMenuList.add( scp.getAspectCategorysMenu() );
        viewMenuList.add( acp.getAspectTargetMenu() );
        viewMenuList.add( new JSeparator() );
        viewMenuList.add( scp.getDirectLineModeMenuItem() );
        viewMenuList.add( new JSeparator() );
        viewMenuList.add( resetAcMenuItem );
        viewMenuList.add( resetRingWidthMenuItem );
        viewMenuList.add( new JSeparator() );
        viewMenuList.add( viewmodeCheckBoxMenuItem );
        //
        natalNameTable = new NatalNameTable();
        transitNameTable = new TransitNameTable();
        cuspTable = new CuspTable( planetRings );
        bodyTable = new BodyTable( planetRings );
        methodTable = new MethodTable();
        nodeApogeeTable = new NodeApogeeTable();
    }


    /**
     * 出生データを受け取る。出生データや出来事データなど日時場所の基礎データを
     * 受け取り、それに基づいて星の位置を計算しホロスコープを描画する。
     */
    @Override
    public void setData( ChannelData channelData ) {
        this.channelData = channelData;
        chartData = channelData.get(0);
        transit = channelData.getTransit();
        chartData.setTabIcon();
        if ( chartData.getSelectedIndex() < 0 ) return;
        calc();
    }

    /**
     * selectedAspectBodyがnullならaspectCommandにコンビネーションフラグを入れて計算。
     * nullではないときはaspectCommandにn=0,p=1,t=2のいずれかがセットされていること。
     */
    void calc() {
        if ( chartData == null ) return;
        showRings();
        //System.out.println("scp.getHouseSystemCode = " + scp.getHouseSystemCode() );
        //System.out.println("scp.scp.getCuspUnknownHouseSystem()" + scp.getCuspUnknownHouseSystem() );
        cc.setHouseSystemCode( scp.getHouseSystemCode() ); //ハウス分割法を指定
        nptChart.setProgressMode( scp.getProgressCode() );
        Data  natalData =
            chartData.getDataList().get( chartData.getSelectedIndex() );
        natalNameTable.setData( chartData.getSelectedData() );
        transitNameTable.setTransit( transit );
        //
        nptChart.setData(natalData);
        nptChart.setTransit(transit);
        natalPlanetRing.setName( natalData.getNatal().getName() );
        natalPlanetRing.setTimePlace( natalData.getTimePlace() );
        transitPlanetRing.setTimePlace(transit);

        natalPlanetRing.setRingName("Natal " + natalData.getNatal().getName() );
        progressPlanetRing.setRingName(
                                 "Progress " + natalData.getNatal().getName() );
        transitPlanetRing.setRingName("Transit");

        //NTP各天体位置を求める
        for ( int i=0; i<3; i++ ) {
            nptBodyList.set( i, nptChart.getBodyList(scp.getBodyIDs(i),i));
            nptCuspList.set( i, nptChart.getCuspList( i ));
            planetRings[i].setBodyList( nptBodyList.get( i ) );
            planetRings[i].setCusps( nptCuspList.get( i ) );
        }
        methodTable.setMethodNames( nptChart.getHouseSystemName(),
                                    nptChart.getProgressMethodName() );

        //アスペクト表示
        if ( isAspectVisible ) {
            aspectCircle.setShowAspectCategorys( scp.getAspectCategorys() );
            aspectCircle.setDirectLineMode( scp.getDirectLineMode() );
            aspectCircle.setSelectedBody( selectedAspectBody );
            List<Aspect> list = null;
            if ( selectedAspectBody == null ) {        //通常のアスペクト
                aspectCommand = acp.getAspectCombinations();
                list = NPTAspectFinder
                    .getAspectList( aspectCommand, nptChart, scp );
            } else {                                   //選択天体とのアスペクト
                int aspectTarget = acp.getAspectTargets();
                // 出生データが別のものに変わる場合があるので取得しなおす
                int id = selectedAspectBody.id;
                int group = selectedAspectBody.group;
                selectedAspectBody = nptChart.getBody( id, group );
                list = NPTAspectFinder.getTargetAspectList(
                    aspectCommand, selectedAspectBody, nptChart, scp, aspectTarget );
            }
            aspectCircle.setAspects(list);
        } else {
            aspectCircle.setAspects( new ArrayList<Aspect>() );
        }
        setAC();
        report();
        zodiacPanel.repaint();
    }

    static final int [] ac_array = new int [] { 0,0,1,2,0,0,1 };

    /**
     * アセンダントの位置が画面左の定位置にくるように設定する。
     */
    private void setAC() {
        int npt = ac_array[ acp.getShowRings() ];
        Body p = nptChart.getBody( AC, npt );
        if ( p == null ) {
//            Body sun = nptChart.getBody(SUN,npt);
//            asc = sun.getSign() * 30f;
            asc = nptChart.getBody( CUSP1, npt ).lon;
        } else asc = p.lon;
        zodiacPanel.setAscendant(asc);
    }
//  private void setAC() {
//    int npt = ac_array[ scp.getShowRings() ];
//    Body p = nptChart.getBody(AC,scp.getBodyListMode());
//    if(p == null) {
//      Body sun = nptChart.getBody(SUN,scp.getBodyListMode());
//      asc = sun.getSign() * 30f;
//    } else asc = p.lon;
//    zodiacPanel.setAscendant(asc);
//  }
//  private void setAC() {
//    Body p = nptChart.getBody(AC,npt);
//    if(p == null) {
//      Body sun = nptChart.getBody(SUN,npt);
//      asc = sun.getSign() * 30f;
//    } else asc = p.lon;
//    zodiacPanel.setAscendant(asc);
//  }

    //天体位置を天体ラベルリストに書きだしと、zodiacRingの登録
    private void report() {
        for ( int i=0,mode = acp.getBodyListMode(); i<3; i++) {
            if ( mode == i ) {
                bodyTable.setBodyList( nptBodyList.get( i ) );
                cuspTable.setCuspList( nptCuspList.get( i ) );
                break;
            }
        }
    }

    /**
     * NPTの各リングを設定に応じて表示したり隠したりする。
     * フィールド変数showRingsが現在の表示状況を表し、設定パネルのshowRings()と
     * 値が異なる場合だけリングの抜き刺し処理をする。
     * メソッド内部では一旦すべてのリングを抜き取り、再挿入する処理をする。
     */
    private void showRings() {
        //配列の値はb2=T,b1=P,b0=N
        int flag = new int []  { 7, 1, 2, 4, 5, 3, 6 }[ acp.getShowRings() ];
        if( flag == showRings ) return; //設定変更されてないときは処理しない。
        for ( PlanetRing r : planetRings ) zodiacPanel.removePlanetRing(r);
        for ( int i=0,j=1; i<3; i++ ) {
            if ( (flag & j) != 0 ) zodiacPanel.addPlanetRing( planetRings[i] );
            j = j << 1;
        }
        int [][] tableVisibles = new int [][] {
            { 1, 1 }, // NPT
            { 1, 0 }, // N
            { 1, 1 }, // P
            { 0, 1 }, // T
            { 1, 1 }, // NT
            { 1, 1 }, // NP
            { 1, 1 }  // PT
        };
        int [] v = tableVisibles[ acp.getShowRings() ];
        if ( v[0] == 1 ) {
            if ( ! zodiacPanel.contain( natalNameTable ) )
                zodiacPanel.addGLayout( natalNameTable );
        } else {
            zodiacPanel.removeGLayout( natalNameTable );
        }
        if ( v[1] == 1 ) {
            if ( ! zodiacPanel.contain( transitNameTable) )
                zodiacPanel.addGLayout( transitNameTable );
        } else {
            zodiacPanel.removeGLayout( transitNameTable );
        }
        viewmode( viewmodeCheckBoxMenuItem.isSelected() );
        selectedAspectBody = null;
        showRings = flag;
        // 一重円の場合はダイレクト結線をdisenabledにする
        int ring = acp.getShowRings();
        scp.getDirectLineModeMenuItem().setEnabled(
                (ring >= 1 && ring <= 3) ? false : true );
    }

    private void viewmode( boolean isNormal ) {
        GLayout [] rm = new GLayout[] { bodyTable, cuspTable, methodTable, nodeApogeeTable };
        GLayout [] layouts = null;
        natalNameTable.setLocation( 1, 0.5, GLayout.G_LEFT, GLayout.G_TOP );
        transitNameTable.setLocation( 99, 0.5, GLayout.G_RIGHT, GLayout.G_TOP );
        if ( isNormal ) {
            layouts = new GLayout[] { bodyTable, cuspTable, methodTable, nodeApogeeTable };
            nodeApogeeTable.setLocation( 99, 70,
                                         GLayout.G_RIGHT, GLayout.G_BOTTOM );
            methodTable.setLocation( 1, 70, GLayout.G_LEFT, GLayout.G_BOTTOM );
        } else {
            // 実は画面外にはみ出して見えない。しかし修正は困難なので、今は放置。
            layouts = new GLayout[] { methodTable, nodeApogeeTable };
            nodeApogeeTable.setLocation( 99, 99,
                                         GLayout.G_RIGHT, GLayout.G_BOTTOM );
            methodTable.setLocation( 1, 99, GLayout.G_LEFT, GLayout.G_BOTTOM );
        }
        for ( GLayout gl : rm ) {
            zodiacPanel.removeGLayout( gl );
        }
        for ( GLayout gl : layouts ) {
            if ( ! zodiacPanel.contain( gl ) ) {
                zodiacPanel.addGLayout( gl );
            }
        }
        zodiacPanel.setViewMode( isNormal );
        zodiacPanel.repaint();

    }

    @Override
    public float getHeightPer() {
        return viewmodeCheckBoxMenuItem.isSelected() ? 0.707f : 1.0f;
    }
    /**
     * 各円の天体イベントリスナ。イベントの発生した天体(Body)を、変数actionBodyに
     * セットしたのち、ポップアップメニューを開く。ユーザがメニューを選択すると、
     * 各メニューのリスナが、actionBodyを参照して、天体のアスペクトやサビアンを
     * 表示する。
     */
    class PlanetHandler extends PlanetActionAdapter {
        DictionaryRequest dictReq = NPTChartDictionaryTool.createRequest();
        @Override
        public void bodyClicked( PlanetEvent evt ) {
            Component evtComp = (Component)evt.getMouseEvent().getSource();
            Point mousePoint = evt.getMouseEvent().getPoint();
            int button = evt.getMouseEvent().getButton();
            if ( button == MouseEvent.BUTTON3 ) {
                wbh.setSelectedObject(null); //右クリがおきたらバルーンはキャンセル
                actionBody = evt.getBody();
                bodyPopupMenu.show( evtComp, mousePoint.x, mousePoint.y );
            } else if ( button == MouseEvent.BUTTON1 ) {
                Body p = evt.getBody();
                if ( wbh.getSelectedObject() != p ) {
                    wbh.setSelectedObject(p);
                    Point point = new Point( mousePoint );
                    SwingUtilities.convertPointToScreen( point, evtComp );
                    SabianDialogHandler dialog = getSabianDialogHandler();
                    if ( ! dialog.isVisible() ) {
                        String msg = Caption.
                            getSabianCaption( p, dialog.getLang(), groups );
                        wordBalloon.show( msg , point );
                    }
                    int npt = p.group;
                    dialog.setBodyList(nptChart.getBodyList(scp.getBodyIDs(npt),npt));
                    dialog.setSelect((int)p.lon);
                    //辞書にリクエストを出す
                    Dictionary dict = getDictionary();
                    NPTChartDictionaryTool.getRequest(p, dictReq);
                    dictReq.setCaption(Caption.getBodyCaption(p, groups));
                    dict.search( dictReq );
                }
            }
        }
        /**
         * 天体のダブクリで放射アスペクト表示
         */
        @Override
        public void bodyDoubleClicked( PlanetEvent evt ) {
            System.out.println("天体ダブクリ" + actionBody );
//            if ( actionBody == null ) return;
            isAspectVisible = true;
            selectedAspectBody = evt.getBody(); //actionBody;
            aspectCommand = selectedAspectBody.group;
            if ( isAspectVisible ) {
                aspectCircle.setShowAspectCategorys(scp.getAspectCategorys());
                aspectCircle.setSelectedBody(selectedAspectBody);
                int aspectTarget = acp.getAspectTargets();
                List<Aspect> list = NPTAspectFinder.getTargetAspectList(
                    aspectCommand, selectedAspectBody, nptChart, scp, aspectTarget );
                aspectCircle.setAspects( list );
            } else {
                aspectCircle.setAspects( new ArrayList<Aspect>() );
            }
            zodiacPanel.repaint();
        }
    }

    /**
     * 天体ポップアップメニューのアイテム。
     * 天体右をｸﾘｯｸ→ｱｽﾍﾟｸﾄ表示の場合の、
     * ｲﾍﾞﾝﾄﾘｽﾅで(transit|prgress|natal)BodyMenuItem
     * にaddListener()して使用される。actionBodyを参照する。
     */
    class BodyAspectHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if ( actionBody == null ) return;
            isAspectVisible = true;
            selectedAspectBody = actionBody;
            aspectCommand = actionBody.group;
            if ( isAspectVisible ) {
                aspectCircle.setShowAspectCategorys(scp.getAspectCategorys());
                aspectCircle.setSelectedBody(selectedAspectBody);
                int aspectTarget = acp.getAspectTargets();
                List<Aspect> list = NPTAspectFinder.getTargetAspectList(
                    aspectCommand, selectedAspectBody, nptChart, scp, aspectTarget );
                aspectCircle.setAspects( list );
            } else {
                aspectCircle.setAspects( new ArrayList<Aspect>() );
            }
            zodiacPanel.repaint();
        }
    }

    /**
     * 天体ポップアップメニューのアイテムでサビアンダイアログを表示する。
     * actionBodyを参照する。
     */
    class SabianHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            SabianDialogHandler dialog = getSabianDialogHandler();
            int npt = actionBody.group;
            dialog.setBodyList( nptChart.getBodyList( scp.getBodyIDs(npt), npt ) );
            dialog.setSelect( (int)actionBody.lon );
            dialog.setVisible( true );
        }
    }

    /**
     * 天体ドラッグハンドラ
     */
    class BodyMotionHandler implements PlanetMotionListener {
        @Override
        public void bodyDragged( PlanetEvent evt ) {
            wbh.setSelectedObject( null );
            int npt = evt.getBody().group;
            moverDialog.setBodys(
                nptChart.getDragBodys( scp.getBodyIDs( npt ), npt ) );
            moverDialog.setValues( evt.getBody().id,
                                   evt.getDraggedAngle(),
                                   BasicBodyMoverDialog.FORWARDS);
            //System.out.println("Dragged Angle = " + evt.getDraggedAngle() );
            moverDialog.setLocationRelativeTo(parentFrame);
            moverDialog.setVisible( true );
            if ( moverDialog.isAccepted() ) {
                //System.out.println( "Move angle to " + moverDialog.getAngle() );
                TimePlace tp = nptChart.search( moverDialog.getBodyID(),
                                                moverDialog.getAngle(),
                                                moverDialog.isBackwards() ,npt );
                if ( npt == 0 ) {
                    Data  natalData = chartData.getSelectedData();
                    natalData.setTimePlace( tp );
                    chartData.updateData( natalData );
                } else if ( npt == 1 ) {
                    transit.setTimePlace( tp );
                    channelData.updateTransit( transit );
                } else if ( npt == 2 ) {
                    transit.setTimePlace( tp );
                    channelData.updateTransit( transit );
                }
            }
            calc();
        }
    }

    /**
     * 天体リストをリングのダブクリで表示
     */
    class RingHandler implements RingActionListener {
        @Override
        public void ringDoubleClicked(RingEvent evt) {
            PlanetRing r = (PlanetRing)evt.getSource();
            for ( int i=0; i<planetRings.length; i++ ) {
                if ( r == planetRings[i] ) {
                    acp.setBodyListMode(i);
                    break;
                }
            }
            setBodyListToTable();
            setNodeApogeeToTable();
            report();
            zodiacPanel.repaint();
        }
    }

    void setBodyListToTable() {
        int mode = acp.getBodyListMode();
        int [] IDs = scp.getBodyIDs(mode);
        String [] npt =  { "ネイタル","プログレス","トランジット" };
        bodyTable.updateBodyList( scp.getBodyIDs( mode ), npt[ mode ] );
    }

    void setNodeApogeeToTable() {
        nodeApogeeTable.updateNodeApogee(
            scp.getBodyIDs( acp.getBodyListMode() ),
            nptChart.getNodeTypeName(),
            nptChart.getApogeeTypeName() );
    }

    /**
     * アスペクト円でのイベントハンドラ
     */
    class AspectCircleHandler extends AspectCircleAdapter {
        DictionaryRequest dictReq = NPTChartDictionaryTool.createRequest();
        //アスペクト円がクリックされた場合
        @Override
        public void aspectCircleClicked(AspectCircleEvent evt) {
            if ( evt.getMouseEvent().getButton() == MouseEvent.BUTTON3 ) { //右クリ
                Component c = (Component)evt.getMouseEvent().getSource();
                Point p = evt.getMouseEvent().getPoint();
                aspectCirclePopupMenu.show(c,p.x,p.y);
            }
        }
        //アスペクト線がクリックされた場合
        @Override
        public void aspectClicked(AspectCircleEvent evt) {
            MouseEvent mev = evt.getMouseEvent();
            Aspect a = evt.getAspect();
            //Shift + 左クリ でアスペクト線の選択
            if ( mev.getButton() == MouseEvent.BUTTON1 && mev.isShiftDown() ) {
                aspectCircle.registMarkingAspect(evt.getAspect());
                zodiacPanel.repaint();
            } else if ( a != wbh.getSelectedObject() ) {
                wbh.setSelectedObject(a);
                Caption.getAspectCaption( a, groups );
                Point p = new Point( mev.getPoint() );
                SwingUtilities.
                    convertPointToScreen( p, (Component)mev.getSource() );
                wordBalloon.show( Caption.getAspectCaption( a,groups ), p );
                Dictionary dict = getDictionary();
                NPTChartDictionaryTool.getRequest(a, dictReq);
                dictReq.setCaption(Caption.getAspectCaption(a, groups));
                dict.search(dictReq);
            }
        }
    }

    /**
     * 仕様設定が変更されたときに呼び出される。
     * updateColorSetting()の次に呼び出される。
     */
    @Override
    public void updateSpecificSetting() {
        //System.out.println("setSpecificConfig()完了");
        cc.setHouseSystemCode( scp.getHouseSystemCode() ); //ハウス分割法を指定
        cc.setCuspUnkownHouseSystem( scp.getCuspUnknownHouseSystem() );
        cc.setPrioritizeSolar( scp.getPrioritizeSolar() );

        nptChart.setProgressMode(scp.getProgressCode());
        progressPlanetRing.setNotDragBodys(nptChart.getNotDragBodys(NPTChart.PROGRESS));
        //ハウス分割法と進行法の表示レイアウトを再設定
        methodTable.updateMethodTable(acp.getShowRings());
        setBodyListToTable();      //createBodyListLayout();
        setNodeApogeeToTable();
        calc();
    }

    /**
     * 配色設定に変更が起きたとき呼び出され、配色設定パネルの情報をホロスコープに
     * 反映させる。init()の次に呼び出される。
     */
    @Override
    public void updateColorSetting() {
        zodiacPanel.setBackground( ccp.getBGColor() );
        zodiacRing.setBackgroundColors( ccp.getZodiacBGColors() );
        zodiacRing.setSignRingLineColor( ccp.getZodiacRingBorderColor() );
        zodiacRing.setSignSymbolColors(ccp.getZodiacSymbolColors() );
        zodiacRing.setSymbolBorderColors(ccp.getZodiacSymbolBorderColors() );
        zodiacRing.setNoSymbolBorders(ccp.isNoSignSymbolBorders() );
        zodiacRing.setNoSignBackgrounds( ccp.isNoZodiacBackground() );
        zodiacRing.setNoSignRingBorder( ccp.isNoZodiacRingBorder() );
        zodiacRing.setNoZodiacGauge( ccp.isNoZodiacGauge());
        zodiacRing.setZodiacGaugeColor( ccp.getZodiacGaugeColor() );
        for ( int npt = 0; npt < 3; npt++ ) {
            planetRings[ npt ].setHouseBGColors( ccp.getHouseBGColors( npt ) );
            planetRings[ npt ].setHouseNumberColors( ccp.getHouseNumberColors( npt ));
            planetRings[ npt ].setHousesGaugeColor( ccp.getHousesGaugeColor( npt ));
            planetRings[ npt ].setHouseCuspsColor( ccp.getCuspsColor(npt));
            planetRings[ npt ].setBodysColor( ccp.getBodysColor(npt));
            planetRings[ npt ].setBodysHighLightColor( ccp.getBodysHighLightColor(npt));
            planetRings[ npt ].setBodysDegreeColor( ccp.getBodysDegreeColor(npt));
            planetRings[ npt ].setOuterHousesNumberColor( ccp.getOuterHousesNumberColor(npt));
            planetRings[ npt ].setOuterCuspsDegreeColor( ccp.getOuterCuspsDegreeColor(npt));
            planetRings[ npt ].setOuterCuspsColor( ccp.getOuterCuspsColor(npt));
            planetRings[ npt ].setHousesHighLightColor( ccp.getHousesHighLightColor(npt));
            planetRings[ npt ].setLeadingLineColor( ccp.getLeadingLineColor(npt));
            planetRings[ npt ].setNoHousesGauge( ccp.isNoHousesGuage(npt));
            planetRings[ npt ].setNoHousesBG( ccp.isNoHousesBG(npt));
            planetRings[ npt ].setHouseInnerLineColor( ccp.getHousesBorderColor(npt));
            planetRings[ npt ].setBodysBorderColor( ccp.getBodysBorderColor(npt) );
            planetRings[ npt ].setBodysEffect( ccp.getBodysEffect(npt) );
            planetRings[ npt ].setTextColor( ccp.getRingTextColor(npt) );
        }
        // 名前などのテキスト情報の配色設定
        natalNameTable.setColors( ccp.getNameColor(),
                                  ccp.getDateColor(),
                                  ccp.getPlaceColor());
        transitNameTable.setColors( ccp.getNameColor(),
                                    ccp.getDateColor(),
                                    ccp.getPlaceColor());
        methodTable.setColor(ccp.getListOtherColor());
        nodeApogeeTable.setColor( ccp.getListOtherColor() );
        cuspTable.setCuspListColor( ccp.getListOtherColor(),
                                    ccp.getListAngleColor(),
                                    ccp.getListSignColor(),
                                    ccp.getListHouseNumberColor() );
        cuspTable.setHighLightColor( ccp.getListHighLightColor() );
        bodyTable.setBodyListColor( ccp.getListOtherColor(),
                                    ccp.getListSignColor(),
                                    ccp.getListAngleColor(),
                                    ccp.getListBodyColor(),
                                    ccp.getListRevColor() );
        bodyTable.setHighLightColor( ccp.getListHighLightColor() );
        //
        aspectCircle.setAspectStyles(ccp.getAspectStyles());
        aspectCircle.setBGColor(ccp.getAspectCircleBGColor());
        aspectCircle.setNoBGColor(ccp.isNoAspectCircleBGColor());
        zodiacPanel.repaint();
    }

    /**
     * 表示メニューに入れるメニューを返す。
     */
    @Override
    public List<Component> getViewMenuList() {
        return viewMenuList;
    }

    /**
     * このモジュール用の仕様設定パネルを返す。
     */
    @Override
    public CustomizePanel getSpecificCustomizePanel() {
        calc();
        return scp;
    }

    /**
     * このモジュール用のカラー設定パネルを返す。
     */
    @Override
    public CustomizePanel getColorCustomizePanel() {
        return ccp;
    }

    /**
     * args[0] = "TRANSIT"を入れて呼び出すと、トランジット円のみになる。
     *
     */
    @Override
    public void setCommand( String [] args ) {
        if ( args == null || args.length == 0 ) return;
        if ( args[0].equalsIgnoreCase("TRANSIT") ) {
            acp.setShowRings("3"); //トランジット円のみ表示
            acp.setBodyListMode(2);
            setBodyListToTable();
            setNodeApogeeToTable();
            calc();
        }
    }
    /**
     * このチャートモジュール名を返す。
     */
    @Override
    public String toString() {
        return "基本ホロスコープ"; //"NPT三重円";
    }

    /**
     * このチャートモジュールのチャンネル数を返す。
     */
    @Override
    public int getChannelSize() {
        return 1;
    }

    /**
     * このチャートモジュールがトランジットを受け取るか返す。
     */
    @Override
    public boolean isNeedTransit() {
        return true;
    }

    /**
     * このチャートモジュールの各チャンネルの名前を返す。
     */
    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    private static final ChartModuleMode [] chartModuleModes = {
        new ChartModuleMode( "ＮＰＴ三重円", "7,NPT" ),
        new ChartModuleMode( "ネイタル円", "1,N" ),
        new ChartModuleMode( "プログレス円", "2,P" ),
        new ChartModuleMode( "トランジット円", "3,T" ),
        new ChartModuleMode( "ＮＴ二重円", "4,NT" ),
        new ChartModuleMode( "ＮＰ二重円", "5,NP" ),
        new ChartModuleMode( "ＰＴ二重円", "6,PT" ),
    };

    @Override
    public ChartModuleMode [] getModuleModes() {
        return chartModuleModes;
    }

    /**
     * 初期化時に一度呼ばれるだけ
     */
    @Override
    public void setModuleMode( ChartModuleMode mode ) {
        if ( mode == null ) return;
        String cmd = mode.getCommand().split(",")[0];
        System.out.println("setModuleModeを実行 : " + cmd );
        acp.setShowRings( cmd );
        methodTable.updateMethodTable(Integer.parseInt(cmd));
    }

    @Override
    public ChartModuleMode getModuleMode() {
        return chartModuleModes[ acp.getShowRings() ];
    }
    /**
     * チャートの画像を返す。
     */
    @Override
    public BufferedImage getBufferedImage( Dimension size ) {
        return zodiacPanel.getBufferedImage( size );
    }
    /**
     * このプラグインは画像を返す機能を実装していて、このメソッドはtrueを返す。
     * 実装していない場合はfalseを返す。
     */
    @Override
    public boolean isImageServiceActivated() {
        return true;
    }

    @Override
    public Printable getPainter() {
        return zodiacPanel;
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    @Override
    public DictionaryActionFile getDictionaryAction() {
        return NPTChartDictionaryTool.DICTIONARY_ACTION_FILE;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        viewmodeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        resetRingWidthMenuItem = new javax.swing.JMenuItem();
        resetAcMenuItem = new javax.swing.JMenuItem();
        menuBar = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        bodyPopupMenu2 = new javax.swing.JPopupMenu();
        bodyMenuItem1 = new javax.swing.JMenuItem();
        bodyMenuItem2 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        bodyMenuItem3 = new javax.swing.JMenuItem();
        aspectMenuItem8 = new javax.swing.JMenuItem();
        bodyMenuItem4 = new javax.swing.JMenuItem();
        bodyMenuItem5 = new javax.swing.JMenuItem();

        viewmodeCheckBoxMenuItem.setSelected(true);
        viewmodeCheckBoxMenuItem.setText("\u5929\u4f53\u30ea\u30b9\u30c8\u8868\u793a");
        viewmodeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewmodeCheckBoxMenuItemActionPerformed(evt);
            }
        });

        resetRingWidthMenuItem.setText("\u30ea\u30f3\u30b0\u5e45\u3092\u521d\u671f\u5024\u306b\u623b\u3059");
        resetRingWidthMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetRingWidthMenuItemActionPerformed(evt);
            }
        });

        resetAcMenuItem.setText("\u30a2\u30bb\u30f3\u30c0\u30f3\u30c8\u3092\u5b9a\u4f4d\u7f6e\u306b");
        resetAcMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAcMenuItemActionPerformed(evt);
            }
        });

        viewMenu.setText("\u8868\u793a(V)");
        menuBar.add(viewMenu);

        bodyMenuItem1.setText("\u3053\u306e\u5929\u4f53\u3068\u306e\u30a2\u30b9\u30da\u30af\u30c8\u3092\u8868\u793a");
        bodyPopupMenu2.add(bodyMenuItem1);

        bodyMenuItem2.setText("\u30b5\u30d3\u30a2\u30f3\u8f9e\u66f8\u3092\u8868\u793a");
        bodyPopupMenu2.add(bodyMenuItem2);

        bodyPopupMenu2.add(jSeparator3);

        bodyMenuItem3.setText("\u5929\u4f53\u306e\u8868\u793a/\u975e\u8868\u793a\u306e\u8a2d\u5b9a");
        bodyMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bodyMenuItem3ActionPerformed(evt);
            }
        });

        bodyPopupMenu2.add(bodyMenuItem3);

        aspectMenuItem8.setText("\u30a2\u30b9\u30da\u30af\u30c8\u3092\u691c\u51fa\u3059\u308b\u5929\u4f53\u3092\u8a2d\u5b9a");
        aspectMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aspectMenuItem8ActionPerformed(evt);
            }
        });

        bodyPopupMenu2.add(aspectMenuItem8);

        bodyMenuItem4.setText("\u9032\u884c\u6cd5\u306e\u8a2d\u5b9a");
        bodyMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bodyMenuItem4ActionPerformed(evt);
            }
        });

        bodyPopupMenu2.add(bodyMenuItem4);

        bodyMenuItem5.setText("\u30cf\u30a6\u30b9\u306e\u8a2d\u5b9a");
        bodyMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bodyMenuItem5ActionPerformed(evt);
            }
        });

        bodyPopupMenu2.add(bodyMenuItem5);

        setLayout(new java.awt.GridLayout(1, 0));

    }// </editor-fold>//GEN-END:initComponents

    private void viewmodeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewmodeCheckBoxMenuItemActionPerformed
        boolean state = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
        viewmode( state );
    }//GEN-LAST:event_viewmodeCheckBoxMenuItemActionPerformed

    private void bodyMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bodyMenuItem5ActionPerformed
        scp.setSelectedTab( NPTSpecificSettingPanel.TAB_HOUSE, 0 );
        this.showSpecificCustomizeDialog();
    }//GEN-LAST:event_bodyMenuItem5ActionPerformed

    private void bodyMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bodyMenuItem4ActionPerformed
        scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PROGRESSION, 0 );
        this.showSpecificCustomizeDialog();
    }//GEN-LAST:event_bodyMenuItem4ActionPerformed

    private void bodyMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bodyMenuItem3ActionPerformed
        scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PLANET, 0 );
        this.showSpecificCustomizeDialog();
    }//GEN-LAST:event_bodyMenuItem3ActionPerformed

    private void aspectMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aspectMenuItem8ActionPerformed
        scp.setSelectedTab( NPTSpecificSettingPanel.TAB_PLANET, 1 );
        this.showSpecificCustomizeDialog();
    }//GEN-LAST:event_aspectMenuItem8ActionPerformed

  private void resetRingWidthMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRingWidthMenuItemActionPerformed
      for ( PlanetRing r : planetRings ) {
          r.setRingWidth( DEFAULT_PLANET_RING_WIDTH );
      }
      zodiacPanel.reposition();
      zodiacPanel.repaint();
  }//GEN-LAST:event_resetRingWidthMenuItemActionPerformed
      //ホロスコープ回転角をリセット
  private void resetAcMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAcMenuItemActionPerformed
      //npt = 0;
      setAC();
      zodiacPanel.setRoll(0d);
      zodiacPanel.repaint();
  }//GEN-LAST:event_resetAcMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aspectMenuItem8;
    private javax.swing.JMenuItem bodyMenuItem1;
    private javax.swing.JMenuItem bodyMenuItem2;
    private javax.swing.JMenuItem bodyMenuItem3;
    private javax.swing.JMenuItem bodyMenuItem4;
    private javax.swing.JMenuItem bodyMenuItem5;
    private javax.swing.JPopupMenu bodyPopupMenu2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem resetAcMenuItem;
    private javax.swing.JMenuItem resetRingWidthMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem viewmodeCheckBoxMenuItem;
    // End of variables declaration//GEN-END:variables

}
