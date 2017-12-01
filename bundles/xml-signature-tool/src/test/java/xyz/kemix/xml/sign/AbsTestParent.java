package xyz.kemix.xml.sign;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 * Created at 2017-12-01
 *
 */
public class AbsTestParent {

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
}
