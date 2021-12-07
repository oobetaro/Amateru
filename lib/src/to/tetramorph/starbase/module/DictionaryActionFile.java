/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.module;

import java.net.URL;

/**
 * 辞書のリソースおよび辞書タイプ名を格納するクラス。
 * @author 大澤義鷹
 */
public class DictionaryActionFile {
    private URL url;
    private String name;
    public DictionaryActionFile( URL url, String name ) {
        this.url = url;
        this.name = name;
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
