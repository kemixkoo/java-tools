package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import xyz.kemix.xml.sign.jdk.key.DSAKeyPairGen;
import xyz.kemix.xml.sign.jdk.key.RSAKeyPairGen;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class JdkXmlEnvelopedSignTest {

    Document loadDoc(String path) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = this.getClass().getResourceAsStream(path);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(stream);
    }

    void console(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }

    void file(Document doc, File file) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(file)));
    }

    @Test
    public void test_valid() throws Exception {
        Document doc = loadDoc("/jdk/demo-enveloped.xml");
        assertNotNull(doc);

        JdkXmlEnvelopedSign sign = new JdkXmlEnvelopedSign();
        boolean valid = sign.valid(doc);
        assertTrue("Valid failure", valid);
    }

    @Test
    public void test_valid_format() throws Exception {
        Document doc = loadDoc("/jdk/demo-enveloped-format.xml");
        assertNotNull(doc);

        JdkXmlEnvelopedSign sign = new JdkXmlEnvelopedSign();
        boolean valid = sign.valid(doc);
        assertFalse("After format the Data, won't be valid yet", valid);
    }

    @Test
    public void test_sign_valid_DSA_IT() throws Exception {
        KeyPair dsaKeypair = new DSAKeyPairGen(1024).generateKey();
        doTestForSignatureMethod(dsaKeypair, SignatureMethod.DSA_SHA1);
    }

    @Test
    public void test_sign_valid_RSA_IT() throws Exception {
        KeyPair rsaKeypair = new RSAKeyPairGen(1024).generateKey();
        doTestForSignatureMethod(rsaKeypair, SignatureMethod.RSA_SHA1);
    }

    private void doTestForSignatureMethod(KeyPair keypair, String method) throws Exception {
        // File tmpFolder = new File(System.getProperty("java.io.tmpdir"), "enveloped");
        // tmpFolder.mkdirs();
        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadDoc("/demo.xml");
            assertNotNull(doc);

            JdkXmlEnvelopedSign sign = new JdkXmlEnvelopedSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);
            sign.sign(doc, keypair);

            // String name = "demo-enveloped_" + method.substring(method.lastIndexOf('#') + 1) + '-'
            // + dm.substring(dm.lastIndexOf('#') + 1);
            // file(doc, new File(tmpFolder, name));

            boolean valid = sign.valid(doc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);
        }
    }
}
