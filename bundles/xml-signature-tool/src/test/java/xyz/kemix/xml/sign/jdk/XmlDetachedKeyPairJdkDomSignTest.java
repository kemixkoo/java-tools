package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;

import javax.xml.crypto.dsig.DigestMethod;

import org.junit.Ignore;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
@Ignore
public class XmlDetachedKeyPairJdkDomSignTest extends AbsTestXmlKeyPairJdkDomSign {

    @Override
    protected AbsXmlKeyPairJdkDomSign createJdkXmlSign() {
        return new XmlDetachedKeyPairJdkDomSign();
    }

    protected void doTestKeyPairForSignatureMethod(KeyPair keypair, String method) throws Exception {
        String[] digestMethods = new String[] { DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512 };

        for (String dm : digestMethods) {
            Document doc = loadXmlDoc(FILE_SHOPPING);
            assertNotNull(doc);

            XmlDetachedKeyPairJdkDomSign sign = (XmlDetachedKeyPairJdkDomSign) createJdkXmlSign();

            sign.setDigestMethod(dm);
            sign.setSignatureMethod(method);
            sign.setKeypair(keypair);
            Document signedDoc = sign.sign(doc);

            String name = getFilePart() + "_" + method.substring(method.lastIndexOf('#') + 1) + '-'
                    + dm.substring(dm.lastIndexOf('#') + 1);
            file(signedDoc, new File(tempDir, name + IXmlSign.EXT_XML));

            sign.setSignatureDoc(signedDoc);

            boolean valid = sign.valid(doc);
            assertTrue("Valid failure with DigestMethod: " + dm + ", signatureMethod: " + method, valid);

            Document signedDoc2 = sign.sign(signedDoc);

            sign.setSignatureDoc(signedDoc2);
            // file(signedDoc, new File(tempDir, name +'-'+ 2 + IXmlSign.EXT_XML));
            boolean valid2 = sign.valid(doc);
            assertTrue("Valid failure again with DigestMethod: " + dm + ", signatureMethod: " + method, valid2);
        }
    }
}
