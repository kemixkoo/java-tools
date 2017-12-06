package xyz.kemix.xml.sign.apache;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
@SuppressWarnings("nls")
public class XmlEnvelopedKeyStorePartDomSign extends AbsXmlKeyStoreSign {

    private static final String SHORT_DS_NS = "ds";

    static class DSNamespaceContext implements NamespaceContext {

        private Map<String, String> namespaceMap = new HashMap<String, String>();

        public DSNamespaceContext() {
            namespaceMap.put(SHORT_DS_NS, Constants.SignatureSpecNS);
            namespaceMap.put("dsig", Constants.SignatureSpecNS);
        }

        public DSNamespaceContext(Map<String, String> namespaces) {
            this();
            namespaceMap.putAll(namespaces);
        }

        public String getNamespaceURI(String arg0) {
            return namespaceMap.get(arg0);
        }

        public void putPrefix(String prefix, String namespace) {
            namespaceMap.put(prefix, namespace);
        }

        public String getPrefix(String arg0) {
            for (String key : namespaceMap.keySet()) {
                String value = namespaceMap.get(key);
                if (value.equals(arg0)) {
                    return key;
                }
            }
            return null;
        }

        public Iterator<String> getPrefixes(String arg0) {
            return namespaceMap.keySet().iterator();
        }
    }

    private final List<QName> namesToSign = new ArrayList<QName>();

    public List<QName> getNamesToSign() {
        return namesToSign;
    }

    @Override
    public Document doSign(Document doc) throws Exception {
        // XMLUtils.setDsPrefix(null);

        // create XMLSignature
        String baseUrl = "";
        final XMLSignature sig = new XMLSignature(doc, baseUrl, getSignatureMethodURI(), getCanonicalizationMethodURI());
        doc.getDocumentElement().appendChild(sig.getElement());

        // set id for sign elements
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new DSNamespaceContext());
        for (QName nameToSign : getNamesToSign()) {
            String expression = "//*[local-name()='" + nameToSign.getLocalPart() + "']";
            NodeList elementsToSign = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
            for (int i = 0; i < elementsToSign.getLength(); i++) {
                Element elementToSign = (Element) elementsToSign.item(i);
                Assert.assertNotNull(elementToSign);
                String id = UUID.randomUUID().toString();
                elementToSign.setAttributeNS(null, Constants._ATT_ID, id);
                elementToSign.setIdAttributeNS(null, Constants._ATT_ID, true);

                Transforms transforms = new Transforms(doc);
                transforms.addTransform(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
                sig.addDocument("#" + id, transforms);
            }
        }

        // load keystore
        final KeyStore keyStore = loadKeyStore();
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
        final XMLSignature signature = new XMLSignature(sigElement, baseUrl);

        // set for signed elements
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new DSNamespaceContext());
        for (QName nameToSign : getNamesToSign()) {
            String expression = "//*[local-name()='" + nameToSign.getLocalPart() + "']";
            Element signedElement = (Element) xpath.evaluate(expression, doc, XPathConstants.NODE);
            signedElement.setIdAttributeNS(null, Constants._ATT_ID, true);
        }

        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());

        return signature.checkSignatureValue(cert);
    }

    protected Element getSignatureNode(Document doc) throws Exception {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new DSNamespaceContext());

        // Find the Signature Element
        String expression = "//" + SHORT_DS_NS + ":" + Constants._TAG_SIGNATURE + "[1]"; // ds:Signature[1]
        Element sigElement = (Element) xpath.evaluate(expression, doc, XPathConstants.NODE);
        return sigElement;
    }
}
