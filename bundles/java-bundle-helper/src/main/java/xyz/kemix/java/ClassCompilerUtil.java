package xyz.kemix.java;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import xyz.kemix.java.io.FileExts;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public class ClassCompilerUtil {
	public static CompilerVersion getJavaVersion(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException();
		}
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IOException("Invalid file: " + file);
		}

		if (!FileExts.CLASS.of(file.getName())) {
			throw new IOException("The file is not java class:" + file);
		}

		BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

		return getJavaVersion(input);
	}

	public static CompilerVersion getJavaVersion(InputStream input) throws IOException {
		if (input == null) {
			throw new NullPointerException();
		}
		try {

			// [-54, -2, -70, -66, 0, 0, 0, 52]
			byte[] data = new byte[8];
			input.read(data, 0, 8);

			// also need check the class flag
			String flag = bytesToHexString(Arrays.copyOf(data, 4));
			if (!"CAFEBABE".equals(flag.toUpperCase())) {
				return null;
			}
			// int minor_version = (((int) data[4]) << 8) + data[5];
			int major_version = (((int) data[6]) << 8) + data[7];

			CompilerVersion compilerVersion = CompilerVersion.get(major_version);

			if (compilerVersion == null) {
				throw new RuntimeException("Don't support the latest compile verion :" + major_version);
			}
			return compilerVersion;
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

}
