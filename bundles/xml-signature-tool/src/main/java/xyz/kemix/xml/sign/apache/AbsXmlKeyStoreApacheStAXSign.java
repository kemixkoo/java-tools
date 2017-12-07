package xyz.kemix.xml.sign.apache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

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

    /**
     * Overwrite the doc
     */
    public void sign(File file) throws Exception {
        // load the file directly, if overwrite, will have problem.
        ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(new FileInputStream(file)));
        sign(bais, new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
     * sign the input stream for xml, and output the signed stream.
     * 
     * no need close the stream, which will be closed after sign
     * 
     */
    public abstract void sign(InputStream inputStream, OutputStream outputStream) throws Exception;

    public boolean valid(File file) throws Exception {
        return valid(new BufferedInputStream(new FileInputStream(file)));
    }

    public abstract boolean valid(InputStream stream) throws Exception;
}
