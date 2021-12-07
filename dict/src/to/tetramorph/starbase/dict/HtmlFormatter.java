/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * 辞書のテキストをHTML形式に変換する。
 * @author 大澤義鷹
 */
class HtmlFormatter {
    private static String HEADDER;
    static {
        StringBuilder sb = new StringBuilder();
        URL url = HtmlFormatter.class.getResource("/resources/dict_headder.html");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while(reader.ready()) {
                sb.append(reader.readLine());
                sb.append("\n");
            }
            HEADDER = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { reader.close(); } catch(Exception e) { }
        }
        System.out.println("辞書ヘッダーファイル読みこみ");
    }
    public static void main(String [] args) {
        HtmlFormatter html = new HtmlFormatter();
        System.out.println( html.sb.toString() );
    }
    StringBuilder sb = new StringBuilder(200);
//    private static final String HEADDER =
//        "<html><head><style>" +
//        "a { text-decoration : none }" +
//        "body { margin-left:8px; margin-right:8px; font-family:'MS Pゴシック'; }" +
//        ".pagetitle { color:#006400; font-weight:bold; font-size:16pt; }"+
//        ".searchlist { font-size:14pt; }" +
//        "div.title1 { font-size:16pt; width:100%; background-color:white; }"+
//        "div.title2 { font-size:16pt; width:100%; background-color:#e0f0e0; }"+
//        "</style></head><body>";
    public HtmlFormatter() {
        setHeadder();
    }
    private void setHeadder() {
        sb.append( HEADDER );
    }
    /**
     * toString()を呼び出す前にこれを実行して、内部のHTML文を閉じること。
     */
    public void setFooter() {
        sb.append("</body></html>");
    }
    public void addSearchResult(String caption,List<DictNode> list ) {
        if ( caption != null )
            sb.append("リクエスト 〔"+caption+"〕<br>");
        sb.append("該当" + list.size() + "件<br>");
        sb.append("<ol>");
        for ( int i=0; i < list.size(); i++ ) {
            DictNode node = list.get(i);
            sb.append("<li><a class='searchlist' href='#jump" + i + "'>");
            sb.append( node.getPathString() );
            sb.append("</a>\n");
        }
        sb.append("</ol>\n");
        for ( int i=0; i < list.size(); i++ ) {
            addPage(list.get(i),i);
        }
    }

    private void addTitle( String title, Integer index ) {
        sb.append("<table border=0 cellspacing=0 width='100%' style='background-color:silver;'>");
        sb.append("<tr><td class='pagetitle'>");
        sb.append(title);
        sb.append("</td><td align='right'>");
        if ( index >= 0 ) {
            String up = (index - 1) < 0 ? "#TOP" : "#jump"+(index-1);
            String dw = "#jump" + (index + 1);
            sb.append("<a class='jump' href='#TOP'>■</a>");
            sb.append("<a class='jump' href='"+up+"'>▲</a>");
            sb.append("<a class='jump' href='"+dw+"'>▼</a>");
        }
        sb.append("</td></table>");
        sb.append("\n");
    }

    public void addPage(DictNode page) {
        addPage(page,-1);
    }

    public void addPage(DictNode page,String keyword) {
        String temp = page.getPathString();
        String path = temp.substring(0,temp.lastIndexOf("/"));
        String title = page.getTitle();
        addTitle(title, -1);
        sb.append("<div class='path'>");
        sb.append( path );
        sb.append("</div>");
        sb.append("<div class='doc'>");
        String text = StringUtils.escape(page.getBody());
        text = text.replaceAll( keyword, "<span class='strong'>"+keyword+"</span>");
//        System.out.println("keyword = " + keyword);
//        System.out.println(text);
        sb.append( text );
        sb.append("</div>");
    }

    public void addPage( DictNode page, Integer listIndex ) {
        String temp = page.getPathString();
        String path = temp.substring(0,temp.lastIndexOf("/"));
        String title = page.getTitle();
        if ( listIndex >= 0 ) {
            sb.append("<a name='jump" + listIndex + "'></a>");
        }
        addTitle(title, listIndex);
        sb.append("<div class='path'>");
        sb.append( path );
        sb.append("</div>");
        sb.append("<div class='doc'>");
        sb.append( StringUtils.escape( page.getBody() ) );
        sb.append("</div>");
    }
    @Override
    public String toString() {
        return sb.toString();
    }
    public URL getHtmlURL() throws IOException {
        File file = File.createTempFile("dicttemp", "html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        return file.toURI().toURL();
    }
}
