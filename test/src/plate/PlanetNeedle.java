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
import java.awt.Component;
import java.awt.event.MouseEvent;
import static java.lang.Math.*;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;
import javax.swing.Timer;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープの惑星や感受点を描画する。時計でいえば惑星針。
 * @author 大澤義鷹
 */
public class PlanetNeedle implements MouseListener,MouseMotionListener {
  double x,y;
  double w;
  double asc;
  double d;
  double bodySize;
  double radius;
  Graphics2D g;
  Font bodyFont;
  FontRenderContext render;
  List<Body> bodys;
  float bodyFontSize;
  Shape [] shapeBuf;
  int onCursor = -1; //オンカーソルしたときのオフセット番号を保管
  Body selectedPoint;
  Aspect selectedAspect;
  Component c;
  PlanetActionListener l;
  /** 
   * Creates a new instance of PlanetNeedle
   */
  public PlanetNeedle() {
  }
  public PlanetNeedle(Component c,PlanetActionListener l) {
    this.c = c;
    this.l = l;
    c.addMouseMotionListener(this);
    c.addMouseListener(this);
  }
  /**
   * 惑星がｸﾘｯｸされたときのリスナを登録する。リスナの削除はnullをsetする。
   */
  public void setPlanetActionListener(PlanetActionListener l) {
    this.l = l;
  }
  /**
   * @param x 軌道の中心のx座標
   * @param y 軌道の中心のy座標
   * @param w 基準となる幅(pixcel)
   * @param asc アセンダントの角度
   * @param d 軌道の直径 0..1
   * @param bodySize 天体の描画サイズ 0..1
   * @param bodys 天体
   * @param selectedAspect selected aspects
   */
  public void setFactor(Graphics2D g,double x,double y,double w,double asc,
    double d,double bodySize,List<Body> bodys,Aspect selectedAspect) {
    this.x = x; this.y = y; this.w = w; this.asc = asc; this.d = d;
    this.bodys = bodys; this.bodySize = bodySize;
    this.g = g;
    this.selectedAspect = selectedAspect;
    render = g.getFontRenderContext();
    radius = d * w / 2d;
    float sz = (float)(bodySize * w);
    if(bodyFont == null || sz != bodyFontSize) {
      bodyFontSize = sz;
      bodyFont = AstroFont.getFont(bodyFontSize);          
    }
  }

  /**
   * setFactor()で与えられたパラメターに従って描画する。
   */
  public void draw() {
    // 天体シンボルを描く
    //for(Body sp: bodys) {
    shapeBuf = new Shape[bodys.size()];
    for(int i=0; i < bodys.size(); i++) {
      Body sp = bodys.get(i);
      double a = 0;
      if(dragging && sp.id == draggBodyID) {
        a = -(draggAngle + 180 - asc);
      } else
        a = -(sp.plot + 180 - asc);
      
      double cv = cos( a * PI / 180d);
      double sv = sin( a * PI / 180d);
      double bx = cv * radius + x;
      double by = sv * radius + y;
      if(dragging && sp.id == draggBodyID) {
        g.setColor(Color.RED);
        g.draw(new Line2D.Double(x,y,bx,by));
      }
      //天体のグラフィック表現を得る
      TextLayout textlayout =
        new TextLayout(""+BODY_CHARS[sp.id],bodyFont,render);
      //そのグラフィック文字の(高さ/2)と(幅/2)を得る
      float h = textlayout.getAscent()/2f;
      float w = textlayout.getAdvance()/2f;
      //シンボルの中心が原点に来るように移動させる
      AffineTransform at = new AffineTransform();
      at.translate(-w,h); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
      Shape signSymbol = textlayout.getOutline(at);
      //シンボルを回転
      AffineTransform at3 = new AffineTransform();
      at3.rotate((a+90) * PI/180f);
      signSymbol = at3.createTransformedShape(signSymbol);
      //シンボルを獣帯円の所定の場所に移動
      AffineTransform at2 = new AffineTransform();
      at2.translate(bx,by);
      signSymbol = at2.createTransformedShape(signSymbol);
      boolean b = false;
      if(selectedAspect != null) b = selectedAspect.contains(sp);
      if(onCursor == i || b) g.setPaint(Color.RED);      
      else g.setPaint(Color.BLACK);
      //角を丸める処理を指定しないとトゲトゲが飛び出して美しくない
      g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND));
      g.draw(signSymbol); //太い線でサインの輪郭を描き、次に・・・
      g.setStroke(new BasicStroke(1f)); 
      g.setPaint(Color.WHITE);
      g.fill(signSymbol); //細い線でサインを塗りつぶす。くっきりしたサインが描ける。
      shapeBuf[i] = signSymbol.getBounds2D(); //当たり判定用のボックスを作成
      //textlayout.draw(g,x-w,y+h);
    }
  }
  /**
   * 指定された座標に天体が存在する場合はtrueを返す。ただし前回コールされたとき
   * 指定した座標に天体が存在し、今回存在しない場合もtrueを返す。前回も今回も
   * 指定座標に天体が存在しないときはfalseを返す。アウトカーソルしたとき、呼びだし
   * 元のコンポーネントではリペイントをかけて、赤点灯を消灯させたいのでこのような
   * 仕組みになっている。x,yにはマウスムーブイベントのx,y座標が与えられる事を
   * 前提にしている。
   */
  public boolean contains(int x,int y) {
    if(shapeBuf != null) {
      for(int i=0; i<shapeBuf.length; i++) {
        if(shapeBuf[i].contains((double)x,(double)y)) {
          onCursor = i;
          selectedPoint = bodys.get(i);
          return true;
        }
      }
    }
    if(onCursor >= 0) { //オンカーソルしてたのが離れた瞬間はtrueとみなす。
      onCursor = -1;
      return true;
    }
    selectedPoint = null;
    return false;
  }
  /**
   * contains()メソッドがtrueを返す場合、このメソッドで選択中の天体を取得できる。
   * falseが返される場合は、このメソッドもnullを返す。
   */
  public Body getSelectedBody() {
    return selectedPoint;
  }
  int clickCount = 0;
  public void mouseClicked(MouseEvent e) {
  }
  Timer clickTimer = null;
  boolean clickCheckTime = false;
  //マウスボタンが押された
  public void mousePressed(final MouseEvent e) {
    clickCount++;
    if(contains(e.getX(),e.getY())) {
      draggBodyID = selectedPoint.id;
    }
  }
  //マウスボタンが離れた
  public void mouseReleased(final MouseEvent e) {
    if(clickCount == 1) {
      clickTimer = new Timer(100,new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if(l != null) {
            if(clickCount>=2)
              l.planetDoubleClicked(selectedPoint);
            else
              l.planetClicked(selectedPoint);
          }
          clickCount = 0;
          clickTimer.stop();
        }
      });
      clickTimer.start();
    }
    dragging = false;
    c.repaint();
  }
  //マウスがドラッグされた
  public void mouseDragged(MouseEvent e) {
      dragging = true;
      double zx = e.getX() - this.x;
      double zy = -(e.getY() - this.y);
      draggAngle = trigon(zx,zy);
      c.repaint();
  }
  //マウスが移動された
  public void mouseMoved(MouseEvent e) {
  }
  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }
  public static double trigon(double x,double y) {
    double a = Math.atan(x/y) * 180d / Math.PI;
    if( y<0 ) a -= 180.0;
    if( a<0 ) a += 360.0;
    a = 180 - a -90;
    if(  a<0 ) a += 360;
    return a;
  }
  
  /**
   * zx,zyを原点とし、x,y点との相対角度を求める。
   * @param zx 原点x
   * @param zy 原点y
   * @param x x座標
   * @param y y座標
   * @return 左端を0度として時計まわりに360度までの角度を返す。
   */
  static double trigon(double zx,double zy,double x,double y) {
    x -= zx; y -= zy;
    double a = Math.atan(x/y) * 180d / Math.PI;
    if( y < 0 ) a -= 180.0;
    if( a < 0 ) a += 360.0;
    a = 180 - a - 90;
    if(  a < 0 ) a += 360;
    return a;
  } 
  int draggBodyID = -1;
  boolean dragging = false;
  double draggAngle = 0;
  
}
