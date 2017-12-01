package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsTestJdkXmlKeyStoreSign extends AbsTestJdkXmlSign {

    @Override
    String getTestName() {
        return "keystore";
    }

    void setStore(AbsJdkXmlKeyStoreSign sign, URL storeUrl) throws IOException {
        sign.setStoreUrl(storeUrl);
        sign.setStorePassword(KeyStoreUtilTest.storePassword);
        sign.setKeyAlias(KeyStoreUtilTest.keyAlias);
        sign.setKeyPassword(KeyStoreUtilTest.keyPassword);
    }

    @Test
    public void test_sign_valid_keyStore_JKS_DSA_IT() throws Exception {
        doTestKeyStoreForSignatureMethod(SignatureMethod.DSA_SHA1, KeyStoreUtil.JKS, "kemix-dsa.jks");
    }

    @Test
    public void test_sign_valid_keyStore_JKS_RSA_IT() throws Exception {
        doTestKeyStoreForSignatureMethod(SignatureMethod.RSA_SHA1, KeyStoreUtil.JKS, "kemix-rsa.jks");
    }

    @Test
    public void test_sign_valid_keyStore_PKCS12_DSA_IT() throws Exception {
        // doTestKeyStoreForSignatureMethod(SignatureMethod.DSA_SHA1, KeyStoreUtil.PKCS12,null);
    }

    @Test
    public void test_sign_valid_keyStore_PKCS12_RSA_IT() throws Exception {
        // doTestKeyStoreForSignatureMethod(SignatureMethod.RSA_SHA1, KeyStoreUtil.PKCS12,null);
    }

    private void doTestKeyStoreForSignatureMethod(String method, String storeType, String storeFileName) throws Exception {

        File tmpFolder = new File(System.getProperty("java.io.tmpdir"), getTestName());
        tmpFolder.mkdirs();
        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadXmlDoc("demo.xml");
            assertNotNull(doc);
            URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + storeFileName);
            assertNotNull(storeUrl);

            AbsJdkXmlKeyStoreSign sign = (AbsJdkXmlKeyStoreSign) createJdkXmlSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);

            sign.setStoreType(storeType);
            setStore(sign, storeUrl);

            Document signedDoc = sign.sign(doc);

            String name = "demo-" + getTestName() + "_" + method.substring(method.lastIndexOf('#') + 1) + '-'
                    + dm.substring(dm.lastIndexOf('#') + 1) + ".xml";
            file(signedDoc, new File(tmpFolder, name));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);
        }
    }

}
