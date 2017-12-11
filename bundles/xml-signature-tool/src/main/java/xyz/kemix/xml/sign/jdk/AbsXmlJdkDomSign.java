package xyz.kemix.xml.sign.jdk;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public abstract class AbsXmlJdkDomSign implements IXmlSign {

    /**
     * support for SHA1, SHA256, SHA512
     */
    private String digestMethod = DigestMethod.SHA256;

    /**
     * support for DSA_SHA1, RSA_SHA1 when use KeyPair to sign
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

    protected SignedInfo createSignedInfo(final String docUri) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        // if null or "", for all doc
        final Reference reference = createReference("");

        final CanonicalizationMethod c14nWithCommentMethod = SIGN_FACTORY.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null);
        final SignatureMethod dsa_sha1SigMethod = SIGN_FACTORY.newSignatureMethod(getSignatureMethod(), null);
        final SignedInfo signedInfo = SIGN_FACTORY.newSignedInfo(c14nWithCommentMethod, dsa_sha1SigMethod,
                Collections.singletonList(reference));
        return signedInfo;
    }

    protected Reference createReference(final String docUri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // if null or "", for all doc
        final Transform envelopedTransform = SIGN_FACTORY.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        final DigestMethod digestMethod = SIGN_FACTORY.newDigestMethod(getDigestMethod(), null);
        final Reference reference = SIGN_FACTORY.newReference(docUri == null ? "" : docUri, digestMethod,
                Collections.singletonList(envelopedTransform), null, null);
        return reference;
    }

    /**
     * The sign value and public key will be stored in node directly.
     * 
     */
    public final Document sign(Document doc) throws Exception {
        beforeSign(doc);
        return doSign(doc);
    }

    protected abstract Document doSign(Document doc) throws Exception;

    protected void beforeSign(Document doc) {
        Node signatureNode = getSignatureNode(doc);
        if (signatureNode != null) {// signed doc, not new doc, so try to remove and re-sign
            doc.getDocumentElement().removeChild(signatureNode);
        }
    }

    /**
     * 
     * Valid the doc with provided signature value
     */
    public abstract boolean valid(Document doc) throws Exception;

    /**
     * Valid the doc with signature value directly
     */
    public abstract boolean validSelf(Document doc) throws Exception;

    protected Node getSignatureNode(Document doc) {
        NodeList signList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (signList.getLength() == 0) {
            return null;
        }
        final Node signatureNode = signList.item(0);
        return signatureNode;
    }

}
