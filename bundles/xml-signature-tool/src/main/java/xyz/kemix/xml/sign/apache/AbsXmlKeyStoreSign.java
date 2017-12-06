package xyz.kemix.xml.sign.apache;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.KeyStoreSetting;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
public abstract class AbsXmlKeyStoreSign implements IXmlSign {

    /**
     * support
     */
    private String signatureMethodUri = XMLSignature.ALGO_ID_SIGNATURE_DSA_SHA256;

    private String digestUri = Constants.ALGO_ID_DIGEST_SHA1;

    private String canonicalizationMethodUri = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    private final KeyStoreSetting storeSetting = new KeyStoreSetting();
    static {
        Init.init();
    }

    public String getSignatureMethodURI() {
        return signatureMethodUri;
    }

    public void setSignatureMethodURI(String signatureMethod) {
        this.signatureMethodUri = signatureMethod;
    }

    public String getDigestURI() {
        return digestUri;
    }

    public void setDigestURI(String digestUri) {
        this.digestUri = digestUri;
    }

    public String getCanonicalizationMethodURI() {
        return canonicalizationMethodUri;
    }

    public void setCanonicalizationMethodURI(String canonicalizationMethodUri) {
        this.canonicalizationMethodUri = canonicalizationMethodUri;
    }

    public KeyStoreSetting getStoreSetting() {
        return storeSetting;
    }

    protected KeyStore loadKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(getStoreSetting().getStoreType());
        keyStore.load(getStoreSetting().getStoreUrl().openStream(), getStoreSetting().getStorePassword());
        return keyStore;
    }

    public final Document sign(Document doc) throws Exception {
        beforeSign(doc);
        return doSign(doc);
    }

    protected abstract Document doSign(Document doc) throws Exception;

    protected void beforeSign(Document doc) throws Exception {
        Element signatureNode = getSignatureNode(doc);
        if (signatureNode != null) {// signed doc, not new doc, so try to remove and re-sign
            doc.getDocumentElement().removeChild(signatureNode);
        }
    }

    protected abstract Element getSignatureNode(Document doc) throws Exception;

    public abstract boolean valid(Document doc) throws Exception;
}
