package xyz.kemix.xml.sign;

import java.math.BigInteger;
import java.util.Base64;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-11-29
 *
 */
public class Base64Util {

    public static String encode(byte[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return Base64.getMimeEncoder().encodeToString(value);
    }

    @SuppressWarnings("restriction")
    public static String encode(BigInteger big) {
        return com.sun.org.apache.xml.internal.security.utils.Base64.encode(big);
    }

    public static byte[] decode(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Base64.getMimeDecoder().decode(value);
    }
}
