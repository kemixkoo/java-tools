package xyz.kemix.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 * Created at 2017-12-07
 *
 */
public final class XMLFileUtil {

    public static Document loadDoc(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(stream);
        } finally {
            stream.close();
        }
    }

    public static Document loadDoc(File file) throws ParserConfigurationException, SAXException, IOException {
        return loadDoc(new BufferedInputStream(new FileInputStream(file)));
    }

    public static void saveDoc(Document doc, OutputStream output) throws Exception {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(output));
        } finally {
            output.close();
        }
    }

    public static void saveDoc(Document doc, File file) throws Exception {
        file.getParentFile().mkdirs();
        saveDoc(doc, new FileOutputStream(file));
    }

    public static void consoleDoc(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }

}
