package xyz.kemix.xml.sign.jdk.key;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Ignore;
import org.junit.Test;

import xyz.kemix.xml.sign.jdk.key.KeyPairGen;
import xyz.kemix.xml.sign.jdk.key.RSAKeyPairGen;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 */
public class RSAKeyPairGenTest extends AbsTestKeyPairGen {

    @Override
    protected KeyPairGen createKeyPairGen(int keySize) {
        return new RSAKeyPairGen(keySize);
    }

    @Test
    public void test_512_IT() throws IOException, GeneralSecurityException {
        doTest(512);
    }

    @Test
    public void test_768_IT() throws IOException, GeneralSecurityException {
        doTest(768);
    }

    @Test
    public void test_1024_IT() throws IOException, GeneralSecurityException {
        doTest(1024);
    }

    @Test
    public void test_2048_IT() throws IOException, GeneralSecurityException {
        doTest(2048);
    }

    @Test
    public void test_666_IT() throws IOException, GeneralSecurityException {
        doTest(666);
    }

    @Test
    @Ignore
    public void test_7777_IT() throws IOException, GeneralSecurityException {
        // doTest(7777); // spent 35s+
    }

    @Test
    @Ignore
    public void test_10000_IT() throws IOException, GeneralSecurityException {
        // doTest(10000); // not stably, spent very long time, 100s+, even 357s
    }

}
