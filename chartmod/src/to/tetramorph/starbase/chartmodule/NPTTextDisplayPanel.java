/*
 * NPTTextDisplayPanel.java
 *
 * Created on 2007/10/17, 20:08
 */

package to.tetramorph.starbase.chartmodule;

import to.tetramorph.starbase.chartparts.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.TimeZone;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Const;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.TestConst;

/**
 * NPT三重円で天体度数や名前等の文字の配色設定の際、プレビューを表示するパネル。
 * @author  大澤義鷹
 */
public class NPTTextDisplayPanel extends GPanel {
    private static final int G_CENTER = GLabel.CENTER;
    private static final int G_RIGHT  = GLabel.RIGHT;
    private static final int G_BOTTOM = GLabel.BOTTOM;
    private static final int G_LEFT = GLabel.LEFT;
    private static final int G_TOP = GLabel.TOP;  private Graphics2D g;
    private Dimension screenSize;
    public Color [] _backgroundColor = new Color[] { Color.WHITE };
    private Color highLightColor = new Color(225,225,225);
    /**
     * Creates new form NPTTextDisplayPanel
     */
    public NPTTextDisplayPanel() {
        initComponents();
        createNatalLayout();
        createBodyLable();
        createHouseLabel();
        createMethodLabel();
        HighLightHandler hlh = new HighLightHandler();
        bodyLabel.setGComponentListener(hlh);
        cuspLabel.setGComponentListener(hlh);
    }
    public void setNameColor(Color color) {
        nNameLabel.setColor(color);
    }
    public Color getNameColor() {
        return nNameLabel.getColor();
    }
    public void setDateColor(Color color) {
        nDateLabel.setColor(color);
    }
    public Color getDateColor() {
        return nDateLabel.getColor();
    }
    public void setPlaceColor(Color color) {
        nLocationLabel.setColor(color);
        nTimeZoneLabel.setColor(color);
        nPlaceLabel.setColor(color);
    }
    public Color getPlaceColor() {
        return nPlaceLabel.getColor();
    }
    //天体
    public void setBodyColor( Color color ) {
        bodyLabel.setBodyColor( color );
    }
    public Color getBodyColor() {
        return bodyLabel.getBodyColor();
    }
    public void setSignColor( Color color ) {
        bodyLabel.setSignColor( color );
        cuspLabel.setSignColor( color );
    }
    public Color getSignColor() {
        return bodyLabel.getSignColor();
    }
    public void setAngleColor( Color color ) {
        bodyLabel.setTextColor( color );
        cuspLabel.setTextColor( color );
    }
    public Color getAngleColor() {
        return bodyLabel.getTextColor();
    }
    public void setRevColor( Color color ) {
        bodyLabel.setRevColor( color );
    }
    public Color getRevColor() {
        return bodyLabel.getRevColor();
    }
    public void setHouseNumberColor( Color color ) {
        cuspLabel.setHouseColor( color );
    }
    public Color getHouseNumberColor() {
        return cuspLabel.getHouseColor();
    }
    public void setHighLightColor( Color color ) {
        highLightColor = color;
    }
    public Color getHighLightColor() {
        return highLightColor;
    }
    public void setOtherColor(Color color ) {
        houseMethodLabel.setColor( color );
        progressMethodLabel.setColor( color );
        nodeLabel.setColor( color );
        apogeeLabel.setColor( color );
        natalLabel.setColor( color );
        houseLabel.setColor( color );
    }
    public Color getOtherColor() {
        return houseMethodLabel.getColor();
    }
    
    @Override public void paintComponent( Graphics graphics ) {
        super.paintComponent(graphics);
        this.g = (Graphics2D)graphics;
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        if(screenSize == null || ! screenSize.equals(getSize())) {
            screenSize = getSize();
        }
        g.setPaint( _backgroundColor[0] );
        g.fillRect( 0, 0, screenSize.width, screenSize.height );
        super.drawLayout( g, (float)screenSize.getWidth() * 2 );
    }
    /**
     * カスプリストのハンドラ。リストのマウスイベントを処理する。
     */
    class HighLightHandler implements GComponentListener {
        public void componentClicked(GComponentEvent evt) {
        }
        public void componentDoubleClicked(GComponentEvent evt) {
        }
        
        public void componentOnCursor(GComponentEvent evt) {
            GComponent l = evt.getGComponent();
            l.setBGColor( highLightColor );
            repaint();
        }
        
        public void componentOutCursor(GComponentEvent evt) {
            GComponent l = evt.getGComponent();
            l.setBGColor( null );
            repaint();
        }
        
    }
    
    /**
     * ネイタルの名前、日時、観測地を表示するためのラベルを作成しzodiacPanelに
     * addする。init()から1度しか呼ばれない。
     */
    private void createNatalLayout() {
        nNameLabel.setInset(G_BOTTOM,0.1);
        nDateLabel.setInsets( 0, 2, 0.1, 0 );
        nTimeZoneLabel.setInsets( 0, 2, 0.1, 0 );
        nPlaceLabel.setInsets( 0, 2, 0.1, 0 );
        nLocationLabel.setInsets( 0, 2, 0.1, 0 );
        GBoxLayout layout = new GBoxLayout( GBoxLayout.VERTICAL );
        layout.add( nNameLabel,nDateLabel,nTimeZoneLabel,nPlaceLabel,nLocationLabel);
        layout.setLocation(0.5,0);
        addGLayout(layout);
        
        TimePlace tp = TestConst.getMyTimePlace();
        tp.setTimeZone(TimeZone.getDefault());
        tp.setPlace(TestConst.getMyPlace());
        nNameLabel.setText( "Nippon Taro" );
        String lon = AngleConverter.getFormattedLongitude(tp.getLongitude());
        String lat = AngleConverter.getFormattedLatitude(tp.getLatitude());
        nLocationLabel.setText( lat + ", " + lon );
        nTimeZoneLabel.setText( tp.getTimeZone().getDisplayName() );
        nDateLabel.setText( tp.getFormattedDate() );
        nPlaceLabel.setText( tp.getPlaceName() );
    }
    private void createBodyLable() {
        GBoxLayout layout = new GBoxLayout( GBoxLayout.VERTICAL );
        natalLabel.setInset( G_BOTTOM, 1 );
        layout.add( natalLabel );
        bodyLabel.setInset( G_BOTTOM, 0.5 );
        bodyLabel.setFontScale( 21 );
        layout.add( bodyLabel );
        layout.setLocation(20.5,0);
        Body body = new Body( Const.MARS, 15.99);
        body.lonSpeed = -0.1;
        bodyLabel.setBody( body );
        addGLayout( layout );
    }
    private void createHouseLabel() {
        GBoxLayout layout = new GBoxLayout(GBoxLayout.VERTICAL);
        houseLabel.setInset( G_BOTTOM, 1 );
        layout.add( houseLabel );
        cuspLabel.setInset( G_BOTTOM, 0.5 );
        Body body = new Body( Const.CUSP1,9.88);
        body.house = 1;
        cuspLabel.setBody( body );
        layout.add( cuspLabel );
        layout.setLocation(20.5,5);
        addGLayout( layout );
    }
    private void createMethodLabel() {
        GBoxLayout layout = new GBoxLayout(GBoxLayout.VERTICAL);
        houseMethodLabel.setInset( G_BOTTOM, 0.4 );
        progressMethodLabel.setInset(G_BOTTOM, 0.6 );
        nodeLabel.setInset( G_BOTTOM, 0.4 );
        layout.add( houseMethodLabel );
        layout.add( progressMethodLabel );
        layout.add( nodeLabel );
        layout.add( apogeeLabel );
        layout.setLocation(34,0);
        addGLayout(layout);
    }
    private GLabel nNameLabel = new GLabel();
    private GLabel nLocationLabel = new GLabel( G_LEFT, 11 );
    private GLabel nDateLabel = new GLabel();
    private GLabel nTimeZoneLabel = new GLabel( G_LEFT, 11 );
    private GLabel nPlaceLabel = new GLabel();
    
    private GLabel natalLabel = new GLabel("ネイタル", G_LEFT, 18d );
    private GBodyLabel bodyLabel = new GBodyLabel();
    
    private GLabel houseLabel = new GLabel("ハウスカスプ", G_LEFT, 16d);
    private GCuspLabel cuspLabel = new GCuspLabel(G_RIGHT,17);
    
    private GLabel houseMethodLabel = new GLabel("ハウス分割法：コッホ", G_LEFT,15 );
    private GLabel progressMethodLabel = new GLabel("進行法：1日1年法", G_LEFT,15);
    private GSymbolLabel nodeLabel = new GSymbolLabel("\u00CB", " 真位置", G_LEFT,15);
    private GSymbolLabel apogeeLabel = new GSymbolLabel("\u00CD", " 密接位置", G_LEFT,15);
    
    /***************************************************************************
     * ここよりテスト用のコード
     ***************************************************************************/
    static void createAndShowGUI() {
        if(UIManager.getLookAndFeel().getName().equals("Metal")) {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("テキストカラー設定");
        NPTTextDisplayPanel panel = new NPTTextDisplayPanel();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /** テスト */
    public static void main(String [] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 300, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents
  
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
  
}
