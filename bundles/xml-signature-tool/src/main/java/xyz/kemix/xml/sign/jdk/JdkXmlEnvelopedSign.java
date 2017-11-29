package xyz.kemix.xml.sign.jdk;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
@SuppressWarnings("nls")
public class JdkXmlEnvelopedSign {

    /**
     * support for SHA1, SHA256, SHA512
     */
    private String digestMethod = DigestMethod.SHA256;

    /**
     * support for DSA_SHA1, RSA_SHA1
     * 
     * If DSA_SHA1, the key pair must be DSA.
     * 
     * If RSA_SHA1, the key pair must be RSA.
     */
    private String signatureMethod = SignatureMethod.DSA_SHA1;

    protected static final XMLSignatureFactory SIGN_FACTORY = XMLSignatureFactory.getInstance("DOM");

    public String getDigestMethod() {
        return digestMethod;
    }

    public void setDigestMethod(String digestMethod) {
        this.digestMethod = digestMethod;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }

    /**
     * After sign, will add one Signature node in the end of XML doc. The sign value and public key will be stored in
     * node directly.
     * 
     */
    public void sign(Document doc, KeyPair keypair) throws Exception {

        Transform envelopedTransform = SIGN_FACTORY.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        DigestMethod sha1DigMethod = SIGN_FACTORY.newDigestMethod(digestMethod, null);

        final String docUri = "";// FIXME, for all doc
        Reference reference = SIGN_FACTORY.newReference(docUri, sha1DigMethod, Collections.singletonList(envelopedTransform),
                null, null);

        // create SignedInfo
        CanonicalizationMethod c14nWithCommentMethod = SIGN_FACTORY.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                (C14NMethodParameterSpec) null);
        SignatureMethod dsa_sha1SigMethod = SIGN_FACTORY.newSignatureMethod(signatureMethod, null);
        SignedInfo signedInfo = SIGN_FACTORY.newSignedInfo(c14nWithCommentMethod, dsa_sha1SigMethod,
                Collections.singletonList(reference));

        // create KeyValue, KeyInfo
        KeyInfoFactory keyInfoFac = SIGN_FACTORY.getKeyInfoFactory();
        KeyValue keyValue = keyInfoFac.newKeyValue(keypair.getPublic());
        KeyInfo keyInfo = keyInfoFac.newKeyInfo(Collections.singletonList(keyValue));

        // create Signature
        final XMLSignature xmlSignature = SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

        // sign
        final DOMSignContext domSignCtx = new DOMSignContext(keypair.getPrivate(), doc.getDocumentElement());
        xmlSignature.sign(domSignCtx);
    }

    /**
     * Valid the doc with signature value directly
     */
    public boolean valid(Document doc) throws Exception {
        // find signature node
        final Node signatureNode = getSignatureNode(doc);
        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));

        PublicKey pubKey = ((KeyValue) signature.getKeyInfo().getContent().get(0)).getPublicKey();
        // if signatureNode is in doc, ok for this also
        // DOMValidateContext valCtx = new DOMValidateContext(publicKey, signatureNode);
        DOMValidateContext valContext = new DOMValidateContext(pubKey, doc.getDocumentElement());

        return signature.validate(valContext);
    }

    protected Node getSignatureNode(Document doc) throws XMLSignatureException {
        NodeList signList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (signList.getLength() == 0) {
            throw new XMLSignatureException("No XML Digital Signature Found, document is discarded");
        }
        final Node signatureNode = signList.item(0);
        return signatureNode;
    }
}
