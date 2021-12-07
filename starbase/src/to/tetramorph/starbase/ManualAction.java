/*
 * ManualAction.java
 *
 * Created on 2008/11/21, 14:34
 *
 */

package to.tetramorph.starbase;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * アマテルのPDFマニュアルをブラウザで開く。Mainの中で使用されている。
 * @author 大澤義孝
 */
public class ManualAction extends AbstractAction {

    private Frame owner;

    /**
     * ManualAction オブジェクトを作成。
     */
    public ManualAction( Frame owner ) {
        this.owner = owner;
    }


    @Override
    public void actionPerformed( ActionEvent evt ) {
        if ( Desktop.isDesktopSupported() ) {
            try {
                File doc = new File( System.getProperty("app.doc"),"man.pdf");
                if ( doc.exists() )
                    Desktop.getDesktop().browse( doc.toURI() );
                else
                    error("マニュアルが見つかりません。");
            } catch ( Exception e ) {
                error("ブラウザが使用できません。\n" + e.toString() );
            }
        } else
            error( "お使いのプラットホームでブラウザ機能がサポートされていません。");

    }

    private void error( String msg ) {
        JOptionPane.showMessageDialog( owner, msg, "ブラウザの起動に失敗",
                                                    JOptionPane.ERROR_MESSAGE );
    }
}
