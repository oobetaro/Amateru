/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

/**
 * コンボボックスに入れるアイテム。選択で返すキーとそのキーを自然言語表現にした
 * 値の二つを保持する。toString()は自然言語のほうを返す。
 * @author 大澤義鷹
 */
class ActionItem {
    String action;
    String alias;
    public ActionItem( String action, String alias ) {
        this.action = action;
        this.alias = alias;
    }
    @Override
    public String toString() {
        return alias;
    }
    public String getAction() {
        return action;
    }
}
