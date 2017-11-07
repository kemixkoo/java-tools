/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import xyz.kemix.java.io.ZipFileUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-30
 *
 */
public class AbstractJavaCompilerTester {
	protected File tempDir;

	@Before
	public void setup() throws IOException {
		tempDir = File.createTempFile(this.getClass().getSimpleName(), "");
		tempDir.delete();
		tempDir.mkdirs();
	}

	protected void prepareTestFiles() throws IOException {
		URL testFileURL = this.getClass().getResource("/files/test_files.zip");
		assertNotNull(testFileURL);
		File testFile = new File(testFileURL.getFile());
		assertTrue(testFile.exists());
		ZipFileUtil.unzip(testFile, tempDir);
	}

	@After
	public void cleanup() throws IOException {

		FileUtils.deleteDirectory(tempDir);
	}

	protected InputStream getResource(String path) {
		return this.getClass().getResourceAsStream('/' + path);
	}
}
