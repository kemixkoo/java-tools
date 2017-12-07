package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;

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
        Document doc = loadXmlDoc(FILE_SHOPPING);
        assertNotNull(doc);

        AbsXmlKeyStoreApacheDomSign sign = createSign();
        setKeyStoreSettings(sign);

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
}
