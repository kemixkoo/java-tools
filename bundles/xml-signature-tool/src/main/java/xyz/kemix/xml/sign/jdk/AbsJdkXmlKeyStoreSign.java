package xyz.kemix.xml.sign.jdk;

import java.net.URL;
import java.security.PublicKey;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public abstract class AbsJdkXmlKeyStoreSign extends AbsJdkXmlSign {

    /**
     * support for JKS, PKCS12
     * 
     */
    private String storeType = KeyStoreUtil.JKS;

    /**
     * if the key type of keystore should be same as signatureMethod.
     */
    private URL storeUrl;

    private char[] storePassword;

    private String keyAlias;

    private char[] keyPassword;

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public URL getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(URL storeUrl) {
        this.storeUrl = storeUrl;
    }

    public char[] getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(char[] storePassword) {
        this.storePassword = storePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public char[] getKeyPassword() {
        if (keyPassword == null) { // if not set, use same password of store
            return storePassword;
        }
        return keyPassword;
    }

    public void setKeyPassword(char[] keyPassword) {
        this.keyPassword = keyPassword;
    }

    @Override
    public boolean valid(Document doc) throws Exception {
        final PublicKey publicKey = KeyStoreUtil.getPublicKey(getStoreUrl(), getStoreType(), getStorePassword(), getKeyAlias());

        // find signature node
        final Node signatureNode = getSignatureNode(doc);
        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));
        DOMValidateContext valContext = new DOMValidateContext(publicKey, signatureNode);

        return signature.validate(valContext);

    }
}
