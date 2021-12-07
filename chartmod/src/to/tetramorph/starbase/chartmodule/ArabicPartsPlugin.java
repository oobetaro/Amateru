/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ArabicPartsPlugin.java
 *
 * Created on 2009/10/21, 19:02:26
 */

package to.tetramorph.starbase.chartmodule;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.logging.Logger;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import to.tetramorph.starbase.chartlib.NPTChartDictionaryTool;
import to.tetramorph.starbase.chartparts.ArabicNode;
import to.tetramorph.starbase.chartparts.ArabicParts;
import to.tetramorph.starbase.chartparts.GetPointMouseHandler;
import to.tetramorph.starbase.lib.Aspect;
import to.tetramorph.starbase.lib.Body;
import to.tetramorph.starbase.lib.Caption;
import to.tetramorph.starbase.lib.ChannelData;
import to.tetramorph.starbase.lib.ChartData;
import to.tetramorph.starbase.lib.Data;
import to.tetramorph.starbase.lib.Natal;
import to.tetramorph.starbase.lib.TimePlace;
import to.tetramorph.starbase.lib.Transit;
import to.tetramorph.starbase.lib.Unit;
import to.tetramorph.starbase.module.ChartModuleMode;
import to.tetramorph.starbase.module.ChartModulePanel;
import to.tetramorph.starbase.util.AngleConverter;
import to.tetramorph.starbase.util.AspectFinder;
import to.tetramorph.starbase.util.ChartConfig;
import to.tetramorph.starbase.util.Dictionary;
import to.tetramorph.starbase.util.DictionaryRequest;
import to.tetramorph.starbase.util.NPTChart;
import to.tetramorph.starbase.util.SabianDialogHandler;
import to.tetramorph.starbase.widget.CustomizePanel;
import to.tetramorph.starbase.widget.WordBalloon;
import to.tetramorph.util.Preference;
import static java.lang.String.format;
/**
 * アラビックパーツ・プラグイン。
 * @author 大澤義孝
 */
public class ArabicPartsPlugin extends ChartModulePanel implements Printable {
    // フォントはこの変数で指定する
    static final String FONT_FAMILY =
         "font-family:'メイリオ','MS Pゴシック',SansSerif;";
    // このフォントサイズを基準に%表記でサイズを指定している。この値を変えれば、
    // すべてのフォントサイズが相対的に変化する。
    static final String BASE_FONT_SIZE = "font-size:10px;";

    static final String AP_NATAL = "ネイタル";
    static final String AP_PROGRESS = "プログレス";
    static final String AP_TRANSIT = "トランジット";
    static final String [] GROUPS = { AP_NATAL,AP_PROGRESS,AP_TRANSIT };
    static final String [] channelNames = { AP_NATAL };
    ChannelData channelData;
    ChartData chartData;
    ChartConfig cc = new ChartConfig();
    ArabicSpecificPanel scp = new ArabicSpecificPanel();
    ArabicParts chart;
    Transit transit;
    BalloonHandler wbh = new BalloonHandler();
    WordBalloon wordBalloon = new WordBalloon(wbh);
    GetPointMouseHandler mouseHandler = new GetPointMouseHandler();
    List<ArabicNode> arabicTableList = null;
    HtmlTag tag = new HtmlTag();
    /** モジュール初期化時に呼ばれる */
    @Override
    public void init() {
        initComponents();
        super.getChartConfig( cc );
        scp.setHouseSystemCode( cc.getHouseSystemCode() );

        Preference specificPref = new Preference();
        scp.getPreference( specificPref );
        specificPref.setProperty( "CuspUnknownHouseSystem", config.getProperty("CuspUnknownHouseSystem"));
        specificPref.setProperty( "PrioritizeSolar", config.getProperty("PrioritizeSolar"));
        setDefaultSpecific( specificPref );

        chart = new ArabicParts(cc);
        editorPane.setFocusable( false );
        editorPane.addHyperlinkListener(new HyperlinkHandler());
        editorPane.addMouseListener(mouseHandler);
        editorPane.setFocusable( true );
        setFocusable( false );
        // 画面がクリックされたときDBのシャッターを閉じる
        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                //コピペ選択のためにクリックしたときはスキップ
                if ( editorPane.getSelectedText() == null )
                    iframe.contentSelected();
            }
        });
    }

    @Override
    public void setData(ChannelData channelData) {
        this.channelData = channelData;
        this.chartData = channelData.get(0);
        this.transit = channelData.getTransit();
        chartData.setTabIcon();
        if ( chartData.getSelectedIndex() < 0 ) return;
        calc();
    }

    void calc() {
        if ( chartData == null ) return;
        cc.setHouseSystemCode( scp.getHouseSystemCode() );
        chart.setProgressMode( scp.getProgressCode() );
        chart.setSource( scp.getArabicPartsSource() );
        chart.setRulerSystem( scp.isModernSystem() ?
            ArabicParts.MODERN : ArabicParts.CLASSIC );
        Data  natalData =
            chartData.getDataList().get( chartData.getSelectedIndex() );
        Natal natal = natalData.getNatal();
        chart.setData(natalData);
        chart.setTransit(transit);
        chart.setGroup(scp.getGroup());
        tag.setViewMode(scp.getDisplayMode());

        //
        arabicTableList = chart.calc();
        // アスペクト検出天体のリストを作る
        Iterator<Body> ite;
        List<Body> bodyList = new ArrayList<Body>();
        int [] useBodyIDs = scp.getAspectNatalBodyIDs();
        for ( ite = chart.getBodyMap().values().iterator(); ite.hasNext(); ) {
            Body body = ite.next();
            for ( int i = 0; i < useBodyIDs.length; i++ ) {
                if ( body.id == useBodyIDs[i] ) {
                    bodyList.add( body );
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>"
        + "a { text-decoration:none; color:#000000; }\n"
        + "a:link { color:#000000; }\n"
        + "a:active { color:#FF0000; }\n"
        + "a:visited { color:#000000; }\n"
        + ".asym { font-family:'AMATERU'; font-size:120%; font-weight:bold; }\n");
        sb.append("td { ").append( BASE_FONT_SIZE ).append(" ").append( FONT_FAMILY ).append("}\n")
        .append("body {").append( BASE_FONT_SIZE ).append(" ").append( FONT_FAMILY ).append(" }\n")
        .append("h3 { font-size:110%; }\n"
        + "</style></head><body>"
        + "<span style='font-size:200%'>");
        sb.append(natal.getName()).append("</span>");
        if ( ! natal.getKana().equals("") ) {
            sb.append("〔").append( natal.getKana() ).append("〕");
        }

        sb.append("<table border=0><tr valign=bottom><td>");
        if ( chart.getGroup() < NPTChart.TRANSIT )
            addDateTabel( natal, sb );
        sb.append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp</td><td>");
        addStatusTable( sb );
        sb.append("</td><td>&nbsp;&nbsp;&nbsp;&nbsp</td><td>");
        if ( chart.getGroup() > NPTChart.NATAL )
            addTransitDateTable(transit,sb);
        sb.append("</td></tr></table>");
        addArabicTable(arabicTableList,bodyList,sb);
        if ( scp.isShowVariableTable() )
            addVariableTable( chart.getBodyMap(), sb );
        sb.append("</body></html>");
        try {
            //setPageの前にeditorPaneを初期化してやらないと更新されない
            Document doc = editorPane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);
            editorPane.setContentType( "text/html;" );
            editorPane.setText( sb.toString() );
            editorPane.setCaretPosition(0); //行頭にスクロール

        } catch ( Exception e ) {
            Logger.getLogger(ArabicPartsPlugin.class.getName())
                    .log(Level.SEVERE,null,e);
        }
    }

    //変数テーブル一覧
    void addVariableTable( Map<String,Body> map, StringBuilder sb ) {
        sb.append( "<h3>変数一覧</h3>\n"
                  + "<table border=1>" );
        sb.append(tr("bgcolor='#E0E0FF'",th("感受点")+th("獣帯座標")+th("黄道座標")));
        for ( Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            Body b = map.get( key );
            sb.append("<tr>").append(td(key)).append("<td align=right>");
            sb.append("<a href='#body ").append( b.id ).append( "'>" );
            if ( ! key.startsWith("CUSP") ) {
                sb.append( tag.getPlanetTag(b) );
            }
            sb.append( tag.getSignTag(b));
            sb.append(AngleConverter.getSignAngle2(b.getSignAngle()));
            sb.append("</a></td><td align=right>");
            sb.append( format("%5.2f",Unit.truncate(b.lon, 2)) );
            sb.append("</td></tr>\n");
        }
        sb.append("</table>");

    }
/*****************************************************************************/
    // アラビック点と天体リストのアスペクトを検出し返す。
    List<Aspect> getAspects( ArabicNode an, List<Body> bodyList ) {
        List<Aspect> resultList = new ArrayList<Aspect>();
        if ( an.arabicBody == null ) return resultList;
        return AspectFinder.getAspects( bodyList,
                                         an.arabicBody,
                                         resultList,
                                         scp.getAspectTypes() );
    }
    // アラビック点の情報(名前、位置、アスペクト、式）をHTMLのタグにして返す。
    String getArabicTag(int row,ArabicNode n,
                          List<Body> bodyList,
                          List<Aspect> aspList) {
        StringBuilder sb = new StringBuilder();
        if ( n.errmsg == null ) {
            sb.append("<tr>").append(td(n.title)).append("<td align=right>");
            Body p = n.arabicBody;
            String angle = AngleConverter.getSignAngle2(p.getSignAngle());
            String sign = tag.getSignTag(p);
            sb.append(String.format("<a href='#arabic %d'>%s%s</a></td>",
                                       row, sign, angle));
        } else {
            if ( n.exp == null ) {
                sb.append("<tr>").append(td(n.title));
            } else {
                sb.append("<tr>");
                sb.append(td("colspan=2", format("%s = %s",n.title,n.exp)));
            }
        }
        //アスペクト表示
        for ( int i : scp.getAspectNatalBodyIDs()) {
            boolean found = false;
            for ( int j = 0; j<aspList.size(); j++ ) {
                Aspect asp = aspList.get(j);
                if ( asp.p1.id == i && ! asp.isNoAspect()) {
                    String col = ( asp.tight ) ? "#FFE0E0" : "white";
                    sb.append("<td align=center bgcolor='").append(col);
                    // aspectTableListの何行目の何番目かを書いておく
                    sb.append("'><a href='#aspect ").append(row).append(" ")
                            .append(j).append("'>");
                    sb.append(tag.getAspectTag(asp));
                    if ( scp.getDisplayMode() == HtmlTag.TEXT_MODE )
                        sb.append( asp.tight ? "!" : "" );
                    sb.append("</a></td>");
                    found = true;
                    break;
                }
            }
            if ( ! found )
                sb.append(td("&nbsp;"));
        }
        if ( n.errmsg == null ) {
            sb.append(td(n.exp));
        } else {
            if ( n.exp == null ) {
                sb.append(td(n.exp));
            } else {
                sb.append(td("style='color:Red'",n.errmsg));
            }
        }
        sb.append("</tr>\n");
        return sb.toString();
    }
    //テーブルヘッダー表示
    void addArabicTableHeadder( StringBuilder sb ) {
        sb.append("<tr bgcolor='#E0E0FF' valign=middle>").append(th("意味")).append(th("位置"));
        for ( int i : scp.getAspectNatalBodyIDs())
            sb.append( th(tag.getPlanetTag(i)) );
        sb.append(th("式")).append("</tr>\n");
    }

    List<List<Aspect>> aspectTableList; //全パーツのアスペクトの表を保管する。
    void addArabicTable( List<ArabicNode> list, List<Body> bodyList,StringBuilder sb ) {
        aspectTableList = new ArrayList<List<Aspect>>();
        sb.append("<H3>アラビックパーツ表（").append(getGroupName()).append("）</H3>\n");
        sb.append("<table border=1 style='white-space: nowrap;'>");
        addArabicTableHeadder(sb);
        for ( int row=0; row<list.size(); row++ ) {
            if ( row > 0 && (row % 10 == 0) ) {
                addArabicTableHeadder(sb);
//            if ( row == 25 || row == 60 || row == 95 ) {
//                sb.append("</table>\n");
//                sb.append("<br><br>\n");
//                sb.append("<table border=1 style='white-space: nowrap;'>");
//                addArabicTableHeadder(sb);
            }
            List<Aspect> aspList = getAspects( list.get(row), bodyList);
            aspectTableList.add(aspList);
            sb.append( getArabicTag( row,list.get(row), bodyList, aspList));
        }
        sb.append("</table>\n");
    }
/*****************************************************************************/

    static String DATA_TABLE_TAG =
       "<table border=0 cellspacing=0 cellpadding=0>\n";
    void addDateTabel( Natal natal,StringBuilder dataBuf ) {
        TimePlace timePlace = natal;
        dataBuf.append( DATA_TABLE_TAG );
        if(natal.getJob().length()>0)
            dataBuf.append(tr(td("align=right style='font-size:110%'",natal.getJob())));
        if(natal.getGender() > Natal.NONE) {
            String gen = natal.getGender() == Natal.MALE ? "男" : "女";
            dataBuf.append(tr(td("align=right style='font-size:110%'",gen)));
        }
        if(natal.getMemo().length()>0)
            dataBuf.append(tr(td("align=right","<div style='width:200px; text-align:left;'>" + natal.getMemo() + "</div>")));
        String date = timePlace.getFormattedDate();
        if(timePlace.getTime() == null) {
            String time = config.getProperty( "DefaultTime" );
            date += "<br>( " + time + " で計算)";
        }
        dataBuf.append(tr(td("align=right style='font-size:110%'", date)));
        String placeName = timePlace.getPlaceName();
        if(placeName.length()==0) placeName = "〔地名未登録〕";
        dataBuf.append(tr(td("align=right style='font-size:110%'",placeName)));
        if(timePlace.getLongitude() != null) {
            String pos = AngleConverter.getFormattedLongitude(timePlace.getLongitude()) + "<br>"
                + AngleConverter.getFormattedLatitude(timePlace.getLatitude());
            dataBuf.append(tr(td("align=right style='font-size:110%'",pos)));
        }
        if(timePlace.getTime() == null)
            dataBuf.append(tr(td("align=right style='font-size:110%'","〔時刻未登録")));
        if(timePlace.getLongitude() == null)
            dataBuf.append(tr(td("align=right style='font-size:110%'","〔緯経度未登録〕")));
        String tz = timePlace.getFormattedTimeZone();
        int dst_offset = timePlace.getCalendar().get(Calendar.DST_OFFSET);
        if ( dst_offset > 0 ) {
            tz = tz.concat("<br>〔夏時間実施中〕");
        }
        if(tz.length()>0)
            dataBuf.append(tr(td("align=right style='font-size:110%'",tz)));
//        if(natal.getMemo().length()>0)
//            dataBuf.append(tr(td("align=right style='font-size:110%'",natal.getMemo())));
        dataBuf.append("</table>");

    }
    void addTransitDateTable( Transit transit, StringBuilder sb ) {
        sb.append(DATA_TABLE_TAG);
        if ( transit.getName().length() > 1 ) {
            sb.append(tr(td("style='font-size:110%'",transit.getName())));
        } else {
            sb.append(tr(td("style='font-size:110%'","トランジット")));
        }
        if ( transit.getMemo().length() > 1 ) {
            sb.append(tr(td("style='font-size:100%'","<div style='width:200px;'>" + transit.getMemo() + "</div>")));
        }
        String date = transit.getFormattedDate();
        if(transit.getTime() == null) {
            String time = config.getProperty("DefaultTime");
            date += "<br>( " + time + " で計算)";
        }
        sb.append(tr(td("align=right style='font-size:110%'",date)));
        String placeName = transit.getPlaceName();
        if(placeName.length()==0) placeName = "〔地名未登録〕";
        sb.append(tr(td("align=right style='font-size:110%'",placeName)));
        if(transit.getLongitude() != null) {
            String pos = AngleConverter.getFormattedLongitude(transit.getLongitude()) + "<br>"
                + AngleConverter.getFormattedLatitude(transit.getLatitude());
            sb.append(tr(td("align=right style='font-size:110%'",pos)));
        }
        if(transit.getTime() == null)
            sb.append(tr(td("align=right style='font-size:110%'","〔時刻未登録〕")));
        if(transit.getLongitude() == null)
            sb.append(tr(td("align=right style='font-size:110%'","〔緯経度未登録〕")));
        String tz = transit.getFormattedTimeZone();
        int dst_offset = transit.getCalendar().get(Calendar.DST_OFFSET);
        if ( dst_offset > 0 ) {
            tz = tz.concat("<br>〔夏時間実施中〕");
        }
        if ( tz.length() > 0 )
            sb.append(tr(td("align=right style='font-size:110%'",tz)));
        sb.append("</table>");
    }

    void addStatusTable(StringBuilder sb) {
        sb.append(DATA_TABLE_TAG);
        chart.getGroup();
        sb.append(tr(td("表示チャート") + td( "：" + getGroupName())));
        if ( chart.getGroup() == NPTChart.PROGRESS )
            sb.append(tr(td("進行法")+td("："+chart.getProgressMethodName())));
        sb.append(tr(td("ノード") + td( "：" + chart.getNodeTypeName())));
        sb.append(tr(td("リリス") + td( "：" + chart.getApogeeTypeName())));
        sb.append(tr(td("ハウス分割法" + td( "：" + chart.getHouseSystemName()))));
        sb.append(tr(td("ルーラー決定法")
                +td( scp.isModernSystem() ? "：モダン十惑星式" : "：古典七惑星式")));
        sb.append("</table>");
    }

    String getGroupName() {
        return GROUPS[ chart.getGroup() ];
    }
//-----------------------------------------------------------------------------
// テーブルタグの挿入補助メソッド（あんまり凝りすぎない事）
    String tr(String td) {
        return "<tr>" + td + "</tr>\n";
    }
    String tr(String opt,String td) {
        return format("<tr %s>%s</tr>\n",opt,td);
    }
    String td(String opt,String value) {
        return format("<td %s>%s</td>",opt,value);
    }
    String td(String value) {
        return format("<td>%s</td>",value);
    }
    String th(String value) {
        return format("<th>%s</th>",value);
    }
//-----------------------------------------------------------------------------
//  HTMLのリンククリックイベント処理。バルーンの表示。辞書への要求。
    class HyperlinkHandler implements HyperlinkListener {
        DictionaryRequest dictReq = NPTChartDictionaryTool.createRequest();

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if ( e.getEventType() != HyperlinkEvent.EventType.ACTIVATED )
                return;

            JEditorPane pane = (JEditorPane) e.getSource();
            String desc = e.getDescription();
            System.out.println( "desc = " + desc );
            String [] temp = desc.split(" ");

            Point point = mouseHandler.getPoint();
            SwingUtilities.convertPointToScreen( point, pane );
            Dictionary dict = getDictionary();
            SabianDialogHandler sabi = getSabianDialogHandler();

            if ( temp[0].equals("#body") || temp[0].equals("#arabic") ) {
                Body body;
                String caption;
                String [] npt = {"N","P","T"};
                if ( temp[0].equals("#body") ) {
                    body = chart.getBody(Integer.parseInt(temp[1]),scp.getGroup());
                    caption = Caption.getBodyCaption(body,npt);
                } else {
                    body = arabicTableList.get(Integer.parseInt(temp[1])).arabicBody;
                    caption = Caption.getSabianCaption(body, sabi.getLang(), npt);
                }
                if ( ! dict.isVisible() && ! sabi.isVisible() ) {
                    //辞書もサビアン辞書も非可視のときはバルーンを表示
                    wbh.setSelectedObject(body);
                    wordBalloon.show(caption, point);
                    return;
                }
                if ( sabi.isVisible() ) {
                    sabi.setSelect((int)body.lon);
                }
                if ( dict.isVisible() ) {
                    NPTChartDictionaryTool.getRequest(body, dictReq);
                    dictReq.setCaption(Caption.getBodyCaption(body));
                    dict.search( dictReq );
                }
            } else if ( temp[0].equals("#aspect") ) {
                int row = Integer.parseInt(temp[1]);
                int col = Integer.parseInt(temp[2]);
                Aspect a = aspectTableList.get(row).get(col);
                String caption = Caption.getAspectSymbolCaption(a);
                System.out.println("caption = " + caption + ", row = " + row + ", col = " + col);
                if ( ! dict.isVisible() ) {
                    wbh.setSelectedObject(a);
                    wordBalloon.show(caption, point);
                    return;
                } else {
                    NPTChartDictionaryTool.getRequest(a,dictReq);
                    dictReq.setCaption(caption);
                    dict.search(dictReq);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        editorPane.setEditable(false);
        jScrollPane1.setViewportView(editorPane);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * このモジュール用の仕様設定パネルを返す。
     */
    @Override
    public CustomizePanel getSpecificCustomizePanel() {
        return scp;
    }
    /**
     * 仕様設定が変更されたときに呼び出される。
     * updateColorSetting()の次に呼び出される。
     */
    @Override
    public void updateSpecificSetting() {
        cc.setHouseSystemCode( scp.getHouseSystemCode() );
        cc.setCuspUnkownHouseSystem( scp.getCuspUnknownHouseSystem() );
        cc.setPrioritizeSolar( scp.getPrioritizeSolar() );
        calc();
    }
    private static final ChartModuleMode [] chartModuleModes = {
        new ChartModuleMode( AP_NATAL, "0,NATAL" ),
        new ChartModuleMode( AP_PROGRESS, "1,PROGRESS" ),
        new ChartModuleMode( AP_TRANSIT, "2,TRANSIT" ),
    };

    @Override
    public ChartModuleMode [] getModuleModes() {
        return chartModuleModes;
    }

    /**
     * アマテルのチャートメニュー→サブメニュー選択時に呼ばれる。
     * NPTのどれを表示するかのリクエスト。
     * 初期化時に一度呼ばれるだけ
     */
    @Override
    public void setModuleMode( ChartModuleMode mode ) {
        if ( mode == null ) return;
        String cmd = mode.getCommand().split(",")[0];
        scp.setGroup( Integer.parseInt(cmd) );
    }

    @Override
    public ChartModuleMode getModuleMode() {
        return chartModuleModes[ scp.getGroup() ];
    }

    @Override
    public boolean isNeedTransit() {
        return true;
    }

    @Override
    public int getChannelSize() {
        return 1;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }
    @Override
    public String toString() {
        return "アラビックパーツ";
    }
    /**
     * このモジュールはマニューバによるアニメーション操作は禁止。
     */
    @Override
    public boolean isAnimationActivated() {
        return false;
    }

    /**
     * 印刷を行う。表示されている画面は、印刷用紙A4に印刷するには大きすぎるため
     * 用紙サイズに合わせるように縮小してGraphicsオブジェクトに書きだす。
     * この印刷メソッドは主にHTMLやテキストを複数ページにわたって書きだすのに
     * 適している。
     *
     * @param grp プリンタに書きだすグラフィックオブジェクト
     * @param pf 用紙のサイズ等の情報
     * @param page 何ページ目の印刷かが指定
     * @return このページがまだ残りがあるか、もう終わりかを返す。残りがある
     * 場合は印刷サービスはpageをインクリメントして呼び出してくる。
     * また同じページ番号で複数回呼び出されることもある。
     */
    @Override
    public int print(Graphics grp, PageFormat pf, int page) {
        Dimension doc = editorPane.getPreferredSize(); //表示領域のサイズを取得
        double img_w = pf.getImageableWidth();
        double img_h = pf.getImageableHeight();
        double img_x = pf.getImageableX();
        double img_y = pf.getImageableY();

        double par_w = img_w / doc.getWidth();            //表示領域の横サイズを1とみなして縮尺率を求める
        double doc_sh = doc.getHeight() * par_w;          //縮小後の表示領域縦サイズを求める
        int page_count = (int)(doc_sh / img_h);            //それを印刷領域縦サイズで割れば何ページあるか求まる
        if ( page_count < (doc_sh / img_h ) ) page_count++; //端数切り上げ

        Graphics2D g = (Graphics2D)grp;

//        if ( page >= 3 ) return Printable.NO_SUCH_PAGE; //テスト用のリミッタ
        if ( page >= page_count ) return Printable.NO_SUCH_PAGE;

        g.translate( img_x, img_y - ( img_h * page ) );  //印刷領域の原点に座標をずらして、JEditorPaneには描画させる
        if ( par_w < 1.0 ) {
            //縮尺率が等倍より小さくなるときは描画されたページ全体に縮小処理を
            //施す。事実上ほとんどの場合、縮小される。
            AffineTransform at = new AffineTransform();
            at.setToScale( par_w, par_w );
            g.transform(at);
        }
        editorPane.paint( g ) ;
        // グラフィックオブジェクトは毎回刷新されるらしくtranslateなどのリカバリ
        // は不要。
        return Printable.PAGE_EXISTS;
    }
    /**
     * このモジュールは縦向き印刷がデフォルトである。
     * 印刷の際の用紙の向き、印刷部数などを定義したハッシュセットを返す。
     * isPrintable()がfalseのときはnullを返す。
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

    @Override
    public Printable getPainter() {
        return this;
    }
    @Override
    public boolean isPrintable() { return true; }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
