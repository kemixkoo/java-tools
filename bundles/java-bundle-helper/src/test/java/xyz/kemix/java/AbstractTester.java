/**
 *  
 */
package xyz.kemix.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-30
 *
 */
public class AbstractTester {
	protected File tempDir;

	@Before
	public void setup() throws IOException {
		tempDir = File.createTempFile(this.getClass().getSimpleName(), "");
		tempDir.delete();
		tempDir.mkdirs();
	}

	@After
	public void cleanup() throws IOException {

		FileUtils.deleteDirectory(tempDir);
	}

	protected InputStream getResource(String path) {
		return this.getClass().getResourceAsStream('/' + path);
	}
}
