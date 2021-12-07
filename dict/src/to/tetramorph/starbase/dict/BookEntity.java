/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;

/**
 * 辞書一冊分のデータを格納するクラス。通常はBookインターフェイスを使って、
 * アクセスする。
 * @author 大澤義鷹
 */
class BookEntity implements Book {

    File file = null;

    Map<String,DictAction> actionMap;
    Map<String,ActionTranslator> transMap;
    DictNode rootNode;
    Properties hedderProp;
    boolean editable;
    String dictType;
//    public BookEntity( File file,
//                          Map<String,DictAction> actionMap,
//                          Map<String,ActionTranslator> transMap,
//                          DictNode bookNode,
//                          Properties hedderProp,
//                          boolean editable ) {
//        this.actionMap = actionMap;
//        this.transMap = transMap;
//        this.rootNode = bookNode;
//        this.hedderProp = hedderProp;
//        this.file = file;
//        this.editable = editable;
//    }

    public BookEntity( File file,
                          Properties hedderProp,
                          Map<String,DictAction> actionMap,
                          BookData data ) {
        this.actionMap = actionMap;
        this.transMap = data.map;
        this.rootNode = data.node;
        this.hedderProp = hedderProp;
        this.file = file;
        editable = hedderProp.getProperty("editable","true").equals("true");
        dictType = data.prop.getProperty("dictType","NPTChart");
    }
   /**
    * データ入力などの際に使用する、アクション選択のコンボボックスモデルを返す。
    * @return
    */
    @Override
    public MutableComboBoxModel getActionComboBoxModel() {
        Vector<ActionItem> v = new Vector<ActionItem>();
        for ( Iterator i = actionMap.keySet().iterator(); i.hasNext(); ) {
            String actionName = (String)i.next();
            String title = actionName;
            if ( transMap.get( actionName ) != null ) {
                title = transMap.get(actionName).get(actionName);
            }
            v.add( new ActionItem( actionName, title ) );
//            System.out.println("actionName = " + actionName + ", title = " + title );
        }
        return new DefaultComboBoxModel( v );
    }

    /**
     * 現在のアクション名のリストを返す。
     * @return
     */
    @Override
    public List<String> getActionList() {
        List<String> results = new ArrayList<String>();
        for ( Iterator<String> i = actionMap.keySet().iterator(); i.hasNext(); )
            results.add( i.next() );
        return results;
    }

    @Override
    public DictAction getAction( String action ) {
        return actionMap.get( action );
    }

    /**
     * 指定されたアクション名の各パラメターの値を自然言語に変換して返す。
     * @param action
     * @param key
     * @return
     */
    @Override
    public String getActionTitle( String action,String key ) {
        ActionTranslator at = transMap.get(action);
        if ( at == null ) throw new IllegalArgumentException("未定義のアクション");
        return at.get(key);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public DictNode getRootNode() {
        return rootNode;
    }
    @Override
    public Map<String,ActionTranslator> getTransMap() {
        return transMap;
    }
    @Override
    public Properties getBookProperties() {
        return hedderProp;
    }
    @Override
    public String getTitle() {
        return (String)rootNode.getUserObject();
    }
    @Override
    public boolean isEditable() {
        return getBookProperties().getProperty("editable","true")
                .equals("true");
    }
    @Override
    public String getDictType() {
        return dictType;
    }
}
