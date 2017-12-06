package xyz.kemix.xml.sign.jdk;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class JdkXmlEnvelopingKeyPairSignTest extends AbsTestJdkXmlKeyPairSign {

    @Override
    protected AbsJdkXmlKeyPairSign createJdkXmlSign() {
        return new JdkXmlEnvelopingKeyPairSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + '-' + "enveloping";
    }
}
