package xyz.kemix.xml.sign.jdk;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 * The signed values in different doc, will be like:
 *
 * <Data> .............. </Data>
 * 
 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"> .............. </Signature>
 */
public class XmlDetachedKeyPairJdkDomSign extends AbsXmlKeyPairJdkDomSign {

    private Document signatureDoc;

    public Document getSignatureDoc() {
        return signatureDoc;
    }

    public void setSignatureDoc(Document signatureDoc) {
        this.signatureDoc = signatureDoc;
    }

    protected Reference createReference(final String docUri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // if null or "", for all doc
        final DigestMethod digestMethod = SIGN_FACTORY.newDigestMethod(getDigestMethod(), null);
        final Reference reference = SIGN_FACTORY.newReference(docUri == null ? "" : docUri, digestMethod);
        return reference;
    }

    @Override
    public Document doSign(Document doc) throws Exception {
        // 1. create SignedInfo
        final SignedInfo signedInfo = createSignedInfo("");// FIXME, for all doc

        // 2. create KeyInfo
        final KeyInfo keyInfo = createKeyInfo();

        // 3. create Signature
        final XMLSignature xmlSignature = SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo);

        // 4. create SignContext
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document newDocument = dbf.newDocumentBuilder().newDocument();
        DOMSignContext dsc = new DOMSignContext(getKeypair().getPrivate(), doc.getDocumentElement());

        // 5. sign
        xmlSignature.sign(dsc);

        // 6. find and set to new doc
        Node signatureNode = getSignatureNode(doc);
        if (signatureNode == null) {
            return null;
        }
        newDocument.insertBefore(newDocument.adoptNode(signatureNode), null);

        return newDocument;
    }

    @Override
    public boolean valid(Document doc) throws Exception {
        if (getSignatureDoc() == null) {
            throw new IllegalArgumentException("Must provide the signature xml document");
        }
        // find signature node
        final Node signatureNode = getSignatureNode(getSignatureDoc());
        if (signatureNode == null) {
            return false;
        }
        // add back
        // doc.getFirstChild().appendChild(doc.adoptNode(signatureNode));

        XMLSignature signature = SIGN_FACTORY.unmarshalXMLSignature(new DOMStructure(signatureNode));

        PublicKey pubKey = ((KeyValue) signature.getKeyInfo().getContent().get(0)).getPublicKey();
        // if signatureNode is not in doc, must use the original doc
        DOMValidateContext valContext = new DOMValidateContext(pubKey, doc.getDocumentElement());

        return signature.validate(valContext);
    }

}
