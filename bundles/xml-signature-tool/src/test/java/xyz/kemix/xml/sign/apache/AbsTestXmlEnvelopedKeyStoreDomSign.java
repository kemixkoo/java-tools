package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.AbsTestXmlSign;
import xyz.kemix.xml.sign.KeyStoreSetting;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public abstract class AbsTestXmlEnvelopedKeyStoreDomSign extends AbsTestXmlSign {

    @Override
    protected String getTestName() {
        return "keystore-enveloped";
    }

    protected abstract AbsXmlKeyStoreSign createSign();

    protected void doTestValid_PaymentInfo(String path, boolean flag) throws Exception {
        Document signedDoc = loadXmlDoc(path);
        assertNotNull(signedDoc);

        AbsXmlKeyStoreSign sign = createSign();

        setKeyStoreSettings(sign);

        boolean valid = sign.valid(signedDoc);
        if (flag) {
            assertTrue("Valid failure", valid);
        } else {
            assertFalse("Should be invalid", valid);
        }
    }

    protected void setKeyStoreSettings(AbsXmlKeyStoreSign sign) {
        KeyStoreSetting storeSetting = sign.getStoreSetting();
        storeSetting.setStoreType(KeyStoreUtil.JKS);
        URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-dsa.jks");
        storeSetting.setStoreUrl(storeUrl);
        storeSetting.setStorePassword(KeyStoreUtilTest.storePassword);
        storeSetting.setKeyAlias(KeyStoreUtilTest.keyAlias);
        storeSetting.setKeyPassword(KeyStoreUtilTest.keyPassword);
    }

    @Test
    public void test_sign_valid_IT() throws Exception {
        Document doc = loadXmlDoc("shopping.xml");
        assertNotNull(doc);

        AbsXmlKeyStoreSign sign = createSign();
        setKeyStoreSettings(sign);

        Document signedDoc = sign.sign(doc);

        file(signedDoc, new File(tempDir, "shooping-" + getTestName() + "-1.xml"));

        boolean valid = sign.valid(signedDoc);
        assertTrue("Valid failure", valid);

        // try sign again
        Document signedDoc2 = sign.sign(signedDoc);

        file(signedDoc2, new File(tempDir, "shooping-" + getTestName() + "-2.xml"));

        boolean valid2 = sign.valid(signedDoc2);
        assertTrue("Valid failure when sign again", valid2);
    }
}
