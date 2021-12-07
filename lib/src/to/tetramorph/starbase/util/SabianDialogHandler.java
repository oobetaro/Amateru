/*
 * SabianDialogHandler.java
 *
 * Created on 2007/03/20, 17:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.util;

import java.util.List;
import to.tetramorph.starbase.lib.Body;

/**
 * SabianDialogはこのインターフェイスを実装しており、モジュールはこのインターフェイス
 * を使ってSabianDialogにアクセスする仕組み。直接SabianDialogのインスタンスを
 * 作成したりすることは禁止している。
 * @author 大澤義鷹
 */
public interface SabianDialogHandler {
    /**
     * 指定角度(0-359)のサビアンを選択する。
     */
    public void setSelect(int angle);

    /**
     * ダイアログを可視または不可視にする。
     */
    public void setVisible(boolean b);
    
    /**
     * ダイアログの可視化状態を返す。可視ならtrueを返す。
     */
    public boolean isVisible();
    
    /**
     * 天体シンボルを該当するサビアンに与える。
     */
    public void setBodyList(List<Body> bodyList);
    
    /**
     * 現在選択されている言語コードを返す。
     * @return Sabian.JPまたはSabian.ENのどちらか。
     */
    public int getLang();
}
