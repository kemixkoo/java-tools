/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.eclipse.EclipsePluginsManager;
import xyz.kemix.maven.plugin.java.compiler.AbstractJavaCompilerTester;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-29
 *
 */
public class EclipsePluginsClassReporterTest extends AbstractJavaCompilerTester {
	File productFolder;

	@Before
	public void setup() throws IOException {
		super.setup();
		prepareTestFiles();

		File myJarFile = new File(tempDir, "myjar.jar");
		assertTrue(myJarFile.exists());
		File innerJarFile = new File(tempDir, "inner-jar.jar");
		assertTrue(innerJarFile.exists());
		File bundleFolder = new File(tempDir, "bundle1");
		assertTrue(bundleFolder.exists());

		File libFolder = new File(tempDir, "lib");
		assertTrue(bundleFolder.exists());
		FileUtils.copyDirectoryToDirectory(libFolder, bundleFolder);

		productFolder = new File(tempDir, "product");
		File pluginsFolder = new File(productFolder, EclipsePluginsManager.FOLDER_PLUGINS);

		FileUtils.copyFileToDirectory(myJarFile, pluginsFolder);
		FileUtils.copyFileToDirectory(innerJarFile, pluginsFolder);
		FileUtils.copyDirectoryToDirectory(bundleFolder, pluginsFolder);
	}

	@Test
	public void test_processFolder_java17_compatible() throws IOException {
		EclipsePluginsClassReporter reporter = new EclipsePluginsClassReporter(CompilerVersion.JAVA_1_7, true, -1,
				true);
		final JSONArray result = reporter.processProduct(productFolder);

		/*
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
		 * "plugins/bundle1","Details":{"Java 1.8":{"Sum":4,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18",
		 * "lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18","xyz/kemix/test/Compiler18",
		 * "xyz/kemix/test/p/after/Compiler18"]},"Java 9":{"Sum":2,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}},{
		 * "BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "plugins/inner-jar.jar","Details":{"Java 1.8":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java 9":{
		 * "Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}},{"BundleName":
		 * "xyz.kemix.myjar","BundleVersion":"1.0.0","FilePath":"plugins/myjar.jar",
		 * "Details":{"Java 1.8":{"Sum":1,"Classes":["xyz/kemix/test/p8/Compiler18"]}
		 * ,"Java 9":{"Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(
				"[{\"BundleName\":\"xyz.kemix.bundle1\",\"BundleVersion\":\"0.0.1\",\"FilePath\":\"plugins/bundle1\",\"Details\":{\"Java 1.8\":{\"Sum\":4,\"Classes\":[\"lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18\",\"lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18\",\"xyz/kemix/test/Compiler18\",\"xyz/kemix/test/p/after/Compiler18\"]},\"Java 9\":{\"Sum\":2,\"Classes\":[\"lib/myjarlib.jar!xyz/kemix/test/Compiler9\",\"xyz/kemix/test/Compiler9\"]}}},{\"BundleName\":\"xyz.kemix.inner.jar\",\"BundleVersion\":\"1.0.1\",\"FilePath\":\"plugins/inner-jar.jar\",\"Details\":{\"Java 1.8\":{\"Sum\":2,\"Classes\":[\"xyz/kemix/test/clazz/Compiler18\",\"xyz/kemix/test/p8/Compiler18\"]},\"Java 9\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler9\"]}}},{\"BundleName\":\"xyz.kemix.myjar\",\"BundleVersion\":\"1.0.0\",\"FilePath\":\"plugins/myjar.jar\",\"Details\":{\"Java 1.8\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p8/Compiler18\"]},\"Java 9\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler9\"]}}}]",
				result.toString());
	}

	@Test
	public void test_processFolder_java18_compatible() throws IOException {
		EclipsePluginsClassReporter reporter = new EclipsePluginsClassReporter(CompilerVersion.JAVA_1_8, true, -1,
				true);
		final JSONArray result = reporter.processProduct(productFolder);

		/*
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
		 * "plugins/bundle1","Details":{"Java 9":{"Sum":2,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}},
		 * {"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "plugins/inner-jar.jar","Details":{"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}, {"BundleName":"xyz.kemix.myjar",
		 * "BundleVersion":"1.0.0","FilePath":"plugins/myjar.jar","Details":{"Java 9":{
		 * "Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(3, result.length());

		// bundle folder
		final JSONObject bundleFolderLine = result.getJSONObject(0);
		assertEquals("xyz.kemix.bundle1", bundleFolderLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("0.0.1", bundleFolderLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("plugins/bundle1", bundleFolderLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject bundleFolderDetails = bundleFolderLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 9
		JSONObject obj9 = bundleFolderDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(2, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr9.length());
		final List<Object> list9 = arr9.toList();
		assertTrue(list9.contains("xyz/kemix/test/Compiler9"));
		assertTrue(list9.contains("lib/myjarlib.jar!xyz/kemix/test/Compiler9"));

		// inner jar
		final JSONObject innerJarLine = result.getJSONObject(1);
		assertEquals("xyz.kemix.inner.jar", innerJarLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.1", innerJarLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("plugins/inner-jar.jar", innerJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject innerJarDetails = innerJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 9
		obj9 = innerJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
		arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));

		// my jar
		final JSONObject myJarLine = result.getJSONObject(2);
		assertEquals("xyz.kemix.myjar", myJarLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.0", myJarLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("plugins/myjar.jar", myJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject myJarDetails = myJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 9
		obj9 = myJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
		arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));
	}

	@Test
	public void test_processFolder_java18_incompatible() throws IOException {
		EclipsePluginsClassReporter reporter = new EclipsePluginsClassReporter(CompilerVersion.JAVA_1_8, false, -1,
				true);
		final JSONArray result = reporter.processProduct(productFolder);

		/*
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
		 * "plugins/bundle1","Details":{"Java 1.7":{"Sum":3,"Classes":[
		 * "xyz/kemix/test/Compiler17","xyz/kemix/test/p/Compiler17",
		 * "xyz/kemix/test/p/after/Compiler17"]},"Java 1.3":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/p/Compiler13"]},"Java 1.6":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/Compiler16","xyz/kemix/test/p/Compiler16"]},"Java 1.5":{"Sum"
		 * :1,"Classes":["xyz/kemix/test/p/Compiler15"]},"Java 9":{"Sum":2,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}},{
		 * "BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "plugins/inner-jar.jar","Details":{"Java 1.7":{"Sum":2,"Classes":[
		 * "xyz/kemix/java/Compiler17","xyz/kemix/test/p/Compiler17"]},"Java 1.3":{"Sum"
		 * :1,"Classes":["xyz/kemix/test/p/Compiler13"]},"Java 1.6":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/p/Compiler16"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]},"Java 1.5":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/p/Compiler15"]}}},{"BundleName":"xyz.kemix.myjar",
		 * "BundleVersion":"1.0.0","FilePath":"plugins/myjar.jar","Details":{"Java 1.7":
		 * {"Sum":1,"Classes":["xyz/kemix/test/Compiler17"]},"Java 1.5":{"Sum":1,
		 * "Classes":["xyz/kemix/test/Compiler15"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(
				"[{\"BundleName\":\"xyz.kemix.bundle1\",\"BundleVersion\":\"0.0.1\",\"FilePath\":\"plugins/bundle1\",\"Details\":{\"Java 1.7\":{\"Sum\":3,\"Classes\":[\"xyz/kemix/test/Compiler17\",\"xyz/kemix/test/p/Compiler17\",\"xyz/kemix/test/p/after/Compiler17\"]},\"Java 1.3\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p/Compiler13\"]},\"Java 1.6\":{\"Sum\":2,\"Classes\":[\"xyz/kemix/test/Compiler16\",\"xyz/kemix/test/p/Compiler16\"]},\"Java 1.5\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p/Compiler15\"]},\"Java 9\":{\"Sum\":2,\"Classes\":[\"lib/myjarlib.jar!xyz/kemix/test/Compiler9\",\"xyz/kemix/test/Compiler9\"]}}},{\"BundleName\":\"xyz.kemix.inner.jar\",\"BundleVersion\":\"1.0.1\",\"FilePath\":\"plugins/inner-jar.jar\",\"Details\":{\"Java 1.7\":{\"Sum\":2,\"Classes\":[\"xyz/kemix/java/Compiler17\",\"xyz/kemix/test/p/Compiler17\"]},\"Java 1.3\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p/Compiler13\"]},\"Java 1.6\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p/Compiler16\"]},\"Java 9\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler9\"]},\"Java 1.5\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/p/Compiler15\"]}}},{\"BundleName\":\"xyz.kemix.myjar\",\"BundleVersion\":\"1.0.0\",\"FilePath\":\"plugins/myjar.jar\",\"Details\":{\"Java 1.7\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler17\"]},\"Java 1.5\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler15\"]},\"Java 9\":{\"Sum\":1,\"Classes\":[\"xyz/kemix/test/Compiler9\"]}}}]",
				result.toString());
	}

	@Test
	public void test_processFolder_java9_compatible() throws IOException {
		EclipsePluginsClassReporter reporter = new EclipsePluginsClassReporter(CompilerVersion.JAVA_9, true, -1, true);
		final JSONArray result = reporter.processProduct(productFolder);
		assertNotNull(result);
		assertEquals(0, result.length());
	}
}
