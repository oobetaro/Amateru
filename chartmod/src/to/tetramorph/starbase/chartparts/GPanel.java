/*
 * GPanel.java
 *
 * Created on 2007/10/17, 16:51
 *
 */

package to.tetramorph.starbase.chartparts;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * GComponentとそのサブクラスの部品を描画するためのパネル。
 * @author 大澤義鷹
 */
public abstract class GPanel extends JPanel {
    protected List<GLayout> layoutList = new ArrayList<GLayout>();
    //ダブクリと認識する判定時間を検出
    private final int multiClickInterval =
        ((Integer)Toolkit.getDefaultToolkit()
        .getDesktopProperty("awt.multiClickInterval")).intValue();
    
    /**
     * GPanel オブジェクトを作成する
     */
    public GPanel() {
        GComponentMouseHandler gmh = new GComponentMouseHandler();
        addMouseListener( gmh );
        addMouseMotionListener( gmh );
    }
    
    /**
     * 登録されているGComponentの部品を描画する。サブクラスのpaintComponent()の中か
     * ら、このメソッドを呼び出すことで描画が行われる。
     * @param g グラフィックスオブジェクト
     * @param baseWidth 基準となる幅 (単位 : pixcel)
     */
    protected void drawLayout( Graphics2D g, float baseWidth ) {
        for ( GLayout rl : layoutList ) {
            rl.setGraphics( g );
            rl.setBaseWidth( baseWidth );
            rl.setBasePoint( basePoint );
            rl.setup();
        }
        for ( GLayout rl : layoutList )
            rl.draw();
    }
    
    protected Point2D basePoint = new Point2D.Double();
    
    public void setBasePoint( Point2D p ) {
        basePoint = p;
    }
    
    /**
     * 部品を追加する。
     */
    public void addGLayout( GLayout rl ) {
        if ( rl == null ) return;
        layoutList.add( rl );
    }
    
    /**
     * 指定された部品を削除する。
     */
    public void removeGLayout( GLayout rl ) {
        if ( rl == null ) return;
        layoutList.remove( rl );
    }
    
    /**
     * 全部品を削除する。
     */
    public void removeAllGLayout() {
        layoutList.clear();
    }
    
    /**
     * 指定された部品がすでにこのパネルにaddされている場合はtrueを返す。
     */
    public boolean contain( GLayout rl ) {
        for ( GLayout gl : layoutList ) {
            if ( gl == rl ) return true;
        }
        return false;
    }
    
    /**
     * マウスハンドラ
     */
    private class GComponentMouseHandler extends MouseAdapter
        implements MouseMotionListener {
        GComponent onCursorComp = null;
        boolean flag;
        
        public void mouseMoved( MouseEvent e ) {
            for ( GLayout gl : layoutList ) {
                GComponent hitComp = gl.contains(e.getX(),e.getY());
                boolean hit = hitComp != null;
                if ( hit ) {
                    if ( onCursorComp == null ) {
                        //オンカーソルイベントをトリガ
                        onCursorComp = hitComp;
                        onCursorComp.getGComponentListener().componentOnCursor(
                            new GComponentEvent( onCursorComp,e,GPanel.this) );
                        repaint();
                        return;
                    } else if ( onCursorComp != hitComp ) {
                        //アウトカーソル
                        onCursorComp.getGComponentListener().componentOutCursor(
                            new GComponentEvent( onCursorComp,e,GPanel.this) );
                        //新しくオンカーソル
                        hitComp.getGComponentListener().componentOnCursor(
                            new GComponentEvent( hitComp,e,GPanel.this) );
                        onCursorComp = hitComp;
                        repaint();
                        return;
                    } else {
                        return;
                    }
                }
            }
            if ( onCursorComp != null ) {
                //アウトカーソル
                onCursorComp.getGComponentListener().componentOutCursor(
                    new GComponentEvent( onCursorComp, e, GPanel.this ) );
            }
            onCursorComp = null;
        }
        
        // クリックされたとき部品にヒットしていたらリスナを呼びだし
        public void mouseClicked( final MouseEvent evt ) {
            int count = evt.getClickCount();
            if ( count == 1 ) {
                new Thread( new Runnable() {
                    public void run() {
                        flag = true;
                        try {
                            Thread.sleep( multiClickInterval );
                        } catch ( InterruptedException e ) {}
                        if ( flag ) clicked( evt );
                    }
                }).start();
            } else if ( count == 2 ) {
                flag = false;
                doubleClicked( evt );
            }
        }
        
        // クリックされたとき、部品にヒットしていたらリスナを呼びだし
        void clicked(MouseEvent evt) {
            for ( GLayout gl : layoutList ) {
                GComponent hitComp = gl.contains(evt.getX(),evt.getY());
                boolean hit = hitComp != null;
                if ( hit ) {
                    hitComp.getGComponentListener().componentClicked(
                        new GComponentEvent( hitComp,evt,GPanel.this ) );
                    break;
                }
            }
        }
        
        // ダブクリされたとき、部品にヒットしていたらリスナを呼びだし
        void doubleClicked( MouseEvent evt ) {
            for ( GLayout gl : layoutList ) {
                GComponent hitComp = gl.contains(evt.getX(),evt.getY());
                boolean hit = hitComp != null;
                if ( hit ) {
                    hitComp.getGComponentListener().componentDoubleClicked(
                        new GComponentEvent( hitComp,evt,GPanel.this ) );
                    break;
                }
            }
        }
        
        public void mouseExited(MouseEvent e) {
            if ( onCursorComp != null ) {
                //アウトカーソル
                onCursorComp.getGComponentListener().componentOutCursor(
                    new GComponentEvent( onCursorComp,e,GPanel.this) );
            }
        }
        
        public void mouseDragged(MouseEvent e) {
        }
    }
    
}
