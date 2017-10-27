package xyz.kemix.java.bundle;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 * 
 * 
 *         Version identifier for bundles
 * 
 *         Version identifiers have four .
 *         <ol>
 *         <li>Major version. A non-negative integer.</li>
 *         <li>Minor version. A non-negative integer.</li>
 *         <li>Micro version. A non-negative integer.</li>
 *         <li>Qualifier. A text string. See {@code BundleVersion(String)} for
 *         the format of the qualifier string.</li>
 *         </ol>
 * 
 */
public class BundleVersion implements Comparable<BundleVersion> {

	private final int major;
	private final int minor;
	private final int micro;
	private final String qualifier;
	private static final String SEPARATOR = ".";
	private transient String versionString /* default to null */;
	private transient int hash /* default to 0 */;

	/**
	 * The empty version "0.0.0".
	 */
	public static final BundleVersion empty = new BundleVersion(0, 0, 0);

	public BundleVersion(int major, int minor, int micro) {
		this(major, minor, micro, null);
	}

	public BundleVersion(int major, int minor, int micro, String qualifier) {
		if (qualifier == null) {
			qualifier = "";
		}

		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.qualifier = qualifier;
		validate();
	}

	/**
	 * Creates a version identifier from the specified string.
	 * 
	 * It's same as the Version of OSGi.
	 * <p>
	 * Version string grammar:
	 * 
	 * <pre>
	 * version ::= major('.'minor('.'micro('.'qualifier)?)?)?
	 * major ::= digit+
	 * minor ::= digit+
	 * micro ::= digit+
	 * qualifier ::= (alpha|digit|'_'|'-')+
	 * digit ::= [0..9]
	 * alpha ::= [a..zA..Z]
	 * </pre>
	 * 
	 */
	public BundleVersion(String version) {
		int maj = 0;
		int min = 0;
		int mic = 0;
		String qual = "";

		try {
			StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
			maj = parseInt(st.nextToken(), version);

			if (st.hasMoreTokens()) { // minor
				st.nextToken(); // consume delimiter
				min = parseInt(st.nextToken(), version);

				if (st.hasMoreTokens()) { // micro
					st.nextToken(); // consume delimiter
					mic = parseInt(st.nextToken(), version);

					if (st.hasMoreTokens()) { // qualifier separator
						st.nextToken(); // consume delimiter
						qual = st.nextToken(""); // remaining string

						if (st.hasMoreTokens()) { // fail safe
							throw new IllegalArgumentException("invalid version \"" + version + "\": invalid format");
						}
					}
				}
			}
		} catch (NoSuchElementException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"invalid version \"" + version + "\": invalid format");
			iae.initCause(e);
			throw iae;
		}

		major = maj;
		minor = min;
		micro = mic;
		qualifier = qual;
		validate();
	}

	private static int parseInt(String value, String version) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"invalid version \"" + version + "\": non-numeric \"" + value + "\"");
			iae.initCause(e);
			throw iae;
		}
	}

	private void validate() {
		if (major < 0) {
			throw new IllegalArgumentException(
					"invalid version \"" + toString() + "\": negative number \"" + major + "\"");
		}
		if (minor < 0) {
			throw new IllegalArgumentException(
					"invalid version \"" + toString() + "\": negative number \"" + minor + "\"");
		}
		if (micro < 0) {
			throw new IllegalArgumentException(
					"invalid version \"" + toString() + "\": negative number \"" + micro + "\"");
		}
		for (char ch : qualifier.toCharArray()) {
			if (('A' <= ch) && (ch <= 'Z')) {
				continue;
			}
			if (('a' <= ch) && (ch <= 'z')) {
				continue;
			}
			if (('0' <= ch) && (ch <= '9')) {
				continue;
			}
			if ((ch == '_') || (ch == '-')) {
				continue;
			}
			throw new IllegalArgumentException(
					"invalid version \"" + toString() + "\": invalid qualifier \"" + qualifier + "\"");
		}
	}

	public static BundleVersion parseVersion(String version) {
		if (version == null) {
			return empty;
		}
		version = version.trim();
		if (version.length() == 0) {
			return empty;
		}

		return new BundleVersion(version);
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getMicro() {
		return micro;
	}

	public String getQualifier() {
		return qualifier;
	}

	@Override
	public String toString() {
		String s = versionString;
		if (s != null) {
			return s;
		}
		int q = qualifier.length();
		StringBuffer result = new StringBuffer(20 + q);
		result.append(major);
		result.append(SEPARATOR);
		result.append(minor);
		result.append(SEPARATOR);
		result.append(micro);
		if (q > 0) {
			result.append(SEPARATOR);
			result.append(qualifier);
		}
		return versionString = result.toString();
	}

	@Override
	public int hashCode() {
		int h = hash;
		if (h != 0) {
			return h;
		}
		h = 31 * 17;
		h = 31 * h + major;
		h = 31 * h + minor;
		h = 31 * h + micro;
		h = 31 * h + qualifier.hashCode();
		return hash = h;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) { // quicktest
			return true;
		}

		if (!(object instanceof BundleVersion)) {
			return false;
		}

		BundleVersion other = (BundleVersion) object;
		return (major == other.major) && (minor == other.minor) && (micro == other.micro)
				&& qualifier.equals(other.qualifier);
	}

	public int compareTo(BundleVersion other) {
		if (other == this) { // quicktest
			return 0;
		}

		int result = major - other.major;
		if (result != 0) {
			return result;
		}

		result = minor - other.minor;
		if (result != 0) {
			return result;
		}

		result = micro - other.micro;
		if (result != 0) {
			return result;
		}

		return qualifier.compareTo(other.qualifier);
	}

}
