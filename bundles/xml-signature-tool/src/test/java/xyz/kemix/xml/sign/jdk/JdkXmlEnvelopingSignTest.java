package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;

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

    @Test
    public void test_valid() throws Exception {
        Document doc = loadDoc("/jdk/demo-enveloping.xml");
        assertNotNull(doc);

        AbsJdkXmlSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertTrue("Valid failure", valid);
    }

    @Test
    public void test_valid_format() throws Exception {
        Document doc = loadDoc("/jdk/demo-enveloping-format.xml");
        assertNotNull(doc);

        AbsJdkXmlSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertFalse("After format the Data, won't be valid yet", valid);
    }

}
