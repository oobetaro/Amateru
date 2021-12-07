/*
 * GLayout.java
 *
 * Created on 2007/10/04, 9:41
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * ZodiacPanel上に文字をレイアウトするための部品。
 * TextReporterなどの文字部品をこのオブジェクトにaddして使用する。
 * 整列方法はSwingのBoxLayoutと概念的には同じで、縦方向(上から下)または横方向(左か
 * ら右)に連続して部品を配置する。各部品との隙間は、各部品ごとにインセット値を設定
 * することによって行う。
 * @author 大澤義鷹
 */
public class GBoxLayout extends GComponent implements GLayout {
    List<GComponent> repoList = new ArrayList<GComponent>();
    double xper,yper;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    int direction = VERTICAL;
    
    /**
     * GLayout オブジェクトを作成する
     * 縦方向にレイアウトする場合はVERTICAL,水平方向ならHORIZONTALを指定。
     */
    public GBoxLayout( int direction ) {
        super();
        this.direction = direction;
    }
    
    /**
     * 子のGComponentを追加する。
     * @exception IllegalArgumentException 引数にnullを指定した場合。
     */
    public void add( GComponent rp ) {
        if ( rp == null ) throw
            new IllegalArgumentException("null禁止");
        repoList.add(rp);
    }
    
    /**
     * 子のGComponentを追加する。add(hoge,payo,fuga...)と列挙可能。
     * @exception IllegalArgumentException 引数の要素がゼロの場合
     */
    public void add( GComponent ... rps ) {
        if ( rps.length == 0 ) throw
            new IllegalArgumentException("no exist argument");
        for ( GComponent c : rps ) add( c );
    }
    
    /**
     * 子をGComponentをすべて削除する。
     */
    public void removeAll() {
        repoList.clear();
    }
    /**
     * このレイアウトに格納されている子のコンポーネントの数を返す。
     */
    public int getComponentCount() {
        return repoList.size();
    }
    
    /**
     * このレイアウトを表示する位置を、baseWidthに対するパーセントで指定する。
     * @param xper 横軸の表示位置。0-100[%]で指定。
     * @param yper 縦軸の表示位置。0-100[%]で指定。
     */
    public void setLocation( double xper, double yper ) {
        this.xper = xper / 100;
        this.yper = yper / 100;
    }
    
    /**
     * このレイアウトを表示する位置を、baseWidthに対するパーセントで、縦と横の
     * アラインを一括セットする。
     * @param xper 横軸の表示位置。0-100[%]で指定。
     * @param yper 縦軸の表示位置。0-100[%]で指定。
     */
    public void setLocation( double xper, double yper, int align, int valign) {
        setLocation( xper, yper );
        setAligns( align, valign );
    }
    
    /**
     * このオブジェクトと格納されているすべてのGComponentにBaseWidthを設定する。
     */
    public void setBaseWidth( float baseWidth ) {
        this.baseWidth = baseWidth;
        for(GComponent r : repoList) r.setBaseWidth(baseWidth);
    }
    
    /**
     * このオブジェクトと格納されているすべてのGComponentにGraphicsを設定する。
     */
    public void setGraphics(Graphics2D g) {
        this.g = g;
        for(GComponent r : repoList) r.setGraphics(g);
    }
    
    protected void draw( double x,double y ) {
        Rectangle2D.Float area = getSize();
        for ( GComponent r : repoList ) {
            double xp = x;
            double yp = y;
            Rectangle2D.Float part = r.getSize();
            Rectangle2D.Float partf = r.getFullSize();
            if ( direction == VERTICAL ) {
                yp += baseWidth * r.getInset( TOP ); //水平と同じ理由
                switch ( r.getAlign() ) {
                    case LEFT :
                        xp += baseWidth * r.getInset( LEFT );
                        break;
                    case RIGHT :
                        xp += ( area.width - part.width )
                        - r.getInset( RIGHT ) * baseWidth;
                        break;
                    case CENTER :
                        xp += ( area.width - partf.width ) / 2;
                        break;
                }
            }
            if ( direction == HORIZONTAL ) {
                //水平配置のときalignは意味を持たないが、左insetは必要
                xp += baseWidth * r.getInset( LEFT );
                switch ( r.getVAlign() ) {
                    case TOP :
                        yp += baseWidth * r.getInset( TOP );
                        break;
                    case BOTTOM :
                        yp += area.height - part.height
                            - r.getInset( BOTTOM ) * baseWidth;
                        break;
                    case CENTER :
                        yp += (area.height - partf.height) / 2 ;
                        break;
                }
            }
            if ( direction == VERTICAL ) {
                r.draw( xp, yp );
                y += partf.height;
            } else {
                r.draw( xp, yp );
                x += partf.width;
            }
        }
    }
    
    public void draw() {
        double xp = xper * baseWidth;
        double yp = yper * baseWidth;
        Rectangle2D.Float fs = getFullSize();
        switch ( getAlign() ) {
            case LEFT :                       break;
            case RIGHT : xp -= fs.width;      break;
            case CENTER : xp -= fs.width / 2; break;
        }
        switch ( getVAlign() ) {
            case TOP :                         break;
            case BOTTOM : yp -= fs.height;     break;
            case CENTER : yp -= fs.height / 2; break;
        }
        draw( xp + basePoint.getX(), yp + basePoint.getY() );
    }
    
    Point2D basePoint = new Point2D.Double();
    
    public void setBasePoint( Point2D basePoint ) {
        this.basePoint = basePoint;
    }
    
    Rectangle2D.Float sizeRect;
    Rectangle2D.Float fullRect;
    
    public Rectangle2D.Float getSize() {
        return sizeRect;
    }
    
    public Rectangle2D.Float getFullSize() {
        return fullRect;
    }
    
    public void setup() {
        sizeRect = new Rectangle2D.Float();
        fullRect = new Rectangle2D.Float();
        for ( GComponent r : repoList ) r.setup();
        for ( int i=0; i < repoList.size(); i++ ) {
            Rectangle2D.Float rect = repoList.get(i).getFullSize();
            if ( direction == VERTICAL ) {
                sizeRect.height += rect.height;
                if ( sizeRect.width < rect.width )
                    sizeRect.width = rect.width;
            } else {
                sizeRect.width += rect.width;
                if ( sizeRect.height < rect.height ) 
                    sizeRect.height = rect.height;
            }
        }
        float w = sizeRect.width;
        float h = sizeRect.height;
        float b = baseWidth;
        fullRect.width = w + insets[ LEFT ] * b + insets[ RIGHT ] * b;
        fullRect.height = h + insets[ TOP ] * b + insets[ BOTTOM ] * b;
    }
    
    /**
     * このレイアウトに格納されているGComponent(GLabelやGBoxLayout)に、指定座標が
     * 含まれる場合はその部品を返す。含まれていない場合は、このコンポーネントに
     * 含まれるかを検査して含まれる場合は、このコンポーネント自身を帰す。
     * それでも含まれない場合はnullを返す。
     * またコンポーネントにGComponentListenerが登録されていない部品は判定から除外
     * される。
     */
    public GComponent contains( int x, int y ) {
        for ( GComponent r : repoList ) {
            GComponent c = r.contains(x,y);
            if ( c != null ) return c;
        }
        if ( getGComponentListener() != null )
            if ( getFullSize().contains(x,y) ) return this;
        return null;
    }
}
