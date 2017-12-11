package xyz.kemix.xml.sign;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import xyz.kemix.xml.XMLFileUtil;
import xyz.kemix.xml.sign.apache.AbsXmlKeyStoreApacheSign;
import xyz.kemix.xml.sign.jdk.AbsXmlKeyStoreJdkDomSign;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public abstract class AbsTestXmlSign extends AbsTestParent {

    protected static final String PATH_XML = "/xml/";

    protected static final String FILE_SHOPPING_NAME = "shopping";

    protected static final String FILE_SHOPPING = FILE_SHOPPING_NAME + IXmlSign.EXT_XML;

    protected Document loadXmlDoc(String path) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = this.getClass().getResourceAsStream(PATH_XML + path);

        return XMLFileUtil.loadDoc(stream);
    }

    protected void console(Document doc) throws Exception {
        XMLFileUtil.consoleDoc(doc);
    }

    protected void file(Document doc, File file) throws Exception {
        XMLFileUtil.saveDoc(doc, file);
    }

    protected String getFilePart() {
        return FILE_SHOPPING_NAME + '-' + getTestName();
    }

    protected void setStore(AbsXmlKeyStoreJdkDomSign sign, URL storeUrl) throws IOException {
        KeyStoreSetting keystoreSetting = sign.getKeystoreSetting();
        keystoreSetting.setStoreType(KeyStoreUtil.JKS);
        keystoreSetting.setStoreUrl(storeUrl);
        keystoreSetting.setStorePassword(KeyStoreUtilTest.storePassword);
        keystoreSetting.setKeyAlias(KeyStoreUtilTest.keyAlias);
        keystoreSetting.setKeyPassword(KeyStoreUtilTest.keyPassword);
    }

    protected void setStore(AbsXmlKeyStoreApacheSign sign, URL storeUrl) {
        KeyStoreSetting storeSetting = sign.getStoreSetting();
        storeSetting.setStoreType(KeyStoreUtil.JKS);
        storeSetting.setStoreUrl(storeUrl);
        storeSetting.setStorePassword(KeyStoreUtilTest.storePassword);
        storeSetting.setKeyAlias(KeyStoreUtilTest.keyAlias);
        storeSetting.setKeyPassword(KeyStoreUtilTest.keyPassword);
    }

    protected abstract String getTestName();

}
