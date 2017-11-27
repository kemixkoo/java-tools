package xyz.kemix.xml.sign.jdk;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 */
public abstract class AbsTestKeyPairGen {

    protected File tempDir;

    @Before
    public void setup() throws IOException {
        tempDir = File.createTempFile(this.getClass().getSimpleName(), "");
        tempDir.delete();
        tempDir.mkdirs();
    }

    @After
    public void cleanup() throws IOException {
        if (tempDir != null)
            FileUtils.deleteDirectory(tempDir);
    }

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
