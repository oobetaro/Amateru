/*
 * ReportPlugin.java
 *
 */
package to.tetramorph.starbase.chartmodule;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import static to.tetramorph.starbase.lib.Const.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.SwingUtilities;
import to.tetramorph.starbase.chartlib.NPTChartDictionaryTool;
import to.tetramorph.starbase.chartparts.GetPointMouseHandler;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.lib.AspectType;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Caption;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.util.Dignity;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.util.Sabian;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.module.DictionaryActionFile;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.ChartAnalyzer;
import to.tetramorph.starbase.util.NatalChart;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.util.DictionaryRequest;
import to.tetramorph.starbase.util.Dispositor;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.util.Preference;
import to.tetramorph.util.StringReplacer;

/**
 * ???????????????????????????????????????????????????????????????????????????????????????????????????
 * ??????????????????????????????
 * 2011-08-03 ?????????????????????????????????????????????????????????????????????????????????????????????
 * ????????????????????????
 */
public class ReportPlugin extends ChartModulePanel implements Printable {

    // ??????????????????????????????????????????
    static final String FONT_FAMILY =
         "font-family:'????????????',SansSerif,'MS P????????????';";

    int [] bodys;
    JEditorPane editorPane = null;
    File htmldir = new File( System.getProperty("app.temp") ); //HTML?????????????????????????????????

    ChannelData channelData;
    ChartData chartData;
    ReportSpecificPanel spec = new ReportSpecificPanel();
    AspectType [] aspectTypes;
    private static int IMAGE_SIZE = 132;
    NatalChart chart;
    ChartConfig cc = new ChartConfig();
    ChartAnalyzer analyzer;
    Map<Integer,Body> baseBodyMap;
    static final int [] MODERN_BODYS = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,
        URANUS,NEPTUNE,PLUTO,AC,MC
    };
    static final int [] CLASSIC_BODYS = {
        SUN,MOON,MERCURY,VENUS,MARS,JUPITER,SATURN,AC,MC
    };
    List<Aspect> aspectList; //????????????????????????????????????
    List<Body> bodyList;
    BalloonHandler wbh = new BalloonHandler();
    WordBalloon wordBalloon = new WordBalloon(wbh);
    GetPointMouseHandler mouseHandler = new GetPointMouseHandler();
    HtmlTag tag = new HtmlTag();

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public void init() {
        setLayout( new GridLayout(0,1) );
        editorPane = new JEditorPane();
        JScrollPane scrollPane = new JScrollPane( editorPane );
        editorPane.setEditable( false );
        editorPane.addHyperlinkListener( new HyperlinkHandler() );
        editorPane.addMouseListener(mouseHandler);
        add(scrollPane);
        editorPane.setFocusable( true );
        setFocusable( false );
        super.getChartConfig(cc); //???????????????????????????????????????cc?????????
        spec = new ReportSpecificPanel();
        //spec.setCuspUnkownHouseSystem( "" + cc.getCuspUnknownHouseSystem() );
        spec.setHouseSystemCode( cc.getHouseSystemCode() );

        Preference specificPref = new Preference();
        spec.getPreference( specificPref );
        specificPref.setProperty( "CuspUnknownHouseSystem", config.getProperty("CuspUnknownHouseSystem"));
        specificPref.setProperty( "PrioritizeSolar", config.getProperty("PrioritizeSolar"));
        
        setDefaultSpecific( specificPref );
        chart = new NatalChart( cc, 0 ); // 0???????????????ID
        // ????????????????????????????????????DB??????????????????????????????
        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                //??????????????????????????????????????????????????????????????????
                if ( editorPane.getSelectedText() == null )
                    iframe.contentSelected();
            }
        });
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    @Override
    public CustomizePanel getSpecificCustomizePanel() {
        //calc();
        return spec;
    }

    /**
     * ???????????????Natal???????????????????????????????????????????????????????????????????????????
     * ?????????Natal????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public void setData( ChannelData channelData ) {
        System.out.println("setData()");
        this.channelData = channelData;
        this.chartData = channelData.get(0);
        chartData.setTabIcon();
        if ( chartData.getSelectedIndex() < 0 ) return;
        calc();
    }


    void calc() {
        if ( chartData == null ) return;
        Natal natal;
        TimePlace timePlace;
        if ( chartData.getSelectedIndex() < 0 ) {
            natal = new Natal();
            natal.setTimePlace( chartData.getTransit() );
            timePlace = chartData.getTransit();
        } else {
            Data data = chartData.getSelectedData();
            natal = data.getNatal();
            timePlace = data.getTimePlace();
        }
        cc.setHouseSystemCode( spec.getHouseSystemCode() );
        chart.setTimePlace( timePlace );
        int [] BASE_BODYS = spec.isModernSystem() ?
                                   MODERN_BODYS    :   CLASSIC_BODYS;
        baseBodyMap = chart.getBodyMap( BASE_BODYS );
        if ( baseBodyMap.get( AC ) == null )
            baseBodyMap.put( AC, chart.getBody( CUSP1 ) );
        if ( baseBodyMap.get( MC ) == null )
            baseBodyMap.put( MC, chart.getBody( CUSP10 ) );

        analyzer = new ChartAnalyzer( baseBodyMap );
        File htmlFile = new File( htmldir, "index.html" );
        StringBuilder posBuf = new StringBuilder( 5000 );

        //////////////
        // ?????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????????????????
        posBuf.append("<table border=0 cellspacing=0 cellpadding=0>");
        for ( int i = 0; i < bodys.length; i++ ) {
            Body p = chart.getBody( bodys[i] );
            if ( p == null) continue;
            String angle = AngleConverter.getSignAngle2(p.getSignAngle());
            posBuf.append("<tr><td align=right>");
            if ( p.house > 0 ) posBuf.append( p.house).append("???");
            posBuf.append("</td>")
                  .append("<td align=left>")
                  .append("<a href='#body ").append(p.id).append("'>")
                  .append( tag.getPlanetTag(p) )
                  .append( tag.getSignTag(p) )
                  .append("</a>")
                  .append(angle);
            if ( p.lonSpeed < 0 )
                posBuf.append( tag.getRetrogradeTag() );
            posBuf.append("</td>").append("</tr>\n");
        }
        posBuf.append("</table>\n\n");
        //System.out.println("posBuf.length = " + posBuf.length() );
        //?????????????????????????????????????????????
        StringBuilder bodyBuf = new StringBuilder( 1024 * 5 );
        bodyBuf.append(
         "<table border=0 cellspacing=0 cellpadding=0 width=128><tr><td bgcolor=Black>"
        + "<table border=0 cellspacing=1 cellpadding=2 width=100%>"
        + "<tr><td align=center bgcolor=White><a href='#planetsTable'>"
        + "????????????</a></td></tr><tr><td valign=top bgcolor=White>")
        .append(posBuf.toString())
        .append("</td></tr></table></td></tr></table>\n" );

        //////////////
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????
        StringBuilder cuspBuf = new StringBuilder(5500);
        if ( chart.getBody( CUSP1 ) != null ) {
            cuspBuf.append(
            "<table border=0 cellspacing=0 cellpadding=0 align=right><tr>"
            + "<td bgcolor=Black>"
            + "<table border=0 cellspacing=1 cellpadding=1 width=150>"
            + "<tr><td bgcolor=White align=center><a href='#keyword ?????????'>"
            + "?????????</a></td><td bgcolor=White align=center>??????</td></tr>" );

            for ( int i = CUSP1, j = 1; i <= CUSP12; i++, j++ ) {
                Body p = chart.getBody( i );
                cuspBuf.append(
                   "<tr><td align=right bgcolor=White valign=middle>"
                 + "<table border=0 cellspacing=0 cellpadding=0><tr>"
                 + "<td align=right width=35>").append(j).append("???</td>")
                 .append("<td align=right width=16>")
                 .append( tag.getSignTag(p) ).append("</td>")
                 .append("<td style='align=right' width=30>")
                 .append( AngleConverter.getSignAngle2( p.getSignAngle() ))
                 .append( "</td></tr></table>"
                          + "</td>"
                          + "<td bgcolor=White valign=middle>" );
                java.util.List<Body> hp = analyzer.getHouseInPlanets( j );
                if ( hp.size() > 0 ) {
                    for(int n=0; n < hp.size(); n++) {
                        cuspBuf.append( tag.getPlanetTag( hp.get(n) ) );
                    }
                }
                cuspBuf.append("</td></tr>\n");
            }
            //?????????????????????????????????
            cuspBuf.append("<tr><td bgcolor=White align=center colspan=2>"
                    + "<a href='#keyword ?????????'>?????????</a>")
            .append( chart.getHouseSystemName() )
            .append( "</a></td></tr>"
                    + "</table>"
                    + "</td></tr></table>\n\n");
        }
        //System.out.println( "cuspBuf.length = " + cuspBuf.length() );

        ///////////////////////
        // ????????????????????????????????????????????????????????????
        int [] asp_bodys = spec.getAspectNatalBodyIDs(); //?????????????????????????????????
        bodyList = chart.getBodyList( asp_bodys );
        aspectList = AspectFinder.getAspects( bodyList, aspectTypes );
        StringBuilder tightBuf = new StringBuilder( 2200 );
        StringBuilder looseBuf = new StringBuilder( 2200 );
        StringBuilder noaspBuf = new StringBuilder( 200 );
        tightBuf.append("<table border=0 cellspacing=0 cellpadding=0>");
        looseBuf.append("<table border=0 cellspacing=0 cellpadding=0>");
        StringBuilder aspBuf = new StringBuilder( 210 );
        for ( int i = 0, j = 1; i < aspectList.size(); i++, j++ ) {
            Aspect a = aspectList.get(i);
            if ( a.isNoAspect() ) {
                noaspBuf.append(tag.getPlanetTag(a.p1));
            } else {
                aspBuf.setLength(0);
                aspBuf.append("<tr><td>")
                      .append("<a href='#aspect ").append(i).append("'>")
                      .append(tag.getPlanetTag(a.p1))
                      .append(tag.getAspectTag(a))
                      .append(tag.getPlanetTag(a.p2))
                      .append("</a>");
                //????????????????????????????????????????????????
                int err = (int)a.error;
                if ( (a.error - err)>0) err++;
                aspBuf.append("&nbsp;").append(err)
                      .append("</td></tr>");
                if ( a.tight )
                    tightBuf.append( aspBuf.toString() );
                else
                    looseBuf.append( aspBuf.toString() );
            }
        }
        tightBuf.append("</table>");
        looseBuf.append("</table>");

        StringBuilder aspfBuf = new StringBuilder( 6000 );
        aspfBuf.append("<table border=0 cellspacing=0 cellpadding=0><tr><td bgcolor=Black>")
               .append("<table border=0 cellspacing=1 cellpadding=2 width=140>");
        if ( noaspBuf.length()>0 ) { //????????????????????????????????????
            aspfBuf.append("<tr><td align=center colspan=2 bgcolor=White>"
            + "<a href='#keyword ?????????????????????'>?????????????????????</a></th></tr>")
            .append("<tr><td colspan=2 bgcolor=White>")
            .append( noaspBuf.toString() ).append("</td></tr>");
        }
        aspfBuf.append("<tr><td align=center colspan=2 bgcolor=White>"
                + "<a href='#keyword ???????????????'>???????????????</a></th></tr>"
                + "<tr><td width=70 bgcolor=White align=center>?????????</td>"
                + "<td width=70 align=center bgcolor=White>????????????</td></tr>"
                + "<tr><td valign=top bgcolor=White>")
                .append( tightBuf.toString() )
                .append("</td><td valign=top bgcolor=White>")
                .append( looseBuf.toString() ).append("</td></tr></table>")
                .append("</td></tr></table>");

        /////////////////////
        // ????????????????????????????????????????????????????????????????????????
        //
        StringBuilder dataBuf = new StringBuilder( 400 );
        dataBuf.append("<table border=0 cellspacing=0 cellpadding=0>");
        if(natal.getJob().length()>0)
            dataBuf.append("<tr><td align=right style='font-size:110%'>")
                   .append( natal.getJob() ).append("</td></tr>");
        if(natal.getGender() > Natal.NONE) {
            String gen = natal.getGender() == Natal.MALE ? "???" : "???";
            dataBuf.append("<tr><td align=right style='font-size:110%'>")
                   .append(gen).append("</td></tr>");
        }
        if ( natal.getMemo().length() > 0 )
            dataBuf.append("<tr><td align=right>")
                   .append( natal.getMemo() ).append("</td></tr>");

        String date = timePlace.getFormattedDate();
        if( timePlace.getTime() == null ) {
            String time = config.getProperty( "DefaultTime" );
            date += "<br><span style='font-size:100%'>( " + time + " ?????????)</span>";
        }
        dataBuf.append("<tr><td align=right style='font-size:110%'>")
               .append(date).append("</td></tr>");
        String placeName = timePlace.getPlaceName();
        if(placeName.length()==0) placeName = "?????????????????????";
        dataBuf.append("<tr><td align=right style='font-size:110%'>")
               .append(placeName).append("</td></tr>");
        if (timePlace.getLongitude() != null ) {
            dataBuf.append("<tr><td align=right' style='font-size:110%'>")
            .append( AngleConverter.getFormattedLongitude( timePlace.getLongitude() ))
            .append( "<br>" )
            .append( AngleConverter.getFormattedLatitude( timePlace.getLatitude() ))
            .append("</td></tr>");
        }
        if ( timePlace.getTime() == null )
            dataBuf.append("<tr><td align=right style='font-size:110%'>?????????????????????</td></tr>");
        if ( timePlace.getLongitude() == null )
            dataBuf.append("<tr><td align=right style='font-size:110%'>????????????????????????</td></tr>");
        String tz = timePlace.getFormattedTimeZone();
        int dst_offset = timePlace.getCalendar().get(Calendar.DST_OFFSET);
        if ( dst_offset > 0 ) {
            tz = tz.concat("<br>????????????????????????");
        }
        if ( tz.length() > 0 )
            dataBuf.append("<tr><td align=right style='font-size:100%'>")
            .append(tz).append("</td></tr>");
        dataBuf.append("<tr><td align=right>")
               .append( spec.isModernSystem() ? "?????????????????????" : "??????????????????" )
               .append("</td></tr></table");

        ///////////////////////////
        // ????????????????????????????????????
        //
        StringBuilder defBuf = new StringBuilder( 2048 );
        defBuf.append(
          "<table border=0 cellspacing=0 cellpadding=0><tr><td bgcolor=Black>"
        + "<table border=0 cellspacing=1 cellpadding=2>"
        + "<tr><td colspan=4 align=center bgcolor=White>"
        + "<a href='#keyword ?????????'>?????????</a></td></tr>"
        + "<tr><td bgcolor=White align=center>??????</td>"
        + "<td bgcolor=White align=center>??????</td>"
        + "<td bgcolor=White align=center>T</td>"
        + "<td bgcolor=White align=center>L</td></tr>");
        for ( int i = 0; i < aspectTypes.length; i++ ) {
            AspectType atype = aspectTypes[i];
            defBuf.append("<tr valign=middle>")
            .append("<td bgcolor=White align=center>")
            .append( tag.getAspectTag(atype.aid) ).append("</td>")
            .append("<td bgcolor=White align=right>")
            .append((int)ASPECT_ANGLE[atype.aid]).append("</td>")
            .append("<td bgcolor=White align=right>")
            .append((int)atype.tightOrb).append("</td>")
            .append("<td bgcolor=White align=right>")
            .append((int)atype.looseOrb).append("</td>")
            .append("</tr>\n");
        }
        defBuf.append("</table></td></tr></table>");

        ////////////////////
        // ??????????????????????????????????????????????????????????????????
        //
        StringBuilder elemBuf = new StringBuilder( 3500 );
        elemBuf.append("<table border=0 cellspacing=0 cellpadding=0 width=140><tr><td bgcolor=Black>");
        elemBuf.append("<table border=0 cellspacing=1 cellpadding=2 width=100%>\n");
        java.util.List<Body[]> elemList = analyzer.getElementsTable();
        String [] tableHeadder = { "???","???","???","???","??????","??????","??????","???","???" };
        for ( int i = 0; i < elemList.size(); i++ ) {
            if ( i == 0 ) elemBuf.append("<tr><td bgcolor=White align=center colspan=3><a href='#keyword ?????????'>?????????</a></td></tr>");
            if ( i == 4 ) elemBuf.append("<tr><td bgcolor=White align=center colspan=3><a href='#keyword ?????????'>?????????</a></td></tr>");
            if ( i == 7 ) elemBuf.append("<tr><td bgcolor=White align=center colspan=3><a href='#keyword ?????????'>?????????</a></td></tr>");
            Body[] elemBodys = elemList.get(i);
            elemBuf.append("<tr valign=middle><td align=center bgcolor=White>").append(tableHeadder[i]).append("</td>")
            .append("<td align=center bgcolor=White>").append(elemBodys.length).append("</td>");
            elemBuf.append("<td bgcolor=White>");
            for ( int j = 0, k = 0; j < elemBodys.length; j++ ) {
                elemBuf.append( tag.getPlanetTag( elemBodys[j] ) );
                if ( k >= 4 ) { elemBuf.append("<br clear>\n"); k = 0; }
                else k++;
            }
            elemBuf.append("</td></tr>\n");
        }
        elemBuf.append("</table>");
        elemBuf.append("</td></tr></table>\n");

        /////////////////////////
        // ????????????????????????????????????????????????????????????
        //
        StringBuilder dispBuf = new StringBuilder( 330 );
        String png = "";
        try {
            png = new File( htmldir,"disp.png").toURI().toURL().toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ReportPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        dispBuf.append("<table border=0 cellspacing=0 cellpadding=0><tr><td bgcolor=Black>")
        .append("<table border=0 cellspacing=1 cellpadding=2>\n")
        .append("<tr><td align=center bgcolor=White><a href='#keyword ?????????????????????'>?????????????????????</a></td></tr>\n")
        .append("<tr><td bgcolor=White><img src='") //disp.png'")
        .append( png )
        .append("'")
        .append(" width=").append(IMAGE_SIZE )
        .append(" height=").append(IMAGE_SIZE )
        .append("></td></tr>")
        .append("</table>\n")
        .append("</td></tr></table>\n");

        /////////////////////
        // ????????????????????????
        //
        StringBuilder sabianBuf = new StringBuilder( 5000 );
        sabianBuf.append("<table border=0 cellspacing=0 cellpadding=0 width=470>"
        + "<tr><td bgcolor=Black>"
        + "<table border=0 cellspacing=1 cellpadding=1 width=100%>\n"
        + "<tr><td bgcolor=White colspan=2 align=center><a href='#keyword "
        + "????????????????????????'>????????????????????????</a></td></tr>\n");
        for ( int i = 0; i < bodys.length; i++ ) {
            Body p = chart.getBody( bodys[i] );
            if ( p == null ) continue;
            int angle = (int)p.getSignAngle();
            String txt = Sabian.getText(p.getSign(),angle,Sabian.JP);
            sabianBuf.append("<tr><td valign=middle bgcolor=White width=60>&nbsp;")
            .append("<a href='#body ").append( bodys[i] ).append("'>")
            .append(tag.getPlanetTag(p))
            .append(tag.getSignTag(p))
            .append("</a>")
            .append( String.format("%2d",angle) ).append("</td>")
            .append("<td bgcolor=White valign=top>")
            .append(txt).append("</td></tr>\n");
        }
        sabianBuf.append("</table>\n</td></tr></table>\n");

        ////////////////////////
        // ??????????????????(??????)???
        //
        boolean dignity_exist = false;
        StringBuilder dignityBuf = new StringBuilder( 800 );
        dignityBuf.append("<table border=0 cellspacing=0 cellpadding=0 width=100><tr><td bgcolor=Black>")
        .append("<table border=0 cellspacing=1 cellpadding=2 width=100%>\n")
        .append("<tr><td colspan=2 bgcolor=White align=center><a href='#keyword ??????'>??????</a></td></tr>");
        for(int i=0; i<bodys.length; i++) {
            Body p = chart.getBody( bodys[i] );
            if ( p == null ) continue;
            if ( p.id > PLUTO ) continue;

            StringBuilder sb = new StringBuilder();
            if ( Dignity.isDignity(p) )   sb.append("?????????");
            if ( Dignity.isExalt(p) )     sb.append("?????????");
            if ( Dignity.isDetriment(p) ) sb.append("?????????");
            if ( Dignity.isFall(p) )      sb.append("?????????");
            if ( sb.length()>0) {
                dignity_exist = true;
                sb.deleteCharAt(sb.length()-1);
                dignityBuf.append("<tr valign=middle><td bgcolor=White width=16>")
                .append(tag.getPlanetTag(p)).append("</td>")
                .append("<td bgcolor=White>").append( sb.toString() )
                .append("</td></tr>\n");
            }
        }
        dignityBuf.append("</table>\n")
        .append("</td></tr></table>");

        //////////////////////////////////////
        // ???????????????????????????????????????????????????
        //
        StringBuilder ascBuf = new StringBuilder( 1500 );
        ascBuf.append("<table border=0 cellspacing=0 cellpadding=0 width=115>\n");
        ascBuf.append("<tr><td bgcolor=Black>");
        ascBuf.append("<table border=0 cellspacing=1 cellpadding=2 width=100%>\n");
        Body ascBody = analyzer.getRisingPlanet();
        if ( ascBody != null ) {
            ascBuf.append("<tr valign=middle ><td bgcolor=White align=right>")
            .append("<a href='#keyword ?????????'>?????????</a></td><td  bgcolor=White>")
            .append( tag.getPlanetTag(ascBody) ).append("</td></tr>\n" );
        }
        Body rulerBody = analyzer.getRulerPlanet( spec.isModernSystem() );
        if ( rulerBody != null ) {
            ascBuf.append("<tr valign=middle><td bgcolor=White align=right>")
            .append("<a href='#keyword ?????????'>?????????</a></td><td  bgcolor=White>")
            .append(tag.getPlanetTag(rulerBody)).append("</td></tr>\n");
        }
        double [] moonface = analyzer.getMoonFace();
        if ( moonface != null ) {
            ascBuf.append("<tr><td bgcolor=White valign=middle align=right>")
            .append("<a href='#keyword ??????'>??????</a></td><td  bgcolor=White>")
            .append( (1+((int)moonface[0])) ).append("???<br>");
            String degress = String.format( "%3.1f", moonface[1] );
            ascBuf.append("<span style='font-size:90%'>(???")
            .append(degress).append(")</span>").append("</td></tr>\n");
        }
        java.util.List<Body> elevPlanet = analyzer.getElevatedPlanets();
        if(elevPlanet.size()>0) {
            ascBuf.append("<tr><td bgcolor=White valign=middle align=right><a href='#keyword ???????????????'>??????????????????</a></td><td bgcolor=White>");
            for(int i=0; i<elevPlanet.size(); i++) {
                ascBuf.append(tag.getPlanetTag(elevPlanet.get(i)));
                if((i % 4) == 3) ascBuf.append("<br>");
            }
            ascBuf.append("</td></tr>");
        }
        Body cp = analyzer.getCulminatedPlanet();
        if(cp != null) {
            ascBuf.append("<tr valign=middle><td bgcolor=White align=right>")
            .append("<a href='#keyword ??????????????????'>??????????????????</a></td><td bgcolor=White>")
            .append( tag.getPlanetTag(cp) ).append("</td></tr>");
        }
        ascBuf.append("</table>\n");
        ascBuf.append("</td></tr></table>");
        //System.out.println( "ascBuf.length = " + ascBuf.length());

        ///////////////////////////
        // ???????????????
        //
        StringBuilder houseSegmentBuf = null;
        String [] houseNames = { "??????????????????","???????????????","??????????????????" };
        //if ( chart.getBody( AC ) != null ) {
            houseSegmentBuf = new StringBuilder( 1200 );
            houseSegmentBuf.append("<table border=0 cellspacing=0 cellpadding=0><tr><td bgcolor=Black>")
            .append("<table border=0 cellspacing=1 cellpadding=2 >\n");
            for ( int j = 0; j < 3; j++ ) {
                java.util.List<Body> ascList = analyzer.getPlanetsByHouseType(j);
                houseSegmentBuf.append("<tr><td bgcolor=White align=right>")
                .append("<a href='#keyword ").append(houseNames[j]).append("'>")
                .append(houseNames[j])
                .append("</a></td>")
                .append("<td bgcolor=White valign=middle>");
                for ( int i = 0; i < ascList.size(); i++ ) {
                    houseSegmentBuf.append( tag.getPlanetTag( ascList.get(i) ) );
                }
                houseSegmentBuf.append("</td></tr>");
            }
            houseSegmentBuf.append("</table>\n")
            .append("</td></tr></table>");
        //System.out.println( "houseSegmentBuf.length = " + houseSegmentBuf.length());
        //}
        ////////////////////////
        // ??????????????????????????????
        //
        StringBuilder frame = new StringBuilder( 830 );
        frame.append("<table border=0 cellspacing=0 cellpadding=0 width=640>")
        .append("<tr valign=top><td colspan=3>%NAME%</td></tr>\n")
        .append("<tr valign=top>\n")
        .append("  <td>")
        .append("  <table border=0 cellspacing=0 cellpadding=0>")
        .append("  <tr valign=top>\n")
        .append("    <td align=right>")
        .append("    <table border=0 cellspacing=0 cellpadding=2>")
        .append("    <tr valign=top>")
        .append("      <td colspan=2 align=right>%DATA%</td>")
        .append("    </tr>")
        .append("    <tr valign=top align=right>")
        .append("      <td>%CUSP%</td>")
        .append("      <td>%ASC%%DIG%</td>")
        .append("    </tr>")
        .append("    <tr valign=top>")
        .append("    <td colspan=2>%HOUSESEG%</td>")
        .append("    </tr>")
        .append("    </table>\n")
        .append("    </td>")
        .append("    <td align=center>")
        .append("    <table border=0 cellspacing=0 cellpadding=2>")
        .append("    <tr valign=top>")
        .append("      <td>%BODY%</td>")
        .append("      <td align=right>%ELEM%%ORB%</td>")
        .append("    </tr>")
        .append("    </table>\n")
        .append("    </td>")
        .append("  </tr>")
        .append("  <tr valign=top>")
        .append("    <td style='padding-top:8px;' colspan=2>%SABI%</td>")
        .append("  </tr>")
        .append("  </table>\n")
        .append("  </td>")
        .append("  <td align=right>%DISPO%<div style='padding-top:8px'>%ASP%</div></td>")
        .append("</table>\n");
        String temp = "";
        String frametable = frame.toString();
        //System.out.println( "frame.length = " + frame.length());
        StringReplacer sr = new StringReplacer( frametable );
        /////////////////////
        // ??????????????????????????????
        //
        StringBuilder sb = new StringBuilder( 100 );
        sb.append("<span style='font-size:200%'>").append(natal.getName()).append("</span>");
        if(! natal.getKana().equals("")) {
            sb.append("<span style='font-size:100%'>???").append(natal.getKana()).append("???</span>");
        }
        sr.replace("%NAME%",sb);
        String vspace= "<span style='font-size:50%'>&nbsp;</font>";
        sr.replace("%DATA%", dataBuf.toString());
        if(ascBuf.length()>0) {
            sr.replace("%ASC%",ascBuf);
        }
        sr.replace("%CUSP%",cuspBuf);
        temp =dignity_exist ? vspace+dignityBuf.toString() : "";
        sr.replace("%DIG%",temp);
        sr.replace("%DISPO%",dispBuf);
        sr.replace("%BODY%",bodyBuf);
        sr.replace("%ELEM%",elemBuf);
        temp = houseSegmentBuf != null ? vspace+houseSegmentBuf.toString():"";
        sr.replace("%HOUSESEG%",temp);
        sr.replace("%ASP%",aspfBuf);
        sr.replace("%ORB%",vspace+defBuf);
        sr.replace("%SABI%",sabianBuf);

        sb = new StringBuilder( 35000 ); // ???????????????????????????????????????????????????
        sb.append("<html>\n").append("<head>\n").append("<style>\n")
          .append("body { font-size:11px; }\n" )
          .append(".asym { font-family:'AMATERU'; font-size:120%; font-weight:bold; }\n")
          .append("td { font-size:11px; ").append( FONT_FAMILY ).append(" }\n")
          .append("a { text-decoration:none; }\n")
          .append("</style>\n").append("</head>\n").append("<body>\n")
          .append(sr.toString())
          .append("</body>").append("</html>");

        //???????????????????????????????????????
        Dispositor disp = new Dispositor();
        int []staying = new int[10];

        for ( int i = SUN; i <= PLUTO; i++ ) {
            if ( chart.getBody( i ) == null ) continue;
            staying[i] = chart.getBody( i ).getSign();
        }

        int rulerType = spec.isModernSystem() ?
                          Dispositor.MODERN   :   Dispositor.CLASSIC;
        BufferedImage dispImage = disp.getGraph( staying, IMAGE_SIZE, rulerType );
        try {
            ImageIO.write( dispImage, "png", new File( htmldir,"disp.png") );
        } catch ( IOException e ) {
            Logger.getLogger(ReportPlugin.class.getName())
                    .log( Level.SEVERE, null, e );
        }

        //WinXP????????????????????????????????????????????????????????????????????????????????????????????????
        //????????????????????????????????????EUC???SJIS?????????????????????????????????????????????
//        PrintWriter stream = null;
//        try {
//            stream = new PrintWriter(htmlFile,"EUC-JP");
//            stream.write(sb.toString());
//        } catch ( IOException e ) {
//            Logger.getLogger(ReportPlugin.class.getName())
//                    .log( Level.SEVERE, null, e );
//        } finally {
//            try { stream.close(); } catch(Exception e) { }
//        }
        try {
            //setPage?????????editorPane???????????????????????????????????????????????????
            Document doc = editorPane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);
//            editorPane.setContentType( "text/html; charset=EUC-JP" );
//            editorPane.setPage(htmlFile.toURI().toURL());
            editorPane.setContentType( "text/html;" );
            editorPane.setText( sb.toString() );
        } catch ( Exception e ) {
            Logger.getLogger(ReportPlugin.class.getName())
                    .log( Level.SEVERE, null, e );
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     * updateColorSetting()??????????????????????????????
     */
    @Override
    public void updateSpecificSetting() {
        System.out.println("updateSpecificSetting()");
        cc.setHouseSystemCode( spec.getHouseSystemCode() );
        cc.setCuspUnkownHouseSystem( spec.getCuspUnknownHouseSystem() );
        cc.setPrioritizeSolar( spec.getPrioritizeSolar() );
        bodys = spec.getNatalBodyIDs();
        aspectTypes = spec.getAspectTypes();
        calc();
    }

    //???????????????????????????????????????????????????????????????
    class HyperlinkHandler implements HyperlinkListener {
        DictionaryRequest dictReq = NPTChartDictionaryTool.createRequest();

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if ( e.getEventType() != HyperlinkEvent.EventType.ACTIVATED )
                return;

            JEditorPane pane = (JEditorPane) e.getSource();
            String desc = e.getDescription();
            String [] temp = desc.split(" ");

            Point point = mouseHandler.getPoint();
            SwingUtilities.convertPointToScreen( point, pane );
            Dictionary dict = getDictionary();
            SabianDialogHandler sabi = getSabianDialogHandler();

            if ( temp[0].equals("#body")) {
                Body body = chart.getBody(Integer.parseInt(temp[1]));
                String caption = Caption.getBodyCaption(body);
                if ( ! dict.isVisible() && ! sabi.isVisible() ) {
                    //????????????????????????????????????????????????????????????????????????
                    wbh.setSelectedObject(body);
                    wordBalloon.show(caption, point);
                    return;
                }
                if ( sabi.isVisible() ) {
                    sabi.setBodyList(bodyList);
                    sabi.setSelect((int)body.lon);
                }
                if ( dict.isVisible() ) {
                    NPTChartDictionaryTool.getRequest(body, dictReq);
                    dictReq.setCaption(Caption.getBodyCaption(body));
                    dict.search( dictReq );
                }
            } else if ( temp[0].equals("#aspect") ) {
                Aspect a = aspectList.get( Integer.parseInt(temp[1]) );
                String caption = Caption.getAspectCaption(a);
                if ( ! dict.isVisible() ) {
                    wbh.setSelectedObject(a);
                    wordBalloon.show(caption, point);
                    return;
                } else {
                    NPTChartDictionaryTool.getRequest(a,dictReq);
                    dictReq.setCaption(Caption.getAspectCaption(a));
                    dict.search(dictReq);
                }
            } else if ( temp[0].equals("#keyword") ) {
                NPTChartDictionaryTool.getRequest(temp[1],dictReq);
                dictReq.setCaption(temp[1]);
                dict.search(dictReq);
                System.out.println("????????????????????? : " + temp[1] );
            }
        }
    }
    /**
     * ????????????????????????????????????????????????????????????
     */
    @Override
    public BufferedImage getBufferedImage( Dimension size ) {
        Dimension rect = editorPane.getPreferredSize();
        BufferedImage img = new BufferedImage( rect.width, rect.height,
            BufferedImage.TYPE_INT_RGB );
        Graphics g = img.getGraphics();
        g.setColor( editorPane.getBackground() );
        g.fillRect( 0, 0, img.getWidth(), img.getHeight() );

        editorPane.print( g );
        g.dispose();
        return img;
    }
    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????true????????????
     * ??????????????????????????????false????????????
     */
    @Override
    public boolean isImageServiceActivated() { return true; }
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public boolean isFixedImageSize() { return true; }

    /**
     * ????????????????????????????????????HTML???????????????????????????????????????????????????????????????
     * ???????????????
     */
    @Override
    public int print( Graphics g1, PageFormat pf, int pageIndex) {
        Dimension docRect = editorPane.getPreferredSize();
        double per = pf.getImageableWidth() / docRect.getWidth();
        if ( per >= 1. ) per = 1.;
        //???????????????
        if ( docRect.getHeight() * per > pf.getImageableHeight() ) {
            double per2 = pf.getImageableHeight()  / (docRect.getHeight() * per);
            per = per2 * per;
        }
        Graphics2D g = (Graphics2D)g1;
        if ( pageIndex == 0 ) {
            g.translate( pf.getImageableX(), pf.getImageableY() );
            if ( per < 1.0 ) {
                AffineTransform at = new AffineTransform();
                at.setToScale( per, per );
                g.transform( at );
            }
        } else {
            return Printable.NO_SUCH_PAGE;
        }
        editorPane.paint(g);
        return Printable.PAGE_EXISTS;
    }

    @Override
    public Printable getPainter() { return this; }
    @Override
    public boolean isPrintable() { return true; }


    @Override
    public String toString() { return "????????????"; }

    @Override
    public boolean isNeedTransit() { return false; }

    @Override
    public int getChannelSize() { return 1; }

    static final String [] channelNames = { "????????????" };

    @Override
    public String[] getChannelNames() { return channelNames; }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     * isPrintable()???false????????????null????????????
     */
    @Override
    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        if ( isPrintable() ) {
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add( OrientationRequested.PORTRAIT );
            aset.add( new Copies(1) );
            aset.add( new JobName("AMATERU", null) );
            return aset;
        }
        return null;
    }
    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    @Override
    public boolean isAnimationActivated() {
        return false;
    }

    //??????????????????????????????????????????????????????
    @Override
    public DictionaryActionFile getDictionaryAction() {
        return NPTChartDictionaryTool.DICTIONARY_ACTION_FILE;
    }

}

