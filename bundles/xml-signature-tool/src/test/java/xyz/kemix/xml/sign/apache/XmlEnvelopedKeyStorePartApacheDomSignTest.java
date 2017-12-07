package xyz.kemix.xml.sign.apache;

import javax.xml.namespace.QName;

import org.junit.Test;

import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public class XmlEnvelopedKeyStorePartApacheDomSignTest extends AbsTestXmlEnvelopedKeyStoreApacheDomSign {

    @Override
    protected AbsXmlKeyStoreApacheDomSign createSign() {
        return new XmlEnvelopedKeyStorePartApacheDomSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + "-part";
    }

    @Override
    protected void setKeyStoreSettings(AbsXmlKeyStoreApacheSign sign) {
        super.setKeyStoreSettings(sign);

        ((XmlEnvelopedKeyStorePartApacheDomSign) sign).getNamesToSign().add(
                new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo"));
    }

    @Test
    public void test_valid() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "_dsa-sha256-sha1" + IXmlSign.EXT_XML, true);
    }

    @Test
    public void test_valid_other() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "_dsa-sha256-sha1-other" + IXmlSign.EXT_XML, true);
    }

    @Test
    public void test_valid_modified() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "_dsa-sha256-sha1-modified" + IXmlSign.EXT_XML, false);
    }

    @Test
    public void test_valid_format() throws Exception {
        doTestValid_PaymentInfo(PATH_APACHE + getFilePart() + "_dsa-sha256-sha1-format" + IXmlSign.EXT_XML, false);
    }
}
