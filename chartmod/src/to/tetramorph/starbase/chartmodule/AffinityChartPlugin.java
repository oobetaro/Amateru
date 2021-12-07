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
import to.tetramorph.starbase.util.AffinityChart;
import to.tetramorph.starbase.chartparts.AspectCircle;
import to.tetramorph.starbase.chartparts.AspectCircleAdapter;
import to.tetramorph.starbase.chartparts.AspectCircleEvent;
import to.tetramorph.starbase.chartparts.AspectCombinationPanel2;
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
 * NPT相性チャートプラグイン
 * @author 大澤義鷹
 */
public class AffinityChartPlugin extends ChartModulePanel {
    // 惑星リング幅の初期値
    private static final double DEFAULT_PLANET_RING_WIDTH = 0.055;
    ChartData chartData; //calc()の入力パラメター
    ChartData chartData2;
    AffinityChart nptChart;
    private static final String [] channelNames = {
        "ネイタル１","ネイタル２","トランジット" };
    double asc;
    ZodiacRing zodiacRing;
    PlanetRing natalPlanetRing, natalPlanetRing2;
    PlanetRing progressPlanetRing, progressPlanetRing2;
    PlanetRing transitPlanetRing;
    PlanetRing [] planetRings; //ﾙｰﾌﾟからの参照用で、各NPTのﾘﾝｸﾞが格納されている。
    ZodiacPanel zodiacPanel;
    Transit transit;
    AspectCircle aspectCircle;
    int aspectCommand = 0;
    //アスペクト組合せ選択パネル
    AspectCombinationPanel2 acp = new AspectCombinationPanel2();
    //仕様設定パネル
    NPTSpecificSettingPanel scp = new NPTSpecificSettingPanel( acp );
    //色設定パネル
    NPTColorSettingPanel ccp = new NPTColorSettingPanel();

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
    static final String [] groups = new String[] { "N","P","T","N2","P2" };
    // calc()計算直後の各天体のリスト
    List<List<Body>> nptBodyList = new ArrayList<List<Body>>();
    List<List<Body>> nptCuspList = new ArrayList<List<Body>>();

    List<Component> viewMenuList = new ArrayList<Component>();
    CuspTable cuspTable;
    BodyTable bodyTable;
    NatalNameTable natalNameTable;
    NatalNameTable natalNameTable2;
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
        aspectCirclePopupMenu.item1.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                selectedAspectBody = null;
                isAspectVisible = true;
                aspectCircle.clearHideAspects();
                calc();
            }
        });
        aspectCirclePopupMenu.item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                isAspectVisible =false;
                calc();
            }
        });
        aspectCirclePopupMenu.item3.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                aspectCircle.hideMarkingAspects();
                zodiacPanel.repaint();
            }
        });

        aspectCirclePopupMenu.item4.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
                aspectCircle.hideUnmarkingAspects();
                zodiacPanel.repaint();
            }
        });
        aspectCirclePopupMenu.item5.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent evt ) {
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
                                    0 );
                showSpecificCustomizeDialog();
            }
        });


//        //天体右ｸﾘｯｸ→ｱｽﾍﾟｸﾄ表示の場合のｲﾍﾞﾝﾄﾘｽﾅを登録(NPT共用)
//        bodyMenuItem1.addActionListener( new BodyAspectHandler() );
//        bodyMenuItem2.addActionListener( new SabianHandler() );
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

        moverDialog = new BasicBodyMoverDialog( parentFrame );
        super.getChartConfig( cc );
        nptChart = new AffinityChart( cc );
        scp.setHouseSystemCode( cc.getHouseSystemCode() );
        zodiacPanel = new ZodiacPanel();
        zodiacPanel.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseReleased( MouseEvent evt ) {
                iframe.contentSelected();
            }
        });
        zodiacRing = new ZodiacRing( 0.8, 0.04 );
        zodiacPanel.setZodiacRing(zodiacRing);

        natalPlanetRing = new PlanetRing();
        progressPlanetRing = new PlanetRing();
        natalPlanetRing2 = new PlanetRing();
        progressPlanetRing2 = new PlanetRing();
        transitPlanetRing = new PlanetRing();

        planetRings = new PlanetRing[] {
            natalPlanetRing, progressPlanetRing,
            natalPlanetRing2, progressPlanetRing2,
            transitPlanetRing
        };

        for ( int i=0; i < planetRings.length; i++ ) {
            nptBodyList.add( new ArrayList<Body>() );
            nptCuspList.add( new ArrayList<Body>() );
        }

        natalPlanetRing.setZodiacRing( zodiacRing );
        PlanetHandler planetHandler = new PlanetHandler();

        BodyMotionHandler bmh = new BodyMotionHandler();
        RingHandler rh = new RingHandler();

        for ( int i = 0; i < planetRings.length; i++ ) {
            planetRings[i].setRingWidth( DEFAULT_PLANET_RING_WIDTH );
            planetRings[i].setPlanetActionListener( planetHandler );
            planetRings[i].setPlanetMotionListener( bmh );
            planetRings[i].setRingActionListener( rh );
            planetRings[i].setNotDragBodys( nptChart.getNotDragBodys(i) );
        }
        int [] groupCodes = new int[] { 0,1,3,4,2 };
        for ( int i=0; i< planetRings.length; i++) {
            planetRings[i].setGroup( groupCodes[i] );
        }
        aspectCircle = new AspectCircle();
        aspectCircle.setAspectCircleListener( new AspectCircleHandler() );
        aspectCircle.setPlanetRings( planetRings );
        zodiacPanel.setAspectCircle( aspectCircle );
        add( zodiacPanel );

        // 仕様設定のデフォルト値をセット
        Preference specificPref = new Preference();
        // NPTCalcConfiPanel()の初期化時の設定値がデフォルト
        scp.getPreference( specificPref );
        specificPref.setProperty( "CuspUnknownHouseSystem", config.getProperty("CuspUnknownHouseSystem"));
        specificPref.setProperty( "PrioritizeSolar", config.getProperty("PrioritizeSolar"));
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
        natalNameTable   = new NatalNameTable();
        natalNameTable2  = new NatalNameTable(); // 0, 9 );
        transitNameTable = new TransitNameTable();
        zodiacPanel.addGLayout( natalNameTable );
        zodiacPanel.addGLayout( natalNameTable2 );
        zodiacPanel.addGLayout( transitNameTable );

        cuspTable        = new CuspTable( planetRings );
        bodyTable        = new BodyTable( planetRings );
        methodTable      = new MethodTable();
        nodeApogeeTable  = new NodeApogeeTable();
    }

    /**
     * 出生データを受け取る。出生データや出来事データなど日時場所の基礎データを
     * 受け取り、それに基づいて星の位置を計算しホロスコープを描画する。
     */
    @Override
    public void setData( ChannelData channelData ) {
        this.channelData = channelData;
        chartData = channelData.get(0);
        chartData2 = channelData.get(1);
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
        cc.setHouseSystemCode( scp.getHouseSystemCode() ); //ハウス分割法を指定
        nptChart.setProgressMode( scp.getProgressCode() );

        Data  natalData =
            chartData.getDataList().get( chartData.getSelectedIndex() );
        natalNameTable.setData( chartData.getSelectedData() );
        transitNameTable.setTransit( transit );
        nptChart.setData( natalData );
        natalPlanetRing.setName( natalData.getNatal().getName() );
        natalPlanetRing.setTimePlace( natalData.getTimePlace() );
        transitPlanetRing.setTimePlace(transit);

        natalPlanetRing.setRingName(
            "Natal1 " + natalData.getNatal().getName() );
        progressPlanetRing.setRingName(
            "Progress1 " + natalData.getNatal().getName() );
        transitPlanetRing.setRingName("Transit");

        Data  natalData2 = null;
        if ( chartData2.getDataList().size() > 0 ) {
            natalData2 =
                chartData2.getDataList().get( chartData2.getSelectedIndex() );
            nptChart.setData2( natalData2 );
            natalPlanetRing2.setRingName(
                "Natal2 "    + natalData2.getNatal().getName() );
            progressPlanetRing2.setRingName(
                "Progress2 " + natalData2.getNatal().getName() );
            natalNameTable2.setData( chartData2.getSelectedData() );
        } else {
            natalPlanetRing2.setRingName( "Natal2 データ未登録" );
            progressPlanetRing2.setRingName( "Progress2 データ未登録" );
        }
        nptChart.setTransit( transit );
        int [] array = new int[] { 0,1,0,1,2 };
        int [] array2 = new int[] { 0,1,3,4,2 };
        //NTP各天体位置を求める
        for ( int i=0; i<5; i++ ) {
            int j = array[i];
            int k = array2[i];
            List<Body> bodyList = nptChart.getBodyList( scp.getBodyIDs(j), k );
            nptBodyList.set( i, bodyList );
            planetRings[i].setBodyList( bodyList );
            List<Body> cuspList = nptChart.getCuspList( k );
            nptCuspList.set( i, cuspList );
            planetRings[i].setCusps( cuspList );
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
                list = AffinityAspectFinder
                    .getAspectList( aspectCommand, nptChart, scp );
            } else {                                   //選択天体とのアスペクト
                int aspectTarget = acp.getAspectTargets();
                int id = selectedAspectBody.id;
                int group = selectedAspectBody.group;
                selectedAspectBody = nptChart.getBody( id, group );
                list = AffinityAspectFinder.getTargetAspectList(
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

    //static final int [] ac_array = new int [] { 0,0,1,2,0,0,1 };

    private void setAC() {
        int npt = 0; //ac_array[ acp.getShowRings() ];
        Body p = nptChart.getBody(AC,npt);
        if ( p == null ) {
//            Body sun = nptChart.getBody(SUN,npt);
//            asc = sun.getSign() * 30f;
            asc = nptChart.getBody( CUSP1, npt ).lon;
        } else asc = p.lon;
        zodiacPanel.setAscendant(asc);
    }


    //天体位置を天体ラベルリストに書きだしと、zodiacRingの登録
    private void report() {
        int [] array = new int [] { 0,1,4,2,3 };
        int mode = array[ acp.getBodyListMode() ];
        bodyTable.setBodyList( nptBodyList.get(mode) );
        cuspTable.setCuspList( nptCuspList.get(mode) );
    }


    /**
     * NPTの各リングを設定に応じて表示したり隠したりする。
     * フィールド変数showRingsが現在の表示状況を表し、設定パネルのshowRings()と
     * 値が異なる場合だけリングの抜き刺し処理をする。
     * メソッド内部では一旦すべてのリングを抜き取り、再挿入する処理をする。
     */
    private void showRings() {
        int flag = new int []  { 31, 5, 21 }[ acp.getShowRings() ];
        if ( flag == showRings ) return; //設定変更されてないときは処理しない。
        for ( PlanetRing r : planetRings ) zodiacPanel.removePlanetRing(r);
        for ( int i=0,j=1; i<5; i++ ) {
            if ( (flag & j) != 0 ) {
                zodiacPanel.addPlanetRing( planetRings[i] );
            }
            j = j << 1;
        }
        if ( acp.getShowRings() == 1 ) {
            //transitNameTable.remove(); // N1 * N2のときTは非表示
            zodiacPanel.removeGLayout( transitNameTable );
        } else {
            if ( ! zodiacPanel.contain( transitNameTable ) )
                zodiacPanel.addGLayout( transitNameTable );
        }
        viewmode( viewmodeCheckBoxMenuItem.isSelected() );
        showRings = flag;
        selectedAspectBody = null;
    }

    private void viewmode( boolean isNormal ) {
        GLayout [] rm = new GLayout[] { bodyTable, cuspTable, methodTable, nodeApogeeTable };
        GLayout [] layouts = null;
        natalNameTable.setLocation( 1, 0.5, GLayout.G_LEFT, GLayout.G_TOP );
        natalNameTable2.setLocation( 1, 9.5, GLayout.G_LEFT, GLayout.G_TOP );
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
        public void bodyClicked(PlanetEvent evt) {
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
                    int npt = p.group % 3;
                    dialog.setBodyList(nptChart.getBodyList(scp.getBodyIDs(npt),npt));
                    dialog.setSelect((int)p.lon);
                    //辞書にリクエストを出す
                    Body body = new Body(p);
                    body.group = npt;
                    Dictionary dict = getDictionary();
                    NPTChartDictionaryTool.getRequest(body, dictReq);
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
            isAspectVisible = true;
            selectedAspectBody = evt.getBody(); //actionBody;
            aspectCommand = selectedAspectBody.group; //actionBody.group;
            if ( isAspectVisible ) {
                aspectCircle.setShowAspectCategorys( scp.getAspectCategorys() );
                aspectCircle.setSelectedBody( selectedAspectBody );
                int aspectTarget = acp.getAspectTargets();
                List<Aspect> list = AffinityAspectFinder.getTargetAspectList(
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
                List<Aspect> list = AffinityAspectFinder.getTargetAspectList(
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
        public void bodyDragged(PlanetEvent evt) {
            wbh.setSelectedObject(null);
            int npt = evt.getBody().group;
            moverDialog.setBodys(
                nptChart.getDragBodys( scp.getBodyIDs( npt % 3 ), npt ) );
            moverDialog.setValues(
                evt.getBody().id,
                evt.getDraggedAngle(),
                BasicBodyMoverDialog.FORWARDS );
            moverDialog.setLocationRelativeTo( parentFrame );
            moverDialog.setVisible( true );
            if ( moverDialog.isAccepted() ) {
                TimePlace tp = nptChart.search(
                    moverDialog.getBodyID(),
                    moverDialog.getAngle(),
                    moverDialog.isBackwards(),
                    npt );
                if ( npt == 0 ) {
                    Data  natalData = chartData.getSelectedData();
                    natalData.setTimePlace(tp);
                    chartData.updateData(natalData);
                } else if ( npt == 1 ) {
                    transit.setTimePlace(tp);
                    channelData.updateTransit(transit);
                } else if ( npt == 2 ) {
                    transit.setTimePlace(tp);
                    channelData.updateTransit(transit);
                } else if ( npt == 3 ) {
                    Data  natalData = chartData2.getSelectedData();
                    natalData.setTimePlace(tp);
                    chartData2.updateData(natalData);
                } else if ( npt == 4 ) {
                    transit.setTimePlace(tp);
                    channelData.updateTransit(transit);
                }
            }
            calc();
        }
    }

    /**
     * 天体リストをリングのダブクリで表示
     */
    class RingHandler implements RingActionListener {
        int [] array = new int[] { 0,1,3,4,2 };
        @Override
        public void ringDoubleClicked(RingEvent evt) {
            System.out.println( evt );
            PlanetRing r = (PlanetRing)evt.getSource();
            for ( int i=0; i<planetRings.length; i++ ) {
                if ( r == planetRings[i] ) {
                    acp.setBodyListMode( array[i] );
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
        int n = mode % 3;
        int [] IDs = scp.getBodyIDs( n );
        String [] npt =  {
            "ネイタル1","プログレス1","トランジット","ネイタル2","プログレス2" };
        bodyTable.updateBodyList( scp.getBodyIDs( n ), npt[ mode ] );
    }

    void setNodeApogeeToTable() {
        int n = acp.getBodyListMode() % 3;
        nodeApogeeTable.updateNodeApogee( scp.getBodyIDs( n ),
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
                NPTChartDictionaryTool.getRequest( tempAspect(a), dictReq );
                dictReq.setCaption( Caption.getAspectCaption(a, groups) );
                getDictionary().search( dictReq );
            }
        }
        //相性円のアスペクトは、groupコードがN2を指す場合があり、それをNに変換
        //したものを作成し、getRequestに与える。
        Aspect tempAspect(Aspect a) {
            Body p1 = new Body(a.p1);
            Body p2 = new Body(a.p2);
            p1.group %= 3;
            p2.group %= 3;
            return new Aspect(p1,p2,a.aid,a.tight,a.error);
        }
    }

    /**
     * 仕様設定が変更されたときに呼び出される。
     * updateColorSetting()の次に呼び出される。
     */
    @Override
    public void updateSpecificSetting() {
        System.out.println("setSpecificConfig()完了");
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
        //for ( int npt = 0; npt < 3; npt++ ) {
        int [] array = new int [] { 0,1,0,1,2 };
        for ( int i = 0; i < planetRings.length; i++ ) {
            int npt = array[i];
            planetRings[ i ].setHouseBGColors( ccp.getHouseBGColors( npt ) );
            planetRings[ i ].setHouseNumberColors( ccp.getHouseNumberColors( npt ));
            planetRings[ i ].setHousesGaugeColor( ccp.getHousesGaugeColor( npt ));
            planetRings[ i ].setHouseCuspsColor( ccp.getCuspsColor(npt));
            planetRings[ i ].setBodysColor( ccp.getBodysColor(npt));
            planetRings[ i ].setBodysHighLightColor( ccp.getBodysHighLightColor(npt));
            planetRings[ i ].setBodysDegreeColor( ccp.getBodysDegreeColor(npt));
            planetRings[ i ].setOuterHousesNumberColor( ccp.getOuterHousesNumberColor(npt));
            planetRings[ i ].setOuterCuspsDegreeColor( ccp.getOuterCuspsDegreeColor(npt));
            planetRings[ i ].setOuterCuspsColor( ccp.getOuterCuspsColor(npt));
            planetRings[ i ].setHousesHighLightColor( ccp.getHousesHighLightColor(npt));
            planetRings[ i ].setLeadingLineColor( ccp.getLeadingLineColor(npt));
            planetRings[ i ].setNoHousesGauge( ccp.isNoHousesGuage(npt));
            planetRings[ i ].setNoHousesBG( ccp.isNoHousesBG(npt));
            planetRings[ i ].setHouseInnerLineColor( ccp.getHousesBorderColor(npt));
            planetRings[ i ].setBodysBorderColor( ccp.getBodysBorderColor(npt) );
            planetRings[ i ].setBodysEffect( ccp.getBodysEffect(npt) );
            planetRings[ i ].setTextColor( ccp.getRingTextColor(npt) );
        }
        // 名前などのテキスト情報の配色設定
        natalNameTable.setColors( ccp.getNameColor(),
                                     ccp.getDateColor(),
                                     ccp.getPlaceColor());
        natalNameTable2.setColors( ccp.getNameColor(),
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
     * このチャートモジュール名を返す。
     */
    @Override
    public String toString() {
        return "相性ホロスコープ"; //"NPT相性円";
    }

    /**
     * このチャートモジュールのチャンネル数を返す。
     */
    @Override
    public int getChannelSize() {
        return 2;
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
        new ChartModuleMode( "相性円", "1,NN'" ),
        new ChartModuleMode( "Ｔ相性円", "2,NN'PT" ),
        new ChartModuleMode( "ＮＰＴ相性円", "0,NN'T" ),
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
//        setBodyListToTable();
//        setNodeApogeeToTable();
//        calc();
    }

    @Override
    public ChartModuleMode getModuleMode() {
        int i = new int[] { 2, 0, 1 }[ acp.getShowRings() ];
        return chartModuleModes[ i ];
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
        resetRingWidthMenuItem = new javax.swing.JMenuItem();
        resetAcMenuItem = new javax.swing.JMenuItem();
        menuBar = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        viewmodeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();

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

        viewmodeCheckBoxMenuItem.setSelected(true);
        viewmodeCheckBoxMenuItem.setText("\u5929\u4f53\u30ea\u30b9\u30c8\u8868\u793a");
        viewmodeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewmodeCheckBoxMenuItemActionPerformed(evt);
            }
        });

        setLayout(new java.awt.GridLayout(1, 0));

    }// </editor-fold>//GEN-END:initComponents

    private void viewmodeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewmodeCheckBoxMenuItemActionPerformed
        boolean state = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
        viewmode( state );
    }//GEN-LAST:event_viewmodeCheckBoxMenuItemActionPerformed

    // リング幅を初期値に戻す

  private void resetRingWidthMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRingWidthMenuItemActionPerformed
      for ( PlanetRing r : planetRings ) {
          r.setRingWidth( DEFAULT_PLANET_RING_WIDTH );
      }
      zodiacPanel.reposition();
  }//GEN-LAST:event_resetRingWidthMenuItemActionPerformed
      //ホロスコープ回転角をリセット
  private void resetAcMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAcMenuItemActionPerformed
      //npt = 0;
      setAC();
      zodiacPanel.setRoll(0d);
      zodiacPanel.repaint();
  }//GEN-LAST:event_resetAcMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem resetAcMenuItem;
    private javax.swing.JMenuItem resetRingWidthMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JCheckBoxMenuItem viewmodeCheckBoxMenuItem;
    // End of variables declaration//GEN-END:variables

}
