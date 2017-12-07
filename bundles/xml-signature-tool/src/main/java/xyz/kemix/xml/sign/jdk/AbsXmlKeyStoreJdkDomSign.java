package xyz.kemix.xml.sign.jdk;

import java.security.PublicKey;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

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
        if (signatureNode != null) {
            return false;
        }
        final PublicKey publicKey = KeyStoreUtil.getPublicKey(getKeystoreSetting().getStoreUrl(), getKeystoreSetting()
                .getStoreType(), getKeystoreSetting().getStorePassword(), getKeystoreSetting().getKeyAlias());

        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));
        DOMValidateContext valContext = new DOMValidateContext(publicKey, signatureNode);

        return signature.validate(valContext);

    }
}
