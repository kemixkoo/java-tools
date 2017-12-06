package xyz.kemix.xml.sign.apache;

import org.junit.Test;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-05
 *
 */
public class XmlEnvelopedKeyStoreDomSignTest extends AbsTestXmlEnvelopedKeyStoreDomSign {

    @Override
    protected AbsXmlKeyStoreSign createSign() {
        return new XmlEnvelopedKeyStoreDomSign();
    }

    @Test
    public void test_valid() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped_dsa-sha256_sha1.xml", true);
    }

    @Test
    public void test_valid_modified() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped_dsa-sha256_sha1-modified.xml", false);
    }

    @Test
    public void test_valid_format() throws Exception {
        doTestValid_PaymentInfo("apache/shopping_keystore-enveloped_dsa-sha256_sha1-format.xml", false);
    }

}
