/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-11-05
 *
 */
public class GeneralClassReporterWithInnerJarTest extends GeneralClassReporterTest {

	@Override
	protected boolean withInnerJar() {
		return true;
	}

	// @Test
	// public void test_processFolder_bundleFolder() throws IOException {
	// File myJarFile = new File(tempDir, "myjar.jar");
	// assertTrue(myJarFile.exists());
	//
	// File bundleFolder = new File(tempDir, "bundle1");
	// File bundleLibFolder = new File(bundleFolder, "lib");
	// File libFolder = new File(tempDir, "lib/myjarlib.jar");
	// bundleLibFolder.mkdirs();
	// FileUtils.copyFileToDirectory(libFolder, bundleLibFolder);
	//
	// GeneralClassReporter reporter = new
	// GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
	// JSONArray result = reporter.processFolder(bundleFolder);
	//
	// /*
	// * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
	// * "bundle1","Details":{"Java 1.8":{"Sum":4,"Classes":[
	// * "lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18",
	// *
	// "lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18","xyz/kemix/test/Compiler18",
	// * "xyz/kemix/test/p/after/Compiler18"]},"Java 9":{"Sum":2,"Classes":[
	// * "lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}}]
	// */
	// assertNotNull(result);
	// assertEquals(2, result.length());
	//
	// // inner jar
	// final JSONObject line = result.getJSONObject(0);
	// assertEquals("xyz.kemix.bundle1",
	// line.getString(ResultKeys.KEY_BUNDLE_NAME));
	// assertEquals("0.0.1", line.getString(ResultKeys.KEY_BUNDLE_VERSION));
	// assertEquals("bundle1", line.getString(ResultKeys.KEY_FILE_PATH));
	// final JSONObject myJarDetails = line.getJSONObject(ResultKeys.KEY_DETAILS);
	//
	// // 1.8
	// JSONObject obj18 =
	// myJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
	// assertEquals(2, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));
	//
	// JSONArray arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
	// assertEquals(2, arr18.length());
	// final List<Object> list18 = arr18.toList();
	// assertTrue(list18.contains("xyz/kemix/test/Compiler18"));
	// assertTrue(list18.contains("xyz/kemix/test/p/after/Compiler18"));
	//
	// // 9
	// JSONObject obj9 =
	// myJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
	// assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
	//
	// JSONArray arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
	// assertEquals(1, arr9.length());
	// assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));
	//
	// }
}
