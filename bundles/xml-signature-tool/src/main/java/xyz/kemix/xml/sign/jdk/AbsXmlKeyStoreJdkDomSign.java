package xyz.kemix.xml.sign.jdk;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.X509Data;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xyz.kemix.xml.sign.KeyStoreSetting;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsXmlKeyStoreJdkDomSign extends AbsXmlJdkDomSign {

    private final KeyStoreSetting keystoreSetting = new KeyStoreSetting();

    public KeyStoreSetting getKeystoreSetting() {
        return keystoreSetting;
    }

    @Override
    public boolean valid(Document doc) throws Exception {
        // find signature node
        final Node signatureNode = getSignatureNode(doc);
        if (signatureNode == null) {
            throw new IllegalArgumentException("Can't valid without signature node.");
        }
        final PublicKey publicKey = KeyStoreUtil.getPublicKey(getKeystoreSetting().getStoreUrl(), getKeystoreSetting()
                .getStoreType(), getKeystoreSetting().getStorePassword(), getKeystoreSetting().getKeyAlias());
        if (publicKey == null) {
            throw new IllegalArgumentException("Can't load the key store.");
        }
        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));
        DOMValidateContext valContext = new DOMValidateContext(publicKey, doc.getDocumentElement());

        return signature.validate(valContext);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean validSelf(Document doc) throws Exception {
        // find signature node
        final Node signatureNode = getSignatureNode(doc);
        if (signatureNode == null) {
            throw new IllegalArgumentException("Can't valid without signature node.");
        }
        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));

        List keystoreList = ((X509Data) signature.getKeyInfo().getContent().get(0)).getContent();
        X509Certificate x509Cert = null;
        for (Object o : keystoreList) {
            if (o instanceof X509Certificate) {
                x509Cert = (X509Certificate) o;
                break;
            }
        }
        if (x509Cert == null) {
            return false;
        }

        DOMValidateContext valContext = new DOMValidateContext(x509Cert.getPublicKey(), doc.getDocumentElement());

        return signature.validate(valContext);
    }
}
