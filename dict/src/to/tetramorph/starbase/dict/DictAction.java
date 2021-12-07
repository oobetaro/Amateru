/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * プラグイン側から提供される送信アクションのデータを一個分格納するクラス。
 * @author 大澤義鷹
 */
public class DictAction {
    private Map<String,Set<String>> paramMap = new LinkedHashMap<String,Set<String>>();
    private String actionName;
    /**
     * オブジェクトを作成する。
     * @param actionName "SelectedBody"等、アクション名を指定する。
     */
    public DictAction( String actionName ) {
        this.actionName = actionName;
    }
    /**
     * 指定されたフィールドキーとその値をこのオブジェクトに登録する。
     * @param key "planet","sign"等を指定する。
     * @param values "sun,mon,mer"などカンマで区切られた文字列を指定。
     */
    public void setParam( String key, String values ) {
        Set<String> vset = new LinkedHashSet<String>();
        for ( String value : values.split(",") )
            vset.add( value );
        paramMap.put( key, vset );
    }

    /**
     * 指定されたフィールドキーに対する値の列挙を返す。
     * @param key "planet"を指定すると、"sun","mon",,,等。
     */
    public Iterator<String> paramIterator( String key ) {
        return paramMap.get(key).iterator();
    }

    /**
     * このオブジェクトのアクション名を返す。
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * このオブジェクトにアクション名をセットする。
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    /**
     * このオブジェクトに登録されているパラメターの数を返す。
     */
    public int getParamCount() {
        return paramMap.size();
    }
    /**
     * パラメターのキー列挙を返す。
     */
    public Iterator<String> keyIterator() {
        return paramMap.keySet().iterator();
    }
}
