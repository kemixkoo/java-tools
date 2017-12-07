package xyz.kemix.xml.sign.apache;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import xyz.kemix.xml.sign.IXmlSign;
import xyz.kemix.xml.sign.KeyStoreSetting;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
public abstract class AbsXmlKeyStoreApacheSign implements IXmlSign {

    protected static final String SHORT_DS_NS = "ds";

    protected static final String SHORT_DSIG_NS = "dsig";

    static class DSNamespaceContext implements NamespaceContext {

        private Map<String, String> namespaceMap = new HashMap<String, String>();

        public DSNamespaceContext() {
            namespaceMap.put(SHORT_DS_NS, Constants.SignatureSpecNS);
            namespaceMap.put(SHORT_DSIG_NS, Constants.SignatureSpecNS);
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

    protected Element getSignatureNode(Document doc) throws Exception {
        // XPathFactory xpf = XPathFactory.newInstance();
        // XPath xpath = xpf.newXPath();
        // xpath.setNamespaceContext(new DSNamespaceContext());
        //
        // // Find the Signature Element
        // String expression = "//" + SHORT_DS_NS + ":" + Constants._TAG_SIGNATURE + "[1]"; // ds:Signature[1]
        // Element sigElement = (Element) xpath.evaluate(expression, doc, XPathConstants.NODE);
        // return sigElement;

        NodeList signList = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE);
        if (signList.getLength() == 0) {
            return null;
        }
        final Element signatureNode = (Element) signList.item(0);
        return signatureNode;
    }
}
