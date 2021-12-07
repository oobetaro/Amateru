/*
 * SignDial.java
 *
 * Created on 2006/10/30, 2:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;

import static java.lang.Math.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import static java.awt.Color.*;
import java.awt.Stroke;
import static to.tetramorph.starbase.lib.Const.*;
import to.tetramorph.starbase.util.AstroFont;

/**
 * ホロスコープの部品で星座シンボルを描くクラス。
 * @author 大澤義鷹
 */
public class SignDial extends ChartParts {
  Font signFont;
  float signFontSize;
  public Color [] symbolColors = { WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,WHITE,
    WHITE,WHITE,WHITE,WHITE };
  public Color [] borderColors = { BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,BLACK,
    BLACK,BLACK,BLACK,BLACK };
  public boolean isNoSymbolBorders = false;
  private float symbolSize;
  public SignDial() {
    
  }
  /** 
   * サインダイアルオブジェクトを作成する。
   * @param bp BasePositionオブジェクト
   */
  public SignDial(BasePosition bp) {
    super(bp);
  }
  /**
   * 描画条件をセットする
   * @param symbolSize サインのシンボルの直径(0..1)
   */
  public void setSymbolSize(float symbolSize) {
    this.symbolSize = symbolSize;
  }
  //星座シンボルのShapeを保管する。
  private Shape [] signShapes = new Shape[12];

  private static final Stroke boldStroke =
    new BasicStroke(2f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  private static final Stroke solidStroke =
    new BasicStroke(1f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
//  /**
//   * 星座ダイアルを描画する。
//   * diameter,roll+ascendant,bp.x,bpy,signFontSize
//   */
//  public void draw2() {
//    float sz = (float)(symbolSize * bp.w);
//    FontRenderContext render = bp.g.getFontRenderContext();
//    if(signFont == null || sz != signFontSize) {
//      signFontSize = sz;
//      signFont = AstroFont.getFont(signFontSize);
//    }
//    double radius = diameter * bp.w / 2d;
//    AffineTransform mvat = new AffineTransform();
//    AffineTransform roat = new AffineTransform();
//    AffineTransform mvat2 = new AffineTransform();
//    
//    // 星座シンボルを描く
//    for(int i=0; i<12; i++) {
//      double a = -(i * 30 + 15 + 180 - (roll+ascendant));
//      double cv = cos(a * PI/180d);
//      double sv = sin(a * PI/180d);
//      float sx = (float)(cv * radius + bp.x);
//      float sy = (float)(sv * radius + bp.y);
//      //サイン文字のグラフィック表現を得る
//      TextLayout textlayout =
//        new TextLayout(""+ZODIAC_CHARS[i],signFont,render);
////      TextLayout textlayout = textLayouts[i];
//      //そのグラフィック文字の(高さ/2)と(幅/2)を得る
//      float h = textlayout.getAscent()/2f;
//      float w = textlayout.getAdvance()/2f;
//
////      //シンボルの中心が原点に来るように移動させる
////      AffineTransform at = new AffineTransform();
////      at.translate(-w,h); //移動前文字の原点は左下にあるから、左に半分、上に半分うごかせばよい
////      Shape signSymbol = textlayout.getOutline(at);
////      //シンボルを回転
////      AffineTransform at3 = new AffineTransform();
////      at3.rotate((a+90) * PI/180f);
////      signSymbol = at3.createTransformedShape(signSymbol);
//      mvat.setToTranslation(-w,h);
//      Shape signSymbol = textlayout.getOutline(mvat);
//      roat.setToRotation((a+90) * PI/180f);
//      signSymbol = roat.createTransformedShape(signSymbol);
//
//      //シンボルを獣帯円の所定の場所に移動
//      //AffineTransform at2 = new AffineTransform();
//      //at2.translate(sx,sy);
//      //signSymbol = at2.createTransformedShape(signSymbol);
//      mvat2.setToTranslation(sx,sy);
//      signSymbol = mvat2.createTransformedShape(signSymbol);
//      if(! isNoSymbolBorders) {
//        bp.g.setPaint(borderColors[i]);
//        //角を丸める処理を指定しないとトゲトゲが飛び出して美しくない
//        bp.g.setStroke(boldStroke);
//        bp.g.draw(signSymbol); //太い線でサインの輪郭を描き、次に・・・
//      }
//      bp.g.setStroke(solidStroke);
//      bp.g.setPaint(symbolColors[i]);
//      bp.g.fill(signSymbol); //細い線でサインを塗りつぶす。くっきりしたサインが描ける。
//    }    
//  }
  //次のパラタメーに変更があった場合はShapeを作り直す
  double r,bx,by,rad;
  /**
   * 12個のサインのShapeを作成し、signShapes[]に保管
   * radius = diameter * bp.w / 2
   * ra = roll + ascendant
   */
  void createShape(FontRenderContext render,double radius,double ra) {
    AffineTransform mvat = new AffineTransform();
    AffineTransform roat = new AffineTransform();
    AffineTransform mvat2 = new AffineTransform();
    // 星座シンボル作成
    for(int i=0; i<12; i++) {
      double a = -(i * 30 + 15 + 180 - ra);
      double cv = cos(a * PI/180d);
      double sv = sin(a * PI/180d);
      float sx = (float)(cv * radius + bp.x);
      float sy = (float)(sv * radius + bp.y);
      //サイン文字のグラフィック表現を得る
      TextLayout textlayout =
        new TextLayout(""+ZODIAC_CHARS[i],signFont,render);
      //そのグラフィック文字の(高さ/2)と(幅/2)を得る
      float h = textlayout.getAscent()/2f;
      float w = textlayout.getAdvance()/2f;

      mvat.setToTranslation(-w,h);
      Shape signSymbol = textlayout.getOutline(mvat);
      roat.setToRotation((a+90) * PI/180f);
      signSymbol = roat.createTransformedShape(signSymbol);
      mvat2.setToTranslation(sx,sy);
      signSymbol = mvat2.createTransformedShape(signSymbol);
      signShapes[i] = signSymbol;
    }
  }
  /**
   * 星座ダイアルを描画する。
   */
  public void draw() {
    float sz = (float)(symbolSize * bp.w);
    FontRenderContext render = bp.g.getFontRenderContext();
    double ra = roll + ascendant;
    double radius = diameter * bp.w / 2d;
    if(signFont == null || sz != signFontSize) {
      signFontSize = sz;
      signFont = AstroFont.getFont(signFontSize);
      //フォントサイズに変更があった場合はShapeを作り直す
      createShape(render,radius,ra); 
    } else if( bx != bp.x || by != bp.y || r != ra || rad != radius) {
      //中心座標や回転角や直径に変更があった場合も同様
      createShape(render,radius,ra);
    }
    //値をキャッシュし、次回の描画前にパラメター変更をチェック
    bx = bp.x;
    by = bp.y;
    rad = radius;
    r = ra;
    //作成され保管されている星座シンボルのShapeを描画
    for(int i=0; i<signShapes.length; i++) {
      Shape signShape = signShapes[i];
      if(! isNoSymbolBorders) { //サイン縁取り線が必要な場合
        bp.g.setPaint(borderColors[i]);
        bp.g.setStroke(boldStroke);
        bp.g.draw(signShape); //太い線でサインの輪郭を描き、次に・・・
      }
      bp.g.setStroke(solidStroke);
      bp.g.setPaint(symbolColors[i]);
                    //細い線でサインを塗りつぶす。くっきりしたサインが描ける。
      bp.g.fill(signShape);
    }    
  }

  public void setSymbolColors(Color [] colors) {
    this.symbolColors = colors;
  }
  public void setSymbolBorderColors(Color [] colors) {
    this.borderColors = colors;
  }
  public void setNoSymbolBorders(boolean b) {
    isNoSymbolBorders = b;
  }
}
