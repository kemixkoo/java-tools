package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class JdkXmlEnvelopedKeyStoreSignTest extends AbsTestJdkXmlKeyStoreSign {

    @Override
    protected AbsJdkXmlKeyStoreSign createJdkXmlSign() {
        return new JdkXmlEnvelopedKeyStoreSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + '-' + "enveloped";
    }

    @Test
    public void test_valid_keyPair_DSA() throws Exception {
        Document doc = loadXmlDoc(AbsTestJdkXmlKeyPairSign.PATH_JDK + getFilePart() + "_dsa-sha1-sha512" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsJdkXmlKeyStoreSign sign = createJdkXmlSign();

        sign.setDigestMethod(DigestMethod.SHA256);
        sign.setSignatureMethod(SignatureMethod.DSA_SHA1);

        sign.getKeystoreSetting().setStoreType(KeyStoreUtil.JKS);
        URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-dsa.jks");
        setStore(sign, storeUrl);

        boolean valid = sign.valid(doc);
        assertTrue("Valid failure for DSA", valid);
    }

    @Test
    public void test_valid_keyPair_RSA() throws Exception {
        Document doc = loadXmlDoc(AbsTestJdkXmlKeyPairSign.PATH_JDK + getFilePart() + "_rsa-sha1-sha512" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsJdkXmlKeyStoreSign sign = createJdkXmlSign();

        sign.setDigestMethod(DigestMethod.SHA256);
        sign.setSignatureMethod(SignatureMethod.RSA_SHA1);

        sign.getKeystoreSetting().setStoreType(KeyStoreUtil.JKS);
        URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-rsa.jks");
        setStore(sign, storeUrl);

        boolean valid = sign.valid(doc);
        assertTrue("Valid failure for RSA", valid);
    }
}
