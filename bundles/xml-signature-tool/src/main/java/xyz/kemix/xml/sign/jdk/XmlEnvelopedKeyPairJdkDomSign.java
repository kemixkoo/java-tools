package xyz.kemix.xml.sign.jdk;

import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;

import org.w3c.dom.Document;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 * The signed xml doc will be like:
 * 
 * <Data> .............. <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"> .............. </Signature> </Data>
 */
@SuppressWarnings("nls")
public class XmlEnvelopedKeyPairJdkDomSign extends AbsXmlKeyPairJdkDomSign {

    /**
     * After sign, will add one Signature node in the end of XML doc. The sign value and public key will be stored in
     * node directly.
     * 
     */
    @Override
    public Document doSign(Document doc) throws Exception {
        // 1. create SignedInfo
        final SignedInfo signedInfo = createSignedInfo("");// FIXME, for all doc

        // 2. create KeyInfo
        final KeyInfo keyInfo = createKeyInfo();

        // 3. create Signature
        final XMLSignature xmlSignature = SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

        // 4. create SignContext
        final DOMSignContext domSignCtx = new DOMSignContext(getKeypair().getPrivate(), doc.getDocumentElement());

        // 5. sign
        xmlSignature.sign(domSignCtx);

        return doc;
    }
}
