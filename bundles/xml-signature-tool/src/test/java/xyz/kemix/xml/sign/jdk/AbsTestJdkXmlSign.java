package xyz.kemix.xml.sign.jdk;

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
public abstract class AbsTestJdkXmlSign {

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

    abstract AbsJdkXmlSign createJdkXmlSign();

    abstract String getTestName();

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
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"), getTestName());
        tmpFolder.mkdirs();
        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadDoc("/demo.xml");
            assertNotNull(doc);

            AbsJdkXmlSign sign = createJdkXmlSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);
            Document signedDoc = sign.sign(doc, keypair);

            String name = "demo-" + getTestName() + "_" + method.substring(method.lastIndexOf('#') + 1) + '-'
                    + dm.substring(dm.lastIndexOf('#') + 1);
            file(signedDoc, new File(tmpFolder, name));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);
        }
    }

}
