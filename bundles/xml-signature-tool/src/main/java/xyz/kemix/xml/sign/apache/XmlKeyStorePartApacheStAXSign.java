package xyz.kemix.xml.sign.apache;

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
import javax.xml.stream.events.XMLEvent;

import org.apache.xml.security.exceptions.XMLSecurityException;
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

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public class XmlKeyStorePartApacheStAXSign extends AbsXmlKeyStoreApacheStAXSign {

    static class TestSecurityEventListener implements SecurityEventListener {

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

    static class XmlReaderToWriter {

        private XmlReaderToWriter() {
        }

        public static void writeAll(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {
            while (xmlr.hasNext()) {
                xmlr.next();
                write(xmlr, writer);
            }
            // write(xmlr, writer); // write the last element
            writer.flush();
        }

        public static void write(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {
            switch (xmlr.getEventType()) {
            case XMLEvent.START_ELEMENT:
                final String localName = xmlr.getLocalName();
                final String namespaceURI = xmlr.getNamespaceURI();
                if (namespaceURI != null && namespaceURI.length() > 0) {
                    final String prefix = xmlr.getPrefix();
                    if (prefix != null)
                        writer.writeStartElement(prefix, localName, namespaceURI);
                    else
                        writer.writeStartElement(namespaceURI, localName);
                } else {
                    writer.writeStartElement(localName);
                }

                for (int i = 0, len = xmlr.getNamespaceCount(); i < len; i++) {
                    String prefix = xmlr.getNamespacePrefix(i);
                    if (prefix == null) {
                        writer.writeDefaultNamespace(xmlr.getNamespaceURI(i));
                    } else {
                        writer.writeNamespace(prefix, xmlr.getNamespaceURI(i));
                    }
                }

                for (int i = 0, len = xmlr.getAttributeCount(); i < len; i++) {
                    final String attUri = xmlr.getAttributeNamespace(i);

                    if (attUri != null && attUri.length() > 0) {
                        final String prefix = xmlr.getAttributePrefix(i);
                        if (prefix != null)
                            writer.writeAttribute(prefix, attUri, xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                        else
                            writer.writeAttribute(attUri, xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                    } else {
                        writer.writeAttribute(xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                    }

                }
                break;
            case XMLEvent.END_ELEMENT:
                writer.writeEndElement();
                break;
            case XMLEvent.SPACE:
            case XMLEvent.CHARACTERS:
                char[] text = new char[xmlr.getTextLength()];
                xmlr.getTextCharacters(0, text, 0, xmlr.getTextLength());
                writer.writeCharacters(text, 0, text.length);
                break;
            case XMLEvent.PROCESSING_INSTRUCTION:
                writer.writeProcessingInstruction(xmlr.getPITarget(), xmlr.getPIData());
                break;
            case XMLEvent.CDATA:
                writer.writeCData(xmlr.getText());
                break;
            case XMLEvent.COMMENT:
                writer.writeComment(xmlr.getText());
                break;
            case XMLEvent.ENTITY_REFERENCE:
                writer.writeEntityRef(xmlr.getLocalName());
                break;
            case XMLEvent.START_DOCUMENT:
                String encoding = xmlr.getCharacterEncodingScheme();
                String version = xmlr.getVersion();

                if (encoding != null && version != null)
                    writer.writeStartDocument(encoding, version);
                else if (version != null)
                    writer.writeStartDocument(xmlr.getVersion());
                break;
            case XMLEvent.END_DOCUMENT:
                writer.writeEndDocument();
                break;
            case XMLEvent.DTD:
                writer.writeDTD(xmlr.getText());
                break;
            }
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

    protected void doSign(InputStream inputStream, OutputStream outputStream) throws Exception {
        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final Key key = keyStore.getKey(getStoreSetting().getKeyAlias(), getStoreSetting().getKeyPassword());
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());

        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();
        List<XMLSecurityConstants.Action> actions = new ArrayList<XMLSecurityConstants.Action>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        properties.setSignatureAlgorithm(getSignatureMethodURI());
        properties.setSignatureCerts(new X509Certificate[] { cert });
        properties.setSignatureKey(key);
        properties.setSignatureKeyIdentifier(SecurityTokenConstants.KeyIdentifier_X509KeyIdentifier);

        for (QName nameToSign : getNamesToSign()) {
            SecurePart securePart = new SecurePart(nameToSign, SecurePart.Modifier.Content);
            properties.addSignaturePart(securePart);
        }

        OutboundXMLSec outboundXMLSec = XMLSec.getOutboundXMLSec(properties);
        XMLStreamWriter xmlStreamWriter = outboundXMLSec.processOutMessage(outputStream, StandardCharsets.UTF_8.name());

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

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
        // Set up the Configuration
        XMLSecurityProperties properties = new XMLSecurityProperties();

        // load keystore
        final KeyStore keyStore = loadKeyStore();
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(getStoreSetting().getKeyAlias());
        properties.setSignatureVerificationKey(cert.getPublicKey());

        List<XMLSecurityConstants.Action> actions = new ArrayList<XMLSecurityConstants.Action>();
        actions.add(XMLSecurityConstants.SIGNATURE);
        properties.setActions(actions);

        final InboundXMLSec inboundXMLSec = XMLSec.getInboundWSSec(properties);

        final TestSecurityEventListener eventListener = new TestSecurityEventListener();
        final XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        try {
            XMLStreamReader securityStreamReader = inboundXMLSec.processInMessage(xmlStreamReader, null, eventListener);

            while (securityStreamReader.hasNext()) {
                securityStreamReader.next();
            }
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

    private QName getSignedQName(List<QName> qnames) {
        if (qnames == null || qnames.isEmpty()) {
            return null;
        }

        return qnames.get(qnames.size() - 1);
    }
}
