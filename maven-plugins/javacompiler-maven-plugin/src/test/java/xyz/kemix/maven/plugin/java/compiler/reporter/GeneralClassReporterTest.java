/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.io.ZipFileUtil;
import xyz.kemix.maven.plugin.java.compiler.AbstractTester;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-31
 *
 */
public class GeneralClassReporterTest extends AbstractTester {
	class GeneralClassReporterTestClass extends GeneralClassReporter {

		public GeneralClassReporterTestClass(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion) {
			super(baseJDKVersion, compatibleJDKVersion, -1, withInnerJar());
		}

	}

	protected boolean withInnerJar() {
		return false;
	}

	@Before
	public void setup() throws IOException {
		super.setup();
		URL testFileURL = this.getClass().getResource("/files/test_files.zip");
		assertNotNull(testFileURL);
		File testFile = new File(testFileURL.getFile());
		assertTrue(testFile.exists());
		ZipFileUtil.unzip(testFile, tempDir);
	}

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

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(baseVersion, compatible);
		JSONArray result = reporter.processClass(new File(url.getPath()));

		/*
		 * [{"FilePath":".","Details":{"Java 1.8":{"Sum":1,"Classes":[
		 * "GeneralClassReporterTest"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(2, line.length());

		assertEquals(".", line.getString(ResultKeys.KEY_FILE_PATH));

		final JSONObject json = line.getJSONObject(ResultKeys.KEY_DETAILS)
				.getJSONObject(CompilerVersion.JAVA_1_8.toString());

		assertEquals(1, json.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray list = json.getJSONArray(ResultKeys.KEY_CLASSES);
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

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(baseVersion, true);
		JSONArray result = reporter.processClass(new File(url.getPath()));
		assertNotNull(result);
		assertEquals(0, result.length());
	}

	@Test
	public void test_processClasses_noBaseFile() throws IOException {
		File jdkTestJar = new File(tempDir, "bundle1/xyz/kemix/test/");
		assertTrue(jdkTestJar.exists());

		File[] classes = jdkTestJar.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileExts.CLASS.of(f);
			}
		});

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processClasses(classes);

		/*
		 * [{"FilePath":"test","Details":{"Java 1.8":{"Sum":1,"Classes":["Compiler18"]}
		 * ,"Java 9":{"Sum":1,"Classes":["Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(2, line.length());

		// 1.8
		assertEquals("test", line.getString(ResultKeys.KEY_FILE_PATH));

		JSONArray list18 = line.getJSONObject(ResultKeys.KEY_DETAILS).getJSONObject(CompilerVersion.JAVA_1_8.toString())
				.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list18.length());
		assertEquals("Compiler18", list18.getString(0));

		// 1.9
		JSONArray list9 = line.getJSONObject(ResultKeys.KEY_DETAILS).getJSONObject(CompilerVersion.JAVA_9.toString())
				.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list9.length());
		assertEquals("Compiler9", list9.getString(0));
	}

	@Test
	public void test_processClasses_withBaseFile_currentFolder() throws IOException {
		File jdkTestJar = new File(tempDir, "bundle1/xyz/kemix/test/");
		assertTrue(jdkTestJar.exists());

		File[] classes = jdkTestJar.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileExts.CLASS.of(f);
			}
		});

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processClasses(jdkTestJar, classes);

		/*
		 * [{"FilePath":".","Details":{"Java 1.8":{"Sum":1,"Classes":["Compiler18"]}
		 * ,"Java 9":{"Sum":1,"Classes":["Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		final JSONObject json = result.getJSONObject(0);
		assertEquals(".", json.getString(ResultKeys.KEY_FILE_PATH));

		JSONObject detailsJson = json.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJson.length());

		// 1.8
		final JSONObject json18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(1, json18.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray list18 = json18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list18.length());
		assertEquals("Compiler18", list18.getString(0));

		// 1.9
		final JSONObject json9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, json9.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray list9 = json9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list9.length());
		assertEquals("Compiler9", list9.getString(0));
	}

	@Test
	public void test_processClasses_withBaseFile_rootFolder() throws IOException {
		File jdkTestJar = new File(tempDir, "bundle1/xyz/kemix/test/");
		assertTrue(jdkTestJar.exists());

		File[] classes = jdkTestJar.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileExts.CLASS.of(f);
			}
		});

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processClasses(tempDir, classes);

		/*
		 * [{"FilePath":"bundle1/xyz/kemix/test","Details":{"Java 1.8":{"Sum":1,
		 * "Classes":["bundle1/xyz/kemix/test/Compiler18"]},"Java 9":{"Sum":1,"Classes":
		 * ["bundle1/xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(2, line.length());

		assertEquals("bundle1/xyz/kemix/test", line.getString(ResultKeys.KEY_FILE_PATH));

		// 1.8
		final JSONObject detailsJson = line.getJSONObject(ResultKeys.KEY_DETAILS);
		JSONArray list18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString())
				.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list18.length());
		assertEquals("bundle1/xyz/kemix/test/Compiler18", list18.getString(0));

		// 1.9
		JSONArray list9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString())
				.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, list9.length());
		assertEquals("bundle1/xyz/kemix/test/Compiler9", list9.getString(0));
	}

	@Test
	public void test_processJar() throws IOException {
		File jdkTestJar = new File(tempDir, "myjar.jar");
		assertTrue(jdkTestJar.exists());

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processJar(jdkTestJar);

		/*
		 * [{"BundleName":"xyz.kemix.myjar1","BundleVersion":"1.0.0","FilePath":
		 * "myjar1.jar","Details":{"Java 1.8":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/p8/Compiler18"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject jar1Json = result.getJSONObject(0);

		assertTrue(jar1Json.has(ResultKeys.KEY_BUNDLE_NAME));
		assertTrue(jar1Json.has(ResultKeys.KEY_BUNDLE_VERSION));
		assertTrue(jar1Json.has(ResultKeys.KEY_FILE_PATH));
		assertTrue(jar1Json.has(ResultKeys.KEY_DETAILS));

		assertEquals(jdkTestJar.getName(), jar1Json.getString(ResultKeys.KEY_FILE_PATH));

		JSONObject detailsJson = jar1Json.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJson.length());

		// 1.8
		assertTrue(detailsJson.has(CompilerVersion.JAVA_1_8.toString()));

		JSONObject json18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertTrue(json18.has(ResultKeys.KEY_CLASSES));
		assertTrue(json18.has(ResultKeys.KEY_CLASSES_SUM));

		int sum18 = json18.getInt(ResultKeys.KEY_CLASSES_SUM);
		assertEquals(1, sum18);

		JSONArray classesJson18 = json18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classesJson18.length());
		assertEquals("xyz/kemix/test/p8/Compiler18", classesJson18.getString(0));

		// 9
		assertTrue(detailsJson.has(CompilerVersion.JAVA_9.toString()));

		JSONObject json9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertTrue(json9.has(ResultKeys.KEY_CLASSES));

		JSONArray classesJson9 = json9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classesJson9.length());
		assertEquals("xyz/kemix/test/Compiler9", classesJson9.getString(0));
	}

	@Test
	public void test_processJars_noBaseFile() throws IOException {
		File[] jars = tempDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileExts.JAR.of(f);
			}
		});
		assertNotNull(jars);

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processJars(jars);

		/*
		 * [{"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "inner-jar.jar","Details":{"Java 1.8":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java 9":{
		 * "Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}},{"BundleName":
		 * "xyz.kemix.myjar","BundleVersion":"1.0.0","FilePath":"myjar.jar","Details":
		 * {"Java 1.8":{"Sum":1,"Classes":["xyz/kemix/test/p8/Compiler18"]},"Java 9":{
		 * "Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}}]
		 */
		doTestProcessJars(result, "");
	}

	protected void doTestProcessJars(JSONArray result, String jarPath) {
		assertNotNull(result);
		assertEquals(2, result.length());

		doTestProcessJars_innerJar(result.getJSONObject(0), jarPath);
		doTestProcessJars_myJar(result.getJSONObject(1), jarPath);
	}

	protected void doTestProcessJars_innerJar(JSONObject innerJarJson, String jarPath) {
		assertTrue(innerJarJson.has(ResultKeys.KEY_BUNDLE_NAME));
		assertTrue(innerJarJson.has(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("xyz.kemix.inner.jar", innerJarJson.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.1", innerJarJson.getString(ResultKeys.KEY_BUNDLE_VERSION));

		assertTrue(innerJarJson.has(ResultKeys.KEY_FILE_PATH));
		assertEquals(jarPath + "inner-jar.jar", innerJarJson.getString(ResultKeys.KEY_FILE_PATH));

		assertTrue(innerJarJson.has(ResultKeys.KEY_DETAILS));
		JSONObject detailsJson = innerJarJson.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJson.length());

		// 1.8
		assertTrue(detailsJson.has(CompilerVersion.JAVA_1_8.toString()));
		JSONObject json18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString());

		assertTrue(json18.has(ResultKeys.KEY_CLASSES_SUM));
		int sum18 = json18.getInt(ResultKeys.KEY_CLASSES_SUM);
		assertEquals(2, sum18);

		assertTrue(json18.has(ResultKeys.KEY_CLASSES));
		JSONArray classesJson8 = json18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, classesJson8.length());
		assertEquals("xyz/kemix/test/clazz/Compiler18", classesJson8.getString(0));
		assertEquals("xyz/kemix/test/p8/Compiler18", classesJson8.getString(1));

		// 9
		assertTrue(detailsJson.has(CompilerVersion.JAVA_9.toString()));
		JSONObject json9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString());

		assertTrue(json9.has(ResultKeys.KEY_CLASSES_SUM));
		int sum9 = json9.getInt(ResultKeys.KEY_CLASSES_SUM);
		assertEquals(1, sum9);

		assertTrue(json9.has(ResultKeys.KEY_CLASSES));
		JSONArray classesJson9 = json9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classesJson9.length());
		assertEquals("xyz/kemix/test/Compiler9", classesJson9.getString(0));
	}

	protected void doTestProcessJars_myJar(JSONObject myJarJson, String jarPath) {
		assertTrue(myJarJson.has(ResultKeys.KEY_BUNDLE_NAME));
		assertTrue(myJarJson.has(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("xyz.kemix.myjar", myJarJson.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.0", myJarJson.getString(ResultKeys.KEY_BUNDLE_VERSION));

		assertTrue(myJarJson.has(ResultKeys.KEY_FILE_PATH));
		assertTrue(myJarJson.has(ResultKeys.KEY_DETAILS));

		assertEquals(jarPath + "myjar.jar", myJarJson.getString(ResultKeys.KEY_FILE_PATH));

		JSONObject detailsJson = myJarJson.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJson.length());

		// 1.8
		assertTrue(detailsJson.has(CompilerVersion.JAVA_1_8.toString()));
		JSONObject json18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString());

		assertTrue(json18.has(ResultKeys.KEY_CLASSES_SUM));
		int sum18 = json18.getInt(ResultKeys.KEY_CLASSES_SUM);
		assertEquals(1, sum18);

		assertTrue(json18.has(ResultKeys.KEY_CLASSES));
		JSONArray classesJson8 = json18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classesJson8.length());
		assertEquals("xyz/kemix/test/p8/Compiler18", classesJson8.getString(0));

		// 9
		assertTrue(detailsJson.has(CompilerVersion.JAVA_9.toString()));
		JSONObject json9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString());

		assertTrue(json9.has(ResultKeys.KEY_CLASSES_SUM));
		int sum9 = json9.getInt(ResultKeys.KEY_CLASSES_SUM);
		assertEquals(1, sum9);

		assertTrue(json9.has(ResultKeys.KEY_CLASSES));
		JSONArray classesJson9 = json9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classesJson9.length());
		assertEquals("xyz/kemix/test/Compiler9", classesJson9.getString(0));
	}

	@Test
	public void test_processJars_withBaseFile() throws IOException {
		File[] jars = tempDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return FileExts.JAR.of(f);
			}
		});
		assertNotNull(jars);
		File newFolder = new File(tempDir, "myfolder/subfolder");
		newFolder.mkdirs();
		for (File f : jars) {
			FileUtils.copyFileToDirectory(f, newFolder);
		}

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processJars(tempDir, newFolder.listFiles());

		/*
		 * [{"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "myfolder/subfolder/inner-jar.jar","Details":{"Java 1.8":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java 9":{
		 * "Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}},{"BundleName":
		 * "xyz.kemix.myjar","BundleVersion":"1.0.0","FilePath":
		 * "myfolder/subfolder/myjar.jar","Details":{"Java 1.8":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/p8/Compiler18"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}]
		 */
		doTestProcessJars(result, "myfolder/subfolder/");
	}

	@Test
	public void test_processFolder_classes() throws IOException {
		File classesFolder = new File(tempDir, "bundle1/xyz/kemix/test/p");
		assertTrue(classesFolder.exists());
		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_5, true);
		JSONArray result = reporter.processFolder(classesFolder, false);

		/*
		 * [{"FilePath":".","Details":{"Java 1.7":{"Sum":1,"Classes":["Compiler17"]}
		 * ,"Java 1.6":{"Sum":1,"Classes":["Compiler16"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		JSONObject line = result.getJSONObject(0);
		assertEquals(2, line.length());
		assertEquals(".", line.getString(ResultKeys.KEY_FILE_PATH));

		final JSONObject detailsJson = line.getJSONObject(ResultKeys.KEY_DETAILS);
		// 1.7
		assertTrue(detailsJson.has(CompilerVersion.JAVA_1_7.toString()));
		JSONObject classes17Obj = detailsJson.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		assertEquals(1, classes17Obj.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray classes17Arr = classes17Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17Arr.length());
		assertEquals("Compiler17", classes17Arr.getString(0));

		// 1.6
		assertTrue(detailsJson.has(CompilerVersion.JAVA_1_6.toString()));
		JSONObject classes16Obj = detailsJson.getJSONObject(CompilerVersion.JAVA_1_6.toString());
		assertEquals(1, classes16Obj.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray classes16Arr = classes16Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes16Arr.length());
		assertEquals("Compiler16", classes16Arr.getString(0));
	}

	@Test
	public void test_processFolder_classesInSubFolders() throws IOException {
		File classesFolder = new File(tempDir, "bundle1/xyz/kemix/test/p");
		assertTrue(classesFolder.exists());
		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_5, true);
		JSONArray result = reporter.processFolder(classesFolder, true);

		/*
		 * [{"FilePath":".","Details":{"Java 1.7":{"Sum":1,"Classes":["Compiler17"]}
		 * ,"Java 1.6":{"Sum":1,"Classes":["Compiler16"]}}},{"FilePath":"after",
		 * "Details":{"Java 1.8":{"Sum":1,"Classes":["after/Compiler18"]},"Java 1.7":{
		 * "Sum":1,"Classes":["after/Compiler17"]}}}]
		 */
		assertNotNull(result);
		assertEquals(2, result.length());

		// current
		JSONObject line = result.getJSONObject(0);
		assertEquals(".", line.getString(ResultKeys.KEY_FILE_PATH));

		final JSONObject detailsJson = line.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJson.length());

		// 1.7
		JSONObject classes17Obj = detailsJson.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		JSONArray classes17Arr = classes17Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17Arr.length());
		assertEquals("Compiler17", classes17Arr.getString(0));

		// 1.6
		JSONObject classes16Obj = detailsJson.getJSONObject(CompilerVersion.JAVA_1_6.toString());
		JSONArray classes16Arr = classes16Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes16Arr.length());
		assertEquals("Compiler16", classes16Arr.getString(0));

		// after
		JSONObject lineAfter = result.getJSONObject(1);
		assertEquals("after", lineAfter.getString(ResultKeys.KEY_FILE_PATH));

		final JSONObject detailsJsonAfter = lineAfter.getJSONObject(ResultKeys.KEY_DETAILS);
		assertEquals(2, detailsJsonAfter.length());

		// 1.8
		JSONObject classes18ObjAfter = detailsJsonAfter.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		JSONArray classes18ArrAfter = classes18ObjAfter.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes18ArrAfter.length());
		assertEquals("after/Compiler18", classes18ArrAfter.getString(0));

		// 1.7
		JSONObject classes17ObjAfter = detailsJsonAfter.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		JSONArray classes17ArrAfter = classes17ObjAfter.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17ArrAfter.length());
		assertEquals("after/Compiler17", classes17ArrAfter.getString(0));
	}

	@Test
	public void test_processFolder_classesInSubFolders_withoutMenifest() throws IOException {
		File classesFolder = new File(tempDir, "bundle1/xyz");
		assertTrue(classesFolder.exists());

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_6, true);
		JSONArray result = reporter.processFolder(classesFolder);

		/*
		 * [{"FilePath":"kemix/test","Details":{"Java 1.8":{"Sum":1,"Classes":[
		 * "kemix/test/Compiler18"]},"Java 1.7":{"Sum":1,"Classes":[
		 * "kemix/test/Compiler17"]},"Java 9":{"Sum":1,"Classes":["kemix/test/Compiler9"
		 * ]}}}, {"FilePath":"kemix/test/p","Details":{"Java 1.7":{"Sum":1,"Classes":[
		 * "kemix/test/p/Compiler17"]}}}, {"FilePath":"kemix/test/p/after","Details":
		 * {"Java 1.8":{"Sum":1,"Classes":["kemix/test/p/after/Compiler18"]},"Java 1.7":
		 * {"Sum":1,"Classes":["kemix/test/p/after/Compiler17"]}}}]
		 */
		assertNotNull(result);
		assertEquals(3, result.length());

		JSONObject testLine = result.getJSONObject(0);
		assertEquals("kemix/test", testLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject testDetails = testLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		JSONObject classes18Obj = testDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		JSONArray classes18Arr = classes18Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes18Arr.length());
		assertEquals("kemix/test/Compiler18", classes18Arr.getString(0));
		// 1.7
		JSONObject classes17Obj = testDetails.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		JSONArray classes17Arr = classes17Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17Arr.length());
		assertEquals("kemix/test/Compiler17", classes17Arr.getString(0));

		// 9
		JSONObject classes9Obj = testDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		JSONArray classes9Arr = classes9Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes9Arr.length());
		assertEquals("kemix/test/Compiler9", classes9Arr.getString(0));

		//
		JSONObject pLine = result.getJSONObject(1);
		assertEquals("kemix/test/p", pLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject pDetails = pLine.getJSONObject(ResultKeys.KEY_DETAILS);
		// 1.7
		classes17Obj = pDetails.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		classes17Arr = classes17Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17Arr.length());
		assertEquals("kemix/test/p/Compiler17", classes17Arr.getString(0));

		//
		JSONObject afterLine = result.getJSONObject(2);
		assertEquals("kemix/test/p/after", afterLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject afterDetails = afterLine.getJSONObject(ResultKeys.KEY_DETAILS);
		// 1.8
		classes18Obj = afterDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		classes18Arr = classes18Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes18Arr.length());
		assertEquals("kemix/test/p/after/Compiler18", classes18Arr.getString(0));
		// 1.7
		classes17Obj = afterDetails.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		classes17Arr = classes17Obj.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, classes17Arr.length());
		assertEquals("kemix/test/p/after/Compiler17", classes17Arr.getString(0));
	}

	@Test
	public void test_processFolder_classesInSubFolders_withMenifest() throws Exception {
		File classesFolder = new File(tempDir, "bundle1");
		assertTrue(classesFolder.exists());

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_6, true);
		JSONArray result = reporter.processFolder(classesFolder);

		/*
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
		 * "bundle1","Details":{"Java 1.8":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/Compiler18","xyz/kemix/test/p/after/Compiler18"]},"Java 1.7":
		 * {"Sum":3,"Classes":["xyz/kemix/test/Compiler17","xyz/kemix/test/p/Compiler17"
		 * ,"xyz/kemix/test/p/after/Compiler17"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		final JSONObject bundleLine = result.getJSONObject(0);
		assertEquals("xyz.kemix.bundle1", bundleLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("0.0.1", bundleLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("bundle1", bundleLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject detailsJson = bundleLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		JSONObject obj18 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(2, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr18.length());
		final List<Object> list18 = arr18.toList();
		assertTrue(list18.contains("xyz/kemix/test/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/p/after/Compiler18"));
		// 1.7
		JSONObject obj17 = detailsJson.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		assertEquals(3, obj17.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr17 = obj17.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(3, arr17.length());
		final List<Object> list17 = arr17.toList();
		assertTrue(list17.contains("xyz/kemix/test/Compiler17"));
		assertTrue(list17.contains("xyz/kemix/test/p/Compiler17"));
		assertTrue(list17.contains("xyz/kemix/test/p/after/Compiler17"));
		// 9
		JSONObject obj9 = detailsJson.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertEquals("xyz/kemix/test/Compiler9", arr9.getString(0));
	}

	@Test
	public void test_processFolder_jarsInSubFolders_withoutMenifest() throws IOException {
		File myJarFile = new File(tempDir, "myjar.jar");
		assertTrue(myJarFile.exists());
		File innerJarFile = new File(tempDir, "inner-jar.jar");
		assertTrue(innerJarFile.exists());

		File abcFolder = new File(tempDir, "abc");
		File xyzFolder = new File(abcFolder, "xyz");
		xyzFolder.mkdirs();
		FileUtils.copyFileToDirectory(myJarFile, xyzFolder);

		File hijFolder = new File(abcFolder, "def/hij");
		hijFolder.mkdirs();
		FileUtils.copyFileToDirectory(innerJarFile, hijFolder);

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_6, true);
		JSONArray result = reporter.processFolder(abcFolder);

		/*
		 * [{"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":
		 * "def/hij/inner-jar.jar","Details":{"Java 1.8":{"Sum":2,"Classes":[
		 * "xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java 1.7"
		 * :{"Sum":2,"Classes":["xyz/kemix/java/Compiler17",
		 * "xyz/kemix/test/p/Compiler17"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}},{"BundleName":"xyz.kemix.myjar",
		 * "BundleVersion":"1.0.0","FilePath":"xyz/myjar.jar","Details":{"Java 1.8":{
		 * "Sum":1,"Classes":["xyz/kemix/test/p8/Compiler18"]},"Java 1.7":{"Sum":1,
		 * "Classes":["xyz/kemix/test/Compiler17"]},"Java 9":{"Sum":1,"Classes":[
		 * "xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(2, result.length());

		// inner jar
		final JSONObject innerJarLine = result.getJSONObject(0);
		assertEquals("xyz.kemix.inner.jar", innerJarLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.1", innerJarLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("def/hij/inner-jar.jar", innerJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject innerJarDetails = innerJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		JSONObject obj18 = innerJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(2, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr18.length());
		final List<Object> list18 = arr18.toList();
		assertTrue(list18.contains("xyz/kemix/test/clazz/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/p8/Compiler18"));
		// 1.7
		JSONObject obj17 = innerJarDetails.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		assertEquals(2, obj17.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr17 = obj17.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr17.length());
		final List<Object> list17 = arr17.toList();
		assertTrue(list17.contains("xyz/kemix/java/Compiler17"));
		assertTrue(list17.contains("xyz/kemix/test/p/Compiler17"));

		// 9
		JSONObject obj9 = innerJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));

		// my jar
		final JSONObject myJarLine = result.getJSONObject(1);
		assertEquals("xyz.kemix.myjar", myJarLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("1.0.0", myJarLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("xyz/myjar.jar", myJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject myJarDetails = myJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		obj18 = myJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(1, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));

		arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr18.length());
		assertTrue(arr18.toList().contains("xyz/kemix/test/p8/Compiler18"));

		// 1.7
		obj17 = myJarDetails.getJSONObject(CompilerVersion.JAVA_1_7.toString());
		assertEquals(1, obj17.getInt(ResultKeys.KEY_CLASSES_SUM));

		arr17 = obj17.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr17.length());
		assertTrue(arr17.toList().contains("xyz/kemix/test/Compiler17"));

		// 9
		obj9 = myJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));

		arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));
	}

	@Test
	public void test_processFolder_bundleFolder() throws IOException {
		File myJarFile = new File(tempDir, "myjar.jar");
		assertTrue(myJarFile.exists());

		File bundleFolder = new File(tempDir, "bundle1");
		File bundleLibFolder = new File(bundleFolder, "lib");
		File libFolder = new File(tempDir, "lib/myjarlib.jar");
		bundleLibFolder.mkdirs();
		FileUtils.copyFileToDirectory(libFolder, bundleLibFolder);

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processFolder(bundleFolder);

		/*
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":
		 * "bundle1","Details":{"Java 1.8":{"Sum":4,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18",
		 * "lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18","xyz/kemix/test/Compiler18",
		 * "xyz/kemix/test/p/after/Compiler18"]},"Java 9":{"Sum":2,"Classes":[
		 * "lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}}]
		 */
		assertNotNull(result);
		assertEquals(1, result.length());

		// inner jar
		final JSONObject line = result.getJSONObject(0);
		assertEquals("xyz.kemix.bundle1", line.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("0.0.1", line.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("bundle1", line.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject myJarDetails = line.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		JSONObject obj18 = myJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(4, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(4, arr18.length());
		final List<Object> list18 = arr18.toList();
		assertTrue(list18.contains("xyz/kemix/test/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/p/after/Compiler18"));
		assertTrue(list18.contains("lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18"));
		assertTrue(list18.contains("lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18"));

		// 9
		JSONObject obj9 = myJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(2, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));

		JSONArray arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr9.length());
		final List<Object> list9 = arr9.toList();
		assertTrue(list9.contains("xyz/kemix/test/Compiler9"));
		assertTrue(list9.contains("lib/myjarlib.jar!xyz/kemix/test/Compiler9"));
	}

	@Test
	public void test_processFolder_bundleJarsAndClasses_SameFolder() throws IOException {
		File bundleFolder = new File(tempDir, "bundle1");
		File bundleLibFolder = new File(bundleFolder, "lib");
		File libFolder = new File(tempDir, "lib/myjarlib.jar");
		bundleLibFolder.mkdirs();
		FileUtils.copyFileToDirectory(libFolder, bundleLibFolder);

		File workingFolder = new File(tempDir, "working");
		FileUtils.copyDirectoryToDirectory(bundleFolder, workingFolder);

		File myJarFile = new File(tempDir, "myjar.jar");
		assertTrue(myJarFile.exists());
		FileUtils.copyFileToDirectory(myJarFile, workingFolder);

		File innerJarFile = new File(tempDir, "inner-jar.jar");
		assertTrue(innerJarFile.exists());
		FileUtils.copyFileToDirectory(innerJarFile, workingFolder);

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processFolder(workingFolder);

		/**
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":"bundle1","Details":{"Java
		 * 1.8":{"Sum":4,"Classes":["lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18","lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18","xyz/kemix/test/Compiler18","xyz/kemix/test/p/after/Compiler18"]},"Java
		 * 9":{"Sum":2,"Classes":["lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}},
		 * {"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":"inner-jar.jar","Details":{"Java
		 * 1.8":{"Sum":2,"Classes":["xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java
		 * 9":{"Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}},
		 * {"BundleName":"xyz.kemix.myjar","BundleVersion":"1.0.0","FilePath":"myjar.jar","Details":{"Java
		 * 1.8":{"Sum":1,"Classes":["xyz/kemix/test/p8/Compiler18"]},"Java
		 * 9":{"Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}}]
		 */
		doTest_processFolder_bundleJarsAndClasses(result, "");
	}

	private void doTest_processFolder_bundleJarsAndClasses(JSONArray result, String path) {
		assertNotNull(result);
		assertEquals(3, result.length());

		// bundle folder
		final JSONObject bundleFolderLine = result.getJSONObject(0);
		assertEquals("xyz.kemix.bundle1", bundleFolderLine.getString(ResultKeys.KEY_BUNDLE_NAME));
		assertEquals("0.0.1", bundleFolderLine.getString(ResultKeys.KEY_BUNDLE_VERSION));
		assertEquals("bundle1", bundleFolderLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject bundleFolderDetails = bundleFolderLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		JSONObject obj18 = bundleFolderDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(4, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));
		JSONArray arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(4, arr18.length());
		List<Object> list18 = arr18.toList();
		assertTrue(list18.contains("lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18"));
		assertTrue(list18.contains("lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/p/after/Compiler18"));

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
		assertEquals(path + "inner-jar.jar", innerJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject innerJarDetails = innerJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		obj18 = innerJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(2, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));
		arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(2, arr18.length());
		list18 = arr18.toList();
		assertTrue(list18.contains("xyz/kemix/test/clazz/Compiler18"));
		assertTrue(list18.contains("xyz/kemix/test/p8/Compiler18"));

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
		assertEquals(path + "myjar.jar", myJarLine.getString(ResultKeys.KEY_FILE_PATH));
		final JSONObject myJarDetails = myJarLine.getJSONObject(ResultKeys.KEY_DETAILS);

		// 1.8
		obj18 = myJarDetails.getJSONObject(CompilerVersion.JAVA_1_8.toString());
		assertEquals(1, obj18.getInt(ResultKeys.KEY_CLASSES_SUM));
		arr18 = obj18.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr18.length());
		assertTrue(arr18.toList().contains("xyz/kemix/test/p8/Compiler18"));

		// 9
		obj9 = myJarDetails.getJSONObject(CompilerVersion.JAVA_9.toString());
		assertEquals(1, obj9.getInt(ResultKeys.KEY_CLASSES_SUM));
		arr9 = obj9.getJSONArray(ResultKeys.KEY_CLASSES);
		assertEquals(1, arr9.length());
		assertTrue(arr9.toList().contains("xyz/kemix/test/Compiler9"));
	}

	@Test
	public void test_processFolder_bundleJarsAndClasses_diffFolder() throws IOException {
		File bundleFolder = new File(tempDir, "bundle1");
		File bundleLibFolder = new File(bundleFolder, "lib");
		File libFolder = new File(tempDir, "lib/myjarlib.jar");
		bundleLibFolder.mkdirs();
		FileUtils.copyFileToDirectory(libFolder, bundleLibFolder);

		File workingFolder = new File(tempDir, "working");
		FileUtils.copyDirectoryToDirectory(bundleFolder, workingFolder);

		File someFolder = new File(workingFolder, "some/other");
		File myJarFile = new File(tempDir, "myjar.jar");
		assertTrue(myJarFile.exists());
		FileUtils.copyFileToDirectory(myJarFile, someFolder);

		File innerJarFile = new File(tempDir, "inner-jar.jar");
		assertTrue(innerJarFile.exists());
		FileUtils.copyFileToDirectory(innerJarFile, someFolder);

		GeneralClassReporter reporter = new GeneralClassReporterTestClass(CompilerVersion.JAVA_1_7, true);
		JSONArray result = reporter.processFolder(workingFolder);

		/**
		 * [{"BundleName":"xyz.kemix.bundle1","BundleVersion":"0.0.1","FilePath":"bundle1","Details":{"Java
		 * 1.8":{"Sum":4,"Classes":["lib/myjarlib.jar!xyz/kemix/test/clazz/Compiler18","lib/myjarlib.jar!xyz/kemix/test/p8/Compiler18","xyz/kemix/test/Compiler18","xyz/kemix/test/p/after/Compiler18"]},"Java
		 * 9":{"Sum":2,"Classes":["lib/myjarlib.jar!xyz/kemix/test/Compiler9","xyz/kemix/test/Compiler9"]}}},
		 * {"BundleName":"xyz.kemix.inner.jar","BundleVersion":"1.0.1","FilePath":"some/other/inner-jar.jar","Details":{"Java
		 * 1.8":{"Sum":2,"Classes":["xyz/kemix/test/clazz/Compiler18","xyz/kemix/test/p8/Compiler18"]},"Java
		 * 9":{"Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}},
		 * {"BundleName":"xyz.kemix.myjar","BundleVersion":"1.0.0","FilePath":"some/other/myjar.jar","Details":{"Java
		 * 1.8":{"Sum":1,"Classes":["xyz/kemix/test/p8/Compiler18"]},"Java
		 * 9":{"Sum":1,"Classes":["xyz/kemix/test/Compiler9"]}}}]
		 */
		doTest_processFolder_bundleJarsAndClasses(result, "some/other/");
	}

}
