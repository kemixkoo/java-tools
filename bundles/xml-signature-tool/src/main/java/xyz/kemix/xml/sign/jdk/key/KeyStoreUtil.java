package xyz.kemix.xml.sign.jdk.key;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.KeyGenerator;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-30
 *
 */
@SuppressWarnings({ "rawtypes", "nls" })
public class KeyStoreUtil {

    public static final String PKCS12 = "PKCS12";

    public static final String JKS = "JKS";

    public static PrivateKey getPrivateKey(URL storeUrl, String storeType, char[] storePassword, String keyAlias,
            char[] keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(storeType);
        InputStream openStream = storeUrl.openStream();
        try {
            keyStore.load(openStream, storePassword);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword);
            return privateKey;
        } finally {
            openStream.close();
        }
    }

    public static PublicKey getPublicKey(URL storeUrl, String storeType, char[] storePassword, String keyAlias) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(storeType);
        InputStream openStream = storeUrl.openStream();
        try {
            keyStore.load(openStream, storePassword);
            final X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
            return cert.getPublicKey();
        } finally {
            openStream.close();
        }
    }

    /**
     * 
     * convert the PKCS12 to JKS
     */
    public static void convertPFX2JKS(URL pfxUrl, File jksFile, char[] storePassword, String keyAlias, char[] keyPassword)
            throws Exception {
        convert(PKCS12, pfxUrl, JKS, jksFile, storePassword, keyAlias, keyPassword);
    }

    /**
     * 
     * convert the JKS to PKCS12
     */
    public static void convertJKS2PFX(URL jksUrl, File pfxFile, char[] storePassword, String keyAlias, char[] keyPassword)
            throws Exception {
        convert(JKS, jksUrl, PKCS12, pfxFile, storePassword, keyAlias, keyPassword);
    }

    private static void convert(String sourceType, URL sourceUrl, String targetType, File targetFile, char[] storePassword,
            String keyAlias, char[] keyPassword) throws Exception {
        KeyStore sourceKeystore = KeyStore.getInstance(sourceType);
        InputStream openStream = sourceUrl.openStream();
        try {
            sourceKeystore.load(openStream, storePassword);
        } finally {
            openStream.close();
        }
        KeyStore targetKeystore = KeyStore.getInstance(targetType);
        targetKeystore.load(null, storePassword);

        Enumeration enums = sourceKeystore.aliases();

        while (enums.hasMoreElements()) {
            String alias = (String) enums.nextElement();
            if (sourceKeystore.isKeyEntry(alias) && alias.equals(keyAlias)) {
                Key key = sourceKeystore.getKey(keyAlias, keyPassword);
                Certificate[] certChain = sourceKeystore.getCertificateChain(keyAlias);
                targetKeystore.setKeyEntry(keyAlias, key, keyPassword, certChain);

            } else if (sourceKeystore.isCertificateEntry(keyAlias)) {
                Certificate cert = sourceKeystore.getCertificate(keyAlias);
                if (cert instanceof X509Certificate) {
                    X509Certificate[] certificates = new X509Certificate[] { (X509Certificate) cert };
                }
                targetKeystore.setCertificateEntry(keyAlias, cert);
            }
        }
        FileOutputStream fos = new FileOutputStream(targetFile);
        try {
            targetKeystore.store(fos, storePassword);
        } finally {
            fos.close();
        }
    }

    public static void createKeyStore(File keystoreFile, String storeType, char[] storePassword, String keyType, String keyAlias,
            char[] keyPassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(storeType);
        keyStore.load(null, null);

        KeyGenerator keyGen = KeyGenerator.getInstance(keyType);
        keyGen.init(128);
        Key key = keyGen.generateKey();
        keyStore.setKeyEntry(keyAlias, key, keyPassword, null);

        keyStore.store(new FileOutputStream(keystoreFile), storePassword);
    }

}
