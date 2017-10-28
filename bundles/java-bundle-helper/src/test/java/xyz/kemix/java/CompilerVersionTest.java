package xyz.kemix.java;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *  
 */

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public class CompilerVersionTest {
	@Test
	public void test_getNum() {
		assertEquals(CompilerVersion.JAVA_1_1.getNum(), 1.1f, 0f);
		assertEquals(CompilerVersion.JAVA_1_7.getNum(), 1.7f, 0f);
		assertEquals(CompilerVersion.JAVA_1_8.getNum(), 1.8f, 0f);
		assertEquals(CompilerVersion.JAVA_9.getNum(), 9f, 0f);
	}

	@Test
	public void test_getValue() {
		assertEquals(CompilerVersion.JAVA_1_1.getValue(), 45);
		assertEquals(CompilerVersion.JAVA_1_7.getValue(), 51);
		assertEquals(CompilerVersion.JAVA_1_8.getValue(), 52);
		assertEquals(CompilerVersion.JAVA_9.getValue(), 53);
	}

	@Test
	public void test_get_name() {
		assertEquals(CompilerVersion.JAVA_1_1, CompilerVersion.get("1.1"));
		assertEquals(CompilerVersion.JAVA_1_2, CompilerVersion.get("1.2"));
		assertEquals(CompilerVersion.JAVA_1_3, CompilerVersion.get("1.3"));
		assertEquals(CompilerVersion.JAVA_1_4, CompilerVersion.get("1.4"));
		assertEquals(CompilerVersion.JAVA_1_5, CompilerVersion.get("1.5"));
		assertEquals(CompilerVersion.JAVA_1_6, CompilerVersion.get("1.6"));
		assertEquals(CompilerVersion.JAVA_1_7, CompilerVersion.get("1.7"));
		assertEquals(CompilerVersion.JAVA_1_8, CompilerVersion.get("1.8"));
		assertEquals(CompilerVersion.JAVA_9, CompilerVersion.get("9"));
	}

	@Test
	public void test_get_num() {
		assertEquals(CompilerVersion.JAVA_1_1, CompilerVersion.get(1.1f));
		assertEquals(CompilerVersion.JAVA_1_2, CompilerVersion.get(1.2f));
		assertEquals(CompilerVersion.JAVA_1_3, CompilerVersion.get(1.3f));
		assertEquals(CompilerVersion.JAVA_1_4, CompilerVersion.get(1.4f));
		assertEquals(CompilerVersion.JAVA_1_5, CompilerVersion.get(1.5f));
		assertEquals(CompilerVersion.JAVA_1_6, CompilerVersion.get(1.6f));
		assertEquals(CompilerVersion.JAVA_1_7, CompilerVersion.get(1.7f));
		assertEquals(CompilerVersion.JAVA_1_8, CompilerVersion.get(1.8f));
		assertEquals(CompilerVersion.JAVA_9, CompilerVersion.get(9f));
	}

	@Test
	public void test_get_value() {
		assertEquals(CompilerVersion.JAVA_1_1, CompilerVersion.get(45));
		assertEquals(CompilerVersion.JAVA_1_2, CompilerVersion.get(46));
		assertEquals(CompilerVersion.JAVA_1_3, CompilerVersion.get(47));
		assertEquals(CompilerVersion.JAVA_1_4, CompilerVersion.get(48));
		assertEquals(CompilerVersion.JAVA_1_5, CompilerVersion.get(49));
		assertEquals(CompilerVersion.JAVA_1_6, CompilerVersion.get(50));
		assertEquals(CompilerVersion.JAVA_1_7, CompilerVersion.get(51));
		assertEquals(CompilerVersion.JAVA_1_8, CompilerVersion.get(52));
		assertEquals(CompilerVersion.JAVA_9, CompilerVersion.get(53));
	}
}
