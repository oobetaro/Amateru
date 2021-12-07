/*
 * 2011/07/14
 */
package to.tetramorph.starbase.dict;

import java.io.File;
import to.tetramorph.util.FileTools;
import to.tetramorph.util.Preference;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

/**
 * アマテル辞書のプロパティを保管する。to.tetramorph.starbase.Configと基本的に同じ
 * ものだが、保存先のファイル名が異なる。保存先はシステムプロパティのapp.properties
 * のフォルダ下のファイル"Dictionary.properties"。
 *
 * 同じクラスなら、わざわざ別にするのはどうよってところだが、辞書のパッケージとは
 * わけておきたい。というわけでこのクラスはpublicではない。
 *
 * ファイルへのセーブとロードは、完全にマニュアルで行う。シャットダウンフックにかけて
 * おくとか過去にやったが、異常終了したとき挙動が怪しい。<br>
 * このクラスを使う前には一度load()を呼び出す。保存されたファイルがあるならロードされ
 * る。<br>
 * キーと値をセットしたら、save()を呼び出せばファイルに保存されるが、セットのたびに
 * セーブするのもなんなので、ダイアログをdispose()するときなど、要所で行えばよい。
 *
 * @author 大澤義孝
 */
final class Config {
    /**
     * 使用ノードタイプやハウス分割法などユーザの設定保管用
     */
    public static Preference usr = new Preference();
    public static final String CONFIG_FILE_NAME = "Dictionary.properties";

    private static File configFile() {
        if ( getProperty("app.properties","").isEmpty() )
            throw new IllegalStateException("app.propertiesが設定されていない");
        File dir = new File( getProperty("app.properties") );
        if ( ! dir.isDirectory() || ! dir.exists() )
            throw new IllegalStateException("プロパティ保管フォルダが無い:"
                    + dir.getAbsolutePath());
        return new File( dir, CONFIG_FILE_NAME );
    }
    /**
     * @return 設定ファイルが存在しているときはtrueを返す。
     */
    public static boolean fileExists() {
        return configFile().exists();
    }


    /**
     * 設定情報を読み込む。
     * @throws IllegalStateException System.getProperty("app.properties")が設定されて
     * いない状態で呼び出されたとき。
     */
    public static void load() {
        usr.clear();
        FileTools.loadProperties(usr, configFile() );
        //load( configFile(), usr );
    }

    /**
     * 設定情報をapp.home/properties/ConfigUsr.xmlに書きだす。
     * @throws IllegalStateException System.getProperty("app.home")が設定されて
     * いない状態で呼び出されたとき。{app.home}/properties/フォルダが存在しないとき。
     */
    public static void save() {
        FileTools.saveProperties( usr, configFile(),
                "【編集厳禁】アマテル辞書設定ファイル　 http://tetramorph.to" );
    }

    /**
     * usrから"DefaultTime"のキーで値を取り出す。値が入っていないときは、
     * "00:00:00"として値を登録し、またシステムプロパティにも同じ内容をセットする。
     * このキーは頻繁にあちこちから参照するので、専用のメソッドを用意した。
     * @return 登録されているデフォルトタイム
     */
    public static String getDefaultTime() {
        if ( usr.getProperty( "DefaultTime", "" ).isEmpty() ) {
             usr.put( "DefaultTime", "00:00:00" );
             setProperty( "DefaultTime", "00:00:00" );
        }
        return usr.getProperty( "DefaultTime" );
    }
    /**
     * デフォルトタイムをdataにセットする。
     * System.setProperty( time )も行われる。
     * @param time "00:00:00"などの時刻文字列
     */
    public static void setDefaultTime( String time ) {
        usr.put( "DefaultTime", time );
        System.setProperty( "DefaultTime", time );
    }

//    public static void main(String[] args) {
//        System.setProperty("app.home","C:/Users/ohsawa/Ama");
//        Config.load();
//        Config.usr.setBoolean("Goo", false);
//        Config.usr.setString("Hoge", "HogeHoge");
//        System.out.println( getDefaultTime() );
//        setDefaultTime("12:00:00");
//        Config.save();
//    }
}
