package xyz.kemix.xml.sign.apache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.stax.ext.InboundXMLSec;
import org.apache.xml.security.stax.ext.OutboundXMLSec;
import org.apache.xml.security.stax.ext.SecurePart;
import org.apache.xml.security.stax.ext.XMLSec;
import org.apache.xml.security.stax.ext.XMLSecurityConstants;
import org.apache.xml.security.stax.ext.XMLSecurityProperties;
import org.apache.xml.security.stax.impl.securityToken.X509SecurityToken;
import org.apache.xml.security.stax.securityEvent.SecurityEvent;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants.Event;
import org.apache.xml.security.stax.securityEvent.SecurityEventListener;
import org.apache.xml.security.stax.securityEvent.SignedElementSecurityEvent;
import org.apache.xml.security.stax.securityEvent.X509TokenSecurityEvent;
import org.apache.xml.security.stax.securityToken.SecurityTokenConstants;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xyz.kemix.xml.XMLFileUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public class XmlKeyStorePartApacheStAXSign extends AbsXmlKeyStoreApacheStAXSign {

    static class StAXSecurityEventListener implements SecurityEventListener {

        private List<SecurityEvent> events = new ArrayList<SecurityEvent>();

        @Override
        public void registerSecurityEvent(SecurityEvent securityEvent) throws XMLSecurityException {
            events.add(securityEvent);
        }

        @SuppressWarnings("unchecked")
        public <T> T getSecurityEvent(Event securityEvent) {
            for (SecurityEvent event : events) {
                if (event.getSecurityEventType() == securityEvent) {
                    return (T) event;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T> List<T> getSecurityEvents(Event securityEvent) {
            List<T> foundEvents = new ArrayList<T>();
            for (SecurityEvent event : events) {
                if (event.getSecurityEventType() == securityEvent) {
                    foundEvents.add((T) event);
                }
            }
            return foundEvents;
        }

        public List<SecurityEvent> getSecurityEvents() {
            return events;
        }
    }

    private final List<QName> namesToSign = new ArrayList<QName>();

    public List<QName> getNamesToSign() {
        return namesToSign;
    }

    public void sign(InputStream inputStream, OutputStream outputStream) throws Exception {
        try {
            doSign(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    protected InputStream removeSignatureNode(InputStream inputStream) throws Exception {
        try {
            Document doc = XMLFileUtil.loadDoc(inputStream);
            Element signatureNode = getSignatureNode(doc);
            if (signatureNode != null) {
                signatureNode.getParentNode().removeChild(signatureNode);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLFileUtil.saveDoc(doc, baos);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            return bais;
        } finally {
            inputStream.close();
        }
    }

    protected void doSign(InputStream inputStream, OutputStream outputStream) throws Exception {
        inputStream = removeSignatureNode(inputStream);

        XMLSecurityProperties properties = new XMLSecurityProperties();

        // Set up
        List<XMLSecurityConstants.Action> actions = new ArrayList<XMLSecurityConstants.Action>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);
        properties.setSignatureAlgorithm(getSignatureMethodURI());

        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final Key key = keyStore.getKey(getStoreSetting().getKeyAlias(), getStoreSetting().getKeyPassword());
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());
        properties.setSignatureKey(key);
        properties.setSignatureCerts(new X509Certificate[] { cert });
        properties.setSignatureKeyIdentifier(SecurityTokenConstants.KeyIdentifier_X509KeyIdentifier);

        // add sign nodes
        for (QName nameToSign : getNamesToSign()) {
            SecurePart securePart = new SecurePart(nameToSign, SecurePart.Modifier.Content);
            properties.addSignaturePart(securePart);
        }

        // create reader
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

        // create writer
        OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
        XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(outputStream, StandardCharsets.UTF_8.name());

        // sign
        XmlReaderToWriter.writeAll(xmlStreamReader, xmlStreamWriter);

        xmlStreamWriter.close();
    }

    public boolean valid(InputStream stream) throws Exception {
        try {
            return doValid(stream);
        } finally {
            stream.close();
        }
    }

    protected boolean doValid(InputStream stream) throws Exception {
        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());

        return validCert(stream, cert);
    }

    private QName getSignedQName(List<QName> qnames) {
        if (qnames == null || qnames.isEmpty()) {
            return null;
        }

        return qnames.get(qnames.size() - 1);
    }

    public boolean validSelf(InputStream stream) throws Exception {
        Document doc = XMLFileUtil.loadDoc(stream);
        Element signElem = XMLUtils.selectDsNode(doc.getDocumentElement().getFirstChild(), Constants._TAG_SIGNATURE, 0);
        if (signElem == null) {
            return false;
        }
        Element keyInfoElem = XMLUtils.selectDsNode(signElem.getFirstChild(), Constants._TAG_KEYINFO, 0);
        if (keyInfoElem == null) {
            return false;
        }
        KeyInfo keyInfo = new KeyInfo(keyInfoElem, null);

        X509Certificate x509Certificate = keyInfo.getX509Certificate();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLFileUtil.saveDoc(doc, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        return validCert(bais, x509Certificate);
    }

    protected boolean validCert(InputStream stream, X509Certificate cert) throws Exception {

        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();

        // load keystore

        properties.setSignatureVerificationKey(cert.getPublicKey());

        List<XMLSecurityConstants.Action> actions = new ArrayList<XMLSecurityConstants.Action>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        final InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);

        final StAXSecurityEventListener eventListener = new StAXSecurityEventListener();
        final XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        try {
            XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader, null, eventListener);

            while (securityStreamReader.hasNext()) {
                securityStreamReader.next();
            }
        } catch (XMLStreamException e) {
            if (e.getCause() instanceof XMLSecurityException) {
                String msgID = ((XMLSecurityException) e.getCause()).getMsgID();
                if ("errorMessages.InvalidSignatureValueException".equals(msgID)) {
                    return false; // after format the signed xml, will got this error
                } else if ("signature.Verification.InvalidDigestOrReference".equals(msgID)) {
                    return false; // after modified for the sign nodes.
                }
            }
            throw e;
        } finally {
            xmlStreamReader.close();
            stream.close();
        }

        // Check that what we were expecting to be signed was actually signed
        List<SignedElementSecurityEvent> signedElementEvents = eventListener
                .getSecurityEvents(SecurityEventConstants.SignedElement);
        if (signedElementEvents == null) {
            return false;
        }

        for (QName nameToSign : getNamesToSign()) {
            boolean found = false;
            for (SignedElementSecurityEvent signedElement : signedElementEvents) {
                if (signedElement.isSigned() && nameToSign.equals(getSignedQName(signedElement.getElementPath()))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        // Check Signing cert
        X509TokenSecurityEvent tokenEvent = (X509TokenSecurityEvent) eventListener
                .getSecurityEvent(SecurityEventConstants.X509Token);
        if (tokenEvent == null) {
            return false;
        }
        if (!(tokenEvent.getSecurityToken() instanceof X509SecurityToken)) {
            return false;
        }
        X509SecurityToken x509SecurityToken = (X509SecurityToken) tokenEvent.getSecurityToken();
        if (!cert.equals(x509SecurityToken.getX509Certificates()[0])) {
            return false;
        }
        return true;

    }

}
