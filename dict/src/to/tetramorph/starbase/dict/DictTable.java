/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 各チャートモジュールの辞書アクション定義情報を格納するクラス。
 * to.tetramorph.starbase.Mainの中で、このオブジェクトは作成され、DictDialogに
 * 引き渡される。<br>
 * 辞書タイプ名：三重円用とか紫微斗数用とか、チャートモジュールが辞書ダイアログ
 * にむけて発行する、辞書アクションコマンドセットの名前。たとえばNPTChartなどと
 * 名前がつけられている。またアマテルブックファイルの中身にはこの名前が書かれて
 * いて、その辞書がどのモジュールに対応しているか判る仕組みになっている。
 * @author 大澤義鷹
 */
public class DictTable {
    private Map<String,String> name2type;
    private Map<String,Map<String,DictAction>> type2action;
    /**
     * オブジェクトを作成する。
     */
    public DictTable() {
        clear();
    }
    //内容を初期化し空にする。
    private void clear() {
        name2type = new HashMap<String,String>();
        type2action = new HashMap<String,Map<String,DictAction>>();
    }
    /**
     * 辞書データを登録する。
     * @param moduleName 「NPT三重円」とかモジュールの名前
     * @param dictType 「辞書タイプ名」。モジュールごとに任意に決められた名前。
     * @param url　「辞書アクションファイル」を指すURL。
     */
    public void add(String moduleName, String dictType, URL url) {
        name2type.put(moduleName,dictType);
        try {
            type2action.put( dictType,
                             FileUtils.getDictActionMap( url.openStream() ) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 辞書タイプ名から辞書アクションマップを返す。
     */
    public Map<String,DictAction> getDictActionMap( String dictType ) {
        Map<String,DictAction> map = type2action.get(dictType);
        return map;
    }
    /**
     * 辞書アクションコマンドを発行するチャートモジュールの判読用の名前の一覧を
     * 返す。発行しない、つまり辞書に対応しないモジュールもあるが、その名前は
     * このリストには含まれない。
     */
    public List<String> getModuleNameList() {
        List<String> list = new ArrayList<String>();
        Iterator<String> i;
        for ( i = name2type.keySet().iterator(); i.hasNext(); ) {
            list.add( i.next()) ;
        }
        return list;
    }

}
