package xyz.kemix.xml.sign.apache;

import javax.xml.namespace.QName;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public class XmlKeyStorePartApacheStAXSignTest extends AbsTestXmlKeyStoreApacheStAXSign {

    @Override
    protected AbsXmlKeyStoreApacheStAXSign createSign() {
        return new XmlKeyStorePartApacheStAXSign();
    }

    @Override
    protected void setKeyStoreSettings(AbsXmlKeyStoreApacheSign sign) {
        super.setKeyStoreSettings(sign);

        ((XmlKeyStorePartApacheStAXSign) sign).getNamesToSign().add(new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo"));
    }
}
