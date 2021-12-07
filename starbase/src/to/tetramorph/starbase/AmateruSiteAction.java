/*
 * AmateruSiteAction.java
 *
 * Created on 2008/11/21, 15:18
 *
 */

package to.tetramorph.starbase;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * ヘルプメニューの中の「アマテルのホームページ」のメニューアクション。
 * 2011-07-29 システムプロパティからサポートURLを取得する方法に変更。
 * @author 大澤義孝
 */
public class AmateruSiteAction extends AbstractAction {
    private Frame owner;

    /**
     * AmateruSiteAction オブジェクトを作成する
     */
    public AmateruSiteAction( Frame owner ) {
        this.owner = owner;
    }
    @Override
    public void actionPerformed( ActionEvent evt ) {
        if ( Desktop.isDesktopSupported() ) {
            try {
                Desktop.getDesktop().browse(
                    new URL( System.getProperty("support_url")).toURI() );
            } catch ( Exception e ) {
                error("ブラウザが使用できません。\n" + e.toString() );
            }
        } else
            error( "お使いのプラットホームでJavaからのブラウザ起動はサポートされていません。");
    }

    private void error( String msg ) {
        JOptionPane.showMessageDialog( owner, msg, "ブラウザの起動に失敗",
                                                    JOptionPane.ERROR_MESSAGE );
    }

}
