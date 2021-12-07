/*
 * PlanetRing.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import static java.lang.Math.*;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.util.AstroFont;
import to.tetramorph.starbase.util.PlotAdjuster;

/**
 * ホロスコープの惑星や感受点を描画する。時計でいえば惑星針。
 * ドラッグ中の天体が常にいちばん手前に表示されるように。
 * @author 大澤義鷹
 */
public class PlanetRing extends ChartParts {
    Font bodyFont = AstroFont.getFont(10);
    //Font houseFont = new Font("Serif",Font.PLAIN,10);
    Font font;
    static Stroke stroke = new BasicStroke(); //描画線のｽﾄﾛｰｸ
    //天体に縁取りをつけるときのｽﾄﾛｰｸ
    static Stroke borderStroke = new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND );
    HouseRing houseRing; //ﾊｳｽのｾｸﾀｰ状ﾘﾝｸﾞ
    HouseNumberDial houseNumberDial; //ﾊｳｽの室番号描画ｵﾌﾞｼﾞｪｸﾄ
    GaugeDial gaugeDial; //円周上の目盛り描画ｵﾌﾞｼﾞｪｸﾄ
    List<Body> bodys;    //表示する天体のﾘｽﾄでsetBodyList()の際これにｾｯﾄされる
    float bodyFontSize; //天体の表示ｻｲｽﾞで%で指定
    int onCursor = -1;   //shapes[]のどれかにオンカーソルした際、その番号が入る。
    Body selectedBody;   //containes()メソッドの中でセットされる。選択された天体オブジェクト。
    Aspect selectedAspect; //選択されたアスペクト情報で外部から入ってくる。
    int draggBodyID = -1;       //ﾄﾞﾗｯｸﾞ中、ﾄﾞﾗｯｸﾞされている天体IDが入る。その他の時は-1。
    double draggAngle = 0;     //ドラッグ中の天体の「黄道座標」
    double ringWidth;     //リングの幅
    double [] cusps;    //カスプの度数
    double symbolSize; //天体シンボルの大きさ(%)setRingWidth()で動的に決定する
    Map<Rectangle2D,Body> boundsMap; //当たり判定に使う天体を囲むﾊﾞｳﾝﾀﾞﾘｰﾎﾞｯｸｽとその天体ｵﾌﾞｼﾞｪｸﾄのﾏｯﾌﾟ
    static Color [] houseBGColors = { Color.WHITE }; //ﾃﾞﾌｫﾙﾄ背景色
    Component component; //描画対象のｺﾝﾎﾟｰﾈﾝﾄ(ﾌｫﾝﾄ取得にのみ使用していて無駄。最終的には削除予定)
    MarkerNeedle markerNeedle;
    TextDial textDial;
    //天体度数表示を行うリング幅(単位pixcel)の最小値。
    //この値より小さいときは画面上でつぶれてしまうので度数表示しない。
    final static int SHOW_DEGREE_MINIMUM_WIDTH = 26; //18;
    boolean isInnerRingOnCursor = false; //内円線上にオンカーソル中はtrue
    HouseRing cuspHouseRing; //獣帯円上のカスプ線
    HouseNumberDial houseNumberDial2; //獣帯円の外にあるハウス番号
    NumberNeedle cuspNumberNeedle;
    PlotAdjuster plotAdjuster = new PlotAdjuster(); //感受点表示位置調整
    int [] notDragBodys = null; //ドラッグを受け付けない天体リスト
    Color bodyColor = Color.BLACK;
    Color bodyHighLightColor = Color.RED;
    Color bodyDegreeColor = Color.BLACK;
    Color bodysBorderColor = Color.BLACK;
    int bodysEffect = 0;

    /**
     * 惑星リングrの複製品を作成する。
     */
    public PlanetRing(PlanetRing r) {
        this();
        setDiameter(r.getDiameter());
        setDiameterOffset(r.getDiameterOffset());
        setRingWidth(r.getRingWidth());
        setBasePosition(r.getBasePosition());
        setRoll(r.getRoll());
        setAscendant(r.getAscendant());
        setBodyList(r.getBodyList());
        setCusps(r.getCusps());
        selectedBody = r.getSelectedBody();
        setDraggBodyID(r.getDraggedBodyID());
        setDraggedAngle(r.getDraggedAngle());
        setRingName(r.getRingName());
        this.component = r.component;
    }
    /**
     * 幅を指定してPlanetRingオブジェクトを作成する。
     */
    public PlanetRing( double ringWidth ) {
        gaugeDial = new GaugeDial();
        houseRing = new HouseRing();
        markerNeedle = new MarkerNeedle();
        houseNumberDial = new HouseNumberDial();
        houseRing.setPaintFormula( HouseRing.DRAW_CUSP | HouseRing.DRAW_INNER_ARC | HouseRing.DRAW_FILL );
        houseRing.setBGColors(houseBGColors);
        textDial = new TextDial();
        cuspHouseRing = new HouseRing();
        cuspHouseRing.setPaintFormula( HouseRing.DRAW_CUSP );
        houseNumberDial2 = new HouseNumberDial();
        cuspNumberNeedle = new NumberNeedle();
        setRingWidth( ringWidth );
    }
    /**
     * デフォルトの幅でPlanetRingオブジェクトを作成する。幅は0.07。
     */
    public PlanetRing() {
        this( 0.07 );
    }
    // ハウスリングの各線のどれを引くか、塗りつぶすかの指定
    public void setPaintFormula(int formula) {
        houseRing.setPaintFormula(formula);
    }
    public int getPaintFormula() {
        return houseRing.getPaintFormula();
    }
    public void setParentComponent(Component component) {
        this.component = component;
    }

    @Override
    public void setBasePosition(BasePosition bp) {
        super.setBasePosition(bp);
        gaugeDial.setBasePosition(bp);
        houseRing.setBasePosition(bp);
        houseNumberDial.setBasePosition(bp);
        markerNeedle.setBasePosition(bp);
        textDial.setBasePosition(bp);
        cuspHouseRing.setBasePosition(bp);
        houseNumberDial2.setBasePosition(bp);
        cuspNumberNeedle.setBasePosition(bp);
    }

    /**
     * リングの内円の直径をセットする。デフォルト値は0。
     */
    @Override
    public void setDiameter(double diameter) {
        super.setDiameter(diameter);
        gaugeDial.setDiameter(diameter);
        houseRing.setDiameter(diameter);
        markerNeedle.setDiameter(diameter);
    }

    /**
     * リングの幅をセットする。デフォルトは0.05。
     */
    public final void setRingWidth(double ringWidth) {
        this.ringWidth = ringWidth;
        houseRing.setRingWidth(ringWidth);
        houseNumberDial.setRingWidth(ringWidth);
        textDial.setRingWidth(ringWidth);
        symbolSize = ringWidth / 2;
    }
    /**
     * リングの幅を返す。
     */
    public double getRingWidth() {
        return ringWidth;
    }
    /**
     * 回転角をセットする。
     */
    @Override
    public void setRoll(double roll) {
        super.setRoll(roll);
        houseRing.setRoll(roll);
        houseNumberDial.setRoll(roll);
        gaugeDial.setRoll(roll);
        markerNeedle.setRoll(roll);
        textDial.setRoll(roll);
        cuspHouseRing.setRoll(roll);
        houseNumberDial2.setRoll(roll);
        cuspNumberNeedle.setRoll(roll);
    }
    @Override
    public void setAscendant(double ascendant) {
        super.setAscendant(ascendant);
        houseRing.setAscendant(ascendant);
        houseNumberDial.setAscendant(ascendant);
        gaugeDial.setAscendant(ascendant);
        markerNeedle.setAscendant(ascendant);
        cuspHouseRing.setAscendant(ascendant);
        houseNumberDial2.setAscendant(ascendant);
        cuspNumberNeedle.setAscendant(ascendant);
    }

    /**
     * 天体リストをセットする。
     */
    public void setBodyList(List<Body> bodys) {
        this.bodys = bodys;
        this.bodyFontSize = 0; //リサイズを検出するフラグをリセットする
        markerNeedle.setBodyList(bodys);
        if ( bp == null ) return; // *1
        double len = bp.w * getDiameter() / 2;
    }

    // *1 bpはZodiacPanelにaddされたときにZodiacPanelがセットする。
    // NPT相性円でadd前に天体リストがセットされるとbpがnullで例外が出る。
    // addされてないリングは表示されないからそのときはsetBodyListを呼ばなければ
    // いいのだが、判定が面倒なので、このメソッド内でnull判定して無視する仕様
    // にしている。後からしわ寄せが来る可能性も無きにしもだが、とりあえずこれで
    // やっつけて置く。

    /**
     * 天体リストを返す。
     */
    public List<Body> getBodyList() {
        return bodys;
    }

    /**
     * アスペクトをセットする。すると二つの天体が赤点灯する。
     * nullをセットすると点灯させない。
     */
    public void setSelectedAspect(Aspect aspect) {
        this.selectedAspect = aspect;
    }
    /**
     * ドラッグを開始する天体を指定する。
     * 0以下の値をセットしたときは、ドラッグ終了となる。その際getSelectedBody()は
     * nullを返すようになる。
     */
    public void setDraggBodyID(int bodyID) {
        if(bodyID >= 0)
            draggBodyID = bodyID;
        else {
            draggBodyID = -1;
            selectedBody = null;
        }
    }
    public int getDraggedBodyID() {
        return draggBodyID;
    }
    /**
     * ドラッグ中の天体の位置角をセットする。
     */
    public void setDraggedAngle(double angle) {
        draggAngle = angle;
    }
    /**
     * ドラッグされた位置(0-360)を返す。
     */
    public double getDraggedAngle() {
        double da = draggAngle < 0 ? draggAngle + 360 : draggAngle;
        return da % 360;
    }
    // 現在のﾘﾝｸﾞの幅と直径から文字のｻｲｽﾞを返す
    float symsize() {
        double h = ringWidth * bp.w * 0.5;
        double w = diameter * bp.w * PI / 40; //直径の40分の1。40は適当に決めた値。
        return (float) Math.min(h,w); //小さい方のサイズを採用する。
    }
    ZodiacRing zodiacRing;
    /**
     * 獣帯リングをセットする。これをセットすると、カスプ度数やアセンダントが獣帯円
     * の外に表示されるようになる。nullをセットするとアセンダントは惑星リングの中に
     * 表示され、カスプ度数は表示されない。
     */
    public void setZodiacRing(ZodiacRing zodiacRing) {
        this.zodiacRing = zodiacRing;
    }
    /**
     * setFactor()で与えられたパラメターに従って描画する。
     */
    @Override
    public void draw() {
        float sz = symsize();
        //ハウス番号のサイズ
        double ringWidthPixcel = ringWidth * bp.w; //リング幅のﾋﾟｸｾﾙ数を得る
        double d  = diameter + diameterOffset;
        double pd = ringWidthPixcel >= SHOW_DEGREE_MINIMUM_WIDTH ? 0.4 : 0.5;
        double planetDiameter = d + ( ringWidth * pd * 2 );
        double plotaj = 0;
        if ( sz != bodyFontSize ) {
            bodyFontSize = sz;
            bodyFont = AstroFont.getFont(bodyFontSize);
            font = component.getFont().deriveFont(sz/2f);
            // 天体針の直径に応じて感受点の表示位置調整を行う
            // 天体針の直径が大きくなるほどplotajは小さくなるが、だいたいつねに
            // 5〜3度の範囲に収まる。公式 : sinθ= a / c。
            // 1.6は補正値。2だと離れすぎ。
            plotaj = toDegrees(
                asin( bodyFontSize / (planetDiameter * bp.w) ) ) * 1.6;
            this.bodys = plotAdjuster.getAdustedList( bodys, plotaj );
//            System.out.println("planetDiameter = " + planetDiameter + ", size = "
//                + plotaj );
        }

        double textDiameter   = d + ( ringWidth * 0.65 * 2 );
        double extDiameter    = ( d + ( ringWidth * 0.15 * 2 ) );
        double numDiameter    = planetDiameter + ( bodyFontSize / bp.w * 1.6 );
        markerNeedle.setExtensionDiameter(extDiameter);
        houseRing.draw();
        markerNeedle.setAcMcVisible ( zodiacRing == null );
        markerNeedle.draw();
        houseNumberDial.setDiameter(planetDiameter);
        houseNumberDial.draw();
        gaugeDial.draw();
        // 天体シンボルを描く
        FontRenderContext render = bp.g.getFontRenderContext();
        double planetRadius = planetDiameter * bp.w / 2d;
        double numRadius    = numDiameter    * bp.w / 2d;

        boundsMap = new HashMap<Rectangle2D,Body>();
        Body draggBody = null;
        for ( Body body : bodys ) {
            if ( body.id == draggBodyID ) {
                draggBody = body;
                continue;
            }
            Shape shape;
            if ( body.id >= AC && body.id <= IC && zodiacRing != null ) {
                //AC,MCはzodiacRingが登録されているときは獣帯円の外におく
                double zd = zodiacRing.getDiameter()
                           + (zodiacRing.getRingWidth() * 1.68 * 2);
                double zr = zd * bp.w / 2;
                //plotではなくlonを使う
                double a = -( body.lon + 180 - ( roll + ascendant ) );
                shape = getBodyShape( a, zr, render, body.id );
            } else { //通常の天体描画
                double a = -( body.plot + 180 - ( roll + ascendant ) );
                shape = getBodyShape( a, planetRadius, render, body.id );
                if ( ringWidthPixcel >= SHOW_DEGREE_MINIMUM_WIDTH ) {
                    //幅が小さすぎるときは数字は表示しない
                    drawNum2( a, numRadius, render, draggBodyID == body.id, body);
                }
            }
            drawEffect(shape);
            Color color;
            if ( selectedAspect != null && selectedAspect.contains(body) )
                color = bodyHighLightColor;
            else color = (onCursor == body.id) ? bodyHighLightColor : bodyColor;
            bp.g.setPaint( color );
            bp.g.setStroke( stroke );
            bp.g.fill( shape );
            //当たり判定ようにｼｪｲﾌﾟのﾊﾞｳﾝｽﾞを保管
            boundsMap.put( shape.getBounds2D(), body );
        }
        if ( draggBody != null ) {
            double a = -( draggAngle + 180 - (roll+ascendant) );//描画角度に変換
            if ( ringWidthPixcel >= SHOW_DEGREE_MINIMUM_WIDTH )
                drawNum2( a, numRadius, render, draggBodyID == draggBody.id,
                          draggBody );
            Shape shape = getBodyShape(a,planetRadius,render,draggBody.id);
            //drawBorder( shape );
            drawEffect( shape );
            bp.g.setPaint( bodyHighLightColor );
            bp.g.setStroke( stroke );
            bp.g.fill( shape );
            boundsMap.put( shape.getBounds2D(), draggBody );
        }
        if ( isInnerRingOnCursor ) {
            textDial.setDiameter( textDiameter );
            textDial.draw();
        }
    }
    private void drawEffect( Shape shape ) {
        if ( bodysEffect == 1 ) {
            bp.g.setStroke( borderStroke );
            bp.g.setPaint( bodysBorderColor );
            bp.g.draw( shape );
        } else if ( bodysEffect == 2 ) {
            AffineTransform at = new AffineTransform();
            double dx = bp.w * 0.002;
            if ( dx < 1 ) dx++;
            at.translate( dx, dx );
            bp.g.setStroke( stroke );
            bp.g.setPaint( bodysBorderColor );
            bp.g.fill( at.createTransformedShape( shape ) );
        }
    }
    /**
     * 獣帯円の上にカスプ線を描画する。これはZodiacRing描画後に呼び出さないと、
     * 上書きされて消えてしまう。setZodiacRing()でZodiacRingオブジェクトが登録さ
     * れていない場合は、なにもしない。
     */
    public void drawOuterCusps() {
        if ( zodiacRing == null ) return;
        double rw = zodiacRing.getRingWidth() * 1.33;
        cuspHouseRing.setRingWidth( rw );
        cuspHouseRing.setDiameter( zodiacRing.getDiameter() );
        cuspHouseRing.draw();
        houseNumberDial2.setRingWidth( zodiacRing.getRingWidth() );
        houseNumberDial2.setDiameter(
            zodiacRing.getDiameter() + zodiacRing.getRingWidth() * 2.5);
        houseNumberDial2.draw();
        cuspNumberNeedle.setSymbolSize( 0.015f );
        cuspNumberNeedle.setVOffset( 0.015 );
//        cuspNumberNeedle.setDiameter(
//            zodiacRing.getDiameter() + zodiacRing.getRingWidth() * 2.75 );
        cuspNumberNeedle.setDiameter(
            ( zodiacRing.getDiameter() + zodiacRing.getRingWidth() * 2 ) * 1.041 );
        cuspNumberNeedle.draw();
    }



    Shape getBodyShape( double a,
                        double radius,
                        FontRenderContext render,
                        int bodyID ) {
        //描画位置を直行座標で求める
        double bx = cos( a * PI / 180d) * radius + bp.x;
        double by = sin( a * PI / 180d) * radius + bp.y;
        TextLayout textlayout =
            new TextLayout(""+BODY_CHARS[bodyID],bodyFont,render);
        //そのグラフィック文字の(高さ/2)と(幅/2)を得る
        float h = textlayout.getAscent()/2f;
        float w = textlayout.getAdvance()/2f;
        //シンボルの中心が原点に来るように移動させる
        AffineTransform at = new AffineTransform();
        //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
        at.translate( -w, h );
        Shape signShape = textlayout.getOutline(at);
        //シンボルを回転
        AffineTransform at3 = new AffineTransform();
        at3.rotate( ( a + 90 ) * PI / 180f );
        signShape = at3.createTransformedShape(signShape);
        //シンボルを獣帯円の所定の場所に移動
        AffineTransform at2 = new AffineTransform();
        at2.translate( bx, by );
        return at2.createTransformedShape( signShape );
    }

    // 天体の頭にある数字を描く
    void drawNum2( double a,
                    double numRadius,
                    FontRenderContext render,
                    boolean isDraggingBody,
                    Body sp) {
        double bx = cos( a * PI / 180d) * numRadius + bp.x;
        double by = sin( a * PI / 180d) * numRadius + bp.y;
        int za = 0;
        if(isDraggingBody) za = Math.abs((int)draggAngle % 30);
        else               za = (int)sp.lon % 30;
        String zn = za < 10 ? " " + za : "" + za;
        TextLayout tl1 = new TextLayout(zn,font,render);
        AffineTransform at = new AffineTransform();
        Shape numShape = tl1.getOutline(at);
        at.translate( -tl1.getAdvance() / 2f, 0 ); //文字列の中央がx=0に来るように移動
        numShape = at.createTransformedShape(numShape);
        Area numArea = new Area(numShape);
        if ( sp.lonSpeed < 0 ) { //逆行がつくときは数字にマークを追加する
            at = new AffineTransform();
            TextLayout tl2 = new TextLayout("▲",font,render);
            Shape revShape = tl2.getOutline(at);
            //印の中央がx=0に来るようにする。数字の上に印を置く。
            at.translate( - tl2.getAdvance() / 2f, -tl1.getAscent() );
            revShape = at.createTransformedShape( revShape );
            numArea.add(new Area(revShape)); //数字と印を合成する。
        }
        at = new AffineTransform();
        at.translate( 0,tl1.getAscent()/2f); //数字の高さをy=0の場所に移動。
        numShape = at.createTransformedShape( numArea );
        //数字と印からなるShapeを180度回転し横倒しにする。
        at = new AffineTransform();
        at.rotate(180 * PI/180f,0,0);
        numShape = at.createTransformedShape( numShape );
        //数字と印を右横に移動
        double hoffset = numRadius; //radius + txw;
        double voffset = 0.005; // 数字と天体の中心線を合わせるための補正値
        at = new AffineTransform();
        at.translate( hoffset,(float)(voffset * bp.w) );
        numShape = at.createTransformedShape( numShape );
        //文字列をホロスコープの中心を基準に回転
        at = new AffineTransform();
        at.rotate( a * PI / 180f, 0d, 0d );
        numShape = at.createTransformedShape( numShape );
        //文字列をキャンバスの中心に移動
        at = new AffineTransform();
        at.translate(bp.x,bp.y);
        numShape = at.createTransformedShape( numShape );
        bp.g.setPaint( bodyDegreeColor );
        bp.g.fill(numShape);
    }

    /**
     * 指定された座標に天体が存在する場合はtrueを返す。
     * このメソッドがtrueを返す場合、selectedBodyに存在した天体がセットされる。
     * 存在しなかった場合はnullがセットされる。
     */
    public boolean contains( int x, int y ) {
        if ( boundsMap == null ) return false;
        Set<Rectangle2D> boundsSet = boundsMap.keySet();
        for ( Iterator ite = boundsSet.iterator(); ite.hasNext(); ) {
            Rectangle2D rect = (Rectangle2D)ite.next();
            if ( rect.contains( (double)x, (double)y ) ) {
                  selectedBody = boundsMap.get( rect );
                  onCursor = selectedBody.id;
                  return true;
            }
        }
        onCursor = -1;
        selectedBody = null;
        return false;
    }
    /**
     * contains()メソッドがtrueを返す場合、このメソッドで選択中の天体を取得できる。
     * falseが返される場合は、このメソッドもnullを返す。
     */
    public Body getSelectedBody() {
        return selectedBody;
    }

    /**
     * ハウスリングにx,y座標が重なるときはtrueを返す。
     */
    public boolean isRingContains(int x,int y) {
        //内円にｶｰｿﾙがあるときはﾘﾝｸﾞ内でもfalseとみなす。
        if ( isInnerRingContains(x,y) ) return false;
        Sector [] sectors = houseRing.getSectors();
        for ( Sector sect : sectors )
            if ( sect.sector.contains(x,y) ) return true;
        return false;
    }
    /**
     * ﾘﾝｸﾞの内円のボーダー線にx,y座標が衝突する場合はtrueを返す。
     */
    public boolean isInnerRingContains(int x,int y) {
        double dx = abs( bp.x - x );
        double dy = abs( bp.y - y );
        //入力座標と中心からの長さ(半径)を求める
        double r1px = sqrt( dx * dx + dy * dy );
        //リング内円の半径を求める
        double r2px = (diameter + diameterOffset) * bp.w / 2;
        //両者の長さが3以内なら当たりと見なす
        isInnerRingOnCursor = abs( r1px - r2px ) < 3;
        return isInnerRingOnCursor;
    }

    @Override
    public void setDiameterOffset(double offset) {
        super.setDiameterOffset( offset );
        gaugeDial.setDiameterOffset( offset );
        houseRing.setDiameterOffset( offset );
        markerNeedle.setDiameterOffset( offset );
    }
    protected PlanetActionListener pal;
    protected RingActionListener ral;
    protected PlanetMotionListener pml;
    /**
     * このリングにリスナをセットする。nullをセットすると、リスナを削除したことに
     * なる。またこのリスナはAWTのリスナとは異なり、PlanetRingを単体動作させだけで
     * は機能せず、ZodiacPanelにこのオブジェクトをaddしてはじめて機能する。
     * このクラス内ではリスナのインスタンスを保管しているだけで内部での利用はない。
     * インスタンスを使用するのはZodiacPanel。
     */
    public void setPlanetActionListener(PlanetActionListener pal) {
        this.pal = pal;
    }
    public void setRingActionListener(RingActionListener ral) {
        this.ral = ral;
    }
    public void setPlanetMotionListener(PlanetMotionListener pml) {
        this.pml = pml;
    }

    public void setCusps(double [] cusps) {
        this.cusps = cusps;
        houseNumberDial.setCusps( cusps );
        houseRing.setCusps( cusps );
        cuspHouseRing.setCusps( cusps );
        houseNumberDial2.setCusps( cusps );
        cuspNumberNeedle.setCusps( cusps );
    }

    public void setCusps(List<Body> cuspList) {
        double [] csp = new double[cuspList.size()];
        for ( int i = 0; i < csp.length; i++ )
            csp[i] = cuspList.get(i).lon;
        setCusps( csp );
    }

    public double [] getCusps() {
        return cusps;
    }
    /**
     * 出生、進行、経過といった情報をセットする。
     */
    public void setRingName(String text) {
        textDial.setText(text);
    }

    public String getRingName() {
        return textDial.getText();
    }

    String name;
    TimePlace timePlace;
    /**
     * 人物名などをセットする。
     */
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    /**
     * 人物の出生時刻、出生地をセットする。
     */
    public void setTimePlace( TimePlace timePlace ) {
        this.timePlace = timePlace;
    }
    public TimePlace getTimePlace() {
        return timePlace;
    }

    int group;
    /**
     * この天体リングがN,P,T,N2,P2などどのグループに属しているかをセットする。
     */
    public void setGroup( int group ) {
        this.group = group;
    }
    /**
     * グループコードを返す。
     */
    public int getGroup() {
        return group;
    }
    /**
     * ドラッグ禁止の感受点(複数)をセットする。
     */
    public void setNotDragBodys(int [] bodys) {
        notDragBodys = bodys;
    }
    /**
     * 指定された天体がドラッグ禁止な場合はtrueを返す。
     */
    public boolean isNotDragBody(int id) {
        if ( notDragBodys == null || notDragBodys.length == 0 ) return true;
        for ( int i : notDragBodys ) {
            if ( i == id ) return true;
        }
        return false;
    }
    public void setHouseBGColors(Color [] colors) {
        houseRing.setBGColors(colors);
    }
    public Color [] getHouseColors() {
        return houseRing.getBGColors();
    }
    public void setHouseInnerLineColor(Color color) {
        houseRing.setInnerLineColor(color);
    }
    public Color getHouseInnerLineColor() {
        return houseRing.getInnerLineColor();
    }
    public void setHouseOuterLineColor(Color color) {
        houseRing.setOuterLineColor(color);
    }
    public Color getHouseOuterLineColor() {
        return houseRing.getOuterLineColor();
    }
    public void setHouseCuspsColor(Color color) {
        houseRing.setCuspColor(color);
    }
    public void setHouseNumberColor(Color color) {
        houseNumberDial.setColor(color);
    }
    public void setHouseNumberColors(Color [] colors) {
        houseNumberDial.setColors( colors );
    }
    public void setHousesGaugeColor(Color color) {
        gaugeDial.setGaugeColor(color);
    }
    public void setBodysBorderColor(Color color) {
        bodysBorderColor = color;
    }
    public void setBodysEffect(int value) {
        bodysEffect = value;
    }
    public HouseRing getHouseRing() {
        return houseRing;
    }
    public void setBodysColor( Color color) {
        bodyColor = color;
    }
    public void setBodysHighLightColor( Color color) {
        bodyHighLightColor = color;
    }
    public void setBodysDegreeColor(Color color) {
        bodyDegreeColor = color;
    }
    public void setOuterHousesNumberColor(Color color) {
        houseNumberDial2.setColor(color);
    }
    public void setOuterCuspsDegreeColor(Color color) {
        cuspNumberNeedle.setColor( color );
    }
    public void setOuterCuspsColor(Color color) {
        cuspHouseRing.setCuspColor(color);
    }
    public void setHousesHighLightColor( Color color ) {
        houseRing.setHighLightHouseColor( color );
    }
    public void setLeadingLineColor( Color color ) {
        markerNeedle.setColor( color );
    }
    public void setNoHousesGauge(boolean b) {
        gaugeDial.setVisible(! b);
    }
    public void setNoHousesBG(boolean b) {
        houseRing.setNoBackground(b);
    }
    /**
     * Natal,Progress,Transitなど円の役割説明分を円弧状に表示するときの文字色
     * @param color
     */
    public void setTextColor( Color color ) {
        textDial.setColor(color);
    }
    public Color getTextColor() {
        return textDial.getColor();
    }
  /*
   * mouseDragged()の動作について
   * マウスカーソルの座標を角度に変換して、そのまま天体の角度とみなして動かすと、
   * 天体の角度とカーソルで指した角度にはズレがあるため、ドラッグの最初で天体の位置
   * がカーソル位置に飛んだように見えてしまう。これをなくすために、カーソルが
   * 円の上で何度ドラッグされたかを求めて、その刻みの値を天体の位置に足していく
   * やり方をしている。つまりカーソルで示された角度を直接天体の描画に使わず、
   * 移動されたオフセット値を天体の位置に足していく。
   *
   */
}
