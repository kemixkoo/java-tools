package xyz.kemix.xml.sign.jdk;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-27
 *
 */
public class DSAKeyPairGenTest extends AbsTestKeyPairGen {

    @Override
    protected KeyPairGen createKeyPairGen(int keySize) {
        return new DSAKeyPairGen(keySize);
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

}
