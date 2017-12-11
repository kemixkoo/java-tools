package xyz.kemix.xml.sign.apache;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public abstract class AbsXmlKeyStoreApacheDomSign extends AbsXmlKeyStoreApacheSign {

    public final Document sign(Document doc) throws Exception {
        beforeSign(doc);
        return doSign(doc);
    }

    protected abstract Document doSign(Document doc) throws Exception;

    protected void beforeSign(Document doc) throws Exception {
        Element signatureNode = getSignatureNode(doc);
        if (signatureNode != null) {// signed doc, not new doc, so try to remove and re-sign
            doc.getDocumentElement().removeChild(signatureNode);
        }
    }

    public abstract boolean valid(Document doc) throws Exception;

    public abstract boolean validSelf(Document doc) throws Exception;
}
