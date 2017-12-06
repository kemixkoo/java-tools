package xyz.kemix.xml.sign;

import java.net.URL;

import xyz.kemix.xml.sign.jdk.key.KeyStoreUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-04
 *
 */
public class KeyStoreSetting {

    /**
     * support for JKS, PKCS12
     * 
     */
    private String storeType = KeyStoreUtil.JKS;

    /**
     * if the key type of keystore should be same as signatureMethod.
     */
    private URL storeUrl;

    private char[] storePassword;

    private String keyAlias;

    private char[] keyPassword;

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public URL getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(URL storeUrl) {
        this.storeUrl = storeUrl;
    }

    public char[] getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(char[] storePassword) {
        this.storePassword = storePassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public char[] getKeyPassword() {
        if (keyPassword == null) { // if not set, use same password of store
            return storePassword;
        }
        return keyPassword;
    }

    public void setKeyPassword(char[] keyPassword) {
        this.keyPassword = keyPassword;
    }
}
