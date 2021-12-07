/*
 * PlanetNeedle.java
 *
 * Created on 2006/10/31, 3:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartplate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.*;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import to.tetramorph.starbase.*;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.AspectStylist;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープのアスペクト円を描くのに使う。
 */
public class AspectCircle {
  /** mode[SHOW]がtrueならアスペクトを表示。falseなら非表示。*/
  public static final int SHOW = 0;
  /** mode[TIGHT]がtrueならタイトアスペクトを表示。falseならタイトアスペクト非表示。*/
  public static final int TIGHT = 1;
  /** mode[LOOSE]がtrueならルーズアスペクトを表示。falseならルーズアスペクト非表示。*/
  public static final int LOOSE = 2;
  /** 
   * mode[CATEGORY1]がtrueなら第1種アスペクト表示。falseなら非表示。
   * 1種とはCONJUNCTION,SEXTILE,SQUARE,TRINE,OPPOSITION。
   */
  public static final int CATEGORY1 = 3;
  /** 
   * mode[CATEGORY2]がtrueなら第2種アスペクト表示。falseなら非表示。
   * 2種とはSEMI_SEXTILE,SEMI_SQUARE,SESQUIQUADRATE(135),ENCONJUNCT(150)
   */
  public static final int CATEGORY2 = 4;
  /** 
   * mode[CATEGORY3]がtrueなら第3種アスペクト表示。falseなら非表示。
   * QUINTILE(72),SEMI_QUINTILE(36),BI_QUINTILE(144)
   */
  public static final int CATEGORY3 = 5;
  /** 
   * mode[CATEGORY4]がtrueなら特殊アスペクト表示。falseなら非表示。
   * PARALLELやその他のアスペクト
   */
  public static final int CATEGORY4 = 6;
  double x,y;
  double w;
  double asc;
  double d;
  double radius;
  Graphics2D g;
  List<Aspect> aspects;   //表示するアスペクトのリスト
  boolean [] mode = new boolean [] { true,true,true,true,true,true,true };       //描画モードで、タイト、ルーズ、1種、2種などの表示選択スイッチ
  AspectStylist aspectStylist = new AspectStylist(); //線のスタイルやカラー情報を管理する
  Body selectedPoint;   //選択された天体
  double spaceWidth;     //線分の中央に開けるブランクの長さ
  float symbolFontSize;  //アスペクト記号のフォントサイズ
  Font symbolFont;        //占星術フォント
  FontRenderContext render; //フォント描画に必要
  //
  boolean [] visibles;   //アスペクト線の表示/非表示を保管する
  Line2D.Double [] lines; //アスペクト線の座標を保管する
  int onCursor = -1;     //オンカーソルしたアスペクト線のオフセットを保管する
  Shape aspectCircle;    //アスペクト線が表示されるサークル円のShape
  double conjRadius;    //コンジャクション記号を出す位置
  Aspect selectedAspect; //選択されたアスペクトオブジェクト
  Ellipse2D.Double selectedCircle;  //アスペクト線が選択されたときにｶｰｿﾙ位置に出現する円
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public AspectCircle() {
  }
  public void setGraphics2D(Graphics2D g) {
    this.g = g;
  }
  double cx,cy,W,dia;
  /** 
   * 描画する基準とする座標をセットする。
   * @param cx 中心座標x
   * @param cy 中心座標y
   * @param W 基準となる描画エリアの幅(つねに正方形とみなす)
   */
  public void setPos(double cx, double cy, double W) {
    this.cx = cx; this.cy = cy; this.W = W;
  }
  public void setSize(double dia) {
    this.dia = dia;
  }
  public void setFactor(double asc,List<Aspect> aspects,boolean [] mode,Body selectedPoint,double symbolSize) {
    setFactor(g,cx,cy,dia,asc,dia,aspects,mode,selectedPoint,symbolSize);
  }
  /**
   * @param x 軌道の中心のx座標
   * @param y 軌道の中心のy座標
   * @param w 基準となる幅(pixcel)
   * @param asc アセンダントの角度
   * @param d アスペクト円の直径 0..1
   * @param aspects アスペクトのリスト
   * @param mode 要素数7のboolean配列。
   * @param selectedPoint 選択中の感受点。これが非nullならこの天体と他の天体とのアスペクトのみを表示。
   * @param symbolSize ｱｽﾍﾟｸﾄ記号のｻｲｽﾞ(0..1)
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double d,List<Aspect> aspects,boolean [] mode,Body selectedPoint,double symbolSize) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.aspects = aspects; this.g = g;
    this.mode = mode;
    this.selectedPoint = selectedPoint;
    radius = d * w / 2d;
    conjRadius = (d - 0.06) * w / 2d;
    spaceWidth = w * 0.04;
    float sz = (float)(symbolSize * w);
    if(symbolFont == null || sz != symbolFontSize) {
      symbolFontSize = sz;
      symbolFont = AstroFont.getFont(symbolFontSize);          
    }    
    render = g.getFontRenderContext();
    visibles = new boolean[aspects.size()];
    for(int i=0; i<visibles.length; i++) visibles[i] = false;
    lines = new Line2D.Double[ aspects.size() ];
    aspectCircle = new Ellipse2D.Double(x-radius,y-radius,d * w,d * w);
  }
  /**
   * アスペクト線を描画する。
   */
  public void draw() {
    if(! mode[SHOW]) return;
    if(onCursor >= 0) {
      g.setPaint(Color.LIGHT_GRAY);
      g.fill(selectedCircle);
    }
    //表示しているアスペクトを識別するためのboolean配列を初期化(すべて非表示に)
    for(int i=0; i<visibles.length; i++) visibles[i] = false;
    for(int i=0; i<aspects.size(); i++) {
      Aspect aspect = aspects.get(i);
      //以下につづく条件式で、描画されないアスペクトは漉し取られる
      if(aspect.isNoAspect()) continue;
      if(aspect.tight && ! mode[TIGHT]) continue;
      if(! aspect.tight && ! mode[LOOSE]) continue;
      if( ! mode[CATEGORY3]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY3)) continue;
      }
      if( ! mode[CATEGORY2]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY2)) continue;
      }
      if( ! mode[CATEGORY1]) {
        if(contains(aspect.aid,ASPECTS_CATEGORY1)) continue;
      }
      if(selectedPoint != null) {
        if(! aspect.contains(selectedPoint)) continue;
      }
      visibles[i] = true; //ここまで落ちてきたアスペクトは描画される
      double c = PI/ 180d;
      double a1 = -(aspect.p1.lon + 180d - asc);
      double cv1 = cos( a1 * c);
      double sv1 = sin( a1 * c);
      double a2 = -(aspect.p2.lon + 180d - asc);
      double cv2 = cos( a2 * c);
      double sv2 = sin( a2 * c);
      double sx,sy,ex,ey,mx,my;
      sx = cv1 * radius + x;
      sy = sv1 * radius + y;
      ex = cv2 * radius + x;
      ey = sv2 * radius + y;
      if(onCursor == i) g.setPaint( Color.RED );
      else g.setPaint( aspectStylist.getColor(aspect) );
      Stroke stroke = (onCursor == i) ? 
        new BasicStroke(2f) : aspectStylist.getStroke(aspect);
      g.setStroke( stroke);
      lines[i] = new Line2D.Double(sx,sy,ex,ey);
      if(selectedPoint != null) {
        // 選択感受点があるときの描画
        double [] p = spacer(sx,sy,ex,ey,spaceWidth);
        //アスペクト記号のグラフィック表現を得る
        TextLayout textlayout =
          new TextLayout(""+ASPECT_CHARS[aspect.aid],symbolFont,render);
        //そのグラフィック文字の(高さ/2)と(幅/2)を得る
        float h = textlayout.getAscent()/2f;
        float w = textlayout.getAdvance()/2f;
        AffineTransform at = new AffineTransform();
        if(p != null) {
          Line2D line1 = new Line2D.Double(p[0],p[1],p[2],p[3]);
          Line2D line2 = new Line2D.Double(p[4],p[5],p[6],p[7]);
          g.draw(line1);
          g.draw(line2);
          g.setStroke(stroke);
          //シンボルの中心が原点に来るように移動させる
          at.translate(-w+p[8],h+p[9]); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
          g.fill(textlayout.getOutline(at));
        } else { //この場合ｺﾝｼﾞｬｸｼｮﾝ
          g.draw(lines[i]);
          Body sp = aspect.getOther(selectedPoint);
          if(sp != null) {
            g.setStroke(stroke);
            double a = -(sp.plot + 180 - asc) * PI / 180d;
            double bx = cos(a) * conjRadius + x;
            double by = sin(a) * conjRadius + y;
            at.translate(-w+bx,h+by); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
            //Paint paint = g.getPaint();
            //g.setPaint(Color.LIGHT_GRAY);
            //g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            //g.draw(textlayout.getOutline(at));
            g.setStroke(stroke);
            //g.setPaint(paint);
            g.fill(textlayout.getOutline(at));
          }
        }
      } else {
        // 選択感受点が無いときの描画
        //Line2D line = new Line2D.Double(sx,sy,ex,ey);
        g.draw(lines[i]);
      }
    }
  } 
  // apectsの配列中にaidが存在すればtrueを返す。
  private boolean contains(int aid,int [] aspects) {
    for(int i=0; i<aspects.length; i++) {
      if(aspects[i] == aid) return true;
    }
    return false;
  }
  /**
   * 入力された座標と、描画されているアスペクト線がヒットする場合はtrueを返す。
   */
  public boolean contains(int x,int y) {
    if(onCursor >= 0) {
      if(selectedCircle.contains(x,y)) return true;
    }
    if(aspects == null) return false;
    for(int i=0; i<aspects.size(); i++) {
      if(visibles[i]) {
        Line2D.Double line = lines[i];
        if(contains(line.x1,line.y1,line.x2,line.y2,x,y)) {
          onCursor = i;
          selectedAspect = aspects.get(onCursor);
          selectedCircle = new Ellipse2D.Double(x-7,y-7,15,15);
          return true;
        }
      }
    }
    if(onCursor >= 0) { //オンカーソルしてたのが離れた瞬間はtrueとみなす。
      onCursor = -1;
      return true;
    }
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
   * 選択されているアスペクトを返す。どのアスペクトも選択されていないときはnull
   * を返す。
   */
  public Aspect getSelectedAspect() {
    return selectedAspect;
    //if(onCursor < 0) return null;
    //return aspects.get(onCursor);    
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
  static double [] spacer(double x1,double y1,double x2,double y2,double size) {
    double [] res = new double[10];
    res[0] = x1; res[1] = y1;
    res[6] = x2; res[7] = y2;
    double dx = Math.abs(x1-x2); //Δx
    double dy = Math.abs(y1-y2); //Δy
    double len = Math.sqrt(dx * dx + dy * dy); //線の長さ
    if(len < size) return null;
    double a = trigon(x1,y1,x2,y2) * Math.PI / 180d; //x1,y1を中心点とした線の角度
    double cv = Math.cos(a);
    double sv = Math.sin(a);
    double r = (len - size) / 2d; //x1,y1から見たブランクラインの始点までの長さ
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
   *原点から指定点が何度の角度にあるかを返す。
   *右を０度として、反時計回りに360度までの値を返す。
   *数学、三角関数でよく使われる座標系。
   */
  static double trigon(double x,double y) {
    double a = Math.atan(x/y)*180d/Math.PI;
    if( y<0 ) a -= 180.0;
    if( a<0 ) a += 360.0;
    a = 180 - a -90;
    if(  a<0 ) a += 360;
    return a;
  }
  /**
   *zx,zyを原点とし、x,y点との相対角度を求める。
   */
  static double trigon(double zx,double zy,double x,double y) {
    x -= zx; y -= zy;
    return trigon(x,y);
  }
  /**
   * 線と点の衝突判定。
   * 線分の長さ L1 = sqrt( (x1-x0)^2 + (y1-y0)^2 )
   * 線分の始点から点までの長さ L2 = sqrt( (x2-x0)^2 + (y2-y0)^2 )
   * (x1-x0)*(x2-x0) + (y1-y0)*(y2-y0) が L1*L2 に等しく、かつL1≧L2の時衝突している
   * @param x0,y0,x1,y1 線の座標
   * @param x2,y2 点の座標。
   */
  static boolean contains(double x0,double y0,double x1,double y1,double x2,double y2) {
    double dx1 = x1-x0;
    double dy1 = y1-y0;
    double dx2 = x2 - x0;
    double dy2 = y2 - y0;
    double l1 = Math.sqrt(dx1*dx1 + dy1*dy1 );
    double l2 = Math.sqrt(dx2*dx2 + dy2*dy2 );
    return (Math.abs((dx1*dx2 + dy1*dy2) - l1*l2) <= 2 && l1 >= l2);
  }
}
