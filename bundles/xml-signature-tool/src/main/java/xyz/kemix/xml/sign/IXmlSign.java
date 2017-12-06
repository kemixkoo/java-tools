package xyz.kemix.xml.sign;

import org.w3c.dom.Document;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public interface IXmlSign {

    String EXT_XML = ".xml";

    Document sign(Document doc) throws Exception;

    boolean valid(Document doc) throws Exception;
}
