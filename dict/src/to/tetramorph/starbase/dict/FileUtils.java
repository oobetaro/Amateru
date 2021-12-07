/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package to.tetramorph.starbase.dict;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * 辞書機能が使う様々なファイル操作メソッド。
 * @author 大澤義鷹
 */
class FileUtils {
    /**
     * 配布用の辞書ファイルはZIP形式で圧縮され、内部に二つのエントリーを持つ。
     * １．オプション情報で、編集許可/禁止、コピペ許可/禁止、閲覧に必要な
     * ハッシュされたパスワード情報等をPropertiesで表現したもの。<br>
     * ２．本文。通常はXMLデータだが、編集禁止の場合は、CipherUtilsで難読化した
     * ものを指定する。
     * このメソッドは、１，２ともに種別など関知せず、ただのバイト配列とみなして、
     * zip書庫に出力するだけ。
     * なおこのメソッドで作成したZIPファイルは、ZIP解凍ツールなどを使って解凍
     * できないことがあるかもしれない。しかしユーザがファイルの中身を編集する
     * のは色々な意味で好ましい事ではないので、ツールが使えないならむしろ好都合
     * である。が、手持ちのツールで解凍してみるとちゃんと解凍できる。
     * @param file 出力先ファイル名
     * @param options オプション情報
     * @param body 本文ファイル(XMLや難読化された文字列のbyte配列
     */
    static void saveZip( File file,Properties options,byte [] body) 
            throws FileNotFoundException, IOException {
        ZipOutputStream zos = null;
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        zos = new ZipOutputStream(bos);
        //オプションを書き込み
        zos.putNextEntry(new ZipEntry("options"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        options.storeToXML(baos, "AMATERU Book file options");
        byte[] temp = baos.toByteArray();
        zos.write(temp, 0, temp.length);
        zos.closeEntry();
        //本文を書き込み
        ZipEntry entry = new ZipEntry("document");
        entry.setSize( body.length );
        zos.putNextEntry(entry);
        zos.write(body, 0, body.length);
        zos.closeEntry();
        zos.close();
    }
    /**
     * zipで保存された辞書ファイルを読みこむ。
     * @param file 辞書ファイル
     * @param options 空のプロパティを与える。値が書きこまれる。
     * @return 本文のバイトデータ。
     * @throws java.util.zip.ZipException
     * @throws java.io.IOException
     */
    static byte[] loadZip(File file, Properties options)
            throws ZipException, IOException {
        byte [] array = null;
        ZipFile zipfile = new ZipFile(file);
        ZipEntry entry = null;
        Enumeration en = zipfile.entries();
        while (en.hasMoreElements()) {
            entry = (ZipEntry) en.nextElement();
            if (entry.getName().equals("options")) {
                options.loadFromXML(zipfile.getInputStream(entry));
            } else if (entry.getName().equals("document")) {
                int size = (int)entry.getSize();
                array = new byte[size];
                BufferedInputStream stream =
                        new BufferedInputStream(zipfile.getInputStream(entry));
                int offset = 0;
                while ( stream.available() > 0 ) {
                    int sz = stream.read(array, offset, size);
                    offset += sz;
                }
                stream.close();
            } else {
                break;
            }
        }
        zipfile.close();
        return array;
    }
    //テキストファイルを読みこんでbyte[]で返す。
    static byte[] getDoc( File file) {
        BufferedInputStream stream = null;
        byte [] array = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            stream = new BufferedInputStream(new FileInputStream( file ));
            int offset = 0;
            while( stream.available() > 0) {
                int sz = stream.read( array, offset, 4096 );
                baos.write(array, offset, sz);
                offset += sz;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { stream.close(); } catch ( Exception e ) { }
        }
        return baos.toByteArray();
    }

    /**
     * バイト配列を単純にファイルに書き出す。
     * @param file 出力先ファイル
     * @param array バイト配列
     * @throws java.io.IOException
     */
    public static void saveByteArray(File file, byte [] array) throws IOException {
        BufferedOutputStream bos =
            new BufferedOutputStream(new FileOutputStream(file));
        bos.write(array, 0, array.length );
        bos.flush();
        bos.close();
    }

//    //デフォルトパスワードで暗号化された本を作る
//    static void test() throws Exception {
//        File file = new File("C:/Documents and Settings/おーさわよしたか/デスクトップ/book.zip");
//        Properties p = new Properties();
//        p.put("title","あまてる夫の辞書");
//        p.put("encrypt", "true");
//        byte [] buf = getDoc(new File("C:/sb/dict/src/resources/default6.xml"));
//        byte[] pw = CipherUtils.getDigest("Amateru Book File");
//        byte[] temp = CipherUtils.encrypt(pw, buf);
//        saveZip(file, p, temp);
//    }
//    //任意のパスワードで暗号化されたテスト用の本を作る
//    static void test2() throws Exception {
//        File file = new File("C:/Documents and Settings/おーさわよしたか/デスクトップ/secret_book.zip");
//        Properties p = new Properties();
//        p.put("title","あまてる子の秘密の辞書");
//        p.put("encrypt", "true");
//        p.put("need_pw","true"); //この文書はパスワード保護されていますよフラグ
//        String input_pw = "PayoPayo"; //このパスワードで暗号化(ユーザに入力させるもの)
//        byte[] pw = CipherUtils.getDigest(input_pw); //pwダイジェストを作る
//        //ダイジェストを文字列/バイト配列で相互変換できるかテスト(本来不要なもの)
////        String password = CipherUtils.getString(pw);
////        System.out.println("password = " + password);
////        byte [] test = CipherUtils.getBytes(password); //pwが元のbyte[]に戻るか検査
////        System.out.println("pw echo  = " + CipherUtils.getString(test));
//
//        byte [] buf = getDoc(new File("C:/sb/dict/src/resources/default5.xml"));
//        byte[] temp = CipherUtils.encrypt(pw, buf); //文書buf[]を暗号化
//        saveZip(file, p, temp);
//    }

//    public static void main( String args[] ) throws Exception {
//        test2();
////        Map<String,DictAction> actionMap = new LinkedHashMap<String,DictAction>();
////        String name = getTestDictActionMap(actionMap);
////        System.out.println("dictype name = " + name );
//    }

    /**
     * アクション情報のストリームを読みこみ、マップに格納して返す。
     * アクション情報は今はaction.xmlファイルから取得しているが、この情報は
     * プラグインの持ち物で、最終的にはプラグインから取得するようにする。
     * その際、引き渡しはInputStreamを使う予定。
     * @param stream アクション情報を読みこむストリーム。
     * @param actionMap 読みこみ結果返却用のマップ
     * @return 辞書タイプ名
     * ブックファイルのストリームではないので注意。
     * @return アクション名→DictActionオブジェクトを返す、マップ。
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public static Map<String,DictAction> getDictActionMap(InputStream actionStream )
                                         throws ParserConfigurationException,
                                                 SAXException,
                                                 IOException {
        Map<String,DictAction> actionMap = new LinkedHashMap<String,DictAction>();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document adoc = docBuilder.newDocument();
        adoc = docBuilder.parse( actionStream );
        Element root = adoc.getDocumentElement();
        NodeList actions = root.getElementsByTagName("action");
        for ( int a = 0; a < actions.getLength(); a++ ) {
            Element e = (Element)actions.item( a );
            String aName = e.getAttribute("name");
            DictAction action = new DictAction( aName );
            NodeList params = e.getElementsByTagName("param");
            for ( int i = 0; i < params.getLength(); i++ ) {
                Element ae = (Element)params.item(i);
                action.setParam( ae.getAttribute("key"),
                                 ae.getAttribute("values") );
            }
            actionMap.put(aName, action);
        }
        return actionMap;
    }

    /**
     * テスト用のActionMapを返す。
     * @param actionMap 値返却用のマップ。
     * @return name アクションの辞書タイプ名
     */
    public static Map<String,DictAction> getTestDictActionMap()
                                         throws ParserConfigurationException,
                                                 SAXException,
                                                 IOException {
        URL url = FileUtils.class.getResource("/resources/action.xml");
        InputStream stream = url.openStream();
        Map<String,DictAction> map = getDictActionMap( stream );
        stream.close();
        return map;
   }

   /**
    * ページタグを再帰的に解析して、DictNodeを編み上げる。
    * @param node ルートとなるpageエレメントのノード。
    * @return 再帰的に解析されツリーに編まれたノード。
    */
   private static DictNode traverse( Node node ) {
        NamedNodeMap map = node.getAttributes();
        if (map == null) return null;
        Node pageNode = map.getNamedItem("title");
        Node folderNode = map.getNamedItem("folder");
        if ( pageNode == null && folderNode == null ) return null;
        String title = pageNode == null ?
               folderNode.getNodeValue() : pageNode.getNodeValue();
        DictNode treeNode = new DictNode( title, pageNode != null );
        if ( pageNode != null ) { //ページの場合
            Node n = node.getFirstChild();
            String bodyText = n == null ? "" : n.getNodeValue();
            treeNode.setBody( bodyText );
            for (int i = 0; i < map.getLength(); i++) {
                Attr att = (Attr) map.item(i);
                treeNode.put(att.getName(), att.getValue());
            }
        }
        if ( node.hasChildNodes() ) {
            NodeList nodeList = node.getChildNodes();
            for ( int i = 0; i < nodeList.getLength(); i++ ) {
                DictNode childTreeNode
                    = traverse( nodeList.item(i) ); //再帰
                if ( childTreeNode != null ) {
                    treeNode.add( childTreeNode );
                }
            }
        }
        return treeNode;
    }

   /**
    * ブックファイルを読みこみBookDataオブジェクトで返す。
    * @param stream 本の読みこみ用ストリーム。これはXMLのドキュメントに接続され
    * ているストリームでなければならない。*.abfを指しているものは不可。
    * @return BookDataオブジェクト
    * @throws javax.xml.parsers.ParserConfigurationException
    * @throws org.xml.sax.SAXException
    * @throws java.io.IOException
    */
   static BookData getBookNode2( InputStream stream )
                                            throws ParserConfigurationException,
                                                    SAXException,
                                                    IOException{
        BookData data = new BookData();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc = docBuilder.parse( stream ); //XML木を作成
        Element rootElement = doc.getDocumentElement();
        //自然言語への変換マップを作成しtransMapに返す。
        NodeList list = rootElement.getElementsByTagName("translate");
        for ( int i = 0; i < list.getLength(); i++ ) {
            Element e = (Element)list.item(i);
            String actionName = e.getAttribute("action");
            if ( e.getFirstChild() != null ) {
                String transTable = e.getFirstChild().getNodeValue();
                ActionTranslator at = new ActionTranslator( transTable );
                data.map.put( actionName, at );
            }
        }

        //ドキュメント内のプロパティ情報を取得(editable,dictType)
        NodeList flagList = rootElement.getElementsByTagName("properties");
        Element fe = (Element)flagList.item(0);
        if ( fe != null ) {
            NodeList entrys = fe.getElementsByTagName("entry");
            for ( int i=0; i < entrys.getLength(); i++ ) {
                Element e = (Element)entrys.item(i);
                String key = e.getAttribute("key");
                String value = e.getTextContent();
                data.prop.setProperty(key, value);
                System.out.println("key = " + key + ",value = " + value);
            }
        }
        //ページタグをDictNodeの表現に変換
        NodeList nodeList = rootElement.getElementsByTagName("page");
        Element e = (Element) nodeList.item(0);
        data.node = traverse(e);
        return data;
    }

    /**
     * transMapの内容をtransrateタグとその内容にしてelementに追加する。
     * 辞書のファイル書き戻しのときに使用する。
     */
    private static void transMapToElement( Map<String,ActionTranslator> transMap,
                                                 Document doc,
                                                 Element element) {
        for (Iterator<String> i = transMap.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            String value = transMap.get(key).getTextValues();
            Element tag = doc.createElement("translate");
            tag.setAttribute("action", key);
            Text text = doc.createTextNode(value);
            tag.appendChild(text);
            element.appendChild(tag);
        }
    }

    private static void addPropertiesElement( Document doc,
                                                    Element element,
                                                    Properties p ) {
        Element tag = doc.createElement("properties");
        for ( Iterator i = p.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String value = p.getProperty(key);
            Element entry = doc.createElement("entry");
            entry.setAttribute("key", key);
            entry.setTextContent(value);
            tag.appendChild(entry);
        }
        element.appendChild(tag);
    }
    /**
     * ツリーモデルをpageタグで表現して、DOMツリーを作成しそれをファイルに保存。
     * @param treeNode 最初はルートノードを指定。
     * @param document ノード作成に必要
     * @param node 子ノードを追加するエレメント
     */
    private static Element dictNodeToElement( DictNode treeNode,
                                                   Document doc,
                                                   Element element ) {
        Element tag = doc.createElement("page");
        if ( treeNode.isPage() ) {
            for ( Iterator<String> i = treeNode.iterator(); i.hasNext(); ) {
                String name = i.next();
                tag.setAttribute( name, treeNode.get(name));
            }
            tag.setAttribute( "title", treeNode.getTitle() );
            Text text = doc.createTextNode( treeNode.getBody() );
            tag.appendChild(text);
        } else {
            tag.setAttribute( "folder", treeNode.getTitle() );
        }
        element.appendChild(tag);
        if ( ! treeNode.isLeaf() ) {
            for ( int i = 0; i < treeNode.getChildCount(); i++) {
                DictNode childNode = (DictNode) treeNode.getChildAt(i);
                Element n = null;
                if ( childNode != null ) {
                    n = dictNodeToElement(childNode, doc, tag); //再帰
                }
            }
        }
        return element;
    }

    /**
     * 本をファイルに保管する時のバイト表現を返す。
     * @param book 保存するブックオブジェクト
     * @param p ファイル保存条件。nullを指定したときは、Book#getBookProperties()
     * の値、つまり本にそのとき設定されている情報に基づいて返す。
     * "editable=treu/false"の値のみ認識する。
     * @return 本のバイト表現。まだ暗号化はされていない、平文のXMLを返す。
     * ただし編集禁止オプションがpで指定されている場合、XMLの中には、編集禁止
     * フラグが立てられている。構文エラー等があった場合はnullを返す。
     */
    public static byte [] getByteArray( Book book, Properties p ) {
        try {
            if ( p == null )
                p = book.getBookProperties();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            doc.setXmlStandalone(true);
            Element dictionary = doc.createElement("dictionary");
            doc.appendChild(dictionary);
            //変換テーブルを出力
            transMapToElement( book.getTransMap(), doc, dictionary );
            //内部プロパティを出力
            Properties prop = new Properties();
            prop.setProperty("dictType", book.getDictType());
            addPropertiesElement( doc, dictionary, prop);
            //ノードを出力
            Element el = dictNodeToElement(book.getRootNode(), doc, dictionary);
            //
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(baos);
            tf.transform(new DOMSource(doc), sr );
            return baos.toByteArray();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static final int ABF_FILE = 0;
    public static final int XML_FILE = 1;
    /**
     * ファイルの拡張子を調べてabfまたはxmlに応じてフラグを返す。
     * @exception abf,xmlどちらでもない場合
     * @param file
     * @return ABF_FILEまたはXML_FILEのいずれか。
     */
    public static int getFileType(File file) {
        String path = file.getAbsolutePath().toLowerCase();
        if ( path.matches(".*(\\.abf)$")) {
            return ABF_FILE;
        } else if ( path.matches(".*(\\.xml)$")) {
            return XML_FILE;
        }
        throw new IllegalArgumentException("アマテルブックファイルではありません");
    }
}
