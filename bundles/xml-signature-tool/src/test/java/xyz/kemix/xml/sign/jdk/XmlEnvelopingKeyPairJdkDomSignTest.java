package xyz.kemix.xml.sign.jdk;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class XmlEnvelopingKeyPairJdkDomSignTest extends AbsTestXmlKeyPairJdkDomSign {

    @Override
    protected AbsXmlKeyPairJdkDomSign createJdkXmlSign() {
        return new XmlEnvelopingKeyPairJdkDomSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + '-' + "enveloping";
    }
}
