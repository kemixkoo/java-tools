package xyz.kemix.xml.sign.apache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.junit.Test;

import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public class XmlKeyStorePartApacheStAXSignTest extends AbsTestXmlKeyStoreApacheStAXSign {

    @Override
    protected String getTestName() {
        return super.getTestName() + "-part";
    }

    @Override
    protected AbsXmlKeyStoreApacheStAXSign createSign() {
        return new XmlKeyStorePartApacheStAXSign();
    }

    @Override
    protected void setKeyStoreSettings(AbsXmlKeyStoreApacheSign sign) {
        super.setKeyStoreSettings(sign);

        ((XmlKeyStorePartApacheStAXSign) sign).getNamesToSign().add(
                new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo"));
    }

    @Test
    public void test_valid() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + IXmlSign.EXT_XML, true);
    }

    @Test
    public void test_valid_other() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "-other" + IXmlSign.EXT_XML, true);
    }

    @Test
    public void test_valid_modified() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "-modified" + IXmlSign.EXT_XML, false);
    }

    @Test
    public void test_valid_format() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "-format" + IXmlSign.EXT_XML, false);
    }

    @Test
    public void test_validSelf() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + PATH_APACHE + getFilePart() + IXmlSign.EXT_XML);
        assertNotNull(stream);

        XmlKeyStorePartApacheStAXSign sign = new XmlKeyStorePartApacheStAXSign();

        sign.getNamesToSign().add(new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo"));

        boolean valid = sign.validSelf(stream);
        assertTrue("No need the key pair, just valid by the signature with keys", valid);
    }
}
