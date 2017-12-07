package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.AbsTestXmlSign;
import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.KeyStoreSetting;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsTestXmlKeyStoreJdkDomSign extends AbsTestXmlSign {

    protected abstract AbsXmlKeyStoreJdkDomSign createJdkXmlSign();

    @Override
    protected String getTestName() {
        return "keystore";
    }

    void setStore(AbsXmlKeyStoreJdkDomSign sign, URL storeUrl) throws IOException {
        KeyStoreSetting keystoreSetting = sign.getKeystoreSetting();
        keystoreSetting.setStoreUrl(storeUrl);
        keystoreSetting.setStorePassword(KeyStoreUtilTest.storePassword);
        keystoreSetting.setKeyAlias(KeyStoreUtilTest.keyAlias);
        keystoreSetting.setKeyPassword(KeyStoreUtilTest.keyPassword);
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
    @Ignore
    public void test_sign_valid_keyStore_PKCS12_DSA_IT() throws Exception {
        // doTestKeyStoreForSignatureMethod(SignatureMethod.DSA_SHA1, KeyStoreUtil.PKCS12,null);
    }

    @Test
    @Ignore
    public void test_sign_valid_keyStore_PKCS12_RSA_IT() throws Exception {
        // doTestKeyStoreForSignatureMethod(SignatureMethod.RSA_SHA1, KeyStoreUtil.PKCS12,null);
    }

    private void doTestKeyStoreForSignatureMethod(String method, String storeType, String storeFileName) throws Exception {

        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadXmlDoc(FILE_SHOPPING);
            assertNotNull(doc);
            URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + storeFileName);
            assertNotNull(storeUrl);

            AbsXmlKeyStoreJdkDomSign sign = (AbsXmlKeyStoreJdkDomSign) createJdkXmlSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);

            sign.getKeystoreSetting().setStoreType(storeType);
            setStore(sign, storeUrl);

            Document signedDoc = sign.sign(doc);

            String name = getFilePart() + "_" + method.substring(method.lastIndexOf('#') + 1) + '-'
                    + dm.substring(dm.lastIndexOf('#') + 1);
            file(signedDoc, new File(tempDir, name + IXmlSign.EXT_XML));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);

            Document signedDoc2 = sign.sign(signedDoc);
            boolean valid2 = sign.valid(signedDoc2);
            // file(signedDoc, new File(tempDir, name + 2 + IXmlSign.EXT_XML));
            assertTrue("Valid failure again with DigestMethod: " + dm + ", signatureMethod: " + method, valid2);
        }
        System.out.println();
    }

}
