package xyz.kemix.xml.sign.apache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-06
 *
 */
public abstract class AbsXmlKeyStoreApacheStAXSign extends AbsXmlKeyStoreApacheSign {

    public void sign(File file, File signedFile) throws Exception {
        sign(new BufferedInputStream(new FileInputStream(file)), new BufferedOutputStream(new FileOutputStream(signedFile)));
    }

    public abstract void sign(InputStream inputStream, OutputStream outputStream) throws Exception;

    public boolean valid(File file) throws Exception {
        return valid(new BufferedInputStream(new FileInputStream(file)));
    }

    public abstract boolean valid(InputStream stream) throws Exception;
}
