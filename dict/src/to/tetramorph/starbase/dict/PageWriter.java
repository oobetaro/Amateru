/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.tree.TreePath;

/**
 * JEditorPaneにページの内容を表示する。
 * @author 大澤義鷹
 */
class PageWriter {
    JEditorPane editorPane;
    public PageWriter(JEditorPane editorPane ) {
        this.editorPane = editorPane;
    }
    public void cls() {
        editorPane.setText("");
    }

    public void writePage( TreePath path ) {
        if ( path == null ) { cls(); return; }
        DictNode page = TreeUtils.getDictNode(path);
        if ( ! page.isPage() ) { cls(); return; }
        HtmlFormatter html = new HtmlFormatter();
        html.addPage(page);
        html.setFooter();
        editorPane.setText(html.toString());
        editorPane.setCaretPosition(0); //スクロール位置を先頭に
    }

    public void writePages( String caption, List<DictNode> list ) {
        StringBuilder sb = new StringBuilder();
        HtmlFormatter html = new HtmlFormatter();
        html.addSearchResult(caption,list);
        html.setFooter();
        editorPane.setText(html.toString());
        editorPane.setCaretPosition(0); //スクロール位置を先頭に
    }
    /**
     * キーワードによる強調表示機能つき。
     * @param page
     * @param keyword
     */
    public void writePage( DictNode page, String keyword) {
        if ( page == null ) { cls(); return; }
        if ( ! page.isPage() ) { cls(); return; }
        HtmlFormatter html = new HtmlFormatter();
        html.addPage(page,keyword);
        html.setFooter();
        editorPane.setText(html.toString());
        editorPane.setCaretPosition(0); //スクロール位置を先頭に
    }
}
