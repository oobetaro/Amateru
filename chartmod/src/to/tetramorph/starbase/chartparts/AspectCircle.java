/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.*;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import to.tetramorph.starbase.lib.AngleUtilities;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectStyle;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープのアスペクト円を描く部品。
 * setAspects(List<Aspect> list)で、描画するアスペクト線を登録する。
 * 線の描き方にはダイレクトモードと、非ダイレクトモードがある。
 * ダイレクトモードは多重円のとき、各天体リングの天体間を直接アスペクト線で結ぶ。
 * 非ダイレクトモードは多重円のときもすべてのアスペクト線をアスペクト円内で結ぶ。
 */
public class AspectCircle extends ChartParts {
    /** 
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[SHOW]がtrueならアスペクトを表示。falseなら非表示。
     */
    public static final int SHOW = 0;
    
    /** 
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[TIGHT]がtrueならタイトアスペクトを表示。falseなら非表示。
     */
    public static final int TIGHT = 1;
    
    /** 
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[LOOSE]がtrueならルーズアスペクトを表示。falseなら非表示。
     */
    public static final int LOOSE = 2;
    
    /**
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[CATEGORY1]がtrueなら第1種アスペクト表示。falseなら非表示。
     * 1種とはCONJUNCTION,SEXTILE,SQUARE,TRINE,OPPOSITION。
     */
    public static final int CATEGORY1 = 3;
    
    /**
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[CATEGORY2]がtrueなら第2種アスペクト表示。falseなら非表示。
     * 2種とはSEMI_SEXTILE,SEMI_SQUARE,SESQUIQUADRATE(135),ENCONJUNCT(150)
     */
    public static final int CATEGORY2 = 4;
    
    /**
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[CATEGORY3]がtrueなら第3種アスペクト表示。falseなら非表示。
     * QUINTILE(72),SEMI_QUINTILE(36),BI_QUINTILE(144)
     */
    public static final int CATEGORY3 = 5;
    
    /**
     * setShowAspectCategoryの引数であたえるboolean配列で、
     * mode[CATEGORY4]がtrueなら特殊アスペクト表示。falseなら非表示。
     * PARALLELやその他のアスペクト
     */
    public static final int CATEGORY4 = 6;
    
    List<Aspect> aspects;   //表示するアスペクトのリスト
    //描画モードで、タイト、ルーズ、1種、2種などの表示選択スイッチ
    boolean [] mode = new boolean [] { 
        true,true,true,true,true,true,true };       
    //選択された天体 (この天体と他の天体とのアスペクトを表示するのに使用)
    Body selectedBody;      
    //アスペクト記号のフォントサイズ(直径サイズから動的に決まる)
    float symbolFontSize;  
    Font symbolFont = AstroFont.getFont(10);        //占星術フォント
    //
    boolean [] visibles;   //全アスペクト線の表示/非表示を保管する
    Line2D.Double [] lines; //全アスペクト線の座標を保管する
    //オンカーソルしたアスペクト線の番号(lines[n])を保管する。-1は無しを意味する。
    int onCursor = -1;     
    //アスペクト線が表示されるサークル円のShapeで円内のｵﾝｶｰｿﾙ判定に用
    Shape aspectCircle;
    Aspect selectedAspect; //選択されたアスペクトオブジェクト
    //アスペクト線が選択されたときにｶｰｿﾙ位置に出現する円
    Ellipse2D.Double selectedMarker = new Ellipse2D.Double(0,0,13,13);
    static BasicStroke onCursorStroke = new BasicStroke(2);
    static BasicStroke lineStroke = new BasicStroke(1);
    AspectCircleListener acl;
    //複数のアスペクトの選択状態保管用
    Set<Aspect> markingAspectSet = new HashSet<Aspect>();
    //非表示にするアスペクト保管用
    Set<Aspect> hideAspectSet = new HashSet<Aspect>();
    Line2D line1 = new Line2D.Double();
    Line2D line2 = new Line2D.Double();
    Color bgColor = Color.WHITE;
    
    /**
     * アスペクト円のイベントリスナをセットする。
     */
    public void setAspectCircleListener(AspectCircleListener acl) {
        this.acl = acl;
    }
    
    /**
     * アスペクト円オブジェクトを作成する。
     */
    public AspectCircle() {
    }
    
    /**
     * アスペクト円オブジェクトを作成する。
     * @param bp BasePositionオブジェクト
     */
    public AspectCircle(BasePosition bp) {
        super(bp);
    }
    /**
     * 描画すべきアスペクトのリストをセットする。
     * @param aspects アスペクトのリスト
     */
    public void setAspects( List<Aspect> aspects) {
        this.aspects = aspects;
        this.selectedBody = selectedBody;
        visibles = new boolean[aspects.size()];
        for(int i=0; i<visibles.length; i++) visibles[i] = false;
        lines = new Line2D.Double[ aspects.size() ];
        for(int i=0; i<lines.length; i++) lines[i] = new Line2D.Double();
        this.clearHideAspects();
        this.clearMarkingAspects();
    }
    
    /**
     * セットした天体とのアスペクトのみを表示する。
     * nullをセットすると、setShowAspectCategorys()の指定値にしたがった描画をする。
     */
    public void setSelectedBody(Body body) {
        selectedBody = body;
    }
    
    /**
     * 種類別にアスペクトの表示/非表示を決定するスイッチをセットする。
     * スイッチはboolean配列で指定し、trueなら表示、falseなら非表示。
     * <pre>
     * mode[0] アスペクト線の表示/非表示
     * mode[1] タイトアスペクトを表示
     * mode[2] ルーズアスペクトを表示
     * mode[3] 第一種アスペクトを表示
     * mode[4] 第二種アスペクトを表示
     * mode[5] 第三種アスペクトを表示
     * mode[6] 特殊アスペクト表示(実際は未対応)
     * </pre>
     */
    public void setShowAspectCategorys(boolean [] mode) {
        this.mode = mode;
    }
    
    /**
     * アスペクト円のみを描画する。
     * draw()に一本化したいところなのだが、描画の重ね合わせの都合で、どうしても
     * アスペクト円を塗りつぶすのは先にすませる必要がある。ZodiacPanelの中では、
     * 各種部品が統合され描画されると、アスペクト円を塗りつぶし
     * その他天体やリング枠線などを描画し、アスペクト線は最後に描く。
     */
    public void drawCircle() {
        double dpx = diameter * bp.w;
        double radius = diameter * bp.w / 2d;
        aspectCircle =
            new Ellipse2D.Double(bp.x - radius, bp.y - radius, dpx, dpx);
        if(! isNoBGColor ) {
            bp.g.setPaint( bgColor );
            bp.g.fill( aspectCircle );
        }        
    }
    
    /**
     * アスペクト線を描画する。
     */
    public void draw() {
        if(! mode[SHOW]) return;
        float sz = (float)(diameter * 0.04 * bp.w);
        if(sz != symbolFontSize) {
            symbolFontSize = sz;
            symbolFont = AstroFont.getFont(symbolFontSize);
        }
        if(onCursor >= 0) {
            bp.g.setPaint(Color.LIGHT_GRAY);
            bp.g.fill(selectedMarker);
        }
        double dpx = diameter * bp.w;
        double radius = diameter * bp.w / 2d;
//        aspectCircle =
//            new Ellipse2D.Double(bp.x - radius, bp.y - radius, dpx, dpx);
//        if(! isNoBGColor ) {
//            bp.g.setPaint( bgColor );
//            bp.g.fill( aspectCircle );
//        }
        for(int i=0; i<aspects.size(); i++) {
            Aspect aspect = aspects.get(i);
            //以下につづく条件式で、描画されないアスペクトは漉し取られる
            if(aspect.isNoAspect()) continue;
            if(aspect.tight && ! mode[TIGHT]) continue;
            if(! aspect.tight && ! mode[LOOSE]) continue;
            if( ! mode[CATEGORY3])
                if(contains(aspect.aid,ASPECTS_CATEGORY3)) continue;
            if( ! mode[CATEGORY2])
                if(contains(aspect.aid,ASPECTS_CATEGORY2)) continue;
            if( ! mode[CATEGORY1])
                if(contains(aspect.aid,ASPECTS_CATEGORY1)) continue;
            if ( selectedBody != null )
                if ( ! aspect.contains( selectedBody ) ) continue;
            if ( hideAspectSet.contains( aspect ) ) continue;
            visibles[i] = true; //ここまで落ちてきたアスペクトは描画される
            double c = PI / 180d;
            double a1 = -(aspect.p1.lon + 180d - (roll+ascendant));
            double a2 = -(aspect.p2.lon + 180d - (roll+ascendant));
            double sx,sy,ex,ey,mx,my;
            double radius1 = 0, radius2 = 0;
            //天体ごとに線を引くか、円内におさまるように線を引くか
            if ( planetRings != null && directDrawMode ) {
                radius1 = searchRing( aspect.p1.group ).getDiameter() * bp.w / 2d;
                radius2 = searchRing( aspect.p2.group ).getDiameter() * bp.w / 2d;
//                radius1 = planetRings[ aspect.p1.group ].getDiameter() * bp.w / 2d;
//                radius2 = planetRings[ aspect.p2.group ].getDiameter() * bp.w / 2d;
            } else {
                radius1 = radius2 = radius; //内円に収まるように描く
            }
            sx = cos( a1 * c) * radius1 + bp.x;
            sy = sin( a1 * c) * radius1 + bp.y;
            ex = cos( a2 * c) * radius2 + bp.x;
            ey = sin( a2 * c) * radius2 + bp.y;
            
            AspectStyle styl = getAspectStyle(aspect);
            AspectStyle hs = getHighLightStyle();
            Stroke stroke;
            if(markingAspectSet.contains(aspect)) {
                stroke = hs.getStroke();
                Color col = (onCursor == i) ? hs.getColor() : styl.getColor();
                bp.g.setPaint( col );
            } else if(onCursor == i) { //オンカーソル時の線種と色設定
                stroke = hs.getStroke();
                bp.g.setPaint(hs.getColor());
            } else {
                stroke = styl.getStroke();
                bp.g.setPaint(styl.getColor());
            }
            bp.g.setStroke( stroke);
            
            lines[i].setLine(sx,sy,ex,ey);
            if(selectedBody != null) { // 選択感受点があるときの描画
                //線分の中央に開けるブランクの長さ
                double spaceWidth = bp.w * diameter * 0.05;  
                double [] p = spacer(sx,sy,ex,ey,spaceWidth);
                //アスペクト記号のグラフィック表現を得る
                TextLayout textlayout =
                    new TextLayout(""+ASPECT_CHARS[aspect.aid],
                                      symbolFont,bp.g.getFontRenderContext());
                //そのグラフィック文字の(高さ/2)と(幅/2)を得る
                float h = textlayout.getAscent()/2f;
                float w = textlayout.getAdvance()/2f;
                AffineTransform at = new AffineTransform();
                if(p != null) {
                    line1.setLine( p[0], p[1], p[2], p[3]);
                    line2.setLine( p[4], p[5], p[6], p[7]);
                    bp.g.draw(line1);
                    bp.g.draw(line2);
                    //シンボルの中心が原点に来るように移動させる
                    //移動前文字の原点は左下にあるから、左に半分、
                    //上に半分うごかせばよい
                    at.translate(-w+p[8],h+p[9]); 
                    bp.g.fill(textlayout.getOutline(at));
                } else { //この場合ｺﾝｼﾞｬｸｼｮﾝ
                    bp.g.draw(lines[i]);
                    Body sp = aspect.getOther(selectedBody);
                    if(sp != null) {
                        bp.g.setStroke(stroke);
                        double a = -(sp.plot + 180 - (roll+ascendant)) * PI / 180d;
                        double conjRadius = (diameter - 0.06) * bp.w / 2d;
                        double bx = cos(a) * conjRadius + bp.x;
                        double by = sin(a) * conjRadius + bp.y;
        //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
                        at.translate(-w+bx,h+by); 
                        bp.g.fill(textlayout.getOutline(at));
                    }
                }
            } else { // 選択感受点が無いときは全アスペクトを描画
                bp.g.draw(lines[i]);
            }
        }
    }
    // apectsの配列中にaidが存在すればtrueを返す。
    private boolean contains(int aid,int [] aspects) {
        for(int i=0; i < aspects.length; i++)
            if(aspects[i] == aid) return true;
        return false;
    }
    /**
     * planetRings[]の中からgroupで指定されたコードを持つPlanetRingを返す。
     */
    private PlanetRing searchRing( int group ) {
        for ( int i=0; i < planetRings.length; i++ ) {
            if ( planetRings[i].getGroup() == group )
                return planetRings[i];
        }
        throw new IllegalArgumentException(
            "PlanetRingを検出できない group = " + group);
    }
    /**
     * 入力された座標と、描画されているアスペクト線がヒットする場合はtrueを返す。
     * このメソッドがtrueを返すときはgetSelectedAspect()は、選択されたアスペクト
     * を返す。falseのときはnullを返す。
     */
    public boolean contains(int x,int y) {
        if(onCursor >= 0)
            if(selectedMarker.contains(x,y)) return true;
        if(aspects == null) return false;
        if ( planetRings != null ) {
            for (PlanetRing ring : planetRings) {
                if ( ring.contains(x, y) ) return false;
            }
        }
        for(int i=0; i<aspects.size(); i++) {
            if(visibles[i]) {
                if(hideAspectSet.contains(aspects.get(i))) continue;
                Line2D.Double line = lines[i];
                if(contains(line.x1, line.y1, line.x2, line.y2,x,y)) {
                    onCursor = i;
                    selectedAspect = aspects.get(onCursor);
                    selectedMarker.x = x - 6;
                    selectedMarker.y = y - 6;
                    return true;
                }
            }
        }
        onCursor = -1;
        selectedAspect = null;
        return false;
    }
    
    /**
     * 点(x,y)がこのアスペクトサークルの中に存在するならtrueを返す。
     */
    public boolean isContainCircle(int x,int y) {
        return aspectCircle.contains(x,y);
    }
    
    /**
     * contain()がtrueを返すとき、このメソッドは選択されているアスペクトを返す。
     * どのアスペクトも選択されていないときはnullを返す。
     */
    public Aspect getSelectedAspect() {
        return selectedAspect;
    }
    
    /**
     * 表示中のアスペクトの中でマーキングするものを登録する。
     * ただしすでに登録したものを再登録したときはそのアスペクトの登録は解除される。
     */
    public void registMarkingAspect(Aspect aspect) {
        if(markingAspectSet.contains(aspect))
            markingAspectSet.remove(aspect);
        else
            markingAspectSet.add(aspect);
    }
    
    /**
     * アスペクトの全マーキングを解除する。
     */
    public void clearMarkingAspects() {
        markingAspectSet.clear();
    }
    
    /**
     * マーキングしたアスペクトを非可視化する。
     */
    public void hideMarkingAspects() {
        for(Iterator ite = markingAspectSet.iterator(); ite.hasNext(); ) {
            Aspect a = (Aspect)ite.next();
            hideAspectSet.add(a);
        }
        markingAspectSet.clear();
    }
    
    /**
     * マーキングされていないアスペクトを非可視にする。
     */
    public void hideUnmarkingAspects() {
        for(Aspect a : aspects) {
            if( ! markingAspectSet.contains(a)) hideAspectSet.add(a);
        }
    }
    
    /**
     * 非可視化されているアスペクトをすべて可視化する。
     */
    public void clearHideAspects() {
        hideAspectSet.clear();
    }
    
    /**
     * 真ん中にスペーサを開けた線の座標を返す。
     * 引数x1,y1,x2,y2は線の座標。sizeは中心に開ける空白の長さ(ブランクライン)。
     * 戻り値は10個の座標値
     * [0] x1
     * [1] y1
     * [2] x1から最初のブランク地点x
     * [3] y1から最初のブランク地点y
     * [4] ブランク地点の終点x
     * [5] ブランク地点の終点y
     * [6] x2
     * [7] y2
     * [8] x1,y1,x2,y2の中心点のx
     * [9] x1,y1,x2,y2の中心点のy
     * 真ん中にﾌﾞﾗﾝｸをあけることができないほど短い線の場合(線の長さ < size)のとき
     * はnullを返す。
     */
    static double [] spacer(double x1,double y1,
                              double x2,double y2,double size) {
        double [] res = new double[10];
        res[0] = x1; res[1] = y1;
        res[6] = x2; res[7] = y2;
        double dx = Math.abs(x1-x2); //Δx
        double dy = Math.abs(y1-y2); //Δy
        double len = Math.sqrt(dx * dx + dy * dy); //線の長さ
        if(len < size) return null;
        //x1,y1を中心点とした線の角度
        double a = Math.toRadians( AngleUtilities.trigon( x1, y1, x2, y2) );
        // * Math.PI / 180d;
        double cv = Math.cos(a);
        double sv = Math.sin(a);
        //x1,y1から見たブランクラインの始点までの長さ
        double r = (len - size) / 2d;
        res[2] = cv * r + x1;
        res[3] = sv * r + y1;
        r += size; //x1,y1からみたブランクライン終点までの長さ
        res[4] = cv * r + x1;
        res[5] = sv * r + y1;
        res[8] = min(x1,x2) + dx / 2d;
        res[9] = min(y1,y2) + dy / 2d;
        return res;
    }
    
    /**
     * 線と点の衝突判定。線分(x0,y0,x1,y1)に点(x2,y2)が接触する場合はtrueを返す。
     * 線分の長さ L1 = sqrt( (x1-x0)^2 + (y1-y0)^2 )
     * 線分の始点から点までの長さ L2 = sqrt( (x2-x0)^2 + (y2-y0)^2 )
     * (x1-x0)*(x2-x0) + (y1-y0)*(y2-y0) が L1*L2 に等しく、かつL1≧L2の時衝突している
     * x0,y0,x1,y1 線の座標を指定。x2,y2 点の座標を指定。
     */
    static boolean contains(double x0,double y0,
                              double x1,double y1,double x2,double y2) {
        double dx1 = x1-x0;
        double dy1 = y1-y0;
        double dx2 = x2 - x0;
        double dy2 = y2 - y0;
        double l1 = Math.sqrt(dx1*dx1 + dy1*dy1 );
        double l2 = Math.sqrt(dx2*dx2 + dy2*dy2 );
        return (Math.abs((dx1*dx2 + dy1*dy2) - l1*l2) <= 2 && l1 >= l2);
    }
    
    
    AspectStyle [] aspectStyles;
    AspectStyle defStyle =
        new AspectStyle(CONJUNCTION,true,Color.BLACK,AspectStyle.SOLID_LINE);
    AspectStyle defStyle2 =
        new AspectStyle(CONJUNCTION,true,Color.RED,AspectStyle.BOLD_LINE);
    
    /**
     * アスペクトスタイルを設定する。
     */
    public void setAspectStyles( AspectStyle [] styles) {
        aspectStyles = styles;
    }
    
    //指定されたアスペクトに該当するアスペクトスタイルを返す。
    private AspectStyle getAspectStyle(Aspect a) {
        if(aspectStyles == null) return defStyle;
        int offset = a.tight ? 1 : 13;
        for(int i=0; i<12; i++) {
            AspectStyle style = aspectStyles[ i + offset ];
            if(style.getAspectID() == a.aid) return style;
        }
        return defStyle;
    }
    
    //ハイライトのアスペクトスタイルを返す
    private AspectStyle getHighLightStyle() {
        if(aspectStyles == null) return defStyle2;
        return aspectStyles[0];
    }
    
    /**
     * アスペクト円の背景色をセットする。
     */
    public void setBGColor(Color color) {
        bgColor = color;
    }
    
    boolean isNoBGColor = true;
    /**
     * アスペクト円を背景色で塗りつぶしたくないときはtrueをセットする。
     * 塗りつぶすならfalseを指定する。
     */
    public void setNoBGColor(boolean b) {
        isNoBGColor = b;
    }
    
    //多重円のPlanetRing配列保管用
    PlanetRing [] planetRings;
    //天体間の線での結び方を選択するスイッチ
    boolean directDrawMode = false;
    
    /**
     * 多重円の状況で円内ではなく天体ごとに線を引く場合に必要になる。
     * PlanetRingのgetDiameter()を使用する。
     */
    public void setPlanetRings(PlanetRing [] planetRings) {
        this.planetRings = planetRings;
    }
    
    /**
     * アスペクト線のダイレクト描画モードを設定する。
     * @param b trueを設定すると、多重円のとき各天体の内径位置同士をアスペクト線
     *          で結ぶ。falseのときは、アスペクト円内にすべてのアスペクト線を描く。
     */
    public void setDirectLineMode(boolean b) {
        this.directDrawMode = b;
    }
    
    /**
     * このアスペクト円がダイレクト描画モードのときtrueを返す。
     */
    public boolean isDirectLineMode() {
        return directDrawMode;
    }
}
