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
 * XML�̊w�K�p�B
 * @author ���V�`��
 */
public class HelloWorld {
    static File homeFile = new File(System.getProperty("user.home"),"�f�X�N�g�b�v");
    static File xmlFile = new File( homeFile,"HelloWorld.xml");
    /**  HelloWorld �I�u�W�F�N�g���쐬���� */
    public HelloWorld() {
    }
    
    public static void main(String[] args) throws Exception {
        // �h�L�������g�r���_�[�t�@�N�g���𐶐��B
        DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbfactory.newDocumentBuilder();
        // �ǂ݂��񂾃t�@�C������͂���Document�I�u�W�F�N�g���擾
        Document doc = builder.parse( xmlFile );
        System.out.println("Namespace = " + doc.getNamespaceURI() );
        Element rootElement = doc.getDocumentElement();
        System.out.println("���[�g�v�f�̃^�O���F" + rootElement.getTagName());
        System.out.println("***** �y�[�W���X�g *****");
        // page�v�f�̃��X�g���擾
        NodeList list = rootElement.getElementsByTagName("page");
        for (int i=0; i < list.getLength() ; i++) {
            Element element = (Element)list.item(i); // page���擾
            String id = element.getAttribute("id");  // id�����̒l���擾
            NodeList titleList = element.getElementsByTagName("title"); // title�v�f�̃��X�g���擾
            Element titleElement = (Element)titleList.item(0); // title�v�f���擾
            // title�v�f�̍ŏ��̎q�m�[�h�i�e�L�X�g�m�[�h�j�̒l���擾
            String title = titleElement.getFirstChild().getNodeValue();
            // file�v�f�̃��X�g���擾
            NodeList fileList = element.getElementsByTagName("file");
            // file�v�f���擾
            Element fileElement = (Element)fileList.item(0);
            // file�v�f�̍ŏ��̎q�m�[�h�i�e�L�X�g�m�[�h�j�̒l���擾
            String file = fileElement.getFirstChild().getNodeValue();
            
            System.out.println("ID�F" + id + "  " +
                "�^�C�g���F" + title + "  " +
                "�t�@�C���F" + file);
        }
    }
}
