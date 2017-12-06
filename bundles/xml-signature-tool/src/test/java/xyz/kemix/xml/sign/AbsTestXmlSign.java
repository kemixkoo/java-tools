package xyz.kemix.xml.sign;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public abstract class AbsTestXmlSign extends AbsTestParent {

    protected static final String PATH_XML = "/xml/";

    protected static final String FILE_SHOPPING_NAME = "shopping";

    protected static final String FILE_SHOPPING = FILE_SHOPPING_NAME + IXmlSign.EXT_XML;

    protected Document loadXmlDoc(String path) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + path);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(stream);
    }

    protected void console(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }

    protected void file(Document doc, File file) throws Exception {
        file.getParentFile().mkdirs();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(file)));
    }

    protected String getFilePart() {
        return FILE_SHOPPING_NAME + '-' + getTestName();
    }

    protected abstract String getTestName();

}
