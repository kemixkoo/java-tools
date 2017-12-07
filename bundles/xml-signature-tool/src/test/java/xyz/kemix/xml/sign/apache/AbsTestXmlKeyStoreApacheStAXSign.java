package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.xml.security.signature.XMLSignature;
import org.junit.Test;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public abstract class AbsTestXmlKeyStoreApacheStAXSign extends AbsTestXmlKeyStoreApacheSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-stax";
    }

    protected abstract AbsXmlKeyStoreApacheStAXSign createSign();

    protected void doTestValid_PaymentInfo(String path, boolean flag) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + path);
        assertNotNull(stream);

        AbsXmlKeyStoreApacheStAXSign sign = createSign();

        setKeyStoreSettings(sign);

        boolean valid = sign.valid(stream);
        if (flag) {
            assertTrue("Valid failure", valid);
        } else {
            assertFalse("Should be invalid", valid);
        }
    }

    @Test
    public void test_sign_valid_IT() throws Exception {

        AbsXmlKeyStoreApacheStAXSign sign = createSign();
        setKeyStoreSettings(sign);

        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + FILE_SHOPPING);
        assertNotNull(stream);

        File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);

        sign.sign(stream, new FileOutputStream(signedFile));

        boolean valid = sign.valid(new FileInputStream(signedFile));
        assertTrue("Valid failure", valid);

        // try sign again
        File signedFile2 = new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML);
        sign.sign(new FileInputStream(signedFile), new FileOutputStream(signedFile2));

        boolean valid2 = sign.valid(new FileInputStream(signedFile2));
        assertTrue("Valid failure when sign again", valid2);
    }

    @Test
    public void test_sign_valid_signatureMethod_DSA_IT() throws Exception {
        for (String signatureMethod : new String[] { XMLSignature.ALGO_ID_SIGNATURE_DSA,
                XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256 }) {

            AbsXmlKeyStoreApacheStAXSign sign = createSign();
            sign.setSignatureMethodURI(signatureMethod);
            setKeyStoreSettings(sign);

            InputStream stream = this.getClass().getResourceAsStream(PATH_XML + FILE_SHOPPING);
            assertNotNull(stream);

            File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);

            sign.sign(stream, new FileOutputStream(signedFile));

            boolean valid = sign.valid(new FileInputStream(signedFile));
            assertTrue("Valid failure for " + signatureMethod, valid);

            // try sign again
            File signedFile2 = new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML);
            sign.sign(new FileInputStream(signedFile), new FileOutputStream(signedFile2));

            boolean valid2 = sign.valid(new FileInputStream(signedFile2));
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

            AbsXmlKeyStoreApacheStAXSign sign = createSign();
            sign.setSignatureMethodURI(signatureMethod);

            setKeyStoreSettings(sign);
            URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-rsa.jks");
            sign.getStoreSetting().setStoreUrl(storeUrl);

            InputStream stream = this.getClass().getResourceAsStream(PATH_XML + FILE_SHOPPING);
            assertNotNull(stream);

            File signedFile = new File(tempDir, getFilePart() + IXmlSign.EXT_XML);

            sign.sign(stream, new FileOutputStream(signedFile));

            boolean valid = sign.valid(new FileInputStream(signedFile));
            assertTrue("Valid failure for " + signatureMethod, valid);

            // try sign again
            File signedFile2 = new File(tempDir, getFilePart() + 2 + IXmlSign.EXT_XML);
            sign.sign(new FileInputStream(signedFile), new FileOutputStream(signedFile2));

            boolean valid2 = sign.valid(new FileInputStream(signedFile2));
            assertTrue("Valid failure when sign again for " + signatureMethod, valid2);
        }
    }

}
