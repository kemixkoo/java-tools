/**
 *  
 */
package xyz.kemix.java;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public enum CompilerVersion {
	JAVA_1_1("1.1"), //
	JAVA_1_2("1.2"), //
	JAVA_1_3("1.3"), //
	JAVA_1_4("1.4"), //
	JAVA_1_5("1.5"), //
	JAVA_1_6("1.6"), //
	JAVA_1_7("1.7"), //
	JAVA_1_8("1.8"), //
	JAVA_9("9"),//
	;
	private static final int INIT_VALUE = 45; // 1.1 ,02D

	private final String name;

	CompilerVersion(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public float getNum() {
		return Float.parseFloat(getName());
	}

	public int getValue() {
		return INIT_VALUE + ordinal();
	}

	public String toString() {
		return "Java" + ' ' + getName();
	}

	public static CompilerVersion get(String name) {
		for (CompilerVersion cv : CompilerVersion.values()) {
			if (name.equals(cv.getName())) {
				return cv;
			}
		}
		return null;
	}

	public static CompilerVersion get(float num) {
		for (CompilerVersion cv : CompilerVersion.values()) {
			if (Float.compare(num, cv.getNum()) == 0) {
				return cv;
			}
		}
		return null;
	}

	public static CompilerVersion get(int value) {
		for (CompilerVersion cv : CompilerVersion.values()) {
			if (value == cv.getValue()) {
				return cv;
			}
		}
		return null;
	}
}
