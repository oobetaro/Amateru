/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * アマテルブックファイルを構文解析した結果を格納し受け渡しするためのクラス。
 * 階層状のノード、アクション名変換テーブル、プロパティならなる。
 * 
 * @author 大澤義鷹
 */
class BookData {
    DictNode node;
    Map<String,ActionTranslator> map = new LinkedHashMap<String,ActionTranslator>();
    Properties prop = new Properties();
    public BookData() {

    }
    
}
