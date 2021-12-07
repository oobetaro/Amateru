/*
 * GComponent.java
 *
 * Created on 2007/10/03, 17:20
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * グラフィックで文字列描画を行うG**部品を作るときの親クラス。
 * @author 大澤義鷹
 */
public abstract class GComponent {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;
    public static final int CENTER = 4;
    
    protected float baseWidth;
    private int align = LEFT;
    private int valign = TOP;
    public Graphics2D g;
    public GComponentListener gcl;
    protected float [] insets = new float [4];
    private String actionCommand = "";
    protected Color bgColor = null;
    /**
     * オブジェクトを作成する。
     * align=LEFT,valign=TOP,insets[0,0,0,0]がデフォルト。
     */
    public GComponent() {
        
    }
    /**
     * 描画基準サイズをピクセルで指定する。
     */
    public void setBaseWidth( float baseWidth ) {
        this.baseWidth = baseWidth;
    }
    /**
     * グラフィックス2Dオブジェクトをセットする。
     */
    public void setGraphics( Graphics2D g ) {
        this.g = g;
    }
    /**
     * 水平軸の配置方式を指定する。初期値はLEFT。
     * TOP,BOTTOMは禁止でそれを指定するとIllegalArgumentException。
     */
    public void setAlign( int align ) {
        if ( align == TOP || align == BOTTOM )
            throw new IllegalArgumentException("LEFT,RIGHT,CENTER only.");
        this.align = align;
    }
    /**
     * 水平軸の配置方法を返す。初期値はLEFT。
     */
    public int getAlign() {
        return align;
    }
    /**
     * 垂直軸の配置方法をセットする。初期値はTOP。
     * LEFT,RIGHTは禁止でそれを指定するとIllegalArgumentException
     */
    public void setVAlign( int valign ) {
        if ( valign == LEFT || valign == RIGHT )
            throw new IllegalArgumentException("TOP,BOTTOM,CENTER only.");
        this.valign = valign;
    }
    /**
     * 垂直軸の配置方法を返す。初期値はTOP。
     */
    public int getVAlign() {
        return valign;
    }
    /**
     * 水平と垂直の配置方法をまとめてセットする。
     * @param align 水平軸の配置。TOP,BOTTOMは禁止。
     * @param valign 垂直軸の配置。LEFT,RIGHTは禁止。
     */
    public void setAligns( int align, int valign ) {
        setAlign(align);
        setVAlign(valign);
    }
    /**
     * 部品のインセットを返す。これは部品の四方に余白を設けるもので、
     * 部品の幅と高さを100[%]としたときの比率をセットする。
     */
    public void setInsets( double top, double left, double bottom, double right ) {
        insets[ TOP ] = (float)top / 100;
        insets[ LEFT ] = (float)left / 100;
        insets[ BOTTOM ] = (float)bottom / 100;
        insets[ RIGHT ] = (float)right / 100;
    }
    /**
     * インセットを個別に返す。
     * @param align TOP,LEFT,BOTTOM,RIGHTのいずれか。
     */
    public float getInset(int align) {
        return insets[align];
    }
    /**
     * インセットを個別に指定する。
     */
    public void setInset(int align,double per) {
        insets[align] = (float)per / 100;
    }
    /**
     * 描画する。x,yはピクセル値
     */
    protected abstract void draw(double x,double y);
    /**
     * 部品のサイズを返す。
     */
    public abstract Rectangle2D.Float getSize();
    /**
     * 部品サイズにインセットを加算したサイズを返す。
     */
    public abstract Rectangle2D.Float getFullSize();
    /**
     * 描画の準備をする。draw(x,y)を呼び出す前にこのメソッドを呼び出されるので、
     * 前準備を行うコードを実装する。
     */
    public abstract void setup();
    /**
     * このコンポーネントに、座標x,yが含まれる場合は自身のコンポーネントを返す。
     * 含まれない場合はnullを返す。
     * このメソッドはGLayoutインターフェィスのメソッドと重複している。
     * このGComponentのサブクラスでGLayoutがimplementsされて、レイアウト用の
     * コンポーネントになった場合、レイアウトに格納されているすべてのGComponent
     * に対して、contains()の判定を行い、合致した場合はその参照を返す。
     * GLayoutが実装されたGComponent#contains()は、まず先にレイアウトされる部品
     * すべてに当たり判定を行い、最後にレイアウト自身の当たり判定を行う。
     */
    public abstract GComponent contains(int x,int y);
    /**
     * GComponentのイベントリスナをセットする。(addListenerと違い、一つしか指定
     * でない)
     */
    public void setGComponentListener( GComponentListener gcl ) {
        this.gcl = gcl;
    }
    /**
     * セットされているGComponentイベントリスナを返す。
     */
    public GComponentListener getGComponentListener() {
        return gcl;
    }
    
    /**
     * アクションコマンドをセットする。オブジェクト識別用の適当な文字列をセットする。
     * nullをセットすると例外がthrowされる。
     * @exception java.lang.IllegalArgumentException
     */
    public void setActionCommand( String cmd ) {
        if ( cmd == null ) throw 
            new IllegalArgumentException("A null string is prohibited.");
        actionCommand = cmd;
    }
    /**
     * アクションコマンドを返す。デフォルトは""。
     */
    public String getActionCommand() {
        return actionCommand;
    }
    
    /**
     * 背景色をセットする。nullを指定すると背景が抜ける。
     */
    public void setBGColor( Color bgColor ) {
        this.bgColor = bgColor;
    }
    
    /**
     * 背景色を返す。
     */
    public Color getBGColor() {
        return bgColor;
    }
}
