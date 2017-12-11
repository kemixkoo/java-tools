package xyz.kemix.xml.sign.mixed;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.apache.XmlKeyStorePartApacheStAXSign;
import xyz.kemix.xml.sign.jdk.XmlEnvelopedKeyStoreJdkDomSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-07
 *
 */
public class XmlJdkDomSignStAXValidMixedTest extends AbsTestXmlMixedSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-dom_stax";
    }

    @Test
    public void test_domSign_staxValid_DSA_IT() throws Exception {
        XmlEnvelopedKeyStoreJdkDomSign domSign = new XmlEnvelopedKeyStoreJdkDomSign();
        domSign.setSignatureMethod(SignatureMethod.DSA_SHA1);
        final URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-dsa.jks");
        doTest_domSign_staxValid(domSign, storeUrl);
    }

    @Test
    public void test_domSign_staxValid_RSA_IT() throws Exception {
        XmlEnvelopedKeyStoreJdkDomSign domSign = new XmlEnvelopedKeyStoreJdkDomSign();
        domSign.setSignatureMethod(SignatureMethod.RSA_SHA1);
        final URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-rsa.jks");
        doTest_domSign_staxValid(domSign, storeUrl);
    }

    private void doTest_domSign_staxValid(XmlEnvelopedKeyStoreJdkDomSign domSign, final URL storeUrl) throws Exception {
        // sign
        setStore(domSign, storeUrl);

        Document doc = loadXmlDoc(FILE_SHOPPING);
        assertNotNull(doc);

        Document signedDoc = domSign.sign(doc);
        File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);
        file(signedDoc, signedFile);

        // valid
        XmlKeyStorePartApacheStAXSign staxSign = new XmlKeyStorePartApacheStAXSign();
        setStore(staxSign, storeUrl);

        boolean valid = staxSign.valid(signedFile);
        assertTrue("Valid failure", valid);
    }
}
