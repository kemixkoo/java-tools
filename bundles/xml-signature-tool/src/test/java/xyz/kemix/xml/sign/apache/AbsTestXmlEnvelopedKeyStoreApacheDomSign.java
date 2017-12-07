package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.xml.security.signature.XMLSignature;
import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public abstract class AbsTestXmlEnvelopedKeyStoreApacheDomSign extends AbsTestXmlKeyStoreApacheSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-enveloped";
    }

    protected abstract AbsXmlKeyStoreApacheDomSign createSign();

    protected void doTestValid_PaymentInfo(String path, boolean flag) throws Exception {
        Document signedDoc = loadXmlDoc(path);
        assertNotNull(signedDoc);

        AbsXmlKeyStoreApacheDomSign sign = createSign();

        setKeyStoreSettings(sign);

        boolean valid = sign.valid(signedDoc);
        if (flag) {
            assertTrue("Valid failure", valid);
        } else {
            assertFalse("Should be invalid", valid);
        }
    }

    @Test
    public void test_sign_valid_IT() throws Exception {
        AbsXmlKeyStoreApacheDomSign sign = createSign();
        setKeyStoreSettings(sign);

        Document doc = loadXmlDoc(FILE_SHOPPING);
        assertNotNull(doc);

        Document signedDoc = sign.sign(doc);

        file(signedDoc, new File(tempDir, getFilePart() + IXmlSign.EXT_XML));

        boolean valid = sign.valid(signedDoc);
        assertTrue("Valid failure", valid);

        // try sign again
        Document signedDoc2 = sign.sign(signedDoc);

        file(signedDoc2, new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML));

        boolean valid2 = sign.valid(signedDoc2);
        assertTrue("Valid failure when sign again", valid2);
    }

    @Test
    public void test_sign_valid_signatureMethod_DSA_IT() throws Exception {
        for (String signatureMethod : new String[] { XMLSignature.ALGO_ID_SIGNATURE_DSA,
                XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256 }) {

            AbsXmlKeyStoreApacheDomSign sign = createSign();
            sign.setSignatureMethodURI(signatureMethod);

            setKeyStoreSettings(sign);

            Document doc = loadXmlDoc(FILE_SHOPPING);
            assertNotNull(doc);

            Document signedDoc = sign.sign(doc);

            file(signedDoc, new File(tempDir, getFilePart() + IXmlSign.EXT_XML));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure for " + signatureMethod, valid);

            // try sign again
            Document signedDoc2 = sign.sign(signedDoc);

            file(signedDoc2, new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML));

            boolean valid2 = sign.valid(signedDoc2);
            assertTrue("Valid failure when sign again for " + signatureMethod, valid2);
        }
    }

    @Test
    public void test_sign_valid_signatureMethod_RSA_IT() throws Exception {
        /*
         * Don't support the XXX__MGF1 and XXX_RIPEMD160
         */
        for (String signatureMethod : new String[] { XMLSignature.ALGO_ID_SIGNATURE_RSA, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA224,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512 }) {

            AbsXmlKeyStoreApacheDomSign sign = createSign();
            sign.setSignatureMethodURI(signatureMethod);

            setKeyStoreSettings(sign);
            URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-rsa.jks");
            sign.getStoreSetting().setStoreUrl(storeUrl);

            Document doc = loadXmlDoc(FILE_SHOPPING);
            assertNotNull(doc);

            Document signedDoc = sign.sign(doc);

            file(signedDoc, new File(tempDir, getFilePart() + IXmlSign.EXT_XML));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure for " + signatureMethod, valid);

            // try sign again
            Document signedDoc2 = sign.sign(signedDoc);

            file(signedDoc2, new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML));

            boolean valid2 = sign.valid(signedDoc2);
            assertTrue("Valid failure when sign again for " + signatureMethod, valid2);
        }
    }
}
