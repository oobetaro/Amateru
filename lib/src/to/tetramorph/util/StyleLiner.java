/*
 *
 */
package to.tetramorph.util;
import java.awt.*;
import to.tetramorph.util.*;

/**
 * java.awt.Graphicsでは、点線や一点鎖線、太線を描く事ができないが、このクラスは
 * それを実現する。
 * 描画速度は若干遅くなるが、このクラスを使うと、色々な線を描く事ができる。
 * ラインスタイルの指定。太線と細線の指定。製図の寸法線のように、線の
 * 中央にスペースを空ける。また線の片端ないしは、両端に矢印をつける。
 * などの機能がある。
 *<p>
 *作者：大辺タロー　作成日：98年10月29日
 */
public class StyleLiner {
  
  /**
   *コンストラクタで引き継がれた、Graphics gの写し。つまり親元のgで、setColor()
   *などを実行すれは、このクラスで描画される色も変化する。
   *初期のオプション設定値は、{ 線幅=plean,矢印=無し,lineStyle=実線,スペーサ=無し}である。
   */
  
  public Graphics g;
  
  /**
   *描画した線のスペーサ位置のＸ座標が入る。線を描くたびに変化する。
   */
  
  public double midXpos=0;
  
  /**
   *描画した線のスペーサ位置のＹ座標が入る。線を描くたびに変化する。
   */
  
  public double midYpos=0;
  
  /**
   *描画した線の長さが入る。線を描くたびに変化する。
   */
  public double lineLength;
  
  private double styleCount;
  private int lineStyle=0xFFFFFFFF;		//ラインスタイルを16進数で保存する。
  private String penType;					//ペンタイプ。plean,boldの二種。
  private double scale;					//スタイルの拡大率
  private int sx,sy;
  
  private boolean spacerActive = false;	//スペーサ機能をアクティブに。
  private double lineMidPoint = 0.5;		//スペーサ設定位置。0.5=中央。
  private double spaceLength = 15.0;		//スペーサの長さ。(±)
  private int midLength = 0;				//始点からlineMidPointまでの長さ。
  
  private boolean drawStartArrow = false;
  private boolean drawEndArrow = false;
  private boolean arrowBold = false;
  private	int arrowLength=10;
  private	int arrowAngle =15;
  
  /**
   *描画対象である、Graphicsクラスを入れてインスタンス化し、
   *以後、描画対象はここで指定されたものとなる。
   */
  
  public StyleLiner(Graphics gg) {
    this.g = gg;
    this.styleCount = 0;
    this.penType = "plean";
    this.scale = 1;
    sx = sy =0;
  }
  
  /**
   *trueを指定すると、線の描画の際、矢印を始点に描くようになる。
   */
  public void activeStartArrow(boolean sw) {
    drawStartArrow = sw;
  }
  /**
   *trueを指定すると、線の描画の際、矢印を終点に描くようになる。
   */
  public void activeEndArrow(boolean sw) {
    drawEndArrow   = sw;
  }
  /**
   *trueを指定すると、描画の際、矢印を塗りつぶす。
   *falseなら製図の矢印のように２本の線が描かれる。
   *boldの線が指定されていると、矢印の頭を線端が突き破ってしまうため、
   *このオプションが指定されているときは、arrowLengthを元に、線の両端を
   *若干短く描画したのち、矢印を描くように処理している。
   */
  public void setArrowBold(boolean sw) {
    arrowBold = sw;
  }
  /**
   *矢印線（＜）の長さを設定する。デフォルトは15dot。
   */
  public void setArrowLength(int length) {
    arrowLength = length;
  }
  /**
   *矢印線（＜）の開きの角度を設定する。デフォルトは±15度。
   */
  public void setArrowAngle(int angle) {
    arrowAngle = angle;
  }
  /**
   *線の中央にスペーサを入れる。つまり、製図の寸法線のように、
   *<------　　------>このように真ん中に数値を描きこむような領域を開けて
   *描画する。spaceLength(単位はdot)は、スペースの長さを指定し、この値は中央から±の
   *範囲に影響を与える。スペーサの位置は中央に設定される。
   *描画後、midXpos,midYposに、スペーサの中心座標が求まる。
   */
  
  public void setSpacer(int spaceLength) {
    setSpacer(0.5,spaceLength);
  }
  
  /**
   *線の中央にスペーサを入れる。つまり、製図の寸法線のように、
   *<------　　------>このように真ん中に数値を描きこむような領域を開けて
   *描画する。spaceLengthは、スペースの長さを指定し、この値は中央から±の
   *範囲に影響を与える。spacePosは、線分のどこにスペーサを設けるかを指定
   *する。0.5であれば、中央に設ける。範囲は(0 < spacePos < 1)でなければならない。
   *範囲外の数値を与えると、0.5にリセットされる。
   *drawLine()で、最初に指定した座標が始点、次が終点で、spacePosの値が0.3
   *であれば、始点寄りにスペースが空けられる事になる。
   *描画後、midXpos,midYposに、スペーサの中心座標が求まる。
   */
  
  public void setSpacer(double spacePos,int spaceLength ) {
    if( spacePos >= 1 || spacePos < 0) lineMidPoint = 0.5;
    else lineMidPoint = spacePos;
    this.spaceLength = spaceLength;
    spacerActive = true;
  }
  
  /**
   *スペーサ機能を無効にする。
   */
  
  public void voidSpacer() {
    spacerActive = false;
  }
  
  /**
   *ボールドタイプの線種であると設定する。点は3*3ドットで、中心のドットが指定された座標である。
   *この指定をした時は、矢印のboldオプションも選択された事になる。
   */
  
  public void setBold() {
    this.penType = "bold";
    setArrowBold(true);
  }
  
  /**
   *プレーンタイプの線種であると設定する。線幅1dotの線を描く。
   *この指定をした時は、矢印のboldオプションはfalseに設定される。
   */
  
  public void setPlean() {
    this.penType = "plean";
    setArrowBold(false);
  }
  
  /**
   *ラインスタイルを設定する。int型のビット情報が即、描画スタイルとなる。
   *各ビットが1ならば描画、0なら描画しない。点線なら、0x55555555というように指定する。
   *デフォルトでは、0xFFFFFFFFで、スタイルはない。値が０ならなにも描画されない。
   */
  
  public void setLineStyle(int style) {
    this.lineStyle = style;
  }
  
  /**
   *ラインスタイルはデフォルトでは、１ドットきざみでlineStyleのビット情報が反映されるが、
   *この値を設定することで、もっと間延びさせる事ができる。(設定値 >= 1)でなければならず、
   *1以下の値を設定しても、1にリセットされる。初期値は1である。2を指定すれば、２倍に間延
   *びする。つまり、"- - -"が、"−　−　−"となる。
   */
  public void setScale(double scale) {
    if(scale<1) this.scale = 1;
    else this.scale = scale;
  }
  
  /**
   *線を描く。x1,y1が始点。x2,y2が終点。どのような線が描かれるかは、他のオプション指定
   *によって異なる。
   */
  
  public void drawLine(int x1, int y1, int x2, int y2) {
    int tmp = 0;
    int xpos = 0;
    int ypos = 0;
    int dx = Math.abs(x1-x2);
    int dy = Math.abs(y1-y2);
    int signX = (x1<x2) ? -1:1;
    int signY = (y1<y2) ? -1:1;
    
    styleCount = 0;
    sx = x1;
    sy = y1;
    lineLength = Math.round(Math.sqrt((double)dx*dx+dy*dy));
    if(spacerActive) {
      midLength = (int)Math.round(
        Math.sqrt((dx*lineMidPoint)*(dx*lineMidPoint)
        +(dy*lineMidPoint)*(dy*lineMidPoint)));
    }
    //線分の中央座標をセットする。これは外部からの参照用で動作とは関係ない。
    midXpos = (double)x1 - dx * lineMidPoint * signX;
    midYpos = (double)y1 - dy * lineMidPoint * signY;
    
    if( dx == 0 && dy == 0) {
      drawPoint(x1,y1);
    } else if( dx <= dy ) {
      if(     y1<=y2 && x1 <= x2) {
        for(int i=0; i<=dy; i++) {
          xpos = (int)Math.round((double)dx*i/dy + x1);
          ypos = y1+i;
//						g.setColor(Color.black);
          drawPoint(xpos,ypos);
        }
      } else if( y1 <= y2 && x1 >= x2){
        for(int i=0; i<=dy; i++) {
          xpos = (int)Math.round((double)x1-dx*i/dy);
          ypos = y1+i;
//						g.setColor(Color.blue);
          drawPoint(xpos,ypos);
        }
      } else if( y1 >= y2 && x1 <= x2) {
        for(int i=0; i<=dy; i++) {
          xpos = (int)Math.round((double)x2-dx*i/dy);
          ypos = y2+i;
//						g.setColor(Color.red);
          drawPoint(xpos,ypos);
        }
      } else if( y1 >= y2 && x1 >= x2) {
        for(int i=0; i<=dy; i++) {
          xpos = (int)Math.round((double)dx*i/dy+x2);
          ypos = y2+i;
//						g.setColor(Color.green);
          drawPoint(xpos,ypos);
        }
      }
    } else {
      if(     y1<=y2 && x1 <= x2) {
        for(int i=0; i<=dx; i++) {
          ypos = (int)Math.round((double)dy*i/dx+y1);
          xpos = x1+i;
//						g.setColor(Color.magenta);
          drawPoint(xpos,ypos);
        }
      } else if( y1 <= y2 && x1 >= x2){
        for(int i=0; i<=dx; i++) {
          ypos = (int)Math.round((double)y1+dy*i/dx);
          xpos = x1-i;
//						g.setColor(Color.blue);
          drawPoint(xpos,ypos);
        }
      } else if( y1 >= y2 && x1 <= x2) {
        for(int i=0; i<=dx; i++) {
          ypos = (int)Math.round((double)y1-dy*i/dx);
          xpos = x1+i;
//						g.setColor(Color.yellow);
          drawPoint(xpos,ypos);
        }
      } else if( y1 >= y2 && x1 >= x2) {
        for(int i=0; i<=dx; i++) {
          ypos = (int)Math.round((double)dy*i/dx+y2);
          xpos = x2+i;
//						g.setColor(Color.green);
          drawPoint(xpos,ypos);
        }
      }
    }
    
    if(drawEndArrow) {
      double deltaX = midXpos-x2;
      double deltaY = midYpos-y2;
      double angle = fnat(deltaX,deltaY);
      if(y2>y1) angle +=180;
      drawArrow(x2,y2,angle);
    }
    if(drawStartArrow) {
      double deltaX = midXpos-x1;
      double deltaY = midYpos-y1;
      double angle = fnat(deltaX,deltaY);
      if(y2<y1) angle +=180;
      drawArrow(x1,y1,angle);
    }
  }
  /**
   *指定されている線種(bold,plean)に従って、指定座標に点を打つ。
   *boldなら、1点は3*3dot、pleanなら1点1dot。
   */
  private void drawPoint(int x, int y) {
    if(filter(x,y)) {
      if( penType.equals("bold") ){
        g.fillRect(x-1,y-1,3,3);
      } else if ( penType.equals("plean") ) g.drawLine(x,y,x,y);
    }
  }
  /**
   *ラインスタイル、スペーサの位置に従って、点を打つか打たないかを決定する。
   *打つときはtrueを返す。
   */
  private boolean filter(int x,int y) {
    
    //始点から現在の点までの長さを求める。
    //sx,syはdrawLineの最初で、x1,y1の値が代入されている。
    
    double c = Math.sqrt((sx-x)*(sx-x)+(sy-y)*(sy-y));
    
    //もしスペーサ機能が有効になっていたら、スペーサ位置に来たときに、
    //点を打たないように、falseを返す。
    
    if(spacerActive)
      if(((midLength - spaceLength)<c) && ((midLength + spaceLength)>c))
        return false;
    
    //矢印がboldかつ、矢印描画swがtrueの時は、矢印の頭を線が突き破らない
    //ように、線の長さを少し短くする。
    
    if(arrowBold && drawStartArrow && c<(arrowLength-2)) return false;
    if(arrowBold && drawEndArrow && c>(lineLength-(arrowLength-2))) return false;
    
    c = Math.round(c/scale % 32);	//(int)lineStyleは 4 byte = 32 bit
    return (((lineStyle >>> (int)c) & 1) ==0) ? false : true;
  }
  /**
   *指定された座標に矢印の頭を描く。
   */
  private void drawArrow(int xpos,int ypos,double angle) {
    int x[] = new int[3];
    int y[] = new int[3];
    x[0]=xpos; y[0]=ypos;
    
    x[1] = (int)Math.round(Math.sin( (angle-arrowAngle) * Math.PI/180 ) * arrowLength + xpos);
    y[1] = (int)Math.round(Math.cos( (angle-arrowAngle) * Math.PI/180 ) * arrowLength + ypos);
    x[2] = (int)Math.round(Math.sin( (angle+arrowAngle) * Math.PI/180 ) * arrowLength + xpos);
    y[2] = (int)Math.round(Math.cos( (angle+arrowAngle) * Math.PI/180 ) * arrowLength + ypos);
    if(!arrowBold) {
      g.drawLine(x[0],y[0],x[1],y[1]);
      g.drawLine(x[0],y[0],x[2],y[2]);
    } else g.fillPolygon(x,y,x.length);
  }
  
  /**
   *Δx,Δyから角度を返す。
   */
  private double fnat(double x,double y) {
    return Math.atan(x/y)*180d/Math.PI;
  }
  
};

