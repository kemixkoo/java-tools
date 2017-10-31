/**
 *  
 */
package xyz.kemix.java;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-30
 *
 */
public class ClassCompilerUtilTest extends AbstractTester {
	@Test(expected = NullPointerException.class)
	public void test_getJavaVersion_nullFile() throws IOException {
		ClassCompilerUtil.getJavaVersion((File) null);
	}

	@Test(expected = FileNotFoundException.class)
	public void test_getJavaVersion_nonExistedFile() throws IOException {
		ClassCompilerUtil.getJavaVersion(new File("abc.txt"));
	}

	@Test(expected = IOException.class)
	public void test_getJavaVersion_notFile() throws IOException {
		File testFile = new File(tempDir, "abc");
		testFile.mkdirs();
		ClassCompilerUtil.getJavaVersion(testFile);
	}

	@Test(expected = IOException.class)
	public void test_getJavaVersion_notClass() throws IOException {
		File testFile = new File(tempDir, "abc.txt");
		testFile.createNewFile();
		ClassCompilerUtil.getJavaVersion(testFile);
	}

	@Test
	public void test_getJavaVersion_9() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_9, CompilerVersion.JAVA_9.getName());
	}

	@Test
	public void test_getJavaVersion_18() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_8);
	}

	@Test
	public void test_getJavaVersion_17() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_7);
	}

	@Test
	public void test_getJavaVersion_16() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_6);
	}

	@Test
	public void test_getJavaVersion_15() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_5);
	}

	// @Test
	public void test_getJavaVersion_14() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_4);
	}

	@Test
	public void test_getJavaVersion_13() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_3);
	}

	@Test
	public void test_getJavaVersion_12() throws IOException {
		doTestJavaVersion(CompilerVersion.JAVA_1_2);

	}

	private void doTestJavaVersion(CompilerVersion version) throws IOException {
		String fileSuffix = String.valueOf((int) (version.getNum() * 10));
		doTestJavaVersion(version, fileSuffix);
	}

	private void doTestJavaVersion(CompilerVersion version, String fileSuffix) throws IOException {
		InputStream resource = getResource("classcompiler/Compiler" + fileSuffix + ".clazz");
		CompilerVersion javaVersion = ClassCompilerUtil.getJavaVersion(resource);
		assertEquals(version, javaVersion);
	}

	// @Test
	public void list_CompilerVersions() throws IOException {
		File folder = new File(this.getClass().getResource("/classcompiler").getPath());
		File[] listFiles = folder.listFiles();
		Arrays.sort(listFiles);
		for (File f : listFiles) {
			FileInputStream fis = new FileInputStream(f);
			CompilerVersion javaVersion = ClassCompilerUtil.getJavaVersion(fis);
			System.out.println(f.getName() + "    === >" + javaVersion);
			fis.close();
		}
	}
}
