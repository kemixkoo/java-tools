package xyz.kemix.xml.sign.mixed;

import javax.xml.namespace.QName;

import xyz.kemix.xml.sign.AbsTestXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-07
 *
 */
public abstract class AbsTestXmlMixedSign extends AbsTestXmlSign {

    protected static final QName PAY_QNAME = new QName("http://www.kemix.xyz/2017/xmlsign#", "PaymentInfo");

    @Override
    protected String getTestName() {
        return "mixed";
    }
}
