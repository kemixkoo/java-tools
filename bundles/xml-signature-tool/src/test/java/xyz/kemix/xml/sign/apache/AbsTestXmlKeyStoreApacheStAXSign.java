package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;

import xyz.kemix.xml.sign.IXmlSign;

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
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + FILE_SHOPPING);
        assertNotNull(stream);

        AbsXmlKeyStoreApacheStAXSign sign = createSign();
        setKeyStoreSettings(sign);

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

}
