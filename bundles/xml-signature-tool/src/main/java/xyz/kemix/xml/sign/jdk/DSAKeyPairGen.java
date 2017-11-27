package xyz.kemix.xml.sign.jdk;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 * java.security.interfaces.DSAPublicKey: sun.security.provider.DSAPublicKeyImpl
 * 
 * java.security.interfaces.DSAPrivateKey: sun.security.provider.DSAPrivateKey
 * 
 * Recommend the keySize to be set 512, 768, 1024, 2048.
 */
public class DSAKeyPairGen extends KeyPairGen {

    public DSAKeyPairGen(int keySize) {
        super("DSA", keySize);
    }

    @Override
    public KeyPair generateKey() throws NoSuchAlgorithmException {
        KeyPair keyPair = super.generateKey();
        // DSAPublicKey pubKey = (DSAPublicKey) keyPair.getPublic();
        // DSAPrivateKey priKey = (DSAPrivateKey) keyPair.getPrivate();
        return keyPair;
    }

    @Override
    protected EncodedKeySpec getPrivateKeySpec(byte[] values) {
        return new PKCS8EncodedKeySpec(values);
    }

    @Override
    protected EncodedKeySpec getPublicKeySpec(byte[] values) {
        return new X509EncodedKeySpec(values);
    }
}
