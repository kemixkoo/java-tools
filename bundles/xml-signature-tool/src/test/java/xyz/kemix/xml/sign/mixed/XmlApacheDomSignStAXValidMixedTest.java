package xyz.kemix.xml.sign.mixed;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.xml.security.signature.XMLSignature;
import org.junit.Test;
import org.w3c.dom.Document;

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
public class XmlApacheDomSignStAXValidMixedTest extends AbsTestXmlMixedSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-dom_stax";
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
        // sign
        setStore(domSign, storeUrl);
        domSign.getNamesToSign().add(PAY_QNAME);

        Document doc = loadXmlDoc(FILE_SHOPPING);
        assertNotNull(doc);

        Document signedDoc = domSign.sign(doc);
        File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);
        file(signedDoc, signedFile);

        // valid
        setStore(staxSign, storeUrl);
        staxSign.getNamesToSign().add(PAY_QNAME);

        boolean valid = staxSign.valid(signedFile);
        assertTrue("Valid failure", valid);
    }

}
