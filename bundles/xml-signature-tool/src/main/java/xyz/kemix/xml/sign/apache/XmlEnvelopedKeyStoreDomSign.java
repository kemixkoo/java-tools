package xyz.kemix.xml.sign.apache;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.UUID;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
public class XmlEnvelopedKeyStoreDomSign extends AbsXmlKeyStoreSign {

    @Override
    public Document doSign(Document doc) throws Exception {
        // XMLUtils.setDsPrefix(null);

        // create XMLSignature
        String baseUrl = "";
        final XMLSignature sig = new XMLSignature(doc, baseUrl, getSignatureMethodURI(), getCanonicalizationMethodURI());
        //
        NodeList childNodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element elementToSign = (Element) item;
                String id = UUID.randomUUID().toString();
                elementToSign.setAttributeNS(null, Constants._ATT_ID, id);
                elementToSign.setIdAttributeNS(null, Constants._ATT_ID, true);

                Transforms transforms = new Transforms(doc);
                transforms.addTransform(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
                sig.addDocument("#" + id, transforms);

            }
        }
        //
        doc.getDocumentElement().appendChild(sig.getElement());

        // keystore
        final KeyStore keyStore = KeyStore.getInstance(getStoreSetting().getStoreType());
        keyStore.load(getStoreSetting().getStoreUrl().openStream(), getStoreSetting().getStorePassword());
        final Key key = keyStore.getKey(getStoreSetting().getKeyAlias(), getStoreSetting().getKeyPassword());
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());

        //
        sig.sign(key);

        sig.addKeyInfo(cert); // add the cert into the doc.

        return doc;
    }

    public boolean valid(Document doc) throws Exception {
        // create XMLSignature
        final Element sigElement = getSignatureNode(doc);
        String baseUrl = "";
        XMLSignature signature = new XMLSignature(sigElement, baseUrl);

        // set for signed elements
        NodeList childNodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element elementToSign = (Element) item;
                Attr idAttr = elementToSign.getAttributeNode(Constants._ATT_ID);
                if (idAttr != null) {// only check the node with Id
                    elementToSign.setIdAttributeNS(null, Constants._ATT_ID, true);
                }
            }
        }

        // load keystore
        final KeyStore keyStore = KeyStore.getInstance(getStoreSetting().getStoreType());
        keyStore.load(getStoreSetting().getStoreUrl().openStream(), getStoreSetting().getStorePassword());
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());

        return signature.checkSignatureValue(cert);
    }

    protected Element getSignatureNode(Document doc) throws Exception {
        NodeList signList = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE);
        if (signList.getLength() == 0) {
            return null;
        }
        final Element signatureNode = (Element) signList.item(0);
        return signatureNode;
    }
}
