package xyz.kemix.xml.sign.jdk;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class JdkXmlEnvelopingSignTest extends AbsTestJdkXmlSign {

    @Override
    AbsJdkXmlSign createJdkXmlSign() {
        return new JdkXmlEnvelopingSign();
    }

    @Override
    String getTestName() {
        return "enveloping";
    }

}
