package xyz.kemix.xml.sign.jdk;

import java.security.KeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsXmlKeyPairJdkDomSign extends AbsXmlJdkDomSign {

    private KeyPair keypair;

    public KeyPair getKeypair() {
        return keypair;
    }

    public void setKeypair(KeyPair keypair) {
        this.keypair = keypair;
    }

    protected KeyInfo createKeyInfo() throws KeyException {
        final KeyInfoFactory keyInfoFac = SIGN_FACTORY.getKeyInfoFactory();
        final KeyValue keyValue = keyInfoFac.newKeyValue(getKeypair().getPublic());
        final KeyInfo keyInfo = keyInfoFac.newKeyInfo(Collections.singletonList(keyValue));
        return keyInfo;
    }

    /**
     * Valid the doc with signature value directly
     */
    public boolean valid(Document doc) throws Exception {
        // find signature node
        final Node signatureNode = getSignatureNode(doc);
        if (signatureNode == null) {
            return false;
        }
        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));

        PublicKey pubKey = ((KeyValue) signature.getKeyInfo().getContent().get(0)).getPublicKey();
        // if signatureNode is in doc, ok for this also
        DOMValidateContext valContext = new DOMValidateContext(pubKey, signatureNode);
        // DOMValidateContext valContext = new DOMValidateContext(pubKey, doc.getDocumentElement());

        return signature.validate(valContext);
    }
}
