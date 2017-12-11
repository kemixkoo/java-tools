package xyz.kemix.xml.sign.mixed;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.XMLFileUtil;
import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.apache.XmlEnvelopedKeyStorePartApacheDomSign;
import xyz.kemix.xml.sign.apache.XmlKeyStorePartApacheStAXSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-07
 *
 */
public class XmlStAXSignApacheDomValidMixedTest extends AbsTestXmlMixedSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-stax_dom";
    }

    @Test
    public void test_domSign_staxValid_DSA_IT() throws Exception {
        XmlEnvelopedKeyStorePartApacheDomSign domSign = new XmlEnvelopedKeyStorePartApacheDomSign();
        domSign.setSignatureMethodURI(XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256);

        XmlKeyStorePartApacheStAXSign staxSign = new XmlKeyStorePartApacheStAXSign();
        staxSign.setSignatureMethodURI(XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256);

        final URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-dsa.jks");
        doTest_domSign_staxValid(domSign, staxSign, storeUrl);
    }

    @Test
    public void test_domSign_staxValid_RSA_IT() throws Exception {
        XmlEnvelopedKeyStorePartApacheDomSign domSign = new XmlEnvelopedKeyStorePartApacheDomSign();
        domSign.setSignatureMethodURI(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);

        XmlKeyStorePartApacheStAXSign staxSign = new XmlKeyStorePartApacheStAXSign();
        staxSign.setSignatureMethodURI(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);

        final URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-rsa.jks");
        doTest_domSign_staxValid(domSign, staxSign, storeUrl);
    }

    private void doTest_domSign_staxValid(XmlEnvelopedKeyStorePartApacheDomSign domSign, XmlKeyStorePartApacheStAXSign staxSign,
            final URL storeUrl) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + FILE_SHOPPING);
        assertNotNull(stream);

        File file = new File(tempDir, FILE_SHOPPING);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(stream, fos);
        fos.close();
        assertTrue(file.exists());

        // sign
        setStore(staxSign, storeUrl);
        staxSign.getNamesToSign().add(PAY_QNAME);

        File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);
        staxSign.sign(file, signedFile);
        assertTrue(signedFile.exists());

        // valid
        setStore(domSign, storeUrl);
        domSign.getNamesToSign().add(PAY_QNAME);

        Document loadDoc = XMLFileUtil.loadDoc(signedFile);
        boolean valid = domSign.valid(loadDoc);
        assertTrue("Valid failure", valid);
    }

}
