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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import to.tetramorph.starbase.lib.Place;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.TestConst;

/**
 * 体脱予測カレンダー表示用のグラフ。
 * @author 大澤義鷹
 */
public class OBEGraphPanel extends JPanel implements OBECalendarListener,
                                                          Printable {
    TimePlace transitTimePlace   = null;
    TimePlace natalTimePlace     = null;
    GregorianCalendar transitCal = null;
    Transit transit              = null;
    String name = null;
    double [][] table         = null;
    int mode                  = 0;
    int MAX_VALUE             = 100;
    
    Color [] BAR_COLORS        = new Color[] { new Color(244,191,91),
                                                new Color(0,255,128),
                                                new Color(0,128,255),
                                                //new Color(99,117,188), 
                                                new Color(251,140,223) };

    private static final Stroke solidStroke = new BasicStroke( 1f,
                                                   BasicStroke.CAP_ROUND,
                                                   BasicStroke.JOIN_ROUND );
    
    private static final Stroke dotStroke = new BasicStroke( 1f, 
                                                   BasicStroke.CAP_SQUARE, 
                                                   BasicStroke.JOIN_BEVEL,
                                                   1f,
                                                   new float[] { 1.5f, 1.5f },
                                                   0f );
    private static final String [] BAR_TITLES = { 
            "太陽のリズム", "火星のリズム", "月のリズム", "個人のリズム" 
    };

    private static Color GRID_COLOR = new Color(200,200,200);
    private static Color GUAGE_COLOR = Color.BLACK;
    private static Color GRAPH_BG = new Color(240,240,240);
    private boolean [] BAR_VISIBLE = new boolean [] { true, true, true, true };
    
    /**  SignGraphPanel オブジェクトを作成する */
    public OBEGraphPanel() {
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
//        int WIDTH = getSize().width;
//        int HEIGHT = getSize().height;
        int WIDTH = size.width;
        int HEIGHT = size.height;
        g.setPaint( Color.WHITE );
        g.fillRect( 0, 0, WIDTH, HEIGHT );
        float width = WIDTH * 0.90f;
        float height = HEIGHT * 0.7f;
        float ofsX = ( WIDTH - width ) / 2f;
        float ofsY = ( HEIGHT - height ) / 2f;
        if ( table == null ) return;
        g.setPaint(Color.BLACK);
        drawGrid( width, height, ofsX, ofsY, g );
        drawVirticalGrid( width, height, ofsX, ofsY, g, transitCal, table );
        drawBar( width, height, ofsX, ofsY, table, g);
        Font font = getFont().deriveFont( height / 17f );
        Rectangle2D r = font.getMaxCharBounds(g.getFontRenderContext());
        drawString( 10, (float)r.getHeight(), "幽体離脱予報グラフ", font, g);
        drawLegend( width,height,ofsX,ofsY,g);
        drawTransitAndNatal( width, height, ofsX, ofsY, 
                             transit, natalTimePlace, name, g );
    }
    /**
     * 印刷用メソッドを実装
     */
    public int print( Graphics graphics, PageFormat pf, int pageIndex ) {
        Graphics2D g = (Graphics2D)graphics;
        if ( pageIndex == 0 ) {
            Dimension size = new Dimension();
            size.setSize( pf.getImageableWidth(), pf.getImageableHeight() );
            g.translate( pf.getImageableX(), pf.getImageableY() );
            paintComponent( g, size );
            g.dispose();
            return Printable.PAGE_EXISTS;
        } else return Printable.NO_SUCH_PAGE;        
    }
    
    /**
     * 各バーの色で矩形をペイントし、その色がなにを意味しているかを表示
     */
    private void drawLegend( float width,
                               float height,
                               float ofsX,
                               float ofsY,
                               Graphics2D g) {
        Font font = getFont().deriveFont( height / 30f );
        float w = width / 5;
        float x = ofsX;
        float y = ofsY + height * 0.95f;
        for ( int i=0, j=0; i < BAR_TITLES.length; i++ ) {
            if ( ! BAR_VISIBLE[i] ) continue; 
            drawLegend( x + w * j, y, BAR_TITLES[i], font, BAR_COLORS[i], g );
            j++;
        }
    }
    
    /**
     * ゲージを描く
     */
    private void drawGrid( float width,
                             float height,
                             float ofsX,
                             float ofsY,
                             Graphics2D g ) {
        float barHeight = ( height * 0.85f );
        float vstep = barHeight / (MAX_VALUE / 10f);
        Rectangle2D rect = new Rectangle2D.Float(ofsX,ofsY,width,barHeight);
        g.setColor( GRAPH_BG );
        g.fill( rect );
        Line2D line = new Line2D.Float();
        Stroke tempStroke = g.getStroke();
        g.setStroke( dotStroke );
        int c = 0;
        for ( float h = vstep; h < barHeight; h += vstep ) {
            Color col = (c == 3) ? Color.RED : GRID_COLOR;
            g.setPaint( col );
            line.setLine( ofsX, h + ofsY, ofsX + width, h + ofsY );
            g.draw( line );
            c++;
        }
        // ここより縦軸の数値を描く
        int point = MAX_VALUE;
        FontRenderContext frc = g.getFontRenderContext();
        Font font = g.getFont().deriveFont( height / 30f);
        AffineTransform at = new AffineTransform();
        antialiasing( true, g);
        g.setPaint( Color.BLACK );
        TextLayout tl = new TextLayout( "00", font, frc );
        float fontMargin = tl.getAdvance() * 0.3f;
        for ( float h = 0; h < barHeight; h += vstep ) {
            TextLayout textLayout = new TextLayout( "" + point, font, frc );
            float adv = textLayout.getAdvance();
            at.setToTranslation( ofsX - adv - fontMargin, h + ofsY );
            Shape shape = textLayout.getOutline(at);
            g.fill( shape );
            at.setToTranslation( ofsX + width + fontMargin, h + ofsY );
            Shape shape2 = textLayout.getOutline(at);
            g.fill( shape2 );
            
            point -= 10;
        }
        antialiasing( false, g);
        g.setStroke( tempStroke );
    }
    
    /**
     * 垂直軸のゲージを描く
     */
    private void drawVirticalGrid( float width,
                                     float height,
                                     float ofsX,
                                     float ofsY,
                                     Graphics2D g,
                                     GregorianCalendar transitCal,
                                     double [][] table ) {
        float step = width * 0.95f / table.length;
        float margin = width * 0.05f / 2f;
        float barHeight = ( height * 0.85f );
        GregorianCalendar cal = (GregorianCalendar)transitCal.clone();
        Line2D line = new Line2D.Float();
        Line2D thrustLine = new Line2D.Float();
        Font font = g.getFont().deriveFont( height / 35f);
        float fontHeight = 
            (float)font.getMaxCharBounds( g.getFontRenderContext() ).getHeight();
        float thrust = height * 0.01f;
        drawString( ofsX + margin, 
                    ofsY - thrust - fontHeight * 1.5f,
                    String.format( "%tY年%tm月%td日 %tH時",transitCal,transitCal,transitCal,transitCal ),
                    font, 
                    g );
        for ( int i=0; i < table.length; i++ ) {
            int hour = cal.get( Calendar.HOUR_OF_DAY );
            if ( ( hour % 6 ) == 0 ) {
                float x = ofsX + margin + step * i;
                Stroke stroke = ( hour == 0 ) ? solidStroke : dotStroke;
                g.setPaint( GRID_COLOR );
                g.setStroke( stroke );
                //float y = ( hour == 0 ) ? ofsY - thrust : ofsY;
                float bottomY = ofsY + barHeight;
                line.setLine( x, ofsY, x, bottomY );
                g.draw( line );
                //ゲージを描く
                g.setStroke( solidStroke );
                g.setPaint( GUAGE_COLOR );
                thrustLine.setLine( x, bottomY, x, bottomY + thrust );
                g.draw( thrustLine );
                if ( hour == 0 ) {
                    thrustLine.setLine( x, ofsY, x, ofsY - thrust );
                    g.draw( thrustLine );
                    //日にちを描く
                    String date = "" + cal.get( Calendar.DAY_OF_MONTH );
                    drawString( x, ofsY - thrust * 2, date, font, g);
                }
                // 時刻を描く ( 幅が狭いときは0,12時のみ描く )
                String tstr = "" + cal.get( Calendar.HOUR_OF_DAY );
                if ( step > 5 ) {
                    drawString( x, bottomY + fontHeight, tstr, font, g);
                } else if ( step > 2.5 && ((cal.get( Calendar.HOUR_OF_DAY) % 12) == 0) ) {
                    drawString( x, bottomY + fontHeight, tstr, font, g);
                } else if ( step > 1.5 && cal.get( Calendar.HOUR_OF_DAY ) == 0 ) {
                    drawString( x, bottomY + fontHeight, tstr, font, g);
                }
            }
            cal.add( Calendar.HOUR_OF_DAY, 1 );
        }
        //単位を描く
        drawString( ofsX + width * 0.96f,
                    ofsY - thrust - fontHeight * 1.5f, 
                    "〔日 ／ ％〕",
                    font,
                    g );
        drawString( ofsX + width * 0.96f,
                    ofsY + thrust + barHeight + fontHeight * 1.5f, 
                    "〔時 ＼ ％〕",
                    font,
                    g );
//        float barHeight = ( height * 0.85f );
//        float vstep = barHeight / (MAX_VALUE / 10f);
        Rectangle2D rect = new Rectangle2D.Float(ofsX,ofsY,width,barHeight);
        g.draw( rect );
        
    }
    
    private void drawString( float x, float y, String str, Font font, Graphics2D g) {
        FontRenderContext frc = g.getFontRenderContext();
        AffineTransform at = new AffineTransform();
        antialiasing( true, g);
        g.setPaint( Color.BLACK );
        TextLayout tl = new TextLayout( str, font, frc );
        at.setToTranslation( x, y );
        Shape shape = tl.getOutline(at);
        g.setColor( Color.BLACK );
        g.fill( shape );
        antialiasing( false, g);
    }
    
    Object ANTIALIASING = null;
    private void antialiasing( boolean on, Graphics2D g ) {
        if ( on ) {
            ANTIALIASING = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON );            
        } else {
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, ANTIALIASING );
        }
    }
    
    /**
     * グラフの棒を描く
     */
    private void drawBar( float width,
                         float height,
                         float ofsX,
                         float ofsY,
                         double [][] table,
                         Graphics2D g ) {
        float step = width * 0.95f / table.length;
        float margin = width * 0.05f / 2f; //width * 0.025f;
        float barWidth = step * 0.5f;
        if ( barWidth < 1f || step < 1f ) barWidth = 1f;
        float barHeight = ( height * 0.85f );
        float per  = barHeight / MAX_VALUE;
        g.setStroke( solidStroke );
        for ( int i=0; i < table.length; i++ ) {
            float Y = barHeight;
            int k = 0;
            for ( int j=0; j < table[i].length; j++ ) {
                if ( ! BAR_VISIBLE[j] ) continue;
                float bh = Math.round( table[i][j] * per );
                int x = (int)( step * i  + margin  + ofsX );
                Y -= bh;
                int y = (int)( Y + ofsY );
                g.setColor( BAR_COLORS[ j ] );
                g.fillRect( x, y, (int) barWidth, (int) bh );
                if ( barWidth >= 3 ) {
                    g.setColor( Color.BLACK );
                    g.drawRect( x, y, (int) barWidth, (int) bh );
                }
            }
        }
    }
    
    /**
     * 凡例を描く。バーの色の矩形とその説明文を描く。
     * @param boxColor バーに対応する色
     */
    private void drawLegend( float x,
                               float y,
                               String str,
                               Font font,
                               Color boxColor,
                               Graphics2D g) {
        FontRenderContext frc = g.getFontRenderContext();
        TextLayout tl = new TextLayout( str, font, frc );
        
        float fontHeight = (float)tl.getBounds().getHeight();
        Rectangle2D rect = new Rectangle2D.Float( x, y, fontHeight * 2, fontHeight);
        g.setPaint( boxColor );
        g.fill( rect );
        g.setPaint( Color.BLACK );
        g.draw( rect );
        AffineTransform at = new AffineTransform();
        at.setToTranslation(x + fontHeight * 2.1f, y + (fontHeight - tl.getBaseline()-2));
        Shape shape = tl.getOutline(at);
        antialiasing(true,g);
        g.fill( shape );
        antialiasing(false,g);
    }
    
    /**
     * グラフの外に、トランジットとネイタルの文字情報を描く。
     * @param name 名前・人名
     */
    private void drawTransitAndNatal( float width,
                                        float height,
                                        float ofsX,
                                        float ofsY,
                                        Transit transit,
                                        TimePlace natalTimePlace,
                                        String name,
                                        Graphics2D g ) {
        Font font = getFont().deriveFont( height / 30f );
        StringBuilder tsb = new StringBuilder();
        tsb.append( "トランジット：" );
        tsb.append( transit.getFormattedDate() );
        tsb.append( " " );
        tsb.append( transit.getFormattedTimeZone() );
        tsb.append( transit.getPlaceName() );
        tsb.append( " " );
        tsb.append( AngleConverter.getFormattedLongitude(
                                            transit.getLongitude()));        
        tsb.append( " " );
        tsb.append( AngleConverter.getFormattedLatitude(
                                            transit.getLatitude()));
        
        StringBuilder nsb = new StringBuilder();
        nsb.append( "ネイタル：" );
        nsb.append( name );
        nsb.append( natalTimePlace.getFormattedDate() );
        nsb.append( " " );
        nsb.append( natalTimePlace.getFormattedTimeZone() );
        nsb.append( natalTimePlace.getPlaceName() );
        nsb.append( " " );
        nsb.append( AngleConverter.getFormattedLongitude(
                                            natalTimePlace.getLongitude()));        
        nsb.append( " " );
        nsb.append( AngleConverter.getFormattedLatitude(
                                            natalTimePlace.getLatitude()));
        
        float x = ofsX;
        float y = ofsY + height * 1.05f;
        float fh = (float)
        font.getMaxCharBounds(g.getFontRenderContext()).getBounds2D().getHeight();
        drawString( x, y, tsb.toString(), font, g );
        drawString( x, y + fh, nsb.toString(), font, g);
    }
    
    /**
     * OBECalendarがこのメソッドでデータを与える。
     * プログラマーが直接呼び出してはいけない。
     */
    public void calcurated( TimePlace transitTimePlace,
                              TimePlace natalTimePlace,
                              double [][] table,
                              GregorianCalendar transitCal ) {
        this.transitTimePlace = transitTimePlace;
        this.natalTimePlace   = natalTimePlace;
        this.transitCal       = transitCal;
        this.table = table;
        repaint();
        setBusy( false );
    }

    /**
     * ネイタルの名前をセットする。
     */
    public void setName( String name ) {
        this.name = name;
    }
    
    /**
     * トランジットをセットする。これは文字列表現用で計算とは無関係。
     */
    public void setTransit( Transit transit ) {
        this.transit = transit;
    }
    
    /**
     * バーの可視状態を設定する。trueなら可視化、falseなら非可視化
     * [0] 太陽バー
     * [1] Ｔ火星バー
     * [2] Ｔ月バー
     * [3] Ｎ−Ｔ月バー
     */
    public void setBarVisible( JCheckBox [] checkBoxs ) {
        for ( int i=0; i < checkBoxs.length; i++ ) {
            BAR_VISIBLE[i] = checkBoxs[i].isSelected();
        }
    }

    boolean BUSY = false;
    
    /**
     * BUSYフラグをセットする。これでセットされたフラグは、このクラスのcalcurated
     * の処理が完了したときにリセットされfalseになる。
     */
    public synchronized void setBusy( boolean b ) {
        this.BUSY = b;
    }
    
    /**
     * BUSYフラグの状態を返す。このクラスを使用している、OBEGraphPlaginでは、
     * OBECalendarに計算開始の指示を出す前に、setBusy(true)にする。
     * 計算が別スレッドで開始され、計算終了とともにこのクラスのcalcuratedが
     * 呼び出されるのだが、それまでビジーフラグはtrueの状態にある。
     * 計算中にプラグインのほうでは、マニューバから時刻の更新要求でsetDateが
     * よばれる場合があり、そのとき計算中なのに次の計算要求を出すと、
     * 常識はずれな場所でExceptionを発生させてしまう危険がある。
     * だからsetDateのメソッド内ではisBusyで計算終了を検査して、計算中、
     * つまりビジーであれば、その回の日付更新をキャンセルする。
     */
    public synchronized boolean isBusy() {
        return BUSY;
    }
    
    //テスト
    public static void main(String [] args) {
        
        System.setProperty("swe.path","c:/users/ephe/");
        System.setProperty("DefaultTime","00:00:00"); //時間を省略する場合は設定されてる必要がある。
        int AD = GregorianCalendar.AD;
        int hourCount = 24 * 3;
        Place place = TestConst.getMyPlace();
        TimePlace natalTimePlace = TestConst.getMyTimePlace();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        OBEGraphPanel pane = new OBEGraphPanel();
//        Transit transit =TestConst.getMyNowTransit(); 
        Transit transit =TestConst.getMyTransit( AD, 2008, 3-1, 25,0,0,0); 
        pane.setTransit( transit );
        pane.setName( TestConst.getMyNatal().getName() );
//        pane.setGraphMode(1);
        OBECalendar obecal = new OBECalendar(pane);
        obecal.setTimePlace( transit,
                             natalTimePlace,
                             hourCount );
        
        frame.getContentPane().add( pane );
        frame.setMinimumSize( new Dimension(600,400) );
        frame.setVisible( true );
    }
    
}
