package xyz.kemix.xml.sign.jdk;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

import org.w3c.dom.Document;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
public class JdkXmlEnvelopedKeyStoreSign extends AbsJdkXmlKeyStoreSign {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Document doSign(Document doc) throws Exception {
        // 1. create SignedInfo
        final SignedInfo signedInfo = createSignedInfo("");// FIXME, for all doc

        // 2.create KeyInfo
        final KeyStore ks = KeyStore.getInstance(getKeystoreSetting().getStoreType());
        ks.load(getKeystoreSetting().getStoreUrl().openStream(), getKeystoreSetting().getStorePassword());
        final KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(getKeystoreSetting().getKeyAlias(),
                new KeyStore.PasswordProtection(getKeystoreSetting().getKeyPassword()));
        final X509Certificate cert = (X509Certificate) keyEntry.getCertificate();
        final KeyInfoFactory keyInfoFac = SIGN_FACTORY.getKeyInfoFactory();
        final X509IssuerSerial newX509IssuerSerial = keyInfoFac.newX509IssuerSerial(cert.getIssuerX500Principal().getName(),
                cert.getSerialNumber());
        List x509Content = new ArrayList();
        x509Content.add(newX509IssuerSerial);
        x509Content.add(cert);
        final KeyInfo keyInfo = keyInfoFac.newKeyInfo(Collections.singletonList(keyInfoFac.newX509Data(x509Content)));

        // 3. create Signature
        final XMLSignature xmlSignature = SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

        // 4. create SignContext
        final DOMSignContext domSignCtx = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());

        // 5. sign
        xmlSignature.sign(domSignCtx);

        return doc;
    }
}
