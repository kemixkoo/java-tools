package xyz.kemix.xml.sign.jdk;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

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
public class JdkXmlDetachedKeyPairSign extends AbsJdkXmlKeyPairSign {

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
        DOMSignContext dsc = new DOMSignContext(getKeypair().getPrivate(), newDocument);

        // 5. sign
        xmlSignature.sign(dsc);

        return newDocument;
    }

}
