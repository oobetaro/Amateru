/*
 * SearchOption.java
 *
 * Created on 2006/07/26, 5:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package to.tetramorph.starbase.lib;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;

/**
 * SearchFrameには、共有している検索オプションがあり、どのオプションがついているかを
 * 各検索モジュールに伝達するためのクラス。
 */
public class SearchOption {
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int NONE = 2;
    public static final int NATAL = 3;
    public static final int EVENT = 4;
    public static final int ORG = 5;
    public static final int COMPOSIT = 6;
    public static final int SEARCH_SUB_FOLDERS = 7;
    public static final int EXCLUDE_TIME_UNCERTAINTY = 8;
    public static final int OTHER_OPTIONS = 9;
    private static final String [] optionNames = { "MAIL","FEMALE","NONE",
    "NATAL","EVENT","ORG","COMPOSIT","SEARCH_SUB_FOLDER",
    "EXCLUDE_TIME_UNCERTAINTY","OTHER_OPTIONS" };
    private Map<Object,Boolean> optionMap = new HashMap<Object,Boolean>();
    private TreePath currentTreePath;
    private SearchResultReceiver receiver;
    private GregorianCalendar beginDate;
    private GregorianCalendar endDate;
    private Connection con;
    /**
     *  ここで検索結果の出力先も引き渡す必要があるのだが、まだ詰めていない。
     */
    public SearchOption() {
    }
    /**
     * 検索用のデータベースコネクションをセットする。
     */
    public void setConnection(Connection con) {
        this.con = con;
    }
    /**
     * 検索用のデータベースコネクションを返す。このコネクションはUPDATE,
     * INSERT,DELETEは禁止されている。
     */
    public Connection getConnection() {
        return con;
    }
    /**
     * Natal内のMALE,FEMALE,NONE,COMPOSIT,EVENT,NATALいずれかのキーを渡すと、
     * それらのオプションが有効かどうかを返す。
     * @param key オプションの種類を表すキー。時にはint、時にはString。
     * @return オプションが有効ならtrue
     */
    public Boolean isSelected(Object key) {
        return optionMap.get(key);
    }
    public void setOption(Object key,boolean selected) {
        optionMap.put(key,selected);
    }
    /**
     * 検索すべきフォルダのパスを返す。
     */
    public TreePath getCurrentTreePath() {
        return currentTreePath;
    }
    /**
     * 検索するフォルダへのパスをセットする
     */
    public void setCurrentTreePath(TreePath currentTreePath) {
        this.currentTreePath = currentTreePath;
    }
    /**
     * 検索結果の出力先レシーバーを返す。
     */
    public SearchResultReceiver getResultReceiver() {
        return receiver;
    }
    /**
     * 検索結果の出力先レシーバーをセットする。
     */
    public void setResultReceiver(SearchResultReceiver receiver) {
        this.receiver = receiver;
    }
    /** 検索日付を返す */
    public GregorianCalendar getBeginDate() {
        return beginDate;
    }
    /** 検索日付をセットする */
    public void setBeginDate(GregorianCalendar beginDate) {
        this.beginDate = beginDate;
    }
    /** 検索終了日付を返す */
    public GregorianCalendar getEndDate() {
        return endDate;
    }
    /** 検索終了日付をセットする */
    public void setEndDate(GregorianCalendar endDate) {
        this.endDate = endDate;
    }
    /**
     * TreePathをTREEPATH表のPATH用文字列にフォーマットして返す。<br>
     * treePathにnullが入った場合は""を返す。<br>
     * [root , MyChart] といったTreePathの場合rootは取り去られ "MyChart"を返す。<br>
     * [root ,MyChart ,Hoge]なら"MyChart/Hoge"を返す。いずれにせよ頭のrootは省略される。
     * 逆変換はDatabaseFrame.foundTreePath(String path)を使用する。
     */
    private static String getPathString(TreePath treePath) {
        //このメソッドはDatabaseにあるメソッドとまったく同じメソッド
        if(treePath == null) return "";
        Object [] path = treePath.getPath();
        StringBuffer sb = new StringBuffer();
        for(int i=1; i < path.length; i++) {
            sb.append(path[i].toString());
            sb.append("/");
        }
        if(sb.length() > 0 ) sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    /**
     * 現在選択されてるパス文字列を返す。
     */
    public String getCurrentPath() {
        return getPathString( getCurrentTreePath() );
    }
    /**
     * オプションの選択状態を識別するためのSQLで表現されたステートメントを返す。
     * それはWHERE句の後に続く条件判定式で次のような構文になっている。
     * "CHARTTYPE IN (?,?,?,?) AND GENDER IN (?,?) AND DATE >= ? AND DATE <= ?"
     */
    public String getExpression() {
        StringBuffer sb = new StringBuffer();
        String path = getPathString( getCurrentTreePath() );
        
        if ( isSelected( SEARCH_SUB_FOLDERS) ) {
            String str = null;
            if ( path.length() >= 1 ) {
                str = String.format(
                "( TREEPATH.PATH = '%s' OR TREEPATH.PATH LIKE '%s/%%') AND ", 
                path, path );
            } else {
                // ルートフォルダが指定された場合は上の方法では無理
                str = "TREEPATH.PATH LIKE '%' AND ";
            }
            sb.append( str );
        } else {
            sb.append("TREEPATH.PATH LIKE '" + path + "' AND ");        
        }
        
        if ( isSelected( EXCLUDE_TIME_UNCERTAINTY ) ) {
            sb.append("TIME IS NOT NULL AND ");
        }
        if ( ! isSelected( OTHER_OPTIONS ) ) {
            if ( sb.length() >= 4 ) sb.delete( sb.length() - 4, sb.length() );
            return sb.toString();
        }
        List<String> list = new ArrayList<String>();
        if (isSelected(NATAL)) list.add(Natal.NATAL);
        if (isSelected(EVENT)) list.add(Natal.EVENT);
        if (isSelected(ORG)) list.add("ORG");
        if (isSelected(COMPOSIT)) list.add(Natal.COMPOSIT);
        if (list.size()<4) {
            sb.append("CHARTTYPE IN (");
            for(int i=0; i<list.size(); i++) {
                sb.append("'" + list.get(i) + "'");
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append(") AND ");
        }
        List<Integer> genderList = new ArrayList<Integer>();
        if (isSelected(MALE)) genderList.add(Natal.MALE);
        if (isSelected(FEMALE)) genderList.add(Natal.FEMALE);
        if (isSelected(NONE)) genderList.add(Natal.NONE);
        if (genderList.size()<3) {
            sb.append("GENDER IN (");
            for(int i=0; i<genderList.size(); i++) {
                sb.append(genderList.get(i));
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append(") AND ");
        }
        if (getBeginDate() != null) {
            TimePlace t = new TimePlace();
            t.setCalendar( beginDate,TimePlace.DATE_ONLY );
            sb.append("JDAY >= " + t.getJDay() + " AND ");
        }
        if (getEndDate() != null) {
            TimePlace t = new TimePlace();
            t.setCalendar( endDate,TimePlace.DATE_ONLY );
            sb.append("JDAY <= "+ t.getJDay() + " AND ");
        }
        if(sb.length() >=4 ) sb.delete(sb.length()-4,sb.length());
        return sb.toString();
    }
    private static final String [] chartTypeNames =
                                        { "NATAL","EVENT","ORG","COMPOSIT" };
    /**
     * オプション情報文字列を返す。
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TreePath=");
        if(getCurrentTreePath() == null) sb.append("null");
        else sb.append(getCurrentTreePath().toString());
        sb.append(",");
        
        sb.append("BeginDate=");
        if(getBeginDate()==null) sb.append("null");
        else sb.append(getBeginDate().toString());
        sb.append(",");
        
        sb.append("EndDate=");
        if(getEndDate()==null) sb.append("null");
        else sb.append(getEndDate().toString());
        sb.append(",");
        
        for(int i=0; i<optionNames.length; i++) {
            sb.append(optionNames[i]+"=");
            if(isSelected(i)) sb.append("on");
            else sb.append("off");
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

//    public static void main( String [] args ) {
//        String path = "Hoge";
//        String str = String.format("( TREEPATH.PATH LIKE '%s' OR TREEPATH.PATH LIKE '%s/%%') AND", path, path );
//        System.out.println( str );
//
//    }
}
