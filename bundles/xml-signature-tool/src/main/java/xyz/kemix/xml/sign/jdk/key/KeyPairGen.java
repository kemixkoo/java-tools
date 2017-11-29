package xyz.kemix.xml.sign.jdk.key;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import xyz.kemix.xml.sign.Base64Util;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 */
public abstract class KeyPairGen {

    public static final String FILE_PRIVATE_KEY = "pri.key";

    public static final String FILE_PUBLIC_KEY = "pub.key";

    private final String algorithm;

    private final int keysize;

    public KeyPairGen(String algorithm, int keysize) {
        this.algorithm = algorithm;
        this.keysize = keysize;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getKeysize() {
        return keysize;
    }

    public KeyPair generateKey() throws NoSuchAlgorithmException {
        // create public/private key
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance(getAlgorithm());
        kpGen.initialize(getKeysize());
        KeyPair keyPair = kpGen.generateKeyPair();
        return keyPair;
    }

    public void saveKeyPair(KeyPair keyPair, File folder) throws IOException {
        if (folder == null) {
            return;
        }
        saveKey(new File(folder, FILE_PRIVATE_KEY), keyPair.getPrivate());
        saveKey(new File(folder, FILE_PUBLIC_KEY), keyPair.getPublic());
    }

    public void saveKeyPair(KeyPair keyPair, File priFile, File pubFile) throws IOException {
        saveKey(priFile, keyPair.getPrivate());
        saveKey(pubFile, keyPair.getPublic());
    }

    public String getKeyString(Key key) {
        return Base64.getMimeEncoder().encodeToString(key.getEncoded());
    }

    public void saveKey(File file, Key key) throws IOException {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file));
            IOUtils.write(getKeyString(key), pw);
        } finally {
            if (pw != null)
                pw.close();
        }
    }

    public KeyPair loadKeyPair(File folder) throws IOException, GeneralSecurityException {
        if (folder == null || !folder.exists()) {
            return null;
        }
        PrivateKey privateKey = loadPrivateKey(new File(folder, FILE_PRIVATE_KEY));
        PublicKey publicKey = loadPublicKey(new File(folder, FILE_PUBLIC_KEY));
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair loadKeyPair(File priFile, File pubFile) throws IOException, GeneralSecurityException {
        PrivateKey privateKey = loadPrivateKey(priFile);
        PublicKey publicKey = loadPublicKey(pubFile);
        return new KeyPair(publicKey, privateKey);
    }

    public PrivateKey loadPrivateKey(File priFile) throws IOException, GeneralSecurityException {
        FileReader fr = null;
        try {
            fr = new FileReader(priFile);
            final String encoded = IOUtils.toString(fr);
            byte[] values = Base64Util.decode(encoded);

            final KeyFactory keyFactory = KeyFactory.getInstance(getAlgorithm());
            return keyFactory.generatePrivate(getPrivateKeySpec(values));

        } finally {
            if (fr != null)
                fr.close();
        }
    }

    public PublicKey loadPublicKey(File pubFile) throws IOException, GeneralSecurityException {

        FileReader fr = null;
        try {
            fr = new FileReader(pubFile);
            final String encoded = IOUtils.toString(fr);
            byte[] values = Base64Util.decode(encoded);

            final KeyFactory keyFactory = KeyFactory.getInstance(getAlgorithm());
            return keyFactory.generatePublic(getPublicKeySpec(values));

        } finally {
            if (fr != null)
                fr.close();
        }
    }

    protected EncodedKeySpec getPrivateKeySpec(byte[] values) {
        return new PKCS8EncodedKeySpec(values);
    }

    protected EncodedKeySpec getPublicKeySpec(byte[] values) {
        return new X509EncodedKeySpec(values);
    };
}
