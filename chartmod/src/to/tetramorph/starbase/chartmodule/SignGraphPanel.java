/*
 * SignGraphPanel.java
 *
 * Created on 2008/03/11, 15:45
 *
 */

package to.tetramorph.starbase.chartmodule;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.util.AstroFont;

/**
 * 星座分布グラフプラグイン。
 * @author 大澤義鷹
 */
public class SignGraphPanel extends JPanel implements Printable {
    public static final int SINGLE_MODE = 0;
    public static final int DUAL_MODE   = 1;
    /**
     * { [0〜11],  サイン別男女合計
     *   [0〜11],  サイン別男性数
     *   [0〜11] } サイン別女性合計
     */
    int [][] array = new int[3][12];
    /**
     * [0] 男女人数
     * [1] 男性人数
     * [2] 女性人数
     */
    int [] quantArray = new int[3];
    int graphMode = 0;
    int bodyID = 0;
    
    private static final Stroke solidStroke =
        new BasicStroke( 1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
  
    /**  SignGraphPanel オブジェクトを作成する */
    public SignGraphPanel() {
    }
    /**
     * 男女別にグラフ表示するときはDUAL_MODE、
     * 性別無視でグラフ表示するときはSINGLE_MODEを指定する。
     */
    public void setGraphMode( int mode ) {
        this.graphMode = mode;
    }
    
    @Override
    public void paintComponent( Graphics graphics ) {
        paintComponent( graphics, getSize() );
    }
    
    /**
     * 指定されたサイズのイメージバッファにグラフを描画して返す。
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
    
    private void paintComponent( Graphics graphics, Dimension size ) {
        Graphics2D g = (Graphics2D)graphics;
        g.setColor( getBackground() );
        
        //g.fillRect( 0, 0, getWidth(), getHeight());
        g.fillRect( 0, 0, size.width, size.height );
        float x = 0, y = 0;
        //while ( x < getWidth() && y < getHeight() ) {
        while ( x < size.width && y < size.height ) {
            x += 8f;
            y += 5.6f;
        }
        int width = (int)((x - 8f) * 0.9f);
        int height = (int)((y - 5.6f) * 0.9f);
//        int ofsX = ( getWidth() - width ) / 2;
//        int ofsY = ( getHeight() - height ) / 2;
        int ofsX = ( size.width - width ) / 2;
        int ofsY = ( size.height - height ) / 2;
        float step = width / array[0].length;
        float barWidth = step * 0.3f;
        drawGrid( width, height, ofsX, ofsY, g );
        if ( graphMode == SINGLE_MODE ) {
            draw( width, height, ofsX, ofsY, array[0], getMax( array[0] ),
                  g, step, barWidth, Color.BLUE );
        } else {
            int max = Math.max( getMax( array[1] ),getMax( array[2] ) );
            draw( width, height, ofsX, ofsY, array[1], max, 
                  g, step, barWidth, Color.BLUE );
            draw( width, height, ofsX+(int)barWidth, ofsY, array[2], max,
                  g, step, barWidth, Color.RED );
        }
        Object temp = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );
        
        g.setStroke( solidStroke );
        drawSign( width, height,ofsX, ofsY, g, step );
        drawTitle( ofsX, ofsY - 5, bodyID, g, width * 0.025f );
        drawTotal( ofsX + width, ofsY - 5, quantArray, g, width * 0.025f );
        
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, temp );
    }
    
    /**
     * 印刷用メソッドを実装
     */
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
    
    /**
     * ゲージを描く
     */
    private void drawGrid( int width,
                             int height,
                             int ofsX,
                             int ofsY,
                             Graphics2D g ) {
        float zeroHeight = ( height * 0.85f );
        float vstep = zeroHeight / 4f;
        g.setColor( Color.GRAY );
        g.drawRect( ofsX, ofsY, width, (int)zeroHeight );
        for ( int i=0; i<4; i++ ) {
            int h = (int)(i * vstep) + ofsY;
            g.drawLine( ofsX, h, ofsX + width, h );
        }
    }
    /**
     * グラフの棒を描く
     */
    private void draw( int width,
                         int height,
                         int ofsX,
                         int ofsY,
                         int [] data,
                         int max,
                         Graphics2D g,
                         float step,
                         float barWidth,
                         Color color ) {
        float zeroHeight = ( height * 0.85f );
        float per  = zeroHeight / max;
        g.setPaint( Color.BLACK );
        float margin = width * 0.025f;
        for ( int i=0; i < data.length; i++ ) {
            if ( data[i] == 0 ) continue;
            float bh = Math.round( data[i] * per );
            int x = (int)( step * i + margin) + ofsX;
            int y = (int)( zeroHeight - bh ) +  ofsY;
            g.setColor( color );
            g.fillRect( x, y, (int)barWidth, (int)bh );
            g.setColor( Color.BLACK );
            g.drawRect( x, y, (int)barWidth, (int)bh );
            int yy = (int)( height * 0.91 ) + ofsY;
            drawValue( x, yy, data[i], g, width * 0.02f);
        }
    }
    /**
     * 棒グラフの下にサインシンボルを描く
     */
    private void drawSign( int width,
                             int height,
                             int ofsX,
                             int ofsY,
                             Graphics2D g,
                             float step ) {
        float fontSize = width * 0.03f;
        Font font = AstroFont.getFont( fontSize );
        float zeroHeight = ( height * 0.9f );
        g.setPaint( Color.BLACK );
        Font tempFont = g.getFont();
        g.setFont( font );
        float margin = width * 0.025f;
        for ( int i=0; i < 12; i++ ) {
            int x = (int)( step * i + margin ) + ofsX;
            int y = (int)zeroHeight + ofsY;
            g.drawString( "" + Const.ZODIAC_CHARS[i], x, y );
        }
        g.setFont( tempFont );
    }
    
    /**
     * 指定座標に縦書きで数字を描く
     */
    private void drawValue( int x,
                              int y,
                              int value,
                              Graphics2D g,
                              float fontSize ) {
        g.setPaint( Color.BLACK );
        Object temp = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );
        Font tempFont = g.getFont();
        Font font = getFont().deriveFont( fontSize );
        g.setFont( font );
        AffineTransform at = new AffineTransform();
        AffineTransform mvat = new AffineTransform();
        FontRenderContext frc = g.getFontRenderContext();
        at.setToRotation( Math.toRadians(90d) );
        TextLayout textLayout = new TextLayout(
            "" + value, font, frc );
        mvat.setToTranslation(x,y);
        Shape shape = textLayout.getOutline(at);
        Shape shape2 = mvat.createTransformedShape(shape);
        g.fill(shape2);
        g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, temp );
        g.setFont( tempFont );
    }
    /**
     * 指定座標にタイトルを描く
     */
    private void drawTitle( int x,
                              int y,
                              int bodyID,
                              Graphics2D g,
                              float fontSize ) {
        AffineTransform at = new AffineTransform();
        Font font = getFont().deriveFont( fontSize );
        FontRenderContext frc = g.getFontRenderContext();
        TextLayout textLayout = new TextLayout(
            Const.PLANET_NAMES[ bodyID ]+"の星座分布", font, frc );
        at.setToTranslation(x,y);
        Shape shape = textLayout.getOutline(at);
        g.fill( shape );
    }
    /**
     * 人物データの男女別総人数を描く。X座標から左寄せで描く
     */
    private void drawTotal( int x,
                              int y,
                              int [] array,
                              Graphics2D g,
                              float fontSize ) {
        AffineTransform at = new AffineTransform();
        Font font = getFont().deriveFont( fontSize );
        FontRenderContext frc = g.getFontRenderContext();
        String str = null;
        if ( graphMode == SINGLE_MODE) {
            str = String.format( "合計＝%d［件］", array[0] );
        } else {
           str = String.format( "男＝%d　女＝%d　合計＝%d［件］",
                                array[1], array[2], array[0] );
        }
        TextLayout textLayout = new TextLayout( str, font, frc );
        float adv = textLayout.getAdvance();
        at.setToTranslation(x-adv,y);
        Shape shape = textLayout.getOutline(at);
        g.fill( shape );
    }
    /**
     * 配列内の値の最大値を返す
     */
    private int getMax( int [] array ) {
        int max = 0;
        for ( int v : array ) {
            if ( max < v ) max = v;
        }
        return max;
    }
    /**
     * グラフ表示する天体リストをセットする。
     * Body.groupフィールドを認識し、このクラスではこのフィールドを男女の識別用
     * として使用する。(通常はN,P,T識別用)。1=男,2=女と認識する。
     * リストの先頭のBody.idを、そのリストの天体とみなす。
     */
    public void setBodyList( List<Body> list ) {
        bodyID = list.get(0).id;
        Arrays.fill( quantArray, 0 );
        quantArray[0] = list.size();
        for ( int i=0; i < array.length; i++)
            Arrays.fill( array[i], 0 );
        //男女別に集計 男女以外のデータはotherに集計
        for ( Body body : list ) {
            int sign = body.getSign();
            array[0][sign]++;
            switch ( body.group ) {
                case 1 : array[1][sign]++; break;
                case 2 : array[2][sign]++; break;
            }
        }
        // 男女の合計を求める
        for ( int i=0; i < array[0].length; i++ ) {
            quantArray[1] += array[1][i];
            quantArray[2] += array[2][i];
        }
    }
    //テスト
    public static void main(String [] args) {
        List<Body> list = new ArrayList<Body>();
        for ( int j=0; j<12; j++) {
            int m = (j+1) * 100;
            for ( int i=0; i < m; i++ ) {
                Body body = new Body(0, j * 30);
                body.group = ( Math.random() > 0.5 ) ? 1 : 2;
                list.add( body );
            }
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        SignGraphPanel pane = new SignGraphPanel();
        pane.setGraphMode(1);
        pane.setBodyList( list );
        frame.getContentPane().add( pane );
        frame.setMinimumSize( new Dimension(600,400) );
        frame.setVisible( true );
    }
}
