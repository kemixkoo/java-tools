package xyz.kemix.xml.sign.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;

import xyz.kemix.xml.sign.IXmlSign;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class XmlEnvelopedKeyPairJdkDomSignTest extends AbsTestXmlKeyPairJdkDomSign {

    @Override
    protected AbsXmlKeyPairJdkDomSign createJdkXmlSign() {
        return new XmlEnvelopedKeyPairJdkDomSign();
    }

    @Override
    protected String getTestName() {
        return super.getTestName() + '-' + "enveloped";
    }

    @Test
    public void test_valid_keyPair_format() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512_format" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertFalse("After format the Data, won't be valid yet", valid);
    }

    @Test
    public void test_valid_keyPair_modified() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512_modified" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertFalse("After change the Data, won't be valid yet", valid);
    }

    @Test
    public void test_valid_keyPair_space_text() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512_space-text" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertFalse("After add one space after <Items> node, won't be valid yet", valid);
    }

    @Test
    public void test_valid_keyPair_space_node() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512_space-node" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertTrue("After add one space like <Items >, won't be valid yet", valid);
    }

    @Test
    public void test_valid_keyPair_space_attr() throws Exception {
        Document doc = loadXmlDoc(PATH_JDK + getFilePart() + "_rsa-sha1-sha512_space-attr" + IXmlSign.EXT_XML);
        assertNotNull(doc);

        AbsXmlJdkDomSign sign = createJdkXmlSign();
        boolean valid = sign.valid(doc);
        assertTrue("After add one space in Attribut for cake, won't be valid yet", valid);
    }
}
