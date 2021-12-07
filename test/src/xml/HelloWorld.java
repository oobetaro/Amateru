/*
 * HelloWorld.java
 *
 * Created on 2008/11/28, 22:16
 *
 */

package xml;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * XMLの学習用。
 * @author 大澤義鷹
 */
public class HelloWorld {
    static File homeFile = new File(System.getProperty("user.home"),"デスクトップ");
    static File xmlFile = new File( homeFile,"HelloWorld.xml");
    /**  HelloWorld オブジェクトを作成する */
    public HelloWorld() {
    }
    
    public static void main(String[] args) throws Exception {
        // ドキュメントビルダーファクトリを生成。
        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbfactory.newDocumentBuilder();
        // 読みこんだファイルを解析してDocumentオブジェクトを取得
        Document doc = builder.parse( xmlFile );
        System.out.println("Namespace = " + doc.getNamespaceURI() );
        Element rootElement = doc.getDocumentElement();
        System.out.println("ルート要素のタグ名：" + rootElement.getTagName());
        System.out.println("***** ページリスト *****");
        // page要素のリストを取得
        NodeList list = rootElement.getElementsByTagName("page");
        for (int i=0; i < list.getLength() ; i++) {
            Element element = (Element)list.item(i); // pageを取得
            String id = element.getAttribute("id");  // id属性の値を取得
            NodeList titleList = element.getElementsByTagName("title"); // title要素のリストを取得
            Element titleElement = (Element)titleList.item(0); // title要素を取得
            // title要素の最初の子ノード（テキストノード）の値を取得
            String title = titleElement.getFirstChild().getNodeValue();
            // file要素のリストを取得
            NodeList fileList = element.getElementsByTagName("file");
            // file要素を取得
            Element fileElement = (Element)fileList.item(0);
            // file要素の最初の子ノード（テキストノード）の値を取得
            String file = fileElement.getFirstChild().getNodeValue();
            
            System.out.println("ID：" + id + "  " +
                "タイトル：" + title + "  " +
                "ファイル：" + file);
        }
    }
}
