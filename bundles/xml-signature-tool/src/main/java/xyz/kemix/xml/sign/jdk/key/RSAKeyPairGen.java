package xyz.kemix.xml.sign.jdk.key;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 * java.security.interfaces.RSAPrivateKey: sun.security.rsa.RSAPrivateCrtKeyImpl
 *
 * java.security.interfaces.RSAPublicKey: sun.security.rsa.RSAPublicKeyImpl
 * 
 * the keySize must be >512, and can be any number also.
 */
public class RSAKeyPairGen extends KeyPairGen {

    public RSAKeyPairGen(int keySize) {
        super("RSA", keySize);
    }

    @Override
    public KeyPair generateKey() throws NoSuchAlgorithmException {
        return super.generateKey();
    }

}
