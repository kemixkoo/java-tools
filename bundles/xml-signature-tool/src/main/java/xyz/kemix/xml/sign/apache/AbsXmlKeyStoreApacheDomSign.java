package xyz.kemix.xml.sign.apache;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public abstract class AbsXmlKeyStoreApacheDomSign extends AbsXmlKeyStoreApacheSign {

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

    public boolean valid(Document doc) throws Exception {
        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());
        return validCert(doc, cert);
    }

    public boolean validSelf(Document doc) throws Exception {
        final Element signElem = getSignatureNode(doc);
        if (signElem == null) {
            return false;
        }
        Element keyInfoElem = XMLUtils.selectDsNode(signElem.getFirstChild(), Constants._TAG_KEYINFO, 0);
        if (keyInfoElem == null) {
            return false;
        }
        KeyInfo keyInfo = new KeyInfo(keyInfoElem, null);

        X509Certificate x509Certificate = keyInfo.getX509Certificate();

        return validCert(doc, x509Certificate);
    }

    protected abstract boolean validCert(Document doc, X509Certificate cert) throws Exception;
}
