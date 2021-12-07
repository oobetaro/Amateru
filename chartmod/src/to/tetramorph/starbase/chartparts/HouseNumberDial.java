/*
 * HouseNumberDial.java
 *
 * Created on 2006/11/03, 2:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.chartparts;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import static java.lang.Math.*;
import java.util.Arrays;
import to.tetramorph.starbase.lib.Const;
/**
 * ハウス番号を表示する。
 * カスプとカスプの中間の位置(角度)にハウス番号を描く。
 * ハウス番号の文字サイズはRingSizeの値に応じて変化する。このオブジェクトは、
 * 幅をもっていないが、PlanetRingの幅がこのオブジェクトに渡されるため、
 * 便宜上そのような作りになっている。
 * @author 大澤義鷹
 */
public class HouseNumberDial extends ChartParts {

  private double [] cusps;
  private double [] houses = new double[12];
  private Color [] numberColors = new Color[12];
  private Font font = new Font("Times",Font.BOLD,10);
  private float fontSize;   //サイズポイント
  private double ringWidth;
  
  public HouseNumberDial() {
    Arrays.fill( numberColors,Color.GRAY );
  }
  /**
   * ハウスダイヤルオブジェクトを作成する。
   */
  public HouseNumberDial(BasePosition bp) {
    super(bp);
  }

  /**
   * リングの幅をセットする。この値は文字フォントのｻｲｽﾞ決定に使われるのみ。
   */
  public void setRingWidth(double ringWidth) {
    this.ringWidth = ringWidth;
  }
  
  public double getRingWidth() {
    return ringWidth;
  }
  /**
   * ハウスカスプを指定する。
   * @param cusps 1室カスプを[0]として12室分のハウスカスプを指定する。
   */
  public void setCusps(double [] cusps) {
    this.cusps = cusps;
    if(cusps.length != houses.length )
      houses = new double[cusps.length];
    for(int i=0; i<cusps.length; i++) {
      double nextCusp = (i == (cusps.length - 1)) ? cusps[0] : cusps[i+1];
      double arcAngle = (nextCusp > cusps[i]) ? 
        abs(nextCusp - cusps[i]) : abs(360 + nextCusp - cusps[i]);
      houses[i] = cusps[i] + arcAngle/2d;
    }
  }
//  public double [] getCusps() {
//    return cusps;
//  }
  // 現在のﾘﾝｸﾞの幅と直径から文字のｻｲｽﾞを返す
  float symsize() {
    double h = ringWidth * bp.w * 0.35; //0.4;
    double w = diameter * bp.w * PI / 50 * 0.8; //48 * 0.8; //直径の48分の1。48は適当に決めた値。
    return (float) Math.min(h,w); //小さい方のサイズを採用する。
  }
  /**
   * このオブジェクトにセットされているパラメターに従って、ハウス番号を描画する。
   */
  public void draw() {
    float sz = symsize();
    if(sz != fontSize) {
      fontSize = sz;
      font = font.deriveFont(fontSize);
    }
    FontRenderContext render = bp.g.getFontRenderContext();
    AffineTransform mvat = new AffineTransform();
    AffineTransform roat = new AffineTransform();
    
    double radius = (diameter + diameterOffset) * bp.w / 2d;
    for(int i=0; i<houses.length; i++) {
      double angle = houses[i];
      //NumberNeedleクラスでは+180していないが、このクラスではしている。
      //なんでかというとsin,cosで回転させるか、rotateメソッドで回転させるかの違いで、
      //+180するのはsin,cosの計算の都合上のこと。
      double a = -( angle + 180 - ( roll + ascendant ));
      double bx = cos( a * PI / 180d) * radius + bp.x;
      double by = sin( a * PI / 180d) * radius + bp.y;
      TextLayout textlayout = 
        new TextLayout( Const.HOUSE_NUMBERS[i], font, render);
      float txh = textlayout.getAscent();
      float txw = textlayout.getAdvance();
      Shape strShape = null;
      //文字列の中心を基準に回転
      roat.setToRotation((a + 90) * PI / 180f, txw / 2f, -txh / 2f);
      strShape = textlayout.getOutline(roat);
      
//      AffineTransform at = new AffineTransform();
//      strShape = textlayout.getOutline(at);
//      at.rotate( (a + 90) * PI / 180f, txw / 2f, -txh / 2f );
//      strShape = at.createTransformedShape( strShape );
//      AffineTransform at2 = new AffineTransform();
//      at2.translate( bx - txw / 2f, by + txh / 2f );
//      strShape = at2.createTransformedShape( strShape );
      mvat.setToTranslation( bx - txw / 2f, by + txh / 2f );
      strShape = mvat.createTransformedShape( strShape );
      //カスプ数と色配列の数が合わない場合、先頭の色に戻す
      Color c = numberColors[ i % numberColors.length ];
      if( c != null) {
        bp.g.setPaint( c );
        bp.g.fill(strShape);
      }
    }
  }
  /**
   * すべてのハウス番号に同じ色を設定する。
   */
  public void setColor(Color color) {
    Arrays.fill( numberColors,color);
  }
  /**
   * 1室から個々の色を設定する。
   * 指定したcolorsの要素数と、与えたカスプの数が異なり、色数が足りなくなったとき
   * は、また色配列の先頭に戻って配色する。
   * 配列要素中にnullがある場合、そのハウス番号は表示しない。
   * @exception IllegalArgumentException nullや要素数0の配列が指定された
   */
  public void setColors(Color [] colors) {
    if(colors == null || colors.length == 0)
      throw new IllegalArgumentException("null禁止");
    if(colors.length != numberColors.length)
      numberColors = new Color[ colors.length ];
    System.arraycopy( colors, 0, numberColors, 0, colors.length );
  }
}
