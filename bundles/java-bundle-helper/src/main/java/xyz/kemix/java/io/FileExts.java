
package xyz.kemix.java.io;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public enum FileExts {
	XML, JAVA, CLASS, JSON,
	//
	JAR, ZIP, WAR, TAR, TAR_GZ("tar.gz"),
	//
	;
	private String realName;

	FileExts() {
		this(null);
	}

	FileExts(String name) {
		this.realName = name;
	}

	public String n() {
		if (realName != null) {
			return realName;
		}
		return name().toLowerCase();
	}

	public String ext() {
		return '.' + n();
	}

	public boolean of(String file) {
		return file != null && file.toLowerCase().endsWith(ext());
	}

	public static FileExts get(String file) {
		if (file != null) {
			for (FileExts fe : FileExts.values()) {
				if (file.toLowerCase().endsWith(fe.ext())) {
					return fe;
				}
			}
		}
		return null;
	}
}