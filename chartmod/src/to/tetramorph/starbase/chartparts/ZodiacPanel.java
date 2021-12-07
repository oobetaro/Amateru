/*
 * ZodiacPanel.java
 * Created on 2007/05/28, 1:27
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Timer;
import to.tetramorph.starbase.lib.AngleUtilities;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Transit;

/**
 * ホロスコープの各部品をこのパネルにaddすることで、部品は描画され様々なイベント
 * 処理を行うことができる。
 * @author  大澤義鷹
 */
public class ZodiacPanel extends GPanel implements Printable {
//    double diameter = 0.8;   //ZodiacRingの直径
//    double ringWidth = 0.05; //ZodiacRingの幅
    double ascendant = 0;    //ZodiacRingのアセンダント値
    BasePosition bp = new BasePosition(); //ZodiacRingの中心座標
    double roll;             //ホロスコープ全体の見かけ上の回転角
    ZodiacRing zodiacRing;
    //複数の惑星リングを格納する
    List<PlanetRing> planetRingList = new ArrayList<PlanetRing>();
    Component component;       //イベントを通達してくる親コンポーネント
    ZodiacMouseHandler zmh;    //複数円のイベント処理をまとめて行うクラス
    AspectCircle aspectCircle; //ホロスコープ中央のアスペクト円
    Data natalData;  //表示用
    Transit transit; //表示用
    
    //ダブクリと認識する判定時間を検出
    private final int multiClickInterval =
        ((Integer)Toolkit.getDefaultToolkit()
        .getDesktopProperty("awt.multiClickInterval")).intValue();
    /**
     * オブジェクトを作成する。
     */
    public ZodiacPanel() {
        initComponents();
        setBackground(Color.WHITE);
        zmh = new ZodiacMouseHandler();
        addMouseListener(zmh);
        addMouseMotionListener(zmh);
        addMouseWheelListener(zmh);
    }
    /**
     * ZodiacRingをセットする。
     */
    public void setZodiacRing(ZodiacRing zr) {
        zodiacRing = zr;
        zodiacRing.setBasePosition(bp);
        zodiacRing.setParentComponent(this);
//        zodiacRing.setDiameter(diameter,ringWidth);
    }
    /**
     * ホロスコープ円全体の回転角をセット。これによりホロスコープをひっくりかえして
     * 見る事ができる。
     */
    public void setRoll(double roll) {
        this.roll = roll;
        if ( aspectCircle != null ) aspectCircle.setRoll(roll);
        zodiacRing.setRoll(roll);
        for ( PlanetRing pr : planetRingList )
            pr.setRoll(roll);
    }
    /**
     * ロール値を返す。ホロスコープの見かけ上の回転角
     */
    public double getRoll() {
        return roll;
    }
    /**
     * アセンダント値をセットする。
     */
    public void setAscendant(double ascendant) {
        this.ascendant = ascendant;
        if ( aspectCircle != null ) aspectCircle.setAscendant(ascendant);
        zodiacRing.setAscendant(ascendant);
        for ( PlanetRing pr : planetRingList )
            pr.setAscendant(ascendant);
    }
    /**
     * アスペクト円をセットする。
     */
    public void setAspectCircle(AspectCircle aspectCircle) {
        this.aspectCircle = aspectCircle;
        aspectCircle.setBasePosition(bp);
    }
    /**
     * チャートの名前・日時場所表示欄に表示するデータをセットする。ホロスコープ円
     * の表示には無関係で、円の右側にある情報一覧に出すデータ。
     */
    public void setNatalData(Data data) {
        natalData = data;
    }
    /**
     * トランジットの日時場所表示欄に表示するデータをセットする。
     * 円の右側にある情報一覧に出すデータ。
     */
    public void setTransit(Transit transit) {
        this.transit = transit;
    }
    
    /**
     * ホロスコープと天体リストを描画
     */
    @Override
    public void paintComponent( Graphics graphics ) {
        Dimension size = getSize();
        paintComponent( graphics, size );
    }
    
    /**
     * 指定されたサイズのイメージバッファにホロスコープを描画して返す。
     */
    public BufferedImage getBufferedImage( Dimension size ) {
        BufferedImage img = new BufferedImage( size.width, size.height, 
                                                BufferedImage.TYPE_INT_RGB );
        Graphics g = img.getGraphics();
        g.setColor( getBackground() );
        g.fillRect( 0, 0, img.getWidth(), img.getHeight() );
        paintComponent( g, size );
        g.dispose();
        return img;
    }
    
    /**
     * 印刷用メソッドを実装
     */
    @Override
    public int print( Graphics graphics, PageFormat pf, int pageIndex ) {
        Graphics2D g = (Graphics2D)graphics;
        if ( pageIndex == 0 ) {
            Dimension size = new Dimension();
            size.setSize( pf.getImageableWidth(), pf.getImageableHeight() );
            //System.out.printf( "print width = %f, height = %f\n", size.getWidth(), size.getHeight() );
            g.translate( pf.getImageableX(), pf.getImageableY() );
            paintComponent( g, size );
            g.dispose();
            return Printable.PAGE_EXISTS;
        } else return Printable.NO_SUCH_PAGE;        
    }
    
    private int viewMode = 0;
    
    public void setViewMode( boolean b ) {
        viewMode = b ? 0 : 1;
    }
    /**
     * paintComponent1,paintComponent2の入口で、viewModeの値によってどちらを
     * 呼び出すかを切り替える。
     */
    private void paintComponent( Graphics graphics, Dimension size ) {
        if ( viewMode == 0 ) {
            paintComponent1( graphics, size );
        } else {
            paintComponent2( graphics, size );
        }
    }
    
    /**
     * 横向きA4比率の画面にホロスコープを右寄りに配置して表示するモード
     */
    private void paintComponent1( Graphics graphics, Dimension size ) {
        super.paintComponent( graphics );
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );
        //Dimension size = getSize();
        double width = size.getWidth();
        double height = size.getHeight();
        double w = 0, h = 0; //黄金比の画面サイズが求まる
        double w2 = 0, h2 = 0, h1 = 0;
        if ( width / 1.414 < height ) {
            w = width;
            h = width / 1.414;
        } else {
            w = height * 1.414;
            h = height;
        }
        //g.drawRect(0,0,(int)w,(int)h);
        w2 = w / 1.414;
        bp.x = w * 0.62;
        bp.y = w2 / 2d;
        bp.w = w2;
        bp.g = g;
        if ( aspectCircle != null && ! planetRingList.isEmpty() ) {
            aspectCircle.setDiameter(
                planetRingList.get(planetRingList.size()-1).getDiameter());
            aspectCircle.drawCircle();
        }
        if ( zmh.dragMode == zmh.DRAG_SWAP || zmh.dragMode == zmh.DRAG_RESIZE ) {
            PlanetRing r = zmh.getDraggPlanetRing();
            //内円から描いていく
            for ( int i=planetRingList.size()-1; i >= 0; i-- ) {
                if ( planetRingList.get(i) == zmh.getTargetPlanetRing() ) 
                    continue;
                planetRingList.get(i).draw();
            }
            if ( r != null ) r.draw(); //ドラッグ最中のリングは最後に描画
        } else {
            //内円から描いていく
            for ( int i=planetRingList.size()-1; i >= 0; i-- )
                planetRingList.get(i).draw();
        }
        zodiacRing.draw();
        for ( PlanetRing r : planetRingList ) r.drawOuterCusps();
        if ( aspectCircle != null && ! planetRingList.isEmpty() ) {
            aspectCircle.draw();
        }
        basePoint.x = 0;
        basePoint.y = 0;
        setBasePoint( basePoint );
        drawLayout( g, (float)w );
    }

    Point2D.Double basePoint = new Point2D.Double();
    
    /**
     * ホロスコープを中央に表示するモード
     */
    private void paintComponent2( Graphics graphics, Dimension size ) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D)graphics;
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        //Dimension size = getSize();
        double width = size.getWidth();
        double height = size.getHeight();
        double baseWidth = width < height ? width : height;
        bp.x = width / 2d;
        bp.y = height / 2d;
        bp.w = baseWidth;
        bp.g = g;
        basePoint.x = bp.x - baseWidth / 2.;
        basePoint.y = bp.y - baseWidth / 2.;
        setBasePoint( basePoint );
        if ( aspectCircle != null && ! planetRingList.isEmpty() ) {
            aspectCircle.setDiameter(
                planetRingList.get( planetRingList.size() - 1 ).getDiameter() );
            aspectCircle.drawCircle();
        }
        if ( zmh.dragMode == zmh.DRAG_SWAP || zmh.dragMode == zmh.DRAG_RESIZE ) {
            PlanetRing r = zmh.getDraggPlanetRing();
            //最内円から描いていく
            for ( int i = planetRingList.size() - 1; i >= 0; i-- ) { 
                if ( planetRingList.get(i) == zmh.getTargetPlanetRing() ) 
                    continue;
                planetRingList.get(i).draw();
            }
            if ( r != null ) r.draw(); //ドラッグ最中のリングは最後に描画
        } else {
            //最内円から描いていく
            for ( int i = planetRingList.size() - 1; i >= 0; i-- ) 
                planetRingList.get(i).draw();
        }
        zodiacRing.draw();
        for ( PlanetRing r : planetRingList ) r.drawOuterCusps();
        if ( aspectCircle != null && ! planetRingList.isEmpty() ) {
            aspectCircle.draw();
        }
        
        drawLayout( g, (float)baseWidth );
    }
    
    /**
     * PlanetRingを追加する。
     */
    public void addPlanetRing(PlanetRing planetRing) {
        planetRing.setBasePosition(bp);
        planetRing.setParentComponent(this);
        planetRing.setRoll(getRoll());
        planetRingList.add(0,planetRing);
        for ( PlanetRing r : planetRingList ) r.setZodiacRing(null);
        planetRingList.get(planetRingList.size()-1).setZodiacRing(zodiacRing);
        reposition();
    }
    /**
     * 指定されたPlanetRingを削除する。
     */
    public void removePlanetRing(PlanetRing planetRing) {
        planetRingList.remove(planetRing);
        reposition();
    }
    
    //複数リングの幅を再計算
    public void reposition() {
        double d = zodiacRing.getDiameter();
        for(int i=0; i<planetRingList.size(); i++) {
            PlanetRing pr = planetRingList.get(i);
            d -= (pr.getRingWidth() * 2);
            pr.setDiameter(d);
        }
    }
    
    /**
     * ホロスコープ円のマウスハンドラ。
     */
    class ZodiacMouseHandler implements MouseMotionListener,
        MouseListener,
        MouseWheelListener {
        int clickCount = 0;
        Timer clickTimer = null;   //ﾀﾞﾌﾞﾙｸﾘｯｸ検出用ﾀｲﾏｰ
        //boolean dragging = false; //ﾄﾞﾗｯｸﾞ中はtrueとなるﾌﾗｸﾞ。
        double mouseAngle;
        boolean clickCheckTime = false;
        double draggAngle;
        Object draggObject = null;//ﾄﾞﾗｯｸﾞ中のﾘﾝｸﾞを保管
        double csrlen;            //ﾄﾞﾗｯｸﾞの際、ｶｰｿﾙが何ﾋﾟｸｾﾙ移動したかを保管
        
        int DRAG_NONE = 0;   //なし
        int DRAG_SWAP = 1;   //惑星ﾘﾝｸﾞの順序入替
        int DRAG_RESIZE = 2; //惑星ﾘﾝｸﾞのﾘｻｲｽﾞ
        int DRAG_SPIN = 3;   //獣帯の回転
        int DRAG_MOVE = 4;   //惑星の移動
        int dragMode = DRAG_NONE; //ﾄﾞﾗｯｸﾞ開始とともにその役割を表すｺﾏﾝﾄﾞがｾｯﾄされる
        
        //ﾘﾝｸﾞが交換発生したとき、そのﾘﾝｸﾞがｾｯﾄされる。
        //交換されﾘﾝｸﾞの位置が変化したために、再び交換が必要なように判定され
        //てしまうのを抑止するためにこの変数はある。
        PlanetRing swapedPlanetRing;
        
        //指定座標とリング中心との長さ(単位pixcel)を返す。
        double length(int x,int y) {
            int dx = Math.abs(x - (int)bp.x);
            int dy = Math.abs(y - (int)bp.y);
            return Math.sqrt((double)(dx * dx + dy * dy));
        }
        // 惑星リングがドラッグされているそのリングオブジェクトを返す
        PlanetRing getDraggPlanetRing() {
            if(draggObject instanceof PlanetRing)
                return (PlanetRing)draggObject;
            return null;
        }
        PlanetRing getTargetPlanetRing() {
            return targetPlanetRing;
        }
        long triggerTime;
        //マウスボタンが押し下げられた
        @Override
        public void mousePressed(final MouseEvent e) {
            //押された回数を保管しておきﾀｲﾏｰで一定期間内に何回押されたかを検出
            clickCount++;
            //押し下げられた時刻を保管し、ドラッグ操作開始のときの時刻と比較し、
            //非常に短い時間しか経過していないときは、ドラッグ操作をスルーする。
            triggerTime = e.getWhen();
        }
        //マウスボタンが離れた
        @Override
        public void mouseReleased(final MouseEvent e) {
            if(clickCount == 1) {
                //ﾀﾞﾌﾞﾙｸﾘｯｸを検出するためのﾀｲﾏｰをｽﾀｰﾄ
                clickTimer = new Timer( multiClickInterval,new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        //惑星リングのイベントハンドラに接続
                        Exit: for (;;) { //惑星リングでイベントを拾ったときは、
                            //アスペクト円のイベントは拾わず抜けるための脱出用
                            for ( PlanetRing r : planetRingList ) {
                                if(r.pal != null && r.contains(e.getX(),e.getY())) {
                                    //天体がクリックされた時
                                    PlanetEvent pev = new PlanetEvent(r.getSelectedBody(),e,r);
                                    if(clickCount == 1 && dragMode == DRAG_NONE) {
                                        //ドラックされたあと押し離された場合はリスナは呼ばない
                                        r.pal.bodyClicked(pev); break Exit;
                                    } else if(clickCount == 2) {
                                        r.pal.bodyDoubleClicked(pev); break Exit;
                                    }
                                } else if(r.isRingContains(e.getX(),e.getY()) &&
                                    ! r.contains(e.getX(),e.getY()) && r.ral != null) {
                                    // 惑星リングがダブルクリックされたら、
                                    if(clickCount == 2) {
                                        RingEvent rev = new RingEvent(e,r);
                                        r.ral.ringDoubleClicked(rev);
                                        break Exit;
                                    }
                                }
                            }
                            //アスペクト円のイベントハンドラに接続
                            if(aspectCircle.acl != null) {
                                if(aspectCircle.contains(e.getX(),e.getY())) {
                                    //アスペクト線にヒット
                                    if(clickCount == 1) {
                                        Aspect asp = aspectCircle.getSelectedAspect();
                                        aspectCircle.acl.aspectClicked(
                                            new AspectCircleEvent(asp,e,aspectCircle));
                                    } else if(clickCount == 2) {
                                        Aspect asp = aspectCircle.getSelectedAspect();
                                        aspectCircle.acl.aspectDoubleClicked(
                                            new AspectCircleEvent(asp,e,aspectCircle));
                                    }
                                } else if(aspectCircle.isContainCircle(e.getX(),e.getY())) {
                                    //アスペクト円にヒット
                                    aspectCircle.acl.aspectCircleClicked(
                                        new AspectCircleEvent(null,e,aspectCircle));
                                }
                            }
                            break;
                        }
                        clickCount = 0;
                        clickTimer.stop();
                    }
                });
                clickTimer.start();
            }
            for ( PlanetRing r : planetRingList ) { 
                //ﾄﾞﾗｯｸﾞ終了検出と終了処理
                if ( r.getDraggedBodyID() >= 0 && r.pml != null ) { 
                    //天体ﾄﾞﾗｯｸﾘｽﾅに通達
                    r.pml.bodyDragged( new PlanetEvent( 
                        r.getSelectedBody(), e, r, r.getDraggedAngle() ) );
                }
                r.setDraggBodyID( -1 );
                r.setDiameterOffset( 0 );
            }
            draggObject = null;
            targetPlanetRing = null;
            dragMode = DRAG_NONE;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }
        
        PlanetRing targetPlanetRing;
        PlanetRing resizePlanetRing;
        double innerDiameter = 0;
        double outerDiameter = 0;
        
        //マウスがドラッグされた
        @Override
        public void mouseDragged( MouseEvent e ) {
            long delay = e.getWhen() - triggerTime;
            if ( delay < 250 ) {
                //ボタンが押し下げられてから250ms以下のときはドラッグ操作だと
                //認めない。誤操作が頻発するのでこの処理を入れた。
//                System.out.println("ドラッグ開始までのミリ秒 = " + delay 
//                                    + "[ms]なので拒否(250ms以上なら受理)" );
                e.consume();
                return;
            }
            
            //System.out.println("ドラックボタン = " + e.getModifiersEx());
            if ( ( e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK ) !=
                InputEvent.BUTTON1_DOWN_MASK ) return;
            double zx = e.getX() - bp.x;
            double zy = -(e.getY() - bp.y);
            if ( dragMode == DRAG_NONE ) { 
                //ドラッグ開始。イベントの起きたリングを保管
                outerDiameter = zodiacRing.getDiameter();
                innerDiameter = planetRingList.isEmpty() ? outerDiameter :
                    planetRingList.get( planetRingList.size() - 1 ).getDiameter();
                mouseAngle = AngleUtilities.trigon( zx, zy );
                if ( zodiacRing.isRingContains( e.getX(), e.getY() ) ) { //獣帯ﾘﾝｸﾞの場合
                    draggObject = zodiacRing;
                    dragMode = DRAG_SPIN; //dragging = true;
                } else { //惑星ﾘﾝｸﾞの場合
                    if ( resizeRequest >= 0 ) { //惑星ﾘﾝｸﾞの幅をﾘｻｲｽﾞ
                        //System.out.println("HouseResized");
                        csrlen = length( e.getX(), e.getY() );
                        targetPlanetRing = planetRingList.get( resizeRequest );
                        draggObject = getDummyPlanetRing( targetPlanetRing );
                        dragMode = DRAG_RESIZE;
                    } else {
                        for ( PlanetRing r : planetRingList ) {
                            if ( r.contains( e.getX(), e.getY() ) ) { //惑星のﾄﾞﾗｯｸﾞ操作受付
                                if ( r.isNotDragBody( r.getSelectedBody().id ) )
                                    break; //ﾄﾞﾗｯｸﾞ禁止天体の場合は脱出
                                //System.out.println("PlanetMoved");
                                draggAngle = r.getSelectedBody().lon;
                                r.setDraggBodyID( r.getSelectedBody().id );
                                r.setDraggedAngle( draggAngle );
                                draggObject = r;
                                dragMode = DRAG_MOVE;
                                break;
                            } else if ( r.isRingContains( e.getX(), e.getY() ) ) {
                                //ﾘﾝｸﾞ交換のﾄﾞﾗｯｸﾞ操作受付
                                //System.out.println("HouseSwaped");
                                csrlen = length(e.getX(),e.getY());
                                //ﾄﾞﾗｯｸﾞのｱﾆﾒ用に複製。外円は描画する。
                                draggObject = getDummyPlanetRing( r );
                                targetPlanetRing = r; //ｵﾘｼﾞﾅﾙも保管しておく
                                dragMode = DRAG_SWAP;
                                break;
                            }
                        }
                    }
                }
            }
            if ( dragMode != DRAG_NONE ) {
                //ドラッグ中。保管してあるリングに対してイベント処理を行う
                double ma = AngleUtilities.trigon( zx, zy );
                if ( draggObject instanceof ZodiacRing ) {
                    //獣帯リングのドラッグによる回転
                    setRoll( getRoll() - AngleUtilities.angleDistance( mouseAngle, ma ) );
                    mouseAngle = ma;
                }
                if ( draggObject instanceof PlanetRing ) {
                    PlanetRing r = (PlanetRing)draggObject;
                    if ( dragMode == DRAG_RESIZE ) { //惑星リングの幅のリサイズ
                        double csrdia = length(e.getX(),e.getY()) * 2 / bp.w;
                        if ( e.isShiftDown() ) springResizeRingWidth( r, csrdia );
                        else resizeRingWidth( r, csrdia );
                    } else if ( dragMode == DRAG_SWAP ) { //惑星ﾘﾝｸﾞの入替
                        if ( planetRingList.size() <= 1 )
                            return; //一重円のときは移動できないので無視
                        double nowLen = length(e.getX(),e.getY());
                        double mvlen = nowLen - csrlen;
                        double dof = (nowLen - csrlen) / bp.w * 2;
                        double rd = r.getDiameter() + dof;
                        if ( rd < innerDiameter ) {
                            r.setDiameterOffset( innerDiameter - r.getDiameter() );
                        } else if ( rd + r.getRingWidth() * 2 > outerDiameter ) {
                            r.setDiameterOffset(
                                (outerDiameter -  r.getRingWidth() * 2) - r.getDiameter());
                        } else {
                            r.setDiameterOffset(dof);
                        }
                        PlanetRing r2 = getUnderRing(r);
                        if ( swapedPlanetRing != r2 ) {
                            swapedPlanetRing = null;
                            if ( r2 != r ) { //交換
                                int i1 = planetRingList.indexOf( targetPlanetRing );
                                int i2 = planetRingList.indexOf( r2 );
                                planetRingList.set( i1, r2 );
                                planetRingList.set( i2, targetPlanetRing );
                                double d = zodiacRing.getDiameter();
                                for ( PlanetRing p : planetRingList ) { //各円のｻｲｽﾞを再計算
                                    d -= ( p.getRingWidth() * 2 );
                                    p.setDiameter( d );
                                }
                                swapedPlanetRing = r2;
                            }
                        }
                    } else if ( dragMode == DRAG_MOVE ) { //天体のドラッグ
                        draggAngle += AngleUtilities.angleDistance(mouseAngle,ma);
                        r.setDraggedAngle( draggAngle );
                        mouseAngle = ma;
                    }
                }
            }
            repaint();
        }
        
        // rの下にある惑星ﾘﾝｸﾞを返す
        PlanetRing getUnderRing(PlanetRing r) {
            double d = r.getDiameter()+r.getDiameterOffset()+(r.getRingWidth());
            for ( PlanetRing p : planetRingList )
                if ( d > p.getDiameter() ) return p;
            return null;
        }
        
        // 外円線つきの新しいリングを返す。これはドラッグ時だけの「見せリング」
        PlanetRing getDummyPlanetRing(PlanetRing r) {
            PlanetRing p = new PlanetRing(r);
            p.setPaintFormula(p.getPaintFormula() | HouseRing.DRAW_OUTER_ARC);
            return p;
        }
        
        // rに対してnewDiameterを適用し、他のリングも必要があれば幅を調整する。
        void resizeRingWidth(PlanetRing r,double newDiameter) {
            int i = planetRingList.indexOf(targetPlanetRing);
            double w = (r.getDiameter() - newDiameter) / 2;
            double  rw = r.getRingWidth() + w;
            if(rw < 0) return;
            if ( i == planetRingList.size() - 1 ) {
                r.setRingWidth(rw);
                r.setDiameter(newDiameter);
                targetPlanetRing.setRingWidth(rw);
                targetPlanetRing.setDiameter(newDiameter);
            } else {
                PlanetRing r2 = planetRingList.get(i+1);
                if ( r2.getRingWidth() - w < 0 ) return;
                r.setRingWidth(rw);
                r.setDiameter(newDiameter);
                targetPlanetRing.setRingWidth(rw);
                targetPlanetRing.setDiameter(newDiameter);
                r2.setRingWidth(r2.getRingWidth() - w);
            }
        }
        // 相対的にリサイズする。
        void springResizeRingWidth(PlanetRing r,double newDiameter) {
            if ( newDiameter > zodiacRing.getDiameter() ) return;
            double nw =  (targetPlanetRing.getDiameter() - newDiameter) / 2; //ﾘｻｲｽﾞ量
            //扱いやすくするため配列に幅をｺﾋﾟｰ
            double [] rw = new double[planetRingList.size()];
            for(int i=0; i<planetRingList.size(); i++)
                rw[i] = planetRingList.get(i).getRingWidth();
            int j = planetRingList.indexOf(targetPlanetRing); //ﾘｻｲｽﾞ要求ﾘﾝｸﾞの番号
            //外円にむかっての幅を求める
            double w = 0;
            for ( int i=j; i>=0; i-- ) w += rw[i];
            double par = (w + nw)/w;
            //ダミー円にも値を適用
            r.setRingWidth(r.getRingWidth() * par);
            r.setDiameter(newDiameter);
            for(int i=j; i>=0; i--) rw[i] *= par; //外円に向かってﾘｻｲｽﾞ幅を算出
            if ( j < planetRingList.size() - 1 ) {  //内円にむかっての幅を求める
                w = 0;
                j++;                   //ﾘｻｲｽﾞ要求されたﾘﾝｸﾞより一つ内円から計算
                //内円までの幅を求める
                for ( int i=j; i<rw.length; i++ ) w += rw[i];
                par = (w - nw)/w; //変化比率を求める
                //内円までの新しい幅を算出
                for ( int i=j; i < rw.length; i++ ) rw[i] *= par;
            }
            for ( int i=0; i<rw.length; i++ )
                planetRingList.get(i).setRingWidth(rw[i]); //各ﾘﾝｸﾞに幅値を書き戻す
            reposition(); //直径を再計算
        }
        
        int resizeRequest = -1;
        // 天体へのｵﾝｶｰｿﾙ,ｱｳﾄｶｰｿﾙを管理するたのめﾊｯｼｭ
        HashMap<PlanetRing,Body> onCursorMap = new HashMap<PlanetRing,Body>();
        Aspect cursorAspect = null; //オンカーソル中のアスペクト保管用
        
        //マウスが移動された(オンカーソル赤点灯のトリガをかける)
        @Override
        public void mouseMoved(MouseEvent e) {
            if ( dragMode != DRAG_NONE ) return;
            if ( zodiacRing.isRingContains( e.getX(), e.getY() ) ) {
                zodiacRing.setHighLight(true);
                setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                repaint();
                return;
            } else if ( zodiacRing.isHighLight() ) {
                zodiacRing.setHighLight(false);
                repaint();
            }
            for ( PlanetRing r : planetRingList ) {
                Body body = onCursorMap.get(r);
                if ( r.contains( e.getX(), e.getY() ) ) {
                    if ( r.pal != null ) {
                        if ( body == null ) {
                            //オンカーソル
                            r.pal.bodyOnCursor(new PlanetEvent(r.getSelectedBody(),e,r));
                            onCursorMap.put( r, r.getSelectedBody() );
                        } else if ( body != r.getSelectedBody() ) {
                            //bodyからアウトカーソル
                            r.pal.bodyOutCursor( new PlanetEvent( body, e, r) );
                            onCursorMap.remove(r);
                            //bodyにオンカーソル
                            r.pal.bodyOnCursor( new PlanetEvent( r.getSelectedBody(), e, r) );
                            onCursorMap.put( r, r.getSelectedBody() );
                        }
                    }
                } else {
                    if(body != null && r.pal != null) {
                        //bodyからアウトカーソル
                        r.pal.bodyOutCursor(new PlanetEvent(body,e,r));
                        onCursorMap.remove(r);
                    }
                }
            }
            //アスペクト円と円内のアスペクトのイベント処理
            if ( aspectCircle != null ) {
                if( aspectCircle.contains( e.getX(), e.getY() ) ) {
                    Aspect asp = aspectCircle.getSelectedAspect();
                    if ( aspectCircle.acl != null ) {
                        if ( cursorAspect == null ) {
                            aspectCircle.acl.aspectOnCursor(
                                new AspectCircleEvent(asp,e,aspectCircle));
                            cursorAspect = asp;
                        } else if( cursorAspect != asp ) {
                            aspectCircle.acl.aspectOutCursor(
                                new AspectCircleEvent(cursorAspect,e,aspectCircle));
                            aspectCircle.acl.aspectOnCursor(
                                new AspectCircleEvent(asp,e,aspectCircle));
                            cursorAspect = asp;
                        }
                        for ( PlanetRing r : planetRingList)
                            r.setSelectedAspect(asp);
                    }
                } else {
                    if ( cursorAspect != null & aspectCircle.acl != null ) {
                        aspectCircle.acl.aspectOutCursor(
                            new AspectCircleEvent( cursorAspect, e, aspectCircle ) );
                        cursorAspect = null;
                        for ( PlanetRing r : planetRingList ) 
                            r.setSelectedAspect(null);
                    }
                }
            }
            //ここでisInnerRingContains()となっても、ドラッグイベント側ではfalseになる
            //事がある。わずかな時間の間にカーソルが移動することがあるらしい。
            //そこでここでどのリングでリサイズ要求があったか登録し、
            //ドラッグイベント側ではisInnerRingContain()判定はしないようにする。
            resizeRequest = -1;
            for ( int i = planetRingList.size() - 1; i >= 0; i-- ) {
                if ( planetRingList.get(i).isInnerRingContains( e.getX(), e.getY() ) ) {
                    setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
                    resizeRequest = i;
                    break;
                }
            }
            if ( resizeRequest < 0 ) setCursor( Cursor.getDefaultCursor() );
            repaint();
        }
        //マウスホイールの回転に応じてringRolled()イベントを発生
        @Override
        public void mouseWheelMoved( MouseWheelEvent e ) {
            if ( zodiacRing.isRingContains( e.getX(), e.getY() ) ) {
                setRoll( getRoll() - e.getWheelRotation() );
                repaint();
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {}
        //マウスカーソルがコンポーネントの外に出たときは、リングにそれを通達
        @Override
        public void mouseExited(MouseEvent e) {
            //System.out.println("マウスカーソル圏外");
            int x = -100, y = -100;
            //天体円のハイライトも消去
            for ( PlanetRing r : planetRingList ) {
//        r.contains(x,y);
                r.isInnerRingContains(x,y);
//        r.isRingContains(x,y);
//        zodiacRing.isRingContains(x,y);
                Body body = onCursorMap.get(r);
                if ( body != null ) {
                    if ( r.pal != null ) {
                        r.pal.bodyOutCursor( new PlanetEvent( body, e, r ) );
                        onCursorMap.remove(r);
                    }
                }
            }
            //アスペクト円でオンカーソルしているものは消去
            if ( cursorAspect != null & aspectCircle.acl != null ) {
                aspectCircle.acl.aspectOutCursor(
                    new AspectCircleEvent( cursorAspect, e, aspectCircle ) );
                cursorAspect = null;
                for ( PlanetRing r : planetRingList ) 
                    r.setSelectedAspect(null);
            }
            aspectCircle.isContainCircle( x, y );
            aspectCircle.contains( x, y );
            //獣帯リングのハイライトも消去
            zodiacRing.setHighLight(false);
            repaint();
        }
        @Override
        public void mouseClicked(MouseEvent e) {}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(null);
    }// </editor-fold>//GEN-END:initComponents
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
  /*
   * PlanetRingやその他の部品で、それ専用のアクションリスナを登録するが、PlanetRing
   * 内でアクションを拾うことはない。結局どうやってるかというと、ZodiacPanelにマウスの
   * アクションをひろうリスナが用意されていて、そのリスナの中から、PlanetRingに
   * セットされているリスナが呼び出される仕組みになっている。
   */
}
