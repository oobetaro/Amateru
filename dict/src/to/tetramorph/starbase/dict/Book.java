/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.MutableComboBoxModel;

/**
 * 辞書にアクセスするインターフェイス。
 * 「アクション名」とはプラグイン側が用意するxmlファイルの中の&lt;action>
 * タグのnameエレメントの値。
 * @author 大澤義鷹
 */
interface Book {

    DictAction getAction(String action);

    /**
     * アクション選択のコンボボックスモデルを返す。
     * @return
     */
    MutableComboBoxModel getActionComboBoxModel();

    /**
     * 使用できるアクション名のリストを返す。
     * @return
     */
    List<String> getActionList();

    /**
     * 指定されたアクション名の各パラメターの値を自然言語に変換して返す。
     * @param action アクション名
     * @param key 変換したいキー。
     * @return 自然言語に変換されたキー。
     */
    String getActionTitle(String action, String key);

    File getFile();
    DictNode getRootNode();
    /**
     * この本のXML表現をバイト配列で返す。
     * @param p "editable=treu/false"の値のみ認識する。
     * @return 本の内容を表現したXMLのデータをバイト配列で返す。構文エラー等が
     * あった場合はnullを返す。
     */
    //byte [] getByteArray( Properties p);
    public Map<String,ActionTranslator> getTransMap();
    public Properties getBookProperties();
    /**
     * 本のタイトルを返す。タイトルとはノードのトップのUserObjectをStringとみな
     * したもの。
     * @return
     */
    public String getTitle();
    /**
     * この本が編集可能な場合はtrueを返す。編集禁止ならfalseを返す。
     * @return
     */
    public boolean isEditable();
    public String getDictType();
}
