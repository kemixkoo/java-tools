package xyz.kemix.xml.sign.jdk.key;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.junit.Assert;

import xyz.kemix.xml.sign.AbsTestParent;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 */
public abstract class AbsTestKeyPairGen extends AbsTestParent {

    protected abstract KeyPairGen createKeyPairGen(int keySize);

    protected void doTest(int keySize) throws IOException, GeneralSecurityException {
        KeyPairGen gen = createKeyPairGen(keySize);
        KeyPair keyPair = gen.generateKey();

        File priFile = new File(tempDir, DSAKeyPairGen.FILE_PRIVATE_KEY);
        File pubFile = new File(tempDir, DSAKeyPairGen.FILE_PUBLIC_KEY);
        Assert.assertFalse(priFile.exists());
        Assert.assertFalse(pubFile.exists());

        gen.saveKeyPair(keyPair, priFile, pubFile);

        Assert.assertTrue(priFile.exists());
        Assert.assertTrue(pubFile.exists());

        KeyPair loadKeyPair = gen.loadKeyPair(priFile, pubFile);
        Assert.assertNotNull(loadKeyPair);

        Assert.assertEquals(gen.getKeyString(keyPair.getPrivate()), gen.getKeyString(loadKeyPair.getPrivate()));
        Assert.assertEquals(gen.getKeyString(keyPair.getPublic()), gen.getKeyString(loadKeyPair.getPublic()));
    }

}
