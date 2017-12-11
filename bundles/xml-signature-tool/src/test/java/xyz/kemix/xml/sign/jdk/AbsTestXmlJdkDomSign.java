package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.AbsTestXmlSign;
import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-07
 *
 */
public abstract class AbsTestXmlJdkDomSign extends AbsTestXmlSign {

    static final String PATH_JDK = "jdk/";

    protected abstract AbsXmlJdkDomSign createJdkXmlSign();

    @Test
    public void test_validSelf() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.validSelf(doc);
        assertTrue("No need the key pair, just valid by the signature with keys", valid);
    }
}
