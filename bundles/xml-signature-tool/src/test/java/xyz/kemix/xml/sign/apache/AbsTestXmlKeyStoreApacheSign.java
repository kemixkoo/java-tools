package xyz.kemix.xml.sign.apache;

import java.net.URL;

import xyz.kemix.xml.sign.AbsTestXmlSign;
import xyz.kemix.xml.sign.KeyStoreSetting;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;
import xyz.kemix.xml.sign.jdk.key.KeyStoreUtilTest;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public abstract class AbsTestXmlKeyStoreApacheSign extends AbsTestXmlSign {

    static final String PATH_APACHE = "apache/";

    @Override
    protected String getTestName() {
        return "keystore";
    }

    protected void setKeyStoreSettings(AbsXmlKeyStoreApacheSign sign) {
        KeyStoreSetting storeSetting = sign.getStoreSetting();
        storeSetting.setStoreType(KeyStoreUtil.JKS);
        URL storeUrl = this.getClass().getResource(KeyStoreUtilTest.PATH_KEYSTORE + "kemix-dsa.jks");
        storeSetting.setStoreUrl(storeUrl);
        storeSetting.setStorePassword(KeyStoreUtilTest.storePassword);
        storeSetting.setKeyAlias(KeyStoreUtilTest.keyAlias);
        storeSetting.setKeyPassword(KeyStoreUtilTest.keyPassword);
    }

}
