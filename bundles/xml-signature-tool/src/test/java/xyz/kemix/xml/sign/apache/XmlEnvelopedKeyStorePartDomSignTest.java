package xyz.kemix.xml.sign.apache;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public class XmlEnvelopedKeyStorePartDomSignTest extends AbsTestXmlEnvelopedKeyStoreDomSign {

    @Override
    protected AbsXmlKeyStoreSign createSign() {
        return new XmlEnvelopedKeyStorePartDomSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + "-part";
    }

    @Override
    protected void setKeyStoreSettings(AbsXmlKeyStoreSign sign) {
        super.setKeyStoreSettings(sign);

        ((XmlEnvelopedKeyStorePartDomSign) sign).getNamesToSign().add(
                new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo"));
    }

    @Test
    public void test_valid() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped-part_dsa-sha256_sha1.xml", true);
    }

    @Test
    public void test_valid_other() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped-part_dsa-sha256_sha1-other.xml", true);
    }

    @Test
    public void test_valid_modified() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped-part_dsa-sha256_sha1-modified.xml", false);
    }

    @Test
    public void test_valid_format() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped-part_dsa-sha256_sha1-format.xml", false);
    }
}
