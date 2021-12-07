/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.util;

import java.util.Properties;

/**
 * 辞書にリクエストを発行する際のリクエストデータ。
 * @author 大澤義鷹
 */
public class DictionaryRequest {
    Properties prop = new Properties();
    private String dictType;
    private String caption;
    private String actionCommand;

    public DictionaryRequest(String dictType) {
        this.dictType = dictType;
    }
    /**
     * プロパティと、キャプションを初期化する。辞書タイプ名はそのまま。
     */
    public void clear() {
        prop.clear();
        caption = null;
        setActionCommand(null);
    }
    /**
     * 辞書アクションのプロパティをセットする。
     * @param key
     * @param value
     */
    public void put(String key,String value) {
        prop.setProperty(key, value);
    }
    /**
     * 辞書アクションのプロパティを返す。
     * @param key プロパティキー
     * @return プロパティの値
     */
    public String get(String key) {
        return prop.getProperty(key);
    }
    /**
     * アトリビュートの値を返す。存在しないキーを指定した場合はdefを返す。
     */
    public String get(String key,String def) {
        String temp = get(key);
        if ( temp == null ) return def;
        return temp;
    }
    /**
     * 辞書タイプ名を返す。
     * @return the dictType
     */
    public String getDictType() {
        return dictType;
    }


    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the actionCommand
     */
    public String getActionCommand() {
        return actionCommand;
    }

    /**
     * @param actionCommand the actionCommand to set
     */
    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

}
