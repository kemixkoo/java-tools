package xyz.kemix.xml.sign.apache;

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
}
