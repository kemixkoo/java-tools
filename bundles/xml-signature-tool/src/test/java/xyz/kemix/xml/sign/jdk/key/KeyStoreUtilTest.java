package xyz.kemix.xml.sign.jdk.key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.kemix.xml.sign.AbsTestParent;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
public class KeyStoreUtilTest extends AbsTestParent {

    public static final char[] storePassword = "123456".toCharArray();

    public static final String keyAlias = "kemix";

    public static final char[] keyPassword = "654321".toCharArray();

    public static final String PATH_KEYSTORE = "/keystore/";

    public static final String PATH_DSA_JKS = PATH_KEYSTORE + "kemix-dsa.jks";

    public static final String PATH_RSA_JKS = PATH_KEYSTORE + "kemix-rsa.jks";

    static final String storeType = KeyStoreUtil.JKS;

    static URL dsaStoreUrl, rsaStoreUrl;

    @BeforeClass
    public static void init() {
        dsaStoreUrl = KeyStoreUtilTest.class.getResource(PATH_DSA_JKS);
        rsaStoreUrl = KeyStoreUtilTest.class.getResource(PATH_RSA_JKS);
        assertNotNull(dsaStoreUrl);
        assertNotNull(rsaStoreUrl);
    }

    @Test
    public void test_getPrivateKey_DSA() throws Exception {
        PrivateKey privateKey = KeyStoreUtil.getPrivateKey(dsaStoreUrl, storeType, storePassword, keyAlias, keyPassword);
        assertNotNull(privateKey);
    }

    @Test
    public void test_getPrivateKey_RSA() throws Exception {
        PrivateKey privateKey = KeyStoreUtil.getPrivateKey(rsaStoreUrl, storeType, storePassword, keyAlias, keyPassword);
        assertNotNull(privateKey);
    }

    @Test
    public void test_getPublicKey_DSA() throws Exception {
        PublicKey publicKey = KeyStoreUtil.getPublicKey(dsaStoreUrl, storeType, storePassword, keyAlias);
        assertNotNull(publicKey);
    }

    @Test
    public void test_getPublicKey_RSA() throws Exception {
        PublicKey publicKey = KeyStoreUtil.getPublicKey(rsaStoreUrl, storeType, storePassword, keyAlias);
        assertNotNull(publicKey);
    }

    // @Test
    public void test_convert_JKS_PFX_DSA_IT() throws Exception {
        File pfxFile = new File(tempDir, "dsa.pfx");

        KeyStoreUtil.convertJKS2PFX(dsaStoreUrl, pfxFile, storePassword, keyAlias, keyPassword);
        assertTrue(pfxFile.exists());

        // TODO??? the store type is JKS still, "keytool -list -rfc -keystore dsa.pfx -storepass 123456"
        PrivateKey privateKey = KeyStoreUtil.getPrivateKey(pfxFile.toURI().toURL(), KeyStoreUtil.PKCS12, storePassword, keyAlias,
                keyPassword);
        assertNotNull(privateKey);

        PublicKey publicKey = KeyStoreUtil.getPublicKey(pfxFile.toURI().toURL(), KeyStoreUtil.PKCS12, storePassword, keyAlias);
        assertNotNull(publicKey);

        //
        File jksFile = new File(tempDir, "dsa.jks");
        KeyStoreUtil.convertPFX2JKS(pfxFile.toURI().toURL(), jksFile, storePassword, keyAlias, keyPassword);
        assertTrue(jksFile.exists());

        PrivateKey jksPrivateKey = KeyStoreUtil.getPrivateKey(pfxFile.toURI().toURL(), KeyStoreUtil.JKS, storePassword, keyAlias,
                keyPassword);
        assertNotNull(jksPrivateKey);

        PublicKey jksPublicKey = KeyStoreUtil.getPublicKey(pfxFile.toURI().toURL(), KeyStoreUtil.JKS, storePassword, keyAlias);
        assertNotNull(jksPublicKey);

        //
        String original = IOUtils.toString(dsaStoreUrl.openStream());
        String newone = IOUtils.toString(jksFile.toURI().toURL().openStream());

        assertEquals(original, newone);
    }

    // @Test
    public void test_convert_JKS_PFX_RSA_IT() throws Exception {
        File pfxFile = new File(tempDir, "rsa.pfx");

        KeyStoreUtil.convertJKS2PFX(rsaStoreUrl, pfxFile, storePassword, keyAlias, keyPassword);
        assertTrue(pfxFile.exists());

        // TODO??? the store type is JKS still, "keytool -list -rfc -keystore dsa.pfx -storepass 123456"
        PrivateKey privateKey = KeyStoreUtil.getPrivateKey(pfxFile.toURI().toURL(), KeyStoreUtil.PKCS12, storePassword, keyAlias,
                keyPassword);
        assertNotNull(privateKey);

        PublicKey publicKey = KeyStoreUtil.getPublicKey(pfxFile.toURI().toURL(), KeyStoreUtil.PKCS12, storePassword, keyAlias);
        assertNotNull(publicKey);

        //
        File jksFile = new File(tempDir, "rsa.jks");
        KeyStoreUtil.convertPFX2JKS(pfxFile.toURI().toURL(), jksFile, storePassword, keyAlias, keyPassword);
        assertTrue(jksFile.exists());

        PrivateKey jksPrivateKey = KeyStoreUtil.getPrivateKey(pfxFile.toURI().toURL(), KeyStoreUtil.JKS, storePassword, keyAlias,
                keyPassword);
        assertNotNull(jksPrivateKey);

        PublicKey jksPublicKey = KeyStoreUtil.getPublicKey(pfxFile.toURI().toURL(), KeyStoreUtil.JKS, storePassword, keyAlias);
        assertNotNull(jksPublicKey);

        //
        String original = IOUtils.toString(rsaStoreUrl.openStream());
        String newone = IOUtils.toString(jksFile.toURI().toURL().openStream());

        assertEquals(original, newone);

    }
}
