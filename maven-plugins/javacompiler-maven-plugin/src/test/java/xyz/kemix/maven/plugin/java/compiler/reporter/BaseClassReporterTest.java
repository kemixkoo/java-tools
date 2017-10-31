/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-31
 *
 */
public class BaseClassReporterTest {

	@Test
	public void test_processClass() throws IOException {
		doTest_ProcessClass(CompilerVersion.JAVA_1_5, true);
		doTest_ProcessClass(CompilerVersion.JAVA_1_6, true);
		doTest_ProcessClass(CompilerVersion.JAVA_1_7, true);
		doTest_ProcessClass(CompilerVersion.JAVA_9, false); // compile by 1.8
	}

	private void doTest_ProcessClass(CompilerVersion baseVersion, boolean compatible) throws IOException {
		final String simpleName = this.getClass().getSimpleName(); // compile by 1.8 at least
		URL url = this.getClass().getResource(simpleName + FileExts.CLASS.ext());
		assertNotNull(url);

		BaseClassReporter reporter = new BaseClassReporter(baseVersion, compatible, -1, false);
		JSONArray result = reporter.processClass(new File(url.getPath()));
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(1, line.length());

		String verKey = line.keys().next(); // Java x
		JSONObject listJson = line.getJSONObject(verKey);
		assertEquals(1, listJson.length());

		JSONArray list = listJson.getJSONArray(AbstractClassReporter.KEY_CLASSES);
		assertEquals(1, list.length());

		//
		assertEquals(simpleName, list.getString(0));

	}

	@Test
	public void test_processClass_compatible() throws IOException {
		doTest_processClassWithCompatibleVersion(CompilerVersion.JAVA_9);
	}

	@Test
	public void test_processClass_sameVersion() throws IOException {
		doTest_processClassWithCompatibleVersion(CompilerVersion.JAVA_1_8);
	}

	private void doTest_processClassWithCompatibleVersion(CompilerVersion baseVersion) throws IOException {
		final String simpleName = this.getClass().getSimpleName(); // if compile by 1.8
		URL url = this.getClass().getResource(simpleName + FileExts.CLASS.ext());
		assertNotNull(url);

		BaseClassReporter reporter = new BaseClassReporter(baseVersion, true, -1, false);
		JSONArray result = reporter.processClass(new File(url.getPath()));
		assertNotNull(result);
		assertEquals(0, result.length());

	}

	@Test
	public void test_processClasses_noBaseFile() throws IOException {
		URL url = this.getClass().getResource("");
		assertNotNull(url);

		File folder = new File(url.getPath());
		assertTrue(folder.exists());

		BaseClassReporter reporter = new BaseClassReporter(CompilerVersion.JAVA_1_7, true, -1, false);
		JSONArray result = reporter.processClasses(folder.listFiles());

		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(1, line.length());

		String verKey = line.keys().next(); // Java x
		JSONObject listJson = line.getJSONObject(verKey);
		assertEquals(1, listJson.length());

		JSONArray list = listJson.getJSONArray(AbstractClassReporter.KEY_CLASSES);
		assertTrue(list.length() > 1);

		final String simpleName = this.getClass().getSimpleName(); // if compile by 1.8
		assertTrue(list.toList().contains(simpleName));
	}

	@Test
	public void test_processClasses_withBaseFile() throws IOException {
		URL root = this.getClass().getResource("/");
		URL url = this.getClass().getResource("");
		assertNotNull(url);

		File folder = new File(url.getPath());
		assertTrue(folder.exists());

		BaseClassReporter reporter = new BaseClassReporter(CompilerVersion.JAVA_1_7, true, -1, false);
		JSONArray result = reporter.processClasses(new File(root.getPath()), folder.listFiles());

		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(1, line.length());

		String verKey = line.keys().next(); // Java x
		JSONObject listJson = line.getJSONObject(verKey);
		assertEquals(1, listJson.length());

		JSONArray list = listJson.getJSONArray(AbstractClassReporter.KEY_CLASSES);
		assertTrue(list.length() > 1);

		final String simpleName = this.getClass().getSimpleName(); // if compile by 1.8
		String path = this.getClass().getPackage().getName().replace('.', '/');
		assertTrue(list.toList().contains(path + '/' + simpleName));
	}

	// @Test
	public void test_processJar() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processJars_noBaseFile() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processJars_withBaseFile() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_onlyClasses() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_classesInSubFolders_withMenifest() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_classesInSubFolders_withoutMenifest() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_jarsInSubFolders_withMenifest() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_jarsInSubFolders_withoutMenifest() {
		fail("Not impl yet!");
	}

	// @Test
	public void test_processFolder_jarsAndClassesInSubFolders() {
		fail("Not impl yet!");
	}

}
