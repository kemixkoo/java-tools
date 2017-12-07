package xyz.kemix.xml.sign.jdk;

import java.util.Collections;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 * The signed xml doc will be like:
 *
 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"> .............. <Data> .............. </Data> </Signature>
 */
@SuppressWarnings("nls")
public class XmlEnvelopingKeyPairJdkDomSign extends AbsXmlKeyPairJdkDomSign {

    @Override
    protected void beforeSign(Document doc) {
        // super.beforeSign(doc); //shouldn't remove the sign node
    }

    @Override
    public Document doSign(Document doc) throws Exception {
        // 1. create SignedInfo
        final SignedInfo signedInfo = createSignedInfo("");// FIXME, for all doc

        // 2. create KeyInfo
        final KeyInfo keyInfo = createKeyInfo();

        // 3. create Signature
        final DOMStructure domStructure = new DOMStructure(doc.getDocumentElement());
        final XMLObject newXMLObject = SIGN_FACTORY.newXMLObject(Collections.singletonList(domStructure), null, null, null);
        final XMLSignature xmlSignature = SIGN_FACTORY.newXMLSignature(signedInfo, keyInfo,
                Collections.singletonList(newXMLObject), null, null);

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
