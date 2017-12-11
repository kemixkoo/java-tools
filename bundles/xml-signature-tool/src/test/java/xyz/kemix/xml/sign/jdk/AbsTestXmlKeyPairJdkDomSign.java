package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.jdk.key.DSAKeyPairGen;
import xyz.kemix.xml.sign.jdk.key.RSAKeyPairGen;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsTestXmlKeyPairJdkDomSign extends AbsTestXmlJdkDomSign {

    protected abstract AbsXmlKeyPairJdkDomSign createJdkXmlSign();

    @Override
    protected String getTestName() {
        return "keypair";
    }

    @Test
    public void test_sign_valid_keyPair_DSA_IT() throws Exception {
        KeyPair dsaKeypair = new DSAKeyPairGen(1024).generateKey();
        doTestKeyPairForSignatureMethod(dsaKeypair, SignatureMethod.DSA_SHA1);
    }

    @Test
    public void test_sign_valid_keyPair_RSA_IT() throws Exception {
        KeyPair rsaKeypair = new RSAKeyPairGen(1024).generateKey();
        doTestKeyPairForSignatureMethod(rsaKeypair, SignatureMethod.RSA_SHA1);
    }

    protected void doTestKeyPairForSignatureMethod(KeyPair keypair, String method) throws Exception {
        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadXmlDoc(FILE_SHOPPING);
            assertNotNull(doc);

            AbsXmlKeyPairJdkDomSign sign = (AbsXmlKeyPairJdkDomSign) createJdkXmlSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);
            sign.setKeypair(keypair);
            Document signedDoc = sign.sign(doc);

            String name = getFilePart() + "_" + method.substring(method.lastIndexOf('#') + 1) + '-'
                    + dm.substring(dm.lastIndexOf('#') + 1);
            file(signedDoc, new File(tempDir, name + IXmlSign.EXT_XML));

            boolean valid = sign.valid(signedDoc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);

            Document signedDoc2 = sign.sign(signedDoc);
            // file(signedDoc, new File(tempDir, name +'-'+ 2 + IXmlSign.EXT_XML));
            boolean valid2 = sign.valid(signedDoc2);
            assertTrue("Valid failure again with DigestMethod: " + dm + ", signatureMethod: " + method, valid2);
        }
    }

}
